package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;

@Service
@Primary
public class MockRemoteDggsService extends RemoteDggsService
{

  @Override
  public String html(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
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
  public JsonArray data(Collection collection, Dggr dggr, String zoneId, Integer zoneDepth, Date datetime, String fiilter) throws IOException, InterruptedException
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/data.geojson"))
    {
      try (Reader reader = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8)))
      {
        return JsonParser.parseReader(reader).getAsJsonArray();
      }
    }
  }
}
