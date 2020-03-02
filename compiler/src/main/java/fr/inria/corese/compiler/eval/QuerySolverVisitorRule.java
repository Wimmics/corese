package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author corby
 */
public class QuerySolverVisitorRule extends QuerySolverVisitorBasic {

    public QuerySolverVisitorRule(Eval e) {
        super(e);
    }

    @Override
    public IDatatype beforeEntailment(DatatypeValue path) {
        IDatatype dt = callback(eval, BEFORE_ENTAIL, toArray(path));
        return dt;
    }

    @Override
    public IDatatype afterEntailment(DatatypeValue path) {
        return callback(eval, AFTER_ENTAIL, toArray(path));
    }
    
    @Override
    public IDatatype beforeRule(Query q) {
        IDatatype dt = callback(eval, BEFORE_RULE, toArray(q));
        return dt;
    }

    // res: Mappings or List<Edge>
    @Override
    public IDatatype afterRule(Query q, Object res) {
        return callback(eval, AFTER_RULE, toArray(q, res));
    }

}
