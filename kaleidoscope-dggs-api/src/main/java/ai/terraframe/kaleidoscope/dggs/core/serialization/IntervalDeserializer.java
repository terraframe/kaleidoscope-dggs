package ai.terraframe.kaleidoscope.dggs.core.serialization;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Interval;

public class IntervalDeserializer extends StdDeserializer<Interval>
{

  private static final long serialVersionUID = -904451118296177873L;

  public IntervalDeserializer()
  {
    this(null);
  }

  public IntervalDeserializer(Class<Interval> t)
  {
    super(t);
  }

  @Override
  public Interval deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
  {
    Interval interval = new Interval();

    JsonNode array = p.getCodec().readTree(p);

    Iterator<JsonNode> nodes = array.iterator();

    while (nodes.hasNext())
    {
      JsonNode node = nodes.next();

      // if (!node.isNull() && !node.isEmpty())
      {
        String text = node.asText();

        interval.add(parse(text));
      }
    }

    return interval;
  }

  public static Date parse(String text)
  {
    return Date.from(Instant.parse(text));
  }

  public static String format(Date date)
  {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

    return formatter.format(date);
  }

}
