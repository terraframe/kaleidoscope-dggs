package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dggr
{
  private String     collectionId;

  private String     id;

  private String     title;

  private String     uri;

  private List<Link> links;
}