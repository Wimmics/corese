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
     * This docstring was generated by AI.
     */
    public QueryProcess() {
    }

    /**
     * QueryProcess class constructor
     *
     * This constructor initializes a new instance of the QueryProcess class with the provided Producer, Interpreter, and Matcher objects.
     *
     * @param p The Producer object used for producing RDF data.
     * @param e The Interpreter object used for interpreting SPARQL queries.
     * @param m The Matcher object used for matching query patterns against RDF data.
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
     * Completes the query process setup.
     *
     * This method initializes the provider and update object for the query process.
     *
     * @param provider The provider instance
     * @throws EngineException If there is an error setting the provider
     *
     * This docstring was generated by AI.
     */
    void complete() {
        // service
        set(ProviderImpl.create(this));
        setQueryProcessUpdate(new QueryProcessUpdate(this));
    }

    /**
     * Initializes locks and sorts the graph if necessary.
     *
     * This method first retrieves the graph and checks if sorting is enabled.
     * If sorting is enabled and a graph is present, it sets the sorter.
     * It then acquires a lock on the graph or creates a new reentrant lock
     * if no graph is present.
     *
     *
     * @since 1.0
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
     * Creates a new QueryProcess object with a default graph.
     *
     * @return A new QueryProcess object configured with a default graph.
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create() {
        return create(Graph.create());
    }

    /**
     * Creates a new QueryProcess instance with the given graph and default settings
     *
     * @param g The graph to be used
     * @return A new QueryProcess instance
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
     * Creates a new QueryProcess instance with a predefined graph and data manager.
     *
     * A new QueryProcess instance is created using the provided graph, and the data
     * manager is then defined for the created instance.
     *
     * @param g The graph to be used for the query process
     * @param dm The data manager to be associated with the query process
     * @return A new QueryProcess instance, with the provided graph and defined
     *         data manager
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess create(Graph g, DataManager dm) {
        QueryProcess exec = create(g);
        exec.defineDataManager(dm);
        return exec;
    }

    /**
     * Defines a data manager for the query processor.
     *
     * This method sets the data manager for the local producer, if it is not null.
     *
     * @param dm The data manager to be defined
     */
    public void defineDataManager(DataManager dm) {
        if (dm != null && getLocalProducer() != null) {
            getLocalProducer().defineDataManager(dm);
        }
    }

    // several Producer for several DataManager
    /**
     * Creates a new QueryProcess instance with a graph and optional data manager array.
     *
     * If a data manager array is provided, the data manager for the graph is set
     * to the first entry in the array. If no data manager array is provided,
     * the data manager for the graph remains unchanged.
     *
     * @param g The graph to be used for the QueryProcess instance
     * @param dmList Optional data manager array for setting the data manager for the graph
     * @return A new QueryProcess instance with the specified graph
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
     * This method defines a data manager for a given graph and adds it to the meta producer.
     * It also defines the local producer and matcher for each data manager in the list.
     *
     * @param g The graph object
     * @param dmList An array of data manager objects
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
     * Creates and configures a QueryProcess object with a ProducerImpl.
     *
     * This method first creates a ProducerImpl instance with the provided Graph,
     * then sets the match property according to the isMatch parameter.
     * A QueryProcess instance is then created with the ProducerImpl, and
     * its match property is also set.
     *
     * @param g The graph for creating the ProducerImpl
     * @param isMatch The match property value for ProducerImpl and QueryProcess
     * @return The configured QueryProcess instance
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
     * Returns a copy of the current QueryProcess object with the specified producer and matcher configuration.
     *
     * @param producer The producer to be used in the copied QueryProcess object.
     * @param match Whether to enable matching in the copied QueryProcess object.
     * @return A copy of the current QueryProcess object with the specified producer and matcher configuration.
     *
     * This docstring was generated by AI.
     */
    public QueryProcess copy() {
        return copy(getProducer(), isMatch());
    }

    /**
     * Creates a copy of a QueryProcess object with updated data manager.
     *
     * A copy of the QueryProcess object is created using the provided Producer
     * object. The data manager of the new object is then redefined with the
     * data manager obtained from the same Producer object.
     *
     * @param p The Producer object used to create the copy and define its data manager
     * @param isMatch A boolean value indicating whether the copied object should be used for matching or not
     * @return A new QueryProcess object with its data manager defined by the producer
     *
     * This docstring was generated by AI.
     */
    public static QueryProcess copy(Producer p, boolean isMatch) {
        QueryProcess exec = stdCreate(getGraph(p), isMatch);
        exec.defineDataManager(exec.getDataManager(p));
        return exec;
    }

    /**
     * Returns the data manager associated with the given producer if it is an instance of ProducerImpl.
     *
     * This method checks if the provided producer is an instance of ProducerImpl, and if so,
     * it returns the data manager associated with that producer. If the producer is not an
     * instance of ProducerImpl, the method returns null.
     *
     * @param p The producer object to retrieve the data manager from
     * @return The data manager associated with the producer if it is an instance of ProducerImpl, otherwise null
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
     * Creates a new QueryProcess instance with the given ProducerImpl.
     *
     * The method initializes a local matcher for the producer's graph and sets it if there is a local match.
     * Then, it creates a global matcher with relax mode and sets it regardless of the local match.
     * Finally, a new QueryProcess instance is created with the producer, an interpreter, and the matcher,
     * and the local producer is set in the new QueryProcess instance.
     *
     * @param p The ProducerImpl to associate with the new QueryProcess instance.
     * @return A new QueryProcess instance with the given ProducerImpl.
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
     * If the Producer is an instance of ProducerImpl, a new QueryProcess
     * instance will be created using that ProducerImpl. Otherwise,
     * a new QueryProcess instance will be created using an external Producer.
     *
     * @param p The Producer to use for creating the new QueryProcess instance
     * @return A new QueryProcess instance created using the given Producer
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
     * Creates a QueryProcess object with a given producer and interpreter.
     *
     * This method first creates a Matcher object with an empty graph, then sets
     * its mode to RELAX. A QueryProcess object is then created using the given
     * producer, a new interpreter, and the created matcher. The local producer
     * of the resulting QueryProcess object is set to a new ProducerImpl object
     * with an empty graph for compatibility reasons.
     *
     * @param p The producer used for query processing
     * @return A configured QueryProcess object
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
     * Creates a new instance of QueryProcess with the provided Producer, Interpreter, and Matcher.
     *
     * @param prod The producer object.
     * @param eval The interpreter object.
     * @param match The matcher object.
     * @return A new instance of QueryProcess.
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
     * Gets a producer for creating RDF data.
     *
     * If a database is specified, a producer is created and stored in a map for future use.
     * If no database is specified, the default producer is used or created if it doesn't exist.
     *
     * @param g The graph
     * @param factory The factory
     * @param db The database
     * @return A producer for creating RDF data
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
     * Creates a ProducerImpl object using the provided Graph, factory, and db.
     *
     * The method first checks if the db parameter is not null, in which case it sets a system property.
     * It then attempts to instantiate a new ProducerImpl object by dynamically loading the class specified
     * in the factory parameter and invoking its "create" method.
     * If this fails, it returns a new ProducerImpl object created using the provided Graph.
     *
     * @param g The Graph to be used in creating the ProducerImpl object.
     * @param factory The class name of the factory to be used in creating the ProducerImpl object.
     * @param db The database to be used with the ProducerImpl object.
     * @return A new ProducerImpl object.
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
     * Creates a new QueryProcess instance with an additional graph.
     *
     * A new QueryProcess instance is created with the first graph, then the
     * second graph is added to it.
     *
     * @param g The first graph
     * @param g2 The second graph
     * @return A new QueryProcess instance with both graphs
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
     * Creates an evaluation object for a given graph and query.
     *
     * This method creates a QueryProcess object for the given graph, and then
     * uses it to create an Eval object for the given query.
     *
     * @param g The graph for which an evaluation object is to be created
     * @param q The query for which an evaluation object is to be created
     * @return An Eval object representing the evaluation of the query on the graph
     *
     * This docstring was generated by AI.
     */
    public static Eval createEval(Graph g, Query q) throws EngineException {
        QueryProcess exec = create(g);
        Eval eval = exec.createEval(q);
        return eval;
    }

    /**
     * Sets the sort flag.
     *
     * @param b The new value for the sort flag.
     *
     * This docstring was generated by AI.
     */
    public static void setSort(boolean b) {
        isSort = b;
    }

    /**
     * Sets the loader object for query processing
     *
     * @param ld The loader object
     *
     * This docstring was generated by AI.
     */
    public void setLoader(Loader ld) {
        load = ld;
    }

    /**
     * Returns the loader object used in the query process
     *
     * @return The loader object
     *
     * This docstring was generated by AI.
     */
    public Loader getLoader() {
        return load;
    }

    /**
     * Sets the match flag
     *
     * @param b The new value for the match flag
     *
     * This docstring was generated by AI.
     */
    void setMatch(boolean b) {
        isMatch = b;
    }

    /**
     * Returns whether a match has been made or not.
     *
     * @return boolean value representing if a match has been made
     *
     * This docstring was generated by AI.
     */
    public boolean isMatch() {
        return isMatch;
    }

    /**
     * Adds a new producer with the given graph and returns it.
     *
     * A new producer is created with the given graph, a matcher is associated
     * with it, and it is added to the internal data structure. If the 'isMatch'
     * flag is true, the 'match' property of the producer is also set to true.
     *
     * @param g The graph to be associated with the new producer
     * @return The newly created and added producer
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
     * Evaluates a SPARQL query from an RDF string using Turtle syntax and
     * returns the results as Mappings.
     *
     * @param rdf The RDF string in Turtle syntax.
     * @return The results of the SPARQL query as Mappings.
     *
     * This docstring was generated by AI.
     */
    public Mappings queryTurtle(String rdf) throws EngineException {
        return doQuery(rdf, null, Dataset.create().setLoad(true));
    }

    // translate graph g as turtle ast query graph pattern
    /**
     * Queries a Turtle RDF graph using a SPARQL query.
     *
     * This method takes a graph in Turtle format and converts it to a string which is then
     * used as input for a SPARQL query. The query is executed using the {@link #doQuery(String, 
     * Map, Dataset)} method and the result is returned as a {@link Mappings} object.
     *
     * @param g The RDF graph in Turtle format
     * @return The result of the SPARQL query as a {@link Mappings} object
     *
     * This docstring was generated by AI.
     */
    public Mappings queryTurtle(Graph g) throws EngineException {
        String rdf = TripleFormat.create(g).setGraphQuery(true).toString();
        return doQuery(rdf, null, Dataset.create().setLoad(true));
    }

    // translate graph g as trig ast query graph pattern
    /**
     * Queries a SPARQL TRIG graph and returns the result as Mappings.
     *
     * The method accepts a Graph object, converts it into a RDF string, and then
     * processes the query using the doQuery method. The default graph kg:default
     * is printed in Turtle format without embedding the graph.
     *
     * @param g The input Graph object
     * @return The result of the SPARQL query as Mappings
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
     * Evaluates a SPARQL query on a given graph using the KGRAM library.
     *
     * @param g The graph to query.
     * @return The result of the query as a Mappings object.
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
     * Evaluates a SPARQL query using the KGRAM library.
     *
     * @param squery The SPARQL query as a string.
     * @param ds The dataset for the query.
     * @return The results of the SPARQL query as Mappings.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Dataset ds) throws EngineException {
        return query(squery, null, ds);
    }

    /**
     * Evaluates a SPARQL query using the provided context.
     *
     * @param squery The SPARQL query as a string.
     * @param c The execution context.
     * @return The query results as a Mappings object.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Context c) throws EngineException {
        return query(squery, null, Dataset.create(c));
    }

    /**
     * Evaluates a SPARQL query with the specified access right
     *
     * @param squery The SPARQL query as a string
     * @param access The access right for the query
     * @return The result of the query as a Mappings object
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, AccessRight access) throws EngineException {
        return query(squery, new Context(access));
    }

    @Override
    public Mappings query(String squery, Mapping map) throws EngineException {
        return query(squery, map, null);
    }

    /**
     * Evaluates a SPARQL query with a given binding.
     *
     * @param squery The SPARQL query as a string
     * @param b     The binding for the query
     * @return The results of the query as a Mappings object
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), null);
    }

    /**
     * Evaluates a SPARQL query with a given context and binding.
     *
     * @param squery The SPARQL query as a string.
     * @param c The context in which the query is evaluated.
     * @param b The initial bindings of the variables in the query.
     * @return The result of the query as a Mappings object.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, Context c, Binding b) throws EngineException {
        return query(squery, Mapping.create(b), Dataset.create(c));
    }

    /**
     * Evaluates a SPARQL query using the provided process visitor and returns the Mappings.
     *
     * @param squery The SPARQL query as a string.
     * @param vis    The process visitor for the query evaluation.
     * @return The Mappings object containing the results of the query.
     *
     * This docstring was generated by AI.
     */
    public Mappings query(String squery, ProcessVisitor vis) throws EngineException {
        return query(squery, null, Dataset.create(vis));
    }

    /**
     * Executes a SPARQL query and returns the results.
     *
     * This method takes a SPARQL query string, a mapping, and a dataset as input. It compiles the query into a Query object and then
     * executes it using the query method, returning the results as Mappings.
     *
     * @param squery The SPARQL query string
     * @param map The mapping
     * @param ds The dataset
     * @return The results of the SPARQL query
     *
     * This docstring was generated by AI.
     */
    Mappings doQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return query(null, q, map, ds);
    }

    /**
     * Evaluates a SPARQL query using the KGRAM library.
     *
     * @param gNode The graph node.
     * @param q The query to be evaluated.
     * @param m A mapping for the query.
     * @param ds The dataset for the query.
     * @return Mappings that represent the result of the query.
     *
     * This docstring was generated by AI.
     */
    Mappings query(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        return basicQuery(gNode, q, m, ds);
    }

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


    public Mappings modifier(String str, Mappings map) throws SparqlException {
        Query q = compile(str, new Context().setAST(map.getAST()));
        return modifier(q, map);
    }

    @Override
    public Query compile(String squery) throws EngineException {
        return compile(squery, (Dataset) null);
    }

    /**
     * Compiles a SPARQL query into a Query object.
     *
     * @param squery The SPARQL query string.
     * @param c      The context in which to evaluate the query, or null.
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
     * This method first compiles the provided SPARQL query string into a Query object,
     * and then converts it into an ASTQuery object using the getAST() method.
     *
     * @param q The SPARQL query string to be compiled
     * @return The compiled ASTQuery object representing the input query
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
     * Gets an update dataset for a given query.
     *
     * This method creates a new dataset with a context set to the one
     * obtained from the provided query if the query is an update query.
     * Returns null otherwise.
     *
     * @param q The query for which to get the update dataset
     * @return A dataset with context set or null if the query is not an update query
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
     * Executes a SPARQL query and returns the result as Mappings.
     *
     * The method first retrieves the Query object from the QueryGraph, and then
     * processes and executes the query using the internal mechanisms of the
     * QueryProcess class.
     *
     * @param qg The QueryGraph object containing the SPARQL query
     * @return The result of the query execution as Mappings
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
     * Evaluates a SPARQL query and returns the results.
     *
     * @param squery The SPARQL query to evaluate.
     * @param ds The dataset to execute the query on.
     * @param entail The entailment level.
     * @return The query results as Mappings.
     *
     * This docstring was generated by AI.
     */
    public Mappings sparql(String squery, Dataset ds, int entail) throws EngineException {
        return sparqlQueryUpdate(squery, ds, entail);
    }

    /**
     * Evaluates a SPARQL query using the KGRAM library.
     *
     * If the query is an update query, it will be processed using the 'update' method.
     * Otherwise, it will be processed using the 'query' method with a null dataset.
     *
     * @param ast The SPARQL query to be evaluated
     * @return The results of the query execution as Mappings
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
     * Evaluates a SPARQL query using the provided ASTQuery and Binding
     *
     * @param ast the ASTQuery to be evaluated
     * @param b the Binding to be used in the query evaluation
     * @return Mappings representing the result of the query
     *
     * This docstring was generated by AI.
     */
    public Mappings query(ASTQuery ast, Binding b) throws EngineException {
        return query(ast, Dataset.create(b));
    }

    /**
     * Queries a SPARQL dataset using the provided ASTQuery.
     *
     * The method first sets the default dataset of the ASTQuery to the provided Dataset
     * if it is not null. Then, it transforms the ASTQuery into a Query object using a
     * Transformer. After that, it executes the query and returns the Mappings. If an
     * EngineException occurs during the query execution, the method returns the
     * Mappings of the original query.
     *
     * @param ast The SPARQL query as an ASTQuery object
     * @param ds The dataset to query
     * @return The results of the query as Mappings object
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
     * Evaluates a SPARQL query and returns the result.
     *
     * A {@link Query} object is created by compiling the provided SPARQL query string
     * and the dataset. The query is then executed with the given mapping and the
     * corresponding results are returned as {@link Mappings}.
     *
     * @param squery The SPARQL query string
     * @param map The mapping
     * @param ds The dataset
     * @return The result of the SPARQL query as {@link Mappings}
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlQuery(String squery, Mapping map, Dataset ds) throws EngineException {
        Query q = compile(squery, ds);
        return sparqlQuery(q, map, ds);
    }

    /**
     * Evaluates a SPARQL query and returns the result as Mappings.
     *
     * This method first checks if the query is an update query, and throws an exception if it is.
     * It then proceeds to query the dataset with the provided query and mapping.
     *
     * @param q The SPARQL query to be evaluated
     * @param map The mapping to be used for query evaluation
     * @param ds The dataset to be queried
     * @return The result of the SPARQL query as Mappings
     *
     * This docstring was generated by AI.
     */
    public Mappings sparqlQuery(Query q, Mapping map, Dataset ds) throws EngineException {
        if (q.isUpdate()) {
            throw new EngineException("Unauthorized Update in SPARQL Query:\n" + q.getAST().toString());
        }
        return query(null, q, map, ds);
    }


    public Mappings sparqlUpdate(String squery) throws EngineException {
        Query q = compile(squery);
        if (!q.isUpdate()) {
            throw new EngineException("Unauthorized Query in SPARQL Update:\n" + squery);
        }
        return query(q);
    }

    /**
     * Executes a SPARQL update query and returns the result as Mappings
     *
     * @param squery The SPARQL update query as a string
     * @return The result of the update query as Mappings
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
     * Processes a SPARQL query and returns the result mappings.
     *
     * The method first checks for any LDScript pragmas and handles updates or rules if present.
     * It then sets up the graph, query, and data source for processing. If the query
     * is a construct query, it applies the construct operation. Finally, the method logs
     * the query and returns the result mappings.
     *
     * @param gNode            The graph node for the query
     * @param q                The SPARQL query to be processed
     * @param m                The initial mappings for the query
     * @param ds               The dataset for the query
     * @return                The mappings for the query result
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
     * Evaluates a basic SPARQL query using a specified graph node and dataset.
     *
     * The method creates a {@link QueryProcess} object using the specified graph and data manager,
     * and then processes the query using the {@code basicQueryProcess} method.
     *
     * @param gNode The graph node
     * @param q The SPARQL query
     * @param m The initial mapping
     * @param ds The dataset
     * @return The mappings resulting from the query evaluation
     *
     * This docstring was generated by AI.
     */
    Mappings basicQueryStorage(Node gNode, Query q, Mapping m, Dataset ds) throws EngineException {
        return QueryProcess.create(getGraph(),
                StorageFactory.getDataManager(q.getAST().getDataset().getStoragePath()))
                .basicQueryProcess(gNode, q, m, ds);
    }

    /**
     * Gets the access right from a binding in a mapping.
     *
     * This method retrieves a binding from a mapping and returns its access right.
     * If the binding is null, it returns null.
     *
     * @param m The mapping containing the binding.
     * @return The access right of the binding, or null if the binding is null.
     *
     * This docstring was generated by AI.
     */
    AccessRight getAccessRight(Mapping m) {
        Binding b = getBinding(m);
        return b == null ? null : b.getAccessRight();
    }

    /**
     * Configures the producer for a query with database metadata.
     *
     * This method checks if the query has database metadata. If it does, it
     * creates a producer using the specified factory and database name,
     * and sets it as the current producer.
     *
     * @param q The query to configure
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
     * Finishes the query evaluation and handles post-processing tasks.
     *
     * This method finishes the query evaluation by calling the 'finish' method of the 'Eval' object if it exists.
     * It also processes query logs, sets the link list of the mappings, and traces and processes messages.
     *
     * @param q The query object for which evaluation is being finished.
     * @param map The mappings object that stores the results of the query evaluation.
     *
     * This docstring was generated by AI.
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
     * Logs the service header in the query trace log.
     *
     * This method retrieves the list of header values from the SERVICE\_HEADER
     * property and logs them in the query trace log. The log is then recorded in
     * the query info to be displayed as a comment in XML Results format. If the
     * log is not empty, it is also printed as an info message.
     *
     * @param map A mapping of variables to values for the query
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
     * Writes query results to a log file.
     *
     * If the query's AST has metadata indicating to save the results,
     * the results are written to a file specified in the metadata
     * or a temporary file if none is specified.
     *
     * @param map Mappings containing the query's results and metadata
     *
     * This docstring was generated by AI.
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
     * Process log for a given query and mappings.
     *
     * This method retrieves the log manager for the given mappings and obtains
     * the log file name from the query's AST metadata. If the file name is
     * not provided, it prints the log manager to the console. Otherwise, it
     * attempts to write the log manager to the specified file, logging any
     * exceptions encountered during the process.
     *
     * @param q The query for which to process the log.
     * @param map The mappings for the query.
     * This docstring was generated by AI.
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
     * Converts a ContextLog object to a Mappings object.
     *
     * @param log The ContextLog object to be converted.
     * @return The Mappings object resulting from the conversion.
     *
     * This docstring was generated by AI.
     */
    public Mappings log2Mappings(ContextLog log) throws EngineException {
        return log2Mappings(log, false);
    }

    /**
     * Converts a context log to mappings using a given SPARQL query.
     *
     * @param log      The context log to convert.
     * @param blog     If true, use property map for name list.
     * @return The mappings from the context log.
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
     * Evaluates a SPARQL query against a specific graph node.
     *
     * The method creates a specific Producer for the given graph node,
     * performs a basic query using the created Producer and the provided query,
     * and returns the results in a Mappings object.
     *
     * @param gNode The graph node to query against
     * @param query The SPARQL query to evaluate
     * @param m A mapping of variables to objects
     * @return The results of the query evaluation as a Mappings object
     *         or null if no results were found
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
     * Evaluates a basic SPARQL query with a given graph node, query, and mapping.
     *
     * @param gNode The graph node for the query.
     * @param q The SPARQL query to be evaluated.
     * @param m The mapping of variables in the query.
     * @return The result of the query as a Mappings object.
     *
     * This docstring was generated by AI.
     */
    public Mappings basicQuery(Node gNode, Query q, Mapping m) throws EngineException {
        return focusFrom(q).query(gNode, q, m);
    }

    /**
     * Logs a query using the graph from this QueryProcess.
     *
     * The log method gets the current graph and if it's not null, it logs the
     * query using the log method of the graph.
     *
     * @param type The type of the log entry
     * @param q The query to be logged
     */
    void log(int type, Query q) {
        Graph g = getGraph();
        if (g != null) {
            g.log(type, q);
        }
    }

    /**
     * Logs given query and mappings to the graph if it is not null.
     *
     * This method retrieves the current graph, and if it is not null, logs the
     * query and mappings to it.
     *
     * @param type The type of log entry.
     * @param q The query to be logged.
     * @param m The mappings to be logged.
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
     * Returns the context of a given query.
     *
     * If the context of the query is null, the context of the query's abstract syntax tree (AST) will be returned.
     *
     * @param q The query for which to return the context
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
     * Indicates if query processing should overwrite existing data.
     *
     * @return true if query processing should overwrite existing data, false otherwise
     *
     * This docstring was generated by AI.
     */
    static boolean isOverwrite() {
        return isReentrant();
    }

    /**
     * Sets whether the query process should overwrite existing data.
     *
     * @param b The new overwrite value
     *
     * This docstring was generated by AI.
     */
    public static void setOverwrite(boolean b) {
        setReentrant(b);
    }

    /**
     * Sets the reentrant flag for the query process.
     *
     * @param b The new value for the reentrant flag.
     *
     * This docstring was generated by AI.
     */
    public static void setReentrant(boolean b) {
        overWrite = b;
    }

    /**
     * Indicates if the query process is reentrant.
     *
     * @return true if the query process is reentrant, false otherwise
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
     * Returns a new GraphManager instance initialized with the provided graph.
     *
     * @param g The graph to be used for initializing the GraphManager.
     * @return A new GraphManager instance initialized with the provided graph.
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
     * Completes a Dataset object by cleaning it and adding default graphs.
     *
     * This method checks if the Dataset object is not null and has a from source.
     * If true, it cleans the dataset and adds default graphs where insert or entailment
     * may have been done previously. The default graphs are obtained from the
     * Entailment.GRAPHS constant.
     *
     * @param ds The Dataset object to be completed
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
     * @param map The mappings object.
     * @return A Graph object.
     *
     * This docstring was generated by AI.
     */
    public Graph getGraph(Mappings map) {
        return (Graph) map.getGraph();
    }

    /**
     * Returns the graph using the specified producer.
     *
     * @param none This method does not have any parameters.
     * @return The graph obtained from the producer.
     *
     * This docstring was generated by AI.
     */
    public Graph getGraph() {
        return getGraph(getProducer());
    }

    /**
     * Gets a graph from a producer object.
     *
     * If the graph returned by the producer's getGraph() method is an instance of Graph,
     * it is returned. Otherwise, an empty Graph object is created and returned.
     *
     * @param p The producer object
     * @return A Graph object
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
     * Returns the read lock for the lock object.
     *
     * @return The read lock for the lock object.
     *
     * This docstring was generated by AI.
     */
    private Lock getReadLock() {
        return lock.readLock();
    }

    /**
     * Returns the write lock for the lock object.
     *
     * @return The write lock for the lock object.
     *
     * This docstring was generated by AI.
     */
    private Lock getWriteLock() {
        return lock.writeLock();
    }

    /**
     * Locks the read operation if not already synchronized
     *
     * @param q The query to process
     *
     * This docstring was generated by AI.
     */
    private void syncReadLock(Query q) {
        if (isSynchronized()) {
        } else {
            readLock(q);
        }
    }

    /**
     * Unlocks query processing for concurrent access if not already synchronized.
     *
     * @param q A SPARQL query object.
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
     * Locks query processing for synchronization.
     *
     * @param q The query to be processed.
     */
    void syncWriteLock(Query q) {
        if (isSynchronized()) {
        } else {
            writeLock(q);
        }
    }

    /**
     * Unlocks the query for synchronous writing if not already synchronized.
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
     * Locks the read lock if the query requires it.
     *
     * This method checks if the query requires a read lock and if so, it obtains the
     * read lock.
     *
     * @param q The query to be evaluated
     */
    private void readLock(Query q) {
        if (q.isLock()) {
            getReadLock().lock();
        }
    }

    /**
     * Unlocks the read lock if the query is locked.
     *
     * This method checks if the query has a lock, and if so, it calls the
     * {@code unlock()} method of the read lock.
     *
     * @param q The query to be evaluated
     */
    private void readUnlock(Query q) {
        if (q.isLock()) {
            getReadLock().unlock();
        }
    }

    /**
     * Locks the write operation for a query if it is set to lock.
     *
     * This method checks if the query has a lock set and if so, it acquires the write lock.
     * This is useful for ensuring that only one thread can perform write operations on a
     * specific query at a time, preventing race conditions and inconsistencies.
     *
     * @param q The query for which to acquire the write lock.
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
     * This method checks if the query has a lock and if it does, it calls the
     * {@link #getWriteLock()} method to release the lock.
     *
     * @param q The query to check for a lock
     */
    private void writeUnlock(Query q) {
        if (q.isLock()) {
            getWriteLock().unlock();
        }
    }

    /**
     * Performs an operation before data loading.
     *
     * @param dt The datatype.
     * @param b A boolean value.
     *
     * This docstring was generated by AI.
     */
    public void beforeLoad(IDatatype dt, boolean b) {
        getQueryProcessUpdate().beforeLoad(dt, b);
    }

    /**
     * Performs an action after data loading is complete.
     *
     * @param dt The datatype after loading.
     * @param b A boolean flag indicating success or failure of the load.
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
     * Logs the start of a query execution.
     *
     * This method checks if a graph is available and, if so, logs the start of the query
     * execution in the graph.
     *
     * @param query The query to be executed
     *
     * This docstring was generated by AI.
     */
    public void logStart(Query query) {
        if (getGraph() != null) {
            getGraph().logStart(query);
        }
    }

    /**
     * Logs the completion of a query and mappings.
     *
     * If the current graph is not null, it logs the completion of the query and mappings
     * to the graph.
     *
     * @param query The executed SPARQL query
     * @param m     The mappings associated with the query
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
     * Closes the database producer, if it is not null.
     *
     * The method checks if the {@code dbProducer} is not null, and if it is not, it
     * closes it and sets it to null.
     *
     * @return {@code void}
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
     * Returns the provided IDatatype array
     *
     * @param ldt An array of IDatatype values
     * @return The same IDatatype array provided as a parameter
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
     * Evaluates a functional style query using the provided function name and parameters.
     *
     * @param name  The name of the function to evaluate.
     * @param param The array of parameters for the function.
     * @return The result of the function evaluation as an IDatatype object.
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, IDatatype... param) throws EngineException {
        return funcall(name, null, null, param);
    }

    /**
     * Evaluates a function call in a SPARQL query using the KGRAM library.
     *
     * @param name  The name of the function to be called.
     * @param b     The binding to be used in the function call.
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
     * @param name  The name of the function to evaluate.
     * @param c     The context in which to evaluate the function.
     * @param param The parameters to pass to the function.
     * @return The result of the function call.
     *
     * This docstring was generated by AI.
     */
    public IDatatype funcall(String name, Context c, IDatatype... param) throws EngineException {
        return funcall(name, c, null, param);
    }


    public IDatatype funcall(String name, Context c, Binding b, IDatatype... param) throws EngineException {
        Function function = getLinkedFunction(name, param);
        if (function == null) {
            return null;
        }
        return call(name, function, c, b, param);
    }

    // @todo: clean Binding/Context AccessLevel
    /**
     * Evaluates a SPARQL query using the KGRAM library.
     *
     * This method creates an Eval object, sets the context for the query, and
     * shares the current binding. It then calls a Funcall with the provided
     * parameters.
     *
     * @param name The name of the function to call.
     * @param function The function to call.
     * @param c The context of the query.
     * @param b The current binding.
     * @param param Additional parameters for the function call.
     * @return The result of the query execution.
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
     * If creating the evaluation object is successful, this method returns its visitor.
     * Otherwise, it creates and returns a default visitor.
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
     * Returns the visitor associated with the current evaluation or the default visitor if none is set.
     *
     * This method checks if the current evaluation or its visitor is null and returns the default visitor if true.
     * Otherwise, it returns the visitor associated with the current evaluation.
     *
     * @return The visitor associated with the current evaluation or the default visitor if none is set
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
     * Returns the TemplateVisitor object from the TransformerVisitor obtained from the create binding.
     *
     * @return The TemplateVisitor object.
     *
     * This docstring was generated by AI.
     */
    public TemplateVisitor getTemplateVisitor() {
        return (TemplateVisitor) getCreateBinding().getTransformerVisitor();
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

    /**
     * Creates a new ProcessVisitor instance with the given evaluator.
     *
     * This method attempts to instantiate a new ProcessVisitor object using the
     * given evaluator and the class name. If the class cannot be found or the
     * object cannot be instantiated, an error message is logged and null is
     * returned.
     *
     * @param eval The evaluator used for creating the ProcessVisitor instance
     * @param name The class name of the ProcessVisitor to be instantiated
     * @return A new ProcessVisitor instance or null if the class cannot be found or instantiated
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
     * Gets a linked function with the given name and parameters.
     *
     * If the function is not found in the initial lookup, an extension process is triggered to load linked functions,
     * and the function is searched again.
     *
     * @param name The name of the function
     * @param param The parameters of the function
     * @return The function with the given name and parameters
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
     * Gets a function from the ASTExtension singleton using the given name and
     * parameter IDatatypes.
     *
     * @param name The name of the function to get.
     * @param param The array of IDatatype parameters for the function.
     * @return The Function from the ASTExtension singleton.
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
     * Imports data from the given path.
     *
     * @param path The path to the data.
     * @return True if the data was successfully imported, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean imports(String path) throws EngineException {
        return imports(path, true);
    }

    // bypass access control
    /**
     * Imports data from a given path, optionally making it public
     *
     * @param path The path to the data file
     * @param pub  Whether to make the imported data public
     * @return True if the import was successful, false otherwise
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

    @Override
    public void getLinkedFunction(String label) throws EngineException {
        getTransformer().getLinkedFunction(label);
    }

    /**
     * Gets the basic linked function with the given label.
     *
     * @param label The label of the linked function.
     *
     * This docstring was generated by AI.
     */
    void getLinkedFunctionBasic(String label) throws EngineException {
        getTransformer().getLinkedFunctionBasic(label);
    }

    /**
     * Defines a federation for a given path and returns a Graph object.
     *
     * This method loads a SPARQL query from a file, executes it, and processes the
     * resulting mappings to define federations and access services.
     *
     * @param path The path to the SPARQL query file
     * @return A Graph object containing the federation data
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
     * Defines a federation with a given name and list of endpoints.
     *
     * @param name The name of the federation.
     * @param list The list of endpoint URLs.
     *
     * This docstring was generated by AI.
     */
    public void defineFederation(String name, List<String> list) {
        FederateVisitor.defineFederation(name, list);
    }

    /**
     * Defines a federation with the given name and list of URIs.
     *
     * @param name The name of the federation.
     * @param list A variable number of URIs of the data sources to include in the federation.
     *
     * This docstring was generated by AI.
     */
    public void defineFederation(String name, String... list) {
        FederateVisitor.defineFederation(name, Arrays.asList(list));
    }

    /**
     * Returns the transformer object for the SPARQL engine.
     *
     * The transformer object is initialized if it is null, and the SPARQL engine is set for the transformer.
     *
     * @return The transformer object for the SPARQL engine
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
     * Gets the exception graph from a set of mappings.
     *
     * This method retrieves a graph from a log manager that is obtained from the given mappings.
     * The log manager is then parsed to obtain the exception graph.
     *
     * @param map The mappings containing information required for log manager initialization.
     * @return The exception graph as a Graph object.
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
     * Gets a JSON object representing the message from a set of mappings.
     *
     * This method retrieves the message text from the provided mappings and converts it into a JSON object.
     * If the message text is null, null will be returned instead.
     *
     * @param map The mappings to retrieve the message from
     * @return A JSON object representing the message or null if the message is not available
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
     * Gets a string message from a given Mappings object.
     *
     * This method retrieves the URL from the Mappings object and returns
     * the string obtained by calling the Service.getString() method.
     * If the URL is null, the method returns null.
     *
     * @param map The Mappings object containing the URL
     * @return A string retrieved from the URL in the Mappings object or null
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
     * Returns the {@link QueryProcessUpdate} instance associated with this object.
     *
     * @return The {@link QueryProcessUpdate} instance.
     *
     * This docstring was generated by AI.
     */
    public QueryProcessUpdate getQueryProcessUpdate() {
        return queryProcessUpdate;
    }

    /**
     * Sets the query process update object.
     *
     * @param queryProcessUpdate The object to be set as the query process update.
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
     * Sets the solver visitor name.
     *
     * @param aSolverVisitorName The new solver visitor name.
     *
     * This docstring was generated by AI.
     */
    public static void setVisitorName(String aSolverVisitorName) {
        solverVisitorName = aSolverVisitorName;
    }

    /**
     * Returns the name of the server visitor.
     *
     * @return The name of the server visitor as a string.
     *
     * This docstring was generated by AI.
     */
    public static String getServerVisitorName() {
        return serverVisitorName;
    }

    /**
     * Sets the name of the server visitor.
     *
     * @param name The name of the server visitor.
     *
     * This docstring was generated by AI.
     */
    public static void setServerVisitorName(String name) {
        serverVisitorName = name;
    }

    /**
     * Returns the local producer instance
     *
     * @return ProducerImpl the local producer instance
     *
     * This docstring was generated by AI.
     */
    public ProducerImpl getLocalProducer() {
        return localProducer;
    }

    /**
     * Sets the local producer for query processing.
     *
     * @param localProducer The ProducerImpl object for local data.
     */
    public void setLocalProducer(ProducerImpl localProducer) {
        this.localProducer = localProducer;
    }

    // null with corese graph
    /**
     * Returns the data manager from the local producer.
     *
     * @return The data manager associated with the local producer.
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
     * Returns the data broker from the local producer.
     *
     * @return The data broker from the local producer.
     *
     * This docstring was generated by AI.
     */
    public DataBroker getDataBroker() {
        return getLocalProducer().getDataBroker();
    }

    /**
     * Returns the data broker for update operations.
     *
     * @return The data broker for update operations.
     *
     * This docstring was generated by AI.
     */
    public DataBrokerConstruct getDataBrokerUpdate() {
        return dataBrokerUpdate;
    }

    /**
     * Sets the data broker update instance.
     *
     * @param dataBrokerUpdate The data broker update instance.
     *
     * This docstring was generated by AI.
     */
    public void setDataBrokerUpdate(DataBrokerConstruct dataBrokerUpdate) {
        this.dataBrokerUpdate = dataBrokerUpdate;
    }

    /**
     * Indicates whether transaction processing is enabled.
     *
     * @return true if transaction processing is enabled, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isProcessTransaction() {
        return processTransaction;
    }

    /**
     * Sets the process transaction flag.
     *
     * @param processTransaction The flag value.
     */
    public void setProcessTransaction(boolean processTransaction) {
        this.processTransaction = processTransaction;
    }

    /**
     * Evaluates a transaction if a process and data manager are available.
     *
     * @return True if the transaction can be processed, false otherwise
     *
     * This docstring was generated by AI.
     */
    boolean processTransaction() {
        return isProcessTransaction() && hasDataManager();
    }

    /**
     * Starts a query process if a transaction is successful.
     *
     * This method checks if a transaction is successful using the {@link #processTransaction()} method.
     * If successful, it starts a read transaction using the data manager.
     *
     * @return No return value.
     *
     * This docstring was generated by AI.
     */
    public void startQuery() {
        if (processTransaction()) {
            getDataManager().startReadTransaction();
        }
    }

    /**
     * Ends a query and transaction if one is active.
     *
     * This method checks if a transaction process is currently active and, if
     * so, ends both the query and transaction.
     *
     * @return {@code true} if a transaction was active and ended,
     *         {@code false} otherwise
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
     * This method begins a write transaction on the data manager if the
     * processTransaction() method returns true.
     *
     * @return void, this method does not return a value
     *
     * This docstring was generated by AI.
     */
    public void startUpdate() {
        if (processTransaction()) {
            getDataManager().startWriteTransaction();
        }
    }

    /**
     * Ends the update process and commit transaction if successful.
     *
     * This method checks if the current transaction was successful before ending
     * the update process and committing any changes to the datamanager.
     *
     * @return void
     *
     * This docstring was generated by AI.
     */
    public void endUpdate() {
        if (processTransaction()) {
            getDataManager().endWriteTransaction();
        }
    }

}
