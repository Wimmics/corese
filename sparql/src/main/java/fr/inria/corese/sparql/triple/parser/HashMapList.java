package fr.inria.corese.sparql.triple.parser;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author corby
 */
public class HashMapList<T> extends HashMap<String, List<T>> {
    
        T getFirst(String key) {
            List<T> list = get(key);
            return list == null || list.isEmpty() ? null : list.get(0);
        }
}
