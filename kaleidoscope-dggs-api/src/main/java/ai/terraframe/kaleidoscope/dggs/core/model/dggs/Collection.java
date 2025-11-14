package ai.terraframe.kaleidoscope.dggs.core.model.dggs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class Collection
{
  private String     url;

  private String     id;

  private String     title;

  private String     description;

  private Double     minCellSize;

  private Extent     extent;

  private List<Link> links;
}
