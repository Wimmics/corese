package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import fr.inria.edelweiss.kgram.core.Distinct;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.acacia.corese.storage.api.IStorage;
import fr.inria.acacia.corese.storage.api.Parameters;
import fr.inria.acacia.corese.storage.util.StorageFactory;
import fr.inria.edelweiss.kgram.api.core.TripleStore;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.api.Tagger;
import fr.inria.edelweiss.kgraph.api.ValueResolver;
import fr.inria.edelweiss.kgraph.logic.*;
import java.util.Map;

/**
 * Graph Manager Edges are stored in an index An index is a table: predicate ->
 * List<Edge>
 * Edge List are sorted Join on a Node is computed by dichotomy
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Graph extends GraphObject implements Graphable, TripleStore {

    private static Logger logger = Logger.getLogger(Graph.class);
    public static final String TOPREL
            = fr.inria.acacia.corese.triple.cst.RDFS.RootPropertyURI;
    static final ArrayList<Entity> EMPTY = new ArrayList<Entity>(0);
    public static boolean valueOut = !true;
    public static final int IGRAPH = -1;
    // edges in chronological order
    public static final int ILIST = -2;
    // NB of Index (subject, object)
    public static final int LENGTH = 2;

    public static final int DEFAULT = 0;
    public static final int EXTENSION = 1;

    static final int COPY = 0;
    static final int MOVE = 1;
    static final int ADD = 2;
    static final int CLEAR = 3;
    static long blankid = 0;
    static final String BLANK = "_:b";
    static final String SKOLEM = ExpType.SKOLEM;
    private static final String NL = System.getProperty("line.separator");
    static final int TAGINDEX = 2;
    // true means same number value with different datatypes do not join in SPARQL
    // false: they join
    private static boolean distinctDatatype = false;
    static boolean byIndexDefault = true;
    
    private static final String[] PREDEFINED = {
        Entailment.DEFAULT, Entailment.ENTAIL, Entailment.RULE,
        RDFS.SUBCLASSOF, RDF.TYPE,  RDF.FIRST, RDF.REST
    }; 
    
    public static final int DEFAULT_INDEX = 0;
    public static final int ENTAIL_INDEX = 1;
    public static final int RULE_INDEX = 2;
    
    public static final int SUBCLASS_INDEX = 3;
    public static final int TYPE_INDEX = 4;
    public static final int FIRST_INDEX = 5;
    public static final int REST_INDEX = 6;
   
    public static final int DEFAULT_UNION = 0;
    public static final int DEFAULT_GRAPH = 1;
    public static int DEFAULT_GRAPH_MODE = DEFAULT_UNION;

    private int defaultGraphMode = DEFAULT_GRAPH_MODE;
    
    private int mode = DEFAULT;
    
    /**
     * Synchronization:
     *
     * several read in // ; only one write lock read: Query (QueryProcess) lock
     * write: Load (Load), Update (QueryProcess), Rule (RuleEngine)
     * synchronized: Entailment synchronized in read, hence only one entailment
     * can occur synchronized: indexNode (index of nodes for path) synchronized:
     * synGetCheck (EdgeIndex) may generate index of nth arg during read see
     * occurrences of graph.readLock() graph.writeLock()
     *
     */
    ReentrantReadWriteLock lock;
    ArrayList<Index> tables;
    // default graph (deprecated)
    Index[] dtables;
    // Table of index 0
    Index table, tlist, tgraph,
            // for rdf:type, no named graph to speed up type test
            dtable;
    // predefined individual
    HashMap<String, Node> system;
    // key -> URI Node
    Hashtable<String, Entity> individual;
    // label -> Blank Node
    Hashtable<String, Entity> blank;
    SortedMap<IDatatype, Entity> 
            // IDatatype -> Literal Node 
            // number with same value but different datatype have different Node
            // but (may) have same index
            literal,
            // this table enables to share index in this case for same value with different datatypes
            sliteral;
    // @deprecated
    // key -> Node
    Map<String, Entity> vliteral;
    // graph nodes: key -> Node
    Hashtable<String, Node> graph;
    // property nodes: label -> Node (for performance)
    Hashtable<String, Node> property;
    ArrayList<Node> nodes;
    NodeIndex gindex;
    ValueResolver values;
    Log log;
    List<GraphListener> listen;
    Workflow manager;
    Tagger tag;
    Entailment inference, proxy;
    EdgeFactory fac;
    private Context context;
    private Distance classDistance, propertyDistance;
    private boolean isSkolem = false;
    // true when graph is modified and need index()
    boolean isUpdate = false,
            isDelete = false,
            // any delete occurred ?
            isDeletion = false,
            isIndex = true,
            // automatic entailment when init()
            isEntail = true,
            isDebug = !true,
            hasDefault = !true;
    private boolean isListNode = !true;
    boolean byIndex = byIndexDefault;
    // optmize EdgeIndexer EdgeList
    private boolean optIndex = true;
    // number of edges
    int size = 0;
    int nodeIndex = 0;
    private int tagCount = 0;
    private String key;
    private String name;
    private boolean hasTag = false;
    private boolean isTuple = false;
    public static final String SYSTEM = ExpType.KGRAM + "system";
    public int count = 0;

    private boolean hasList = false;
    
    private Node ruleGraph, defaultGraph, entailGraph;
    
   private ArrayList<Node> systemNode, defaultGraphList;
   DataStore dataStore;

    private IStorage storageMgr;

    static {
        setCompareIndex(true);
    }
    
    // SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...))
    /**
     * @return the isSkolem
     */
    public boolean isSkolem() {
        return isSkolem;
    }

    /**
     * @param isSkolem the isSkolem to set
     */
    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Contain undefined datatype
     */
    public boolean isFlawed() {
        for (Entity ent : getLiteralNodes()) {
            IDatatype dt = (IDatatype) ent.getNode().getValue();
            if (DatatypeMap.isUndefined(dt)) {
                return true;
            }
        }
        return false;
    }

    public boolean typeCheck() {
        if (inference == null) {
            return true;
        }
        return inference.typeCheck();
    }

    /**
     * @return the isTuple
     */
    public boolean isTuple() {
        return isTuple;
    }

    /**
     * @param isTuple the isTuple to set
     */
    public void setTuple(boolean isTuple) {
        this.isTuple = isTuple;
    }

    /**
     * @return the hasList
     */
    public boolean hasList() {
        return hasList;
    }

    /**
     * @param hasList the hasList to set
     */
    public void setHasList(boolean hasList) {
        this.hasList = hasList;
        setList();
    }

    /**
     * Create specific Index where edges are sorted newest first
     * Use case: RuleEngine focus on new edges
     */
    void setList() {
        if (hasList) {
            tlist = createIndex(byIndex, ILIST);
            tables.add(tlist);
        } else if (tables.get(tables.size() - 1).getIndex() == ILIST) {
            tables.remove(tables.size() - 1);
            tlist = null;
        }
    }

    Index createIndex(boolean b, int i) {
        return new EdgeIndexer(this, b, i);
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toGraph() {
        return null;
    }

    @Override
    public void setGraph(Object obj) {
    }

    @Override
    public Object getGraph() {
        return this;
    }
    
     @Override
    public Graph getTripleStore() {
        return this;
    }
    
    @Override
    public Iterable getLoop(){
        return getEdges();
    }

    /**
     * @return the context
     */
    public Context getContext() {
        if (context == null) {
            context = new Context(this);
        }
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * @return the optIndex
     */
    public boolean isOptIndex() {
        return optIndex;
    }

    /**
     * @param optIndex the optIndex to set
     */
    public void setOptIndex(boolean optIndex) {
        this.optIndex = optIndex;
    }

    @Override
    public int pointerType() {
        return GRAPH_POINTER;
    }

    /**
     * @return the defaultGraph
     */
    public int getDefaultGraphMode() {
        return defaultGraphMode;
    }

    /**
     * @param defaultGraph the defaultGraph to set
     */
    public void setDefaultGraphMode(int defaultGraph) {
        this.defaultGraphMode = defaultGraph;
    }
   
    class TreeNode extends TreeMap<IDatatype, Entity> {

        TreeNode() {
            this(true);
        }

        TreeNode(boolean diff) {
            super(new Compare(diff));
        }
    }

    /**
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node 
     * It represent (1 integer) and (1.0 float) as two different Nodes
     */
    class Compare implements Comparator<IDatatype> {

        boolean diff = true;

        Compare(boolean b) {
            diff = b;
        }

        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {

            // xsd:integer differ from xsd:decimal 
            // same node for same datatype 
            if (diff && dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null) {
                int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
                if (cmp != 0) {
                    return cmp;
                }
            }

            int res = dt1.compareTo(dt2);
            return res;
        }
    }

    Graph() {
        this(LENGTH);
    }

    Graph(int length) {
        lock = new ReentrantReadWriteLock();

        tables = new ArrayList<Index>(length);

        for (int i = 0; i < length; i++) {
            // edge Index by subject, object
            tables.add(createIndex(byIndex, i));
        }
        // edge Index by named graph
        tgraph = createIndex(byIndex, IGRAPH);
        tables.add(tgraph);

        table = getIndex(0);
        
        // Literals including numbers:
        literal  = Collections.synchronizedSortedMap(new TreeNode(true));
        // Literal number only (to manage Node index):
        sliteral = Collections.synchronizedSortedMap(new TreeNode(false));
        // deprecated:
        vliteral = Collections.synchronizedMap(new HashMap<String, Entity>());
        // URI Node
        individual = new Hashtable<String, Entity>();
        // Blank Node
        blank = new Hashtable<String, Entity>();
        // Named Graph Node
        graph = new Hashtable<String, Node>();
        // Property Node
        property = new Hashtable<String, Node>();
        
        // Index of nodes of named graphs
        // Use case: SPARQL Property Path
        gindex = new NodeIndex();
        values = new ValueResolverImpl();
        fac = new EdgeFactory(this);
        manager = new Workflow(this);
        key = hashCode() + ".";
        initSystem();
        dataStore = new DataStore(this);
    }
    
    /**
     * System Node are predefined such as kg:default Node for default graph
     * They have an index but they are not yet stored in any graph table
     * but system table
     * They are retrieved by getResource, getNode, getGraph, getProperty on demand
     */
    void initSystem(){
        system = new HashMap<String, Node>();
        systemNode = new ArrayList<Node>();
        for (String uri : PREDEFINED){
            Node n = createSystemNode(uri);
            system.put(uri, n);
            systemNode.add(n);
        }
        defaultGraph = system.get(Entailment.DEFAULT);
        ruleGraph    = system.get(Entailment.RULE);
    }
    
    NodeImpl createSystemNode(String label){
        IDatatype dt = DatatypeMap.newResource(label);
        NodeImpl node = NodeImpl.create(dt, this);
        index(dt, node);
        return node;
    }
    
    Node getSystemNode(String name){
        return system.get(name);        
    }
    
    @Override
    public Node getNode(int n){
        return systemNode.get(n);
    }
    
    public Node getNodeDefault(){
        return getNode(DEFAULT_INDEX);
    }
    
    public Node getNodeRule(){
        return getNode(RULE_INDEX);
    }
    
     public Node getNodeEntail(){
        return getNode(ENTAIL_INDEX);
    }

    public static Graph create() {
        return new Graph();
    }

    /**
     * @param b true for RDFS entailment
     */
    public static Graph create(boolean b) {
        Graph g = new Graph();
        if (b) {
            g.setEntailment();
        }
        return g;
    }

    public EdgeFactory getEdgeFactory() {
        return fac;
    }

    public void setOptimize(boolean b) {
    }

    public static void setValueTable(boolean b) {
        valueOut = b;
        if (!b) {
            setCompareKey(false);
        }
    }

    public static void setCompareKey(boolean b) {
        if (b) {
            setValueTable(true);
        }
    }

    /**
     * Edge Index is sorted on integer index value of Node Set default behavior
     * for all graphs PRAGMA: PB with several graphs, index are not shared
     */
    public static void setCompareIndex(boolean b) {
        byIndexDefault = b;
        //EdgeIndex.setCompareIndex(b);
        Distinct.setCompareIndex(b);
        //MatcherImpl.setCompareIndex(b);
    }

    /**
     * set byIndex on this graph only reset EdgeIndex as well and sort edge list
     * accordingly
     */
    public void setByIndex(boolean b) {
        byIndex = b;
        for (Index id : getIndexList()) {
            id.setByIndex(b);
        }
    }

    public boolean isByIndex() {
        return byIndex;
    }

    /**
     * if true: Keep number datatypes separate but Join fails on different
     * datatypes default is false (hence join works) PRAGMA: true works only
     * with setCompareIndex(true)
     */
    public static void setDistinctDatatype(boolean b) {
        distinctDatatype = b;
    }

    public static void setNodeAsDatatype(boolean b) {
        NodeImpl.byIDatatype = b;
    }

    public boolean isLog() {
        return log != null;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log l) {
        log = l;
    }

    public void log(int type, Object obj) {
        if (log != null) {
            log.log(type, obj);
        }
    }

    public void log(int type, Object obj1, Object obj2) {
        if (log != null) {
            log.log(type, obj1, obj2);
        }
    }

    public void addEngine(Engine e) {
        manager.addEngine(e);
    }

    public void removeEngine(Engine e) {
        manager.removeEngine(e);
    }

    public Workflow getWorkflow() {
        return manager;
    }

    public void setWorkflow(Workflow wf) {
        manager = wf;
    }

    public void setClearEntailment(boolean b) {
        manager.setClearEntailment(b);
    }

    /**
     * Process entailments
     */
    public synchronized void process() {
        manager.process();
    }

    public synchronized void process(Engine e) {
        manager.process(e);
    }

    /**
     * Remove entailments
     */
    public synchronized void remove() {
        manager.remove();
    }

    public void addListener(GraphListener gl) {
        if (listen == null) {
            listen = new ArrayList<GraphListener>();
        }
        if (!listen.contains(gl)) {
            listen.add(gl);
            gl.addSource(this);
        }
    }

    public void removeListener(GraphListener gl) {
        if (listen != null) {
            listen.remove(gl);
        }
    }
    
    public void removeListener() {
        if (listen != null) {
            listen.clear();
        }
    }

    public List<GraphListener> getListeners() {
        return listen;
    }

    public void setTagger(Tagger t) {
        tag = t;
        if (t != null) {
            setTag(true);
        }
    }

    public Tagger getTagger() {
        return tag;
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    void clearDistance() {
        setClassDistance(null);
        setPropertyDistance(null);
    }

    public Entailment getEntailment() {
        return inference;
    }

    /**
     * b=true require entailments to be performed before next query
     */
    public void setEntail(boolean b) {
        isEntail = b;
    }

    public boolean isEntail() {
        return isEntail;
    }

    public void setEntailment(Entailment i) {
        inference = i;
        manager.addEngine(i);
    }

    /**
     * Set RDFS entailment
     */
    public void setEntailment() {
        Entailment entail = Entailment.create(this);
        setEntailment(entail);
    }

    /**
     * (des)activate RDFS entailment
     */
    public void setEntailment(boolean b) {
        if (inference != null) {
            inference.setActivate(b);
        }
    }

    /**
     * Use Case: GUI Remove or perform RDFS Entailment
     */
    synchronized public void setRDFSEntailment(boolean b) {
        if (b) {
            if (inference == null) {
                setEntailment();
            } else {
                setEntailment(true);
            }
            setEntail(true);
            init();
        } else if (inference != null) {
            setEntailment(false);
            inference.remove();
        }
    }

    public void set(String property, boolean value) {
        localSet(property, value);
        if (inference != null) {
            inference.set(property, value);
        }
    }

    void localSet(String property, boolean value) {
        if (property.equals(Entailment.DUPLICATE_INFERENCE)) {
            for (Index t : tables) {
                t.setDuplicateEntailment(value);
            }
        }
    }

    public void entail() {
        if (inference != null) {
            inference.process();
        }
    }

    public void setDefault(boolean b) {
        hasDefault = b;
    }

    public boolean hasDefault() {
        return hasDefault;
    }

    @Override
    public String toString() {
        return toRDF();
    }

    public String toRDF() {
        Serializer sb = new Serializer();
        sb.open("kg:Graph");

        sb.appendPNL("kg:edge     ", size());
        sb.appendPNL("kg:node     ", nbNodes());
        sb.appendPNL("kg:graph    ", graph.size());
        sb.appendPNL("kg:property ", table.size());
        sb.appendPNL("kg:uri      ", individual.size());
        sb.appendPNL("kg:bnode    ", blank.size());
        sb.appendPNL("kg:literal  ", literal.size());
        sb.appendPNL("kg:date     ", DatatypeMap.newDate());

        sb.close();

        for (Index t : getIndexList()) {
            if (t.getIndex() == 0 || t.cardinality() > 0) {
                sb.appendNL(t.toRDF());
            }
        }

        return sb.toString();
    }

    /**
     * Generate an RDF Graph that describes the KGRAM system and the current RDF
     * graph
     */
    public Graphable describe() {
        return getContext();
    }

    public String toString2() {
        String str = "";
        int uri = 0, blank = 0, string = 0, lit = 0, date = 0, num = 0;

        for (Entity e : getNodes()) {
            uri++;
        }

        for (Entity e : getBlankNodes()) {
            blank++;
        }

        for (Entity e : getLiteralNodes()) {
            IDatatype dt = (IDatatype) e.getNode().getValue();
            if (dt.isNumber()) {
                num++;
            } else if (dt.getCode() == IDatatype.STRING) {
                string++;
            } else if (dt.getCode() == IDatatype.LITERAL) {
                lit++;
            } else if (dt.getCode() == IDatatype.DATE) {
                date++;
            }

        }

        str += "uri: " + uri;
        str += "\nblank: " + blank;

        str += "\nnum: " + num;
        str += "\nstring: " + string;
        str += "\nliteral: " + lit;
        str += "\ndate: " + date;

        return str;
    }

    public String display() {
        String sep = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        if (getIndex() instanceof EdgeIndexer) {
            EdgeIndexer ie = (EdgeIndexer) getIndex();

            for (Node p : getSortedProperties()) {
                if (sb.length() > 0) {
                    sb.append(NL);
                }
                EdgeList list = ie.get(p);
                sb.append(p + " (" + list.size() + ") : ");
                sb.append(sep);
                for (Entity ent : list) {
                    sb.append(ent);
                    sb.append(sep);
                }
            }
        }

        return sb.toString();
    }

    public Entailment getProxy() {
        if (proxy == null) {
            proxy = inference;
            if (proxy == null) {
                proxy = Entailment.create(this);
            }
        }
        return proxy;
    }

    public boolean isType(Edge edge) {
        return getProxy().isType(edge);
    }

    public boolean isType(Node pred) {
        return getProxy().isType(pred);
    }

    public boolean isSubClassOf(Node pred) {
        return getProxy().isSubClassOf(pred);
    }

    public boolean isSubClassOf(Node node, Node sup) {
        return getProxy().isSubClassOf(node, sup);
    }

    /**
     * ************************************************************
     *
     * Consistency Management
     *
     *************************************************************
     */
    /**
     * send e.g. by kgram eval() before every query execution restore
     * consistency if updates have been done, perform entailment when delete is
     * performed, it is the user responsibility to delete the entailments that
     * depend on it it can be done using: drop graph kg:entailment Rules are not
     * automatically run, use re.process()
     */
    public synchronized void init() {
        if (isIndex) {
            if (isDebug) {
                logger.info("Graph index");
            }
            index();
        }

        if (isUpdate) {
            // use case: previously load or sparql update
            // clean meta properties 
            // redefine meta properties
            update();
        }

        if (isEntail) {
            if (isDebug) {
                logger.info("Graph entailment");
            }
            process();
            isEntail = false;
        }

    }

    private void update() {
        isUpdate = false;
        // node index
        clearIndex();
        clearDistance();

        if (isDelete) {
            manager.onDelete();
            isDelete = false;
        }
    }

    public void clean() {
        // clean timestamp index
        if (hasList) {
            tlist.clean();
        }
    }

    public void cleanEdge() {
        // clean rule engine timestamp
        for (Entity ent : getEdges()) {
            ent.getEdge().setIndex(-1);
        }
    }

    public void setUpdate(boolean b) {
        isUpdate = b;
        if (isUpdate) {
            setEntail(true);
        }
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    private void setDelete(boolean b) {
        setUpdate(b);
        isDelete = b;
        isDeletion = true;
    }

    public boolean hasEntailment() {
        return inference != null && inference.isActivate();
    }

    // true when index must be sorted 
    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean b) {
        isIndex = b;
    }

    /**
     * Property Path start a new shortest path Only with one user (no thread
     * here)
     *
     * @deprecated
     */
    public void initPath() {
    }

    /**
     * **********************************************************************
     */
    public ValueResolver getValueResolver() {
        return values;
    }

    public Index getIndex() {
        return table;
    }

    /**
     * When load is finished, sort edges
     */
    public void index() {
        for (Index ei : getIndexList()) {
            ei.index();
        }
        isIndex = false;
    }
    
    public void compact(){
        cleanIndex();
        table.compact();
    }
    
    void cleanIndex() {
        for (Index ei : getIndexList()) {
            if (ei.getIndex() != 0) {
                ei.clean();
            }
        }
    }
 

    public void prepare() {
        if (isIndex) {
            index();
        }
    }
    
    /**
     * Draft (transitivity is missing ...)
     */
    public void sameas(){
        for (Entity ent : getEdges(OWL.SAMEAS)){
            ent.getNode(1).setIndex(ent.getNode(0).getIndex());
        }
        index();
    }

    void clearIndex() {
        gindex.clear();
    }

    synchronized void indexNode() {
        if (gindex.size() == 0) {
            table.indexNode();
        }
    }

    public void indexResources() {
        int i = 0;
        for (Entity n : getRBNodes()) {
            n.getNode().setIndex(i++);
        }
    }

    void define(Entity ent) {
        gindex.add(ent);
    }

    public Iterable<Node> getProperties() {
        return table.getProperties();
    }

    public Iterable<Node> getSortedProperties() {
        return table.getSortedProperties();
    }

    public Entity add(Entity edge) {
        return add(edge, true);
    }

    public Entity add(Entity edge, boolean duplicate) {
        // store edge in index 0
        Entity ent = table.add(edge, duplicate);
        // tell other index that predicate has instances
        if (ent != null) {
            if (edge.getGraph() == null){
                System.out.println("Graph: " + edge);
            }
            addGraphNode(edge.getGraph());
            addPropertyNode(edge.getEdge().getEdgeNode());

            for (Index ei : getIndexList()) {
                if (ei.getIndex() != 0) {
                    ei.declare(edge, duplicate);
                }
            }
            //tlist.declare(edge);
            size++;
        }
        return ent;
    }

    public boolean exist(Entity edge) {
        return table.exist(edge);
    }

    public boolean exist(Node p, Node n1, Node n2) {
        p = getPropertyNode(p);
        if (p == null) {
            return false;
        }
        return table.exist(p, n1, n2);
    }

    public Entity addEdgeWithNode(Entity ee) {
        addGraphNode(ee.getGraph());
        addPropertyNode(ee.getEdge().getEdgeNode());
        for (int i = 0; i < ee.nbNode(); i++) {
            add(ee.getNode(i));
        }
        return addEdge(ee);
    }

    public Node addList(List<Node> list) {
        return addList(addDefaultGraphNode(), list);
    }

    public Node addList(Node g, List<Node> list) {
        addGraphNode(g);
        Node fst = addProperty(RDF.FIRST);
        Node rst = addProperty(RDF.REST);
        Node nil = addResource(RDF.NIL);
        Node head = addBlank();
        Node cur = head;
        Node tmp;
        int s = list.size() - 1;
        int i = 0;

        for (Node n : list) {
            tmp = nil;
            if (i++ < s) {
                tmp = addBlank();
            }
            add(n);
            addEdge(g, cur, fst, n);
            addEdge(g, cur, rst, tmp);
            cur = tmp;
        }
        return head;
    }

    public Entity addEdge(Entity edge) {
        return addEdge(edge, true);
    }

    public Entity addEdge(Entity edge, boolean duplicate) {
        Entity ent = add(edge, duplicate);
        if (ent != null) {
            setUpdate(true);
            //OC:
            manager.onInsert(ent.getGraph(), edge.getEdge());
//			if (inference!=null){
//				inference.onInsert(ent.getGraph(), edge);
//			}
        }
        return ent;
    }

    public int add(List<Entity> lin) {
        return add(lin, null, true);
    }

    public int add(List<Entity> lin, List<Entity> lout, boolean duplicate) {
        int n = 0;
        for (Entity ee : lin) {
            Entity ent = addEdge(ee, duplicate);
            if (ent != null) {
                n++;
                if (lout != null) {
                    lout.add(ent);
                }
            }
        }
        return n;
    }

    public void addOpt(Node p, List<Entity> list) {
        if (list.isEmpty()) {
            return;
        }
        if (p == null) {
            addOpt(list);
        } else {
            p = list.get(0).getEdge().getEdgeNode();
            add(p, list);
        }
    }

    /**
     * PRAGMA: there is no duplicate in list, all edges are inserted predicate
     * is declared in graph TODO: if same predicate, perform ensureCapacity on
     * Index list
     */
    void add(Node p, List<Entity> list) {
        setIndex(true);
        for (Index ei : getIndexList()) {
            ei.add(p, list);
            ei.index(p, false);
        }
        setIndex(false);
        size += list.size();
    }

    public void addOpt(List<Entity> list) {
        if (list.isEmpty()) {
            return;
        }
        // fake index not sorted, hence add(edge) is done at end of index list
        setIndex(true);
        HashMap<String, Node> t = new HashMap<String, Node>();

        for (Entity ee : list) {

            Node pred = ee.getEdge().getEdgeNode();
            t.put(pred.getLabel(), pred);

            // add Entity at the end of list index
            addEdge(ee);
        }

        for (Node pred : t.values()) {
            for (Index ei : getIndexList()) {
                ei.index(pred, false);
            }
        }
        setIndex(false);
    }

    public Entity create(Node source, Node subject, Node predicate, Node value) {
        return fac.create(source, subject, predicate, value);
    }

    public Entity createDelete(Node source, Node subject, Node predicate, Node value) {
        return fac.createDelete(source, subject, predicate, value);
    }

    public Entity create(Node source, Node predicate, List<Node> list) {
        return fac.create(source, predicate, list);
    }

    public Entity createDelete(Node source, Node predicate, List<Node> list) {
        return fac.createDelete(source, predicate, list);
    }

    public Entity create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        return null;
    }

    public int size() {
        return size;
    }

    public int nbNodes() {
        return nbIndividuals() + nbBlanks() + nbLiterals();
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public int nbResources() {
        return nbIndividuals() + nbBlanks();
    }

    public int nbIndividuals() {
        return individual.size();
    }

    public int nbBlanks() {
        return blank.size();
    }

    public int nbLiterals() {
        return literal.size();
    }
    
    void setSize(int n) {
        size = n;
    }

    public Node copy(Node node) {
        return getNode((IDatatype) node.getValue(), true, false);
    }

    public Node getTopClass() {
        Node n = getNode(OWL.THING);
        if (n == null) {
            n = getNode(RDFS.RESOURCE);
        }
        if (n == null) {
            n = createNode(RDFS.RESOURCE);
        }
        return n;
    }

    public Node getTopProperty() {
        Node n = getNode(TOPREL);
        if (n == null) {
            n = createNode(TOPREL);
        }
        return n;
    }

    public List<Node> getTopProperties() {
        List<Node> nl = new ArrayList<Node>();
        Node n;

//		n = getNode(OWL.TOPOBJECTPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
//		n = getNode(OWL.TOPDATAPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
        if (nl.size() == 0) {
            n = getTopProperty();
            nl.add(n);
        }

        return nl;
    }

    // used by construct
    public Node getNode(Node gNode, IDatatype dt, boolean create, boolean add) {
        if (dt.isBlank() && isSkolem()) {
            dt = skolem(dt);
        }
        return getNode(dt, create, add);
    }

    /**
     * Given a constant query node, return the target node in current graph if
     * it exists
     *
     */
    public Node getNode(Node node) {
        IDatatype dt = (IDatatype) node.getValue();
        return getNode(dt, false, false);
    }

    public Node createNode(IDatatype dt) {
        return getNode(dt, true, false);
    }

    // all nodes
    // TODO: check producer
    public Node addNode(IDatatype dt) {
        return getNode(dt, true, true);
    }

    // used by construct
    public Node getNode(IDatatype dt, boolean create, boolean add) {
        if (dt.isLiteral()) {
            return getLiteralNode(dt, create, add);
        } else if (dt.isBlank()) {
            return getBlankNode(dt, create, add);
        } else {
            return getResourceNode(dt, create, add);
        }
    }
    
    // May return Node with different datatype for number
    public Node getExtNode(Node node) {
        IDatatype dt = (IDatatype) node.getValue();
         if (dt.isNumber()) {
            return getExtLiteralNode(dt);
        }
         else {
             return getNode(node);
         }
    }


    public Node getResourceNode(IDatatype dt, boolean create, boolean add) {
        String key = getKey(dt);
        Node node = getNode(key, dt.getLabel());
        if (node != null) {
            return node;
        }
        node = getResource(key, dt.getLabel());
        if (node == null && create) {
            node = createNode(key, dt);
        }
        if (add) {
            add(dt, node);
        }
        return node;
    }

    public Node getBlankNode(IDatatype dt, boolean create, boolean add) {
        Node node = getBlankNode(dt.getLabel());
        if (node != null) {
            return node;
        }
        if (node == null && create) {
            node = buildNode(dt);
        }
        if (add) {
            add(dt, node);
        }
        return node;
    }

    public Node getLiteralNode(IDatatype dt, boolean create, boolean add) {
        String key = getKey(dt);
        Node node = getLiteralNode(key, dt);
        if (node != null) {
            return node;
        }
        if (create) {
            node = createNode(key, dt);
            if (add) {
                addLiteralNode(dt, node);
            }
        }
        return node;
    }

    /**
     * Retrieve a node/graph node/property node
     */
    public Node getResource(String name) {
        return getResource(getID(name), name);
    }

    Node getResource(String key, String name) {
        Node node = getNode(key, name);
        if (node == null) {
            node = getGraphNode(key, name);
        }
        if (node == null) {
            node = getPropertyNode(name);
        }
        if (node == null) {
            node = getSystemNode(name);
        }
        return node;
    }

    // resource or blank
    public boolean isIndividual(Node node) {
        return individual.containsKey(getID(node))
                || blank.containsKey(node.getLabel());
    }

    // resource node
    public Node getNode(String name) {
        return getNode(getID(name), name);
    }

    Node getNode(String key, String name) {
        return (Node) individual.get(key);
    }

    void addNode(IDatatype dt, Node node) {
        individual.put(getID(node), (Entity) node);
    }

    public Node getBlankNode(String name) {
        return (Node) blank.get(name);
    }

    void addBlankNode(IDatatype dt, Node node) {
        blank.put(node.getLabel(), (Entity) node);
    }

    String getID(Node node) {
        if (valueOut) {
            return node.getKey();
        } else {
            return node.getLabel();
        }
    }

    String getID(String str) {
        if (valueOut) {
            return values.getKey(str);
        } else {
            return str;
        }
    }

    String getKey(IDatatype dt) {
        if (valueOut) {
            return values.getKey(dt);
        } else {
            return dt.getLabel();
        }
    }

    Node basicAddGraph(String label) {
        String key = getID(label);
        Node node = getGraphNode(key, label);
        if (node != null) {
            return node;
        }
        node = getResource(key, label);
        if (node == null) {
            IDatatype dt = DatatypeMap.createResource(label);
            node = createNode(key, dt);
            indexNode(dt, node);
        }
        //graph.put(label, node);	               
        graph.put(key, node);
        return node;
    }
    
    Node basicAddGraphNode(Node node){
       graph.put(node.getLabel(), node);
       return node; 
    }

    Node basicAddResource(String label) {
        String key = getID(label);
        Node node = getNode(key, label);
        if (node != null) {
            return node;
        }
        node = getGraphNode(key, label);
        if (node == null) {
            node = getPropertyNode(label);
        }
        if (node == null){
            node = getSystemNode(label);
        }
        if (node != null) {
            add((IDatatype) node.getValue(), node);
            return node;
        }
        IDatatype dt = DatatypeMap.createResource(label);
        node = createNode(key, dt);
        add(dt, node);
        return node;
    }

    Node basicAddResource2(String label) {
        String key = getID(label);
        Node node = getResource(key, label);
        if (node != null) {
            return node;
        }
        IDatatype dt = DatatypeMap.createResource(label);
        node = createNode(key, dt);
        add(dt, node);
        return node;
    }

    Node basicAddProperty(String label) {
        Node node = getPropertyNode(label);
        if (node != null) {
            return node;
        }
        node = getResource(label);
        if (node == null) {
            IDatatype dt = DatatypeMap.createResource(label);
            node = buildNode(dt);
            indexNode(dt, node);
        }
        property.put(label, node);
        return node;
    }

    Node basicAddBlank(String label) {
        Node node = getBlankNode(label);
        if (node == null) {
            IDatatype dt = DatatypeMap.createBlank(label);
            if (dt != null) {
                node = buildNode(dt);
                indexNode(dt, node);
                addBlankNode(dt, node);
            }
        }
        return node;
    }

    public void add(Node node) {
        IDatatype dt = (IDatatype) node.getValue();
        add(dt, node);
    }

    void add(IDatatype dt, Node node) {
        if (dt.isLiteral()) {
            addLiteralNode(dt, node);
        } else if (dt.isBlank()) {
            addBlankNode(dt, node);
            indexNode(dt, node);
        } else {
            addNode(dt, node);
            indexNode(dt, node);
        }
    }

    public void addLiteralNode(IDatatype dt, Node node) {
        if (valueOut) {
            vliteral.put(node.getKey(), (Entity) node);
            indexNode(dt, node);
        } else {
            literal.put(dt, (Entity) node);
            if (distinctDatatype) {
                // no join on number with different datatypes
                indexNode(dt, node);
            } else {
                // join on number with different datatypes
                indexLiteralNode(dt, node);
            }
        }
    }

    /**
     * Assign an index to Literal Node Assign same index to same number values:
     * 1, '1'^^xsd:double, 1.0 have same index If EdgeIndex is sorted by index,
     * dichotomy enables join on semantically equivalent values
     */
    void indexLiteralNode(IDatatype dt, Node node) {
        if (dt.isNumber()) {
            Entity n = sliteral.get(dt);
            if (n == null) {
                sliteral.put(dt, (Entity) node);
                indexNode(dt, node);
            } else if (node.getIndex() == -1) {
                // assign same index as existing same value
                node.setIndex(n.getNode().getIndex());
            }
        } else {
            indexNode(dt, node);
        }
    }

    public Node getLiteralNode(IDatatype dt) {
        return getLiteralNode(getKey(dt), dt);
    }
                 
    // return number with possibly different datatype that match dt
    public Node getExtLiteralNode(IDatatype dt) {
        return (Node) sliteral.get(dt);    
    }

    public Node getLiteralNode(String key, IDatatype dt) {
        if (valueOut) {
            return (Node) vliteral.get(key);
        } else {
            return (Node) literal.get(dt);
        }
    }

    public Node getGraphNode(String label) {
        return getGraphNode(getID(label), label);
    }

    Node getGraphNode(String key, String label) {
        return graph.get(key);
    }

    public void addGraphNode(Node gNode) {
        if (!isGraphNode(gNode)) {
            //graph.put(gNode.getLabel(), gNode);
            graph.put(getID(gNode), gNode);
            indexNode((IDatatype) gNode.getValue(), gNode);
        }
    }

    public boolean isGraphNode(Node node) {
        //return graph.containsKey(node.getLabel());
        return graph.containsKey(getID(node));
    }

    public Node getPropertyNode(String label) {
        return property.get(label);
    }

    public Node getPropertyNode(Node p) {
        return property.get(p.getLabel());
    }

    public void addPropertyNode(Node pNode) {
        if (!property.containsKey(pNode.getLabel())) {
            property.put(pNode.getLabel(), pNode);
            indexNode((IDatatype) pNode.getValue(), pNode);
        }
    }
    
    public DataStore getDataStore(){
        return dataStore;
    }
    
    public DataProducer getDefault(){
        return getDataStore().getDefault();
    }
    
     public DataProducer getNamed(){
        return getDataStore().getNamed();
    }

    public Iterable<Entity> getEdges() {
        Iterable<Entity> ie = table.getEdges();
        if (ie == null) {
            return new ArrayList<Entity>();
        }
        return ie;
    }

    public Edge getEdge(Node pred, Node node, int index) {
        Iterable<Entity> it = getEdges(pred, node, index);
        if (it == null) {
            return null;
        }
        for (Entity ent : it) {
            return ent.getEdge();
        }
        return null;
    }

    public Edge getEdge(String name, Node node, int index) {
        Node pred = getPropertyNode(name);
        if (pred == null){
            return null;
        }
        return getEdge(pred, node, index);
    }

    public Edge getEdge(String name, String arg, int index) {
        Node pred = getPropertyNode(name);
        Node node = getNode(arg);
        if (pred == null || node == null) {
            return null;
        }
        Edge edge = getEdge(pred, node, index);
        return edge;
    }

    public IDatatype getValue(String name, IDatatype dt) {
        Node node = getNode(dt);
        if (node == null) {
            return null;
        }
        return getValue(name, node);
    }
        
    public IDatatype getValue(String name, Node node){
       Node value = getNode(name, node);
       if (value == null){
           return null;
       }
       return (IDatatype) value.getValue();
    }
    
    public Node getNode(String name, Node node){
        Edge edge = getEdge(name, node, 0);
        if (edge == null) {
            return null;
        }
        return edge.getNode(1);
    }

    public Iterable<Node> getNodes(Node pred, Node node, int n) {
        Iterable<Entity> it = getEdges(pred, node, n);
        if (it == null) {
            return new ArrayList<Node>();
        }
        int index = (n == 0) ? 1 : 0;
        return NodeIterator.create(it, index);
    }

    public Iterable<Entity> properGetEdges(Node predicate, Node node, int n) {
        Iterable<Entity> it = getEdges(predicate, node, null, n);
        if (it == null){
            return EMPTY;
        }
        return it;
    }
    
    public Iterable<Entity> getEdges(Node predicate, Node node, int n) {
        return getEdges(predicate, node, null, n);
    }

    public Iterable<Entity> getEdges(Node predicate, Node node, Node node2, int n) {
        if (isTopRelation(predicate)) {
            return getEdges(node, n);
        } else {
            return basicEdges(predicate, node, node2, n);
        }
    }

    public Iterable<Entity> basicEdges(Node predicate, Node node, Node node2, int n) {
        return getIndex(n).getEdges(predicate, node, node2);
    }

    /**
     * with rdfs:subPropertyOf
     */
    public Iterable<Entity> getAllEdges(Node predicate, Node node, Node node2, int n) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();

        for (Node pred : getProperties(predicate)) {
            Iterable<Entity> it = getIndex(n).getEdges(pred, node);
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<Entity>();
        }
        return meta;
    }

    public Iterable<Node> getProperties(Node p) {
        ArrayList<Node> list = new ArrayList<Node>();
        for (Node n : getProperties()) {
            if (getEntailment().isSubPropertyOf(n, p)) {
                list.add(n);
            }
        }
        return list;
    }

    /**
     * Return start blank node for all lists
     */
    public List<Node> getLists() {
        List<Node> list = new ArrayList<Node>();
        for (Entity ent : getEdges(RDF.FIRST)) {
            Node start = ent.getNode(0);
            Edge edge = getEdge(RDF.REST, start, 1);
            if (edge == null) {
                list.add(start);
            }
        }
        return list;
    }

    /**
     *
     * Return the root of the graph, when it is a tree (e.g. SPIN Graph)
     */
    public Node getRoot() {
        for (Entity ent : getBlankNodes()) {
            Node node = ent.getNode();
            if (!hasEdge(node, 1)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasEdge(Node node, int i) {
        Iterable<Entity> it = getEdges(node, i);
        return it.iterator().hasNext();
    }

    public List<Node> getList(Node node) {
        List<Node> list = new ArrayList<Node>();
        list(node, list);
        return list;
    }
    
    public List<IDatatype> getDatatypeList(Node node) {
        List<Node> list = getList(node);
        ArrayList<IDatatype> ldt = new ArrayList<IDatatype>();
        for (Node n : list) {
            ldt.add(value(n));
        }
        return ldt;
    }

    /**
     * node is a list Node compute the list of elements
     */
    void list(Node node, List<Node> list) {
        if (node.getLabel().equals(RDF.NIL)) {
        } else {
            Edge first = getEdge(RDF.FIRST, node, 0);
            if (first != null) {
                list.add(first.getNode(1));
            }
            Edge rest = getEdge(RDF.REST, node, 0);
            if (rest != null) {
                list(rest.getNode(1), list);
            }
        }
    }
    
    /**
     *     
     */
    
    public IDatatype list(Node node){
        ArrayList<IDatatype> list = reclist(node);
        if (list == null){
            return null;
        }
        return DatatypeMap.createList(list);
    }
    
      public ArrayList<IDatatype> reclist(Node node) {
        if (node.getLabel().equals(RDF.NIL)) {
            return new ArrayList<IDatatype>();
        } 
        else {
            Edge first = getEdge(RDF.FIRST, node, 0);
            Edge rest  = getEdge(RDF.REST, node, 0);
            if (first == null || rest == null) {
                return null;
            }
            ArrayList<IDatatype> list = reclist(rest.getNode(1));
            if (list == null){
                return null;
            }
            Node val = first.getNode(1);
            
            if (val.isBlank()){
                // may be a list
                ArrayList<IDatatype> ll = reclist(val);
                if (ll == null){
                    // not a list
                    list.add(0, value(val));
                }
                else {
                    // list
                    list.add(0, DatatypeMap.createList(ll));
                }
            }
            else {
                list.add(0, value(val));
            }
            return list;
        }
    }
      
      IDatatype value(Node n){
          return (IDatatype) n.getValue();
      }


    boolean isTopRelation(Node predicate) {
        return predicate.getLabel().equals(TOPREL);
    }

    // without duplicates 
    public Iterable<Entity> getNodeEdges(Node node) {
        return getDataStore().getDefault().iterate(node, 0);
    }

    public Iterable<Entity> getNodeEdges(Node gNode, Node node) {
        return getDataStore().getNamed().from(gNode).iterate(node, 0);
    }

    Index getIndex(int n, boolean def) {
        if (def) {
            return dtables[n];
        }
        return getIndex(n);
    }

    public List<Index> getIndexList() {
        return tables;
    }

    // synchronized
    public Index getIndex(int n) {
        switch (n) {
            case IGRAPH:
                return tgraph;
            case ILIST:
                return tlist;
        }
//        if (n + 1 >= tables.size()) {
//            //setIndex(n, new EdgeIndex(this, n));	
//        }
        return tables.get(n);
    }

    void setIndex(int n, Index e) {
        tables.add(n, e);
    }

    public Iterable<Entity> getEdges(Node node, int n) {
        return getSortedEdges(node, n);
    }

    public Iterable<Entity> getSortedEdges(Node node, int n) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();

        for (Node pred : getSortedProperties()) {
            Iterable<Entity> it = getIndex(n).getEdges(pred, node);
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<Entity>();
        }
        return meta;
    }

    public Iterable<Entity> getEdges(String p) {
        Node predicate = getPropertyNode(p);
        if (predicate == null) {
            return EMPTY;
        }
        return getEdges(predicate);
    }
    
    public Entity getEdge(String p){
        Iterator<Entity> it = getEdges(p).iterator();
        if (it.hasNext()){
            return it.next();
        }
        return null;
    }
    
    public Iterable<Entity> getEdges(String p, Node n, int i) {
        Node predicate = getPropertyNode(p);
        if (predicate == null) {
            return EMPTY;
        }
        Iterable<Entity> it = getEdges(predicate, n, i);
        if (it == null){
            return EMPTY;
        }
        return it;
    }
    
    public Iterable<Entity> getEdges(IDatatype s, IDatatype p, IDatatype o) {
        Node ns = null, np, no = null;
        if (p == null){
            np = getTopProperty();
        }
        else {
            np = getPropertyNode(p);
        }
        if (s != null){
            ns = getNode(s);
        }        
        if (o != null){
            no = getNode(o);
        }
        if (s == null && o != null){
           return getEdges(np, no, null, 1); 
        }
        return getEdges(np, ns, no, 0);
    }

    public Iterable<Entity> getEdges(Node predicate) {
        Iterable<Entity> it = getEdges(predicate, null, 0);
        if (it == null) {
            it = EMPTY;
        }
        return it;
    }

    public int size(Node predicate) {
        if (isTopRelation(predicate)) {
            return size();
        }
        Node pred = getPropertyNode(predicate.getLabel());
        if (pred == null) {
            return 0;
        }
        return table.size(pred);
    }

    public Iterable<Node> getGraphNodes() {
        return graph.values();
    }
    
    public int nbGraphNodes(){
        return graph.size();
    }

    public Iterable<Entity> getNodes() {
        return individual.values();
    }

    public Iterable<Entity> getBlankNodes() {
        return blank.values();
    }

    /**
     * resource & blank TODO: a node may have been deleted (by a delete triple)
     * but still be in the table
     */
    public Iterable<Entity> getRBNodes() {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        return meta;
    }

    public Iterable<Entity> getLiteralNodes() {
        if (valueOut) {
            return vliteral.values();
        }
        return literal.values();
    }

    public Iterable<Entity> getAllNodes() {
        if (isDeletion) {
            // recompute existing nodes (only if it has not been already recomputed)
            return getAllNodesIndex();
        } else {
            // get nodes from tables
            return getAllNodesDirect();
        }
    }

    /**
     * TODO: a node may have been deleted (by a delete triple) but still be in
     * the table
     */
    public Iterable<Entity> getAllNodesDirect() {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        meta.next(getLiteralNodes());
        return meta;
    }

    /**
     * Prepare an index of nodes for each graph, enumerate all nodes TODO: there
     * are duplicates (same node in several graphs)
     */
    public Iterable<Entity> getAllNodesIndex() {
        indexNode();
        return gindex.getNodes();
    }

    public Iterable<Entity> getNodes(Node gNode) {
        indexNode();
        return gindex.getNodes(gNode);
    }

    /**
     * May infer datatype from property range
     */
    public Node addLiteral(String pred, String label, String datatype, String lang) {
        String range = null;
        if (lang == null
                && inference != null && inference.isDatatypeInference()) {
            range = inference.getRange(pred);
            if (range != null
                    && !range.startsWith(Entailment.XSD)) {
                range = null;
            }
        }
        if (datatype == null) {
            if (range != null) {
                datatype = range;
            }
        }

        IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }

    public String newBlankID() {
        if (isSkolem) {
            return skolem(blankID());
        } else {
            return blankID();
        }
    }

    String blankID() {
        return BLANK + blankid++;
    }

    public String skolem(String id) {
        String str = values.getKey(key + id);
        return SKOLEM + str;
    }

    public IDatatype skolem(IDatatype dt) {
        if (!dt.isBlank()) {
            return dt;
        }
        String id = skolem(dt.getLabel());
        return createSkolem(id);
    }

    public Node skolem(Node node) {
        if (!node.isBlank()) {
            return node;
        }
        String id = skolem(node.getLabel());
        return NodeImpl.create(createSkolem(id), this);
    }

    IDatatype createSkolem(String id) {
        return DatatypeMap.createSkolem(id);
    }

    public void deleteGraph(String name) {
        graph.remove(getID(name));
    }

    void indexNode(IDatatype dt, Node node) {
        index(dt, node);

        // save values to other medias other than RAM
        if (storable(dt)) {
            dt.setValue(dt.getLabel(), node.getIndex(), storageMgr);
        }
    }

    void index(IDatatype dt, Node node) {
        if (node.getIndex() == -1) {
            node.setIndex(nodeIndex++);
        }
    }

    //check if store dt to file
    boolean storable(IDatatype dt) {

        // check storage manager
        boolean r = (storageMgr != null) && storageMgr.enabled() && (dt != null);
        if (!r) {
            return false;
        }

        // check data type
        r &= DatatypeMap.persistentable(dt);

        // check string length
        r &= storageMgr.check(dt.getLabel());

        return r;
    }

    // ==== example: how to set up parameters ====
    // Graph g = Graph.create();
    // Parameters params = Parameters.create();
    // params.add(Parameters.type.MAX_LIT_LEN, 128);
    // g.setPersistent(IOperation.STORAGE_FILE, params);
    public void setStorage(int type, Parameters params) {
        storageMgr = StorageFactory.create(type, params);
        storageMgr.enable(true);
    }

    public void setStorage(int type) {
        this.setStorage(type, null);
    }

    public IStorage getStorageMgr(){
        return this.storageMgr;
    }
    
    /**
     * Only for new node that does not exist
     */
    Node buildNode(IDatatype dt) {
        return createNode(getKey(dt), dt);
    }

    Node createNode(String key, IDatatype dt) {
        Node node;
        if (valueOut) {
            node = NodeImpl.create(dt, this);
            node.setKey(key);
            values.setValue(key, dt);
        } else {
            node = NodeImpl.create(dt, this);
        }

        return node;
    }

    public IDatatype getValue(Node node) {
        return values.getValue(node.getKey());
    }

    // resource nodes
    public Node createNode(String name) {
        IDatatype dt = DatatypeMap.createResource(name);
        if (dt == null) {
            return null;
        }
        return buildNode(dt);
    }

    /**
     * **************************************************************
     *
     * Graph operations
     *
     ***************************************************************
     */
    public boolean compare(Graph g) {
        return compare(g, false, false, isDebug);
    }

    public boolean compare(Graph g, boolean isGraph) {
        return compare(g, isGraph, false, isDebug);
    }

    public boolean compare(Graph g2, boolean isGraph, boolean detail, boolean isDebug) {
        if (isIndex()) {
            index();
        }
        if (g2.isIndex()) {
            g2.index();
        }
        
        return new GraphCompare(this, g2).compare(isGraph, detail, isDebug);
    }
    

    /**
     * Create a graph for each named graph
     */
    public List<Graph> split() {

        if (graph.size() == 1) {
            ArrayList<Graph> list = new ArrayList<Graph>();
            list.add(this);
            return list;
        }

        return gSplit();
    }

    List<Graph> gSplit() {

        GTable map = new GTable();

        for (Entity ent : getEdges()) {
            Graph g = map.getGraph(ent.getGraph());
            g.addEdgeWithNode(ent);
        }

        ArrayList<Graph> list = new ArrayList<Graph>();
        for (Graph g : map.values()) {
            list.add(g);
        }

        return list;

    }

    class GTable extends HashMap<Node, Graph> {

        public Graph getGraph(Node gNode) {
            Graph g = get(gNode);
            if (g == null) {
                g = Graph.create();
                put(gNode, g);
            }
            return g;
        }
    }

    public List<Entity> getEdgeList(Node n) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (Entity e : getEdges(n, 0)) {
            list.add(e);
        }
        return list;
    }

    /**
     *
     * Without rule entailment
     */
    public List<Entity> getEdgeListSimple(Node n) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (Entity e : getEdges(n, 0)) {
            if (!getProxy().isRule(e)) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * ***************************************************************
     *
     * Update
     *
     ****************************************************************
     */
    public Entity insert(Entity ent) {
        return addEdge(ent);
    }

    public List<Entity> delete(Entity edge) {
        List<Entity> res = null;

        if (edge.getGraph() == null) {
            res = deleteAll(edge);
        } else {
            Entity ee = basicDelete(edge);
            if (ee != null) {
                res = new ArrayList<Entity>();
                res.add(ee);
            }
        }

        if (res != null) {
            deleted(res);
        }
        return res;
    }

    public List<Entity> delete(Entity edge, List<Constant> from) {
        List<Entity> res = null;

        for (Constant str : from) {
            Node node = getGraphNode(str.getLabel());

            if (node != null) {
                fac.setGraph(edge, node);
                Entity ent = basicDelete(edge);
                if (ent != null) {
                    if (res == null) {
                        res = new ArrayList<Entity>();
                    }
                    res.add(ent);
                    setDelete(true);
                }
            }
        }

        if (res != null) {
            deleted(res);
        }
        return res;
    }

    /**
     * Does not delete nodes
     */
    Entity basicDelete(Entity edge) {
        Entity res = null;

        for (Index ie : tables) {
            Entity ent = ie.delete(edge);
            if (isDebug) {
                logger.debug("delete: " + ie.getIndex() + " " + edge);
                logger.debug("delete: " + ie.getIndex() + " " + ent);
            }
            if (ent != null) {
                setDelete(true);
                res = ent;
            }
        }
        return res;
    }

    /**
     * delete occurrences of this edge in all graphs
     */
    List<Entity> deleteAll(Entity edge) {
        ArrayList<Entity> res = null;

        for (Node graph : getGraphNodes()) {
            fac.setGraph(edge, graph);
            Entity ent = basicDelete(edge);
            if (ent != null) {
                if (res == null) {
                    res = new ArrayList<Entity>();
                }
                res.add(ent);
                setDelete(true);
            }
        }

        return res;
    }

    /**
     * This edge has been deleted TODO: Delete its nodes from tables if needed
     */
    void deleted(List<Entity> list) {
        for (Entity ent : list) {
            Edge edge = ent.getEdge();

            for (int i = 0; i < edge.nbNode(); i++) {
                delete(edge.getNode(i));
            }
        }
    }

    void delete(Node node) {
    }

    // clear all except graph names.
    // they must be cleared explicitely
    void clear() {
        clearIndex();
        clearNodes();
        for (Index t : tables) {
            t.clear();
        }
        manager.onClear();
        //OC:
//		if (inference!=null){
//			inference.onClear();
//		}
        clearDistance();
        isIndex = true;
        isUpdate = false;
        isDelete = false;
        size = 0;
        if (storageMgr != null) {
            storageMgr.clean();
        }
    }

    void clearNodes() {
        individual.clear();
        blank.clear();
        literal.clear();
        property.clear();
    }

    public boolean clearDefault() {
        clear();
        return true;
    }

    public boolean clearNamed() {
        clear();
        return true;
    }

    public boolean dropGraphNames() {
        graph.clear();
        clearNamedGraph();
        return true;
    }
    
    void clearNamedGraph(){
        
    }

    public boolean clear(String uri, boolean isSilent) {
        if (uri != null) {
            Node gg = getGraphNode(uri);
            if (isDebug) {
                logger.debug("** clear: " + gg);
            }
            if (gg != null) {
                setDelete(true);
                getIndex(IGRAPH).clear(gg);
            }
        }

        return true;
    }

    public boolean update(String source, String target, boolean isSilent, int mode) {
        Node g1 = getGraphNode(source);
        Node g2 = getGraphNode(target);

        if (g1 == null) {
            return false;
        }
        setUpdate(true);

        if (g2 == null) {
            g2 = addGraph(target);
        }

        switch (mode) {
            case ADD:
                getIndex(IGRAPH).add(g1, g2);
                break;
            case MOVE:
                getIndex(IGRAPH).move(g1, g2);
                break;
            case COPY:
                getIndex(IGRAPH).copy(g1, g2);
                break;

        }

        return true;
    }

    public boolean add(String source, String target, boolean isSilent) {
        return update(source, target, isSilent, ADD);
    }

    public boolean move(String source, String target, boolean isSilent) {
        return update(source, target, isSilent, MOVE);
    }

    public boolean copy(String source, String target, boolean isSilent) {
        return update(source, target, isSilent, COPY);
    }

    /**
     * *******************************************************
     *
     * Distance
     *
     *******************************************************
     */
    public void setClassDistance(Distance distance) {
        this.classDistance = distance;
    }

    synchronized public Distance setClassDistance() {
        if (classDistance != null) {
            return classDistance;
        }
        setClassDistance(Distance.classDistance(this));
        return classDistance;
    }

    public Distance getClassDistance() {
        return classDistance;
    }

    public void setPropertyDistance(Distance distance) {
        this.propertyDistance = distance;
    }

    synchronized public Distance setPropertyDistance() {
        if (propertyDistance != null) {
            return propertyDistance;
        }
        setPropertyDistance(Distance.propertyDistance(this));
        return propertyDistance;
    }

    public Distance getPropertyDistance() {
        return propertyDistance;
    }

    /**
     * **************************************************
     *
     * User API
     *
     * TODO: no code here, use call to basic methods secure addEdge wrt
     * addGraph/addProperty api with IDatatype
     *
     * *************************************************
     */
    /**
     * Add a copy of the entity edge Use case: entity comes from another graph,
     * create a local copy of nodes
     */
    public Edge copy(Entity ent) {
        Node g = basicAddGraph(ent.getGraph().getLabel());
        Node p = basicAddProperty(ent.getEdge().getEdgeNode().getLabel());

        ArrayList<Node> list = new ArrayList<Node>();

        for (int i = 0; i < ent.nbNode(); i++) {
            Node n = addNode((IDatatype) ent.getNode(i).getValue());
            list.add(n);
        }

        Edge e = addEdge(g, p, list);
        return e;
    }

    /**
     * TODO: setUpdate(true)
     */
    Entity copy(Node gNode, Node pred, Entity ent) {
        Entity e = fac.copy(gNode, pred, ent);
        //fac.setGraph(e, gNode);

        if (hasTag() && e.nbNode() == 3) {
            // edge has a tag
            // copy must have a new tag
            tag(e);
        }
        Entity res = add(e);
        return res;
    }

    /**
     * Copy g into this
     */
    public List<Entity> copy(List<Entity> list) {
        for (Index id : tables) {
            if (id.getIndex() != 0) {
                id.clearCache();
            }
        }

        if (isDebug) {
            logger.info("Copy: " + list.size());
        }

        isIndex = true;
        for (Entity ent : list) {
            add(ent);
        }
        isIndex = false;

        table.index();

        return list;
    }

    @Deprecated
    public List<Entity> copy(Graph g, boolean b) {
        ArrayList<Entity> list = new ArrayList<Entity>();

        for (Index id : tables) {
            if (id.getIndex() != 0) {
                id.clearCache();
            }
        }

        for (Node pred : g.getProperties()) {
            if (isDebug) {
                logger.info("Copy: " + pred + " from " + g.size(pred) + " to " + size(pred));
            }

            for (Entity ent : g.getEdges(pred)) {

                if (!exist(ent)) {
                    list.add(ent);
                }
            }

            isIndex = true;
            for (Entity ent : list) {
                add(ent);
            }
            isIndex = false;
        }

        table.index();

        return list;
    }

    public List<Entity> copy2(Graph g, boolean b) {
        ArrayList<Entity> list = new ArrayList<Entity>();

        for (Node pred : g.getProperties()) {
            if (isDebug) {
                logger.info("Copy: " + pred + " from " + g.size(pred) + " to " + size(pred));
            }

            for (Entity ent : g.getEdges(pred)) {
                Entity ee = add(ent);
                if (ee != null) {
                    list.add(ee);
                }
            }
        }

        return list;
    }

    public void copy(Graph g) {
        copyNode(g);
        for (Entity ent : g.getEdges()) {
            copy(ent);
        }
    }
    
    void copyNode(Graph g){
    }
    
    public Graph copy(){
        Graph g = empty();
        g.copy(this);
        return g;
    }
    
    public Graph empty(){
        Graph g = Graph.create();
        g.inherit(this);
        return g;
    }
    
    void inherit(Graph g){
        setSkolem(g.isSkolem());
    }
    
    public Graph union(Graph g){
        Graph gu = Graph.create();
        gu.copy(this);
        gu.copy(g);
        return gu;       
    }

    void copyEdge(Entity ent) {
    }

    /**
     * Add edge and add it's nodes
     */
    public Entity add(Node source, Node subject, Node predicate, Node value) {
        Entity e = fac.create(source, subject, predicate, value);
        Entity ee = addEdgeWithNode(e);
        return ee;
    }

    /**
     * Add edge and add it's nodes
     */
    public Entity add(IDatatype subject, IDatatype predicate, IDatatype value) {
        Node def = addDefaultGraphNode();
        return add((IDatatype) def.getValue(), subject, predicate, value);
    }
    
    public Entity add(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        Entity e = fac.create(createNode(source), createNode(subject), createNode(predicate), createNode(value));
        Entity ee = addEdgeWithNode(e);
        return ee;
    }
    
    

    /**
     * Add Edge, not add nodes
     */
    public Edge addEdge(Node source, Node subject, Node predicate, Node value) {
        Entity e = fac.create(source, subject, predicate, value);
        Entity ee = addEdge(e);
        if (ee != null) {
            return ee.getEdge();
        }
        return null;
    }

    public Edge addEdge(Node subject, Node predicate, Node value) {
        Node g = addDefaultGraphNode();
        return addEdge(g, subject, predicate, value);
    }

    // tuple
    public Edge addEdge(Node source, Node predicate, List<Node> list) {
        Entity e;
        if (list.size() == 2) {
            e = fac.create(source, list.get(0), predicate, list.get(1));
        } else {
            e = fac.create(source, predicate, list);
        }

        Entity ee = addEdge(e);
        if (ee != null) {
            return ee.getEdge();
        }
        return null;
    }

    /**
     * Graph in itself is not considered as a graph node for SPARQL path unless
     * explicitely referenced as a subject or object Hence ?x :p* ?y does not
     * return graph nodes
     */
    public Node addGraph(String label) {
        return basicAddGraph(label);
    }
    
    public Node addDefaultGraphNode(){
        return basicAddGraphNode(defaultGraph);
    }
    
    public boolean isDefaultGraphNode(Node g){
        return g == defaultGraph; 
    }
    
    public boolean isDefaultGraphNode(String name){
        return name.equals(Entailment.DEFAULT);
    }
    
    public Node getDefaultGraphNode(){
        return defaultGraph;
    }
    
    public Node addRuleGraphNode(){
        return basicAddGraphNode(ruleGraph);
    }
       
    public Node getRuleGraphNode(){
        return ruleGraph;
    }
    
    public boolean isRuleGraphNode(Node node){
        return node == ruleGraph;
    }

    public Node addResource(String label) {
        return basicAddResource(label);
    }

    /**
     * Property in itself is not considered as a graph node for SPARQL path
     * unless explicitely referenced as a subject or object Hence ?x :p* ?y does
     * not return property nodes
     */
    public Node addProperty(String label) {
        return basicAddProperty(label);
    }

    /**
     * label *must* have been generated by newBlankID()
     */
    public Node addBlank(String label) {
        if (isSkolem) {
            return basicAddResource(label);
        } else {
            return basicAddBlank(label);
        }
    }

    public IDatatype createBlank(String label) {
        if (isSkolem) {
            return createSkolem(label);
        } else {
            return DatatypeMap.createBlank(label);
        }
    }

    public Node addBlank() {
        return addBlank(newBlankID());
    }

    public Node addLiteral(String label, String datatype, String lang) {
        IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }

    public Node addLiteral(String label, String datatype) {
        IDatatype dt = DatatypeMap.createLiteral(label, datatype, null);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }

    public Node addLiteral(String label) {
        return addLiteral(label, null, null);
    }

    public Node addLiteral(int n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    public Node addLiteral(long n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    public Node addLiteral(double n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    public Node addLiteral(float n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    public Node addLiteral(boolean n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    public void setDebug(boolean b) {
        isDebug = b;
        manager.setDebug(b);
        if (inference != null) {
            inference.setDebug(b);
        }
    }

    /**
     * *******************************************************
     */
    /**
     *
     * Generate a unique tag for each triple
     */
    Node tag() {
        IDatatype dt = DatatypeMap.newInstance(tagString());
        Node tag = getNode(dt, true, true);
        return tag;
    }

    void tag(Entity ent) {
        fac.tag(ent);
    }

    String tagString() {
        Tagger t = getTagger();
        if (t == null) {
            return key + tagCount++;
        }
        return t.tag();
    }

    public boolean hasTag() {
        return hasTag;
    }

    boolean needTag(Entity ent) {
        return hasTag()
                && ent.nbNode() == TAGINDEX
                && !getProxy().isEntailed(ent.getGraph());
    }

    public void setTag(boolean b) {
        hasTag = b;
        if (b) {
            setTuple(true);
        }
    }

    /**
     * This log would be used to broadcast deletion to peers
     */
    void logDelete(Entity ent) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                gl.delete(this, ent);
            }
        }
    }

    void logInsert(Entity ent) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                gl.insert(this, ent);
            }
        }
    }

    public void logStart(Query q) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                gl.start(this, q);
            }
        }
    }

    public void logFinish(Query q) {
        logFinish(q, null);
    }

    public void logFinish(Query q, Mappings m) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                gl.finish(this, q, m);
            }
        }
    }

    public void logLoad(String path) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                gl.load(path);
            }
        }
    }

    boolean onInsert(Entity ent) {
        if (listen != null) {
            for (GraphListener gl : listen) {
                if (!gl.onInsert(this, ent)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if query may succeed on graph PRAGMA: no RDFS entailments, simple
     * RDF match
     */
    public boolean check(Query q) {
        return check(q, q.getBody());
    }

    boolean check(Query q, Exp exp) {

        switch (exp.type()) {

            case ExpType.EDGE:
                Edge edge = exp.getEdge();
                Node pred = edge.getEdgeNode();
                Node var = edge.getEdgeVariable();

                if (var == null) {

                    if (getPropertyNode(pred) == null) {
                        // graph does not contain this property: fail now
                        return false;
                    } else if (isType(pred)) {
                        Node value = edge.getNode(1);
                        // ?c a owl:TransitiveProperty
                        if (value.isConstant()) {
                            if (getNode(value) == null) {
                                return false;
                            }
                        } else if (q.getBindingNodes().contains(value) && q.getMappings() != null) {
                            // ?c a ?t with bindings
                            for (Mapping map : q.getMappings()) {

                                Node node = map.getNode(value);
                                if (node != null && getNode(node) != null) {
                                    // graph  contain node
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                } else if (q.getBindingNodes().contains(var) && q.getMappings() != null) {
                    // property variable with bindings: check the bindings
                    for (Mapping map : q.getMappings()) {

                        Node node = map.getNode(var);
                        if (node != null && getPropertyNode(node) != null) {
                            // graph  contain a property
                            return true;
                        }
                    }

                    return false;
                }

                break;

            case ExpType.UNION:

                for (Exp ee : exp.getExpList()) {
                    if (check(q, ee)) {
                        return true;
                    }
                }
                return false;

            case ExpType.AND:
            case ExpType.GRAPH:

                for (Exp ee : exp.getExpList()) {
                    boolean b = check(q, ee);
                    if (!b) {
                        return false;
                    }
                }
        }

        return true;
    }

    public Graph getNamedGraph(String name) {
        return null;
    }

    public void setNamedGraph(String name, Graph g) {
    }

    public Dataset getDataset() {
        Dataset ds = Dataset.create();
        for (Node node : getGraphNodes()) {
            ds.addFrom(node.getLabel());
            ds.addNamed(node.getLabel());
        }
        return ds;
    }
}
