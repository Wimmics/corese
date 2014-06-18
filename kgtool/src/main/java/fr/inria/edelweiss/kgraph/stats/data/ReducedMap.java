package fr.inria.edelweiss.kgraph.stats.data;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.stats.Options;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Save part of the map between nodes and their count, in order to reduce the
 * size of mapping
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 4 juin 2014
 */
public class ReducedMap extends BaseMap {

    private Map<String, Integer> topXBottomY;

    private int numResourcesCached = 0, numTriplesCached = 0;
    //nuymber of resources and triples in the whole graph
    //private int numAllTriples, numAllResources;
    private final double[] options;
    private int avgNumber = 0;

    public ReducedMap(double[] options) {
        super();
        if (options == null || options.length != 4) {
            this.options = Options.DEF_PARA_CUTOFF;
        } else {
            this.options = options;
        }
    }

    public void cut() {
        topXBottomY = new HashMap<String, Integer>();
        ValueComparator vc = new ValueComparator(this.full);

        TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>(vc);
        treeMap.putAll(this.full);
        cutoff(treeMap, topXBottomY, options[0], options[1]);
        cutoff(treeMap.descendingMap(), topXBottomY, options[2], options[3]);

        avgNumber = (this.total - this.numTriplesCached) / (this.size() - this.numResourcesCached);
    }

    // 10% top nodes and 10%bottom nodes
    // or the ones whose sum accumulative is greater than 50% of total triples
    // subject or predicate or object
    private void cutoff(Map<String, Integer> fullSortedMap, Map<String, Integer> cutoff, double res_limit, double tri_limit) {
        ArrayList<String> keys = new ArrayList<String>(fullSortedMap.keySet());
        int triple = 0, resource = 0;
        int counter;
        for (int i = 0; i < keys.size(); i++) {
            if (triple < this.total * tri_limit
                    && resource <= this.size() * res_limit) {
                counter = fullSortedMap.get(keys.get(i));
                triple += counter;
                resource++;
                cutoff.put(keys.get(i), counter);
            } else {
                break;
            }
        }

        numTriplesCached += triple;
        numResourcesCached += resource;
    }

    @Override
    public int get(String value) {
        if (topXBottomY.containsKey(value)) {
            return topXBottomY.get(value);
        } else {
            return avgNumber;
        }
    }

    @Override
    public int get(Node n) {
        return this.get(n.getLabel());
    }

    // Internal class for sorting tree map
    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        @Override
        public int compare(String o1, String o2) {
            Integer i1 = base.get(o1), i2 = base.get(o2);
            if (i1 == null || i2 == null) {
                return -1;
            }

            if (i1.intValue() < i2.intValue()) {
                return 1;
            } else if (i1.intValue() == i2.intValue()) {
                return o1.compareTo(o2);
            } else {
                return -1;
            }
        }
    }
}
