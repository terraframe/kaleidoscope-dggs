package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DggsJsonData
{
  private String            dggrs;

  private List<PropertyMap> values;

  private List<String>      depths;

  private String            zoneId;

}
