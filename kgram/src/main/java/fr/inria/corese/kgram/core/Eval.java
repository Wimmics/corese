package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import java.util.ArrayList;
import java.util.List;


import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
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
import java.util.HashMap;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(Eval.class);
    
    static final int STOP = -2;
    public static int count = 0;
    ResultListener listener;
    EventManager manager;
    private ProcessVisitor visitor;
    private SPARQLEngine sparqlEngine;            
    boolean hasEvent = false;
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
    private boolean  
            hasFilter = false,
            hasCandidate = false,
            hasStatement = false,
            hasProduce  = false;

    //Edge previous;
    /**
     *
     * @param p edge and node producer
     * @param e filter evaluator, given an environment (access to variable
     * binding)
     */
    Eval(Producer p, Evaluator e, Matcher m) {
        producer = p;
        saveProducer = producer;
        evaluator = e;
        match = m;
        plugin = this;
        lPathFinder = new ArrayList<PathFinder>();
        setVisitor(new DefaultProcessVisitor());
        e.setKGRAM(this);
        initCallback();
    }
    
    class DefaultProcessVisitor implements ProcessVisitor {}
    
    void initCallback(){
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
        return query(q, null);
    }

    public Mappings query(Query q, Mapping m) {
        if (hasEvent) {
            send(Event.BEGIN, q);
        }
        initMemory(q);
        if (m != null && m.getBind() != null && memory.getBind() != null) {
            memory.getBind().share(m.getBind());
        }
        producer.start(q);
        getVisitor().init(q);
        getVisitor().before(q);        
        Mappings map = eval(null, q, m);        
        getVisitor().orderby(map);
        getVisitor().after(map);

        producer.finish(q);
        if (hasEvent) {
            send(Event.END, q, map);
        }

        return map;
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
            if (! values.isPostpone() && !q.isAlgebra()) {
                for (Mapping m : values.getMappings()) {
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
        List<Node> from = query.getFrom(gNode);
        Mappings map = p.getMappings(gNode, from, exp, memory);
     */
    Mappings exec(Node gNode, Producer p, Exp exp, Mapping m){
        if (true){
            List<Node> from = query.getFrom(gNode);
            Mappings map = p.getMappings(gNode, from, exp, memory);
            return map;
        }
        Stack stack = Stack.create(exp);
        set(stack); 
        if (m != null){
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
        if (exp.getNodeList() != null){
            for (Node qnode : exp.getNodeList()) {
                Node node = m.getNodeValue(qnode);
                if (node != null) {
                    memory.push(qnode, node, -1);
                }
            }
        }
        else {
            memory.push(m, 0);
        }
    }
    
    /**
     * Evaluate exp with SPARQL Algebra on Mappings, not with Memory stack
     * 
     * */
    void process(Node gNode, Producer p, Exp exp){        
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
     * gNode = subQuery.getGraphNode(graphName);
     * node = env.getNode(graphName)
     * 
     */
    public Mappings query(Node gNode, Node node, Query query) {
        if (gNode != null && node != null) {
            getMemory().push(gNode, node);
        }
        return eval(gNode, query, null);
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
     *
     * Copy current evaluator to eval subquery same memory (share bindings) new
     * exp stack
     */
   
    public Eval copy(Memory m, Producer p, Evaluator e) {
        return copy(m, p, e, query);
    }
    
    // q may be the subQuery
    Eval copy(Memory m, Producer p, Evaluator e, Query q) {
        Eval ev = create(p, e, match);
        if (q != null){
            ev.complete(q);
        }
        ev.setSPARQLEngine(getSPARQLEngine());
        ev.setMemory(m);
        ev.set(provider);
        ev.setVisitor(getVisitor());
        ev.startExtFun(q);
        ev.setPathType(isPathType);
        if (hasEvent) {
            ev.setEventManager(manager);
        }
        return ev;
    }

    void setLevel(int n) {
        level = n;
    }

    void setDebug(boolean b) {
        debug = b;
    }

    public void setSubEval(boolean b) {
        isSubEval = b;
    }

    /**
     * copy memory for sub query 
     * copy sub query select variables that are
     * already bound in current memory
     * Use case: subquery and exists
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

    
//    public Memory getMemory(Exp exp) {
//        return getMemory(memory, exp);
//    }

    /**
     * copy of Memory may be stored in exp. 
     * Reuse data structure after cleaning
     * and init copy current memory content into target memory
     * Use case: exists {}
     */
    public Memory getMemory(Memory memory, Exp exp) {
        Memory mem;
        if (memory.isFake()) {
            // Temporary memory created by PathFinder
            mem = memory;
        } 
        else if (!memory.hasBind() && exp.getObject() != null) {
            mem = (Memory) exp.getObject();
            mem.start();
            memory.copyInto(null, mem, exp);
        } 
        else {
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

    public Memory getMemory() {
        return memory;
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
            getEvaluator().init(memory);
            // create memory bind stack
            memory.init(q);
            if (hasEvent) {
                memory.setEventManager(manager);
            }
            producer.init(q);
            evaluator.start(memory);
            debug = q.isDebug();
            if (q.isAlgebra()){
                complete(q);
            }
            if (debug){
                System.out.println(q);
            }
        }
    }
    
    void complete(Query q){
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
        hasStatement    = getVisitor().statement();
        hasProduce      = getVisitor().produce();
        hasCandidate    = getVisitor().candidate();
        hasFilter       = getVisitor().filter();
    }

    private void complete() {
        results.complete(this);
    }

    int compare(Node n1, Node n2) {
        return evaluator.compare(memory, producer, n1, n2);
    }

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
//			if (hasEvent){
//				send(Event.LIMIT, query, map);
//			}
            // backjump to send finish events to listener
            // and perform 'close instructions' if any
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
     * AND pushes the elements in the stack UNION generates two copies of the
     * stack that are run in sequence, one for each arg of the union OPTION{EXP}
     * generates WATCH EXP CONTINUE OPTION{A UNION B} generates two stacks that
     * share the same CONTINUE if one of them succeeds, the option succeeds
     * GRAPH NODE EXP evaluate the EXP and relations must be quads that contain
     * a graph node NODE enumerate and bind a node NODE may also bind NODEs
     * given in the args EDGE enumerate relations FILTER evaluate a filter
     *
     * QUERY implements select subquery, share bindings NOT { EXP } implements
     * NAF compiled as WATCH EXP BACKJUMP FORALL {}{} EXIST {}
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
    private int eval(Producer p, Node gNode, Stack stack, int n) {
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
                    || (maxExp.type() == UNION || (n == level && maxExp.type() == NEXT))) {
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

            if (exp.isFree() && exp.getNext() == null) {
                // add next for checking success of exp
                Exp next = Exp.create(NEXT);
                next.add(exp);
                exp.setNext(next);
                stack.add(n + 1, next);
            }
                        
            if (exp.isBGPAble()){
                // evaluate and record result for next time
                // template optimization 
                exp.setBGPAble(false);
                backtrack = bgpAble(p, gNode, exp, stack, n);
                exp.setBGPAble(true);
            }
            else {
                // draft test
                if (query.getGlobalQuery().isAlgebra()){
                    switch (exp.type()){
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

                case NEXT:
                    // we have reached the end of an exp, it's next status is success=true
                    exp.status(true);
                    stack.remove(n);
                    backtrack = eval(p, gNode, stack, n);
                    break;

                case EMPTY:

                    eval(p, gNode, stack, n + 1);
                    break;

                case AND:
                        getVisitor().bgp(this, getGraphNode(gNode), exp, null);
                        stack = stack.and(exp, n);
                        backtrack = eval(p, gNode, stack, n);
                    break;

                case BGP:
                    backtrack = bgp(p, gNode, exp, stack, n);
                    break;

                case SERVICE:
                    backtrack = service(p, gNode, exp, stack, n);
                    break;

                case GRAPH:  
                    if (!true) {
                        backtrack = genGraph(p, gNode, exp, stack, n);
                    } 
                    else {
                        Node gg = getNode(exp.getGraphName());
                        if (gg != null && p.isProducer(gg)) {
                            // graph $path { } or named graph in GraphStore 
                            backtrack = inGraph(p, p.getProducer(gg, memory),
                                    gNode, exp, stack, n);
                        } else {
                            backtrack = graph(gNode, exp, stack, n);
                        }
                    }
                    break;

                case RESTORE:
                    backtrack = eval(exp.getProducer(), gNode, stack, n + 1);
                    break;

                case GRAPHNODE:

                    backtrack = graphNode(gNode, exp, stack, n);
                    break;
                                 
                case UNION:
                case QUERY:
                case OPTIONAL:
                case MINUS:
                case JOIN:
                case FILTER:
                case BIND:

                    if (gNode != null && !env.isBound(gNode)) {
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n);
                    } else {
                        switch (exp.type()) {
                            case UNION:
                                backtrack = union(p, gNode, exp, stack, n);
                                break;
                            case QUERY:
                                backtrack = query(p, gNode, exp, stack, n);
                                break;
                            case OPTIONAL:
                                backtrack = optional(p, gNode, exp, stack, n);
                                break;
                            case MINUS:
                                backtrack = minus(p, gNode, exp, stack, n);
                                break;
                            case JOIN:
                                backtrack = join(p, gNode, exp, stack, n);
                                break;
                            case FILTER:
                                backtrack = filter(p, gNode, exp, stack, n);
                                break;
                            case BIND:
                                backtrack = bind(p, gNode, exp, stack, n);
                                break;
                        }
                    }
                    break;


                case WATCH:
                    if (gNode != null && !env.isBound(gNode)) {
                        // bind graph ?g before watch
                        // use case: graph ?g {option {?x ?p ?y} !bound(?x) ?z ?q ?t}
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n);
                    } else {
                        backtrack = watch(p, gNode, exp, stack, n);
                    }
                    break;

                case CONTINUE:

                    // optional succeed, mark it and continue
                    exp.status(!exp.skip());
                    backtrack = eval(p, gNode, stack, n + 1);
                    break;

                case BACKJUMP:

                    // NOT fail: mark it and backjump
                    exp.status(!exp.skip());
                    // PRAGMA: each instruction that pushed something MUST pop it
                    // between WATCH and BACKJUMP, e.g. EDGE and NODE
                    if (isEvent) {
                        send(Event.FINISH, exp, gNode, stack);
                    }
                    return stack.indexOf(exp.first());
               
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

//                case NODE:
//                    backtrack = node(gNode, exp, stack, n);
//                    break;
             
                case VALUES:

                    backtrack = values(p, gNode, exp, stack, n);

                    break;

                case POP:

                    // pop BIND( f(?x) as ?y)
                    backtrack = pop(gNode, exp, stack, n);

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

        if (exp.isFree()) {
            if (exp.getNext().status()) {
                // it has succeeded
            } else {
                // exp is not connected by variables to preceding exp
                // exp has failed (to reach it's next ending exp)
                // hence exp will always fail again
                // so declare it as failing and do not evaluate it again later
                exp.setFail(true);
                if (exp.size() > 0) {
                    // use case:
                    // exp = OPTION{STMT} stack = WATCH STMT CONTINUE
                    // tag STMT as failing
                    exp.get(0).setFail(true);
                }
                // in addition if exp is not in UNION/OPTION we can backjump to stop
                backtrack = -1;
            }
        }

        return backtrack;

    }
    
    Node getGraphNode(Node node) {
        return (node == null) ? null : memory.getNode(node);
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
     * Eval exp alone in a fresh new Memory Node gNode : actual graph node Node
     * node : exp graph node
     */
    public Mappings subEval(Producer p, Node gNode, Node node, Exp exp, Exp main) {
        return subEval(p, gNode, node, exp, main, null, null, false);
    }
    
    Mappings subEval(Producer p, Node gNode, Node node, Exp exp, Exp main, Mappings map) {
        return subEval(p, gNode, node, exp, main, map, null, false);
    }

    Mappings subEval(Producer p, Node gNode, Node node, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
        Memory mem = new Memory(match, evaluator);
        getEvaluator().init(mem);
        mem.init(query);
        mem.setAppxSearchEnv(this.memory.getAppxSearchEnv());
        Eval eval = copy(mem, p, evaluator);
        graphNode(gNode, node, mem);
        bind(mem, exp, main, map, m, bind);        
        Mappings lMap = eval.subEval(query, node, Stack.create(exp), 0);
        return lMap;
    }

    /**
     * fresh memory mem inherits data from current memory to evaluate exp (in main)
     * 
     */
    void bind(Memory mem, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
        if (m != null) {
            mem.push(m, -1);
        }      
        if ((bind || main.isOptional() || main.isJoin() || main.isUnion()) && exp.getNodeList() != null) {           
            // A optional B
            // bind variables of A from environment
            bindExpNodeList(mem, exp);
        }
        joinMappings(mem, exp, main, map);
    }

    /**
     * Use case: federated query, service clause
     * Eval exp in the context of partial solution Mappings
     * join(A, B) optional(A, B) minus(A, B) union(A, B)
     * A and/or B evaluated in the context of partial solution map
     * map taken into account by service clause if any
     */
    void joinMappings(Memory mem, Exp exp, Exp main, Mappings map) {
        switch (main.type()) {
            case Exp.JOIN:
                service(exp, mem);
        }
        mem.setJoinMappings(map);
    }
    
    void bindExpNodeList(Memory mem, Exp exp) {
        for (Node qnode : exp.getNodeList()) {
            // Node node = memory.getNode(qnode);
            // getOuterNodeSelf use case: join(subquery, exp)  -- federated query use case
            // qnode in subquery is not the same as qnode in memory
            Node node = memory.getNode(memory.getQuery().getOuterNodeSelf(qnode));
            if (node != null) {
                mem.push(qnode, node, -1);
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
            System.out.println("KG: " + serv + " " + memory.getNode(serv));
            mem.push(serv, memory.getNode(serv));
        }
    }

    /**
     * Bind graph node in new memory if it is bound in current memory use case:
     * graph ?g {pat1 minus pat2}
     */
    private void graphNode(Node gNode, Node node, Memory mem) {
        if (gNode != null && memory.isBound(gNode)) {
            mem.push((node == null)?gNode:node, memory.getNode(gNode));
        }
    }

    private int watch(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Exp skip = exp.first();
        skip.status(skip.skip()); //true);
        // eval the optional exp
        backtrack = eval(p, gNode, stack, n + 1);
        if (backtrack == STOP) {
            return backtrack;
        }
        // we are back, now test the checker to see
        // if the option fail/not succeed
        if (skip.status()) {
            //  option fail / not succeed: skip it and eval the rest of the stack
            backtrack = eval(p, gNode, stack, stack.indexOf(skip) + 1);
        }
        return backtrack;
    }

    private int minus(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        if (query.isTest()) {
            // old
            return minus1(p, gNode, exp, stack, n);
        }
        else {
            // new
            return minus2(p, gNode, exp, stack, n);        
        }
    }
    
    private int minus2(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node qNode = query.getGraphNode();

        Node node1 = null, node2 = null;
        if (hasGraph) {
            node1 = qNode;
            node2 = exp.getGraphNode();
        }
        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp, memory.getResetJoinMappings());
        
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
            boolean ok = ! set.minusCompatible(map);
            if (ok) {
                if (env.push(map, n)) {
                    // query fake graph node must not be bound
                    // for further minus ...
                    if (hasGraph) {
                        env.pop(qNode);
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
     * exp a Join, Minus or Optional
     */
     Exp prepareRest(Exp exp, MappingSet set1) {
        Exp rest = exp.rest();
        // in-scope variables in rest
        // except those that are only in right arg of an optional in rest
        List<Node> nodeListInScope = exp.rest().getRecordInScopeNodes();  
        if (!nodeListInScope.isEmpty() && set1.hasIntersection(nodeListInScope)) {
            // generate values when at least one variable in-subscope is always 
            // bound in map1, otherwise it would generate duplicates in map2
            // or impose irrelevant bindings 
            // map = select distinct map1 wrt exp inscope nodes 
            Mappings map = set1.getMappings().distinct(nodeListInScope);
            if (exp.isJoin() || isFederate(rest)) {
                // service clause in rest may take Mappings into account
                set1.setJoinMappings(map);
            } 
            else {
                // inject Mappings in copy of rest as a values clause            
                rest = complete(exp.rest(), map);
            }
        }
        return rest;
    }
     
    
//    Exp prepareRest2(Exp exp, MappingSet set1) {
//        Exp rest = exp.rest();
//        // in-scope variables in rest
//        // except those that are only in right arg of an optional in rest
//        List<Node> nodeListInScope = exp.rest().getRecordInScopeNodes();  
//        if (!nodeListInScope.isEmpty() && set1.hasIntersection(nodeListInScope)) {
//            // generate values when at least one variable in-subscope is always 
//            // bound in map1, otherwise it would generate duplicates in map2
//            // or impose irrelevant bindings 
//            Mappings map = set1.getMappings().distinct(nodeListInScope);
//            if (exp.isJoin() || isFederate(rest)) {
////                    || memory.getQuery().getGlobalQuery().isFederate() // everybody is a service 
////                    || rest.size() == 1 && rest.get(0).isService() // rest is a service
////               ) {
//                // service clause in rest may take Mappings into account
//                // select distinct map1 wrt map2 inscope nodes  
//                //exp.rest().setMappings(map1dist);
//                set1.setJoinMappings(map);
//            } 
//            else {
//                // inject Mappings in right arg as a values clause            
//                // select distinct map1 wrt map2 inscope nodes 
////                Exp values = Exp.createValues(nodeListInScope, map1dist);               
////                rest = exp.rest().duplicate();
////                rest.getExpList().add(0, values);
//                rest = complete(exp.rest(), map);
//            }
//        }
//        return rest;
//    }
    
    boolean isFederate(Exp exp) {
        return memory.getQuery().getGlobalQuery().isFederate()
                || exp.size() == 1 && exp.get(0).isService();
    }
    
    Exp complete(Exp exp, Mappings map) {
        if (map == null || ! map.isNodeList()) {
            return exp;
        }
        Exp values = Exp.createValues(map.getNodeList(), map);
        Exp res = exp.duplicate();
        res.getExpList().add(0, values);
        return res;
    }
    
    private int minus1(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node qNode = query.getGraphNode();

        Node node1 = null, node2 = null;
        if (hasGraph) {
            node1 = qNode;
            node2 = exp.getGraphNode();
        }
        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp);
        
        if (map1.isEmpty()) {
            return backtrack;
        }
        
        Exp rest = exp.rest();
                            
        Mappings map2 = subEval(p, gNode, node2, rest, exp);
        
        if (query.isDebug()) {
            System.out.println("Ev minus: " + map1.size() + " " +map2.size());
        }
                        
        for (Mapping map : map1) {
            boolean ok = true;
            for (Mapping minus : map2) {
                if (map.compatible(minus)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                if (env.push(map, n)) {
                    // query fake graph node must not be bound
                    // for further minus ...
                    if (hasGraph) {
                        env.pop(qNode);
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
     * JOIN(e1, e2) Eval e1, eval e2, generate all joins that are compatible in
     * cartesian product
     */
    private int join(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Memory env = memory;
        
        Mappings map1 = subEval(p, gNode, gNode, exp.first(), exp, memory.getResetJoinMappings());
        if (map1.size() == 0) {
            //exp.rest().setMappings(null);
            getVisitor().join(this, getGraphNode(gNode), exp, map1, map1);
            return backtrack;
        }
           
        MappingSet set1 = new MappingSet(map1);
        Exp rest = prepareRest(exp, set1);
        
        Mappings map2 = subEval(p, gNode, gNode, rest, exp, set1.getJoinMappings());
        
        getVisitor().join(this, getGraphNode(gNode), exp, map1, map2);

        if (map2.size() == 0) {
            return backtrack;
        }

        Node qn1 = null, qn2 = null;

        for (int j = n + 1; j < stack.size(); j++) {
            // check if next exp is filter (?x = ?y)
            // where ?x in map1 and ?y in map2
            Exp e = stack.get(j);

            if (!e.isFilter()) {
                break;
            } else if (e.size() == 1 && e.get(0).isBindVar()) {
                // b = BIND(?x, ?y)
                Exp b = e.get(0);
                qn1 = b.get(0).getNode();
                qn2 = b.get(1).getNode();

                // Do the mappings bind  variables ?x and ?y respectively ?
                if ((map1.get(0).getNode(qn1) != null && map2.get(0).getNode(qn2) != null)) {
                    // ok do nothing
                    break;
                } else if ((map1.get(0).getNode(qn2) != null && map2.get(0).getNode(qn1) != null)) {
                    // ok switch variables: qn1 for map1 etc.
                    Node tmp = qn2;
                    qn2 = qn1;
                    qn1 = tmp;
                    break;
                } else {
                    // Mappings do not bind variables
                    qn2 = null;
                }

//                if (qn2 != null) {
//                    // sort map2 Mappings according to value of ?y
//                    // in order to perform dichotomy with ?x = ?y
//                    // ?x in map1, ?y in map2
//                    map2.sort(this, qn2);
//                }
            }
        }

        if (qn1 != null && qn2 != null) {
            // sort map2 Mappings according to value of ?y
            // in order to perform dichotomy with ?x = ?y
            // ?x in map1, ?y in map2
            map2.sort(this, qn2);

            // exploit dichotomy for ?x = ?y
            for (Mapping m1 : map1) {

                Node n1 = m1.getNode(qn1);
                if (n1 != null) {

                    if (env.push(m1, n)) {

                        // index of ?y in map2
                        int nn = map2.find(n1, qn2);

                        if (nn >= 0 && nn < map2.size()) {

                            for (int i = nn; i < map2.size(); i++) {

                                // enumerate occurrences of ?y in map2
                                Mapping m2 = map2.get(i);
                                Node n2 = m2.getNode(qn2);
                                if (n2 == null || !n1.match(n2)) { // was equals
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval(p, gNode, stack, n + 1);
                                    env.pop(m2);
                                    if (backtrack < n) {
                                        return backtrack;
                                    }
                                }
                            }

                        }

                        env.pop(m1);
                    } else {
                        //System.out.println("fail push");
                    }
                }
            }
        } else {
            backtrack = join(p, gNode, stack, env, map1, map2, n);
        }
        return backtrack;
    }

    int join(Producer p, Node gNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) {
        int backtrack = n - 1;
       
        Node q = map1.getCommonNode(map2);

        if (q == null) {
            // no variable in common : simple cartesian product
            backtrack = simpleJoin(p, gNode, stack, env, map1, map2, n);
        } else {
            // map1 and map2 share a variable q
            // sort map2 on q
            // enumerate map1
            // retreive the index of value of q in map2 by dichotomy
            map2.sort(this, q);

            for (Mapping m1 : map1) {

                // value of q in map1
                Node n1 = m1.getNode(q);
                if (env.push(m1, n)) {

                    if (n1 == null) {
                        // enumerate all map2
                        for (Mapping m2 : map2) {

                            if (env.push(m2, n)) {
                                backtrack = eval(p, gNode, stack, n + 1);
                                env.pop(m2);
                                if (backtrack < n) {
                                    return backtrack;
                                }
                            }
                        }
                    } else {

                        // first, try : n2 == null
                        for (Mapping m2 : map2) {

                            Node n2 = m2.getNode(q);
                            if (n2 != null) {
                                break;
                            }

                            if (env.push(m2, n)) {
                                backtrack = eval(p, gNode, stack, n + 1);
                                env.pop(m2);
                                if (backtrack < n) {
                                    return backtrack;
                                }
                            }
                        }

                        // second, try : n2 != null
                        int nn = map2.find(n1, q);

                        if (nn >= 0 && nn < map2.size()) {

                            for (int i = nn; i < map2.size(); i++) {

                                // get value of q in map2
                                Mapping m2 = map2.get(i);
                                Node n2 = m2.getNode(q);

                                if (n2 == null || !n1.match(n2)) { // was equals
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval(p, gNode, stack, n + 1);
                                    env.pop(m2);
                                    if (backtrack < n) {
                                        return backtrack;
                                    }
                                }
                            }
                        }
                    }

                    env.pop(m1);
                }
            }
        }
        return backtrack;
    }

    int simpleJoin(Producer p, Node gNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n) {
        int backtrack = n - 1;
        for (Mapping m1 : map1) {

            if (env.push(m1, n)) {

                for (Mapping m2 : map2) {

                    if (env.push(m2, n)) {
                        backtrack = eval(p, gNode, stack, n + 1);
                        env.pop(m2);
                        if (backtrack < n) {
                            return backtrack;
                        }
                    }
                }

                env.pop(m1);
            }
        }
        return backtrack;
    }
    
    
    
    private int union(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        if (query.isTest()) {
            return union2(p, gNode, exp, stack, n);
        }
        else {
            return union1(p, gNode, exp, stack, n);
        }
    }

    

    // new 
    private int union1(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())
        Mappings map = memory.getResetJoinMappings();

        Mappings map1 = unionBranch(p, gNode, exp.first(), exp, map);
        Mappings map2 = unionBranch(p, gNode, exp.rest(),  exp, map);

        getVisitor().union(this, getGraphNode(gNode), exp, map1, map2);

        int b1 = unionPush(p, gNode, exp, stack, n, map1);
        int b2 = unionPush(p, gNode, exp, stack, n, map2);

        return backtrack;
    }
    
    /**
     * Eval one exp of union main
     * map is partial solution Mappings resulting from previous statement evaluation,
     * typically eval(A) in join(A, union(B, C))
     * In federated case (or if exp is itself a union), 
     * map is passed to subEval(exp, map), it may be taken into account by service in exp
     * In non federated case, map is included in copy of exp as a values(var, map) clause
     */
    Mappings unionBranch(Producer p, Node gNode, Exp exp, Exp main, Mappings map) {
        Node ggNode = (gNode == null) ? null : query.getGraphNode();
        if (isFederate(exp) || exp.isUnion()) {
            return subEval(p, gNode, ggNode, exp, main, map);
        } else {
            // exp += values(var, map)
            Exp ee = complete(exp, map);
            return subEval(p, gNode, ggNode, ee, main);
        }
    }

    /**
     * Push Mappings of branch of union in the stack   
     */
    int unionPush(Producer p, Node gNode, Exp exp, Stack stack, int n, Mappings map) {
        int backtrack = n - 1;
        Memory env = memory;
        for (Mapping m : map) {
            if (env.push(m, n)) {
                backtrack = eval(p, gNode, stack, n+1 );
                env.pop(m);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }
    
    private int genGraph(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n -1;
        Node name = exp.getGraphName();
        Node gg = getNode(name);
        Node ggNode = query.getGraphNode();        
        Mappings map = memory.getResetJoinMappings();
        Mappings res;
        
        if ((gg != null && p.isProducer(gg)) || memory.isBound(name)) {
            res = graph(p, exp, stack, map, n);          
        }
        else {
            res = genGraphNodes(exp, stack, map, n);
        }
                        
        if (res == null) {
            return backtrack;        
        }
        Memory env = memory;
        
        for (Mapping m : res) { 
            Node val1 = null, val2 = null;
            if (ggNode != null) {
                val1 = m.getNode(ggNode);
                val2 = m.getNode(name);
                if (val1 != null && val2 != null && ! val1.equals(val2)) {
                    continue;
                }
            }
            if (env.push(m, n)) {
                boolean pop = false;
                if (val1 != null) {
                    env.pop(ggNode);
                    if (val2 == null) {
                        if (env.push(name, val1)) {
                            pop = true;
                        }
                        else {
                            env.pop(m);
                            continue;
                        }
                    }
                }
                
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(m);
                if (pop) {
                    env.pop(name);
                }
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }
    
    
    private Mappings genGraphNodes(Exp exp, Stack stack, Mappings map, int n) {
        Memory env = memory;
        Producer prod = producer;
        Query qq = query;
        Matcher mm = match;
        int backtrack = n - 1;
        Node name = exp.getGraphName();        
        Mappings res = null;
        
        Iterable<Node> graphNodes = null;
        if (map != null) {
            List<Node> list = map.aggregate(name);
            if (! list.isEmpty()) {
                graphNodes = list;
            }           
        }
        if (graphNodes == null) {
            graphNodes = prod.getGraphNodes(name, qq.getFrom(name), env);
        }
        
        for (Node graph : graphNodes) {
            if (mm.match(name, graph, env) &&
                env.push(name, graph, n)) {
                Mappings m = graph(prod, exp, stack, map, n);
                if (res == null) {
                    res = m;
                }
                else {
                    res.add(m);
                }
                env.pop(name);            
            }
        }
        return res;
    }
    
    private Mappings graph(Producer p,  Exp exp, Stack stack, Mappings map, int n) {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())
        
        Node name = exp.getGraphName();
        Node gg = getNode(name);
        Node ggNode = query.getGraphNode(); 
        Producer np = p;
        if (gg != null && p.isProducer(gg)) {
            // graph ?g { }
            // named graph in GraphStore 
            np = p.getProducer(gg, memory);                             
            np.setGraphNode(name);  // the new gNode
            ggNode = null;  
        }
        
        
        Exp main = exp;
        Exp body = exp.rest();
        Mappings res;
       
        if (isFederate(exp)) {
            res = subEval(np, name, ggNode, body, main, map);
        } 
        else {
            // exp += values(var, map)
            Exp ee = body;
            if (gg.getPath() == null){
                // TODO: fix it
                ee = complete(body, map); 
            }
            res = subEval(np, name, ggNode, ee, main);
        }
        return res;      
    }

    
     private Mappings graph2(Producer np, Producer p, Node ggNode,  Exp exp, Stack stack, Mappings map, int n) {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())
                     
        Exp main = exp;
        Exp body = exp.rest();
        Mappings res;
        Node name   = exp.getGraphName();
       
        if (isFederate(exp)) {
            res = subEval(np, name, ggNode, body, main, map);
        } 
        else {
            // exp += values(var, map)
            Exp ee = complete(body, map);           
            res = subEval(np, name, ggNode, ee, main);
        }
        return res;      
    }

     // old
    private int union2(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        Stack nstack = stack.copy(exp.first(), n);
        int b1 = eval(p, gNode, nstack, n);
        if (b1 == STOP) {
            return b1;
        }
        // clean past because we test other branch of union
        //if (draft) edgeToDiffer = null;
        if (debug && exp.rest().getStack() == null) {
            // warn debug trace that we go into second branch
            // of a UNION for the first time
            maxExp = exp;
        }
        nstack = stack.copy(exp.rest(), n);
        backtrack = eval(p, gNode, nstack, n);

        if (backtrack == STOP) {
            return backtrack;
        }
        // backjump is max of both indexes
        if (b1 > backtrack) {
            backtrack = b1;
        }

        // cannot require to change a node because first
        // branch of union may succeed with same node
        //if (draft) edgeToDiffer = null;
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
        Eval eval = copy(mem, producer, evaluator);
        Mappings lMap = eval.subEval(query, null, Stack.create(exp), 0);
        return lMap;
    }

    private int bgp(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        List<Node> from = query.getFrom(gNode);
        Mappings map = p.getMappings(gNode, from, exp, memory);
        
        for (Mapping m : map) {
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
      * Mappings of exp may be cached for specific query node
      * Use case: datashape template exp = graph ?shape {?sh sh:path ?p} 
      * exp is evaluated once for each value of ?sh and Mappings are cached
      * successive evaluations of exp on ?sh get Mappings from cache 
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


    private int service(Producer p, Node gNode, Exp exp, Stack stack, int n) {      
        int backtrack = n - 1;
        Memory env = memory;
        Node serv = exp.first().getNode();
        Node node = serv;

        if (serv.isVariable()) {
            node = env.getNode(serv);
        }

        if (provider != null) {
            // service delegated to provider
            Mappings lMap = provider.service(node, exp, env.getJoinMappings(), this);
            
            for (Mapping map : lMap) {
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
            return query(p, gNode, q, stack, n);
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

    /**
     * exp is graph ?g { PAT } set GRAPHNODE(?g) before and GRAPHNODE(?gNode)
     * after
     */
    private int graph(Node gNode, Exp exp, Stack stack, int n) {
        getVisitor().graph(this, exp.getGraphName(), exp, null);
        int backtrack = n - 1;
        // set GRAPHNODE
        stack.set(n, exp.first());
        // set graph body
        stack.add(n + 1, exp.rest());
        Exp restore = Exp.create(GRAPHNODE, Exp.create(NODE, gNode));
        restore.status(true);
        // set restore previous graph node
        stack.add(n + 2, restore);

        if (exp.first().rest() != null) {
            // use case: GRAPHNODE(NODE(?g), FILTER(?g = 'src'))
            // add the filter
            stack.add(n + 1, exp.first().rest());
        }
        // do next
        backtrack = eval(gNode, stack, n);
        return backtrack;
    }

    /**
     * exp is GRAPHNODE(?g) set current gNode as ?g
     */
    private int graphNode(Node gNode, Exp exp, Stack stack, int n) {
        // two cases:
        // 1. leave graph ?g {} reset gNode to former graph node (may be null)
        // 2. enter graph ?g {} set gNode to ?g

        int backtrack = n - 1;
        Node nextGraph = exp.first().getNode();
        List<Node> from = query.getFrom(gNode);

        Memory env = memory;

        if (exp.status()) {
            // leave graph ?g {}
            if (env.isBound(gNode)) {
                // check that ?g is in from 
                // use case:
                // graph ?g {{select * where {?x ?p ?g}}}
                if (!producer.isGraphNode(gNode, from, env)) {
                    return env.getIndex(gNode);
                }
            } else {
                return graphNodes(gNode, nextGraph, exp, stack, n, n + 1);
            }
        } // set new graph 
        else if (env.isBound(nextGraph)) {
            // check that ?g is a graph 
            // use case:
            // ?x ?p ?g . graph ?g { }
            //if (! producer.isGraphNode(nextGraph, from, env)){
            if (!producer.isGraphNode(nextGraph, query.getFrom(nextGraph), env)) {
                return env.getIndex(nextGraph);
            }
        }

        // leaving: set new graph 
        if (exp.status()) {
            // leave graph ?g {} ; restore previous graph node (or null)
            backtrack = eval(nextGraph, stack, n + 1);
        } else if (query.getFrom(nextGraph).size() > 0) {
            // from named graph ?g {}
            // enumerate target named graphs
            backtrack = graphNodes(nextGraph, nextGraph, exp, stack, n, n + 1);
        } else {
            // graph ?g {}
            // variable ?g bound by target pattern
            backtrack = eval(nextGraph, stack, n + 1);
        }

        return backtrack;
    }

    /**
     * exp is (1) GRAPHNODE(nextGraph) or (2) option|not (1) gNode is not bound,
     * enumerate graph names here then set gNode as nextGraph use case : graph
     * ?gNode { optional { }} . GRAPHNODE(nextGraph) next is n+1 (2) exp =
     * option{} | not{} gNode is not bound, enumerate graph nodes and bind gNode
     * use case: graph ?gNode { not {} } use case: graph ?gNode { optional {?x p
     * ?y } !bound(?y) ?z q ?t} next is n
     */
    private int graphNodes(Node gNode, Node nextGraph, Exp exp, Stack stack, int n, int next) {
        Memory env = memory;
        Producer prod = producer;
        Query qq = query;
        Matcher mm = match;
        int backtrack = n - 1;

        for (Node graph : prod.getGraphNodes(gNode, qq.getFrom(gNode), env)) {
            if (//member(graph, query.getFrom(gNode)) && 
                    mm.match(gNode, graph, env)
                    && env.push(gNode, graph, n)) {
                // set new/former gNode
                backtrack = eval(nextGraph, stack, next);
                env.pop(gNode);
                if (backtrack < next - 1) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }

    Node getNode(Node gNode) {
        Node gg = memory.getNode(gNode);
        if (gg != null) {
            return gg;
        }
        if (gNode.isConstant()) {
            return gNode;
        }
        return null;
    }

    /**
     * graph $path { } switch producer p to np = producer($path) add RESTORE(p)
     * after this graph exp
     */
    private int inGraph(Producer p, Producer np, Node gNode, Exp exp, Stack stack, int n) {
        np.setGraphNode(exp.getGraphName());  // the new gNode
        int backtrack = n - 1;

        stack.set(n, exp.rest());

        Exp next = getRestore(p, exp);
        stack.add(n + 1, next);
        backtrack = eval(np, gNode, stack, n);
        for (int i = n + 1; i < stack.size() && stack.get(i) != next;) {
            stack.remove(i);
        }
        if (stack.size() > n + 1) {
            stack.remove(n + 1);
        }
        stack.set(n, exp);
        return backtrack;
    }

    Exp getRestore(Producer p, Exp exp) {
        Exp next = exp.getRestore();
        if (next == null) {
            next = Exp.create(RESTORE);
            exp.setRestore(next);
        }
        next.setProducer(p);
        return next;
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
        
        getVisitor().bind(this, getGraphNode(gNode), exp, node==null?null:node.getDatatypeValue());

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
        Mappings map = evaluator.eval(exp.getFilter(), env, exp.getNodeList());
        getVisitor().values(this, getGraphNode(gNode), exp, map);
        if (map != null) {
            HashMap<String, Node>  tab =  toMap(exp.getNodeList());
            for (Mapping m : map) {
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
    
    HashMap<String, Node> toMap(List<Node> list){
        HashMap<String, Node> m = new HashMap<String, Node>();
        for (Node node : list){
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
        else if (exp.status()) {
            // this is deprecated, it was used with OPTION() semantics
            // it is not used with OPTIONAL()
            if (exp.getNode() != null) {
                // ! bound(?x)
                if (stack.get(n - 1).type() == CONTINUE) {
                    // optional {} !bound()
                    // WATCH BODY CONTINUE !bound(?x)
                    // get index of WATCH:
                    backtrack = stack.indexOf(stack.get(n - 1).first());
                } else {
                    // backjump just before where ?x was bound                   
                    backtrack = env.getIndex(exp.getNode()) - 1;
                }
            }
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
            boolean b = match(map);
            boolean success = match(map) && env.push(map, n);

            if (isEvent) {
                send(evENUM, exp, map, success);
            }

            if (success) {
                isSuccess = true;
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(map);
                map.setRead(true);

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
        for (Node qNode : varList){ //map.getQueryNodes()) {

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
        for (Node qq : varList){ //map.getQueryNodes()) {
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

            Edge edge = it.next();
            //if (query.isDebug())System.out.println("E: " + ent);
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

                if (bmatch) {
                    if (hasCandidate) {
                        getVisitor().candidate(this, getGraphNode(gNode), qEdge, edge);
                    }
 
                    bmatch = push(qEdge, edge, gNode, graph, n);
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
            return new IterableEntity((Iterable)res.getObject());
        }
        else if (res instanceof Loopable) {
            Iterable loop = ((Loopable) res).getLoop();
            if (loop != null) {
                return new IterableEntity(loop);
            }
        }
        return null;
    }
    

      /**
         * eval callback by name
         * Only with functions defined in the query 
         * name is not an export function, but it can call export functions
         * @param name
         * @return
         * @throws EngineException 
         * */
       
	public Object eval(String name, Object[] param) {
            Expr exp = getExpression(name);           
            if (exp != null){              
                return  eval(exp, param);
            }
            return null;           
        }
    
   public Object eval(Expr exp, Object[] param){
        return evaluator.eval(exp, memory, producer, param);
    }

    Expr getExpression(String name) {
        return getExpression(query, name);
    }

    Expr getExpression(Query q, String name) {       
        return q.getExpression(name, inherit(q, name));
    }
    
    boolean inherit(Query q, String name){
        return ! (q.isFun() && local.containsKey(name));
    }

    /**
     * select * where {{select distinct ?y where {?x p ?y}} . ?y q ?z} new eval,
     * new memory, share only sub query select variables
     *
     */
    private int query(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        return query(p, p, gNode, exp, stack, n);
    }
    
    private int query(Producer p1, Producer p2, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isEvent = hasEvent;
        Query subQuery = exp.getQuery();
        Memory env = memory;
        Mappings lMap;

        if (exp.getObject() != null && !isBound(subQuery, env) && gNode == null) {
            // result is cached, can reuse it
            lMap = (Mappings) exp.getObject();
        } else 
        {
            // copy current Eval,  new stack
            // bind sub query select nodes in new memory
            Eval ev = copy(copyMemory(memory, query, subQuery, null), p1, evaluator, subQuery);
            // draft federated query
            ev.getMemory().setJoinMappings(memory.getJoinMappings());
            Node subNode = null;

            if (gNode != null) {
                // find equivalent gNode in subquery 
                subNode = subQuery.getGraphNode(gNode);
                if (env.isBound(gNode)) {
                    // bind outer gNode as graph node
                    ev.getMemory().push(subNode, env.getNode(gNode));
                }
            }
            
            lMap = ev.eval(subNode, subQuery, null);
            if (! subQuery.isFun() && !isBound(subQuery, env) && gNode == null && ! query.getGlobalQuery().isAlgebra()) {
                exp.setObject(lMap);
            }
        }
        
        getVisitor().query(this, getGraphNode(gNode), exp, lMap);
        
        // enumerate the result of the sub query
        // bind the select nodes into the stack
        for (Mapping map : lMap) {
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
     * SPARQL semantics
     *
     */
    private int optional(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        if (query.isTest()) {
            return optional1(p, gNode, exp, stack, n);
        }
        else {
            return optional2(p, gNode, exp, stack, n);
        }
    }
    
    /**
     * Draft test
     * 
     * A optional B Filter F
     * map1 = eval(A) ; map2 = eval(values Vb { distinct(map1/Vb) }  B)
     * Vb = variables in-subscope in B, ie in-scope except in right arg of an optional in B
     * for m1 in map1: for m2 in map2:
     * if m1.compatible(m2): merge = m1.merge(m2)
     * if eval(F(merge)) result += merge ...
     */
    private int optional2(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node proxyGraphNode = null;
        if (hasGraph) {
            proxyGraphNode = query.getGraphNode();
        }

        Mappings map1 = subEval(p, gNode, proxyGraphNode, exp.first(), exp, memory.getResetJoinMappings());
        if (map1.isEmpty()) {
            return backtrack;
        }
        
        MappingSet set1 = new MappingSet(map1);
        Exp rest = prepareRest(exp, set1); 
        /**
         * Push bindings from map1 into rest
         * when there is at least one variable in-subscope of rest
         * that is always bound in map1
         * ?x p ?y optional { ?y q ?z } -> values ?y { y1  yn }
         * {?x p ?y optional { ?y q ?z }} optional { ?z r ?t } -> 
         * if ?z is not bound in every map1, generate no values.
         */
        Mappings map2 = subEval(p, gNode, proxyGraphNode, rest, exp, set1.getJoinMappings());
        
        getVisitor().optional(this, getGraphNode(gNode), exp, map1, map2);
                       
        MappingSet set = new MappingSet(exp, set1, new MappingSet(map2));
        set.setDebug(query.isDebug());
        set.start();
               
        for (Mapping m1 : map1) {
            boolean success = false;
            int nbsuc = 0;
            for (Mapping m2 : set.getCandidateMappings(m1)) {  
                Mapping merge = m1.merge(m2);
                if (merge != null) {
                    success = filter(proxyGraphNode, merge, exp);
                    if (success) {
                        nbsuc++;
                        if (env.push(merge, n)) {
                            if (hasGraph) {
                                env.pop(proxyGraphNode);
                            }
                            backtrack = eval(p, gNode, stack, n + 1);
                            env.pop(merge);
                            if (backtrack < n) {
                                return backtrack;
                            }
                        }
                    }
                }
            }

            if (nbsuc == 0) { 
                if (env.push(m1, n)) {
                    if (hasGraph) {
                        env.pop(proxyGraphNode);
                    }
                    backtrack = eval(p, gNode, stack, n + 1);
                    env.pop(m1);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }
    
    
    /**
     * proxyGraphNode is the fake graphNode ?_kgram_ that is a proxy for the named graph ?g
     */
    boolean filter(Node proxyGraphNode, Mapping map, Exp exp) {
        if (exp.isPostpone()) {
            // A optional B
            // filters of B must be evaluated now
            for (Exp f : exp.getPostpone()) {
                map.setQuery(query);
                map.setMap(memory.getMap());
                map.setBind(memory.getBind());
                map.setGraphNode(proxyGraphNode);   
                boolean b = evaluator.test(f.getFilter(), map);
                map.setGraphNode(null);  
                if (hasFilter) {
                    Node graphNode = (proxyGraphNode == null) ? null : map.getNode(proxyGraphNode);
                    b = getVisitor().filter(this, graphNode, f.getFilter().getExp(), b);
                }
                if (! b) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    
    private int optional1(Producer p, Node gNode, Exp exp, Stack stack, int n) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Node qNode = query.getGraphNode();
        Memory env = memory;

        Node node1 = null;
        if (hasGraph) {
            node1 = qNode;
        }

        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp);

        for (Mapping r1 : map1) {
            boolean success = false;

            Mappings map2 = subEval(p, gNode, node1, exp.rest(), exp, null, r1, false);
            int nbsuc = 0;
            
            for (Mapping r2 : map2) {
                success = filter(gNode, r2, exp);
                if (success){
                    nbsuc++;
                    if (env.push(r2, n)) {
                        if (hasGraph) {
                            env.pop(qNode);
                        }
                        backtrack = eval(p, gNode, stack, n + 1);
                        env.pop(r2);
                        if (backtrack < n) {
                            return backtrack;
                        }
                    }
                }
            }

            if (nbsuc == 0) { // || fail) {
                if (env.push(r1, n)) {
                    if (hasGraph) {
                        env.pop(qNode);
                    }
                    backtrack = eval(p, gNode, stack, n + 1);
                    env.pop(r1);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }

    /**
     * In case of backjump Some node must differ between previous and current A
     * node that is common to qEdge and qPast
     */
//    private boolean differ(Exp qEdge, Exp qPast, Edge previous, Edge current) {
//        for (int i = 0; i < qEdge.nbNode(); i++) {
//            Node qNode = qEdge.getNode(i);
//            if (qNode != null && qPast.contains(qNode)) {
//                // They share a node, do they differ ?
//                if (!previous.getNode(i).same(current.getNode(i))) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

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
            if (env.isBound(subNode)){
                return true;
            }
            // get outer node:
            Node outNode = qq.getOuterNodeSelf(subNode);            
            if (outNode != null && env.isBound(outNode)) {
                return true;
            }
            if (env.getBind() != null && env.getBind().isBound(subNode.getLabel())){
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
                if (! isSubEval) {
                    b = getVisitor().distinct(ans);
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
        if (query.getGlobalQuery().isAlgebra()){
            // eval distinct later
            results.add(map);
        }
        else {
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

    private boolean push(Edge qEdge, Edge ent, Node gNode, Node node, int n) {
        Memory env = memory;
        if (!env.push(qEdge, ent, n)) {
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

   

}
