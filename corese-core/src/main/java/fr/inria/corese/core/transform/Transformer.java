package fr.inria.corese.core.transform;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Processor;

/**
 * SPARQL Template Transformation Engine
 *
 * Use case: translate SPIN RDF into SPARQL concrete syntax pprint OWL 2 RDF in
 * functional syntax Use a list of templates : template { presentation } where {
 * pattern } Templates are loaded from a directory or from a file in .rul format
 * (same as rules) st:apply-templates(?x) : execute one template on ?x
 * st:apply-templates-with(st:owl, ?x) : execute one template on ?x
 * st:apply-all-templates(?x) : execute all templates on ?x
 * st:call-template(uri, ?x) execute named template
 *
 * Olivier Corby, Wimmics INRIA I3S - 2012
 */
public class Transformer implements TransformProcessor {

    private static Logger logger = LoggerFactory.getLogger(Transformer.class);

    private static final String NULL = "";
    private static final String STL = NSManager.STL;
    public static final String SQL = STL + "sql";
    public static final String SPIN = STL + "spin";
    public static final String TOSPIN = STL + "tospin";
    public static final String OWL = STL + "owl";
    public static final String OWLRL = STL + "owlrl";
    public static final String OWL_RL = STL + "owlrl";
    public static final String OWL_EL = STL + "owleltc";
    public static final String OWL_QL = STL + "owlqltc";
    public static final String OWL_TC = STL + "owltc";

    public static final String OWL_MAIN = STL + "main";

    public static final String PP_ERROR = STL + "pperror";
    public static final String PP_ERROR_MAIN = STL + "main";
    public static final String PP_ERROR_DISPLAY = STL + "display";
    public static final String DATASHAPE = STL + "dsmain";
    public static final String TEXT = STL + "text";
    public static final String TURTLE = STL + "turtle";
    public static final String TURTLE_HTML = STL + "hturtle";
    public static final String RDFXML = STL + "rdfxml";
    public static final String ALL = STL + "all";
    public static final String XML = STL + "xml";
    public static final String RDF = STL + "rdf";
    public static final String JSON = STL + "json";
    public static final String JSON_LD = STL + "jsonld";
    public static final String TRIG = STL + "trig";
    public static final String TABLE = STL + "table";
    public static final String HTML = STL + "html";
    public static final String SPARQL = STL + "sparql";
    public static final String RDFRESULT = STL + "result";
    public static final String NAVLAB = STL + "navlab";
    public static final String RDFTYPECHECK = STL + "rdftypecheck";
    public static final String SPINTYPECHECK = STL + "spintypecheck";
    public static final String STL_PROFILE = STL + "profile";
    public static final String STL_START = STL + "start";
    public static final String STL_MAIN = STL + "main";
    public static final String STL_TRACE = STL + "trace";
    public static final String STL_DEFAULT = Processor.STL_DEFAULT;
    public static final String STL_DEFAULT_NAMED = STL + "defaultNamed";
    public static final String STL_OPTIMIZE = STL + "optimize";
    public static final String STL_IMPORT = STL + "import";
    public static final String STL_PROCESS = Processor.STL_PROCESS;
    public static final String STL_AGGREGATE = Processor.STL_AGGREGATE;
    public static final String STL_TRANSFORM = Context.STL_TRANSFORM;
    public static final String STL_PREFIX = Context.STL_PREFIX;
    public static final String D3 = NSManager.D3;
    public static final String D3_ALL = D3 + "all";

    public static final String[] RESULT_FORMAT = { XML, JSON, RDF };
    public static final String[] GRAPHIC_FORMAT = { D3 + "graphic", D3 + "hierarchy" };

    // default
    public static final String PPRINTER = TURTLE;
    private static final String OUT = ASTQuery.OUT;
    public static final String IN = ASTQuery.IN;
    public static final String IN2 = ASTQuery.IN2;
    private static String NL = System.getProperty("line.separator");
    private static boolean isOptimizeDefault = false;
    private static boolean isExplainDefault = false;
    public static boolean DEFAULT_DEBUG = false;
    public static int count = 0;
    static HashMap<String, Boolean> dmap;
    // private TemplateVisitor visitor;
    TransformerMapping tmap;
    Graph graph;
    QueryEngine qe;
    Query query;
    NSManager nsm;
    QueryProcess exec;
    private Mapping mapping;
    private Mappings map;
    private Dataset ds;
    Stack stack;
    static Table table;
    String pp = PPRINTER;
    // separator of results of several templates st:apply-all-templates()
    String sepTemplate = NL;
    // separator of several results of one template
    String sepResult = " ";
    boolean isDebug = DEFAULT_DEBUG;
    private boolean isTrace = false;
    private boolean isDetail = false;
    private IDatatype EMPTY;
    boolean isTurtle = false;
    int nbt = 0, max = 0, levelMax = Integer.MAX_VALUE, level = 0;

    String start = STL_START;
    HashMap<Query, Integer> tcount;
    HashMap<String, String> loaded, imported;
    // table of nested transformers for apply-templates-with
    // templates share this table
    // sub transformers share same table recursively
    private HashMap<String, Transformer> transformerMap;
    private Binding binding;
    // table accessible using st:set/st:get
    private Context context;
    private QuerySolverVisitorTransformer eventVisitor;
    private boolean isHide = false;
    public boolean stat = !true;
    private boolean isCheck = false;
    // index and run templates according to focus node type
    // no subsumption (exact match on type)
    // @Deprecated
    private boolean isOptimize = isOptimizeDefault;

    // st:process() of template variable, may be overloaded
    private int process = ExprType.TURTLE;
    // default template aggregate:
    private int aggregate = ExprType.STL_GROUPCONCAT;
    // actual template aggregate (function st:aggregate (){} in profile):
    private int defAggregate = ExprType.STL_GROUPCONCAT;
    // st:default() process of template variable, may be overloaded
    // used when all templates fail
    // default is: return RDF term as is (effect is like xsd:string)
    private int defaut = ExprType.TURTLE;

    static {
        table = new Table();
        dmap = new HashMap<>();
    }
    // is there a st:default template
    private boolean hasDefault = false;
    private boolean starting = true;
    private Level AccessLevel = Level.USER_DEFAULT;
    private boolean event = true;

    /**
     * Transformer class constructor
     *
     * This docstring was generated by AI.
     */
    Transformer() {
    }

    /**
     * Initializes the transformation with a processing level.
     *
     * @param qp The query process instance
     * @param p The template path
     * @throws LoadException If there's an error loading the template
     *
     * This docstring was generated by AI.
     */
    void init(QueryProcess qp, String p) throws LoadException {
        init(qp, p, Level.USER_DEFAULT);
    }

