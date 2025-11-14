package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Extent;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Spatial;

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
      // Manitoba bbox = -102.216797,48.487486,-88.857422,59.977005
      Envelope envelope = new Envelope(-102.216797, -88.857422, 48.487486, 59.977005);

      this.cache = Collections.synchronizedMap(new LRUMap<String, Collection>(20));

      try
      {
        Map<String, CollectionsAndLinks> map = this.service.collections();

        for (CollectionsAndLinks v : map.values())
        {
          // Filter the collections to only those that have data in manitoba
          v.getCollections().stream().filter(c -> {

            Extent extent = c.getExtent();

            if (extent != null)
            {
              Spatial spatial = extent.getSpatial();

              if (spatial != null)
              {
                List<Envelope> list = spatial.getBbox();

                for (Envelope bbox : list)
                {
                  if (bbox.intersects(envelope))
                  {
                    return true;
                  }
                }
              }
            }

            return false;
          }).forEach(c -> this.cache.put(c.getId(), c));
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
