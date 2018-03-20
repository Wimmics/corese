package fr.inria.corese.core.stats.data;

import fr.inria.corese.kgram.api.core.Node;
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

    Map<WrappedNode, Integer> full;
    //number of total values, not distinct
    int total = 0;

    public BaseMap() {
        this.full = new HashMap<WrappedNode, Integer>();
    }

    public void add(Node node) {
        WrappedNode n = new WrappedNode(node);
        if (this.full.containsKey(n)) {
            this.full.put(n, this.full.get(n) + 1);
        } else {
            this.full.put(n, 1);
        }
        total++;
    }

    public int get(Node n) {
        Integer v = this.full.get(new WrappedNode(n));
        return v == null ? 0 : v.intValue();
    }

    public int size() {
        return this.full.size();
    }
    
    public void clear(){
        this.full.clear();
    }
}
