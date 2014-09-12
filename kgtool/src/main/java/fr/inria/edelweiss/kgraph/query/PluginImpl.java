package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Processor;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.path.Path;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.xml.stream.events.Namespace;

/**
 * Plugin for filter evaluator Compute semantic similarity of classes and
 * solutions for KGRAPH
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {

    static Logger logger = Logger.getLogger(PluginImpl.class);
    static String DEF_PPRINTER = Transformer.PPRINTER;
    private static final String NL = System.getProperty("line.separator");
   
    String PPRINTER = DEF_PPRINTER;
    // for storing Node setProperty() (cf Nicolas Marie store propagation values in nodes)
    // idem for setObject()
    static Table table;
    MatcherImpl match;
    Loader ld;
    private Object dtnumber;
    boolean isCache = false;
    TreeNode cache;

    PluginImpl(Matcher m) {
        if (table == null) {
            table = new Table();
        }
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
        dtnumber = getValue(Processor.FUN_NUMBER);
        cache = new TreeNode();
    }

    public static PluginImpl create(Matcher m) {
        return new PluginImpl(m);
    }  
    
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
    
    // DRAFT: store current query in the Graph
    public void start(Producer p, Environment env){
        
    }
    
    public void finish(Producer p, Environment env){
        Graph g = getGraph(p);
        if (g != null){
            g.setQueryNode(DatatypeMap.createObject("query", env.getQuery()));
        }
    }

    public Object function(Expr exp, Environment env, Producer p) {

        switch (exp.oper()) {         

            case GRAPH:
                return getGraph(p);

            case LEVEL:
                return getLevel(env, p);
                
            case STL_NL:
                return nl(null, env, p);
                
            case STL_ISSTART:
                return isStart(env, p);

            case PROLOG:
                return prolog(null, env, p);
                
            case STL_PREFIX:
                return prefix(env, p);
                
            case STL_NUMBER:
                return getValue(1 + env.count());
                                     
            case FOCUS_NODE:
                return getFocusNode(null, env);

            case SIM:
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                // solution similarity
                return similarity(g, env);
                
            case DESCRIBE:
                return describe(p, exp, env);
                
            case QUERY:
                return query(p, exp, env);
                         
                
        }

        return null;
    }

    public Object function(Expr exp, Environment env, Producer p, Object o) {
        IDatatype dt = datatype(o);

        switch (exp.oper()) {

            case KGRAM:
            case NODE:
           // case LOAD:
            case DEPTH:
            case SKOLEM:
                
                Graph g = getGraph(p);
                if (g == null){
                    return null;
                }
                
                switch (exp.oper()) {
                    case KGRAM:
                        return kgram(g, o);

                    case NODE:
                        return node(g, o);

//                    case LOAD:
//                        return load(g, o);

                    case DEPTH:
                        return depth(g, o);
                        
                     case SKOLEM:               
                        return g.skolem(dt);    
                }                
                
            case INDENT:
                return indent(dt, env, p);
              
            case STL_NL:
                return nl(dt, env, p);   
                
            case PROLOG:
                return prolog(dt, env, p);
                
            case STL_PROCESS:
                return process(exp, env, p, dt);
                
            case APPLY_TEMPLATES:
            case APPLY_TEMPLATES_ALL:
                return pprint(dt, null, null, null, exp, env, p);

            case CALL_TEMPLATE:
                return pprint(null, dt, null, exp, env, p);

            case APPLY_TEMPLATES_WITH:
                return pprint(dt, null, null, exp, env, p);
                
            case APPLY_TEMPLATES_GRAPH:
            case APPLY_TEMPLATES_NOGRAPH:
                return pprint(null, null, dt, exp, env, p);

            case TURTLE:
                return turtle(dt, env, p);

            case PPURI:
            case URILITERAL:
            case XSDLITERAL:
                return uri(exp, dt, env, p);
                
            case STL_LOAD:
                load(dt, env, p);
                return EMPTY;
                               
            case FOCUS_NODE:
                return getFocusNode(dt, env);    

            case VISITED:
                return visited(dt, env, p);



            case GET:
                return getObject(o);

            case SET:
                return setObject(o, null);

            case QNAME:
                return qname(o, env);
                
            case PROVENANCE:
                return provenance(exp, env, o);
                
            case TIMESTAMP:
                return timestamp(exp, env, o);
                
            case INDEX:
               return index(p, exp, env, o); 
          
            case ID:
               return id(exp, env, dt); 
                
            case TEST:
                return test(p, exp, env, dt);
                
             case LOAD:
                return load(p, exp, env, dt);    
           
        }
        return null;
    }

    private IDatatype visited(IDatatype dt, Environment env, Producer p) {
        Transformer pp = getTransformer(env, p);
        boolean b = pp.isVisited(dt);
        return getValue(b);
    }

    public Object function(Expr exp, Environment env, Producer p, Object o1, Object o2) {
        IDatatype dt1 = (IDatatype) o1,
                dt2 = (IDatatype) o2;
        switch (exp.oper()) {

            case GETP:
                return getProperty(dt1, dt2.intValue());

            case SETP:
                return setProperty(dt1, dt2.intValue(), null);

            case SET:
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
                        return similarity(g, dt1, dt2);

                    case PSIM:
                        // prop similarity
                        return pSimilarity(g, dt1, dt2);


                    case ANCESTOR:
                        // common ancestor
                        return ancestor(g, dt1, dt2);
                }

             case WRITE:                
                return write(dt1, dt2);   
                
            case APPLY_TEMPLATES:
            case APPLY_TEMPLATES_ALL:
                // dt1: focus
                // dt2: arg
                return pprint(dt1, dt2, null, null, exp, env, p);
                               
            case APPLY_TEMPLATES_WITH_GRAPH:
            case APPLY_TEMPLATES_WITH_NOGRAPH:
                // dt1: transformation 
                // dt2: graph            
                return pprint(dt1, null, dt2, exp, env, p);

            case APPLY_TEMPLATES_WITH:
            case APPLY_TEMPLATES_WITH_ALL:
                // dt1: transformation
                // dt2: focus
                if (isCache){
                    IDatatype dt = cache.get(dt2);
                    if (dt != null){
                        return dt;
                    }
                }
                IDatatype dt =  pprint(dt2, null, dt1, null, exp, env, p);
                if (isCache){
                    cache.put(dt2, dt);
                }
                return dt;

            case CALL_TEMPLATE:
                // dt1: template name
                // dt2: focus
                return pprint(dt2, null, null, dt1, exp, env, p);

            case CALL_TEMPLATE_WITH:
                // dt1: transformation
                // dt2: template name
                return pprint(dt1, dt2, null, exp, env, p);
                
            case STORE:
                return store(p, dt1, dt2);

        }

        return null;
    }

    public Object eval(Expr exp, Environment env, Producer p, Object[] args) {

        IDatatype dt1 =  (IDatatype) args[0];
        IDatatype dt2 =  (IDatatype) args[1];
        IDatatype dt3 =  (IDatatype) args[2];

        switch (exp.oper()) {

            case SETP:
                return setProperty(dt1, dt2.intValue(), dt3);


            case CALL_TEMPLATE:
                // dt1: template name
                // dt2: focus
                // dt3: arg
                return pprint(getArgs(args, 1), dt2, dt3, null, dt1, null, exp, env, p);

            case CALL_TEMPLATE_WITH:
                // dt1: transformation
                // dt2: template name
                // dt3: focus
                return pprint(getArgs(args, 2), dt3, null, dt1, dt2, null, exp, env, p);

            case APPLY_TEMPLATES_WITH:
            case APPLY_TEMPLATES_WITH_ALL:
                // dt1: transformation
                // dt2: focus
                // dt3: arg
                if (isCache){
                    IDatatype dt = cache.get(dt2);
                    if (dt != null){
                        return dt;
                    }
                }
                IDatatype dt = pprint(getArgs(args, 1), dt2, dt3, dt1, null, null, exp, env, p);
                //System.out.println("PI: " + dt2 + " " + dt3 + " " + dt);
                if (isCache){
                    cache.put(dt2, dt);
                }
                return dt;
                
            case APPLY_TEMPLATES:
            case APPLY_TEMPLATES_ALL:
                // dt1: focus
                // dt2: arg
                return pprint(getArgs(args, 0), dt1, dt2, null, null, null, exp, env, p);  
                
            case APPLY_TEMPLATES_WITH_GRAPH:
            case APPLY_TEMPLATES_WITH_NOGRAPH:
                // dt1: transformation 
                // dt2: graph 
                // dt3; focus
                return pprint(getArgs(args, 2), dt3, null, dt1, null, dt2, exp, env, p);

        }

        return null;
    }
    
    
    Object[] getArgs(Object[] obj, int n){
        return Arrays.copyOfRange(obj, n, obj.length);
    }

    IDatatype similarity(Graph g, IDatatype dt1, IDatatype dt2) {

        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    IDatatype ancestor(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
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
    public IDatatype similarity(Graph g, Environment env) {
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
        Distance distance = g.setClassDistance();

        // number of node + edge in the answer
        int count = 0;
        float dd = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null) {
                Entity edge = memory.getEdge(qEdge);

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

    
    private IDatatype write(IDatatype dtfile, IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        ql.write(dtfile.getLabel(), dt.getLabel());
        return dt;
    }
   
    private Object getFocusNode(IDatatype dt, Environment env) {
        String name = Transformer.IN;
        if (dt != null){
            name = dt.getLabel();
        }
        Node node = env.getNode(name);
        if (node == null){
            return null;
        }
        return node.getValue();   
    }

    Path getPath(Expr exp, Environment env){
        Node qNode = env.getQueryNode(exp.getExp(0).getLabel());
        if (qNode == null) {
            return null;
        }
        Path p = env.getPath(qNode);
        return p;
    }
    
    Entity getEdge(Expr exp, Environment env){
        Memory mem = (Memory) env;
        return mem.getEdge(exp.getExp(0).getLabel());
    }
    
    private Object provenance(Expr exp, Environment env, Object o) {
       Entity e = getEdge(exp, env);
       if (e == null){
           return  null;
       }
        return e.getProvenance();
    }
    
    // index of rule provenance object
     private Object id(Expr exp, Environment env, IDatatype dt) {
       Object obj = dt.getObject();
       if (obj != null && obj instanceof Query){
           Query q = (Query) obj;
           return getValue(q.getID());
       }
       return null;
    }

    private Object timestamp(Expr exp, Environment env, Object o) {
         Entity e = getEdge(exp, env);
        if (e == null){
            return  null;
        }
        int level = e.getEdge().getIndex();
        return getValue(level);
    }
    
    public IDatatype index(Producer p, Expr exp, Environment env, Object o){
        IDatatype dt = (IDatatype) o;
        Node n = p.getNode(dt);
        return getValue(n.getIndex());
    }
    
    private Object test(Producer p, Expr exp, Environment env, IDatatype dt) {
        IDatatype res = DatatypeMap.createObject("rule", env.getQuery());
        return res;
    }
    
    private Object query(Producer p, Expr exp, Environment env) {
        Graph g = getGraph(p);
        Node q = g.getQueryNode();
        if (q == null){
            q = DatatypeMap.createObject("query", env.getQuery());
        }
        return q;
    }

     private Object load(Producer p, Expr exp, Environment env, IDatatype dt) {
         Graph g = Graph.create();
         Load ld = Load.create(g);
         try {
             ld.load(dt.getLabel(), Load.TURTLE_FORMAT);
         } catch (LoadException ex) {
             logger.error("Load error: " + dt);
         }
        IDatatype res = DatatypeMap.createObject("load", g);
        return res;
    }
    
    private Object describe(Producer p, Expr exp, Environment env) {
        IDatatype res = DatatypeMap.createObject("index", describe((Graph)p.getGraph()));
        return res;
    }
    
    
    
    /**
     * Generate an RDF Graph that describes the KGRAM system and the 
     * current RDF graph
     */
    Graphable describe(final Graph g){
        return new Graphable(){

            @Override
            public String toGraph() {
                return (g.toString());
            }

            @Override
            public void setGraph(Object obj) {
            }

            @Override
            public Object getGraph() {
                return null;
            }
            
        };
    }

    /**
     * obj has getObject() which is Graphable
     * store the graph has an extended named graph
     */
    private Object store(Producer p, IDatatype name, IDatatype obj) {
        if (p.isProducer(obj)){
            Producer pp = p.getProducer(obj);
            Graph g = (Graph) p.getGraph();
            g.setNamedGraph(name.getLabel(), (Graph) pp.getGraph());        
        }
        return obj;
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

    Object getProperty(Object o, Integer n) {
        PTable t = getPTable(n);
        return t.get(o);
    }

    Node node(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        Node n = g.getNode(dt, false, false);
        return n;
    }

    IDatatype depth(Graph g, Object o) {
        Node n = node(g, o);
        if (n == null || g.getClassDistance() == null) {
            return null;
        }
        Integer d = g.getClassDistance().getDepth(n);
        if (d == null) {
            return null;
        }
        return getValue(d);
    }

    IDatatype load(Graph g, Object o) {
        loader(g);
        IDatatype dt = (IDatatype) o;
        try {
            ld.loadWE(dt.getLabel());
        } catch (LoadException e) {
            logger.error(e);
            return FALSE;
        }
        return TRUE;
    }

    void loader(Graph g) {
        if (ld == null) {
            ld = ManagerImpl.getLoader();
            ld.init(g);
        }
    }

    Object kgram(Graph g, Object o) {
        IDatatype dt = (IDatatype) o;
        String query = dt.getLabel();
        QueryProcess exec = QueryProcess.create(g, true);
        try {
            Mappings map = exec.sparqlQuery(query);
            if (map.getGraph() == null){
                return DatatypeMap.createObject("Mappings", map);
            }
            else {
                return DatatypeMap.createObject("Graph", map.getGraph());
            }
        } catch (EngineException e) {
            return new Mappings();
        }
    }

    IDatatype qname(Object o, Environment env) {
        IDatatype dt = (IDatatype) o;
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
    
    /**
     * Increment indentation level
     */
    IDatatype indent(IDatatype dt, Environment env, Producer prod) {
        Transformer t = getTransformer(env, prod);
        t.setLevel(t.getLevel() + dt.intValue());        
        return EMPTY;
    }

    /**
     * New Line with indentation given by t.getLevel()
     * Increment level if dt!=null
     */
    IDatatype nl(IDatatype dt, Environment env, Producer prod) {
        Transformer t = getTransformer(env, prod);
        if (dt != null){
            t.setLevel(t.getLevel() + dt.intValue());
        }
        return t.tabulate();
   }
    
    IDatatype prolog(IDatatype dt, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        String title = null;
        if (dt != null){
            title = dt.getLabel();
        }
        String pref = p.getNSM().toString(title);
        return getValue(pref);
    }
    
    Mappings prefix(Environment env, Producer prod){
         Transformer p = getTransformer(env, prod);                 
         return p.NSMtoMappings();
    }
    
    IDatatype isStart(Environment env, Producer prod){
         Transformer p = getTransformer(env, prod);  
         boolean b = p.isStart();
         return getValue(b);
    }
      
    IDatatype pprint(IDatatype trans, IDatatype temp, IDatatype name, Expr exp, Environment env, Producer prod) {
        Transformer p = getTransformer(exp, env, prod, getLabel(trans), getLabel(name));
        return p.process(getLabel(temp),
                exp.oper() == ExprType.APPLY_TEMPLATES_ALL
                || exp.oper() == ExprType.APPLY_TEMPLATES_WITH_ALL,
                exp.getModality());
    }

 
    /**
     * exp is the calling expression: kg:pprint kg:pprintAll kg:template focus
     * is the node to be printed tbase is the path of the template base to be
     * used, may be null temp is the name of a named template, may be null
     * modality: kg:pprintAll(?x ; separator = "\n")
     */
    
    
     IDatatype pprint(IDatatype focus, IDatatype arg, IDatatype trans, IDatatype temp, Expr exp, Environment env, Producer prod) {
          return pprint(null, focus, arg, trans, temp, null, exp, env, prod); 
     }
     
     IDatatype pprint(Object[] args, IDatatype focus, IDatatype arg, IDatatype trans, IDatatype temp, IDatatype name, 
             Expr exp, Environment env, Producer prod) {        
        Transformer p = getTransformer(exp, env, prod, getLabel(trans), getLabel(name));
        IDatatype dt = p.process(args, focus, arg,
                getLabel(temp),
                exp.oper() == ExprType.APPLY_TEMPLATES_ALL
                || exp.oper() == ExprType.APPLY_TEMPLATES_WITH_ALL,
                exp.getModality(), exp, env.getQuery());
        return dt;
    }
     
    /**
     * st:process(var) : default variable processing by SPARQL Template
     * Ask PPrinter what is default behavior
     * set st:process() to it's default behavior
     * the default behavior is st:apply-templates
     */
    public Object process(Expr exp, Environment env, Producer p, IDatatype dt) {
        Query q = env.getQuery();
        Transformer pp = getTransformer(env, p);
        // overload current st:process() oper code to default behaviour oper code
        // future executions of this st:process() will directly execute target default behavior
        Expr def = q.getProfile(Transformer.STL_PROCESS); //pp.getProcessExp();
        
        if (def == null){
            int oper = pp.getProcess();                     
            exp.setOper(oper);
            Object res = function(exp, env, p, dt);
            // if we want STL_PROCESS to get back to it's initial behavior:
            // unset the comment below
            // exp.setOper(ExprType.STL_PROCESS);
            return res;
        }
        else {     
            Expr ee = rewrite(exp, def, (ASTQuery)env.getQuery().getAST());          
            exp.setOper(SELF);
            exp.setExp(0, ee);
            return getEvaluator().eval(ee, env, p);
        } 
    }
    
    
    /**
     * proc: st:process(?y)
     * def:  st:process(?x) = st:apply-templates(?x)
     * copy def right exp and rename its variable (?x) as proc variable (?y)
     * PRAGMA: do no process exists {} in def
     */
    Expr rewrite(Expr proc, Expr def, ASTQuery ast){
        Term tproc = (Term) proc;
        Term tdef  = (Term) def;
        Variable v1 = tdef.getArg(0).getArg(0).getVariable(); // ?x
        Variable v2 = tproc.getArg(0).getVariable(); // ?y
        Expression tt = tdef.getArg(1).copy(v1, v2);
        tt.compile(ast);
        return tt;
    }

    
    /**
     * 
    
     */


    IDatatype turtle(IDatatype o, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        IDatatype dt = p.turtle(o);
        return dt;
    }
    
    IDatatype xsdLiteral(IDatatype o, Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        IDatatype dt = p.xsdLiteral(o);
        return dt;
    }

    IDatatype uri(Expr exp, IDatatype dt, Environment env, Producer prod) {
        if (dt.isURI()) {
            return turtle(dt, env, prod);
        } else if (dt.isLiteral() && exp.oper() == ExprType.URILITERAL) {
            return turtle(dt, env, prod);
        } else if (dt.isLiteral() && exp.oper() == ExprType.XSDLITERAL) {
            return xsdLiteral(dt, env, prod);
        } else {
            return pprint(dt, null, null, null, exp, env, prod);
        }
    }
    
    private void load(IDatatype dt, Environment env, Producer p) {
        Transformer t = getTransformer(env, p);
        t.load(dt.getLabel());
    }

    IDatatype getLevel(Environment env, Producer prod) {
        return getValue(level(env, prod));
    }

    int level(Environment env, Producer prod) {
        Transformer p = getTransformer(env, prod);
        return p.level();
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

    Transformer getTransformer(Environment env, Producer p) {
        return getTransformer(null, env, p, (String) null, null);
    }
    
    Transformer getTransformer(Expr exp, Environment env, Producer prod, String trans) {
        return getTransformer(exp, env, prod, trans, null);
    }
    
    /**
     * name is a named graph
     * TODO: cache for named graph
     */    
    Transformer getTransformer(Expr exp, Environment env, Producer prod, String trans, String name) {    
        Query q = env.getQuery();
        String p = null;

        if (trans != null) {
            p = trans;
        } else if (q.hasPragma(Pragma.TEMPLATE)) {
            p = (String) q.getPragma(Pragma.TEMPLATE);
        } 

        Transformer t = (Transformer) q.getTransformer(p);
        
        if (p == null && t != null){
            p = t.getTransformation();
        }
        
        if (name != null){
            // transform named graph
            try {
                boolean with = (exp == null) ? true : 
                           exp.oper() == ExprType.APPLY_TEMPLATES_GRAPH
                        || exp.oper() == ExprType.APPLY_TEMPLATES_WITH_GRAPH;
                
               Transformer gt = Transformer.create((Graph) prod.getGraph(), p, name, with);
               
               if (t == null){
                   // get current transformer if any to get its NSManager 
                  t = (Transformer) q.getTransformer(null);
               }
               if (t != null && t.getNSM().isUserDefine()){
                   gt.setNSM(t.getNSM());
               }
               else {
                   gt.setNSM(((ASTQuery) q.getAST()).getNSM());
               }  
               
               t = gt;
            } catch (LoadException ex) {
                logger.error(ex);
                t = Transformer.create(Graph.create(), null);
            }
       }
        else if (t == null) {    
            t = Transformer.create(prod, p);
            t.setNSM(((ASTQuery) q.getAST()).getNSM());
            q.setTransformer(p, t);
        }
        
        return t;
    }
    
  

    public void setPPrinter(String str) {
        PPRINTER = str;
    }
    
    /**
     * create concat(str, st:number(), str)
     */
    public Expr createFunction(String name, List<Object> args, Environment env){
        Term t = Term.function(name);
        for (Object arg : args){
            if (arg instanceof IDatatype){
                // str: arg is a StringBuilder, keep it as is
                Constant cst = Constant.create("Future", null, null);
                cst.setDatatypeValue((IDatatype) arg);
                t.add(cst);
            }
            else {
                // st:number()
               t.add((Expression) arg);
            }
        }
        t.compile((ASTQuery)env.getQuery().getAST());
        return t;
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
    
    
    
}
