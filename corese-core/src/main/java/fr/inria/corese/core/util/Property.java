package fr.inria.corese.core.util;

import static fr.inria.corese.core.util.Property.Value.DATATYPE_ENTAILMENT;
import static fr.inria.corese.core.util.Property.Value.IMPORT;
import static fr.inria.corese.core.util.Property.Value.LOAD_RULE;
import static fr.inria.corese.core.util.Property.Value.PREFIX;
import static fr.inria.corese.core.util.Property.Value.SERVICE_HEADER;
import static fr.inria.corese.core.util.Property.Value.SERVICE_SEND_PARAMETER;
import static fr.inria.corese.core.util.Property.Value.STORAGE;
import static fr.inria.corese.core.util.Property.Value.STORAGE_MODE;
import static fr.inria.corese.core.util.Property.Value.VARIABLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.compiler.federate.RewriteBGPList;
import fr.inria.corese.compiler.federate.SelectorFilter;
import fr.inria.corese.compiler.federate.SelectorIndex;
import fr.inria.corese.core.EdgeFactory;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.index.EdgeManagerIndexer;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.load.ServiceParser;
import fr.inria.corese.core.producer.DataFilter;
import fr.inria.corese.core.query.CompileService;
import fr.inria.corese.core.query.MatcherImpl;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.ParserHandler;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import fr.inria.corese.sparql.triple.parser.visitor.ASTParser;

/**
 * Corese configuration with properties such as: LOAD_IN_DEFAULT_GRAPH
 * Usage:
 * Property.set(LOAD_IN_DEFAULT_GRAPH, true);
 * Property.load(property-file-path);
 * Property.init(graph);
 * corese-gui.jar -init property-file-path
 * corese-server.jar -init property-file-path
 * A property file example is in core resources/data/corese/property.properties
 * Define variable:
 * VARIABLE = $home=/user/home/
 * LOAD_DATASET = $home/file.ttl
 * 
 * Olivier Corby
 */
public class Property {

    private static Logger logger = LoggerFactory.getLogger(Property.class);
    final static String VAR_CHAR = "$";
    public static final String STAR = "*";
    final static String LOCAL = "./";
    final static String SEP = ";";
    final static String EQ = "=";
    private static Property singleton;
    private static final String STD = "std";
    public static final String RDF_XML = "rdf+xml";
    public static final String TURTLE = "turtle";
    public static final String TRIG = "trig";
    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String DATASET = "dataset";
    public static final String DB = "db";
    public static final String DB_ALL = "db_all";

    private HashMap<Value, Boolean> booleanProperty;
    private HashMap<Value, String> stringProperty;
    private HashMap<Value, Integer> integerProperty;
    private HashMap<Value, List<String>> listProperty;
    private HashMap<String, String> variableMap;
    private HashMap<String, String> imports;

    // Java Properties manage property file (at user option)
    private Properties properties;

    private String path, parent;
    private boolean debug = false;

    public enum Value {
        // VARIABLE = home=/home/name/dir
        VARIABLE,
        IMPORT,

        TRACE_MEMORY,
        TRACE_GENERIC,
        // generic property for testing purpose
        TEST_FEDERATE,
        // turtle file path where federation are defined
        FEDERATION,
        // generate partition of connected bgp
        FEDERATE_BGP,
        // do not split complete partition if any
        FEDERATE_PARTITION,
        // test and use join between right and left exp of optional
        FEDERATE_OPTIONAL,
        FEDERATE_MINUS,
        FEDERATE_UNDEFINED,
        // complete bgp partition with additional partition of triple alone (as before)
        FEDERATE_COMPLETE,
        // source selection with filter
        FEDERATE_FILTER,
        FEDERATE_FILTER_ACCEPT,
        FEDERATE_FILTER_REJECT,
        // source selection with bind (exists {t1 . t2} as ?b_i)
        FEDERATE_JOIN,
        // authorize path in join test
        FEDERATE_JOIN_PATH,
        FEDERATE_SPLIT,

        // index query pattern skip predicate for source discovery
        FEDERATE_INDEX_SKIP,
        FEDERATE_INDEX_PATTERN,
        FEDERATE_INDEX_SUCCESS,
        FEDERATE_INDEX_LENGTH,

