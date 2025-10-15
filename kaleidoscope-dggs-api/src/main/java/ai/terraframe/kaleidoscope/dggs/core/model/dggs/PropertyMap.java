package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class PropertyMap
{
  private Map<String, List<PropertyData>> properties;

  public PropertyMap()
  {
    this.properties = new HashMap<>();
  }

  @JsonAnySetter
  public void addDynamicProperty(String key, List<PropertyData> value)
  {
    this.properties.put(key, value);
  }
}
