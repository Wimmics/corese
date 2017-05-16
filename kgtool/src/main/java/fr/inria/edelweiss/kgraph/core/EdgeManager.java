package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import static fr.inria.edelweiss.kgraph.core.EdgeManagerIndexer.IGRAPH;
import static fr.inria.edelweiss.kgraph.core.EdgeManagerIndexer.ILIST;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Edge List of a predicate
 * Edge may be stored without predicate Node to spare memory
 * Edges are sorted according to 
 * 1- edge.getNode(index).getIndex() (subject)
 * 2- other nodes (object, graph)
 * index 0:
 * g1 s1 p o1 ; g2 s1 p o1 ; g1 s1 p o2 ; g2 s2 p o3 ; ... 
 * 
 * This version manages XSD datatypes this way:
 * integer, long, decimal (and short, byte, int, etc.) 
 * - have same node index when values are equal
 * - different labels are possible for same value: 1 and 01 are kept as is
 * with same node index
 * integer/long/decimal and double and float have different node index
 * Nodes with same node index which are not sameTerm are kept in the list:
 * s p 01, 1, 1.0, '1'^^xsd:long, 1e1
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EdgeManager implements Iterable<Entity> {
    Graph graph;
    private Node predicate;
    ArrayList<Entity> list;
    Comparator<Entity> comp;
    int index = 0, other = 0, next = IGRAPH;

    EdgeManager(Graph g, Node p, int i) {
        graph = g;
        predicate = p;
        list = new ArrayList<Entity>();
        index = i;
        if (index == 0) {
            other = 1;
        }
        else if (index == IGRAPH){
            next = 1;
        }
    }

    ArrayList<Entity> getList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    void clear() {
        list.clear();
    }

    Entity get(int i) {
        return list.get(i);
    }
    
    int getIndex(){
        return index;
    }

    /**
     * Remove duplicate edges
     */
    int reduce() {
        ArrayList<Entity> l = new ArrayList<Entity>();
        Entity pred = null;
        int count = 0;
        for (Entity ent : list) {
            if (pred == null) {
                l.add(ent);
            } else if (comp.compare(ent, pred) != 0) {
                l.add(ent);
            } else {
                count++;
            }
            pred = ent;
        }
        list = l;
        return count;
    }
    
    /**
     * Replace kg:rule Edge by compact EdgeInternalRule
     */
    void compact(){
        ArrayList<Entity> l = new ArrayList<Entity>(list.size());
        for (Entity ent : list) {
           Entity ee = graph.getEdgeFactory().compact(ent);
           l.add(ee);
        }
        list = l;
    }

    void sort() {
        Collections.sort(list, comp);
    }

    /**
     * Copy Index(0) into this index
     */
    void copy(EdgeManager el) {
        list.ensureCapacity(el.size());
        if (index < 2) {
            // we are sure that there are at least 2 nodes
            list.addAll(el.getList());
        } else {
            for (Entity ent : el) {
                // if additional node is missing: do not index this edge
                if (ent.nbNode() > index) {
                    list.add(ent);
                }
            }
        }
    }

    void add(Entity ent) {
        list.add(ent);
    }

    void add(int i, Entity ent) {
        list.add(i, ent);
    }

    Entity remove(int i) {
        Entity ent = list.get(i);
        list.remove(i);
        return ent;
    }

    /**
     * PRAGMA: All Edge in list have p as predicate, no duplicates Use case:
     * Rule Engine
     */
    void add(List<Entity> l) {
        list.ensureCapacity(l.size() + list.size());
        list.addAll(l);
    }

    /**
     * Return place where edge should be inserted in this Index return -1 if
     * already exists
     */
    int getPlace(Entity edge) {
        int i = find(edge);

        if (i >= list.size()) {
            i = list.size();
        } else if (index == 0) {
            int res = comp.compare(edge, list.get(i));
            if (res == 0) {
                // eliminate duplicate at load time for index 0                   
                return -1;
            }
        }

        return i;
    }

    /**
     * Place of edge in this Index
     */
    int find(Entity edge) {
        return find(edge, 0, list.size());
    }

    int find(Entity edge, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = comp.compare(list.get(mid), edge);
            if (res >= 0) {
                return find(edge, first, mid);
            } else {
                return find(edge, mid + 1, last);
            }
        }
    }

    /**
     * Test if an edge (n1 p n2) exist in this Index (in any named graph)
     */
    boolean exist(Node n1, Node n2) {
        int n = findNodeTerm(n1, n2, 0, list.size());
        if (n >= 0 && n < list.size()) {
            Entity ent = list.get(n);
            if (n1.getIndex() == getNodeIndex(ent, 0)
                    && n2.getIndex() == getNodeIndex(ent, 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find index of edge If not found, return -1
     */
    int findIndexNodeTerm(Entity ent) {
        int i = find(ent);

        if (i >= size()) {
            return -1;
        }

        int res = comp.compare(ent, list.get(i));

        if (res == 0) {
            return i;
        }

        return -1;
    }

    /**
     * Find index of node as node(index) -1 if not found
     */
    int findIndex(Node n) {
        int i = findNodeTerm(n, 0, list.size());
        if (i >= 0 && i < list.size()
                && getNodeIndex(i, index) == n.getIndex()) {
            return i;
        }
        return -1;
    }

    boolean exist(Entity ent) {
        int i = findIndexNodeTerm(ent);
        return i != -1;
    }

    /**
     * Iterate edges with index node node1 (and other node node2)
     */
    Iterable<Entity> getEdges(Node node1, Node node2) {
        int n = findNodeTerm(node1, node2, 0, list.size());
        if (n >= 0 && n < list.size()) {
            int n1 = getNodeIndex(n, index);
            if (n1 == node1.getIndex()) {
                int n2 = getNodeIndex(n, other);
                if (n2 != node2.getIndex()) {
                    return null;
                }
                Iterate it = new Iterate(this, n);
                return it;
            }
        }
        return null;
    }

    /**
     * Iterate edges with index node node1
     */
    Iterable<Entity> getEdges(Node node) {
        // node is bound, enumerate edges where node = edge.getNode(index)
        int n = findNodeIndex(node, 0, list.size());
        if (n >= 0 && n < list.size()) {
            int i = getNodeIndex(n, index);
            if (i == node.getIndex()) {
                if (EdgeManagerIndexer.test) {
                    // draft
                    return new EdgeManagerIterate(this, n);

                } else {
                    // format
                    return new Iterate(this, n);
                }
            }
        }
        return null;
    }

    /**
     * return index of edge where edge.getNode(index) sameTerm node
     */
    int findNodeTerm(Node n, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = compareNodeTerm(getNode(mid, index), n);
            if (res >= 0) {
                return findNodeTerm(n, first, mid);
            } else {
                return findNodeTerm(n, mid + 1, last);
            }
        }
    }
    
    /**
     * return index of edge where edge.getNode(index).index() =  node.index()
     */
    int findNodeIndex(Node n, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = compareNodeIndex(getNode(mid, index), n);
            if (res >= 0) {
                return findNodeIndex(n, first, mid);
            } else {
                return findNodeIndex(n, mid + 1, last);
            }
        }
    }
    
    

    /**
     * return index of edge where
     * edge.getNode(index) == node1 and edge.getNode(other) == node2
     */
    
    int findNodeTerm(Node n1, Node n2, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            if (compare(list.get(mid), n1, n2) >= 0) {
                return findNodeTerm(n1, n2, first, mid);
            } else {
                return findNodeTerm(n1, n2, mid + 1, last);
            }
        }
    }   
    
     int compare(Entity ent, Node n1, Node n2) {
        int res = compareNodeTerm(ent.getNode(index), n1);
        if (res == 0) {
            res = compareNodeTerm(ent.getNode(other), n2);
        }
        return res;
    }

    /**
     * n1 n2 are node.getIndex()
     */
    int intCompare(int n1, int n2) {
        if (n1 < n2) {
            return -1;
        } else if (n1 == n2) {
            return 0;
        } else {
            return +1;
        }
    }
    
    /**
     * Compare nodes with sameTerm semantics
     * if value, datatype, label are equal : return 0
     * return +-1 otherwise
     */
    int compareNodeTerm(Node n1, Node n2){
        int res = intCompare(n1.getIndex(), n2.getIndex());
        if (res == 0){
            // same node index (compatible datatypes)
            // check datatype and label
            res = getValue(n1).compareTo(getValue(n2));
        }
        return res;
    }
    
    int compareNodeIndex(Node n1, Node n2){
        return intCompare(n1.getIndex(), n2.getIndex());       
    }
    
    IDatatype getValue(Node n){
        return (IDatatype) n.getValue();
    }

    @Override
    public Iterator<Entity> iterator() {
        return list.iterator();
    }

    /**
     * @return the predicate
     */
    public Node getPredicate() {
        return predicate;
    }


    /**
     * edge = list.get(n) node = edge.getNode(index) Return an iterator of edges
     * with node as index element
     */
    class Iterate implements Iterable<Entity>, Iterator<Entity> {

        List<Entity> list;
        int node;
        int ind, start;

        Iterate(EdgeManager l, int n) {
            list = l.getList();
            node = getNodeIndex(list.get(n), index);
            start = n;
        }

        @Override
        public Iterator<Entity> iterator() {
            ind = start;
            return this;
        }

        @Override
        public boolean hasNext() {
            boolean b = ind < list.size()
                    && getNodeIndex(list.get(ind), index) == node;
            return b;
        }

        @Override
        public Entity next() {
            Entity ent = list.get(ind++);
            return ent; 
        }

        @Override
        public void remove() {
        }         
    }
    
       

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(Entity ent, int n) {
        return ent.getNode(n).getIndex();
    }

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(int i, int n) {
        Entity ent = list.get(i);
        return ent.getNode(n).getIndex();
    }
    
    // getNode(IGRAPH) must return getGraph()
    Node getNode(int i, int n) {
        Entity ent = list.get(i);
        return ent.getNode(n);
    }
    

    /**
     * **************************************************************
     */
    Comparator<Entity> getComparator(int n) {
        switch (n) {
            case ILIST:
                return getListComparator();
        }
        return getComparator();
    }
    
    void setComparator(Comparator<Entity> c){
        comp = c;
    }


    /**
     * Compare two edges to sort them in Index
     */
     Comparator<Entity> getComparator() {

        return new Comparator<Entity>() {
            
            @Override
            public int compare(Entity o1, Entity o2) {

                // check the Index Node
                int res = compareNodeTerm(o1.getNode(index), o2.getNode(index));

                if (res != 0) {
                    return res;
                }
                
                res = compareNodeTerm(o1.getNode(other), o2.getNode(other));

                if (res != 0) {
                    return res;
                }
                
                if (o1.nbNode() == 2 && o2.nbNode() == 2) {
                    // compare third Node
                    res = compareNodeTerm(o1.getNode(next), o2.getNode(next));
                    return res;
                }

                // common arity
                int min = Math.min(o1.nbNode(), o2.nbNode());

                for (int i = 0; i < min; i++) {
                    // check other common arity nodes
                    if (i != index) {
                        res = compareNodeTerm(o1.getNode(i), o2.getNode(i));
                        if (res != 0) {
                            return res;
                        }
                    }
                }

                if (o1.nbNode() == o2.nbNode()) {
                    // same arity, nodes are equal
                    // check graph node
                    return compareNodeTerm(o1.getNode(IGRAPH), o2.getNode(IGRAPH));
                }
                else if (o1.nbNode() < o2.nbNode()) {
                    // smaller arity edge is before
                    return -1;
                } else {
                    return 1;
                }

            }
        };
    }

    /**
     * sort in reverse order of edge timestamp
     * new edge first (for RuleEngine)
     */
    Comparator<Entity> getListComparator() {

        return new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                int i1 = o1.getEdge().getIndex();
                int i2 = o2.getEdge().getIndex();
                
                if (i1 > i2) {
                    return -1;
                } else if (i1 == i2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };

    }

}
