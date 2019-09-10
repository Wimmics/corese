package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.HashMap;
import java.util.List;

/**
 * Sort services in BGP until successive services share variables 
 * if possible
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Sorter {
    
    class Table extends HashMap<Exp, List<Variable>> { }
 
    
    void process(Exp bgp) {
        //if (sortable(bgp)) {
            first(bgp);
            sort(bgp);
        //}
    }
    
    /**
     * Pick the first suitable exp in bgp
     * exp with a constant term if any.
     */
    void first(Exp bgp) {
        int j = -1;
        
        for (int i = 0; i<bgp.size(); i++) {
            Exp exp = bgp.get(i);
            if (hasConstant(exp)) {
                if (i > 0) {
                    setFirst(bgp, i);
                }
                return;
            }
            else if (j == -1 && hasFilter(exp)) {
                j = i;
            }
        }
        if (j > 0) {
            // ee has a filter: put it first
            setFirst(bgp, j);
        }
        
    }
    
    void setFirst(Exp bgp, int i) {
        Exp ee = bgp.get(i);
        bgp.remove(i);
        bgp.add(0, ee);
    }
    
    boolean hasConstant(Exp exp) {
        if (exp.isFilter()) {
        } // nothing yet
        else if (exp.isTriple() && exp.getTriple().isConstantNode()) {
            return true;
        } else if (exp.isBGP()) {
            for (Exp ee : exp.getBody()) {
                if (hasConstant(ee)) {
                    return true;
                }
            }
        } else if (exp.isService() || exp.isGraph()) {
            if (hasConstant(exp.getBodyExp())) {
                return true;
            }
        } else if (exp.isUnion()) {
            if (hasConstant(exp.get(0)) && hasConstant(exp.get(1))) {
                return true;
            }
        } else if (exp.isOptional() || exp.isMinus()) {
            if (hasConstant(exp.get(0))) {
                return true;
            }
        }
        else if (exp.isQuery()) {
            if (hasConstant(exp.getQuery().getBody())) {
                return true;
            }
        }
        return false;
    }
    
    boolean hasFilter(Exp exp) {
        if (exp.isFilter() || exp.isValues()) {
            return true;
        } 
        else if (exp.isBGP()) {
            for (Exp ee : exp.getBody()) {
                if (hasFilter(ee)) {
                    return true;
                }
            }
        } else if (exp.isService() || exp.isGraph()) {
            if (hasFilter(exp.getBodyExp())) {
                return true;
            }
        } else if (exp.isUnion()) {
            if (hasFilter(exp.get(0)) && (hasFilter(exp.get(1)) || hasConstant(exp.get(1)))) {
                return true;
            }
            else if (hasConstant(exp.get(0)) && hasFilter(exp.get(1))) {
                return true;
            }
        } else if (exp.isOptional() || exp.isMinus()) {
            if (hasFilter(exp.get(0))) {
                return true;
            }
        }
        else if (exp.isQuery()) {
            if (hasFilter(exp.getQuery().getBody())) {
                return true;
            }
        }
        return false;
    }
    
   /**
    * Order bgp patterns in such a way that two successive patterns share variables
    * if possible
    * size > 2.
    */
    void sort(Exp bgp) {
        if (bgp.size() <= 2) {
            return;
        }
        Table table = new Table();
        
        for (Exp exp : bgp) {
            List<Variable> list = exp.getVariables();
            table.put(exp, list);
        }
        
        List<Variable> l1 = table.get(bgp.get(0));
        
        // find e2, in the rest of the list, that share variables
        // with previous patterns. If e2 is not just after e1, move e2 just after e1
        for (int i = 0; i < bgp.size(); i++) {
            Exp e1 = bgp.get(i);
            
            for (int j = i+1; j < bgp.size(); j++) {
                Exp e2 = bgp.get(j);
                List<Variable> l2 = table.get(e2);
                
                if (connected(l1, l2)) { 
                    // include variables of e2 into current list of variables l1
                    include(l1, l2);
                    if (j > i+1) {
                        bgp.remove(j);
                        bgp.add(i+1, e2);                       
                    }
                    break;
                }
            }
        }
    }
    
    boolean connected(List<Variable> l1, List<Variable> l2) {
        for (Variable var : l1) {
            if (l2.contains(var)) {
                return true;
            }
        }
        return false;
    }
    
    void include(List<Variable> l1, List<Variable> l2) {
        for (Variable var : l2) {
            if (! l1.contains(var)) {
                l1.add(var);
            }
        }
    }
    
    
    boolean sortable(Exp bgp) {
        for (Exp exp : bgp) {
            if (exp.isService()) {
                // ok
            }
            else if (exp.isBGP() || exp.isUnion() || exp.isOptional() || exp.isMinus() || exp.isGraph()) {
                if (sortable(exp)) {
                    // ok
                }
                else {
                    return false;
                }
            }
            else {
                return false ;
            }
        }
        return true;
    }

}
