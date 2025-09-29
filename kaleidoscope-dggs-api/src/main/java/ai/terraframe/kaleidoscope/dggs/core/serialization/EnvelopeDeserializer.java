package ai.terraframe.kaleidoscope.dggs.core.serialization;

import java.io.IOException;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class EnvelopeDeserializer extends StdDeserializer<Envelope>
{

  private static final long serialVersionUID = -904451118296177873L;

  public EnvelopeDeserializer()
  {
    this(null);
  }

  public EnvelopeDeserializer(Class<Envelope> t)
  {
    super(t);
  }

  @Override
  public Envelope deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
  {
    JsonNode node = p.getCodec().readTree(p);    
    
    // p.readValueAsTree().toString()

    double minX = Double.parseDouble(node.get(0).asText());
    double minY = Double.parseDouble(node.get(1).asText());
    double maxX = Double.parseDouble(node.get(2).asText());
    double maxY = Double.parseDouble(node.get(3).asText());

    return new Envelope(minX, maxX, minY, maxY);
  }

}
