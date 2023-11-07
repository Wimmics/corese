package fr.inria.corese.core;

import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.OWL;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.index.NodeManager;
import fr.inria.corese.core.index.EdgeManagerIndexer;
import fr.inria.corese.core.producer.DataProducer;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Distinct;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.util.StorageFactory;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.api.Log;
import fr.inria.corese.core.api.Tagger;
import fr.inria.corese.core.api.ValueResolver;
import fr.inria.corese.core.edge.EdgeTripleNode;
import fr.inria.corese.core.edge.TripleNode;
import fr.inria.corese.core.query.QueryCheck;
import fr.inria.corese.core.util.Property;
import java.util.Map;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.GRAPH;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONObject;

/**
 * Graph Manager Edges are stored in an index An index is a table: predicate ->
 * List<Edge>
 * Edge List are sorted Join on a Node is computed by dichotomy getEdges()
 * return edges of all named graphs as quads Default Graph:
 * g.getDefault().iterate() Named Graphs: g.getNamed().iterate() See
 * DataProducer for more iterators
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Graph extends GraphObject implements
        Iterable<Edge>,
        fr.inria.corese.sparql.api.Graph,
        Graphable, TripleStore {

    static {
        Corese.init();
    }
    private static Logger logger = LoggerFactory.getLogger(Graph.class);
    private static final String SHAPE_CONFORM = NSManager.SHAPE + "conforms";
    public static final String SYSTEM = ExpType.KGRAM + "system";
    public static final String TOPREL
            = fr.inria.corese.sparql.triple.cst.RDFS.RootPropertyURI;
    static final ArrayList<Edge> EMPTY = new ArrayList<Edge>(0);
    public static boolean valueOut = !true;
    public static final int IGRAPH = -1;
    // edges in chronological order
    public static final int ILIST = -2;
    // NB of Index (subject, object)
    public static final int LENGTH = 2;

    public static final int DEFAULT = 0;
    public static final int EXTENSION = 1;

    public static final int COPY = 0;
    public static final int MOVE = 1;
    public static final int ADD = 2;
    public static final int CLEAR = 3;
    static long blankid = 0;
    static long triplerefid = 0;
    public static String BLANK = "_:b";
    public static String TRIPLE_REF = "_:t";
    static final String SKOLEM = ExpType.SKOLEM;
    private static final String NL = System.getProperty("line.separator");
    static final int TAGINDEX = 2;
    static boolean byIndexDefault = true;
    public static boolean VERBOSE = false;
    public static boolean DEBUG_SPARQL = false;
    public static boolean SKOLEM_DEFAULT = false;
    // graph ?g { } iterate std and external named graph when true
    public static boolean EXTERNAL_NAMED_GRAPH = false;
    // specific graph name for rule constraint error
    public static boolean CONSTRAINT_NAMED_GRAPH = true;
    public static boolean CONSTRAINT_GRAPH = false;
    // Prototype for additional Node e.g. fuzzy edge
    public static boolean METADATA_DEFAULT = false;
    // RDF Star
    public static boolean EDGE_METADATA_DEFAULT = false;
    // for external agent such as corese gui, meaningless otherwise
    public static boolean RDFS_ENTAILMENT_DEFAULT = true;
    // same triple s p o have same reference node in different named graphs
    public static boolean TRIPLE_UNIQUE_NAME = true;

    private static final String[] PREDEFINED = {
        Entailment.DEFAULT, Entailment.ENTAIL, Entailment.RULE, Entailment.CONSTRAINT,
        RDFS.SUBCLASSOF, RDFS.LABEL,
        RDF.TYPE, RDF.FIRST, RDF.REST
    };

    public static final int DEFAULT_INDEX = 0;
    public static final int ENTAIL_INDEX = 1;
    public static final int RULE_INDEX = 2;
    public static final int RULE_CONSTRAINT = 3;

    public static final int SUBCLASS_INDEX = 4;
    public static final int LABEL_INDEX = 5;

    public static final int TYPE_INDEX = 6;
    public static final int FIRST_INDEX = 7;
    public static final int REST_INDEX = 8;

    // @todo: currently useless
    public static final int DEFAULT_UNION = 0;
    public static final int DEFAULT_GRAPH = 1;
    public static int DEFAULT_GRAPH_MODE = DEFAULT_UNION;

    private int defaultGraphMode = DEFAULT_GRAPH_MODE;

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
    // List of subject/object/graph Index
    // Index is HashMap: PredicateNode -> List of Edge with PredicateNode as predicate
    // In the Index, edge does not contain the predicate Node to spare memory
    // List of edge is sorted by Node index, each Node is allocated an integer index 
    // Index of subject: edge list sorted by subject/object/graph
    // Index of object:  edge list sorted by object/subject/graph
    // Index of graph:   edge list sorted by graph/subject/object
    private ArrayList<EdgeManagerIndexer> tables;
    // Index of subject with index=0
    private EdgeManagerIndexer subjectIndex;
    private EdgeManagerIndexer namedGraphIndex;
    // edge Index for RuleEngine where edge are sorted newest first
    EdgeManagerIndexer ruleEdgeIndex;
    // predefined individual Node such as kg:default named graph
    HashMap<String, Node> system;
    // key -> URI Node ; member of graph nodes (subject/object)
    Hashtable<String, Node> individual;
    // label -> Blank Node ; member of graph nodes (subject/object)
    Hashtable<String, Node> blank;
    // Triple Reference Node
    Hashtable<String, Node> triple;
    // named graph id nodes: key -> named graph id Node (possibly not subject/object Node)
    Hashtable<String, Node> graph;
    // property nodes: label -> property Node (possibly not subject/object Node)
    Hashtable<String, Node> property;
    // allocate Node (1 and 01 have different Node)
    private SortedMap<IDatatype, Node> literalNodeManager;
    // allocate index (1 and 01 have same index)
    private SortedMap<IDatatype, Node> literalIndexManager;
    // @todo
    // key -> Node for value management in external memory
    Map<String, Node> vliteral;
    ValueResolver values;
    // Node iterator for named Graph
    NodeGraphIndex nodeGraphIndex;
    Log log;
    private List<GraphListener> listenerList;
    Workflow manager;
    EventManager eventManager;
    // @deprecated (use case: crdt datatypes ...)
    Tagger tag;
    // RDFS Entailment
    Entailment proxy;
    EdgeFactory fac;
    // @deprecated history management
    private Context context;
    // semantic distance in class/property Hierarchy
    private Distance classDistance, propertyDistance;
    private boolean isSkolem = SKOLEM_DEFAULT;
    boolean isIndexable = true,
            isDebug = !true;
    private boolean debugSparql = DEBUG_SPARQL;
    // edge index sorted by index
    boolean byIndex = byIndexDefault;
    // number of edges
    int size = 0;
    // counter for Graph Node index
    int nodeIndex = 0;
    private int tagCount = 0;
    // skolem 
    private String key;
    // name of this graph
    private String name;
    // @deprecated
    private boolean hasTag = false;
    // edge with List of Nodes (not just subject/object)
    private boolean isTuple = false;
    // prototype for additional Node e.g. fuzzy edge
    private boolean metadata = METADATA_DEFAULT;
    // RDF Star
    private boolean edgeMetadata = EDGE_METADATA_DEFAULT;
    // consider external namedGraph Nodes (see ProducerImpl and GraphStore)
    private boolean allGraphNode = EXTERNAL_NAMED_GRAPH;
    //public int count = 0;
    // true when there is a specific Edge Index for RuleEngine
    private boolean hasRuleEdgeList = false;
    // predefined Node for specific named Graph
    private Node // rule entailment named graph
            ruleGraph,
            // rule error named graph (cf RuleEngine OWL RL inconsistency) 
            constraintGraph,
            // kg:default named graph
            defaultGraph,
            // RDFS entailment named graph
            entailGraph;
    // List of predefined (graph) Node  
    private ArrayList<Node> systemNode;
    List<Edge> emptyEdgeList;

    // Manager of sparql edge iterator with possible default graph specification
    DataStore dataStore;
    // @todo external memory literal value manager
    private IStorage storageMgr;

    static {
        setCompareIndex(true);
    }

    public boolean isSkolem() {
        return isSkolem;
    }

    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Contain undefined datatype
     */
    public boolean isFlawed() {
        for (Node ent : getLiteralNodes()) {
            IDatatype dt =  ent.getValue();
            if (DatatypeMap.isUndefined(dt)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCorrect() {
        return ! isFlawed();
    }

    public boolean typeCheck() {
        if (getEntailment() == null) {
            return true;
        }
        return getEntailment().typeCheck();
    }

    // Shape result graph, return sh:conforms value
    public boolean conform() {
        Edge e = getEdge(SHAPE_CONFORM);
        if (e == null) {
            return false;
        }
        return e.getNode(1).getDatatypeValue().booleanValue();
    }

    public boolean isTuple() {
        return isTuple;
    }

    public void setTuple(boolean isTuple) {
        this.isTuple = isTuple;
    }

    public boolean hasRuleEdgeList() {
        return hasRuleEdgeList;
    }

    public void setHasList(boolean hasRuleEdgeList) {
        this.hasRuleEdgeList = hasRuleEdgeList;
        setList();
    }

    /**
     * Create specific Index where edges are sorted newest first Use case:
     * RuleEngine focus on new edges
     */
    void setList() {
        if (hasRuleEdgeList) {
            ruleEdgeIndex = createIndex(byIndex, ILIST);
            getIndexList().add(ruleEdgeIndex);
        } else if (getIndexList().get(getIndexList().size() - 1).getIndex() == ILIST) {
            getIndexList().remove(getIndexList().size() - 1);
            ruleEdgeIndex = null;
        }
    }

    EdgeManagerIndexer createIndex(boolean b, int i) {
        return new EdgeManagerIndexer(this, b, i);
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
    public String getDatatypeLabel() {
        return String.format("[Graph: size=%s]", size());
    }

    /**
     * Return copy edges in specific objects
     *
     * @return
     */
    @Override
    public Iterable<Edge> getLoop() {

        Iterator<Edge> it = iterator();

        return () -> new Iterator<Edge>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                Edge edge = it.next();
                if (edge == null) {
                    return null;
                }
                return getEdgeFactory().copy(edge);
            }
        };
    }

    DataProducer getDataProducer(Node... from) {
        DataProducer dp;
        if (from == null || from.length == 0) {
            dp = getDataStore().getNamed(null, null);
        } else {
            dp = getDataStore().getNamed(Arrays.asList(from), null);
        }
        return dp;
    }

    /**
     * Return statements with the specified subject, predicate, object and
     * (optionally) from exist in this graph.
     *
     * @param s    The subject of the statements to match, null to match statements
     *             with any subject.
     * @param p    Predicate of the statements to match, null to match statements
     *             with any predicate.
     * @param o    Object of the statements to match, null to match statements with
     *             any object.
     * @param from Contexts of the statements to match. If from = Node[0] or from =
     *             null, statements will match disregarding their context.
     * @return List of edges that match the specified pattern.
     */
    public Iterable<Edge> getEdgesRDF4J(Node s, Node p, Node o, Node... from) {
        DataProducer dp = getDataProducer(from);
        return dp.iterate(bnvalue(s), bnvalue(p), bnvalue(o));
    }

    IDatatype bnvalue(Node n) {
        if (n == null) {
            return DatatypeMap.createBlank();
        }
        return  n.getDatatypeValue();
    }

    /**
     * Iterate graph edges Successive edges of the same property are returned in
     * the "same physical" Edge object The reason is that edges are stored
     * without the property Node. Iterator creates a buffer to store a copy of
     * edges with the property Node. For performance purpose in SPARQL Producer,
     * we iterate edges in the same buffer If needed, it may be necessary to
     * make copy of edge buffer using g.getEdgeFactory().copy(edge) .
     */
    @Override
    public Iterator<Edge> iterator() {
        return getEdges().iterator();
    }

    @Override
    public IDatatype getValue(String var, int n) {
        int i = 0;
        for (Edge ent : getEdges()) {
            if (i++ == n) {
                return DatatypeMap.createObject(ent);
            }
        }
        return null;
    }

    public Context getContext() {
        if (context == null) {
            context = new Context(this);
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public PointerType pointerType() {
        return GRAPH;
    }

    public int getDefaultGraphMode() {
        return defaultGraphMode;
    }

    public void setDefaultGraphMode(int defaultGraph) {
        this.defaultGraphMode = defaultGraph;
    }
    
    public TreeNode treeNode() {
        return new TreeNode();
    }

    /**
     * With CompareNode: manage 1, 01, 1.0 as different Node (with same index)
     * With CompareIndex: manage IDatatype(1) IDatatype(01) IDatatype(1.0) with
     * same index 
     * With CompareIndexStrict: manage IDatatype(1) IDatatype(01)
     * with same index and 1.0 with different index (sparql compliant)
     */
    public class TreeNode extends TreeMap<IDatatype, Node> {

        // allocate Node, sameTerm semantics
        // 1 and 01 and 1.0 have different Node
        public TreeNode() {
            super(new CompareNode());
        }

        // allocate Node index
        TreeNode(boolean entailment) {
            super((entailment)
                    ? // with datatype entailment
                    // index(1) = index(01) = index(1.0)
                    // same value => same index 
                    // integer|decimal vs integer|decimal => same index
                    // integer|decimal vs double|float    => different index                    
                    new CompareWithDatatypeEntailment() :
                    // without datatype entailment
                    // index(1) = index(01) != index(1.0) 
                    // different datatype => different index
                    new CompareWithoutDatatypeEntailment());
        }

        void put(Node node) {
            put( node.getDatatypeValue(), node);
        }

        boolean contains(Node node) {
            return containsKey( node.getDatatypeValue());
        }
    }


    /**
     * Comparator for Node allocation where 1 and 01 have different Node with same Node index
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node It represents (1 integer) and (01
     * integer) as two different nodes that will be assigned the same node index
     * in order to join in SPARQL
     */
    class CompareNode implements Comparator<IDatatype> {

        CompareNode() {
        }

        // sameTerm semantics, strict order, 1 != 01 (to get different Node)
        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {
            int res = dt1.compareTo(dt2);
            return res;
        }
    }

    public Graph() {
        this(LENGTH);
    }

    public Graph(int length) {
        lock = new ReentrantReadWriteLock();

        setIndexList(new ArrayList<>(length));

        // index of subject/object
        for (int i = 0; i < length; i++) {
            getIndexList().add(createIndex(byIndex, i));
        }

        // index of graph name
        setNamedGraphIndex(createIndex(byIndex, IGRAPH));
        getIndexList().add(getNamedGraphIndex());

        // index of subject
        setSubjectIndex(getIndex(0));

        // Note: 
        // nodeManager  allocate different Node to 1, 01, 1.0
        // indexManager allocate same Node index to 1, 01 (and also 1.0 as corese default mode) 
        // Literals (all of them) comparator = CompareNode and compareTo()
        // different Node allocated when different value or different datatype or different label
        setLiteralNodeManager(Collections.synchronizedSortedMap(new TreeNode()));
        // Literal numbers and booleans to manage Node index:
        // comparator = CompareIndex and compare()
        // 1, 01, 1.0 have same index, 1 double has different index
        // same index means that SPARQL perform a join on nodes with same index
        // when DatatypeMap.SPARQLCompliant = false (true), 1 and 1.0 have same (different) index
        // corese default is false, which means that corese sparql perform a join on 1 and 1.0 (which is not standard)
        setLiteralIndexManager(Collections.synchronizedSortedMap(new TreeNode(DatatypeMap.DATATYPE_ENTAILMENT)));
        // deprecated:
        vliteral = Collections.synchronizedMap(new HashMap<>());
        // URI Node
        individual = new Hashtable<>();
        // Blank Node
        blank = new Hashtable<>();
        // rdf star triple reference node
        triple = new Hashtable<>();
        // Named Graph Node
        graph = new Hashtable<>();
        // Property Node
        property = new Hashtable<>();

        // Index of nodes of named graphs
        // Use case: SPARQL Property Path
        nodeGraphIndex = new NodeGraphIndex();
        // @todo: values stored in external memory 
        values = new ValueResolverImpl();
        fac = new EdgeFactory(this);
        // Entailment Manager: RuleEngine, RDFS Entailment
        manager = new Workflow(this);
        key = hashCode() + ".";
        initSystem();
        // default and named graph manager for sparql edge iterator 
        dataStore = new DataStore(this);
        eventManager = new EventManager(this);
        eventManager.setVerbose(VERBOSE);
        emptyEdgeList = new ArrayList<>(0);
    }

    /**
     * System Node are predefined such as kg:default Node for default graph They
     * have an index but they are not yet stored in any graph table but system
     * table They are retrieved by getResource, getNode, getGraph, getProperty
     * on demand
     */
    void initSystem() {
        system = new HashMap<>();
        systemNode = new ArrayList<>();
        for (String uri : PREDEFINED) {
            Node n = createSystemNode(uri);
            system.put(uri, n);
            systemNode.add(n);
        }
        defaultGraph = system.get(Entailment.DEFAULT);
        ruleGraph = system.get(Entailment.RULE);
        constraintGraph = system.get(Entailment.CONSTRAINT);
    }

    Node createSystemNode(String label) {
        IDatatype dt = DatatypeMap.newResource(label);
        Node node = NodeImpl.create(dt, this);
        index(dt, node);
        return node;
    }

    Node getSystemNode(String name) {
        return system.get(name);
    }

    @Override
    public Node getNode(int n) {
        return systemNode.get(n);
    }

    public Node getNodeDefault() {
        return getNode(DEFAULT_INDEX);
    }

    public Node getNodeRule() {
        return getNode(RULE_INDEX);
    }

    public Node getNodeEntail() {
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
        //Group.setCompareIndex(b);
        //MatcherImpl.setCompareIndex(b);
    }

    /**
     * set byIndex on this graph only reset EdgeIndex as well and sort edge list
     * accordingly
     */
    public void setByIndex(boolean b) {
        byIndex = b;
        for (EdgeManagerIndexer id : getIndexList()) {
            id.setByIndex(b);
        }
    }

    public boolean isByIndex() {
        return byIndex;
    }

    /**
     *
     */
    @Deprecated
    public static void setDistinctDatatype(boolean b) {

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
    public synchronized void process() throws EngineException {
        manager.process();
    }

    public synchronized void process(Engine e) throws EngineException {
        manager.process(e);
    }

    /**
     * Remove entailments
     */
    public synchronized void remove() {
        manager.remove();
    }

    public void addListener(GraphListener gl) {
        if (getListenerList() == null) {
            setListenerList(new ArrayList<>());
        }
        if (!listenerList.contains(gl)) {
            getListenerList().add(gl);
            gl.addSource(this);
        }
    }

    public void removeListener(GraphListener gl) {
        if (getListenerList() != null) {
            getListenerList().remove(gl);
        }
    }

    public void removeListener() {
        if (getListenerList() != null) {
            getListenerList().clear();
        }
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
        return getLock().readLock();
    }

    public Lock writeLock() {
        return getLock().writeLock();
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public boolean isReadLocked() {
        return getLock().getReadLockCount() > 0;
    }

    public boolean isLocked() {
        return isReadLocked() || getLock().isWriteLocked();
    }

    void clearDistance() {
        setClassDistance(null);
        setPropertyDistance(null);
    }

    public Entailment getEntailment() {
        return getWorkflow().getEntailment();
    }

    /**
     * Set RDFS entailment
     */
    public void setEntailment() {
        getWorkflow().setEntailment();
    }

    /**
     * Use Case: GUI Remove or perform RDFS Entailment
     */
    synchronized public void setRDFSEntailment(boolean b) {
        getWorkflow().setRDFSEntailment(b);
        if (b) {
            getEventManager().start(Event.ActivateEntailment);
            init();
        }
    }

    public void pragmaRDFSentailment(boolean b) {
        getWorkflow().pragmaRDFSentailment(b);
        if (b) {
            getEventManager().start(Event.ActivateEntailment);
        }
    }

    public void set(String property, boolean value) {
        localSet(property, value);
        if (getEntailment() != null) {
            getEntailment().set(property, value);
        }
    }

    void localSet(String property, boolean value) {
        if (property.equals(Entailment.DUPLICATE_INFERENCE)) {
            for (EdgeManagerIndexer t : getIndexList()) {
                t.setDuplicateEntailment(value);
            }
        }
    }

//    public void setDefault(boolean b) {
//        hasDefault = b;
//    }
//
//    public boolean hasDefault() {
//        return hasDefault;
//    }
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
        sb.appendPNL("kg:property ", getSubjectIndex().size());
        sb.appendPNL("kg:uri      ", individual.size());
        sb.appendPNL("kg:bnode    ", blank.size());
        sb.appendPNL("kg:triple    ", triple.size());
        sb.appendPNL("kg:literal  ", getLiteralNodeManager().size());
        sb.appendPNL("kg:nodeManager  ", getNodeManager().isEffective());
        if (getNodeManager().isEffective()) {
            sb.appendPNL("kg:nbSubject  ", getNodeManager().size());
            sb.appendPNL("kg:nbProperty  ", getNodeManager().count());
        }
        sb.appendPNL("kg:date     ", DatatypeMap.newDate());

        sb.close();

        for (EdgeManagerIndexer t : getIndexList()) {
            if (t.getIndex() == 0 || t.cardinality() > 0) {
                sb.appendNL(t.toRDF());
            }
        }

        return sb.toString();
    }

    public NodeManager getNodeManager() {
        return getIndex().getNodeManager();
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

        for (Node e : getNodes()) {
            uri++;
        }

        for (Node e : getBlankNodes()) {
            blank++;
        }

        for (Node e : getLiteralNodes()) {
            IDatatype dt =  e.getValue();
            if (dt.isNumber()) {
                num++;
            } else if (dt.getCode() == IDatatype.STRING) {
                string++;
            } else if (dt.getCode() == IDatatype.LITERAL) {
                lit++;
            } else if (dt.isDate()) {
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
    
    public String display(int max) {
        return display(0, max);
    }

    public String display() {
        return display(0, Integer.MAX_VALUE);
    }

    public String display(int n, int max) {
        String sep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        for (Node p : getSortedProperties()) {
            if (sb.length() > 0) {
                sb.append(NL);
            }
            sb.append(String.format("predicate %s [%s]", p, getIndex().size(p)));
            sb.append(sep);
            int i = 0;
            for (Edge ent : (n == 0) ? getEdges(p) : getIndex(n).getEdges()) {
                sb.append((i < 10) ? "0" : "").append(i++).append(" ");
                sb.append(ent);
                sb.append(sep);
                if (i>=max) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    public Entailment getProxy() {
        if (proxy == null) {
            proxy = getEntailment();
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
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public void setEventHandler(EventHandler h) {
        getEventManager().setEventHandler(h);
    }

    public void setVerbose(boolean b) {
        getEventManager().setVerbose(b);
        if (b) {
            // hide to logger
            getEventManager().hide(Event.Insert);
            getEventManager().hide(Event.Construct);
        }
    }

    public boolean isVerbose() {
        return getEventManager().isVerbose();
    }

    // RDF Star
    public boolean isEdgeMetadata() {
        return edgeMetadata;
    }
    
    public boolean isRDFStar() {
        return edgeMetadata;
    }

    public void setEdgeMetadata(boolean b) {
        edgeMetadata = b;
    }

    public static void setEdgeMetadataDefault(boolean b) {
        EDGE_METADATA_DEFAULT = b;
    }

    public static void setRDFStar(boolean b) {
        setEdgeMetadataDefault(b);
    }

    /**
     * EventManager call init() before query execution 
     * First time: Index graph: edge list sorted and reduced, compute graph node index
     * Next time:  recompute graph node index
     * All time:   Perform entailment.
     */
    public synchronized void init() {
        getEventManager().start(Event.InitGraph, isIndexable());

        if (isIndexable()) {
            // sort edge list and reduce (delete duplicate edges)
            index();
        }

        if (getEventManager().isUpdate()) {
            // use case: previously load or sparql update
            // clear nodeIndexManager
            onUpdate();
            // recompute IndexNodeManager
            performIndexNodeManager();
        }

        if (getEventManager().isEntail() && getWorkflow().isAvailable()) {
            try {
                // run manager inference engine
                process();
            } catch (EngineException ex) {
                logger.error(ex.getMessage());
            }
            getEventManager().setEntail(false);
            if (isDebug && getEventManager().isUpdate()) {
                logger.info("Graph modified after entailment");
            }
        }

        // when entailment has modified the graph
        // recompute IndexNodeManager
        performIndexNodeManager();

        getEventManager().finish(Event.InitGraph);
    }

    public IDatatype start() {
        init();
        return DatatypeMap.TRUE;
    }

    private void onUpdate() {
        getEventManager().setUpdate(false);
        // node index
        clearNodeIndex();
        clearDistance();
        if (getEventManager().isDelete()) {
            manager.onDelete();
            getEventManager().setDelete(false);
        }
    }

    public void clean() {
        // clean timestamp index
        if (hasRuleEdgeList) {
            ruleEdgeIndex.clean();
        }
    }

    public void cleanEdge() {
        // clean rule engine timestamp
        for (Edge ent : getEdges()) {
            ent.setEdgeIndex(-1);
        }
    }

    public boolean hasEntailment() {
        return getEntailment() != null && getEntailment().isActivate();
    }

    // true when index must be sorted 
    public boolean isIndexable() {
        return isIndexable;
    }
    
    public void setIndexable(boolean b) {
        isIndexable = b;
    }
    
    // already indexed
    public boolean isIndexed() {
        return ! isIndexable();
    }
    
    public void setIndexed(boolean b) {
        isIndexable = !b;
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

    public EdgeManagerIndexer getIndex() {
        return getSubjectIndex();
    }

    void startUpdate() {
    }
    
    public void finishUpdate() {
        getIndex().finishUpdate();
    }
    
    public void finishRuleEngine() {
        getIndex().finishRuleEngine();
    }
            
    public void startLoad() {
        if (size() == 0 || Property.booleanValue(Property.Value.GRAPH_INDEX_LOAD_SKIP)) {
            // graph is empty, optimize loading as if the graph were not indexed
            // because in this case, edges are added directly
            //logger.info("Set graph as not indexed");
            setIndexed(false);
        }
    }

    /**
     * Graph updated: nodeManager content is obsolete
     */
    void eventUpdate() {
        clearNodeManager();
    }   
    
    
    
    
    // Clear Index node -> (predicate:position)
    public void clearNodeManager() {
        for (EdgeManagerIndexer id : getIndexList()) {
            id.getNodeManager().desactivate();
        }
    }
    
    // Clear Index of nodes in their named graph
    void clearNodeIndex() {
        getNodeGraphIndex().clear();
    }



    /**
     * Pragma: to be called after reduce (after index()))
     *
     * @param b
     */
    public void tuneNodeManager(boolean b) {
        for (EdgeManagerIndexer id : getIndexList()) {
            if (b) {
                id.getNodeManager().setAvailable(b);
                if (id.getIndex() == 0) {
                    id.indexNodeManager();
                }
            } else {
                id.getNodeManager().desactivate();
                id.getNodeManager().setAvailable(b);
            }

        }
    }

    /**
     * When load is finished, sort edges Side effect: index NodeManager
     */
    public void index() {
        if (size() > 0) {
            getEventManager().start(Event.IndexGraph);
            basicIndex();
            getEventManager().finish(Event.IndexGraph);
            setIndexed(true);
        }
    }

    void basicIndex() {
        for (EdgeManagerIndexer ei : getIndexList()) {
            ei.index();
        }
    }

    public void compact() {
        cleanIndex();
        if (containsCoreseNode(getNode(Graph.RULE_INDEX))) {
            getSubjectIndex().compact();
        }
    }

    public void cleanIndex() {
        for (EdgeManagerIndexer ei : getIndexList()) {
            if (ei.getIndex() != 0) {
                ei.clean();
            }
        }
    }

    /**
     * Prepare the graph in order to perform eg a Query In practice it generates
     * the Index properly.
     */
    public void prepare() {
        getEventManager().start(Event.Process);
    }

    void indexGraph() {
        if (isIndexable()) {
            index();
        }
        performIndexNodeManager();
    }

    void performIndexNodeManager() {
        if (!getNodeManager().isActive()) {
            indexNodeManager();
        }
    }
    
   /**
    * Index node -> (predicate:position)
    * Pragma: graph must be indexed first
    */
    public void indexNodeManager() {
        getIndex().indexNodeManager();
    }

    // Index graph nodes in their named graph
    synchronized void indexNode() {
        if (getNodeGraphIndex().size() == 0) {
            getSubjectIndex().indexNode();
        }
    }

    public void indexResources() {
        int i = 0;
        for (Node n : getRBNodes()) {
            n.setIndex(i++);
        }
    }

    // declare subject/object as graph vertex
    public void define(Edge ent) {
        if (ent.isAsserted()) {
            nodeGraphIndex.add(ent);
        }
    }

    public Iterable<Node> getProperties() {
        return getSubjectIndex().getProperties();
    }

    public Iterable<Node> getSortedProperties() {
        return getSubjectIndex().getSortedProperties();
    }
    
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        insert(addBlank().getDatatypeValue(), key, value);
        return value;
    }
      
    public Edge add(Edge edge) {
        return add(edge, true);
    }

    public Edge add(Edge edge, boolean duplicate) {
        // store edge in index 0
        Edge ent = getSubjectIndex().add(edge, duplicate);
        // tell other index that predicate has instances
        if (ent != null) {
            if (edge.getGraph() == null) {
                System.out.println("Graph: " + edge);
            }
            addGraphNode(edge.getGraph());
            addPropertyNode(edge.getEdgeNode());
            declare(edge, duplicate);
            size++;
        }
        return ent;
    }

    void declare(Edge edge, boolean duplicate) {
        for (EdgeManagerIndexer ei : getIndexList()) {
            if (ei.getIndex() != 0) {
                ei.declare(edge, duplicate);
            }
        }
    }

    public boolean exist(Edge edge) {
        return getSubjectIndex().exist(edge);
    }
    
    public Edge find(Edge edge) {
        return getSubjectIndex().find(edge);
    }
    
    public Edge findEdge(Node s, Node p, Node o) {
        return getSubjectIndex().findEdge(s, p, o);
    }
    
    public TripleNode findTriple(Node s, Node p, Node o) {
        Edge edge = findEdge(s, p, o);
        if (edge == null || ! edge.isTripleNode()) {
            return null;
        }
        return ((EdgeTripleNode) edge).getTriple();
    }

    public boolean exist(Node p, Node n1, Node n2) {
        p = getPropertyNode(p);
        if (p == null) {
            return false;
        }
        return getSubjectIndex().exist(p, n1, n2);
    }
    
    public Edge addEdgeWithNode(Edge ee) {
        addEdgeNode(ee);
        return addEdge(ee);
    }

    /**
     * DataManager create and insert nodes in graph and then insert
     * edge
     *
     */
    public Edge insertEdgeWithTargetNode(Edge ee) {
        ee.setGraph(basicAddGraph(ee.getGraph()));
        ee.setProperty(basicAddProperty(ee.getProperty().getLabel()));
        ee.setNode(0, addNode(ee.getNode(0)));
        ee.setNode(1, addNode(ee.getNode(1)));
        Edge res = addEdge(ee);
        return res;
    }
    
    public Iterable<Edge> deleteEdgeWithTargetNode(Edge edge) {
        return delete(
                edge.getGraphNode(), edge.getSubjectNode(),
                edge.getPropertyNode(),
                edge.getObjectNode());
    }

    public void addEdgeNode(Edge ee) {
        addGraphNode(ee.getGraph());
        addPropertyNode(ee.getEdgeNode());
        for (int i = 0; i < ee.nbGraphNode(); i++) {
            add(ee.getNode(i));
        }
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

    public Edge addEdge(Edge edge) {
        return addEdge(edge, true);
    }

    public Edge addEdge(Edge edge, boolean duplicate) {
        Edge ent = add(edge, duplicate);
        if (ent != null) {
            getEventManager().process(Event.Insert, ent);
            manager.onInsert(ent.getGraph(), edge);
        }
        return ent;
    }


    public void addOpt(Node p, List<Edge> list) {
        if (list.isEmpty()) {
            return;
        }
        if (p == null) {
            addOpt(list);
        } else {
            p = list.get(0).getEdgeNode();
            add(p, list);
        }
    }

    /**
     * PRAGMA: there is no duplicate in list, all edges are inserted predicate
     * is declared in graph TODO: if same predicate, perform ensureCapacity on
     * Index list
     */
    void add(Node p, List<Edge> list) {
        for (EdgeManagerIndexer ei : getIndexList()) {
            ei.add(p, list);
        }
        //getEventManager().process(Event.Insert);
        for (Edge e : list) {
            getEventManager().process(Event.Insert, e);
        }
        size += list.size();
    }

    /**
     * Use cas: RuleEngine PRAGMA: edges in list do not exists in graph (no
     * duplicate)
     */
    public void addOpt(List<Edge> list) {
        if (list.isEmpty()) {
            return;
        }
        // fake index not sorted, hence add(edge) is done at end of index list
        setIndexed(false);
        HashMap<String, Node> t = new HashMap<>();

        for (Edge ee : list) {

            Node pred = ee.getEdgeNode();
            t.put(pred.getLabel(), pred);

            // add Edge at the end of list index
            addEdge(ee);
        }

        for (Node pred : t.values()) {
            for (EdgeManagerIndexer ei : getIndexList()) {
                // sort but does not reduce:
                ei.index(pred);
            }
        }
        setIndexed(true);
    }

    /**
     * Use case: Entailment PRAGMA: edges in list may exist in graph
     */
    public List<Edge> copy(List<Edge> list) {
        for (EdgeManagerIndexer id : getIndexList()) {
            if (id.getIndex() != 0) {
                id.clearCache();
            }
        }

        if (isDebug) {
            logger.info("Copy: " + list.size());
        }

        // fake Index not sorted to add edges at the end of the Index
        setIndexed(false);
        for (Edge ent : list) {
            Edge e = add(ent);
            if (e != null) {
                getEventManager().process(Event.Insert, e);
            }
        }
        setIndexed(true);
        // sort and reduce
        getSubjectIndex().index();

        return list;
    }

    public Edge create(Node source, Node subject, Node predicate, Node value) {
        return fac.create(source, subject, predicate, value);
    }
    
    /**
     * create edge for insert in the graph
     * pragma: nodes are already inserted
     */
    public Edge createForInsert(Node source, Node subject, Node predicate, Node value) {
        Edge edge = fac.create(source, subject, predicate, value);
        if (edge.isTripleNode()) {
            initTripleNode(edge);
        }
        return edge;
    }
    
    /**
     * edge = (g, TripleNode(s p o))
     * index or share existing (s p o)
     */
    void initTripleNode(Edge edge) {
        Node tripleNode = edge.getTripleNode();
        if (tripleNode.getDatatypeValue() == null) {
            IDatatype dt = createTripleReference(edge);
            Node node = getTripleNode(dt.getLabel());
            if (node == null) {
                tripleNode.setDatatypeValue(dt);
                indexNode(dt, tripleNode);
                addTripleNode(dt, tripleNode);
            }
            else {
                // share existing TripleNode
                edge.setTripleNode(node);
            }
            edge.getTripleNode().setEdge(tripleNode.getEdge());
        }
    }

    public Edge createDelete(Node source, Node subject, Node predicate, Node value) {
        return fac.createDelete(source, subject, predicate, value);
    }

    public Edge createDelete(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        Node graph = (source == null) ? null : getCreateNode(source);
        return fac.createDelete(graph, getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }

    Node getCreateNode(IDatatype dt) {
        return getNode(dt, true, false);
    }

    public Edge create(Node source, Node predicate, List<Node> list) {
        return fac.create(source, predicate, list);
    }
    
    public Edge create(Node source, Node predicate, List<Node> list, boolean nested) {
        return fac.create(source, predicate, list, nested);
    }

    public Edge createDelete(Node source, Node predicate, List<Node> list) {
        return fac.createDelete(source, predicate, list);
    }
    
    public Edge create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value, IDatatype ref) {
        List<Node> list = list(
                getCreateNode(subject),
                getCreateNode(value),
                getCreateNode(ref));
        return create(getCreateNode(source), getCreateNode(predicate), list);
    }
    
    // rdf star 
    // triple(s, p, o)  
    // filter bind <<s p o>>
    public IDatatype createTriple(IDatatype s, IDatatype p, IDatatype o) {
        IDatatype ref = createTripleReference();
        Edge e = create(getDefaultGraphDatatypeValue(), s, p, o, ref);
        e.setCreated(true);
        e.setNested(true);
        return ref;
    }
    
    List<Node> list(Node... list) {
        ArrayList<Node> alist = new ArrayList<>();
        alist.addAll(Arrays.asList(list));
        return alist;
    }

    public Edge create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        return create(getCreateNode(source), getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }
    
    public Edge create(IDatatype subject, IDatatype predicate, IDatatype value) {
        return create(getDefaultGraphNode(), getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }

    @Override
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
    
    public int nbTriples() {
        return triple.size();
    }

    public int nbLiterals() {
        return getLiteralNodeManager().size();
    }

    public void setSize(int n) {
        size = n;
    }

    public Node copy(Node node) {
        Node res = getExtNode(node);
        if (res == null) {
            res = getNode(getDatatypeValue(node), true, false);
        }
        return res;
    }

    IDatatype getDatatypeValue(Node node) {
        return  node.getValue();
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
    
     public Node getTopClass(String defaut, String... nameList) {
        for (String name : nameList) {
            Node n = getNode(name);
            if (n != null) {
                return n;
            }
        }
        return createNode(defaut);
    }

    public Node getTopProperty() {
        Node n = getNode(TOPREL);
        if (n == null) {
            n = createNode(TOPREL);
        }
        return n;
    }

    /**
     * predicate = rdfs:subClassOf return top level classes: those that are
     * object of subClassOf but not subject
     */
    public List<Node> getTopLevel(Node predicate) {
        ArrayList<Node> list = new ArrayList<>();
        TreeNode subject = new TreeNode(), object = new TreeNode();

        for (Edge edge : getEdges(predicate)) {
            subject.put(edge.getNode(0));
            object.put(edge.getNode(1));
        }

        for (Node node : object.values()) {
            if (!subject.contains(node) && !list.contains(node)) {
                list.add(node);
            }
        }
        return list;
    }

    public List<Node> getTopProperties() {
        List<Node> nl = new ArrayList<>();
        Node n;
        if (nl.isEmpty()) {
            n = getTopProperty();
            nl.add(n);
        }

        return nl;
    }

    // used by construct
    public Node getNode(Node gNode, IDatatype dt, boolean create, boolean add) {
        if (dt.isBlank() && ! dt.isTriple() && isSkolem()) {
            dt = skolem(dt);
        }
        return getNode(dt, create, add);
    }

    /**
     * Given a constant query node, return the target node in current graph if
     * it exists
     *
     */
    @Override
    public Node getNode(Node node) {
        IDatatype dt = getDatatypeValue(node);
        return getNode(dt, false, false);
    }

    @Override
    public Node getVertex(Node node) {
        if (node.getDatatypeValue().isURI()) {
            return getNode(node.getLabel());
        }
        return getNode(node);
    }

    public Node createNode(IDatatype dt) {
        return getNode(dt, true, false);
    }

    // all nodes
    // TODO: check producer
    public Node addNode(IDatatype dt) {
        return getNode(dt, true, true);
    }

    public Node addNode(Node node) {
        return getNode(value(node), true, true);
    }

    // used by construct
    public Node getNode(IDatatype dt, boolean create, boolean add) {
        if (dt.isLiteral()) {
            return getLiteralNode(dt, create, add);
        } 
        else if (dt.isTriple()) {
            return getTripleNode(dt, create, add);
        }
        else if (dt.isBlank()) {
            return getBlankNode(dt, create, add);
        } else {
            return getResourceNode(dt, create, add);
        }
    }

    // May return Node with same value but different label
    public Node getExtNode(Node node) {
        IDatatype dt = getDatatypeValue(node);
        if (isSameIndexAble(dt)) {
            return getExtLiteralNode(dt);
        } else {
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
    
    public Node getBlankNode1(IDatatype dt, boolean create, boolean add) {
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
    
    // bnode may be a named graph id
    public Node getBlankNode(IDatatype dt, boolean create, boolean add) {
        Node node = getBlankNodeBasic(dt.getLabel());
        if (node != null) {
            return node;
        }
        node = getBlankNodeGraph(dt.getLabel());
        if (node == null && create) {
            node = buildNode(dt);
        }
        if (add) {
            add(dt, node);
        }
        return node;
    }
    
    public Node getTripleNode(IDatatype dt, boolean create, boolean add) {
        Node node = getTripleNode(dt.getLabel());
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
                || blank.containsKey(node.getLabel())
                || triple.containsKey(node.getLabel());
    }

    // resource node
    public Node getNode(String name) {
        return getNode(getID(name), name);
    }

    Node getNode(String key, String name) {
        return individual.get(key);
    }

    void addNode(IDatatype dt, Node node) {
        individual.put(getID(node), node);
    }

    public Node getBlankNode1(String name) {
        return getBlankNodeBasic(name);
    }
    
    // bnode subject/object or named graph id
    public Node getBlankNode(String name) {
        Node node = getBlankNodeBasic(name);
        if (node == null) {
            node = getBlankNodeGraph(name);
        }
        return node;
    }
    
    public Node getBlankNodeBasic(String name) {
        return blank.get(name);
    }
    
    // named graph id may be a bnode
    public Node getBlankNodeGraph(String name) {
        return graph.get(name);
    }
    
    public Node getTripleNode(String name) {
        return triple.get(name);
    }

    void addBlankNode(IDatatype dt, Node node) {
        blank.put(node.getLabel(), node);
    }
    
    void addTripleNode(IDatatype dt, Node node) {
        triple.put(node.getLabel(), node);
    }
    
    public void removeTripleNode(Node node) {
        triple.remove(node.getLabel());
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

    Node basicAddGraph(Node node) {
        return basicAddGraph(node.getLabel(), node.isBlank());
    }
    
    Node basicAddGraph(String label) {
        return basicAddGraph(label, false);
    }
    
    Node basicAddGraph1(String label, boolean bnode) {
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
        graph.put(key, node);
        return node;
    }
    
    Node basicAddGraph(String label, boolean bnode) {
        String key = getID(label);
        Node node = getGraphNode(key, label);
        if (node != null) {
            return node;
        }
        if (bnode || isBlank(label)) {
            node = getBlankNodeBasic(label);
            if (node == null) {
                IDatatype dt = DatatypeMap.createBlank(label);
                node = createNode(key, dt);
                indexNode(dt, node);
            }
        } else {
            node = getResource(key, label);
            if (node == null) {
                IDatatype dt = DatatypeMap.createResource(label);
                node = createNode(key, dt);
                indexNode(dt, node);
            }
        }
        graph.put(key, node);
        return node;
    }
    
    boolean isBlank(String label) {
        return label.startsWith(BLANK);
    }

    Node basicAddGraphNode(Node node) {
        graph.put(node.getLabel(), node);
        return node;
    }

    Node basicAddResource(String label) {
        Node node = getResource(label);
        if (node != null) {
            add(node.getDatatypeValue(), node);
            return node;
        }
        IDatatype dt = DatatypeMap.createResource(label);
        String key = getID(label);
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

    Node basicAddBlank1(String label) {
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
    
    // add bnode as subject/object
    // bnode may already exist as subject/object or as named graph id
    Node basicAddBlank(String label) {
        Node node = getBlankNodeBasic(label);
        if (node != null) {
            return node;
        }
        else {
            node = getBlankNodeGraph(label);
        }
        if (node == null) {
            IDatatype dt = DatatypeMap.createBlank(label);
            if (dt != null) {
                node = buildNode(dt);
                indexNode(dt, node);
                addBlankNode(dt, node);
            }
        }
        else {
            // node is named graph id but not a graph node (subject/object)
            // register node as graph node
            addBlankNode(node.getDatatypeValue(), node);  
        }
        
        return node;
    }
    
    Node basicAddTripleReference(String label) {
        Node node = getTripleNode(label);
        if (node == null) {
            IDatatype dt = createTripleReference(label);
            node = buildNode(dt);
            indexNode(dt, node);
            addTripleNode(dt, node);
        }
        return node;
    }
    
     Node basicAddTripleReference(Node s, Node p, Node o) {
        String label = reference(s, p, o);
        Node node = getTripleNode(label);
        if (node == null) {
            IDatatype dt = createTripleReference(label);
            node = new TripleNode(s, p, o);
            node.setDatatypeValue(dt);
            dt.setEdge(node.getEdge());
            indexNode(dt, node);
            addTripleNode(dt, node);
        }
        return node;
    }
    


    public void add(Node node) {
        IDatatype dt = getDatatypeValue(node);
        add(dt, node);
    }

    public void add(Node node, int n) {
        if (isMetadata() && n > 1) {
            return;
        }
        add(node);
    }

    void add(IDatatype dt, Node node) {
        if (dt.isLiteral()) {
            addLiteralNode(dt, node);
        } 
        else if (dt.isTriple()) {
            addTripleNode(dt, node);
            indexNode(dt, node);
        }
        else if (dt.isBlank()) {
            addBlankNode(dt, node);
            indexNode(dt, node);
        } else {
            addNode(dt, node);
            indexNode(dt, node);
        }
    }

    public void addLiteralNode(IDatatype dt, Node node) {
        if (valueOut) {
            vliteral.put(node.getKey(), node);
            indexNode(dt, node);
        } else {
            getLiteralNodeManager().put(dt, node);
            indexLiteralNode(dt, node);
        }
    }

    /**
     * 01 and 1 have same index true and '1'^^xsd:boolean have same index date
     * with Z and date with +00:00 have same value but different label hence
     * they have different Node with same index
     */
    boolean isSameIndexAble(IDatatype dt) {
        return dt.isNumber() || dt.isBoolean() || dt.isDate();
    }

    /**
     * Assign an index to Literal Node Assign same index to same number values:
     * same datatype with same value and different label have same index 1, 01,
     * 1.0 have same index: they join with SPARQL; 1, 1 double have different
     * index, they do not join
     */
    void indexLiteralNode(IDatatype dt, Node node) {
        if (isSameIndexAble(dt)) {
            Node n = getLiteralIndexManager().get(dt);
            if (n == null) {
                getLiteralIndexManager().put(dt, node);
                indexNode(dt, node);
            } else if (node.getIndex() == -1) {
                // assign same index as existing same value
                node.setIndex(n.getIndex());
            }
        } else {
            indexNode(dt, node);
        }
    }

    public Node getLiteralNode(IDatatype dt) {
        return getLiteralNode(getKey(dt), dt);
    }

    // return same datatype value with possibly different label (e.g. 10 vs 1e1)
    public Node getExtLiteralNode(IDatatype dt) {
        return getLiteralIndexManager().get(dt);
    }

    public Node getLiteralNode(String key, IDatatype dt) {
        if (valueOut) {
            return vliteral.get(key);
        } else {
            return getLiteralNodeManager().get(dt);
        }
    }

    public Node getGraphNode(String label) {
        return getGraphNode(getID(label), label);
    }

    public Node getGraphNode(Node node) {
        return getGraphNode(node.getLabel());
    }

    /**
     * Include external named graph node
     *
     */
    public Node getGraphNodeWithExternal(Node node) {
        Node n = getGraphNode(node);
        if (n == null
                && getNamedGraph(node.getLabel()) != null) {
            return node;
        }
        return n;
    }

    Node getGraphNode(String key, String label) {
        return graph.get(key);
    }

    public void addGraphNode(Node gNode) {
        if (!containsCoreseNode(gNode)) {
            //graph.put(gNode.getLabel(), gNode);
            graph.put(getID(gNode), gNode);
            indexNode( gNode.getValue(), gNode);
        }
    }

    public boolean containsCoreseNode(Node node) {
        //return graph.containsKey(node.getLabel());
        return graph.containsKey(getID(node));
    }

    public Node getPropertyNode(String label) {
        return property.get(label);
    }

    @Override
    public Node getPropertyNode(Node p) {
        return property.get(p.getLabel());
    }

    public void addPropertyNode(Node pNode) {
        if (!property.containsKey(pNode.getLabel())) {
            property.put(pNode.getLabel(), pNode);
            indexNode( pNode.getValue(), pNode);
        }
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public DataProducer getDefault() {
        return getDataStore().getDefault();
    }

    public DataProducer getNamed() {
        return getDataStore().getNamed();
    }

    public Iterable<Edge> getEdges() {
        Iterable<Edge> ie = getSubjectIndex().getEdges();
        if (ie == null) {
            return new ArrayList<>();
        }
        return ie;
    }

    @Override
    public Edge getEdge(Node pred, Node node, int index) {
        Iterable<Edge> it = getEdges(pred, node, index);
        if (it == null) {
            return null;
        }
        for (Edge ent : it) {
            return ent;
        }
        return null;
    }
    
    // DataManager api
    
    public Iterable<Edge> iterate(Node s, Node p, Node o, List<Node> from) {
        DataProducer dp = new DataProducer(this);
        if (from != null && !from.isEmpty()) {
            dp.fromSelect(from);
        }
        return dp.iterate(s, p, o);
    }
    
    public Iterable<Edge> insert(Node s, Node p, Node o, List<Node> contexts) {
        if (contexts==null||contexts.isEmpty()) {
            Edge edge = insert(s, p, o);
        }
        else {
            for (Node g : contexts) {
                Edge edge = insert(g, s, p, o);
            }
        }
        return emptyEdgeList;
    }
    
    public Iterable<Edge> delete(Node s, Node p, Node o, List<Node> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            List<Edge> edge = delete(s, p, o);
        } else {
            for (Node g : contexts) {
                List<Edge> edge = delete(g, s, p, o);
            }
        }
        return emptyEdgeList;
    }
    

    @Override
    public Node value(Node subj, Node pred, int n) {
        Node ns = getNode(subj);
        Node np = getPropertyNode(pred);
        if (ns == null || np == null) {
            return null;
        }
        Edge edge = getEdge(np, ns, 0);
        if (edge == null) {
            return null;
        }
        return edge.getNode(n);
    }

    public Edge getEdge(String name, Node node, int index) {
        Node pred = getPropertyNode(name);
        if (pred == null) {
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

    public IDatatype getValue(String name, Node node) {
        Node value = getNode(name, node);
        if (value == null) {
            return null;
        }
        return  value.getValue();
    }

    public Node getNode(String name, Node node) {
        Edge edge = getEdge(name, node, 0);
        if (edge == null) {
            return null;
        }
        return edge.getNode(1);
    }

    public Iterable<Node> getNodes(Node pred, Node node, int n) {
        Iterable<Edge> it = getEdges(pred, node, n);
        if (it == null) {
            return new ArrayList<>();
        }
        int index = (n == 0) ? 1 : 0;
        return NodeIterator.create(it, index);
    }
    
    public void trace(String format, Object... obj) {
        if (isDebugSparql()) {
            logger.info(String.format(format, obj));
        }
    }

    public Iterable<Edge> properGetEdges(Node predicate, Node node, Node node2, int n) {
        trace("Edge iterator for: p=%s n=%s n2=%s", predicate, node, node2);
        Iterable<Edge> it = getEdges(predicate, node, node2, n);
        if (it == null) {
            trace("Edge iterator fail for: p=%s n=%s",predicate, node);
            return EMPTY;
        }
        return it;
    }

    public Iterable<Edge> getEdges(Node predicate, Node node, int n) {
        return getEdges(predicate, node, null, n);
    }

    public Iterable<Edge> getEdges(Node predicate, Node node, Node node2, int n) {
        if (isTopRelation(predicate)) {
            return getEdges(node, n);
        } else {
            return basicEdges(predicate, node, node2, n);
        }
    }

    public Iterable<Edge> basicEdges(Node predicate, Node node, Node node2, int n) {
        return getIndex(n).getEdges(predicate, node, node2);
    }

    /**
     * with rdfs:subPropertyOf
     */
    public Iterable<Edge> getAllEdges(Node predicate, Node node, Node node2, int n) {
        MetaIterator<Edge> meta = new MetaIterator<>();

        for (Node pred : getProperties(predicate)) {
            Iterable<Edge> it = getIndex(n).getEdges(pred, node);
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<Edge>();
        }
        return meta;
    }

    public Iterable<Node> getProperties(Node p) {
        ArrayList<Node> list = new ArrayList<>();
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
        List<Node> list = new ArrayList<>();
        for (Edge ent : getEdges(RDF.FIRST)) {
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
        for (Node node : getBlankNodes()) {
            if (!hasEdge(node, 1)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasEdge(Node node, int i) {
        Iterable<Edge> it = getEdges(node, i);
        return it.iterator().hasNext();
    }

    public List<Node> getList(Node node) {
        List<Node> list = new ArrayList<Node>();
        list(node, list);
        return list;
    }

    public List<IDatatype> getDatatypeList(IDatatype dt) {
        Node node = getNode(dt);
        if (node == null) {
            return null;
        }
        return getDatatypeList(node);
    }

    public List<IDatatype> getDatatypeList(Node node) {
        List<Node> list = getList(node);
        ArrayList<IDatatype> ldt = new ArrayList<>();
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
    public IDatatype list(Node node) {
        ArrayList<IDatatype> list = reclist(node);
        if (list == null) {
            return null;
        }
        return DatatypeMap.createList(list);
    }

    public ArrayList<IDatatype> reclist(Node node) {
        if (node.getLabel().equals(RDF.NIL)) {
            return new ArrayList<>();
        } else {
            Edge first = getEdge(RDF.FIRST, node, 0);
            Edge rest = getEdge(RDF.REST, node, 0);
            if (first == null || rest == null) {
                return null;
            }
            ArrayList<IDatatype> list = reclist(rest.getNode(1));
            if (list == null) {
                return null;
            }
            Node val = first.getNode(1);

            if (val.isBlank() && ! val.isTriple()) {
                // may be a list
                ArrayList<IDatatype> ll = reclist(val);
                if (ll == null) {
                    // not a list
                    list.add(0, value(val));
                } else {
                    // list
                    list.add(0, DatatypeMap.createList(ll));
                }
            } else {
                list.add(0, value(val));
            }
            return list;
        }
    }

    IDatatype value(Node n) {
        return  n.getValue();
    }

    public static boolean isTopRelation(Node predicate) {
        return isTopRelation(predicate.getLabel());
    }

    static boolean isTopRelation(String predicate) {
        return predicate.equals(TOPREL);
    }

    // without duplicates 
    public Iterable<Edge> getNodeEdges(Node node) {
        return getDataStore().getDefault().iterate(node, 0);
    }

    public Iterable<Edge> getNodeEdges(Node gNode, Node node) {
        return getDataStore().getNamed().from(gNode).iterate(node, 0);
    }

    public List<EdgeManagerIndexer> getIndexList() {
        return tables;
    }

    public EdgeManagerIndexer getIndex(int n) {
        switch (n) {
            case IGRAPH:
                return getNamedGraphIndex();
            case ILIST:
                return ruleEdgeIndex;
        }
//        if (n + 1 >= tables.size()) {
//            //setIndex(n, new EdgeIndex(this, n));	
//        }
        return getIndexList().get(n);
    }

    void setIndex(int n, EdgeManagerIndexer e) {
        getIndexList().add(n, e);
    }

    public Iterable<Edge> getEdges(Node node, int n) {
        if (node == null) {
            // without NodeManager
            return getSortedEdgesBasic(node, n);
        } else {
            // with NodeManager
            return getIndex(n).getSortedEdges(node);
        }
    }

    // without NodeManager
    public Iterable<Edge> getSortedEdgesBasic(Node node, int n) {
        MetaIterator<Edge> meta = new MetaIterator<Edge>();

        for (Node pred : getSortedProperties()) {
            Iterable<Edge> it = getIndex(n).getEdges(pred, node);
            if (it != null) {
                meta.next(it);
            }
        }
        if (meta.isEmpty()) {
            return new ArrayList<Edge>();
        }
        return meta;
    }

    public Iterable<Edge> getEdges(String p) {
        Node predicate = getPropertyNode(p);
        if (predicate == null) {
            if (isTopRelation(p)) {
                return getEdges();
            }
            return EMPTY;
        }
        return getEdges(predicate);
    }

    public Edge getEdge(String p) {
        Iterator<Edge> it = getEdges(p).iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public Iterable<Edge> getEdges(String p, Node n, int i) {
        Node predicate = getPropertyNode(p);
        if (predicate == null) {
            return EMPTY;
        }
        Iterable<Edge> it = getEdges(predicate, n, i);
        if (it == null) {
            return EMPTY;
        }
        return it;
    }

    public Iterable<Edge> getEdges(IDatatype s, IDatatype p, IDatatype o) {
        Node ns = null, np, no = null;
        if (p == null) {
            np = getTopProperty();
        } else {
            np = getPropertyNode(p);
            if (np == null) {
                return EMPTY;
            }
        }
        if (s != null) {
            ns = getNode(s);
        }
        if (o != null) {
            no = getNode(o);
        }
        if (s == null && o != null) {
            return getEdges(np, no, null, 1);
        }
        Iterable<Edge> it = getEdges(np, ns, no, 0);
        if (it == null) {
            return EMPTY;
        }
        return it;
    }

    public Iterable<Edge> getEdges(Node predicate) {
        Iterable<Edge> it = getEdges(predicate, null, 0);
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
        return getSubjectIndex().size(pred);
    }

    public Node getGraphNode() {
        for (Node node : getGraphNodes()) {
            if (!node.getLabel().startsWith(NSManager.KGRAM)) {
                return node;
            }
        }
        return null;
    }

    public Iterable<Node> getGraphNodes() {
        return graph.values();
    }

    /**
     * from is empty: return defined named graph list from is not empty: return
     * subset of defined named graph member of from
     *
     * When from is empty: iterate standard named graph nodes and when
     * isAllGraphNode() = true include external named graph nodes
     *
     * When from is not empty: include external graph node that are in from
     * because they are explicitly required in the query use case: sparql micro
     * service need external graph node
     */
    public Iterable<Node> getGraphNodes(List<Node> from) {
        if (from.size() > 0) {
            return getTheGraphNodes(from);
        }
        return getTheGraphNodes();
    }

    public Iterable<Node> getTheGraphNodes() {
        return isAllGraphNode() ? getGraphNodesAll() : getGraphNodes();
    }

    Iterable<Node> getTheGraphNodes(List<Node> from) {
        List<Node> list = new ArrayList<>();
        for (Node nn : from) {
            Node target = getGraphNodeWithExternal(nn);
            if (target != null) {
                list.add(target);
            }
        }
        return list;
    }

    public List<Node> getGraphNodesExtern() {
        return new ArrayList<>(0);
    }

    public Iterable<Node> getGraphNodesAll() {
        ArrayList<Node> list = new ArrayList<>();
        for (Node node : getGraphNodes()) {
            list.add(node);
        }
        for (Node node : getGraphNodesExtern()) {
            list.add(node);
        }
        return list;
    }

    public int nbGraphNodes() {
        return graph.size();
    }

    public Iterable<Node> getNodes() {
        return individual.values();
    }

    public Iterable<Node> getBlankNodes() {
        return blank.values();
    }
    
    public Iterable<Node> getTripleNodes() {
        return triple.values();
    }
    
    public Hashtable<String, Node> getTripleNodeMap() {
        return triple;
    }

    /**
     * resource & blank TODO: a node may have been deleted (by a delete triple)
     * but still be in the table
     */
    public Iterable<Node> getRBNodes() {
        MetaIterator<Node> meta = new MetaIterator<>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        return meta;
    }
    
    public Iterable<Node> getSubjectNodes() {
        MetaIterator<Node> meta = new MetaIterator<>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        meta.next(getTripleNodes());
        return meta;
    }

    public Iterable<Node> getLiteralNodes() {
        if (valueOut) {
            return vliteral.values();
        }
        return getLiteralNodeManager().values();
    }

    /**
     * return graph vertex: subject/object of asserted edges
     * return iterable of NodeGraph(node, graph) 
     * MUST perform n.getNode() to get the node
     * compute graph nodes (only if it has not been already computed)
     * 
     */
    public Iterable<Node> getAllNodeIterator() {
        return getNodeGraphIterator();
    }

    public Iterable<Node> getAllNodeIterator2() {
        if (getEventManager().isDeletion()) {
            // recompute existing nodes (only if it has not been already recomputed)
            // iterable NodeGraph(node, graph)
            return getNodeGraphIterator();
        } else {
            // get nodes from basic node tables
            return getNodeIterator();
        }
    }

    /**
     * Iterate nodes from basic graph node tables
     * not exactly graph vertex with rdf star
     * @note: 
     * consider nodes from nested triple
     * although they are just quoted 
     */
    public Iterable<Node> getNodeIterator() {
        MetaIterator<Node> meta = new MetaIterator<>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        meta.next(getLiteralNodes());
        meta.next(getTripleNodes());
        return meta;
    }

    /**
     * return iterable of NodeGraph(node, graph) 
     * MUST perform n.getNode() to get the node
     *
     */
    public Iterable<Node> getNodeGraphIterator() {
        indexNode();
        return getNodeGraphIndex().getDistinctNodes();
    }

    // return iterable of NodeGraph(node, graph)
    // MUST perform n.getNode() to get the node
    public Iterable<Node> getNodeGraphIterator(Node gNode) {
        if (gNode == null) {
            return getNodeGraphIterator();
        }
        indexNode();
        return getNodeGraphIndex().getNodes(gNode);
    }

    public boolean contains(Node graph, Node node) {
        indexNode();
        return getNodeGraphIndex().contains(graph, node);
    }
    
    public NodeGraphIndex getNodeGraphIndex() {
        return nodeGraphIndex;
    }

    /**
     * May infer datatype from property range
     */
    public Node addLiteral(String pred, String label, String datatype, String lang) {
        IDatatype dt = createLiteral(pred, label, datatype, lang);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }
    
    /**
     * May infer datatype from property range
     */
    public IDatatype createLiteral(String pred, String label, String datatype, String lang) {
        String range = null;
        if (lang == null
                && getEntailment() != null && getEntailment().isDatatypeInference()) {
            range = getEntailment().getRange(pred);
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

        return  DatatypeMap.createLiteral(label, datatype, lang);
    }

    public String newBlankID() {
        if (isSkolem) {
            return skolem(blankID());
        } else {
            return blankID();
        }
    }
    
    public String newTripleReferenceID() {
        return TRIPLE_REF + triplerefid++;
    }

    synchronized String blankID() {
        return BLANK + blankid++;
    }

    public String skolem(String id) {
        String str = values.getKey(key + id);
        return SKOLEM + str;
    }

    public IDatatype skolem(IDatatype dt) {
        if (dt.isTriple()) {
            return dt;
        }
        if (!dt.isBlank()) {
            return dt;
        }
        String id = skolem(dt.getLabel());
        return createSkolem(id);
    }

    public Node skolem(Node node) {
        if (node.isTriple()) {
            return node;
        }
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

    public IStorage getStorageMgr() {
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
        prepare();
        g2.prepare();
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

        for (Edge ent : getEdges()) {
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

    public List<Edge> getEdgeList(Node n) {
        ArrayList<Edge> list = new ArrayList<Edge>();
        for (Edge e : getEdges(n, 0)) {
            list.add(e);
        }
        return list;
    }

    /**
     *
     * Without rule entailment
     */
    public List<Edge> getEdgeListSimple(Node n) {
        ArrayList<Edge> list = new ArrayList<Edge>();
        for (Edge e : getEdges(n, 0)) {
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
    public Edge insert(Edge ent) {
        return addEdge(ent);
    }

    public List<Edge> delete(Edge edge) {
        List<Edge> res = null;

        if (edge.getGraph() == null) {
            res = deleteAll(edge);
        } else {
            Edge ee = basicDelete(edge);
            if (ee != null) {
                res = new ArrayList<>();
                res.add(ee);
                getEventManager().process(Event.Delete, ee, edge);
            }
        }

        if (res != null) {
            deleted(res);
        }
        return res;
    }

    public List<Edge> delete(Edge edge, List<Constant> from) {
        List<Edge> res = null;

        for (Constant str : from) {
            Node node = getGraphNode(str.getLabel());

            if (node != null) {
                fac.setGraph(edge, node);
                Edge ent = basicDelete(edge);
                if (ent != null) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }
                    res.add(ent);
                    getEventManager().process(Event.Delete, ent, edge);
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
    Edge basicDelete(Edge edge) {
        Edge res = null;

        for (EdgeManagerIndexer ie : getIndexList()) {
            Edge ent = ie.delete(edge);
            if (isDebug) {
                logger.debug("delete: " + ie.getIndex() + " " + edge);
                logger.debug("delete: " + ie.getIndex() + " " + ent);
            }
            if (ent != null) {
                res = ent;
            }
        }
        return res;
    }

    /**
     * delete occurrences of this edge in all graphs
     */
    List<Edge> deleteAll(Edge edge) {
        ArrayList<Edge> res = null;
        Node graphName = null;
        for (Node graph : getGraphNodes()) {
            //fac.setGraph(edge, graph);
            edge.setGraph(graph);
            Edge ent = basicDelete(edge);
            if (ent != null) {
                if (res == null) {
                    res = new ArrayList<>();
                }
                res.add(ent);
                graphName = ent.getGraph();
                //setDelete(true);
                getEventManager().process(Event.Delete, ent, edge);
            }
        }
        if (graphName != null) {
            edge.setGraph(graphName);
        }
        return res;
    }

    /**
     * This edge has been deleted TODO: Delete its nodes from tables if needed
     */
    void deleted(List<Edge> list) {
        for (Edge edge : list) {
            for (int i = 0; i < edge.nbNode(); i++) {
                delete(edge.getNode(i));
            }
        }
    }

    void delete(Node node) {
    }

    // clear all except graph names.
    // they must be cleared explicitely
    public void clear() {
        clearNodeIndex();
        clearNodes();
        for (EdgeManagerIndexer t : getIndexList()) {
            t.clear();
        }
        manager.onClear();
        clearDistance();

        setIndexable(true);
        getEventManager().initStatus();

        size = 0;
        if (storageMgr != null) {
            storageMgr.clean();
        }
    }

    void clearNodes() {
        individual.clear();
        blank.clear();
        triple.clear();
        getLiteralNodeManager().clear();
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

        if (this.graph.isEmpty()) {
            return false;
        }

        this.graph.clear();
        return true;
    }

    public boolean clear(String uri, boolean isSilent) {
        return this.clear(uri);
    }

    public boolean clear(String graph_name) {
        if (graph_name != null) {
            Node gg = getGraphNode(graph_name);
            if (gg != null) {
                getEventManager().process(Event.Delete, gg);
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
        getEventManager().process(Event.Insert);
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
     * Add a copy of edge Use case: edge comes from another graph,
     * create a local copy of nodes
     */
    public Edge copy(Edge edge) {
        Node g = basicAddGraph(edge.getGraph());
        Node p = basicAddProperty(edge.getEdgeNode().getLabel());

        ArrayList<Node> list = new ArrayList<>();

        // nbNodeIndex() exclude metadata TripleNode as getNode(2)
        for (int i = 0; i < edge.nbNodeIndex(); i++) {
            Node n = addNode(edge.getNode(i).getValue());
            list.add(n);
        }
        Edge e = addEdge(g, p, list);
        return e;
    }

    /**
     * TODO: setUpdate(true)
     */
    public Edge copy(Node gNode, Node pred, Edge ent) {
        Edge e = fac.copy(gNode, pred, ent);
        Edge res = add(e);
        return res;
    }

    public void copy(Graph g) {
        copyNode(g);
        for (Edge ent : g.getEdges()) {
            copy(ent);
        }
    }

    void copyNode(Graph g) {
    }

    public Graph copy() {
        Graph g = empty();
        g.copy(this);
        return g;
    }

    public Graph empty() {
        Graph g = create();
        g.inherit(this);
        return g;
    }

    public Graph construct() {
        Graph g = create();
        g.setEdgeMetadata(isEdgeMetadata());
        return g;
    }

    void inherit(Graph g) {
        setSkolem(g.isSkolem());
    }

    public Graph union(Graph g) {
        Graph gu = Graph.create();
        gu.copy(this);
        gu.copy(g);
        gu.init();
        return gu;
    }

    public Graph merge(Graph g) {
        copy(g);
        init();
        return this;
    }

    void copyEdge(Edge ent) {
    }

    /**
     * Add edge and add it's nodes
     * Node MUST be graph node
     */
    public Edge add(Node source, Node subject, Node predicate, Node value) {
        Edge e = fac.create(source, subject, predicate, value);
        Edge ee = addEdgeWithNode(e);
        return ee;
    }
    
    // Node MAY not be graph node
    // It will be replaced by graph node
    public Edge insert(Node s, Node p, Node o) {
        return insert(null, s, p, o);
    }
    
    // Node MAY not be graph node
    // It will be replaced by graph node
    public Edge insert(Node g, Node s, Node p, Node o) {
        if (g == null) {
            g = addDefaultGraphNode();
        }
        Edge e = fac.create(g, s, p, o);
        return insertEdgeWithTargetNode(e);
    }

    /**
     * Add edge and add it's nodes
     * xt:insert
     */
    public Edge insert(IDatatype subject, IDatatype predicate, IDatatype value) {
        return insert(null, subject, predicate, value);
    }

    public Edge insert(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        return insert(source==null?addDefaultGraphNode():createNode(source), 
                createNode(subject), createNode(predicate), createNode(value));        
    }
    
    public List<Edge> delete(Node s, Node p, Node o) {
        return delete(null, s.getDatatypeValue(), p.getDatatypeValue(), o.getDatatypeValue());
    }

    public List<Edge> delete(Node g, Node s, Node p, Node o) {       
        return delete(g==null?null:g.getDatatypeValue(), 
                s.getDatatypeValue(), p.getDatatypeValue(), o.getDatatypeValue());
    }

    public List<Edge> delete(IDatatype subject, IDatatype predicate, IDatatype value) {
        return delete(null, subject, predicate, value);
    }

    public List<Edge> delete(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        Edge e = createDelete(source, subject, predicate, value);
        List<Edge> list = delete(e);
        return list;
    }

    /**
     * Add Edge, not add nodes
     */
    public Edge addEdge(Node source, Node subject, Node predicate, Node value) {
        Edge e = fac.create(source, subject, predicate, value);
        Edge ee = addEdge(e);
        if (ee != null) {
            return ee;
        }
        return null;
    }

    public Edge addEdge(Node subject, Node predicate, Node value) {
        Node g = addDefaultGraphNode();
        return addEdge(g, subject, predicate, value);
    }

    // tuple
    public Edge addEdge(Node source, Node predicate, List<Node> list) {
        Edge e;
        if (list.size() == 2) {
            e = fac.create(source, list.get(0), predicate, list.get(1));
        } else {
            e = fac.create(source, predicate, list);
        }

        Edge ee = addEdge(e);
        if (ee != null) {
            return ee;
        }
        return null;
    }

    /**
     * Graph in itself is not considered as a graph node for SPARQL path unless
     * explicitely referenced as a subject or object Hence ?x :p* ?y does not
     * return named graph nodes
     */
    public Node addGraph(String label) {
        return basicAddGraph(label);
    }
    
    public Node addGraph(String label, boolean bnode) {
        return basicAddGraph(label, bnode);
    }

    public Node addDefaultGraphNode() {
        return basicAddGraphNode(defaultGraph);
    }

    public boolean isDefaultGraphNode(Node g) {
        return g == defaultGraph;
    }

    public boolean isDefaultGraphNode(String name) {
        return name.equals(Entailment.DEFAULT);
    }

    public Node getDefaultGraphNode() {
        return defaultGraph;
    }
    
    public IDatatype getDefaultGraphDatatypeValue() {
        return getDefaultGraphNode().getDatatypeValue();
    }

    public Node addRuleGraphNode() {
        return basicAddGraphNode(ruleGraph);
    }

    public Node getRuleGraphNode() {
        return ruleGraph;
    }

    public Node addConstraintGraphNode() {
        return basicAddGraphNode(constraintGraph);
    }

    public Node getConstraintGraphNode() {
        return constraintGraph;
    }
    
    public Graph getConstraintGraph() {
        return this;
    }

    public boolean isRuleGraphNode(Node node) {
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
    
    public Edge beforeInsert(Edge edge) {
        return edge;
    }
    
    public Node addTripleReference() {
        return addTripleReference(newTripleReferenceID());
    }
    
    public Node addTripleReference(String label) {
        return basicAddTripleReference(label);
    }
    
    public Node addTripleReference(Node s, Node p, Node o) {
        if (Property.booleanValue(Property.Value.RDF_STAR_TRIPLE)) {
            return basicAddTripleReference(s, p, o);
        }
        return basicAddTripleReference(reference(s, p, o));
    }
    
    public Node getTripleReference(Node s, Node p, Node o) {
        return getTripleNode(reference(s, p, o));
    }  
    
    public Node getTripleReference(Edge edge) {
        return getTripleNode(reference(edge.getSubjectNode(), edge.getPropertyNode(), edge.getObjectNode()));
    }  
    
    public IDatatype createTripleReference(Node s, Node p, Node o) {
        return createTripleReference(reference(s, p, o));
    }
    
    public IDatatype createTripleReference(Edge edge) {
        return createTripleReference(edge.getSubjectNode(), edge.getPropertyNode(), edge.getObjectNode());
    }
    
    /**
     * generate unique reference node ID for given s p o
     * pragma: nodes MUST have been inserted in the graph to have an index
     * _:ti.j.k where i, j, k are node index
     */
    public String reference(Node s, Node p, Node o) {
        return String.format("%s%s.%s.%s", TRIPLE_REF, reference(s), reference(p), reference(o));
    }
    
    
    
    public String reference(Node n) {
        IDatatype dt = n.getValue();
        if (dt.isURI()) {
            return Integer.toString(n.getIndex());
        }
        if (dt.isNumber()) {
            return referenceNumber(dt);
        }
        if (dt.isBoolean()) {
            // distinguish true from 1
            return String.format("b%s", dt.getLabel());
        }
        // dates may also have same index but different labels, see isDate() above
        // date with Z and date with +00:00 have same index (same value) but different labels
        // they should have different ID
        if (dt.isDate()) {
            if (DatatypeMap.getTZ(dt).equals("Z")) {
                return String.format("d%s", n.getIndex());
            }
        }
        return Integer.toString(n.getIndex());
    }
    
    // 1 and 1.0 may have same index -> consider value to differentiate them
    String referenceNumber(IDatatype dt) {
        return String.format("%s(%s)", shortDatatypeLabel(dt), dt.getLabel());
    }
    
    String shortDatatypeLabel(IDatatype dt) {
        return dt.getDatatype().getLabel().substring(NSManager.XSD_LENGTH, NSManager.XSD_LENGTH+3);
    }
    
    public IDatatype createTripleReference() {
        return createTripleReference(newTripleReferenceID());
    }
    
    IDatatype createTripleReference(String label) {
        return DatatypeMap.createTripleReference(label);
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

    public Node addTripleName() {
        return addBlank();
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
    }

    public void setDebugMode(boolean b) {
        setDebug(b);
        manager.setDebug(b);
        for (EdgeManagerIndexer id : getIndexList()) {
            id.getNodeManager().setDebug(b);
        }
        if (getEntailment() != null) {
            getEntailment().setDebug(b);
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

    public void tag(Edge ent) {
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

    boolean needTag(Edge ent) {
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
    public void logDelete(Edge ent) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.delete(this, ent);
            }
        }
    }

    public void logInsert(Edge ent) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.insert(this, ent);
            }
        }
    }

    public void declareUpdate(boolean b) {
        for (EdgeManagerIndexer ind : getIndexList()) {
            ind.declareUpdate(b);
        }
    }

    public void logStart(Query q) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.start(this, q);
            }
        }
    }

    public void logFinish(Query q) {
        logFinish(q, null);
    }

    public void logFinish(Query q, Mappings m) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.finish(this, q, m);
            }
        }
    }

    public void logLoad(String path) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.load(path);
            }
        }
    }

    public boolean onInsert(Edge ent) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
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
        return new QueryCheck(this).check(q);
    }

    // overloaded by GraphStore
    public void shareNamedGraph(Graph g) {
    }

    public Collection<String> getNames() {
        return new ArrayList<>(0);
    }

    public Graph getNamedGraph(String name) {
        return null;
    }

    public Graph setNamedGraph(String name, Graph g) {
        return this;
    }

    public Dataset getDataset() {
        Dataset ds = Dataset.create();
        for (Node node : getGraphNodes()) {
            ds.addFrom(node.getLabel());
            ds.addNamed(node.getLabel());
        }
        return ds;
    }

    public Graph getRuleGraph(boolean constraint) {
        return this;
    }

    public Node getRuleGraphName(boolean constraint) {
        return (constraint && CONSTRAINT_NAMED_GRAPH) ? addConstraintGraphNode() : addRuleGraphNode();
    }

    public boolean isMetadata() {
        return metadata;
    }

    public boolean isMetadataNode() {
        return isEdgeMetadata() || isMetadata();
    }
    
    public boolean isFormerMetadata() {
        return isMetadataNode() && ! Property.booleanValue(Property.Value.RDF_STAR_TRIPLE);
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    /**
     * @Draft For each triple pattern: Search if there exists graph name,
     * subject, property, object in the graph with similar URI
     * mode=message&param=sv:distance~n => levenshtein distance <= n
     */
    public JSONObject match(ASTQuery ast) {
        return match(ast, 1);
    }

    public JSONObject match(ASTQuery ast, int d) {
        return new GraphDistance(this).match(ast, d);
    }

    public JSONObject cardinality(ASTQuery ast) {
        return new GraphDistance(this).cardinality(ast);
    }

    public SortedMap<IDatatype, Node> getLiteralNodeManager() {
        return literalNodeManager;
    }

    public void setLiteralNodeManager(SortedMap<IDatatype, Node> literal) {
        this.literalNodeManager = literal;
    }

    public SortedMap<IDatatype, Node> getLiteralIndexManager() {
        return literalIndexManager;
    }

    public void setLiteralIndexManager(SortedMap<IDatatype, Node> sliteral) {
        this.literalIndexManager = sliteral;
    }

    public static void setDefaultVerbose(boolean b) {
        VERBOSE = b;
    }

    public static void setDefaultSkolem(boolean b) {
        SKOLEM_DEFAULT = b;
    }

    public boolean isAllGraphNode() {
        return allGraphNode;
    }

    public void setAllGraphNode(boolean allGraphNode) {
        this.allGraphNode = allGraphNode;
    }

    public EdgeManagerIndexer getSubjectIndex() {
        return subjectIndex;
    }

    void setSubjectIndex(EdgeManagerIndexer table) {
        this.subjectIndex = table;
    }

    EdgeManagerIndexer getNamedGraphIndex() {
        return namedGraphIndex;
    }

    void setNamedGraphIndex(EdgeManagerIndexer tgraph) {
        this.namedGraphIndex = tgraph;
    }

    public void setIndexList(ArrayList<EdgeManagerIndexer> tables) {
        this.tables = tables;
    }

    public boolean isDebugSparql() {
        return debugSparql;
    }

    public void setDebugSparql(boolean debugSparql) {
        this.debugSparql = debugSparql;
    }

    public List<GraphListener> getListenerList() {
        return listenerList;
    }

    public void setListenerList(List<GraphListener> listenerList) {
        this.listenerList = listenerList;
    }

}
