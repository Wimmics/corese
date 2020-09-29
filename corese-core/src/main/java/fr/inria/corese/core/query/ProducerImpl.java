package fr.inria.corese.core.query;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import static fr.inria.corese.kgram.sorter.core.Const.ALL;
import static fr.inria.corese.kgram.sorter.core.Const.NA;
import static fr.inria.corese.kgram.sorter.core.Const.OBJECT;
import static fr.inria.corese.kgram.sorter.core.Const.PREDICATE;
import static fr.inria.corese.kgram.sorter.core.Const.SUBJECT;
import static fr.inria.corese.kgram.sorter.core.Const.TRIPLE;
import fr.inria.corese.kgram.sorter.core.IProducerQP;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.core.index.EdgeManagerIndexer;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.core.Index;
import fr.inria.corese.core.util.ValueCache;
import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import java.util.HashMap;

/**
 * Producer Implement getEdges() for KGRAM interpreter rely on
 * graph.getDataStore().getDefault() graph.getDataStore().getNamed()
 *
 * @author Olivier Corby, Edelweiss INRIA 2010ta
 *
 */
public class ProducerImpl implements Producer, IProducerQP {

    public static final int OWL_RL = 1;

    static final int IGRAPH = Graph.IGRAPH;
    static final int ILIST = Graph.ILIST;
    public static final String TOPREL = Graph.TOPREL;
    List<Edge> empty = new ArrayList<Edge>(0);
    List<Node> emptyFrom = new ArrayList<Node>(0);
    DataProducer ei;
    Graph graph,
            // cache for handling (fun() as var) created Nodes
            local;
    Mapper mapper;
    MatcherImpl match;
    QueryEngine qengine;
    FuzzyMatch fuzzy = new FuzzyMatch();
    ValueCache vcache;
    RDFizer toRDF;
    Node graphNode;
    private Query query;

    // if true, perform local match
    boolean isMatch = false;
    private boolean selfValue;
    private boolean speedUp = false;
    private int index = -1;
    int mode = DEFAULT;
    private IDatatype prevdt;
    private Node prevnode;

    HashMap<Edge, DataProducer> cache;
    ProducerImplNode pn;

    public ProducerImpl() {
        this(Graph.create());
    }

    public ProducerImpl(Graph g) {
        graph = g;
        local = Graph.create();
        mapper = new Mapper(this);
        ei = DataProducer.create(g);
        toRDF = new RDFizer();
        vcache = new ValueCache();
        cache = new HashMap<Edge, DataProducer>();
        pn = new ProducerImplNode(this);
    }

    public static ProducerImpl create(Graph g) {
        ProducerImpl p = new ProducerImpl(g);
        return p;
    }

