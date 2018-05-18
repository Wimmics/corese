package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;

/**
 * Callback manager for functions with specific annotations
 * 
 * @before function us:before() {}
 * @after  function us:after(?m) {}
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class QuerySolverVisitor implements ProcessVisitor {

    Eval eval;
    QuerySolver solver;
    
    QuerySolverVisitor(QuerySolver s, Eval e) {
        eval = e;
        solver = s;
    }
    
    @Override
    public void setProcessor(Eval e) {
        eval = e;
    }
    
     @Override
    public void before() {
        callback(Metadata.BEFORE, new IDatatype[0]);
    }
    
     @Override
    public void after(Mappings map) {       
        callback(Metadata.AFTER, toArray(map)); 
    }
     
    public void callback(int name, IDatatype[] param) {
        Expr exp = eval.getEvaluator().getDefine(getEnvironment(), name, param.length);
        if (exp != null) {
            solver.funcall(exp.getFunction().getLabel(), param, eval);
        }
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
