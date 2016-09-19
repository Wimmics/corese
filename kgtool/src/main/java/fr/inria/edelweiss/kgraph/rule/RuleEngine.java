package fr.inria.edelweiss.kgraph.rule;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.printer.SPIN;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Closure;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.GraphManager;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

/**
 * Forward Rule Engine 
 * Use construct {} where {} SPARQL Query as Rule
 * Optimizations:
 * Do not create Mappings, create triples directly
 * Consider rules for which new relevant triples are available
 * Consider solutions with new triple
 * Focus on new triples using specific Graph Index sorted by timestamp
 * Eval transitive rule at saturation using specific Java code
 * Eval pseudo transitive rule just after it's transitive rule 
 * (cf rdf:type & rdfs:subClassOf)
 * 
 * OWL_RL profile load specific rule base
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 * Wimmics INRIA I3S, 2014
 */
public class RuleEngine implements Engine, Graphable {
    static final String NL = System.getProperty("line.separator");
    public static final int OWL_RL_FULL = -1;
    public static final int STD = 0;
    public static final int OWL_RL = 1;
    public static final int OWL_RL_LITE = 2;
    
    public static final String OWL_RL_PATH      = "/rule/owlrl.rul";
    public static final String OWL_RL_LITE_PATH = "/rule/owlrllite.rul";
    
    private static final String UNKNOWN = "unknown";
    private static Logger logger = LogManager.getLogger(RuleEngine.class);
    Graph graph;
    QueryProcess exec;
    private QueryEngine qengine;
    List<Rule> rules;
    List<Record> records;
    HashMap<Integer, String> path;
    private Object spinGraph;
    private Dataset ds;
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
    int loop = 0;
    int profile = STD;
    private boolean isActivate = true;
    private boolean isOptTransitive = false;
    private boolean isFunTransitive = false;
    private boolean isConnect = false;
    private boolean isDuplicate = false;
    private boolean isSkipPath = false;
    private Context context;

    public RuleEngine() {
        rules = new ArrayList<Rule>();
        initPath();
    }
    
    // predefined rule bases
    void initPath(){
        path = new HashMap();
        path.put(OWL_RL,      "/rule/owlrl.rul");
        path.put(OWL_RL_LITE, "/rule/owlrllite.rul");
    }
    
    void set(Graph g) {
        graph = g;
    }

    public void set(QueryProcess p) {
        exec = p;
        p.setListPath(true);
    }
    
    /**
     * setProfile(OWL_RL) load OWL RL rule base and clean the OWL/RDF graph
     * 
     */
    public void setProfile(int n) {
        profile = n;

        switch (n) {

            case OWL_RL:                   
            case OWL_RL_LITE:    
                //optimizeOWLRL();
                try {
                    load(path.get(n));
                } catch (LoadException ex) {
                    logger.error(ex);
                }
                break;        
        }
    }
    
    void processProfile(){
        switch (profile) {

            case OWL_RL:                   
            case OWL_RL_LITE:    
                optimizeOWLRL();               
                break;        
        }
    }
    
    void load(String name) throws LoadException {
        Load ld = Load.create(graph);
        ld.setEngine(this);
        InputStream stream = RuleEngine.class.getResourceAsStream(name);
        ld.parse(stream, Load.RULE_FORMAT);
    }
      
    /**
     * 
     */
    public void optimizeOWLRL() {
        setSpeedUp(true);
        try {
            cleanOWL();
        } catch (IOException ex) {
            LogManager.getLogger(RuleEngine.class.getName()).log(Level.ERROR, "", ex);
        } catch (EngineException ex) {
            LogManager.getLogger(RuleEngine.class.getName()).log(Level.ERROR, "", ex);
        }
        // enable graph Index by timestamp
        graph.setHasList(true);
        
    }
    
