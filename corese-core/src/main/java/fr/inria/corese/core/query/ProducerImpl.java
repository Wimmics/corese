package fr.inria.corese.core.query;

import static fr.inria.corese.kgram.sorter.core.Const.ALL;
import static fr.inria.corese.kgram.sorter.core.Const.NA;
import static fr.inria.corese.kgram.sorter.core.Const.OBJECT;
import static fr.inria.corese.kgram.sorter.core.Const.PREDICATE;
import static fr.inria.corese.kgram.sorter.core.Const.SUBJECT;
import static fr.inria.corese.kgram.sorter.core.Const.TRIPLE;
import static fr.inria.corese.sparql.triple.parser.Metadata.RDF_STAR_SELECT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.Index;
import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.index.EdgeManagerIndexer;
import fr.inria.corese.core.producer.DataBrokerExtern;
import fr.inria.corese.core.producer.DataBrokerLocal;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
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
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.sorter.core.IProducerQP;
import fr.inria.corese.kgram.sorter.core.QPGNode;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 * Producer Implement getEdges() for KGRAM interpreter rely on
 * graph.getDataStore().getDefault() graph.getDataStore().getNamed()
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class ProducerImpl
        implements Producer, IProducerQP {
    private static Logger logger = LoggerFactory.getLogger(ProducerImpl.class);

    public static final int OWL_RL = 1;
    static final int IGRAPH = Graph.IGRAPH;
    static final int ILIST = Graph.ILIST;
    public static final String TOPREL = Graph.TOPREL;

    List<Edge> empty = new ArrayList<>(0);
    List<Node> emptyFrom = new ArrayList<>(0);
    DataProducer ei;
    private DataManager dataManager;
    private DataBroker dataBroker;
    private Graph graph;
    Graph local;
    Mapper mapper;
    MatcherImpl match;
    QueryEngine qengine;
    RDFizer toRDF;
    Node graphNode;
    private Query query;

    // if true, perform local match
    boolean isMatch = false;
    private boolean speedUp = false;
    int mode = DEFAULT;

    private ProducerImplNode pn;

    public ProducerImpl() {
        this(Graph.create());
    }

    public ProducerImpl(Graph g) {
        graph = g;
        local = Graph.create();
        mapper = new Mapper(this);
        ei = DataProducer.create(g);
        toRDF = new RDFizer();
        setDataBroker(new DataBrokerLocal(g));
        setNodeIterator(new ProducerImplNode(this));
    }

    public static ProducerImpl create(Graph g) {
        ProducerImpl p = new ProducerImpl(g);
        return p;
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
        // named graph id may be a bnode, 
        // in this case the bnode is already a target graph Node
        if (gNode.isBlank()) {
            return gNode;
        }
        return getValue(gNode, env);
    }

    /**
     * Possibly get a Node with a different number datatype than required for D
     * entailment
     */
    Node getValue(Node qNode, Environment env) {
        return getValue(qNode, qNode.isVariable() ? env.getNode(qNode) : null, env);
    }

    Node getValue(Node qNode, Node node, Environment env) {
        if (qNode.isConstant()) {
            node = getExtNode(qNode);
        } else if (node != null && isExtern(node)) {
            node = getExtNode(node);
        }
        return node;
    }

    Node getExtNode(Node node) {
        return getDataBroker().getNode(node);
    }

    // eg BIND(node as ?x)
    boolean isExtern(Node node) {
        return node.getIndex() == -1
                || node.getTripleStore() != getGraph();
    }

    // ?s rdf:type aClass with corese RDFS entailment
    // do not focus on aClass because RDFS entailment
    // does not generate transitive closure
    // hence another class may match aClass
    // in this case return true
    boolean isSkipTypeObjectNode(Query q, Edge edge) {
        return getDataBroker().isTypeProperty(q, edge);
    }

    /**
     *
     * @param namedGraphURI : null or named graph URI
     *                      stemming from graph URI { } or graph VAR {}
     *                      when VAR, namedGraphURI is a value of VAR computed by
     *                      kgram
     *                      Note: namedGraphURI may be undefined in target graph
     * @param from          : null/empty or default graph specification with select
     *                      from
     * @param edge          : query Edge
     * @param env           : Environment with partial variable bindings
     *                      if namedGraphURI == null:
     *                      query default graph, possibly with from, eliminate
     *                      duplicate edges (same
     *                      edge with different named graph)
     *                      if namedGraphURI != null: query named graph, "from" is
     *                      useless here because kgram
     *                      iterate from named
     */
    @Override
    public Iterable<Edge> getEdges(Node namedGraphURI, List<Node> from, Edge edge,
            Environment env) {
        Node predicate = getPredicate(edge, env);
        if (predicate == null) {
            return empty;
        }

        Query q = env.getQuery();
        if (q.isRule()) { // && ! hasDataManager()) {
            Iterable<Edge> it = getRuleEdgeList(q, edge, env, namedGraphURI, from, predicate);
            if (it != null) {
                return it;
            }
        }

        ASTQuery ast = q.getAST();
        // int level = -1;
        int focusNodeIndex = 0;

        Node focusNode = null, objectNode = null;

        for (Index ei : getGraph().getIndexList()) {
            // enumerate graph index to get the index i of nodes in edge: 0, 1, GRAPHINDEX
            // by convention the index of last is the index of graph node wich is -1
            // search edge query node with a value or which is a constant
            int index = ei.getIndex();
            if (index < edge.nbNode()) {
                // get ith node
                Node qNode = getNode(edge, namedGraphURI, index);

                if (qNode != null) {
                    if (index == 1
                            && qNode.isConstant()
                            && isSkipTypeObjectNode(q, edge)) {
                        // ?s rdf:type aClass with corese RDFS entailment
                        // do not focus on aClass because RDFS entailment
                        // does not generate transitive closure
                        // hence another class may match aClass
                        // in this case skip binding
                    } else {
                        Node val = qNode.isVariable() ? env.getNode(qNode) : null;
                        // candidate query node value:
                        focusNode = getValue(qNode, val, env);

                        if (focusNode == null) {
                            if (qNode.isConstant() || val != null) {
                                // search a value that is not in the graph: fail
                                // here we take into account subject/object/graph
                                // if named graph is undefined we fail here
                                // System.out.println("exit on: " + qNode + " " + focusNode);
                                return empty;
                            }
                        } else {
                            focusNodeIndex = index;
                            if (index == 0 && !isSkipTypeObjectNode(q, edge)) {
                                // use case where query edge object is also known
                                objectNode = getValue(edge.getNode(1), env);
                            }
                            break;
                        }
                    }
                }
            }
        }

        Iterable<Edge> it;

        if (mode == EXTENSION && getQuery() == q) {
            // Producer for an external named graph
            // bind (us:graph() as ?g) graph ?g { }
            // GraphStore external named graph
            it = getGraph().getDataStore().getDefault(emptyFrom).iterate(predicate, focusNode, focusNodeIndex);
        } else {
            // if query edge has no metadata: skip target edge metadata
            boolean skip = getGraph().isEdgeMetadata() && edge.nbNode() == 2;
            it = getEdges(namedGraphURI, getNode(namedGraphURI, env), from, predicate, focusNode, objectNode,
                    focusNodeIndex,
                    skip, getAccessRight(env), isNested(q, edge));
        }
        // in case of local Matcher
        it = localMatch(it, namedGraphURI, edge, env);

        return it;
    }

    boolean isNested(Query q, Edge edge) {
        return edge.isNested() || q.getGlobalAST().hasMetadata(RDF_STAR_SELECT);
    }

    // special case with optimizations for rule engine
    Iterable<Edge> getRuleEdgeList(Query q, Edge edge, Environment env, Node gNode, List<Node> from, Node predicate) {
        if (q.getEdgeList() != null
                && edge.getEdgeIndex() == q.getEdgeIndex()) {
            // transitive rule (see RuleEngine)
            // there is a list of candidate edges
            //logger.info("use query edge list: "+ q.getEdgeList().size() + " " +edge);
            return q.getEdgeList();
        } else if (hasDataManager()) {
            // skip
            //logger.info("datamanager skip: " + edge);
        } else {
            Exp exp = env.getExp();
            if (exp != null && exp.getEdge() == edge && exp.getLevel() != -1) {
                // edge level computed by RuleEngine ResultWatcher
                int level = exp.getLevel();
                // rule engine requires new edges with level >= exp.getLevel()
                // ILIST is index of specific Edge Index sorted by reverse level
                Iterable<Edge> it = getGraph().getDataStore().getDefault(from).level(level).iterate(predicate, null,
                        ILIST);
                return localMatch(it, gNode, edge, env);
            }
        }
        return null;
    }

    AccessRight getAccessRight(Environment env) {
        return env.getBind().getAccessRight();
    }

    /**
     * Enumerate candidate edges either from default graph or from named graphs
     * predicate: property name or Graph.TOPREL when predicate is unbound variable
     * focusNode: subject/object/graph node if any of them is known (constant or
     * bound variable), otherwise null
     * objectNode: the object Node, possibly null
     * int n: the index of focusNode in the triple: subject=0, object=1,
     * graph=Graph.IGRAPH
     * nested == false: return asserted edges
     * nested == true: return nested and asserted edges
     */
    public Iterable<Edge> getEdges(Node queryGraphNode, Node targetGraphNode, List<Node> from,
            Node predicate, Node focusNode, Node objectNode, int focusNodeIndex,
            boolean skip, AccessRight access, boolean nested) {
        if (hasDataManager()) {
            // external graph iterator
            return getExternalEdges(queryGraphNode, targetGraphNode, from,
                    predicate, focusNode, objectNode, focusNodeIndex, nested);
        } else {
            // corese graph iterator
            return getDataProducer(queryGraphNode, targetGraphNode, from, skip, access, nested)
                    .iterate(predicate, focusNode, objectNode, focusNodeIndex);
        }
    }

    /**
     * 
     * @param queryGraphNode
     * @param targetGraphNode
     * @param from
     * @param predicate
     * @param focusNode
     * @param objectNode
     * @param focusNodeIndex
     * @return
     */
    public Iterable<Edge> getExternalEdges(Node queryGraphNode, Node targetGraphNode,
            List<Node> from, Node predicate, Node focusNode, Node objectNode, int focusNodeIndex, boolean nested) {

        List<Node> list = getFrom(targetGraphNode, from);
        Node subject = (focusNodeIndex == 0) ? focusNode : null;
        Node object = (focusNodeIndex == 1) ? focusNode : objectNode;
        Node property = predicate;
        if (predicate.getLabel().equals(TOPREL)) {
            // unbound property variable has TOPREL for property name
            property = null;
        }

        return getDataBroker().getEdgeList(subject, property, object, list);
    }

    // use case: xt:exists(s, p, o) -- see PluginImpl exists()
    // future use case: xt:edge(s, p, o [,g])
    @Override
    public Iterable<Edge> getEdges(Node s, Node p, Node o, List<Node> from) {
        if (hasDataManager()) {
            return getDataManager().getEdges(s, p, o, from);
        }
        return getLocalEdges(s, p, o, from);
    }

    List<Node> nodeList(Node node) {
        if (node == null) {
            return null;
        }
        return List.of(node);
    }

    Edge result(Iterable<Edge> it) {
        if (it == null) {
            return null;
        }
        Iterator<Edge> iter = it.iterator();
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    @Override
    public Edge insert(Node g, Node s, Node p, Node o) {
        if (hasDataManager()) {
            Iterable<Edge> it = getDataManager().insert(s, p, o, nodeList(g));
            return result(it);
        }
        return getGraph().insert(g, s, p, o);
    }

    @Override
    public Iterable<Edge> delete(Node g, Node s, Node p, Node o) {
        if (hasDataManager()) {
            return getDataManager().delete(s, p, o, nodeList(g));
        }
        return getGraph().delete(g, s, p, o);
    }

    Iterable<Edge> getLocalEdges(Node s, Node p, Node o, List<Node> from) {
        DataProducer dp = new DataProducer(getGraph()).setDuplicate(true);
        if (from != null && !from.isEmpty()) {
            // dp.from(from.get(0));
            dp.fromSelect(from);
        }
        return dp.iterate(s, p, o);
    }

    List<Node> getFrom(Node graph, List<Node> from) {
        if (graph == null) {
            return from;
        }
        ArrayList<Node> list = new ArrayList<>();
        list.add(graph);
        return list;
    }

    void trace(Iterable<Edge> it) {
        System.out.println("trace iterator:");
        for (Edge e : it) {
            System.out.println(e);
        }
    }

    /**
     * 
     * @param queryGraphNode:  named graph or null (query node)
     * @param targetGraphNode: target named graph (graph node)
     * @param from:            from or from named or null
     * @param skip:            skip nodes other than subject/object/graph to
     *                         determine that triples are different (default false)
     *                         use case: rdf star reference node does not count to
     *                         determine if two triples are different
     * @param access:          access right to skip triples whose access would be
     *                         forbidden
     * @return DataProducer
     */
    DataProducer getDataProducer(Node queryGraphNode, Node targetGraphNode, List<Node> from,
            boolean skip, AccessRight access, boolean nested) {
        DataProducer dp;
        if (queryGraphNode == null) {
            dp = getGraph().getDataStore().getDefault(from).setSkipEdgeMetadata(skip);
        } else {
            dp = getGraph().getDataStore().getNamed(from, targetGraphNode).setSkipEdgeMetadata(skip);
        }
        if (AccessRight.isActive()) {
            dp.access(access);
        }
        if (getGraph().isRDFStar()) {
            // asserted query edge require asserted target edge
            dp.status(nested);
        }
        return dp;
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) throws SparqlException {
        if (env instanceof Memory) {
            if (env.getQuery().isDebug()) {
                System.out.println("BGP:\n" + exp);
            }
            Memory mem = (Memory) env;
            Eval eval = mem.getEval();
            // prevent loop on BGP exp:
            exp.setType(Exp.AND);
            Mappings map = eval.subEval(this, gNode, gNode, exp, exp, null);
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
     * Edge Iterator with rdfs entailment, ?x a us:Person return one occurrence of
     * each value of ?x
     * deprecated:
     * Perform local Ontology match.match() Enable to
     * have a local ontology in case of several graphs with local ontologies In
     * addition,
     */
    Iterable<Edge> localMatch(Iterable<Edge> it, Node gNode, Edge edge, Environment env) {
        if (isMatch && !env.getQuery().isRelax()) {
            MatchIterator mit = new MatchIterator(it, gNode, edge, getGraph(), env, match);
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
            predicate = getDataBroker().getProperty(predicate);
        }
        return predicate;
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
                // start = qNode;
                start = getExtNode(qNode);
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
                start = getExtNode(start); // graph.getNode(start);
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

        Node predicate = getDataBroker().getProperty(exp.getLongName());
        if (predicate == null) {
            if (isdb) {
                predicate = exp.getDatatypeValue();
            } else {
                return empty;
            }
        }
        Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index,
                getGraph().isEdgeMetadata(), getAccessRight(env), isNested(env.getQuery(), edge));

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
        MetaIterator<Edge> meta = new MetaIterator<>();
        for (Node predicate : getDataBroker().getPropertyList()) {
            if (match(exp, predicate)) {
                // exclude
            } else {
                Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index,
                        getGraph().isEdgeMetadata(), getAccessRight(env), isNested(env.getQuery(), edge));

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
        return getNodeIterator().getNodeIterator(gNode, from, edge, env, exp, index);
    }

    /**
     * Return list of named graph Nodes, possibly in from
     * use case 1: eval graph ?g {} ; ?g in unbound ; node = ?g
     * use case 2: select from g1 g2 { s p* o}
     * Note: parameter node is now useless
     * 
     * @return subset of from defined in target graph
     */
    @Override
    public Iterable<Node> getGraphNodes(Node node, List<Node> from,
            Environment env) {
        return getDataBroker().getGraphList(from);
    }

    /**
     * Cast Java value into IDatatype
     */
    @Override
    public IDatatype getValue(Object value) {
        return DatatypeMap.getValue(value);
    }

    IDatatype nodeValue(Node n) {
        return n.getValue();
    }

    // cast IDatatype or Java value into DatatypeValue
    @Override
    public IDatatype getDatatypeValue(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof IDatatype) {
            return (IDatatype) value;
        } else {
            return getValue(value);
        }
    }

    /**
     * Return a Node given a value (IDatatype value)
     * Use case:
     * select/bind (exp as node)
     * return the Node in the graph if any or return the IDatatype value as is
     * (to speed up)
     * get named graph Node
     *
     */
    @Override
    synchronized public Node getNode(Object value) {
        if (!(value instanceof IDatatype)) {
            return DatatypeMap.createObject(value);
        }
        return getNode((IDatatype) value);
    }

    public Node getNode(IDatatype dt) {
        if (dt.isFuture()) {
            // future: template intermediate result
            return dt;
        }
        Node node = getGraph().getNode(dt, false, false);
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
        // cache.clear();
    }

    @Override
    public void start(Query q) {
        getGraph().getEventManager().start(event(q), q.getAST());
    }

    @Override
    public void finish(Query q) {
        getGraph().getEventManager().finish(event(q), q.getAST());
    }

    Event event(Query q) {
        if (q.getAST().isUpdate()) {
            // fake query exec on global update query for init purpose (Visitor)
            return Event.InitUpdateQuery;
        }
        if (q.isInitMode()) {
            // fake select where query for initialization purpose e.g. Visitor
            return Event.InitQuery;
        }
        // sparql query or update where part
        return Event.Query;
    }

    @Override
    public void initPath(Edge edge, int index) {

    }

    @Override
    public boolean isBindable(Node node) {
        IDatatype dt = node.getValue();
        // 1 && 1.0 are not same Node: cannot bind (see kgram Eval)
        return !dt.isNumber();
    }

    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        Node node = getValue(gNode, env);
        if (node != null && getGraph().containsCoreseNode(node)) {
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
    public Mappings map(List<Node> nodes, IDatatype object) {
        return mapper.map(nodes, object);
    }

    @Override
    public Mappings map(List<Node> nodes, IDatatype object, int n) {
        return mapper.map(nodes, object, n);
    }

    @Override
    public List<Node> toNodeList(IDatatype obj) {
        return mapper.toNodeList(obj);
    }

    void filter(Environment env) {
        // KGRAM exp for current edge
        Exp exp = env.getExp();
        List<String> lVar = new ArrayList<>();
        List<Node> lNode = new ArrayList<>();

        for (Filter f : exp.getFilters()) {
            // filters attached to current edge
            if (f.getExp().isExist()) {
                // skip exists { PAT }
                continue;
            }

            // function exp.bind(f) tests whether current edge binds all variable of filter
            // f
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

    /**
     * Overloading of graph ?g { } The value of ?g may an extended graph
     * producer
     */
    @Override
    public boolean isProducer(Node node) {
        IDatatype dt = node.getValue();
        if (dt.getNodeObject() != null) {
            return toRDF.isGraphAble(dt.getNodeObject()) || dt.getNodeObject() instanceof Producer;
        }
        // system named graph in a GraphStore
        return getGraph().getNamedGraph(node.getLabel()) != null;
    }

    /**
     * Node contains a Graph: return Producer for this Graph
     */
    @Override
    public Producer getProducer(Node node, Environment env) {
        IDatatype dt = node.getValue();
        Object obj = dt.getNodeObject();
        Graph g = null;

        if (obj == null) {
            g = getGraph().getNamedGraph(node.getLabel());
        } else if (obj instanceof Producer) {
            return (Producer) obj;
        } else {
            g = toRDF.getGraph(obj);
        }

        if (g == null) {
            g = Graph.create();
        }
        ProducerImpl p = getProducer(g, env.getQuery());
        return p;
    }

    public static ProducerImpl getProducer(Graph g, Query q) {
        ProducerImpl p = getProducer(g);
        p.setQuery(q);
        return p;
    }

    public static ProducerImpl getProducer(Graph g) {
        ProducerImpl p = ProducerImpl.create(g);
        MatcherImpl m = MatcherImpl.create(g);
        p.setMode(EXTENSION);
        p.setMatch(true);
        p.set(m);
        return p;
    }

    public boolean isSpeedUp() {
        return speedUp;
    }

    public void setSpeedUp(boolean speedUp) {
        this.speedUp = speedUp;
    }

    // public int getIndex() {
    // return index;
    // }
    //
    //
    // public void setIndex(int index) {
    // this.index = index;
    // }

    // @todo: useless
    // record named graph node when node is a Producer in order to manage backtrack
    // see Eval edge() & path()
    // useless because now gNode is an URI, not a variable
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
                // Iterator<Node> it = this.graph.getProperties().iterator();
                // while (it.hasNext()) {
                // Node predNode = it.next();
                // if
                // (qpgn.getExpNode(PREDICATE).getLabel().equalsIgnoreCase(predNode.getLabel()))
                // {
                // return this.graph.size(predNode);
                // }
                // }
                Node pred = getGraph().getPropertyNode(qpgn.getExpNode(PREDICATE).getLabel());
                if (pred != null) {
                    return this.getGraph().size(pred);
                }

                // case 3: not found
                return 0;
            // to do for SUBJECT | OBJECT
            // for the moment, cannot get these values directly
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
                return this.getGraph().size();
            case PREDICATE:
                return this.getGraph().getIndex().size();
            // to do for SUBJECT | OBJECT
            // for the moment, cannot get these values directIProducerQP
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
            return getGraph().getEdgeFactory().copy(ent);
        }
        return ent;
    }

    @Override
    public void close() {

    }

    @Override
    public DatatypeValueFactory getDatatypeValueFactory() {
        return DatatypeMap.getDatatypeMap();
    }

    /**
     * DataManager for external graph
     * With corese graph, dataManager = null,
     * and there is a corese DataBrokerLocal
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Define external DataManager
     */
    public void defineDataManager(DataManager dataManager) {
        setDataManager(dataManager);
        defineDataBroker(dataManager);
    }

    /**
     * Broker between ProducerImpl and DataManager
     * Enables us to tune plugin between corese and external graph
     */
    void defineDataBroker(DataManager dataManager) {
        if (dataManager instanceof DataProducer) {
            // @note: for testing purpose
            // DataProducer on a corese graph
            DataProducer dp = (DataProducer) dataManager;
            setDataBroker(new DataBrokerLocal(dp.getGraph()));
        } else {
            setDataBroker(new DataBrokerExtern(dataManager) {
            });
        }
    }

    @Override
    public boolean hasDataManager() {
        return getDataManager() != null;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public ProducerImplNode getNodeIterator() {
        return pn;
    }

    public void setNodeIterator(ProducerImplNode pn) {
        this.pn = pn;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public String blankNode() {
        if (hasDataManager()) {
            return getDataManager().blankNode();
        }
        return getGraph().newBlankID();
    }
}
