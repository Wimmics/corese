package fr.inria.corese.core.shacl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * API for LDScript SHACL Interpreter
 * Interpreter is defined in corese-core resources/function/datashape
 * 
 * define LDScript global variable:
 * shacl.input().setVariable("?var", val).
 * 
 * @author Olivier Corby, Wimmics, INRIA, 2019
 */
public class Shacl {

    private static Logger logger = LoggerFactory.getLogger(Shacl.class);
    static final String SH = NSManager.SHACL;
    private static final String NBRESULT = NSManager.SHACL + "result";
    private static final String TRACE_VAR = "?shaclTrace";
    public static final String TRACEMAPSUC_VAR = "?recordmapsuc";
    public static final String TRACEMAPFAIL_VAR = "?recordmapfail";
    public static final String MAPMAP_VAR = "?mapmap";
    public static final String SETUP_VAR = "?setup";
    public static final String SETUP_DETAIL = SH + "nodeDetail";
    public static final String SETUP_DETAIL_BOOLEAN = SH + "booleanDetail";
    public static final String SETUP_TYPE = SH + "type";

    static final String FUNEVAL = SH + "funeval";
    static final String PARSE = SH + "funparse";

    static final String SHACL = SH + "shacl";
    // Probabilistic SHACL
    static final String EXTENDED_SHACL = SH + "extendedshacl";
    // modss
    public static final int PROBABILISTIC_MODE = 1;
    public static final int POSSIBILISTIC_MODE = 2; // not avalaible

    static final String SHEX = SH + "shex";
    static final String SHAPE = SH + "shaclshape";
    static final String NODE = SH + "shaclnode";
    static final String SHAPE_GRAPH = SH + "shaclShapeGraph";
    static final String NODE_GRAPH = SH + "shaclNodeGraph";
    static final String FOCUS = SH + "focuslist";
    static final String CONFORM = SH + "conforms";
    static final String TRACE = SH + "trace";
    static final String TRACERECORD = SH + "tracerecord";
    static final String DEF = SH + "def";

    private Graph graph;
    private Graph shacl;
    private Graph result;
    private DataManager dataManager;
    private Binding bind;
    private Binding input;

    // Default SHACL Interpreter in Corese
    private static String SHACL_Interpreter = "http://ns.inria.fr/sparql-template/function/datashape/main.rq";

    static {
        init();
    }