    /**
     * Clean OWL RDF graph
     */
    public void cleanOWL() throws IOException, EngineException{
        Cleaner cl = new Cleaner(graph);
        cl.clean(Cleaner.OWL);
        graph.getIndex(1).clean();
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
    
    /**
     * Consider rules if there are new triples
     * Consider solutions that contain new triples
     * Do not create Mappings, create edge directly
     * Loop on transitive rules
     * Specific Closure code on transitive rules
     */
     public void setSpeedUp(boolean b) {
        setOptimize(b);
        setConstructResult(b);
        setOptTransitive(b);
        setFunTransitive(b);
        getQueryProcess().setListPath(b);
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
    
    /**
     * 
     * @return true if there is no Constraint Violation
     */
    public boolean success(){
        try {
            String q = QueryLoad.create().getResource("/query/rulesuccess.rq");
            QueryProcess ex = QueryProcess.create(graph);
            Mappings map = ex.query(q);
            return map.size() == 0;
        } catch (IOException ex) {
            logger.error(ex);
        } catch (EngineException ex) {
            logger.error(ex);
        }
        return true;
    }
    
    /**
     * @return a Graph of Constraint Violation, may be empty
     */
    public Graph constraint(){
         try {
            String q = QueryLoad.create().getResource("/query/ruleconstraint.rq");
            QueryProcess ex = QueryProcess.create(graph);
            Mappings map = ex.query(q);
            return (Graph) map.getGraph();
        } catch (IOException ex) {
            logger.error(ex);
        } catch (EngineException ex) {
            logger.error(ex);
        }
        return Graph.create();
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

    public Graph getRDFGraph() {
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
            LogManager.getLogger(RuleEngine.class.getName()).log(Level.ERROR, "", ex);
        } catch (EngineException ex) {
            LogManager.getLogger(RuleEngine.class.getName()).log(Level.ERROR, "", ex);
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
        defRule(Rule.create(UNKNOWN, rule));
    }
    
    public void defRule(Rule rule) {
        declare(rule);
        rules.add(rule);
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
        if (isTransformation()) {
            return qengine.defQuery(rule);
        } else {
            Query qq = exec.compileRule(rule, ds);
            if (qq != null) {
                Rule r = Rule.create(name, qq);
                defRule(r);
                return qq;
            }
            return null;
        }
    }
    
    void declare(Rule r) {
        Query q = r.getQuery();
        q.setID(rules.size());
        r.setIndex(rules.size());
        // Provenance Node set to entailed triples
        Node prov = DatatypeMap.createObject(q.getAST().toString(), q, IDatatype.RULE);
        q.setProvenance(prov);
        r.setProvenance(prov);
    }

    public int synEntail() {
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
        begin();
        int start = graph.size();
        try {
            infer();
            if (trace){
                //traceSize();
            }
            return graph.size() - start;
        }
        catch (OutOfMemoryError e){
            logger.error("Out of Memory: Rule Engine");
            return graph.size() - start;
        }
        finally {
            end();
            clean();
        }
    }
    
    // take a picture of graph Index, store it in graph kg:re1
    void begin(){
        processProfile();
        graph.getContext().storeIndex(NSManager.KGRAM+"re1");
        context();
    }
    
    void context(){
        if (getContext() != null){
            for  (Rule r : getRules()){
                r.getQuery().setContext(getContext());
            }
        }
    }
    
    /**
     * take a picture of graph Index, store it in graph kg:re2
     * store this engine in graph context, 
     * get rule base SPIN graph using: graph kg:engine {} 
     */
    void end(){
        graph.getContext().setRuleEngine(this);
        graph.getContext().storeIndex(NSManager.KGRAM+"re2");
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
        int size = graph.size(),
                start = size;
        loop = 0;
        int skip = 0, nbrule = 0, loopIndex = 0, tskip = 0, trun = 0, tnbres = 0;
        boolean go = true;

        // Entailment 
        graph.init();

        Record nt = null;
        stable = new STable();

        if (isOptimize) {
            // consider only rules that match newly entailed edge predicates
            // consider solutions that contain at leat one newly entailed edge           
            start();
            initOptimize();
        }

        while (go) {
            skip = 0;
            nbrule = 0;
            tnbres = 0;
            if (trace) {
                System.out.println("Loop: " + loop);
            }

          
            if (isOptimize){
                rw.start(loop);
                rw.setTrace(trace);
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
                        nt = record(rule, loopIndex, loop);
                        nbres = process(rule, nt, loop, loopIndex,  nbrule);
                        if (isClosure(rule)){
                            // rule run at saturation: record nb edge after saturation
                            nt = record(rule, loopIndex+1, loop);
                        }
                        setRecord(rule, nt);
                        tnbres += nbres;
                        nbrule++;
                        loopIndex++;
                    } else {
                        // run rules for which new edges have been created
                        // since previous run
                        int save = graph.size();
                        nt = record(rule, loopIndex, loop);
                        Record ot = rule.getRecord();

                        if (nt.accept(ot)) {
                            
                            if (trace){
                                ot.trace(nt);
                            }
                            
                            rw.start(ot, nt);
                            nbres = process(rule, nt, loop, loopIndex,  nbrule);
                            if (isClosure(rule)){
                                // rule run at saturation: record nb edge after execution
                                nt = record(rule, loopIndex+1, loop);
                            }
                            setRecord(rule, nt);
                            tnbres += nbres;
                            nbrule++;
                            loopIndex++;
                        } else {
                            skip++;
                        }

                    }

                    rw.finish(rule);
                } else {
                    nbres = process(rule, null, loop, -1,  nbrule);
                    nbrule++;
                }

                if (trace) {
                    stable.record(rule, nbres);
                }
            }



            if (trace) {
                System.out.println("NBrule: " + nbrule);
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
            } 
            else {
                go = false;
            }            
        }        
        
        if (debug) {
            System.out.println("Total Skip: " + tskip);
            System.out.println("Total Run: " + trun);     
            logger.debug("** Rule: " + (graph.size() - start));
        }               
    }
    
    void initOptimize() {
        // kgram return solutions that contain newly entailed edge
        rw = new ResultWatcher(graph);
        rw.setSkipPath(isSkipPath);
        if (isConstructResult) {
            // Construct will take care of duplicates
            rw.setDistinct(false);
        }
        // kgram interact with result watcher
        exec.addResultListener(rw);
    }
    
    
    
    /**
     * r is transitive closure
     * OR
     * r  = ?x rdf:type ?c2 :- ?x rdf:type ?c1 & ?c1 rdfs:subClassOf ?c2
     * pr = ?c1 rdfs:subClassOf ?c3 := ?c1 rdfs:subClassOf ?c2 & ?c2 rdfs:subClassOf ?c3
     * r is considered as closure (because previous pr is closure)
     */
    boolean isClosure(Rule r){
        if (r.isClosure()){
            // transitive rule at saturation
            return true;
        }
        if (r.isPseudoTransitive()){
            // r = rdf:type ? after pr = rdfs:subClassOf ?
            if (r.getIndex() > 0){
                Rule pr = rules.get(r.getIndex() - 1);
                if (pr.isClosure()){
                    // rdfs:subClassOf ?
                    return r.isPseudoTransitive(pr);
                }
            }
        }
        return false;
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
       graph.clean();
       graph.compact();
       cleanRules();
    }

    /**
     * Process one rule 
     */
    int process(Rule rule, Record nt,  int loop, int loopIndex,  int nbr) {
        if (trace){
           System.out.println(loop + " : " + nbr + " : " + rule.getIndex() + " " + ((rw!=null)?rw.isNew():""));
           System.out.println(rule.getAST());
        }
        
        Date d1 = new Date();
        boolean isConstruct = isOptimize && isConstructResult;

        Query qq = rule.getQuery();
        Construct cons = Construct.create(qq);
        cons.setDefaultGraph(graph.addRuleGraphNode());
        cons.setRule(rule, rule.getIndex(), rule.getProvenance());
        cons.set(new GraphManager(graph));
        cons.setLoopIndex(loopIndex);
        cons.setDebug(debug);

        if (isConstruct) {
            // kgram Result Listener create edges in list
            // after query completes, edges are inserted in graph
            // no Mappings are created by kgram
            cons.setBuffer(true);
            cons.setInsertList(new ArrayList<Entity>());
            //cons.setDefaultGraph(graph.addRuleGraphNode());
            Mappings map = Mappings.create(qq);
            // ResultWatcher call cons to create edges when a solution occur
            rw.setConstruct(cons);
            rw.setMappings(map);
        }

        int start = graph.size();
        
        if (isOptTransitive() && isFunTransitive() && rule.isTransitive()){
            // Java code emulate a transitive rule
            Closure clos = getClosure(rule);
            int index = (rule.getRecord() == null) ? -1 : rule.getRecord().getIndex();
            clos.closure(loop, loopIndex, index);
        }
        else {
            process(rule, cons);
            
            if (graph.size() > start && isConstruct 
                    && isOptTransitive() && rule.isAnyTransitive()){ 
                 // optimization for transitive rules: eval at saturation
                 transitive(rule, cons);               
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
    
    /**
     * Transitive Rule is executed at saturation in a loop
       for loops after first one, kgram take new edge list into account
       for evaluating the where part, the first query edge matches new edges only
       Producer will consider list of edges created at preceeding loop.
    */
    void transitive(Rule rule, Construct cons) {
        rule.setClosure(true);
        Query qq = rule.getQuery();
        boolean go = true;
        
        while (go) {
            // Producer will take this edge list into account
            qq.setEdgeList(cons.getInsertList());
            qq.setEdgeIndex(rule.getEdgeIndex());
            cons.setInsertList(new ArrayList<Entity>());
            int size = graph.size();

            process(rule, cons);

            if (graph.size() == size) {
                qq.setEdgeList(null);
                go = false;
            }
        }
    }
    
    Closure getClosure(Rule r){
       if (r.getClosure() == null){
          Closure c = new Closure(graph, rw.getDistinct());
          c.setTrace(trace);
          r.setClosure(c);
          c.setQuery(r.getQuery());
          c.setConnect(isConnect());
          c.init(r.getPredicate(0), r.getPredicate(1));
          r.setClosure(true);
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
            cons.insert(map, null);
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
        records = new ArrayList<Record>();
        sort();
        int i = 0;
        for (Rule r : rules) {
            init(r);
            r.setIndex(i);
            r.getQuery().setID(i);
            i++;
        }
        
        graph.cleanEdge();
    }

  

    /**
     * Store list of predicates of this rule
     */
    void init(Rule rule) {
        rule.set(rule.getQuery().getNodeList());
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

    @Override
    public String toGraph() {
        return toRDF();
    }
      
    /**
     * Return the rule base as a SPIN graph
     * graph eng:engine {}
     */
    public String toRDF() {    
        SPIN sp = SPIN.create();
        for (Rule r : rules){   
            sp.init();
            ASTQuery ast = (ASTQuery) r.getAST();
            sp.visit(ast, "kg:r" + r.getIndex()); 
            sp.nl();
        }
        return sp.toString();
    }
    
     /**
     *  graph eng:record {}
     */
    public Graphable getRecord(){
        final RuleEngine re = this;
        return new Graphable(){

            @Override
            public String toGraph() {
                return re.toRDFRecord();
            }

            @Override
            public void setGraph(Object obj) {
            }

            @Override
            public Object getGraph() {
                return null;            
            }
            
        };
    }
    
    public String toRDFRecord() { 
        String str = "";
        for (Record r : records){
            str += r.toRDF();
        }
        return str;
    }

    @Override
    public void setGraph(Object obj) {
        spinGraph = obj;
    }

    @Override
    public Object getGraph() {
        return spinGraph;
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * @return the qengine
     */
    public QueryEngine getQueryEngine() {
        return qengine;
    }

    /**
     * @param qengine the qengine to set
     */
    public void setQueryEngine(QueryEngine qengine) {
        this.qengine = qengine;
    }

    /**
     * @return the isTransformation
     */
    public boolean isTransformation() {
        return qengine != null && qengine.isTransformation();
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
     * Put pseudo transitive rule after it's transitive rule
     * tr = c1 subclassof c3 :- c1 subclassof c2 & c2 subclassof c3   
     * pr = x type c2        :- x type c1        & c1 subclassof c2
     * when tr runs at saturation (in a loop), one execution of pr just after tr
     * generates all rdf:type triples (at once)
     * then, if no new rdf:type/rdfs:subClassOf occur after pr, 
     * we can skip tr and pr at next loop
     * hence we gain one execution at last loop
     */
    void sort(){
        for (int i = 0; i < rules.size(); i++){
            Rule tr = rules.get(i);
            if (tr.isTransitive()){
                for (int j = 0; j < rules.size(); j++){
                    Rule pr = rules.get(j);
                    if (pr.isPseudoTransitive(tr)){
                        rules.remove(tr);
                        rules.remove(pr);
                        rules.add(tr);
                        rules.add(pr);
                        return;
                    }
                }
            }
        }
    }
    
   
    /**
     * Record predicates cardinality in graph
     */
    Record record(Rule r, int n, int l) {
        Record itable = new Record(r, n, l, graph.size());

        for (Node pred : r.getPredicates()) {
            int size = graph.size(pred);
            itable.put(pred, size);
        }

        return itable;
    }    

    
    public List<Record> getRecords(){
        return records;
    }

    void setRecord(Rule r, Record t) {
        r.setRecord(t);
        records.add(t);
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
        graph.clean();
    }

    public int type() {
        return RULE_ENGINE;
    }
}
