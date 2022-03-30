package fr.inria.corese.core.index;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import static fr.inria.corese.core.index.EdgeManagerIndexer.IGRAPH;
import static fr.inria.corese.core.index.EdgeManagerIndexer.ILIST;
import fr.inria.corese.core.util.Tool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Edge List of a predicate
 * Edge may be stored without predicate Node to spare memory
 * Edges are sorted according to 
 * 1- focus node index and then by compareTo when they have same index
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
    private EdgeManagerIndexer indexer;
    // Predicate of this EdgeManager: edges in this edge list have this predicate
    private Node predicate;
    private ArrayList<Edge> edgeList;
    // comparator to sort edge list: g s p o t < g s p o
    private Comparator<Edge> comparatorIndex;
    // comparator to retrieve place of edge: g s p o t = g s p o
    private Comparator<Edge> comparator;
    // index of first Node to sort 
    private int index = 0;
    // index of second Node to sort
    private int other = 0;
    // index of third Node to sort
    private int next = IGRAPH;
    boolean indexedByNode = false;

    EdgeManager(EdgeManagerIndexer indexer, Node p, int i) {
        graph = indexer.getGraph();
        this.indexer = indexer;        
        predicate = p;
        edgeList = new ArrayList<>();
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
        return getEdgeList();
    }

    public int size() {
        return getEdgeList().size();
    }

    void clear() {
        getEdgeList().clear();
    }

    Edge get(int i) {
        return getEdgeList().get(i);
    }
    
    int getIndex(){
        return index;
    }
    
  
    /**
     * input edge are sorted with reference node first: g s p o t <before> g s p o
     * remove duplicate edges, share reference node if any, share asserted if any
     * example:
     * g s p o t - <<g s p o t>> - g s p o - g1 s p o - g1 s p o - g2 s p o - g3 s p o t
     * reduce ->
     * g s p o t - g1 s p o t - g2 s p o t - g3 s p o t
     * In addition edges may be asserted or not
     * Remaining edge becomes asserted if one occurrence (with same g s p o) is asserted
     * Create NodeManager: node -> (predicate:position) 
     */
    int reduce(NodeManager nodeManager) {
        ArrayList<Edge> reduceNodeList = new ArrayList<>();
        Edge pred = null;
        int count = 0, ind = 0;
        int begin=0, end=0;
        //System.out.println("before reduce: " + list);
        boolean isMetadata = getGraph().isMetadataNode();
        
        for (Edge edge : getEdgeList()) {
            if (pred == null) {
                pred = edge;
                reduceNodeList.add(edge);
                begin = ind;
                //nodeManager.add(edge.getNode(getIndex()), getPredicate(), ind);
                ind++;
            } else if (equalWithoutConsideringMetadata(pred, edge)) {
                // g s p o = g s p o -- with or without reference node
                if (isMetadata) {
                    // rdf star graph
                    Node ref = pred.getReferenceNode();
                    if (ref == null) {
                        // pred have no reference node
                        ref = getGraph().getTripleReference(edge.getSubjectNode(), getPredicate(), edge.getObjectNode());
                    }
                    if (ref == null) {
                        // no reference node at all for any s p o
                        // skip edge
                        count++;
                    }
                    else {
                        // reference node exist for some s p o, 
                        // may be on pred g s p o or another following gi s p o
                        if (pred.getReferenceNode() == null) {
                            // pred must share ref node of other s p o
                            Edge predCopy = getGraph().getEdgeFactory().name(pred, getPredicate(), ref);
                            // replace pred by predCopy with shared ref node
                            reduceNodeList.set(reduceNodeList.size() - 1, predCopy);
                            pred = predCopy;
                            // skip edge
                            count++;
                        }
                        else {
                            // pred has reference node
                            if (edge.isAsserted()) {
                                // in case pred was not asserted
                                pred.setAsserted(true);
                            }
                            // skip edge
                            count++;
                        }
                    }
                }
                else {
                    // edge = pred, graph not metadata
                    // skip redundant edge
                    count++;
                }
            } else {
                reduceNodeList.add(edge);
                if (edge.getNode(getIndex()) != pred.getNode(getIndex())) {
                    nodeManager.add(pred.getNode(getIndex()), getPredicate(), begin, ind);
                    begin = ind;
                }
                pred = edge;
                ind++;
            }
        }
        
        if (pred!=null) {
            nodeManager.add(pred.getNode(getIndex()), getPredicate(), begin, reduceNodeList.size());
        }
        
        reduceNodeList.trimToSize();
        setEdgeList(reduceNodeList);
        if (count > 0) {
            graph.setSize(graph.size() - count);
        }
        //System.out.println("after reduce: " + list);
        return count;
    }   
    
    // when rdf star: equal g s p o without considering reference node t
    // g s p o = g s p o t = g s p o t2
    boolean equalWithoutConsideringMetadata(Edge e1, Edge e2) {
        return getComparatorEqualWithoutMetadata().compare(e1, e2) == 0;
    }
     
    NodeManager getNodeManager() {
        return getIndexer().getNodeManager();
    }
    
    
    // compare subject object graph (not compare reference node if any)
    int compare3(Edge e1, Edge e2) {
        int res = compare2(e1, e2);        
        if (res == 0) {
            res = compareNodeTerm(e1.getNode(IGRAPH), e2.getNode(IGRAPH));
        }
        return res;
    }
    
    // compare subject object (not compare graph)
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
     * generate map: node -> (predicate:position)
     * with position of node in this edge list of predicate  
     */
    void indexNodeManager(NodeManager nodeManager) {
        Edge pred = null;
        int begin = 0;
        int end = 0;
        
        for (Edge edge : getEdgeList()) {
            Node focus = edge.getNode(getIndex());
            if (pred == null) {
                // first edge
            } 
            else if (focus != pred.getNode(getIndex())) {
                nodeManager.add(pred.getNode(getIndex()), getPredicate(), begin, end);
                begin = end;
            }
            end++;
            pred = edge;
        }
        if (pred!=null) {
            nodeManager.add(pred.getNode(getIndex()), getPredicate(), begin, size());
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
        for (Edge ent : getEdgeList()) {
           ent.setEdgeIndex(-1);
        }
    }
    
    void doCompact(){
        ArrayList<Edge> l = new ArrayList<>(getEdgeList().size());
        for (Edge ent : getEdgeList()) {
           Edge ee = getGraph().getEdgeFactory().compact(ent);
           l.add(ee);
        }
        setEdgeList(l);
    }

    /**
     * Main function that sort Index edge list
     */ 
    void sort() {
        Collections.sort(getEdgeList(), getComparatorIndex());
    }

    /**
     * Copy Index(0) into this index
     */
    void copy(EdgeManager el) {
        getEdgeList().ensureCapacity(el.size());
        if (getIndex() < 2) {
            // we are sure that there are at least 2 nodes
            getEdgeList().addAll(el.getList());
        } else {
            for (Edge ent : el) {
                // if additional node is missing: do not index this edge
                if (ent.nbNode() > getIndex()) {
                    getEdgeList().add(ent);
                }
            }
        }
    }

    void add(Edge ent) {
        getEdgeList().add(ent);
    }

    void add(int i, Edge ent) {
        getEdgeList().add(i, ent);
    }
    
    void set(int i, Edge ent) {
        getEdgeList().set(i, ent);
    }

    Edge remove(int i) {
        Edge ent = getEdgeList().get(i);
        getEdgeList().remove(i);
        return ent;
    }

    /**
     * PRAGMA: All Edge in list have p as predicate, no duplicates Use case:
     * Rule Engine
     */
    void add(List<Edge> l) {
        getEdgeList().ensureCapacity(l.size() + getEdgeList().size());
        getEdgeList().addAll(l);
    }
    
    // pragma: node is bound
    // pragma: when node2 is bound: node=subject node2=object
    // use case: DataProducer iterator provides two nodes only for subject/object
    // otherwise it provides one node only
    Iterable<Edge> getEdges(Node node, Node node2) {
        if (node2 == null) {
            return getEdgesBasic(node);
        }
        else {
            return getEdgesBasic(node, node2);
        }
    }
    
    // specific iterator when subject/object are both known
    Iterable<Edge> getEdgesBasic(Node subject, Node object) {
        int beginIndex = 0;
        if (getNodeManager().isConsultable()) {
            beginIndex = getNodeManager().getPosition(subject, getPredicate());
            if (beginIndex < 0 || beginIndex >= size()) {
                return null;
            }
        }

        // index of edge: node predicate node2
        beginIndex = findEdgeNodeTerm(subject, object, beginIndex, size());
                
        if (beginIndex >= 0 && beginIndex < size()) {
            // iterate edges with node and node2
           return new EdgeManagerIterate(this, beginIndex, object.getIndex());
        }
        else if (getGraph().isDebugSparql()) {
            traceEdgeList();
        }
        
        return null;
    }
    
    // node is bound, enumerate edges where node = edge.getNode(index)
    Iterable<Edge> getEdgesBasic(Node node) {
        int beginIndex = findNodeIndex(node);
        getGraph().trace("Get edges: node=%s label=%s index=%s place=%s", 
                node, node.getLabel(), node.getIndex(), beginIndex);
        
        if (beginIndex >= 0 && beginIndex < getEdgeList().size()) {
           return new EdgeManagerIterate(this, beginIndex);
        }
        else if (getGraph().isDebugSparql()) {
            traceEdgeList();
        }
        
        return null;
    }
    
    void traceEdgeList() {
        for (Edge e : getEdgeList()) {
            System.out.println(String.format("%s label=%s index=%s", e,
                    e.getSubjectNode().getLabel(),
                    e.getSubjectNode().getIndex()));
        }
    }
    
    Iterable<Edge> getEdges(Node node, int beginIndex) {
        return new EdgeManagerIterate(this, beginIndex);
    }
    
        
    // use case: rdfs entailment
    boolean exist(Edge edge) {
        int i = findEdgeEqualWithoutMetadata(edge);
        return i != -1;
    }


    /**
     * Return place where edge should be inserted in this Index return -1 if
     * already exists
     */
    int getPlace(Edge edge) {
        int i = find(edge);
        if (i >= getEdgeList().size()) {
            i = getEdgeList().size();
        } 
        return i;
    }
   

    /**
     * Place of edge in this Index, e.g. to insert edge
     */
    int find(Edge edge) {
        return basicFind(getComparatorIndex(), edge, 0, getEdgeList().size());
    }

    /**
     * use case: Construct find occurrence of edge for rdf star
     * @todo: Index is sorted and reduced ?
     */
    Edge findEdge(Edge edge) {
        int i = findEdgeEqualWithoutMetadata(edge);
        getGraph().trace("Find edge: %s place=%s", edge, i);
        if (i == -1) {
            if (getGraph().isDebugSparql()) {
                traceEdgeList();
            }
            return null;
        }
        return getEdgeList().get(i);
    }

    /**
     * Find index of edge If not found, return -1
     */
    int findEdgeEqualWithoutMetadata(Edge edge) {
        int i = basicFind(getComparatorEqualWithoutMetadata(), edge, 0, getEdgeList().size());
        if (i >= size()) {
            return -1;
        }
        int res = getComparatorEqualWithoutMetadata().compare(edge, getEdgeList().get(i));
        if (res == 0) {
            return i;
        }

        return -1;
    }
    
    /**
     * There are two comparator
     * sort Index:     g s p o t < g s p o
     * retrieve Edge:  g s p o t = g s p o
     */
    int basicFind(Comparator<Edge> comp, Edge edge, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = comp.compare(getEdgeList().get(mid), edge);
            if (res >= 0) {
                return basicFind(comp, edge, first, mid);
            } else {
                return basicFind(comp, edge, mid + 1, last);
            }
        }
    }
     
    /**
     * Test if an edge (n1 p n2) exist in this Index (in any named graph)
     * use case: rule engine
     */
    boolean exist(Node n1, Node n2) {
        int n = findEdgeNodeTerm(n1, n2);
        if (n >= 0 && n < getEdgeList().size()) {
            Edge ent = getEdgeList().get(n);
            if (n1.getIndex() == getNodeIndex(ent, 0)
                    && n2.getIndex() == getNodeIndex(ent, 1)) {
                return true;
            }
        }
        return false;
    }
    
    Edge findEdge(Node n1, Node n2) {
        int n = findEdgeNodeTerm(n1, n2);
        if (n >= 0 && n < size()) {
            Edge edge = getEdgeList().get(n);
            if (compareNodeTerm(n1, edge.getNode(0)) == 0
             && compareNodeTerm(n2, edge.getNode(1)) == 0) {
                return edge;
            }
        }
        return null;
    }

    /**
     * return index of edge where
     * edge.getNode(index) == node1 and edge.getNode(other) == node2
     */
    int findEdgeNodeTerm(Node n1, Node n2) {
        return findEdgeNodeTerm(n1, n2, 0, size());
    }
    
    int findEdgeNodeTerm(Node n1, Node n2, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            if (compareNodeTerm(getEdgeList().get(mid), n1, n2) >= 0) {
                return findEdgeNodeTerm(n1, n2, first, mid);
            } else {
                return findEdgeNodeTerm(n1, n2, mid + 1, last);
            }
        }
    }   

    
   /**
     * Find index of node as node(index) -1 if not found
     * use case: index of graph node n in graph node index
     */
    int findIndex(Node n) {
        int i = findNodeTerm(n, 0, getEdgeList().size());
        if (i >= 0 && i < getEdgeList().size()
                && getNodeIndex(i, getIndex()) == n.getIndex()) {
            return i;
        }
        return -1;
    }

    /**
     * return index of edge where edge.getNode(index) sameTerm node
     */
    int findNodeTerm(Node n, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = compareNodeTerm(getNode(mid, getIndex()), n);
            if (res >= 0) {
                return findNodeTerm(n, first, mid);
            } else {
                return findNodeTerm(n, mid + 1, last);
            }
        }
    }
    
    // use case: getEdges(node)
    int findNodeIndex(Node node) {
        return getNodeIndex(node);
    }
    
    int getNodeIndex(Node n) {
        if (getNodeManager().isConsultable() 
                && ! n.getDatatypeValue().isNumber()
                && ! n.getDatatypeValue().isBoolean()){
            return  getNodeManager().getPosition(n, getPredicate());
        }
        return findNodeIndexBasic(n);
    }
    
    int findNodeIndexBasic(Node node) {
        int n = findNodeIndex(node, 0, getEdgeList().size());
        if (n >= 0 && n < getEdgeList().size()) {
            int i = getNodeIndex(n, getIndex());
            if (i == node.getIndex()) {
                return n;
            }
        }
        return -1;
    }

 
    int findNodeIndex(Node n, int first, int last) {
        if (first >= last) {
            return first;
        } else {
            int mid = (first + last) / 2;
            int res = compareNodeIndex(getNode(mid, getIndex()), n);
            if (res >= 0) {
                return findNodeIndex(n, first, mid);
            } else {
                return findNodeIndex(n, mid + 1, last);
            }
        }
    }
    
    
    int compareNodeTerm(Edge edge, Node n1, Node n2) {
        int res = compareNodeTerm(edge.getNode(getIndex()), n1);
        if (res == 0) {
            res = compareNodeTerm(edge.getNode(getOther()), n2);
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
            res = n1.getValue().compareTo(n2.getValue());
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
    
    @Override
    public Iterator<Edge> iterator() {
        return getEdgeList().iterator();
    }

    public Node getPredicate() {
        return predicate;
    }      

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(Edge ent, int n) {
        return ent.getNode(n).getIndex();
    }

    // getNode(IGRAPH) must return getGraph()
    int getNodeIndex(int i, int n) {
        Edge ent = getEdgeList().get(i);
        return ent.getNode(n).getIndex();
    }
    
    // getNode(IGRAPH) must return getGraph()
    Node getNode(int i, int n) {
        Edge ent = getEdgeList().get(i);
        return ent.getNode(n);
    }
    

    /**
     * **************************************************************
     * There are two comparator
     * sort Index:     g s p o t < g s p o
     * retrieve Edge:  g s p o t = g s p o
     * after sort Index, reduce() + metadate() remove duplicates
     * and we keep only g s p o t (g s p o is removed if any)
     * At the end, the index contains only g s p o t (if any t)
     * Then, to retrieve an edge g s p o [t], we can consider edge equality 
     * without looking at metadata (because they are not both in the Index)
     */
    Comparator<Edge> createComparatorIndex(int n) {
        switch (n) {
            case ILIST:
                return createListComparator();
        }
        return createComparatorIndex();
    }
    
    Comparator<Edge> createComparator(int n) {
        switch (n) {
            case ILIST:
                return createListComparator();
        }
        return createComparator();
    }
    
    // called by EdegeManagerIndexer because every EdgeManager share same comparator object
    void setComparatorIndex(Comparator<Edge> c){
        comparatorIndex = c;
    }
    
    // used to sort edgeList and find edge place in index
    // g s p o t < g s p o
    Comparator<Edge> getComparatorIndex() {
        return comparatorIndex;
    }  
    
    // used to compare and retrieve edge when Index is sorted and reduced 
    // g s p o t = g s p o
    Comparator<Edge> getComparatorEqualWithoutMetadata() {
        return comparator;
    }

    void setComparator(Comparator<Edge> c){
        comparator = c;
    }
    
    /**
     * Compare two edges to sort and retrieve them in Index
     * rdf star edges are compared on g s p o only, not on ref id
     * hence they are considered equal when g s p o are equal 
     * not considering triple ref id
     * g s p o = g s p o t
     * @note:  
     * sort is not deterministic for rdf star triple with same g s p o 
     * list will be reduced by metadata()
     */
    
    // compare edges
    // g s p o t = g s p o
    Comparator<Edge> createComparator() {
        return createComparator(true);
    }
    
    // sort edgeList
    // true:  g s p o t = g s p o
    // false: g s p o t < g s p o
    Comparator<Edge> createComparatorIndex() {
        return createComparator(false);
    }
       
    Comparator<Edge> createComparator(boolean equalWithoutConsideringMetadataNode) {

        return new Comparator<>() {
            boolean equalWithoutConsideringMetadata = equalWithoutConsideringMetadataNode;
     
            @Override
            public int compare(Edge e1, Edge e2) {
                
                // check the Index Node
                int res = compareNodeTerm(e1.getNode(getIndex()), e2.getNode(getIndex()));
                if (res != 0) {  
                    //System.out.println("subject: " + res);
                    return res;
                }
                
                res = compareNodeTerm(e1.getNode(getOther()), e2.getNode(getOther()));
                if (res != 0) {
                    //System.out.println("object: " + res);
                    return res;
                }
                                
                if (e1.nbNodeIndex()== 2 && e2.nbNodeIndex()== 2) {
                    // compare third Node (i.e. graph node in the general case)
                    res = compareNodeTermNull(e1.getNode(getNext()), e2.getNode(getNext()));
                    //System.out.println("graph: " + res);                    
                    return res;
                }
                
                // one of them has metadata, compare third Node
                if (getGraph().isMetadataNode()) {
                    // rdf star:  g s p o = g s p o t = g s p o t2
                    // compare third Node (i.e. graph in the general case)
                    res = compareNodeTermNull(e1.getNode(getNext()), e2.getNode(getNext()));
                    if (res == 0) {
                        // equal on g s p o
                        //System.out.println("equalWithoutConsideringMetadata: " + equalWithoutConsideringMetadata);
                        if (equalWithoutConsideringMetadata) {
                            //System.out.println("meta1: " + res);
                            return res;
                        }
                        else {
                            res = compareEqualWhenMetadata(e1, e2);
                            //System.out.println("meta2: " + res);
                            return res;
                        }
                    }
                    else {
                        //System.out.println("meta3: " + res);
                        return res;
                    }
                }

                // more than two nodes, not rdf star
                // common arity
                int min = Math.min(e1.nbNodeIndex(), e2.nbNodeIndex());

                for (int i = 0; i < min; i++) {
                    // check other common arity nodes
                    if (i != getIndex()) {
                        res = compareNodeTerm(e1.getNode(i), e2.getNode(i));
                        if (res != 0) {
                            return res;
                        }
                    }
                }

                if (e1.nbNodeIndex() == e2.nbNodeIndex()) {
                    // same arity, nodes are equal
                    // check graph node
                    return compareNodeTerm(e1.getNode(IGRAPH), e2.getNode(IGRAPH));
                }
                else if (e1.nbNodeIndex() < e2.nbNodeIndex()) {
                    // smaller arity edge is before
                    return -1;
                } else {
                    return 1;
                }

            }
        };
    }
     
     /**
      * Edges are equal on g s p o
      * edge with metadata < edge without metadata
      */ 
     int compareEqualWhenMetadata(Edge e1, Edge e2) {
         if (e1.hasReferenceNode()) {
             if (e2.hasReferenceNode()) {
                 return compareNodeTerm(e1.getReferenceNode(), e2.getReferenceNode());
             }
             else {
                 return -1;
             }
         }
         else if (e2.hasReferenceNode()) {
             return 1;
         }
         else {
             return 0;
         }
     }

    /**
     * sort in reverse order of edge timestamp
     * new edge first (for RuleEngine)
     */
    Comparator<Edge> createListComparator() {

        return new Comparator<>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                int i1 = o1.getEdgeIndex();
                int i2 = o2.getEdgeIndex();
                
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

    public ArrayList<Edge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(ArrayList<Edge> edgeList) {
        this.edgeList = edgeList;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public EdgeManagerIndexer getIndexer() {
        return indexer;
    }

    public void setIndexer(EdgeManagerIndexer indexer) {
        this.indexer = indexer;
    }

}
