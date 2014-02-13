package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Plugin;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.api.query.Results;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.edelweiss.kgram.event.EventImpl;
import fr.inria.edelweiss.kgram.event.EventListener;
import fr.inria.edelweiss.kgram.event.EventManager;
import fr.inria.edelweiss.kgram.event.ResultListener;
import fr.inria.edelweiss.kgram.path.PathFinder;
import fr.inria.edelweiss.kgram.tool.Message;
import fr.inria.edelweiss.kgram.tool.ResultsImpl;
import java.util.Iterator;

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

    private static Logger logger = Logger.getLogger(Eval.class);
    static final int STOP = -2;
    ResultListener listener;
    EventManager manager;
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
        return eval(null, q, null);
    }

    public Mappings query(Query q, Mapping map) {
        if (hasEvent) {
            send(Event.BEGIN, q);
        }

        Mappings maps = eval(null, q, map);

        if (hasEvent) {
            send(Event.END, q, maps);
        }

        return maps;
    }

    Mappings eval(Query q) {
        return eval(null, q, null);
    }

    private Mappings eval(Node gNode, Query q, Mapping map) {
        init(q);

        if (q.isCheck()) {
            // Draft
            Checker check = Checker.create(this);
            check.check(q);
        }

        if (map != null) {
            bind(map);
        }

        if (!q.isFail()) {
            if (q.getMappings() != null) {

                for (Mapping m : q.getMappings()) {
                    if (binding(m)) {
                        eval(gNode, q);
                        free(m);
                    }
                }
            } else {
                eval(gNode, q);
            }

            aggregate();
            // order by
            complete();
            template();
        }

        if (debug && !isSubEval && !q.isSubQuery()) {
            debug();
        }

        return results;
    }

    public Mappings filter(Mappings map, Query q) {
        Query qq = map.getQuery();
        init(qq);
        qq.compile(q.getHaving());
        qq.index(qq, q.getHaving().getFilter());
        //q.complete();

        map.filter(evaluator, q.getHaving().getFilter(), memory);
        return map;
    }

    void eval(Node gNode, Query q) {
        if (q.isFunctional()) {
            // select xpath() as ?val
            // select unnest(fun()) as ?x
            function();
        } else {
            Stack stack = Stack.create(q.getBody());
            set(stack);
            eval(gNode, stack, 0, false);
        }
    }

    /**
     * values var { val }
     */
    boolean binding(Mapping map) {
        int i = 0;
        for (Node qNode : map.getQueryNodes()) {

            Node node = map.getNode(qNode);
            if (node != null) {
                Node value = producer.getNode(node.getValue());
                boolean suc = memory.push(qNode, value, -1);

                if (!suc) {
                    int j = 0;
                    for (Node qq : map.getQueryNodes()) {

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
                }
            }
            i++;
        }
        return true;
    }

    void free(Mapping map) {
        for (Node qNode : map.getQueryNodes()) {
            memory.pop(qNode);
        }
    }

    /**
     * Subquery processed by a function call that return Mappings Producer may
     * cast the result into Mappings use case: {select xpath(?x, '/book/title')
     * as ?val where {}} Mappings may be completed by filter (e.g. for casting)
     * Mappings will be processed later by aggregates and order by/limit etc.
     */
    private void function () {
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
        if (q.isDebug()){
            debug = true;
        }
        eval(gNode, stack, n, false);

        //memory.setResults(save);
        return results;
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
                Message.log(Message.FAIL_AT, maxExp);
            }
        }
    }

    /**
     *
     * Copy current evaluator to eval subquery same memory (share bindings) new
     * exp stack
     */
    Eval copy() {
        return copy(copyMemory(query), producer, evaluator);
    }

    public Eval copy(Memory m, Producer p, Evaluator e) {
        Eval ev = create(p, e, match);
        ev.setMemory(m);
        ev.set(provider);
        //ev.setSubEval(true);
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

    // copy memory for path
    private Memory copyMemory(Query query) {
        return copyMemory(memory, query, null);
    }

    /**
     * copy memory for sub query copy sub query select variables that are
     * already bound in current memory
     *
     */
    private Memory copyMemory(Query query, Query sub) {
        return copyMemory(memory, query, sub);
    }

    private Memory copyMemory(Memory memory, Query query, Query sub) {
        Memory mem = new Memory(match, evaluator);
        if (sub == null) {
            mem.init(query);
        } else {
            mem.init(sub);
        }
        memory.copyInto(sub, mem);
        if (hasEvent) {
            memory.setEventManager(manager);
        }
        return mem;
    }

    /**
     * copy of Memory may be stored in exp. Reuse data structure after cleaning
     * and init copy current memory content into target memory
     */
    public Memory getMemory(Exp exp) {
        return getMemory(memory, exp);
    }

    public Memory getMemory(Memory memory, Exp exp) {
        Memory mem;
        if (memory.isFake()) {
            // Temporary memory created by PathFinder
            mem = memory;
        } else if (exp.getObject() != null) {
            mem = (Memory) exp.getObject();
            mem.start();
            memory.copyInto(null, mem);
        } else {
            mem = copyMemory(memory, query, null);
            exp.setObject(mem);
        }
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
    private void init(Query q) {
        if (memory == null) {
            // when subquery, memory is already assigned
            // assign stack index to EDGE and NODE
            q.complete();
            memory = new Memory(match, evaluator);
            // create memory bind stack
            memory.init(q);
            if (hasEvent) {
                memory.setEventManager(manager);
            }
            producer.init(q.nbNodes(), q.nbEdges());
            debug = q.isDebug();
        }
        start(q);
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
        // save current results in case of sub query
        //save = memory.getResults();
        // set new results in case of sub query (for aggregates)
        memory.setEval(this);
        memory.setResults(results);
    }

    private void complete() {
        results.complete();
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
                        logger.warn("Bind: " + qqNode + " = " + node);
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
        PathFinder pathFinder = PathFinder.create(p, match, evaluator, query);
        //pathFinder.setDefaultBreadth(false);
        if (hasEvent) {
            pathFinder.set(manager);
        }
        pathFinder.set(listener);
        pathFinder.setList(query.isListPath());
        // TODO: subQuery 
        pathFinder.setCheckLoop(query.isCheckLoop());
        pathFinder.setCountPath(query.isCountPath());
        pathFinder.init(exp.getRegex(), exp.getObject(), exp.getMin(), exp.getMax());
        lPathFinder.add(pathFinder);
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
        store(p);       
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
    private int eval(Node gNode, Stack stack, int n, boolean option) {
        return eval(producer, gNode, stack, n, option);
    }

    /**
     * gNode is the query graph name if any, may be null
     */
    private int eval(Producer p, Node gNode, Stack stack, int n, boolean option) {
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
                    String s = Integer.toString(n);
                    if (n <= 9) {
                        s = "0" + s;
                    }
                    Message.log(Message.EVAL, s + " " + maxExp);
                }
            }
        }

        if (n > maxLevel) {
            maxLevel = n;
        }

        Exp exp = stack.get(n);

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

            switch (exp.type()) {

                case NEXT:
                    // we have reached the end of an exp, it's next status is success=true
                    exp.status(true);
                    stack.remove(n);
                    backtrack = eval(p, gNode, stack, n, option);
                    break;


                case EMPTY:

                    eval(p, gNode, stack, n + 1, option);
                    break;


                case AND:
                    stack = stack.and(exp, n);
                    backtrack = eval(p, gNode, stack, n, option);
                    break;

                case SERVICE:
                    //stack = stack.and(exp.rest(), n);
                    backtrack = service(p, gNode, exp, stack, n, option);
                    break;

                case GRAPH:

                    if (env.isPath(exp.getGraphName())) {
                        // graph $path { }
                        // switch Producer to path
                        backtrack = 
                                inGraph(p, memory.getPath(exp.getGraphName()),
                                     gNode, exp, stack, n, option);
                    } 
                    else { 
                        Node gg = getNode(exp.getGraphName());
                        if (gg != null && p.isProducer(gg)) {
                            // graph $path { }
                            // named graph in GraphStore 
                            // switch Producer 
                            backtrack = 
                                    inGraph(p, p.getProducer(gg),
                                         gNode, exp, stack, n, option);
                        } 
                        else {
                            backtrack = graph(gNode, exp, stack, n, option);
                        }
                    }
                    break;


                case RESTORE:
                    backtrack = eval(exp.getProducer(), gNode, stack, n + 1, option);
                    break;


                case GRAPHNODE:

                    backtrack = graphNode(gNode, exp, stack, n, option);
                    break;


                case UNION:

                    backtrack = union(p, gNode, exp, stack, n, option);
                    break;


                case OPTION: // compiled as: WATCH EXP CONTINUE
                // true means if reach CONT, WATCH must backtrack after
                // option succeed
               
                {
                    stack = stack.watch(exp.first(), WATCH, CONTINUE, true, n);
                    backtrack = eval(p, gNode, stack, n, true);
                }

                break;
                    
                    
                 case OPTIONAL:
				if (gNode!=null && ! env.isBound(gNode)){
					backtrack = graphNodes(gNode, gNode, exp, stack, n, n, option);
				}
				else {
					backtrack = optional(p, gNode, exp, stack, n, option);
				}
                    break;
    


                case MINUS:
                    if (gNode != null && !env.isBound(gNode)) {
                        // bind ?g before minus
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n, false);
                    } else {
                        backtrack = minus(p, gNode, exp, stack, n, true);
                    }
                    break;


                case JOIN:

                    backtrack = join(p, gNode, exp, stack, n, true);
                    break;


                case NOT:
                    /**
                     * compiled as: WATCH EXP BACKJUMP true means if reach
                     * BACKJUMP, WATCH must backtrack after negation fail
                     *
                     */
                    stack = stack.watch(exp.first(), WATCH, BACKJUMP, true, n);
                    backtrack = eval(p, gNode, stack, n, true);
                    break;


                case EXIST:

                    // compiled as: WATCH EXP BACKJUMP
                    // false means if reach BACK, WATCH must not backtrack after (must skip it)
                    // exist succeed
                    stack = stack.watch(exp.first(), WATCH, BACKJUMP, false, n);
                    backtrack = eval(p, gNode, stack, n, true);
                    break;


                case WATCH:
                    if (gNode != null && !env.isBound(gNode)) {
                        // bind graph ?g before watch
                        // use case: graph ?g {option {?x ?p ?y} !bound(?x) ?z ?q ?t}
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n, true);
                    } else {
                        backtrack = watch(p, gNode, exp, stack, n, option);
                    }
                    break;


                case CONTINUE:

                    // optional succeed, mark it and continue
                    exp.status(!exp.skip());
                    backtrack = eval(p, gNode, stack, n + 1, true);
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


                case FILTER:
                    if (gNode != null && !env.isBound(gNode)) {
                        // graph ?g { filter(?g != <uri>) }
                        // bind ?g before filter
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n, false);
                    } else {
                        backtrack = filter(p, gNode, exp, stack, n, option);
                    }
                    break;

                case PATH:
                    backtrack = path(p, gNode, exp, stack, n, option);
                    break;


                case EDGE:
                    backtrack = edge(p, gNode, exp, stack, n, option);
                    break;


                case NODE:
                    backtrack = node(gNode, exp, stack, n, option);
                    break;

                case QUERY:

                    if (gNode != null && !env.isBound(gNode)) {
                        // bind graph ?g before subquery
                        backtrack = graphNodes(gNode, gNode, exp, stack, n, n, true);
                    } else {
                        backtrack = query(p, gNode, exp, stack, n, option);
                    }
                    break;


                case VALUES:

                    backtrack = values(p, gNode, exp, stack, n, option);

                    break;


                case POP:

                    // pop BIND( f(?x) as ?y)
                    backtrack = pop(gNode, exp, stack, n, option);

                    break;

                /**
                 * ********************************
                 *
                 * Draft extensions
                 *
                 */
                case BIND:
                    /**
                     * use case: ?x p ?y FILTER ?t = ?y BIND(?t, ?y) ?z q ?t
                     *
                     */
                    backtrack = bind(p, gNode, exp, stack, n, option);
                    break;


                case EVAL:

                    // ?doc xpath('/book/title/text()') ?title
                    backtrack = eval(gNode, exp, stack, n, option);
                    break;


                case SCOPE:

                    backtrack = eval(p, gNode, stack.copy(exp.get(0), n), n, option);

                    break;


                case ACCEPT:
                    // use case: select distinct ?x where
                    // check that ?x is distinct
                    if (optim) {
                        if (results.accept(env.getNode(exp.getNode()))) {
                            // backjump here when a mapping will be found with this node
                            // see store()
                            backjump = n - 1;
                            backtrack = eval(p, gNode, stack, n + 1, option);
                        }
                    } else {
                        backtrack = eval(p, gNode, stack, n + 1, option);
                    }
                    break;


                case EXTERN:

                    // draft trace expression
                    plugin.exec(exp, env, n);
                    backtrack = eval(p, gNode, stack, n + 1, option);
                    break;



                case SCAN:
                    // scan a partial result (for trace/debug)

                    Mapping scan = env.store(query, p);
                    if (scan != null) {
                        logger.debug(scan);
                    }
                    backtrack = eval(p, gNode, stack, n + 1, option);
                    break;


               
                case FORALL:

                    Exp fa = Exp.create(NOT, Exp.create(AND, exp.first(), Exp.create(NOT, exp.rest())));
                    stack.set(n, fa);
                    backtrack = eval(p, gNode, stack, n, true);
                    break;


                case IF:
                    backtrack = ifthenelse(gNode, exp, stack, n, option);
                    break;

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
    private int bind(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        Memory env = memory;
        int backtrack = n - 1;

        if (exp.isBindCst()) {
             backtrack = cbind(p, gNode, exp, stack, n, option);
            return backtrack;
        }
        else {
            
            // ?x = ?y
            int i = 0, j = 1;
            Node node = env.getNode(exp.get(i).getNode());
            if (node == null) {
                i = 1;
                j = 0;
                node = env.getNode(exp.get(i).getNode());
                if (node == null) {
                    // no binding: continue
                    backtrack = eval(p, gNode, stack, n + 1, option);
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
                backtrack = eval(p, gNode, stack, n + 1, option);
                env.pop(qNode);
            } else {
                backtrack = eval(p, gNode, stack, n + 1, option);
            }

            return backtrack;
        }
    }

    /**
     * exp : BIND{?x = cst1 || ?x = cst2} Bind ?x with all its values
     */
    private int cbind(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Memory env = memory;
        Producer prod = producer;

        Node qNode = exp.get(0).getNode();
        if (!exp.status() || env.isBound(qNode)) {
            return eval(p, gNode, stack, n + 1, option);
        }

        if (exp.getNodeList() == null) {
            // Constant are not yet transformed into Node
            for (Object value : exp.getValues()) {
                // get constant Node
                Node node = prod.getNode(value);
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
                backtrack = eval(p, gNode, stack, n + 1, option);
                env.pop(qNode);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        } else {
            backtrack = eval(p, gNode, stack, n + 1, option);
        }
        return backtrack;
    }

    /**
     * Eval exp alone in a fresh new Memory Node gNode : actual graph node Node
     * node : exp graph node
     */
     private Mappings subEval(Producer p, Node gNode, Node node, Exp exp, Exp main) {
           return subEval(p, gNode, node, exp, main, null);
     }
    
    private Mappings subEval(Producer p, Node gNode, Node node, Exp exp, Exp main, Mapping m) {
        Memory mem = new Memory(match, evaluator);
        mem.init(query);
        Eval eval = copy(mem, p, evaluator);
        graphNode(gNode, node, mem);
        bind(mem, exp, main, m);
       if (main.type() == Exp.JOIN) {
            service(exp, mem);
        }
       Mappings lMap = eval.subEval(query, node, Stack.create(exp), 0);
        return lMap;
    }
    
    void bind(Memory mem, Exp exp, Exp main, Mapping m){
        if (m != null){
            mem.push(m, -1);
        }
        if (main.isOptional() && exp.getNodeList() != null){
            // A optional B
            // bind variables of A from environment
            for (Node qnode : exp.getNodeList()){
                Node node = memory.getNode(qnode);
                if (node != null){
                    mem.push(qnode, node, -1);
                }
            }
        }
    }

    /**
     * JOIN(service ?s {}, exp) if ?s is bound, bind it for subeval ...
     */
    void service(Exp exp, Memory mem) {
        if (exp.type() == Exp.SERVICE){
           bindService(exp, mem);
        }
        else for (Exp ee : exp.getExpList()) {
            
            switch (ee.type()){
                
                case Exp.SERVICE:
                    bindService(ee, mem);
                    break;
                    
                case Exp.AND:
                case Exp.JOIN:
                    service(ee, mem);
                    break;
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
            mem.push(node, memory.getNode(gNode));
        }
    }

    private int watch(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Exp skip = exp.first();
        skip.status(skip.skip()); //true);
        // eval the optional exp
        backtrack = eval(p, gNode, stack, n + 1, true);
        if (backtrack == STOP) {
            return backtrack;
        }
        // we are back, now test the checker to see
        // if the option fail/not succeed
        if (skip.status()) {
            //  option fail / not succeed: skip it and eval the rest of the stack
            backtrack = eval(p, gNode, stack, stack.indexOf(skip) + 1, true);
        }
        return backtrack;
    }

    /**
     * {?x c:name ?name} minus {?x c:name 'John'} TODO: optimize it, cache
     * results in exp (like subquery)
     */
    private int minus(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node qNode = query.getGraphNode();

        Node node1 = null, node2 = null;
        if (hasGraph) {
            node1 = qNode;
            node2 = exp.getGraphNode();
        }
        Mappings lMap1 = subEval(p, gNode, node1, exp.first(), exp);
        Mappings lMap2 = subEval(p, gNode, node2, exp.rest(), exp);

        for (Mapping map : lMap1) {
            boolean ok = true;
            for (Mapping minus : lMap2) {
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
                    backtrack = eval(p, gNode, stack, n + 1, option);
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
    private int join(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Memory env = memory;
        Mappings map1 = subEval(p, gNode, gNode, exp.first(), exp);
        
        if (map1.size() == 0){
            return backtrack;
        }

        exp.rest().setMappings(map1);
        Mappings map2 = subEval(p, gNode, gNode, exp.rest(), exp);

        if (map2.size() == 0){
            return backtrack;
        }

        Node qn1 = null, qn2 = null;
        
        for (int j = n + 1; j < stack.size(); j++) {
            // check if next exp is filter (?x = ?y)
            // where ?x in map1 and ?y in map2
            Exp e = stack.get(j);

            if (! e.isFilter()){
                break;
            }
            else if (e.size() == 1 && e.get(0).isBindVar()) {
                // b = BIND(?x, ?y)
                Exp b = e.get(0);
                qn1 = b.get(0).getNode();
                qn2 = b.get(1).getNode();
                
                // Do the mappings bind  variables ?x and ?y respectively ?
                if ((map1.get(0).getNode(qn1) != null && map2.get(0).getNode(qn2) != null)) {
                    // ok do nothing
                } 
                else if ((map1.get(0).getNode(qn2) != null && map2.get(0).getNode(qn1) != null)) {
                    // ok switch variables: qn1 for map1 etc.
                    Node tmp = qn2;
                    qn2 = qn1;
                    qn1 = tmp;
                } 
                else {
                    // Mappings do not bind variables
                    qn2 = null;
                }

                if (qn2 != null) {
                    // sort map2 Mappings according to value of ?y
                    // in order to perform dichotomy with ?x = ?y
                    // ?x in map1, ?y in map2
                    map2.sort(qn2);
                }
            }
        }
        
        if (qn1 != null && qn2 != null) {
            // exploit dichotomy for ?x = ?y
            for (Mapping m1 : map1) {

                Node n1 = m1.getNode(qn1);
                if (n1 != null) {

                    if (env.push(m1, n)) {

                        // indev of ?y in map2
                        int nn = map2.find(n1, qn2);

                        if (nn >= 0 && nn < map2.size()) {

                            for (int i = nn; i < map2.size(); i++) {
                                
                                // enumerate occurrences of ?y in map2
                                Mapping m2 = map2.get(i);
                                Node n2 = m2.getNode(qn2);
                                if (n2 == null || !n1.equals(n2)) {
                                    // as map2 is sorted, if ?x != ?y we can exit the loop
                                    break;
                                } else if (env.push(m2, n)) {
                                    backtrack = eval(p, gNode, stack, n + 1, option);
                                    env.pop(m2);
                                    if (backtrack < n) {
                                        return backtrack;
                                    }
                                }
                            }

                        }

                        env.pop(m1);
                    }
                }
            }
        }
        else {
            backtrack = join(p, gNode, stack, env, map1, map2, n, option);           
        }
        return backtrack;
    }
    
    int join(Producer p, Node gNode, Stack stack, Memory env, Mappings map1, Mappings map2, int n, boolean option){
        int backtrack = n-1;
         for (Mapping m1 : map1) {

                if (env.push(m1, n)) {

                    for (Mapping m2 : map2) {

                        if (env.push(m2, n)) {
                            backtrack = eval(p, gNode, stack, n + 1, option);
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
    
    
     private int join2(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Memory env = memory;
        Mappings map1 = subEval(p, gNode, gNode, exp.first(), exp);
        
        if (map1.size() == 0){
            return backtrack;
        }

        exp.rest().setMappings(map1);
        Mappings map2 = subEval(p, gNode, gNode, exp.rest(), exp);

        if (map2.size() == 0){
            return backtrack;
        }
       
        backtrack = join(p, gNode, stack, env, map1, map2, n, option);           

        return backtrack;
    }
    

    private int union(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Stack nstack = stack.copy(exp.first(), n);
        int b1 = eval(p, gNode, nstack, n, option);
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
        backtrack = eval(p, gNode, nstack, n, option);

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

    private int service2(Node gNode, Exp exp, Stack stack, int n, boolean option) {
        stack = stack.and(exp.rest(), n);
        int backtrack = eval(gNode, stack, n, option);
        return backtrack;
    }

    private Mappings service(Node serv, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        mem.init(query);
        Eval eval = copy(mem, producer, evaluator);
        Mappings lMap = eval.subEval(query, null, Stack.create(exp), 0);
        return lMap;
    }

    private int service(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {

        int backtrack = n - 1;
        Memory env = memory;
        Node serv = exp.first().getNode();
        Node node = serv;

        if (serv.isVariable()) {
            node = env.getNode(serv);
            if (node == null) {
                logger.error("Service variable unbound: " + serv);
                return backtrack;
            }
        }

        if (provider != null) {
            // service delegated to provider
            Mappings lMap = provider.service(node, exp.rest(), exp.getMappings(), env);
            for (Mapping map : lMap) {
                // push each Mapping in memory and continue
                complete(query, map);

                // draft test:
                //submit(map);

                // remove comment:
                if (env.push(map, n, false)) {
                    backtrack = eval(gNode, stack, n + 1, option);
                    env.pop(map, false);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        } else {
            Query q = exp.rest().getQuery();
            return query(p, gNode, q, stack, n, option);
        }

        return backtrack;
    }

    void complete(Query q, Mapping map) {
        int i = 0;
        for (Node node : map.getQueryNodes()) {
            Node out = q.getOuterNode(node);
            if (out != null) {
                map.getQueryNodes()[i] = out;
            }
            i++;
        }
    }

    /**
     * exp is graph ?g { PAT } set GRAPHNODE(?g) before and GRAPHNODE(?gNode)
     * after
     */
    private int graph(Node gNode, Exp exp, Stack stack, int n, boolean option) {
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
        backtrack = eval(gNode, stack, n, true);
        return backtrack;
    }

    /**
     * exp is GRAPHNODE(?g) set current gNode as ?g
     */
    private int graphNode(Node gNode, Exp exp, Stack stack, int n, boolean option) {
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
                return graphNodes(gNode, nextGraph, exp, stack, n, n + 1, option);
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
            backtrack = eval(nextGraph, stack, n + 1, true);
        } else if (query.getFrom(nextGraph).size() > 0) {
            // from named graph ?g {}
            // enumerate target named graphs
            backtrack = graphNodes(nextGraph, nextGraph, exp, stack, n, n + 1, option);
        } else {
            // graph ?g {}
            // variable ?g bound by target pattern
            backtrack = eval(nextGraph, stack, n + 1, true);
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
    private int graphNodes(Node gNode, Node nextGraph, Exp exp, Stack stack, int n, int next, boolean option) {
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
                backtrack = eval(nextGraph, stack, next, option);
                env.pop(gNode);
                if (backtrack < next - 1) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }

    Node getNode(Node gNode){
       Node gg = memory.getNode(gNode);
       if (gg != null){
           return gg;
       }
       if (gNode.isConstant()){
            return gNode;
       }
       return null;
    }
    
    
    /**
     * graph $path { } switch producer p to np = producer($path) add RESTORE(p) after this graph
     * exp
     */
 
    private int inGraph(Producer p, Producer np, Node gNode, Exp exp, Stack stack, int n, boolean option) {

        int backtrack = n - 1;

        stack.set(n, exp.rest());

        if (n == stack.size() - 1) {
            // last statement: switch Producer to Path			
            backtrack = eval(np, gNode, stack, n, option);
            stack.reset(n, exp);
        } else {
            Exp next = getRestore(p, exp);
            stack.add(n + 1, next);
            backtrack = eval(np, gNode, stack, n, option);
            for (int i = n + 1; stack.get(i) != next;) {
                stack.remove(i);
            }
            stack.remove(n + 1);
            stack.set(n, exp);
        }

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
     * Special case: optional{} !bound(?x) When filter fail, backjump before
     * optional
     *
     */
    private int filter(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Memory env = memory;

        env.setGraphNode(gNode);
        //memory.setStack(stack);
        //exp.getFilter().getExp().isExist();
        boolean success = evaluator.test(exp.getFilter(), env, p);
        env.setGraphNode(null);
        //memory.setStack(null);

        if (hasEvent) {
            send(Event.FILTER, exp, success);
        }

        if (success) {
            backtrack = eval(p, gNode, stack, n + 1, option);
        } else if (exp.status()) {
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

    private int path(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
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

        for (Mapping map : path.candidate(gNode, qq.getFrom(gNode), env)) {
            boolean success = match(map) && env.push(map, n);

            if (isEvent) {
                send(evENUM, exp, map, success);
            }

            if (success) {
                isSuccess = true;
                backtrack = eval(p, gNode, stack, n + 1, option);
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
            int bj = env.getIndex(gNode, exp.getEdge());
            backtrack = bj;
        }
        path.stop();
        return backtrack;
    }
   

    private int values(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;

        for (Mapping map : exp.getMappings()) {

            //System.out.println("** E: " + map);
            if (memory.push(map, n)) {
                backtrack = eval(p, gNode, stack, n + 1, option);
                memory.pop(map);

                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;

    }

    /**
     * Enumerate candidate edges
     *
     */
    private int edge(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
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

        Iterable<Entity> entities = p.getEdges(gNode, qq.getFrom(gNode), qEdge, env);
        Iterator<Entity> it = entities.iterator();

        while (it.hasNext()) {
//		for (Entity map : p.getEdges(gNode, qq.getFrom(gNode), qEdge,  env)){			
            Entity ent = it.next();

            if (ent != null) {
                nbEdge++;
                boolean trace = false;
                Edge edge = ent.getEdge();
                graph = ent.getGraph();

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
                    bmatch = push(qEdge, ent, gNode, graph, n);
                }

                if (isEvent) {
                    send(evENUM, exp, edge, bmatch);
                }

                if (bmatch) {
                    isSuccess = true;
                    backtrack = eval(p, gNode, stack, n + 1, option);

                    env.pop(qEdge, ent);
                    if (hasGraphNode) {
                        env.pop(gNode);
                    }

                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }


        //edgeToDiffer = null;

        if (!isSuccess && optim) {
            // backjump to max index where nodes are bound for first time:
            // (2) x r t
            // (1) y q z
            // (0) x p y
            // (2) may backjump to (0) because they share x
            // in addition we may require to change the value of x by
            // setting edgeToDiffer to x r t
            int bj = env.getIndex(gNode, qEdge);
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

    private int node(Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        // enumerate candidate nodes
        Node qNode = exp.getNode();
        Memory env = memory;

        //if (qNode == null) break;

        if (exp.hasArg()) {
            // target nodes to bind to this qnode
            for (Exp ee : exp) {
                Node node = ee.getNode();
                if (match.match(qNode, node, env) && env.push(qNode, node, n)) {
                    backtrack = eval(gNode, stack, n + 1, option);
                    env.pop(qNode);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        } else {
            for (Entity entity : producer.getNodes(gNode, query.getFrom(gNode), qNode, env)) {

                if (entity != null) {
                    Node node = entity.getNode();
                    Node graph = entity.getGraph();

                    if (match(qNode, node, gNode, graph) && push(qNode, node, gNode, graph, n)) {

                        backtrack = eval(gNode, stack, n + 1, option);

                        if (gNode != null) {
                            env.pop(gNode);
                        }
                        env.pop(qNode);

                        if (backtrack < n) {
                            return backtrack;
                        }
                    }
                }

            }
        }


        return backtrack;
    }

    /**
     * select * where {{select distinct ?y where {?x p ?y}} . ?y q ?z} new eval,
     * new memory, share only sub query select variables
     *
     */
    private int query(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isEvent = hasEvent;
        Query subQuery = exp.getQuery();
        Memory env = memory;
        Mappings lMap;

        if (exp.getObject() != null && !isBound(subQuery, env) && gNode == null) {
            // result is cached, can reuse it
            lMap = (Mappings) exp.getObject();
        } else {
            // copy current Eval,  new stack
            // bind sub query select nodes in new memory
            Eval ev = copy(copyMemory(query, subQuery), p, evaluator);

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

            if (!isBound(subQuery, env) && gNode == null) {
                exp.setObject(lMap);
            }
        }

        // enumerate the result of the sub query
        // bind the select nodes into the stack
        for (Mapping map : lMap) {
            boolean bmatch = push(subQuery, map, n);

            if (isEvent) {
                send(evENUM, exp, map, bmatch);
            }

            if (bmatch) {
                backtrack = eval(gNode, stack, n + 1, option);
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
    private int pop(Node gNode, Exp exp, Stack stack, int n, boolean option) {
        for (Exp ee : exp.first().getQuery().getSelectFun()) {
            Node node = ee.getNode();
            memory.pop(node);
            break;
        }
        return eval(gNode, stack, n + 1, option);
    }

    /**
     * Edge as Function use case: ?x xpath('/book/title') ?y
     */
    private int eval(Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Edge qEdge = exp.getEdge();
        Node qNode = qEdge.getNode(1);
        Memory env = memory;
        Evaluator ev = evaluator;
        Matcher mm = match;

        for (Node node : ev.evalList(exp.getFilter(), env)) {

            if (mm.match(qNode, node, env) && env.push(qNode, node)) {

                backtrack = eval(gNode, stack, n + 1, option);
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
    private int optional(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
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
            
            Mappings map2 = subEval(p, gNode, node1, exp.rest(), exp, r1);

            for (Mapping r2 : map2) {

                success = true;
    
                if (env.push(r2, n)) {
                    if (hasGraph) {
                        env.pop(qNode);
                    }
                    backtrack = eval(p, gNode, stack, n + 1, true);
                    env.pop(r2);
                    if (backtrack < n){
                        return backtrack;
                    }
                }                               
            }

            if (! success) {
                // all r2 fail
                if (env.push(r1, n)) {
                    if (hasGraph) {
                        env.pop(qNode);
                    }
                    backtrack = eval(p, gNode, stack, n + 1, true);
                    env.pop(r1);
                    if (backtrack < n){
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }

    
    
    private int optional2(Producer p, Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Node qNode = query.getGraphNode();
        Memory env = memory;

        Node node1 = null;
        if (hasGraph) {
            node1 = qNode;
        }

        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp);
        Mappings map2 = subEval(p, gNode, node1, exp.rest(), exp);
               
        for (Mapping r1 : map1) {

            boolean success = false;

            for (Mapping r2 : map2) {

                if (r1.compatible(r2, true)) {
                    success = true;
    
                    if (env.push(r1, n)) {
                       if (hasGraph) {
                            env.pop(qNode);
                        }
                        if (env.push(r2, n)) {
                            if (hasGraph) {
                                env.pop(qNode);
                            }
                            eval(p, gNode, stack, n + 1, true);
                            env.pop(r2);
                        }
                        env.pop(r1);
                    }
                }
            }

            if (! success) {
                // all r2 fail
                if (env.push(r1, n)) {
                    if (hasGraph) {
                        env.pop(qNode);
                    }
                    eval(p, gNode, stack, n + 1, true);
                    env.pop(r1);
                }
            }
        }


        return backtrack;
    }

    private int ifthenelse(Node gNode, Exp exp, Stack stack, int n, boolean option) {
        int backtrack = n - 1;
        Eval eva = copy();
        Mappings lRes = eva.subEval(query, gNode, Stack.create(exp.first()), 0);

        if (lRes.size() > 0) {
            for (Mapping rr : lRes) {
                if (memory.push(rr, n)) {
                    backtrack = eval(gNode, stack.copy(exp.rest(), n), n, true);
                    memory.pop(rr);
                    if (backtrack < n - 1) {
//						if (hasEvent){
//							send(Event.FINISH, exp, gNode, stack);
//						}
                        return backtrack;
                    }
                }
            }
        } else if (exp.size() > 2) {
            backtrack = eval(gNode, stack.copy(exp.last(), n), n, true);
        }
        return backtrack;
    }

    /**
     * In case of backjump Some node must differ between previous and current A
     * node that is common to qEdge and qPast
     */
    private boolean differ(Exp qEdge, Exp qPast, Edge previous, Edge current) {
        for (int i = 0; i < qEdge.nbNode(); i++) {
            Node qNode = qEdge.getNode(i);
            if (qNode != null && qPast.contains(qNode)) {
                // They share a node, do they differ ?
                if (!previous.getNode(i).same(current.getNode(i))) {
                    return true;
                }
            }
        }
        return false;
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
                outNode = qq.getOuterNode(subNode);
                exp.add(outNode);
            } else {
                outNode = exp.get(0).getNode();
            }
            if (node != null) {
                // a value may be null because of an option {}
                if (!(mm.match(outNode, node, env) && env.push(outNode, node, n))) {
                    for (int i = 0; i < k; i++) {
                        subNode = subQuery.getSelect().get(i);
                        outNode = qq.getOuterNode(subNode);
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
                Node outNode = qq.getOuterNode(subNode);
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
            // get outer node:
            Node outNode = qq.getOuterNode(subNode);
            if (outNode != null && env.isBound(outNode)) {
                return true;
            }
        }
        return false;
    }

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
    private void store(Producer p) {
        boolean store = true;
        if (listener != null) {
            store = listener.process(memory);
        }
        if (store) {
            Mapping ans = memory.store(query, p, isSubEval);
            submit(ans);
            if (hasEvent) {
                //send(Event.RESULT, query, ans);
                send(Event.RESULT, ans);
            }
        }
    }

    void submit(Mapping map) {
        results.submit(map);
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
        if (gNode == null) {
            return true;
        }
        return match.match(gNode, graphNode, memory);
    }

    private boolean push(Edge qEdge, Entity ent, Node gNode, Node node, int n) {
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
}
