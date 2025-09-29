package ai.terraframe.kaleidoscope.dggs.core.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class BasicMessage extends Message
{
  private String content;

  @Override
  public Type getType()
  {
    return Type.BASIC;
  }

}
