package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;

@Service
public class RemoteDggsService implements RemoteDggsServiceIF
{
  private static final int    MAX_TIMEOUT_MINUTES = 5;

  private static final Logger log                 = LoggerFactory.getLogger(RemoteDggsService.class);

  @Autowired
  private AppProperties       properties;

  @Override
  public Zones zones(String collectionId, String dggrsId, Integer zoneLevel, Location location) throws IOException, InterruptedException
  {
    Geometry geometry = location.getGeometry();
    Envelope envelope = geometry.getEnvelopeInternal();

    return zones(collectionId, dggrsId, zoneLevel, envelope);
  }

  @Override
  public Zones zones(String collectionId, String dggrsId, Integer zoneLevel, Envelope envelope) throws IOException, InterruptedException
  {
    String url = this.properties.getDggsUrl() + "/dggs/collections/" + collectionId + "/dggs/" + dggrsId + "/zones";

    String params = "f=json";
    params += "&zone-level=" + zoneLevel;
    params += "&bbox=" + envelope.getMinX();
    params += "&bbox=" + envelope.getMinY();
    params += "&bbox=" + envelope.getMaxX();
    params += "&bbox=" + envelope.getMaxY();
    params += "&bbox-crs=" + "https://www.opengis.net/def/crs/EPSG/0/4326";

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

    // TODO: Handle error message
    throw new RuntimeException(response.body());
  }

  @Override
  public JsonArray data(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/dggs/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String url = this.properties.getDggsUrl() + "/dggs/collections/" + collectionId + "/dggs/" + dggrsId + "/zones/" + zoneId + "/data";

    String params = "f=geojson";
    params += "&zone-depth=" + zoneDepth;
    params += "&geometry=zone-region";
    

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/geo+json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();
    
     // TODO : determine what property to use 
    return object.get("elevation").getAsJsonObject().get("features").getAsJsonArray();
  }

  @Override
  public String html(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/dggs/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String url = this.properties.getDggsUrl() + "/dggs/collections/" + collectionId + "/dggs/" + dggrsId + "/zones/" + zoneId + "/data";
    String params = "f=html&zone-depth=" + zoneDepth;

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    return response.body();
  }

  private JsonArray parseHTML(String body)
  {
    int startIndex = body.indexOf("L.geoJson(");
    int endIndex = body.indexOf("onEachFeature") - 32;

    String substring = body.substring(startIndex, endIndex);
    substring = substring.replace("L.geoJson(", "");

    return JsonParser.parseString(substring).getAsJsonArray();
  }

  @Override
  public String getCollectionId(String category)
  {
    if (category.equalsIgnoreCase("elevation"))
    {
      return "winnipeg-dem";
    }
    else if (category.equalsIgnoreCase("temperature"))
    {
      return "manitoba-tm";
    }
    else if (category.equalsIgnoreCase("precipitation"))
    {
      return "manitoba-pr";
    }

    throw new UnsupportedOperationException("Unknown category [" + category + "]");
  }

}
