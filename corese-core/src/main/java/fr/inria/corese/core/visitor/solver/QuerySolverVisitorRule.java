package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2020
 */
public class QuerySolverVisitorRule extends QuerySolverVisitorBasic {
    
    RuleEngine re;

    public QuerySolverVisitorRule(Eval e) {
        super(e);
    }
    
    public QuerySolverVisitorRule(RuleEngine re, Eval e) {
        super(e);
        this.re = re;
    }

    @Override
    public IDatatype beforeEntailment(DatatypeValue path) {
        IDatatype dt = callback(getEval(), BEFORE_ENTAIL, toArray(re, path));
        return dt;
    }
    
    @Override
    public IDatatype afterEntailment(DatatypeValue path) {
        return callback(getEval(), AFTER_ENTAIL, toArray(re, path));
    }
    
    @Override
    public IDatatype beforeRule(Query q) {
        IDatatype dt = callback(getEval(), BEFORE_RULE, toArray(re, q));
        return dt;
    }

    // res: Mappings or List<Edge>
    @Override
    public IDatatype afterRule(Query q, Object res) {
        return callback(getEval(), AFTER_RULE, toArray(re, q, res));
    }

}
