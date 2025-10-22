package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Envelope;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionDggs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionQueryables;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggrs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;

@Service
@Primary
public class MockRemoteDggsService extends RemoteDggsService
{

  @Override
  public String html(Collection collection, Dggrs dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/response.html"))
    {
      StringBuilder textBuilder = new StringBuilder();

      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        int c = 0;
        while ( ( c = reader.read() ) != -1)
        {
          textBuilder.append((char) c);
        }
      }

      return textBuilder.toString();
    }
  }

  @Override
  public JsonArray geojson(Collection collection, Dggrs dggr, String zoneId, Integer zoneDepth, Date datetime, String fiilter) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/data.geojson"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

        String propertyName = object.keySet().stream().findFirst().orElseThrow(() -> new GenericRestException("Collection data does not contain any properties"));

        return object.get(propertyName).getAsJsonObject().get("features").getAsJsonArray();
      }
    }
  }

  @Override
  public Zones zones(Collection collection, Dggrs dggr, Integer zoneLevel, Envelope envelope, Date datetime) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/zones.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readerFor(Zones.class).readValue(reader);
      }
    }
  }

  @Override
  public DggsJsonData json(Collection collection, Dggrs dggrs, String zoneId, Integer zoneDepth, Date datetime, String filter) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/dggsjson.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readerFor(DggsJsonData.class).readValue(reader);
      }
    }

  }

  @Override
  public Map<String, CollectionsAndLinks> collections() throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/collections.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        CollectionsAndLinks value = mapper.readerFor(CollectionsAndLinks.class).readValue(reader);
        value.getCollections().forEach(collection -> collection.setUrl("http://127.0.0.1/test"));

        return Map.of("http://127.0.0.1/test", value);
      }
    }
  }

  @Override
  public DggrsAndLinks dggs(String baseUrl, String collectionId) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/dggs.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readerFor(DggrsAndLinks.class).readValue(reader);
      }
    }
  }

  @Override
  public CollectionDggs dggs(String baseUrl, String collectionId, String dggrsId) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/collection-dggs.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readerFor(CollectionDggs.class).readValue(reader);
      }
    }
  }

  @Override
  public Optional<CollectionQueryables> queryables(String baseUrl, String collectionId) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/queryables.json"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        ObjectMapper mapper = new ObjectMapper();

        return Optional.of(mapper.readerFor(CollectionQueryables.class).readValue(reader));
      }
    }
  }
}
