package ai.terraframe.kaleidoscope.dggs.core.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.map.LRUMap;

public class SupplierLRUCache<K, V>
{
  // Objects in the global cache
  private final Map<K, V>        cache;

  private final Function<V, K[]> mapper;

  public SupplierLRUCache(Function<V, K[]> mapper)
  {
    this(mapper, 20);
  }

  public SupplierLRUCache(Function<V, K[]> mapper, int maxSize)
  {
    this.mapper = mapper;
    this.cache = Collections.synchronizedMap(new LRUMap<K, V>(20));
  }

  public void put(V value)
  {
    K[] keys = mapper.apply(value);

    for (K key : keys)
    {
      cache.put(key, value);
    }
  }

  public void remove(V value)
  {
    K[] keys = mapper.apply(value);

    for (K key : keys)
    {
      cache.remove(key, value);
    }
  }

  public Optional<V> get(K key, Supplier<Optional<V>> supplier)
  {
    return Optional.ofNullable(this.cache.get(key)).or(() -> {
      Optional<V> v = supplier.get();

      v.ifPresent(value -> {
        K[] keys = mapper.apply(value);

        for (K k : keys)
        {
          cache.put(k, value);
        }
      });

      return v;
    });
  }

  public void clear()
  {
    this.cache.clear();
  }

}
