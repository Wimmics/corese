package fr.inria.edelweiss.kgraph.persistent.ondisk;

import java.util.Map.Entry;

/**
 * FileHandlerCache.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 févr. 2015
 */
public class LRUCacheFileHandler extends LRUCache{

    public LRUCacheFileHandler(int capacity) {
        super(capacity);
    }

    @Override
    public boolean removeEldestEntry(Entry eldest) {
        FileHandler fh = (FileHandler)eldest.getValue();
        fh.close();
        return super.removeEldestEntry(eldest);
    }
}
