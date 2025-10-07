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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ai.terraframe.kaleidoscope.dggs.core.config.AppProperties;
import ai.terraframe.kaleidoscope.dggs.core.model.CollectionAttribute;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.BedrockResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.InformationResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.ToolUseResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Temporal;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage.MessageType;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage.SenderRole;
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
import software.amazon.awssdk.services.bedrockruntime.model.ToolUseBlock;

@Service
public class BedrockConverseService
{
  public static final String  LOCATION_DATA       = "Location_Data";

  public static final String  NAME_RESOLUTION     = "Name_Resolution";

  private static final int    MAX_TIMEOUT_MINUTES = 5;

  private static final Logger log                 = LoggerFactory.getLogger(BedrockConverseService.class);

  @Autowired
  private AppProperties       properties;

  @Autowired
  private MetadataService     service;

  public Tool getDataToolSpec()
  {
    HashMap<String, Document> properties = new HashMap<>();
    properties.put("uri", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "The uri of the location") //
        .build());
    properties.put("category", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "The subject category") //
        .build());
    properties.put("date", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("format", "date-time") //
        .putString("description", "Optional date in ISO 8601 format (e.g., 2025-09-30T00:00:00Z)") //
        .build());
    properties.put("filter", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "Optional filter criteria. For example elevation > 2.3)") //
        .build());
    properties.put("zone-depth", Document.mapBuilder() //
        .putString("type", "integer") //
        .putString("description", "Optional zone depth in which to get the data)") //
        .build());

    return Tool.fromToolSpec(ToolSpecification.builder() //
        .name(LOCATION_DATA) //
        .description("Get the information for a location uri and category.") //
        .inputSchema(schema -> schema.json(Document.mapBuilder() //
            .putString("type", "object") //
            .putMap("properties", properties) //
            .putList("required", List.of(Document.fromString("uri"), Document.fromString("category"))) //
            .build()))
        .build());
  }

  public Tool getLocationToolSpec()
  {
    HashMap<String, Document> properties = new HashMap<>();
    properties.put("locationName", Document.mapBuilder() //
        .putString("type", "string") //
        .putString("description", "Name of the location") //
        .build());

    return Tool.fromToolSpec(ToolSpecification.builder() //
        .name(NAME_RESOLUTION) //
        .description("Resolves a location name to its uri.") //
        .inputSchema(schema -> schema.json(Document.mapBuilder() //
            .putString("type", "object") //
            .putMap("properties", properties) //
            .putList("required", List.of(Document.fromString("locationName"))) //
            .build()))
        .build());
  }

  public BedrockResponse execute(List<Collection> collections, List<ClientMessage> messages)
  {
    try (BedrockRuntimeAsyncClient client = getClient())
    {
      StringBuilder systemPrompt = new StringBuilder("""
          You are a location analysis assistant that provides the location information based on a user question.
          The user is going to ask a question about a location.  Categorize the subject of the question as one of the
          following data collection options:\n\n""");

      collections.forEach(collection -> {

        StringBuilder builder = new StringBuilder();
        builder.append("'" + collection.getId() + "' - " + collection.getDescription() + "\n");

        Temporal temporal = collection.getExtent().getTemporal();

        if (temporal != null && temporal.getInterval().size() > 0)
        {
          builder.append(" -- The collection has the following time intervals: " + temporal.toDescription() + "\n");
        }

        List<CollectionAttribute> attributes = this.service.getAttributes(collection);
        
        if(attributes.size() > 0) {
          builder.append(" -- The collection supports the following attributes for filtering: \n" + StringUtils.join(attributes.stream().map(a -> a.getName() + " - " + a.getDescription()).toList(), ","));         
        }

        systemPrompt.append(builder + "\n");
      });

      systemPrompt.append("""

          Use the 'Name_Resolution' to resolve a location name to its location uri. If you can determine
          the subject category and a location uri then use the 'Location_Data' tool. Otherwise ask follow-up
          questions to determine the subject category and location uir. If the subject is not one of the
          options then tell the user that you do not have any data for that subject. When using the 'Location_Data'
          tool the user can additionally specify filter criteria using for the 'filter' attribute for the defined
          attributes above.  The format of the filter criteria should be generated as CQL2 Text.  The following are
          examples of CQL2 filters:

          - Filter Features by Attribute
          ```
          population > 1000000
          ```

          - Combine Filters with Logical Operators
          ```
          population > 1000000 AND area < 500
          ```

          - Temporal Filter
          ```
          datetime BETWEEN 2023-01-01 AND 2023-12-31
          ```

          - String Matching
          ```
          name LIKE 'New%'
          ```

          """);

      SystemContentBlock system = SystemContentBlock.fromText(systemPrompt.toString());

      List<Message> bedrockMessages = messages.stream().map(message -> {
        return Message.builder() //
            .content(ContentBlock.fromText(convertClientMessage(message))) //
            .role(message.getRole().equals(SenderRole.USER) ? ConversationRole.USER : ConversationRole.ASSISTANT).build();
      }).toList();

      String modelId = "us.anthropic.claude-3-7-sonnet-20250219-v1:0";

      CompletableFuture<ConverseResponse> request = client.converse(params -> params //
          .modelId(modelId) //
          .system(system) //
          .toolConfig(config -> config.tools(getDataToolSpec(), getLocationToolSpec())) //
          .messages(bedrockMessages) //
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
              Message message = response.output().message();
              List<ContentBlock> content = message.content();

              ContentBlock toolResponse = content.stream() //
                  .filter(block -> block.type().equals(ContentBlock.Type.TOOL_USE)) //
                  .findFirst().orElseThrow();

              ToolUseBlock toolUse = toolResponse.toolUse();
              Map<String, Document> map = toolUse.input().asMap();

              ToolUseResponse toolUseResponse = new ToolUseResponse(content.toString(), toolUse.name(), toolUse.toolUseId(), map);

              future.complete(toolUseResponse);
            }
            else
            {
              String text = response.output().message().content().get(0).text();

              future.complete(new InformationResponse(text));
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

  public String convertClientMessage(ClientMessage message)
  {
    if (message.getMessageType().equals(MessageType.NAME_RESOLUTION))
    {
      Map<String, Object> data = message.getData();

      String toolUseId = (String) data.get("toolUseId");
      String locationName = (String) data.get("locationName");

      return "ToolUseBlock(ToolUseId=" + toolUseId + ", Name=Name_Resolution, Input={\"locationName\": \"" + locationName + "\"})";
    }
    else if (message.getMessageType().equals(MessageType.LOCATION_RESOLVED))
    {
      Map<String, Object> data = message.getData();

      String toolUseId = (String) data.get("toolUseId");
      String uri = (String) data.get("uri");

      JsonArray toolResults = new JsonArray();

      JsonObject toolResult = new JsonObject();
      toolResult.addProperty("toolUseId", toolUseId);
      toolResult.addProperty("content", "[{\"json\": {'uri': '" + uri + "'}}]");

      toolResults.add(toolResult);

      return toolResults.toString();
    }

    return message.getText();
  }

}