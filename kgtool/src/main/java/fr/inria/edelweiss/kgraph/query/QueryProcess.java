package fr.inria.edelweiss.kgraph.query;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.Option;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgenv.parser.Transformer;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.approximate.ext.ASTRewriter;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.load.Service;
import fr.inria.edelweiss.kgtool.util.Extension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Evaluator of SPARQL query by KGRAM Implement KGRAM as a lightweight version
 * with KGRAPH
 *
 * Query and Update are synchronized by a read/write lock on the graph There may
 * be several query in parallel OR only one update In addition, graph.init() is
 * synchronized because it may modify the graph
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryProcess extends QuerySolver {

    private static Logger logger = LogManager.getLogger(QueryProcess.class);
    //sort query edges taking cardinality into account
    static boolean isSort = false;
    private Manager updateManager;
    private GraphManager graphManager;
    Loader load;
    ReentrantReadWriteLock lock;
    // Producer may perform match locally
    boolean isMatch = false;

    static {
        setJoin(false);
        new Extension().process();
    }

    public QueryProcess() {
    }

    /**
     * Generate JOIN(A, B) if A and B do not share a variable (in triples)
     */
    public static void setJoin(boolean b) {
        if (b) {
            Query.testJoin = true;
        } else {
            Query.testJoin = false;
        }
    }

    /**
     * True means SPARQL semantics (default value) False means Corese semantics
     * (deprecated)
     */
    public static void setOptional(boolean b) {
        if (b) {
            Option.isOptional = true;
            Query.isOptional = true;
        } else {
            Option.isOptional = false;
            Query.isOptional = false;
        }
    }
    
    public static void testAlgebra(boolean b){
         fr.inria.edelweiss.kgenv.parser.Transformer.ISBGP = b;
         Eval.testAlgebra = b;
    }

    protected QueryProcess(Producer p, Evaluator e, Matcher m) {
        super(p, e, m);
        Graph g = getGraph(p);
        if (g != null) {
            // construct 
            setGraphManager(new GraphManager(g));
            // update
            setManager(new ManagerImpl(getGraphManager()));
        }
        // service
        set(ProviderImpl.create());
        init();
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
        switch (getMode()) {
            case SERVER_MODE:
                PluginImpl.readWriteAuthorized = false;
        }
    }

    public void setManager(Manager man) {
        updateManager = man;
    }

    Manager getManager() {
        return updateManager;
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

	private static ProducerImpl p;

    /**
	 * isMatch = true: Each Producer perform local Matcher.match() on its
	 * own graph for subsumption Hence each graph can have its own ontology
	 * and return one occurrence of each resource for ?x rdf:type aClass
	 * isMatch = false: (default) Global producer perform Matcher.match()
     */
    public static QueryProcess create(Graph g, boolean isMatch) {
		String factory = System.getProperty("fr.inria.corese.factory");//"fr.inria.corese.tinkerpop.Factory";
		if (factory == null || factory.compareTo("") == 0) {
			logger.info("property fr.inria.corese.factory not defined, using the default ProducerImpl");
				p = ProducerImpl.create(g);
				p.setMatch(isMatch);
		} else if (p == null) {
			logger.info("property fr.inria.corese.factory defined. Using factory: " + factory);
			try {
					Class<?> classFactory = Class.forName(factory);
				Method method = classFactory.getMethod("create", Graph.class);
				p = (ProducerImpl) method.invoke(null, g);
        p.setMatch(isMatch);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.fatal(ex);
				logger.fatal("impossible to create a producer, aborting");
				System.exit(-1);
			}
		}
        QueryProcess exec = QueryProcess.create(p);

        exec.setMatch(isMatch);
        return exec;
//		ProducerImpl p = ProducerImpl.create(g);
//		p.setMatch(isMatch);
//		QueryProcess exec = QueryProcess.create(p);
//		exec.setMatch(isMatch);
//		return exec;
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
        Interpreter eval = interpreter(p);
        eval.getProxy().setPlugin(PluginImpl.create(m));
        return eval;
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
        return query(q, map, ds);
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

    /**
     * defaut and named specify a Dataset if the query has no from/using (resp.
     * using named), kgram use this defaut (resp. named) if it exist for update,
     * this using is *not* used in the delete clause W3C sparql protocol use
     * this function
     */
    @Override
    public Mappings query(Query q) {
        return qquery(q, null, null);
    }

    @Override
    public Mappings eval(Query query) {
        return qquery(query, null, null);
    }

    @Override
    public Mappings eval(Query query, Mapping m) {
        return qquery(query, m, null);
    }

    @Override
    public Query load(String path) {
        QueryLoad ql = QueryLoad.create();
        try {
            String str = ql.readWE(path);
            Query q = compile(str);
            return q;
        } catch (LoadException ex) {
			logger.error(ex);
        } catch (EngineException ex) {
			logger.error(ex);
        }
        return null;
    }

    public Mappings qquery(Query q, Mapping map, Dataset ds) {
        try {
            return query(q, map, ds);
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
    public Mappings query(Graph g) {
        QueryGraph qg = QueryGraph.create(g);
        return query(qg);
    }

    public Mappings query(QueryGraph qg) {
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

    public Mappings query(ASTQuery ast) {
        if (ast.isUpdate()) {
            return update(ast);
        }
        return query(ast, null);
    }

//	public Mappings query(ASTQuery ast, List<String> from, List<String> named) {
//		Dataset ds = Dataset.create(from, named);
//		return query(ast, ds);
//	}
    public Mappings query(ASTQuery ast, Dataset ds) {
        if (ds != null) {
            ast.setDefaultDataset(ds);
        }
        Transformer transformer = transformer();
        Query query = transformer.transform(ast);
        try {
            return query(query, null, ds);
        } catch (EngineException e) {
            return Mappings.create(query);
        }
    }

    /**
     * equivalent of std query(ast) but for update
     */
    public Mappings update(ASTQuery ast) {
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
    Mappings query(Query q, Mapping m, Dataset ds) throws EngineException {

        pragma(q);
        if (q.getService() != null) {
            //@service <http://dbpedia.org/sparql>
            //select where {}
            return service(q, m);
        }
        Mappings map;

        if (q.isUpdate() || q.isRule()) {
            log(Log.UPDATE, q);
            map = synUpdate(q, ds);
            // map is the result of the last Update in q
            // hence the query in map is a local query corresponding to the last Update in q
            // return the Mappings of the last Update and the global query q
            map.setQuery(q);
        } else {
            map = synQuery(q, m);

            if (q.isConstruct()) {
                // construct where
                construct(map, null);
            }
            log(Log.QUERY, q, map);
        }

        finish(q, map);
        return map;
    }

    void finish(Query q, Mappings map) {
        Eval eval = map.getEval();
        if (eval != null) {
            eval.finish(q, map);
            map.setEval(null);
        }
    }

    Mappings synQuery(Query query, Mapping m) {
        Mappings map = null;
        try {
            readLock();
            logStart(query);
            map = query(query, m);

            return map;
        } finally {
            logFinish(query, map);
            readUnlock();
        }
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

    Mappings synUpdate(Query query, Dataset ds) throws EngineException {
        if (getMode() == SERVER_MODE) {
            return new Mappings();
        }
        Graph g = getGraph();
        GraphListener gl = (GraphListener) query.getPragma(Pragma.LISTEN);
        try {
            if (!isSynchronized()) { // && ! query.isSynchronized()){
                // TRICKY:
                // if query comes from workflow or from RuleEngine cleaner, 
                // it is synchronized by graph.init()
                // and it already has a lock by synQuery/synUpdate                           
                writeLock();
            }
            if (gl != null) {
                g.addListener(gl);
            }
            //g.logStart(query);
            if (query.isRule()) {
                return rule(query);
            } else {
                return update(query, ds);
            }
        } finally {
            //g.logFinish(query);
            if (gl != null) {
                g.removeListener(gl);
            }
            if (!isSynchronized()) { // && ! query.isSynchronized()){
                writeUnlock();
            }
        }
    }

    /**
     * Annotated query with a service send query to server
     *
     * @service <http://dbpedia.org/sparql>
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

    /**
     * Compute a construct where query considered as a (unique) rule Syntax:
     * rule construct {} where {}
     */
    Mappings rule(Query q) {
        RuleEngine re = RuleEngine.create(getGraph());
        re.setDebug(isDebug());
        re.defRule(q);
        getGraph().process(re);
        return Mappings.create(q);
    }

    /**
     * from and named (if any) specify the Dataset over which update take place
     * where {} clause is computed on this Dataset delete {} clause is computed
     * on this Dataset insert {} take place in Entailment.DEFAULT, unless there
     * is a graph pattern or a with
     *
     * This explicit Dataset is introduced because Corese manages the default
     * graph as the union of named graphs whereas in some case (W3C test case,
     * protocol) there is a specific default graph hence, ds.getFrom()
     * represents the explicit default graph
     *
     */
    Mappings update(Query query, Dataset ds) throws EngineException {
        if (ds != null && ds.isUpdate()) {
            // TODO: check complete() -- W3C test case require += default + entailment + rule
            complete(ds);
        }
        UpdateProcess up = UpdateProcess.create(this, ds);
        up.setDebug(isDebug());
        Mappings map = up.update(query);
        //map.setGraph(getGraph());
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
     * construct {} where {}      *
     */
    void construct(Mappings map, Dataset ds) {
        Query query = map.getQuery();
        Graph gg = Graph.create();
        // can be required to skolemize
        gg.setSkolem(isSkolem());
        Construct cons = Construct.create(query, new GraphManager(gg));
        cons.setDebug(isDebug() || query.isDebug());
        cons.construct(map);
        map.setGraph(gg);
    }

    /**
	 * Pragma specific to kgraph (in addition to generic pragma in
	 * QuerySolver)
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

    private void readLock() {
        getReadLock().lock();
    }

    private void readUnlock() {
        getReadLock().unlock();
    }

    private void writeLock() {
        getWriteLock().lock();
    }

    private void writeUnlock() {
        getWriteLock().unlock();
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

    void logStart(Query query) {
        if (getGraph() != null) {
            getGraph().logStart(query);
        }
    }

    void logFinish(Query query, Mappings m) {
        if (getGraph() != null) {
            getGraph().logFinish(query, m);
        }
    }

    /**
     * @return the graphManager
     */
    public GraphManager getGraphManager() {
        return graphManager;
    }

    /**
     * @param graphManager the graphManager to set
     */
    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
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
}