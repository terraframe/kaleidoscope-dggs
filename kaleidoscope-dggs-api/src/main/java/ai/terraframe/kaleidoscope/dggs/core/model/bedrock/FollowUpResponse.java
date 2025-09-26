package ai.terraframe.kaleidoscope.dggs.core.model.bedrock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FollowUpResponse extends BedrockResponse
{
  private String content;

  @Override
  public Type getType()
  {
    return Type.FOLLOW_UP;
  }
}
