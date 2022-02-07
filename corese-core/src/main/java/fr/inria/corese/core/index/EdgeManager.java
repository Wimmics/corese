package fr.inria.corese.core.index;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import static fr.inria.corese.core.index.EdgeManagerIndexer.IGRAPH;
import static fr.inria.corese.core.index.EdgeManagerIndexer.ILIST;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;
import java.util.HashMap;

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
public class EdgeManager implements Iterable<Edge> {
    Graph graph;
    EdgeManagerIndexer indexer;
    private Node predicate;
    ArrayList<Edge> list;
    Comparator<Edge> comp;
    int index = 0, other = 0, next = IGRAPH;
    int meta = Edge.REF_INDEX;
    boolean indexedByNode = false;

    EdgeManager(EdgeManagerIndexer indexer, Node p, int i) {
        graph = indexer.getGraph();
        this.indexer = indexer;        
        predicate = p;
        list = new ArrayList<>();
        index = i;
        if (index == 0) {
            other = 1;
        }
        else if (index == IGRAPH){
            next = 1;
        }
    }
    
    public Graph getGraph() {
        return graph;
    }

    ArrayList<Edge> getList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    void clear() {
        list.clear();
    }

    Edge get(int i) {
        return list.get(i);
    }
    
    int getIndex(){
        return index;
    }

    /**
     * Remove duplicate edges
     */
    int reduce(NodeManager mgr) {
        ArrayList<Edge> l = new ArrayList<>();
        Edge pred = null;
        int count = 0, ind = 0;
        //System.out.println("before reduce: " + list);
        
        for (Edge edge : list) {
            if (pred == null) {
                l.add(edge);
                mgr.add(edge.getNode(index), predicate, ind);
                ind++;
            } else if (equalExceptIfMetadata(pred, edge)) {
                // skip edge
                count++;
            } else {
                l.add(edge);
                if (edge.getNode(index) != pred.getNode(index)) {
                    mgr.add(edge.getNode(index), predicate, ind);
                }
                ind++;
            }
            pred = edge;
        }
        
        list = l;
        if (count > 0) {
            graph.setSize(graph.size() - count);
        }
        //System.out.println("after reduce: " + list);
        return count;
    }
    
    /**
     * return true when edges are equal, except if one has metadata
     */
    boolean equalExceptIfMetadata(Edge e1, Edge e2) {
        if (comp.compare(e1, e2) == 0) {
            if (graph.isEdgeMetadata() && (e1.getReferenceNode() != null || e2.getReferenceNode() != null)) {
                indexer.setLoopMetadata(true);
                return false;
            }
            return true;
        }
        return false;
    }
         
    /**
     * Context: RDF*
     * Merge duplicate triples, keep one metadata node
     * PRAGMA: index = 0, list is sorted and reduced
     * rdf* triple with same g s p o  and with or without ref id are not yet reduced
     * because they are considered equal by compare()
     * we remove these duplicate here and keep triple with ref id if any
     */
    void metadata() {
        ArrayList<Edge> l = new ArrayList<>();
        Edge e1 = null;
        int count = 0;
        int i = 0;
        
        for (Edge e2 : list) { 
            if (e1 == null) {
                e1 = e2;
                l.add(e2);
            }
            else if (compare3(e1, e2) == 0){
                //  g s p o t1 vs g s p o t2
                //  keep g s p o t1
                if (e1.getReferenceNode() == null) { 
                    // e2 replace e1
                    share(e2, e1);
                    e1 = e2;
                    l.set(l.size()-1, e2);
                    count ++;
                }  
                else {
                    share(e1, e2);
                    merge(e1, e2);
                    // e1 replace e2
                    count ++;
                }
            }
            else if (Graph.TRIPLE_UNIQUE_NAME && compare2(e1, e2) == 0) {
                // g1 s p o t1 vs g2 s p o t2
                // replace by 
                // g1 s p o t1 vs g2 s p o t1
                name(e1, e2, l, i);
                e1 = l.get(l.size()-1);
            }
            else {
                e1 = e2;
                l.add(e2);
            }
            i++;
        }
        
        list = l;
        if (count > 0) {
            graph.setSize(graph.size() - count);
        }       
    }
    
