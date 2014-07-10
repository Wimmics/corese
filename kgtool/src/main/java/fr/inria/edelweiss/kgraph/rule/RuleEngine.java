package fr.inria.edelweiss.kgraph.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Closure;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Forward Rule Engine Use construct {} where {} SPARQL Query as Rule
 *
 * TODO: This engine creates target blank nodes for rule blank nodes hence it
 * may loop:
 *
 * construct {?x ex:rel _:b2} where {?x ex:rel ?y}
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 */
public class RuleEngine implements Engine {
    public static final int STD = 0;
    public static final int OWL_RL = 1;
    public static final int OWL_RL_FULL = 2;
    
    
    private static final String UNKNOWN = "unknown";
    private static Logger logger = Logger.getLogger(RuleEngine.class);
    Graph graph;
    QueryProcess exec;
    List<Rule> rules;
    private Dataset ds;
    private PTable ptable;
    RTable rtable;
    STable stable;
    // check that kgram solutions contain a newly entailed edge
    ResultWatcher rw;
    // kgram ResultListener create edges instead of create Mappings
    // LIMITATION: do not use if rule creates Node because graph would be 
    // modified during query execution
    private boolean isConstructResult = false;
    // run rules for wich new edges were created at loop n-1
    // check that rule solutions contains one edge from loop n-1
    // LIMITATION: do not use if Corese RDFS entailment is set to true
    // because we test predicate equality (we do not check rdfs:subPropertyOf)
    private boolean isOptimize = false;
    boolean debug = false,
            trace = false;
    private boolean test = false;
    // RETE like, is not efficient
    private boolean isOptimization = false;
    int loop = 0;
    private boolean isActivate = true;
    int profile = STD;
    private boolean isOptTransitive = true;
    private boolean isFunTransitive = false;
    private boolean isConnect = false;
    private boolean isDuplicate = false;
    private boolean isSkipPath = false;

    RuleEngine() {
        rules = new ArrayList<Rule>();
    }

    void set(Graph g) {
        graph = g;
    }

    public void set(QueryProcess p) {
        exec = p;
        p.setListPath(true);
    }
    
    public void setProfile(int n){
        profile = n;
        
        switch (n){
            case OWL_RL_FULL :
                try {
                    owlrlFull();
                } catch (IOException ex) {
                    logger.error(ex);
                } catch (EngineException ex) {
                    logger.error(ex);
                }
                
            // continue  
                
            case OWL_RL:
                optimizeOWLRL();
                
            default:
                
                
        }
    }
    
    void optimizeOWLRL(){
          setSpeedUp(true);
          // In OWL RL, path exp does not prevent optimize ResultWatcher
          // because path exp process OWL structural predicates
          // mainly rdf:rest*/rdf:first
          setSkipPath(true);
          getQueryProcess().setListPath(true);
    }
    
    
    /**
     * Replace duplicate OWL expressions by one of them
     *
     */
    public void owlrlFull() throws IOException, EngineException{
        QueryLoad ql = QueryLoad.create();
        // replace different bnodes that represent same OWL expression
        // by same bnode
        String unify = ql.getResource("/query/unify2.rq");
        // remove triples with obsolete bnodes as subject
        String clean = ql.getResource("/query/clean.rq");
        // tell Transformer to cache st:hash transformation result
        exec.getEvaluator().setMode(Evaluator.CACHE_MODE);
        // replace duplicate OWL expressions by one of them
        Mappings m1 = exec.query(unify);
        if (trace){
            System.out.println("unify: " + m1.size());
        }
        Mappings m2 = exec.query(clean);
         if (trace){
            System.out.println("clean: " + m2.size());
        }
        exec.getEvaluator().setMode(Evaluator.NO_CACHE_MODE);
    }
    
    public int getProfile(){
        return profile;
    }

    public QueryProcess getQueryProcess() {
        return exec;
    }

    public void set(Sorter s) {
        if (exec != null) {
            exec.set(s);
        }
    }

    public void setOptimize(boolean b) {
        isOptimize = b;
    }
    
     public void setSpeedUp(boolean b) {
        setOptimize(b);
        setConstructResult(b);
        setFunTransitive(b);
    }

    public void setTrace(boolean b) {
        trace = b;
    }