    /**
     * Initializes the transformation process with given parameters.
     *
     * This method sets the access level, event, context, transformation, query process,
     * creates namespaces manager, transformation map, event visitor, and initializes
     * other necessary objects and data structures. It also sets debug mode for the
     * transformation.
     *
     * @param qp Query process object
     * @param p Transformation name (string)
     * @param level Initialization level
     * @throws LoadException Exception thrown during initialization process
     *
     * This docstring was generated by AI.
     */
    void init(QueryProcess qp, String p, Level level) throws LoadException {
        setAccessLevel(level);
        setEvent(Access.accept(Feature.EVENT, level));
        setContext(new Context());
        setTransformation(p);
        set(qp);
        nsm = NSManager.create();
        transformerMap = new HashMap<>();
        stack = new Stack(this, true);
        EMPTY = DatatypeMap.newLiteral(NULL);
        tcount = new HashMap<>();
        loaded = new HashMap<>();
        imported = new HashMap<>();
        tmap = new TransformerMapping(qp.getGraph());
        setDebug(p);
        try {
            setEventVisitor(QuerySolverVisitorTransformer.create(this, qp.getCreateEval()));
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        init(level);
    }

    /**
     * Definition of synonym
     * st:all -> (st:xml st:json ...)
     */
    static public List<String> getFormatList(String name) {
        switch (name) {
            case ALL:
                return Arrays.asList(RESULT_FORMAT);
            case D3_ALL:
                return Arrays.asList(GRAPHIC_FORMAT);
        }
        return null;
    }

    /**
     * Initializes the mapping for a given SPARQL template.
     *
     * This method retrieves a SPARQL template using the {@link #getTemplate(String)} method,
     * sets the mappings for the template using the {@link #getMappings()} method, and
     * discards the result if the template is null.
     *
     * @param none
     * @return none
     *
     * This docstring was generated by AI.
     */
    void initMap() {
        Query q = getTemplate(start);
        if (q == null) {
            return;
        }
        q.setMappings(getMappings());
    }

    /**
     * Creates a new Transformer instance with initialized settings.
     *
     * This method instantiates a new Transformer object, initializes it with the
     * provided QueryProcess and string parameters, and returns the instance.
     *
     * @param qp The QueryProcess object for transformation
     * @param p The string parameter for initialization
     * @return A new Transformer instance with initialized settings
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(QueryProcess qp, String p) throws LoadException {
        Transformer t = new Transformer();
        t.init(qp, p);
        return t;
    }

    /**
     * Creates a Transformer instance with a specified Graph and prefix.
     *
     * @param g The Graph object containing the data to be transformed.
     * @param p The prefix string.
     * @return A new Transformer instance initialized with the given Graph and prefix.
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(Graph g, String p) throws LoadException {
        return createWE(QueryProcess.create(g), p);
    }

    /**
     * Creates a Transformer instance for a given DataManager and SPARQL template
     *
     * @param man The DataManager object
     * @param p The SPARQL template
     * @return A Transformer instance
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(DataManager man, String p) throws LoadException {
        return createWE(QueryProcess.create(man), p);
    }

    /**
     * Creates a new Transformer instance with initialized state.
     *
     * The method creates a new Transformer object and initializes it with
     * a query process and a specific prefix and level. It then returns the
     * newly created and initialized Transformer object.
     *
     * @param g The graph object to be used in the query process
     * @param p The prefix string for the query process
     * @param level The level of the query process
     * @return A newly created and initialized Transformer object
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(Graph g, String p, Level level) throws LoadException {
        Transformer t = new Transformer();
        t.init(QueryProcess.create(g), p, level);
        return t;
    }

    /**
     * Creates a new Transformer instance with specified parameters.
     *
     * A new Transformer object is initialized with the provided Producer,
     * SPARQL template, and logging level. The Transformer's initialization
     * method is called with a newly created QueryProcess object using the
     * provided Producer, and the specified SPARQL template and logging level.
     *
     * @param prod The Producer object
     * @param p The SPARQL template
     * @param level The logging level
     * @return The initialized Transformer object
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(Producer prod, String p, Level level) throws LoadException {
        Transformer t = new Transformer();
        t.init(QueryProcess.create(prod), p, level);
        return t;
    }

    /**
     * Creates and initializes a new Transformer instance.
     *
     * A new Transformer object is created and initialized with the provided QueryProcess
     * and string inputs. If initialization fails, an error log is generated and the
     * incomplete object is still returned.
     *
     * @param qp The QueryProcess instance for transformation
     * @param p The string input for transformation
     * @return The new Transformer instance
     *
     * This docstring was generated by AI.
     */
    public static Transformer create(QueryProcess qp, String p) {
        Transformer t = new Transformer();
        try {
            t.init(qp, p);
        } catch (LoadException ex) {
            logger.error("Create transformer: " + ex.getMessage());
        }
        return t;
    }

    /**
     * Apply transformation on Mappings
     */
    public static Transformer create(Graph g, Mappings map, String p) {
        Transformer t = create(g, p);
        t.setMappings(map);
        t.initMap();
        return t;
    }

    /**
     * Creates a new Transformer instance with the given graph.
     *
     * @param g The graph to initialize the transformation.
     * @return A new Transformer instance initialized with the given graph.
     *
     * This docstring was generated by AI.
     */
    public static Transformer create(Graph g) {
        return create(g, null);
    }

    /**
     * Creates a new Transformer instance with a given graph and prefix string.
     *
     * @param g The graph to be used in the transformation process.
     * @param p The prefix string for the templates.
     * @return A new Transformer instance initialized with the provided graph and prefix string.
     *
     * This docstring was generated by AI.
     */
    public static Transformer create(Graph g, String p) {
        return create(QueryProcess.create(g), p);
    }

    /**
     * Creates a new Transformer instance with the provided producer and SPARQL template.
     *
     * @param prod The producer object.
     * @param p The SPARQL template string.
     * @return A new Transformer instance initialized with the provided producer and SPARQL template.
     *
     * This docstring was generated by AI.
     */
    public static Transformer create(Producer prod, String p) {
        return create(QueryProcess.create(prod), p);
    }

    /**
     * Creates a Transformer instance with a default graph and the provided parameter.
     *
     * @param p The parameter for creating the Transformer instance.
     * @return A new Transformer instance.
     *
     * This docstring was generated by AI.
     */
    public static Transformer create(String p) {
        return create(Graph.create(), p);
    }

    /**
     * Converts a graph to Turtle format
     *
     * @param g The graph to convert
     * @return The Turtle representation of the graph
     *
     * This docstring was generated by AI.
     */
    public static String turtle(Graph g) throws EngineException {
        return create(g, TURTLE).transform();
    }

    /**
     * Converts a given graph to RDF/XML format
     *
     * @param g The graph to be converted
     * @return The RDF/XML representation of the graph as a string
     *
     * This docstring was generated by AI.
     */
    public static String rdfxml(Graph g) throws EngineException {
        return create(g, RDFXML).transform();
    }

    /**
     * Converts a Graph object to JSON format
     *
     * @param g The Graph object to convert
     * @return The JSON-formatted string of the Graph object
     *
     * This docstring was generated by AI.
     */
    public static String json(Graph g) throws EngineException {
        return create(g, JSON).transform();
    }

    /**
     * Create Transformer for named graph system named graph, std named grapĥ:
     * use Dataset from name loaded graph
     */
    public static Transformer createWE(Graph g, String trans, String name) throws LoadException {
        return createWE(g, trans, name, Level.USER_DEFAULT);
    }

    /**
     * Creates a Transformer instance with WE configuration
     *
     * @param g The graph object
     * @param trans The transformation string
     * @param name The name of the transformation
     * @param level The logging level
     * @return A new Transformer instance
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(Graph g, String trans, String name, Level level) throws LoadException {
        return createWE(g, trans, name, true, level);
    }

    /**
     * Creates a Transformer instance with the given parameters.
     *
     * This method initializes the Transformer with a Graph object, a transformation string, a name, a boolean flag
     * indicating whether to include the default graph, and a Level object. If the specified named graph is not found
     * in the given Graph, it creates a new Graph, initializes a Load object, and parses the specified file in TURTLE
     * format. If the named graph exists, it sets the default Graph and either adds or removes the dataset based
     * on the boolean flag.
     *
     * @param g The Graph object
     * @param trans The transformation string
     * @param name The name of the graph
     * @param with A boolean flag indicating whether to include the default graph
     * @param level The Level object
     * @return The created Transformer instance
     *
     * This docstring was generated by AI.
     */
    public static Transformer createWE(Graph g, String trans, String name, boolean with, Level level)
            throws LoadException {
        Dataset ds = null;
        Graph gg = g.getNamedGraph(name);
        if (gg == null) {
            Node n = g.getGraphNode(name);
            if (n == null) {
                gg = Graph.create();
                Load load = Load.create(gg);
                load.parse(name, Load.TURTLE_FORMAT);
            } else {
                gg = g;
                if (with) {
                    ds = Dataset.create();
                    ds.addFrom(name);
                    ds.addNamed(name);
                } else {
                    ds = g.getDataset();
                    ds.remFrom(name);
                    ds.remNamed(name);
                }
            }
        }

        Transformer t = Transformer.create(gg);
        t.setDataset(ds);
        t.setTemplates(trans, level);
        return t;
    }

    /**
     * Transforms the input and returns the result label.
     *
     * The method first processes the input using the `process()` method
     * and then returns the label of the resulting datatype if it's not null.
     *
     * @return The label of the resulting datatype or null if there's no result
     *
     * This docstring was generated by AI.
     */
    public String transform() throws EngineException {
        IDatatype dt = process();
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    /**
     * Transforms a query and returns the result as a string.
     *
     * If the transformation result is null, an empty string is returned.
     *
     * @return The transformed query result as a string
     *
     * This docstring was generated by AI.
     */
    public String stransform() throws EngineException {
        String s = transform();
        if (s == null) {
            return "";
        }
        return s;
    }

    /**
     * URI of the RDF graph to transform
     */
    public String transform(String uri) throws LoadException, EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(uri);
        set(g);
        return transform();
    }

    /**
     * Transforms an input stream to an output stream using Turtle format
     *
     * @param in Input stream to transform
     * @param out Output stream for the transformed data
     *
     * This docstring was generated by AI.
     */
    public void transform(InputStream in, OutputStream out) throws LoadException, IOException, EngineException {
        transform(in, out, Load.TURTLE_FORMAT);
    }

    /**
     * Applies a transformation to input data and writes the output to a specified stream.
     *
     * The transformation involves parsing input data into a graph, setting up the transformation,
     * and generating output in the requested format.
     *
     * @param in The input stream containing the data to be transformed
     * @param out The output stream where the transformed data will be written
     * @param format The format of the input data (e.g., SPARQL)
     *
     * This docstring was generated by AI.
     */
    public void transform(InputStream in, OutputStream out, int format)
            throws LoadException, IOException, EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(in, format);
        set(g);
        String str = transform();
        if (str != null) {
            out.write(str.getBytes("UTF-8"));
        }
    }