        FEDERATE_BLACKLIST,
        FEDERATE_BLACKLIST_EXCEPT,
        FEDERATE_QUERY_PATTERN,
        FEDERATE_PREDICATE_PATTERN,

        // boolan value
        DISPLAY_URI_AS_PREFIX,
        // rdf star reference node displayed as nested triple
        DISPLAY_AS_TRIPLE,
        // Graph node implemented as IDatatype instead of NodeImpl
        GRAPH_NODE_AS_DATATYPE,
        BLANK_NODE,
        // load rdf file into graph kg:default instead of graph file-path
        LOAD_IN_DEFAULT_GRAPH,
        // constraint rule error in specific named graph
        CONSTRAINT_NAMED_GRAPH,
        // constraint rule error in external named graph
        CONSTRAINT_GRAPH,
        // graph ?g { } iterate std and external named graph
        EXTERNAL_NAMED_GRAPH,
        GRAPH_INDEX_END,
        GRAPH_INDEX_TRANSITIVE,
        GRAPH_INDEX_LOAD_SKIP,
        // rdf* draft
        RDF_STAR,
        // enforce compliance: no literal as subject
        RDF_STAR_VALIDATION,
        // joker: asserted query triple return asserted and nested triple (default
        // false)
        RDF_STAR_SELECT,
        // joker: asserted delete triple deletes asserted and nested triple (default
        // false)
        RDF_STAR_DELETE,
        // use TripleNode implementation
        RDF_STAR_TRIPLE,
        // corese server for micro services
        REENTRANT_QUERY,
        // activate access level control (default is true)
        ACCESS_LEVEL,
        // activate access right for rdf triples wrt to namespace (default is false)
        ACCESS_RIGHT,
        // activate @event ldscript function call for sparql query processing
        EVENT,
        SKOLEMIZE,

        VERBOSE,
        SOLVER_DEBUG,
        TRANSFORMER_DEBUG,

        LOG_NODE_INDEX,
        LOG_RULE_CLEAN,

        SOLVER_SORT_CARDINALITY,
        SOLVER_QUERY_PLAN, // STD | ADVANCED
        // string value
        SOLVER_VISITOR,
        SOLVER_OVERLOAD,
        RULE_VISITOR,
        TRANSFORMER_VISITOR,
        SERVER_VISITOR,
        PREFIX,
        // Testing purpose
        INTERPRETER_TEST,
        // 1 ; 01 ; 1.0 have different Node
        // when true: nodes can be joined by graph matching
        // when false: they do not join
        DATATYPE_ENTAILMENT,
        SPARQL_COMPLIANT,
        SPARQL_ORDER_UNBOUND_FIRST,

        DISABLE_OWL_AUTO_IMPORT,
        OWL_CLEAN,
        OWL_CLEAN_QUERY,
        OWL_RL,

        RULE_TRANSITIVE_FUNCTION,
        RULE_TRANSITIVE_OPTIMIZE,
        // rule engine use edge index with data manager
        RULE_DATAMANAGER_OPTIMIZE,
        // replace kg:rule_i by kg:rule
        RULE_DATAMANAGER_CLEAN,
        // for testing edge iterator filter edge index
        RULE_DATAMANAGER_FILTER_INDEX,
        RULE_TRACE,

        FUNCTION_PARAMETER_MAX,

        // init graph
        GUI_TITLE,
        GUI_BROWSE,
        GUI_XML_MAX,
        GUI_TRIPLE_MAX,
        GUI_INDEX_MAX,
        // rdf+xml turtle json
        GUI_CONSTRUCT_FORMAT,
        GUI_SELECT_FORMAT,
        GUI_DEFAULT_QUERY,
        GUI_QUERY_LIST,
        GUI_TEMPLATE_LIST,
        GUI_EXPLAIN_LIST,
        GUI_RULE_LIST,

        // application/rdf+xml
        LOAD_FORMAT,
        // integer value
        // max number of triples for each rdf file load
        LOAD_LIMIT,
        LOAD_WITH_PARAMETER,
        LOAD_DATASET,
        LOAD_QUERY,
        LOAD_FUNCTION,
        LOAD_RULE,

