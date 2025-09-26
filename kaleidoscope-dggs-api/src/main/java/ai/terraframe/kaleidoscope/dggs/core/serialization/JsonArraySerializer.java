package ai.terraframe.kaleidoscope.dggs.core.serialization;

import java.io.IOException;

import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.gson.JsonArray;

public class JsonArraySerializer extends StdSerializer<JsonArray>
{

  private static final long serialVersionUID = -904451118296177873L;

  public JsonArraySerializer()
  {
    this(null);
  }

  public JsonArraySerializer(Class<JsonArray> t)
  {
    super(t);
  }

  @Override
  public void serialize(JsonArray value, JsonGenerator gen, SerializerProvider provider) throws IOException
  {
    try
    {
      if (value != null)
      {
        gen.writeRawValue(value.toString());
      }
    }
    catch (IOException e)
    {
      throw new JsonParseException(e);
    }
  }

}