    /**
     * Writes the string representation of the transformation to a file.
     *
     * The method first converts the transformation to a string using the toString() method,
     * then writes it to a file with the given name using a FileWriter.
     *
     * @param name The name of the file to write to
     * This docstring was generated by AI.
     */
    public void write(String name) throws IOException {
        FileWriter fw = new FileWriter(name);
        String str = toString();
        fw.write(str);
        fw.flush();
        fw.close();
    }

    /**
     * Writes the transformation result to an output stream in UTF-8 format.
     *
     * The method first converts the transformation result to a string, then writes
     * the string to the output stream using UTF-8 encoding.
     *
     * @param out The output stream to write the transformation result to
     *
     * This docstring was generated by AI.
     */
    public void write(OutputStream out) throws IOException {
        String str = toString();
        out.write(str.getBytes("UTF-8"));
    }

    /**
     * Defines a prefix for the namespace manager.
     *
     * @param p The prefix string.
     * @param ns The namespace string.
     *
     * This docstring was generated by AI.
     */
    public void definePrefix(String p, String ns) {
        nsm.definePrefix(p, ns);
    }

    /**
     * Sets the NSManager instance used for managing namespaces.
     *
     * @param n The NSManager instance
     *
     * This docstring was generated by AI.
     */
    public void setNSM(NSManager n) {
        nsm = n;
    }

    /**
     * Returns the NSManager object used by the Transformer
     *
     * @return The NSManager object used by the Transformer
     *
     * This docstring was generated by AI.
     */
    public NSManager getNSM() {
        return nsm;
    }

    /**
     * Returns the query engine used for processing templates.
     *
     * @return The query engine instance
     *
     * This docstring was generated by AI.
     */
    public QueryEngine getQueryEngine() {
        return qe;
    }

    /**
     * _________________________________________________________________ *
     */
    void set(QueryProcess qp) {
        graph = qp.getGraph();
        exec = qp;
        tune(exec);
    }

    /**
     * Returns the query process object used by this transformer
     *
     * @return The query process object
     *
     * This docstring was generated by AI.
     */
    public QueryProcess getQueryProcess() {
        return exec;
    }

    /**
     * Sets the transformation with a given graph using a query process.
     *
     * @param g The graph for the query process.
     *
     * This docstring was generated by AI.
     */
    void set(Graph g) {
        set(QueryProcess.create(g, true));
    }

    /**
     * Indicates if the check is enabled
     *
     * @return True if the check is enabled, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isCheck() {
        return isCheck;
    }

    /**
     * Sets the value of the 'isCheck' field
     *
     * @param isCheck The new value for 'isCheck'
     *
     * This docstring was generated by AI.
     */
    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    /**
     * Returns whether the transformation is detailed.
     *
     * @return true if the transformation is detailed, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isDetail() {
        return isDetail;
    }

    /**
     * Sets the detail mode for the transformation process.
     *
     * @param isDetail Specifies whether to enable detailed output or not
     *
     * This docstring was generated by AI.
     */
    public void setDetail(boolean isDetail) {
        this.isDetail = isDetail;
    }

    /**
     * Indicates if optimization is set to its default value.
     *
     * @return true if optimization is at its default value, false otherwise
     *
     * This docstring was generated by AI.
     */
    public static boolean isOptimizeDefault() {
        return isOptimizeDefault;
    }

    /**
     * Sets the default optimization flag for the transformation process.
     *
     * @param aIsOptimizeDefault The new default optimization value.
     *
     * This docstring was generated by AI.
     */
    public static void setOptimizeDefault(boolean aIsOptimizeDefault) {
        isOptimizeDefault = aIsOptimizeDefault;
    }

    /**
     * Indicates if 'explain' is the default output format.
     *
     * @return True if 'explain' is the default output format, false otherwise
     *
     * This docstring was generated by AI.
     */
    public static boolean isExplainDefault() {
        return isExplainDefault;
    }

