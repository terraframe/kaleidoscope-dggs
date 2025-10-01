package ai.terraframe.kaleidoscope.dggs.core.model.bedrock;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.core.document.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ToolUseResponse extends BedrockResponse
{
  private String                content;

  private String                name;

  private String                toolUseId;

  private Map<String, Document> parameters;

  @Override
  public Type getType()
  {
    return Type.TOOL_USE;
  }
}
