package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryableProperties
{
  private Map<String, Queryable> properties;

  public QueryableProperties()
  {
    this.properties = new HashMap<>();
  }

  @JsonAnySetter
  public void addDynamicProperty(String key, Queryable value)
  {
    this.properties.put(key, value);
  }

}
