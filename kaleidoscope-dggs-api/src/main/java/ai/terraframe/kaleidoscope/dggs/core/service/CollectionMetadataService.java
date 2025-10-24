package ai.terraframe.kaleidoscope.dggs.core.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.terraframe.kaleidoscope.dggs.core.cache.SupplierLRUCache;
import ai.terraframe.kaleidoscope.dggs.core.model.CollectionAttribute;
import ai.terraframe.kaleidoscope.dggs.core.model.CollectionMetadata;
import ai.terraframe.kaleidoscope.dggs.core.model.GenericRestException;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Collection;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.CollectionDggs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.Dggrs;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.DggrsAndLinks;
import ai.terraframe.kaleidoscope.dggs.core.model.dggs.QueryableProperties;

@Service
public class CollectionMetadataService
{
  private static final String                          DEFAULT_DGGRS = "ISEA3H";

  @Autowired
  private RemoteDggsServiceIF                          service;

  @Autowired
  private CollectionService                            collectionService;

  private SupplierLRUCache<String, CollectionMetadata> cache;

  public CollectionMetadataService()
  {
    this.cache = new SupplierLRUCache<String, CollectionMetadata>((v) -> {
      return new String[] { v.getCollectionId() };
    }, 20);
  }

  public Optional<CollectionMetadata> get(String collectionId)
  {
    return this.cache.get(collectionId, () -> {

      try
      {
        Collection collection = this.collectionService.getOrThrow(collectionId);

        DggrsAndLinks dggs = this.service.dggs(collection.getUrl(), collectionId);

        List<Dggrs> dggrses = dggs.getDggrs();

        Optional<Dggrs> dggrs = dggrses.stream().filter(dggr -> dggr.getId().equals(DEFAULT_DGGRS)) //
            .findFirst() //
            .or(() -> dggrses.size() > 0 ? Optional.ofNullable(dggrses.get(0)) : Optional.empty());

        return dggrs.map(dggr -> {
          try
          {
            List<CollectionAttribute> attributes = new LinkedList<>();

            CollectionDggs dggses = this.service.dggs(collection.getUrl(), collectionId, dggr.getId());

            this.service.queryables(collection.getUrl(), collectionId).ifPresent(queryables -> {
              QueryableProperties properties = queryables.getProperties();
              properties.getProperties() //
                  .entrySet() //
                  .stream() //
                  .filter(entry -> !StringUtils.isBlank(entry.getValue().getType())) //
                  .filter(entry -> !entry.getValue().getType().equals("date")) //
                  .filter(entry -> !entry.getKey().equals("date")) //
                  .forEach(entry -> {
                    attributes.add(new CollectionAttribute(entry.getKey(), entry.getValue().getDescription()));
                  });
            });

            return new CollectionMetadata(collectionId, attributes, dggs, dggses, dggr);
          }
          catch (InterruptedException | IOException e)
          {
            throw new RuntimeException(e);
          }
        });

      }
      catch (Exception e)
      {
        throw new GenericRestException("Unable to get DGGRS information for collection [" + collectionId + "]", e);
      }
    });
  }

  public CollectionMetadata getOrThrow(String collectionId)
  {
    return this.get(collectionId).orElseThrow(() -> new GenericRestException("Unable to find collection with id [" + collectionId + "]"));
  }

}
