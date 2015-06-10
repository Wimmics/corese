package fr.inria.edelweiss.kgram.filter;

import fr.inria.edelweiss.kgram.api.core.Expr;
import java.util.Collection;
import java.util.HashMap;

/**
 * Manage extension functions Expr exp must have executed exp.local() do tag
 * local variables
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {

    static final String NL = System.getProperty("line.separator");
    HashMap<String, Expr> map;

    public Extension() {
        map = new HashMap();
    }

    /**
     *
     * exp: st:fac(?x) = if (?x = 1, 1, ?x * st:fac(?x - 1))
     */
    public void define(Expr exp) {
        Expr fun = exp.getExp(0);
        map.put(fun.getLabel(), exp);
    }
    
    public Collection<Expr> values(){
        return map.values();
    }
    
    public void add(Extension ext){
        for (Expr e : ext.values()){
            if (! isDefined(e.getExp(0))){
                define(e);
            }
        }
    }

    public boolean isDefined(Expr exp) {
        return map.containsKey(exp.getLabel());
    }

    /**
     * exp: st:fac(?n) values: #[10] actual values of parameters return body of
     * fun
     */
    public Expr get(Expr exp, Object[] values) {
        return map.get(exp.getLabel());
    }

    public Expr get(Expr exp) {
        return map.get(exp.getLabel());
    }
    
    public Expr get(String label) {
        return map.get(label);
    }
    

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expr exp : map.values()) {
            sb.append(exp);
            sb.append(NL);
            sb.append(NL);
        }
        return sb.toString();
    }
}
