package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collection
{
  private String     url;

  private String     id;

  private String     title;

  private String     description;

  private Double     minCellSize;

  private Extent     extent;

  private List<Link> links;
}
