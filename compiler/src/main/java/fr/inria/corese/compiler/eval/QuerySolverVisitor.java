package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
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

    public static final String SHARE    = "@share";
    public static final String BEFORE   = "@before";
    public static final String AFTER    = "@after";
    public static final String START    = "@start";
    public static final String FINISH   = "@finish";    
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
    public static final String FEDERATE = "@federate";
    public static final String QUERY    = "@query";
    public static final String GRAPH    = "@graph";
    public static final String AGGREGATE= "@aggregate";
    public static final String HAVING   = "@having";
    public static final String FUNCTION = "@function";
    
    static final String[] EVENT_LIST = {
        BEFORE, AFTER, START, FINISH, PRODUCE, RESULT, STATEMENT, CANDIDATE, PATH, STEP, VALUES, BIND,
        BGP, JOIN, OPTIONAL, MINUS, UNION, FILTER, SELECT, FEDERATE, QUERY, GRAPH, 
        AGGREGATE, HAVING, FUNCTION, ORDERBY, DISTINCT
    };
    boolean active = false, select = false;
    private boolean shareable = false;
    
    Eval eval;
    Query query;
    ASTQuery ast;
    IDatatype distinct;

    QuerySolverVisitor(Eval e) {
        eval = e;
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
            distinct = DatatypeMap.map();
        }
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
        // special case: let(construct where)
        return start(q);
    }
    
    @Override
    public IDatatype after(Mappings map) {
        if (map.getQuery() == query) {
            return callback(eval, AFTER, toArray(map));
        }
        // special case: let(construct where)
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
    public boolean distinct(Mapping map) {
        IDatatype key = callback(eval, DISTINCT, toArray(map));
        if (key == null) {
            return true;
        }
        IDatatype res = distinct.get(key);
        if (res == null) {
            distinct.set(key, key);
            return true;
        }
        return false;
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
        return callback(eval, FEDERATE, toArray(s, e, m));
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
    
 
    // @before function us:before(?q) {}
    public IDatatype callback(Eval ev, String metadata, IDatatype[] param) {
        if (active || ! accept(metadata)) {
            return null;
        }
        Function function = (Function) eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, param.length);
        if (function != null) {
            // prevent infinite loop in case where there is a query in the function
            active = true;
            IDatatype dt = call(function, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
            active = false;
            return dt;
        }
        return null;
    }
    
    // param = Mappings map
    IDatatype sort(Eval ev, String metadata, IDatatype[] param) {
        if (active || ! accept(metadata)) {
            return null;
        }
        // function us:compare(?m1, ?m2)
        Function function = (Function) eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, 2);
        if (function != null) {
            // prevent infinite loop in case where there is a query in the function
            active = true;
            IDatatype dt = new ListSort("sort").sort((Computer) ev.getEvaluator(), (Binding) ev.getEnvironment().getBind(), ev.getEnvironment(), 
                    ev.getProducer(),  function, param[0] );
            active = false;
            return dt;
        }
        return null;
    }
    
    // @type us:before function us:event () {}
    public IDatatype method(Eval ev, String name, String type, IDatatype[] param) {
        Function exp = (Function) eval.getEvaluator().getDefineMethod(getEnvironment(), name, DatatypeMap.newResource(type), param);
        if (exp != null) {
            return call(exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
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
    

}