    // e1 replace e2
    void share(Edge e1, Edge e2) {
       if (e2.isAsserted()) {
            e1.setAsserted(true);
        }
    }
      

     
    // keep only one metadata node (e1)
    void merge(Edge e1, Edge e2) {
        indexer.replace(e2.getReferenceNode(), e1.getReferenceNode());
    }
    
    /**
     * Replace subject/object by target node in map if any
     * In this case, these nodes are triple ID metadata
     */
    void replace(HashMap<Node, Node> map) {
        boolean b = false;
        for (Edge e : list) {
            for (int i = 0; i < 2; i++) {
                Node n = map.get(e.getNode(i));
                if (n != null) {
                    b = true;
                    e.setNode(i, n);
                }
            }
        }
        if (b) {
           complete();
        }
    }
    
    /** 
     *  e1 = g1 s p o t1 ; e2 = g2 s p o t2
     *  merge reference nodes and replace by: 
     *  g1 s p o t1 ; g2 s p o t1
     *  e1 is in list, e2 is candidate
     *  int i is index of e2 in Index main list
     */
    void name(Edge e1, Edge e2, List<Edge> edgeList, int i) {
        if (e1.getReferenceNode() != null) {
            if (e2.getReferenceNode() != null) {
                // replace reference node t2 of e2 with t1
                merge(e1, e2);
                e2.setNode(meta, e1.getReferenceNode());
            } else {
                // set/add reference node of e2 to t1
                e2 = graph.getEdgeFactory().name(e2, predicate, e1.getReferenceNode());
            }
        } else if (e2.getReferenceNode() != null) {
            // set/add reference node of e1 to t2
            e1 = graph.getEdgeFactory().name(e1, predicate, e2.getReferenceNode());
            edgeList.set(edgeList.size() - 1, e1);
        } else {
            Node ref = getSameEdgeReferenceNode(e2, i);
            // e1, e2 have no reference node
            // add reference node in case there is a third one with reference node
            // e1 = g1 s p o -> g1 s p o t
            // e2 = g2 s p o -> g2 s p o t
            if (ref != null) {
                e1 = graph.getEdgeFactory().name(e1, predicate, ref);
                e2 = graph.getEdgeFactory().name(e2, predicate, ref);
                edgeList.set(edgeList.size() - 1, e1);
            }
        }
        edgeList.add(e2);
    }
    
    /**
     * Is there an edge similar to e in Index list after index n
     * with a reference node
     * similar: same subject/object
     */
    Node getSameEdgeReferenceNode(Edge e, int n) {
        for (int i=n+1; i<list.size(); i++) {
            Edge e2 = list.get(i);
            if (compare2(e, e2) == 0) {
                if (e2.hasReference()) {
                    return e2.getReferenceNode();
                }
            }
            else {
                return null;
            }
        }
        return null;
    } 
      
    void complete() {
        sort();
        reduce(indexer.getNodeManager());
    }
    
    // compare subject object graph (not compare reference node if any)
//    int compare3old(Edge e1, Edge e2) {
//        int res = compareNodeTerm(e1.getNode(index), e2.getNode(index));
//        if (res == 0) {
//            res = compareNodeTerm(e1.getNode(other), e2.getNode(other));
//        }
//        if (res == 0) {
//            res = compareNodeTerm(e1.getNode(IGRAPH), e2.getNode(IGRAPH));
//        }
//        return res;
//    }
    
    // compare subject object graph (not compare reference node if any)
    int compare3(Edge e1, Edge e2) {
        int res = compare2(e1, e2);        
        if (res == 0) {
            res = compareNodeTerm(e1.getNode(IGRAPH), e2.getNode(IGRAPH));
        }
        return res;
    }
    
