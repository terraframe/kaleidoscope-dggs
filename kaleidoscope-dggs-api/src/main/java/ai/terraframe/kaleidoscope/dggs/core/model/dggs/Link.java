package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link
{
  private String rel;

  private String href;

  private String title;

  @Override
  public String toString()
  {
    return "Link{" + "rel=" + rel + ", href=" + href + ", title=" + title + '}';
  }
}
