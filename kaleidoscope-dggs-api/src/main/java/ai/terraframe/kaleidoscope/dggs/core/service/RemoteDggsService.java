package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ai.terraframe.kaleidoscope.dggs.core.config.AppProperties;
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Interval;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Temporal;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;
import ai.terraframe.kaleidoscope.dggs.core.serialization.IntervalDeserializer;

@Service
public class RemoteDggsService implements RemoteDggsServiceIF
{
  private static final Logger log = LoggerFactory.getLogger(RemoteDggsService.class);

  @Autowired
  private AppProperties       properties;

  @Override
  public Map<String, CollectionsAndLinks> collections() throws IOException, InterruptedException
  {
    HashMap<String, CollectionsAndLinks> map = new HashMap<String, CollectionsAndLinks>();

    for (String baseUrl : this.properties.getDggsUrls())
    {

      String url = baseUrl + "/collections";

      String params = "f=json";

      HttpRequest request = HttpRequest.newBuilder() //
          .uri(URI.create(url + "?" + params)) //
          .header("Content-Type", "application/json") //
          .GET().build();

      HttpClient client = HttpClient.newHttpClient();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200)
      {
        String body = response.body();

        ObjectMapper mapper = new ObjectMapper();

        CollectionsAndLinks value = mapper.readerFor(CollectionsAndLinks.class).readValue(body);
        value.getCollections().forEach(collection -> collection.setUrl(baseUrl));

        map.put(baseUrl, value);
      }
      else
      {
        String body = response.body();

        System.out.println("Error for request[" + request.toString() + "]");

        // TODO: Handle error message
        throw new RuntimeException(body);
      }
    }

    return map;
  }

  @Override
  public DggrsAndLinks dggs(String baseUrl, String collectionId) throws IOException, InterruptedException
  {
    String url = baseUrl + "/collections/" + collectionId + "/dggs/";

    String params = "f=json";

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();
      DggrsAndLinks value = mapper.readerFor(DggrsAndLinks.class).readValue(body);
      value.getDggrs().stream().forEach(dggr -> dggr.setCollectionId(collectionId));

      return value;
    }

    String body = response.body();

    System.out.println("Error for request[" + request.toString() + "]");

    // TODO: Handle error message
    throw new RuntimeException(body);
  }

  @Override
  public Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Location location) throws IOException, InterruptedException
  {
    Geometry geometry = location.getGeometry();
    Envelope envelope = geometry.getEnvelopeInternal();

    return zones(collection, dggr, zoneLevel, envelope);
  }

  @Override
  public Zones zones(Collection collection, Dggr dggr, Integer zoneLevel, Envelope envelope) throws IOException, InterruptedException
  {
    String url = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggr.getId() + "/zones";

    String params = "f=json";
    params += "&zone-level=" + zoneLevel;
    params += "&bbox=" + envelope.getMinX();
    params += "&bbox=" + envelope.getMinY();
    params += "&bbox=" + envelope.getMaxX();
    params += "&bbox=" + envelope.getMaxY();
    params += "&bbox-crs=" + URLEncoder.encode("https://www.opengis.net/def/crs/EPSG/0/4326", "UTF-8");

    Temporal temporal = collection.getExtent().getTemporal();

    if (temporal != null && temporal.getInterval().size() > 0)
    {
      Interval interval = temporal.getInterval().get(0);

      List<Date> dates = interval.getDates();

      if (dates.size() > 0)
      {
        params += "&dateTime=" + URLEncoder.encode(IntervalDeserializer.format(dates.get(0)), "UTF-8");
      }
    }

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();

      return mapper.readerFor(Zones.class).readValue(body);
    }

    String body = response.body();

    System.out.println("Error for request[" + request.toString() + "]");

    // TODO: Handle error message
    throw new RuntimeException(body);
  }

  @Override
  public JsonArray data(Collection collection, Dggr dggrs, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String url = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggrs.getId() + "/zones/" + zoneId + "/data";

    String params = "f=geojson";
    params += "&zone-depth=" + zoneDepth;
    params += "&geometry=zone-region";

    Temporal temporal = collection.getExtent().getTemporal();

    // Do you need to include dateTime for temporal data
    if (temporal != null && temporal.getInterval().size() > 0)
    {
      Interval interval = temporal.getInterval().get(0);

      List<Date> dates = interval.getDates();

      if (dates.size() > 0)
      {
        params += "&dateTime=" + URLEncoder.encode(IntervalDeserializer.format(dates.get(0)), "UTF-8");
      }
    }

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/geo+json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

      // TODO : determine what property to use
      String propertyName = object.keySet().stream().findFirst().orElseThrow(() -> new GenericRestException("Collection data does not contain any properties"));

      return object.get(propertyName).getAsJsonObject().get("features").getAsJsonArray();
    }

    String body = response.body();

    System.out.println("Error for request[" + request.toString() + "]");

    // TODO: Handle error message
    throw new RuntimeException(body);
  }

  @Override
  public String html(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String url = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggr.getId() + "/zones/" + zoneId + "/data";
    String params = "f=html&zone-depth=" + zoneDepth;

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      return response.body();
    }

    String body = response.body();

    System.out.println("Error for request[" + request.toString() + "]");

    // TODO: Handle error message
    throw new RuntimeException(body);
  }

  public static JsonArray htmlToGeojson(String html)
  {
    int startIndex = html.indexOf("L.geoJson(");
    int endIndex = html.indexOf("onEachFeature") - 32;

    String substring = html.substring(startIndex, endIndex);
    substring = substring.replace("L.geoJson(", "");

    return JsonParser.parseString(substring).getAsJsonArray();
  }

}
