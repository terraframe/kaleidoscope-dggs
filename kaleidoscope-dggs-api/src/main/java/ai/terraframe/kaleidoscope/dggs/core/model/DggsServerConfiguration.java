package ai.terraframe.kaleidoscope.dggs.core.model;

import java.util.function.Function;
import java.util.function.Supplier;

import org.locationtech.jts.geom.Envelope;

public class DggsServerConfiguration
{
  public static class Builder
  {
    private Function<Envelope, String> bbox;

    private Supplier<String>           zoneDepth;

    public Builder()
    {
      this.bbox = ( envelope -> {
        String params = "&bbox=" + envelope.getMinX();
        params += "&bbox=" + envelope.getMinY();
        params += "&bbox=" + envelope.getMaxX();
        params += "&bbox=" + envelope.getMaxY();

        return params;
      } );

      this.zoneDepth = null;
    }

    public Builder withBbox(Function<Envelope, String> bbox)
    {
      this.bbox = bbox;

      return this;
    }

    public Builder withZoneDepth(Supplier<String> zoneDepth)
    {
      this.zoneDepth = zoneDepth;

      return this;
    }

    public DggsServerConfiguration build()
    {
      DggsServerConfiguration configuration = new DggsServerConfiguration();
      configuration.setBbox(bbox);
      configuration.setZoneDepth(zoneDepth);

      return configuration;
    }
  }

  private Function<Envelope, String> bbox;

  private Supplier<String>           zoneDepth;

  public void setBbox(Function<Envelope, String> bbox)
  {
    this.bbox = bbox;
  }

  public void setZoneDepth(Supplier<String> zoneDepth)
  {
    this.zoneDepth = zoneDepth;
  }

  public String getBBoxParam(Envelope envelope)
  {
    return this.bbox.apply(envelope);
  }

  public boolean hasDefaultZoneDepth()
  {
    return this.zoneDepth != null;
  }

  public String getDefaultZoneDepth()
  {
    return this.zoneDepth.get();
  }
}
