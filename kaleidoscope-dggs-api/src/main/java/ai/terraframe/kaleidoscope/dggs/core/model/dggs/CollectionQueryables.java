package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionQueryables
{
  private String              description;

  private Boolean             additionalProperties;

  private String              title;

  private String              type;

  private QueryableProperties properties;
}
