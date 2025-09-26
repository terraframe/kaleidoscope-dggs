package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;

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
    // String inputText = "What is the elevation of Winnipeg?";
    //
    // Zones zones = this.service.zones(inputText);
    //
    // System.out.println(zones);

  }

}
