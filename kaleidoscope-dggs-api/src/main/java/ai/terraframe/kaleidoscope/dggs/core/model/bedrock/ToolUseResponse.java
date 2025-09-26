package ai.terraframe.kaleidoscope.dggs.core.model.bedrock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ToolUseResponse extends BedrockResponse
{
  private String locationName;

  private String category;

  @Override
  public Type getType()
  {
    return Type.TOOL_USE;
  }
}
