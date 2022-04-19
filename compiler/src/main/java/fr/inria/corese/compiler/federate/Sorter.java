package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.NSManager;
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
    
    class Result {
        private Triple triple;
        private boolean find = false;

        public Triple getTriple() {
            return triple;
        }

        public void setTriple(Triple triple) {
            this.triple = triple;
        }
        
        void addTriple(Triple t) {
            if (getTriple() == null) {
                setTriple(t);
                setFind(true);
            }
            else if (prefer(t, getTriple())){
                // prefer local predicate vs system predicate
                setTriple(t);
                setFind(true);
            }
        }
        
        boolean prefer(Triple t1, Triple t2) {
            return ! isSystem(t1) && isSystem(t2);
        }
        
        boolean isSystem(Triple t) { 
            return NSManager.nsm().isSystemURI(
                    t.getPredicate().getLabel());
        }

        public boolean isFind() {
            return find;
        }

        public void setFind(boolean find) {
            this.find = find;
        }
    }
     
    
    void process(Exp bgp) {
        first(bgp);
        sort(bgp);
    }
    
    /**
     * Pick the first suitable exp in bgp
     * exp with a constant term if any.
     */
    void first(Exp bgp) {
        int j = -1;
        int p = -1;
        Result res = new Result();

        for (int i = 0; i<bgp.size(); i++) {
            Exp exp = bgp.get(i);
            res.setFind(false);
            if (hasConstant(exp, res)) {
                if (i > 0) {
                    setFirst(bgp, i);
                }
                return;
            }
            else {
                if (j == -1 && hasFilter(exp)) {
                    j = i;
                }
                if (res.isFind()) {
                    // triple with constant predicate
                    p = i;
                }
            }
        }
        
        if (j > 0) {
            // ee has a filter: put it first
            setFirst(bgp, j);
        } 
        else if (p > 0) {
            // triple with cst predicate
            setFirst(bgp, p);
        }
    }
    
    void setFirst(Exp bgp, int i) {
        Exp ee = bgp.get(i);
        bgp.remove(i);
        bgp.add(0, ee);
    }
    
    boolean hasConstant(Exp exp) {
        return hasConstant(exp, new Result());
    }


    boolean hasConstant(Exp exp, Result res) {
        if (exp.isFilter()) {
        } // nothing yet
        else if (exp.isTriple()) { 
            if (exp.getTriple().isConstantNode()) {
                return true;
            }
            if (exp.getTriple().getPredicate().isConstant()) {
                res.addTriple(exp.getTriple());
            }
        } else if (exp.isBGP()) {
            for (Exp ee : exp.getBody()) {
                if (hasConstant(ee, res)) {
                    return true;
                }
            }
        } else if (exp.isService() || exp.isGraph()) {
            if (hasConstant(exp.getBodyExp(), res)) {
                return true;
            }
        } else if (exp.isUnion()) {
            if (hasConstant(exp.get(0), res) && hasConstant(exp.get(1), res)) {
                return true;
            }
        } else if (exp.isOptional() || exp.isMinus()) {
            if (hasConstant(exp.get(0), res)) {
                return true;
            }
        }
        else if (exp.isQuery()) {
            if (hasConstant(exp.getAST().getBody(), res)) {
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
            if (hasFilter(exp.getAST().getBody())) {
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
