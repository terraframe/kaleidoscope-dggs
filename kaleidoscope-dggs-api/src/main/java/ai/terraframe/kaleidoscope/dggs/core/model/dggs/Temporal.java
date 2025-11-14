package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ai.terraframe.kaleidoscope.dggs.core.serialization.IntervalDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Temporal
{
  @JsonDeserialize(contentUsing = IntervalDeserializer.class)
  private List<Interval> interval;

  public String toDescription()
  {
    StringBuilder builder = new StringBuilder();
    this.interval.stream().map(Interval::toDescription).forEach(builder::append);

    return builder.toString();
  }
}
