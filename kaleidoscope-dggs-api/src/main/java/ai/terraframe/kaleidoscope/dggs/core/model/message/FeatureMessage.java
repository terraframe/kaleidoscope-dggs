package ai.terraframe.kaleidoscope.dggs.core.model.message;

import java.util.Collection;
import java.util.List;

import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FeatureMessage extends Message
{
  private String             toolUseId;

  private List<DggsJsonData> zones;

  private LocationPage       page;

  private Integer            population;

  public FeatureMessage(String toolUseId, List<DggsJsonData> zones, Collection<Location> locations)
  {
    this.toolUseId = toolUseId;
    this.zones = zones;
    this.page = new LocationPage(locations, locations.size(), 0, locations.size());
  }

  public FeatureMessage(String toolUseId, List<DggsJsonData> zones, Collection<Location> locations, Integer population)
  {
    this(toolUseId, zones, locations);

    this.population = population;
  }

  @Override
  public Type getType()
  {
    return Type.FEATURES;
  }
}
