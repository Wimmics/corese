package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.corese.kgram.api.core.Node;
import java.util.Map;

/**
 * select distinct ?x ?y select (count(distinct *) as ?c)
 *
 * group by ?x ?y min(?l, groupBy(?x, ?y))
 *
 * Compare Mapping using a TreeMap
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class Group implements Comparator<Mappings> {

    TreeMapping table;

    private List<Node> nodes;

    Node fake;

    boolean isDistinct = false,
            // min(?l, groupBy(?x, ?y))
            isExtend = false;
    private boolean isFake = false;
    private static boolean compareIndex = false;

   
    public boolean isFake() {
        return isFake;
    }

    
    public void setFake(boolean afake) {
        this.isFake = afake;
    }

   
    public static boolean isCompareIndex() {
        return compareIndex;
    }

    
    public static void setCompareIndex(boolean b) {
        compareIndex = b;
    }

    class TreeMapping extends TreeMap<Mapping, Mappings> {

        TreeMapping(List<Node> ln) {
            super(new Compare(ln));
        }
    }
    
    public void dispose() {
        table.clear();
    }

    class Compare implements Comparator<Mapping> {

        List<Node> list;
        int size;

        Compare(List<Node> ln) {
            list = ln;
            size = list.size();
        }

        @Override
        public int compare(Mapping m1, Mapping m2) {
            if (isExtend) {
                return compareExtend(m1, m2);
            }

            if (isDistinct) {
                return compareDistinct(m1, m2);
            }
            
            return compareGroup(m1, m2);
        }

        int compareExtend(Mapping m1, Mapping m2) {
            Node[] g1 = m1.getGroupNodes();
            Node[] g2 = m2.getGroupNodes();

            for (int i = 0; i < size; i++) {
                int res = compare(g1[i], g2[i]);
                if (res != 0) {
                    return res;
                }
            }
            return 0;
        }

        int compareDistinct(Mapping m1, Mapping m2) {
            for (int i = 0; i < size; i++) {
                int res = compare(m1.getDistinctNode(i), m2.getDistinctNode(i));
                if (res != 0) {
                    return res;
                }
            }
            return 0;
        }
        
        int compareGroup(Mapping m1, Mapping m2) {
            for (int i = 0; i < size; i++) {
                Node qNode = list.get(i);
                int res = compare(m1.getGroupBy(qNode, i), m2.getGroupBy(qNode, i));
                if (res != 0) {
                    return res;
                }
            }
            return 0;
        }

        int compare(Node n1, Node n2) {
            if (n1 == n2) {
                return 0;
            } else if (n1 == null) {
                return -1;
            } else if (n2 == null) {
                return +1;
            } else if (isCompareIndex() && n1.getIndex() != -1 && n2.getIndex() != -1) {
                return Integer.compare(n1.getIndex(), n2.getIndex());
            } else {
                return n1.compare(n2);
            }
        }
    }

    Group() {

    }

    public static Group create(List<Node> lNode) {       
        return new Group(lNode);
    }

    public static Group createFromExp(List<Exp> list) {
        List<Node> nodes = new ArrayList<>();
        for (Exp exp : list) {
            nodes.add(exp.getNode());
        }
        return new Group(nodes);
    }
   
    Group(List<Node> list) {
        setNodeList(list);
        table = new TreeMapping(list);
    }

    public List<Node> getNodeList() {
        return nodes;
    }

    public void setDistinct(boolean b) {
        isDistinct = b;
    }

    public void setExtend(boolean b) {
        isExtend = b;
    }

    public void setDuplicate(boolean b) {
    }

    // TODO
    boolean accept(Node node) {
        return true;
    }

    @Override
    public int compare(Mappings lm1, Mappings lm2) {
        Mapping m1 = lm1.get(0);
        Mapping m2 = lm2.get(0);
        return lm1.compare(m1, m2);
    }

    Node getGroupBy(Mapping map, Node qNode, int n) {
        if (isDistinct) {
            return map.getDistinctNode(n);
        } 
        else {
            return map.getGroupBy(qNode, n);
        }
    }

    /**
     * add map in a group by
     */
    public boolean add(Mapping map) {
        if (isExtend) {
            // min(?l, groupBy(?x, ?y))
            // store value of ?x ?y in an array to speed up
            map.setGroup(getNodeList());
        }

        Mappings lm = table.get(map);
        if (lm == null) {
            lm = new Mappings();
            lm.setFake(isFake());
            table.put(map, lm);
        }
        lm.add(map);

        return true;
    }

    // select distinct *
    // select (avg(distinct ?x) as ?a
    public boolean isDistinct(Mapping map) {
        map.computeDistinct(getNodeList());

        if (table.containsKey(map)) {
            return false;
        }
        table.put(map, null); 
        return true;
    }

    Iterable<Mappings> getValues() {
        return table.values();
    }
    
    public Map<Mapping,Mappings> getTable() {
        return table;
    }

    public void setNodeList(List<Node> nodes) {
        this.nodes = nodes;
    }


}
