package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.List;

/**
 *
 * @author corby
 */
public class QuerySolverVisitorRuleUser extends QuerySolverVisitorRule {
    
    public QuerySolverVisitorRuleUser() {}
    
    public QuerySolverVisitorRuleUser(RuleEngine t, Eval e) { super(t, e); }
    
     @Override
    public IDatatype init() {
        System.out.println("User defined Rule Visitor");
        setEntailment(true);
        return DatatypeMap.TRUE;
    }
    
    @Override
    public DatatypeValue entailment(Query rule, List<Edge> construct, List<Edge> where) { 
        System.out.println("construct: " + construct);
        System.out.println("where:     " + where);
        System.out.println();
        return DatatypeMap.TRUE;
    }



}
    