    public static RuleEngine create(Graph g) {
        RuleEngine eng = new RuleEngine();
        eng.set(g);
        eng.set(QueryProcess.create(g));
        return eng;
    }

    public static RuleEngine create(QueryProcess q) {
        RuleEngine eng = new RuleEngine();
        eng.set(q);
        return eng;
    }

    public static RuleEngine create(Graph g, QueryProcess q) {
        RuleEngine eng = new RuleEngine();
        eng.set(g);
        eng.set(q);
        return eng;
    }

    public boolean process() {
        if (graph == null) {
            set(Graph.create());
        }
        //OC:
        //synEntail();
        int size = graph.size();
        entail();
        return graph.size() > size;
    }

    public int process(Graph g) {
        set(g);
        if (exec == null) {
            set(QueryProcess.create(g));
        }
        return entail();
    }

    public int process(Graph g, QueryProcess q) {
        set(g);
        set(q);
        return entail();
    }

    public int process(QueryProcess q) {
        if (graph == null) {
            set(Graph.create());
        }
        set(q);
        return entail();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public void clear() {
        rules.clear();
    }
    
    public boolean isEmpty(){
        return rules.isEmpty();
    }
    
    public String getConstraintViolation(){
        try {
            String q = QueryLoad.create().getResource("/query/constraint.rq");
            QueryProcess ex = QueryProcess.create(graph);
            Mappings map = ex.query(q);
            return map.getTemplateStringResult();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RuleEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(RuleEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Define a construct {} where {} rule
     */
    public Query defRule(String rule) throws EngineException {
        return defRule(UNKNOWN, rule);
    }

    public void defRule(Query rule) {
        rules.add(Rule.create(UNKNOWN, rule));
    }

    public void addRule(String rule) {
        try {
            defRule(rule);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ResultWatcher getResultListener() {
        return rw;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Query defRule(String name, String rule) throws EngineException {
        Query qq = exec.compileRule(rule, ds);

        if (!qq.isConstruct()) {
            // template
            qq.setRule(false);
            ASTQuery ast = (ASTQuery) qq.getAST();
            ast.setRule(false);
        }

        if (qq != null) { // && qq.isConstruct()) {
            rules.add(Rule.create(name, qq));
            return qq;
        }
        return null;
    }

    int synEntail() {
        try {
            graph.writeLock().lock();
            return entail();
        } finally {
            graph.writeLock().unlock();
        }
    }

    
    
    
    /**
     * Process rule base at saturation PRAGMA: not synchronized on write lock
     */
    public int entail() {
        int start = graph.size();
        try {
            infer();
            if (trace){
                traceSize();
            }
            return graph.size() - start;
        }
        catch (OutOfMemoryError e){
            logger.error("Out of Memory: Rule Engine");
            return graph.size() - start;
        }
        finally {
            cleanRules();
        }
    }
    
    void traceSize() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("size: " + heapSize / 1000000);

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        // Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("max size: " + heapMaxSize / 1000000);

        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("free size: " + heapFreeSize / 1000000);
    }
     
    void infer(){

        if (isOptimize || isOptimization) {
            // consider only rules that match newly entailed edge predicates
            // consider solutions that contain at leat one newly entailed edge
            start();
            if (loop != 0) {
                // start a new rule processing
                clean();
            }
        }
        if (isOptimization) {
            // apply pertinent rules on newly entailed edges using kgram edge binding
            // not so efficient
            start2();
        }

        int size = graph.size(),
                start = size;
        loop = 0;
        int skip = 0, nbrule = 0, loopIndex = 0, tskip = 0, trun = 0, tnbres = 0;
        boolean go = true;

        // Entailment 
        graph.init();

        List<Entity> list = null, current;

        ITable t = null;
        stable = new STable();

        if (isOptimize) {
            // kgram return solutions that contain newly entailed edge
            rw = new ResultWatcher(graph);
            rw.setSkipPath(isSkipPath);
            if (isConstructResult){
                // Construct will take care of duplicates
                rw.setDistinct(false);
            }
            exec.addResultListener(rw);
        }

        while (go) {
            skip = 0;
            nbrule = 0;
            tnbres = 0;
            if (trace) {
                System.out.println("Loop: " + loop);
            }

            // List of edges created by rules in this loop 
            current = list;
            list = new ArrayList<Entity>();


            if (isOptimize){
                rw.start(loop);
            }
            for (Rule rule : rules) {

                if (debug) {
                    rule.getQuery().setDebug(true);
                }

                int nbres = 0;

                if (isOptimize) {
                    // start exec ResultWatcher, it checks that each solution 
                    // of rule contains at least one new edge 
                    rw.start(rule);

                    if (loop == 0) {
                        // run all rules, add all solutions
                        t = record(rule, loopIndex);
                        nbres = process(rule, null, list, loop, loopIndex, -1, nbrule);
                        if (rule.isClosure()){
                            // rule run at saturation: record nb edge after saturation
                            t = record(rule, loopIndex);
                        }
                        setRecord(rule, t);
                        tnbres += nbres;
                        nbrule++;
                        loopIndex++;
                    } else {
                        // run rules for which new edges have been created
                        // since previous run
                        // boolean run = true;
                        int save = graph.size();
                        t = record(rule, loopIndex);
                        ITable ot = getRecord(rule);
                        rw.setLoop(ot.getIndex());
                        if (accept(rule, ot, t)) {
                            nbres = process(rule, null, list, loop, loopIndex, ot.getIndex(), nbrule);
                            if (rule.isClosure()){
                                t = record(rule, loopIndex);
                            }
                            setRecord(rule, t);
                            tnbres += nbres;
                            nbrule++;
                            loopIndex++;
                        } else {
                            skip++;
                        }

                    }

                    rw.finish(rule);
                } else {
                    nbres = process(rule, null, list, loop, -1, -1, nbrule);
                    nbrule++;
                }

                if (trace) {
                    stable.record(rule, nbres);
                }
            }



            if (trace) {
                System.out.println("NBrule: " + nbrule);
//                System.out.println("nbres: "  + tnbres);   
                System.out.println("Graph: " + graph.size());
            }

            if (debug) {
                System.out.println("Skip: " + skip);
                System.out.println("Run: " + nbrule);
                System.out.println("Graph: " + graph.size());
                tskip += skip;
                trun += nbrule;
            }



            if (graph.size() > size) {
                // There are new edges: entailment again
                size = graph.size();
                loop++;


            } else {
                go = false;
            }
            
        }
        
        
        if (debug) {
            System.out.println("Total Skip: " + tskip);
            System.out.println("Total Run: " + trun);
        }
        if (debug) {
            logger.debug("** Rule: " + (graph.size() - start));
        }       
        
    }
    
    void cleanRules(){
        for (Rule r : rules){
            r.clean();
        }
    }

    public void trace() {
        for (Rule r : stable.sort()) {
            System.out.println(stable.get(r) + " " + r.getQuery().getAST());
        }
    }

    /**
     * Clean index of edges that are stored when isOptim=true
     */
    public void clean() {
        for (Entity ent : graph.getEdges()) {
            ent.getEdge().setIndex(-1);
        }
    }

    /**
     * Process one rule Store created edges into list
     */
    int process(Rule rule, List<Entity> current, List<Entity> list, int loop, int loopIndex, int prevIndex, int nbr) {
        if (trace){
           System.out.println(loop + " : " + nbr + "\n" + rule.getAST());
        }
        Date d1 = new Date();
        boolean isConstruct = isOptimize && isConstructResult;

        Query qq = rule.getQuery();
        Construct cons = Construct.create(qq, Entailment.RULE);
        cons.setRule(rule, rule.getIndex());
        cons.setGraph(graph);
        cons.setLoopIndex(loopIndex);
        cons.setDebug(debug);
        cons.setTest(test);

        List<Entity> le = null;

        if (isConstruct) {
            // kgram Result Listener create edges in list
            // after query completes, edges are inserted in graoh
            // no Mappings are created by kgram
            le = new ArrayList<Entity>();
            cons.setBuffer(true);
            cons.setInsertList(le);
            cons.setDefaultGraph(graph.addGraph(Entailment.RULE));
            Mappings map = Mappings.create(qq);
            rw.setConstruct(cons);
            rw.setMappings(map);
        }

        int start = graph.size();
        
        if (isFunTransitive() && rule.isTransitive() ){
            // Java code emulate a transitive rule
            Closure clos = getClosure(rule);
            clos.setConnect(isConnect());
            if (loop == 0){
                clos.init(rule.getTransitivePredicate());
            }
            clos.closure(loop, loopIndex, prevIndex);
            rule.setClosure(true);
        }
        else {
            process(rule, cons);

            if (graph.size() > start && isConstruct) {

                if (isOptTransitive() && rule.isAnyTransitive()){ 
                    // optimization for transitive rules: eval at saturation
                    
                    rule.setClosure(true);

                    boolean go = true;

                    while (go) {
                        // if this rule is transitive, it is executed at saturation in a loop.
                        // for loops after first one, kgram take new edge list into account
                        // for evaluating the where part, the first query edge matches new edges only

                        // consider list of edges created at preceeding loop:
                        qq.setEdgeList(cons.getInsertList());
                        qq.setEdgeIndex(rule.getEdgeIndex());
                        le = new ArrayList<Entity>();
                        cons.setInsertList(le);

                        int size = graph.size();

                        process(rule, cons);

                        if (graph.size() == size) {
                            qq.setEdgeList(null);
                            go = false;
                        }
                    }
                }
            }
        }

        Date d2 = new Date();
        if (trace){
            double tt = (d2.getTime() - d1.getTime()) / ( 1000.0) ;
            if (tt > 1){
                System.out.println("Time : " + tt);
                //System.out.println(rule.getAST());
                rule.setTime(tt + rule.getTime());
            }
            System.out.println("New: " + (graph.size() - start));
            System.out.println("Size: " + graph.size());

        }

        return graph.size() - start;
    }
    
    Closure getClosure(Rule r){
       if (r.getClosure() == null){
          Closure c = new Closure(graph, rw.getDistinct());
          c.setTrace(trace);
          r.setClosure(c);
       }
       return r.getClosure();
    }
   
    
    // process rule
    void process(Rule r, Construct cons) {
        Query qq = r.getQuery();      
        Mappings map = exec.query(qq, null);

        if (cons.isBuffer()) {
            // cons insert list contains only new edge that do not exist
            graph.addOpt(r.getUniquePredicate(), cons.getInsertList());
        } else {
            // create edges from Mappings as usual
            cons.insert(map, graph, null);
        }
    }
    
    
    
    /**
     * **************************************************
     *
     * Compute rule predicates Accept rule if some predicate has new triple in
     * graph
     *
     * *************************************************
     */
    /**
     * Compute table of rule predicates, for all rules
     */
    void start() {
        rtable = new RTable();

        for (Rule rule : rules) {
            init(rule);
        }
        
        //graph.indexResources();
    }

    /**
     * Generate a table : predicate -> List of Rule
     */
    void start2() {
        ptable = new PTable();
        String top = null;
        for (Rule rule : rules) {
            for (Node pred : rule.getPredicates()) {
                ptable.add(pred.getLabel(), rule);
                if (pred.getLabel().equals(Graph.TOPREL)) {
                    top = pred.getLabel();
                }
            }
        }

        if (top != null) {
            ptable.setTop(top);
            List<Rule> l = ptable.get(top);
            for (String p : ptable.keySet()) {
                if (!p.equals(top)) {
                    ptable.add(p, l);
                }
            }
        }
    }

    /**
     * Store list of predicates of this rule
     */
    void init(Rule rule) {
        rule.set(rule.getQuery().getNodeList());
        for (Node p : rule.getPredicates()) {
            if (p.equals(Graph.TOPREL)) {
                rule.setGeneric(true);
            }
        }
    }

    /**
     * @return the isOptimization
     */
    public boolean isOptimization() {
        return isOptimization;
    }

    /**
     * @param isOptimization the isOptimization to set
     */
    public void setOptimization(boolean isOptimization) {
        this.isOptimization = isOptimization;
    }

    /**
     * @return the isConstructResult
     */
    public boolean isConstructResult() {
        return isConstructResult;
    }

    /**
     * @param isConstructResult the isConstructResult to set
     */
    public void setConstructResult(boolean isConstructResult) {
        this.isConstructResult = isConstructResult;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return ds;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.ds = dataset;
    }

    /**
     * @return the isFunTransitive
     */
    public boolean isFunTransitive() {
        return isFunTransitive;
    }

    /**
     * @param isFunTransitive the isFunTransitive to set
     */
    public void setFunTransitive(boolean isFunTransitive) {
        this.isFunTransitive = isFunTransitive;
    }

    /**
     * @return the isConnect
     */
    public boolean isConnect() {
        return isConnect;
    }

    /**
     * @param isConnect the isConnect to set
     */
    public void setConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    /**
     * @return the isDuplicate
     */
    public boolean isDuplicate() {
        return isDuplicate;
    }

    /**
     * @param isDuplicate the isDuplicate to set
     */
    public void setDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    /**
     * @return the isSkipPath
     */
    public boolean isSkipPath() {
        return isSkipPath;
    }

    /**
     * @param isSkipPath the isSkipPath to set
     */
    public void setSkipPath(boolean isSkipPath) {
        this.isSkipPath = isSkipPath;
    }

    /**
     * @return the isOptTransitive
     */
    public boolean isOptTransitive() {
        return isOptTransitive;
    }

    /**
     * @param isOptTransitive the isOptTransitive to set
     */
    public void setOptTransitive(boolean isOptTransitive) {
        this.isOptTransitive = isOptTransitive;
    }

    /**
     * @return the test
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    class PTable extends HashMap<String, List<Rule>> {

        String top;
        List<Rule> empty = new ArrayList<Rule>();

        void setTop(String n) {
            top = n;
        }

        void add(String label, Rule r) {
            List<Rule> l = get(label);
            if (l == null) {
                l = new ArrayList<Rule>();
                put(label, l);
            }
            l.add(r);
        }

        void add(String label, List<Rule> lr) {
            List<Rule> lp = get(label);
            for (Rule r : lr) {
                if (!lp.contains(r)) {
                    lp.add(r);
                }
            }
        }

        List<Rule> getRules(String label) {
            List<Rule> l = get(label);
            if (l == null) {
                if (top != null) {
                    return get(top);
                } else {
                    return empty;
                }
            } else {
                return l;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String p : keySet()) {
                sb.append(p);
                sb.append("\n");
                for (Rule r : getRules(p)) {
                    sb.append(r.getAST());
                    sb.append("\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    class ETable extends HashMap<Node, List<Entity>> {

        void add(Entity ent) {
            Node p = ent.getEdge().getEdgeNode();
            List<Entity> l = get(p);
            if (l == null) {
                l = new ArrayList<Entity>();
                put(p, l);
            }
            l.add(ent);
        }
    }

    class ITable extends Hashtable<String, Integer> {

        private int index;

        ITable(int n) {
            index = n;
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
    }

    class RTable extends Hashtable<Rule, ITable> {
    }

    class STable extends Hashtable<Rule, Integer> {

        void record(Rule r, int n) {
            Integer i = get(r);
            if (i == null) {
                i = 0;
            }
            put(r, i + n);
        }

        List<Rule> sort() {
            ArrayList<Rule> list = new ArrayList<Rule>();

            for (Rule r : keySet()) {
                list.add(r);
            }

            Collections.sort(list,
                    new Comparator<Rule>() {
                @Override
                public int compare(Rule o1, Rule o2) {
                    return get(o2).compareTo(get(o1));
                }
            });

            return list;
        }
    }

    /**
     * Record predicates cardinality in graph
     */
    ITable record(Rule r, int n) {
        ITable itable = new ITable(n);

        for (Node pred : r.getPredicates()) {
            int size = graph.size(pred);
            itable.put(pred.getLabel(), size);
        }

        return itable;
    }

    /**
     * Rule is selected if one of its predicate has a new triple in graph
     */
    boolean accept(Rule rule, ITable told, ITable tnew) {
        for (Node pred : rule.getPredicates()) {
            String name = pred.getLabel();
            if (tnew.get(name) > told.get(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return previous record of rule predicate cardinality
     */
    ITable getRecord(Rule r) {
        return rtable.get(r);
    }

    void setRecord(Rule r, ITable t) {
        rtable.put(r, t);
    }
    
    public void init() {
    }

    public void onDelete() {
    }

    public void onInsert(Node gNode, Edge edge) {
    }

    public void onClear() {
    }

    public void setActivate(boolean b) {
        isActivate = b;
    }

    public boolean isActivate() {
        return isActivate;
    }

    public void remove() {
        graph.clear(Entailment.RULE, true);
    }

    public int type() {
        return RULE_ENGINE;
    }
}
