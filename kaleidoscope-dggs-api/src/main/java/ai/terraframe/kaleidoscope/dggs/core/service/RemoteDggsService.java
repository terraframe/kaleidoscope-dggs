package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;
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
import ai.terraframe.kaleidoscope.dggs.core.model.RemoteDggsException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionDggs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionQueryables;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggrs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Interval;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Temporal;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Zones;
import ai.terraframe.kaleidoscope.dggs.core.serialization.IntervalDeserializer;

@Service
public class RemoteDggsService implements RemoteDggsServiceIF
{
  private static final Logger       log = LoggerFactory.getLogger(RemoteDggsService.class);

  @Autowired
  private AppProperties             properties;

  private Map<String, Zones>        zones;

  private Map<String, DggsJsonData> dggsjson;

  public RemoteDggsService()
  {
    this.zones = Collections.synchronizedMap(new LRUMap<String, Zones>(10));
    this.dggsjson = Collections.synchronizedMap(new LRUMap<String, DggsJsonData>(10));
  }

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

      log.trace("Remote request: [" + request.toString() + "]");

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
        this.throwErrorResponse(request, response);
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

    log.trace("Remote request: [" + request.toString() + "]");

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

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public CollectionDggs dggs(String baseUrl, String collectionId, String dggrsId) throws IOException, InterruptedException
  {
    String url = baseUrl + "/collections/" + collectionId + "/dggs/" + dggrsId;

    String params = "f=json";

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    log.trace("Remote request: [" + request.toString() + "]");

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();
      CollectionDggs value = mapper.readerFor(CollectionDggs.class).readValue(body);

      return value;
    }

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public Optional<CollectionQueryables> queryables(String baseUrl, String collectionId) throws IOException, InterruptedException
  {
    String url = baseUrl + "/collections/" + collectionId + "/queryables";

    String params = "f=json";

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    log.trace("Remote request: [" + request.toString() + "]");

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();
      return Optional.of(mapper.readerFor(CollectionQueryables.class).readValue(body));
    }
    else if (response.statusCode() == 204)
    {
      return Optional.empty();
    }

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public Zones zones(Collection collection, Dggrs dggr, Integer zoneLevel, Location location, Date datetime) throws IOException, InterruptedException
  {
    Geometry geometry = location.getGeometry();
    Envelope envelope = geometry.getEnvelopeInternal();

    return zones(collection, dggr, zoneLevel, envelope, datetime);
  }

  @Override
  public Zones zones(Collection collection, Dggrs dggr, Integer zoneLevel, Envelope envelope, Date datetime) throws IOException, InterruptedException
  {
    String baseUrl = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggr.getId() + "/zones";

    String params = "f=json";
    params += "&bbox=" + envelope.getMinX();
    params += "&bbox=" + envelope.getMinY();
    params += "&bbox=" + envelope.getMaxX();
    params += "&bbox=" + envelope.getMaxY();
    params += "&bbox-crs=" + URLEncoder.encode("https://www.opengis.net/def/crs/EPSG/0/4326", "UTF-8");
    params = resolveDatetimeParameter(collection, datetime, params);

    if (zoneLevel != null)
    {
      params += "&zone-level=" + zoneLevel;
    }

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(baseUrl + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    String url = request.toString();

    if (this.zones.containsKey(url))
    {
      return this.zones.get(url);
    }

    log.trace("Remote request: [" + url + "]");

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();

      Zones value = mapper.readerFor(Zones.class).readValue(body);

      this.zones.put(url, value);

      return value;
    }

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public JsonArray geojson(Collection collection, Dggrs dggrs, String zoneId, Integer zoneDepth, Date datetime, String filter) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String url = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggrs.getId() + "/zones/" + zoneId + "/data";

    String params = "f=geojson";
    params += "&geometry=zone-region";
    params = resolveDatetimeParameter(collection, datetime, params);

    if (zoneDepth != null)
    {
      params += "&zone-depth=" + zoneDepth;
    }

    if (filter != null)
    {
      params += "&filter=" + URLEncoder.encode(filter, "UTF-8");
      params += "&filter-lang=cql2-text";
    }

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url + "?" + params)) //
        .header("Content-Type", "application/geo+json") //
        .GET().build();

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    log.trace("Remote request: [" + request.toString() + "]");

    if (response.statusCode() == 200)
    {
      JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

      String propertyName = object.keySet().stream().findFirst().orElseThrow(() -> new GenericRestException("Collection data does not contain any properties"));

      return object.get(propertyName).getAsJsonObject().get("features").getAsJsonArray();
    }

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public DggsJsonData json(Collection collection, Dggrs dggrs, String zoneId, Integer zoneDepth, Date datetime, String filter) throws IOException, InterruptedException
  {
    // https://ogc-dggs-testing.fmecloud.com/api/collections/winnipeg-dem/dggs/ISEA3H/zones/G0-51FC9-A/data?f=html&zone-depth=7

    String baseUrl = collection.getUrl() + "/collections/" + collection.getId() + "/dggs/" + dggrs.getId() + "/zones/" + zoneId + "/data";

    String params = "f=json";
    params = resolveDatetimeParameter(collection, datetime, params);

    if (zoneDepth != null)
    {
      params += "&zone-depth=" + zoneDepth;
    }

    if (filter != null)
    {
      params += "&filter=" + URLEncoder.encode(filter, "UTF-8");
      params += "&filter-lang=cql2-text";
    }

    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(baseUrl + "?" + params)) //
        .header("Content-Type", "application/json") //
        .GET().build();

    String url = request.toString();

    if (this.dggsjson.containsKey(url))
    {
      return this.dggsjson.get(url);
    }

    log.trace("Remote request: [" + url + "]");

    HttpClient client = HttpClient.newHttpClient();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200)
    {
      String body = response.body();

      ObjectMapper mapper = new ObjectMapper();

      DggsJsonData value = mapper.readerFor(DggsJsonData.class).readValue(body);

      // Cache the response
      this.dggsjson.put(url, value);

      return value;
    }

    this.throwErrorResponse(request, response);

    return null;
  }

  @Override
  public String html(Collection collection, Dggrs dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
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

    this.throwErrorResponse(request, response);

    return null;
  }

  private void throwErrorResponse(HttpRequest request, HttpResponse<String> response)
  {
    String body = response.body();

    log.error("Error for DGGS request[" + request.toString() + "]: " + response.statusCode());

    throw new RemoteDggsException(body, response.statusCode());
  }

  private String resolveDatetimeParameter(Collection collection, Date datetime, String params) throws UnsupportedEncodingException
  {
    Temporal temporal = collection.getExtent().getTemporal();

    if (temporal != null && temporal.getInterval().size() > 0)
    {
      if (datetime != null)
      {
        // Validate the date time is valid for the given collection
        if (!temporal.getInterval().stream().anyMatch(i -> i.contains(datetime)))
        {
          throw new GenericRestException("Give date [" + IntervalDeserializer.format(datetime) + "] is outside of the date range of the collection");
        }

        return ( params += "&datetime=" + URLEncoder.encode(IntervalDeserializer.format(datetime), "UTF-8") );
      }

      // No date provided in the question, default to the start date of the
      // first interval
      Interval interval = temporal.getInterval().get(0);

      List<Date> dates = interval.getDates();

      if (dates.size() > 0)
      {
        return ( params += "&datetime=" + URLEncoder.encode(IntervalDeserializer.format(dates.get(dates.size() - 1)), "UTF-8") );
      }
    }

    // Collection does not contain temporal restrictions. Do no include the date
    // parameter
    return params;
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
