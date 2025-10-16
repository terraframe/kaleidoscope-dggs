package ai.terraframe.kaleidoscope.dggs.core.model.message;

import java.util.List;

import ai.terraframe.kaleidoscope.dggs.core.model.Location;
import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
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
  private String       toolUseId;

  private LocationPage page;

  public FeatureMessage(String toolUseId, List<Location> locations)
  {
    this.toolUseId = toolUseId;
    this.page = new LocationPage(locations, locations.size(), 0, locations.size());
  }

  @Override
  public Type getType()
  {
    return Type.FEATURES;
  }
}
