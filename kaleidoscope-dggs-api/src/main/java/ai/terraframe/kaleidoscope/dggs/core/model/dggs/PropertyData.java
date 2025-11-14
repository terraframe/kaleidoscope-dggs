package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyData
{
  private String       depth;

  private Shape        shape;

  private List<Object> data;

}