        RDFS_ENTAILMENT,

        LDSCRIPT_VARIABLE,
        LDSCRIPT_DEBUG,
        LDSCRIPT_CHECK_DATATYPE,
        LDSCRIPT_CHECK_RDFTYPE,

        SERVICE_BINDING,
        SERVICE_SLICE,
        SERVICE_LIMIT,
        SERVICE_TIMEOUT,
        SERVICE_SEND_PARAMETER,
        SERVICE_PARAMETER,
        SERVICE_LOG,
        SERVICE_REPORT,
        SERVICE_DISPLAY_RESULT,
        SERVICE_DISPLAY_MESSAGE,
        SERVICE_HEADER,

        // service result may be RDF graph (e.g. when format=turtle)
        // apply service query on the graph
        SERVICE_GRAPH,

        STORAGE,
        STORAGE_SERVICE,
        // default storage: db|dataset
        STORAGE_MODE,

        // parser configuration
        STRICT_MODE,
    };

    static {
        start();
    }

    static void start() {
        singleton = new Property();
        set(SERVICE_SEND_PARAMETER, true);
        set(DATATYPE_ENTAILMENT, true);
    }

    Property() {
        booleanProperty = new HashMap<>();
        stringProperty = new HashMap<>();
        integerProperty = new HashMap<>();
        listProperty = new HashMap<>();
        properties = new Properties();
        variableMap = new HashMap<>();
        imports = new HashMap<>();
    }

    public static Property getSingleton() {
        return singleton;
    }

    public static void load(String path) throws FileNotFoundException, IOException {
        start();
        getSingleton().basicLoad(path);
    }

    /**
     * Use case: corese gui initialize graph
     */
    public static void init(Graph g) {
        getSingleton().basicInit(g);
    }

    public static void set(Value value, boolean b) {
        getSingleton().basicSet(value, b);
    }

    public static void set(Value value, String str) {
        getSingleton().basicSet(value, str);
    }

    public static void set(Value value, String... str) {
        getSingleton().basicSet(value, str);
    }

    public static void set(Value value, double d) {
        getSingleton().basicSet(value, Double.toString(d));
    }

    public static void set(Value value, int n) {
        getSingleton().basicSet(value, n);
    }

    public static Boolean get(Value value) {
        return getSingleton().getBooleanProperty().get(value);
    }

    public static boolean booleanValue(Value value) {
        Boolean b = get(value);
        return b != null && b;
    }

    public static boolean hasValue(Value value, boolean b) {
        return get(value) != null && get(value) == b;
    }

    public static Set<Value> getPropertySet() {
        return getSingleton().getBooleanProperty().keySet();
    }

    public static String display() {
        return getSingleton().basicDisplay();
    }

    /**
     * Implementation for singleton
     */

    public String basicDisplay() {
        return String.format("Property:\n%s\n%s",
                getBooleanProperty().toString(), getStringProperty().toString());
    }

    void basicLoad(String path) throws FileNotFoundException, IOException {
        setPath(path);
        File file = new File(path);
        setParent(file.getParent());
        getImports().put(path, path);
        getProperties().load(new FileReader(path));
        // start with variable because import may use variable,
        // as well as other properties
        defineVariable();
        imports();
        init();
    }

    /**
     * @todo: ./ in imported file is the path of main property file
     * @todo: there is no recursive import
     */
    private void imports() throws IOException {
        if (getProperties().containsKey(IMPORT.toString())) {

            for (String name : getPropertiesValue(IMPORT).split(SEP)) {
                String path = expand(name);

                if (getImports().containsKey(path)) {
                    logger.info("Skip import: " + path);
                } else {
                    getImports().put(path, path);
                    logger.info("Import: " + path);
                    getProperties().load(new FileReader(path));
                    // @note: imported variable overload Properties and Property variable property
                    // but complete variable hashmap properly
                    defineVariable();
                }
            }
        }
    }

    void init() {
        defineProperty();
    }

