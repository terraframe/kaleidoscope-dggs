package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MockRemoteDggsService extends RemoteDggsService
{

  @Override
  public String html(String collectionId, String dggrsId, String zoneId, Integer zoneDepth) throws IOException, InterruptedException
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
}
