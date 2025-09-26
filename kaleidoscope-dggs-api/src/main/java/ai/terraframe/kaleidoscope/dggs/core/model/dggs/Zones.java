package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zones
{
  private List<LinkTemplate> linkTemplates;

  private List<Link>         links;

  private List<String>       zones;

  private Double             returnedAreaMetersSquare;

  @Override
  public String toString()
  {
    return "Zones{" + "linkTemplates=" + linkTemplates + ", links=" + links + ", zones=" + zones + ",returnedAreaMetersSquare=" + returnedAreaMetersSquare + '}';
  }
}