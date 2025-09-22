package ai.terraframe.kaleidoscope.dggs.web.model;

import java.util.List;
import java.util.Map;

import ai.terraframe.kaleidoscope.dggs.core.model.Style;
import ai.terraframe.kaleidoscope.dggs.core.model.VectorLayer;
import lombok.Data;

@Data
public class Configuration
{
  private Map<String, Style> styles;

  private List<VectorLayer>  layers;

  private String             token;

}
