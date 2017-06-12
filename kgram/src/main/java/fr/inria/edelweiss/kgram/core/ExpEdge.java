package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ExpEdge extends Exp {
    
    static final ArrayList<Filter> EMPTY = new ArrayList<>(0);
    

    ExpEdge(int t){
        super(t);
    }
    
    
    /**
     ?s ope cst | cst ope ?s  ope ::= = < <= > >=
      
     fun(?s, cst)   fun ::= contains, strstarts, strends, regex
      
     node ::= subject, predicate, object
     
     getFilter(node, type) getFilters(node, type) 
     getFilter(node)       getFilters(node) 
     getFilters()
    
    ?x p ?y  filters (?y < 12) (?x = <test>)
    getFilter(object) = (?y < 12)

     */
    
    public Filter getFilter(int node) {
        return getFilter(node, ExprType.JOKER);
    }

    public Filter getFilter(int node, int type) {
        List<Filter> list = getFilters(node, type);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    public List<Filter> getFilters(int node) {
        return getFilters(node, ExprType.JOKER);
    }  
    
    /**
     * node: subject|predicate|object
     * type: operator or function or any
     * return filter exp list where 
     * - first arg is variable = node variable
     * - second arg is constant
     * - oper = type
     */
    @Override
    public List<Filter> getFilters(int node, int type){
        Node n = getNode(node);
        if (n == null || ! n.isVariable()){
            return EMPTY;
        }
        ArrayList<Filter> list = new ArrayList<>();
        for (Filter f : getFilters()){            
            if (match(n, f.getExp(), type)){
                list.add(f);                                    
            }
        }
        return list;
    }
    
    boolean match(Node n, Expr e, int type) {
        if (e.match(type)) {
            if (e.type() == ExprType.BOOLEAN) {
                for (Expr ee : e.getExpList()){
                    if (! match(n, ee, type)){
                        return false;
                    }
                }
                return true;
            } else {
                Expr var = e.getExp(0);
                if (var.isVariable() && var.getLabel().equals(n.getLabel())) {
                    if (e.arity() == 1) {
                        return true;
                    } else {
                        Expr cst = e.getExp(1);
                        if (cst.type() == ExprType.CONSTANT) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
   
    
}
