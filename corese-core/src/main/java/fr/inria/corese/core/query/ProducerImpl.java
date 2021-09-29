package fr.inria.corese.core.query;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
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
import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.producer.DataBrokerExtern;
import fr.inria.corese.core.producer.DataBrokerLocal;
import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.triple.function.term.Binding;
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
        implements Producer, IProducerQP 
{
    public static final int OWL_RL = 1;
    static final int IGRAPH = Graph.IGRAPH;
    static final int ILIST = Graph.ILIST;
    public static final String TOPREL = Graph.TOPREL;
    
    List<Edge> empty = new ArrayList<>(0);
    List<Node> emptyFrom = new ArrayList<>(0);
    DataProducer ei;
    private DataManager dataManager;
    private DataBroker dataBroker;
    Graph graph,
            // cache for handling (fun() as var) created Nodes
            local;
    Mapper mapper;
    MatcherImpl match;
    QueryEngine qengine;
    FuzzyMatch fuzzy = new FuzzyMatch();
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

    Node getExtNode(Node node) {
        return getDataBroker().getNode(node);
    }
    

    // eg BIND(node as ?x)
    boolean isExtern(Node node) {
        return node.getIndex() == -1
                || node.getTripleStore() != graph;
    }

    boolean isType(Query q, Edge edge) {
        return getDataBroker().isTypeProperty(q, edge);
    }
    

    /**
     *
     * @param namedGraphURI : null or named graph URI 
     * stemming from graph URI { } or graph VAR {}
     * when VAR, namedGraphURI is a value of VAR computed by kgram
     * Note: namedGraphURI may be undefined in target graph
     * @param from : null/empty or default graph specification with select from
     * @param edge : query Edge
     * @param env : Environment with partial variable bindings 
     * if namedGraphURI == null:
     * query default graph, possibly with from, eliminate duplicate edges (same
     * edge with different named graph) 
     * if namedGraphURI != null: query named graph, "from" is useless here because kgram
     * iterate from named   
     */
    @Override
    public Iterable<Edge> getEdges(Node namedGraphURI, List<Node> from, Edge edge,
            Environment env) {
        //System.out.println("PI: enter " + namedGraphURI);
        Node predicate = getPredicate(edge, env);
        if (predicate == null) {
            return empty;
        }

        Query q = env.getQuery();
        if (q.isRule()) {
            Iterable<Edge> it = getRuleEdgeList(q, edge, env, namedGraphURI, from, predicate);
            if (it != null) {
                return it;
            }
        }        
        
        ASTQuery ast =  q.getAST();
        //int level = -1;
        int focusNodeIndex = 0;

        Node focusNode = null, objectNode = null;
        boolean isType = false;
      
        for (Index ei : graph.getIndexList()) {
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
                            && isType(q, edge)) { //&& graph.hasEntailment())                     
                        // RDFS entailment on ?x rdf:type c:Engineer
                        // RDFS entailment does not compute transitive closure of subClassOf
                        // but it checks subsumption with specific method
                        // hence no focus on object node because
                        // we want no dichotomy on c:Engineer because we check subsumption                           
                    } 
                    else {
                        Node val = qNode.isVariable() ? env.getNode(qNode) : null;
                        // candidate query node value:
                        focusNode = getValue(qNode, val, env);

                        if (focusNode == null) {
                            if (qNode.isConstant() || val != null) {
                                // search a value that is not in the graph: fail
                                // here we take into account subject/object/graph
                                // if named graph is undefined we fail here
                                //System.out.println("exit on: " + qNode + " " + focusNode);
                                return empty;
                            }
                        } else {
                            focusNodeIndex = index;
                            if (index == 0 && !isType(q, edge)) {
                                // in case query object Node also have a value
                                objectNode = getValue(edge.getNode(1),  env);
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
            it = graph.getDataStore().getDefault(emptyFrom).iterate(predicate, focusNode, focusNodeIndex);
        } else {
            boolean skip = graph.isEdgeMetadata() && edge.nbNode()==2;
            it = getEdges(namedGraphURI, getNode(namedGraphURI, env), from, predicate, focusNode, objectNode, focusNodeIndex, skip, getAccessRight(env));
        }
        // in case of local Matcher
        it = localMatch(it, namedGraphURI, edge, env);

        return it;
    }      
    
    // special case with tricky optimizations for rule engine
    Iterable<Edge> getRuleEdgeList(Query q, Edge edge, Environment env, Node gNode, List<Node> from, Node predicate) {
        if (q.getEdgeList() != null
                && edge.getIndex() == q.getEdgeIndex()) {
            // transitive rule (see RuleEngine)
            // there is a list of candidate edges
            return q.getEdgeList();
        } else {
            Exp exp = env.getExp();
            if (exp != null && exp.getEdge() == edge && exp.getLevel() != -1) {
                int level = exp.getLevel();
                // int n = ILIST;
                // rule engine requires new edges with level >= exp.getLevel()
                // ILIST is index of specific Edge Index sorted by reverse level
                Iterable<Edge> it = graph.getDataStore().getDefault(from).level(level).iterate(predicate, null, ILIST);
                return localMatch(it, gNode, edge, env);
            }
        }
        return null;
    }
    
    AccessRight getAccessRight(Environment env) {
        Binding b = (Binding) env.getBind();
        return b.getAccessRight();
    }
    

    /**
     * Enumerate candidate edges either from default graph or from named graphs
     * predicate: property name or Graph.TOPREL when predicate is unbound variable
     * focusNode: subject/object/graph node if any of them is known (constant or bound variable), otherwise null
     * objectNode: the object Node, possibly null
     * int n:  the index of focusNode in the triple: subject=0, object=1, graph=Graph.IGRAPH
     */
    public Iterable<Edge> getEdges(Node queryGraphNode, Node targetGraphNode, List<Node> from,
            Node predicate, Node focusNode, Node objectNode, int focusNodeIndex, boolean skip, AccessRight access) {
        if (hasDataManager()) {
            // external graph iterator
            return getExternalEdges(queryGraphNode, targetGraphNode, from, predicate, focusNode, objectNode, focusNodeIndex);
        } else {
            // corese graph iterator
            return getDataProducer(queryGraphNode, targetGraphNode, from, skip, access)
                    .iterate(predicate, focusNode, focusNodeIndex);
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
            List<Node> from, Node predicate, Node focusNode, Node objectNode, int focusNodeIndex) {
        
        List<Node> list = getFrom(targetGraphNode, from);
        Node subject = (focusNodeIndex==0)?focusNode:null;
        Node property = predicate;
        if (predicate.getLabel().equals(TOPREL)) {
            // unbound property variable has TOPREL for property name
            property = null;
        }
        
//        System.out.println(String.format("External iterator: g: %s s: %s p: %s o: %s" , 
//                targetGraphNode, subject, predicate, objectNode));
//        System.out.println("from: " + from);
        
        // @TODO
        // predicate = cos:Property when it is a variable
        
        //trace(getDataManager().iterate(predicate, focusNode, focusNodeIndex));
        
        return getDataBroker().getEdgeList(subject, property, objectNode, list);
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
     * @deprecated
     * User may provide another DataManager factory 
     */
    //@Override
    public DataManager newInstance(Node queryGraphNode, Node targetGraphNode, List<Node> from, boolean skipMetadataNode, AccessRight access) {
        return getDataProducer(queryGraphNode, targetGraphNode, from, skipMetadataNode, access);
    }
    

    /**
     * 
     * @param queryGraphNode: named graph or null (query node)
     * @param targetGraphNode: target named graph  (graph node)
     * @param from:  from or from named or null
     * @param skip: skip nodes other than subject/object/graph to determine that triples are different (default false)
     * @param access: access right to skip triples whose access would be forbidden
     * @return DataProducer
     */
    DataProducer getDataProducer(Node queryGraphNode, Node targetGraphNode, List<Node> from, boolean skip, AccessRight access) {
        DataProducer dp;
        if (queryGraphNode == null) {
            dp = graph.getDataStore().getDefault(from).setSkipEdgeMetadata(skip);
        } else {
            dp = graph.getDataStore().getNamed(from, targetGraphNode).setSkipEdgeMetadata(skip);
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
     * Edge Iterator with rdfs entailment, ?x a us:Person return one occurrence of
     * each value of ?x
     * deprecated:
     * Perform local Ontology match.match() Enable to
     * have a local ontology in case of several graphs with local ontologies In
     * addition, 
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
                //	start = qNode;
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
                predicate =  exp.getDatatypeValue();
            } else {
                return empty;
            }
        }

        Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index, graph.isEdgeMetadata(), getAccessRight(env));

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
                Iterable<Edge> it = getEdges(gNode, src, from, predicate, start, null, index, graph.isEdgeMetadata(), getAccessRight(env));

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
        return  n.getValue();
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
        //cache.clear();
    }

    @Override
    public void start(Query q) {
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
        IDatatype dt =  node.getValue();
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

//    private boolean isSelfValue() {
//        return selfValue;
//    }
//
//    public void setSelfValue(boolean selfValue) {
//        this.selfValue = selfValue;
//    }

    /**
     * Overloading of graph ?g { } The value of ?g may an extended graph
     * producer
     */
    @Override
    public boolean isProducer(Node node) {
        IDatatype dt =  node.getValue();
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
        IDatatype dt =  node.getValue();
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

    
//    public int getIndex() {
//        return index;
//    }
//
//    
//    public void setIndex(int index) {
//        this.index = index;
//    }

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
        }
        else {
            setDataBroker(new DataBrokerExtern(dataManager){});
        }
    }
    
    public boolean hasDataManager() {
        return getDataManager()!=null;
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
}
