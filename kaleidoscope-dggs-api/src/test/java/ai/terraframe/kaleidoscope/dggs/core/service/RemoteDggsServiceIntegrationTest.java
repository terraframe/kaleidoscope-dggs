package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.config.TestConfiguration;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Extent;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestConfiguration.class)
@AutoConfigureMockMvc
public class RemoteDggsServiceIntegrationTest
{
  @Autowired
  private RemoteDggsServiceIF service;

  @Test
  public void testData() throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/dggs/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=3
    Collection collection = mockCollection();
    Dggr dggr = mockDggr(collection);

    JsonArray data = this.service.data(collection, dggr, "G0-51FC9-A", 3);

    Assert.assertEquals(37, data.size());

  }

  @Test
  public void testZones() throws IOException, InterruptedException
  {
    Collection collection = mockCollection();
    Dggr dggr = mockDggr(collection);
    Envelope envelope = new Envelope(-97.34912125, -96.95599652, 49.71363749, 49.9938672);

    Zones response = this.service.zones(collection, dggr, 9, envelope);

    System.out.println(response);

    // Assert.assertEquals(37, data.size());

  }

  @Test
  public void testDeserializeCollection() throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/collections.json"))
    {

      ObjectMapper mapper = new ObjectMapper();

      CollectionsAndLinks value = mapper.readerFor(CollectionsAndLinks.class).readValue(istream);

      Assert.assertNotNull(value);
    }

  }

  private Dggr mockDggr(Collection collection)
  {
    return new Dggr(collection.getId(), "ISEA3H", "", "", new LinkedList<>());
  }

  private Collection mockCollection()
  {
    return new Collection("https://ogc-dggs-testing.fmecloud.com/api", "winnipeg-dem", "Test", "Elevation of winnipeg", 0.01D, new Extent(), new LinkedList<>());
  }

}
