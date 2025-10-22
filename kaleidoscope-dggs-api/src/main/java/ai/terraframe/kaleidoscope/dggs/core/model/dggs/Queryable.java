package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Queryable
{
  @JsonAlias("x-ogc-role")
  private String xOgcRole;

  private String format;

  private String description;

  private String title;

  private String type;
}
