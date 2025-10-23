package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.Style;
import ai.terraframe.kaleidoscope.dggs.core.model.VectorLayer;

@Service
public class ConfigurationService
{

  public Map<String, Style> getStyles() throws IOException, ParseException
  {
    return new HashMap<String, Style>();
  }

  public List<VectorLayer> getVectorLayers() throws IOException, ParseException
  {
    return new LinkedList<VectorLayer>();
  }

}