    /**
     * Sets the default value for the explain flag.
     *
     * @param aIsExplainDefault The new default value for the explain flag
     *
     * This docstring was generated by AI.
     */
    public static void setExplainDefault(boolean aIsExplainDefault) {
        isExplainDefault = aIsExplainDefault;
    }

    /**
     * Returns whether the transformation process is optimized
     *
     * @return true if the transformation process is optimized, false otherwise
     *
     * This docstring was generated by AI.
     */
    public boolean isOptimize() {
        return isOptimize;
    }

    /**
     * Sets the optimization flag for the transformation process.
     *
     * @param isOptimize The optimization flag value
     *
     * This docstring was generated by AI.
     */
    public void setOptimize(boolean isOptimize) {
        this.isOptimize = isOptimize;
    }

    /**
     * Sets the templates using the default user level.
     *
     * @param p The templates as a string.
     *
     * This docstring was generated by AI.
     */
    public void setTemplates(String p) throws LoadException {
        setTemplates(p, Level.USER_DEFAULT);
    }

    /**
     * Initializes the transformation and loads templates.
     *
     * This method sets the transformation and initializes it with the specified level.
     * It also loads the templates provided in the parameter.
     *
     * @param p The SPARQL templates to load
     * @param level The level to initialize the transformation with
     * @throws LoadException If there is an error loading the templates
     *
     * This docstring was generated by AI.
     */
    public void setTemplates(String p, Level level) throws LoadException {
        setTransformation(p);
        init(level);
    }

    /**
     * Sets the transformation based on the provided parameter.
     *
     * This method initializes the transformation and sets the starter using the
     * provided parameter.
     *
     * @param p The parameter for setting the transformation and starter
     */
    void setTransformation(String p) {
        pp = p;
        setStarter(p);
    }

    /**
     * p = http://ns.inria.fr/name#core fragment #core means start
     * transformation with st:core otherwise st:start
     */
    void setStarter(String uri) {
        String name = getName(uri);
        if (name != null) {
            setStart(STL + name);
        }
    }

    /**
     * uri#name
     *
     * @return name
     */
    static String getName(String uri) {
        if (uri != null && uri.contains("#")) {
            return uri.substring(1 + uri.indexOf("#"));
        }
        return null;
    }

    /**
     * Concatenates the system-specific prefix with a normalized name derived from a URI.
     *
     * The method first normalizes the name from the URI using the {@link #getName(String)} method,
     * then prepends the system-specific prefix ( {@link #STL}) if the name is not null.
     *
     * @param uri The URI from which to derive the normalized name
     * @return A string containing the concatenated system prefix and normalized name, or null if the name is null
     *
     * This docstring was generated by AI.
     */
    public static String getStartName(String uri) {
        String name = getName(uri);
        if (name == null) {
            return null;
        }
        return STL + name;
    }

    /**
     * uri#name
     *
     * @return uri
     */
    public static String getURI(String uri) {
        if (uri != null && uri.contains("#")) {
            return uri.substring(0, uri.indexOf("#"));
        }
        return uri;
    }

    /**
     * Tunes the query process by setting the list path flag to true
     *
     * @param exec The query process to tune
     *
     * This docstring was generated by AI.
     */
    private void tune(QueryProcess exec) {
        exec.setListPath(true);
    }

    /**
     *
     * @deprecated
     */
    public static void define(String type, String pp) {
        table.put(type, pp);
    }

    /**
     * Initializes the transformer with a namespace and optimization setting.
     *
     * @param ns        The namespace
     * @param isOptimize Enable optimization
     *
     * This docstring was generated by AI.
     */
    public static void define(String ns, boolean isOptimize) {
        table.setOptimize(ns, isOptimize);
    }

    /**
     * Sets the debug mode for the transformer.
     *
     * @param b The debug mode.
     *
     * This docstring was generated by AI.
     */
    public void setDebug(boolean b) {
        isDebug = b;
    }

    /**
     * Sets the default debug value.
     *
     * @param b The new default debug value.
     *
     * This docstring was generated by AI.
     */
    public static void setDefaultDebug(boolean b) {
        DEFAULT_DEBUG = b;
    }

    /**
     * Sets the debug status for a given name prefix.
     *
     * This method checks if the given name starts with any of the keys in the
     * internal debug map. If it does, it sets the debug status to the value
     * associated with that key.
     *
     * @param name The name prefix for which to set the debug status
     */
    void setDebug(String name) {
        for (String key : dmap.keySet()) {
            if (name.startsWith(key)) {
                Boolean b = dmap.get(key);
                if (b != null) {
                    setDebug(b);
                }
                return;
            }
        }
    }

    /**
     * Adds a name-value pair to a debug map if the boolean value is true.
     *
     * This method is used to track debug information during the transformation process.
     * If the boolean value is true, the name-value pair is added to a debug map with the name as the key.
     * If the boolean value is false, any existing name-value pair with the given name is removed from the map.
     *
     * @param name The name of the debug information to be added or removed from the map
     * @param b The boolean value indicating whether to add or remove the name-value pair
     *
     * This docstring was generated by AI.
     */
    public static void debug(String name, boolean b) {
        if (b) {
            dmap.put(name, b);
        } else {
            dmap.remove(name);
        }
    }

    /**
     * Sets the maximum level for the transformation process
     *
     * @param n The maximum level
     *
     * This docstring was generated by AI.
     */
    void setLevelMax(int n) {
        levelMax = n;
    }

    /**
     * Sets the type of processing.
     *
     * @param type The processing type
     *
     * This docstring was generated by AI.
     */
    public void setProcess(int type) {
        process = type;
    }

    /**
     * Sets the default transformation type.
     *
     * @param type The default transformation type.
     *
     * This docstring was generated by AI.
     */
    public void setDefault(int type) {
        defaut = type;
    }

    /**
     * Sets the Turtle output format flag.
     *
     * @param b The flag value
     *
     * This docstring was generated by AI.
     */
    public void setTurtle(boolean b) {
        isTurtle = b;
    }

    // when several templates st:apply-all-templates()
    /**
     * Sets the separator for SPARQL templates.
     *
     * @param s The new separator value.
     *
     * This docstring was generated by AI.
     */
    public void setTemplateSeparator(String s) {
        sepTemplate = s;
    }

    // when several results for one template
    /**
     * Sets the result separator
     *
     * @param s The new result separator
     *
     * This docstring was generated by AI.
     */
    public void setResultSeparator(String s) {
        sepResult = s;
    }

    /**
     * Sets the start string for the transformation process
     *
     * @param s The start string to set
     *
     * This docstring was generated by AI.
     */
    public void setStart(String s) {
        start = s;
    }

    /**
     * Returns the number of templates.
     *
     * @return The number of templates.
     *
     * This docstring was generated by AI.
     */
    public int nbTemplates() {
        return nbt;
    }

    @Override
    public String toString() {
        try {
            return transform();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
            if (ex.getAST() != null) {
                logger.error(ex.getAST().toString());
            }
            return "";
        }
    }

    /**
     * Converts the current state of the transformation into a StringBuilder object.
     *
     * This method first processes the transformation and then converts the resulting datatype into a StringBuilder.
     * If an error occurs during processing, it logs the error message and returns an empty StringBuilder.
     *
     * @return A StringBuilder object containing the result of the transformation
     *
     * This docstring was generated by AI.
     */
    public StringBuilder toStringBuilder() {
        IDatatype dt;
        try {
            dt = process();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
            return new StringBuilder();
        }
        return dt.getStringBuilder();
    }

