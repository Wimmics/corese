package fr.inria.corese.sparql.storage.cache;

/**
 * Interface for implementing cache ICache.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @param <K>
 * @param <V>
 * @date 4 févr. 2015
 */
public interface ICache<K, V> {

    public int size();

    public V get(Object key);

    public V put(K key, V value);

    public boolean containsKey(Object key);

    public void clear();
}
