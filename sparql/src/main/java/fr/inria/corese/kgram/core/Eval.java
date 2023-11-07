package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import static fr.inria.corese.kgram.api.core.ExprType.UNNEST;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Loopable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Plugin;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.api.query.Results;
import fr.inria.corese.kgram.api.query.SPARQLEngine;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.kgram.event.EventImpl;
import fr.inria.corese.kgram.event.EventListener;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.path.PathFinder;
import fr.inria.corese.kgram.tool.Message;
import fr.inria.corese.kgram.tool.ResultsImpl;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;

/**
 * KGRAM Knowledge Graph Abstract Machine Compute graph homomorphism and
 * (extended) SPARQL Use: a Stack of expression Exp a Memory for Node/Edge
 * bindings an abstract Producer of candidate Node/Edge an abstract Evaluator of
 * Filter an abstract Matcher of Node/Edge
 *
 * - path statement is an EDGE with a boolean isPath this edge needs an Edge
 * Node (a property variable)
 *
 *
 * TODO: optimize: query ordering, search by dichotomy (in a cache)
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Eval implements ExpType, Plugin {

    // true = new processing of named graph 
    public static boolean NAMED_GRAPH_DEFAULT = true;
    static Logger logger = LoggerFactory.getLogger(Eval.class);
    public static boolean JOIN_MAPPINGS = true;

    // draft test: when edge() has Mappings map parameter, push clause values(map)
    private static boolean pushEdgeMappings = true;
    // draft test: graph() has Mappings map parameter and eval body with map parameter
    private static boolean parameterGraphMappings = true;
    // draft test: union() has Mappings map parameter and eval branch with map parameter
    private static boolean parameterUnionMappings = true;
    public static int DISPLAY_RESULT_MAX = 10;
    
    static final int STOP = -2;
    public static int count = 0;
    private ResultListener listener;
    EventManager manager;
    private ProcessVisitor visitor;
    private SPARQLEngine sparqlEngine;
    boolean hasEvent = false;
    boolean namedGraph = NAMED_GRAPH_DEFAULT;
    // Edge and Node producer
    private Producer producer;
    // Edge and Node producer
    Producer saveProducer;
    Provider provider;
    // Filter evaluator
    Evaluator evaluator;
    Matcher match;
    List<PathFinder> lPathFinder;
    // Processing EXTERN expressions
    Plugin plugin;
    // Stacks for binding edges and nodes
    Memory memory;
    private Stack current;
    Query query;
    Exp maxExp;
    Node nn;
    Exp edgeToDiffer;
    Mapping mapping;
    Mappings results,
            // initial results to be completed
            initialResults;
    EvalSPARQL evalSparql;
    CompleteSPARQL completeSparql;
    List<Node> empty = new ArrayList<>(0);
    //HashMap<String, Boolean> local;
    
    EvalGraph evalGraphNew;
    EvalJoin join;
    EvalOptional optional;
    
    static {
        setNewMappingsVersion(true);
    }

    int // count number of eval() calls
            nbEdge = 0, nbCall = 0,
            rcount = 0,
            backjump = -1, indexToDiffer = -1,
            // max level in stack for debug
            level = -1,
            maxLevel = -1,
            limit = Integer.MAX_VALUE;
    private boolean debug = false;
    // subeval = false: eval query/subquery
    // subeval = true:  eval statement (union, optional, minus, graph, join)
    boolean isSubEval = false,
            // return only select variables in Mapping
            onlySelect = true,
            optim = true,
            draft = true;
    private boolean hasListener = false;
    boolean storeResult = true;
    private int nbResult;
    boolean hasFilter = false;
    private boolean hasCandidate = false,
            hasStatement = false,
            hasProduce = false;
    private boolean stop = false;
    private boolean joinMappings = JOIN_MAPPINGS;
    
    public Eval() {
    }

    /**
     *
     * @param p edge and node producer
     * @param e filter evaluator, given an environment (access to variable
     * binding)
     */
    public Eval(Producer p, Evaluator e, Matcher m) {
        producer = p;
        saveProducer = producer;
        evaluator = e;
        match = m;
        plugin = this;
        lPathFinder = new ArrayList<>();
        setVisitor(new ProcessVisitorDefault());
        e.setKGRAM(this);
        evalGraphNew = new EvalGraph(this);
        join = new EvalJoin(this);
        optional = new EvalOptional(this);

    }

    public static Eval create(Producer p, Evaluator e, Matcher m) {
        return new Eval(p, e, m);
    }

    public void set(Provider p) {
        provider = p;
    }

    public void set(Producer p) {
        setProducer(p);
    }

    public void set(Matcher m) {
        match = m;
    }

    public void set(Evaluator e) {
        evaluator = e;
    }

    @Deprecated
    public Results exec(Query q) throws SparqlException {
        Mappings maps = query(q, null);
        return ResultsImpl.create(maps);
    }

    /**
     * Main eval function
     */
    public Mappings query(Query q) throws SparqlException {
        return query(null, q, (Mapping) null);
    }
    
    public Mappings query(Query q, Mapping m) throws SparqlException {
        return query(null, q, m);
    }
    
    public Mappings query(Node graphNode, Query q, Mapping m) throws SparqlException {
        return queryBasic(graphNode, q, m);
    }
    
    Mappings queryBasic(Node graphNode, Query q, Mapping m) throws SparqlException {
        if (hasEvent) {
            send(Event.BEGIN, q);
        }
        initMemory(q);
        share(m);
        getProducer().start(q);
        getVisitor().init(q);
        share(getVisitor());
        getVisitor().before(q);
        Mappings map = eval(graphNode, q, m, null);
        getVisitor().orderby(map);
        getVisitor().after(map);

        getProducer().finish(q);
        if (hasEvent) {
            send(Event.END, q, map);
        }
        map.setBinding(getBind());
        clean();
        return map;
    }

    // share global variables and ProcessVisitor
    void share(Mapping m) {
        if (m != null && m.getBind() != null) {
            if (getBind() != null) {
                getBind().share(m.getBind());
            }
            if (m.getBind().getVisitor() != null) {
                // use case: let (?g = construct where)
                // see Interpreter exist() getMapping()
                setVisitor(m.getBind().getVisitor());
            }
        }
    }

    // store ProcessVisitor into Bind for future sharing by
    // Transformer and Interpreter exist
    // use case: metadata @share
    void share(ProcessVisitor vis) {
        if (vis.isShareable() && getBind().getVisitor() == null) {
            getBind().setVisitor(vis);
        }
    }

    public void finish(Query q, Mappings map) {
    }

    public Mappings eval(Node gNode, Query q, Mapping map) throws SparqlException {
        return eval(gNode, q, map, null);
    }
    
    /**
     * Mapping m is binding parameter, possibly null
     * a) from template call with parameter:   st:call-template(st:name, ?x, ?y)
     * b) from query exec call with parameter: exec.query(q, m)
     * Mappings map is results or previous statement, possibly null
     * use case: optional(A, B) map = relevant subset of results of A
     * for main query and subquery
     */
    Mappings eval(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        init(q);
        if (q.isValidate()) {
            // just compile and complete query
            return getResults();
        }
        if (q.isCheck()) {
            // Draft
            Checker check = Checker.create(this);
            check.check(q);
        }
       
        if (!q.isFail()) {           
            queryWE(gNode, q, m, map);

            if (q.getQueryProfile() == Query.COUNT_PROFILE) {
                countProfile();
            } else {
                if (q.isAlgebra()) {
                    getMemory().setResults(getResults());
                    completeSparql.complete(getProducer(), getResults());
                }
                aggregate();
                // order by
                complete();
                template();
            }
        }

        if (isDebug() && !isSubEval && !q.isSubQuery()) {
            debug();
        }
        getEvaluator().finish(getMemory());
        return getResults();
    }
    
    int queryWE(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        try {
            return query(gNode, q, m, map);
        } catch (SparqlException ex) {
            if (ex.isStop()) {
                // LDScriptException stop means stop query processing
                return 0;
            }
            // exception means this is an error
            throw ex;
        }
    }

    /**
     * Mapping m is binding parameter, possibly null
     * a) from template call with parameter:   st:call-template(st:name, ?x, ?y)
     * b) from query exec call with parameter: exec.query(q, m)
     * Mappings map is results or previous statement, possibly null
     * use case: optional(A, B) map = relevant subset of results of A
     */
    int query(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        if (m != null) {
            // bind mapping variables into memory
            bind(m);
        }
        if (q.getValues() == null) {
            // no external values
            return eval(gNode, q, map);
        } 
        // external values clause
        // select * where {} values var {}
//        else if (map == null && m == null) {
//            // there is no binding parameter
//            // external values evaluated as join(values, body)
//            return queryWithJoinValues(gNode, q, map);
//        } 
        else {
            // there is binding parameter (m and/or map)
            // Mapping m is bound in memory, keep it, mappings map is passed as eval parameter 
            // bind external values one by one in memory and eval one by one
            return queryWithValues(gNode, q, map);
        }
    }

    /**
     * External values clause evaluated as join(values, body)
     */
    int queryWithJoinValues(Node gNode, Query q, Mappings map)
            throws SparqlException {
        Exp values = Exp.create(AND, q.getValues());
        return evalExp(gNode, q, Exp.create(JOIN, values, q.getBody()), map);
    }
    
    /**
     * 
     * Bind external values one by one in memory and eval one by one
     */
    int queryWithValues(Node gNode, Query q, Mappings map)
            throws SparqlException {
        Exp values = q.getValues();

        if (!values.isPostpone() && !q.isAlgebra()) {
            for (Mapping m : values.getMappings()) {
                if (stop) {
                    return STOP;
                }
                if (valuesBinding(values.getNodeList(), m, -1)) {
                    eval(gNode, q, map);
                    free(values.getNodeList(), m);
                }
            }
            return 0;
        }
        return eval(gNode, q, map);
    }
    
    int eval(Node gNode, Query q, Mappings map) throws SparqlException {
        return evalExp(gNode, q, q.getBody(), map);
    }


    int evalExp(Node gNode, Query q, Exp exp, Mappings map)
            throws SparqlException {
        Stack stack = Stack.create(exp);
        set(stack);
        return eval(getProducer(), gNode, stack, map, 0);
    }
    
    /**
     * We just counted number of results: nbResult Just build a Mapping
     */
    void countProfile() {
       // Node n = getEvaluator().cast(nbResult, getMemory(), getProducer());
        Node n = DatatypeMap.newInstance(nbResult);
        Mapping m = Mapping.create(getQuery().getSelectFun().get(0).getNode(), n);
        getResults().add(m);
    }    
    
