package ai.terraframe.kaleidoscope.dggs.core.service;

import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.DggsServerConfiguration;

@Service
public class DggsServerConfigurationService
{
  private static final String GEO_INSIGHT = "https://dggs.geoinsight.ai";

  public DggsServerConfiguration get(String baseUrl)
  {
    if (baseUrl.equals(GEO_INSIGHT))
    {
      return new DggsServerConfiguration.Builder() //
          .withBbox(envelope -> {
            return "&bbox=" + envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY();
          }) //
          .withZoneDepth(() -> {
            return "5";
          }) //
          .build();
    }

    return new DggsServerConfiguration.Builder().build();
  }

  public String getBBoxParam(String url, Envelope envelope)
  {
    return this.get(url).getBBoxParam(envelope);
  }

  public boolean hasDefaultZoneDepth(String url)
  {
    return this.get(url).hasDefaultZoneDepth();
  }

  public String getDefaultZoneDepth(String url)
  {
    return this.get(url).getDefaultZoneDepth();
  }
}
