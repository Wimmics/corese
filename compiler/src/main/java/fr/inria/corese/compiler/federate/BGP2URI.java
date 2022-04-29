package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
class BGP2URI extends HashMap<BasicGraphPattern, List<String>> {

    // equivalent of get when different bgp object which have same content
    // must be same key
    BasicGraphPattern getKey(BasicGraphPattern bgp) {
        for (BasicGraphPattern exp : keySet()) {
            if (bgp.bgpEqual(exp)) {
                return exp;
            }
        }
        return bgp;
    }

    List<BasicGraphPattern> keyList() {
        ArrayList<BasicGraphPattern> list = new ArrayList<>();
        list.addAll(keySet());
        return list;
    }

    List<BasicGraphPattern> sortKeyList() {
        return sort(keyList());
    }

    List<BasicGraphPattern> sort(List<BasicGraphPattern> list) {
        list.sort(new Comparator<>() {
            public int compare(BasicGraphPattern bgp1, BasicGraphPattern bgp2) {
                return -Integer.compare(bgp1.size(), bgp2.size());
            }
        });
        return list;
    }

    // search a BGP that is a partition on its own
    BasicGraphPattern singlePartition(int size) {
        for (BasicGraphPattern bgp : keySet()) {
            if (bgp.size() == size) {
                return bgp;
            }
        }
        return null;
    }

    // compute natural subpartition if any
    // with |bgp|>1 and |uri|=1
    List<BasicGraphPattern> partition() {
        ArrayList<BasicGraphPattern> bgpList = new ArrayList<>();

        for (BasicGraphPattern bgp : keySet()) {
            List<String> uriList = get(bgp);
            if (bgp.size() > 1 && uriList.size() == 1 && !intersect(bgp)) {
                bgpList.add(bgp);
            }
        }

        return bgpList;
    }

    // intersection of bgp with other bgp
    boolean intersect(BasicGraphPattern bgp) {
        for (BasicGraphPattern exp : keySet()) {
            if (exp != bgp) {
                if (!bgp.intersectionTriple(exp).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

}
