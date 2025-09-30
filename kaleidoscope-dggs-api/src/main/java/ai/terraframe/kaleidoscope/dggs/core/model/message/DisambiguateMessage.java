package ai.terraframe.kaleidoscope.dggs.core.model.message;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import ai.terraframe.kaleidoscope.dggs.core.model.LocationPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class DisambiguateMessage extends Message
{
  private LocationPage page;

  private String       category;

  @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private Date         datetime;

  @Override
  public Type getType()
  {
    return Type.DISAMBIGUATE;
  }
}
