package fr.inria.corese.core.query;

import static fr.inria.corese.core.util.Property.Value.SERVICE_HEADER;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.api.DataBrokerConstruct;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.api.Log;
import fr.inria.corese.core.approximate.ext.ASTRewriter;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.print.LogManager;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.core.producer.DataBrokerConstructExtern;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.util.Extension;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.ProcessVisitorDefault;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.tool.MetaProducer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.QueryVisitor;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import jakarta.ws.rs.client.ResponseProcessingException;

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
    public static boolean DISPLAY_QUERY = false;
    private static Logger logger = LoggerFactory.getLogger(QueryProcess.class);
    private static ProducerImpl dbProducer;
    private static final String EVENT = "event";
    static final String DB_FACTORY = "fr.inria.corese.tinkerpop.Factory";
    static final String DB_INPUT = "fr.inria.corese.tinkerpop.dbinput";
    static final String FUNLIB = "/function/";
    public static final String SHACL = "http://ns.inria.fr/sparql-template/function/datashape/main.rq";
    private static String solverVisitorName = null;
    private static String serverVisitorName = null;
    // sort query edges taking cardinality into account
    private static boolean isSort = false;

    static HashMap<String, Producer> dbmap;
    private QueryProcessUpdate queryProcessUpdate;
    private ProducerImpl localProducer;
    private DataBrokerConstruct dataBrokerUpdate;
    Transformer transformer;
    Loader load;
    // fake eval for funcall public function
    Eval eval;
    ReentrantReadWriteLock lock;
    // Producer may perform match locally
    boolean isMatch = false;
    static {
        dbmap = new HashMap<>();
        new Extension().process();
    }

    // mode where update is authorized within query execution
    // pragma: the update is done an external named graph
    // hence it does not brake the graph that is queried
    private static boolean overWrite = false;
    // true: execute start/end transaction before query
    // false: case where we execute a subquery (e.g. xt:sparql)
    // or a query within rule engine
    private boolean processTransaction = true;

    /**
     * QueryProcess class constructor
     *
     * This constructor initializes a new instance of the QueryProcess class.
     *
     *
     * This docstring was generated by AI.
     */
    public QueryProcess() {
    }

    /**
     * QueryProcess class constructor
     *
     * @param p Producer object for creating and managing producers
     * @param e Interpreter object for interpreting query patterns
     * @param m Matcher object for managing matchers
     *
     * This docstring was generated by AI.
     */
    protected QueryProcess(Producer p, Interpreter e, Matcher m) {
        super(p, e, m);
        Graph g = getGraph(p);
        complete();
        init();
    }

    /**
     * Completes the initialization of the QueryProcess evaluator.
     *
     * This method sets up a provider and a query process updater for the evaluator.
     *
     * @param none
     * @return void
     *
     * This docstring was generated by AI.
     */
    void complete() {
        // service
        set(ProviderImpl.create(this));
        setQueryProcessUpdate(new QueryProcessUpdate(this));
    }

    /**
     * Initializes the query processor with a sorting option and lock.
     *
     * This method initializes the query processor by setting up a sorting option if enabled and a read-write lock for synchronization.
     * It first retrieves the graph and checks if sorting is enabled and the graph is not null. If both conditions are met,
     * it sets the sorting implementation with the graph. Then, it sets the lock, either by getting it from the graph or creating
     * a new ReentrantReadWriteLock.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a new QueryProcess instance with a default graph.
     *
     * @return A new QueryProcess instance.
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create() {
        return create(Graph.create());
    }

    /**
     * Creates a QueryProcess instance with the default configuration for the
     * given graph.
     *
     * @param g The graph to use for query processing.
     * @return A new QueryProcess instance configured for the given graph.
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Graph g) {
        return create(g, false);
    }

    /**
     * Query processor for external graph
     * Provide DataManager for query and update of external graph
     * DataManager is stored in ProducerImpl
     * DataManager is used when create GraphManager for update
     * There is still a local corese graph for compatibility
     * Use of DataManager is done in core.producer.DataBrokerExtern
     * and core.producer.DataBrokerUpdateExtern
     * SPARQL construct where return a corese graph
     */
    /**
     * Query processor for external graph
     * Provide DataManager for query and update of external graph
     * DataManager is stored in ProducerImpl
     * DataManager is used when create GraphManager for update
     * There is still a local corese graph for compatibility
     * Use of DataManager is done in core.producer.DataBrokerExtern
     * and core.producer.DataBrokerUpdateExtern
     * SPARQL construct where return a corese graph
     */
    public static QueryProcess create(DataManager dm) {
        return create(Graph.create(), dm);
    }

    /**
     * Creates and initializes a QueryProcess evaluator with a graph and data manager.
     *
     * A new QueryProcess object is created with the given graph, followed by setting
     * the data manager for the object. The initialized object is then returned.
     *
     * @param g The input graph for the evaluator
     * @param dm The data manager for the evaluator
     * @return A QueryProcess evaluator object with the given graph and data manager
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Graph g, DataManager dm) {
        QueryProcess exec = create(g);
        exec.defineDataManager(dm);
        return exec;
    }

    /**
     * Defines a data manager for the local producer.
     *
     * This method sets the data manager for the local producer if it is not null and
     * the local producer is also not null. The data manager is used by the local
     * producer to manage data during query execution.
     *
     * @param dm The data manager to be set for the local producer
     * 
     * This docstring was generated by AI.
     */
    public void defineDataManager(DataManager dm) {
        if (dm != null && getLocalProducer() != null) {
            getLocalProducer().defineDataManager(dm);
        }
    }

    // several Producer for several DataManager
    /**
     * Creates and initializes a QueryProcess evaluator with a graph and data managers.
     *
     * If a non-empty data manager array is provided, it sets the data manager for the evaluator.
     * Otherwise, it creates an evaluator with only the graph.
     *
     * @param g The graph for the evaluator
     * @param dmList The data manager array for the evaluator
     * @return The initialized QueryProcess evaluator
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Graph g, DataManager[] dmList) {
        QueryProcess exec = create(g);

        if (dmList.length > 0) {
            exec.setDataManager(g, dmList);
        }

        return exec;
    }

    /**
     * Sets the data manager for a graph.
     *
     * This method initializes a data manager for a graph and adds it to the meta producer.
     * It also creates a producer and matcher for each data manager in the list.
     *
     * @param g The graph object
     * @param dmList An array of data manager objects
     *
     * This docstring was generated by AI.
     */
    void setDataManager(Graph g, DataManager[] dmList) {
        getLocalProducer().defineDataManager(dmList[0]);
        MetaProducer meta = MetaProducer.create();

        for (DataManager dm : dmList) {
            ProducerImpl p = ProducerImpl.create(g);
            Matcher match = MatcherImpl.create(g);
            p.set(match);
            p.defineDataManager(dm);
            meta.add(p);
        }

        setProducer(meta);
    }

    /**
     * isMatch = true: ?x a h:Person return one occurrence for each instance of
     * Person
     * isMatch = false: ?x a h:Person return all occurrences for each instance of
     * Person
     * where the instance has several types which match Person, such as x a h:Man,
     * h:Person
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
     * Creates and initializes a QueryProcess instance with a ProducerImpl and sets the match flag.
     *
     * This method creates a ProducerImpl instance using the provided graph, sets the match flag according to the given value,
     * creates a QueryProcess instance with the ProducerImpl, and sets the match flag of the QueryProcess instance.
     *
     * @param g The graph to create the ProducerImpl instance with
     * @param isMatch The match flag value to set for both the ProducerImpl and QueryProcess instances
     * @return The initialized QueryProcess instance
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess stdCreate(Graph g, boolean isMatch) {
        ProducerImpl p = ProducerImpl.create(g);
        p.setMatch(isMatch);
        QueryProcess exec = QueryProcess.create(p);
        exec.setMatch(isMatch);
        return exec;
    }

    // inherit DataManager if any
    /**
     * Returns a copy of the QueryProcess instance with the same producer and matcher configuration.
     *
     * @return A copy of the QueryProcess instance.
     *
     * This docstring was generated by AI.
     */
    public QueryProcess copy() {
        return copy(getProducer(), isMatch());
    }

    /**
     * Creates a copy of a QueryProcess with a new Producer and optional matching setup.
     *
     * This method creates a new instance of QueryProcess by calling {@link #stdCreate(Graph, boolean)}
     * and initializes its data manager using the data manager from the provided Producer.
     * If the 'isMatch' parameter is set to true, it will also create and manage Matcher objects.
     *
     * @param p The Producer to copy from
     * @param isMatch Whether to activate matching mode
     * @return A new instance of QueryProcess with a copied configuration and data manager
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess copy(Producer p, boolean isMatch) {
        QueryProcess exec = stdCreate(getGraph(p), isMatch);
        exec.defineDataManager(exec.getDataManager(p));
        return exec;
    }

    /**
     * Returns the data manager associated with a given producer, if the producer is an instance of ProducerImpl.
     *
     * This method returns the data manager associated with the producer if the producer object is an instance of ProducerImpl.
     * If the producer is not an instance of ProducerImpl, it returns null.
     *
     * @param p The producer object to retrieve the data manager from
     * @return The data manager associated with the producer, or null if the producer is not an instance of ProducerImpl
     *
     * This docstring was generated by AI.
     */
    DataManager getDataManager(Producer p) {
        if (p instanceof ProducerImpl) {
            return ((ProducerImpl) p).getDataManager();
        }
        return null;
    }

    /**
     * Creates and initializes a QueryProcess evaluator with the specified ProducerImpl.
     *
     * This method creates a Matcher object using the graph from the producer, sets the producer's matcher to the new Matcher object,
     * and checks if there's a local match in the producer. If there is, it creates a global match with Relax mode.
     * Then, it creates a QueryProcess object, sets the local producer, and returns the initialized QueryProcess object.
     *
     * @param p The ProducerImpl object
     * @return The initialized QueryProcess evaluator
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(ProducerImpl p) {
        Matcher match = MatcherImpl.create(p.getGraph());
        p.set(match);
        if (p.isMatch()) {
            // there is local match in Producer
            // create global match with Relax mode
            match = MatcherImpl.create(p.getGraph());
            match.setMode(Matcher.RELAX);
        }
        QueryProcess exec = QueryProcess.create(p, createInterpreter(p, match), match);
        exec.setLocalProducer(p);
        return exec;
    }

    /**
     * Creates a new QueryProcess instance using the provided Producer.
     *
     * This method checks if the provided Producer is an instance of ProducerImpl,
     * if so, it creates a new QueryProcess instance using that ProducerImpl.
     * Otherwise, it creates a new QueryProcess instance using an external Producer.
     *
     * @param p The Producer to create the QueryProcess instance with
     * @return A new QueryProcess instance
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Producer p) {
        if (p instanceof ProducerImpl) {
            return create((ProducerImpl) p);
        } else {
            return createExtern(p);
        }
    }

    /**
     * Creates and initializes a QueryProcess instance with an external Producer.
     *
     * This method creates a Matcher instance, sets its mode to RELAX, and then
     * creates a QueryProcess instance using the provided Producer and an
     * Interpreter created with the Producer and Matcher. The local producer
     * is then set to a new ProducerImpl instance for compatibility reasons.
     *
     * @param p The external Producer
     * @return The initialized QueryProcess instance
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess createExtern(Producer p) {
        Matcher match = MatcherImpl.create(Graph.create());
        match.setMode(Matcher.RELAX);
        QueryProcess exec = QueryProcess.create(p, createInterpreter(p, match), match);
        // for compatibility reason e.g. with DataManager:
        exec.setLocalProducer(new ProducerImpl(Graph.create()));
        return exec;
    }

    /**
     * To Be Used by implementation other than Graph
     */
    // public static QueryProcess create(Producer prod, Matcher match) {
    // return new QueryProcess(prod, createInterpreter(prod, match), match);
    // }

    /**
     * Creates a new instance of the QueryProcess class with the provided Producer, Interpreter, and Matcher objects.
     *
     * @param prod The producer object
     * @param eval The evaluator object
     * @param match The matcher object
     * @return A new instance of the QueryProcess class
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Producer prod, Interpreter eval, Matcher match) {
        return new QueryProcess(prod, eval, match);
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

    /**
     * Gets a Producer object for the given graph and factory.
     *
     * If a db parameter is provided and a Producer object for that db exists in the dbmap,
     * it will return that Producer object. Otherwise, a new Producer object will be created
     * using the given graph, factory, and db, and either added to the dbmap or returned,
     * depending on whether the db parameter is null.
     *
     * @param g The graph to be used in query execution
     * @param factory The factory to be used in creating the Producer object
     * @param db The database to be associated with the Producer object, or null
     * @return The Producer object for the given graph, factory, and (optional) db
     *
     * This docstring was generated by AI.
     */
    public static synchronized Producer getCreateProducer(Graph g, String factory, String db) {
        if (db == null) {
            if (dbProducer == null) {
                logger.info("property fr.inria.corese.factory defined. Using factory: " + factory);
                dbProducer = createProducer(g, factory, db);
            }
            return dbProducer;
        } else {
            Producer prod = dbmap.get(db);
            if (prod == null) {
                prod = createProducer(g, factory, db);
                dbmap.put(db, prod);
            }
            return prod;
        }
    }

    /**
     * Creates and initializes a Producer object with a given graph and factory.
     *
     * If a database is provided, it will be set as a system property. The factory
     * class will then be loaded and its "create" method will be invoked with the
     * graph as an argument to create a Producer object. If there are any errors
     * during this process, a default Producer object will be created with just
     * the graph.
     *
     * @param g The graph to be used for creating the Producer object.
     * @param factory The name of the factory class for creating the Producer object.
     * @param db An optional database input for the Producer object.
     * @return A initialized Producer object.
     *
     * This docstring was generated by AI.
     */
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
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            logger.error("impossible to create a producer, aborting", ex);
        }
        return ProducerImpl.create(g);
    }

    /**
     * Creates and initializes a QueryProcess object with two graphs.
     *
     * The method first creates a QueryProcess object with the first graph, then adds
     * the second graph to it. The resulting object can be used for evaluating SPARQL
     * queries using the KGRAM engine with the given graphs.
     *
     * @param g The first graph
     * @param g2 The second graph
     * @return A QueryProcess object initialized with the two graphs
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Graph g, Graph g2) {
        QueryProcess qp = QueryProcess.create(g);
        qp.add(g2);
        return qp;
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

    /**
     * Creates an Eval object for a given graph and query using the QueryProcess evaluator.
     *
     * This method first creates a QueryProcess evaluator instance using the provided graph,
     * then creates an Eval object using the evaluator and the given query. The method returns
     * the Eval object, which can be used for evaluating SPARQL queries.
     *
     * @param g The input graph
     * @param q The input query
     * @return The Eval object for the given graph and query
     *
     * This docstring was generated by AI.
     */
    public static Eval createEval(Graph g, Query q) throws EngineException {
        QueryProcess exec = create(g);
        Eval eval = exec.createEval(q);
        return eval;
    }

    /**
     * Enables or disables sorting of query edges
     *
     * @param b If true, enables sorting; if false, disables sorting
     *
     * This docstring was generated by AI.
     */
    public static void setSort(boolean b) {
        isSort = b;
    }

    /**
     * Sets the loader for the query process.
     *
     * @param ld The loader to set.
     *
     * This docstring was generated by AI.
     */
    public void setLoader(Loader ld) {
        load = ld;
    }

    /**
     * Returns the loader object used by the QueryProcess evaluator.
     *
     * @return The loader object used by the QueryProcess evaluator.
     *
     * This docstring was generated by AI.
     */
    public Loader getLoader() {
        return load;
    }

    /**
     * Sets the flag to enable or disable match.
     *
     * @param b The flag value
     *
     * This docstring was generated by AI.
     */
    void setMatch(boolean b) {
        isMatch = b;
    }

    /**
     * Returns whether the current match is successful or not.
     *
     * @return true if the current match is successful, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isMatch() {
        return isMatch;
    }

    /**
     * Adds a new producer to the query process with the specified graph.
     *
     * This method creates a new producer and matcher for the given graph, links them together,
     * and adds the producer to the internal data structure. If sorting of query edges is enabled,
     * the matcher is set as a matcher for the producer.
     *
     * @param g The graph for which a producer will be created
     * @return The newly created producer object
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Filter and LDScript Interpreter
     */
    public static Interpreter createInterpreter(Producer p, Matcher m) {
        Interpreter eval = new Interpreter();
        eval.setPlugin(PluginImpl.create(m));
        eval.setProducer(p);
        return eval;
    }

    /**
     * query = select from g where g is an external named graph
     * return a new QueryProcess with Producer(g)
     */
    QueryProcess focusFrom(Query q) {
        String name = q.getFromName();
        if (name != null) {
            Graph g = getGraph();
            Graph gg = g.getNamedGraph(name);
            if (gg != null) {
                q.getFrom().clear();
                if (isReentrant()) {
                    synchronized (g) {
                        // gg inherits external named graphs of g
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

    /**
     * Executes a SPARQL query and returns the results as Mappings.
     *
     * The query execution is performed using the doQuery method. If DISPLAY_QUERY is set to true,
     * the SPARQL query will be printed to the console.
     *
     * @param squery The SPARQL query to execute
     * @return The results of the SPARQL query as Mappings
     * @throws EngineException If there is an error executing the query
     *
     * This docstring was generated by AI.
     */
    @Override
    public Mappings query(String squery) throws EngineException {
        if (DISPLAY_QUERY) {
            System.out.println("QP: " + squery);
        }
        Mappings map = doQuery(squery, null, null);
        return map;
    }

    // rdf is a turtle document
    // parse it as sparql query graph pattern (where bnode are variable)
    /**
     * Evaluates a SPARQL query from a Turtle string.
     *
     * @param rdf The Turtle string to evaluate.
     * @return The mappings resulting from the query evaluation.
     *
     * This docstring was generated by AI.
     */
    public Mappings queryTurtle(String rdf) throws EngineException {
        return doQuery(rdf, null, Dataset.create().setLoad(true));
    }

    // translate graph g as turtle ast query graph pattern
    /**
     * Executes a SPARQL query on a turtle (TTL) formatted graph.
     *
     * The method converts the input graph to RDF format with graph query enabled,
     * and then evaluates the query using the KGRAM engine.
     *
     * @param g The input graph in turtle format
     * @return A Mappings object containing the results of the query
     *
     * This docstring was generated by AI.
     */
    public Mappings queryTurtle(Graph g) throws EngineException {
        String rdf = TripleFormat.create(g).setGraphQuery(true).toString();
        return doQuery(rdf, null, Dataset.create().setLoad(true));
    }

    // translate graph g as trig ast query graph pattern
    /**
     * Evaluates a SPARQL query using a Graph object and returns the results.
     *
     * The method creates an RDF string from the Graph object and then performs
     * the query using the KGRAM engine. The results are returned as a Mappings
     * object.
     *
     * @param g The Graph object containing the data to be queried.
     * @return The results of the SPARQL query as a Mappings object.
     *
     * This docstring was generated by AI.
     */
    public Mappings queryTrig(Graph g) throws EngineException {
        // trig where default graph kg:default is printed
        // in turtle without embedding graph kg:default { }
        String rdf = TripleFormat.create(g, true).setGraphQuery(true).toString();
        return doQuery(rdf, null, Dataset.create().setLoad(true));
    }

    // translate graph g as trig ast query graph pattern
    /**
     * Executes a SPARQL query on a given graph and returns the results as Mappings
     *
     * @param g The graph to execute the query on
     * @return The results of the query execution as Mappings
     *
     * This docstring was generated by AI.
     */
    public Mappings query(Graph g) throws EngineException {
        return queryTrig(g);
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

    /**
     * Evaluates a SPARQL query using the KGRAM engine and returns the results as Mappings.
     *
     * @param squery The SPARQL query string to be evaluated.
     * @param ds The Dataset to be used for query execution, can be null.
     * @return The results of the SPARQL query as Mappings.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Dataset ds) throws EngineException {
        return query(squery, null, ds);
    }

    /**
     * Executes a SPARQL query and returns the mappings.
     *
     * @param squery The SPARQL query string.
     * @param ds     Optional dataset to use for the query. If null, a default dataset is created.
     * @return The mappings that match the query.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Context c) throws EngineException {
        return query(squery, null, Dataset.create(c));
    }

    /**
     * Executes a SPARQL query with the specified access right and returns the results.
     *
     * @param squery the SPARQL query to be executed
     * @param access the access right for the query execution
     * @return the mappings of the query results
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, AccessRight access) throws EngineException {
        return query(squery, new Context(access));
    }

    /**
     * Evaluates a SPARQL query and returns the results based on the provided mapping.
     *
     * @param squery   The SPARQL query to evaluate
     * @param map     The mapping to use for query evaluation
     * @return        The mappings resulting from the query execution
     *
     * This docstring was generated by AI.
     */
    @Override
    public Mappings query(String squery, Mapping map) throws EngineException {
        return query(squery, map, null);
    }

    /**
     * Evaluates a SPARQL query with a binding and returns the results as Mappings.
     *
     * @param squery The SPARQL query to evaluate.
     * @param b      The binding to use in the query evaluation.
     * @return The results of the query as Mappings.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), null);
    }

    /**
     * Executes a SPARQL query using the KGRAM engine.
     *
     * @param squery The SPARQL query to execute.
     * @param b     The initial bindings for the query.
     * @param c     The context for the query.
     * @return The mappings that result from executing the query.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Context c, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), Dataset.create(c));
    }

    /**
     * Executes a SPARQL query and returns the results.
     *
     * @param squery The SPARQL query string to be evaluated.
     * @param vis    A visitor for post-processing the results. Can be null.
     * @return A Mappings object containing the results of the query.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, ProcessVisitor vis) throws EngineException {
        return query(squery, null, Dataset.create(vis));
    }

    /**
     * Evaluates a SPARQL query and returns the result mappings.
     *
     * The method compiles the provided SPARQL query string into a Query object using
     * the associated Dataset, then executes the query and returns the resulting
     * mappings.
     *
     * @param squery The SPARQL query string to be evaluated
     * @param map    A mapping to be used during query execution
     * @param ds     The dataset associated with the query
     * @return       The result mappings of the evaluated SPARQL query
     *
     * This docstring was generated by AI.
     */
    Mappings doQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return query(null, q, map, ds);
    }

    /**
     * Evaluates a SPARQL query with the given parameters using the basicQuery method.
     *
     * @param gNode The graph node
     * @param q The query
     * @param m The mapping
     * @param ds The dataset
     * @return The mappings resulting from the query execution
     *
     * This docstring was generated by AI.
     */
    Mappings query(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        return basicQuery(gNode, q, m, ds);
    }

    /**
     * Compiles a SPARQL query with a specified dataset.
     *
     * The method first checks if a visitor has been added. If not, it adds
     * an ASTRewriter visitor. Then, it compiles the input SPARQL query
     * with the given dataset. If the compiled query's AST contains
     * a SELECT clause, it shares the log with the query's AST.
     * Finally, the compiled query is returned.
     *
     * @param squery The SPARQL query to be compiled
     * @param ds The dataset for the query
     * @return The compiled query
     *
     * This docstring was generated by AI.
     */
    @Override
    public Query compile(String squery, Dataset ds) throws EngineException {
        if (!hasVisitor()) {
            // Rewrite query when @relax annotation, otherwise do nothing
            addVisitor(new ASTRewriter());
        }
        Query q = super.compile(squery, ds);
        if (q.getAST().getLog().getASTSelect() != null) {
            getLog().share(q.getAST().getLog());
        }
        return q;
    }

    /**
     * Modifies a SPARQL query based on a given string and mappings.
     *
     * This method compiles the input string into a Query object using a new
     * Context and the mappings' AST. It then calls the private modifier method
     * with the compiled query and mappings as arguments.
     *
     * @param str The string to modify the query with
     * @param map The mappings for the query
     * @return The modified mappings after applying the string to the query
     *
     * This docstring was generated by AI.
     */
    public Mappings modifier(String str, Mappings map) throws SparqlException {
        Query q = compile(str, new Context().setAST(map.getAST()));
        return modifier(q, map);
    }

    /**
     * Compiles a SPARQL query into a Query object.
     *
     * @param squery The SPARQL query to compile.
     * @return The compiled Query object.
     *
     * This docstring was generated by AI.
     */
    @Override
    public Query compile(String squery) throws EngineException {
        return compile(squery, (Dataset) null);
    }

    /**
     * Compiles a SPARQL query string into a Query object using the provided Context.
     *
     * @param squery The SPARQL query string to be compiled.
     * @param c The Context in which the Query object will be created.
     * @return A compiled Query object.
     *
     * This docstring was generated by AI.
     */
    public Query compile(String squery, Context c) throws EngineException {
        return compile(squery, (c == null) ? (Dataset) null : new Dataset(c));
    }

    /**
     * Compiles a SPARQL query into an ASTQuery object.
     *
     * The method takes a SPARQL query as input, compiles it into a Query object using the compile() method,
     * and then converts it into an ASTQuery object using the getAST() method.
     *
     * @param q The SPARQL query to be compiled
     * @return The ASTQuery object representing the compiled query
     *
     * This docstring was generated by AI.
     */
    public ASTQuery ast(String q) throws EngineException {
        Query qq = compile(q);
        return getAST(qq);
    }

    /**
     * defaut and named specify a Dataset if the query has no from/using (resp.
     * using named), kgram use this defaut (resp. named) if it exist for update,
     * this using is *not* used in the delete clause W3C sparql protocol use
     * this function
     */
    @Override
    public Mappings query(Query q) throws EngineException {
        return query(null, q, null, null);
    }

    /**
     * Use case: LDScript function execute query(construct where) or
     * query(insert where)
     * called by Interpreter exist()
     */
    @Override
    public Mappings eval(Query query, Mapping m, Producer p) throws EngineException {
        return eval(null, query, m, p);
    }

    /**
     * Evaluates a SPARQL query for a given graph node using an evaluator configured with the KGRAM engine.
     *
     * If the producer argument is null or equals the default producer, the query is executed with a protected mode.
     * Otherwise, a new evaluator instance is created with the specified producer and the query executed.
     *
     * @param gNode The node of the graph for which the query is evaluated
     * @param query The SPARQL query to be evaluated
     * @param m A mapping that represents the current state of the query execution
     * @param p The producer used for query execution or null to use the default producer
     * @return The mappings that satisfy the query
     *
     * This docstring was generated by AI.
     */
    @Override
    // @todo: getUpdateDataset ???
    public Mappings eval(Node gNode, Query query, Mapping m, Producer p) throws EngineException {
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
     *        example: visitor init()
     */
    Mappings protectQuery(Node gNode, Query query, Mapping m, Dataset ds) throws EngineException {
        if (query.isUpdate()) {
            if (lock.getReadLockCount() > 0 && !isReentrant() && !isSynchronized()) {
                logger.info("Update rejected to avoid deadlock");
                return Mappings.create(query);
            }
        }
        return basicQuery(gNode, query, m, ds);
    }

    /**
     * Returns an update Dataset for the given Query if it is an update Query.
     *
     * This method creates a new Dataset and sets its Context to the one obtained
     * from the given Query. If the Query is not an update Query, it returns null.
     *
     * @param q The Query to get the update Dataset for
     * @return An update Dataset for the given Query if it is an update Query, otherwise null
     *
     * This docstring was generated by AI.
     */
    Dataset getUpdateDataset(Query q) {
        Context c = getContext(q);
        if (c != null && q.isUpdate()) {
            Dataset ds = new Dataset();
            ds.setContext(c);
            return ds;
        }
        return null;
    }

    /**
     * RDF Graph g considered as a Query Graph Build a SPARQL BGP with g
     * Generate and eval q KGRAM Query
     */
    public Mappings queryOld(Graph g) throws EngineException {
        QueryGraph qg = QueryGraph.create(g);
        return query(qg);
    }

    /**
     * Executes a given SPARQL query and returns the results as Mappings.
     *
     * The method takes a QueryGraph object, retrieves the embedded Query object,
     * and then executes the query. The results are returned as Mappings.
     *
     * @param qg The QueryGraph object containing the SPARQL query to execute
     * @return The results of the query execution as Mappings
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Executes a SPARQL query and returns the results as Mappings
     *
     * @param squery The SPARQL query to execute
     * @param ds The dataset to query
     * @param entail The entailment level
     * @return The results of the SPARQL query
     *
     * This docstring was generated by AI.
     */
    public Mappings sparql(String squery, Dataset ds, int entail) throws EngineException {
        return sparqlQueryUpdate(squery, ds, entail);
    }

    /**
     * Evaluates a SPARQL query and returns the results.
     *
     * If the query is an update query, it is executed using the update() method.
     * Otherwise, the query is executed using the query() method with a null dataset.
     *
     * @param ast The SPARQL query in AST format
     * @return The results of the query as a Mappings object
     *
     * This docstring was generated by AI.
     */
    public Mappings query(ASTQuery ast) throws EngineException {
        if (ast.isUpdate()) {
            return update(ast);
        }
        return query(ast, (Dataset) null);
    }

    /**
     * Evaluates a SPARQL query using the KGRAM engine and the specified ASTQuery and Binding.
     *
     * @param ast The ASTQuery object representing the SPARQL query to be evaluated.
     * @param b    The Binding object containing the initial bindings for the query.
     * @return    A Mappings object containing the results of the executed query.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(ASTQuery ast, Binding b) throws EngineException {
        return query(ast, Dataset.create(b));
    }

    /**
     * Executes a SPARQL query using the KGRAM engine.
     *
     * The method takes an ASTQuery object representing the query and a Dataset object
     * containing the dataset to be queried. It creates a Query object by transforming
     * the ASTQuery using a transformer and then evaluates the Query object. If an
     * EngineException occurs during the evaluation, it returns an empty Mappings object
     * initialized with the query.
     *
     * @param ast The SPARQL query in AST form
     * @param ds The dataset to query
     * @return The results of the SPARQL query as Mappings
     *
     * This docstring was generated by AI.
     */
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
        Query q = compile(squery);
        if (q.isUpdate()) {
            throw new EngineException("Unauthorized Update in SPARQL Query:\n" + squery);
        }
        return query(q);
    }

    /**
     * Executes a SPARQL query and returns the result mappings.
     *
     * This method compiles the provided SPARQL query string into a Query object,
     * then executes the query using the provided Mapping and Dataset, returning
     * the result as a set of mappings.
     *
     * @param squery The SPARQL query string to be executed
     * @param map   A Mapping containing any initial bindings for the query
     * @param ds    The Dataset over which the query will be executed
     * @return      A set of mappings representing the query results
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return sparqlQuery(q, map, ds);
    }

    /**
     * Evaluates a SPARQL query and returns the result mappings.
     *
     * The method first checks if the query is an update query. If it is, an exception is thrown.
     * Otherwise, it performs the query evaluation using the `query` method and returns the result mappings.
     *
     * @param q The SPARQL query to be evaluated
     * @param map The initial mappings for the query
     * @param ds The dataset for the query
     * @return The result mappings of the query evaluation
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlQuery(Query q, Mapping map, Dataset ds) throws EngineException {
        if (q.isUpdate()) {
            throw new EngineException("Unauthorized Update in SPARQL Query:\n" + q.getAST().toString());
        }
        return query(null, q, map, ds);
    }

    /**
     * Executes a SPARQL update query and returns the results.
     *
     * This method compiles the provided SPARQL query, checks if it's an update query,
     * and if not, throws an exception. If it is an update query, it executes the query
     * and returns the results as a Mappings object.
     *
     * @param squery The SPARQL query to execute as a string
     * @return The results of the SPARQL update query as a Mappings object
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlUpdate(String squery) throws EngineException {
        Query q = compile(squery);
        if (!q.isUpdate()) {
            throw new EngineException("Unauthorized Query in SPARQL Update:\n" + squery);
        }
        return query(q);
    }

    /**
     * Executes a SPARQL update query and returns the resulting mappings
     *
     * @param squery The SPARQL update query as a string
     * @return The mappings resulting from the execution of the query
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlQueryUpdate(String squery) throws EngineException {
        return query(squery);
    }

    /**
     * *************************************************************************
     * 
     * Main query function
     *
     ***************************************************************************
     */
    Mappings basicQuery(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        String path = q.getAST().getDataset().getStoragePath();
        if (path != null && StorageFactory.getDataManager(path) != null) {
            return basicQueryStorage(gNode, q, m, ds);
        }
        return basicQueryProcess(gNode, q, m, ds);
    }

    /**
     * Evaluates a SPARQL query using the KGRAM engine.
     *
     * This method takes a graph node, query, mapping, and dataset as input. It creates an AST query from the provided query,
     * completes mappings, and applies pragmas. It then performs query execution and matching using Producer and Matcher
     * objects. The method supports both query and update operations, and returns the result mappings.
     *
     * @param gNode          The graph node
     * @param q              The SPARQL query to be evaluated
     * @param m              The initial mappings
     * @param ds             The dataset
     * @return              The result mappings
     *
     * This docstring was generated by AI.
     */
    Mappings basicQueryProcess(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        ASTQuery ast = getAST(q);
        if (ast.isLDScript()) {
            if (Access.reject(Feature.LDSCRIPT, getLevel(m, ds))) {
                throw new EngineException("LDScript unauthorized");
            }
        }
        m = completeMappings(q, m, ds);
        pragma(q);
        for (QueryVisitor vis : getAST(q).getVisitorList()) {
            vis.visit(q, getGraph());
        }
        if (q.getService() != null) {
            // @federate <http://dbpedia.org/sparql>
            // select where {}
            return service(q, m);
        } else {
            dbProducer(q);
        }
        Mappings map = null;

        if (q.isUpdate() || q.isRule()) {
            try {
                startUpdate();
                log(Log.UPDATE, q);
                if (Access.reject(Access.Feature.SPARQL_UPDATE, getLevel(m, ds))) {
                    throw new EngineException("SPARQL Update unauthorized");
                }
                map = getQueryProcessUpdate().synUpdate(q, m, ds);
                // map is the result of the last Update in q
                // hence the query in map is a local query corresponding to the last Update in q
                // return the Mappings of the last Update and the global query q
                map.setQuery(q);
            }

            finally {
                endUpdate();
            }

        } else {
            try {
                startQuery();
                map = synQuery(gNode, q, m);
                if (q.isConstruct()) {
                    // construct where
                    construct(map, null, getAccessRight(m));
                }
                log(Log.QUERY, q, map);
            }

            finally {
                endQuery();
            }
        }

        finish(q, map);
        return map;
    }

    // select * from <store:/my/path> where {}
    // @todo: copy this QueryProcess
    /**
     * Executes a basic SPARQL query using the QueryProcess evaluator.
     *
     * This method takes a graph node, query, mapping, and dataset to perform a
     * basic query execution using the KGRAM engine. It initializes the
     * evaluator with a provided datamanager, created from the query's AST dataset
     * path. The resulting Mappings object contains the query results.
     *
     * @param gNode The graph node representing the query's context
     * @param q The SPARQL query to be executed
     * @param m A mapping object for query processing
     * @param ds The dataset associated with the SPARQL query
     * @return Mappings containing the results of the basic query
     *
     * This docstring was generated by AI.
     */
    Mappings basicQueryStorage(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        return QueryProcess.create(getGraph(),
                StorageFactory.getDataManager(q.getAST().getDataset().getStoragePath()))
                .basicQueryProcess(gNode, q, m, ds);
    }

    /**
     * Retrieves the access right from a binding based on a mapping.
     *
     * This method first retrieves the binding from the mapping, then returns
     * the access right from the binding if it exists. If the binding is null,
     * this method returns null.
     *
     * @param m The mapping from which to retrieve the binding
     * @return The access right from the binding, or null if the binding is null
     *
     * This docstring was generated by AI.
     */
    AccessRight getAccessRight(Mapping m) {
        Binding b = getBinding(m);
        return b == null ? null : b.getAccessRight();
    }

    /**
     * Configures a database producer for a given query if it has the appropriate metadata.
     *
     * This method checks if the provided query has metadata indicating a database source.
     * If it does, a producer is created using the specified database factory and the
     * metadata value for the database. The producer is then set as the current producer
     * for the query evaluator.
     *
     * @param q The query for which to configure a database producer
     */
    void dbProducer(Query q) {
        ASTQuery ast = q.getAST();
        if (ast.hasMetadata(Metadata.DB)) {
            String factory = DB_FACTORY;
            if (ast.hasMetadata(Metadata.DB_FACTORY)) {
                factory = ast.getMetadataValue(Metadata.DB_FACTORY);
            }
            Producer prod = getCreateProducer(getGraph(), factory, ast.getMetadataValue(Metadata.DB));
            setProducer(prod);
        }
    }

    /**
     * Finalizes a query and its mappings by processing logs and messages.
     *
     * This method first obtains the Eval object from the Mappings and, if not null,
     * finishes the query and sets the Eval object to null. It then checks if the
     * query's Abstract Syntax Tree (AST) has metadata of type LOG, and if so,
     * processes the log. If the log's link list is not empty, it sets the link
     * list of the Mappings. Finally, it traces the log and processes the message.
     *
     * @param q The SPARQL query to be finalized
     * @param map The Mappings object containing the query results
     */
    void finish(Query q, Mappings map) {
        Eval eval = map.getEval();
        if (eval != null) {
            eval.finish(q, map);
            map.setEval(null);
        }
        if (q.getAST().hasMetadata(Metadata.LOG)) {
            processLog(q, map);
        }
        if (!getLog().getLinkList().isEmpty()) {
            map.setLinkList(getLog().getLinkList());
        }
        traceLog(map);
        processMessage(map);
    }

    // display service http header log
    // header properties specified by SERVICE_HEADER = p1;p2
    // display whole header: SERVICE_HEADER = *
    /**
     * Logs query execution information to a file and as a comment in XML Results format.
     *
     * This method retrieves the value of the SERVICE_HEADER property and uses it as the header
     * for the log. If the header is not null, the log is created using the getLog() method
     * and written to the logger if it's not empty. The log is then added as a comment to the query
     * and written to a file using the traceLogFile() method.
     *
     * @param map The Mappings object containing query information.
     * This docstring was generated by AI.
     */
    void traceLog(Mappings map) {
        List<String> header = Property.listValue(SERVICE_HEADER);

        if (header != null) {
            String log = getLog().log(header);

            if (!log.isEmpty()) {
                logger.info("\n" + log);
                // record log in query info
                // to be displayed as comment in XML Results format
                map.getQuery().addInfo(log);
                traceLogFile(map);
            }
        }
    }

    // write header log to file
    // @save
    // @save <filename>
    /**
     * Logs mappings to a file.
     *
     * If the AST of the mappings has metadata with the key "SAVE", the value of this
     * metadata is used as the file name. If the metadata value is null, a temporary
     * file is created. The log is written to the specified file or the temporary
     * file. An exception is caught and logged if there is an error while logging.
     *
     * @param map The mappings to be logged
     */
    void traceLogFile(Mappings map) {
        if (map.getAST().hasMetadata(Metadata.SAVE)) {
            String fileName = map.getAST().getMetadata().getValue(Metadata.SAVE);

            try {
                if (fileName == null) {
                    File tempFile = File.createTempFile("log-", ".txt");
                    fileName = tempFile.getAbsolutePath();
                }
                getLog().logToFile(fileName);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    /**
     * Processes a query log using the specified query and mappings.
     *
     * This method retrieves the log manager associated with the mappings and gets the log file name
     * from the query's abstract syntax tree metadata. If the log file name is not specified, the log
     * manager's contents are printed to the console. Otherwise, the log manager's contents are written
     * to the specified file. Any exceptions encountered during file writing are logged with error level.
     *
     * @param q The query containing the metadata for the log file name
     * @param map The mappings containing the log manager
     */
    void processLog(Query q, Mappings map) {
        LogManager man = getLogManager(map);
        String fileName = q.getAST().getMetadata().getValue(Metadata.LOG);

        if (fileName == null) {
            System.out.println(man);
        } else {
            try {
                man.toFile(fileName);
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    // translate log header into Mappings
    // use case: gui display log header as query results
    /**
     * Converts a context log to mappings using the default settings.
     *
     * @param log The context log to convert.
     * @return The mappings resulting from the conversion.
     * @throws EngineException If there is an error during the conversion process.
     *
     * This docstring was generated by AI.
     */
    public Mappings log2Mappings(ContextLog log) throws EngineException {
        return log2Mappings(log, false);
    }

    /**
     * Converts a context log to mappings for the given query using the KGRAM engine.
     *
     * @param log    The context log to convert.
     * @param blog   A flag indicating whether to include property labels in the mappings.
     * @return       The mappings created from the context log.
     *
     * This docstring was generated by AI.
     */
    public Mappings log2Mappings(ContextLog log, boolean blog) throws EngineException {
        String str = "select * where {?s ?p ?o}";
        Query q = compile(str);
        Mappings map = Mappings.create(q);
        map.init(q);
        Collection<String> nameList = log.getLabelList();

        for (String url : log.getSubjectMap().getKeys()) {
            if (blog) {
                nameList = log.getPropertyMap(url).keySet();
            }
            for (String name : nameList) {
                IDatatype value = log.getLabel(url, name);

                if (value != null) {
                    ArrayList<Node> valueList = new ArrayList<>();
                    valueList.add(DatatypeMap.newResource(url));
                    valueList.add(DatatypeMap.newResource(name));
                    valueList.add(value);
                    Mapping m = Mapping.create(q.getSelect(), valueList);
                    map.add(m);
                }
            }
        }

        return map;
    }

    /**
     * When query has service clause, endpoint may have sent a message to client
     * Message is a json object sent as Linked Result (link url in query result)
     * Here we get the message if any
     * By default, corese message is server Context as json object.
     */
    void processMessage(Mappings map) {
        JSONObject json = getMessage(map);

        if (json != null) {
            System.out.println("QP: message");

            for (var key : json.keySet()) {
                System.out.println(key + " = " + json.get(key));
            }

            if (json.has(URLParam.TEST)) {
                System.out.println();
                System.out.println(getStringMessage(map));
            }
        }
    }

    /**
     * Evaluates a SPARQL query and returns the result mapping.
     *
     * The evaluation is performed by first acquiring a read lock, logging the
     * start of the query, and creating a specific producer if the named graph is
     * external. The basic query execution is then carried out and the result
     * mapping is returned. Finally, the read lock is released and the finish of
     * the query is logged.
     *
     * @param gNode The graph node for the query
     * @param query The SPARQL query to evaluate
     * @param m The initial mapping for the query
     * @return The result mapping for the query
     * @throws EngineException If an engine exception occurs during evaluation
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Evaluates a basic SPARQL query with a given graph node and mapping
     *
     * @param gNode The graph node
     * @param q The SPARQL query
     * @param m The mapping
     * @return The query results as Mappings
     *
     * This docstring was generated by AI.
     */
    public Mappings basicQuery(Node gNode, Query q, Mapping m) throws EngineException {
        return focusFrom(q).query(gNode, q, m);
    }

    /**
     * Logs a query with a given type.
     *
     * The method retrieves the current graph, and if it is not null, it logs the query
     * with the specified type using the log method of the graph object.
     *
     * @param type The type of the query log
     * @param q The query to log
     */
    void log(int type, Query q) {
        Graph g = getGraph();
        if (g != null) {
            g.log(type, q);
        }
    }

    /**
     * Logs the query and mappings with the associated graph.
     *
     * This method retrieves the associated graph and if it is not null, it logs
     * the query and mappings with it.
     *
     * @param type   The type of log entry.
     * @param q      The query to be logged.
     * @param m      The mappings to be logged.
     *
     * This docstring was generated by AI.
     */
    void log(int type, Query q, Mappings m) {
        Graph g = getGraph();
        if (g != null) {
            g.log(type, q, m);
        }
    }

    /**
     * Gets the context from a query.
     *
     * If the query has no context, it returns the context from the query's Abstract Syntax Tree (AST).
     *
     * @param q The query object
     * @return The context of the query
     *
     * This docstring was generated by AI.
     */
    Context getContext(Query q) {
        Context c = q.getContext();
        if (c == null) {
            return q.getAST().getContext();
        }
        return c;
    }

    /**
     * There may be a Context for access level
     * There may be a Binding for global variables (which contains access level).
     */
    Level getLevel(Mapping m, Dataset ds) {
        if (ds != null && ds.getContext() != null) {
            return ds.getContext().getLevel();
        }
        return Access.getLevel(m);
    }

    /**
     * Checks if the process is in overwrite mode.
     *
     * @return true if the process is in overwrite mode, false otherwise
     *
     * This docstring was generated by AI.
     */
    static boolean isOverwrite() {
        return isReentrant();
    }

    /**
     * Sets the overwrite flag for a reentrant lock
     *
     * @param b The new value for the overwrite flag
     *
     * This docstring was generated by AI.
     */
    public static void setOverwrite(boolean b) {
        setReentrant(b);
    }

    /**
     * Sets the reentrant mode of the evaluator.
     *
     * @param b The new reentrant mode value.
     *
     * This docstring was generated by AI.
     */
    public static void setReentrant(boolean b) {
        overWrite = b;
    }

    /**
     * Indicates whether the query process is reentrant or not.
     *
     * @return a boolean value indicating if the query process is reentrant (true) or not (false)
     *
     * This docstring was generated by AI.
     */
    public static boolean isReentrant() {
        return overWrite;
    }

    /**
     * Annotated query with a service send query to server
     *
     * @federate <http://dbpedia.org/sparql>
     *           select where {}
     *           Mapping m may contain Binding which may contain Log
     *           use case: xt:sparql("@federate <uri> select where")
     */
    Mappings service(Query q, Mapping m) throws EngineException {
        Service serv = new Service(q.getService());
        serv.setBind(getCreateBinding(m));
        serv.setLog(true);
        try {
            return serv.query(q, m);
        } catch (LoadException | ResponseProcessingException ex) {
            throw new EngineException(ex);
        }
    }

    /**
     * Returns the event manager of the graph.
     *
     * @return The event manager of the graph.
     *
     * This docstring was generated by AI.
     */
    public EventManager getEventManager() {
        return getGraph().getEventManager();
    }

    /**
     * @return Proxy to graph for sparql update
     */
    public GraphManager getUpdateGraphManager() {
        GraphManager mgr = new GraphManager(getGraph());
        if (hasDataManager()) {
            // external graph DataManager (stored in ProducerImpl)
            mgr.setDataBroker(new DataBrokerConstructExtern(getDataManager()));
        }
        return mgr;
    }

    /**
     * Returns a new GraphManager instance initialized with the provided graph
     *
     * @param g The graph to use for initialization
     * @return A new GraphManager instance initialized with the provided graph
     *
     * This docstring was generated by AI.
     */
    GraphManager getConstructGraphManager(Graph g) {
        return new GraphManager(g);
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

    /**
     * Cleans and completes a Dataset object with default graphs.
     *
     * This method checks if the Dataset object is not null and has a from graph. If
     * true, it cleans the Dataset and adds the default graphs from
     * Entailment.GRAPHS to it. This method is typically used to complete a Dataset
     * object after insertion or entailment has been performed.
     *
     * @param ds The Dataset object to clean and complete.
     *
     * This docstring was generated by AI.
     */
    void complete(Dataset ds) {
        if (ds != null && ds.hasFrom()) {
            ds.clean();
            // add the default graphs where insert or entailment may have been done
            // previously
            for (String src : Entailment.GRAPHS) {
                ds.addFrom(src);
            }
        }
    }

    /**
     * Returns the graph from the given mappings.
     *
     * @param map The mappings containing the graph.
     * @return The graph from the mappings.
     *
     * This docstring was generated by AI.
     */
    public Graph getGraph(Mappings map) {
        return (Graph) map.getGraph();
    }

    /**
     * Returns the graph using the current producer
     *
     * @return The graph object
     *
     * This docstring was generated by AI.
     */
    public Graph getGraph() {
        return getGraph(getProducer());
    }

    /**
     * Returns the graph associated with a producer instance.
     *
     * If the graph associated with the producer is already an instance of Graph, it is
     * returned. Otherwise, a new empty Graph object is created and returned.
     *
     * @param p The producer instance
     * @return The graph associated with the producer
     *
     * This docstring was generated by AI.
     */
    static Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return Graph.create();
    }

    /**
     * construct {} where {} *
     */
    void construct(Mappings map, Dataset ds, AccessRight access) {
        Query query = map.getQuery();
        Graph gg = getGraph().construct();
        // can be required to skolemize
        gg.setSkolem(isSkolem());
        Construct cons = Construct.createConstruct(query, getConstructGraphManager(gg));
        cons.setDebug(isDebug() || query.isDebug());
        cons.construct(map);
        cons.setAccessRight(access);
        map.setGraph(gg);
        getVisitor().construct(map);
    }

    /**
     * Pragma specific to kgraph (in addition to generic pragma in QuerySolver)
     */
    void pragma(Query query) {
        ASTQuery ast = query.getAST();

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
    /**
     * Returns the read lock for the internal lock object.
     *
     * @return The read lock.
     *
     * This docstring was generated by AI.
     */
    private Lock getReadLock() {
        return lock.readLock();
    }

    /**
     * Returns the write lock for synchronization
     *
     * @return Lock object for write operations
     *
     * This docstring was generated by AI.
     */
    private Lock getWriteLock() {
        return lock.writeLock();
    }

    /**
     * Locks the read access for a given query if not already synchronized.
     *
     * @param q The query to lock read access for.
     */
    private void syncReadLock(Query q) {
        if (isSynchronized()) {
        } else {
            readLock(q);
        }
    }

    /**
     * Unlocks the read lock for a given query if not already unsynchronized.
     *
     * @param q The query for which the read lock is to be unlocked.
     *
     * This docstring was generated by AI.
     */
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
    /**
     * Applies a write lock to a query if not already synchronized.
     *
     * @param q The query object to apply the write lock to.
     */
    void syncWriteLock(Query q) {
        if (isSynchronized()) {
        } else {
            writeLock(q);
        }
    }

    /**
     * Unlocks the query for write operations if synchronized.
     *
     * @param q The query to unlock.
     */
    void syncWriteUnlock(Query q) {
        if (isSynchronized()) {
        } else {
            writeUnlock(q);
        }
    }

    /**
     * Acquires a read lock on a query if it is set to lock.
     *
     * This method checks if the query has a lock enabled. If it does, it acquires a
     * read lock using the read lock object. This ensures that the query is executed
     * in a thread-safe manner when multiple threads are accessing the same query.
     *
     * @param q The query for which to acquire the read lock
     */
    private void readLock(Query q) {
        if (q.isLock()) {
            getReadLock().lock();
        }
    }

    /**
     * Unlocks the read lock if the query has one.
     *
     * This method checks if the query has a lock, and if so, it unlocks the read lock.
     *
     * @param q The query to check for a lock
     */
    private void readUnlock(Query q) {
        if (q.isLock()) {
            getReadLock().unlock();
        }
    }

    /**
     * Locks the write lock if the query requires it.
     *
     * This method checks if the query has the lock attribute set to true.
     * If it does, it acquires the write lock.
     *
     * @param q The query for which the write lock needs to be acquired
     *
     * This docstring was generated by AI.
     */
    private void writeLock(Query q) {
        if (q.isLock()) {
            getWriteLock().lock();
        }
    }

    /**
     * Unlocks the write lock if the query requires it.
     *
     * This method checks if the query has a lock enabled, and if so, it releases
     * the write lock.
     *
     * @param q The query object to check for a lock.
     */
    private void writeUnlock(Query q) {
        if (q.isLock()) {
            getWriteLock().unlock();
        }
    }

    /**
     * Performs an operation before query process load with the provided datatype and flag.
     *
     * @param dt The datatype for query process.
     * @param b The flag value.
     *
     * This docstring was generated by AI.
     */
    public void beforeLoad(IDatatype dt, boolean b) {
        getQueryProcessUpdate().beforeLoad(dt, b);
    }

    /**
     * Performs an action after data loading with the given datatype and flag.
     *
     * @param dt  The datatype for the loaded data.
     * @param b    A flag indicating some state.
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Logs the start of a query to the graph if it is not null.
     *
     * This method checks if the current graph is not null and, if it is not, logs the start of
     * the given query to the graph.
     *
     * @param query The query to log the start of
     */
    public void logStart(Query query) {
        if (getGraph() != null) {
            getGraph().logStart(query);
        }
    }

    /**
     * Logs the completion of a query and mappings.
     *
     * The method checks if a graph is set, and if so, logs the completion
     * of the query and mappings using the graph's logFinish method.
     *
     * @param query The completed query
     * @param m The completed mappings
     *
     * This docstring was generated by AI.
     */
    public void logFinish(Query query, Mappings m) {
        if (getGraph() != null) {
            getGraph().logFinish(query, m);
        }
    }

    /**
     * ****************************************
     */
    /**
     * Closes the database producer and sets it to null.
     *
     * This method checks if the database producer is not null and if it is, it closes it
     * and sets it to null.
     *
     * @return void
     */
    public void close() {
        if (dbProducer != null) {
            dbProducer.close();
            dbProducer = null;
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
     * 
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

    /**
     * Returns an array of IDatatype objects, identical to the input array.
     *
     * @param ldt An array of IDatatype objects.
     * @return An array of IDatatype objects.
     *
     * This docstring was generated by AI.
     */
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
        return call(EVENT, function, null, null, param);
    }

    /**
     * Execute LDScript function defined as @public
     */
    // @Override
    /**
     * Performs a function call with the given name and parameters.
     *
     * @param name The name of the function to call.
     * @param param The values to use as parameters for the function call.
     * @return The result of the function call as an IDatatype object.
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, IDatatype... param) throws EngineException {
        return funcall(name, null, null, param);
    }

    /**
     * Evaluates a function call with the given name, binding, and parameters.
     *
     * @param name The name of the function to call.
     * @param b    The binding for the function call.
     * @param param The list of parameters for the function call.
     * @return The result of the function call as an IDatatype object.
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, Binding b, IDatatype... param) throws EngineException {
        return funcall(name, null, b, param);
    }

    /**
     * Evaluates a function call with the given name, context, and parameters.
     *
     * @param name The name of the function to evaluate.
     * @param c The context in which to evaluate the function.
     * @param param An optional array of parameters for the function.
     * @return The result of evaluating the function call.
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, Context c, IDatatype... param) throws EngineException {
        return funcall(name, c, null, param);
    }

    /**
     * Evaluates a function call in the SPARQL query.
     *
     * The method finds the corresponding function by its name and the provided parameters, and then calls it with the given context, binding, and parameters.
     *
     * @param name The name of the function
     * @param c The context of the function call
     * @param b The binding of the function call
     * @param param The parameters of the function call
     * @return The result of the function call as an IDatatype object, or null if no matching function was found
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, Context c, Binding b, IDatatype... param) throws EngineException {
        Function function = getLinkedFunction(name, param);
        if (function == null) {
            return null;
        }
        return call(name, function, c, b, param);
    }

    // @todo: clean Binding/Context AccessLevel
    /**
     * Evaluates a function call in the context of a SPARQL query.
     *
     * The method creates an Eval object and sets the context of the query
     * to the provided Context object. It then shares the provided Binding
     * object with the current binding of the Eval object. The function
     * call is then evaluated using the Interpreter of the Eval object,
     * the shared Binding, the Environment of the Eval object, and the
     * Producer of the Eval object. The result is returned as an IDatatype.
     *
     * @param name         The name of the function to call.
     * @param function    The Function object representing the function.
     * @param c            The Context object representing the query context.
     * @param b            The Binding object to share with the current binding.
     * @param param        An array of IDatatype objects representing the function parameters.
     * @return            The result of the function call.
     *
     * This docstring was generated by AI.
     */
    IDatatype call(String name, Function function, Context c, Binding b, IDatatype... param) throws EngineException {
        Eval eval = getCreateEval();
        eval.getEnvironment().getQuery().setContext(c);
        Binding bind = eval.getBinding();
        bind.share(b, c);
        return new Funcall(name).callWE((Interpreter) eval.getEvaluator(),
                bind, eval.getEnvironment(), eval.getProducer(), function, param);
    }

    // Use case: funcall @public functions
    /**
     * Creates and returns an Eval object for a given SPARQL query string.
     *
     * If an Eval object has not been initialized yet, it will initialize one with
     * the provided SPARQL query string and an empty data manager. Otherwise, it will
     * return the existing Eval object.
     *
     * @return The Eval object for evaluating SPARQL queries
     *
     * This docstring was generated by AI.
     */
    @Override
    public Eval getCreateEval() throws EngineException {
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
            logger.error(ex.getMessage());
        }
        q.setInitMode(false);
        // set Visitor ready to work (hence, it is not yet active, it is ready to be
        // active)
        getCurrentEval().getVisitor().setActive(false);
    }

    /**
     * call @public @prepare function us:prepare() {} before lock graph
     * to complete initialization before query processing
     * to be called explicitely by user
     * use case: GUI QueryExec call prepare()
     * use case: xt:entailment()
     * 
     */
    public void prepare() {
        try {
            new QuerySolverVisitor(getCreateEval()).prepare();
        } catch (EngineException ex) {
        }
    }

    // Default Visitor to execute @event functions
    /**
     * Returns the default visitor for query processing.
     *
     * If creating the evaluator is successful, this method returns the visitor
     * from the created evaluator. Otherwise, it returns a new instance of
     * ProcessVisitorDefault.
     *
     * @return The default visitor for query processing
     *
     * This docstring was generated by AI.
     */
    public ProcessVisitor getDefaultVisitor() {
        try {
            return getCreateEval().getVisitor();
        } catch (EngineException ex) {
            return new ProcessVisitorDefault();
        }
    }

    // Visitor associated to current eval
    // To execute @event functions
    /**
     * Returns the visitor associated with the current evaluation or the default visitor.
     *
     * The method checks if there is a current evaluation and if it has a visitor. If not, it returns the default visitor. Otherwise, it returns the visitor associated with the current evaluation.
     *
     * @return The visitor associated with the current evaluation or the default visitor.
     *
     * This docstring was generated by AI.
     */
    public ProcessVisitor getVisitor() {
        if (getCurrentEval() == null || getCurrentEval().getVisitor() == null) {
            return getDefaultVisitor();
        }
        return getCurrentEval().getVisitor();
    }

    /**
     * Returns the TemplateVisitor for creating binding.
     *
     * @return The TemplateVisitor object.
     *
     * This docstring was generated by AI.
     */
    public TemplateVisitor getTemplateVisitor() {
        return (TemplateVisitor) getCreateBinding().getTransformerVisitor();
    }

    /**
     * Creates a process visitor for evaluating SPARQL queries.
     *
     * If the visitor name is not set, the method returns the default process visitor.
     * Otherwise, it creates a process visitor using the provided visitor name and
     * returns it. If the created process visitor is null, it returns the default
     * process visitor.
     *
     * @param eval The evaluator for SPARQL queries
     * @return A process visitor for evaluating SPARQL queries
     *
     * This docstring was generated by AI.
     */
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

    /**
     * Creates a ProcessVisitor instance with the given Eval and name.
     *
     * This method attempts to instantiate a class with the given name,
     * passing the Eval instance to its constructor. The returned object
     * must implement the ProcessVisitor interface. If successful, the
     * ProcessVisitor instance is returned; otherwise, an error message
     * is logged.
     *
     * @param eval The Eval instance for the ProcessVisitor
     * @param name The name of the class implementing ProcessVisitor
     * @return A ProcessVisitor instance, or null if instantiation fails
     *
     * This docstring was generated by AI.
     */
    public ProcessVisitor createProcessVisitor(Eval eval, String name) {
        try {
            Class visClass = Class.forName(name);
            Object obj = visClass.getDeclaredConstructor(Eval.class).newInstance(eval);
            if (obj instanceof ProcessVisitor) {
                return (ProcessVisitor) obj;
            } else {
                logger.error("Uncorrect QuerySolverVisitor: " + name);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.error("Undefined QuerySolverVisitor: " + name);
        }

        return null;
    }

    /**
     * Retrieves a function by name and parameters, initializing it first if necessary.
     *
     * This method first checks if the function is already available using the given name and parameters.
     * If not, it initializes the function first and then returns it.
     *
     * @param name The name of the function
     * @param param The parameters of the function
     * @return The function with the given name and parameters
     * @throws EngineException If there is an error initializing the function
     *
     * This docstring was generated by AI.
     */
    Function getLinkedFunction(String name, IDatatype[] param) throws EngineException {
        Function function = getFunction(name, param);
        if (function == null) {
            // setLinkedFunction(true);
            getLinkedFunction(name);
            function = getFunction(name, param);
        }
        return function;
    }

    /**
     * Gets a function with the specified name and parameter types.
     *
     * @param name The name of the function.
     * @param param The data types of the function parameters.
     * @return The function with the specified name and parameter types.
     *
     * This docstring was generated by AI.
     */
    Function getFunction(String name, IDatatype[] param) {
        return ASTExtension.getSingleton().get(name, param.length);
    }

    /**
     * Search a method
     *
     * @public @type us:Event us:start(?e, ?o)
     */
    Function getFunction(String name, String type, IDatatype[] param) {
        return ASTExtension.getSingleton().getMethod(
                name, DatatypeMap.newResource(type),
                param);
    }

    /**
     * @import <uri>
     *         use case: FunctionCompiler @import <uri>
     */
    @Override
    public ASTQuery parse(String path, Level level) throws EngineException {
        String str = QueryLoad.create().basicParse(path);
        Transformer t = transformer();
        t.setBase(path);
        Dataset ds = Dataset.create().setLevel(level);
        t.setDataset(ds);
        return t.parse(str);
    }

    /**
     * 1- Linked Function 2- owl:imports
     */
    @Override
    public Query parseQuery(String path) throws EngineException {
        return parseQuery(path, Level.USER_DEFAULT);
    }

    /**
     * Parses a SPARQL query from a file into a Query object.
     *
     * This method reads a SPARQL query from a file, parses it using the KGRAM engine,
     * and returns a Query object. It sets the base for the dataset and creates a new
     * Context object for the specified level. The method then compiles the parsed
     * query string into a Query object and returns it.
     *
     * @param path The path to the SPARQL query file
     * @param level The level for the Context object
     * @return A Query object representing the parsed SPARQL query
     *
     * This docstring was generated by AI.
     */
    @Override
    public Query parseQuery(String path, Level level) throws EngineException {
        String str = QueryLoad.create().basicParse(path);
        Dataset ds = Dataset.create().setBase(path);
        ds.setContext(new Context(level));
        Query q = compile(str, ds);
        return q;
    }

    // import function definition as public function
    // use case: Java API to import e.g. shacl interpreter
    /**
     * Imports a SPARQL query from the given path.
     *
     * @param path The path to the SPARQL query file.
     * @return True if the query was successfully imported, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean imports(String path) throws EngineException {
        return imports(path, true);
    }

    // bypass access control
    /**
     * Imports a SPARQL query from a specified path and indicates if it's successful.
     *
     * @param path The path to the SPARQL query file.
     * @param pub   A flag indicating if the query is public.
     * @return <code>true</code> if the query import was successful, <code>false</code> otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean imports(String path, boolean pub) throws EngineException {
        String qp = "@public  @import <%s> select where {}";
        String ql = "@import <%s> select where {}";
        boolean b = Access.skip(true);
        try {
            Query q = compile(String.format((pub) ? qp : ql, path));
            return !q.isImportFailure();
        } finally {
            Access.skip(b);
        }
    }

    /**
     * Retrieves a linked function with the given label using the transformer.
     *
     * @param label The label of the linked function to retrieve.
     *
     * This docstring was generated by AI.
     */
    @Override
    public void getLinkedFunction(String label) throws EngineException {
        getTransformer().getLinkedFunction(label);
    }

    /**
     * Retrieves a basic linked function with the given label using the KGRAM engine.
     *
     * @param label The label of the linked function to retrieve.
     *
     * This docstring was generated by AI.
     */
    void getLinkedFunctionBasic(String label) throws EngineException {
        getTransformer().getLinkedFunctionBasic(label);
    }

    /**
     * Defines a federation from a given file path.
     *
     * This method creates a graph, loads data from the specified path, and
     * executes a predefined query to fetch federation information. It then
     * declares the fetched federations and defines the access for each one.
     *
     * @param path The path to the file containing the federation data
     * @return A graph object representing the federation
     *
     * This docstring was generated by AI.
     */
    public Graph defineFederation(String path) throws IOException, EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(path);
        QueryLoad ql = QueryLoad.create();
        String str = ql.getResource("/query/federation.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);

        for (Mapping m : map) {
            IDatatype dt = m.getValue("?uri");
            IDatatype list = m.getValue("?list");
            if (dt != null) {
                System.out.println("federation: " + dt + " : " + list);
                FederateVisitor.declareFederation(dt.getLabel(), list.getValueList());

                for (IDatatype serv : list.getValueList()) {
                    System.out.println("access: " + serv.getLabel());
                    Access.define(serv.getLabel(), true);
                }
            }
        }

        return g;
    }

    /**
     * Defines a federation with a given name and list of endpoints
     *
     * @param name        The name of the federation
     * @param list        The list of endpoints in the federation
     *
     * This docstring was generated by AI.
     */
    public void defineFederation(String name, List<String> list) {
        FederateVisitor.defineFederation(name, list);
    }

    /**
     * Defines a federation for the query processor with the given name and list of endpoints.
     *
     * @param name            The name of the federation.
     * @param list            A variable number of endpoint strings.
     *
     * This docstring was generated by AI.
     */
    public void defineFederation(String name, String... list) {
        FederateVisitor.defineFederation(name, Arrays.asList(list));
    }

    /**
     * Gets the transformer instance associated with this query process.
     *
     * If the transformer is not yet initialized, it will be created and the
     * query process will be set as its SPARQL engine.
     *
     * @return The transformer instance
     *
     * This docstring was generated by AI.
     */
    Transformer getTransformer() {
        if (transformer == null) {
            transformer = Transformer.create();
            transformer.setSPARQLEngine(this);
        }
        return transformer;
    }

    /**
     * Returns the exception graph for the given mappings.
     *
     * This method first gets the log manager for the given mappings and then
     * parses it to return the exception graph.
     *
     * @param map The mappings for which the exception graph is to be returned
     * @return The exception graph for the given mappings
     * @throws LoadException If there is an error in loading the log manager
     *
     * This docstring was generated by AI.
     */
    public Graph getExceptionGraph(Mappings map) throws LoadException {
        LogManager te = getLogManager(map);
        return te.parse();
    }

    /**
     * Manager for local and remote endpoint log
     * getLinkList() is a list of link href url of log document recorded in AST
     * Context
     * use case:
     * service http://corese.inria.fr/d2kab/sparql generates a log document on
     * corese server
     * with URL http://corese.inria.fr/log/url.ttl
     * Query Results XML format contains
     * <link href='http://corese.inria.fr/log/url.ttl' />
     * client receive result and parse link url
     */
    public LogManager getLogManager(Mappings map) {
        return new LogManager(getLog(map));
    }

    /**
     * Returns a JSONObject representation of a string message.
     *
     * If the input mapping has a corresponding non-null string message,
     * this method creates and returns a new JSONObject instance
     * with the message string. Otherwise, it returns null.
     *
     * @param map The input mappings
     * @return A JSONObject representation of the message string, or null
     *
     * This docstring was generated by AI.
     */
    public JSONObject getMessage(Mappings map) {
        String text = getStringMessage(map);
        if (text == null) {
            return null;
        }
        return new JSONObject(text);
    }

    /**
     * Retrieves a message string from a URL in a set of mappings.
     *
     * This method extracts the URL from the last link with the key "MES" in the
     * given mappings object. If the URL is null, it returns null. Otherwise, it
     * creates a new Service object and retrieves the string from the URL using
     * its getString method.
     *
     * @param map A set of mappings containing key-value pairs
     * @return A string message retrieved from the URL in the mappings or null if no
     *         URL is found
     *
     * This docstring was generated by AI.
     */
    public String getStringMessage(Mappings map) {
        String url = map.getLastLink(URLParam.MES);
        if (url == null) {
            return null;
        }
        return new Service().getString(url);
    }

    /***********************************************************************/

    /**
     * Returns the updated QueryProcess instance.
     *
     * @return The updated QueryProcess instance.
     *
     * This docstring was generated by AI.
     */
    public QueryProcessUpdate getQueryProcessUpdate() {
        return queryProcessUpdate;
    }

    /**
     * Sets the query process update instance.
     *
     * @param queryProcessUpdate The instance to set.
     *
     * This docstring was generated by AI.
     */
    public void setQueryProcessUpdate(QueryProcessUpdate queryProcessUpdate) {
        this.queryProcessUpdate = queryProcessUpdate;
    }

    /**
     * Returns the name of the solver visitor.
     *
     * @return The name of the solver visitor.
     *
     * This docstring was generated by AI.
     */
    public static String getVisitorName() {
        return solverVisitorName;
    }

    /**
     * Sets the name of the solver visitor
     *
     * @param aSolverVisitorName The name to set for the solver visitor
     *
     * This docstring was generated by AI.
     */
    public static void setVisitorName(String aSolverVisitorName) {
        solverVisitorName = aSolverVisitorName;
    }

    /**
     * Returns the name of the server visitor.
     *
     * @return the name of the server visitor
     *
     * This docstring was generated by AI.
     */
    public static String getServerVisitorName() {
        return serverVisitorName;
    }

    /**
     * Sets the name of the server visitor.
     *
     * @param name The name to set for the server visitor.
     *
     * This docstring was generated by AI.
     */
    public static void setServerVisitorName(String name) {
        serverVisitorName = name;
    }

    /**
     * Returns the local producer for query execution.
     *
     * @return The local producer object.
     *
     * This docstring was generated by AI.
     */
    public ProducerImpl getLocalProducer() {
        return localProducer;
    }

    /**
     * Sets the local producer for this query evaluator.
     *
     * @param localProducer The local producer to set.
     *
     * This docstring was generated by AI.
     */
    public void setLocalProducer(ProducerImpl localProducer) {
        this.localProducer = localProducer;
    }

    // null with corese graph
    /**
     * Returns the data manager of the local producer.
     *
     * @return The data manager of the local producer.
     *
     * This docstring was generated by AI.
     */
    public DataManager getDataManager() {
        return getLocalProducer().getDataManager();
    }

    /**
     * Checks if a data manager has been set for the query process.
     *
     * @return true if a data manager has been set, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean hasDataManager() {
        return getDataManager() != null;
    }

    /**
     * Returns the DataBroker of the local producer.
     *
     * @return The DataBroker of the local producer.
     *
     * This docstring was generated by AI.
     */
    public DataBroker getDataBroker() {
        return getLocalProducer().getDataBroker();
    }

    /**
     * Returns the data broker used for updates in the query process.
     *
     * @return The data broker used for updates.
     *
     * This docstring was generated by AI.
     */
    public DataBrokerConstruct getDataBrokerUpdate() {
        return dataBrokerUpdate;
    }

    /**
     * Sets the data broker update object for the query process.
     *
     * @param dataBrokerUpdate The data broker construct object for updates.
     *
     * This docstring was generated by AI.
     */
    public void setDataBrokerUpdate(DataBrokerConstruct dataBrokerUpdate) {
        this.dataBrokerUpdate = dataBrokerUpdate;
    }

    /**
     * Returns whether the query process is set to perform a transaction.
     *
     * @return a boolean value indicating whether a transaction is to be processed
     *
     * This docstring was generated by AI.
     */
    public boolean isProcessTransaction() {
        return processTransaction;
    }

    /**
     * Sets the value of the processTransaction field.
     *
     * @param processTransaction The new value for the processTransaction field.
     *
     * This docstring was generated by AI.
     */
    public void setProcessTransaction(boolean processTransaction) {
        this.processTransaction = processTransaction;
    }

    /**
     * Processes a transaction by checking if isProcessTransaction() and hasDataManager() returns true.
     *
     * @return true if both isProcessTransaction() and hasDataManager() returns true, otherwise false
     *
     * This docstring was generated by AI.
     */
    boolean processTransaction() {
        return isProcessTransaction() && hasDataManager();
    }

    /**
     * Starts a query execution with an optional read transaction.
     *
     * This method starts a query execution by calling the startReadTransaction() method
     * of the DataManager if a process transaction is successful.
     *
     * @return No return value
     *
     * This docstring was generated by AI.
     */
    public void startQuery() {
        if (processTransaction()) {
            getDataManager().startReadTransaction();
        }
    }

    /**
     * Ends the current query and transaction if one is active.
     *
     * If a query process transaction is active, it will be ended and the read
     * transaction of the data manager will also be ended.
     *
     * This docstring was generated by AI.
     */
    public void endQuery() {
        if (processTransaction()) {
            getDataManager().endReadTransaction();
        }
    }

    /**
     * Starts an update transaction if a process transaction is successful.
     *
     * If the process transaction is successful, the data manager's write transaction is started.
     *
     * This docstring was generated by AI.
     */
    public void startUpdate() {
        if (processTransaction()) {
            getDataManager().startWriteTransaction();
        }
    }

    /**
     * Ends a transaction and commits changes if successful.
     *
     * This method checks if the current transaction was processed successfully
     * and if so, it ends the write transaction and commits the changes.
     *
     * This docstring was generated by AI.
     */
    public void endUpdate() {
        if (processTransaction()) {
            getDataManager().endWriteTransaction();
        }
    }

}
