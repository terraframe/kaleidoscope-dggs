package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.List;

import org.geotools.api.feature.simple.SimpleFeature;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class DggalServiceTest
{
  @Autowired
  private RemoteDggsService dggs;

  @Autowired
  private DggalService      service;

  @Test
  public void testGetFeatures() throws IOException, InterruptedException
  {
    DggsJsonData json = this.dggs.json(null, null, null, null, null, null);

    List<SimpleFeature> features = this.service.dggsjsonToFeatures(json);

    Assert.assertEquals(271, features.size());

  }
}
