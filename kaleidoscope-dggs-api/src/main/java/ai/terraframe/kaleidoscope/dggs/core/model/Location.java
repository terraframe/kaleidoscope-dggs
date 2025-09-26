package ai.terraframe.kaleidoscope.dggs.core.model;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ai.terraframe.kaleidoscope.dggs.core.serialization.GeometrySerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Location
{
  private Map<String, Object> properties = new HashMap<>();

  private String              type       = "Feature";

  private String              id;

  @JsonSerialize(using = GeometrySerializer.class)
  private Geometry            geometry;

  public Location(String uri, String code, String type, String label, Geometry geometry)
  {
    this.id = uri;
    this.geometry = geometry;

    this.properties.put("uri", uri);
    this.properties.put("type", type);
    this.properties.put("code", code);
    this.properties.put("label", label);
  }

  public void addProperty(String name, Object value)
  {
    this.properties.put(name, value);
  }

}
