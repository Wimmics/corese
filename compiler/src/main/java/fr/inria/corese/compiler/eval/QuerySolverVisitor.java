package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Hierarchy;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.PointerObject;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.extension.ListSort;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback manager for LDScript functions with specific annotations Eval SPARQL
 * processor calls before() and after()
 *
 * @before      function us:before(?q) {}
 * @after       function us:after(?m) {}
 * @produce     function us:produce(?q) {}
 * @candidate   function us:candidate(?q, ?e) {}
 * @result      function us:result(?m) {}
 * @solution    function us:solution(?m) {}

 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class QuerySolverVisitor extends PointerObject implements ProcessVisitor {
    private static Logger logger = LoggerFactory.getLogger(QuerySolverVisitor.class);
   
    public static final String SHARE    = "@share";
    public static final String INIT     = "@init";
    public static final String BEFORE   = "@before";
    public static final String AFTER    = "@after";
    public static final String START    = "@start";
    public static final String FINISH   = "@finish"; 
    public static final String OVERLOAD = "@overload"; 
    
    public static final String LIMIT    = "@limit";
    public static final String TIMEOUT  = "@timeout";
    public static final String ORDERBY  = "@orderby";
    public static final String DISTINCT = "@distinct";
    public static final String PRODUCE  = "@produce";
    public static final String RESULT   = "@result";
    public static final String STATEMENT= "@statement";
    public static final String CANDIDATE= "@candidate";
    public static final String PATH     = "@path";
    public static final String STEP     = "@step";
    public static final String VALUES   = "@values";
    public static final String BIND     = "@bind";
    public static final String BGP      = "@bgp";
    public static final String JOIN     = "@join";
    public static final String OPTIONAL = "@optional";
    public static final String MINUS    = "@minus";
    public static final String UNION    = "@union";
    public static final String FILTER   = "@filter";
    public static final String SELECT   = "@select";
    public static final String SERVICE  = "@service";
    public static final String QUERY    = "@query";
    public static final String GRAPH    = "@graph";
    public static final String AGGREGATE= "@aggregate";
    public static final String HAVING   = "@having";
    public static final String FUNCTION = "@function";
    

    
    static final String[] EVENT_LIST = {
        BEFORE, AFTER, START, FINISH, PRODUCE, RESULT, STATEMENT, CANDIDATE, PATH, STEP, VALUES, BIND,
        BGP, JOIN, OPTIONAL, MINUS, UNION, FILTER, SELECT, SERVICE, QUERY, GRAPH, 
        AGGREGATE, HAVING, FUNCTION, ORDERBY, DISTINCT
    };
    private boolean active = false;
    boolean select = false;
    private boolean shareable = false;
    private boolean debug = false;
    
    Eval eval;
    Query query;
    ASTQuery ast;
    HashMap <Environment, IDatatype> distinct;
    QuerySolverOverload overload;
  

    QuerySolverVisitor(Eval e) {
        eval = e;
        distinct = new HashMap<>();
        overload = new QuerySolverOverload(this);
    }

    @Override
    public void setProcessor(Eval e) {
        eval = e;
    }
    
    @Override
    public void init(Query q) {
        // Visitor may be reused by let (?g = construct where)
        if (query == null) {
            query = q;
            ast = (ASTQuery) q.getAST();
            setSelect();
            initialize();
            callback(eval, INIT, toArray(q));
        }
    }
    
    void initialize() {
    }
    
    Hierarchy getHierarchy() {
        return getEnvironment().getExtension().getHierarchy();
    }
    
    // datatype(us:km, us:length)
    public IDatatype datatype(IDatatype type, IDatatype sup) {
        getHierarchy().defSuperType(type, sup);
        return type;
    }
    
    @Override
    public DatatypeValue datatype(DatatypeValue type, DatatypeValue sup) {
        getHierarchy().defSuperType(type, sup);
        return type;
    }
    
    List<String> getSuperTypes(IDatatype type) {
        return getHierarchy().getSuperTypes(null, type);
    }
    
    String getSuperType(DatatypeValue dt) {
        return getSuperType ((IDatatype) dt);
    }
    
    String getSuperType(IDatatype type) {
        List<String> list = getSuperTypes(type);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size()-1);
    }
    
    // Draft for testing
    void test() {
        IDatatype dt = DatatypeMap.map();
        dt.set(DatatypeMap.newInstance("test"), DatatypeMap.newInstance(10));
        Binding b = (Binding) getEnvironment().getBind();
        b.setVariable("?global", dt);
    }
    
    void setSelect() {
        setShareable(ast.hasMetadata(SHARE));  
        if (ast.getMetadata() != null) {
            for (String name : EVENT_LIST) {
                if (ast.getMetadata().hasMetadata(name)) {
                    select = true;
                    break;
                }
            }
        }
    }
    
    boolean accept(String name) {
        if (select) {
            return ast.getMetadata().hasMetadata(name) ;
        }
        return true;
    }
        
    @Override
    public IDatatype before(Query q) {
        if (query == q) {
            return callback(eval, BEFORE, toArray(q));
        }
        // subquery
        return start(q);
    }
    
    @Override
    public IDatatype after(Mappings map) {
        if (map.getQuery() == query) {
            return callback(eval, AFTER, toArray(map));
        }
        // subquery
        return finish(map);
    }
    
    @Override
    public IDatatype start(Query q) {
        return callback(eval, START, toArray(q));
    }

    @Override
    public IDatatype finish(Mappings map) {
        return callback(eval, FINISH, toArray(map));
    }
    
    @Override
    public IDatatype orderby(Mappings map) {
        return sort(eval, ORDERBY, toArray(map));
    }
    
    @Override
    public boolean distinct(Eval eval, Query q, Mapping map) {
        IDatatype key = callback(eval, DISTINCT, toArray(q, map));
        if (key == null) {
            return true;
        }       
        return distinct(eval, q, key);
    }
    
    boolean distinct(Eval eval, Query q, IDatatype key) {
        IDatatype res = getDistinct(eval, q).get(key);
        if (res == null) {
            getDistinct(eval, q).set(key, key);
            return true;
        }
        return false;
    }
    
    /**
     * Query and subquery must have different table for distinct
     * As they have different environment, we assign the table to the environment
     */
    IDatatype getDistinct(Eval eval, Query q) {
        IDatatype dt = distinct.get(eval.getEnvironment());
        if (dt == null) {
            dt = DatatypeMap.map();
            distinct.put(eval.getEnvironment(), dt);
        }
        return dt;
    }
    
    @Override
    public boolean limit(Mappings map) {
        IDatatype dt = callback(eval, LIMIT, toArray(map));
        return dt == null || dt.booleanValue();
    }
    
    @Override
    public int timeout(Node serv) {
        IDatatype dt = callback(eval, TIMEOUT, toArray(serv));
        if (dt == null) {
            return 0;
        }
        return dt.intValue();
    }
    
    
    @Override
    public IDatatype produce(Eval eval, Node g, Edge q) {  
        return callback(eval, PRODUCE, toArray(g, q));
    }
      
    @Override
    public IDatatype candidate(Eval eval, Node  g, Edge q, Edge e) {  
        return callback(eval, CANDIDATE, toArray(g, q, e));
    }
        
    @Override
    public boolean result(Eval eval, Mappings map, Mapping m) {       
        return result(callback(eval, RESULT, toArray(map, m)));       
    }
    
    boolean result(IDatatype dt) {
        if (dt == null) {
            return true;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype statement(Eval eval, Node g, Exp e) { 
        return callback(eval, STATEMENT, toArray(g, e));
    }
       
    @Override
    public IDatatype path(Eval eval, Node g, Edge q, Path p, Node s, Node o) {       
        return callback(eval, PATH, toArray(g, q, p, s, o));
    }
    
    @Override
    public boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) {  
         return result(callback(eval, STEP, toArray(g, q, p, s, o)));         
    }
       
    @Override
    public IDatatype values(Eval eval, Node g, Exp e, Mappings m) { 
        return callback(eval, VALUES, toArray(g, e, m));    
    }  
    
    @Override
    public IDatatype bind(Eval eval, Node g, Exp e, DatatypeValue dt) { 
        return callback(eval, BIND, toArray(g, e, dt));    
    } 
    
    @Override
    public IDatatype bgp(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, BGP, toArray(g, e, m));
    }
    
    @Override
    public IDatatype join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, JOIN, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, OPTIONAL, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, MINUS, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, UNION, toArray(g, e, m1, m2));
    }
    
     @Override
    public IDatatype graph(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, GRAPH, toArray(g, e, m));
    }
    
    
    @Override
    public IDatatype service(Eval eval, Node s, Exp e, Mappings m) {       
        return callback(eval, SERVICE, toArray(s, e, m));
    }
    
    @Override
    public IDatatype query(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, QUERY, toArray(g, e, m));
    }
    
    @Override
    public boolean filter() {      
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), FILTER, 3);
        return (exp != null);
    } 
    
    @Override
    public boolean filter(Eval eval, Node g, Expr e, boolean b) {       
        IDatatype dt = callback(eval, FILTER, toArray(g, e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype function(Eval eval, Expr funcall, Expr fundef) {       
        IDatatype dt = callback(eval, FUNCTION, toArray(funcall, fundef));       
        return dt;
    }
    
    
    @Override
    public boolean having(Eval eval, Expr e, boolean b) {       
        IDatatype dt = callback(eval, HAVING, toArray(e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public DatatypeValue select(Eval eval, Expr e, DatatypeValue dt) {       
        IDatatype val = callback(eval, SELECT, toArray(e, dt));
        return dt;
    }
    
    @Override
    public DatatypeValue aggregate(Eval eval, Expr e, DatatypeValue dt) {       
        IDatatype val = callback(eval, AGGREGATE, toArray(e, dt));
        return dt;
    }
    
    
    public void setOverload(boolean b) {
        overload.setOverload(b);
    }
    
    public boolean isOverload() {
        return overload.isOverload();
    }

    
    @Override
    public IDatatype error(Eval eval, Expr exp, DatatypeValue[] args) {
        return  overload.error(eval, exp, (IDatatype[]) args);
    }
    
    @Override
    public boolean overload(Expr exp, DatatypeValue res, DatatypeValue dt1, DatatypeValue dt2) {
        // prevent overload within overload
        return ! isActive() && overload.overload(exp, (IDatatype)res, (IDatatype)dt1, (IDatatype)dt2);
    }
       
   @Override
    public IDatatype overload(Eval eval, Expr exp, DatatypeValue res, DatatypeValue[] args) {
        return overload.overload(eval, exp, (IDatatype) res, (IDatatype[]) args);
    }   
    
    @Override
    public int compare(Eval eval, int res, DatatypeValue dt1, DatatypeValue dt2) {
        if (! isActive() && overload.overload((IDatatype)dt1, (IDatatype)dt2)) {
            return overload.compare(eval, res, (IDatatype)dt1, (IDatatype)dt2);
        }
        return res;
    }
    
    @Override
    public boolean produce() {
        return accept(PRODUCE) && define(PRODUCE, 2);
    }
    
    @Override
    public boolean candidate() {
        return accept(CANDIDATE) && define(CANDIDATE, 3);
    }
    
    @Override
    public boolean statement() {
        return accept(STATEMENT) && define(STATEMENT, 2);
    }
    
    boolean define(String name, int arity) {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), name, arity);
        return (exp != null);
    }
    
    void trace(Eval ev, String metadata, IDatatype[] param) {
        if (isDebug()) {
            logger.info(String.format(metadata));
        }
    }
    
 
    /**
     * @before function us:before(?q)
     * call function us:before
     * set Visitor as inactive during function call to prevent loop and also in case where
     * function execute a query (which would trigger Visitor recursively)
     */
    public IDatatype callback(Eval ev, String metadata, IDatatype[] param) {
        if (isActive() || ! accept(metadata)) {
            return null;
        }
        trace(ev, metadata, param);
        Function function = (Function) eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, param.length);
        if (function != null) {
            // prevent infinite loop in case where there is a query in the function
            setActive(true);
            IDatatype dt = call(function, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
            setActive(false);
            return dt;
        }
        return null;
    }
    
    /**
     * @eq    function us:eq(?e, ?x, ?y)
     * @error function us:error(?e, ?x, ?y)
     * Function call is performed even if Visitor is inactive
     * use case: @select function execute ?a = ?b on extension datatype
     * we want @eq function us:eq(?e, ?x, ?y) to handle ?a = ?b
     */
    public IDatatype callbackBasic(Eval ev, String metadata, IDatatype[] param) {       
        trace(ev, metadata, param);
        Function function = (Function) eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, param.length);
        if (function != null) {
            return call(function, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
        }
        return null;
    }
    
    // param = Mappings map
    IDatatype sort(Eval ev, String metadata, IDatatype[] param) {
        if (isActive() || ! accept(metadata)) {
            return null;
        }
        // function us:compare(?m1, ?m2)
        Function function = (Function) eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, 2);
        if (function != null) {
            // prevent infinite loop in case where there is a query in the function
            setActive(true);
            IDatatype dt = new ListSort("sort").sort((Computer) ev.getEvaluator(), (Binding) ev.getEnvironment().getBind(), ev.getEnvironment(), 
                    ev.getProducer(),  function, param[0] );
            setActive(false);
            return dt;
        }
        return null;
    }
    
    public IDatatype method(Eval ev, String name,  IDatatype[] param) {
        if (isActive()) {
            return null;
        }
        Function exp = (Function) eval.getEvaluator().getDefineMethod(getEnvironment(), name, null, param);
        if (exp != null) {
            setActive(true);
            IDatatype dt = call(exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
            setActive(false);
            return dt;
        }
        return null;
    }
    
    
    public IDatatype methodBasic(Eval ev, String name,  IDatatype[] param) {       
        Function exp = (Function) eval.getEvaluator().getDefineMethod(getEnvironment(), name, null, param);
        if (exp != null) {
            IDatatype dt = call(exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
            return dt;
        }
        return null;
    }
    
    IDatatype call(Function fun, IDatatype[] param, Evaluator eval, Environment env, Producer p) {
        return new Funcall(fun.getFunction().getLabel()).call((Computer) eval, (Binding) env.getBind(), env, p, fun, param);
    }


    public IDatatype funcall(Eval ev, String name, IDatatype[] param) {
        return funcall(name, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
    }

    IDatatype funcall(String name, IDatatype[] param, Evaluator eval, Environment env, Producer p) {
        Function fun = (Function) eval.getDefine(env, name, param.length);
        if (fun != null) {
           return call(fun, param, eval, env, p);
        }
        return null;
    }
    
   
    
    
    IDatatype[] toArray(Object... lobj) {
        IDatatype[] param = new IDatatype[lobj.length];
        int i = 0;
        for (Object obj : lobj) {
            param[i++] = DatatypeMap.getValue(obj);
        }
        return param;
    }

    Environment getEnvironment() {
        return eval.getEnvironment();
    }

       
    @Override
    public boolean isShareable() {
        return shareable;
    }

    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }
    
     /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void sleep(boolean b) {
        setActive(b);
    }
    
    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    @Override
    public Object getObject() {
        return this;
    }
    

}
