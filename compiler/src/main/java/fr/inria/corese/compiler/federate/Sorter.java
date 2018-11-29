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
        if (sortable(bgp)) {
            first(bgp);
            sort(bgp);
        }
    }
    
    /**
     * Pick the first suitable exp in bgp
     * exp with a constant term if any.
     */
    void first(Exp bgp) {
        for (int i = 0; i<bgp.size(); i++) {
            Exp exp = bgp.get(i);
            if (hasConstant(exp)) {
                if (i > 0) {
                    bgp.remove(i);
                    bgp.add(0, exp);
                }
                break;
            }
        }
    }
    
    boolean hasConstant(Exp bgp) {
        for (Exp exp : bgp.getBodyExp()) {
            if (exp.isFilter()) {} // nothing yet
            else if (exp.isTriple()) {
                Triple t = exp.getTriple();
                if (t.getSubject().isConstant() || t.getObject().isConstant()) {
                    return true;
                }
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
            if (! exp.isService()) {
                return false ;
            }
        }
        return true;
    }

}
