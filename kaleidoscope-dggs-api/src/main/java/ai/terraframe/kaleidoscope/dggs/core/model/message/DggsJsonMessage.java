package ai.terraframe.kaleidoscope.dggs.core.model.message;

import java.util.List;

import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggsJsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DggsJsonMessage extends Message
{
  private List<DggsJsonData> zones;

  @Override
  public Type getType()
  {
    return Type.DGGS_JSON;
  }

}
