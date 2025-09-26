package ai.terraframe.kaleidoscope.dggs.core.serialization;

import java.io.IOException;

import org.locationtech.jts.geom.Envelope;
import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EnvelopeSerializer extends StdSerializer<Envelope>
{

  private static final long serialVersionUID = -904451118296177873L;

  public EnvelopeSerializer()
  {
    this(null);
  }

  public EnvelopeSerializer(Class<Envelope> t)
  {
    super(t);
  }

  @Override
  public void serialize(Envelope value, JsonGenerator gen, SerializerProvider provider) throws IOException
  {
    try
    {
      if (value != null)
      {
        double[] array = new double[] { value.getMinX(), value.getMinY(), value.getMaxX(), value.getMaxY() };

        gen.writeArray(array, 0, array.length);
      }
    }
    catch (IOException e)
    {
      throw new JsonParseException(e);
    }
  }

}