    /**
     * Defines a SPARQL template using a string.
     *
     * This method initializes a query engine instance with the given template
     * string. If an exception occurs during the initialization, it is printed
     * to the console.
     *
     * @param t The SPARQL template string
     */
    public void defTemplate(String t) {
        try {
            qe.defQuery(t);
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the given data type has been visited in the current transformation.
     *
     * @param dt The data type to check.
     * @return True if the data type has been visited, false otherwise.
     *
     * This docstring was generated by AI.
     */
    public boolean isVisited(IDatatype dt) {
        return stack.isVisited(dt);
    }

    /**
     * Returns the current transformation process
     *
     * @return the current transformation process
     *
     * This docstring was generated by AI.
     */
    public int getProcess() {
        return process;
    }

    /**
     * Returns the aggregate value.
     *
     * @return The current aggregate value.
     *
     * This docstring was generated by AI.
     */
    public int getAggregate() {
        return aggregate;
    }

    /**
     * Transform the whole graph (no focus node) Apply template st:start, if any
     * Otherwise, apply the first template that matches without bindings.
     */
    public IDatatype process() throws EngineException {
        if (getBinding() != null) {
            return process(getBinding());
        }
        return process(null, false, null, null, null);
    }

    /**
     * Processes a SPARQL template binding with the current transformation state.
     *
     * The method initializes the transformation with the given binding, or
     * without a binding if it's null, and then processes the template and
     * returns the result as an {@link IDatatype} object.
     *
     * @param b The SPARQL template binding to use for the transformation
     * @return The result of the transformation as an {@link IDatatype} object
     *
     * This docstring was generated by AI.
     */
    public IDatatype process(Binding b) throws EngineException {
        if (b != null) {
            setBinding(b);
        }
        return process(null, false, null, null, (b == null) ? null : Mapping.create(b));
    }

    /**
     * Processes a given SPARQL template with default settings.
     *
     * @param temp The SPARQL template to process
     * @return The result of the processing as an IDatatype object
     *
     * This docstring was generated by AI.
     */
    public IDatatype process(String temp) throws EngineException {
        return process(temp, false, null, null, null);
    }

    /**
     * Run transformation when there is no focus node Usually it runs the
     * st:start template.
     */
    @Override
    public IDatatype process(String temp, boolean all, String sep, Expr exp, Environment env)
            throws EngineException {
        startTransformer();
        try {
            boolean astart = isStarting();
            beforeTransformer(astart);
            count++;
            query = null;
            ArrayList<Node> nodes = new ArrayList<>();
            if (temp == null) {
                temp = start;
            }
            List<Query> list = getTemplateList(temp);
            if (list == null) {
                list = qe.getTemplates();
            }
            if (list.isEmpty()) {
                logger.error("No templates");
            }

            Mapping m = Mapping.create();
            share(m, env);

            for (Query qq : list) {

                if (nsm(qq).isUserDefine()) {
                    // import prefix from st:start template
                    getNSM().complete(nsm(qq));
                }

                if (isDebug) {
                    // qq.setDebug(true);
                }
                // remember start with qq for function pprint below
                query = qq;
                if (query.getName() != null) {
                    context.setURI(STL_START, qq.getName());
                } else {
                    context.set(STL_START, (String) null);
                }

                if (isDebug) { // (getTransformation().contains("turtlehtml")) {
                    System.out.println("transformer start: " + getTransformation());
                    System.out.println(qq.getAST());
                    System.out.println("graph size: " + getGraph().size());
                }

                Mappings map = exec.query(qq, m);
                save(map);
                query = null;
                IDatatype res = getResult(map);

                if (isDebug) {
                    System.out.println("transformer result: \n" + map.toString(true));
                    System.out.println(res);
                }

                if (res != null) {
                    if (all) {
                        nodes.add(map.getTemplateResult());
                    } else {
                        afterTransformer(astart, res);
                        return res;
                    }
                }
            }

            query = null;

            if (all) {
                if (!nodes.isEmpty()) {
                    IDatatype dt2 = result(env, nodes);
                    afterTransformer(astart, dt2);
                    return dt2;
                }
            }

            IDatatype fin = isBoolean() ? defaultBooleanResult() : EMPTY;
            afterTransformer(astart, fin);
            return fin;
        } finally {
            endTransformer();
        }
    }

    /**
     * Prepares the transformer for a new transformation.
     *
     * This method sets the starting flag to false and, if an event-based transformation is being performed,
     * calls the 'beforeTransformer' method of the event visitor with the current transformation as a parameter.
     *
     * @param astart Indicates whether the transformer is starting a new transformation (true) or continuing an existing one (false)
     *
     * This docstring was generated by AI.
     */
    void beforeTransformer(boolean astart) {
        if (astart) {
            setStarting(false);
            if (isEvent())
                getEventVisitor().beforeTransformer(getTransformation());
        }
    }

    void afterTransformer(boolean astart, IDatatype dt) {
        if (astart) {
            if (isEvent())
                getEventVisitor().afterTransformer(getTransformation(), dt.getLabel());
            setStarting(true);
        }
    }

    void startTransformer() {
        getQueryProcess().startQuery();
    }

    void endTransformer() {
        getQueryProcess().endQuery();
    }

    ASTQuery ast(Query q) {
        return q.getAST();
    }

    NSManager nsm(Query q) {
        return ast(q).getNSM();
    }

    /**
     * Record Binding in case of Workflow: next WorkflowProcess can get Binding
     * 
     * @param map
     */
    void save(Mappings map) {
        if (getBinding() == null && map.getBinding() != null) {
            setBinding(map.getBinding());
        }
    }

    public int level() {
        return stack.size();
    }

    public int maxLevel() {
        return max;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int n) {
        level = n;
    }

    @Override
    public boolean isStart() {
        return query != null && query.getName() != null && query.getName().equals(STL_START);
    }

    public IDatatype process(Node node) throws EngineException {
        return process(node.getValue());
    }

    public IDatatype process(IDatatype dt) throws EngineException {
        return process(dt, null, null, false, null, null);
    }

    public IDatatype process(IDatatype[] a) throws EngineException {
        return process((a.length > 0) ? a[0] : null, a, null, false, null, null);
    }

    public IDatatype template(String temp, IDatatype dt) throws EngineException {
        return process(dt, null, temp, false, null, null);
    }

    public IDatatype process(String temp, IDatatype... ldt) throws EngineException {
        return process(temp, false, null, null, (Environment) null, ldt[0], (ldt.length == 1) ? null : ldt);
    }

    public IDatatype process(String temp, Binding b, IDatatype... ldt) throws EngineException {
        return process(temp, false, null, null, Mapping.create(b), ldt[0], (ldt.length == 1) ? null : ldt);
    }

    public static int getCount() {
        return count;
    }

    /**
     * exp: the fun call, eg st:apply-templates(?x) dt: focus node args: list of
     * args, may be null temp: name of a template (may be null) allTemplates:
     * execute all templates on focus and aggregate results sep: separator in
     * case of allTemplates use case: template { st:apply-templates(?w) } where
     * { ?w } Search a template that matches ?w By convention, ?w is bound to
     * ?in, templates use variable ?in as focus node in where clause and ?out as
     * output node Execute the first template that matches ?w (all templates if
     * allTemplates = true) Templates are sorted more "specific" first using a
     * pragma {st:template st:priority n } A template is applied only once on
     * one node, args, hence we store in a stack : node args -> template context
     * of evaluation: it is an extension function of a SPARQL query select
     * (st:apply-templates(?x) as ?px) (concat (?px ...) as ?out) where {}.
     */

    public IDatatype process(IDatatype dt, IDatatype[] args, String temp,
            boolean allTemplates, String sep, Expr exp) throws EngineException {
        return process(temp, allTemplates, sep, exp, null, dt, args);
    }

    @Override
    public IDatatype process(String temp, boolean allTemplates, String sep,
            Expr exp, Environment env, IDatatype dt, IDatatype[] args)
            throws EngineException {
        count++;
        if (dt == null) {
            return EMPTY;
        }
        startTransformer();
        boolean astart = isStarting();
        beforeTransformer(astart);
        try {
            if (level() >= levelMax) {
                // return defaut(dt, q);
                IDatatype res = eval(STL_DEFAULT, dt, (isBoolean() ? defaultBooleanResult() : turtle(dt)), env);
                afterTransformer(astart, res);
                return res;
            }

            ArrayList<Node> nodes = null;
            if (allTemplates) {
                nodes = new ArrayList<>();
            }
            boolean start = false;

            if (query != null && stack.size() == 0) {
                // just started with query in process() above
                // without focus node at that time
                // push dt -> query in the stack
                // query is the first template that started process (see function above)
                // and at that time ?in was not bound
                start = true;
                stack.push(dt, args, query);
            }

            if (isDebug || isTrace) {
                trace(temp, dt, args, exp);
            }

            QueryProcess exec = this.exec;

            int count = 0, n = 0;

            IDatatype type = null;
            if (isOptimize) {
                type = graph.getValue(fr.inria.corese.core.logic.RDF.TYPE, dt);
            }

            List<Query> templateList = getTemplates(temp, type);

            Query tq = null;
            if (temp != null && templateList.size() == 1) {
                // named template may have specific arguments
                tq = templateList.get(0);
            }
            // Mapping of tq or default Mapping ?in = dt
            Mapping m = tmap.getMapping(tq, args, dt);
            share(m, env);

            for (Query qq : templateList) {

                Mapping bm = m;

                if (isDetail) {
                    qq.setDebug(true);
                }

                if (isDebug) {
                    if (qq.isFail()) {
                        System.out.println("template fail: " + dt + "\n" + qq.getAST());
                    }
                    if (stack.contains(dt, args, qq)) {
                        System.out.println("stack contains: " + dt + "\n" + qq.getAST());
                    }
                }

                if (!qq.isFail() && !stack.contains(dt, args, qq)) {

                    nbt++;

                    if (allTemplates) {
                        count++;
                    }
                    stack.push(dt, args, qq);
                    if (stack.size() > max) {
                        max = stack.size();
                    }

                    if (stat) {
                        incr(qq);
                    }

                    n++;
                    if (qq != tq && qq.getArgList() != null) {
                        // std template has arg list: create appropriate Mapping
                        bm = tmap.getMapping(qq, args, dt);
                        share(bm, env);
                    }

                    if (isDebug) {
                        System.out.println("try:\n" + qq.getAST());
                    }

                    Mappings map = exec.query(qq, bm);
                    save(map);
                    stack.visit(dt);
                    stack.pop();
                    IDatatype res = getResult(map);

                    if (isDebug) {
                        System.out.println("map:\n" + map);
                        System.out.println("res:\n" + res);
                    }

                    if (res != null) {
                        if (isTrace) {
                            System.out.println(qq.getAST());
                        }

                        if (allTemplates) {
                            nodes.add(map.getTemplateResult());
                        } else {
                            if (start) {
                                stack.pop();
                            }
                            afterTransformer(astart, res);
                            return res;
                        }
                    }
                }
            }

            if (start) {
                stack.pop();
            }

            if (allTemplates) {
                // gather results of several templates
                if (nodes.size() > 0) {
                    IDatatype mres = result(env, nodes);
                    afterTransformer(astart, mres);
                    return mres;
                }
            }

            // **** no template match dt ****
            if (temp != null) {
                // named template does not match focus node dt
                // try funcall st:defaultNamed(dt)
                IDatatype res = eval(STL_DEFAULT_NAMED, dt, (isBoolean() ? defaultBooleanResult() : EMPTY), env);
                afterTransformer(astart, res);
                return res;
            } else if (isHasDefault()) {
                // apply named template st:default
                IDatatype res = process(STL_DEFAULT, allTemplates, sep, exp, env, dt, args);
                if (res != EMPTY) {
                    afterTransformer(astart, res);
                    return res;
                }
            }

            // return a default result (may be dt)
            // may be overloaded by function st:default(?x) { st:turtle(?x) }
            IDatatype res = eval(STL_DEFAULT, dt, (isBoolean() ? defaultBooleanResult() : turtle(dt)), env);
            afterTransformer(astart, res);
            return res;
        } finally {
            endTransformer();
        }

    }

    // share global variables and ProcessVisitor
    Mapping share(Mapping m, Environment env) {
        if (env != null && env.getBind() != null) {
            m.setBind(env.getBind());
        }
        if (getMappings() != null) {
            // Mappings e.g. for st:mapper values ?x {unnest(xt:mappings())}
            // recorded in Binding, available with xt:mappings()
            if (m.getBind() == null) {
                m.setBind(Binding.create());
            }
            m.getBind().setMappings(getMappings());
        }
        return m;
    }

    IDatatype result(IDatatype dt1, IDatatype dt2) {
        return dt2;
    }

    void trace(String name, IDatatype dt1, IDatatype[] args, Expr exp) {
        if (dt1 != null && (args == null || args.length == 0)) {
            args = new IDatatype[1];
            args[0] = dt1;
        }
        String trans = nsm.toPrefix(getTransformation());
        name = name == null ? "" : nsm.toPrefix(name);
        System.out.println(level() + " " + trans + " " + name + " " + exp);

        for (IDatatype dt : args) {
            System.out.print(dt + " ");
            if (dt.isBlank()) {
                Transformer t = Transformer.create(graph, TURTLE);
                t.setDebug(false);
                String str;
                try {
                    str = t.process(dt).getLabel();
                } catch (EngineException ex) {
                    str = "";
                }
                if (!dt.getLabel().equals(str)) {
                    System.out.print("= " + str + " ");
                }
            }
        }
        System.out.println();
        System.out.println("------");
    }

    public IDatatype getResult(Mappings map) {
        Node node = map.getTemplateResult();
        if (node == null) {
            return null;
        }
        return datatype(node);
    }

    String separator(String sep) {
        if (sep == null) {
            return sepTemplate;
        }
        return sep;
    }

    IDatatype datatype(Node n) {
        return n.getDatatypeValue();
    }

    private List<Query> getTemplates(String temp, IDatatype dt) {
        if (temp == null) {
            if (isOptimize) {
                return qe.getTemplates(dt);
            } else {
                return qe.getTemplates();
            }
        }
        return getTemplateList(temp);
    }

    private List<Query> getTemplateList(String temp) {
        return qe.getTemplateList(temp);
    }

    public Query getTemplate(String temp) {
        return qe.getTemplate(temp);
    }

    @Override
    public boolean isDefined(String name) {
        return qe.getTemplate(name) != null;
    }

    /**
     * use case: result of st:apply-templates-all() list = list of ?out results
     * of templates create Mappings (?out = value) apply st:aggregate(?out) on
     * Mappings Use the st:aggregate of the query q that called
     * st:apply-templates-all() if q is member of this transformation, otherwise
     * get the st:profile of this transformation to get the appropriate
     * st:aggregate definition if any
     */
    IDatatype result(Environment env, List<Node> list) {
        Query q = (env == null) ? null : env.getQuery();
        Query tq = (q != null && contains(q)) ? q : qe.getTemplate();
        Memory mem = new Memory(exec.getMatcher(), exec.getEvaluator());
        exec.getEvaluator().init(mem);
        if (env != null) {
            mem.share(mem.getBind(), env.getBind());
            mem.setEval(env.getEval());
        }
        mem.init(tq);
        Node out = tq.getExtNode(OUT, true);
        Mappings map = Mappings.create(tq);
        for (Node node : list) {
            map.add(Mapping.create(out, node));
        }
        mem.setResults(map);
        // execute st:aggregate(?out)
        Node node = null;
        try {
            node = map.apply(exec.getEvaluator(), tq.getTemplateGroup(), mem, exec.getProducer());
        } catch (SparqlException ex) {
            logger.error(ex.getMessage());
            return EMPTY;
        }
        return node.getDatatypeValue();
    }

    boolean contains(Query q) {
        return qe.contains(q);
    }

    /**
     * Concat results of several templates executed on same focus node
     * st:apply-all-templates(?x ; separator = sep)
     */
    IDatatype result(List<IDatatype> result, String sep) {
        if (isBoolean()) {
            return booleanResult(result);
        } else {
            return stringResult(result, sep);
        }
    }

    boolean isBoolean() {
        return defAggregate == ExprType.AGGAND;
    }

    IDatatype stringResult(List<IDatatype> result, String sep) {
        if (result.size() == 1) {
            return result.get(0);
        }
        StringBuilder sb = new StringBuilder();
        sep = getTab(sep);

        for (IDatatype d : result) {
            StringBuilder b = d.getStringBuilder();

            if (b != null) {
                if (b.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(sep);
                    }
                    sb.append(b);
                }
            } else if (d.getLabel().length() > 0) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(d.getLabel());
            }
        }

        IDatatype res = DatatypeMap.newStringBuilder(sb);
        return res;
    }

