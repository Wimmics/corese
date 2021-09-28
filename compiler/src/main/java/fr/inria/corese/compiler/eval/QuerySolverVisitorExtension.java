package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.List;

/**
 * Draf example of specific Visitor, e.g. implemented in Java instead of LDScript
 * Use case: 
 * for one query: exec.query(q, new QuerySolverVisitorExtension())
 * for one query: exec.query(q, Mapping.create(vis));
 * 
 * for all query: QueryProcess.setSolverVisitorName("fr.inria.corese.compiler.eval.QuerySolverVisitorExtension");
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorExtension extends QuerySolverVisitor {
    
    public QuerySolverVisitorExtension() {}
  
    public QuerySolverVisitorExtension(Eval ev) {
        super(ev);
    }
    
    @Override
    public IDatatype update(Query q, List<Edge> delete, List<Edge> insert) {
        System.out.println("My Visitor update");
        return super.update(q, delete, insert);
    }
    
    @Override
    public IDatatype insert(IDatatype path, Edge triple) {
        System.out.println("My Visitor insert: " + triple);
        return  path;
        //return super.insert(path, triple);
    }
}
