package fr.inria.edelweiss.kgraph.stats.data;

import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.HashMap;
import java.util.Map;

/**
 * The statistics data in fact is a map, this base class implements the basic
 * map between distinct node and its number
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 17 juin 2014
 */
public class BaseMap {

    Map<String, Integer> full;
    int total = 0;

    public BaseMap() {
        this.full = new HashMap<String, Integer>();
    }

    public void add(Node n) {
        String v = n.getLabel();
        if (this.full.containsKey(v)) {
            this.full.put(v, this.full.get(v) + 1);
        } else {
            this.full.put(v, 1);
        }
        total++;
    }

    public int get(String s) {
        Integer v = this.full.get(s);
        return v == null ? 0 : v.intValue();
    }

    public int get(Node n) {
        return this.get(n.getLabel());
    }

    public int size() {
        return this.full.size();
    }
}