    /**
     * Import SHACL Interpreter as public functions
     */
    static void init() {
        QueryProcess exec = QueryProcess.create(Graph.create());
        try {
            exec.imports(SHACL_Interpreter);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    public Shacl(Graph g) {
        setGraph(g);
        setShacl(g);
    }

    public Shacl(Graph g, Graph shacl) {
        setGraph(g);
        setShacl(shacl);
    }

    /**
     * Set a different SHACL interpreter, ex. SHACL-S
     */
    public static void setSHACL_Interpreter(String SHACL_Interpreter) {
        Shacl.SHACL_Interpreter = SHACL_Interpreter;
    }

    public static String getSHACL_Interpreter() {
        return SHACL_Interpreter;
    }

    public Binding input() {
        if (getInput() == null) {
            setInput(Binding.create());
        }
        return getInput();
    }

    public Binding output() {
        if (getBind() == null) {
            setBind(Binding.create());
        }
        return getBind();
    }

    /**
     * Define trace=true as LDScript global variable
     */
    public Shacl setTrace(boolean b) {
        input().setVariable(TRACE_VAR, DatatypeMap.newInstance(b));
        return this;
    }

    public IDatatype setup() {
        IDatatype map = input().getVariable(SETUP_VAR);
        if (map == null) {
            map = DatatypeMap.map();
            input().setVariable(SETUP_VAR, map);
        }
        return map;
    }

    // sh:setup(sh:booleanDetail, true)
    public Shacl setup(String name, boolean b) {
        setup().set(DatatypeMap.newResource(name), (b) ? DatatypeMap.TRUE : DatatypeMap.FALSE);
        return this;
    }

    // additional report for boolean operator arguments
    public Shacl booleanDetail(boolean b) {
        setup(SETUP_DETAIL_BOOLEAN, b);
        return this;
    }

    /**
     * API aligned with LDScript
     */

    public Graph shacl() throws EngineException {
        return eval();
    }

    public Graph shex() throws EngineException {
        return eval(SHEX, getShacl());
    }

    public Graph shaclshape(IDatatype shape) throws EngineException {
        return shape(shape);
    }

    public Graph shaclshape(IDatatype shape, IDatatype node) throws EngineException {
        if (node == null) {
            return shape(shape);
        }
        return shape(shape, node);
    }

    public Graph shaclnode(IDatatype node) throws EngineException {
        return node(node);
    }

    /**
     * Native Java API
     */

    /**
     * Parse shacl RDF graph and return a list expression
     */
    public IDatatype funparse() throws EngineException {
        return funcall(PARSE);
    }

    /**
     * Execute shacl as list expression
     * shapeList : list of (list expression)
     */
    public Graph funeval(IDatatype shapeList) throws EngineException {
        return eval(FUNEVAL, shapeList);
    }

    /**
     * Parse shacl graph as list expression and execute expression
     */
    public Graph funeval() throws EngineException {
        IDatatype dt = funparse();
        return funeval(dt);
    }

    /**
     * Evaluate shacl shape whole graph
     */
    public Graph eval() throws EngineException {
        return eval(SHACL, getShacl());
    }

    public Graph eval(Graph shacl) throws EngineException {
        setShacl(shacl);
        return eval(SHACL, shacl);
    }

    /**
     * probabilistic SHACL Evaluation (usage for Corese GUI)
     * @return
     * @throws EngineException
     */
    public Graph eval(int mode) throws EngineException {
        return eval(EXTENDED_SHACL, getShacl(), mode, DatatypeMap.createLiteral(String.valueOf(0.1), fr.inria.corese.sparql.datatype.RDF.xsddouble));
    }

    /**
     * probabilistic SHACL Evaluation (usage for Corese server) with p-value
     * @return
     * @throws EngineException
     */
    public Graph eval(Graph shacl, int mode, IDatatype p) throws EngineException {
        setShacl(shacl);
        return eval(EXTENDED_SHACL, shacl, mode, p);
    }

    /**
     * probabilistic SHACL Evaluation (usage for Corese server) with p-value and the number of considered triples
     * @return
     * @throws EngineException
     */
    public Graph eval(Graph shacl, int mode, IDatatype p, IDatatype nTriples) throws EngineException {
        setShacl(shacl);
        return eval(EXTENDED_SHACL, shacl, mode, p, nTriples);
    }
    
    /**
     * Evaluate shape/node
     */
    public Graph shape(IDatatype sh) throws EngineException {
        return eval(SHAPE_GRAPH, getShacl(), sh);
    }

    public Graph shape(IDatatype sh, IDatatype node) throws EngineException {
        return eval(SHAPE_GRAPH, getShacl(), sh, node);
    }

    public Graph node(IDatatype node) throws EngineException {
        return eval(NODE_GRAPH, getShacl(), node);
    }

    // public Graph shape(IDatatype sh) throws EngineException {
    // return eval(SHAPE, sh);
    // }
    //
    // public Graph shape(IDatatype sh, IDatatype node) throws EngineException {
    // return eval(SHAPE, sh, node);
    // }
    //
    // public Graph node(IDatatype node) throws EngineException {
    // return eval(NODE, node);
    // }

    /*
     * Return list of shape + target nodes
     */
    public IDatatype focus() throws EngineException {
        return focus(getGraph());
    }

    public IDatatype focus(Graph shacl) throws EngineException {
        setShacl(shacl);
        IDatatype dt = funcall(FOCUS, shacl);
        return dt;
    }

    /**
     * Validation report is conform ?
     */
    public boolean conform(Graph g) {
        for (Edge edge : g.getEdges(CONFORM)) {
            return edge.getNode(1).getDatatypeValue().booleanValue();
        }
        // logger.error("Validation Report Graph has no conform");
        return true;
    }

    // number of failure in report graph
    // number of value of property sh:result
    public int nbResult(Graph g) {
        return g.size(DatatypeMap.newResource(NBRESULT));
    }

    // [] a sh:AbstractResult
    public int nbAbstractResult(Graph g) {
        DataProducer dp = new DataProducer(g).iterate(
                DatatypeMap.createBlank(),
                DatatypeMap.newResource(RDF.TYPE),
                DatatypeMap.newResource(NSManager.SHACL + "AbstractResult"));
        return dp.cardinality();
    }

    /**
     * Display list of constraints that have been evaluated
     */
    public void trace() throws EngineException {
        IDatatype dt = getVariable(MAPMAP_VAR);
        trace(dt);
    }

    /**
     * Display additional information about evaluation
     */
    public void tracerecord() throws EngineException {
        IDatatype suc = getVariable(TRACEMAPSUC_VAR);
        IDatatype fail = getVariable(TRACEMAPFAIL_VAR);
        tracerecord(suc);
        tracerecord(fail);
    }

    public IDatatype getVariable(String name) {
        return getBind().getVariable(name);
    }

    // _________________________________________________

    Graph eval(String name, Object... obj) throws EngineException {
        IDatatype dt = funcall(name, obj);
        if (dt.getPointerObject() == null) {
            throw new EngineException("No validation graph");
        }

        setResult((Graph) dt.getPointerObject());
        getResult().index();
        return getResult();
    }

    IDatatype funcall(String name, Object... obj) throws EngineException {
        try {
            QueryProcess exec = QueryProcess.create(getGraph(), getDataManager());
            if (getDataManager() != null) {
                getDataManager().startReadTransaction();
            }
            IDatatype res = exec.funcall(name, getInput(), param(obj));
            setBind(exec.getCreateBinding());
            if (res == null) {
                throw new EngineException("SHACL Error");
            }
            return res;
        } finally {
            if (getDataManager() != null) {
                getDataManager().endReadTransaction();
            }
        }
    }

    IDatatype[] param(Object[] param) {
        IDatatype[] res = new IDatatype[param.length];
        for (int i = 0; i < param.length; i++) {
            res[i] = datatype(param[i]);
        }
        return res;
    }

    IDatatype datatype(Object obj) {
        return (obj instanceof IDatatype) ? (IDatatype) obj : DatatypeMap.createObject(obj);
    }

    void trace(IDatatype mapmap) throws EngineException {
        funcall(TRACE, getShacl(), mapmap);
    }

    void tracerecord(IDatatype mapmap) throws EngineException {
        funcall(TRACERECORD, getShacl(), mapmap);
    }

    public Graph getResult() {
        return result;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        graph.init();
        this.graph = graph;
    }

    public void setResult(Graph result) {
        this.result = result;
    }

    public Graph getShacl() {
        return shacl;
    }

    public void setShacl(Graph shacl) {
        shacl.init();
        this.shacl = shacl;
    }

    public Binding getBind() {
        return bind;
    }

    public void setBind(Binding bind) {
        this.bind = bind;
    }

    public Binding getInput() {
        return input;
    }

    public void setInput(Binding input) {
        this.input = input;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
