package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.script.Funcall;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;

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
public class QuerySolverVisitor implements ProcessVisitor {
    private static final String EVENT_METHOD = NSManager.USER+"event";
    boolean active = false;
    
    Eval eval;

    QuerySolverVisitor(Eval e) {
        eval = e;
    }

    @Override
    public void setProcessor(Eval e) {
        eval = e;
    }

    @Override
    public IDatatype before(Query q) {
        return callback(eval, Metadata.META_BEFORE, toArray(q));
    }

    @Override
    public IDatatype after(Mappings map) {
        return callback(eval, Metadata.META_AFTER, toArray(map));
    }
    
    @Override
    public boolean produce() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_PRODUCE, 2);
        return (exp != null);
    }
          
    @Override
    public IDatatype produce(Eval eval, Node g, Edge q) {       
        IDatatype dt = callback(eval, Metadata.META_PRODUCE, toArray(g, q));
        return dt;
    }
    
    @Override
    public boolean candidate() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_CANDIDATE, 3);
        return (exp != null);
    }
   
    @Override
    public IDatatype candidate(Eval eval, Node  g, Edge q, Edge e) {       
        return callback(eval, Metadata.META_CANDIDATE, toArray(g, q, e));
    }
        
    @Override
    public boolean result(Eval eval, Mappings map, Mapping m) {       
        return result(callback(eval, Metadata.META_RESULT, toArray(map, m)));
        
    }
    
    boolean result(IDatatype dt) {
        if (dt == null) {
            return true;
        }
        return dt.booleanValue();
    }
    
    @Override
    public IDatatype statement(Eval eval, Node g, Exp e) { 
        return callback(eval, Metadata.META_STATEMENT, toArray(g, e));
    }
    
    @Override
    public boolean statement() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_STATEMENT, 2);
        return (exp != null);
    }
    
 
    @Override
    public IDatatype path(Eval eval, Node g, Edge q, Path p, Node s, Node o) {       
        return callback(eval, Metadata.META_PATH, toArray(g, q, p, s, o));
    }
    
    @Override
    public boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) {  
         return result(callback(eval, Metadata.META_STEP, toArray(g, q, p, s, o)));         
    }
       
    @Override
    public IDatatype values(Eval eval, Node g, Exp e, Mappings m) { 
        return callback(eval, Metadata.META_VALUES, toArray(g, e, m));    
    }  
    
    @Override
    public IDatatype bind(Eval eval, Node g, Exp e, DatatypeValue dt) { 
        return callback(eval, Metadata.META_BIND, toArray(g, e, dt));    
    } 
    
    @Override
    public IDatatype bgp(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, Metadata.META_BGP, toArray(g, e, m));
    }
    
    @Override
    public IDatatype join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_JOIN, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_OPTIONAL, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_MINUS, toArray(g, e, m1, m2));
    }
    
    @Override
    public IDatatype union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_UNION, toArray(g, e, m1, m2));
    }
    
     @Override
    public IDatatype graph(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, Metadata.META_GRAPH, toArray(g, e, m));
    }
    
    
    @Override
    public IDatatype service(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, Metadata.META_FEDERATE, toArray(g, e, m));
    }
    
    @Override
    public IDatatype query(Eval eval, Node g, Exp e, Mappings m) {       
        return callback(eval, Metadata.META_QUERY, toArray(g, e, m));
    }
    
    @Override
    public boolean filter() {      
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_FILTER, 3);
        return (exp != null);
    } 
    
    @Override
    public boolean filter(Eval eval, Node g, Expr e, boolean b) {       
        IDatatype dt = callback(eval, Metadata.META_FILTER, toArray(g, e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public boolean having(Eval eval, Expr e, boolean b) {       
        IDatatype dt = callback(eval, Metadata.META_HAVING, toArray(e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
    
    @Override
    public DatatypeValue select(Eval eval, Expr e, DatatypeValue dt) {       
        IDatatype val = callback(eval, Metadata.META_SELECT, toArray(e, dt));
        return dt;
    }
    
    @Override
    public DatatypeValue aggregate(Eval eval, Expr e, DatatypeValue dt) {       
        IDatatype val = callback(eval, Metadata.META_AGGREGATE, toArray(e, dt));
        return dt;
    }
 
    // @before function us:before(?q) {}
    public IDatatype callback(Eval ev, String metadata, IDatatype[] param) {
        if (active) {
            return null;
        }
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, param.length);
        if (exp != null) {
            // prevent infinite loop in case where there is a query in the function
            active = true;
            IDatatype dt = call((Function) exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
            active = false;
            return dt;
        }
        return null;
    }
    
    // @type us:before function us:event () {}
    public IDatatype method(Eval ev, String name, String type, IDatatype[] param) {
        Expr exp = eval.getEvaluator().getDefineMethod(getEnvironment(), name, DatatypeMap.newResource(type), param);
        if (exp != null) {
            return call((Function) exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
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

    Query getQuery() {
        return getEnvironment().getQuery();
    }

    ASTQuery getAST() {
        return (ASTQuery) getQuery().getAST();
    }

}
