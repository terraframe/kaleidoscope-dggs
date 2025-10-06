package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.CollectionAttribute;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;

@Service
public class MetadataService
{

  public List<CollectionAttribute> getAttributes(Collection collection)
  {
    List<CollectionAttribute> list = new LinkedList<CollectionAttribute>();

    if (collection.getId().equals("winnipeg-dem"))
    {
      list.add(new CollectionAttribute("elevation", "Elevation of the terrain"));
    }

    return list;
  }

}
