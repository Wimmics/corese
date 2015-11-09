package fr.inria.edelweiss.kgenv.parser;

import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Detect Query Pattern, that may be optimized
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class QueryProfile {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
    Query query;
    
    QueryProfile(Query q){
        query = q;
    }
    
    void profile(){
        count(query);
    }
    
    // select (count(*) as ?c) where {}
    void count(Query q){
        if (q.getSelectFun().size() == 1
                && q.getGroupBy().isEmpty()
                && q.getLimit() == Integer.MAX_VALUE
                && ! q.isTemplate()){
            Exp exp = q.getSelectFun().get(0);
            Filter f = exp.getFilter();
            if (f != null 
                    && f.getExp().oper() == ExprType.COUNT
                    && ! f.getExp().isDistinct()
                    && f.getExp().getExpList().isEmpty()){
                q.setQueryProfile(Query.COUNT_PROFILE);
            }
        }
    }

}
