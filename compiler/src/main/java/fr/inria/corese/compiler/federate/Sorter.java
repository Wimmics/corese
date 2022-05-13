package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.HashMap;
import java.util.List;

/**
 * Sort services in BGP until successive services share variables 
 * if possible
 * Find best first bgp with constant triple or with filter
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Sorter {
    
    class Table extends HashMap<Exp, List<Variable>> { }  
    FederateVisitor visitor;
    private SelectorFilter selector;
    
    Sorter(FederateVisitor vis) {
        visitor = vis;
        selector = new SelectorFilter(vis, vis.getAST());
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
        SorterResult res;
        double max = 0;
        int imax = 0;
        int i = 0;
        
        for (Exp exp : bgp) {
            res = newSorterResult();           
            hasConstant(exp, res);
            hasFilter(exp, res);            
            double score = res.score();
            if (exp.isFilter()) {
                //skip
            } 
            else if (score > max) {
                max = score;
                imax = i;
            }
            
            i++;
        }
        
        if (imax != 0) {
            setFirst(bgp, imax);
        }             
    }
    
    SorterResult newSorterResult() {
        return new SorterResult(getSelector());
    }
    
    void setFirst(Exp bgp, int i) {
        Exp ee = bgp.get(i);
        bgp.remove(i);
        bgp.add(0, ee);
    }
    
    boolean hasConstant(Exp exp) {
        SorterResult r = newSorterResult();
        hasConstant(exp, r);
        return r.getConstantTriple()!=null;
    }

    // focus on triple with constant
    void hasConstant(Exp exp, SorterResult res) {
        if (exp.isTriple()) { 
            res.submit(exp.getTriple());
        } else if (exp.isBGP()) {
            for (Exp ee : exp.getBody()) {
                hasConstant(ee, res);
            }
        } else if (exp.isService() || exp.isGraph()) {
            hasConstant(exp.getBodyExp(), res);
        } else if (exp.isUnion()) {
            SorterResult r1 = newSorterResult();
            SorterResult r2 = newSorterResult();
            hasConstant(exp.get(0), r1);
            hasConstant(exp.get(1), r2);
            res.union(r1, r2);
        } else if (exp.isOptional() || exp.isMinus()) {
            hasConstant(exp.get(0), res);
        }
        else if (exp.isQuery()) {
            hasConstant(exp.getAST().getBody(), res);
        }
    }
    
    boolean hasFilter(Exp exp) {
        return hasFilter(exp, newSorterResult());
    }
    
    boolean hasFilter(Exp exp, SorterResult res) {
        if (exp.isFilter()) {
            res.submit(exp.getFilter());
        }
        else if (exp.isValues() && exp.getValuesExp().isDefined()) {
            res.incrFilter();
        }
        else if (exp.isBGP()) {
            for (Exp ee : exp.getBody()) {
                hasFilter(ee, res);
            }
        } else if (exp.isService() || exp.isGraph()) {
            hasFilter(exp.getBodyExp(), res);
        } else if (exp.isUnion()) {
            if (hasFilter(exp.get(0)) && (hasFilter(exp.get(1)) || hasConstant(exp.get(1)))) {
                res.incrFilter();
            }
            else if (hasConstant(exp.get(0)) && hasFilter(exp.get(1))) {
                res.incrFilter();
            }
        } else if (exp.isOptional() || exp.isMinus()) {
            hasFilter(exp.get(0), res);
        }
        else if (exp.isQuery()) {
            hasFilter(exp.getAST().getBody(), res);
        }
        return res.nbFilter()>0;
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
            else if (exp.isBGP() || exp.isBinaryExp()|| exp.isGraph()) {
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

    public SelectorFilter getSelector() {
        return selector;
    }

    public void setSelector(SelectorFilter selector) {
        this.selector = selector;
    }

}