    void defineProperty() {
        for (String name : getProperties().stringPropertyNames()) {
            String value = getProperties().getProperty(name);
            try {
                define(name, value);
            } catch (Exception e) {
                logger.info("Incorrect Property: " + name + " " + value);
            }
        }
    }

    /**
     * Do it first
     * VARIABLE = $gui=/a/path
     */
    void defineVariable() {
        if (getProperties().containsKey(VARIABLE.toString())) {
            basicSet(VARIABLE, getPropertiesValue(VARIABLE));
            // variable definitions in a hashmap
            defineVariableMap();
        }
    }

    public void define(String name, String value) throws IOException {
        Value pname = Value.valueOf(name);
        if (value.equals("true") | value.equals("false")) {
            Boolean b = Boolean.valueOf(value);
            basicSet(pname, b);
        } else {
            try {
                int n = Integer.valueOf(value);
                basicSet(pname, n);
            } catch (Exception e) {
                basicSet(pname, value);
            }
        }
    }

    void basicSet(Value value, boolean b) {
        if (isDebug()) {
            logger.info(value + " = " + b);
        }
        getBooleanProperty().put(value, b);

        switch (value) {

            case OWL_CLEAN:
                RuleEngine.OWL_CLEAN = b;
                break;

            case RULE_DATAMANAGER_OPTIMIZE:
                RuleEngine.RULE_DATAMANAGER_OPTIMIZE = b;
                break;

            case LOAD_WITH_PARAMETER:
                Service.LOAD_WITH_PARAMETER = b;
                break;

            case DISPLAY_URI_AS_PREFIX:
                Constant.DISPLAY_AS_PREFIX = b;
                CoreseDatatype.DISPLAY_AS_PREFIX = b;
                break;

            case DISPLAY_AS_TRIPLE:
                DatatypeMap.DISPLAY_AS_TRIPLE = b;
                break;

            case GRAPH_NODE_AS_DATATYPE:
                NodeImpl.byIDatatype = b;
                break;

            case CONSTRAINT_NAMED_GRAPH:
                Graph.CONSTRAINT_NAMED_GRAPH = b;
                break;

            case CONSTRAINT_GRAPH:
                Graph.CONSTRAINT_GRAPH = b;
                break;

            case EXTERNAL_NAMED_GRAPH:
                Graph.EXTERNAL_NAMED_GRAPH = b;
                break;

            case LOAD_IN_DEFAULT_GRAPH:
                Load.setDefaultGraphValue(b);
                break;

            case SKOLEMIZE:
                Graph.setDefaultSkolem(b);
                break;

            case GRAPH_INDEX_END:
                EdgeManagerIndexer.RECORD_END = b;
                break;

            case RDF_STAR_TRIPLE:
                EdgeFactory.EDGE_TRIPLE_NODE = b;
                EdgeFactory.OPTIMIZE_EDGE = !b;
                // continue;

            case RDF_STAR:
                Graph.setRDFStar(b);
                ASTParser.RDF_STAR = b;
                break;

            case RDF_STAR_VALIDATION:
                // check subject literal is an error
                ParserHandler.rdf_star_validation = b;
                MatcherImpl.RDF_STAR_VALIDATION = b;
                break;

            case RDF_STAR_SELECT:
                DataFilter.RDF_STAR_SELECT = b;
                break;

            case DATATYPE_ENTAILMENT:
                // when true: graph match can join 1, 01, 1.0
                DatatypeMap.DATATYPE_ENTAILMENT = b;
                break;

            case SPARQL_COMPLIANT:
                // default is false
                // true: literal is different from string
                // true: from named without from is sparql compliant
                DatatypeMap.setSPARQLCompliant(b);
                QuerySolver.SPARQL_COMPLIANT_DEFAULT = b;
                // SPARQL_COMPLIANT => ! DATATYPE_ENTAILMENT
                set(DATATYPE_ENTAILMENT, !b);
                break;

            case SPARQL_ORDER_UNBOUND_FIRST:
                Mappings.setOrderUnboundFirst(b);
                break;

            case REENTRANT_QUERY:
                QueryProcess.setOverwrite(b);
                break;

            case ACCESS_LEVEL:
                Access.setActive(b);
                if (b) {
                    Access.setDefaultUserLevel(Level.DEFAULT);
                } else {
                    Access.setDefaultUserLevel(Level.SUPER_USER);
                }
                break;

            case ACCESS_RIGHT:
                AccessRight.setActive(b);
                break;

            case EVENT:
                QuerySolver.setVisitorable(b);
                break;

            case VERBOSE:
                Graph.setDefaultVerbose(b);
                break;

            case TRANSFORMER_DEBUG:
                Transformer.setDefaultDebug(b);
                break;

            case SOLVER_DEBUG:
                Query.DEBUG_DEFAULT = b;
                break;

            case LDSCRIPT_DEBUG:
                Binding.DEBUG_DEFAULT = b;
                Function.nullcheck = b;
                break;

            case LDSCRIPT_CHECK_DATATYPE:
                Function.typecheck = b;
                break;

            case LDSCRIPT_CHECK_RDFTYPE:
                Function.rdftypecheck = b;
                break;

            case INTERPRETER_TEST:
                Interpreter.testNewEval = b;
                break;

            case FEDERATE_BGP:
                FederateVisitor.FEDERATE_BGP = b;
                break;

            case FEDERATE_PARTITION:
                FederateVisitor.PARTITION = b;
                break;

            case FEDERATE_COMPLETE:
                FederateVisitor.COMPLETE_BGP = b;
                break;

            case FEDERATE_FILTER:
                FederateVisitor.SELECT_FILTER = b;
                break;

            case FEDERATE_JOIN:
                FederateVisitor.SELECT_JOIN = b;
                FederateVisitor.USE_JOIN = b;
                break;

            case FEDERATE_OPTIONAL:
                FederateVisitor.OPTIONAL = b;
                break;

            case FEDERATE_MINUS:
                FederateVisitor.MINUS = b;
                break;

            case FEDERATE_UNDEFINED:
                FederateVisitor.UNDEFINED = b;
                break;

            case FEDERATE_JOIN_PATH:
                FederateVisitor.SELECT_JOIN_PATH = b;
                break;

            case TRACE_GENERIC:
                FederateVisitor.TRACE_FEDERATE = b;
                RewriteBGPList.TRACE_BGP_LIST = b;
                break;

            case SOLVER_SORT_CARDINALITY:
                QueryProcess.setSort(b);
                break;

            case SOLVER_OVERLOAD:
                TermEval.OVERLOAD = b;
                break;

            case RDFS_ENTAILMENT:
                Graph.RDFS_ENTAILMENT_DEFAULT = b;
                break;

            case SERVICE_REPORT:
                ASTParser.SERVICE_REPORT = b;
                break;

            case SERVICE_DISPLAY_MESSAGE:
                ServiceParser.DISPLAY_MESSAGE = b;
                break;

            case STRICT_MODE:
                ASTQuery.STRICT_MODE = b;
                break;
        }
    }

