package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
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
    public IDatatype produce(Eval eval, Edge edge) {       
        IDatatype dt = callback(eval, Metadata.META_PRODUCE, toArray(edge));
        return dt;
    }
    
    @Override
    public boolean produce() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_PRODUCE, 1);
        return (exp != null);
    }
    
    @Override
    public IDatatype result(Eval eval, Mapping m) {       
        return callback(eval, Metadata.META_RESULT, toArray(m));
    }
    
    @Override
    public boolean result() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_RESULT, 1);
        return (exp != null);
    }
    
    @Override
    public IDatatype statement(Eval eval, Exp e) {       
        return callback(eval, Metadata.META_STATEMENT, toArray(e));
    }
    
    @Override
    public boolean statement() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_STATEMENT, 1);
        return (exp != null);
    }
    
    @Override
    public IDatatype candidate(Eval eval, Edge q, Edge e) {       
        return callback(eval, Metadata.META_CANDIDATE, toArray(q, e));
    }
    
    @Override
    public boolean candidate() {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), Metadata.META_CANDIDATE, 2);
        return (exp != null);
    }
    
    @Override
    public IDatatype optional(Eval eval, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_OPTIONAL, toArray(e, m1, m2));
    }
    
    @Override
    public IDatatype minus(Eval eval, Exp e, Mappings m1, Mappings m2) {       
        return callback(eval, Metadata.META_MINUS, toArray(e, m1, m2));
    }
    
     @Override
    public IDatatype service(Eval eval, Exp e, Mappings m) {       
        return callback(eval, Metadata.META_FEDERATE, toArray(e, m));
    }
    
    @Override
    public boolean filter(Eval eval, Expr e, boolean b) {       
        IDatatype dt = callback(eval, Metadata.META_FILTER, toArray(e, DatatypeMap.newInstance(b)));
        if (dt == null) {
            return b;
        }
        return dt.booleanValue();
    }
 
    // @before function us:before(?q) {}
    public IDatatype callback(Eval ev, String metadata, IDatatype[] param) {
        Expr exp = eval.getEvaluator().getDefineMetadata(getEnvironment(), metadata, param.length);
        if (exp != null) {
            return call((Function) exp, param, ev.getEvaluator(), ev.getEnvironment(), ev.getProducer());
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
            param[i++] = DatatypeMap.createObject(obj);
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
