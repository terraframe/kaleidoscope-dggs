package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ai.terraframe.kaleidoscope.dggs.core.serialization.EnvelopeDeserializer;
import ai.terraframe.kaleidoscope.dggs.core.serialization.EnvelopeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Spatial
{

  @JsonSerialize(using = EnvelopeSerializer.class)
  @JsonDeserialize(contentUsing = EnvelopeDeserializer.class)
  private List<Envelope> bbox;
}
