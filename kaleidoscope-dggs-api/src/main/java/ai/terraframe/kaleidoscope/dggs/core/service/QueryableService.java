package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.cache.SupplierLRUCache;
import ai.terraframe.kaleidoscope.dggs.core.model.CollectionAttribute;
import ai.terraframe.kaleidoscope.dggs.core.model.CollectionQueryables;
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;

@Service
public class QueryableService
{

  private SupplierLRUCache<String, CollectionQueryables> cache;

  public QueryableService()
  {
    this.cache = new SupplierLRUCache<String, CollectionQueryables>((v) -> {
      return new String[] { v.getCollectionId() };
    }, 20);
  }

  public Optional<List<CollectionAttribute>> get(String collectionId)
  {
    return this.cache.get(collectionId, () -> {

      List<CollectionAttribute> list = new LinkedList<CollectionAttribute>();

      if (collectionId.equals("winnipeg-dem"))
      {
        list.add(new CollectionAttribute("elevation", "Elevation of the terrain"));
      }

      return Optional.ofNullable(new CollectionQueryables(collectionId, list));
    }).map(c -> c.getAttributes());
  }

  public List<CollectionAttribute> getOrThrow(String collectionId)
  {
    return this.get(collectionId).orElseThrow(() -> new GenericRestException("Unable to find querayable information for the collection [" + collectionId + "]"));
  }

}
