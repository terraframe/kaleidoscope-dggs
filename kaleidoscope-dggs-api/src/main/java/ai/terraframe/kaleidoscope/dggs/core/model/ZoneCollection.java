package ai.terraframe.kaleidoscope.dggs.core.model;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.JsonArray;

import ai.terraframe.kaleidoscope.dggs.core.serialization.EnvelopeSerializer;
import ai.terraframe.kaleidoscope.dggs.core.serialization.JsonArraySerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneCollection
{
  @JsonSerialize(using = EnvelopeSerializer.class)
  private Envelope  bbox;

  @JsonSerialize(using = JsonArraySerializer.class)
  private JsonArray features;
}
