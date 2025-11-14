package ai.terraframe.kaleidoscope.dggs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionAttribute
{
  private String name;

  private String description;

  private String type;
}
