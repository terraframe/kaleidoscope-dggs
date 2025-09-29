package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionsAndLinks
{
  private List<Link>       links;

  private List<Collection> collections;

}
