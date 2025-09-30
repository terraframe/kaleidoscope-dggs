package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.BedrockResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.bedrock.ToolUseResponse;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Extent;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class BedrockConverseServiceIntegrationTest
{
  @Autowired
  private BedrockConverseService service;

  @Test
  public void testGetName() throws InterruptedException, ExecutionException, TimeoutException
  {
    String inputText = "What is the elevation of Winnipeg?";

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.getLocationFromText(collections, inputText);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    Assert.assertEquals("Winnipeg", message.asType(ToolUseResponse.class).getLocationName());
    Assert.assertEquals("winnipeg-dem", message.asType(ToolUseResponse.class).getCategory());
    Assert.assertNull(message.asType(ToolUseResponse.class).getDate());
  }

  @Test
  public void testGetNameWithTime() throws InterruptedException, ExecutionException, TimeoutException
  {
    String inputText = "What is the elevation of Winnipeg on 2020-01-17?";

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.getLocationFromText(collections, inputText);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    Assert.assertEquals("Winnipeg", message.asType(ToolUseResponse.class).getLocationName());
    Assert.assertEquals("winnipeg-dem", message.asType(ToolUseResponse.class).getCategory());
    Assert.assertNotNull(message.asType(ToolUseResponse.class).getDate());
  }
}
