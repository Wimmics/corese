package fr.inria.corese.core.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.Index;
import fr.inria.corese.core.Serializer;
import fr.inria.corese.core.index.PredicateList.Cursor;
import fr.inria.corese.core.util.Property;
import java.util.HashMap;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 * Table property node -> List<Edge>
 * Edge may be stored using internal structure without property Node to spare memory
 * Sorted by getNode(index), getNode(other) 
 * At the beginning, only table of
 * index 0 is fed with edges 
 * Other index are built at runtime on demand: 
 * ?x p ?y . ?z q ?y 
 * Index(1) of q is built for 2nd triple pattern
 * Hence Index(1) may be partial (not for all properties)
 * Nodes are sorted by Node index and then by compareTo when they are equal
 * Nodes with same node index which are not sameTerm are kept in the list:
 * s p 01, 1, 1.0, '1'^^xsd:long, 1e1
 * 
 * NodeManager is a Node Index with a table:
 * s -> (p1:i1 .. pn:in)
 * with for each node s the list of its predicates and for each predicate p
 * the position i of the triple s p o in the graph index edge list
 * given s and p we have direct access to triple s p o in the graph index at position i
 * given s we have the list of predicates of s 
 * hence ?s ?p ?o iterates directly the list of predicates of each subject s
 * very interesting when there are many candidate properties
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EdgeManagerIndexer 
        implements Index {
    public static boolean TRACE_REDUCE = false;
    public static boolean TRACE_INSERT = false;
    // draft test to iterate edge list with subList(b, e) with rdf star only (predicate!=null)
    public static boolean ITERATE_SUBLIST = false;
    public static boolean RECORD_END = false;
    // true: store internal Edge without predicate Node
    public static boolean test = true;
    private static final String NL = System.getProperty("line.separator");
    static final int IGRAPH = Graph.IGRAPH;
    static final int ILIST = Graph.ILIST;
    private static Logger logger = LoggerFactory.getLogger(EdgeManagerIndexer.class);
    private boolean byIndex = true;
    int index = 0, other = 1;
    int count = 0;
    int scoIndex = -1, typeIndex = -1;
    boolean isDebug = !true,
            isUpdate = true,
            isIndexer = false,
            // do not create entailed edge in kg:entailment if it already exist in another graph
            isOptim = false;
    Comparator<Edge> comparatorIndex, comparator;
    private Graph graph;
    List<Node> sortedProperties;
    PredicateList sortedPredicates;
    // Property Node -> Edge List 
    HashMap<Node, EdgeManager> table;
    private NodeManager nodeManager;
    //TransitiveEdgeManager transitiveManager;
    private boolean debug = false;

    public EdgeManagerIndexer(Graph g, boolean bi, int index) {
        init(g, bi, index);
        table = new HashMap();
        nodeManager = new NodeManager(g, index);
    }

    void init(Graph g, boolean bi, int n) {
        setGraph(g);
        index = n;
        byIndex = bi;
        switch (index) {
            case 0:
                other = 1;
                break;
            default:
                other = 0;
                break;
        }
    }
    
    @Override
    public NodeManager getNodeManager() {
        return nodeManager;
    }
    
    Graph getGraph() {
        return graph;
    }

    @Override
    public int size() {
        return table.size();
    }

    void put(Node n, EdgeManager l) {
        table.put(n, l);
    }

    /**
     * Return edges as they are stored, in internal format
     */
    @Override
    public EdgeManager get(Node n) {
        return table.get(n);
    }
    
    /**
     * Iterate edges in external format as complete Edge
     */
    Iterable<Edge> extGet(Node p){
        return getEdges(p, null, null);
    }

     int intCompare(int n1, int n2) {
        if (n1 < n2) {
            return -1;
        } else if (n1 == n2) {
            return 0;
        } else {
            return +1;
        }
    }
     
     int nodeCompare(Node n1, Node n2){
         return intCompare(n1.getIndex(), n2.getIndex());
     }

    @Override
    public boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex();
    }

   

    Node getNode(Edge ent, int n) {
//        if (n == IGRAPH) {
//            return ent.getGraph();
//        }
        return ent.getNode(n);
    }

    /**
     * Ordered list of properties For pprint TODO: optimize it
     */
    @Override
    public List<Node> getSortedProperties() {
        if (isUpdate) {
            sortProperties();
            isUpdate = false;
            sortedPredicates = new PredicateList(sortedProperties);
        }
        return sortedProperties;
    }
    
    @Override
    public PredicateList getSortedPredicates() {
        getSortedProperties();
        return sortedPredicates;
    }
    
    @Override
    public int nbProperties() {
        return table.size();
    }

    synchronized void sortProperties() {
        sortedProperties = new ArrayList<Node>();
        for (Node pred : getProperties()) {
            sortedProperties.add(pred);
        }
        Collections.sort(sortedProperties, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.compare(o2);
            }
        });
    }

    @Override
    public Iterable<Node> getProperties() {
        return table.keySet();
    }

    @Override
    public void setDuplicateEntailment(boolean b) {
        isOptim = !b;
    }

    /**
     * 
     * The index of Node the Index is sorted with
     */
    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Iterate edges in external format
     */
    @Override
    public Iterable<Edge> getEdges() {
        MetaIterator<Edge> meta = new MetaIterator<>();
        for (Node pred : getSortedProperties()) {
            Iterable<Edge> it = extGet(pred);
            meta.next(it);
        }
        if (meta.isEmpty()) {
            return null;
        }
        return meta;
    }

    @Override
    public String toString() {
        return toRDF();
    }

    @Override
    public String toRDF() {
        Serializer sb = new Serializer();
        sb.open("kg:Index");
        sb.appendPNL("kg:index ", index);
        int total = 0;
        int count = 0;
        for (Node pred : getSortedProperties()) {
            int i = get(pred).size();
            if (i > 0) {
                total += i;
                sb.append("kg:item [ ");
                sb.appendP("kg:num ", count++);
                sb.appendP("rdf:predicate ", pred);
                sb.append("rdf:value ", i);
                sb.appendNL("] ;");
            }
        }
        sb.appendNL("kg:total ", total);
        sb.close();
        return sb.toString();
    }

    @Override
    public int cardinality() {
        int total = 0;
        for (Node pred : getProperties()) {
            total += get(pred).size();
        }
        return total;
    }

    /**
     * Clean the content of the Index but keep the properties Hence Index can be
     * reused
     */
    @Override
    public void clean() {
        for (Node p : getProperties()) {
            get(p).clear();
        }
    }

    @Override
    public void clear() {
        recordUpdate(true);
        if (index == 0) {
            logClear();
        }
        table.clear();
        getNodeManager().clear();
    }

    @Override
    public void clearIndex(Node pred) {
        EdgeManager l = get(pred);
        if (l != null) {         
            l.clear();
        }
    }

    @Override
    public void clearCache() {
        for (Node pred : getProperties()) {
            clearIndex(pred);
        }
    }

    /**
     * Add a property in the table
     */
    @Override
    public Edge add(Edge edge) {
        return add(edge, false);
    }

    // generate internal representation for edge, 
    // possibly without predicate and named graph if kg:default
    Edge internal(Edge edge){
        return getGraph().getEdgeFactory().internal(edge);
    }
    
    /**
     * Add edge in internal format that may be more synthetic
     * predicate and graph may be omitted
     */
    @Override
    public Edge add(Edge edge, boolean duplicate) {
        if (index != IGRAPH && edge.nbNode() <= index) {
            // use case:  additional node is not present, do not index on this node
            // never happens for subject object and graph
            return null;
        }
        Edge internal = internal(edge); 
        EdgeManager el = define(edge.getEdgeNode());
        //System.out.println("add issort: " + isSort(edge) + " " + edge);
        if (isSort(edge)) {
            // edges are sorted, check presence by dichotomy
            int i = el.getPlace(edge);
            trace("insert: %s at %s", edge, i);
            
            if (getIndex() == 0) {
                if (getGraph().isFormerMetadata()) {
                    // add edge with metadata take care of it
                    // ok
                }
                else if (i < el.size() &&
                        el.equalWithoutConsideringMetadata(el.get(i), edge)) {
                    // eliminate duplicate at insertion time for index 0 
                    if (edge.isAsserted() && el.get(i).isNested()) {
                        el.get(i).setAsserted(true);
                    }
                    i = -1;
                }
            }
            
            if (i == -1) {
                trace("skip insert edge: ", edge);
                count++;
                return null;
            }       

            if (onInsert(edge)) {
                if (getGraph().isFormerMetadata()) {
                    // rdf star edge with reference node: g s p o t
                     return addWithMetadata(el, edge, internal, i);
                }
                else {
                    el.add(i, internal);
                    logInsert(edge);
                }
            } else {
                return null;
            }
        } else {
            // edges are not already sorted (load time)
            // add all edges, duplicates will be removed later when first query occurs
            if (onInsert(edge)) {
                el.add(internal);
                logInsert(edge);
            } else {
                return null;
            }
        }

        return edge;
    }
    
    /**
     * g s p o t  before  g s p o
     * edge with ref node    compare with index i
     * edge without ref node compare with index i-1
     * check redundancy in 4 cases:
     * edge g s p o t vs edgeList (g s p o t) or (g s p o)
     * edge g s p o   vs edgeList (g s p o t) or (g s p o)
     * 
    */ 
    Edge addWithMetadata(EdgeManager el, Edge edge, Edge internal, int i) {
        trace("insert: %s at %s", edge, i);
        if (el.getEdgeList().isEmpty()) {
            el.add(i, edge);
            logInsert(edge);
        }
        else if (edge.hasReferenceNode()) {
            if (i == el.getEdgeList().size()) {
                el.add(i, edge);
                share(el, edge, i);
                logInsert(edge);
            }
            else {
                Edge current = el.get(i); 
                trace("current: %s", current);
                if (el.equalWithoutConsideringMetadata(current, edge)) {
                    trace("they are equal");
                    if (!current.hasReferenceNode()) {
                        trace("set edge at: %s", i);
                        // g s p o t replace g s p o
                        el.set(i, edge);
                        if (current.isAsserted()) {
                            edge.setAsserted(true);
                        }
                        share(el, edge, i);
                    }
                    // in case edge does not replace current
                    if (edge.isAsserted()) {
                        current.setAsserted(true);
                    }
                    return null;
                } else {
                    trace("insert at: %s", i);
                    el.add(i, edge);
                    share(el, edge, i);
                    logInsert(edge);
                }
            }
        }
        else {
            // edge has no reference node
            if (i < el.getEdgeList().size()) {
                Edge current = el.get(i);
                if (el.equalWithoutConsideringMetadata(current, edge)) {
                    // current has no reference node (otherwise i would be just after current, i.e. i+1)
                    // skip edge
                    return null;
                }
            }

            if (i == 0) {
                insertEdgeWhenMetadata(el, edge, internal, i);
            } else {
                Edge current = el.get(i - 1);
                if (el.equalWithoutConsideringMetadata(current, edge)) {
                    // current has reference node because it is before i
                    // edge has no reference node => current asserted
                    current.setAsserted(true);
                    return null;
                } else {
                    insertEdgeWhenMetadata(el, edge, internal, i);
                }
            }      
        }
        
        return edge;
    }
    
    void trace(String mes, Object... obj) {
        if (TRACE_INSERT) {
            System.out.println(String.format(mes, obj));
        }
    }
    
    /**
     * edge with reference inserted at index i
     * share its reference with equal edge before and after i
     */
    void share(EdgeManager el, Edge edge, int n) {
        trace("share: %s at %s", edge, n);
        boolean loop = true;        
        for (int i = n + 1; i < el.size() && loop; i++) {
            loop = shareReference(el, edge, i);
        }
        
        loop = true;
        for (int i = n - 1; i >= 0 && loop; i--) {
            loop = shareReference(el, edge, i);
        }

    }
    
    /**
     * return true while get(i) == edge
     */
    boolean shareReference(EdgeManager el, Edge edge, int i) {
        Edge e = el.get(i);
        trace("share test: %s", e);
        if (el.compare2(e, edge) == 0) {
            // same s p o, g may differ
            trace("they are equal");
            if (!e.hasReferenceNode()) {
                trace("copy reference node");
                Edge copy = getGraph().getEdgeFactory().name(e, el.getPredicate(), edge.getReferenceNode());
                el.set(i, copy);
                return true;
            }
        }
        else {
            trace("thay are not equal");
        }
        return false;
    }
    
    /**
     * insert edge with no reference
     * if there exist similar edge with reference, 
     * this edge share reference
     */
    void insertEdgeWhenMetadata(EdgeManager el, Edge edge, Edge internal, int i) {
        Node ref = getGraph().getTripleReference(edge);
        if (ref == null) {
            el.add(i, internal);
        } else {
            Edge copy = getGraph().getEdgeFactory().name(edge, el.getPredicate(), ref);
            el.add(i, copy);
        }
        logInsert(edge);
    }
    
    /**
     * PRAGMA: 
     * This is already reduced() ie there is no duplicates in this manager
     * All Edge in list have p as predicate 
     * Use case: Rule Engine
     */
    @Override
    public void add(Node p, List<Edge> list) {
        EdgeManager l = define(p);
        if (index == 0 || l.size() > 0) {
            l.add(list);
        }
        index(p);
        if (index == 0) {
            recordUpdate(true);
        }
    }

    Edge tag(Edge ent) {
        getGraph().tag(ent);
        return ent;
    }


    EdgeManager getListByLabel(Edge e) {
        Node pred = getGraph().getPropertyNode(e.getEdgeNode().getLabel());
        if (pred == null) {
            return null;
        }
        return get(pred);
    }

    @Override
    public boolean exist(Edge edge) {
        if (index != IGRAPH && edge.nbNode() <= index) {
            // use case:  additional node is not present, do not index on this node
            // never happens for subject object and graph
            return false;
        }
        EdgeManager list = getListByLabel(edge);
        if (list == null) {
            return false;
        }
        return list.exist(edge);
    }
    
    /**
     * use case: Construct find occurrence of edge for rdf star
     * Index is sorted and reduced
     */
    @Override
    public Edge find(Edge edge) {
        EdgeManager list = getListByLabel(edge);
        if (list == null) {
            getGraph().trace("Find edge: undefined property %s", edge);
            return null;
        }
        return list.findEdge(edge);
    }


    boolean isSort(Edge edge) {
        return !getGraph().isIndexable();
    }

    /**
     * Store that the property exist by creating an empty list It may be fed
     * later if we need a join at getNode(index) If the list already contains
     * edges, we add it now.
     */
    @Override
    public void declare(Edge edge) {
        declare(edge, true);
    }

    @Override
    public void declare(Edge edge, boolean duplicate) {
        EdgeManager list = define(edge.getEdgeNode());
        if (list.size() > 0) {
            add(edge, duplicate);
        }      
    }

    /**
     * Create and store an empty list if needed
     */
    private EdgeManager define(Node predicate) {
        EdgeManager list = get(predicate);
        if (list == null) {
            list = new EdgeManager(this, predicate, index);
            // comparator index: g s p o t < g s p o
            list.setComparatorIndex(getCreateComparatorIndex(list));
            // comparator basic: g s p o t = g s p o 
            list.setComparator(getCreateComparator(list));            
            put(predicate, list);
        }
        return list;
    }
    
    /**
     * 
     * All EdgeManager(index) share same Comparator
     */       
    Comparator<Edge> getCreateComparatorIndex(EdgeManager el){
        if (comparatorIndex == null){
            comparatorIndex = el.createComparatorIndex(index);
        }
        return comparatorIndex;
    }
    
    Comparator<Edge> getCreateComparator(EdgeManager el){
        if (comparator == null){
            comparator = el.createComparator(index);
        }
        return comparator;
    }

    /**
     * Sort edges by values of first Node and then by value of second Node
     */
    @Override
    public void index() {
        index(true);
    }

    void index(boolean reduce) {
        for (Node pred : getProperties()) {
            basicIndex(pred);
        }
        if (reduce && index == 0) {
            reduce();
        }
    }
    
    /**
     * To be called after reduce is done
     * Generate Node Index: node -> (predicate:position)
     */
    @Override
    public void indexNodeManager() {
        getNodeManager().start();
        for (Node pred : getSortedProperties()) {
            checkGet(pred).indexNodeManager(getNodeManager());
        }
        getNodeManager().finish();
    }
    
    /**
     * called on every Index
     * Desactivate nodeManager because we add triples 
     * hence subjects may have new properties 
     * hence nodeManager content is obsolete
     * PRAGMA: it does not reduce
     */
    @Override
    public void index(Node pred) {
        getNodeManager().desactivate();
        basicIndex(pred);
    }

    void basicIndex(Node pred) {
        get(pred).sort();
    }

 
    /**
     * eliminate duplicate edges
     * index NodeManager
     */
    private void reduce() {
        if (TRACE_REDUCE) {
            System.out.println("before reduce:\n" + getGraph().display());
        }
        getNodeManager().start();
        for (Node pred : getSortedProperties()) {
            reduce(pred);
        }
        getNodeManager().finish();
        if (TRACE_REDUCE) {
            System.out.println("after reduce:\n" + getGraph().display());
        }
    }

    private void reduce(Node pred) {
        get(pred).reduce(getNodeManager());       
    }
    
    @Override
    public void indexNode() {
        for (Node pred : getProperties()) {
            for (Edge ent : get(pred)) {
                getGraph().define(ent);
            }
        }
    }

   @Override
    public void compact(){
        for (Node pred : getProperties()) {
                EdgeManager el = get(pred);
                el.compact();
        }
    }

    @Override
    public int duplicate() {
        return count;
    }

    /**
     * Check that this table has been built and initialized (sorted) for this
     * predicate If not, copy edges from table of index 0 and then sort these
     * edges
     */
      EdgeManager checkGet(Node pred) {
        if (index == 0) {
            return get(pred);
        } else {
            return synCheckGet(pred);
        }
    }


    /**
     * Create the index of property pred on nth argument It is synchronized on
     * pred hence several synGetCheck can occur in parallel on different pred
     */
    private EdgeManager synCheckGet(Node pred) {
        synchronized (pred) {
            EdgeManager list = get(pred);
            if (list != null && list.size() == 0) {
                EdgeManager std = (EdgeManager) getGraph().getIndex().get(pred);
                list.copy(std);
                list.sort();
            }
            return list;
        }
    }
    
    @Override
    public Iterable<Edge> getSortedEdges(Node node) {
        if (ITERATE_SUBLIST) {
            return getSortedEdgesSubList(node);
        }
        else {
            return getSortedEdgesBasic(node);
        }
    }

    
    // use case: node ?p ?o where ?p is unbound
    // get node predicate list
    public Iterable<Edge> getSortedEdgesBasic(Node node) {
        PredicateList list = getNodeManager().getPredicates(node);
        MetaIterator<Edge> meta = new MetaIterator<>();
        int i = 0;
        for (Node pred : list.getPredicateList()) {
            Iterable<Edge> it = getEdges(pred, node, list.getPosition(i++));
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<>();
        }
        return meta;
    }
    
    public Iterable<Edge> getSortedEdgesSubList(Node node) {
        PredicateList list = getNodeManager().getPredicates(node);
        MetaIterator<Edge> meta = new MetaIterator<>();
        int i = 0;
        for (Node pred : list.getPredicateList()) {
            Iterable<Edge> it = getEdgeSubList(pred, node, list.getCursor(i++));
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<>();
        }
        return meta;
    }
    
    Iterable<Edge> getEdgeSubList(Node p, Node n, Cursor cursor) {
        if (cursor == null) {
            return getEdges(p, n);
        }
        EdgeManager man = checkGet(p);
        return man.getEdgeList().subList(cursor.getBegin(), cursor.getEnd());
    }

    /**
     * Return iterator of Edge with possibly node as element
     */
    @Override
    public Iterable<Edge> getEdges(Node pred, Node node) {
        return getEdges(pred, node, null);
    }

    @Override
    public Iterable<Edge> getEdges(Node pred, Node node, Node node2) {
        EdgeManager list = checkGet(pred);
        if (list == null){ 
            return list;
        }
        else if (node == null){
            return new EdgeManagerIterate(list);
        }
        else {
            return list.getEdges(node, node2);
        }
    }
    
    @Override
    public Iterable<Edge> getEdges(Node pred, Node node, int beginIndex) {
        if (beginIndex == -1){
            return getEdges(pred, node, null);
        }
        EdgeManager list = checkGet(pred);
        if (list == null){ 
            return list;
        }
        else {
            if (debug) {
                logger.info("getEdges: " + pred + " " + node + " " + beginIndex);
            }
            return list.getEdges(node, beginIndex);
        }    
    }

    @Override
    public int size(Node pred) {
        EdgeManager list = get(pred);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

   
    /**
     * Written for index = 0
     */
    @Override
    public boolean exist(Node pred, Node n1, Node n2) {
        EdgeManager list = checkGet(pred);
        if (list == null) {
            return false;
        }
        return list.exist(n1, n2);
    }
    
    public Edge findEdge(Node s, Node p, Node o) {
        EdgeManager list = checkGet(p);
        if (list == null) {
            return null;
        }
        return list.findEdge(s, o);
    }

    
    public boolean isByIndex() {
        return byIndex;
    }

    
    @Override
    public void setByIndex(boolean byIndex) {
        this.byIndex = byIndex;
        index(false);
    }

   

    /**
     * **********************************************************************
     *
     * Update
     *
     * TODO: remove nodes from individual & literal if needed
     *
     * TODO: semantics of default graph (kgraph:default vs union of all graphs)
     *
     */
    @Override
    public Edge delete(Edge edge) {
        Node pred = getGraph().getPropertyNode(edge.getEdgeNode());
        if (pred == null){
            return null;
        }
        return delete(pred, edge);
    }

    @Override
    public Edge delete(Node pred, Edge edge) {
        if (index != IGRAPH && edge.nbNode() <= index) {
            // use case:  additional node is not present, do not index on this node
            // never happens for subject object and graph
            return null;
        }
        EdgeManager list = get(pred);
        
        if (list == null) {
            return null;
        }

        int i = list.findEdgeEqualWithoutMetadata(edge);

        if (i == -1) {
            return null;
        }
        
        Edge target = list.get(i);
        
        if (AccessRight.acceptDelete(edge, target)
                && accept(edge, target)) {
                       
            if (getGraph().isRDFStar() && target.hasReferenceNode()) {  
                // delete tuple(s p o t)
                if (superUser(edge)) {
                    remove(list, i);
                }
                else {
                    // target = tuple(s p o t) with possibly t q v
                    // set target as nested triple instead of deleting target
                    // @todo: we may also delete t q v in the same delete operation 
                    // in this case we should delete target ...
                    target.setNested(true);
                }               
            } 
            else {
                remove(list, i);
            }
            
            logDelete(target);
            return target;
        } 
        
        return null;
    }
    
    void remove(EdgeManager list, int i) {
        if (getIndex() == 0) {
            getGraph().setSize(getGraph().size() - 1);
        }
        list.remove(i);
    }
    
    boolean superUser(Edge edge) {
        return Property.booleanValue(Property.Value.RDF_STAR_DELETE);
    }
    
    /**
     * general case: asserted delete asserted 
     * super user:   asserted delete asserted & nested ;
     *               nested delete nested
     */
    boolean accept(Edge edge, Edge target) {
        if (edge.isAsserted()) { 
            return target.isAsserted() ||  superUser(edge);
        } 
        else if (target.isNested() && superUser(edge)) {
            // nested delete nested when super user
            return true;
        }
        else {
            return false;
        }
    }

    boolean onInsert(Edge ent) {
        return getGraph().onInsert(ent);
    }

    void logDelete(Edge ent) {
        if (ent != null) {
            recordUpdate(true);
            if (getIndex() == 0) {
                getGraph().logDelete(ent);
            }
        }
    }
    
    void recordUpdate(boolean b) {
        setUpdate(b);
        if (index == 0) {
            // tell all Index that update occur
            getGraph().declareUpdate(b);
        }
    }
    
    @Override
    public void declareUpdate(boolean b) {
        setUpdate(b);
    }
    
    void setUpdate(boolean b) {
        isUpdate = b;
    }

    void logInsert(Edge ent) {
        recordUpdate(true);
        if (getIndex() == 0) {
            getGraph().logInsert(ent);
        }
    }

    void logClear() {
        for (Node node : getSortedProperties()) {
            for (Edge ent : get(node)) {
                logDelete(ent);
            }
        }
    }

   

    /**
     * PRAGMA
     * Update functions are called with index = IGRAPH
     */
    @Override
    public void clear(Node gNode) {
        update(gNode, null, Graph.CLEAR);
    }

    @Override
    public void copy(Node g1, Node g2) {
        update(g2, null, Graph.CLEAR);
        update(g1, g2, Graph.COPY);
    }

    @Override
    public void move(Node g1, Node g2) {
        update(g2, null, Graph.CLEAR);
        update(g1, g2, Graph.MOVE);
    }

    @Override
    public void add(Node g1, Node g2) {
        update(g1, g2, Graph.COPY);
    }

    private void update(Node g1, Node g2, int mode) {
        for (Node pred : getProperties()) {

            EdgeManager list = checkGet(pred);
            if (list == null) {
                continue;
            }

            if (isDebug) {
                for (Edge ee : list) {
                    logger.debug("** EI: " + ee);
                }
            }

            int n = list.findIndex(g1);
            if (isDebug) {
                logger.debug("** EI find: " + g1 + " " + (n != -1));
            }

            if (n == -1) {
                continue;
            }

            update(g1, g2, pred, list, n, mode);

        }
    }

    private void update(Node g1, Node g2, Node pred, EdgeManager list, int n, int mode) {
        boolean isBefore = false;
        
        if (g2 != null && nodeCompare(g1, g2) < 0) {
            isBefore = true;
        }
        
        for (int i = n; i < list.size();) {
            Edge ent = list.get(i);
            Edge ee;
            if (same(getNode(ent, index), g1)) {

                switch (mode) {

                    case Graph.CLEAR:
                        clear(pred, ent);
                        list.remove(i);
                        break;

                    case Graph.MOVE:
                        clear(pred, ent);
                        list.remove(i);
                        ee = copy(g2, pred, ent);
                        if (isBefore) {
                        } else if (ee != null) {
                            i++;
                        }
                        break;

                    case Graph.COPY:
                        ee = copy(g2, pred, ent);
                        // get next ent
                        i++;
                        // g2 is before g1 hence ent was added before hence incr i again
                        if (isBefore) {
                        } else if (ee != null) {
                            i++;
                        }
                        break;
                }
            } else {
                break;
            }
        }
    }

    private Edge copy(Node gNode, Node pred, Edge ent) {
        return getGraph().copy(gNode, pred, ent);
    }

    private void clear(Node pred, Edge ent) {
        for (EdgeManagerIndexer ei : getGraph().getIndexList()) {
            if (ei.getIndex() != IGRAPH) {
                Edge rem = ei.delete(pred, ent);
                if (isDebug && rem != null) {
                    logger.debug("** EI clear: " + ei.getIndex() + " " + rem);
                }
            }
        }
    }

    @Override
    public void delete(Node pred) {
    }
    
    public void finishRuleEngine() {
    }
    
    @Override
    public void finishUpdate() {
    }
    

    public void setNodeManager(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
      
}
