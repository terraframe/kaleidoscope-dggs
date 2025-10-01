package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.Arrays;
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
import ai.terraframe.kaleidoscope.dggs.core.model.message.ClientMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.DisambiguateMessage;
import ai.terraframe.kaleidoscope.dggs.core.model.message.Message;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class ChatServiceIntegrationTest
{
  @Autowired
  private ChatService service;

  @Test
  public void testGetName() throws InterruptedException, ExecutionException, TimeoutException
  {
    List<ClientMessage> messages = Arrays.asList(new ClientMessage("What is the elevation of Winnipeg?"));

    Message message = this.service.query(messages);

    Assert.assertNotNull(message);
    Assert.assertEquals(Message.Type.DISAMBIGUATE, message.getType());
    DisambiguateMessage disambiguate = message.asType(DisambiguateMessage.class);

    Assert.assertNotNull(disambiguate.getToolUseId());
    Assert.assertEquals("Winnipeg", disambiguate.getLocationName());
    Assert.assertTrue(disambiguate.getPage().getCount() > 1);
  }

}
