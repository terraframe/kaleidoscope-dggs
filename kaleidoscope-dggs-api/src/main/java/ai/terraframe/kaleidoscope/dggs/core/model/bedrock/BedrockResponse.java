package ai.terraframe.kaleidoscope.dggs.core.model.bedrock;

public abstract class BedrockResponse
{
  public static enum Type {
    TOOL_USE, INFORMATION
  }

  public abstract Type getType();

  @SuppressWarnings("unchecked")
  public <T extends BedrockResponse> T asType(Class<T> clazz)
  {
    return (T) this;
  }
}
