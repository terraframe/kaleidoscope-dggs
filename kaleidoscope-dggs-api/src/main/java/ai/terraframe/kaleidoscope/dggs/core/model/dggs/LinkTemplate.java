package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkTemplate
{
  private String uriTemplate;

  private String rel;

  private String title;

}