package fr.inria.corese.core.shacl;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API for LDScript SHACL Interpreter
 * Interpreter is defined in corese-core resources/function/datashape
 * 
 * @author Olivier Corby, Wimmics, INRIA, 2019
 */
public class Shacl {

    
    static final Logger logger = Logger.getLogger(Shacl.class.getName());
    static final String SH = NSManager.SHACL;
    private static final String NBRESULT = NSManager.SHACL + "result";
    private static final String TRACE_VAR  = "?shaclTrace";
    public static final String TRACEMAPSUC_VAR = "?recordmapsuc";
    public static final String TRACEMAPFAIL_VAR = "?recordmapfail";
    public static final String MAPMAP_VAR = "?mapmap";
    public static final String SETUP_VAR  = "?setup";
    public static final String SETUP_DETAIL = SH + "detail";
    public static final String SETUP_TYPE   = SH + "type";
    
    static final String FUNEVAL = SH+"funeval";
    static final String PARSE   = SH+"funparse";
    
    static final String SHACL   = SH+"shacl";
    static final String SHEX    = SH+"shex";
    static final String SHAPE   = SH+"shaclshape";
    static final String NODE    = SH+"shaclnode";
    static final String FOCUS   = SH+"focuslist";
    static final String CONFORM = SH+"conforms";
    static final String TRACE   = SH+"trace";
    static final String TRACERECORD   = SH+"tracerecord";
    static final String DEF     = SH+"def";
    
    private Graph graph;
    private Graph shacl;
    private Graph result;
    private Binding bind;
    private Binding input;
    
    static {
        init();
    }
    
    /**
     * Import SHACL Interpreter as public functions
     */
    static void init() {
        QueryProcess exec = QueryProcess.create(Graph.create());
        try {
            System.out.println("Import LDScript SHACL Interpreter");
            exec.imports("http://ns.inria.fr/sparql-template/function/datashape/main.rq");
        } catch (EngineException ex) {
            logger.log(Level.SEVERE, null, ex);
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
    
    public Shacl setup(String name, boolean b){
        setup().set(DatatypeMap.newResource(name), (b) ? DatatypeMap.TRUE : DatatypeMap.FALSE);
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
     * Evaluate shape/node
     */
    public Graph shape(IDatatype sh) throws EngineException {
        return eval(SHAPE, sh);
    }
    
    public Graph shape(IDatatype sh, IDatatype node) throws EngineException {
        return eval(SHAPE, sh, node);
    }
    
    public Graph node(IDatatype node) throws EngineException {
        return eval(NODE, node);
    }
    
    
     
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
        logger.warning("Validation Report Graph has no conform");
        return true;
    }
    

    // number of failure in report graph
    // number of value of property sh:result
    public int nbResult(Graph g) {
        return g.size(DatatypeMap.newResource(NBRESULT));
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
        IDatatype suc  = getVariable(TRACEMAPSUC_VAR);
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
            throw new EngineException("No validation graph") ;
        }
        
        setResult((Graph) dt.getPointerObject());
        getResult().index();
        return getResult();
    }
    
   
    
    IDatatype funcall(String name, Object... obj) throws EngineException {
        QueryProcess exec = QueryProcess.create(getGraph());
        IDatatype res = exec.funcall(name, getInput(), param(obj));
        setBind(exec.getBinding());
        if (res == null) {
            throw new EngineException("SHACL Error") ;
        }
        return res;
    }
    
    IDatatype[] param(Object[] param) {
        IDatatype[] res = new IDatatype[param.length];
        for (int i = 0; i<param.length; i++) {
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

    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * @param result the result to set
     */
    public void setResult(Graph result) {
        this.result = result;
    }

    /**
     * @return the shacl
     */
    public Graph getShacl() {
        return shacl;
    }

    /**
     * @param shacl the shacl to set
     */
    public void setShacl(Graph shacl) {
        this.shacl = shacl;
    }

    /**
     * @return the bind
     */
    public Binding getBind() {
        return bind;
    }

    /**
     * @param bind the bind to set
     */
    public void setBind(Binding bind) {
        this.bind = bind;
    }

    /**
     * @return the input
     */
    public Binding getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(Binding input) {
        this.input = input;
    }
    
    
}
