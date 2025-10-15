package ai.terraframe.kaleidoscope.dggs.core.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.cache.SupplierLRUCache;
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggr;

@Service
public class DggrsService
{
  private static final String            DEFAULT_DGGRS = "ISEA3H";

  @Autowired
  private RemoteDggsServiceIF            service;

  @Autowired
  private CollectionService              collectionService;

  private SupplierLRUCache<String, Dggr> cache;

  public DggrsService()
  {
    this.cache = new SupplierLRUCache<String, Dggr>((v) -> {
      return new String[] { v.getId(), v.getCollectionId() };
    }, 20);
  }

  public Optional<Dggr> get(String collectionId)
  {
    return this.cache.get(collectionId, () -> {

      try
      {
        Collection collection = this.collectionService.getOrThrow(collectionId);

        List<Dggr> dggrs = this.service.dggs(collection.getUrl(), collectionId).getDggrs();

        return dggrs.stream().filter(dggr -> dggr.getId().equals(DEFAULT_DGGRS)) //
            .findFirst() //
            .or(() -> dggrs.size() > 0 ? Optional.ofNullable(dggrs.get(0)) : Optional.empty());
      }
      catch (Exception e)
      {
        throw new GenericRestException("Unable to get DGGRS information for colleciton [" + collectionId + "]");
      }
    });
  }

  public Dggr getOrThrow(String collectionId)
  {
    return this.get(collectionId).orElseThrow(() -> new GenericRestException("Unable to find collection with id [" + collectionId + "]"));
  }

}
