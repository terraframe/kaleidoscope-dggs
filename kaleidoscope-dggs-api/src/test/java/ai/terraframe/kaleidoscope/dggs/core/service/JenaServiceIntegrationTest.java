package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
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
  public void testFullTextLookup() throws InterruptedException, ExecutionException, TimeoutException
  {
    LocationPage page = this.service.fullTextLookup("Winnipeg");

    Assert.assertEquals(2, page.getCount());

    Location location = page.getLocations().get(0);
    Geometry geometry = location.getGeometry();

    Assert.assertNotNull(geometry);
    Assert.assertEquals("http://terraframe.ai#Subdivision-4611040", location.getId());
    Assert.assertEquals("http://terraframe.ai#Subdivision-4611040", location.getProperties().get("uri"));
    Assert.assertEquals("http://terraframe.ai#Subdivision", location.getProperties().get("type"));
    Assert.assertEquals("4611040", location.getProperties().get("code"));
    Assert.assertEquals("Winnipeg", location.getProperties().get("label"));
    
    Geometry envelope = geometry.getEnvelope();
    
    System.out.println(envelope.toText());

    Envelope envelopeInternal = geometry.getEnvelopeInternal();

    System.out.println(envelopeInternal);
  }

  @Test
  public void testGetLocation() throws InterruptedException, ExecutionException, TimeoutException
  {
    Location location = this.service.getLocation("http://terraframe.ai#Subdivision-4611040");

    Assert.assertNotNull(location);
    Assert.assertNotNull(location.getGeometry());
  }

  @Test
  public void testWithinBox() throws InterruptedException, ExecutionException, TimeoutException
  {
    // -97.34912125 : -96.95599652, 49.71363749 : 49.9938672
    
    String wkt = "POLYGON((-97.449031 49.613756, -97.449031 50.093865, -96.855997 50.093865, -96.855997 49.613756, -97.449031 49.613756))";
    
    WKTReader reader = WKTReader.extract(wkt);
    Geometry envelope = reader.getGeometry();

    List<Location> locations = this.service.getWithinEnvelope(envelope, "http://terraframe.ai#Subdivision");

    Assert.assertEquals(3, locations.size());
  }

}
