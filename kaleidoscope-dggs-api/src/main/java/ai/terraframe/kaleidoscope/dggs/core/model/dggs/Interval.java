package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interval
{
  private List<Date> dates;

  public Interval()
  {
    this.dates = new LinkedList<>();
  }

  public void add(Date date)
  {
    this.dates.add(date);
  }
}