    public FuzzyMatch getFuzzyMatch() {
        return fuzzy;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setMode(int n) {
        switch (n) {
            case SKIP_DUPLICATE_TEST:
                setSpeedUp(true);
                break;

            case EXTENSION:
                mode = n;
                break;

        }
    }

    public void setMatch(boolean b) {
        isMatch = b;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void set(Matcher m) {
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
    }

    public void set(QueryEngine qe) {
        qengine = qe;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    public Graph getLocalGraph() {
        return local;
    }

    Node getNode(Edge edge, Node gNode, int i) {
        switch (i) {
            case IGRAPH:
                return gNode;
            case ILIST:
                return null;
            default:
                return edge.getNode(i);
        }
    }
    
    Node getNode(Node gNode, Environment env) {
        if (gNode == null) {
            return null;
        }
        return getValue(gNode, env);
    }

    /**
     * Possibly get a Node with a different number datatype than required for D
     * entailment
     */
    Node getValue(Node qNode, Environment env) {
        return getValue(qNode, qNode.isVariable()?env.getNode(qNode):null, env);
    }
    
    Node getValue(Node qNode, Node node, Environment env) {
        if (qNode.isConstant()) {
            node = getExtNode(qNode);
        }
        else if (node != null && isExtern(node)) {
            node = getExtNode(node);
        }
        return node;
    }
    
//    Node getValue2(Node qNode, Node node, Environment env) {
//        if (node == null) {
//            if (qNode.isConstant()) {
//                node = getExtNode(qNode);
//            }
//        } else if (isExtern(node)) {
//            node = getExtNode(node);
//        }
//        return node;
//    }

    Node getExtNode(Node node) {
        return graph.getExtNode(node);
    }

    // eg BIND(node as ?x)
    boolean isExtern(Node node) {
        return node.getIndex() == -1
                || node.getTripleStore() != graph;
    }

    boolean isType(Edge edge, Environment env) {
        return graph.isType(edge) || env.getQuery().isRelax(edge);
    }

    /**
     *
     * @param gNode : null or named graph pattern: graph gNode { }
     * @param from : null, from or from named if gNode != null
     * @param edge : query Edge
     * @param env : Environment with partial variable bindings if gNode == null:
     * query default graph, possibly with from eliminate duplicate edges (same
     * edge with different named graph) if gNode != null: query named graph,
     * possibly with from named gNode may be a constant value or it may have a
     * value in env
     *
     * Use cases: 1. named graph: gNode edge, from named gNode edge (gNode: uri
     * or var with or without value in env) 2. default graph: edge, from edge if
     * edge has a bound query node, use its value to focus candidate edges in
     * the Edge Index
     */
    @Override
    public Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge edge,
            Environment env) {

//        if (gNode != null) {
//            if (gNode.isVariable()) {
//                System.out.println(gNode);
//                System.out.println(env.getQuery().getAST());
//            }
//        }
        
        Node predicate = getPredicate(edge, env);
        if (predicate == null) {
            return empty;
        }

        Query q = env.getQuery();
        ASTQuery ast = (ASTQuery) q.getAST();

        int level = -1;
        int n = 0;

        if (q.isRule()) {
            // special case with tricky optimizations for rule engine
            if (q.getEdgeList() != null
                    && edge.getIndex() == q.getEdgeIndex()) {
                // transitive rule (see RuleEngine)
                // there is a list of candidate edges
                return q.getEdgeList();
            } else {
                Exp exp = env.getExp();
                if (exp != null && exp.getEdge() == edge && exp.getLevel() != -1) {
                    level = exp.getLevel();
                    n = ILIST;
                    // rule engine requires new edges with level >= exp.getLevel()
                    // ILIST is index of specific Edge Index sorted by reverse level
                    Iterable<Edge> it = graph.getDataStore().getDefault(from).level(level).iterate(predicate, null, ILIST);
                    return localMatch(it, gNode, edge, env);
                }
            }
        }

        Node focusNode = null, objectNode = null;
        boolean isType = false;
      
        //gNode = (gNode != null && gNode.isVariable()) ? env.getNode(gNode) : gNode;

        for (Index ei : graph.getIndexList()) {
            // enumerate graph index to get the index i of nodes in edge: 0, 1, GRAPHINDEX
            // by convention the index of last is the index of graph node wich is -1
            // search edge query node with a value or which is a constant
            int i = ei.getIndex();
            if (i < edge.nbNode()) {
                // get ith node
                Node qNode = getNode(edge, gNode, i);
                if (qNode != null) {
                    if (i == 1
                            && qNode.isConstant()
                            && isType(edge, env)
                            && graph.hasEntailment()) {
                        // RDFS entailment on ?x rdf:type c:Engineer
                        // no focus on object node because:
                        // no dichotomy on c:Engineer because we want subsumption                           
                    } else {
                        Node val = qNode.isVariable() ? env.getNode(qNode) : null;
                        // candidate query node value:
                        focusNode = getValue(qNode, val, env);

                        if (focusNode == null) {
                            if (qNode.isConstant() || val != null) {
                                // search a value that is not in the graph: fail
                                return empty;
                            }
                        } else {
                            n = i;
                            if (i == 0 && !isType(edge, env)) {
                                // in case query object Node also have a value
                                Node val2 = env.getNode(edge.getNode(1));
                                objectNode = getValue(edge.getNode(1), val2, env);
                            }
                            break;
                        }
                    }
                }
            }
        }

        Iterable<Edge> it;

        if (mode == EXTENSION && getQuery() == q) {
            // Producer for an external graph ?g :
            // bind (us:graph() as ?g) graph ?g { }         
            it = graph.getDataStore().getDefault(emptyFrom).iterate(predicate, focusNode, n);
        } else {
            boolean skip = graph.isEdgeMetadata() && edge.nbNode()==2;
            byte access = ast.getAccess().getWhere();
            it = getEdges(gNode, getNode(gNode, env), from, predicate, focusNode, objectNode, n, skip, access);
        }
        // in case of local Matcher
        it = localMatch(it, gNode, edge, env);

        return it;
    }

    /**
     * Enumerate candidate edges either from default graph or from named graphs
     */
    public Iterable<Edge> getEdges(Node gNode, Node sNode, List<Node> from,
            Node predicate, Node focusNode, Node objectNode, int n, boolean skip, byte access) {
        return dataProducer(gNode, from, sNode, skip, access).iterate(predicate, focusNode, n);
    }
    
    Iterable<Edge> getEdges(Node gNode, Node sNode, List<Node> from,
            Node predicate, Node focusNode, Node objectNode, int n, boolean skip) {
        return dataProducer(gNode, from, sNode, skip, AccessRight.PUBLIC).iterate(predicate, focusNode, n);
    }

    DataProducer dataProducer(Node gNode, List<Node> from, Node sNode, boolean skip, byte access) {
        DataProducer dp;
        if (gNode == null) {
            dp = graph.getDataStore().getDefault(from).setSkipEdgeMetadata(skip);
        } else {
            dp = graph.getDataStore().getNamed(from, sNode).setSkipEdgeMetadata(skip);
        }
        if (AccessRight.isActive()) {
            dp.access(access);
        }
        return dp;
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) throws SparqlException{
        if (env instanceof Memory) {
            if (env.getQuery().isDebug()) {
                System.out.println("BGP:\n" + exp);
            }
            Memory mem = (Memory) env;
            Eval eval = mem.getEval();
            // prevent loop on BGP exp:
            exp.setType(Exp.AND);
            Mappings map = eval.subEval(this, gNode, gNode, exp, null);
            if (env.getQuery().isDebug()) {
                System.out.println("BGP:\n" + map);
            }
            exp.setType(Exp.BGP);
            return map;
        } else {
            return Mappings.create(query, true);
        }
    }

    /**
     *
     */
    boolean isFuzzy(Edge edge, int i) {
        int type = fuzzy.fuzzy(edge.getLabel());
        return 0 <= i && i <= 1
                && (i == type || type == 2);
    }

    /**
     * Iterator of Entity that performs local Ontology match.match() Enable to
     * have a local ontology in case of several graphs with local ontologies In
     * addition, with rdfs entailment, ?x a us:Person return one occurrence of
     * each value of ?x
     */
    Iterable<Edge> localMatch(Iterable<Edge> it, Node gNode, Edge edge, Environment env) {
        if (isMatch && !env.getQuery().isRelax()) {
            MatchIterator mit = new MatchIterator(it, gNode, edge, graph, env, match);
            return mit;
        } else {
            // if query is relax, we want all types to find best match
            // hence skip MatchIterator
            return it;
        }
    }  

    /**
     * Return Node that represents the predicate of the Edge
     */
    Node getPredicate(Edge edge, Environment env) {
        Node var = edge.getEdgeVariable();
        Node predicate = edge.getEdgeNode();
        if (var != null) {
            predicate = env.getNode(var);
            if (predicate == null) {
                predicate = edge.getEdgeNode();
            }
        }
        String name = predicate.getLabel();
        if (!name.equals(TOPREL)) {
            predicate = graph.getPropertyNode(name);
        }
        return predicate;
    }

    boolean isFromOK(List<Node> from) {
        for (Node node : from) {
            Node tfrom = graph.getNode(node);
            if (tfrom != null && graph.containsCoreseNode(tfrom)) {
                return true;
            }
        }
        return false;
    }

    boolean isFrom(Node ent, List<Node> from) {
        for (Node node : from) {
            if (ent.same(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Edges for Property Path elementary Regex property | ^property | !(p1 | |
     * pn) gNode: query named graph from: from/named src: target named graph
     * start: the start node of target edges index: 0 for start subject, 1 for
     * start object index = 1 occurs when edge object has a value or is constant
     * in this case path search goes from object to subject.
     *
     */
    @Override
    public Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge edge, Environment env,
            Regex exp, Node src, Node start, int index) {

        boolean isdb = isDB();
        if (start == null) {
            Node qNode = edge.getNode(index);
            if (qNode.isConstant()) {
                //	start = qNode;
                start = graph.getExtNode(qNode);
                if (start == null) {
                    if (isdb) {
                        start = qNode;
                    } else {
                        return empty;
                    }
                }
            }
        }

        if (start != null && isExtern(start)) {
            if (!isdb) {
                start = graph.getNode(start);
            }
        }

        if (exp.isReverse()) {
            // ?x ^p ?y
            // start node is now reverse(index) node
            index = (index == 0) ? 1 : 0;
        }
        
        if (src != null && isExtern(src)) {
              src = getExtNode(src);
        }

        if (exp.isNot()) {
            return getNegEdges(gNode, from, edge, env, exp, src, start, index);
        }

        Node predicate = graph.getPropertyNode(exp.getLongName());
        if (predicate == null) {
            if (isdb) {
                predicate = (IDatatype) exp.getDatatypeValue();
            } else {
                return empty;
            }
        }

        Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index, graph.isEdgeMetadata());

        return it;
    }

    boolean isDB() {
        return getClass() != ProducerImpl.class;
    }

    /**
     * regex is a negation ?x ! rdf:type ?y ?x !(rdf:type|rdfs:subClassOf) ?y
     * enumerate properties but those in the negation
     */
    public Iterable<Edge> getNegEdges(Node gNode, List<Node> from, Edge edge, Environment env,
            Regex exp, Node src, Node start, int index) {

        exp = exp.getArg(0);
        MetaIterator<Edge> meta = new MetaIterator<Edge>();
        for (Node predicate : graph.getSortedProperties()) {
            if (match(exp, predicate)) {
                // exclude
            } else {
                Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index, graph.isEdgeMetadata());

                if (it != null) {
                    meta.next(it);
                }
            }
        }
        if (meta.isEmpty()) {
            return empty;
        }
        return meta;
    }

    boolean match(Regex exp, Node pred) {
        if (exp.isConstant()) {
            return exp.getLongName().equals(pred.getLabel());
        }
        for (int i = 0; i < exp.getArity(); i++) {
            if (exp.getArg(i).getLongName().equals(pred.getLabel())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Node> getNodes(Node gNode, List<Node> from, Edge edge,
            Environment env, List<Regex> exp, int index) {
        return pn.getNodeIterator(gNode, from, edge, env, exp, index);
    }

    public boolean contains(Node gNode, Node node) {
        Node g = graph.getNode(gNode);
        Node n = graph.getNode(node);
        if (g == null || n == null) {
            return false;
        }
        return graph.contains(g, n);
    }

    /**
     * Return list of named graph Nodes, possibly in from
     */
    @Override
    public Iterable<Node> getGraphNodes(Node node, List<Node> from,
            Environment env) {
        // TODO check from
        if (from.size() > 0) {
            return getGraphNodes2(node, from, env);
        }

        return graph.isAllGraphNode() ? graph.getGraphNodesAll() : graph.getGraphNodes();
    }

    Iterable<Node> getGraphNodes2(Node node, final List<Node> from,
            Environment env) {

        List<Node> list = new ArrayList<Node>();
        for (Node nn : from) {
            Node target = graph.getGraphNode(nn.getLabel());
            if (target == null) {
                if (graph.getNamedGraph(nn.getLabel()) != null){
                    target = nn;
                }
            }
            if (target != null) {
                list.add(target);
            }
        }

        return list;
    }

    /**
     * Cast Java value into IDatatype
     */
    @Override
    public IDatatype getValue(Object value) {
        return DatatypeMap.getValue(value);
    }

    IDatatype nodeValue(Node n) {
        return (IDatatype) n.getValue();
    }

    // cast IDatatype or Java value into DatatypeValue
    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof IDatatype) {
            return (IDatatype) value;
        } else {
            return getValue(value);
        }
    }

    /**
     * Return a Node given a value (IDatatype value) Use case: select/bind (exp
     * as node) return the Node in the graph or return the IDatatype value as is
     * (to speed up)
     *
     */
    @Override
    synchronized public Node getNode(Object value) {
        // TODO Auto-generated method stub
        if (!(value instanceof IDatatype)) {
            return DatatypeMap.createObject(value);
        }
        IDatatype dt = (IDatatype) value;
        if (dt.isFuture()) {
            // future: template intermediate result 
            return dt;
        }
        Node node = graph.getNode(dt, false, false);
        if (node != null) {
            return node;
        }
        return dt;
    }

    public Object getValue(Node node) {
        return node.getValue();
    }

    @Override
    public void init(Query q) {
        cache.clear();
    }

    @Override
    public void start(Query q) {
        //graph.init();
        graph.getEventManager().start(Event.Query, q.getAST());
    }

    @Override
    public void finish(Query q) {
        graph.getEventManager().finish(Event.Query, q.getAST());
    }

    @Override
    public void initPath(Edge edge, int index) {

    }

    @Override
    public boolean isBindable(Node node) {
//        if (!Graph.valueOut) {
//            return true;
//        }
        IDatatype dt = (IDatatype) node.getValue();
        // 1 && 1.0 are not same Node: cannot bind (see kgram Eval)
        return !dt.isNumber();
    }

    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        Node node = getValue(gNode, env );
        if (node != null && graph.containsCoreseNode(node)) {
            if (from.isEmpty()) {
                return true;
            } else {
                return ei.getCreateDataFrom().isFrom(from, node);
            }
        } else {
            return false;
        }
    }

    /**
     * Cast function result into Mappings
     */
    @Override
    public Mappings map(List<Node> nodes, Object object) {
        return mapper.map(nodes, object);
    }

    @Override
    public List<Node> toNodeList(Object obj) {
        return mapper.toNodeList(obj);
    }

    void filter(Environment env) {
        // KGRAM exp for current edge
        Exp exp = env.getExp();
        List<String> lVar = new ArrayList<String>();
        List<Node> lNode = new ArrayList<Node>();

        for (Filter f : exp.getFilters()) {
            // filters attached to current edge
            if (f.getExp().isExist()) {
                // skip exists { PAT }
                continue;
            }

            // function exp.bind(f) tests whether current edge binds all variable of filter f			
            for (String var : f.getVariables()) {
                if (!lVar.contains(var)) {
                    Node node = env.getNode(var);
                    if (node != null) {
                        lVar.add(var);
                        lNode.add(node);
                    }
                }
            }
        }

        System.out.print("bindings ");
        for (String var : lVar) {
            System.out.print(var + " ");
        }
        System.out.println("{(");
        for (Node node : lNode) {
            System.out.print(node + " ");
        }
        System.out.println(")}");

    }

    private boolean isSelfValue() {
        return selfValue;
    }

    public void setSelfValue(boolean selfValue) {
        this.selfValue = selfValue;
    }

    /**
     * Overloading of graph ?g { } The value of ?g may an extended graph
     * producer
     */
    @Override
    public boolean isProducer(Node node) {
        IDatatype dt = (IDatatype) node.getValue();
        if (dt.getObject() != null) {
            return toRDF.isGraphAble(dt.getObject()) || dt.getObject() instanceof Producer;
        }
        // system named graph in a GraphStore
        return graph.getNamedGraph(node.getLabel()) != null;
    }

    /**
     * Node contains a Graph: return Producer for this Graph
     */
    @Override
    public Producer getProducer(Node node, Environment env) {
        IDatatype dt = (IDatatype) node.getValue();
        Object obj = dt.getObject();
        Graph g = null;

        if (obj == null) {
            g = graph.getNamedGraph(node.getLabel());
        } else if (obj instanceof Producer) {
            return (Producer) obj;
        } else {
            g = toRDF.getGraph(obj);
        }

        if (g == null) {
            g = Graph.create();
        }
        ProducerImpl p = ProducerImpl.create(g);
        MatcherImpl m = MatcherImpl.create(g);
        p.setMode(EXTENSION);
        p.setMatch(true);
        p.set(m);
        // producer remember the query that createt it
        // use case: templates may share this producer
        p.setQuery(env.getQuery());
        return p;
    }

    /**
     * @return the speedUp
     */
    public boolean isSpeedUp() {
        return speedUp;
    }

    /**
     * @param speedUp the speedUp to set
     */
    public void setSpeedUp(boolean speedUp) {
        this.speedUp = speedUp;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void setGraphNode(Node n) {
        graphNode = n;
    }

    @Override
    public Node getGraphNode() {
        return graphNode;
    }

    /**
     * IProducer for QP. F. Song 2014
     *
     * @param qpgn QPG node
     * @param type
     * @return
     */
    @Override
    public int getCount(QPGNode qpgn, int type) {
        switch (type) {
            case PREDICATE:
                // case 1: unbound
                if (qpgn.getExpNode(PREDICATE).isVariable()) {
                    return getSize(PREDICATE);
                }

                // case 2: bound
//                Iterator<Node> it = this.graph.getProperties().iterator();
//                while (it.hasNext()) {
//                    Node predNode = it.next();
//                    if (qpgn.getExpNode(PREDICATE).getLabel().equalsIgnoreCase(predNode.getLabel())) {
//                        return this.graph.size(predNode);
//                    }
//                }
                Node pred = graph.getPropertyNode(qpgn.getExpNode(PREDICATE).getLabel());
                if (pred != null) {
                    return this.graph.size(pred);
                }

                //case 3: not found
                return 0;
            //to do for SUBJECT | OBJECT
            //for the moment, cannot get these values directly
            case SUBJECT:
            case OBJECT:
            case TRIPLE:
            default:
                return NA;
        }

    }

    @Override
    public int getSize(int type) {
        switch (type) {
            case ALL:
                return this.graph.size();
            case PREDICATE:
                return this.graph.getIndex().size();
            //to do for SUBJECT | OBJECT
            //for the moment, cannot get these values directIProducerQP      
            case SUBJECT:
            case OBJECT:
            default:
                return NA;
        }
    }

    /**
     * @return the query
     */
    public Query getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    @Override
    public Edge copy(Edge ent) {
        if (EdgeManagerIndexer.test) {
            return graph.getEdgeFactory().copy(ent);
        }
        return ent;
    }

    @Override
    public void close() {

    }
    
    public DatatypeValueFactory getDatatypeValueFactory() {
        return DatatypeMap.getDatatypeMap();
    }
}
