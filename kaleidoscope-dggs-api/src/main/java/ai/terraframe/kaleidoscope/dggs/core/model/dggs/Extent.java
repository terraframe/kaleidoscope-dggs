package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Extent
{
  private Spatial  spatial;

  private Temporal temporal;

}