    void basicSet(Value value, String... str) {
        if (isDebug()) {
            logger.info(value + " = " + str);
        }
        switch (value) {
            case FEDERATE_BLACKLIST:
                blacklist(str);
                break;
            case FEDERATE_BLACKLIST_EXCEPT:
                blacklistExcept(str);
                break;
        }
    }

    void basicSet(Value value, String str) {
        if (isDebug()) {
            logger.info(value + " = " + str);
        }
        getStringProperty().put(value, str);
        switch (value) {

            case FEDERATION:
                defineFederation(str);
                break;

            case FEDERATE_INDEX_PATTERN:
                SelectorIndex.QUERY_PATTERN = str;
                break;

            case FEDERATE_QUERY_PATTERN:
                setQueryPattern(str);
                break;

            case FEDERATE_PREDICATE_PATTERN:
                setPredicatePattern(str);
                break;

            case FEDERATE_FILTER_ACCEPT:
                setFilterAccept(str);
                break;

            case FEDERATE_FILTER_REJECT:
                setFilterReject(str);
                break;

            case FEDERATE_INDEX_SKIP:
                setIndexSkip(str);
                break;

            case FEDERATE_BLACKLIST:
                blacklist(str);
                break;

            case FEDERATE_BLACKLIST_EXCEPT:
                blacklistExcept(str);
                break;

            case FEDERATE_SPLIT:
                split(str);
                break;

            case FEDERATE_INDEX_SUCCESS:
                FederateVisitor.NB_SUCCESS = Double.valueOf(str);
                break;

            case LOAD_FORMAT:
                Load.LOAD_FORMAT = str;
                break;

            case SERVICE_BINDING:
                CompileService.setBinding(str);
                break;

            case SERVICE_PARAMETER:
                // set in table
                break;

            case SERVICE_HEADER:
                getListProperty().put(SERVICE_HEADER, getList(str));
                break;

            case LDSCRIPT_VARIABLE:
                variable();
                break;

            case BLANK_NODE:
                Graph.BLANK = str;
                break;

            case SOLVER_QUERY_PLAN:
                queryPlan(str);
                break;

            case SOLVER_VISITOR:
                QueryProcess.setVisitorName(expand(str));
                break;

            case RULE_VISITOR:
                QuerySolverVisitorRule.setVisitorName(expand(str));
                break;

            case TRANSFORMER_VISITOR:
                QuerySolverVisitorTransformer.setVisitorName(expand(str));
                break;

            case SERVER_VISITOR:
                QueryProcess.setServerVisitorName(expand(str));
                break;

            case ACCESS_LEVEL:
                accessLevel(str);
                break;

            case PREFIX:
                prefix();
                break;

            case LOAD_FUNCTION:
                loadFunction(str);
                break;

        }
    }

