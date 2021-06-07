package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author corby
 */
public class HashMapList<T> extends HashMap<String, List<T>> {

    public T getFirst(String key) {
        List<T> list = get(key);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    public List<T> getCreate(String key) {
        List<T> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put(key, list);
        }
        return list;
    }
    
    public void setFirst(String key, T val) {
        List<T> list = getCreate(key);
        list.set(0, val);
    }
    
    public void add(String key, T val) {
        List<T> list = getCreate(key);
        if (!list.contains(val)) {
            list.add(val);
        }
    }
}
