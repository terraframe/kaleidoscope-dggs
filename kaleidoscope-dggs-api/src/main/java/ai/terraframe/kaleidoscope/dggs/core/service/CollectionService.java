package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;

@Service
public class CollectionService
{
  @Autowired
  private RemoteDggsServiceIF     service;

  private Map<String, Collection> cache;

  public CollectionService()
  {
    this.cache = null;
  }

  public synchronized Map<String, Collection> getCache()
  {
    if (this.cache == null)
    {
      this.cache = Collections.synchronizedMap(new LRUMap<String, Collection>(20));

      try
      {
        Map<String, CollectionsAndLinks> map = this.service.collections();

        for (CollectionsAndLinks v : map.values())
        {
          v.getCollections().forEach(c -> this.cache.put(c.getId(), c));
        }
      }
      catch (InterruptedException | IOException e)
      {
        throw new GenericRestException("Unable to get collections from DGGS server");
      }

    }

    return cache;
  }

  public Optional<Collection> get(String id)
  {
    return Optional.ofNullable(this.getCache().get(id));
  }

  public Collection getOrThrow(String id)
  {
    return Optional.ofNullable(this.getCache().get(id)).orElseThrow(() -> new GenericRestException("Unable to find collection with id [" + id + "]"));
  }

  public List<Collection> getAll()
  {
    return new LinkedList<>(this.getCache().values());
  }

}
