package fr.inria.corese.core.query;

import fr.inria.corese.core.approximate.ext.AppxSearchPlugin;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.util.StorageFactory;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.ProxyInterpreter;
import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.compiler.api.ProxyPlugin;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.edge.EdgeQuad;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.print.RDFFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.GraphListen;
import fr.inria.corese.core.util.MappingsGraph;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.core.workflow.ShapeWorkflow;
import fr.inria.corese.sparql.api.GraphProcessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.GRAPH;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPINGS;
import static fr.inria.corese.kgram.api.core.PointerType.TRIPLE;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.Access;
import java.util.logging.Level;
import javax.ws.rs.core.Response;


/**
 * Plugin for filter evaluator 
 * Compute semantic similarity of classes and solutions 
 * Implement graph specific function for LDScript
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl 
        extends ProxyInterpreter  
        implements ProxyPlugin, GraphProcessor
{

    static public Logger logger = LoggerFactory.getLogger(PluginImpl.class);
    static String DEF_PPRINTER = Transformer.PPRINTER;
    public static boolean readWriteAuthorized = true;
    private static final String NL = System.getProperty("line.separator");
   
    static int nbBufferedValue = 0;
    static final String EXT = ExpType.EXT;
    public static final String METADATA = EXT+"metadata";
    public static final String VISITOR  = EXT+"visitor";
    public static final String LISTEN   = EXT+"listen";
    public static final String SILENT   = EXT+"silent";
    public static final String DEBUG    = EXT+"debug";
    public static final String EVENT    = EXT+"event";
    public static final String VERBOSE  = EXT+"verbose";
    public static final String METHOD   = EXT+"method";
    public static final String EVENT_HIGH = EXT+"events";
    public static final String EVENT_LOW= EXT+"eventLow";
    public static final String SHOW     = EXT+"show";
    public static final String HIDE     = EXT+"hide";
    public static final String NODE_MGR = EXT+"nodeManager";
    public static final String RDF_STAR = EXT+"rdfstar";
    public static final String TYPECHECK= EXT+"typecheck";
    public static final String RDF_TYPECHECK= EXT+"rdftypecheck";
    public static final String VARIABLE = EXT+"variable";
    public static final String URI      = EXT+"uri";
    public static final String TRANSFORMER = EXT+"transformer";
    public static final String BINDING  = EXT+"binding";
    public static final String DYNAMIC_CAPTURE  = EXT+"dynamic";
    
    private static final String QM      = "?";
    
    String PPRINTER = DEF_PPRINTER;
    // for storing Node setProperty() (cf Nicolas Marie store propagation values in nodes)
    // idem for setObject()
    static Table table;
    MatcherImpl match;
    Loader ld;
    private Object dtnumber;
    boolean isCache = false;
    TreeNode cache;
    
    ExtendGraph ext;
    private PluginTransform pt;
    private static IStorage storageMgr;
    private AppxSearchPlugin pas;
    HashMap<String, Query> queryCache;
    
    int index = 0;

    public PluginImpl() {
        init();
    }

    PluginImpl(Matcher m) {
        this();
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
    }
     
    void init(){
        if (table == null) {
            table = new Table();
        }       
        dtnumber = getValue(Processor.FUN_NUMBER);
        cache = new TreeNode();
        ext = new ExtendGraph(this);
        pt  = new PluginTransform(this);
        pas = new AppxSearchPlugin(this); 
        queryCache = new HashMap<String, Query>();
    }

    public static PluginImpl create(Matcher m) {
        return new PluginImpl(m);
    }  
    
    @Override
    public void setMode(int mode){
        switch (mode){
            
            case Evaluator.CACHE_MODE:
                isCache = true;
            break;
                
            case Evaluator.NO_CACHE_MODE:
                isCache = false;
                cache.clear();
            break;                
        }
    }
       
    @Override
    public void start(Producer p, Environment env){
        setMethodHandler(p, env);
    }
    
    
    @Override
    public PluginTransform getComputerTransform(){
        return pt;
    }
    
    /**
     * Draft test
     * Assign class hierarchy to query extension 
     * Goal: emulate method inheritance for xt:method(name, term)
     * Search method name in type hierarchy 
     * @test select where 
     */
    void setMethodHandler(Producer p, Environment env){
        Extension ext = env.getQuery().getActualExtension();
        ASTQuery ast  = (ASTQuery) env.getQuery().getAST();
        if (ext != null && ext.isMethod() && ast.hasMetadata(Metadata.METHOD)){
            ClassHierarchy ch = new ClassHierarchy(getGraph(p));
            if (env.getQuery().getGlobalQuery().isDebug()){
                ch.setDebug(true);
            }
            ext.setHierarchy(ch);
            // WARNING: draft test below
            // store current graph in the Interpreter 
            // hence it does not scale with several graph
            // e.g. in server mode
            Interpreter.getExtension().setHierarchy(ch);
        }
    }
    
    @Override
    public void finish(Producer p, Environment env){
        Graph g = getGraph(p);
        if (g != null){
            g.getContext().setQuery(env.getQuery());
        }
    }

    @Override
    public IDatatype function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {   
            
            case XT_NAME: return name(env);

            case KG_GRAPH:
                return DatatypeMap.createObject(getGraph(p));
                
            case SIM:               
                // solution similarity
                return similarity(env, p);
                
            case DESCRIBE:
                return ext.describe(p, exp, env); 
                
             case XT_EDGES:
                 return edge(env, p, null, null, null);
                 
             case XT_EXISTS:
                 return exists(env, p, null, null, null);
                 
             case APP_SIM:
                 return pas.eval(exp, env, p);
                 
             case XT_ENTAILMENT:
                 return entailment(env, p, null);
                 
             case STL_INDEX:
                return index(env, p);    
                   
            default: 
                return pt.function(exp, env, p);
                
        }
    }
      
    @Override
    public IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt) {
        switch (exp.oper()) {

            case KGRAM:
            case NODE:
           // case LOAD:
            case DEPTH:
            case SKOLEM:
            case DB:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                
                switch (exp.oper()) {
                    case KGRAM:
                        return kgram(env, g, dt);

//                    case NODE:
//                        return node(g, o);

//                    case LOAD:
//                        return load(g, o);

                    case DEPTH:
                        return depth(env, p, dt);
                        
                     case SKOLEM:               
                        return g.skolem(dt);  
                        
                     case DB:
                        return db(dt.getLabel(), g);
                }                
                                        
            case READ:
                return read(dt, env, p);
                                         

            case EVEN: 
                return even(exp, dt);
                
            case ODD:
                return odd(exp, dt);

//            case GET_OBJECT:
//                return DatatypeMap.createObject(getObject(o));
//
//            case SET_OBJECT:
//                return setObject(o, null);

            case QNAME:
                return qname(dt, env);
                
            case PROVENANCE:
                return provenance(exp, env, dt);
                
            case TIMESTAMP:
                return timestamp(exp, env, dt);
                
            case INDEX:
               return index(p, exp, env, dt); 
          
            case ID:
               return id(exp, env, dt); 
                
            case TEST:
                return test(p, exp, env, dt);
                
             case LOAD:
                return load(dt);
                 
             case EXTENSION:
                return ext.extension(p, exp, env, dt); 
                 
             case QUERY:
                return ext.query(p, exp, env, dt); 
                 
             case XT_GRAPH:
             case XT_SUBJECT:
             case XT_PROPERTY:
             case XT_OBJECT:
             case XT_INDEX:
                 return access(exp, env, p, dt);
                 
             case XT_EDGES:
                 return edge(env, p, null, dt, null);
                 
             case XT_EXISTS:
                 return exists(env, p, null, dt, null);    
                                 
             case XT_ENTAILMENT:
                 return entailment(env, p, dt);
                                         
             default:
                 return pt.function(exp, env, p, dt);
           
        }
        
    }

 
    @Override
    public IDatatype function(Expr exp, Environment env, Producer p, IDatatype dt1 , IDatatype dt2) {        
        switch (exp.oper()) {

            case GETP:
                return getProperty(dt1, dt2.intValue());

            case SETP:
                return setProperty(dt1, dt2.intValue(), null);

            case SET_OBJECT:
                return setObject(dt1, dt2);

               
            case SIM:              
            case PSIM:               
            case ANCESTOR:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                switch (exp.oper()) {
                    case SIM:
                        // class similarity
                        return similarity(env, p, dt1, dt2);

                    case PSIM:
                        // prop similarity
                        return pSimilarity(g, dt1, dt2);


                    case ANCESTOR:
                        // common ancestor
                        return ancestor(g, dt1, dt2);
                }
                
             case LOAD:
                return load(dt1, dt2);   

             case WRITE:                
                return write(dt1, dt2);   
                
             case XT_VALUE:
                 return value(p, dt1, dt2, DatatypeMap.ONE);
                 
              case XT_EDGES:
                 return edge(env, p, dt1, dt2, null); 
                 
              case XT_EXISTS:
                 return exists(env, p, dt1, dt2, null);   
                  
              case XT_TUNE:
                 return tune(exp, env, p, dt1, dt2);     
                
            case STORE:
                return ext.store(p, env, dt1, dt2);
                
            case XT_UNION:
                return union(exp, env, p, dt1, dt2);
                
            case XT_MINUS:
            case XT_OPTIONAL:
            case XT_JOIN:
                return algebra(exp, env, p, dt1, dt2);    
                
            default:
                return pt.function(exp, env, p, dt1, dt2);
        }

    }

    @Override
    public IDatatype eval(Expr exp, Environment env, Producer p, IDatatype[] param) {       
        switch (exp.oper()) {
            
            case SETP:
                
                IDatatype dt1 =  param[0];
                IDatatype dt2 =  param[1];
                IDatatype dt3 =  param[2];
                return setProperty(dt1, dt2.intValue(), dt3);
                                                                 
            case APPROXIMATE:
                return pas.eval(exp, env, p, param);
                               
            case XT_EDGES:
                return edge(env, p, param[0], param[1], param[2]);
                
            case XT_EXISTS:
                return exists(env, p, param[0], param[1], param[2]);    
                
             case XT_TRIPLE:
                return triple(exp, env, p, param[0], param[1], param[2]); 
                
            case XT_VALUE:
                 return value(p, param[0], param[1], param[2]);    
                 
             case KGRAM:
                 Graph g = getGraph(p);
                 if (g == null){return null;}
                 return sparql(env, p, param);

            default: 
                return pt.eval(exp, env, p, param);  
        }

    }
    
    @Override
    public IDatatype spin(IDatatype dt) {
        SPINProcess sp = SPINProcess.create();
        try {
            Graph g = sp.toSpinGraph(dt.stringValue());
            return DatatypeMap.createObject(g);
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(PluginImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public IDatatype graph(IDatatype dt) {
        if (dt.getPointerObject() == null) {
            return null;
        }
        Graph g;
        
        switch (dt.pointerType()) {
            case MAPPINGS:
                g = graph(dt.getPointerObject().getMappings());
                if (g == null) {
                    return null;
                }
                return DatatypeMap.createObject(g);
                
            case GRAPH:
                return dt; 
        }
        
        return null;
    }
    
    Graph graph(Mappings map) {
        return MappingsGraph.create(map).getGraph();
    }
    
    @Override
    public IDatatype format(Mappings map, int format) {
        ResultFormat ft = ResultFormat.create(map, format);
        return DatatypeMap.newInstance(ft.toString());
    }
    
    @Override
    public IDatatype format(IDatatype[] ldt) {
        return pt.format(ldt);
    }
    
    @Override
    public IDatatype approximate(Expr exp, Environment env, Producer p, IDatatype[] param) {
        return pas.eval(exp, env, p, param);
    }
      
    @Override
    public IDatatype approximate(Expr exp, Environment env, Producer p) {
        return pas.eval(exp, env, p);
    }
    
    @Override
    public IDatatype similarity(Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        Graph g = getGraph(p);
        Node n1 = g.getNode(dt1); //.getLabel());
        Node n2 = g.getNode(dt2); //.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    IDatatype ancestor(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1); //.getLabel());
        Node n2 = g.getNode(dt2); //.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        Node n = distance.ancestor(n1, n2);
        return (IDatatype) n.getValue();
    }

    IDatatype pSimilarity(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setPropertyDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    /**
     * Similarity of a solution with Corese method Sum distance of approximate
     * types Divide by number of nodes and edge
     *
     * TODO: cache distance in Environment during query proc
     */
    @Override
    public IDatatype similarity(Environment env, Producer p) {
        Graph g = getGraph(p);
        if (g == null) {
            return null;
        }
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        if (memory.getQueryEdges() == null){
            return getValue(0);
        }
        Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
        Distance distance = g.setClassDistance();

        // number of node + edge in the answer
        int count = 0;
        float dd = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null) {
                Edge edge = memory.getEdge(qEdge);

                if (edge != null) {
                    count += 1;

                    for (int i = 0; i < edge.nbNode(); i++) {
                        // count nodes only once
                        Node n = edge.getNode(i);
                        if (!visit.containsKey(n)) {
                            count += 1;
                            visit.put(n, true);
                        }
                    }

                    if ((g.isType(qEdge) || env.getQuery().isRelax(qEdge))
                            && qEdge.getNode(1).isConstant()) {

                        Node qtype = g.getNode(qEdge.getNode(1).getLabel());
                        Node ttype = g.getNode(edge.getNode(1).getLabel());

                        if (qtype == null) {
                            // query type is undefined in ontology
                            qtype = qEdge.getNode(1);
                        }
                        if (ttype == null) {
                            // target type is undefined in ontology
                            ttype = edge.getNode(1);
                        }

                        if (!subClassOf(g, ttype, qtype, env)) {
                            dd += distance.distance(ttype, qtype);
                        }
                    }
                }
            }
        }

        if (dd == 0) {
            return getValue(1);
        }

        double sim = distance.similarity(dd, count);

        return getValue(sim);

    }

    boolean subClassOf(Graph g, Node n1, Node n2, Environment env) {
        if (match != null) {
            return match.isSubClassOf(n1, n2, env);
        }
        return g.isSubClassOf(n1, n2);
    }

    
    IDatatype load(IDatatype dt) {
         return load(dt, null);
    }

    public IDatatype load(IDatatype dt, IDatatype format) {
        return load(dt, null, format, null);
    }
    
    @Override
    public IDatatype load(IDatatype dt, IDatatype graph, IDatatype expectedFormat, IDatatype requiredFormat) {
//        if (!readWriteAuthorized()) {
//            return null;
//        }
        Graph g;
        if (graph == null || graph.pointerType() != GRAPH) {
            g = Graph.create();
        } else {
            g = (Graph) graph.getPointerObject();
        }
        Load ld = Load.create(g);
        try {
            if (requiredFormat == null) {
                ld.parse(dt.getLabel(), getFormat(expectedFormat));
            } else {
                //System.out.println("PI: " + requiredFormat + " " + getFormat(requiredFormat));
                ld.parseWithFormat(dt.getLabel(), getFormat(requiredFormat));
            }
        } catch (LoadException ex) {
            logger.error("Load error: " + dt + " " + ((requiredFormat != null) ? requiredFormat : ""));
            logger.error(ex.getMessage());
            //ex.printStackTrace();
        }
        IDatatype res = DatatypeMap.createObject(g);
        return res;
    }
    
    int getFormat(IDatatype dt) {
        return (dt == null) ? Load.UNDEF_FORMAT : LoadFormat.getDTFormat(dt.getLabel());
    }
    
    @Override
    public IDatatype write(IDatatype dtfile, IDatatype dt) {
//        if (!readWriteAuthorized()) {
//            return null;
//        }
        QueryLoad ql = QueryLoad.create();
        ql.write(dtfile.getLabel(), dt.getLabel());
        return dt;
    }
    
    @Override
    public IDatatype syntax(IDatatype syntax, IDatatype graph, IDatatype node) {
        Graph g = (Graph) graph.getPointerObject();
        ResultFormat ft = ResultFormat.create(g, syntax.getLabel()); 
        String str = (node == null)?ft.toString():ft.toString(node);
        return DatatypeMap.newInstance(str);
    }
   
    static boolean readWriteAuthorized() {
        return Access.provide(Access.Feature.READ_WRITE);
    }

    Path getPath(Expr exp, Environment env){
        Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
        if (qNode == null) {
            return null;
        }
        Path p = env.getPath(qNode);
        return p;
    }
    
    Edge getEdge(Expr exp, Environment env){
        Memory mem = (Memory) env;
        return mem.getEdge(exp.getExp(0).getLabel());
    }
    
    private IDatatype provenance(Expr exp, Environment env, IDatatype dt) {
       Edge e = getEdge(exp, env);
       if (e == null){
           return  null;
       }
        return DatatypeMap.createObject(e.getProvenance());
    }
    
    // index of rule provenance object
     private IDatatype id(Expr exp, Environment env, IDatatype dt) {
       Object obj = dt.getObject();
       if (obj != null && obj instanceof Query){
           Query q = (Query) obj;
           return getValue(q.getID());
       }
       return null;
    }

    private IDatatype timestamp(Expr exp, Environment env, IDatatype dt) {
         Edge e = getEdge(exp, env);
        if (e == null){
            return  null;
        }
        int level = e.getIndex();
        return getValue(level);
    }
    
    
    public IDatatype index(Producer p, Expr exp, Environment env, IDatatype dt){
        Node n = p.getNode(dt);
        return getValue(n.getIndex());
    }
    
    private IDatatype test(Producer p, Expr exp, Environment env, IDatatype dt) {
        IDatatype res = DatatypeMap.createObject("rule", env.getQuery());
        return res;
    }

    private IDatatype even(Expr exp, IDatatype dt) {
        boolean b = dt.intValue() % 2 == 0 ;
        return getValue(b);
    }
    
    private IDatatype odd(Expr exp, IDatatype dt) {
        boolean b = dt.intValue() % 2 != 0 ;
        return getValue(b);        
    }

    private IDatatype bool(Expr exp, Environment env, Producer p, IDatatype dt) {
        if (dt.stringValue().contains("false")){
            return FALSE;
        }
        return TRUE;
    }

    /**
     * @return the pt
     */
    public PluginTransform getPluginTransform () {
        return pt;
    }
    
    @Override
    public IDatatype index(Environment env, Producer p) {
        return getValue(index++);
    }
       
    @Override
    public IDatatype entailment(Environment env, Producer p, IDatatype dt) { 
        Graph g = getGraph(p);
        if (dt != null && dt.isPointer() && dt.getPointerObject().pointerType() == GRAPH){
            g = (Graph) dt.getPointerObject().getTripleStore();
        }
        boolean b = env.getEval().getSPARQLEngine().isSynchronized();
        if (g.isReadLocked() && ! b) {
            // use case where isSynchronised():
            // @afterUpdate, QueryProcess isSynchronised(), we can perform entailment
            logger.info("Graph locked, perform entailment on copy");
            g = g.copy();
        }
        RuleEngine re = RuleEngine.create(g);
        re.setSynchronized(b);
        //re.setVisitor(env.getEval().getVisitor());
        re.setProfile(RuleEngine.OWL_RL);
        re.process();
        return DatatypeMap.createObject(g);
    }
    
    /**
     * param[0] = shapeGrah
     */
    @Override
    public IDatatype shape(Expr exp, Environment env, Producer p, IDatatype[] param) {
        switch (exp.oper()) {
            case XT_SHAPE_GRAPH:
                return shapeGraph(getGraph(p), param);
            case XT_SHAPE_NODE:
                return shapeNode(getGraph(p), param);
        }
        return null;
    }
    
    // param[0] = shapeGrah ; param [1] = shape
    IDatatype shapeGraph(Graph g, IDatatype[] param) {
        if (param.length ==  2) {
            return new ShapeWorkflow().processGraph(g, param[0], param[1]);
        }
        return new ShapeWorkflow().processGraph(g, param[0]);
    }
    
    // param[0] = shapeGrah = s ; param.length >= 2
    IDatatype shapeNode(Graph g, IDatatype[] param) {
        switch (param.length) {
            case 2: return new ShapeWorkflow().processNode(g, param[0], param[1]);
            case 3: return new ShapeWorkflow().processNode(g, param[0], param[1], param[2]);
        }
       return null;
    }
    
    @Override
    public IDatatype insert(Environment env, Producer p, IDatatype... param) {
        Graph g = getGraph(p);
        IDatatype first = param[0];
        Edge e;
        if (param.length==3) {
            e = g.add(first, param[1], param[2]);
        }
        else if (first.isPointer() && first.pointerType() == PointerType.GRAPH){
            Graph gg = (Graph) first.getPointerObject();
            e = gg.add(param[1], param[2], param[3]);
        } 
        else {
            e = g.add(first, param[1], param[2], param[3]);
        }
        return (e==null)?FALSE:TRUE;
    }
    
    @Override
    public IDatatype delete(Environment env, Producer p, IDatatype... param) {
        Graph g = getGraph(p);
        List<Edge> le;
        if (param.length == 3) {
            le = g.delete(null, param[0], param[1], param[2]);
        } else {
            le = g.delete(param[0], param[1], param[2], param[3]);
        }
        return (le == null) ? FALSE : TRUE;
    }
    
    @Override
    public IDatatype value(Environment env, Producer p, IDatatype graph, IDatatype node, IDatatype predicate, int n) {
        Graph g = (Graph) ((graph == null) ?  p.getGraph() :  graph.getPointerObject());
        Node val = g.value(node, predicate, n);
        if (val == null) {
            return null;
        }
        return (IDatatype) val.getDatatypeValue();
    }
    
    @Override
    public IDatatype exists(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) {
        DataProducer dp = new DataProducer(getGraph(p));
        if (env.getGraphNode() != null) {
            dp.from(env.getGraphNode());
        }
        for (Edge ent : dp.iterate(subj, pred, obj)) {
            return (ent == null) ? FALSE :TRUE;
        }
        return FALSE;
    }
    
    @Override
    public IDatatype mindegree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index, IDatatype m) {
        int min = m.intValue();
        if (index == null) {
            // input + output edges
            int d = degree(env, p, node, pred, 0, min) + degree(env, p, node, pred, 1, min);
            return DatatypeMap.newInstance(d>=min);
        }
        int d = degree(env, p, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d>=min);
    }
    
    @Override
    public IDatatype degree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index) { 
        int min = Integer.MAX_VALUE;
        if (index == null) {
            // input + output edges
            int d = degree(env, p, node, pred, 0, min) + degree(env, p, node, pred, 1, min);
            return DatatypeMap.newInstance(d);
        }
        int d = degree(env, p, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d);
    }
        
    int degree(Environment env, Producer p, IDatatype node, IDatatype pred, int n, int min) {
        IDatatype sub = (n==0)?node:null;
        IDatatype obj = (n==1)?node:null;
        DataProducer dp = new DataProducer(getGraph(p));
        Node graph = env.getGraphNode();
        if (graph != null) {
            dp = dp.from(graph);
        }
        int count = 0;
        
        for (Edge edge : dp.iterate(sub, pred, obj)) {
            if (edge == null) {
                break;
            }
            if (node.equals(edge.getNode(n).getDatatypeValue())) {
                count++;
                if (count >= min) {
                    break;
                }
            }
            else {
                break;
            }
        }
        return count;
    }
    
    public Graph getGraph() {
        return (Graph) getProducer().getGraph();
    }
    
    
    // name of  current named graph 
    IDatatype name(Environment env) {
        if (env.getGraphNode() == null) {
            return null;
        }      
        Node n = env.getNode(env.getGraphNode());
        if (n == null) {
            return null;
        }
        return (IDatatype) n.getDatatypeValue();
    }
    
    /*
     * Return Loopable with edges
     */
    @Override
    public IDatatype edge(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) { 
        return edgeList(env, p, subj, pred, obj, null);
    }
    
    @Override
    public IDatatype edge(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) { 
        return edgeList(env, p, subj, pred, obj, graph);
    }
    
    public IDatatype edge(IDatatype subj, IDatatype pred) {   
       return DatatypeMap.createObject(getDataProducer(null, getProducer(), subj, pred, null));        
    }
    
    public IDatatype edge(IDatatype subj, IDatatype pred, IDatatype obj) { 
        return DatatypeMap.createObject(getDataProducer(null, getProducer(), subj, pred, obj));  
    }
    
    IDatatype edgeList(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph);       
        return dp.getEdges();
    }
    
    @Override
    public IDatatype subjects(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph).setDuplicate(false);
        return dp.getSubjects();
    }
    
    @Override
    public IDatatype objects(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph).setDuplicate(false);
        return dp.getObjects();
    }
    
    
    DataProducer getEdgeProducer(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getDataProducer(env, p, subj, pred, obj);
        if (graph != null && (! graph.isList() || graph.size() > 0)) {
            dp.from(graph);
        }
        else if (env != null && env.getGraphNode() != null) {
            dp.from(env.getGraphNode());
        }
        return dp;
    }
          
    DataProducer getDataProducer(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj){
       return new DataProducer(getGraph(p)).setDuplicate(true).iterate(subj, pred, obj);       
    } 
    
  
    IDatatype value(IDatatype dt){
        if (dt == null || dt.isBlank()){
            return null;
        }
        return dt;
    }
    
    IDatatype triple(Expr exp, Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj){
        EdgeQuad edge = EdgeQuad.create(DatatypeMap.newResource(Entailment.DEFAULT), subj, pred, obj);
        return (IDatatype) edge.getNode().getValue();
    }

    private IDatatype accessGraph(Expr exp, Environment env, Producer p, IDatatype dt) {
        if (dt.isPointer()){
            Pointerable obj = dt.getPointerObject();
           switch (dt.pointerType()){
               case TRIPLE:
                   return (IDatatype) obj.getEdge().getGraph().getValue();
               case MAPPINGS:                   
                   return DatatypeMap.createObject(obj.getMappings().getGraph());
           }           
        }
        return null;
    }
    
    private IDatatype access(Expr exp, Environment env, Producer p, IDatatype dt) {
        if (! (dt.isPointer() && dt.pointerType() == TRIPLE)){
            return null;
        }
        Edge ent = dt.getPointerObject().getEdge();        
        switch (exp.oper()){
            case XT_GRAPH:
                return (IDatatype) ent.getGraph().getDatatypeValue();
                
            case XT_SUBJECT:
                return (IDatatype) ent.getNode(0).getDatatypeValue();
                
            case XT_OBJECT:
                return (IDatatype) ent.getNode(1).getDatatypeValue();
                
            case XT_PROPERTY:
                return (IDatatype) ent.getEdgeNode().getDatatypeValue();
                
            case XT_INDEX:
                return getValue(ent.getIndex());
        }
        return null;
    }
    

    public IDatatype value(Producer p, IDatatype subj, IDatatype pred, IDatatype dt) {
       Graph g = getGraph(p);
       Node ns = g.getNode(subj);
       Node np = g.getPropertyNode(pred.getLabel());
       if (ns == null || np == null){
           return null;
       }
       Edge edge = g.getEdge(np, ns, 0);
       if (edge == null){
           return null;
       }
       return (IDatatype) edge.getNode(dt.intValue()).getDatatypeValue();
    }
    
    @Override
    public IDatatype union(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        if ((! (dt1.isPointer() && dt2.isPointer()))
            || (dt1.pointerType() != dt2.pointerType()) ){
            return null;
        }
        
        if (dt1.pointerType() == MAPPINGS){
            return algebra(exp, env, p, dt1, dt2);
        }
        
        if (dt1.pointerType() == GRAPH){
            Graph g1 = (Graph) dt1.getPointerObject();
            Graph g2 = (Graph) dt2.getPointerObject();
            Graph g = g1.union(g2);
            return DatatypeMap.createObject(g);
        }
        
        return null;
    }
    
    @Override
    public IDatatype algebra(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        if ((! (dt1.isPointer() && dt2.isPointer()))
            || (dt1.pointerType() != dt2.pointerType()) ){
            return null;
        }
        
        if (dt1.pointerType() == MAPPINGS){
            Mappings m1 = dt1.getPointerObject().getMappings();
            Mappings m2 = dt2.getPointerObject().getMappings();
            
            Mappings m = null;
            switch (exp.oper()){
                case XT_MINUS:      m = m1.minus(m2); break;
                case XT_JOIN:       m = m1.join(m2); break;
                case XT_OPTIONAL:   m = m1.optional(m2); break;                   
                case XT_UNION:      m = m1.union(m2); break;                   
            }
            
            return DatatypeMap.createObject(m);
        }
        
        return null;
    }
    
    Binding getBinding(Environment env) {
        return (Binding) env.getBind();
    }

    @Override
    public IDatatype tune(Expr exp, Environment env, Producer p, IDatatype... dt) {
        if (dt.length < 2) {
            return null;
        }
        IDatatype dt1 = dt[0];
        IDatatype dt2 = dt[1];
        IDatatype dt3 = (dt.length > 2) ? dt[2] : null;
        Graph g = getGraph(p);
        String label = dt1.getLabel();
        switch (label) {
            case LISTEN:
                if (dt2.booleanValue()) {
                    if (env.getEval() != null) {
                        g.addListener(new GraphListen(env.getEval()));
                    }
                } else {
                    g.removeListener();
                }
                break;
            case VERBOSE:
                getEventManager(p).setVerbose(dt2.booleanValue());
                break;
            case DEBUG:
                switch (dt2.getLabel()) {
                    case TRANSFORMER:
                        // xt:tune(st:debug, st:transformer, st:ds)
                        Transformer.debug(dt3.getLabel(), dt.length > 3 ? dt[3].booleanValue() : true);
                        break;
                        
                    case BINDING:
                        getBinding(env).setDebug(dt3.booleanValue());
                        break;
                        
                    default:
                        getEvaluator().setDebug(dt2.booleanValue());
                }
                break;
                
            case EVENT_HIGH:
                getEventManager(p).setVerbose(dt2.booleanValue());
                getGraph(p).setDebugMode(dt2.booleanValue());
                break;
            case EVENT_LOW:
                getEventManager(p).setVerbose(dt2.booleanValue());
                getEventManager(p).hide(Event.Insert);
                getEventManager(p).hide(Event.Construct);
                getGraph(p).setDebugMode(dt2.booleanValue());
                break;
            case METHOD:
                getEventManager(p).setMethod(dt2.booleanValue());
                break;
            case SHOW:
                getEventManager(p).setVerbose(true);
                Event e = Event.valueOf(dt2.stringValue().substring(NSManager.EXT.length()));
                if (e != null) {
                    getEventManager(p).show(e);
                }
                break;
            case HIDE:
                getEventManager(p).setVerbose(true);
                e = Event.valueOf(dt2.stringValue().substring(NSManager.EXT.length()));
                if (e != null) {
                    getEventManager(p).hide(e);
                }
                break;
            case NODE_MGR:
                getGraph(p).tuneNodeManager(dt2.booleanValue());
                break;
            case VISITOR:
                QuerySolver.setVisitorable(dt2.booleanValue());
                break;
            case EVENT:
                QuerySolverVisitorBasic.setEvent(dt2.booleanValue());
                break;

            case RDF_STAR:
                if (dt2.getLabel().equals(VARIABLE)) {
                    ASTQuery.REFERENCE_QUERY_BNODE = !ASTQuery.REFERENCE_QUERY_BNODE;
                    System.out.println("rdf* query variable: " + ASTQuery.REFERENCE_QUERY_BNODE);
                } else if (dt2.getLabel().equals(URI)) {
                    ASTQuery.REFERENCE_DEFINITION_BNODE = !ASTQuery.REFERENCE_DEFINITION_BNODE;
                    System.out.println("rdf* id uri: " + ASTQuery.REFERENCE_DEFINITION_BNODE);
                }
                break;
                
            case TYPECHECK:
                Function.typecheck = dt2.booleanValue();
                System.out.println("typecheck: " + Function.typecheck);
                break;
            case RDF_TYPECHECK:
                Function.rdftypecheck = dt2.booleanValue();
                System.out.println("rdftypecheck: " + Function.rdftypecheck);
                break;

        }

        return TRUE;
    }
    
    EventManager getEventManager(Producer p) {
        return getGraph(p).getEventManager();
    }
    
    /**
     * @deprecated
     * */
    private Object tune(Expr exp, Environment env, Producer p, IDatatype dt) {
        Graph g = getGraph(p);
        if (dt.getLabel().equals(LISTEN)) {
            return tune(exp, env, p, dt, TRUE);
        } else if (dt.getLabel().equals(SILENT)) {
            return tune(exp, env, p, dt, FALSE);
        }
        return TRUE;
    }

   
 
    class Table extends Hashtable<Integer, PTable> {
    }

    class PTable extends Hashtable<Object, Object> {
    }

    PTable getPTable(Integer n) {
        PTable t = table.get(n);
        if (t == null) {
            t = new PTable();
            table.put(n, t);
        }
        return t;
    }

    Object getObject(Object o) {
        return getProperty(o, Node.OBJECT);
    }

    IDatatype setObject(Object o, Object v) {
        setProperty(o, Node.OBJECT, v);
        return TRUE;
    }

    IDatatype setProperty(Object o, Integer n, Object v) {
        PTable t = getPTable(n);
        t.put(o, v);
        return TRUE;
    }

    IDatatype getProperty(Object o, Integer n) {
        PTable t = getPTable(n);
        return DatatypeMap.createObject(t.get(o));
    }

    Node node(Graph g, IDatatype dt) {
        Node n = g.getNode(dt, false, false);
        return n;
    }

    @Override
    public IDatatype depth(Environment env, Producer p, IDatatype dt) {
        Graph g = getGraph(p);
        Node n = node(g, dt);
        if (n == null){ 
            return null;
        }
        Integer d = g.setClassDistance().getDepth(n);
        if (d == null) {
            return null;
        }
        return getValue(d);
    }

    @Deprecated
    IDatatype load(Graph g, Object o) {
//        if (! readWriteAuthorized()){
//            return null;
//        }
        loader(g);
        IDatatype dt = (IDatatype) o;
        try {
            ld.parse(dt.getLabel());
        } catch (LoadException e) {
            logger.error(e.getMessage());
            return FALSE;
        }
        return TRUE;
    }

    void loader(Graph g) {
        if (ld == null) {
            ld = GraphManager.getLoader();
            ld.init(g);
        }
    }
    
    IDatatype db(Environment env, Graph g){
        ASTQuery ast = (ASTQuery) env.getQuery().getAST();
        String name = ast.getMetadataValue(Metadata.DB);
        return db(name, g);
    }
    
    IDatatype db(String name, Graph g){
        Producer p = QueryProcess.getCreateProducer(g, QueryProcess.DB_FACTORY, name);
        return DatatypeMap.createObject(p);
    }

    IDatatype kgram(Environment env, Graph g, IDatatype dt) {
        return kgram(env, g, dt.getLabel(), null);
    }
    
    /**
     * param[0] = query
     * param[i, i+1] =  var, val
     */
    @Override
    public IDatatype sparql(Environment env, Producer p, IDatatype[] param) {
//        if (! Access.provide(Access.Feature.SPARQL)) {
//            return null;
//        }
        return kgram(env, getGraph(p), param[0].getLabel(), 
                (param.length == 1) ? null : createMapping(p, param, 1));
    }
    
    /**
     * First param is query
     * other param are variable bindings (variable, value)
     */
    Mapping createMapping(Producer p, IDatatype[] param, int start){
        ArrayList<Node> var = new ArrayList<Node>();
        ArrayList<Node> val = new ArrayList<Node>();
        for (int i = start; i < param.length; i += 2){
            var.add(NodeImpl.createVariable(clean(param[i].getLabel())));
            val.add(p.getNode(param[i+1]));
        }
        return Mapping.create(var, val);      
    }
    
    String clean(String name){
        if (name.startsWith("$")){
            return QM.concat(name.substring(1));
        }
        return name;
    }
    
    
    Dataset getDataset(Environment env) {
        Context c = (Context) env.getQuery().getContext();
        if (c != null) {
            return new Dataset(c);
        }
        return null;
    }
    
    
    Dataset getDataset() {        
        Context c = getPluginTransform().getContext();
        if (c != null) {
            return new Dataset(c);
        }
        return null;
    } 
    
    IDatatype kgram(Environment env, Graph g, String  query, Mapping m) {  
        QueryProcess exec = QueryProcess.create(g, true);
        exec.setRule((env==null)?false:env.getQuery().isRule());
        try {
            Mappings map;
            if (g.getLock().getReadLockCount() == 0 && ! g.getLock().isWriteLocked()) {
                // use case: LDScript direct call  
                map = exec.query(query, m, (env==null)?null:getDataset(env));
            }
            else {
                map = exec.sparqlQuery(query, m, (env==null)?null:getDataset(env));
            }
            if (map.getGraph() == null){
                return DatatypeMap.createObject(map);
            }
            else {
                return DatatypeMap.createObject(map.getGraph());
            }
        } catch (EngineException e) {
            return DatatypeMap.createObject(new Mappings());
        }
    }
    
     /**
      * This PluginImpl was created for executing a Method such as java:report()
      * where java: = <function:// ...>
      * This PluginImpl contains Environment and Producer
      * use case: JavaCompiler external function
      */
    public IDatatype kgram(IDatatype query, IDatatype... ldt) {  
        Graph g = getGraph(getProducer());
        Mapping m = null;
        if (ldt.length > 0){
            m = createMapping(getProducer(), ldt, 0);
        }
        else {
            m = new Mapping();
        }
        // share LDScript global variables
        m.setBind(getEnvironment().getBind());
        QueryProcess exec = QueryProcess.create(g, true);
        try {   
            Query q = queryCache.get(query.getLabel());
            if (q == null){
                q = exec.compile(query.getLabel());
                queryCache.put(query.getLabel(), q);
            }
            q.complete(getEnvironment().getQuery(), getPluginTransform().getContext());
            Mappings map = exec.sparqlQuery(q, m);
            if (map.getGraph() == null){
                return DatatypeMap.createObject(map);
            }
            else {
                return DatatypeMap.createObject(map.getGraph());
            }
        } catch (EngineException e) {
            System.out.println(e);
            e.printStackTrace();
            return DatatypeMap.createObject(new Mappings());
        }
    }
    

    IDatatype qname(IDatatype dt, Environment env) {
        if (!dt.isURI()) {
            return dt;
        }
        Query q = env.getQuery();
        if (q == null) {
            return dt;
        }
        ASTQuery ast = (ASTQuery) q.getAST();
        NSManager nsm = ast.getNSM();
        String qname = nsm.toPrefix(dt.getLabel(), true);
        if (qname.equals(dt.getLabel())) {
            return dt;
        }
        return getValue(qname);
    }
    
    
  
    
    IDatatype read(IDatatype dt, Environment env, Producer p){
        return read(dt);
    }
    
    @Override
    public IDatatype read(IDatatype dt){
        QueryLoad ql = QueryLoad.create();
        String str = null;
        try {
            str = ql.readWE(dt.getLabel());
        } catch (LoadException ex) {
            logger.error(  "", ex);
        }
        if (str == null){
            return null; //str = "";
        }
        return DatatypeMap.newInstance(str);
    }

    @Override
    public IDatatype httpget(IDatatype uri) {
//        if (reject(Feature.READ_WRITE)) {
//            return null;
//        }
        try {
            Service s = new Service();
            Response res = s.get(uri.getLabel());
            String str = res.readEntity(String.class);
            return DatatypeMap.newInstance(str);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\n" + uri.getLabel(),"");
        }
        return null;
    }
  
    String getLabel(IDatatype dt) {
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return null;
    }

    public Transformer getTransformer(Environment env, Producer p) {
        return pt.getTransformer(env, p);
    } 
    
    public TemplateVisitor getVisitor(Environment env, Producer p){
        return pt.getVisitor(env, p);
    }
    
    @Override
    public Expr decode(Expr exp, Environment env, Producer p){
        return pt.decode(exp, env, p);
    }

    public void setPPrinter(String str) {
        PPRINTER = str;
    }
    
    /**
     * create concat(str, st:number(), str)
     */
    @Override
    public Expr createFunction(String name, List<Object> args, Environment env)
    throws EngineException {
        return pt.createFunction(name, args, env);
    }
    
    /**
     * exp = funcall(arg, arg)
     * arg evaluates to name
     * Generate extension function for predefined function name
     * rq:plus -> function rq:plus(x, y){ rq:plus(x, y) }
     */
    @Override
    public Expr getDefine(Expr exp, Environment env, String name, int n) throws EngineException{
        if (Processor.getOper(name) == ExprType.UNDEF){
            return null;            
        }       
        Query q = env.getQuery().getGlobalQuery();
        ASTQuery ast = getAST((Expression) exp, q);
        Function fun = ast.defExtension(name, name, n);
        q.defineFunction(fun);
        ASTExtension ext = Interpreter.getCreateExtension(q);
        ext.define(fun);
        return fun;
    }
    
    // use exp AST to compile exp
    // use case: uri() uses ast base
    ASTQuery getAST(Expression exp, Query q){
        ASTQuery ast = exp.getAST();
        if (ast != null) {
            return ast.getGlobalAST();
        } else {
            return (ASTQuery) q.getAST();
        }
    }
    
     public class TreeNode extends TreeMap<IDatatype, IDatatype> {

         TreeNode(){
            super(new Compare());
        }
         
      }

    /**
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node in graph It (may) represent (1
     * integer) and (1.0 float) as two different Nodes Current implementation of
     * EdgeIndex sorted by values ensure join (by dichotomy ...)
     */
     class Compare implements Comparator<IDatatype> {

        public int compare(IDatatype dt1, IDatatype dt2) {

            // xsd:integer differ from xsd:decimal 
            // same node for same datatype 
            if (dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null) {
                int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
                if (cmp != 0) {
                    return cmp;
                }
            }

            int res = dt1.compareTo(dt2);
            return res;
        }
    }
    
     /**
      * STTL create intermediate string result (cf Proxy STL_CONCAT) 
      * Save string value to disk using Fuqi StrManager
      * Each STTL Transformation would have its own StrManager
      * Managed in the Context to be shared between subtransformation (cf OWL2)
      */
    @Override
    public IDatatype getBufferedValue(StringBuilder sb, Environment env){
        if (storageMgr == null){
            createManager();
        }
        if (storageMgr.check(sb.length())){
            IDatatype dt = getValue(sb.toString());
            dt.setValue(dt.getLabel(), nbBufferedValue++, storageMgr);
            return dt;
        }
        else {
            return DatatypeMap.newStringBuilder(sb);
        }               
    }
    
    void createManager(){
        storageMgr = StorageFactory.create(IStorage.STORAGE_FILE, null);
        storageMgr.enable(true);
    }
    
    public GraphProcessor getGraphProcessor() {
        return this;
    }
    
}
