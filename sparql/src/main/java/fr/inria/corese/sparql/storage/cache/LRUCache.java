package fr.inria.corese.sparql.storage.cache;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Latest recently used cache Cache.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @param <K>
 * @param <V>
 * @date 2 févr. 2015
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> implements ICache<K, V> {

    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity + 1, 1.1f, true);
        this.capacity = capacity;
    }

    @Override
    public boolean removeEldestEntry(Entry eldest) {
        return size() > capacity;
    }
}
