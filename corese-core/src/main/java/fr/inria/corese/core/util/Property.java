package fr.inria.corese.core.util;

import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.Transformer;
import static fr.inria.corese.core.util.Property.Value.LOAD_RULE;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorTransformer;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
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

    private HashMap<Value, Boolean> booleanProperty;
    private HashMap<Value, String> stringProperty;
    // Java Properties manage property file (at user option)
    private Properties properties;

    public enum Value {
        // boolan value
        // Graph node implemented as IDatatype instead of NodeImpl
        GRAPH_NODE_AS_DATATYPE,
        // load rdf file into graph kg:default instead of graph file-path 
        LOAD_IN_DEFAULT_GRAPH,
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
        // integer value
        // max number of triples for each rdf file load
        LOAD_LIMIT,
        SKOLEMIZE,
        SOLVER_DEBUG,
        TRANSFORMER_DEBUG,
        SOLVER_SORT_CARDINALITY,
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
        LOAD_DATASET,
        LOAD_FUNCTION,
        LOAD_RULE,
        RDFS_ENTAILMENT

    };

    static {
        singleton = new Property();
    }

    Property() {
        booleanProperty = new HashMap<>();
        stringProperty = new HashMap<>();
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
            case GRAPH_NODE_AS_DATATYPE:
                NodeImpl.byIDatatype = b;
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

    void basicSet(Value value, int n) {
        logger.info(value + " = " + n);
        switch (value) {
            case LOAD_LIMIT:
                Load.setLimitDefault(n);
                break;
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

}
