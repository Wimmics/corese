package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
/**
 * service -> List of connected BGP
 */
class URI2BGPList {

    HashMap<String, List<BasicGraphPattern>> map;
    private ArrayList<Triple> tripleList;
    
    URI2BGPList() {
        map = new HashMap<>();
        tripleList = new ArrayList<>();
    }

    List<BasicGraphPattern> get(String label) {
        List<BasicGraphPattern> bgpList = map.get(label);
        if (bgpList == null) {
            bgpList = new ArrayList<>();
            map.put(label, bgpList);
        }
        return bgpList;
    }

    void add(String label, BasicGraphPattern bgp) {
        List<BasicGraphPattern> bgpList = map.get(label);
        if (bgpList == null) {
            bgpList = new ArrayList<>();
            map.put(label, bgpList);
        }
        bgpList.add(bgp);
    }

    HashMap<String, List<BasicGraphPattern>> getMap() {
        return map;
    }

    // for every URI: merge list of BGP into one BGP
    void merge() {
        for (String uri : map.keySet()) {
            List<BasicGraphPattern> list = map.get(uri);
            BasicGraphPattern bgp = list.get(0);
            for (BasicGraphPattern exp : list) {
                if (exp != bgp) {
                    bgp.include(exp);
                }
            }
            for (int i = 1; i < list.size(); i++) {
                list.remove(i);
            }
        }
    }

    /**
     * merge two service bgp with same URI if there is a filter with variables
     * and each service bind some (but not all) of the variables
     */
    void merge(List<Expression> filterList) {
        for (String uri : map.keySet()) {
            List<BasicGraphPattern> list = map.get(uri);
            merge(list, filterList);
        }
    }

    void merge(List<BasicGraphPattern> list, List<Expression> filterList) {
        if (list.size() == 2) {
            List<Variable> l1 = list.get(0).getSubscopeVariables();
            List<Variable> l2 = list.get(1).getSubscopeVariables();
            for (Expression filter : filterList) {
                List<Variable> varList = filter.getInscopeVariables();
                if (gentle(l1, l2, varList)) {
                    list.get(0).include(list.get(1));
                    list.remove(1);
                    return;
                }
            }
        }
    }

    // every var in l3 in l1 or l2
    // some  var in l3 not in l1 and in l2
    boolean gentle(List<Variable> l1, List<Variable> l2, List<Variable> l3) {
        for (Variable var : l3) {
            if (!(l1.contains(var) || l2.contains(var))) {
                return false;
            }
        }
        for (Variable var : l3) {
            if (!l1.contains(var) || !l2.contains(var)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String uri : map.keySet()) {
            sb.append(uri).append(": ");
            for (BasicGraphPattern bgp : map.get(uri)) {
                sb.append(bgp).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    void add(Triple t) {
        getTripleList().add(t);
    }

    public ArrayList<Triple> getTripleList() {
        return tripleList;
    }

    public void setTripleList(ArrayList<Triple> tripleList) {
        this.tripleList = tripleList;
    }

}
