package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import ai.terraframe.kaleidoscope.dggs.core.config.AppProperties;

@Service
public class RemoteDggsService implements RemoteDggsServiceIF
{
  private static final int    MAX_TIMEOUT_MINUTES = 5;

  private static final Logger log                 = LoggerFactory.getLogger(RemoteDggsService.class);

  @Autowired
  private AppProperties       properties;

  @Override
  public JsonArray data(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
  {
    // TODO: Change to calling the API with the geojson format and
    // application/geo+json content type when the backend
    // supports those functions
    String body = this.html(collectionId, dggrsId, zoneId, zoneDepth);

    // HACK parse the geojson out of the html
    return parseHTML(body);
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

}