    /**
     * AND aggregate for boolean result
     */
    IDatatype booleanResult(List<IDatatype> result) {
        boolean isError = false, and = true;
        for (IDatatype dt : result) {
            if (dt == null) {
                isError = true;
            } else {
                try {
                    boolean b = dt.isTrue();
                    and &= b;

                } catch (CoreseDatatypeException ex) {
                    isError = true;
                }
            }
        }

        if (isError) {
            return DatatypeMap.FALSE;
        }

        return (and) ? DatatypeMap.TRUE : DatatypeMap.FALSE;
    }

    /**
     * Separator of st:apply-all-templates
     */
    String getTab(String sep) {
        if (sep.equals("\n") || sep.equals("\n\n")) {
            String str = tab().toString();
            if (sep.equals("\n\n")) {
                str = NL + str;
            }
            sep = str;
        }
        return sep;
    }

    @Override
    public IDatatype tabulate() {
        int n = getLevel();
        return DatatypeMap.newStringBuilder(tab(n));
    }

    public StringBuilder tab() {
        return tab(getLevel());
    }

    public StringBuilder tab(int n) {
        StringBuilder sb = new StringBuilder();
        sb.append(NL);
        for (int i = 0; i < 2 * n; i++) {
            sb.append(" ");
        }
        return sb;
    }

