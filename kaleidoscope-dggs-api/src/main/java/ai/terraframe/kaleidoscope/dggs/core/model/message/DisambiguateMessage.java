package ai.terraframe.kaleidoscope.dggs.core.model.message;

import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DisambiguateMessage extends Message
{
  private String       toolUseId;

  private String       locationName;

  private LocationPage page;

  @Override
  public Type getType()
  {
    return Type.DISAMBIGUATE;
  }
}
