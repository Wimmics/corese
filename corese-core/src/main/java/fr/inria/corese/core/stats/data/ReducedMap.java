package fr.inria.corese.core.stats.data;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.stats.Options;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    private Map<WrappedNode, Integer> topXBottomY;

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
        topXBottomY = new HashMap<WrappedNode, Integer>();
        ValueComparator vc = new ValueComparator(this.full);

        TreeMap<WrappedNode, Integer> treeMap = new TreeMap<WrappedNode, Integer>(vc);
        treeMap.putAll(this.full);
        //top X
        cutoff(treeMap, topXBottomY, options[0], options[1]);
        //bottom Y
        cutoff(treeMap.descendingMap(), topXBottomY, options[2], options[3]);

        if(super.size()  == this.numResourcesCached){
            avgNumber = 0;
        }else{
            avgNumber = (this.total - this.numTriplesCached) / (super.size() - this.numResourcesCached);
        }
    }

    // 10% top nodes and 10%bottom nodes
    // or the ones whose sum accumulative is greater than 50% of total triples
    // subject or predicate or object
    private void cutoff(Map<WrappedNode, Integer> fullSortedMap, Map<WrappedNode, Integer> cutoff, double res_limit, double tri_limit) {
        List<WrappedNode> keys = new ArrayList<WrappedNode>(fullSortedMap.keySet());
        int triple = 0, resource = 0;
        int counter;
        for (int i = 0; i < keys.size(); i++) {
            if (triple < this.total * tri_limit
                    && resource <= super.size() * res_limit) {
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
    public int get(Node node) {
        WrappedNode n = new WrappedNode(node);
        if (topXBottomY.containsKey(n)) {
            return topXBottomY.get(n);
        } else {
            return avgNumber;
        }
    }

    public int length(){
        return topXBottomY.size();
    }
    // Internal class for sorting tree map
    class ValueComparator implements Comparator<WrappedNode> {

        Map<WrappedNode, Integer> map;

        public ValueComparator(Map<WrappedNode, Integer> base) {
            this.map = base;
        }

        @Override
        public int compare(WrappedNode o1, WrappedNode o2) {
            
            Integer i1 = map.get(o1), i2 = map.get(o2);
            if (i1 == null || i2 == null) {
                return -1;
            }

            if (i1.intValue() < i2.intValue()) {
                return 1;
            } else if (i1.intValue() == i2.intValue()) {
                return o1.getNode().compare(o2.getNode());
            } else {
                return -1;
            }
        }
    }
}