    void setQueryPattern(String str) {
        QueryLoad ql = QueryLoad.create();
        for (Pair pair : getValueList(Value.FEDERATE_QUERY_PATTERN)) {
            try {
                SelectorIndex.defineQueryPattern(pair.getKey(), ql.readWE(pair.getPath()));
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    void setPredicatePattern(String str) {
        QueryLoad ql = QueryLoad.create();
        for (Pair pair : getValueList(Value.FEDERATE_PREDICATE_PATTERN)) {
            try {
                SelectorIndex.definePredicatePattern(pair.getKey(), ql.readWE(pair.getPath()));
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    void setFilterAccept(String str) {
        for (String ope : str.split(SEP)) {
            SelectorFilter.defineOperator(ope, true);
        }
    }

    void setFilterReject(String str) {
        for (String ope : str.split(SEP)) {
            SelectorFilter.rejectOperator(ope, true);
        }
    }

    void setIndexSkip(String str) {
        for (String ope : str.split(SEP)) {
            SelectorIndex.skipPredicate(ope);
        }
    }

    void split(String list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String str : list.split(SEP)) {
            alist.add(NSManager.nsm().toNamespace(str));
        }
        logger.info("Split: " + alist);
        FederateVisitor.DEFAULT_SPLIT = alist;
    }

    List<String> getList(String list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String str : list.split(SEP)) {
            alist.add(str);
        }
        return alist;
    }

    void blacklist(String list) {
        FederateVisitor.BLACKLIST = getList(list);
    }

    void blacklistExcept(String list) {
        FederateVisitor.BLACKLIST_EXCEPT = getList(list);
    }

    void blacklist(String... list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String str : list) {
            alist.add(str);
        }
        FederateVisitor.BLACKLIST = alist;
    }

    void blacklistExcept(String... list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String str : list) {
            alist.add(str);
        }
        FederateVisitor.BLACKLIST_EXCEPT = alist;
    }

    void basicSet(Value value, int n) {
        if (isDebug()) {
            logger.info(value + " = " + n);
        }
        getIntegerProperty().put(value, n);

        switch (value) {

            case SERVICE_SLICE:
            case SERVICE_LIMIT:
            case SERVICE_TIMEOUT:
                // use integer table
                break;

            case SERVICE_DISPLAY_RESULT:
                ProviderService.DISPLAY_RESULT_MAX = n;
                ContextLog.DISPLAY_RESULT_MAX = n;
                Eval.DISPLAY_RESULT_MAX = n;
                break;

            case LOAD_LIMIT:
                Load.setLimitDefault(n);
                break;

            case FUNCTION_PARAMETER_MAX:
                ASTExtension.FUNCTION_PARAMETER_MAX = n;
                break;

            case FEDERATE_INDEX_LENGTH:
                FederateVisitor.NB_ENDPOINT = n;
                break;
        }
    }

    // variable definition may use preceding variables
    private void defineVariableMap() {
        for (Pair pair : getValueListBasic(Value.VARIABLE)) {
            String var = varName(pair.getKey());

            if (getVariableMap().containsKey(var)) {
                logger.info("Overload variable: " + var);
            }
            logger.info(String.format("variable: %s=%s", var, expand(pair.getValue())));

            getVariableMap().put(var, expand(pair.getValue()));
        }
    }

    String varName(String key) {
        return key.startsWith(VAR_CHAR) ? key : VAR_CHAR + key;
    }

    String expand(String value) {
        if (value.startsWith(VAR_CHAR)) {
            for (String var : getVariableMap().keySet()) {
                if (value.startsWith(var)) {
                    return value.replace(var, getVariableMap().get(var));
                }
            }
        } else if (value.startsWith("./")) {
            // relative path
            return complete(value);
        }
        return value;
    }

    String complete(String value) {
        return getParent().concat(value.substring(1));
    }

    void queryPlan(String str) {
        switch (str) {
            case STD:
                QuerySolver.QUERY_PLAN = Query.QP_DEFAULT;
                break;
            default:
                QuerySolver.QUERY_PLAN = Query.QP_HEURISTICS_BASED;
                break;
        }
    }

    void defineFederation(String path) {
        logger.info("federation: " + path);
        QueryProcess exec = QueryProcess.create(Graph.create());
        try {
            Graph g = exec.defineFederation(path);
        } catch (IOException | EngineException | LoadException ex) {
            logger.error(ex.toString());
        }
    }

    void loadFunction(String str) {
        QueryProcess exec = QueryProcess.create();
        for (String name : str.split(SEP)) {
            try {
                exec.imports(str);
            } catch (EngineException ex) {
                logger.error(ex.toString());
            }
        }
    }

    /**
     * Init graph with properties such as load dataset
     * use case: corese gui
     */
    void basicInit(Graph g) {
        for (String name : getProperties().stringPropertyNames()) {
            String value = getProperties().getProperty(name);
            try {
                define(name, value, g);
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        // after load dataset
        if (getStringProperty().containsKey(LOAD_RULE)) {
            loadRule(g, getStringProperty().get(LOAD_RULE));
        }
    }

    void define(String name, String value, Graph g) {
        try {
            Value pname = Value.valueOf(name);
            define(pname, value, g);
        } catch (Exception e) {

        }
    }

    void define(Value name, String value, Graph g) {
        switch (name) {
            case LOAD_DATASET:
                loadList(g, value);
                break;
        }
    }

    void loadRule(Graph g, String path) {
        for (String name : path.split(SEP)) {
            RuleEngine re = RuleEngine.create(g);
            try {
                String file = expand(name);
                re.setProfile(file);
                re.process();
            } catch (LoadException | EngineException ex) {
                logger.error(ex.toString());
            }
        }
    }

    void loadList(Graph g, String path) {
        Load ld = Load.create(g);
        for (String name : path.split(SEP)) {
            try {
                String file = expand(name);
                logger.info("Load: " + file);
                ld.parse(file.strip());
            } catch (LoadException ex) {
                logger.error(ex.toString());
            }
        }
    }

    /**
     * LDScript static variable
     * LDSCRIPT_VARIABLE = var=val;var=val
     */
    void variable() {
        for (Pair pair : getValueListBasic(Value.LDSCRIPT_VARIABLE)) {
            String var = pair.getKey().strip();
            String val = pair.getValue().strip();
            logger.info(String.format("ldscript variable: %s=%s", var, val));
            IDatatype dt = DatatypeMap.newValue(val);
            Binding.setStaticVariable(var, dt);
        }
    }

    public static List<Pair> getValueList(Value val) {
        return getSingleton().getValueListBasic(val);
    }

    public List<Pair> getValueListBasic(Value val) {
        String str = Property.stringValue(val);
        ArrayList<Pair> list = new ArrayList<>();
        if (str == null) {
            return list;
        }
        for (String elem : str.split(SEP)) {
            String[] def = elem.split(EQ, 2);
            if (def.length >= 2) {
                list.add(new Pair(def[0], def[1]));
            }
        }
        return list;
    }

    public class Pair {

        private String first;
        private String second;

        Pair(String f, String r) {
            first = f;
            second = r;
        }

        public String getKey() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getValue() {
            return second;
        }

        public String getPath() {
            return expand(getValue());
        }

        public void setSecond(String second) {
            this.second = second;
        }

    }

    public List<List<String>> getStorageparameters() {

        String storages = Property.stringValue(STORAGE);

        List<List<String>> storageList = new ArrayList<>();
        if (storages == null) {
            return storageList;
        }

        for (String storageStr : storages.split(SEP)) {

            String[] storageLst = storageStr.split(",", 3);
            storageList.add(List.of(storageLst).stream().map(str -> this.expand(str)).collect(Collectors.toList()));
        }
        return storageList;
    }

    void prefix() {
        for (Pair pair : getValueListBasic(PREFIX)) {
            logger.info(String.format("prefix %s: <%s>", pair.getKey().strip(), pair.getValue().strip()));
            NSManager.defineDefaultPrefix(pair.getKey().strip(), pair.getValue().strip());
        }
    }

    void accessLevel(String str) {
        try {
            Level level = Level.valueOf(str);
            Access.setDefaultUserLevel(level);
        } catch (Exception e) {
            logger.error("Undefined Access Level: " + str);
        }
    }

    public HashMap<Value, Boolean> getBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(HashMap<Value, Boolean> booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public HashMap<Value, String> getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(HashMap<Value, String> stringProperty) {
        this.stringProperty = stringProperty;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public HashMap<Value, Integer> getIntegerProperty() {
        return integerProperty;
    }

    public void setIntegerProperty(HashMap<Value, Integer> integerProperty) {
        this.integerProperty = integerProperty;
    }

    public static Integer intValue(Value val) {
        return getSingleton().getIntegerProperty().get(val);
    }

    public static List<String> listValue(Value value) {
        return getSingleton().getListProperty().get(value);
    }

    String getPropertiesValue(Value value) {
        if (getProperties().containsKey(value.toString())) {
            return (String) getProperties().get(value.toString());
        }
        return null;
    }

    public static String stringValue(Value val) {
        return getSingleton().getStringProperty().get(val);
    }

    public static String pathValue(Value val) {
        return getSingleton().expand(stringValue(val));
    }

    public static String[] stringValueList(Value val) {
        String str = stringValue(val);
        if (val == null || str == null) {
            return new String[0];
        }
        return str.split(SEP);
    }

    public HashMap<String, String> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(HashMap<String, String> variableMap) {
        this.variableMap = variableMap;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public HashMap<String, String> getImports() {
        return imports;
    }

    public void setImports(HashMap<String, String> imports) {
        this.imports = imports;
    }

    public HashMap<Value, List<String>> getListProperty() {
        return listProperty;
    }

    public void setListProperty(HashMap<Value, List<String>> listProperty) {
        this.listProperty = listProperty;
    }

    // current storage mode to create QueryProcess
    public static boolean isDataset() {
        if (stringValue(STORAGE) == null) {
            // there is no db storage path
            return true;
        }
        if (stringValue(STORAGE_MODE) == null) {
            // there is db storage path and no mode specified -> db mode, not dataset
            return false;
        }
        // STORAGE_MODE = db|dataset
        return stringValue(STORAGE_MODE).equals(DATASET);
    }

    // current storage mode
    public static boolean isStorage() {
        return !isDataset();
    }

    // consider all db
    public static boolean isStorageAll() {
        return isStorage() &&
                protectEquals(stringValue(STORAGE_MODE), DB_ALL);
    }

    static boolean protectEquals(String var, String val) {
        return var != null && var.equals(val);
    }

}