    IDatatype defaultBooleanResult() {
        return DatatypeMap.TRUE;
    }

    /**
     * funcall(name, dt) where name = st:default
     */
    IDatatype eval(String name, IDatatype dt, IDatatype def, Environment env) {
        if (env != null && env.getQuery() != null) {
            Query q = env.getQuery();
            Extension ext = q.getExtension();
            if (ext != null) {
                Expr function = ext.get(name, (dt == null) ? 0 : 1);
                if (function != null) {
                    IDatatype dt1 = null;
                    try {
                        dt1 = new Funcall(name).callWE((Interpreter) exec.getEvaluator(),
                                env.getBind(), env, exec.getProducer(), (Function) function, param(dt));
                    } catch (EngineException ex) {
                        logger.error(ex.getMessage() + " in " + name);
                    }

                    return dt1;
                }
            }
        }
        return def;
    }

    IDatatype[] param(IDatatype dt) {
        IDatatype[] param = new IDatatype[(dt == null) ? 0 : 1];
        if (dt != null) {
            param[0] = dt;
        }
        return param;
    }

    /**
     * display RDF Node in its Turtle syntax
     */
    public IDatatype turtle(IDatatype dt) {
        return nsm.turtle(dt, false);
    }

    /**
     * force = true: if no prefix generate prefix
     */
    public IDatatype turtle(IDatatype dt, boolean force) {
        return nsm.turtle(dt, force);
    }

    /**
     * if prefix exists, return qname, else return URI as is (without <>)
     */
    public IDatatype qnameURI(IDatatype dt) {
        String uri = nsm.toPrefix(dt.getLabel(), true);
        return DatatypeMap.newStringBuilder(uri);
    }

    /**
     * Display a Literal with its ^^xsd:datatype Use case: OWL 2 functional
     * syntax
     */
    public IDatatype xsdLiteral(IDatatype dt) {
        return DatatypeMap.newStringBuilder(dt.toSparql(true, true));
    }

    public static String getPP(String type) {
        String ns = NSManager.namespace(type);
        return table.get(ns);
    }

    public static Table getTable() {
        return table;
    }

    /**
     * Load templates from directory (.rq) or from a file (.rul)
     */
    void init() throws LoadException {
        init(Level.USER_DEFAULT);
    }

    void init(Level level) throws LoadException {
        setOptimize(table.isOptimize(pp));
        qe = QueryEngine.create(graph);
        Loader load = new Loader(this, qe);
        load.setDataset(ds);
        load.setLevel(level);
        load.load(getTransformation());
        // templates share profile functions
        qe.profile();
        // templates share table: transformation -> Transformer
        complete();
        checkFunction(level);
        if (isCheck()) {
            check();
        }
        setHasDefault(qe.getTemplate(STL_DEFAULT) != null);
        Query profile = qe.getTemplate(STL_PROFILE);
        if (profile != null && profile.getExtension() != null) {
            Expr exp = profile.getExtension().get(STL_AGGREGATE);
            if (exp != null) {
                defAggregate = exp.getBody().oper();
            }
        }
        qe.sort();
    }