    //compare subject object (not compare graph)
    int compare2(Edge e1, Edge e2) {
        for (int i = 0; i < 2; i++) {
            int res = compareNodeTerm(e1.getNode(i), e2.getNode(i));
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }
    
    /**
     * 
     */
    void indexNodeManager(NodeManager mgr) {
        Edge pred = null;
        int ind = 0;
        for (Edge ent : list) {
            if (pred == null) {
                mgr.add(ent.getNode(index), predicate, ind);
            } else if (ent.getNode(index) != pred.getNode(index)) {
                mgr.add(ent.getNode(index), predicate, ind);
            }
            ind++;
            pred = ent;
        }
    }
    
    /**
     * Replace kg:rule Edge by compact EdgeInternalRule
     */
    
    void compact() {
        if (graph.isMetadata() || graph.isRDFStar()) {
            doCompactMetadata();
        }
        else {
            doCompact();
        }
    }

    // keep metadata, reset index
    void doCompactMetadata(){
        for (Edge ent : list) {
           ent.setIndex(-1);
        }
    }
    
    void doCompact(){
        ArrayList<Edge> l = new ArrayList<>(list.size());
        for (Edge ent : list) {
           Edge ee = graph.getEdgeFactory().compact(ent);
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
            for (Edge ent : el) {
                // if additional node is missing: do not index this edge
                if (ent.nbNode() > index) {
                    list.add(ent);
                }
            }
        }
    }

    void add(Edge ent) {
        list.add(ent);
    }

    void add(int i, Edge ent) {
        list.add(i, ent);
    }

    Edge remove(int i) {
        Edge ent = list.get(i);
        list.remove(i);
        return ent;
    }

    /**
     * PRAGMA: All Edge in list have p as predicate, no duplicates Use case:
     * Rule Engine
     */
    void add(List<Edge> l) {
        list.ensureCapacity(l.size() + list.size());
        list.addAll(l);
    }

    /**
     * Return place where edge should be inserted in this Index return -1 if
     * already exists
     */
    int getPlace(Edge edge) {
        int i = find(edge);

        if (i >= list.size()) {
            i = list.size();
        } else if (index == 0) {
            if (equalExceptIfMetadata(list.get(i), edge)) {
                // eliminate duplicate at load time for index 0                   
                return -1;
            }
        }

        return i;
    }
    
    int getPlace2(Edge edge) {
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
    int find(Edge edge) {
        return find(edge, 0, list.size());
    }

    int find(Edge edge, int first, int last) {
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
            Edge ent = list.get(n);
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
    int findIndexNodeTerm(Edge ent) {
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
    
    Edge findEdge(Edge edge) {
        int i = findIndexNodeTerm(edge);
        if (i == -1) {
            return null;
        }
        return list.get(i);
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

    boolean exist(Edge ent) {
        int i = findIndexNodeTerm(ent);
        return i != -1;
    }

    /**
     * Iterate edges with index node node1 (and other node node2)
     */
    Iterable<Edge> getEdges(Node node1, Node node2) {
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
     Iterable<Edge> getEdges(Node node) {
        // node is bound, enumerate edges where node = edge.getNode(index)
        int n = findNodeIndex(node);
        if (n >= 0 && n < list.size()) {
           return new EdgeManagerIterate(this, n);
        }
        return null;
    }
    
    Iterable<Edge> getEdges(Node node, int n) {
        return new EdgeManagerIterate(this, n);
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
    
    int findNodeIndex(Node node) {
        return getNodeIndex(node);
    }
    
    int getNodeIndex(Node n) {
        if (indexer.getNodeManager().isConsultable() 
                && ! n.getDatatypeValue().isNumber()
                && ! n.getDatatypeValue().isBoolean()){
            return  indexer.getNodeManager().getPosition(n, predicate);
        }
        return findNodeIndexBasic(n);
    }
    
    int findNodeIndexBasic(Node node) {
        int n = findNodeIndex(node, 0, list.size());
        if (n >= 0 && n < list.size()) {
            int i = getNodeIndex(n, index);
            if (i == node.getIndex()) {
                return n;
            }
        }
        return -1;
    }

    
    
    /**
     * return index of edge where edge.getNode(index).index() =  node.index()
     */
    int debugNodeIndex(Node n) {
        if (indexer.getNodeManager().isConsultable() 
                && ! n.getDatatypeValue().isNumber()
                && ! n.getDatatypeValue().isBoolean()){
            int i = indexer.getNodeManager().getPosition(n, predicate);
            int i2 = findNodeIndexBasic(n);
            if ((i < 0 && i2 >= 0)
                    || (i >= 0 && i2 < 0)
                    || (i >= 0 && i2 >= 0 && i != i2)) {
                System.out.println("**************** EM: " + n + " " + predicate + " " + list.size());
                System.out.println("**************** EM: " + i + " " + i2 + " " + n + " " + indexer.getNodeManager().size());
                System.out.println(indexer.getNodeManager().display());
                System.out.println(index + " : " + list);
            }
            else {
                return i;
            }
        }
   
        return findNodeIndexBasic(n);
    }
    
 
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
    
     int compare(Edge ent, Node n1, Node n2) {
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
    
    /**
     * compare named graph nodes
     * use case: find occurrence of delete edge
     * for rdf star edge with reference (see Construct find(edge))
     * named graph node of delete edge may be null
     * context: 
     * index == 0 (subject index)
     * edge subject and object are ==
     * if delete graph node is null, edge is found
     */
    int compareNodeTermNull(Node n1, Node n2) {
        if (n1 == null || n2 == null) {
            // occur only for find delete edge 
            // Construct getGraphManager().find(edge)
            return 0;
        }
        else {
            return compareNodeTerm(n1, n2);
        }
    }
    
    int compareNodeIndex(Node n1, Node n2){
        return intCompare(n1.getIndex(), n2.getIndex());       
    }
    
    IDatatype getValue(Node n){
        return  n.getValue();
    }

    @Override
    public Iterator<Edge> iterator() {
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
    @Deprecated
    class Iterate implements Iterable<Edge>, Iterator<Edge> {

        List<Edge> list;
        int node;
        int ind, start;

        Iterate(EdgeManager l, int n) {
            list = l.getList();
            node = getNodeIndex(list.get(n), index);
            start = n;
        }

        @Override
        public Iterator<Edge> iterator() {
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
        public Edge next() {
            Edge ent = list.get(ind++);
            return ent; 
        }

        @Override
        public void remove() {
        }         
    }
    
       

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(Edge ent, int n) {
        return ent.getNode(n).getIndex();
    }

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(int i, int n) {
        Edge ent = list.get(i);
        return ent.getNode(n).getIndex();
    }
    
    // getNode(IGRAPH) must return getGraph()
    Node getNode(int i, int n) {
        Edge ent = list.get(i);
        return ent.getNode(n);
    }
    

    /**
     * **************************************************************
     */
    Comparator<Edge> getComparator(int n) {
        switch (n) {
            case ILIST:
                return getListComparator();
        }
        return getComparator();
    }
    
    void setComparator(Comparator<Edge> c){
        comp = c;
    }


    /**
     * Compare two edges to sort them in Index
     * rdf star edge are equal when g s p o are equal 
     * wether or not there is a triple ref id
     * @note:  sort edge list is not deterministic for rdf star triple
     * list will be reduced by metadata()
     */
     Comparator<Edge> getComparator() {

        return new Comparator<Edge>() {
            
            @Override
            public int compare(Edge o1, Edge o2) {

                // check the Index Node
                int res = compareNodeTerm(o1.getNode(index), o2.getNode(index));
                if (res != 0) {                   
                    return res;
                }
                
                res = compareNodeTerm(o1.getNode(other), o2.getNode(other));
                if (res != 0) {
                    return res;
                }
                                
                if (o1.nbNode() == 2 && o2.nbNode() == 2 || graph.isMetadataNode()) {
                    // compare third Node (i.e. graph in the general case)
                    res = compareNodeTermNull(o1.getNode(next), o2.getNode(next));
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
    Comparator<Edge> getListComparator() {

        return new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                int i1 = o1.getIndex();
                int i2 = o2.getIndex();
                
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
