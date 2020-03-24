package fr.inria.corese.core.index;

import fr.inria.corese.core.Event;
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
 * Nodes are sorted by Node index 
 * Nodes with same node index which are not sameTerm are kept in the list:
 * s p 01, 1, 1.0, '1'^^xsd:long, 1e1
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EdgeManagerIndexer 
        implements Index {

    /**
     * @return the loopMetadata
     */
    public boolean isLoopMetadata() {
        return loopMetadata;
    }

    /**
     * @param loopMetadata the loopMetadata to set
     */
    public void setLoopMetadata(boolean loopMetadata) {
        this.loopMetadata = loopMetadata;
    }
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
    Comparator<Edge> comp;
    Graph graph;
    List<Node> sortedProperties;
    PredicateList sortedPredicates;
    // Property Node -> Edge List 
    HashMap<Node, EdgeManager> table;
    NodeManager nodeManager;
    HashMap<Node, Node> map;
    private boolean debug = false;
    private boolean loopMetadata = false;

    public EdgeManagerIndexer(Graph g, boolean bi, int index) {
        init(g, bi, index);
        table = new HashMap();
        nodeManager = new NodeManager(g, index);
    }

    void init(Graph g, boolean bi, int n) {
        graph = g;
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
                // TODO Auto-generated method stub
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
        MetaIterator<Edge> meta = new MetaIterator<Edge>();
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
        nodeManager.clear();
    }

    @Override
    public void clearIndex(Node pred) {
        EdgeManager l = get(pred);
        if (l != null) { // && l.size() > 0) {            
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

    Edge internal(Edge ent){
        return graph.getEdgeFactory().internal(ent);
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
       
        if (isSort(edge)) {
            // edges are sorted, check presence by dichotomy
            int i = el.getPlace(edge);
            if (i == -1) {
                count++;
                return null;
            }       

            if (onInsert(edge)) {
                el.add(i, internal);
                logInsert(edge);
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
        graph.tag(ent);
        return ent;
    }


    EdgeManager getListByLabel(Edge e) {
        Node pred = graph.getPropertyNode(e.getEdgeNode().getLabel());
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

    boolean isSort(Edge edge) {
        return !graph.isIndex();
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
            setComparator(list);
            put(predicate, list);
        }
        return list;
    }
    
    /**
     * 
     * All EdgeManager(index) share same Comparator
     */       
    void setComparator(EdgeManager el){
        if (comp == null){
            // create Comparator that will be shared
            comp = el.getComparator(index);
        }
        el.setComparator(comp);
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
     */
    @Override
    public void indexNodeManager() {
        nodeManager.start();
        for (Node pred : getSortedProperties()) {
            checkGet(pred).indexNodeManager(nodeManager);
        }
        nodeManager.finish();
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
        nodeManager.desactivate();
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
        nodeManager.start();
        for (Node pred : getSortedProperties()) {
            reduce(pred);
        }
        nodeManager.finish();
    }

    private void reduce(Node pred) {
        get(pred).reduce(nodeManager);
    }
    
    @Override
    public void indexNode() {
        for (Node pred : getProperties()) {
            for (Edge ent : get(pred)) {
                graph.define(ent);
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
                EdgeManager std = (EdgeManager) graph.getIndex().get(pred);
                list.copy(std);
                list.sort();
            }
            return list;
        }
    }
    
    
    @Override
    public Iterable<Edge> getSortedEdges(Node node) {
        PredicateList list = getNodeManager().getPredicates(node);
        MetaIterator<Edge> meta = new MetaIterator<Edge>();
        int i = 0;
        for (Node pred : list) {
            Iterable<Edge> it = getEdges(pred, node, list.getPosition(i++));
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<Edge>();
        }
        return meta;
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
            return list.getEdges(node);
        }
        //return list.getEdges(node, node2);
    }
    
    @Override
    public Iterable<Edge> getEdges(Node pred, Node node, int n) {
        if (n == -1){
            return getEdges(pred, node, null);
        }
        EdgeManager list = checkGet(pred);
        if (list == null){ 
            return list;
        }
        else {
            if (debug) {
                logger.info("getEdges: " + pred + " " + node + " " + n);
            }
            return list.getEdges(node, n);
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

    void trace(List<Edge> list) {
        Node nn = list.get(0).getNode(1);
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).getNode(1).same(nn)) {
                nn = list.get(i).getNode(1);
                logger.debug(nn.toString());
            }
        }
    }

    /**
     * @return the byIndex
     */
    public boolean isByIndex() {
        return byIndex;
    }

    /**
     * @param byIndex the byIndex to set
     */
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
        Node pred = graph.getPropertyNode(edge.getEdgeNode());
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
        //EdgeManager list = getListByLabel(edge);
        EdgeManager list = get(pred);
        
        if (list == null) {
            return null;
        }

        int i = list.findIndexNodeTerm(edge);

        if (i == -1) {
            return null;
        }
        
        Edge target = list.get(i);
        if (AccessRight.isActive() && AccessRight.reject(edge.getLevel(), target.getLevel())) {
            return null;
        }
        
        Edge ent = list.remove(i);
        if (getIndex() == 0) {
            graph.setSize(graph.size() - 1);
        }
        logDelete(ent);
        return ent;
    }

    boolean onInsert(Edge ent) {
        return graph.onInsert(ent);
    }

    void logDelete(Edge ent) {
        if (ent != null) {
            recordUpdate(true);
            if (getIndex() == 0) {
                graph.logDelete(ent);
            }
        }
    }
    
    void recordUpdate(boolean b) {
        setUpdate(b);
        if (index == 0) {
            // tell all Index that update occur
            graph.declareUpdate(b);
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
            graph.logInsert(ent);
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

    /**
     * TODO: setUpdate(true)
     */
    private Edge copy(Node gNode, Node pred, Edge ent) {
        return graph.copy(gNode, pred, ent);
    }

    private void clear(Node pred, Edge ent) {
        for (Index ei : graph.getIndexList()) {
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
    
    /**
     *
     */
    @Override
    public void finishUpdate() {
        if (graph.isEdgeMetadata()) {
            metadata();
        }
    }
    
    /**
     * Merge duplicate rdf* triples:
     * triple(s p o [q v]) triple(s p o [r s])
     * ->
     * triple(s p o [q v ; r s])
     * PRAGMA: graph must be indexed (edges must be sorted)
     */
    @Override
    public void metadata() {
        if (map == null) {
            map = new HashMap<>();
        }
        graph.cleanIndex();
        graph.clearNodeManager();
        setLoopMetadata(true);
        graph.getEventManager().start(Event.IndexMetadata);
        // loop because edge merge may lead to duplicate triple(t1 p t2 t3) triple (t1 p t2 t3)
        while (isLoopMetadata()) {
            graph.getEventManager().process(Event.IndexMetadata);
            setLoopMetadata(false);
            for (Node p : getProperties()) {
                // merge duplicate triples with metadata nodes
                // keep only one triple with one metadata node
                get(p).metadata();
            }
            if (map.size() > 0) {
                // replace nodes that have been merged
                replace();
            }
        }
        graph.getEventManager().finish(Event.IndexMetadata);
        //graph.indexNodeManager();
        map.clear();
    }
    
    // record that n1 be replaced by n2
    void replace(Node n1, Node n2) {
        map.put(n1, n2);
    }
    
     /**
      * replace nodes that have been merged
      * _:b1 q v _:b2 r s 
      * ->
      * _:b1 q v ; r s
     */
    void replace() {
        for (Node p : getProperties()) {
            get(p).replace(map);
        }
    }
      
}
