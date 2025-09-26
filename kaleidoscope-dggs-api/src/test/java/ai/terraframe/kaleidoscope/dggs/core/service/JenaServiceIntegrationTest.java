package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class JenaServiceIntegrationTest
{
  @Autowired
  private JenaService service;

  @Test
  public void testGetName() throws InterruptedException, ExecutionException, TimeoutException
  {
    LocationPage page = this.service.fullTextLookup("Winnipeg");

    Assert.assertEquals(2, page.getCount());

    Location location = page.getLocations().get(0);
    Geometry geometry = location.getGeometry();
    Envelope envelopeInternal = geometry.getEnvelopeInternal();

    System.out.println(envelopeInternal);
  }

}
