package fr.inria.corese.core.print.rdfc10;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A map structure where each key is associated with a list of values.
 * This class facilitates the storage of multiple values per key.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ListMap<K, V> implements Map<K, List<V>> {
    private final Map<K, List<V>> map = new TreeMap<>();

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public List<V> get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a set view of the keys contained in this map.
     * The set is ordered according to the natural ordering of its elements,
     * which is determined by the internal TreeMap used in the implementation.
     *
     * @return a set view of the keys contained in this map, ordered according to
     *         the natural ordering of its elements
     */
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public List<V> put(K key, List<V> value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        map.putAll(m);
    }

    @Override
    public List<V> remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<List<V>> values() {
        return map.values();
    }

    /**
     * Adds a value to the list associated with a specific key.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{\n");
        map.forEach((key, value) -> sb.append("\t").append(key).append(" -> ").append(value).append("\n"));
        sb.append("}");

        return sb.toString();
    }
}
