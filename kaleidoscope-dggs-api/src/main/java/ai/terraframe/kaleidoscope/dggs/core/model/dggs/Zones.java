package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Zones
{
  private List<LinkTemplate> linkTemplates;

  private List<Link>         links;

  private List<String>       zones;

  private Double             returnedAreaMetersSquare;
}