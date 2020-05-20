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
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Loopable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.DatatypeValue;
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
    private static Logger logger = LoggerFactory.getLogger(Eval.class);

    static final int STOP = -2;
    public static int count = 0;
    ResultListener listener;
    EventManager manager;
    private ProcessVisitor visitor;
    private SPARQLEngine sparqlEngine;
    boolean hasEvent = false;
    boolean namedGraph = NAMED_GRAPH_DEFAULT;
    // Edge and Node producer
    Producer producer, saveProducer;
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
    List<Node> empty = new ArrayList<Node>(0);
    HashMap<String, Boolean> local;
    
    EvalGraphNew evalGraphNew;
    EvalGraph evalGraph;
    EvalJoin join;
    EvalOptional optional;

    int // count number of eval() calls
            nbEdge = 0, nbCall = 0,
            rcount = 0,
            backjump = -1, indexToDiffer = -1,
            // max level in stack for debug
            level = -1,
            maxLevel = -1,
            limit = Integer.MAX_VALUE;
    boolean debug = false,
            isSubEval = false,
            // return only select variables in Mapping
            onlySelect = true,
            optim = true,
            draft = true;
    private boolean hasListener = false;
    private boolean isPathType = false;
    boolean storeResult = true;
    private int nbResult;
    boolean hasFilter = false;
    private boolean hasCandidate = false,
            hasStatement = false,
            hasProduce = false;
    private boolean stop = false;
    
    boolean newGraph = true;

    //Edge previous;
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
        lPathFinder = new ArrayList<PathFinder>();
        setVisitor(new ProcessVisitorDefault());
        e.setKGRAM(this);
        initCallback();
        evalGraph    = new EvalGraph(this);
        evalGraphNew = new EvalGraphNew(this);
        join = new EvalJoin(this);
        optional = new EvalOptional(this);

    }

    void initCallback() {
        local = new HashMap<>();
    }

    public static Eval create(Producer p, Evaluator e, Matcher m) {
        return new Eval(p, e, m);
    }

    public void set(Provider p) {
        provider = p;
    }

    public void set(Producer p) {
        producer = p;
    }

    public void set(Matcher m) {
        match = m;
    }

    public void set(Evaluator e) {
        evaluator = e;
    }

    public Results exec(Query q) {
        Mappings maps = query(q, null);
        return ResultsImpl.create(maps);
    }

    /**
     * Eval KGRAM query and subquery For subquery, this eval is a copy which
     * shares the memory with outer eval
     */
    public Mappings query(Query q) {
        return query(null, q, null);
    }
    
    public Mappings query(Query q, Mapping m) {
        return query(null, q, m);
    }
    
    public Mappings query(Node gNode, Query q, Mapping m) {
        if (hasEvent) {
            send(Event.BEGIN, q);
        }
        initMemory(q);
        share(m);
        producer.start(q);
        getVisitor().init(q);
        share(getVisitor());
        getVisitor().before(q);
        Mappings map = eval(gNode, q, m);
        getVisitor().orderby(map);
        getVisitor().after(map);

        producer.finish(q);
        if (hasEvent) {
            send(Event.END, q, map);
        }
        map.setBinding(memory.getBind());
        clean();
        return map;
    }

    // share global variables and ProcessVisitor
    void share(Mapping m) {
        if (m != null && m.getBind() != null) {
            if (memory.getBind() != null) {
                memory.getBind().share(m.getBind());
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
    void share(ProcessVisitor vis) {
        if (vis.isShareable() && getMemory().getBind().getVisitor() == null) {
            getMemory().getBind().setVisitor(vis);
        }
    }

    public void finish(Query q, Mappings map) {
    }

    public Mappings eval(Node gNode, Query q, Mapping map) {
        init(q);
        if (q.isValidate()) {
            // just compile and complete query
            return results;
        }
        if (q.isCheck()) {
            // Draft
            Checker check = Checker.create(this);
            check.check(q);
        }

        if (map != null) {
            bind(map);
        }
        if (!q.isFail()) {
            query(gNode, q);

            if (q.getQueryProfile() == Query.COUNT_PROFILE) {
                countProfile();
            } else {
                if (q.isAlgebra()) {
                    memory.setResults(results);
                    completeSparql.complete(producer, results);
                }
                aggregate();
                // order by
                complete();
                template();
            }
        }

        if (debug && !isSubEval && !q.isSubQuery()) {
            debug();
        }
        evaluator.finish(memory);
        return results;
    }

    /**
     * We just counted number of results: nbResult Just build a Mapping
     */
    void countProfile() {
        Node n = evaluator.cast(nbResult, memory, producer);
        Mapping m = Mapping.create(query.getSelectFun().get(0).getNode(), n);
        results.add(m);
    }

    int query(Node gNode, Query q) {
        if (q.getValues() != null) {
            Exp values = q.getValues();
            if (!values.isPostpone() && !q.isAlgebra()) {
                for (Mapping m : values.getMappings()) {
                    if (stop) {
                        return STOP;
                    }
                    if (binding(values.getNodeList(), m)) {
                        eval(gNode, q);
                        free(values.getNodeList(), m);
                    }
                }
                return 0;
            }
        }

        return eval(gNode, q);
    }

    public Mappings filter(Mappings map, Query q) {
        Query qq = map.getQuery();
        init(qq);
        qq.compile(q.getHaving().getFilter());
        qq.index(qq, q.getHaving().getFilter());
        //q.complete();

        map.filter(evaluator, q.getHaving().getFilter(), memory);
        return map;
    }

    int eval(Node gNode, Query q) {
        if (q.isFunctional()) {
            // select xpath() as ?val
            // select unnest(fun()) as ?x
            function();
            return 0;
        } else {
            Stack stack = Stack.create(q.getBody());
            set(stack);
            return eval(gNode, stack, 0);
        }
    }

    /**
     * SPARQL algebra requires kgram to compute BGP exp and return Mappings
     * List<Node> from = query.getFrom(gNode); Mappings map =
     * p.getMappings(gNode, from, exp, memory);
     */
    Mappings exec(Node gNode, Producer p, Exp exp, Mapping m) {
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

    /**
     * Subquery processed by a function call that return Mappings Producer may
     * cast the result into Mappings use case: {select xpath(?x, '/book/title')
     * as ?val where {}} Mappings may be completed by filter (e.g. for casting)
     * Mappings will be processed later by aggregates and order by/limit etc.
     */
    private void function() {
        Exp exp = query.getFunction();
        if (exp == null) {
            return;
        }
        Mappings lMap = evaluator.eval(exp.getFilter(), memory, exp.getNodeList());
        if (lMap != null) {
            for (Mapping map : lMap) {
                map = complete(map, producer);
                submit(map);
            }
        }
    }

    /**
     * additional filter of functional select xpath() as ?val xsd:integer(?val)
     * as ?int
     */
    private Mapping complete(Mapping map, Producer p) {
        for (Exp ee : query.getSelectFun()) {
            Filter f = ee.getFilter();
            if (f != null && !f.isFunctional()) {
                memory.push(map, -1);
                Node node = evaluator.eval(f, memory, producer);
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

    /**
     * this eval is a fresh copy
     */
    public Mappings subEval(Query q, Node gNode, Stack stack, int n) {
        setSubEval(true);
        starter(q);
        if (q.isDebug()) {
            debug = true;
        }
        eval(gNode, stack, n);

        //memory.setResults(save);
        return results;
    }

    /**
     * gNode = subQuery.getGraphNode(graphName); node = env.getNode(graphName)
     *
     */
//    public Mappings query(Node gNode, Node node, Query query) {
//        if (gNode != null && node != null) {
//            getMemory().push(gNode, node);
//        }
//        return eval(gNode, query, null);
//    }

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
                for (Filter filter : query.getFailures()) {
                    Message.log(filter + " ");
                }
                Message.log();
            } else {
                Message.log(Message.FAIL_AT);
                Message.log(maxExp);
            }
        }
    }

    /**
     * Eval exp alone in a fresh new Memory Node gNode : actual graph node Node
     * node : exp graph node
     */
    public Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main) {
        return subEval(p, gNode, queryNode, exp, main, null, null, false);
    }

    Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map) {
        return subEval(p, gNode, queryNode, exp, main, map, null, false);
    }

    Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
       return (newGraph) ? 
                 subEvalNew(p, gNode, queryNode, exp, main, map, m, bind, false) :
                 subEvalOld(p, gNode, queryNode, exp, main, map, m, bind);
    }
    
    /**
     * ext = false : gNode is the URI, queryNode is meaningless
     * ext = true :  gNode is external graph, queryNode is named graph variable
     * 
     */
    Mappings subEvalNew(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map, Mapping m, boolean bind, boolean external) {    
        Memory mem = new Memory(match, getEvaluator());
        getEvaluator().init(mem);
        mem.share(memory);
        mem.init(query);
        mem.setAppxSearchEnv(this.memory.getAppxSearchEnv());
        Eval eval = copy(mem, p);
        if (external) {
            if (queryNode != null) {
                mem.push(queryNode, gNode, -1);
            }
            gNode = null;
        }
        bind(mem, exp, main, map, m, bind);
        Mappings lMap = eval.subEval(query, gNode, Stack.create(exp), 0);
        return lMap;
    }
    
    Mappings subEvalOld(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {    
        Memory mem = new Memory(match, getEvaluator());
        getEvaluator().init(mem);
        mem.share(memory);
        mem.init(query);
        mem.setAppxSearchEnv(this.memory.getAppxSearchEnv());
        Eval eval = copy(mem, p);
        graphNode(p, gNode, queryNode, mem);
        bind(mem, exp, main, map, m, bind);
        Mappings lMap = eval.subEval(query, queryNode, Stack.create(exp), 0);
        return lMap;
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
        Eval ev = create(p, e, match);
        if (q != null) {
            ev.complete(q);
        }
        ev.setSPARQLEngine(getSPARQLEngine());
        ev.setMemory(m);
        ev.set(provider);
        if (!extern || getVisitor().isShareable()) {
            ev.setVisitor(getVisitor());
        }
        ev.startExtFun(q);
        ev.setPathType(isPathType);
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

    Mappings getResults() {
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
            q.complete(producer);//service while1 / Query
            memory = new Memory(match, evaluator);
            memory.setEval(this);
            getEvaluator().init(memory);
            // create memory bind stack
            memory.init(q);
            if (hasEvent) {
                memory.setEventManager(manager);
            }
            producer.init(q);
            evaluator.start(memory);
            debug = q.isDebug();
            if (q.isAlgebra()) {
                complete(q);
            }
            if (debug) {
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

//    int compare(Node n1, Node n2) {
//        return evaluator.compare(memory, producer, n1, n2);
//    }

    private void aggregate() {
        results.aggregate(evaluator, memory, producer);
    }

    private void template() {
        results.template(evaluator, memory, producer);
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
            //Node qqNode = query.getSelectNode(qNode.getLabel());            
            Node qqNode = query.getOuterNode(qNode);
            if (qqNode != null) {
                Node node = map.getNode(qNode);
                if (node != null) {
                    bind(qqNode, node);
                    if (debug) {
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
        pathFinder.set(listener);
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

    private int solution(Producer p, int n) {
        int backtrack = n - 1;
        int status = store(p);
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
     * Eval a stack of KGRAM expressions
     *
     * Manage backjump, i.e. backtrack at lever less than n-1 needed for NOT in
     * order to backtrack at once before the not
     *
     *
     *
     */
    private int eval(Node gNode, Stack stack, int n) {
        return eval(producer, gNode, stack, n);
    }

    /**
     * gNode is the query graph name if any, may be null
     */
   int eval(Producer p, Node gNode, Stack stack, int n) {
        int backtrack = n - 1;
        boolean isEvent = hasEvent;
        Memory env = memory;

        nbCall++;

        if (n >= stack.size()) {
            backtrack = solution(p, n);
            return backtrack;
        }

        if (debug) {

            if (n > level
                    || (maxExp.type() == UNION)) {
                Exp ee = stack.get(n);
                if (ee.type() != AND) {
                    level = n;
                    maxExp = stack.get(n);
                    String s = String.format("%02d", n);
                    Message.log(Message.EVAL, s + " " + maxExp);
                }
            }
        }

        if (n > maxLevel) {
            maxLevel = n;
        }

        Exp exp = stack.get(n);
        if (hasListener) {
            exp = listener.listen(exp, n);
        }

        if (isEvent) {
            send(Event.START, exp, gNode, stack);
        }

        if (exp.isFail()) {
            // a false filter was detected at compile time
            // or exp was identified as always failing
            // no use to eval this exp
        } else {

            if (exp.isBGPAble()) {
                // evaluate and record result for next time
                // template optimization 
                exp.setBGPAble(false);
                backtrack = bgpAble(p, gNode, exp, stack, n);
                exp.setBGPAble(true);
            } else {
                // draft test
                if (query.getGlobalQuery().isAlgebra()) {
                    switch (exp.type()) {
                        case BGP:
                        case JOIN:
                        case MINUS:
                        case OPTIONAL:
                        case GRAPH:
                        case UNION:
                            process(gNode, p, exp);
                            return backtrack;
                    }
                };

                if (hasStatement) {
                    getVisitor().statement(this, getGraphNode(gNode), exp);
                }

                switch (exp.type()) {

                    case EMPTY:

                        eval(p, gNode, stack, n + 1);
                        break;

                    case AND:
//                        getVisitor().bgp(this, getGraphNode(gNode), exp, null);
//                        stack = stack.and(exp, n);
//                        backtrack = eval(p, gNode, stack, n);
                        
                        backtrack = and(p, gNode, exp, stack, n);
                        break;

                    case BGP:
                        backtrack = bgp(p, gNode, exp, stack, n);
                        break;

                    case SERVICE:
                        backtrack = service(p, gNode, exp, getMappings(), stack, n);
                        break;

                    case GRAPH:
                        backtrack = 
                                (newGraph) ?
                                evalGraphNew.eval(p, gNode, exp, getMappings(), stack, n) :
                                evalGraph.namedGraph(p, gNode, exp, getMappings(), stack, n)
                                ;
                        break;

                    case UNION:
                        backtrack = union(p, gNode, exp, getMappings(), stack, n);
                        break;
                    
                    case OPTIONAL:
                        backtrack = optional.eval(p, gNode, exp, getMappings(), stack, n);
                        break;
                    case MINUS:
                        backtrack = minus(p, gNode, exp, getMappings(), stack, n);
                        break;
                    case JOIN:
                        backtrack = join.eval(p, gNode, exp, getMappings(), stack, n);
                        break;
                    case QUERY:
                        backtrack = query(p, gNode, exp, getMappings(), stack, n); 
                        break;    
                    case FILTER:
                        backtrack = filter(p, gNode, exp, stack, n);
                        break;
                    case BIND:
                        backtrack = bind(p, gNode, exp, stack, n);
                        break;

                    case PATH:
                        backtrack = path(p, gNode, exp, stack, n);
                        break;

                    case EDGE:
                        if (query.getGlobalQuery().isPathType() && exp.hasPath()) {
                            backtrack = path(p, gNode, exp.getPath(), stack, n);
                        } else {
                            backtrack = edge(p, gNode, exp, stack, n);
                        }
                        break;

                    case VALUES:

                        backtrack = values(p, gNode, exp, stack, n);

                        break;

                    /**
                     * ********************************
                     *
                     * Draft extensions
                     *
                     */
                    case OPT_BIND:
                        /**
                         * use case: ?x p ?y FILTER ?t = ?y BIND(?t, ?y) ?z q ?t
                         *
                         */
                        backtrack = optBind(p, gNode, exp, stack, n);
                        break;

                    case EVAL:

                        // ?doc xpath('/book/title/text()') ?title
                        backtrack = eval(gNode, exp, stack, n);
                        break;

                    case SCOPE:

                        backtrack = eval(p, gNode, stack.copy(exp.get(0), n), n);

                        break;

                    case ACCEPT:
                        // use case: select distinct ?x where
                        // check that ?x is distinct
                        if (optim) {
                            if (results.accept(env.getNode(exp.getNode()))) {
                                // backjump here when a mapping will be found with this node
                                // see store()
                                backjump = n - 1;
                                backtrack = eval(p, gNode, stack, n + 1);
                            }
                        } else {
                            backtrack = eval(p, gNode, stack, n + 1);
                        }
                        break;

                    case EXTERN:

                        // draft trace expression
                        plugin.exec(exp, env, n);
                        backtrack = eval(p, gNode, stack, n + 1);
                        break;

                    case SCAN:
                        // scan a partial result (for trace/debug)

                        Mapping scan = env.store(query, p);
                        if (scan != null) {
                            logger.debug(scan.toString());
                        }
                        backtrack = eval(p, gNode, stack, n + 1);
                        break;

                }
            }
        }

        if (isEvent) {
            send(Event.FINISH, exp, gNode, stack);
        }

        return backtrack;

    }
    
    Mappings getMappings() {
        return memory.getResetJoinMappings();
    }


    /**
     * ____________________________________________________ *
     */
    /**
     * use case:
     *
     * (n) EDGE{?x ?q ?z} (n+1) FILTER{?x = ?y} with BIND {?x := ?y} compiled
     * as:
     *
     * (n) BIND {?x := ?y} (n+1) EDGE{?x ?q ?z} (n+2) FILTER{?x = ?y}
     */
    private int optBind(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        Memory env = memory;
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
            if (!env.isBound(qNode) && producer.isBindable(node)) {
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
    private int cbind(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = memory;
        Producer prod = producer;

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
     * fresh memory mem inherits data from current memory to evaluate exp (in
     * main)
     * Use case: template parameters are bound in memory, bind them in mem
     *
     */
    void bind(Memory mem, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
        if (m != null) {
            mem.push(m, -1);
        }

        if (main.isGraph() && main.getNodeList() != null) {
            bindExpNodeList(mem, main, main.getGraphName());
        } else if ((bind || main.isBinary()) && exp.getNodeList() != null) {
            // A optional B
            // bind variables of A from environment
            bindExpNodeList(mem, exp, null);
        }

        joinMappings(mem, exp, main, map);
    }

    /**
     * Use case: federated query, service clause Eval exp in the context of
     * partial solution Mappings join(A, B) optional(A, B) minus(A, B) union(A,
     * B) A and/or B evaluated in the context of partial solution map map taken
     * into account by service clause if any
     */
    void joinMappings(Memory mem, Exp exp, Exp main, Mappings map) {
        switch (main.type()) {
            case Exp.JOIN:
                service(exp, mem);
        }
        mem.setJoinMappings(map);
    }

    // except may be a graphNode: do not bind it here 
    // because it is bound by graphNode()
    void bindExpNodeList(Memory mem, Exp exp, Node except) {
        for (Node qnode : exp.getNodeList()) {
            // getOuterNodeSelf use case: join(subquery, exp)  -- federated query use case
            // qnode in subquery is not the same as qnode in memory
            if (except == null || qnode != except) {
                Node node = memory.getNode(memory.getQuery().getOuterNodeSelf(qnode));
                if (node != null) {
                    mem.push(qnode, node, -1);
                }
            }
        }
    }

    /**
     * JOIN(service ?s {}, exp) if ?s is bound, bind it for subeval ...
     */
    void service(Exp exp, Memory mem) {
        if (exp.type() == SERVICE) {
            bindService(exp, mem);
        } else {
            for (Exp ee : exp.getExpList()) {

                switch (ee.type()) {

                    case SERVICE:
                        bindService(ee, mem);
                        break;

                    case AND:
                    case BGP:
                    case JOIN:
                        service(ee, mem);
                        break;
                }
            }
        }
    }

    void bindService(Exp exp, Memory mem) {
        Node serv = exp.first().getNode();
        if (serv.isVariable() && memory.isBound(serv)) {
            //System.out.println("KG: " + serv + " " + memory.getNode(serv));
            mem.push(serv, memory.getNode(serv));
        }
    }

    /**
     * Bind graph node in new memory if it is bound in current memory use case:
     * graph ?g {pat1 minus pat2}
     */
    
    private void graphNode(Producer p, Node graphNode, Node queryNode, Memory mem) {
        if (graphNode != null) {
            Node qNode = (queryNode == null) ? graphNode : queryNode;
            if (graphNode.isConstant()) {
                mem.push(qNode, p.getNode(graphNode));
            }
            else if (memory.isBound(graphNode)) {
                mem.push(qNode, memory.getNode(graphNode));
            } 
        }
    }
    
//    private void graphNode2(Producer p, Node graphNode, Node queryNode, Memory mem) {
//        if (graphNode != null) {
//            if (memory.isBound(graphNode)) {
//                mem.push((queryNode == null) ? graphNode : queryNode, memory.getNode(graphNode));
//            } else if (graphNode.isConstant()) {
//                mem.push((queryNode == null) ? graphNode : queryNode, p.getNode(graphNode));
//            }
//        }
//    }

    private int minus(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node queryNode = query.getGraphNode();

        Node node1 = null, node2 = null;
        if (hasGraph) {
            node1 = queryNode;
            node2 = exp.getGraphNode();
        }
        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        if (map1.isEmpty()) {
            return backtrack;
        }

        MappingSet set1 = new MappingSet(map1);
        Exp rest = prepareRest(exp, set1);
        Mappings map2 = subEval(p, gNode, node2, rest, exp, set1.getJoinMappings());

        getVisitor().minus(this, getGraphNode(gNode), exp, map1, map2);

        MappingSet set = new MappingSet(exp, set1, new MappingSet(map2));
        set.setDebug(query.isDebug());
        set.start();
        
        for (Mapping map : map1) {
            if (stop) {
                return STOP;
            }
            boolean ok = !set.minusCompatible(map);
            if (ok) {
                if (env.push(map, n)) {
                    // query fake graph node must not be bound
                    // for further minus ...
                    if (newGraph) { } //do nothing
                    else if (hasGraph) {
                        env.pop(queryNode);
                    }
                    backtrack = eval(p, gNode, stack, n + 1);
                    env.pop(map);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }
        return backtrack;
    }

    /**
     * exp a Join, Minus, Optional, Union
     */
    Exp prepareRest(Exp exp, MappingSet set1) {
        Exp rest = exp.rest();
        // in-scope variables in rest
        // except those that are only in right arg of an optional in rest
        List<Node> nodeListInScope = rest.getRecordInScopeNodes();
        if (!nodeListInScope.isEmpty() && set1.hasIntersection(nodeListInScope)) {
            // generate values when at least one variable in-subscope is always 
            // bound in map1, otherwise it would generate duplicates in map2
            // or impose irrelevant bindings 
            // map = select distinct map1 wrt exp inscope nodes 
            Mappings map = set1.getMappings().distinct(nodeListInScope);
            //Mappings map = set1.getMappings();                      
            map.setNodeList(nodeListInScope);
            if (exp.isJoin() || isAndJoin(rest) || isFederate(rest)) {
                // service clause in rest may take Mappings into account
                set1.setJoinMappings(map);
            } else {
                // inject Mappings in copy of rest as a values clause            
                rest = complete(rest, map);
            }
        }
        return rest;
    }
    
    
    // exp = and(join(and(edge) service()))
    boolean isAndJoin(Exp exp){
        if (exp.isAnd()) {
            if (exp.size() != 1) {
                return false;
            }
            return isAndJoin(exp.get(0));
        }
        else if (exp.isJoin()) {
            Exp fst = exp.get(0);
            if (fst.isAnd() && fst.size() > 0 && fst.get(0).isEdgePath()) {
                fst.setMappings(true);
                return true;
            }
        }
        return false;
    }

    boolean isFederate(Exp exp) {
        if (memory.getQuery().getGlobalQuery().isFederate()) {
            return true;
        }
        return isRecFederate(exp);
    }
        
    /**
     * exp is rest of minus, optional, union: exp is AND
     * exp is rest of join: AND is not mandatory, it may be a service
     * 
     */ 
    boolean isRecFederate(Exp exp){ 
        if (exp.isService()) {
            return true;
        }
        if (exp.size() == 1) {
            Exp ee = exp.get(0);
            return  (ee.isService() ||  (ee.isBinary() &&  isFederate2(ee)));        
        }
        else if (exp.isBGPAnd() && exp.size() > 0) {
            Exp ee = exp.get(0);
            return  (ee.isService() ||  (ee.isBinary() &&  isFederate2(ee))); 
        }
        else {
            return false;
        }
    }
       
    // binary such as union
    boolean isFederate2(Exp exp) {
        return exp.size() == 2 && isRecFederate(exp.get(0)) && isRecFederate(exp.get(1));
    }

    Exp complete(Exp exp, Mappings map) {
        if (map == null || !map.isNodeList()) {
            return exp;
        }
        Exp values = Exp.createValues(map.getNodeList(), map);
        Exp res = exp.duplicate();
        res.getExpList().add(0, values);
        return res;
    }

    // new 
    private int union(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())

        Mappings map1 = unionBranch(p, gNode, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        Mappings map2 = unionBranch(p, gNode, exp.rest(), exp, data);

        getVisitor().union(this, getGraphNode(gNode), exp, map1, map2);
             
        int b1 = unionPush(p, gNode, exp, stack, n, map1);
        int b2 = unionPush(p, gNode, exp, stack, n, map2);

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
    Mappings unionBranch(Producer p, Node gNode, Exp exp, Exp main, Mappings map) {
        Node queryNode = (gNode == null) ? null : query.getGraphNode();
        if (isFederate(exp) || exp.isUnion()) {
            return subEval(p, gNode, queryNode, exp, main, map);
        } else {
            // exp += values(var, map)
            Exp ee = complete(exp, map);
            return subEval(p, gNode, queryNode, ee, main);
        }
    }

    /**
     * Push Mappings of branch of union in the stack
     */
    int unionPush(Producer p, Node gNode, Exp exp, Stack stack, int n, Mappings map) {
        int backtrack = n - 1;
        Memory env = memory;
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            if (env.push(m, n)) {
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(m);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }

   
    private int service2(Node gNode, Exp exp, Stack stack, int n) {
        stack = stack.and(exp.rest(), n);
        int backtrack = eval(gNode, stack, n);
        return backtrack;
    }

    private Mappings service(Node serv, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        mem.init(query);
        mem.share(memory);
        Eval eval = copy(mem, producer);
        Mappings lMap = eval.subEval(query, null, Stack.create(exp), 0);
        return lMap;
    }
    
    
    private int and(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        getVisitor().bgp(this, getGraphNode(gNode), exp, null);
        if (exp.isMappings()) {
            Mappings data = getMappings();
            if (data != null) {
                exp = complete(exp, data);
            }
        }
        stack = stack.and(exp, n);
        return eval(p, gNode, stack, n);
    }


    private int bgp(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        List<Node> from = query.getFrom(gNode);
        Mappings map = p.getMappings(gNode, from, exp, memory);

        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(query);
            boolean b = memory.push(m, n, false);
            if (b) {
                int back = eval(p, gNode, stack, n + 1);
                memory.pop(m);
                if (back < n) {
                    return back;
                }
            }
        }
        return backtrack;
    }

    /**
     *
     * Exp evaluated as a BGP, get result Mappings, push Mappings and continue
     * Use case: cache the Mappings
     */
    private int bgpAble(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Mappings map = getMappings(p, gNode, exp);
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(query);
            boolean b = memory.push(m, n, false);
            if (b) {
                int back = eval(p, gNode, stack, n + 1);
                memory.pop(m);
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
     */
    Mappings getMappings(Producer p, Node gNode, Exp exp) {
        if (exp.hasCache()) {
            Node n = memory.getNode(exp.getCacheNode());
            if (n != null) {
                Mappings m = exp.getMappings(n);
                if (m == null) {
                    m = subEval(p, gNode, gNode, exp, exp, null, null, true);
                    exp.cache(n, m);
                }
                return m;
            }
        }
        return subEval(p, gNode, gNode, exp, exp, null, null, true);
    }

    private int service(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = memory;
        Node serv = exp.first().getNode();
        Node node = serv;

        if (serv.isVariable()) {
            node = env.getNode(serv);
        }

        if (provider != null) {
            // service delegated to provider
            Mappings lMap = provider.service(node, exp, data, this);

            for (Mapping map : lMap) {
                if (stop) {
                    return STOP;
                }
                // push each Mapping in memory and continue
                complete(query, map);
                if (env.push(map, n, false)) {
                    backtrack = eval(gNode, stack, n + 1);
                    env.pop(map, false);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        } else {
            Query q = exp.rest().getQuery();
            return query(p, gNode, q, data, stack, n);
        }

        return backtrack;
    }

    void complete(Query q, Mapping map) {
        int i = 0;
        for (Node node : map.getQueryNodes()) {
            Node out = q.getOuterNode(node);
            map.getQueryNodes()[i] = out;
            i++;
        }
    }

//    Node getNode(Node gNode) {
//        Node gg = memory.getNode(gNode);
//        if (gg != null) {
//            return gg;
//        }
//        if (gNode.isConstant()) {
//            return gNode;
//        }
//        return null;
//    }
    
    Node getNode(Producer p, Node gNode) {
        if (gNode.isConstant()) {
            return p.getNode(gNode);
        } 
        return memory.getNode(gNode);        
    }
    
    Node getGraphNode(Node node) {
        return (node == null) ? null : node.isConstant() ? node : memory.getNode(node);
    }

    /**
     * bind(exp as var)
     */
    private int bind(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        if (exp.isFunctional()) {
            return extBind(p, gNode, exp, stack, n);
        }

        int backtrack = n - 1;
        Memory env = memory;

        env.setGraphNode(gNode);
        Node node = evaluator.eval(exp.getFilter(), env, p);
        env.setGraphNode(null);

        getVisitor().bind(this, getGraphNode(gNode), exp, node == null ? null : node.getDatatypeValue());

        if (node == null) {
            backtrack = eval(p, gNode, stack, n + 1);
        } else if (memory.push(exp.getNode(), node, n)) {
            backtrack = eval(p, gNode, stack, n + 1);
            memory.pop(exp.getNode());
        }

        return backtrack;
    }

    private int extBind(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = memory;
        env.setGraphNode(gNode);
        Mappings map = evaluator.eval(exp.getFilter(), env, exp.getNodeList());
        env.setGraphNode(null);
        getVisitor().values(this, getGraphNode(gNode), exp, map);
        if (map != null) {
            HashMap<String, Node> tab = toMap(exp.getNodeList());
            for (Mapping m : map) {
                if (stop) {
                    return STOP;
                }
                if (env.push(tab, m, n)) {
                    backtrack = eval(p, gNode, stack, n + 1);
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
        HashMap<String, Node> m = new HashMap<String, Node>();
        for (Node node : list) {
            m.put(node.getLabel(), node);
        }
        return m;
    }

    /**
     * Special case: optional{} !bound(?x) When filter fail, backjump before
     * optional
     *
     */
    private int filter(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = memory;
        boolean success = true;

        if (exp.isPostpone()) {
            // use case: optional { filter (exp) }, eval later
        } else {
            env.setGraphNode(gNode);
            success = evaluator.test(exp.getFilter(), env, p);
            env.setGraphNode(null);

            if (hasFilter) {
                success = getVisitor().filter(this, getGraphNode(gNode), exp.getFilter().getExp(), success);
            }
        }

        if (hasEvent) {
            send(Event.FILTER, exp, success);
        }

        if (success) {
            backtrack = eval(p, gNode, stack, n + 1);
        }

        return backtrack;
    }

    private int path(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1, evENUM = Event.ENUM;
        PathFinder path = getPathFinder(exp, p);
        Filter f = null;
        Memory env = memory;
        Query qq = query;
        boolean isEvent = hasEvent;

        if (stack.size() > n + 1) {
            if (stack.get(n + 1).isFilter()) {
                f = stack.get(n + 1).getFilter();
            }
        }

        path.start(exp.getEdge(), query.getPathNode(), env, f);
        boolean isSuccess = false;

        List<Node> list = qq.getFrom(gNode);
        Node bNode = gNode;

        if (p.getMode() == Producer.EXTENSION) {
            if (p.getQuery() == memory.getQuery()) {
                list = empty;
                bNode = p.getGraphNode();
            } else {
                bNode = null;
            }
        }

        for (Mapping map : path.candidate(gNode, list, env)) {
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
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(map);
                //map.setRead(true);

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
            int bj = env.getIndex(bNode, exp.getEdge());
            backtrack = bj;
        }
        path.stop();
        return backtrack;
    }

    private int values(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        getVisitor().values(this, getGraphNode(gNode), exp, exp.getMappings());

        for (Mapping map : exp.getMappings()) {
            if (stop) {
                return STOP;
            }
            if (binding(exp.getNodeList(), map, n)) {
                backtrack = eval(p, gNode, stack, n + 1);
                free(exp.getNodeList(), map);

                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;

    }

    /**
     * values var { val }
     */
    boolean binding(List<Node> varList, Mapping map) {
        return binding(varList, map, -1);
    }

    boolean binding(List<Node> varList, Mapping map, int n) {
        int i = 0;
        for (Node qNode : varList) { //map.getQueryNodes()) {

            Node node = map.getNode(qNode);
            if (node != null) {
                Node value = producer.getNode(node.getValue());
                boolean suc = memory.push(qNode, value, n);
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
        for (Node qq : varList) { //map.getQueryNodes()) {
            Node nn = map.getNode(qq);
            if (nn != null) {
                if (j >= i) {
                    return false;
                } else {
                    j++;
                }
                memory.pop(qq);
            }
        }
        return false;
    }

    void free(List<Node> varList, Mapping map) {
        for (Node qNode : varList) {
            memory.pop(qNode);
        }
    }

    /**
     * Enumerate candidate edges
     *
     */
    private int edge(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isSuccess = false,
                hasGraphNode = gNode != null,
                isEvent = hasEvent;
        Edge qEdge = exp.getEdge();
        Edge previous = null;
        Node graph = null;
        Memory env = memory;
        env.setExp(exp);
        //Producer prod = producer;
        Query qq = query;
        List<Node> list = qq.getFrom(gNode);
        // the backtrack gNode
        Node bNode = gNode;
        boolean matchNBNode = qEdge.isMatchArity();

        if (p.getMode() == Producer.EXTENSION) {
            // Producer is Extension only for the query that created it
            // use case: templates may share same Producer
            if (p.getQuery() == memory.getQuery()) {
                list = empty;
                bNode = p.getGraphNode();
            } else {
                bNode = null;
            }
        }

//        StopWatch sw = new StopWatch();
//        sw.start();
        Iterable<Edge> entities;
        if (hasProduce) {
            // draft
            entities = produce(p, gNode, list, qEdge);
            if (entities == null) {
                entities = p.getEdges(gNode, list, qEdge, env);
            }
        } else {
            entities = p.getEdges(gNode, list, qEdge, env);
        }

        Iterator<Edge> it = entities.iterator();

        while (it.hasNext()) {

            if (stop) {
                return STOP;
            }

            Edge edge = it.next();
            //if (query.isDebug())System.out.println("E: " + edge);
            if (edge != null) {
                nbEdge++;
                if (hasListener && !listener.listen(qEdge, edge)) {
                    continue;
                }

                //Edge edge = ent;
                graph = edge.getGraph();

//				if (draft && edgeToDiffer != null && previous != null){
//					// draft backjump with position
//					// backjump require different node
//					// between previous and current edge
////					if (indexToDiffer != n){
////						System.out.println(query);
////						edgeToDiffer = null;
////					}
////					else 
//						if (! differ(exp, edgeToDiffer, previous, edge)){
//						continue;
//					}
//					else {
//						edgeToDiffer = null;
//					}
//				}
                previous = edge;
                boolean bmatch = match(qEdge, edge, gNode, graph, env);
                
                if (matchNBNode) {
                    bmatch &= (qEdge.nbNode() == edge.nbNode());
                }

                if (bmatch) {
                    if (hasCandidate) {
                        getVisitor().candidate(this, getGraphNode(gNode), qEdge, edge);
                    }

                    bmatch = push(p, qEdge, edge, gNode, graph, n);
                }

                if (isEvent) {
                    send(evENUM, exp, edge, bmatch);
                }

                if (bmatch) {
                    isSuccess = true;
                    backtrack = eval(p, gNode, stack, n + 1);

                    env.pop(qEdge, edge);
                    if (hasGraphNode) {
                        env.pop(gNode);
                    }

                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }
//        sw.stop();
//        logger.info("\n\tGet EDGE in " + sw.getTime() + "ms.  \n\tFOR "+exp+"\n");

        //edgeToDiffer = null;
        if (!isSuccess && optim) {
            // backjump to max index where nodes are bound for first time:
            // (2) x r t
            // (1) y q z
            // (0) x p y
            // (2) may backjump to (0) because they share x
            // in addition we may require to change the value of x by
            // setting edgeToDiffer to x r t
            //int bj = env.getIndex(gNode, qEdge);
            int bj = env.getIndex(bNode, qEdge);
            backtrack = bj;
//			if (draft && !option && ! isSubQuery && bj >=0 && stack.get(bj).isEdge()){
//				// advanced backjump between edge only
//				// require that edge at index bj change at least one node
//				// not with option 
//				edgeToDiffer = exp;
//				// fake index of x
//				//indexToDiffer = bj;
//			}

        }

        return backtrack;
    }

    Iterable<Edge> produce(Producer p, Node gNode, List<Node> from, Edge edge) {
        DatatypeValue res = getVisitor().produce(this, gNode, edge);
        if (res == null) {
            return null;
        }
        if (res.getObject() != null && (res.getObject() instanceof Iterable)) {
            return new IterableEntity((Iterable) res.getObject());
        } else if (res instanceof Loopable) {
            Iterable loop = ((Loopable) res).getLoop();
            if (loop != null) {
                return new IterableEntity(loop);
            }
        }
        return null;
    }

    /**
     * eval callback by name Only with functions defined in the query name is
     * not an export function, but it can call export functions
     *
     * @param name
     * @return
     * @throws EngineException 
         *
     */
    public Object eval(String name, Object[] param) {
        Expr exp = getExpression(name);
        if (exp != null) {
            return eval(exp, param);
        }
        return null;
    }

    public Object eval(Expr exp, Object[] param) {
        return evaluator.eval(exp, memory, producer, param);
    }

    Expr getExpression(String name) {
        return getExpression(query, name);
    }

    Expr getExpression(Query q, String name) {
        return q.getExpression(name, inherit(q, name));
    }

    boolean inherit(Query q, String name) {
        return !(q.isFun() && local.containsKey(name));
    }

    /**
     * select * where {{select distinct ?y where {?x p ?y}} . ?y q ?z} new eval,
     * new memory, share only sub query select variables
     *
     */
    private int query(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        return query(p, p, gNode, exp, data, stack, n);
    }

    private int query(Producer p1, Producer p2, Node gNode, Exp exp, Mappings data, Stack stack, int n) {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isEvent = hasEvent;
        Query subQuery = exp.getQuery();
        Memory env = memory;
        getVisitor().start(subQuery);

        // copy current Eval,  new stack
        // bind sub query select nodes in new memory
        Eval ev = copy(copyMemory(memory, query, subQuery, null), p1, evaluator, subQuery, false);
        // draft federated query
        ev.getMemory().setJoinMappings(data); //memory.getJoinMappings());
        Node subNode = null;

        if (newGraph) {}
        else if (gNode != null) {
            // find equivalent gNode in subquery 
            subNode = subQuery.getGraphNode(gNode);
            if (env.isBound(gNode)) {
                // bind outer gNode as graph node
                ev.getMemory().push(subNode, env.getNode(gNode));
            }
        }

        Node gg = newGraph ? gNode : subNode;
        Mappings lMap = ev.eval(gg, subQuery, null);

        getVisitor().query(this, getGraphNode(gNode), exp, lMap);
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
                backtrack = eval(p2, gNode, stack, n + 1);
                pop(subQuery, map);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }

    /**
     * exp.first() is a subquery that implements a BIND() pop the binding at the
     * end of group pattern
     */
    private int pop(Node gNode, Exp exp, Stack stack, int n) {
        for (Exp ee : exp.first().getQuery().getSelectFun()) {
            Node node = ee.getNode();
            memory.pop(node);
            break;
        }
        return eval(gNode, stack, n + 1);
    }

    /**
     * Edge as Function use case: ?x xpath('/book/title') ?y
     */
    private int eval(Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Edge qEdge = exp.getEdge();
        Node qNode = qEdge.getNode(1);
        Memory env = memory;
        Evaluator ev = evaluator;
        Matcher mm = match;

        for (Node node : ev.evalList(exp.getFilter(), env)) {

            if (mm.match(qNode, node, env) && env.push(qNode, node)) {

                backtrack = eval(gNode, stack, n + 1);
                env.pop(qNode);
                if (backtrack < n) {
//					if (hasEvent){
//						send(Event.FINISH, exp, gNode, stack);
//					}
                    return backtrack;
                }
            }
        }
        return backtrack;
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
     * Is one node of subquery select already bound in memory ?
     */
    private boolean isBound(Query subQuery, Memory env) {
        Query qq = query;

        for (Node subNode : subQuery.getSelect()) {
            if (env.isBound(subNode)) {
                return true;
            }
            // get outer node:
            Node outNode = qq.getOuterNodeSelf(subNode);
            if (outNode != null && env.isBound(outNode)) {
                return true;
            }
            if (env.getBind() != null && env.getBind().isBound(subNode.getLabel())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exec(Exp exp, Environment env, int n) {
        if (exp.getObject() instanceof String) {
            String label = (String) exp.getObject();
            if (env.getNode(label) != null) {
                logger.debug(n + ": " + label + " " + env.getNode(label).getLabel());
            }
        }
    }

    /**
     * Store a new result
     */
    private int store(Producer p) {
        boolean store = true;
        if (listener != null) {
            store = listener.process(memory);
        }
        if (store) {
            nbResult++;
        }
        if (storeResult && store) {
            Mapping ans = memory.store(query, p, isSubEval);
            if (ans != null && acceptable(ans)) {
                //submit(ans);
                if (hasEvent) {
                    send(Event.RESULT, ans);
                }
                boolean b = true;
                if (!isSubEval) {
                    b = getVisitor().distinct(this, query, ans);
                    if (b) {
                        b = getVisitor().result(this, results, ans);
                    }
                }
                if (b) {
                    results.add(ans);
                }
            }
        }
        return -1;
    }

    boolean acceptable(Mapping m) {
        return query.getGlobalQuery().isAlgebra() || results.acceptable(m);
    }

    void submit(Mapping map) {
        if (query.getGlobalQuery().isAlgebra()) {
            // eval distinct later
            results.add(map);
        } else {
            results.submit(map);
        }
    }

    public int nbResult() {
        return results.size();
    }

    public int getCount() {
        return nbEdge;
    }

    private boolean match(Edge qEdge, Edge edge, Node gNode, Node graphNode, Memory memory) {
        if (!match.match(qEdge, edge, memory)) {
            return false;
        }
        if (gNode == null || graphNode == null) {
            return true;
        }
        return match.match(gNode, graphNode, memory);
    }

    private boolean push(Producer p, Edge qEdge, Edge ent, Node gNode, Node node, int n) {
        Memory env = memory;
        if (!env.push(p, qEdge, ent, n)) {
            return false;
        }
        if (gNode != null && !env.push(gNode, node, n)) {
            env.pop(qEdge, ent);
            return false;
        }
        return true;
    }

    private boolean match(Node qNode, Node node, Node gNode, Node graphNode) {
        Memory env = memory;
        if (!match.match(qNode, node, env)) {
            return false;
        }
        if (gNode == null) {
            return true;
        }
        return match.match(gNode, graphNode, env);
    }

    private boolean push(Node qNode, Node node, Node gNode, Node graphNode, int n) {
        Memory env = memory;
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
        Memory env = memory;
        Matcher mm = match;
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
        listener = el;
        hasListener = listener != null;
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

    /**
     * @return the isPathType
     */
    public boolean isPathType() {
        return isPathType;
    }

    /**
     * @param isPathType the isPathType to set
     */
    public void setPathType(boolean isPathType) {
        this.isPathType = isPathType;
    }

    /**
     * @return the sparqlEngine
     */
    public SPARQLEngine getSPARQLEngine() {
        return sparqlEngine;
    }

    /**
     * @param sparqlEngine the sparqlEngine to set
     */
    public void setSPARQLEngine(SPARQLEngine sparqlEngine) {
        this.sparqlEngine = sparqlEngine;
    }

    /**
     * @return the visitor
     */
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void finish() {
        setStop(true);
        join.setStop(true);
        evalGraphNew.setStop(true);
        optional.setStop(true);
    }

}
