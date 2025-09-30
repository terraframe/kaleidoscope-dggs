package ai.terraframe.kaleidoscope.dggs.core.model.bedrock;

import java.util.Date;

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

  private Date   date;

  public ToolUseResponse(String locationName, String category)
  {
    this.locationName = locationName;
    this.category = category;
  }

  @Override
  public Type getType()
  {
    return Type.TOOL_USE;
  }
}
