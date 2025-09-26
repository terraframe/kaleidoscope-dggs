package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkTemplate
{
  private String uriTemplate;

  private String rel;

  private String title;

  @Override
  public String toString()
  {
    return "LinkTemplate{" + "uriTemplate=" + uriTemplate + ", rel=" + rel + ", title=" + title + '}';
  }
}