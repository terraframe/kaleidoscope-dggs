package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;
import ai.terraframe.kaleidoscope.dggs.core.service.RemoteDggsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class RemoteDggsServiceTest
{
  @Autowired
  private RemoteDggsService service;

  @Test
  public void testData() throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/dggs/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    JsonArray data = this.service.data("winnipeg-dem", "ISEA3H", "G0-51FC9-A", 7);

    System.out.println(data.toString());
  }
}
