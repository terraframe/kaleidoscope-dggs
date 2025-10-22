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
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage.MessageType;
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage.SenderRole;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class BedrockConverseServiceIntegrationTest
{
  @Autowired
  private BedrockConverseService service;

  @Test
  public void testNameResolution() throws InterruptedException, ExecutionException, TimeoutException
  {
    List<ClientMessage> messages = Arrays.asList(new ClientMessage("What is the elevation of Winnipeg?"));

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.NAME_RESOLUTION, toolUse.getName());
    Assert.assertEquals("Winnipeg", toolUse.getParameters().get("locationName").asString());
  }

  @Test
  public void testLocationInformation() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK2Pd27KVg";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("What is the elevation of Winnipeg?"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.LOCATION_DATA, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
    Assert.assertFalse(toolUse.getParameters().containsKey("date"));
  }

  @Test
  public void testLocationInformationWithTime() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK2Pd27KVg";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("What is the elevation of Winnipeg on 2020-01-17?"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.LOCATION_DATA, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
    Assert.assertTrue(toolUse.getParameters().containsKey("date"));
    Assert.assertEquals("2020-01-17T00:00:00Z", toolUse.getParameters().get("date").asString());
  }

  @Test
  public void testLocationInformationWithFilter() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK2Pd27KVg";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("Give me the flood data of Winnipeg where the water level is greater than 10.5"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.LOCATION_DATA, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
    Assert.assertTrue(toolUse.getParameters().containsKey("filter"));
    Assert.assertEquals("waterlevel > 10.5", toolUse.getParameters().get("filter").asString());
  }
  
  @Test
  public void testLocationInformationWithBadFilter() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK2Pd27KVg";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("Give me the elevation data of Winnipeg where the color is blue"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.INFORMATION, message.getType());
  }



  @Test
  public void testLocationInformationWithZoneDepth() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK2Pd27KVg";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("Give me the elevation data of Winnipeg at a zone depth of 12"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.LOCATION_DATA, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
    Assert.assertTrue(toolUse.getParameters().containsKey("zone-depth"));
    Assert.assertEquals(12, toolUse.getParameters().get("zone-depth").asNumber().intValue());
  }

  @Test
  public void testPowerInfrastructure() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK234534";

    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("Give me the power infrastructure using the elevation collection of Winnipeg"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
    );

    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));

    BedrockResponse message = this.service.execute(collections, messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);

    Assert.assertEquals(BedrockConverseService.POWER_INFRASTRUCTURE, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
  }
  
  @Test
  public void testConnectedDissementationAreas() throws InterruptedException, ExecutionException, TimeoutException
  {
    String uri = "terraframe.ai#Subdivision-44101234";
    String toolUseId = "tooluse_vLS7L15WRsSbSK234534";
    
    List<ClientMessage> messages = Arrays.asList( //
        new ClientMessage("Analyze for the connected dissementation areas using the elevation collection of Winnipeg"), //
        new ClientMessage(SenderRole.SYSTEM, MessageType.NAME_RESOLUTION, "There are multiple locations").put("toolUseId", toolUseId).put("locationName", "Winnipeg"), //
        new ClientMessage(SenderRole.USER, MessageType.LOCATION_RESOLVED, "44101234").put("toolUseId", toolUseId).put("uri", uri) //
        );
    
    List<Collection> collections = Arrays.asList(new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>()));
    
    BedrockResponse message = this.service.execute(collections, messages);
    
    Assert.assertNotNull(message);
    Assert.assertEquals(BedrockResponse.Type.TOOL_USE, message.getType());
    ToolUseResponse toolUse = message.asType(ToolUseResponse.class);
    
    Assert.assertEquals(BedrockConverseService.DISSEMINATION_AREAS, toolUse.getName());
    Assert.assertEquals(uri, toolUse.getParameters().get("uri").asString());
    Assert.assertEquals("winnipeg-dem", toolUse.getParameters().get("category").asString());
  }
}
