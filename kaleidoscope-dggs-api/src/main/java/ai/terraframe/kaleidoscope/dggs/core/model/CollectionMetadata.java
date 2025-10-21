package ai.terraframe.kaleidoscope.dggs.core.model;

import java.util.List;

import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionDggs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggrs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMetadata
{
  private String                    collectionId;

  private List<CollectionAttribute> attributes;

  private DggrsAndLinks             dggrses;

  private CollectionDggs            dggs;

  private Dggrs                     dggrs;

}
