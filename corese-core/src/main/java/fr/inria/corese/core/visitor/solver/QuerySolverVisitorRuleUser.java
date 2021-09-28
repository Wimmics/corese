package fr.inria.corese.core.visitor.solver;

import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author corby
 */
public class QuerySolverVisitorRuleUser extends QuerySolverVisitorRule {
    
    int count = 0;
    HashMap<Query, Integer> mapInference;
    
    public QuerySolverVisitorRuleUser() {
        setup();
    }
    
    void setup() {
        mapInference = new HashMap<>();
    }
    
    public QuerySolverVisitorRuleUser(RuleEngine t, Eval e) { 
        super(t, e); 
        setup();
    }
    
    @Override
    public IDatatype init() {
        System.out.println("User defined Rule Visitor");
        mapInference.clear();
        setEntailment(true);
        count = 0;
        return DatatypeMap.TRUE;
    }
    
    Integer incr(HashMap<Query, Integer> map, Query q) {
        Integer nb = map.get(q);
        if (nb == null) {
            nb = 0;
        }
        nb+=1;
        map.put(q, nb);
        return nb;
    }
    
    @Override
    public IDatatype beforeRule(Query q) {
        q.setDetail(true);
        return DatatypeMap.TRUE;
    }
    
    @Override
    public boolean entailment() {
        return true;
    }
    
    @Override
    public IDatatype entailment(Query q, List<Edge> construct, List<Edge> where) { 
        incr(mapInference, q);
        System.out.println("rule: " + q.getID());
        System.out.println("entailment: " + mapInference.get(q));
        
        System.out.println("construct: " + construct);
        System.out.println("where:     " + where);
        System.out.println();
        return DatatypeMap.TRUE;
    }



}
    
