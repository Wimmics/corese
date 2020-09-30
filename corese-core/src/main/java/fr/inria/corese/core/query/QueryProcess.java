package fr.inria.corese.core.query;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.ProxyInterpreter;
import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.api.Log;
import fr.inria.corese.core.approximate.ext.ASTRewriter;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.core.query.update.Manager;
import fr.inria.corese.core.query.update.ManagerImpl;
import fr.inria.corese.core.util.Extension;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.sparql.api.QueryVisitor;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluator of SPARQL query by KGRAM 
 *
 * Query and Update are synchronized by a read/write lock on the graph There may
 * be several query in parallel OR only one update In addition, graph.init() is
 * synchronized because it may modify the graph
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryProcess extends QuerySolver {

    private static Logger logger = LoggerFactory.getLogger(QueryProcess.class);
    private static ProducerImpl p;
    private static final String EVENT = "event";
    static final String DB_FACTORY = "fr.inria.corese.tinkerpop.Factory";
    static final String DB_INPUT = "fr.inria.corese.tinkerpop.dbinput";
    static final String FUNLIB = "/function/";
    public static final String SHACL = "http://ns.inria.fr/sparql-template/function/datashape/main.rq";
    private static String solverVisitorName = null; 
    private static String serverVisitorName = null;
    //sort query edges taking cardinality into account
    static boolean isSort = false;

    static HashMap<String, Producer> dbmap;
    private Manager updateManager;
    private GraphManager graphManager;
    private QueryProcessUpdate queryProcessUpdate;
    Transformer transformer;
    Loader load;
    // fake eval for funcall public function
    Eval eval;
    ReentrantReadWriteLock lock;
    // Producer may perform match locally
    boolean isMatch = false;

    static {
        //setJoin(false);
        dbmap = new HashMap<>();
        new Extension().process();
    }

    private static boolean overWrite = false;

    public QueryProcess() {
    }

    /**
     * Generate JOIN(A, B) if A and B do not share a variable (in triples)
     */
//    public static void setJoin(boolean b) {
//        if (b) {
//            Query.testJoin = true;
//        } else {
//            Query.testJoin = false;
//        }
//    }

    protected QueryProcess(Producer p, Evaluator e, Matcher m) {
        super(p, e, m);
        Graph g = getGraph(p);
        complete();
        init();
    }

    void complete() {
        // service
        set(ProviderImpl.create(this));
        setQueryProcessUpdate(new QueryProcessUpdate(this));
    }

    void init() {
        Graph g = getGraph();
        if (isSort && g != null) {
            set(SorterImpl.create(g));
        }

        if (g != null) {
            lock = g.getLock();
        } else {
            // TODO: the lock should be unique to all calls
            // hence it should be provided by Producer
            lock = new ReentrantReadWriteLock();
        }
    }

    @Override
    public void initMode() {
    }

    public static QueryProcess create() {
        return create(Graph.create());
    }

    public static QueryProcess create(Graph g) {
        return create(g, false);
    }

    /**
     * Create an Eval initialized with a query q that contains function
     * definitions This Eval can be used to call these functions:
     * eval.eval(name, param) Use case: define callback functions.
     *
     */
    public static Eval createEval(Graph g, String q) throws EngineException {
        QueryProcess exec = create(g);
        Eval eval = exec.createEval(q, null);
        return eval;
    }

    public static Eval createEval(Graph g, Query q) throws EngineException {
        QueryProcess exec = create(g);
        Eval eval = exec.createEval(q);
        return eval;
    }

    /**
     * isMatch = true:  ?x a h:Person return one occurrence  for each instance of Person
     * isMatch = false: ?x a h:Person return all occurrences for each instance of Person 
     * where the instance has several types which match Person, such as x a h:Man, h:Person
     * default isMatch = false
     * In addition, each Producer perform local Matcher.match() on its own
     * graph for subsumption Hence each graph can have its own ontology and
     * return one occurrence of each resource for ?x rdf:type aClass isMatch =
     * false: (default) Global producer perform Matcher.match()
     */
    public static QueryProcess create(Graph g, boolean isMatch) {
        String factory = System.getProperty("fr.inria.corese.factory");
        if (factory == null || factory.compareTo("") == 0) {
            return stdCreate(g, isMatch);
        } else {
            return dbCreate(g, isMatch, factory, null);
        }
    }

    /**
     * When there is a graph database to manage the graph
     */
    public static QueryProcess dbCreate(Graph g, boolean isMatch, String factory, String db) {
        Producer p = getCreateProducer(g, factory, db);
        QueryProcess exec = QueryProcess.create(p);
        exec.setMatch(isMatch);
        return exec;
    }

    public static QueryProcess stdCreate(Graph g, boolean isMatch) {
        ProducerImpl p = ProducerImpl.create(g);
        p.setMatch(isMatch);
        QueryProcess exec = QueryProcess.create(p);
        exec.setMatch(isMatch);
        return exec;
    }

    public static synchronized Producer getCreateProducer(Graph g, String factory, String db) {
        if (db == null) {
            if (p == null) {
                logger.info("property fr.inria.corese.factory defined. Using factory: " + factory);
                p = createProducer(g, factory, db);
            }
            return p;
        } else {
            Producer prod = dbmap.get(db);
            if (prod == null) {
                prod = createProducer(g, factory, db);
                dbmap.put(db, prod);
            }
            return prod;
        }
    }

    static ProducerImpl createProducer(Graph g, String factory, String db) {
        if (db != null) {
            System.setProperty(DB_INPUT, db);
        }
        try {
            Class<?> classFactory = Class.forName(factory);
            Method method = classFactory.getMethod("create", Graph.class);
            ProducerImpl p = (ProducerImpl) method.invoke(null, g);
            logger.info("Connect db");
            return p;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.error("impossible to create a producer, aborting", ex);
        }
        return ProducerImpl.create(g);
    }

    public static QueryProcess create(Graph g, Graph g2) {
        QueryProcess qp = QueryProcess.create(g);
        qp.add(g2);
        return qp;
    }

    public static void setSort(boolean b) {
        isSort = b;
    }

    public void setLoader(Loader ld) {
        load = ld;
    }

    public Loader getLoader() {
        return load;
    }

    void setMatch(boolean b) {
        isMatch = b;
    }

    public Producer add(Graph g) {
        ProducerImpl p = ProducerImpl.create(g);
        Matcher match = MatcherImpl.create(g);
        p.set(match);
        if (isMatch) {
            p.setMatch(true);
        }
        add(p);
        return p;
    }

    public static QueryProcess create(Producer p) {
        Matcher match;
        if (p instanceof ProducerImpl) {
            ProducerImpl prod = (ProducerImpl) p;
            match = MatcherImpl.create(prod.getGraph());
            prod.set(match);
            if (prod.isMatch()) {
                // there is local match in Producer
                // create global match with Relax mode 
                match = MatcherImpl.create(prod.getGraph());
                match.setMode(Matcher.RELAX);
            }
        } else {
            match = MatcherImpl.create(Graph.create());
            match.setMode(Matcher.RELAX);
        }
        QueryProcess exec = QueryProcess.create(p, match);
        return exec;
    }

    /**
     * To Be Used by implementation other than Graph
     */
    public static QueryProcess create(Producer prod, Matcher match) {
        return new QueryProcess(prod, createInterpreter(prod, match), match);
    }

    public static QueryProcess create(Producer prod, Evaluator eval, Matcher match) {
        return new QueryProcess(prod, eval, match);
    }

    public static Interpreter createInterpreter(Producer p, Matcher m) {
        PluginImpl plugin = PluginImpl.create(m);
        ProxyInterpreter proxy = new ProxyInterpreter();
        proxy.setPlugin(plugin);
        Interpreter eval = new Interpreter(proxy);
        eval.setProducer(p);
        return eval;
    }

    /**
     * query = select from g where g is an external named graph return a new
     * QueryProcess with Producer(g)
     */
    QueryProcess focusFrom(Query q) {
        String name = getFromName(q);
        if (name != null) {
            Graph g = getGraph();
            Graph gg = g.getNamedGraph(name);
            if (gg != null) {
                q.getFrom().clear();
                if (isReentrant()) {
                    synchronized (g) {
                        gg.shareNamedGraph(g);
                    }
                }
                if (isDebug()) {
                    System.out.println("QP: update external graph name: " + name);
                }
                return create(gg);
            }
        }
        return this;
    }

    /**
     * *************************************************************
     *
     * API for query
     *
     ***************************************************************
     */
    public Mappings update(String squery) throws EngineException {
        return doQuery(squery, null, null);
    }

    @Override
    public Mappings query(String squery) throws EngineException {
        //System.out.println("QP: " + squery);
        return doQuery(squery, null, null);
    }

    /**
     * defaut and named specify a Dataset if the query has no from/using (resp.
     * named), kgram use defaut (resp. named) if it exist for update, defaut is
     * also used in the delete clause (when there is no with in the query) W3C
     * sparql test cases use this function
     */
    @Override
    public Mappings query(String squery, Mapping map, Dataset ds) throws EngineException {
        return doQuery(squery, map, ds);
    }

    Mappings doQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return query(null, q, map, ds);
    }

    @Override
    public Query compile(String squery, Dataset ds) throws EngineException {
        if (!hasVisitor()) {
            // Rewrite query when @relax annotation, otherwise do nothing
            addVisitor(new ASTRewriter());
        }
        return super.compile(squery, ds);
    }

    @Override
    public Query compile(String squery) throws EngineException {
        return compile(squery, null);
    }

    public ASTQuery ast(String q) throws EngineException {
        Query qq = compile(q);
        return getAST(qq);
    }

    public Mappings query(String squery, Dataset ds) throws EngineException {
        return query(squery, null, ds);
    }

    public Mappings query(String squery, Context c) throws EngineException {
        return doQuery(squery, null, Dataset.create(c));
    }

    @Override
    public Mappings query(String squery, Mapping map) throws EngineException {
        return query(squery, map, null);
    }

    public Mappings query(String squery, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), null);
    }
   
    public Mappings query(String squery, Context c, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), Dataset.create(c));
    }
    
    public Mappings query(String squery, ProcessVisitor vis) throws EngineException {
        return query(squery, Mapping.create(vis), null);
    }

    /**
     * defaut and named specify a Dataset if the query has no from/using (resp.
     * using named), kgram use this defaut (resp. named) if it exist for update,
     * this using is *not* used in the delete clause W3C sparql protocol use
     * this function
     */
    @Override
    public Mappings query(Query q) {
        return qquery(null, q, null, null);
    }

    @Override
    public Mappings eval(Query query) {
        return qquery(null, query, null, null);
    }

    @Override
    public Mappings eval(Query query, Mapping m) {
        return qquery(null, query, m, null);
    }

    /**
     * Use case: LDScript function execute query(construct where) or
     * query(insert where)
     */
    @Override
    public Mappings eval(Query query, Mapping m, Producer p) {
        return eval(null, query, m, p);
    }

    @Override
    public Mappings eval(Node gNode, Query query, Mapping m, Producer p) {
        Dataset ds = getUpdateDataset(query);
        if (p == null || p == getProducer()) {
            return protectQuery(gNode, query, m, ds);
        }
        return create(p).protectQuery(gNode, query, m, ds);
    }

    /**
     * Protect LDScript update query wrt select where use case isSynchronized()
     * :
     *
     * @event function call may perform update before or after select query
     * example: visitor init()
     */
    Mappings protectQuery(Node gNode, Query query, Mapping m, Dataset ds) {
        if (query.isUpdate()) {
            if (lock.getReadLockCount() > 0 && !isReentrant() && !isSynchronized()) {
                logger.info("Update rejected to avoid deadlock");
                return Mappings.create(query);
            }
        }
        return qquery(gNode, query, m, ds);
    }

    Dataset getUpdateDataset(Query q) {
        Context c = getContext(q);
        if (c != null && q.isUpdate()) {
            Dataset ds = new Dataset();
            ds.setContext(c);
            return ds;
        }
        return null;
    }


    public Mappings qquery(Node gNode, Query q, Mapping map, Dataset ds) {
        try {
            return query(gNode, q, map, ds);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Mappings.create(q);
    }

    /**
     * RDF Graph g considered as a Query Graph Build a SPARQL BGP with g
     * Generate and eval q KGRAM Query
     */
    public Mappings query(Graph g) throws EngineException {
        QueryGraph qg = QueryGraph.create(g);
        return query(qg);
    }

    public Mappings query(QueryGraph qg) throws EngineException {
        Query q = qg.getQuery();
        return query(q);
    }

    /**
     * q is construct {} where {} eval the construct consider the result as a
     * query graph execute the query graph
     */
    public Mappings queryGraph(String q) throws EngineException {
        Mappings m = query(q);
        Graph g = getGraph(m);
        return query(g);
    }

    /**
     * KGRAM + full SPARQL compliance : - type of arguments of functions (e.g.
     * sparql regex require string) - variable in select with group by - specify
     * the dataset
     */
    public Mappings sparql(String squery, Dataset ds) throws EngineException {
        return sparqlQueryUpdate(squery, ds, RDFS_ENTAILMENT);
    }

    public Mappings sparql(String squery, Dataset ds, int entail) throws EngineException {
        return sparqlQueryUpdate(squery, ds, entail);
    }

    public Mappings query(ASTQuery ast) throws EngineException {
        if (ast.isUpdate()) {
            return update(ast);
        }
        return query(ast, null);
    }

    public Mappings query(ASTQuery ast, Dataset ds) throws EngineException {
        if (ds != null) {
            ast.setDefaultDataset(ds);
        }
        Transformer transformer = transformer();
        Query query = transformer.transform(ast);
        try {
            return query(null, query, null, ds);
        } catch (EngineException e) {
            return Mappings.create(query);
        }
    }

    /**
     * equivalent of std query(ast) but for update
     */
    public Mappings update(ASTQuery ast) throws EngineException {
        Transformer transformer = transformer();
        Query query = transformer.transform(ast);
        return query(query);
    }

    /**
     * ****************************************
     *
     * Secure Query OR Update
     *
     *****************************************
     */
    public Mappings sparqlQuery(String squery) throws EngineException {
        Query q = compile(squery, null);
        if (q.isUpdate()) {
            throw new EngineException("Unauthorized Update in SPARQL Query:\n" + squery);
        }
        return query(q);
    }

    public Mappings sparqlQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return sparqlQuery(q, map);
    }

    public Mappings sparqlQuery(Query q, Mapping map) throws EngineException {
        if (q.isUpdate()) {
            throw new EngineException("Unauthorized Update in SPARQL Query:\n" + q.getAST().toString());
        }
        return eval(q, map);
    }

    public Mappings sparqlUpdate(String squery) throws EngineException {
        Query q = compile(squery, null);
        if (!q.isUpdate()) {
            throw new EngineException("Unauthorized Query in SPARQL Update:\n" + squery);
        }
        return query(q);
    }

    public Mappings sparqlQueryUpdate(String squery) throws EngineException {
        return query(squery);
    }

    /**
     * **************************************************************************
     *
     *
     ***************************************************************************
     */
    Mappings query(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        ASTQuery ast = getAST(q);
        if (ast.isLDScript()) {
            if (Access.reject(Feature.LD_SCRIPT, getLevel(m, ds, q))) {
                logger.info("LDScript unauthorized");
                throw new EngineException("LDScript unauthorized") ;
                //return Mappings.create(q);
            }
        }
        m = createMapping(m, ds);
        pragma(q);
        for (QueryVisitor vis : getAST(q).getVisitorList()) {
            vis.visit(q, getGraph());
        }
        if (q.getService() != null) {
            //@federate <http://dbpedia.org/sparql>
            //select where {}
            return service(q, m);
        } else {
            dbProducer(q);
        }
        Mappings map;

        if (q.isUpdate() || q.isRule()) {
            log(Log.UPDATE, q);
            if (Access.reject(Access.Feature.SPARQL_UPDATE, getLevel(m, ds, q))) { 
                logger.error("SPARQL Update unauthorized");
                //return Mappings.create(q);
                throw new EngineException("SPARQL Update unauthorized") ;
            }
            map = getQueryProcessUpdate().synUpdate(q, m, ds);
            // map is the result of the last Update in q
            // hence the query in map is a local query corresponding to the last Update in q
            // return the Mappings of the last Update and the global query q
            map.setQuery(q);
        } else {
            map = synQuery(gNode, q, m);

            if (q.isConstruct()) {
                // construct where
                construct(map, null);
            }
            log(Log.QUERY, q, map);
        }

        finish(q, map);
        return map;
    }
    
    /**
     * 
     */
    Mapping createMapping(Mapping m, Dataset ds) {
        if (ds != null) {
            if (ds.getBinding() != null) {
                if (m == null) {
                    m = new Mapping();
                }
                if (m.getBind() == null) {
                    m.setBind(ds.getBinding());
                }
            }
            if (ds.getContext() != null) {
                if (m == null) {
                    m = new Mapping();
                }
                Binding b = (Binding) m.getBind();
                if (b == null) {
                    b = Binding.create();
                    m.setBind(b);
                }
                b.setAccessLevel(ds.getContext().getLevel());
                b.setDebug(ds.getContext().isDebug());
            }
        }
        return m;
    }
    

    Mapping createMapping2(Mapping m, Dataset ds) {
        if (m == null) {
            if (ds != null) { 
                if (ds.getBinding() != null) {
                    m = Mapping.create(ds.getBinding());
                }
            }
        } else if (m.getBind() == null) { 
            if (ds != null) {
                m.setBind(ds.getBinding());
            }
        }
        return m;
    }

    void dbProducer(Query q) {
        ASTQuery ast = getAST(q);
        if (ast.hasMetadata(Metadata.DB)) {
            String factory = DB_FACTORY;
            if (ast.hasMetadata(Metadata.DB_FACTORY)) {
                factory = ast.getMetadataValue(Metadata.DB_FACTORY);
            }
            Producer prod = getCreateProducer(getGraph(), factory, ast.getMetadataValue(Metadata.DB));
            setProducer(prod);
        }
    }

    void finish(Query q, Mappings map) {
        Eval eval = map.getEval();
        if (eval != null) {
            eval.finish(q, map);
            map.setEval(null);
        }
    }

    Mappings synQuery(Node gNode, Query query, Mapping m) throws EngineException {
        Mappings map = null;
        try {
            syncReadLock(query);
            logStart(query);
            // select from g where
            // if g is an external named graph, create specific Producer(g)
            return basicQuery(gNode, query, m);
        } finally {
            logFinish(query, map);
            syncReadUnlock(query);
        }
    }

    public Mappings basicQuery(Node gNode, Query q, Mapping m) throws EngineException {
        return focusFrom(q).query(gNode, q, m);
    }

    void log(int type, Query q) {
        Graph g = getGraph();
        if (g != null) {
            g.log(type, q);
        }
    }

    void log(int type, Query q, Mappings m) {
        Graph g = getGraph();
        if (g != null) {
            g.log(type, q, m);
        }
    }

    Context getContext(Query q) {
        Context c = (Context) q.getContext();
        if (c == null) {
            return getAST(q).getContext();
        }
        return c;
    }

    /**
     * There may be a Context for access level
     * There may be a Binding for global variables (which contains access level).
     */
    Level getLevel(Mapping m, Dataset ds, Query q) {
        if (ds != null && ds.getContext() != null) {
            return  ds.getContext().getLevel();
        }
        if (m != null && m.getBind() != null) {
            return  ((Binding)m.getBind()).getAccessLevel();
        }
        return Level.DEFAULT;
        
//        Context c = getContext(q);
//        if (c == null) {
//            return Level.DEFAULT;
//        }
//        return c.getLevel();
    }

    static boolean isOverwrite() {
        return isReentrant();
    }

    public static void setOverwrite(boolean b) {
        setReentrant(b);
    }

    public static void setReentrant(boolean b) {
        overWrite = b;
    }

    public static boolean isReentrant() {
        return overWrite;
    }

    String getFromName(Query query) {
        List<Node> from = query.getFrom();
        if (from != null && from.size() == 1) {
            return from.get(0).getLabel();
        }
        return null;
    }

    /**
     * Annotated query with a service send query to server
     *
     * @federate <http://dbpedia.org/sparql>
     * select where {}
     */
    Mappings service(Query q, Mapping m) throws EngineException {
        Service serv = new Service(q.getService());
        try {
            return serv.query(q, m);
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
    }

    public EventManager getEventManager() {
        return getGraph().getEventManager();
    }

    ManagerImpl createUpdateManager(Graph g) {
        GraphManager man = new GraphManager(g);
        man.setQueryProcess(this);
        return new ManagerImpl(man);
    }

    /**
     * Implement SPARQL compliance
     */
    Mappings sparqlQueryUpdate(String squery, Dataset ds, int entail) throws EngineException {
        getEvaluator().setMode(Evaluator.SPARQL_MODE);
        setSPARQLCompliant(true);

        if (entail != STD_ENTAILMENT) {
            // include RDF/S entailments in the default graph
            if (ds == null) {
                ds = Dataset.create();
            }
            if (ds.getFrom() == null) {
                ds.defFrom();
            }
            complete(ds);
        }

        // SPARQL compliance
        ds.complete();

        Mappings map = query(squery, null, ds);

        if (!map.getQuery().isCorrect()) {
            map.clear();
        }
        return map;
    }

    void complete(Dataset ds) {
        if (ds != null && ds.hasFrom()) {
            ds.clean();
            // add the default graphs where insert or entailment may have been done previously
            for (String src : Entailment.GRAPHS) {
                ds.addFrom(src);
            }
        }
    }

    public Graph getGraph(Mappings map) {
        return (Graph) map.getGraph();
    }

    public Graph getGraph() {
        return getGraph(getProducer());
    }

    Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return null;
    }

    /**
     * construct {} where {} *
     */
    void construct(Mappings map, Dataset ds) {
        Query query = map.getQuery();
        Graph gg = getGraph().construct();
        // can be required to skolemize
        gg.setSkolem(isSkolem());
        Construct cons = Construct.create(query, new GraphManager(gg));
        cons.setDebug(isDebug() || query.isDebug());
        cons.construct(map);
        map.setGraph(gg);
    }

    /**
     * Pragma specific to kgraph (in addition to generic pragma in QuerySolver)
     */
    void pragma(Query query) {
        ASTQuery ast = (ASTQuery) query.getAST();

        if (ast != null && ast.getPragma() != null) {
            PragmaImpl.create(this, query).parse();
        }

        if (getPragma() != null) {
            PragmaImpl.create(this, query).parse(getPragma());
        }
    }

    /**
     * **********************************************
     */
    private Lock getReadLock() {
        return lock.readLock();
    }

    private Lock getWriteLock() {
        return lock.writeLock();
    }

    private void syncReadLock(Query q) {
        if (isSynchronized()) {
        } else {
            readLock(q);
        }
    }

    private void syncReadUnlock(Query q) {
        if (isSynchronized()) {
        } else {
            readUnlock(q);
        }
    }

    // if query comes from workflow or from RuleEngine cleaner, 
    // it is synchronized by graph.init()
    // and it already has a lock by synQuery/synUpdate 
    // hence do nothing
    void syncWriteLock(Query q) {
        if (isSynchronized()) {
        } else {
            writeLock(q);
        }
    }

    void syncWriteUnlock(Query q) {
        if (isSynchronized()) {
        } else {
            writeUnlock(q);
        }
    }

    private void readLock(Query q) {
        if (q.isLock()) {
            getReadLock().lock();
        }
    }

    private void readUnlock(Query q) {
        if (q.isLock()) {
            getReadLock().unlock();
        }
    }

    private void writeLock(Query q) {
        if (q.isLock()) {
            getWriteLock().lock();
        }
    }

    private void writeUnlock(Query q) {
        if (q.isLock()) {
            getWriteLock().unlock();
        }
    }

    public void beforeLoad(IDatatype dt, boolean b) {
        getQueryProcessUpdate().beforeLoad(dt, b);
    }

    public void afterLoad(IDatatype dt, boolean b) {
        getQueryProcessUpdate().afterLoad(dt, b);
    }

    /**
     * ***************************************************
     */
    /**
     * skolemize the blank nodes of the result Mappings
     */
    public Mappings skolem(Mappings map) {
        Graph g = getGraph();
        if (map.getGraph() != null) {
            // result of construct where
            g = (Graph) map.getGraph();
        }
        for (Mapping m : map) {
            Node[] nodes = m.getNodes();
            int i = 0;
            for (Node n : nodes) {
                if (n.isBlank()) {
                    nodes[i] = g.skolem(n);
                }
                i++;
            }
        }
        return map;
    }

    public void logStart(Query query) {
        if (getGraph() != null) {
            getGraph().logStart(query);
        }
    }

    public void logFinish(Query query, Mappings m) {
        if (getGraph() != null) {
            getGraph().logFinish(query, m);
        }
    }

    /**
     * ****************************************
     */
    public void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }
    
    
    /***************************************************************************
     * 
     * Function call and event function
     * 
     *************************************************************************/

    
    /**
     * Logger xt:method(us:start, us:Event, event, obj)
     * Use case: event logger
     * @deprecated
     */
    public void event(Event name, Event e, Object o) throws EngineException {
        IDatatype[] param = (o == null) ? param(DatatypeMap.createObject(e))
                : param(DatatypeMap.createObject(e), DatatypeMap.createObject(o));
        EventManager mgr = getGraph().getEventManager();
        boolean b = mgr.isVerbose();
        mgr.setVerbose(false);
        method(NSManager.USER + name.toString().toLowerCase(), NSManager.USER + e.toString(), param);
        mgr.setVerbose(b);
    }

    // import function definition as public function
    public boolean imports(String path) throws EngineException {
        return imports(path, true);
    }
    
    public boolean imports(String path, boolean pub) throws EngineException {
        String qp = "@public  @import <%s> select where {}";
        String ql = "@import <%s> select where {}";
        Query q = compile(String.format((pub)?qp:ql, path));
        return ! q.isImportFailure();
    }
    
    IDatatype[] param(IDatatype... ldt) {
        return ldt;
    }

    /**
     * method call: name of method, name of type
     */
    public IDatatype method(String name, String type, IDatatype[] param) throws EngineException {
        Function function = getFunction(name, type, param);
        if (function == null) {
            return null;
        }
        return call(EVENT, function, param, null);
    }
    
    public IDatatype callback(String name, IDatatype... param) throws EngineException {
        return new QuerySolverVisitor(getEval()).callback(getEval(), name, param);
    }
    
    /**
     * Execute LDScript function defined as @public
     */
    //@Override
    public IDatatype funcall(String name, IDatatype... param) throws EngineException {
        return funcall(name, (Context) null, param);
    }

    public IDatatype funcall(String name, Binding b, IDatatype... param) throws EngineException {
        return funcall(name, new Context().setBind(b), param);
    }

    public IDatatype funcall(String name, Context c, IDatatype... param) throws EngineException {
        Function function = getLinkedFunction(name, param);
        if (function == null) {
            return null;
        }
        return call(name, function, param, c);
    }

    IDatatype call(String name, Function function, IDatatype[] param, Context c) throws EngineException {
        Eval eval = getEval();
        eval.getMemory().getQuery().setContext(c);
        Binding b = getBind(eval);
        if (c != null) { 
            if (c.getBind() != null) {
                // share global variables
                b.share(c.getBind());
            }
            b.setAccessLevel(c.getLevel());
        }
        return new Funcall(name).call((Interpreter) eval.getEvaluator(),
                b, eval.getMemory(), eval.getProducer(), function, param);
    }

    Binding getBind(Eval eval) {
        return (Binding) eval.getMemory().getBind();
    }

    // Use case: funcall @public functions
    public Eval getEval() throws EngineException {
        if (eval == null) {
            eval = createEval("select where {}  ", null);
        }
        return eval;
    }

    /**
     * event @update: take care of query @event functions 
     * create current Eval with a ProcessVisitor
    *
     */
    public void init(Query q, Mapping m) {
        q.setInitMode(true);
        try {
            super.query(q, m);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(QueryProcess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        q.setInitMode(false);
        // set Visitor ready to work (hence, it is not yet active, it is ready to be active)
        getCurrentEval().getVisitor().setActive(false);
    }
    
    // Default Visitor to execute @event functions
    public ProcessVisitor getVisitor() {
        try {
            return getEval().getVisitor();
        } catch (EngineException ex) {
            return new ProcessVisitorDefault();
        }
    }
  
    // Visitor associated to current eval
    // To execute @event functions
    public ProcessVisitor getCurrentVisitor() {
        if (getCurrentEval() == null || getCurrentEval().getVisitor() == null) {
            return getVisitor();
        }
        return getCurrentEval().getVisitor();
    }
    
    
    @Override
    public ProcessVisitor createProcessVisitor(Eval eval) {
        if (getVisitorName() == null) {
            return super.createProcessVisitor(eval);
        }
        ProcessVisitor vis = createProcessVisitor(eval, getVisitorName());
        if (vis == null) {
            return super.createProcessVisitor(eval);
        }
        return vis;
    }

    
    public ProcessVisitor createProcessVisitor(Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(Eval.class).newInstance(eval);
            if (obj instanceof ProcessVisitor) {
                return (ProcessVisitor) obj;
            } else {
                logger.error("Uncorrect QuerySolverVisitor: " + name);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(QueryProcess.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            logger.error("Undefined QuerySolverVisitor: " + name);
        }
            
        return null;
    }
    
    
    

    Function getLinkedFunction(String name, IDatatype[] param) throws EngineException {
        Function function = getFunction(name, param);
        if (function == null) {
            //setLinkedFunction(true);
            getLinkedFunction(name);
            function = getFunction(name, param);
        }
        return function;
    }

    Function getFunction(String name, IDatatype[] param) {
        return Interpreter.getExtension().get(name, param.length);
    }

    /**
     * Search a method
     *
     * @public @type us:Event us:start(?e, ?o)
     */
    Function getFunction(String name, String type, IDatatype[] param) {
        return  Interpreter.getExtension().getMethod(
                name, DatatypeMap.newResource(type),
                param);
    }

    String basicParse(String path) throws EngineException {
        QueryLoad ql = QueryLoad.create();
        String pp = (path.endsWith("/")) ? path.substring(0, path.length() - 1) : path;
        String str = null;
        try {
            if (pp.startsWith(NSManager.STL)) {
                // @import <function/test.rq> within transformation such as st:turtle
                // the import uri is st:function/test.rq
                // consider it as a resource
                String name = "/" + NSManager.nsm().strip(pp, NSManager.STL);
                str = ql.getResource(name);
            } else {
                str = ql.readWE(pp);
            }
            return str;
        } catch (LoadException | IOException ex) {
            ex.printStackTrace();
            logger.error(ex.toString());
            throw new EngineException(ex);
        }
    }

    /**
     * Parse a function definition document use case: @import <uri>
     */
    @Override
    public ASTQuery parse(String path) throws EngineException {
        String str = basicParse(path);
        Transformer t = transformer();
        t.setBase(path);
        return t.parse(str);
    }

    /**
     * 1- Linked Function 2- owl:imports
     */
    @Override
    public Query parseQuery(String path) throws EngineException {
        String str = basicParse(path);
        Query q = compile(str, new Dataset().setBase(path));
        return q;
    }

    @Override
    public void getLinkedFunction(String label) throws EngineException {
        getTransformer().getLinkedFunction(label);
    }

    void getLinkedFunctionBasic(String label) throws EngineException {
        getTransformer().getLinkedFunctionBasic(label);
    }

    Transformer getTransformer() {
        if (transformer == null) {
            transformer = Transformer.create();
            transformer.setSPARQLEngine(this);
        }
        return transformer;
    }
    
    /***********************************************************************/
    
    /**
     * @return the queryProcessUpdate
     */
    public QueryProcessUpdate getQueryProcessUpdate() {
        return queryProcessUpdate;
    }

    /**
     * @param queryProcessUpdate the queryProcessUpdate to set
     */
    public void setQueryProcessUpdate(QueryProcessUpdate queryProcessUpdate) {
        this.queryProcessUpdate = queryProcessUpdate;
    }

    /**
     * @return the solverVisitorName
     */
    public static String getVisitorName() {
        return solverVisitorName;
    }
    

    /**
     * @param aSolverVisitorName the solverVisitorName to set
     */
    public static void setVisitorName(String aSolverVisitorName) {
        solverVisitorName = aSolverVisitorName;
    }

    /**
     * @return the serverVisitorName
     */
    public static String getServerVisitorName() {
        return serverVisitorName;
    }

    /**
     * @param serverVisitorName the serverVisitorName to set
     */
    public static void setServerVisitorName(String name) {
        serverVisitorName = name;
    }
}
