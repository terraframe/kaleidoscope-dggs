package ai.terraframe.kaleidoscope.dggs.core.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionQueryables
{

  private String                    collectionId;

  private List<CollectionAttribute> attributes;
}
