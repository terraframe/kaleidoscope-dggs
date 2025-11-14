package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link
{
  private String  href;

  private String  rel;

  private String  type;

  private String  hreflang;

  private String  title;

  private Integer length;
}
