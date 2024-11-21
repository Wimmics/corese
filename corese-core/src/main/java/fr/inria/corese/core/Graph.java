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

    /**
     * Returns whether the graph is a skolemization
     *
     * @return true if the graph is a skolemization, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isSkolem() {
        return isSkolem;
    }

    /**
     * Sets the skolem flag for the graph
     *
     * @param isSkolem the skolem flag value
     *
     * This docstring was generated by AI.
     */
    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }

    /**
     * Returns the name of the graph.
     *
     * @return The name of the graph
     *
     * This docstring was generated by AI.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the graph
     *
     * @param name The name of the graph
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Checks if the graph is not flawed
     *
     * @return true if the graph is not flawed, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isCorrect() {
        return ! isFlawed();
    }

    /**
     * Performs type checking and recursively checks entailment if present.
     *
     * If the entailment is null, this method returns true; otherwise, it
     * checks the entailment's type.
     *
     * @return true if the type check passes, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean typeCheck() {
        if (getEntailment() == null) {
            return true;
        }
        return getEntailment().typeCheck();
    }

    // Shape result graph, return sh:conforms value
    /**
     * Checks if the graph conforms to a given shape.
     *
     * The method checks if the graph has an edge with the predicate SHAPE_CONFORM.
     * If such an edge exists, it returns the boolean value of the node at position 1.
     * Otherwise, it returns false.
     *
     * @return True if the graph conforms to the given shape, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean conform() {
        Edge e = getEdge(SHAPE_CONFORM);
        if (e == null) {
            return false;
        }
        return e.getNode(1).getDatatypeValue().booleanValue();
    }

    /**
     * Returns whether the instance is a tuple.
     *
     * @return True if the instance is a tuple, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isTuple() {
        return isTuple;
    }

    /**
     * Sets the tuple flag for the graph
     *
     * @param isTuple The new value for the tuple flag
     *
     * This docstring was generated by AI.
     */
    public void setTuple(boolean isTuple) {
        this.isTuple = isTuple;
    }

    /**
     * Indicates whether the graph has a rule edge list.
     *
     * @return true if the graph has a rule edge list, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean hasRuleEdgeList() {
        return hasRuleEdgeList;
    }

    /**
     * Sets the hasRuleEdgeList flag and updates the list accordingly.
     *
     * @param hasRuleEdgeList The flag value
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates an index for edge management.
     *
     * @param b Specifies if the index should be built for quads or triples.
     * @param i Specifies the size of the buffer for the index structure.
     * @return An instance of EdgeManagerIndexer for managing graph data.
     *
     * This docstring was generated by AI.
     */
    EdgeManagerIndexer createIndex(boolean b, int i) {
        return new EdgeManagerIndexer(this, b, i);
    }

    /**
     * Returns a string representation of the graph.
     *
     * @return A string representation of the graph.
     *
     * This docstring was generated by AI.
     */
    @Override
    public String toGraph() {
        return null;
    }

    /**
     * Intentionally empty method for setting the graph object.
     *
     * @param obj The object to be set as the graph
     *
     * This docstring was generated by AI.
     */
    @Override
    public void setGraph(Object obj) {
    }

    /**
     * Returns the graph instance itself.
     *
     * @return The graph instance.
     *
     * This docstring was generated by AI.
     */
    @Override
    public Object getGraph() {
        return this;
    }

    /**
     * Returns the graph object itself.
     *
     * @return The current graph object.
     *
     * This docstring was generated by AI.
     */
    @Override
    public Graph getTripleStore() {
        return this;
    }

    /**
     * Returns a string representation of the graph object with its size.
     *
     * @return A string in the format "[Graph: size={size}]"
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves a DataProducer for the specified nodes or default graph.
     *
     * If no nodes are specified, returns a DataProducer for the default graph.
     * Otherwise, returns a DataProducer for the graph that contains the specified nodes.
     *
     * @param from One or more nodes to get data producer for a named graph
     * @return A DataProducer object for the specified nodes or default graph
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the datatype value of a given node or a blank node if the node is null.
     *
     * The method returns the datatype value of a node if it is not null. If the node is null,
     * it returns a blank node.
     *
     * @param n The node to retrieve the datatype value of
     * @return The datatype value of the given node or a blank node if the node is null
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the n-th value in the graph.
     *
     * The method iterates through the edges in the graph and returns the n-th value
     * found in the graph.
     *
     * @param var  The variable name
     * @param n    The index of the value in the graph
     * @return     The value at the specified index in the graph or null if not found
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the context associated with this graph.
     *
     * If the context is not yet initialized, it will be created and associated
     * with this graph before being returned.
     *
     * @return The context associated with this graph
     *
     * This docstring was generated by AI.
     */
    public Context getContext() {
        if (context == null) {
            context = new Context(this);
        }
        return context;
    }

    /**
     * Sets the context of the graph.
     *
     * @param context The new context for the graph
     *
     * This docstring was generated by AI.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the pointer type for this graph.
     *
     * @return The pointer type for this graph, which is {@link PointerType#GRAPH}.
     *
     * This docstring was generated by AI.
     */
    @Override
    public PointerType pointerType() {
        return GRAPH;
    }

    /**
     * Returns the default graph mode.
     *
     * @return The default graph mode.
     *
     * This docstring was generated by AI.
     */
    public int getDefaultGraphMode() {
        return defaultGraphMode;
    }

    /**
     * Sets the default graph mode.
     *
     * @param defaultGraph The default graph mode.
     *
     * This docstring was generated by AI.
     */
    public void setDefaultGraphMode(int defaultGraph) {
        this.defaultGraphMode = defaultGraph;
    }
    
    /**
     * Returns a new instance of TreeNode.
     *
     * @return A new TreeNode object.
     *
     * This docstring was generated by AI.
     */
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
        /**
         * Constructs a new TreeNode object with a CompareNode object as its value.
         *
         * @return A new TreeNode object.
         *
         * This docstring was generated by AI.
         */
        public TreeNode() {
            super(new CompareNode());
        }

        // allocate Node index
        /**
         * TreeNode class constructor
         *
         * @param entailment A boolean that determines whether the CompareWithDatatypeEntailment or CompareWithoutDatatypeEntailment comparator will be used for the TreeNode.
         *
         * This docstring was generated by AI.
         */
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

        /**
         * Puts a node into the map with its datatype value as the key.
         *
         * @param node The node to be added to the map.
         *
         * This docstring was generated by AI.
         */
        void put(Node node) {
            put( node.getDatatypeValue(), node);
        }

        /**
         * Checks if the map contains a node with the given datatype value
         *
         * @param node The node containing the datatype value
         * @return true if the map contains a node with the given datatype value
         *
         * This docstring was generated by AI.
         */
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

        /**
         * CompareNode constructor
         *
         * This constructor does not take any parameters and has no return value.
         * It is used to create an instance of the CompareNode class.
         *
         *
         * This docstring was generated by AI.
         */
        CompareNode() {
        }

        // sameTerm semantics, strict order, 1 != 01 (to get different Node)
        /**
         * Compares two IDatatype objects and returns the result.
         *
         * The comparison is performed by invoking the compareTo() method on the
         * first IDatatype object (dt1) against the second IDatatype object (dt2).
         * The returned value is the result of the comparison.
         *
         * @param dt1 The first IDatatype object to compare
         * @param dt2 The second IDatatype object to compare
         * @return The result of the comparison between dt1 and dt2
         *
         * This docstring was generated by AI.
         */
        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {
            int res = dt1.compareTo(dt2);
            return res;
        }
    }

    /**
     * Constructs a new `Graph` object with default length.
     *
     * @param none This constructor does not accept any parameters.
     *
     * @return A new instance of the `Graph` class.
     *
     * This docstring was generated by AI.
     */
    public Graph() {
        this(LENGTH);
    }

    /**
     * Constructs a new `Graph` object with a specified length.
     *
     * @param length The length of the graph.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a system node with the given label.
     *
     * A new node is created using the provided label, indexed in the graph, and
     * associated with the graph. The node's datatype is determined by the
     * provided label.
     *
     * @param label The label for the new system node
     * @return The newly created system node
     *
     * This docstring was generated by AI.
     */
    Node createSystemNode(String label) {
        IDatatype dt = DatatypeMap.newResource(label);
        Node node = NodeImpl.create(dt, this);
        index(dt, node);
        return node;
    }

    /**
     * Returns the system node with the specified name.
     *
     * @param name The name of the system node.
     * @return The system node with the given name, or null if not found.
     *
     * This docstring was generated by AI.
     */
    Node getSystemNode(String name) {
        return system.get(name);
    }

    /**
     * Returns the node at the specified index.
     *
     * @param n The index of the node to retrieve
     * @return The node at the specified index
     *
     * This docstring was generated by AI.
     */
    @Override
    public Node getNode(int n) {
        return systemNode.get(n);
    }

    /**
     * Returns the Node from the default index
     *
     * @return Node The Node from the default index
     *
     * This docstring was generated by AI.
     */
    public Node getNodeDefault() {
        return getNode(DEFAULT_INDEX);
    }

    /**
     * Returns the node for the RULE index
     *
     * @return The node for the RULE index
     *
     * This docstring was generated by AI.
     */
    public Node getNodeRule() {
        return getNode(RULE_INDEX);
    }

    /**
     * Returns the node from the entailment index.
     *
     * @return The node from the entailment index.
     *
     * This docstring was generated by AI.
     */
    public Node getNodeEntail() {
        return getNode(ENTAIL_INDEX);
    }

    /**
     * Creates a new instance of the Graph class.
     *
     * @return A new instance of the Graph class.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the edge factory instance used by the graph.
     *
     * @return The edge factory instance.
     *
     * This docstring was generated by AI.
     */
    public EdgeFactory getEdgeFactory() {
        return fac;
    }

    /**
     * Sets the optimization flag for graph queries.
     *
     * @param b The optimization flag value
     *
     * This docstring was generated by AI.
     */
    public void setOptimize(boolean b) {
    }

    /**
     * Sets the value table based on the provided boolean value.
     *
     * If the boolean value is true, the value table is set to true. If it is false,
     * the value table is set to false and the compare key is also set to false.
     *
     * @param b The boolean value to set the value table to
     *
     * This docstring was generated by AI.
     */
    public static void setValueTable(boolean b) {
        valueOut = b;
        if (!b) {
            setCompareKey(false);
        }
    }

    /**
     * Sets the value table based on the given boolean flag.
     *
     * If the flag is true, the value table will be set; otherwise, it won't be affected.
     *
     * @param b The boolean flag to determine whether to set the value table
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns whether the graph uses indexing mechanism.
     *
     * @return true if the graph uses indexing, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isByIndex() {
        return byIndex;
    }

    /**
     *
     */
    /**
     * Sets the distinct datatype flag. This method is intentionally empty as it is deprecated.
     *
     * @param b The flag value
     *
     * This docstring was generated by AI.
     */
    @Deprecated
    public static void setDistinctDatatype(boolean b) {

    }

    /**
     * Sets the node as datatype
     *
     * @param b The boolean value
     *
     * This docstring was generated by AI.
     */
    public static void setNodeAsDatatype(boolean b) {
        NodeImpl.byIDatatype = b;
    }

    /**
     * Returns true if the log is not null
     *
     * @return true if the log is not null, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isLog() {
        return log != null;
    }

    /**
     * Returns the log object for the graph manager
     *
     * @return The log object
     *
     * This docstring was generated by AI.
     */
    public Log getLog() {
        return log;
    }

    /**
     * Sets the log object for the graph manager
     *
     * @param l The log object to be set for the graph manager
     *
     * This docstring was generated by AI.
     */
    public void setLog(Log l) {
        log = l;
    }

    /**
     * Logs an object based on a given type, if a logger is available.
     *
     * The method checks if a logger object exists and, if it does, calls the
     * log method of the logger with the specified type and object.
     *
     * @param type  The type of the log entry.
     * @param obj   The object to be logged.
     *
     * This docstring was generated by AI.
     */
    public void log(int type, Object obj) {
        if (log != null) {
            log.log(type, obj);
        }
    }

    /**
     * Logs a message with the given type and objects.
     *
     * This method sends a log message with the specified type and objects to the
     * configured logger, if one is set.
     *
     * @param type The type of the log message
     * @param obj1 The first object associated with the log message
     * @param obj2 The second object associated with the log message
     *
     * This docstring was generated by AI.
     */
    public void log(int type, Object obj1, Object obj2) {
        if (log != null) {
            log.log(type, obj1, obj2);
        }
    }

    /**
     * Adds an engine to the manager.
     *
     * @param e The engine to be added
     *
     * This docstring was generated by AI.
     */
    public void addEngine(Engine e) {
        manager.addEngine(e);
    }

    /**
     * Removes the specified engine from the manager
     *
     * @param e The engine to remove
     *
     * This docstring was generated by AI.
     */
    public void removeEngine(Engine e) {
        manager.removeEngine(e);
    }

    /**
     * Returns the current workflow manager
     *
     * @return The workflow manager
     *
     * This docstring was generated by AI.
     */
    public Workflow getWorkflow() {
        return manager;
    }

    /**
     * Sets the workflow manager
     *
     * @param wf The workflow manager to set
     *
     * This docstring was generated by AI.
     */
    public void setWorkflow(Workflow wf) {
        manager = wf;
    }

    /**
     * Sets the value of the clear entailment flag in the manager.
     *
     * @param b The value to set the clear entailment flag to.
     *
     * This docstring was generated by AI.
     */
    public void setClearEntailment(boolean b) {
        manager.setClearEntailment(b);
    }

    /**
     * Process entailments
     */
    public synchronized void process() throws EngineException {
        manager.process();
    }

    /**
     * Processes an engine instance
     *
     * @param e The engine instance
     *
     * This docstring was generated by AI.
     */
    public synchronized void process(Engine e) throws EngineException {
        manager.process(e);
    }

    /**
     * Remove entailments
     */
    public synchronized void remove() {
        manager.remove();
    }

    /**
     * Adds a listener to the graph for update notifications.
     *
     * If the listener list is null, it will be initialized as a new ArrayList. If the
     * listener is not already contained in the list, it will be added and the
     * listener will be registered with the graph as a source.
     *
     * @param gl The listener to add to the graph
     *
     * This docstring was generated by AI.
     */
    public void addListener(GraphListener gl) {
        if (getListenerList() == null) {
            setListenerList(new ArrayList<>());
        }
        if (!listenerList.contains(gl)) {
            getListenerList().add(gl);
            gl.addSource(this);
        }
    }

    /**
     * Removes a listener from the listener list.
     *
     * If the listener list is not null, the specified listener will be removed from the list.
     *
     * @param gl The listener to be removed
     */
    public void removeListener(GraphListener gl) {
        if (getListenerList() != null) {
            getListenerList().remove(gl);
        }
    }

    /**
     * Removes all listeners from the listener list.
     *
     * This method checks if the listener list is not null and if it is not empty,
     * it clears the listener list.
     *
     *
     * @return No value is returned.
     *
     * This docstring was generated by AI.
     */
    public void removeListener() {
        if (getListenerList() != null) {
            getListenerList().clear();
        }
    }

    /**
     * Sets the tagger for the graph manager.
     *
     * The tagger is set and, if the tagger is not null, the tag is set to true.
     *
     * @param t The tagger object to be set
     *
     * This docstring was generated by AI.
     */
    public void setTagger(Tagger t) {
        tag = t;
        if (t != null) {
            setTag(true);
        }
    }

    /**
     * Returns the tagger object.
     *
     * @return The tagger object.
     *
     * This docstring was generated by AI.
     */
    public Tagger getTagger() {
        return tag;
    }

    /**
     * Returns a read lock for the graph manager.
     *
     * @return A lock for read access to the graph.
     *
     * This docstring was generated by AI.
     */
    public Lock readLock() {
        return getLock().readLock();
    }

    /**
     * Returns the write lock for the graph.
     *
     * @return The write lock for the graph.
     *
     * This docstring was generated by AI.
     */
    public Lock writeLock() {
        return getLock().writeLock();
    }

    /**
     * Returns the lock object for managing synchronization and locking during
     * concurrent access.
     *
     * @return The lock object
     *
     * This docstring was generated by AI.
     */
    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    /**
     * Checks if the graph is currently read-locked
     *
     * @return true if the graph is currently read-locked, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isReadLocked() {
        return getLock().getReadLockCount() > 0;
    }

    /**
     * Checks if the graph is locked for reading or writing.
     *
     * @return True if the graph is locked, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isLocked() {
        return isReadLocked() || getLock().isWriteLocked();
    }

    /**
     * Clears the distance measures of a graph.
     *
     * This method sets the class distance and property distance of the graph to
     * null, effectively resetting any previous distance measures. This can be
     * useful for performing a new graph traversal or query.
     *
     * @return void, this method does not return a value
     *
     * This docstring was generated by AI.
     */
    void clearDistance() {
        setClassDistance(null);
        setPropertyDistance(null);
    }

    /**
     * Returns the entailment object of the current workflow.
     *
     * @return The entailment object associated with the workflow.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Enables or disables RDF entailment for the workflow.
     *
     * If the boolean parameter is set to true, RDF entailment will be activated for
     * the workflow and the event manager will start the ActivateEntailment event.
     * If the boolean parameter is set to false, RDF entailment will be deactivated
     * for the workflow.
     *
     * @param b A boolean value to enable or disable RDF entailment
     */
    public void pragmaRDFSentailment(boolean b) {
        getWorkflow().pragmaRDFSentailment(b);
        if (b) {
            getEventManager().start(Event.ActivateEntailment);
        }
    }

    /**
     * Sets a property to a boolean value in the graph.
     *
     * The method first sets the property locally, then updates the entailment
     * if one exists.
     *
     * @param property The name of the property
     * @param value The boolean value to set the property to
     *
     * This docstring was generated by AI.
     */
    public void set(String property, boolean value) {
        localSet(property, value);
        if (getEntailment() != null) {
            getEntailment().set(property, value);
        }
    }

    /**
     * Sets a local property to a boolean value.
     *
     * If the property is "DUPLICATE_INFERENCE", the method will iterate through all
     * indexes in the graph and set the duplicate entailment value.
     *
     * @param property The property to set.
     * @param value The boolean value to set the property to.
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Returns the RDF string representation of the graph
     *
     * @return The RDF string representation of the graph
     *
     * This docstring was generated by AI.
     */
    @Override
    public String toString() {
        return toRDF();
    }

    /**
     * Generates an RDF representation of the graph.
     *
     * The RDF representation is created by iterating over the indexed edges and
     * appending their RDF representation to a string builder. Various graph
     * statistics, such as the number of nodes, edges, and triples, are also
     * included in the RDF representation.
     *
     * @return The RDF representation of the graph
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the node manager of the index.
     *
     * @return The node manager of the index.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns a string representation of the graph's nodes, categorized by type.
     *
     * The method iterates over each node in the graph and counts the number of nodes of each type (URI, blank, literal, string, number, and date),
     * then returns the counts as a formatted string.
     *
     * @return A string representing the node counts of each type in the graph
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Displays a subset of the graph with a given maximum limit.
     *
     * @param max The maximum number of elements to display
     * @return A string representation of the graph subset
     *
     * This docstring was generated by AI.
     */
    public String display(int max) {
        return display(0, max);
    }

    /**
     * Displays a subgraph of the graph manager
     *
     * @param startIndex The starting index of the subgraph
     * @param endIndex The ending index of the subgraph
     * @return A string representation of the subgraph
     *
     * This docstring was generated by AI.
     */
    public String display() {
        return display(0, Integer.MAX_VALUE);
    }

    /**
     * Displays a partial list of edges and their associated predicates.
     *
     * The method iterates through the sorted properties (predicates) of the graph, adding
     * each one and its corresponding edges to a string builder. The number of edges
     * displayed for each property can be limited using the 'n' parameter, while the 'max'
     * parameter determines the maximum number of edges to display in total.
     *
     * @param n An integer to limit the number of edges displayed for each property
     * @param max An integer to limit the total number of edges displayed
     * @return A string representation of the displayed edges and their associated predicates
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns a proxy entailment for the graph manager.
     *
     * If the proxy is not yet initialized, the method creates a new entailment using
     * the current graph manager. If the creation of a new entailment fails, a new
     * entailment is initialized using the default configuration.
     *
     * @return A proxy entailment for the graph manager
     *
     * This docstring was generated by AI.
     */
    public Entailment getProxy() {
        if (proxy == null) {
            proxy = getEntailment();
            if (proxy == null) {
                proxy = Entailment.create(this);
            }
        }
        return proxy;
    }

    /**
     * Checks if an edge has a type
     *
     * @param edge The edge to check
     * @return True if the edge has a type, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isType(Edge edge) {
        return getProxy().isType(edge);
    }

    /**
     * Checks if a node is a type node in the graph
     *
     * @param pred The node to check
     * @return True if the node is a type node, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isType(Node pred) {
        return getProxy().isType(pred);
    }

    /**
     * Checks if this node is a subclass of the given predicate
     *
     * @param pred The predicate node
     * @return True if this node is a subclass of the given predicate, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isSubClassOf(Node pred) {
        return getProxy().isSubClassOf(pred);
    }

    /**
     * Checks if the given node is a subclass of the specified superclass node
     *
     * @param node The node to check for being a subclass
     * @param sup The superclass node to compare against
     * @return True if the given node is a subclass of the specified superclass node, false otherwise
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Sets the event handler for the graph
     *
     * @param h The event handler
     *
     * This docstring was generated by AI.
     */
    public void setEventHandler(EventHandler h) {
        getEventManager().setEventHandler(h);
    }

    /**
     * Sets the verbose mode for event management.
     *
     * In verbose mode, additional events such as insertion and construction are hidden
     * from the logger.
     *
     * @param b The verbose mode flag
     *
     * This docstring was generated by AI.
     */
    public void setVerbose(boolean b) {
        getEventManager().setVerbose(b);
        if (b) {
            // hide to logger
            getEventManager().hide(Event.Insert);
            getEventManager().hide(Event.Construct);
        }
    }

    /**
     * Indicates whether the event manager is in verbose mode.
     *
     * @return True if the event manager is verbose, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean isVerbose() {
        return getEventManager().isVerbose();
    }

    // RDF Star
    /**
     * Returns whether this edge has metadata associated with it.
     *
     * @return true if this edge has metadata, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isEdgeMetadata() {
        return edgeMetadata;
    }
    
    /**
     * Returns whether the graph is an RDF* graph
     *
     * @return true if the graph is an RDF* graph, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isRDFStar() {
        return edgeMetadata;
    }

    /**
     * Sets the flag indicating whether edge metadata should be stored.
     *
     * @param b The new value for the edge metadata flag
     *
     * This docstring was generated by AI.
     */
    public void setEdgeMetadata(boolean b) {
        edgeMetadata = b;
    }

    /**
     * Sets the default value for edge metadata.
     *
     * @param b The new default value for edge metadata
     *
     * This docstring was generated by AI.
     */
    public static void setEdgeMetadataDefault(boolean b) {
        EDGE_METADATA_DEFAULT = b;
    }

    /**
     * Sets the RDF Star mode
     *
     * @param b The new value for RDF Star mode
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Initializes the graph and returns a datatype value.
     *
     * This method calls the 'init' method to initialize the graph, and then
     * returns the 'DatatypeMap.TRUE' value. This method is typically called
     * when the graph is first started.
     *
     * @return The 'DatatypeMap.TRUE' value.
     *
     * This docstring was generated by AI.
     */
    public IDatatype start() {
        init();
        return DatatypeMap.TRUE;
    }

    /**
     * Performs cleanup and reset operations after a graph update.
     *
     * This method disables updates in the event manager, clears the node index and
     * distance, and performs delete operations if applicable. It is called after
     * a graph update event.
     *
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Cleans the timestamp index in the graph manager.
     *
     * This method cleans the timestamp index for rule edge lists in the graph manager.
     * It only performs an action if rule edge lists are present.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    public void clean() {
        // clean timestamp index
        if (hasRuleEdgeList) {
            ruleEdgeIndex.clean();
        }
    }

    /**
     * Resets the rule engine timestamp for all edges.
     *
     * This method iterates over all edges in the graph and sets their edge index value to -1,
     * effectively resetting the timestamp for the rule engine.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    public void cleanEdge() {
        // clean rule engine timestamp
        for (Edge ent : getEdges()) {
            ent.setEdgeIndex(-1);
        }
    }

    /**
     * Checks if entailment is activated in this graph.
     *
     * @return True if entailment is activated, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean hasEntailment() {
        return getEntailment() != null && getEntailment().isActivate();
    }

    // true when index must be sorted 
    /**
     * Returns whether the graph is indexable or not.
     *
     * @return true if the graph is indexable, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isIndexable() {
        return isIndexable;
    }
    
    /**
     * Sets the indexable flag for the graph manager.
     *
     * @param b The new value for the indexable flag.
     *
     * This docstring was generated by AI.
     */
    public void setIndexable(boolean b) {
        isIndexable = b;
    }
    
    // already indexed
    /**
     * Returns whether the graph is indexed or not
     *
     * @return true if the graph is indexed, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isIndexed() {
        return ! isIndexable();
    }
    
    /**
     * Sets whether the graph is indexed
     *
     * @param b Whether the graph should be indexed
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Returns the value resolver of the graph manager.
     *
     * @return The value resolver of the graph manager.
     *
     * This docstring was generated by AI.
     */
    public ValueResolver getValueResolver() {
        return values;
    }

    /**
     * Returns the subject edge indexer of the graph.
     *
     * @return The subject edge indexer of the graph.
     *
     * This docstring was generated by AI.
     */
    public EdgeManagerIndexer getIndex() {
        return getSubjectIndex();
    }

    /**
     * Starts an update process in the graph manager.
     *
     * This method is intentionally empty and needs to be implemented by the user.
     * It indicates the start of a sequence of operations that modify the state
     * of the graph.
     *
     * @since 1.0
     *
     * This docstring was generated by AI.
     */
    void startUpdate() {
    }
    
    /**
     * Finishes updating the index structure of the graph
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    public void finishUpdate() {
        getIndex().finishUpdate();
    }
    
    /**
     * Finishes the rule engine in the index.
     *
     * @since 1.0
     */
    public void finishRuleEngine() {
        getIndex().finishRuleEngine();
    }
            
    /**
     * Starts the loading process of the graph, optimizing it if the graph is empty.
     *
     * If the graph is empty or the {@code Property.Value.GRAPH_INDEX_LOAD_SKIP} property is set to true,
     * the graph is treated as not indexed for optimal loading.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Desactivates all node managers associated with edge indexers in the graph.
     *
     * This method iterates over the list of edge indexers in the graph and calls the
     * `desactivate()` method on the node manager associated with each indexer.
     * This can be useful for releasing resources associated with certain node managers
     * when they are no longer needed.
     *
     * @since 1.0
     */
    public void clearNodeManager() {
        for (EdgeManagerIndexer id : getIndexList()) {
            id.getNodeManager().desactivate();
        }
    }
    
    // Clear Index of nodes in their named graph
    /**
     * Clears the node index of the graph manager.
     *
     * @since 1.0
     */
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

    /**
     * Basic indexing method for the graph manager.
     *
     * This method iterates through the list of indexers and calls the index method on each one to build
     * the edge index in the graph.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    void basicIndex() {
        for (EdgeManagerIndexer ei : getIndexList()) {
            ei.index();
        }
    }

    /**
     * Compacts the index and subject index in the graph manager.
     *
     * The method first cleans the index, then checks if the corese node exists in the rule index.
     * If it does, the subject index is compacted.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    public void compact() {
        cleanIndex();
        if (containsCoreseNode(getNode(Graph.RULE_INDEX))) {
            getSubjectIndex().compact();
        }
    }

    /**
     * Cleans the index structures of the graph manager.
     *
     * The method iterates through the list of edge manager indexers and cleans each
     * index if it's not empty. An index is cleaned by calling the `clean()` method.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Indexes the graph if it is indexable.
     *
     * This method checks if the graph is indexable, and if so, it indexes the graph.
     * It also performs indexing on the node manager.
     *
     * @throws IllegalStateException if the graph is not indexable
     * @see #isIndexable()
     * @see #index()
     * @see #performIndexNodeManager()
     *
     * This docstring was generated by AI.
     */
    void indexGraph() {
        if (isIndexable()) {
            index();
        }
        performIndexNodeManager();
    }

    /**
     * Performs the index node manager initialization if it's not active.
     *
     * This method checks if the node manager's indexing feature is active. If not,
     * it calls the `indexNodeManager()` method to initialize it.
     *
     * @return void
     */
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
    /**
     * Indexes a node in the graph if necessary.
     *
     * This method synchronizes access to the node index using the
     * {@code indexNode()} method of the subject index. It checks if the
     * current node graph index is empty before performing the indexing
     * operation.
     *
     * @return {@code void}
     *
     * This docstring was generated by AI.
     */
    synchronized void indexNode() {
        if (getNodeGraphIndex().size() == 0) {
            getSubjectIndex().indexNode();
        }
    }

    /**
     * Indexes resource nodes in the graph with unique integer identifiers.
     *
     * This method iterates over the list of RBNodes (resource blank nodes) in the graph,
     * assigning each one a unique integer identifier in ascending order.
     * The identifiers are stored in each node's index property.
     *
     * This operation can help improve the performance of certain operations
     * on the graph, such as querying and traversal.
     *
     * @return void This method does not return a value.
     *
     * This docstring was generated by AI.
     */
    public void indexResources() {
        int i = 0;
        for (Node n : getRBNodes()) {
            n.setIndex(i++);
        }
    }

    // declare subject/object as graph vertex
    /**
     * Adds an asserted edge to the graph index if it is asserted.
     *
     * The method checks if the given edge is asserted. If it is, the edge is added
     * to the graph index using the {@code nodeGraphIndex.add(ent)} method.
     *
     * @param ent The edge to be added to the graph index
     *
     * This docstring was generated by AI.
     */
    public void define(Edge ent) {
        if (ent.isAsserted()) {
            nodeGraphIndex.add(ent);
        }
    }

    /**
     * Returns an iterable collection of properties in this graph.
     *
     * @return An iterable collection of nodes representing the properties.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getProperties() {
        return getSubjectIndex().getProperties();
    }

    /**
     * Returns the sorted properties from the subject index.
     *
     * @return An iterable of nodes representing the sorted properties.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getSortedProperties() {
        return getSubjectIndex().getSortedProperties();
    }
    
    /**
     * Sets a value in the graph using the provided key.
     *
     * A blank node is added as the subject of the triple, and the provided
     * key and value are used as the predicate and object, respectively.
     * The method then returns the provided value.
     *
     * @param key The predicate of the triple
     * @param value The object of the triple
     * @return The object of the triple, which is the provided value
     *
     * This docstring was generated by AI.
     */
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        insert(addBlank().getDatatypeValue(), key, value);
        return value;
    }
      
    /**
     * Adds an edge to the graph with the 'addAll' flag set to true
     *
     * @param edge The edge to be added to the graph
     * @return The added edge
     *
     * This docstring was generated by AI.
     */
    public Edge add(Edge edge) {
        return add(edge, true);
    }

    /**
     * Adds an edge to the graph and updates the index structure.
     *
     * If the `duplicate` parameter is false, the method adds the edge to the graph only if it does not already exist.
     * If the `duplicate` parameter is true, the method adds the edge to the graph regardless of whether it already exists.
     * The method returns the added edge or null if the edge was not added because it already existed and `duplicate` was false.
     *
     * @param edge The edge to be added to the graph
     * @param duplicate If true, the edge is added regardless of whether it already exists; if false, the edge is added only if it does not already exist
     * @return The added edge or null if the edge was not added because it already existed and `duplicate` was false
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Declares an edge in the graph, handling duplicates.
     *
     * The method iterates over all registered indexers, declaring the edge
     * in each one that is not empty. If <code>duplicate</code> is true,
     * any existing edge with the same properties will be replaced.
     *
     * @param edge   The edge to declare in the graph
     * @param duplicate  Whether to replace existing edges with the same properties
     *
     * This docstring was generated by AI.
     */
    void declare(Edge edge, boolean duplicate) {
        for (EdgeManagerIndexer ei : getIndexList()) {
            if (ei.getIndex() != 0) {
                ei.declare(edge, duplicate);
            }
        }
    }

    /**
     * Checks if an edge exists in the graph.
     *
     * @param edge The edge to check for existence.
     * @return <code>true</code> if the edge exists, <code>false</code> otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean exist(Edge edge) {
        return getSubjectIndex().exist(edge);
    }
    
    /**
     * Finds an edge in the graph by its subject index.
     *
     * @param edge The edge to find
     * @return The found edge
     *
     * This docstring was generated by AI.
     */
    public Edge find(Edge edge) {
        return getSubjectIndex().find(edge);
    }
    
    /**
     * Finds an edge in the graph using a subject, predicate, and object.
     *
     * @param s The subject node.
     * @param p The predicate node.
     * @param o The object node.
     * @return The edge if it exists; {@code null} otherwise.
     *
     * This docstring was generated by AI.
     */
    public Edge findEdge(Node s, Node p, Node o) {
        return getSubjectIndex().findEdge(s, p, o);
    }
    
    /**
     * Finds a triple node in the graph.
     *
     * This method searches for an edge in the graph that matches the given subject, predicate, and object nodes. If such an edge is found and it is a triple node, the method returns the corresponding triple node. Otherwise, it returns null.
     *
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @return The triple node corresponding to the found edge, or null if no such edge or if the edge is not a triple node
     *
     * This docstring was generated by AI.
     */
    public TripleNode findTriple(Node s, Node p, Node o) {
        Edge edge = findEdge(s, p, o);
        if (edge == null || ! edge.isTripleNode()) {
            return null;
        }
        return ((EdgeTripleNode) edge).getTriple();
    }

    /**
     * Checks if a triple with the given property and nodes exists in the graph.
     *
     * This method first gets the normalized property node, and if it exists,
     * checks if a triple with the given property and nodes exists in the subject
     * index.
     *
     * @param p The property node
     * @param n1 The first node
     * @param n2 The second node
     * @return True if the triple exists, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean exist(Node p, Node n1, Node n2) {
        p = getPropertyNode(p);
        if (p == null) {
            return false;
        }
        return getSubjectIndex().exist(p, n1, n2);
    }
    
    /**
     * Adds an edge with a node to the graph.
     *
     * This method first adds the edge node and then adds the edge to the graph.
     *
     * @param ee The edge to be added
     * @return The edge with the added node
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Deletes an edge with a target node from the graph.
     *
     * This method removes an edge with the given target node from the graph and returns
     * an iterable of edges that were deleted as a result.
     *
     * @param edge The edge to delete, which must contain a valid target node
     * @return An iterable of edges that were deleted as a result of the operation
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> deleteEdgeWithTargetNode(Edge edge) {
        return delete(
                edge.getGraphNode(), edge.getSubjectNode(),
                edge.getPropertyNode(),
                edge.getObjectNode());
    }

    /**
     * Adds an edge node to the graph.
     *
     * This method adds an edge node and its associated nodes to the graph. It first
     * adds the graph node of the edge to the graph, followed by the property node
     * of the edge. Then, it adds each node in the edge to the graph.
     *
     * @param ee The edge node to be added
     *
     * This docstring was generated by AI.
     */
    public void addEdgeNode(Edge ee) {
        addGraphNode(ee.getGraph());
        addPropertyNode(ee.getEdgeNode());
        for (int i = 0; i < ee.nbGraphNode(); i++) {
            add(ee.getNode(i));
        }
    }

    /**
     * Adds a list of nodes to the graph, starting with the default graph node.
     *
     * @param list The list of nodes to be added.
     * @return The node that was created as the parent node of the list.
     *
     * This docstring was generated by AI.
     */
    public Node addList(List<Node> list) {
        return addList(addDefaultGraphNode(), list);
    }

    /**
     * Adds a list of nodes to a graph, creating a linked list structure in the process.
     *
     * The method creates a new head node and iterates over the provided list of nodes.
     * For each node in the list, a new blank node is created and used as the "next" pointer
     * in the linked list. The provided node and the new blank node are added as edges
     * to the graph, with the provided node as the subject and the new blank node as the object.
     * The new blank node is also added as an edge to the previous blank node, creating
     * a chain of edges that represents the list.
     *
     * @param g The graph to add the list to.
     * @param list The list of nodes to add to the graph.
     * @return The head node of the linked list created in the graph.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds an edge to the graph with automatic indexing
     *
     * @param edge The edge to add
     * @return The added edge with any modifications made during indexing
     *
     * This docstring was generated by AI.
     */
    public Edge addEdge(Edge edge) {
        return addEdge(edge, true);
    }

    /**
     * Adds an edge to the graph and processes events if successful.
     *
     * The method adds the given edge to the graph. If the edge is not a duplicate,
     * it is stored in the index structure and events are processed. Otherwise,
     * the existing edge is returned.
     *
     * @param edge The edge to add to the graph
     * @param duplicate Whether the edge being added is a duplicate
     * @return The added edge if it was not a duplicate, or the existing edge if it was
     *
     * This docstring was generated by AI.
     */
    public Edge addEdge(Edge edge, boolean duplicate) {
        Edge ent = add(edge, duplicate);
        if (ent != null) {
            getEventManager().process(Event.Insert, ent);
            manager.onInsert(ent.getGraph(), edge);
        }
        return ent;
    }


    /**
     * Adds a list of edges to the graph, associated with a predicate node.
     *
     * If the list is empty, the method does nothing. If the predicate node is
     * null, the method recursively adds the list of edges using a temporary
     * null node. Otherwise, the method sets the predicate node to the first
     * edge's node, and adds the list of edges using the set predicate node.
     *
     * @param p The predicate node to associate the list of edges with
     * @param list The list of edges to add to the graph
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a new edge in the graph with the given nodes and predicate.
     *
     * @param source  The source node of the edge.
     * @param subject The subject node of the edge.
     * @param predicate The predicate of the edge.
     * @param value  The value node of the edge.
     *
     * @return The newly created edge.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a delete edge in the graph.
     *
     * @param source The source node of the edge.
     * @param subject The subject node of the edge.
     * @param predicate The predicate node of the edge.
     * @param value The object node of the edge.
     *
     * @return A delete edge object.
     *
     * This docstring was generated by AI.
     */
    public Edge createDelete(Node source, Node subject, Node predicate, Node value) {
        return fac.createDelete(source, subject, predicate, value);
    }

    /**
     * Creates a delete edge in the graph.
     *
     * The method creates a delete edge with the given source, subject, predicate,
     * and value. If the source is null, no graph node is associated with the
     * delete edge.
     *
     * @param source The source node of the delete edge (can be null)
     * @param subject The subject node of the delete edge
     * @param predicate The predicate of the delete edge
     * @param value The value of the delete edge
     * @return The newly created delete edge
     *
     * This docstring was generated by AI.
     */
    public Edge createDelete(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        Node graph = (source == null) ? null : getCreateNode(source);
        return fac.createDelete(graph, getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }

    /**
     * Gets or creates a node with the given datatype
     *
     * @param dt The datatype of the node
     * @return The node with the specified datatype
     *
     * This docstring was generated by AI.
     */
    Node getCreateNode(IDatatype dt) {
        return getNode(dt, true, false);
    }

    /**
     * Creates a new edge with the given source node, predicate, and list of nodes.
     *
     * @param source The source node.
     * @param predicate The predicate.
     * @param list The list of nodes.
     * @return The newly created edge.
     *
     * This docstring was generated by AI.
     */
    public Edge create(Node source, Node predicate, List<Node> list) {
        return fac.create(source, predicate, list);
    }
    
    /**
     * Creates a new edge in the graph with the given source node, predicate,
     * list of objects, and nested flag.
     *
     * @param source     The source node of the edge.
     * @param predicate  The predicate of the edge.
     * @param list       The list of object nodes for the edge.
     * @param nested     A flag indicating whether the edge is nested.
     * @return The newly created edge.
     *
     * This docstring was generated by AI.
     */
    public Edge create(Node source, Node predicate, List<Node> list, boolean nested) {
        return fac.create(source, predicate, list, nested);
    }

    /**
     * Creates a delete edge in the graph.
     *
     * @param source The source node of the edge.
     * @param predicate The predicate of the edge.
     * @param list The list of target nodes of the edge.
     * @return A new delete edge object.
     *
     * This docstring was generated by AI.
     */
    public Edge createDelete(Node source, Node predicate, List<Node> list) {
        return fac.createDelete(source, predicate, list);
    }
    
    /**
     * Creates a new edge in the graph with the given parameters.
     *
     * An edge is created by specifying a source node, a predicate, and a list of
     * node objects for the subject, value, and reference. These nodes are
     * obtained through the getCreateNode() method.
     *
     * @param source     The source node for the edge
     * @param subject    The subject node for the edge
     * @param predicate  The predicate for the edge
     * @param value      The value node for the edge
     * @param ref        The reference node for the edge
     * @return           The newly created edge
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Creates a new triple in the graph with a given subject, predicate, and object.
     *
     * The method creates a new triple with the given subject, predicate, and object,
     * and returns a reference to the triple. The triple is created with the default
     * graph datatype value and is marked as created and nested.
     *
     * @param s The subject of the triple
     * @param p The predicate of the triple
     * @param o The object of the triple
     * @return The reference to the created triple
     *
     * This docstring was generated by AI.
     */
    public IDatatype createTriple(IDatatype s, IDatatype p, IDatatype o) {
        IDatatype ref = createTripleReference();
        Edge e = create(getDefaultGraphDatatypeValue(), s, p, o, ref);
        e.setCreated(true);
        e.setNested(true);
        return ref;
    }
    
    /**
     * Returns a list of nodes with the given nodes added.
     *
     * A new ArrayList is created and the given nodes are added to it.
     *
     * @param list An array of nodes to be added to the new list
     * @return A new ArrayList containing the given nodes
     *
     * This docstring was generated by AI.
     */
    List<Node> list(Node... list) {
        ArrayList<Node> alist = new ArrayList<>();
        alist.addAll(Arrays.asList(list));
        return alist;
    }

    /**
     * Creates a new edge in the graph.
     *
     * @param source The source node of the edge.
     * @param subject The subject node of the edge.
     * @param predicate The predicate of the edge.
     * @param value The object value of the edge.
     * @return The newly created edge.
     *
     * This docstring was generated by AI.
     */
    public Edge create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        return create(getCreateNode(source), getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }
    
    /**
     * Creates a new edge with the given subject, predicate, and value.
     *
     * @param subject The subject node ID.
     * @param predicate The predicate node ID.
     * @param value The value node ID.
     * @return The newly created edge.
     *
     * This docstring was generated by AI.
     */
    public Edge create(IDatatype subject, IDatatype predicate, IDatatype value) {
        return create(getDefaultGraphNode(), getCreateNode(subject), getCreateNode(predicate), getCreateNode(value));
    }

    /**
     * Returns the number of triples in the graph.
     *
     * @return The size of the graph
     *
     * This docstring was generated by AI.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the total number of nodes in the graph.
     *
     * @return The total number of nodes in the graph, obtained by summing the
     * number of individuals, blanks, and literals.
     *
     * This docstring was generated by AI.
     */
    public int nbNodes() {
        return nbIndividuals() + nbBlanks() + nbLiterals();
    }

    /**
     * Returns the node index.
     *
     * @return The node index.
     *
     * This docstring was generated by AI.
     */
    public int getNodeIndex() {
        return nodeIndex;
    }

    /**
     * Returns the number of individuals and blank nodes in the graph
     *
     * @return The total number of resources in the graph
     *
     * This docstring was generated by AI.
     */
    public int nbResources() {
        return nbIndividuals() + nbBlanks();
    }

    /**
     * Returns the number of individuals in the graph.
     *
     * @return The number of individuals in the graph.
     *
     * This docstring was generated by AI.
     */
    public int nbIndividuals() {
        return individual.size();
    }

    /**
     * Returns the number of blank nodes in the graph.
     *
     * @return The size of the blank node map.
     *
     * This docstring was generated by AI.
     */
    public int nbBlanks() {
        return blank.size();
    }
    
    /**
     * Returns the number of triples in the graph.
     *
     * @return The number of triples in the graph.
     *
     * This docstring was generated by AI.
     */
    public int nbTriples() {
        return triple.size();
    }

    /**
     * Returns the number of literal nodes in the graph.
     *
     * @return The number of literal nodes.
     *
     * This docstring was generated by AI.
     */
    public int nbLiterals() {
        return getLiteralNodeManager().size();
    }

    /**
     * Sets the size of the graph.
     *
     * @param n The new size
     *
     * This docstring was generated by AI.
     */
    public void setSize(int n) {
        size = n;
    }

    /**
     * Creates a copy of a given node.
     *
     * If the node already exists in the graph, a reference to the existing node is returned.
     * Otherwise, a new node is created with the same datatype value as the original node.
     *
     * @param node The node to copy
     * @return The copied node
     *
     * This docstring was generated by AI.
     */
    public Node copy(Node node) {
        Node res = getExtNode(node);
        if (res == null) {
            res = getNode(getDatatypeValue(node), true, false);
        }
        return res;
    }

    /**
     * Returns the datatype value of a given node
     *
     * @param node The node to get the datatype value of
     * @return The datatype value of the given node
     *
     * This docstring was generated by AI.
     */
    IDatatype getDatatypeValue(Node node) {
        return  node.getValue();
    }

    /**
     * Retrieves the top class node of the graph.
     *
     * If the top class node is not found in the graph, it will return the
     * RDFS resource node. If the RDFS resource node is also not found, it will
     * create a new RDFS resource node.
     *
     * @return The top class node of the graph
     *
     * This docstring was generated by AI.
     */
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
    
     /**
      * Returns the first non-null node from the given names or creates a new node.
      *
      * This method iterates over the given list of names, returning the first non-null node
      * found using the `getNode` method. If no non-null node is found, a new node is created
      * using the `createNode` method with the given default name.
      *
      * @param defaut The default name for the new node
      * @param nameList A list of node names to search for
      * @return The first non-null node from the given names or a new node with the default name
      *
      * This docstring was generated by AI.
      */
     public Node getTopClass(String defaut, String... nameList) {
        for (String name : nameList) {
            Node n = getNode(name);
            if (n != null) {
                return n;
            }
        }
        return createNode(defaut);
         }

    /**
     * Returns the top property node of the graph manager.
     *
     * If the top property node does not exist, it creates a new one.
     *
     * @return The top property node of the graph manager.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the top properties from the graph.
     *
     * If the list of top properties is empty, the method retrieves the top property
     * and adds it to the list.
     *
     * @return A list of Node objects representing the top properties
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Retrieves a node in the graph with the given IDatatype, creating and adding it if necessary.
     *
     * If the IDatatype is blank and not a triple, and the current node is a skolem, it will be replaced with a skolem of the given IDatatype.
     *
     * @param gNode The node to use as a base for the lookup.
     * @param dt The IDatatype to look up or create.
     * @param create If true, a new node will be created if one does not already exist with the given IDatatype.
     * @param add If true, the new node will be added to the graph.
     * @return The node with the given IDatatype, or null if no such node exists and create is false.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves a node from the graph by its identifier.
     *
     * If the node is a URI, the method looks up the node by its label. Otherwise, it looks up the node using the
     * node itself.
     *
     * @param node The identifier of the node to retrieve
     * @return The node from the graph, or null if the node is not found
     *
     * This docstring was generated by AI.
     */
    @Override
    public Node getVertex(Node node) {
        if (node.getDatatypeValue().isURI()) {
            return getNode(node.getLabel());
        }
        return getNode(node);
    }

    /**
     * Creates a new node with the given datatype.
     *
     * @param dt The datatype of the new node.
     * @return A new node with the given datatype.
     *
     * This docstring was generated by AI.
     */
    public Node createNode(IDatatype dt) {
        return getNode(dt, true, false);
    }

    // all nodes
    // TODO: check producer
    /**
     * Adds a node with the given datatype and returns it.
     *
     * @param dt The datatype of the node.
     * @return The newly added node.
     *
     * This docstring was generated by AI.
     */
    public Node addNode(IDatatype dt) {
        return getNode(dt, true, true);
    }

    /**
     * Adds a node and returns the resulting node
     *
     * @param node The node to add
     * @return The resulting node after adding the given node
     *
     * This docstring was generated by AI.
     */
    public Node addNode(Node node) {
        return getNode(value(node), true, true);
    }

    // used by construct
    /**
     * Retrieves a node from the graph based on the given datatype.
     *
     * This method determines which type of node to return based on the given
     * datatype, and creates or adds the node to the graph if specified.
     *
     * @param dt The datatype representing the node
     * @param create If true, creates a new node if one does not exist
     * @param add If true, adds the node to the graph
     * @return The retrieved node
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Retrieves an external node based on a given node.
     *
     * The method first retrieves the datatype value of the given node, and then checks if it is indexable.
     * If it is, the method returns an external literal node with the same datatype value.
     * Otherwise, it returns the node with the matching identifier.
     *
     * @param node The node to retrieve the external node for
     * @return The external node for the given node
     *
     * This docstring was generated by AI.
     */
    public Node getExtNode(Node node) {
        IDatatype dt = getDatatypeValue(node);
        if (isSameIndexAble(dt)) {
            return getExtLiteralNode(dt);
        } else {
            return getNode(node);
        }
    }

    /**
     * Retrieves or creates a resource node in the graph.
     *
     * If a node with the specified datatype exists, it is returned. If it does not
     * exist and 'create' is true, a new node is created. If 'add' is true, the
     * datatype is added to the node.
     *
     * @param dt        The datatype of the resource node.
     * @param create    If true, a new node is created if it does not already exist.
     * @param add       If true, the datatype is added to the node.
     * @return          The resource node with the specified datatype.
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Retrieves or creates a blank node with the specified datatype.
     *
     * If a blank node with the specified datatype label exists, it is returned.
     * Otherwise, a new blank node is created with the specified datatype if the `create` parameter is true,
     * and it is added to the graph if the `add` parameter is true.
     *
     * @param dt The datatype
     * @param create If true, creates a new blank node if one does not already exist
     * @param add If true, adds the new or existing blank node to the graph
     * @return The blank node with the specified datatype label
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Retrieves or creates a blank node with the given datatype.
     *
     * If a blank node with the given datatype label exists, it is returned. Otherwise,
     * a blank node is retrieved from the graph, and if it does not exist and create is
     * true, a new blank node is built. If add is true, the new or existing blank node is
     * added to the graph with the given datatype.
     *
     * @param dt          The datatype of the blank node
     * @param create     Whether to create a new blank node if it does not exist
     * @param add        Whether to add the blank node to the graph
     * @return           The blank node with the given datatype
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Retrieves or creates a triple node in the graph manager.
     *
     * The method first attempts to retrieve a node with the given label. If the node is
     * null and `create` is true, a new node is created. If `add` is true, the new node is added
     * to the graph.
     *
     * @param dt The datatype representing the node label
     * @param create If true, a new node is created if it doesn't exist
     * @param add If true, the new node is added to the graph if it's created
     * @return The retrieved or created node
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves or creates a literal node in the graph.
     *
     * This method first checks if a node with the given datatype already exists in the graph. If it does, the method returns that node. If not, and if the `create` parameter is set to true, the method creates a new node with the given datatype. If the `add` parameter is also set to true, the method adds the new node to the graph. In all cases, the method returns the node.
     *
     * @param dt The datatype of the node to retrieve or create
     * @param create Whether to create a new node if one does not already exist
     * @param add Whether to add the new node to the graph if it is created
     * @return The node with the given datatype, or null if no such node exists and `create` is false
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves a node based on a key and name.
     *
     * The method first checks for an existing node in the system, followed by
     * the graph, property, and system nodes. If a node is found, it's returned;
     * otherwise, null is returned.
     *
     * @param key A string representing the key
     * @param name A string representing the name
     * @return The node associated with the key and name, or null if not found
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Checks if a given node is an individual node.
     *
     * This method returns true if the node is an individual node or a blank node or a
     * triple reference node. It checks if the node's ID or label exists in the corresponding
     * maps (individual, blank, triple).
     *
     * @param node The node to be checked
     * @return true if the node is an individual node or a blank node or a triple reference node
     *
     * This docstring was generated by AI.
     */
    public boolean isIndividual(Node node) {
        return individual.containsKey(getID(node))
                || blank.containsKey(node.getLabel())
                || triple.containsKey(node.getLabel());
    }

    // resource node
    /**
     * Returns the node with the given name.
     *
     * @param name The name of the node.
     * @return The node with the given name.
     *
     * This docstring was generated by AI.
     */
    public Node getNode(String name) {
        return getNode(getID(name), name);
    }

    /**
     * Returns the node with the given key from the individual map.
     *
     * @param key   The key of the node to retrieve.
     * @param name  Not used in this method.
     * @return      The node with the given key, or null if it does not exist.
     *
     * This docstring was generated by AI.
     */
    Node getNode(String key, String name) {
        return individual.get(key);
    }

    /**
     * Adds a node to the individual map with a given IDatatype.
     *
     * @param dt The IDatatype value.
     * @param node The Node to be added.
     *
     * This docstring was generated by AI.
     */
    void addNode(IDatatype dt, Node node) {
        individual.put(getID(node), node);
    }

    /**
     * Returns a blank node with the given name
     *
     * @param name The name of the blank node
     * @return A blank node with the given name
     *
     * This docstring was generated by AI.
     */
    public Node getBlankNode1(String name) {
        return getBlankNodeBasic(name);
    }
    
    // bnode subject/object or named graph id
    /**
     * Returns a blank node using a name if it exists, otherwise creates a new one.
     *
     * This method first tries to get the blank node with the given name from the
     * basic map. If it is not found, it tries to get it from the graph map. If it
     * still does not exist, a new blank node is created with the given name.
     *
     * @param name The name of the blank node
     * @return The blank node associated with the given name or a new blank node
     *         if it does not exist
     *
     * This docstring was generated by AI.
     */
    public Node getBlankNode(String name) {
        Node node = getBlankNodeBasic(name);
        if (node == null) {
            node = getBlankNodeGraph(name);
        }
        return node;
    }
    
    /**
     * Returns a blank node from the map with the given name
     *
     * @param name The name of the blank node
     * @return The blank node with the given name or null if not found
     *
     * This docstring was generated by AI.
     */
    public Node getBlankNodeBasic(String name) {
        return blank.get(name);
    }
    
    // named graph id may be a bnode
    /**
     * Returns the blank node with the given name from the graph.
     *
     * @param name The name of the blank node.
     * @return The blank node with the given name, or null if it doesn't exist.
     *
     * This docstring was generated by AI.
     */
    public Node getBlankNodeGraph(String name) {
        return graph.get(name);
    }
    
    /**
     * Returns the triple node with the given name from the triple map.
     *
     * @param name The name of the triple node.
     * @return The {@link Node} object associated with the given name, or null if no such node exists.
     *
     * This docstring was generated by AI.
     */
    public Node getTripleNode(String name) {
        return triple.get(name);
    }

    /**
     * Adds a blank node to the graph with the given datatype and label.
     *
     * @param dt The datatype of the blank node.
     * @param node The label of the blank node.
     *
     * This docstring was generated by AI.
     */
    void addBlankNode(IDatatype dt, Node node) {
        blank.put(node.getLabel(), node);
    }
    
    /**
     * Adds a triple node to the graph with the given label and node object.
     *
     * @param dt The node label.
     * @param node The node object.
     *
     * This docstring was generated by AI.
     */
    void addTripleNode(IDatatype dt, Node node) {
        triple.put(node.getLabel(), node);
    }
    
    /**
     * Removes a triple node from the graph manager.
     *
     * @param node The node to be removed from the graph.
     *
     * This docstring was generated by AI.
     */
    public void removeTripleNode(Node node) {
        triple.remove(node.getLabel());
    }

    /**
     * Returns the identifier of a node based on a flag.
     *
     * If the flag {@code valueOut} is true, then the method returns the node's key.
     * Otherwise, it returns the node's label.
     *
     * @param node The node to get the identifier from
     * @return The identifier of the node as a string
     *
     * This docstring was generated by AI.
     */
    String getID(Node node) {
        if (valueOut) {
            return node.getKey();
        } else {
            return node.getLabel();
        }
    }

    /**
     * Returns the ID of a string if the value output flag is true,
     * otherwise returns the original string.
     *
     * This method checks the value output flag and returns the key
     * associated with the input string from the values map if the
     * flag is true, otherwise it returns the original string.
     *
     * @param str The input string
     * @return The ID of the input string if value output flag is true,
     * otherwise the original string
     *
     * This docstring was generated by AI.
     */
    String getID(String str) {
        if (valueOut) {
            return values.getKey(str);
        } else {
            return str;
        }
    }

    /**
     * Returns the key for a datatype based on a flag.
     *
     * If the valueOut flag is true, the key is retrieved from the values table using
     * the provided datatype. Otherwise, the label of the datatype is returned.
     *
     * @param dt The datatype to get the key for
     * @return The key for the provided datatype
     *
     * This docstring was generated by AI.
     */
    String getKey(IDatatype dt) {
        if (valueOut) {
            return values.getKey(dt);
        } else {
            return dt.getLabel();
        }
    }

    /**
     * Adds a node to the graph with basic properties
     *
     * @param node The node to add
     * @return The node added to the graph
     *
     * This docstring was generated by AI.
     */
    Node basicAddGraph(Node node) {
        return basicAddGraph(node.getLabel(), node.isBlank());
    }
    
    /**
     * Adds a graph with the given label and returns the node.
     *
     * @param label The label of the graph to add
     * @return The node representing the added graph
     *
     * This docstring was generated by AI.
     */
    Node basicAddGraph(String label) {
        return basicAddGraph(label, false);
    }
    
    /**
     * Adds a node to the graph with the specified label and determines if it is a blank node.
     *
     * The method first checks if a node with the given label already exists in the graph. If it does, the method returns the existing node. If it doesn't, the method creates a new node with the specified label and adds it to the graph. If the label corresponds to a blank node, the method creates a new blank node. Otherwise, it creates a new resource node.
     *
     * @param label The label of the node
     * @param bnode A boolean flag indicating if the node is a blank node
     * @return The node added to the graph
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Adds a graph node to the manager using a label and a flag to indicate
     * if it's a blank node.
     *
     * This method checks if a node with the given label already exists,
     * and if not, creates a new node for the label and adds it to the graph.
     * If the flag is true or the label is blank, a blank node is created,
     * otherwise, a resource node is created. The node is added to various
     * maps and tables for storing system nodes, individual nodes, blank nodes,
     * and triple reference nodes. The method returns the added node.
     *
     * @param label The label for the graph node
     * @param bnode A flag indicating if the label is for a blank node
     * @return The added graph node
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Checks if a node label is blank
     *
     * @param label The node label to check
     * @return True if the label starts with the blank node prefix, false otherwise
     *
     * This docstring was generated by AI.
     */
    boolean isBlank(String label) {
        return label.startsWith(BLANK);
    }

    /**
     * Adds a node to the graph with its label as the key.
     *
     * The method retrieves the node's label and adds the node to the graph using the
     * label as the key. The node is also returned as the result.
     *
     * @param node The node to add to the graph
     * @return The node that was added to the graph
     *
     * This docstring was generated by AI.
     */
    Node basicAddGraphNode(Node node) {
        graph.put(node.getLabel(), node);
        return node;
    }

    /**
     * Adds a new resource node to the graph with the given label.
     *
     * If a node with the same label already exists in the graph, it is returned.
     * Otherwise, a new node with the given label is created and added to the graph.
     *
     * @param label The label of the resource node
     * @return The node with the given label
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds a property to the graph with the given label and returns the node representing the property.
     *
     * If a node with the given label already exists in the graph, it is returned. Otherwise, a new node is created,
     * indexed, and added to the graph. If the label does not correspond to a resource, a new node with an appropriate
     * datatype is created.
     *
     * @param label The label of the property to add
     * @return The node representing the added property
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds a blank node with the given label to the graph.
     *
     * If a blank node with the given label already exists, it is returned. Otherwise,
     * a new blank node is created, added to the graph, and returned.
     *
     * @param label The label of the blank node
     * @return The blank node with the given label
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Adds a blank node with the given label to the graph.
     *
     * If a blank node with the given label already exists, it is returned.
     * If not, a new blank node is created and added to the graph.
     *
     * @param label The label for the blank node
     * @return The blank node with the given label
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Adds a triple reference node to the graph if it does not already exist.
     *
     * This method checks if a node with the given label already exists in the graph.
     * If it does not, a new triple reference node is created, added to the graph,
     * and indexed. The method then returns the newly created node.
     *
     * @param label The label of the triple reference node
     * @return The node with the given label
     *
     * This docstring was generated by AI.
     */
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
    
     /**
      * Adds a triple reference to the graph with a unique label.
      *
      * This method creates a new triple node with the given subject, predicate,
      * and object nodes, adds it to the graph, and returns the node. If a node
      * with the same label already exists, it returns that node instead.
      *
      * @param s The subject node of the triple
      * @param p The predicate node of the triple
      * @param o The object node of the triple
      * @return The triple node with the given subject, predicate, and object nodes
      *
      * This docstring was generated by AI.
      */
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
    


    /**
     * Adds a node to the graph with a datatype value.
     *
     * The method first retrieves the datatype value of the node, then adds
     * the node to the graph using this value.
     *
     * @param node The node to be added to the graph
     *
     * This docstring was generated by AI.
     */
    public void add(Node node) {
        IDatatype dt = getDatatypeValue(node);
        add(dt, node);
    }

    /**
     * Adds a node to the graph with a given frequency.
     *
     * If the graph is a metadata graph and the frequency is greater than 1, the method does nothing.
     * Otherwise, the node is added to the graph with the specified frequency.
     *
     * @param node The node to be added to the graph
     * @param n The frequency of the node
     */
    public void add(Node node, int n) {
        if (isMetadata() && n > 1) {
            return;
        }
        add(node);
    }

    /**
     * Adds a node to the graph based on its data type.
     *
     * The method checks the data type of the provided datatype object and adds the
     * node to the appropriate data structure in the graph. If the data type is
     * a literal, it is added to the literal nodes. If it is a triple, it is added
     * to the triple nodes and indexed for efficient querying. If it is blank,
     * it is added to the blank nodes and indexed. Otherwise, it is added to the
     * regular nodes and indexed.
     *
     * @param dt The datatype of the node to be added.
     * @param node The node to be added to the graph.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds a new literal node to the graph.
     *
     * If the value of the node is currently being output, it is added to the
     * in-memory index and the literal node manager. If not, it is only added
     * to the literal node manager. The nodes are indexed by their datatype
     * and key.
     *
     * @param dt The datatype of the node
     * @param node The node to be added
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the literal node associated with the given datatype.
     *
     * @param dt The datatype for which to retrieve the literal node.
     * @return The literal node associated with the given datatype.
     *
     * This docstring was generated by AI.
     */
    public Node getLiteralNode(IDatatype dt) {
        return getLiteralNode(getKey(dt), dt);
    }

    // return same datatype value with possibly different label (e.g. 10 vs 1e1)
    /**
     * Returns the external literal node for the given datatype.
     *
     * @param dt The datatype
     * @return The external literal node
     *
     * This docstring was generated by AI.
     */
    public Node getExtLiteralNode(IDatatype dt) {
        return getLiteralIndexManager().get(dt);
    }

    /**
     * Retrieves a node from the graph based on a key and datatype.
     *
     * The method checks the 'valueOut' flag to determine which map to search.
     * If 'valueOut' is true, it searches the 'vliteral' map with the given key.
     * Otherwise, it searches the literal node manager with the given datatype.
     *
     * @param key The key to search for in the map(s).
     * @param dt The datatype to search for in the literal node manager.
     * @return The node retrieved from the map(s) based on the given key and datatype.
     *
     * This docstring was generated by AI.
     */
    public Node getLiteralNode(String key, IDatatype dt) {
        if (valueOut) {
            return vliteral.get(key);
        } else {
            return getLiteralNodeManager().get(dt);
        }
    }

    /**
     * Returns a graph node with the given label.
     *
     * @param label The label of the node to retrieve.
     * @return A {@link Node} object representing the graph node with the given label.
     *
     * This docstring was generated by AI.
     */
    public Node getGraphNode(String label) {
        return getGraphNode(getID(label), label);
    }

    /**
     * Returns the graph node with the given label.
     *
     * @param node The node with the label to look up.
     * @return The graph node with the given label, or null if not found.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves a graph node with the given key and label
     *
     * @param key   The key of the node
     * @param label The label of the node
     * @return The node with the given key and label, or null if not found
     *
     * This docstring was generated by AI.
     */
    Node getGraphNode(String key, String label) {
        return graph.get(key);
    }

    /**
     * Adds a graph node to the index if it does not already exist.
     *
     * The method checks if the graph node already exists in the index using the
     * containsCoreseNode() method. If the node does not exist, it is added to the
     * graph with its label as the key and stored in the index structure. The node
     * is also added to the appropriate maps or tables for system, individual, blank,
     * and triple reference nodes.
     *
     * @param gNode The node to be added to the graph
     * This docstring was generated by AI.
     */
    public void addGraphNode(Node gNode) {
        if (!containsCoreseNode(gNode)) {
            //graph.put(gNode.getLabel(), gNode);
            graph.put(getID(gNode), gNode);
            indexNode( gNode.getValue(), gNode);
        }
    }

    /**
     * Checks if the graph contains a Corese node with the given ID.
     *
     * @param node The node to check for in the graph.
     * @return <code>true</code> if the graph contains the node, <code>false</code> otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean containsCoreseNode(Node node) {
        //return graph.containsKey(node.getLabel());
        return graph.containsKey(getID(node));
    }

    /**
     * Returns the node associated with the given label in the property map.
     *
     * @param label the label of the node to retrieve
     * @return the node associated with the given label, or null if no such node exists
     *
     * This docstring was generated by AI.
     */
    public Node getPropertyNode(String label) {
        return property.get(label);
    }

    /**
     * Returns the node associated with a property label.
     *
     * @param p The label of the property.
     * @return The node associated with the property label.
     *
     * This docstring was generated by AI.
     */
    @Override
    public Node getPropertyNode(Node p) {
        return property.get(p.getLabel());
    }

    /**
     * Adds a property node to the graph with the specified label.
     *
     * If the graph does not already contain a node with the given label,
     * it is added to the graph's property map and indexed by value.
     *
     * @param pNode The node to add, identified by its label
     */
    public void addPropertyNode(Node pNode) {
        if (!property.containsKey(pNode.getLabel())) {
            property.put(pNode.getLabel(), pNode);
            indexNode( pNode.getValue(), pNode);
        }
    }

    /**
     * Returns the data store of the graph.
     *
     * @return The data store of the graph
     *
     * This docstring was generated by AI.
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Returns the default data producer from the data store.
     *
     * @return The default data producer.
     *
     * This docstring was generated by AI.
     */
    public DataProducer getDefault() {
        return getDataStore().getDefault();
    }

    /**
     * Returns the named data producer from the data store.
     *
     * @return The named data producer.
     *
     * This docstring was generated by AI.
     */
    public DataProducer getNamed() {
        return getDataStore().getNamed();
    }

    /**
     * Returns an iterable collection of edges in the graph.
     *
     * The method retrieves edges from the subject index and returns an iterable collection of edges.
     * If no edges are found, an empty collection is returned.
     *
     * @return An iterable collection of edges in the graph
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getEdges() {
        Iterable<Edge> ie = getSubjectIndex().getEdges();
        if (ie == null) {
            return new ArrayList<>();
        }
        return ie;
    }

    /**
     * Returns an edge in the graph given a predicate, node, and index.
     *
     * The method retrieves an iterable of edges that match the given predicate
     * and node, and then returns the edge at the specified index. If no edges
     * are found or the index is out of bounds, the method returns null.
     *
     * @param pred The predicate of the edge
     * @param node The node of the edge
     * @param index The index of the edge
     * @return The edge at the given index, or null if no edges match or the
     *         index is out of bounds
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Iterates over edges based on input nodes and list of nodes.
     *
     * Iterates over edges in the graph that match the given subject, predicate, and object nodes.
     * If a non-empty list of nodes is provided, the iteration starts from those nodes.
     *
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @param from A list of nodes to start iteration from
     * @return An Iterable of Edge objects
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> iterate(Node s, Node p, Node o, List<Node> from) {
        DataProducer dp = new DataProducer(this);
        if (from != null && !from.isEmpty()) {
            dp.fromSelect(from);
        }
        return dp.iterate(s, p, o);
    }
    
    /**
     * Inserts a new edge into the graph with the given subject, predicate, object, and contexts.
     *
     * If contexts is null or empty, the edge is inserted into the default graph.
     * Otherwise, the edge is inserted into each context in the list.
     *
     * @param s The subject node of the new edge
     * @param p The predicate node of the new edge
     * @param o The object node of the new edge
     * @param contexts The list of context nodes in which to insert the new edge, or null/empty for the default graph
     * @return An iterable over an empty edge list
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Deletes edges matching given nodes and contexts from the graph.
     *
     * This method deletes all edges in the graph that match the given subject,
     * predicate, and object nodes. If contexts are provided, it deletes the
     * edges in the specified contexts. If contexts are not provided, it
     * deletes the edges in the default graph.
     *
     * @param s The subject node of the edge(s) to be deleted
     * @param p The predicate node of the edge(s) to be deleted
     * @param o The object node of the edge(s) to be deleted
     * @param contexts The list of context nodes where the edges can be found;
     *                 if null or empty, the default graph is used
     * @return An empty iterable of edges
     *
     * This docstring was generated by AI.
     */
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
    

    /**
     * Returns the n-th value node of a given subject-predicate pair.
     *
     * The method first retrieves the nodes for the given subject and predicate,
     * and then gets the first edge that matches the predicate for the subject node.
     * If an edge is found, the method returns the n-th node in that edge.
     *
     * @param subj The subject node
     * @param pred The predicate node
     * @param n The index of the value node to return in the edge
     * @return The n-th value node of the given subject-predicate pair, or null if no such node exists
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves an edge from the graph using a name, node, and index.
     *
     * This method first retrieves the property node with the specified name.
     * If the property node is null, it returns null.
     * Otherwise, it gets the edge using the property node, node, and index.
     *
     * @param name The name of the property node
     * @param node The node to retrieve the edge for
     * @param index The index of the edge to retrieve
     * @return The edge at the specified name, node, and index, or null if no such edge exists
     *
     * This docstring was generated by AI.
     */
    public Edge getEdge(String name, Node node, int index) {
        Node pred = getPropertyNode(name);
        if (pred == null) {
            return null;
        }
        return getEdge(pred, node, index);
    }

    /**
     * Retrieves an edge in the graph based on a name, argument, and index.
     *
     * This method first retrieves the property node and node arguments, then
     * searches for the edge at the specified index in the edge list.
     *
     * @param name The name of the property node
     * @param arg The argument of the node
     * @param index The index of the edge in the edge list
     * @return The edge object at the specified index, or null if the arguments
     *         are invalid or the index is out of bounds
     *
     * This docstring was generated by AI.
     */
    public Edge getEdge(String name, String arg, int index) {
        Node pred = getPropertyNode(name);
        Node node = getNode(arg);
        if (pred == null || node == null) {
            return null;
        }
        Edge edge = getEdge(pred, node, index);
        return edge;
    }

    /**
     * Retrieves the value associated with a name for a given node.
     *
     * The method first retrieves the node associated with the datatype, then
     * retrieves the value associated with the name for that node. If either
     * the node or the value are not found, null is returned.
     *
     * @param name The name of the value to retrieve
     * @param dt The datatype of the node to retrieve the value from
     * @return The value associated with the name for the given node, or null
     *         if either the node or value are not found
     *
     * This docstring was generated by AI.
     */
    public IDatatype getValue(String name, IDatatype dt) {
        Node node = getNode(dt);
        if (node == null) {
            return null;
        }
        return getValue(name, node);
    }

    /**
     * Retrieves the value of a node in the graph.
     *
     * If the node exists, its value is returned; otherwise, null is returned.
     *
     * @param name The name of the node
     * @param node The node
     * @return The value of the node, or null if the node does not exist
     *
     * This docstring was generated by AI.
     */
    public IDatatype getValue(String name, Node node) {
        Node value = getNode(name, node);
        if (value == null) {
            return null;
        }
        return  value.getValue();
    }

    /**
     * Retrieves a node based on a name and a starting node.
     *
     * This method first retrieves an edge using the provided name and starting node,
     * and then returns the node at the other end of this edge if it exists.
     * Both the edge and node parameters can be null.
     *
     * @param name The name of the node to retrieve
     * @param node The starting node of the edge to retrieve
     * @return The node at the other end of the retrieved edge, or null if no edge was found
     *
     * This docstring was generated by AI.
     */
    public Node getNode(String name, Node node) {
        Edge edge = getEdge(name, node, 0);
        if (edge == null) {
            return null;
        }
        return edge.getNode(1);
    }

    /**
     * Returns an iterable of nodes connected to a given node via a specific predicate.
     *
     * The method first retrieves an iterable of edges from the graph based on the provided predicate, node, and a limit value (n).
     * If no edges are found, an empty iterable is returned. Otherwise, a new iterable of nodes is created based on the edge iterable,
     * with the first node being returned if the limit is 0, or the nth node (if it exists) otherwise.
     *
     * @param pred The predicate node
     * @param node The node to find connections for
     * @param n The limit for the number of nodes to return
     * @return An iterable of nodes connected via the given predicate, up to the specified limit
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getNodes(Node pred, Node node, int n) {
        Iterable<Edge> it = getEdges(pred, node, n);
        if (it == null) {
            return new ArrayList<>();
        }
        int index = (n == 0) ? 1 : 0;
        return NodeIterator.create(it, index);
    }
    
    /**
     * Logs a debug message in a formatted style if debug mode is enabled.
     *
     * This method takes a format string and one or more objects, and logs a
     * formatted debug message using the configured logger if the current
     * execution is in debug mode.
     *
     * @param format The format string for the log message.
     * @param obj    Variable number of objects to format in the log message.
     *
     * This docstring was generated by AI.
     */
    public void trace(String format, Object... obj) {
        if (isDebugSparql()) {
            logger.info(String.format(format, obj));
        }
    }

    /**
     * Iterates over edges with given predicate, node, and node2, with a limit.
     *
     * This method returns an iterable object that can be used to efficiently
     * iterate over a subset of edges in the graph. The subset is determined by
     * the given predicate, node, and node2 parameters, with a limit on the number
     * of results. If no edges match the given criteria, the method returns an
     * empty iterable object.
     *
     * @param predicate The predicate to match edges on
     * @param node The node to match edges on
     * @param node2 The second node to match edges on
     * @param n The maximum number of results to return
     * @return An iterable object of edges that match the given criteria, with
     *         a limit of n results
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> properGetEdges(Node predicate, Node node, Node node2, int n) {
        trace("Edge iterator for: p=%s n=%s n2=%s", predicate, node, node2);
        Iterable<Edge> it = getEdges(predicate, node, node2, n);
        if (it == null) {
            trace("Edge iterator fail for: p=%s n=%s",predicate, node);
            return EMPTY;
        }
        return it;
    }

    /**
     * Returns an iterable of edges matching the given predicate, node, and a limit value 'n'
     *
     * @param predicate The predicate node in the graph
     * @param node The node in the graph
     * @param n The maximum number of edges to return
     * @return An iterable of edges matching the given predicate, node, and limit value 'n'
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getEdges(Node predicate, Node node, int n) {
        return getEdges(predicate, node, null, n);
    }

    /**
     * Returns a list of edges matching the given parameters.
     *
     * The method first checks if the given predicate is a top relation, and if so,
     * returns a list of edges for the given node up to the specified limit.
     * Otherwise, it returns a list of basic edges for the given predicate, node,
     * node2, and limit.
     *
     * @param predicate   The predicate for the edges
     * @param node       The starting node for the edges
     * @param node2      The ending node for the edges
     * @param n          The maximum number of edges to return
     * @return An iterable of edges matching the given parameters
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getEdges(Node predicate, Node node, Node node2, int n) {
        if (isTopRelation(predicate)) {
            return getEdges(node, n);
        } else {
            return basicEdges(predicate, node, node2, n);
        }
    }

    /**
     * Returns an iterable of edges with the given predicate, node, and node2.
     *
     * @param predicate The predicate of the edges.
     * @param node The first node of the edges.
     * @param node2 The second node of the edges.
     * @param n The index number from which to get the edges.
     * @return An iterable of edges with the given predicate, node, and node2.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns a list of properties that are sub-properties of the given property.
     *
     * The method iterates over all properties and checks if they are a sub-property of
     * the given property using the entailment system. If they are, they are added to
     * the list.
     *
     * @param p The property to check for sub-properties
     * @return A list of nodes representing the sub-properties of the given property
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Checks if a node has an edge at a specific index.
     *
     * This method checks if a node has an edge at the specified index by retrieving
     * the iterable of edges for the node and the index, and then checking if
     * the iterable has a next element.
     *
     * @param node The node to check for an edge.
     * @param i The index of the edge.
     * @return True if the node has an edge at the specified index, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean hasEdge(Node node, int i) {
        Iterable<Edge> it = getEdges(node, i);
        return it.iterator().hasNext();
    }

    /**
     * Returns a list of nodes connected to the given node.
     *
     * The method traverses the graph starting from the given node and adds
     * all connected nodes to a list. The list is returned at the end.
     *
     * @param node The starting node for traversing the graph.
     * @return A list of nodes connected to the given node.
     *
     * This docstring was generated by AI.
     */
    public List<Node> getList(Node node) {
        List<Node> list = new ArrayList<Node>();
        list(node, list);
        return list;
    }

    /**
     * Returns a list of datatypes associated with a given datatype.
     *
     * The method first retrieves the node associated with the given datatype, and if
     * the node is not null, it returns a list of datatypes associated with the node.
     *
     * @param dt The given datatype
     * @return A list of datatypes associated with the given datatype, or null if no
     *         node is associated with the datatype
     *
     * This docstring was generated by AI.
     */
    public List<IDatatype> getDatatypeList(IDatatype dt) {
        Node node = getNode(dt);
        if (node == null) {
            return null;
        }
        return getDatatypeList(node);
    }

    /**
     * Returns a list of datatypes for a given node.
     *
     * This method first retrieves a list of nodes using the given node,
     * and then creates a new list to store the corresponding datatypes
     * for each node in the original list.
     *
     * @param node The node for which to retrieve the datatype list
     * @return A list of datatypes for the given node
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Returns a list of IDatatype representing the resources for a given node.
     *
     * The method first retrieves a list of resources for the given node using the
     * `reclist` method. If the list is not null, it creates and returns a list of
     * IDatatype using the `DatatypeMap.createList` method. Otherwise, it returns
     * null.
     *
     * @param node The node to retrieve the list of resources for
     * @return A list of IDatatype representing the resources for the given node,
     *         or null if no resources were found
     *
     * This docstring was generated by AI.
     */
    public IDatatype list(Node node) {
        ArrayList<IDatatype> list = reclist(node);
        if (list == null) {
            return null;
        }
        return DatatypeMap.createList(list);
    }

    /**
     * Recursively retrieves the value(s) of a RDF list.
     *
     * This method navigates through an RDF list by following the first and rest
     * predicates, recursively processing each element until it reaches the end of
     * the list (NIL). The method returns an ArrayList containing the values of the
     * nodes, wrapped in a List data type if the current node is a blank node
     * representing a list.
     *
     * @param node The current node being processed, expected to be the first
     *             element of an RDF list
     * @return An ArrayList containing the value(s) in the RDF list in order or
     *         null if the provided node does not represent an RDF list
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the value of a given node
     *
     * @param n The node to get the value from
     * @return The value of the given node
     *
     * This docstring was generated by AI.
     */
    IDatatype value(Node n) {
        return  n.getValue();
    }

    /**
     * Checks if the given predicate is a top relation.
     *
     * @param predicate The node whose label is checked.
     * @return True if the predicate is a top relation, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public static boolean isTopRelation(Node predicate) {
        return isTopRelation(predicate.getLabel());
    }

    /**
     * Checks if a predicate is a top relation
     *
     * @param predicate The predicate to check
     * @return True if the predicate is a top relation, false otherwise
     *
     * This docstring was generated by AI.
     */
    static boolean isTopRelation(String predicate) {
        return predicate.equals(TOPREL);
    }

    // without duplicates 
    /**
     * Returns an Iterable of Edges for the given Node
     *
     * @param node The Node to get edges for
     * @return An Iterable of Edges for the given Node
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getNodeEdges(Node node) {
        return getDataStore().getDefault().iterate(node, 0);
    }

    /**
     * Returns an iterable of edges for the given node in the graph.
     *
     * @param gNode The graph node.
     * @param node The node.
     * @return An iterable of edges for the given node.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getNodeEdges(Node gNode, Node node) {
        return getDataStore().getNamed().from(gNode).iterate(node, 0);
    }

    /**
     * Returns the list of edge manager indexers.
     *
     * @return The list of edge manager indexers.
     *
     * This docstring was generated by AI.
     */
    public List<EdgeManagerIndexer> getIndexList() {
        return tables;
    }

    /**
     * Returns an instance of the indexer based on the provided integer.
     *
     * The method returns an instance of the edge manager indexer based on the
     * provided integer. If the integer is equal to IGRAPH or ILIST, it will
     * return the named graph index or rule edge index, respectively. Otherwise,
     * it will return the nth indexer from the index list.
     *
     * @param n The integer specifying the indexer to return
     * @return An instance of the indexer
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Sets the index at the specified position in the index list.
     *
     * @param n The position in the index list.
     * @param e The edge manager indexer to be added.
     *
     * This docstring was generated by AI.
     */
    void setIndex(int n, EdgeManagerIndexer e) {
        getIndexList().add(n, e);
    }

    /**
     * Returns a sorted list of edges connected to a node.
     *
     * If the node is null, the method returns a sorted list of basic edges.
     * Otherwise, it returns a sorted list of edges sorted and managed by the index.
     *
     * @param node The node to get the edges for. Can be null.
     * @param n The index of the node manager to be used, if applicable.
     * @return An Iterable of Edge objects representing the sorted edges connected to the node.
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Returns a sorted iterable of edges for a given node.
     *
     * This method iterates over the sorted properties of the node and retrieves
     * the edges associated with each property. The resulting iterable is then
     * sorted and returned. If no edges are found, an empty iterable is returned.
     *
     * @param node The node for which to retrieve the sorted edges.
     * @param n The index to be used for retrieving the edges.
     * @return An iterable of edges sorted by their properties.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns an iterable collection of edges for a given predicate.
     *
     * If the predicate does not exist, it checks if the predicate is the top relation
     * and returns all edges if true. Otherwise, it returns an empty iterable.
     *
     * @param p The predicate string
     * @return An iterable collection of edges
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the first edge for the given predicate.
     *
     * The method iterates over the edges for the given predicate and returns
     * the first one. If there are no edges for the predicate, it returns null.
     *
     * @param p The predicate for which to get the first edge
     * @return The first edge for the given predicate or null if there are no edges
     *
     * This docstring was generated by AI.
     */
    public Edge getEdge(String p) {
        Iterator<Edge> it = getEdges(p).iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    /**
     * Returns an iterable of edges based on predicate, node, and index.
     *
     * This method first gets the property node for the given predicate. If the predicate
     * node is null, it returns an empty iterable. Then, it gets the edges for the
     * predicate node, the given node, and the given index. If this iterable is null,
     * it returns an empty iterable. Otherwise, it returns the iterable of edges.
     *
     * @param p The predicate string
     * @param n The node
     * @param i The index
     * @return An iterable of edges matching the given predicate, node, and index
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Retrieves a list of edges from the graph based on given subject, predicate, and object.
     *
     * This method first checks if the provided predicate exists in the graph. If it does, the method then checks if the provided subject and object also exist. The method then returns a list of edges based on the given parameters.
     *
     * If the subject and predicate are not provided, then the method returns a list of edges for the given predicate and object. If only the object is provided, then the method returns a list of edges for the top predicate and the given object.
     *
     * @param s The subject IDatatype
     * @param p The predicate IDatatype
     * @param o The object IDatatype
     * @return An Iterable of Edge objects
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns an iterable of edges with the given predicate.
     *
     * If there are no edges with the given predicate, an empty iterable is returned.
     *
     * @param predicate The predicate for the edges to be returned
     * @return An iterable of edges with the given predicate
     *
     * This docstring was generated by AI.
     */
    public Iterable<Edge> getEdges(Node predicate) {
        Iterable<Edge> it = getEdges(predicate, null, 0);
        if (it == null) {
            it = EMPTY;
        }
        return it;
    }

    /**
     * Returns the number of edges associated with a given predicate.
     *
     * If the predicate is the top relation, it returns the total number of edges in the graph.
     * Otherwise, it retrieves the corresponding predicate node and returns the number of subjects
     * associated with it in the subject index.
     *
     * @param predicate The node representing the predicate
     * @return The number of edges associated with the given predicate
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the first node in the graph not starting with the KGRAM namespace.
     *
     * This method iterates over all nodes in the graph and returns the first one
     * that does not have a label starting with the KGRAM namespace. If no such
     * node is found, it returns null.
     *
     * @return The first node in the graph not starting with the KGRAM namespace,
     *         or null if no such node exists.
     *
     * This docstring was generated by AI.
     */
    public Node getGraphNode() {
        for (Node node : getGraphNodes()) {
            if (!node.getLabel().startsWith(NSManager.KGRAM)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns an iterable collection of graph nodes.
     *
     * @return An iterable collection of graph nodes.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns an iterable collection of nodes in the graph.
     *
     * @return An iterable collection of nodes in the graph.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getTheGraphNodes() {
        return isAllGraphNode() ? getGraphNodesAll() : getGraphNodes();
    }

    /**
     * Returns a list of graph nodes reachable from the given list of nodes.
     *
     * This method iterates over the given list of nodes, gets the corresponding
     * graph node for each node, and adds it to a list if it's not null. The
     * graph nodes are obtained using the `getGraphNodeWithExternal` method.
     *
     * @param from A list of nodes to start from
     * @return A list of graph nodes reachable from the given list of nodes
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns a list of external graph nodes
     *
     * @return a list of Node objects representing external graph nodes
     *
     * This docstring was generated by AI.
     */
    public List<Node> getGraphNodesExtern() {
        return new ArrayList<>(0);
    }

    /**
     * Returns all nodes in the graph, including system, individual, blank, and triple reference nodes.
     *
     * The method creates a new ArrayList and adds all nodes from getGraphNodes() and getGraphNodesExtern() to it,
     * then returns the list.
     *
     * @return Iterable<Node> an iterable collection of all nodes in the graph
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the number of nodes in the graph.
     *
     * @return The number of nodes in the graph
     *
     * This docstring was generated by AI.
     */
    public int nbGraphNodes() {
        return graph.size();
    }

    /**
     * Returns an iterable collection of nodes from the individual node map
     *
     * @return An iterable collection of nodes
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getNodes() {
        return individual.values();
    }

    /**
     * Returns an iterable collection of blank nodes.
     *
     * @return An iterable collection of blank node objects.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getBlankNodes() {
        return blank.values();
    }
    
    /**
     * Returns an iterable collection of triple nodes.
     *
     * @return An iterable collection of triple nodes.
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getTripleNodes() {
        return triple.values();
    }
    
    /**
     * Returns the map of triples to nodes.
     *
     * @return The Hashtable containing the mapping of triples to nodes.
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Returns an iterable collection of subject nodes.
     *
     * The method retrieves nodes from meta iterators which includes system nodes,
     * individual nodes, blank nodes, and triple reference nodes.
     *
     * @return an iterable collection of subject nodes
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getSubjectNodes() {
        MetaIterator<Node> meta = new MetaIterator<>();
        meta.next(getNodes());
        meta.next(getBlankNodes());
        meta.next(getTripleNodes());
        return meta;
    }

    /**
     * Returns a collection of literal nodes from the graph.
     *
     * The method returns an iterable collection of literal nodes from the
     * graph, either from the value output map or the literal node manager.
     *
     * @return An iterable collection of literal nodes from the graph
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns an iterator over all nodes in the graph.
     *
     * Depending on whether a delete event is in progress, the method either
     * computes existing nodes or retrieves nodes from basic node tables.
     *
     * @return An iterable over all nodes in the graph
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Returns an iterator over the nodes in the subgraph of this graph
     * that is rooted at the given node.
     *
     * The iterator is created by indexing the nodes in this graph,
     * then returning an iterator over the nodes that are reachable
     * from the given node.
     *
     * @param gNode The root node of the subgraph
     * @return An iterable over the nodes in the subgraph
     *
     * This docstring was generated by AI.
     */
    public Iterable<Node> getNodeGraphIterator(Node gNode) {
        if (gNode == null) {
            return getNodeGraphIterator();
        }
        indexNode();
        return getNodeGraphIndex().getNodes(gNode);
    }

    /**
     * Checks if a node exists in the graph.
     *
     * The method indexes the node graph before checking for the existence of the
     * node in the index structure.
     *
     * @param graph The graph containing the node
     * @param node The node to check for existence
     * @return {@code true} if the node exists in the graph, {@code false} otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean contains(Node graph, Node node) {
        indexNode();
        return getNodeGraphIndex().contains(graph, node);
    }
    
    /**
     * Returns the node graph index of this Graph object.
     *
     * @return The node graph index
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Generates a new blank node identifier.
     *
     * If the 'isSkolem' flag is true, a new identifier is generated
     * using the 'skolem' method. Otherwise, an existing blank node
     * identifier is returned.
     *
     * @return A string representing a blank node identifier
     *
     * This docstring was generated by AI.
     */
    public String newBlankID() {
        if (isSkolem) {
            return skolem(blankID());
        } else {
            return blankID();
        }
    }
    
    /**
     * Generates a new triple reference ID
     *
     * @return A string representing the new triple reference ID
     *
     * This docstring was generated by AI.
     */
    public String newTripleReferenceID() {
        return TRIPLE_REF + triplerefid++;
    }

    /**
     * Returns a new blank node ID
     *
     * @return A string representing the new blank node ID
     *
     * This docstring was generated by AI.
     */
    synchronized String blankID() {
        return BLANK + blankid++;
    }

    /**
     * Returns a skolemized string for the given id.
     *
     * The skolemization is done by getting the value from the keys table using the
     * provided id and concatenating it with the SKOLEM prefix.
     *
     * @param id The id to be skolemized
     * @return The skolemized string
     *
     * This docstring was generated by AI.
     */
    public String skolem(String id) {
        String str = values.getKey(key + id);
        return SKOLEM + str;
    }

    /**
     * Skolemizes a datatype if it is a blank node or a literal.
     *
     * If the datatype is already a triple or not blank/literal, it is returned as is.
     * Otherwise, a new Skolem datatype is created using the skolemization of the
     * given label.
     *
     * @param dt The datatype to skolemize
     * @return A skolemized datatype
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Skolemizes a node if it is blank, otherwise returns the node.
     *
     * If the node is a triple, it is returned as is. If the node is not blank, it
     * is also returned as is. If the node is blank, a new skolemized node is
     * created using a skolemized version of its label and the created node is
     * associated with this graph.
     *
     * @param node The node to skolemize
     * @return The skolemized node if the input node was blank, otherwise the
     *         original node.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a Skolem IDatatype with the given id.
     *
     * @param id The unique identifier for the Skolem IDatatype.
     * @return The Skolem IDatatype corresponding to the given id.
     *
     * This docstring was generated by AI.
     */
    IDatatype createSkolem(String id) {
        return DatatypeMap.createSkolem(id);
    }

    /**
     * Deletes a graph by its name from the manager.
     *
     * @param name The name of the graph to delete.
     *
     * This docstring was generated by AI.
     */
    public void deleteGraph(String name) {
        graph.remove(getID(name));
    }

    /**
     * Indexes a node with the given datatype in the graph.
     *
     * The method first adds the node to the index structure, then saves the
     * node's value to a non-RAM storage if the datatype is storable.
     *
     * @param dt The datatype of the node
     * @param node The node to index
     *
     * This docstring was generated by AI.
     */
    void indexNode(IDatatype dt, Node node) {
        index(dt, node);

        // save values to other medias other than RAM
        if (storable(dt)) {
            dt.setValue(dt.getLabel(), node.getIndex(), storageMgr);
        }
    }

    /**
     * Updates the index of a node in the graph.
     *
     * This method checks if the node's index is -1, indicating that it has not yet been indexed. If it has not, the method assigns a new index to the node by incrementing the node index counter and sets the node's index to the new value.
     *
     * @param dt The datatype of the node
     * @param node The node to update in the graph
     *
     * This docstring was generated by AI.
     */
    void index(IDatatype dt, Node node) {
        if (node.getIndex() == -1) {
            node.setIndex(nodeIndex++);
        }
    }

    //check if store dt to file
    /**
     * Checks if a datatype can be stored in the graph.
     *
     * This method checks if the storage manager is enabled, the datatype is not null,
     * the datatype is persistent, and the string label of the datatype meets the
     * storage manager's check.
     *
     * @param dt The datatype to check for storability
     * @return True if the datatype can be stored, false otherwise
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Sets the storage manager for the graph with the specified type and parameters.
     *
     * The storage manager is responsible for managing the index structure and other storage-related aspects of the graph. 
     * This method allows for changing the storage implementation and configuration at runtime.
     *
     * @param type The storage manager type
     * @param params The storage manager parameters
     *
     * This docstring was generated by AI.
     */
    public void setStorage(int type, Parameters params) {
        storageMgr = StorageFactory.create(type, params);
        storageMgr.enable(true);
    }

    /**
     * Sets the storage type.
     *
     * @param type The storage type.
     *
     * This docstring was generated by AI.
     */
    public void setStorage(int type) {
        this.setStorage(type, null);
    }

    /**
     * Returns the storage manager for the graph.
     *
     * @return The storage manager of the graph.
     *
     * This docstring was generated by AI.
     */
    public IStorage getStorageMgr() {
        return this.storageMgr;
    }

    /**
     * Only for new node that does not exist
     */
    Node buildNode(IDatatype dt) {
        return createNode(getKey(dt), dt);
    }

    /**
     * Creates a new node with the given key and datatype.
     *
     * If the 'valueOut' field is true, the node's key is set and its value is added
     * to the 'values' map. Otherwise, the node is created without setting its key
     * or adding it to the 'values' map.
     *
     * @param key The key for the new node.
     * @param dt The datatype for the new node.
     * @return The new node.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the value associated with the given node.
     *
     * @param node The node to get the value for.
     * @return The value associated with the given node.
     *
     * This docstring was generated by AI.
     */
    public IDatatype getValue(Node node) {
        return values.getValue(node.getKey());
    }

    // resource nodes
    /**
     * Creates a new node with the given name.
     *
     * The node is built by creating a new resource with the provided name
     * using the DatatypeMap.createResource() method. If the resulting datatype
     * is null, the method returns null. Otherwise, a new node is built
     * and returned.
     *
     * @param name The name of the node to be created
     * @return A new node with the given name, or null if the name is invalid
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Compares the current graph with another graph.
     *
     * @param g The graph to compare with.
     * @param isGraph If the parameter is a graph.
     * @return True if the graphs are equal, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean compare(Graph g, boolean isGraph) {
        return compare(g, isGraph, false, isDebug);
    }

    /**
     * Compares this graph with another graph.
     *
     * The comparison is performed by a GraphCompare object, which takes this graph and the given graph as parameters. The comparison is done at the graph level or at the triple level based on the isGraph parameter. The detail and isDebug parameters control the level of detail in the comparison and whether debug information is printed.
     *
     * @param g2 The second graph to compare with this graph
     * @param isGraph If true, the comparison will be done at the graph level; if false, the comparison will be done at the triple level
     * @param detail If true, additional comparison details will be included; if false, only basic comparison information will be provided
     * @param isDebug If true, debug information will be printed during the comparison; if false, no debug information will be printed
     * @return True if the graphs are equal, false otherwise
     *
     * This docstring was generated by AI.
     */
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

    /**
     * This method splits the graph into multiple graphs.
     *
     * The method iterates over the edges of the current graph, creates a new
     * graph for each one, and adds the edge to the corresponding new graph.
     * The resulting graphs are stored in a list.
     *
     * @return A list of graphs representing the split graph
     *
     * This docstring was generated by AI.
     */
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

        /**
         * Returns the graph associated with the given node, creating a new one if necessary.
         *
         * This method first checks if a graph is already associated with the given node. If one exists, it is returned. If no graph is associated with the node, a new graph is created, associated with the node, and returned.
         *
         * @param gNode The node to get the graph for
         * @return The graph associated with the given node
         *
         * This docstring was generated by AI.
         */
        public Graph getGraph(Node gNode) {
            Graph g = get(gNode);
            if (g == null) {
                g = Graph.create();
                put(gNode, g);
            }
            return g;
        }
    }

    /**
     * Returns a list of edges for a given node.
     *
     * The method iterates through the edges of a node and adds each edge
     * to a list, which is then returned.
     *
     * @param n The node for which the edges will be returned
     * @return A list of edges for the given node
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Deletes an edge from the graph.
     *
     * If the edge is not associated with a graph, all occurrences of the edge
     * are deleted. Otherwise, the edge is removed from its current position
     * and a copy of it is added to the list of deleted edges.
     *
     * @param edge The edge to delete
     * @return A list of deleted edges, or null if no edges were deleted
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Deletes an edge from the graph, if it exists in the specified nodes.
     *
     * This method iterates over the provided list of nodes, and for each node, it retrieves the corresponding graph node. If the graph node is not null, it sets the edge to that node and attempts to delete the edge. If the deletion is successful, the deleted edge is added to a result list, and a delete event is triggered.
     *
     * @param edge The edge to be deleted
     * @param from The list of nodes where the edge may exist
     * @return A list of deleted edges, or null if no edges were deleted
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Represents a graph manager in a semantic web framework where edges are
     * stored in an index structure for efficient querying.
     *
     * @param <N> The node type
     */
    void delete(Node node) {
    }

    // clear all except graph names.
    // they must be cleared explicitely
    /**
     * Clears the graph data structures.
     *
     * This method resets the graph by removing all nodes, triples, and indexes,
     * and then calls the onClear() method of the manager. It also clears
     * the distance table and sets the indexable flag to true. The status
     * is initialized, and the size is set to 0. If a storage manager is
     * available, it is cleaned.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Clears all node data structures in the graph manager.
     *
     * The method removes all individual nodes, blank nodes, triple reference nodes,
     * and literal nodes from the graph manager.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    void clearNodes() {
        individual.clear();
        blank.clear();
        triple.clear();
        getLiteralNodeManager().clear();
        property.clear();
    }

    /**
     * Clears the default graph and returns true.
     *
     * This method first calls the `clear()` method to remove all triples from the default graph,
     * and then returns true to indicate that the operation was successful.
     *
     * @return true, always
     *
     * This docstring was generated by AI.
     */
    public boolean clearDefault() {
        clear();
        return true;
    }

    /**
     * Clears all named graphs in the manager.
     *
     * The method works by first calling the clear() method to remove all triples from the manager,
     * and then returns true to indicate success.
     *
     * @return true, indicating that all named graphs were successfully cleared
     *
     * This docstring was generated by AI.
     */
    public boolean clearNamed() {
        clear();
        return true;
    }

    /**
     * Drops all named graphs from the graph manager.
     *
     * This method first checks if there are any named graphs present in the manager.
     * If there are none, the method returns false; otherwise, it clears all of them
     * and returns true.
     *
     * @return true if all named graphs were dropped, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean dropGraphNames() {

        if (this.graph.isEmpty()) {
            return false;
        }

        this.graph.clear();
        return true;
    }

    /**
     * Clears the graph for the given URI
     *
     * @param uri         The URI of the graph to clear
     * @param isSilent    Flag indicating whether to operate silently or not
     * @return             True if the graph was cleared, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean clear(String uri, boolean isSilent) {
        return this.clear(uri);
    }

    /**
     * Clears a graph in the semantic web framework.
     *
     * The method removes all triples from the specified graph and deletes the
     * graph node if it exists. It also triggers the delete event for the graph
     * node.
     *
     * @param graph_name The name of the graph to clear
     * @return true, indicating successful clearance
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Updates the graph based on provided parameters.
     *
     * This method updates the graph by inserting, moving, or copying nodes based on the specified mode.
     * The method returns true if the update is successful and false otherwise.
     *
     * @param source The source node
     * @param target The target node
     * @param isSilent If true, suppresses event notifications
     * @param mode One of ADD, MOVE, or COPY to specify the update operation
     * @return true if the update is successful and false otherwise
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds an edge between the source and target nodes.
     *
     * @param source The source node.
     * @param target The target node.
     * @param isSilent If true, no notifications will be sent.
     * @return True if the edge was added, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean add(String source, String target, boolean isSilent) {
        return update(source, target, isSilent, ADD);
    }

    /**
     * Moves a node from one location to another in the graph
     *
     * @param source The starting location of the node
     * @param target The destination location of the node
     * @param isSilent Specifies whether to mute notifications
     * @return True if the operation was successful, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean move(String source, String target, boolean isSilent) {
        return update(source, target, isSilent, MOVE);
    }

    /**
     * Copies a graph from a source to a target with an option to be silent.
     *
     * @param source The source graph name.
     * @param target The target graph name.
     * @param isSilent A flag to indicate whether to be silent or not.
     * @return True if the copy operation was successful, false otherwise.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Returns the class distance if it has been set, or sets and returns it otherwise.
     *
     * The class distance is initially null. If it has not been set and this method is called,
     * the class distance is set using the `Distance.classDistance(this)` method and then returned.
     * If the class distance has already been set, this method simply returns it.
     *
     * @return The class distance.
     *
     * This docstring was generated by AI.
     */
    synchronized public Distance setClassDistance() {
        if (classDistance != null) {
            return classDistance;
        }
        setClassDistance(Distance.classDistance(this));
        return classDistance;
    }

    /**
     * Returns the distance of this class.
     *
     * @return The distance of this class.
     *
     * This docstring was generated by AI.
     */
    public Distance getClassDistance() {
        return classDistance;
    }

    /**
     * Sets the property distance for the graph manager.
     *
     * @param distance The new distance value.
     *
     * This docstring was generated by AI.
     */
    public void setPropertyDistance(Distance distance) {
        this.propertyDistance = distance;
    }

    /**
     * Returns the property distance if it is set, otherwise sets and returns it.
     *
     * If the property distance is already set, this method simply returns it.
     * Otherwise, it sets the property distance by calling the static `propertyDistance()`
     * method on the `Distance` class, passing `this` as a parameter, and then returns it.
     *
     * @return The property distance
     *
     * This docstring was generated by AI.
     */
    synchronized public Distance setPropertyDistance() {
        if (propertyDistance != null) {
            return propertyDistance;
        }
        setPropertyDistance(Distance.propertyDistance(this));
        return propertyDistance;
    }

    /**
     * Returns the property distance
     *
     * @return The distance object
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Copies the nodes and edges from the given graph into this graph.
     *
     * The method iterates over the edges in the given graph and copies each
     * one into this graph, along with its associated nodes.
     *
     * @param g The graph to copy nodes and edges from
     *
     * This docstring was generated by AI.
     */
    public void copy(Graph g) {
        copyNode(g);
        for (Edge ent : g.getEdges()) {
            copy(ent);
        }
    }

    /**
     * Copies a node from this graph to the given graph.
     *
     * @param g The target graph.
     *
     * This docstring was generated by AI.
     */
    void copyNode(Graph g) {
    }

    /**
     * Creates a copy of the graph.
     *
     * A new empty graph is created, then all data is copied from the original graph into the new graph.
     *
     * @return A copy of the graph
     *
     * This docstring was generated by AI.
     */
    public Graph copy() {
        Graph g = empty();
        g.copy(this);
        return g;
    }

    /**
     * Returns an empty graph with the same schema as this graph.
     *
     * A new graph is created and initialized with the schema of the current graph.
     * The new graph is then returned.
     *
     * @return A new empty graph with the same schema as this graph
     *
     * This docstring was generated by AI.
     */
    public Graph empty() {
        Graph g = create();
        g.inherit(this);
        return g;
    }

    /**
     * Constructs and returns a new graph with specified metadata.
     *
     * This method creates a new instance of the `Graph` class, sets its edge metadata
     * based on the value of `isEdgeMetadata()`, and returns the newly created graph.
     *
     * @return The newly created `Graph` object
     *
     * This docstring was generated by AI.
     */
    public Graph construct() {
        Graph g = create();
        g.setEdgeMetadata(isEdgeMetadata());
        return g;
    }

    /**
     * Sets the skolem flag to the same value as the skolem flag of the input graph
     *
     * @param g The input graph
     *
     * This docstring was generated by AI.
     */
    void inherit(Graph g) {
        setSkolem(g.isSkolem());
    }

    /**
     * This method creates a new graph that is the union of this graph and another graph.
     *
     * The new graph contains all the triples from both this graph and the provided graph.
     *
     * @param g The graph to be unioned with this graph
     * @return A new graph that is the union of this graph and the provided graph
     *
     * This docstring was generated by AI.
     */
    public Graph union(Graph g) {
        Graph gu = Graph.create();
        gu.copy(this);
        gu.copy(g);
        gu.init();
        return gu;
    }

    /**
     * Merges the contents of another graph into this graph.
     *
     * The method copies the contents of the input graph `g` into this graph
     * and initializes the state of this graph.
     *
     * @param g The graph to merge into this graph
     * @return This graph, with the merged contents
     *
     * This docstring was generated by AI.
     */
    public Graph merge(Graph g) {
        copy(g);
        init();
        return this;
    }

    /**
     * Copies an edge entity.
     *
     * @param ent The edge to be copied.
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Inserts a new edge into the graph.
     *
     * @param s The subject node of the new edge.
     * @param p The predicate node of the new edge.
     * @param o The object node of the new edge.
     * @return The newly inserted edge.
     *
     * This docstring was generated by AI.
     */
    public Edge insert(Node s, Node p, Node o) {
        return insert(null, s, p, o);
    }
    
    // Node MAY not be graph node
    // It will be replaced by graph node
    /**
     * Inserts a new edge into the graph.
     *
     * The method takes in four nodes: a graph node, a subject node, a predicate node, and an object node. It creates a new edge object using the provided nodes and inserts it into the graph. If the graph node is null, a default graph node is used instead.
     *
     * @param g The graph node
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @return The newly inserted edge, or null if the graph node is null
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Inserts a new edge into the graph.
     *
     * The method creates a new edge by specifying a source, subject, predicate, and value.
     * The source, subject, predicate, and value are first converted into nodes.
     * If the source node is not specified, the default graph node is used.
     *
     * @param source The node representing the source of the edge or null for the default graph node
     * @param subject The node representing the subject of the edge
     * @param predicate The node representing the predicate of the edge
     * @param value The node representing the value of the edge
     * @return The newly created edge instance
     *
     * This docstring was generated by AI.
     */
    public Edge insert(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value) {
        return insert(source==null?addDefaultGraphNode():createNode(source), 
                createNode(subject), createNode(predicate), createNode(value));        
    }
    
    /**
     * Deletes an edge from the graph.
     *
     * @param s The subject node.
     * @param p The predicate node.
     * @param o The object node.
     * @return A list of deleted edges.
     *
     * This docstring was generated by AI.
     */
    public List<Edge> delete(Node s, Node p, Node o) {
        return delete(null, s.getDatatypeValue(), p.getDatatypeValue(), o.getDatatypeValue());
    }

    /**
     * Deletes triples from the graph based on given nodes and predicate.
     *
     * This method removes triples that match the given graph, subject, predicate, and object nodes
     * from the graph manager. If any of the nodes are null, it would be treated as a wildcard.
     *
     * @param g The graph node, can be null
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @return A list of deleted edges
     *
     * This docstring was generated by AI.
     */
    public List<Edge> delete(Node g, Node s, Node p, Node o) {       
        return delete(g==null?null:g.getDatatypeValue(), 
                s.getDatatypeValue(), p.getDatatypeValue(), o.getDatatypeValue());
    }

    /**
     * Deletes an edge from the graph with the given subject, predicate, and value.
     *
     * @param subject The subject IDatatype of the edge to delete.
     * @param predicate The predicate IDatatype of the edge to delete.
     * @param value The value IDatatype of the edge to delete.
     * @return A list of deleted edges.
     *
     * This docstring was generated by AI.
     */
    public List<Edge> delete(IDatatype subject, IDatatype predicate, IDatatype value) {
        return delete(null, subject, predicate, value);
    }

    /**
     * Deletes an edge from the graph.
     *
     * The method creates a delete edge object using the given parameters, then
     * deletes it from the graph, and returns a list of deleted edges.
     *
     * @param source The source node ID
     * @param subject The subject node ID
     * @param predicate The predicate ID
     * @param value The object value ID
     * @return A list of deleted edges
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Adds an edge to the graph with the given subject, predicate, and value node.
     *
     * A default graph node is first added, then the edge is added to that graph node.
     *
     * @param subject The subject node of the edge
     * @param predicate The predicate node of the edge
     * @param value The object node of the edge
     * @return The newly created edge
     *
     * This docstring was generated by AI.
     */
    public Edge addEdge(Node subject, Node predicate, Node value) {
        Node g = addDefaultGraphNode();
        return addEdge(g, subject, predicate, value);
    }

    // tuple
    /**
     * Adds an edge to the graph with the specified source node, predicate, and list of object nodes.
     *
     * If the list of object nodes has a size of 2, a new edge is created with two object nodes.
     * If the list of object nodes has a size other than 2, a new edge is created with the list of object nodes.
     * If an edge with the same source, predicate, and object nodes already exists, it is returned.
     * Otherwise, the new edge is added to the graph and returned.
     *
     * @param source The source node of the edge
     * @param predicate The predicate of the edge
     * @param list The list of object nodes of the edge
     * @return The edge that was added or an existing edge with the same source, predicate, and object nodes
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Adds a new graph to the manager with the given label and blank node flag.
     *
     * @param label The label of the new graph.
     * @param bnode A flag indicating whether the new graph is a blank node.
     * @return The node representing the new graph.
     *
     * This docstring was generated by AI.
     */
    public Node addGraph(String label, boolean bnode) {
        return basicAddGraph(label, bnode);
    }

    /**
     * Adds a default graph node and returns it.
     *
     * @return The default graph node.
     *
     * This docstring was generated by AI.
     */
    public Node addDefaultGraphNode() {
        return basicAddGraphNode(defaultGraph);
    }

    /**
     * Checks if the given node is the default graph node
     *
     * @param g The node to check
     * @return True if the given node is the default graph node, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isDefaultGraphNode(Node g) {
        return g == defaultGraph;
    }

    /**
     * Checks if the given name is the default graph node
     *
     * @param name The name to check
     * @return True if the name is the default graph node, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isDefaultGraphNode(String name) {
        return name.equals(Entailment.DEFAULT);
    }

    /**
     * Returns the default graph node
     *
     * @return The default graph node
     *
     * This docstring was generated by AI.
     */
    public Node getDefaultGraphNode() {
        return defaultGraph;
    }
    
    /**
     * Returns the default graph datatype value.
     *
     * @return The default graph datatype value.
     *
     * This docstring was generated by AI.
     */
    public IDatatype getDefaultGraphDatatypeValue() {
        return getDefaultGraphNode().getDatatypeValue();
    }

    /**
     * Adds a rule graph node and returns it.
     *
     * @return The added rule graph node.
     *
     * This docstring was generated by AI.
     */
    public Node addRuleGraphNode() {
        return basicAddGraphNode(ruleGraph);
    }

    /**
     * Returns the rule graph node
     *
     * @return The rule graph node
     *
     * This docstring was generated by AI.
     */
    public Node getRuleGraphNode() {
        return ruleGraph;
    }

    /**
     * Adds a constraint graph node and returns it.
     *
     * @return The added constraint graph node.
     *
     * This docstring was generated by AI.
     */
    public Node addConstraintGraphNode() {
        return basicAddGraphNode(constraintGraph);
    }

    /**
     * Returns the constraint graph node.
     *
     * @return The constraint graph node.
     *
     * This docstring was generated by AI.
     */
    public Node getConstraintGraphNode() {
        return constraintGraph;
    }
    
    /**
     * Returns the constraint graph, referring to the current graph instance.
     *
     * @return The {@code Graph} instance representing the constraint graph.
     *
     * This docstring was generated by AI.
     */
    public Graph getConstraintGraph() {
        return this;
    }

    /**
     * Checks if the given node is the rule graph node
     *
     * @param node The node to check
     * @return True if the node is the rule graph node, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isRuleGraphNode(Node node) {
        return node == ruleGraph;
    }

    /**
     * Adds a resource with the given label to the graph
     *
     * @param label The label of the resource to add
     * @return The newly added resource node
     *
     * This docstring was generated by AI.
     */
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
    
    /**
     * Returns the edge before it is inserted into the graph.
     *
     * @param edge The edge to be inserted.
     * @return The same edge that was passed in as a parameter.
     *
     * This docstring was generated by AI.
     */
    public Edge beforeInsert(Edge edge) {
        return edge;
    }
    
    /**
     * Adds a new triple reference node to the graph and returns it.
     *
     * @return The new triple reference node.
     *
     * This docstring was generated by AI.
     */
    public Node addTripleReference() {
        return addTripleReference(newTripleReferenceID());
    }
    
    /**
     * Adds a triple reference with the given label and returns it.
     *
     * @param label The label of the triple reference
     * @return The added triple reference node
     *
     * This docstring was generated by AI.
     */
    public Node addTripleReference(String label) {
        return basicAddTripleReference(label);
    }
    
    /**
     * Adds a triple reference to the graph.
     *
     * If the RDF_STAR_TRIPLE property is true, the method adds the triple reference
     * directly. Otherwise, it first wraps the triple in a reference before adding it.
     *
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @return The node representing the added triple reference
     *
     * This docstring was generated by AI.
     */
    public Node addTripleReference(Node s, Node p, Node o) {
        if (Property.booleanValue(Property.Value.RDF_STAR_TRIPLE)) {
            return basicAddTripleReference(s, p, o);
        }
        return basicAddTripleReference(reference(s, p, o));
    }
    
    /**
     * Returns the triple reference node for a given subject, predicate, and object.
     *
     * @param s The subject node.
     * @param p The predicate node.
     * @param o The object node.
     * @return The triple reference node.
     *
     * This docstring was generated by AI.
     */
    public Node getTripleReference(Node s, Node p, Node o) {
        return getTripleNode(reference(s, p, o));
    }  
    
    /**
     * Returns the triple reference node for a given edge.
     *
     * @param edge The edge for which to retrieve the triple reference node.
     * @return The triple reference node for the given edge.
     *
     * This docstring was generated by AI.
     */
    public Node getTripleReference(Edge edge) {
        return getTripleNode(reference(edge.getSubjectNode(), edge.getPropertyNode(), edge.getObjectNode()));
    }  
    
    /**
     * Creates a triple reference with the given nodes
     *
     * @param s The subject node
     * @param p The predicate node
     * @param o The object node
     * @return A triple reference corresponding to the given nodes
     *
     * This docstring was generated by AI.
     */
    public IDatatype createTripleReference(Node s, Node p, Node o) {
        return createTripleReference(reference(s, p, o));
    }
    
    /**
     * Creates a triple reference with the given edge components.
     *
     * @param edge The edge containing subject, property, and object nodes.
     * @return A new triple reference associated with the edge.
     *
     * This docstring was generated by AI.
     */
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
    
    
    
    /**
     * Returns a string representation of a node's reference in the graph.
     *
     * The returned string representation is based on the node's value type.
     * If the value is a URI, the returned string is the node's index.
     * If the value is a number, the returned string is the result of the
     * {@code referenceNumber} method called with the number value.
     * If the value is a boolean, the returned string is "btrue" or "bfalse",
     * depending on the boolean value.
     * If the value is a date, the returned string is "dindex", where "index"
     * is the node's index, if the timezone is "Z", or "bindex", where "index"
     * is the node's index, if the timezone is not "Z".
     * If the value is none of the above, the returned string is the node's index.
     *
     * @param n The node to be referenced
     * @return A string representation of the node's reference
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Formats a reference number for a datatype
     *
     * @param dt The datatype
     * @return A string representing the formatted reference number
     *
     * This docstring was generated by AI.
     */
    String referenceNumber(IDatatype dt) {
        return String.format("%s(%s)", shortDatatypeLabel(dt), dt.getLabel());
    }
    
    /**
     * Returns the shortened datatype label of a given datatype.
     *
     * @param dt The datatype.
     * @return The shortened datatype label as a string.
     *
     * This docstring was generated by AI.
     */
    String shortDatatypeLabel(IDatatype dt) {
        return dt.getDatatype().getLabel().substring(NSManager.XSD_LENGTH, NSManager.XSD_LENGTH+3);
    }
    
    /**
     * Creates a triple reference with a new ID.
     *
     * @return A new triple reference ID.
     *
     * This docstring was generated by AI.
     */
    public IDatatype createTripleReference() {
        return createTripleReference(newTripleReferenceID());
    }
    
    /**
     * Creates a triple reference with the given label.
     *
     * @param label The label for the triple reference
     * @return An IDatatype representing the triple reference
     *
     * This docstring was generated by AI.
     */
    IDatatype createTripleReference(String label) {
        return DatatypeMap.createTripleReference(label);
    }

    /**
     * Creates a blank node with the given label.
     *
     * If the `isSkolem` flag is true, a skolem blank node is created, otherwise
     * a blank node is created using the `createBlank` method from the
     * `DatatypeMap` class.
     *
     * @param label The label for the blank node
     * @return An `IDatatype` object representing the blank node
     *
     * This docstring was generated by AI.
     */
    public IDatatype createBlank(String label) {
        if (isSkolem) {
            return createSkolem(label);
        } else {
            return DatatypeMap.createBlank(label);
        }
    }

    /**
     * Adds a new blank node to the graph and returns it.
     *
     * @return The newly created blank node.
     *
     * This docstring was generated by AI.
     */
    public Node addBlank() {
        return addBlank(newBlankID());
    }

    /**
     * Adds a blank node to the graph and returns it.
     *
     * @return The newly added blank node.
     *
     * This docstring was generated by AI.
     */
    public Node addTripleName() {
        return addBlank();
    }

    /**
     * Adds a literal with the given label, datatype, and language to the graph.
     *
     * The method first creates a new literal using the provided parameters and the
     * DatatypeMap. If the literal cannot be created, the method returns null.
     * Otherwise, it adds the new literal as a node in the graph.
     *
     * @param label The label of the literal
     * @param datatype The datatype of the literal
     * @param lang The language of the literal
     * @return The node representing the added literal, or null if the literal
     *         could not be created
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(String label, String datatype, String lang) {
        IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }

    /**
     * Adds a new node with a literal label and datatype to the graph.
     *
     * The method creates a new node with a given label and datatype using the
     * DatatypeMap.createLiteral() method. If the creation is successful,
     * the node is added to the graph using the addNode() method.
     *
     * @param label The string label of the literal
     * @param datatype The datatype of the literal
     * @return The added node or null if the node creation fails
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(String label, String datatype) {
        IDatatype dt = DatatypeMap.createLiteral(label, datatype, null);
        if (dt == null) {
            return null;
        }
        return addNode(dt);
    }

    /**
     * Adds a literal node with the given label to the graph.
     *
     * @param label The label of the node.
     * @return A node object representing the new literal node.
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(String label) {
        return addLiteral(label, null, null);
    }

    /**
     * Adds a new literal node with the given integer value to the graph.
     *
     * @param n The integer value of the literal node.
     * @return The new literal node that was added to the graph.
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(int n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    /**
     * Adds a literal node to the graph with the given long value.
     *
     * @param n The long value for the new literal node.
     * @return The new node object representing the added literal node.
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(long n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    /**
     * Adds a literal value as a node to the graph
     *
     * @param n The literal value as a double
     * @return The node representing the added literal value
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(double n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    /**
     * Adds a literal node with the given float value to the graph.
     *
     * @param n The float value for the literal node.
     * @return The added node.
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(float n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    /**
     * Adds a literal node to the graph.
     *
     * @param n The boolean value for the datatype of the literal node.
     * @return The newly added node.
     *
     * This docstring was generated by AI.
     */
    public Node addLiteral(boolean n) {
        return addNode(DatatypeMap.newInstance(n));
    }

    /**
     * Sets the debug mode for the graph manager.
     *
     * @param b {@code true} to enable debug mode, {@code false} to disable it
     *
     * This docstring was generated by AI.
     */
    public void setDebug(boolean b) {
        isDebug = b;
    }

    /**
     * Sets the debug mode for the graph manager and related components.
     *
     * This method enables or disables the debug mode for the `Graph` object, as well as for its internal components such as the
     * `EdgeManagerIndexer` objects and the `Entailment` object. When debug mode is enabled, these components will produce
     * debugging output.
     *
     * @param b The new debug mode setting
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Tags the given edge using the factory object.
     *
     * @param ent The edge to be tagged
     *
     * This docstring was generated by AI.
     */
    public void tag(Edge ent) {
        fac.tag(ent);
    }

    /**
     * Returns a string used for tagging a resource.
     *
     * The method first retrieves the tagger object and checks if it is not null.
     * If the tagger is null, a new tag string is created using the key and an incremented tag count.
     * If the tagger is not null, the tagger's tag method is called to get the tag string.
     *
     * @return The tag string for the resource
     *
     * This docstring was generated by AI.
     */
    String tagString() {
        Tagger t = getTagger();
        if (t == null) {
            return key + tagCount++;
        }
        return t.tag();
    }

    /**
     * Returns whether the graph has a tag.
     *
     * @return True if the graph has a tag, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean hasTag() {
        return hasTag;
    }

    /**
     * Checks if a tag is needed for a given edge.
     *
     * This method returns true if the graph has a tag, the edge has the
     * number of nodes equal to the tag index, and the edge's graph is not
     * entailed by the proxy.
     *
     * @param ent The edge to check for a needed tag
     * @return True if a tag is needed for the given edge, false otherwise
     *
     * This docstring was generated by AI.
     */
    boolean needTag(Edge ent) {
        return hasTag()
                && ent.nbNode() == TAGINDEX
                && !getProxy().isEntailed(ent.getGraph());
    }

    /**
     * Sets the tag value and updates the tuple status accordingly.
     *
     * This method sets the value of the 'hasTag' instance variable to the input
     * boolean value 'b'. If 'b' is true, it also sets the 'tuple' instance
     * variable to true.
     *
     * @param b The value to set the 'hasTag' instance variable to
     */
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

    /**
     * Logs the insertion of an edge in the graph.
     *
     * This method notifies all registered listeners about the insertion of a new edge
     * in the graph. If there are no registered listeners, the method does nothing.
     *
     * @param ent The edge to be logged
     */
    public void logInsert(Edge ent) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.insert(this, ent);
            }
        }
    }

    /**
     * Declares an update operation on the graph.
     *
     * The method iterates over all indexes in the graph and declares the update
     * operation on them. 
     *
     * @param b A boolean flag indicating whether to declare an update operation
     */
    public void declareUpdate(boolean b) {
        for (EdgeManagerIndexer ind : getIndexList()) {
            ind.declareUpdate(b);
        }
    }

    /**
     * Logs the start of a query execution.
     *
     * The method notifies all registered listeners that a query execution is starting
     * on the current graph instance.
     *
     * @param q The query being executed
     */
    public void logStart(Query q) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.start(this, q);
            }
        }
    }

    /**
     * Logs the finish time of a query.
     *
     * @param q The query object
     * @param log The log object, or null if not provided
     *
     * This docstring was generated by AI.
     */
    public void logFinish(Query q) {
        logFinish(q, null);
    }

    /**
     * Logs query finish and notifies registered listeners.
     *
     * This method checks if there are any registered listeners and, if so,
     * notifies each one of the query finish event by calling the 'finish'
     * method on each listener, passing this graph, the query, and the
     * mappings as arguments.
     *
     * @param q The query being logged
     * @param m The mappings associated with the query
     */
    public void logFinish(Query q, Mappings m) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.finish(this, q, m);
            }
        }
    }

    /**
     * Logs the loading of a graph from a specified path.
     *
     * This method notifies all registered GraphListeners of the loading of a
     * graph from the provided path. If there are no registered listeners,
     * the method does nothing.
     *
     * @param path The path of the graph file to be loaded
     *
     * This docstring was generated by AI.
     */
    public void logLoad(String path) {
        if (getListenerList() != null) {
            for (GraphListener gl : getListenerList()) {
                gl.load(path);
            }
        }
    }

    /**
     * Notifies registered listeners of an edge insertion.
     *
     * This method calls the onInsert() method of each registered GraphListener,
     * allowing them to react to the insertion of a new edge. If any listener
     * returns false, this method also returns false to indicate that the
     * insertion was not successful. Otherwise, it returns true to indicate
     * that the insertion was successful.
     *
     * @param ent The inserted edge
     * @return True if the edge was successfully inserted, false otherwise
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Shares a named graph.
     *
     * @param g The graph to share
     *
     * This docstring was generated by AI.
     */
    public void shareNamedGraph(Graph g) {
    }

    /**
     * Returns an empty collection of strings.
     *
     * @return An empty collection of strings.
     *
     * This docstring was generated by AI.
     */
    public Collection<String> getNames() {
        return new ArrayList<>(0);
    }

    /**
     * Returns the named graph with the given name.
     *
     * @param name The name of the graph.
     * @return The {@link Graph} with the given name or null if it doesn't exist.
     *
     * This docstring was generated by AI.
     */
    public Graph getNamedGraph(String name) {
        return null;
    }

    /**
     * Sets the named graph with the given name to the provided graph
     *
     * @param name The name of the graph
     * @param g The graph to set
     * @return The current instance of the Graph for method chaining
     *
     * This docstring was generated by AI.
     */
    public Graph setNamedGraph(String name, Graph g) {
        return this;
    }

    /**
     * Returns a new Dataset object containing all nodes from the graph.
     *
     * The method creates a new Dataset object and adds all graph nodes to it.
     * For each graph node, it adds a default graph from the node's label
     * and a named graph with the node's label as the name.
     *
     * @return A new Dataset object containing all nodes from the graph
     *
     * This docstring was generated by AI.
     */
    public Dataset getDataset() {
        Dataset ds = Dataset.create();
        for (Node node : getGraphNodes()) {
            ds.addFrom(node.getLabel());
            ds.addNamed(node.getLabel());
        }
        return ds;
    }

    /**
     * Returns this graph instance for rule processing
     *
     * @param constraint If true, use constraint rule graph
     * @return This graph instance
     *
     * This docstring was generated by AI.
     */
    public Graph getRuleGraph(boolean constraint) {
        return this;
    }

    /**
     * Returns the name of the rule graph or constraint graph.
     *
     * @param constraint If true, returns the constraint graph name; otherwise,
     *                    returns the rule graph name.
     * @return The node representing the name of the rule or constraint graph.
     *
     * This docstring was generated by AI.
     */
    public Node getRuleGraphName(boolean constraint) {
        return (constraint && CONSTRAINT_NAMED_GRAPH) ? addConstraintGraphNode() : addRuleGraphNode();
    }

    /**
     * Returns whether this graph is a metadata graph
     *
     * @return true if this graph is a metadata graph, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isMetadata() {
        return metadata;
    }

    /**
     * Checks if the node is a metadata node
     *
     * @return true if the node is an edge metadata or a metadata node
     *
     * This docstring was generated by AI.
     */
    public boolean isMetadataNode() {
        return isEdgeMetadata() || isMetadata();
    }
    
    /**
     * Checks if a node is a former metadata node
     *
     * @return true if the node is a metadata node and not a RDF-star triple, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isFormerMetadata() {
        return isMetadataNode() && ! Property.booleanValue(Property.Value.RDF_STAR_TRIPLE);
    }

    /**
     * Sets the metadata value
     *
     * @param metadata The new metadata value
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Executes a query and returns the result as a JSON object using GraphDistance
     *
     * @param ast The ASTQuery object representing the query
     * @param d An integer value
     * @return A JSONObject containing the result of the query
     *
     * This docstring was generated by AI.
     */
    public JSONObject match(ASTQuery ast, int d) {
        return new GraphDistance(this).match(ast, d);
    }

    /**
     * Returns the cardinality for the given ASTQuery using a GraphDistance object.
     *
     * @param ast The ASTQuery object for which to return the cardinality.
     * @return A JSONObject containing the cardinality information.
     *
     * This docstring was generated by AI.
     */
    public JSONObject cardinality(ASTQuery ast) {
        return new GraphDistance(this).cardinality(ast);
    }

    /**
     * Returns the map of literal nodes managed by this graph.
     *
     * @return A sorted map containing literal nodes as values and their corresponding
     *         datatype keys.
     *
     * This docstring was generated by AI.
     */
    public SortedMap<IDatatype, Node> getLiteralNodeManager() {
        return literalNodeManager;
    }

    /**
     * Sets the map for managing literal nodes in the graph.
     *
     * @param literal The sorted map of literals and nodes.
     *
     * This docstring was generated by AI.
     */
    public void setLiteralNodeManager(SortedMap<IDatatype, Node> literal) {
        this.literalNodeManager = literal;
    }

    /**
     * Returns the literal index manager of the graph.
     *
     * @return A sorted map of literals to nodes.
     *
     * This docstring was generated by AI.
     */
    public SortedMap<IDatatype, Node> getLiteralIndexManager() {
        return literalIndexManager;
    }

    /**
     * Sets the literal index manager in the graph manager
     *
     * @param sliteral The sorted map of IDatatype to Node
     *
     * This docstring was generated by AI.
     */
    public void setLiteralIndexManager(SortedMap<IDatatype, Node> sliteral) {
        this.literalIndexManager = sliteral;
    }

    /**
     * Sets the default verbose mode
     *
     * @param b The new verbose mode
     *
     * This docstring was generated by AI.
     */
    public static void setDefaultVerbose(boolean b) {
        VERBOSE = b;
    }

    /**
     * Sets the default Skolem mode.
     *
     * @param b The new default Skolem mode.
     *
     * This docstring was generated by AI.
     */
    public static void setDefaultSkolem(boolean b) {
        SKOLEM_DEFAULT = b;
    }

    /**
     * Returns whether the object is a graph node.
     *
     * @return Whether the object is a graph node
     *
     * This docstring was generated by AI.
     */
    public boolean isAllGraphNode() {
        return allGraphNode;
    }

    /**
     * Sets whether all nodes in the graph are to be considered.
     *
     * @param allGraphNode If true, all nodes in the graph will be considered.
     *
     * This docstring was generated by AI.
     */
    public void setAllGraphNode(boolean allGraphNode) {
        this.allGraphNode = allGraphNode;
    }

    /**
     * Returns the subject indexer of the graph.
     *
     * @return The subject indexer of the graph.
     *
     * This docstring was generated by AI.
     */
    public EdgeManagerIndexer getSubjectIndex() {
        return subjectIndex;
    }

    /**
     * Sets the subject index to the provided EdgeManagerIndexer table.
     *
     * @param table The EdgeManagerIndexer table to set as the subject index.
     *
     * This docstring was generated by AI.
     */
    void setSubjectIndex(EdgeManagerIndexer table) {
        this.subjectIndex = table;
    }

    /**
     * Returns the indexer for named graphs.
     *
     * @return The EdgeManagerIndexer for named graphs.
     *
     * This docstring was generated by AI.
     */
    EdgeManagerIndexer getNamedGraphIndex() {
        return namedGraphIndex;
    }

    /**
     * Sets the named graph index for the graph manager.
     *
     * @param tgraph The edge manager indexer for the named graph index.
     *
     * This docstring was generated by AI.
     */
    void setNamedGraphIndex(EdgeManagerIndexer tgraph) {
        this.namedGraphIndex = tgraph;
    }

    /**
     * Sets the list of index tables for the graph manager.
     *
     * @param tables The list of edge manager indexers.
     *
     * This docstring was generated by AI.
     */
    public void setIndexList(ArrayList<EdgeManagerIndexer> tables) {
        this.tables = tables;
    }

    /**
     * Returns whether SPARQL debugging is enabled
     *
     * @return boolean value indicating whether SPARQL debugging is enabled
     *
     * This docstring was generated by AI.
     */
    public boolean isDebugSparql() {
        return debugSparql;
    }

    /**
     * Sets the debug SPARQL flag
     *
     * @param debugSparql The flag value
     *
     * This docstring was generated by AI.
     */
    public void setDebugSparql(boolean debugSparql) {
        this.debugSparql = debugSparql;
    }

    /**
     * Returns the list of graph listeners.
     *
     * @return The list of graph listeners.
     *
     * This docstring was generated by AI.
     */
    public List<GraphListener> getListenerList() {
        return listenerList;
    }

    /**
     * Sets the listener list for the graph manager.
     *
     * @param listenerList The list of graph listeners
     *
     * This docstring was generated by AI.
     */
    public void setListenerList(List<GraphListener> listenerList) {
        this.listenerList = listenerList;
    }

}
