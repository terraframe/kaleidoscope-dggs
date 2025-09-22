package ai.terraframe.kaleidoscope.dggs.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest
{
  private String query;

  private int    limit  = 1000;

  private int    offset = 0;
}
