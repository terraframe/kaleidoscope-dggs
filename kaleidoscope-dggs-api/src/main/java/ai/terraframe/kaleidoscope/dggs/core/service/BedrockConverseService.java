/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.terraframe.kaleidoscope.dggs.core.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.config.AppProperties;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.BedrockResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.FollowUpResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.ToolUseResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.StopReason;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.Tool;
import software.amazon.awssdk.services.bedrockruntime.model.ToolSpecification;

@Service
public class BedrockConverseService
{
  private static final int    MAX_TIMEOUT_MINUTES = 5;

  private static final Logger log                 = LoggerFactory.getLogger(BedrockConverseService.class);

  @Autowired
  private AppProperties       properties;

  public ToolSpecification getToolSpec()
  {
    HashMap<String, Document> properties = new HashMap<>();
    properties.put("locationName", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "The name of the location.") //
        .build());
    properties.put("category", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "The subject category") //
        .build());

    return ToolSpecification.builder() //
        .name("Location_Bounds") //
        .description("Get the bounding box from a location name.") //
        .inputSchema(schema -> schema.json(Document.mapBuilder() //
            .putString("type", "object") //
            .putMap("properties", properties) //
            .putList("required", List.of(Document.fromString("locationName"))) //
            .build()))
        .build();
  }

  public BedrockResponse getLocationFromText(List<Collection> collections, String inputText)
  {
    try (BedrockRuntimeAsyncClient client = getClient())
    {
      StringBuilder systemPrompt = new StringBuilder("""
          You are a location analysis assistant that provides the location and subject cateogry base on a user question.
          The user is going to ask a question about a location.  Categorize the subject of the question as one of the
          following options:\n\n""");

      collections.forEach(collection -> {
        systemPrompt.append("'" + collection.getId() + "' - " + collection.getDescription() + "\n");
      });

      systemPrompt.append("""
              
          If you can determine the subject category and a location name then use the 'Location_Bounds' tool.
          Otherwise ask follow-up questions to determine the subject category and location name. If the subject
          is not one of the options then tell the user that you do not have any data for that subject.
          """);

      SystemContentBlock system = SystemContentBlock.fromText(systemPrompt.toString());

      Message message = Message.builder() //
          .content(ContentBlock.fromText(inputText)) //
          .role(ConversationRole.USER).build();

      String modelId = "us.anthropic.claude-3-7-sonnet-20250219-v1:0";

      CompletableFuture<ConverseResponse> request = client.converse(params -> params //
          .modelId(modelId) //
          .system(system) //
          .toolConfig(config -> config.tools(Tool.fromToolSpec(getToolSpec()))) //
          .messages(message) //
          .inferenceConfig(config -> config //
              .maxTokens(512) //
              .temperature(0.5F) //
              .topP(0.9F)));

      // Prepare a future object to handle the asynchronous response.
      CompletableFuture<BedrockResponse> future = new CompletableFuture<BedrockResponse>();

      // Handle the response or error using the future object.
      request.whenComplete((response, error) -> {

        try
        {
          if (error == null)
          {
            // Extract the generated text from Bedrock's response object.
            if (StopReason.TOOL_USE.equals(response.stopReason()))
            {
              ContentBlock toolResponse = response.output().message().content().stream() //
                  .filter(block -> block.type().equals(ContentBlock.Type.TOOL_USE)) //
                  .findFirst().orElseThrow();

              Map<String, Document> map = toolResponse.toolUse().input().asMap();
              String locationName = map.get("locationName").asString();
              String category = map.get("category").asString();

              future.complete(new ToolUseResponse(locationName, category));
            }
            else
            {
              String text = response.output().message().content().get(0).text();

              future.complete(new FollowUpResponse(text));
            }
          }
          else
          {
            future.completeExceptionally(error);
          }
        }
        catch (Exception e)
        {
          future.completeExceptionally(e);
        }
      });

      try
      {
        // Wait for the future object to complete and retrieve the generated
        // text.
        return future.get(MAX_TIMEOUT_MINUTES, TimeUnit.MINUTES);

      }
      catch (ExecutionException | InterruptedException | TimeoutException e)
      {
        System.err.printf("Can't invoke '%s': %s", modelId, e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  private BedrockRuntimeAsyncClient getClient()
  {
    final Duration sdkTimeout = Duration.ofMinutes(MAX_TIMEOUT_MINUTES);
    final Duration nettyReadTimeout = Duration.ofMinutes(MAX_TIMEOUT_MINUTES);

    AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey());

    return BedrockRuntimeAsyncClient.builder() //
        .region(properties.getRegion()) //
        .credentialsProvider(StaticCredentialsProvider.create(credentials)) //
        .httpClientBuilder(NettyNioAsyncHttpClient.builder() //
            .readTimeout(nettyReadTimeout)) //
        .overrideConfiguration(cfg -> {
          cfg.apiCallTimeout(sdkTimeout);
          cfg.apiCallAttemptTimeout(sdkTimeout);
        }).build();
  }

}