package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
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
     * - oper = type or oper = boolean connector on such subexp
     */
    @Override
    public List<Filter> getFilters(int node, int type){
        Node n = getNode(node);
        if (n == null || ! n.isVariable()){
            return EMPTY;
        }
        ArrayList<Filter> list = new ArrayList<>();
        for (Filter f : getFilters()){            
            if (match(f.getExp(), n, node, type)){
                list.add(f);                                    
            }
        }
        return list;
    }
    
    /**
     * If e is boolean connector, check subexp recursively
     * Otherwise check:
     * e match type
     * first arg is variable n
     * second arg (if any) is constant
     * type may be a query type such as TINKERPOP that match a set of oper
     */
    boolean match(Expr e, Node n, int node, int type) {
        if (e.type() == ExprType.BOOLEAN) {
            for (Expr ee : e.getExpList()) {
                if (!match(ee, n, node, type)) {
                    return false;
                }
            }
            return true;
        } else if (e.match(type) && match(e, n, node)) {
            if (e.arity() == 1) {
                return true;
            } else {
                Expr cst = e.getExp(1);
                if (cst.isConstant()) {
                    if (e.arity() == 3){
                        // regex
                        return e.getExp(2).isConstant();
                    }
                    return true;
                } else if (e.oper() == ExprType.IN) {
                    for (Expr ee : cst.getExpList()) {
                        if (!ee.isConstant()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * lang, datatype not with predicate
     */
    boolean compatible(Expr e, int node){
        if (node == PREDICATE){
            if (e.match(ExprType.LANG) || e.match(ExprType.DATATYPE)){
                return false;
            }             
        }
        return true;
    }
    
    /**
     * exp is 
     * var = cst
     * datatype(var) = cst
     * lang(var) = cst
     */
    boolean match(Expr exp, Node n, int node) {
        if (exp.arity() > 0) {
            Expr fst = exp.getExp(0);
            if (fst.isVariable()){
                return fst.getLabel().equals(n.getLabel());
            }
            else if (compatible(fst, node) && (fst.match(ExprType.DATATYPE) || fst.match(ExprType.LANG)) && fst.arity() == 1) {
                // datatype(var) == cst
                Expr var = fst.getExp(0);
                return var.isVariable() && var.getLabel().equals(n.getLabel());
            }
        }
        return false;
    }

}