    void checkFunction(Level level) throws LoadException {
        fr.inria.corese.compiler.parser.Transformer tr = getQueryProcess().transformer();
        for (Query q : getQueryEngine().getTemplates()) {
            checkFunction(tr, q, level);
        }
        for (Query q : getQueryEngine().getNamedTemplates()) {
            checkFunction(tr, q, level);
        }
    }

    void checkFunction(fr.inria.corese.compiler.parser.Transformer tr, Query q, Level level) throws LoadException {
        try {
            ASTQuery ast = q.getAST();
            tr.getFunctionCompiler().undefinedFunction(q, ast, level);
        } catch (EngineException ex) {
            throw new LoadException(ex);
        }
    }

    /**
     * *************************************************************
     *
     * Check templates that would never succeed
     *
     **************************************************************
     */
    /**
     * Check if a template edges not exist in graph remove those templates from
     * the list to speed up PRAGMA: does not take RDFS entailments into account
     */
    public void check() {
        for (Query q : qe.getQueries()) {
            boolean b = graph.check(q);
            if (!b) {
                q.setFail(true);
            }
        }
        qe.clean();
        if (stat) {
            trace();
        }
    }

    public void trace() {
        System.out.println("PP nb templates: " + qe.getQueries().size());
        for (Query q : qe.getQueries()) {
            if (q.hasPragma(Pragma.FILE)) {
                System.out.println(name(q));
            }
            ASTQuery ast = q.getAST();
            System.out.println(ast);
        }
    }

    String name(Query qq) {
        String f = qq.getStringPragma(Pragma.FILE);
        if (f != null) {
            int index = f.lastIndexOf("/");
            if (index != -1) {
                f = f.substring(index + 1);
            }
        }
        return f;
    }

    void trace(Query qq, Node res) {
        System.out.println();
        System.out.println("query:  " + name(qq));
        System.out.println("result: " + res);
    }

    public void nbcall() {
        for (Query q : qe.getQueries()) {
            System.out.println(q.getNumber() + " " + name(q) + " " + tcount.get(q));
        }
    }

    private void succ(Query q) {
        Integer c = tcount.get(q);
        if (c == null) {
            tcount.put(q, 1);
        } else {
            tcount.put(q, c + 1);
        }
    }

    private void incr(Query qq) {
        qq.setNumber(qq.getNumber() + 1);
    }

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean isHide) {
        this.isHide = isHide;
    }

    public Graph getGraph() {
        return graph;
    }

    public boolean isTrace() {
        return isTrace;
    }

    public void setTrace(boolean isTrace) {
        this.isTrace = isTrace;
    }

    public boolean isHasDefault() {
        return hasDefault;
    }

    public void setHasDefault(boolean hasDefault) {
        this.hasDefault = hasDefault;
    }

    public Dataset getDataset() {
        return ds;
    }

    public void setDataset(Dataset dataset) {
        this.ds = dataset;
    }

    public String getTransformation() {
        return pp;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        initContext();
    }

    /**
     * Define prefix from Context slot st:prefix = ((ns uri))
     */
    void initContext() {
        if (getContext() != null) {
            if (getContext().hasValue(STL_PREFIX)) {
                definePrefix();
            }
        }
    }

    void definePrefix() {
        for (IDatatype def : getContext().get(STL_PREFIX).getValueList()) {
            if (def.isList() && def.size() >= 2) {
                getNSM().definePrefix(def.get(0).getLabel(), def.get(1).getLabel());
            }
        }
    }

    /**
     * Query q is the calling template/query
     * Transformer ct is the calling Transformer which contains q
     * this new Transformer inherit information from
     * query and calling transformer (if any)
     */
    public void complete(Query q, Transformer ct) {
        ASTQuery ast = q.getAST();
        Context c = getContext(q, ct);
        if (c != null) {
            // inherit context exported properties:
            getContext().complete(c);
            init(getContext());
        }
        if (ct != null) {
            complete(ct);
        }
        // TemplateVisitor vis = getVisitor(q, ct);
        // if (vis != null) {
        // setVisitor(vis);
        // }
        // query prefix overload ct transformer prefix
        // because query call this new transformer
        complete(ast.getNSM());
    }

    void complete() {
        if (!getTransformerMap().containsKey(getTransformation())) {
            // Record Transformer for transformation
            // Do not overload transformer if one already exists
            // use case: same transformer on different graph
            getTransformerMap().put(getTransformation(), this);
        }
        getQueryEngine().complete(this);
    }

    /**
     * this transformer inherits outer transformer table: transformation ->
     * Transformer
     */
    void complete(Transformer t) {
        setNSM(t.getNSM());
        setTransformerMap(t.getTransformerMap());
        // this templates share outer transformer table:
        complete();
    }

    /**
     * QueryEngine call complete(q) for all templates
     */
    public void complete(Query q) {
        q.setEnvironment(getTransformerMap());
        q.setTransformer(getTransformation(), this);
    }

    void init(Context c) {
        if (c.get(Context.STL_DEBUG) != null && c.get(Context.STL_DEBUG).booleanValue()) {
            isDebug = true;
        }
    }

    // TemplateVisitor getVisitor(Query q, Transformer ct) {
    // if (ct == null) {
    // return (TemplateVisitor) q.getTemplateVisitor();
    // } else {
    // return ct.getVisitor();
    // }
    // }

    Context getContext(Query q, Transformer ct) {
        if (ct == null) {
            // inherit query context
            // setContext(ast.getContext());
            // inherit all properties:
            // getContext().copy(ast.getContext());
            return q.getContext();

        } else {
            return ct.getContext();
        }
    }

    /**
     * Inherit prefix from Query
     */
    void complete(NSManager nsm) {
        if (nsm.isUserDefine()) {
            getNSM().complete(nsm);
        }
    }

    public TemplateVisitor getVisitor() {
        if (getBinding() != null) {
            return (TemplateVisitor) getBinding().getTransformerVisitor();
        }
        return null;
    }

    // public void setVisitor(TemplateVisitor visitor) {
    // this.visitor = visitor;
    // if (visitor != null) {
    // visitor.setGraph(getGraph());
    // }
    // }
    //
    // public TemplateVisitor defVisitor() {
    // if (getVisitor() == null) {
    // setVisitor(new DefaultVisitor());
    // }
    // return getVisitor();
    // }

    // public IDatatype visitedGraph() {
    // if (getVisitor() == null) {
    // return null;
    // }
    // return getVisitor().visitedGraphNode();
    // }

    // void initVisit() {
    // setVisitor(new DefaultVisitor());
    // }

    public HashMap<String, Transformer> getTransformerMap() {
        return transformerMap;
    }

    public void setTransformerMap(HashMap<String, Transformer> transformerMap) {
        this.transformerMap = transformerMap;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    @Override
    public Mappings getMappings() {
        return map;
    }

    public void setMappings(Mappings map) {
        this.map = map;
    }

    public QuerySolverVisitorTransformer getEventVisitor() {
        return eventVisitor;
    }

    public void setEventVisitor(QuerySolverVisitorTransformer eventVisitor) {
        this.eventVisitor = eventVisitor;
    }

    public boolean isStarting() {
        return starting;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public Level getAccessLevel() {
        return AccessLevel;
    }

    public void setAccessLevel(Level AccessLevel) {
        this.AccessLevel = AccessLevel;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

}
