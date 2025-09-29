package ai.terraframe.kaleidoscope.dggs.core.model.message;

public abstract class Message
{
  public static enum Type {
    ZONES, DISAMBIGUATE, BASIC
  }

  public abstract Type getType();

  @SuppressWarnings("unchecked")
  public <T extends Message> T asType(Class<T> clazz)
  {
    return (T) this;
  }

}
