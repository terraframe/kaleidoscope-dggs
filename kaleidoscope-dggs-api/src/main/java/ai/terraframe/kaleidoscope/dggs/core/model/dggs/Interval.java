package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ai.terraframe.kaleidoscope.dggs.core.serialization.IntervalDeserializer;
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

  public String toDescription()
  {
    if (dates.size() > 0)
    {
      if (dates.size() == 1)
      {
        return IntervalDeserializer.format(dates.get(0));
      }

      if (dates.size() == 2)
      {
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);

        return IntervalDeserializer.format(startDate) + " - " + IntervalDeserializer.format(endDate);
      }

      throw new UnsupportedOperationException("Unabled to determine range with more than two entries");
    }

    return "";

  }

  public boolean contains(Date datetime)
  {
    if (dates.size() > 0)
    {
      if (dates.size() == 1)
      {
        Date startDate = dates.get(0);

        return ( startDate.before(datetime) || startDate.equals(datetime) );
      }

      if (dates.size() == 2)
      {
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);

        return ( startDate.before(datetime) || startDate.equals(datetime) ) && ( endDate.after(datetime) || endDate.equals(datetime) );
      }

      throw new UnsupportedOperationException("Unabled to determine range with more than two entries");
    }

    return false;
  }
}
