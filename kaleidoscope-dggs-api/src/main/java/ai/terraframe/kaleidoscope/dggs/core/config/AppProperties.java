package ai.terraframe.kaleidoscope.dggs.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;

@Service
public class AppProperties
{
  @Autowired
  private Environment env;

  public String getModel()
  {
    return env.getProperty("bedrock.model", "us.anthropic.claude-3-7-sonnet-20250219-v1:0");
  }

  public Region getRegion()
  {
    return Region.of(env.getProperty("bedrock.region"));
  }

  public String getAccessKeyId()
  {
    return env.getProperty("access.key.id");
  }

  public String getSecretAccessKey()
  {
    return env.getProperty("secret.access.key");
  }

  public String getJenaUrl()
  {
    return env.getProperty("jena.url");
  }

  public String[] getDggsUrls()
  {
    return env.getProperty("dggs.urls", "https://ogc-dggs-testing.fmecloud.com/api/dggs").split(",");
  }
}
