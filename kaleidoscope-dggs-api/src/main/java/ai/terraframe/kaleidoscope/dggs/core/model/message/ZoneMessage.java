package ai.terraframe.kaleidoscope.dggs.core.model.message;

import ai.terraframe.kaleidoscope.dggs.core.model.ZoneCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ZoneMessage extends Message
{
  private ZoneCollection collection;

  @Override
  public Type getType()
  {
    return Type.ZONES;
  }

}
