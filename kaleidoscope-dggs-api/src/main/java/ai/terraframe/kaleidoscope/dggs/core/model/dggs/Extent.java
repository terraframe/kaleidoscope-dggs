package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Extent
{
  private Spatial  spatial;

  private Temporal temporal;

}
