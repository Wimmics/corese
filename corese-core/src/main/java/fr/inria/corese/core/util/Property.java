package fr.inria.corese.core.util;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.query.CompileService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.Transformer;
import static fr.inria.corese.core.util.Property.Value.LOAD_RULE;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Corese configuration with properties such as: LOAD_IN_DEFAULT_GRAPH
 * Usage:  
 * Property.set(LOAD_IN_DEFAULT_GRAPH, true);
 * Property.load(property-file-path);
 * Property.init(graph);
 * corese-gui.jar    -init property-file-path
 * corese-server.jar -init property-file-path
 */
public class Property {

    private static Logger logger = LoggerFactory.getLogger(Property.class);
    private static Property singleton;
    private static final String STD = "std";
    
    private HashMap<Value, Boolean> booleanProperty;
    private HashMap<Value, String> stringProperty;
    private HashMap<Value, Integer> integerProperty;
    // Java Properties manage property file (at user option)
    private Properties properties;

    public enum Value {
        // boolan value
        DISPLAY_URI_AS_PREFIX,
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
        // mockup of rdf* where triples are assserted
        RDF_STAR,
        // corese server for micro services
        REENTRANT_QUERY,
        // activate access level control (default is true)
        ACCESS_LEVEL,
        // activate access right for rdf triples wrt to namespace (default is false)
        ACCESS_RIGHT,
        // activate @event ldscript function call for sparql query processing
        EVENT,
        VERBOSE,
        LOAD_FORMAT,
        // integer value
        // max number of triples for each rdf file load
        LOAD_LIMIT,
        SKOLEMIZE,
        SOLVER_DEBUG,
        TRANSFORMER_DEBUG,
        SOLVER_SORT_CARDINALITY,
        SOLVER_QUERY_PLAN, // STD | ADVANCED
        // string value
        SOLVER_VISITOR,
        RULE_VISITOR,
        TRANSFORMER_VISITOR,
        SERVER_VISITOR,
        PREFIX,
        // Testing purpose
        INTERPRETER_TEST,
        SPARQL_COMPLIANT,
        // init graph
        GUI_TITLE,
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
        SERVICE_PARAMETER,
        SERVICE_BROWSE
    };

    static {
        singleton = new Property();
    }

    Property() {
        booleanProperty = new HashMap<>();
        stringProperty = new HashMap<>();
        integerProperty = new HashMap<>();
        properties = new Properties();
    }

    public static Property getSingleton() {
        return singleton;
    }

