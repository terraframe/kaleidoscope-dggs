package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDggs
{
  private List<LinkTemplate> linkTemplates;

  private Integer            defaultDepth;

  private String             description;

  private List<Link>         links;

  private String             id;

  private String             title;

  private String             uri;

  private Integer            maxRefinementLevel;

}