//    public Mappings filter(Mappings map, Query q) throws SparqlException {
//        Query qq = map.getQuery();
//        init(qq);
//        qq.compile(q.getHaving().getFilter());
//        qq.index(qq, q.getHaving().getFilter());
//        map.filter(getEvaluator(), q.getHaving().getFilter(), getMemory());
//        return map;
//    }    

   

    /**
     * Subquery processed by a function call that return Mappings Producer may
     * cast the result into Mappings use case: {select xpath(?x, '/book/title')
     * as ?val where {}} Mappings may be completed by filter (e.g. for casting)
     * Mappings will be processed later by aggregates and order by/limit etc.
     */
    private void function() throws SparqlException {
        Exp exp = query.getFunction();
        if (exp == null) {
            return;
        }
        Mappings lMap = eval(exp.getFilter(), memory, exp.getNodeList());
        if (lMap != null) {
            for (Mapping map : lMap) {
                map = complete(map, getProducer());
                submit(map);
            }
        }
    }

    /**
     * additional filter of functional select xpath() as ?val xsd:integer(?val)
     * as ?int
     */
    private Mapping complete(Mapping map, Producer p) throws SparqlException {
        for (Exp ee : query.getSelectFun()) {
            Filter f = ee.getFilter();
            if (f != null && !f.isFunctional()) {
                memory.push(map, -1);
                Node node = eval(null, f, memory, getProducer());
                memory.pop(map);
                map.setNode(ee.getNode(), node);
            }
        }

        if (query.getOrderBy().size() > 0 || query.getGroupBy().size() > 0) {
            memory.push(map, -1);
            Mapping m = memory.store(query, p, true, true);
            memory.pop(map);
            map = m;
        }
        return map;
    }
    
    // draft for processing EXTERN expression
    public void add(Plugin p) {
        plugin = p;
    }

    public void setMatcher(Matcher m) {
        match = m;
    }

    public void setMappings(Mappings lMap) {
        initialResults = lMap;
    }

    void debug() {
        Message.log(Message.LOOP, nbCall + " " + nbEdge);
        if (results.size() == 0) {
            if (query.isFail()) {
                Message.log(Message.FAIL);
                System.out.println("eval: " + query);
                for (Filter filter : query.getFailures()) {
                    Message.log(filter + " ");
                }
                Message.log();
            } else {
                if (maxExp == null) {
                    Message.log(Message.FAIL_AT, "init phase, e.g. parameter binding");
                } else {
                    Message.log(Message.FAIL_AT);
                    Message.log(maxExp);
                    getTrace().append(String.format("SPARQL fail at: %s", maxExp)).append(Message.NL);
                }
            }
        }
    }
    
    StringBuilder getTrace() {
        return getBind().getTrace();
    }
    
    /**
     * this eval is a fresh copy
     * use by compiler interpreter for exists {}
     */   
    public Mappings subEval(Query q, Node gNode, Stack stack, int n) throws SparqlException {
        return subEval(q, gNode, stack, null, n);
    }
    
    /**
     * main subEval function
     * evaluates exp in new Eval with new Memory
     * for optional, minus, union, join, graph
     * not for subquery which has its own processing
     * 
     */
    Mappings subEval(Query q, Node gNode, Stack stack, Mappings map, int n) throws SparqlException {
        setSubEval(true);
        starter(q);
        if (q.isDebug()) {
            setDebug(true);
        }
        eval(getProducer(), gNode, stack, map, n);

        return getResults();
    }


    /**
     * Eval exp in a fresh new Memory where exp is part of main expression
     * use case: main = optional, minus, union, join
     * Node gNode : actual graph node 
     * Node queryNode : exp query graph node
     * use case: union
     */

    // use case: optional, join, minus
    public Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map) throws SparqlException {
        return subEval(p, gNode, queryNode, exp, main, map, null, false, false);
    }
        
    /**
     * external = false : graphNode is named graph URI or null, queryGraphNode is meaningless
     * external = true :  graphNode is external graph, 
     * queryGraphNode: named graph variable if any or null
     * graphNode: if ext=true & queryNode=null -> graphNode can be null
     * external graph: external graph in GraphStore, PPath or Node graph pointer
     * When external=true: Producer p is new Producer(externalGraph)
     * exp is current exp to evaluate in new Eval with new Memory
     * main is embedding statement of exp (main = A optional B, exp = A | exp = B)
     * map and m are possible bindings stemming from previous statement evaluation
     * 
     */
    Mappings subEval(Producer p, Node graphNode, Node queryGraphNode, Exp exp, Exp main, 
            Mappings map, Mapping m, boolean bind, boolean external) throws SparqlException {    
        Memory mem = new Memory(match, getEvaluator());
        getEvaluator().init(mem);
        // share Binding (global variable, context, etc.)
        mem.share(getMemory());
        mem.init(getQuery());
        mem.setAppxSearchEnv(getMemory().getAppxSearchEnv());
        Eval eval = copy(mem, p);
        if (external) {
            if (queryGraphNode != null) {
                // graph ?queryNode { } -> bind ?queryNode = graphNode for further use
                // usually we do not bind named graph variable before 
                // statement evaluation to preserve sparql semantics
                // external graph is an extension to sparql
                mem.push(queryGraphNode, graphNode, -1);
            }
            // named graph is external graph, not URI -> 
            // do not pass graphNode as named graph URI
            // Producer p is bound to external named graph
            graphNode = null;
        }
        bind(mem, exp, main, map, m, bind);
        Mappings lMap = eval.subEval(getQuery(), graphNode, Stack.create(exp), map, 0);
        return lMap;
    }  
    
    
    /**
     * subEval with bind parameters
     * freshMemory inherits data to evaluate exp 
     *
     */
    void bind(Memory freshMemory, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
        if (m != null) {
            freshMemory.push(m, -1);
        }
        
        // QuerySorter may have computed exp node list candidate for binding
        if (main.isGraph() && main.getNodeList() != null) {
            bindExpNodeList(freshMemory, main, main.getGraphName());
        } else if ((bind || main.isBinary()) && exp.getNodeList() != null) {           
            // A optional B
            // bind variables of A from environment
            bindExpNodeList(freshMemory, exp, null);
        }

        bindService(freshMemory, exp, main);
    }



    /**
     * Bind relevant nodes of exp from memory to fresh memory
     * except may be a graphNode: do not bind it here 
     * because it is bound somewhere else by subEval
     * getNodeList() is computer by QuerySorter
     * 
     */
    void bindExpNodeList(Memory freshMemory, Exp exp, Node except) {
        for (Node qnode : exp.getNodeList()) {
            // getOuterNodeSelf use case: join(subquery, exp)  -- federated query use case
            if (except == null || qnode != except) {
                Node myqnode = getMemory().getQuery().getOuterNodeSelf(qnode);               
                Node node    = getMemory().getNode(myqnode);
                if (node != null) {
                    freshMemory.push(qnode, node, -1);
                }
            }
        }
    }

    /**
     * Bind service variable if any
     */
    void bindService(Memory freshMemory, Exp exp, Exp main) {
        switch (main.type()) {
            case Exp.JOIN:
                service(exp, freshMemory);
        }
    }
    
    /**
     * special case for service variable
     * JOIN(service ?s {}, exp) if ?s is bound in main memory, 
     * bind it in subeval memory
     */
    void service(Exp exp, Memory freshMemory) {
        if (exp.type() == SERVICE) {
            bindService(exp, freshMemory);
        } else {
            for (Exp ee : exp.getExpList()) {
                switch (ee.type()) {
                    case SERVICE:
                        bindService(ee, freshMemory);
                        break;
                    case AND:
                    case BGP:
                    case JOIN:
                        service(ee, freshMemory);
                        break;
                }
            }
        }
    }

    // bind service variable for further use
    void bindService(Exp exp, Memory freshMemory) {
        Node serv = exp.first().getNode();
        if (serv.isVariable() && getMemory().isBound(serv)) {
            freshMemory.push(serv, getMemory().getNode(serv));
        }
    }

    /**
     *
     * Copy current evaluator to eval subquery same memory (share bindings) new
     * exp stack
     */
    Eval copy(Memory m, Producer p) {
        return copy(m, p, getEvaluator(), query, false);
    }

    // extern = true if the statement to evaluate is LDScript query:
    // let (select where)
    public Eval copy(Memory m, Producer p, boolean extern) {
        return copy(m, p, getEvaluator(), query, extern);
    }

    // q may be the subQuery
    Eval copy(Memory m, Producer p, Evaluator e, Query q, boolean extern) {
        Eval ev = create(p, e, getMatcher());
        if (q != null) {
            ev.complete(q);
        }
        ev.setSPARQLEngine(getSPARQLEngine());
        ev.setMemory(m);
        ev.set(getProvider());
        if (!extern || getVisitor().isShareable()) {
            ev.setVisitor(getVisitor());
        }
        ev.startExtFun(q);
        if (hasEvent) {
            ev.setEventManager(manager);
        }
        return ev;
    }
    
    public Memory createMemory(Environment env, Exp exp) {
        if (env instanceof Memory) {
            return getMemory((Memory) env, exp);
        } else if (env instanceof Mapping) {
            return getMemory((Mapping) env, exp);
        } else {
            return null;
        }
    }

    /**
     * copy of Memory may be stored in exp. Reuse data structure after cleaning
     * and init copy current memory content into target memory Use case: exists
     * {}
     */
    public Memory getMemory(Memory memory, Exp exp) {
        Memory mem;
        if (memory.isFake()) {
            // Temporary memory created by PathFinder
            mem = memory;
        } else if (!memory.hasBind() && exp.getObject() != null) {
            mem = (Memory) exp.getObject();
            mem.start();
            memory.copyInto(null, mem, exp);
        } else {
            mem = copyMemory(memory, memory.getQuery(), null, exp);
            exp.setObject(mem);
        }
        return mem;
    }

    /**
     * use case: exists {} in aggregate select (count(if (exists { BGP }, ?x,
     * ?y)) as ?c) Env is a Mapping Copy Mapping into fresh Memory in order to
     * evaluate exists {} in Memory TODO: optimize by storing mem
     *
     *
     */
    public Memory getMemory(Mapping map, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        getEvaluator().init(mem);
        mem.init(query);
        mem.copy(map, exp);
        return mem;
    }

    /**
     * copy memory for sub query copy sub query select variables that are
     * already bound in current memory Use case: subquery and exists
     */
    private Memory copyMemory(Memory memory, Query query, Query sub, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        getEvaluator().init(mem);
        if (sub == null) {
            mem.init(query);
        } else {
            mem.init(sub);
        }
        memory.copyInto(sub, mem, exp);
        if (hasEvent) {
            memory.setEventManager(manager);
        }
        return mem;
    }

    void setLevel(int n) {
        level = n;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public void setSubEval(boolean b) {
        isSubEval = b;
    }

    public Memory getMemory() {
        return memory;
    }
    
    public Binding getBinding() {
        return getEnvironment().getBind();
    }
    
    public Binding getBind() {
        return getEnvironment().getBind();
    }
    
    Query getQuery() {
        return query;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public Matcher getMatcher() {
        return match;
    }

    public Producer getProducer() {
        return producer;
    }

    public Provider getProvider() {
        return provider;
    }

    public Environment getEnvironment() {
        return memory;
    }

    public Mappings getResults() {
        return results;
    }

    void setResult(Mappings r) {
        results = r;
    }

    public void setMemory(Memory mem) {
        memory = mem;
    }

    // total init (for global query)
    public void init(Query q) {
        initMemory(q);
        start(q);
        profile(q);
    }

    void initMemory(Query q) {
        if (memory == null) {
            // when subquery, memory is already assigned
            // assign stack index to EDGE and NODE
            q.complete(getProducer());//service while1 / Query
            memory = new Memory(match, evaluator);
            memory.setEval(this);
            getEvaluator().init(memory);
            // create memory bind stack
            memory.init(q);
            if (hasEvent) {
                memory.setEventManager(manager);
            }
            getProducer().init(q);
            evaluator.start(memory);
            setDebug(q.isDebug());
            if (q.isAlgebra()) {
                complete(q);
            }
            if (isDebug()) {
                System.out.println(q);
            }
        }
    }

    void complete(Query q) {
        evalSparql = new EvalSPARQL(q, this);
        completeSparql = new CompleteSPARQL(q, this);
    }

    void profile(Query q) {
        switch (q.getQueryProfile()) {

            // select (count(*) as ?c) where {}
            // do not built Mapping, just count them
            case Query.COUNT_PROFILE:
                storeResult = false;
        }
    }

    // partial init (for global query and subqueries)
    private void start(Query q) {
        limit = q.getLimitOffset();
        starter(q);
    }

    public void setLimit(int n) {
        limit = n;
    }

    // for sub exp
    private void starter(Query q) {
        query = q;
        // create result holder
        if (initialResults != null) {
            results = initialResults;
        } else {
            results = Mappings.create(query, isSubEval);
        }
        if (hasEvent) {
            results.setEventManager(manager);
        }
        startExtFun(q);
        // set new results in case of sub query (for aggregates)
        memory.setEval(this);
        memory.setResults(results);
    }

    void startExtFun(Query q) {
        hasStatement = getVisitor().statement();
        hasProduce = getVisitor().produce();
        hasCandidate = getVisitor().candidate();
        hasFilter = getVisitor().filter();
    }

    private void complete() {
        results.complete(this);
    }

    private void aggregate() throws SparqlException {
        results.aggregate(evaluator, memory, getProducer());
    }

    private void template() throws SparqlException {
        results.template(evaluator, memory, getProducer());
    }
    
    /**
     * Process map with new query modifier
     * Use case: user edit query modifier and click Modifier button in GUI
     * select distinct select exp 
     * select aggregate group by having
     * order by limit offset
     */
    public Mappings modifier(Query q, Mappings map) throws SparqlException {
        if (q.isDebug()) {
            System.out.println("modifier");
        }
        q.complete(getProducer());        
        Memory env = new Memory(getMatcher(), getEvaluator());
        env.init(q).setBinding(getMemory().getBind()).setResults(map);
        env.setEval(this);
        
        if (q.isDebug()) {
            System.out.println("prepare modifier");
        }
        map.modify(q);
        
        if (q.isDebug()) {
            System.out.println("select expression");
        }
        map.modifySelect(this, q);  
        
        if (q.isDebug()) {
            System.out.println("aggregate");
        }
        map.modifyAggregate(q, getEvaluator(), env, getProducer()); 
        
        if (q.isDebug()) {
            System.out.println("order by");
        }
        map.modifyDistinct();
        map.modifyOrderBy(this, q);
        map.modifyLimitOffset();
        Mappings res = map.modifyValues(q);
        return res;
    }

    /**
     * We can bind nodes before processing query
     */
    boolean bind(Node qnode, Node node) {
        return memory.push(qnode, node, -1);
    }

    /**
     * Bind select nodes of Mapping to [select] nodes of query
     */
    void bind(Mapping map) {
        for (Node qNode : map.getSelectQueryNodes()) {
            Node qqNode = query.getOuterNode(qNode);
            if (qqNode != null) {
                Node node = map.getNode(qNode);
                if (node != null) {
                    bind(qqNode, node);
                    if (isDebug()) {
                        logger.debug("Bind: " + qqNode + " = " + node);
                    }
                }
            }
        }
    }

    private PathFinder getPathFinder(Exp exp, Producer p) {
        List<PathFinder> lp = lPathFinder;
        for (PathFinder pf : lp) {
            if (pf.getEdge() == exp.getEdge()) {
                return pf;
            }
        }
        PathFinder pathFinder = PathFinder.create(this, p, query);
        //pathFinder.setDefaultBreadth(false);
        if (hasEvent) {
            pathFinder.set(manager);
        }
        pathFinder.set(getListener());
        pathFinder.setList(query.getGlobalQuery().isListPath());
        // rdf:type/rdfs:subClassOf* generated system path does not store the list of edges
        // to be optimized
        pathFinder.setStorePath(query.getGlobalQuery().isStorePath() && !exp.isSystem());
        pathFinder.setCache(query.getGlobalQuery().isCachePath());
        // TODO: subQuery 
        pathFinder.setCheckLoop(query.isCheckLoop());
        pathFinder.setCountPath(query.isCountPath());
        pathFinder.init(exp.getRegex(), exp.getObject(), exp.getMin(), exp.getMax());
        // TODO: check this with clean()
        if (p.getMode() == Producer.EXTENSION && p.getQuery() == memory.getQuery()) {
            // do nothing
        } else {
            lPathFinder.add(pathFinder);
        }
        return pathFinder;
    }

    /**
     * What should be done before throw LimitException - close path threads if
     * any
     */
    private void clean() {
        for (PathFinder pf : lPathFinder) {
            pf.stop();
        }
    }

    private int solution(Producer p, Mapping m, int n) throws SparqlException {
        int backtrack = n - 1;
        int status = store(p, m);
        if (status == STOP) {
            return STOP;
        }
        if (results.size() >= limit) {
            clean();
            // backjump to send finish events to listener
            // and perform 'close instructions' if any
            return STOP;
        }
        if (!getVisitor().limit(results)) {
            clean();
            return STOP;
        }
        if (backjump != -1) {
            if (!isSubEval && optim) {
                // use case: select distinct ?x where
                // backjump where ?x is defined to get a new one
                backtrack = backjump;
                backjump = -1;
            }
        } else if (query.isDistinct()) {
            if (!isSubEval && optim) {
                int index = memory.getIndex(query.getSelect());
                if (index != -1) {
                    backtrack = index;
                }
            }
        }
        return backtrack;
    }

    /**
     * gNode is a graph name URI if any, may be null
     */
    int eval(Producer p, Node gNode, Stack stack, int n) throws SparqlException {
        return eval(p, gNode, stack, null, n);
    }
   
    /**
     * Mappings map, possibly null, is result of previous expression that may be used to evaluate current exp
     * optional(s p o, o q r)
     * map = eval(s p o); eval(o q r, map) -> can use o in map
     * map is relevant subset of result, projected on relevant subset of variables of right expression
     * special cases:
     * 1) when there is no relevant subset of results wrt variables, map=full result in case exp is a union
     * 2) relevant subset of result contains full result in case exp is a union
     * case union: each branch of union select its own relevant subset of results from full result
     * Mappings map is recursively passed as parameter until one exp can use it
     * It can be passed recursively through several statements: join(A, optional(union(B, C), D))
     * Eventually, and() edge() path() transform Mappings map into values clause
     */
    int eval(Producer p, Node graphNode, Stack stack, Mappings map, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean isEvent = hasEvent;
        Memory env = getMemory();

        nbCall++;

        if (n >= stack.size()) {
            backtrack = solution(p, null, n);
            return backtrack;
        }

        if (isDebug()) {

            if (n > level
                    || (maxExp.type() == UNION)) {
                Exp ee = stack.get(n);
                if (true){//(ee.type() != AND) {
                    level = n;
                    maxExp = stack.get(n);
                    String s = String.format("%02d", n);
                    Message.log(Message.EVAL, s + " " + maxExp);
                    if (map!=null) {
                        logger.warn(String.format("With mappings:\nvalues %s\n%s",
                                map.getNodeList(), map.toString(false, false, DISPLAY_RESULT_MAX)));
                    }
                    getTrace().append(String.format("Eval: %02d %s", n, maxExp))
                            .append(Message.NL).append(Message.NL);
                }
            }
        }

        if (n > maxLevel) {
            maxLevel = n;
        }

        Exp exp = stack.get(n);
        if (hasListener) {
            // rule engine may have a ResultWatcher listener
            exp = getListener().listen(exp, n);
        }
        
        if (isEvent) {
            send(Event.START, exp, graphNode, stack);
        }

        if (exp.isFail()) {
            // a false filter was detected at compile time
            // or exp was identified as always failing
            // no use to eval this exp
        } else {

            if (exp.isBGPAble()) {
                // @deprecated
                // evaluate and record result for next time
                // template optimization 
                exp.setBGPAble(false);
                backtrack = bgpAble(p, graphNode, exp, stack, n);
                exp.setBGPAble(true);
            } else {
                // draft test not used
                if (query.getGlobalQuery().isAlgebra()) {
                    switch (exp.type()) {
                        case BGP:
                        case JOIN:
                        case MINUS:
                        case OPTIONAL:
                        case GRAPH:
                        case UNION:
                            process(graphNode, p, exp);
                            return backtrack;
                    }
                };

                if (hasStatement) {
                    getVisitor().statement(this, getGraphNode(graphNode), exp);
                }
              
                if (!isJoinMappings()) {
                    // for testing and debug
                    map = null;
                }

                switch (exp.type()) {

                    case EMPTY:

                        eval(p, graphNode, stack, n + 1);
                        break;

                    case AND:                        
                        backtrack = and(p, graphNode, exp, stack, map, n);
                        break;

                    case BGP:
                        backtrack = bgp(p, graphNode, exp, stack, n);
                        break;

                    case SERVICE:
                        // @note: map processing is not optimal for service with union
                        // we pass mappings only for variables that are in-scope in 
                        // both branches of the union
                        // it can be bypassed with values var {undef}
                        backtrack = service(p, graphNode, exp, map, stack, n);
                        break;

                    case GRAPH:
                        backtrack = 
                                evalGraphNew.eval(p, graphNode, exp, map, stack, n);
                        break;

                    case UNION:
                        backtrack = union(p, graphNode, exp, map, stack, n);
                        break;
                    
                    case OPTIONAL:
                        backtrack = optional.eval(p, graphNode, exp, map, stack, n);
                        break;
                    case MINUS:
                        backtrack = minus(p, graphNode, exp, map, stack, n);
                        break;
                    case JOIN:
                        backtrack = join.eval(p, graphNode, exp, map, stack, n);
                        break;
                    case QUERY:
                        backtrack = query(p, graphNode, exp, map, stack, n); 
                        break;    
                    case FILTER:
                        backtrack = filter(p, graphNode, exp, stack, n);
                        break;
                    case BIND:
                        backtrack = bind(p, graphNode, exp, map, stack, n);
                        break;

                    case PATH:
                        backtrack = path(p, graphNode, exp, map, stack, n);
                        break;

                    case EDGE:
                        backtrack = edge(p, graphNode, exp, map, stack, n);
                        break;

                    case VALUES:
                        backtrack = values(p, graphNode, exp, stack, n);
                        break;

                    /**
                     * ********************************
                     *
                     * Draft extensions
                     *
                     */
                    case OPT_BIND:
                        // use case: ?x p ?y FILTER ?t = ?y BIND(?t, ?y) ?z q ?t 
                        // not effective
                        backtrack = optBind(p, graphNode, exp, stack, n);
                        break;

                    case ACCEPT:
                        // use case: select distinct ?x where
                        // check that ?x is distinct
                        if (optim) {
                            if (getResults().accept(env.getNode(exp.getNode()))) {
                                // backjump here when a mapping will be found with this node
                                // see store()
                                backjump = n - 1;
                                backtrack = eval(p, graphNode, stack, n + 1);
                            }
                        } else {
                            backtrack = eval(p, graphNode, stack, n + 1);
                        }
                        break;                                     
                }
            }
        }

        if (isEvent) {
            send(Event.FINISH, exp, graphNode, stack);
        }

        return backtrack;
    }
    



    private int minus(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        //boolean hasGraph = gNode != null;
        Memory env = getMemory();
        Node queryGraphNode = null; //getQuery().getGraphNode();

//        Node node1 = null, node2 = null;
//        if (hasGraph) {
//            node1 = queryGraphNode;
//            node2 = exp.getGraphNode();
//        }
        Mappings map1 = subEval(p, graphNode, queryGraphNode, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        if (map1.isEmpty()) {
            return backtrack;
        }

        MappingSet set1 = new MappingSet(env.getQuery(), map1);
        Exp rest = exp.rest(); 
        Mappings minusMappings = set1.prepareMappingsRest(rest);
        
        Mappings map2 = subEval(p, graphNode, queryGraphNode, rest, exp, minusMappings); 

        getVisitor().minus(this, getGraphNode(graphNode), exp, map1, map2);

        MappingSet set = new MappingSet(env.getQuery(), exp, set1, new MappingSet(memory.getQuery(), map2));
        set.setDebug(getQuery().isDebug());
        set.start();
        
        for (Mapping map : map1) {
            if (stop) {
                return STOP;
            }
            boolean ok = !set.minusCompatible(map);
            if (ok) {
                if (env.push(map, n)) {
                    backtrack = eval(p, graphNode, stack, n + 1);
                    env.pop(map);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }
        return backtrack;
    }

   
    boolean isFederate(Exp exp) {
        if (getQuery().getGlobalQuery().isFederate()) {
            return true;
        }
        return exp.isRecFederate();
    }
        

    private int union(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())

        Mappings map1 = unionBranch(p, graphNode, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        Mappings map2 = unionBranch(p, graphNode, exp.rest(), exp, data);

        getVisitor().union(this, getGraphNode(graphNode), exp, map1, map2);
             
        int b1 = unionPush(p, graphNode, exp, stack, n, map1);
        int b2 = unionPush(p, graphNode, exp, stack, n, map2);

        return backtrack;
    }

    /**
     * Eval one exp of union main map is partial solution Mappings resulting
     * from previous statement evaluation, typically eval(A) in join(A, union(B,
     * C)) In federated case (or if exp is itself a union), map is passed to
     * subEval(exp, map), it may be taken into account by service in exp In non
     * federated case, map is included in copy of exp as a values(var, map)
     * clause
     */
    Mappings unionBranch(Producer p, Node graphNode, Exp exp, Exp main, Mappings data) throws SparqlException {
        Node queryNode = null; //(graphNode == null) ? null : query.getGraphNode();
        
        if (exp.isFirstWith(UNION)) {
            // union in union: eval inner union with parameter data as is
            return subEval(p, graphNode, queryNode, exp, main, data);
        }
        else if (isFederate(exp) || exp.isUnion() || isParameterUnionMappings()) {
            Mappings unionData = unionData(exp, data);          
            return subEval(p, graphNode, queryNode, exp, main, unionData);
        }
        else {
            // exp += values(var, map)
            Exp ee = exp.complete(data);
            return subEval(p, graphNode, queryNode, ee, main, null);
        }
    }
    
    /**
     * exp is a branch of union
     * extract from data relevant mappings for exp in-scope variables
     */
    Mappings unionData(Exp exp, Mappings data) {
        if (data != null) {
            if (data.getNodeList() == null) {
                MappingSet ms = new MappingSet(getQuery(), data);
                Mappings map = ms.prepareMappingsRest(exp); 
                return map;
            } 
            else if (data.getJoinMappings()!=null) {
                // select relevant variables from original join Mappings 
                return unionData(exp, data.getJoinMappings());
            }
        }        
        return data;
    }

    /**
     * Push Mappings of branch of union in the stack
     */
    int unionPush(Producer p, Node graphNode, Exp exp, Stack stack, int n, Mappings map) throws SparqlException {
        int backtrack = n - 1;
        Memory env = getMemory();
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            if (env.push(m, n)) {
                backtrack = eval(p, graphNode, stack, n + 1);
                env.pop(m);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }
      
    private int and(Producer p, Node graphNode, Exp exp, Stack stack, Mappings data, int n) throws SparqlException {
        getVisitor().bgp(this, getGraphNode(graphNode), exp, null);

        if (data != null && exp.size() > 0 && exp.get(0).isEdgePath()) {
            // pass Mappings data as values clause
            exp = exp.complete(data);
            //System.out.println(exp);
            stack = stack.and(exp, n);
            // Mappings data is in stack, not in parameter
            return eval(p, graphNode, stack, n);
        }
        else {
            stack = stack.and(exp, n);
            // pass Mappings data as parameter
            return eval(p, graphNode, stack, data, n);
        }
    }
    
    private int bgp(Producer p, Node graphNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        List<Node> from = getQuery().getFrom(graphNode);
        Mappings map = p.getMappings(graphNode, from, exp, getMemory());

        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(getQuery());
            boolean b = getMemory().push(m, n, false);
            if (b) {
                int back = eval(p, graphNode, stack, n + 1);
                getMemory().pop(m);
                if (back < n) {
                    return back;
                }
            }
        }
        return backtrack;
    }


    private int service(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = getMemory();
        Node serv = exp.first().getNode();
        Node node = serv;

        if (serv.isVariable()) {
            node = env.getNode(serv);
        }

        if (getProvider() != null) {
            // service delegated to provider
            Mappings map = getProvider().service(node, exp, selectQueryMappings(data), this);

//            if (stack.isCompleted()) {
//                return result(p, lMap, n);
//            }
                                 
            for (Mapping m : map) {
                if (stop) {
                    return STOP;
                }
                // push each Mapping in memory and continue
                complete(getQuery(), m, false);
                
                if (env.push(m, n, false, false)) {
                    backtrack = eval(p, graphNode, stack, n + 1);
                    env.pop(m, false);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        } else {
            Query q = exp.rest().getQuery();
            return query(p, graphNode, q, data, stack, n);
        }

        return backtrack;
    }
    
    // stack = just one service: store and return result directly
    int result(Producer p, Mappings lMap, int n) throws SparqlException {
        for (Mapping map : lMap) {
            complete(getQuery(), map, true);
            solution(p, map, n);
        }
        return STOP;
    }

    // process additional variable provided by service
    // such as ?_server_0 in federated mode
    void complete(Query q, Mapping map, boolean addNode) {
        int i = 0;
        for (Node node : map.getQueryNodes()) {
            Node out = q.getOuterNode(node);
            // draft: use case ?_server_0
            if (out == null) {
                out = node;
                if (addNode && ! q.getSelect().contains(node)) {
                    q.getSelect().add(node);
                }
            }
            map.getQueryNodes()[i] = out;
            i++;
        }
    }
    
    Node getNode(Producer p, Node gNode) {
        if (gNode.isConstant()) {
            return p.getNode(gNode);
        } 
        return getMemory().getNode(gNode);        
    }
    
//    Node getGraphNode(Node node) {
//        return (node == null) ? null : node.isConstant() ? node : getMemory().getNode(node);
//    }
    
    Node getGraphNode(Node node) {
        return node;
    }
    /**
     * bind(exp as var)
     */
    private int bind(Producer p, Node graphNode, Exp exp, Mappings map, Stack stack, int n) throws SparqlException {
        if (exp.isFunctional()) {
            return extBind(p, graphNode, exp, stack, n);
        }

        int backtrack = n - 1;
        Memory env = getMemory();
        Node node = eval(graphNode, exp.getFilter(), env, p);

        getVisitor().bind(this, getGraphNode(graphNode), exp, node == null ? null : node.getDatatypeValue());

        if (node == null) {
            backtrack = eval(p, graphNode, stack, map, n + 1);
        } else if (env.push(exp.getNode(), node, n)) {
            // kgram authorize to bind a variable "again"
            // hence push may fail if value is not the same
            backtrack = eval(p, graphNode, stack, map, n + 1);
            env.pop(exp.getNode());
        }

        return backtrack;
    }

    /**
     * values (?x ?y) { unnest(exp) }
     * compiled as extended bind 
     */
    private int extBind(Producer p, Node graphNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = getMemory();
        env.setGraphNode(graphNode);
        //Mappings map = getEvaluator().eval(exp.getFilter(), env, exp.getNodeList());
        Mappings map = eval(exp.getFilter(), env, exp.getNodeList());
        env.setGraphNode(null);
        getVisitor().values(this, getGraphNode(graphNode), exp, map);
        
        if (map != null) {
            HashMap<String, Node> tab = toMap(exp.getNodeList());
            for (Mapping m : map) {
                if (stop) {
                    return STOP;
                }
                if (env.push(tab, m, n)) {
                    backtrack = eval(p, graphNode, stack, n + 1);
                    env.pop(tab, m);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }

    HashMap<String, Node> toMap(List<Node> list) {
        HashMap<String, Node> m = new HashMap<>();
        for (Node node : list) {
            m.put(node.getLabel(), node);
        }
        return m;
    }

    /**
     *
     */
    private int filter(Producer p, Node graphNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = getMemory();
        boolean success = true;

        if (exp.isPostpone()) {
            // use case: optional { filter (exp) }, eval later
        } else {
            success = test(graphNode, exp.getFilter(), env, p);

            if (hasFilter) {
                success = getVisitor().filter(this, getGraphNode(graphNode), exp.getFilter().getExp(), success);
            }
        }

        if (hasEvent) {
            send(Event.FILTER, exp, success);
        }

        if (success) {
            backtrack = eval(p, graphNode, stack, n + 1);
        }

        return backtrack;
    }
    
//    boolean test2(Node graphNode, Filter f, Environment env, Producer p) throws SparqlException {
//        env.setGraphNode(graphNode);
//        boolean b = getEvaluator().test(f, env, p);
//        env.setGraphNode(null);
//        return b;
//    }
    
   
    boolean test(Node graphNode, Filter f, Environment env, Producer p) throws SparqlException {
        try {
            env.setGraphNode(graphNode);
            IDatatype dt = eval(f, env, p);
            return isTrue(dt);
        } finally {
            env.setGraphNode(null);
        }
    }
    
    boolean isTrue(IDatatype dt) {
        if (dt == null) {
            return false;
        }
        return dt.isTrueTest();
    }
    
    Node eval(Node graphNode, Filter f, Environment env, Producer p) throws SparqlException {
        try {
            env.setGraphNode(graphNode);
            return  eval(f.getExp(), env, p);
        } finally {
            env.setGraphNode(null);
        }
    }

    
    // evalWE clean the binding stack if an EngineException is thrown
    IDatatype eval(Filter f, Environment env, Producer p) throws EngineException {
        return f.getExp().evalWE(getEvaluator(), env.getBind(), env, p);        
    }
       
    IDatatype eval(Expr e, Environment env, Producer p) throws EngineException {
        return e.evalWE(getEvaluator(), env.getBind(), env, p);        
    }
    
//        if (dt == null) {
//            // Evaluation error, may be overloaded by visitor event @error function 
//            DatatypeValue res = env.getVisitor().error(env.getEval(), exp, EMPTY);
//            if (res != null) {
//                return (IDatatype) res;
//            }
//        }

    // values var { unnext(exp) }
    // @todo Producer is not current producer but global producer
    Mappings eval(Filter f, Environment env, List<Node> nodes)
            throws EngineException {
        return eval(f, env, getProducer(), nodes);
    }
    
    Mappings eval(Filter f, Environment env, Producer p, List<Node> nodes)
            throws EngineException {

        int n = 1;
        Expr exp = f.getExp();
        switch (exp.oper()) {

            case UNNEST:
                if (hasListener) {
                    listener.listen(exp);
                }
                if (exp.arity() == 2) {
                    // unnest(exp, 2)
                    IDatatype dt = eval(exp.getExp(1), env, p);
                    if (dt == null) {
                        return new Mappings();
                    }
                    n = dt.intValue();
                }
                exp = exp.getExp(0);

            default:
                IDatatype res = eval(exp, env, p);
                if (res == null) {
                    return new Mappings();
                }
                // use std producer to map in case local p cannot map 
                return getProducer().map(nodes, res, n);
        }
    }
    
    /**
     * Enumerate candidate edges
     * exp = query edge
     * graphNode = null for default graph triple pattern 
     * graphNode = URI of named graph for named graph triple pattern
     * graph ?g { exp } evaluates exp for each value of ?g = g_i
     * in this case graphNode = g_i and from is useless here 
     * from named has been used in named graph pattern evaluator
     * data is possible relevant bindings coming from preceding statement evaluation
     * data may be null
     *
     */
    private int path(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1, evENUM = Event.ENUM;
        PathFinder path = getPathFinder(exp, p);
        Filter f = null;
        Memory env = getMemory();
        Query qq = getQuery();
        boolean isEvent = hasEvent;
        
        if (data!=null && data.getNodeList()!=null && isPushEdgeMappings()) {   
            // push values(data) before edge in stack
            logger.info(String.format("Push path mappings:\nvalue %s\n%s", 
                 data.getNodeList(), data.toString(false, false, DISPLAY_RESULT_MAX)));
            return eval(p, graphNode, stack.addCopy(n, exp.getValues(data)), n);
        }

        if (stack.size() > n + 1) {
            if (stack.get(n + 1).isFilter()) {
                f = stack.get(n + 1).getFilter();
            }
        }

        path.start(exp.getEdge(), qq.getPathNode(), env, f);
        boolean isSuccess = false;

        List<Node> list = qq.getFrom(graphNode);
        Node backtrackNode = graphNode;

        if (p.getMode() == Producer.EXTENSION) {
            if (p.getQuery() == env.getQuery()) {
                list = empty;
                backtrackNode = p.getGraphNode();
            } else {
                backtrackNode = null;
            }
        }

        for (Mapping map : path.candidate(graphNode, list, env)) {
            if (stop) {
                path.stop();
                return STOP;
            }
            boolean b = match(map);
            boolean success = match(map) && env.push(map, n);

            if (isEvent) {
                send(evENUM, exp, map, success);
            }

            if (success) {
                isSuccess = true;
                backtrack = eval(p, graphNode, stack, n + 1);
                env.pop(map);

                if (backtrack < n) {
                    path.stop();
                    // remove it to get fresh automaton next time
                    lPathFinder.remove(path);
                    return backtrack;
                }
            }
        }

        if (!isSuccess && optim) {
            // backjump to max index where nodes are bound for first time:
            int bj = env.getIndex(backtrackNode, exp.getEdge());
            backtrack = bj;
        }
        path.stop();
        return backtrack;
    }

    private int values(Producer p, Node graphNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        getVisitor().values(this, getGraphNode(graphNode), exp, exp.getMappings());
        
        for (Mapping map : exp.getMappings()) {
            if (stop) {
                return STOP;
            }
            if (valuesBinding(exp.getNodeList(), map, n)) {
                backtrack = eval(p, graphNode, stack, n + 1);
                free(exp.getNodeList(), map);

                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;

    }

    boolean valuesBinding(List<Node> varList, Mapping map, int n) {
        int i = 0;
        for (Node qNode : varList) { 
            Node node = map.getNode(qNode);
            if (node != null) {
                Node value = getProducer().getNode(node.getValue());
                boolean suc = getMemory().push(qNode, value, n);
                if (!suc) {
                    popBinding(varList, map, i);
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    boolean popBinding(List<Node> varList, Mapping map, int i) {
        int j = 0;
        for (Node qq : varList) { 
            Node nn = map.getNode(qq);
            if (nn != null) {
                if (j >= i) {
                    return false;
                } else {
                    j++;
                }
                getMemory().pop(qq);
            }
        }
        return false;
    }

    void free(List<Node> varList, Mapping map) {
        for (Node qNode : varList) {
            getMemory().pop(qNode);
        }
    }
    
    void trace(String mes, Object... obj) {
        if (getQuery().isDebug()) {
            System.out.println(String.format(mes, obj));
        }
    }

    /**
     * Enumerate candidate edges
     * exp = query edge
     * graphNode = null for default graph triple pattern 
     * graphNode = URI of named graph for named graph triple pattern
     * graph ?g { exp } evaluates exp for each value of ?g = g_i
     * in this case graphNode = g_i and from is useless here 
     * from named has been used in named graph pattern evaluator
     * data is possible relevant bindings coming from preceding statement evaluation
     * data may be null
     *
     */
    private int edge(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isSuccess = false,
                hasGraphNode = graphNode != null,
                isEvent = hasEvent;
        Edge qEdge = exp.getEdge();
        Node graph = null;
        Memory env = getMemory();
        // used by RuleEngine ResultWatcher ProducerImpl
        env.setExp(exp);
        Query qq = getQuery();
        // for default graph triple pattern, consider select from if any
        // for named graph triple pattern, from named is useless here because graphNode = URI of one named graph
        List<Node> graphNodeList = qq.getFrom(graphNode);
        // the backtrack graphNode (deprecated)
        Node backtrackGraphNode = graphNode;
        boolean matchNBNode = qEdge.isMatchArity();
       
        if (data != null && data.getNodeList() != null) {
            if (isPushEdgeMappings()) {
                // push values(data) before edge in stack
                if (isDebug()) {
                    logger.warn(String.format("Push edge mappings:\nvalues %s\n%s",
                            data.getNodeList(), data.toString(false, false, DISPLAY_RESULT_MAX)));
                }
                return eval(p, graphNode, stack.addCopy(n, exp.getValues(data)), n);
            } else if (isDebug()) {
                logger.warn(String.format("Eval edge skip mappings:\nvalues %s\n%s",
                        data.getNodeList(), data.toString(false, false, DISPLAY_RESULT_MAX)));
            }
        }

        if (p.getMode() == Producer.EXTENSION) {
            // Producer is Extension only for the query that created it
            // use case: templates may share same Producer
            if (p.getQuery() == env.getQuery()) {
                // special case graph ?g { exp } where ?g is external graph
                // forget from and from named if any
                graphNodeList = empty;
                backtrackGraphNode = p.getGraphNode();
            } else {
                backtrackGraphNode = null;
            }
        }

        Iterable<Edge> entities;
        if (hasProduce) {
            // draft not used
            entities = produce(p, graphNode, graphNodeList, qEdge);
            if (entities == null) {
                entities = p.getEdges(graphNode, graphNodeList, qEdge, env);
            }
        } else {
            entities = p.getEdges(graphNode, graphNodeList, qEdge, env);
        }

        Iterator<Edge> it = entities.iterator();

        while (it.hasNext()) {

            if (stop) {
                return STOP;
            }

            Edge edge = it.next();
            if (edge != null) {
               nbEdge++;
                if (hasListener && !listener.listen(exp, qEdge, edge)) {
                    continue;
                }

                graph = edge.getGraph();
                boolean bmatch = match(qEdge, edge, graphNode, graph, env);
                //trace ("I: %s Q: %s E: %s match: %s", qEdge.getEdgeIndex(), qEdge, edge, bmatch);

                if (matchNBNode) {
                    bmatch &= (qEdge.nbNode() == edge.nbNode());
                }

                if (bmatch) {
                    if (hasCandidate) {
                        IDatatype dt = getVisitor().candidate(this, getGraphNode(graphNode), qEdge, edge);
                        if (dt != null) {
                            bmatch = dt.booleanValue();
                        }
                    }

                    bmatch &= push(p, qEdge, edge, graphNode, graph, n);
                    if (!bmatch) trace("push success: %s", bmatch);
                }

                if (isEvent) {
                    send(evENUM, exp, edge, bmatch);
                }

                if (bmatch) {
                    isSuccess = true;
                    backtrack = eval(p, graphNode, stack, n + 1);

                    env.pop(qEdge, edge);
                    if (hasGraphNode) {
                        env.pop(graphNode);
                    }

                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        if (!isSuccess && optim) {
            // compute index in the stack of exp where to backtrack
            // backtrack index is the highest index in the stack among edge node index
            // we backtrack as much as possible in the exp stack
            backtrack = env.getIndex(backtrackGraphNode, qEdge);
        }

        return backtrack;
    }


   
    /**
     * subquery
     * select * where {{select * where {?x p ?y}} . ?y q ?z} 
     * new eval, new memory, share only sub query select variables
     *
     */
    private int query(Producer p, Node graphNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
       int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isEvent = hasEvent;
        Query subQuery = exp.getQuery();
        Memory env = getMemory();
        getVisitor().start(subQuery);

        // copy current Eval,  new stack
        // bind sub query select nodes in new memory
        Eval ev = copy(copyMemory(env, getQuery(), subQuery, null), p, getEvaluator(), subQuery, false);      
        ev.setDebug(isDebug());
        Mappings lMap = ev.eval(graphNode, subQuery, null, selectQueryMappings(data));
        
        if (isDebug()) {
            logger.info("subquery results size:\n"+ lMap.size());
        }

        getVisitor().query(this, getGraphNode(graphNode), exp, lMap);
        getVisitor().finish(lMap);

        // enumerate the result of the sub query
        // bind the select nodes into the stack
        for (Mapping map : lMap) {
            if (stop) {
                return STOP;
            }
            boolean bmatch = push(subQuery, map, n);

            if (isEvent) {
                send(evENUM, exp, map, bmatch);
            }

            if (bmatch) {
                backtrack = eval(p, graphNode, stack, n + 1);
                pop(subQuery, map);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }
    
    /**
     * Skip Mappings with null nodeList for subquery and service
     */
    Mappings selectQueryMappings(Mappings data) {
        if (data != null) {
            if (data.getNodeList() == null) {
                // no variable in-scope wrt select clause
                return null;
            } else {
                // forget original join Mappings (in case of union in body)
                // because original Mappings may not fit the select clause
                data.setJoinMappings(null);
            }
        }
        return data;
    }
    
     
    /**
     * res is a result of sub query bind the select nodes of sub query into
     * current memory retrieve outer node that correspond to sub node
     *
     */
    private boolean push(Query subQuery, Mapping res, int n) {
        int k = 0;
        Memory env = memory;
        Matcher mm = match;
        Query qq = query;

        for (Exp exp : subQuery.getSelectFun()) {
            Node subNode = exp.getNode();
            Node node = res.getNode(subNode);
            Node outNode; //= query.getNode(subNode);
            if (exp.size() == 0) {
                // store outer node for next time
                outNode = qq.getOuterNodeSelf(subNode);   //ici              
                exp.add(outNode);
            } else {
                outNode = exp.get(0).getNode();
            }

            if (node != null) {
                // a value may be null because of an option {}
                if (!(mm.match(outNode, node, env) && env.push(outNode, node, n))) {
                    for (int i = 0; i < k; i++) {
                        subNode = subQuery.getSelect().get(i);
                        outNode = qq.getOuterNodeSelf(subNode);
                        Node value = res.getNode(subNode);
                        if (value != null) {
                            env.pop(outNode);
                            env.popPath(outNode);
                        }
                    }
                    return false;
                } else {
                    if (res.isPath(subNode)) {
                        env.pushPath(outNode, res.getPath(subNode));
                    }
                }
            }
            k++;
        }

        return true;
    }

    /**
     * pop select nodes of sub query
     */
    private void pop(Query subQuery, Mapping ans) {
        Memory env = memory;
        Query qq = query;

        for (Node subNode : subQuery.getSelect()) {
            if (ans.isBound(subNode)) {
                Node outNode = qq.getOuterNodeSelf(subNode);
                env.pop(outNode);
                env.popPath(outNode);
            }
        }
    }


    /**
     * Store a new result
     */
    private int store(Producer p, Mapping m) throws SparqlException {
        boolean store = true;
        if (getListener() != null) {
            store = getListener().process(getMemory());
        }
        if (store) {
            nbResult++;
        }
        if (storeResult && store) {
            Mapping ans = m;
            if (m == null) {
               ans = getMemory().store(getQuery(), p, isSubEval);
            }
            store(ans);
        }
        return -1;
    }
    
    void store(Mapping ans) {
        if (ans != null && acceptable(ans)) {
            //submit(ans);
            if (hasEvent) {
                send(Event.RESULT, ans);
            }
            boolean b = true;
            if (!isSubEval) {
                b = getVisitor().distinct(this, getQuery(), ans);
                if (b) {
                    b = getVisitor().result(this, getResults(), ans);
                }
            }
            if (b) {
                getResults().add(ans);
            }
        }
    }

    boolean acceptable(Mapping m) {
        return getQuery().getGlobalQuery().isAlgebra() || getResults().acceptable(m);
    }

    void submit(Mapping map) {
        if (getQuery().getGlobalQuery().isAlgebra()) {
            // eval distinct later
            getResults().add(map);
        } else {
            getResults().submit(map);
        }
    }

    public int nbResult() {
        return getResults().size();
    }

    public int getCount() {
        return nbEdge;
    }

    private boolean match(Edge qEdge, Edge edge, Node gNode, Node graphNode, Memory memory) {
        if (!getMatcher().match(qEdge, edge, memory)) {
            return false;
        }
        if (gNode == null || graphNode == null) {
            return true;
        }
        return getMatcher().match(gNode, graphNode, memory);
    }

    private boolean push(Producer p, Edge qEdge, Edge ent, Node gNode, Node node, int n) {
        Memory env = getMemory();
        if (!env.push(p, qEdge, ent, n)) {
            return false;
        }
//        if (gNode != null && !env.push(gNode, node, n)) {
//            env.pop(qEdge, ent);
//            return false;
//        }
        return true;
    }

    private boolean match(Node qNode, Node node, Node gNode, Node graphNode) {
        Memory env = getMemory();
        if (!getMatcher().match(qNode, node, env)) {
            return false;
        }
        if (gNode == null) {
            return true;
        }
        return getMatcher().match(gNode, graphNode, env);
    }

    private boolean push(Node qNode, Node node, Node gNode, Node graphNode, int n) {
        Memory env = getMemory();
        if (!env.push(qNode, node, n)) {
            return false;
        }
        if (gNode != null && !env.push(gNode, graphNode, n)) {
            env.pop(qNode);
            return false;
        }
        return true;
    }

    // for path 
    private boolean match(Mapping map) {
        int i = 0;
        Memory env = getMemory();
        Matcher mm = getMatcher();
        for (Node qNode : map.getQueryNodes()) {
            Node node = map.getNode(i++);
            if (!mm.match(qNode, node, env)) {
                return false;
            }
        }
        return true;
    }

    /**
     * ********************************************************
     *
     * @param el
     */
    public void addResultListener(ResultListener el) {
        setListener(el);
        hasListener = getListener() != null;
        if (hasListener) {
            evaluator.addResultListener(el);
        }
    }

    public void addEventListener(EventListener el) {
        createManager();
        el.setObject(this);
        manager.addEventListener(el);
    }

    void createManager() {
        if (manager == null) {
            setEventManager(new EventManager());
            if (memory != null) {
                memory.setEventManager(manager);
            }
            if (results != null) {
                results.setEventManager(manager);
            }
        }
    }

    public void setEventManager(EventManager man) {
        manager = man;
        hasEvent = true;
    }

    public EventManager getEventManager() {
        return manager;
    }

    boolean send(int type, Object obj) {
        Event e = EventImpl.create(type, obj);
        return manager.send(e);
    }

    boolean send(int type, Object obj, Object arg) {
        Event e = EventImpl.create(type, obj, arg);
        return manager.send(e);
    }

    boolean send(int type, Object obj, Object arg, Object arg2) {
        Event e = EventImpl.create(type, obj, arg, arg2);
        return manager.send(e);
    }

    void set(Stack current) {
        this.current = current;
    }

    public Stack getStack() {
        return current;
    }
       
    public SPARQLEngine getSPARQLEngine() {
        return sparqlEngine;
    }

    
    public void setSPARQLEngine(SPARQLEngine sparqlEngine) {
        this.sparqlEngine = sparqlEngine;
    }

   
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

   
    public boolean isStop() {
        return stop;
    }

    
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void finish() {
        setStop(true);
        join.setStop(true);
        evalGraphNew.setStop(true);
        optional.setStop(true);
    }

    public static boolean isPushEdgeMappings() {
        return pushEdgeMappings;
    }

    public static void setPushEdgeMappings(boolean aPushEdgeMappings) {
        pushEdgeMappings = aPushEdgeMappings;
    }

    public static boolean isParameterGraphMappings() {
        return parameterGraphMappings;
    }

    public static void setParameterGraphMappings(boolean aParameterGraphMappings) {
        parameterGraphMappings = aParameterGraphMappings;
    }

    public static boolean isParameterUnionMappings() {
        return parameterUnionMappings;
    }

    public static void setParameterUnionMappings(boolean aParameterUnionMappings) {
        parameterUnionMappings = aParameterUnionMappings;
    }
    
    public static void setNewMappingsVersion(boolean b) {
        setPushEdgeMappings(b);
        setParameterGraphMappings(b);
        setParameterUnionMappings(b);
    }

    public boolean isDebug() {
        return debug;
    }
    
    
    
    
    /***************************************************************************
     * 
     * Alternative interpreter
     * 
     */
    
     /**
     * SPARQL algebra requires kgram to compute BGP exp and return Mappings
     * List<Node> from = query.getFrom(gNode); Mappings map =
     * p.getMappings(gNode, from, exp, memory);
     */
    Mappings exec(Node gNode, Producer p, Exp exp, Mapping m) throws SparqlException {
        if (true) {
            List<Node> from = query.getFrom(gNode);
            Mappings map = p.getMappings(gNode, from, exp, memory);
            return map;
        }
        Stack stack = Stack.create(exp);
        set(stack);
        if (m != null) {
            process(exp, m);
        }
        eval(p, gNode, stack, 0);
        Mappings map = Mappings.create(query);
        map.add(results);
        memory.start();
        results.clear();
        return map;
    }

    void process(Exp exp, Mapping m) {
        if (exp.getNodeList() != null) {
            for (Node qnode : exp.getNodeList()) {
                Node node = m.getNodeValue(qnode);
                if (node != null) {
                    memory.push(qnode, node, -1);
                }
            }
        } else {
            memory.push(m, 0);
        }
    }

    /**
     * Evaluate exp with SPARQL Algebra on Mappings, not with Memory stack
     *
     *
     */
    void process(Node gNode, Producer p, Exp exp) {
        results = evalSparql.eval(gNode, p, exp);
    }
    
    //    int eval(Node gNode, Query q, Mappings map) throws SparqlException {
//        if (q.isFunctional()) {
//            // select xpath() as ?val
//            // select unnest(fun()) as ?x
//            function();
//            return 0;
//        } else {
//            return evalExp(gNode, q, q.getBody(), map);
//        }
//    }
    
    
    
    /**
     * use case:
     *
     * (n) EDGE{?x ?q ?z} (n+1) FILTER{?x = ?y} with BIND {?x := ?y} compiled
     * as:
     *
     * (n) BIND {?x := ?y} (n+1) EDGE{?x ?q ?z} (n+2) FILTER{?x = ?y}
     */
    private int optBind(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        Memory env = getMemory();
        int backtrack = n - 1;
        if (exp.isBindCst()) {
            backtrack = cbind(p, gNode, exp, stack, n);
            return backtrack;
        } else {

            // ?x = ?y
            int i = 0, j = 1;
            Node node = env.getNode(exp.get(i).getNode());
            if (node == null) {
                i = 1;
                j = 0;
                node = env.getNode(exp.get(i).getNode());
                if (node == null) {
                    // no binding: continue
                    backtrack = eval(p, gNode, stack, n + 1);
                    return backtrack;
                }
            }

            Node qNode = exp.get(j).getNode();
            if (!env.isBound(qNode) && getProducer().isBindable(node)) {
                // bind qNode with same index as other variable
                env.push(qNode, node, env.getIndex(exp.get(i).getNode()));
                if (hasEvent) {
                    send(Event.BIND, exp, qNode, node);
                }
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(qNode);
            } else {
                backtrack = eval(p, gNode, stack, n + 1);
            }

            return backtrack;
        }
    }

    /**
     * exp : BIND{?x = cst1 || ?x = cst2} Bind ?x with all its values
     */
    private int cbind(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        Producer prod = getProducer();

        Node qNode = exp.get(0).getNode();
        if (!exp.status() || env.isBound(qNode)) {
            return eval(p, gNode, stack, n + 1);
        }

        if (exp.getNodeList() == null) {
            // Constant are not yet transformed into Node
            for (Object value : exp.getObjectValues()) {
                // get constant Node
                Expr cst = (Expr) value;
                Node node = prod.getNode(cst.getValue());
                if (node != null && prod.isBindable(node)) {
                    // store constant Node into Bind expression
                    // TODO: 
                    // if there are several producers, it is considered
                    // bindable for all producers. This may be a problem.
                    exp.addNode(node);
                } else {
                    // Constant fails being a Node: stop binding
                    exp.setNodeList(null);
                    exp.status(false);
                    break;
                }
            }
        }

        if (exp.getNodeList() != null) {
            // get variable Node
            for (Node node : exp.getNodeList()) {
                // Enumerate constant Node
                env.push(qNode, node, n);
                if (hasEvent) {
                    send(Event.BIND, exp, qNode, node);
                }
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(qNode);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        } else {
            backtrack = eval(p, gNode, stack, n + 1);
        }
        return backtrack;
    }

    /**
     *
     * Exp evaluated as a BGP, get result Mappings, push Mappings and continue
     * Use case: cache the Mappings
     * @deprecated
     */
    private int bgpAble(Producer p, Node graphNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Mappings map = getMappings(p, graphNode, exp);
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(getQuery());
            boolean b = getMemory().push(m, n, false);
            if (b) {
                int back = eval(p, graphNode, stack, n + 1);
                getMemory().pop(m);
                if (back < n) {
                    return back;
                }
            }
        }
        return backtrack;
    }

    /**
     * Mappings of exp may be cached for specific query node Use case: datashape
     * template exp = graph ?shape {?sh sh:path ?p} exp is evaluated once for
     * each value of ?sh and Mappings are cached successive evaluations of exp
     * on ?sh get Mappings from cache
     * @deprecated
     */
    Mappings getMappings(Producer p, Node graphNode, Exp exp) throws SparqlException {
        if (exp.hasCache()) {
            // @deprecated
            Node n = getMemory().getNode(exp.getCacheNode());
            if (n != null) {
                Mappings m = exp.getMappings(n);
                if (m == null) {
                    m = subEval(p, graphNode, graphNode, exp, exp, null, null, true, false);
                    exp.cache(n, m);
                }
                return m;
            }
        }
        return subEval(p, graphNode, null, exp, exp, null, null, true, false);
    }
    
    /**
     * Draf extension where a Visitor provides Edge iterator
     */
    Iterable<Edge> produce(Producer p, Node gNode, List<Node> from, Edge edge) {
        IDatatype res = getVisitor().produce(this, gNode, edge);
        if (res == null) {
            return null;
        }
        if (res.getNodeObject() != null && (res.getNodeObject() instanceof Iterable)) {
            return new IterableEntity((Iterable) res.getNodeObject());
        } else if (res instanceof Loopable) {
            Iterable loop = ((Loopable) res).getLoop();
            if (loop != null) {
                return new IterableEntity(loop);
            }
        }
        return null;
    }
    
    
    
    @Override
    @Deprecated
    public void exec(Exp exp, Environment env, int n) {
        if (exp.getObject() instanceof String) {
            String label = (String) exp.getObject();
            if (env.getNode(label) != null) {
                logger.debug(n + ": " + label + " " + env.getNode(label).getLabel());
            }
        }
    }

    public boolean isJoinMappings() {
        return joinMappings;
    }

    public void setJoinMappings(boolean joinMappings) {
        this.joinMappings = joinMappings;
    }

    public ResultListener getListener() {
        return listener;
    }

    public void setListener(ResultListener listener) {
        this.listener = listener;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

}