    public static void load(String path) throws FileNotFoundException, IOException {
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
    
     public static void set(Value value, int n) {
        getSingleton().basicSet(value, n);
    }

    public static Boolean get(Value value) {
        return getSingleton().getBooleanProperty().get(value);
    }
    
    public static boolean booleanValue(Value value) {
        Boolean b = get(value);
        return b!=null && b;
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
        getProperties().load(new FileReader(path));
        init();
    }



    void init() {
        for (String name : getProperties().stringPropertyNames()) {
            String value = getProperties().getProperty(name);
            try {
                define(name, value);
            } catch (Exception e) {
                logger.info("Incorrect Property: " + name + " " + value);
            }
        }
    }

    void define(String name, String value) {
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
        logger.info(value + " = " + b);
        getBooleanProperty().put(value, b);

        switch (value) {
            
            case LOAD_WITH_PARAMETER:
                Service.LOAD_WITH_PARAMETER = b;
                break;
            
            case DISPLAY_URI_AS_PREFIX:
                Constant.DISPLAY_AS_PREFIX = b;
                CoreseDatatype.DISPLAY_AS_PREFIX = b;
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

            case RDF_STAR:
                Graph.setRDFStar(b);
                break;
                
            case SPARQL_COMPLIANT:
                // default is false
                // true: literal is different from string
                // true: do not join 1 and 1.0 
                // true: from named without from is sparql compliant
                DatatypeMap.setSPARQLCompliant(b);
                QuerySolver.SPARQL_COMPLIANT_DEFAULT = b;
                break;

            case REENTRANT_QUERY:
                QueryProcess.setOverwrite(true);
                break;

            case ACCESS_LEVEL:
                Access.setActive(b);
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

            case SOLVER_SORT_CARDINALITY:
                QueryProcess.setSort(b);
                break;
                
            case RDFS_ENTAILMENT:
                Graph.RDFS_ENTAILMENT_DEFAULT = b;
                break;
        }
    }

    void basicSet(Value value, String str) {
        logger.info(value + " = " + str);
        getStringProperty().put(value, str);
        switch (value) {
            
            case LOAD_FORMAT:
                Load.LOAD_FORMAT = str;
                break;
            
            case SERVICE_BINDING:
                CompileService.setBinding(str);
                break;
                
            case SERVICE_PARAMETER:
                // set in table
                break;
                
                
            case LDSCRIPT_VARIABLE:
                variable(str);
                break;
                
            case BLANK_NODE:
                Graph.BLANK = str;
                break;
                
            case SOLVER_QUERY_PLAN:
                queryPlan(str);
                break;
                
            case SOLVER_VISITOR:
                QueryProcess.setVisitorName(str);
                break;

            case RULE_VISITOR:
                QuerySolverVisitorRule.setVisitorName(str);
                break;

            case TRANSFORMER_VISITOR:
                QuerySolverVisitorTransformer.setVisitorName(str);
                break;

            case SERVER_VISITOR:
                QueryProcess.setServerVisitorName(str);
                break;

            case ACCESS_LEVEL:
                accessLevel(str);
                break;

            case PREFIX:
                prefix(str);
                break;
                
            case LOAD_FUNCTION:           
                loadFunction(str);
                break;

        }
    }
    
    void basicSet(Value value, int n) {
        logger.info(value + " = " + n);
        getIntegerProperty().put(value, n);
        
        switch (value) {
            
            case SERVICE_SLICE:                
            case SERVICE_LIMIT:                
            case SERVICE_TIMEOUT:
                // use integer table
                break;
                
            case LOAD_LIMIT:
                Load.setLimitDefault(n);
                break;
        }
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
    
    void loadFunction(String str) {
        QueryProcess exec = QueryProcess.create();
        for (String name : str.split(";")) {
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
        for (String name : path.split(";")) {
            RuleEngine re = RuleEngine.create(g);
            try {
                re.setProfile(name);
                re.process();
            } catch (LoadException | EngineException ex) {
                logger.error(ex.toString());
            }
        }
    }


    void loadList(Graph g, String path) {
        Load ld = Load.create(g);
        for (String name : path.split(";")) {
            try {
                System.out.println("Load: " + name);
                ld.parse(name.strip());
            } catch (LoadException ex) {
                logger.error(ex.toString());
            }
        }
    }
    
    /**
     * LDScript static variable
     * LDSCRIPT_VARIABLE = var=val;var=val
     */
    void variable(String str) {
        String[] list = str.split(";");
        for (String elem : list) {
            String[] def = elem.split("=");
            if (def.length >= 2) {
                String var = def[0].strip();
                String val = def[1].strip();
                System.out.println("variable: " + var + "=" + val);
                IDatatype dt = DatatypeMap.newValue(val);
                Binding.setStaticVariable(var, dt);
            }
        }
    }

    void prefix(String str) {
        String[] list = str.split(";");
        for (String elem : list) {
            String[] def = elem.split("=");
            if (def.length >= 2) {
                System.out.println("prefix " + def[0].strip() + ": <" + def[1].strip() + ">");
                NSManager.defineDefaultPrefix(def[0].strip(), def[1].strip());
            }
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
    
    public static String stringValue(Value val) {
        return getSingleton().getStringProperty().get(val);
    }

}
