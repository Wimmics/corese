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
    private String name;
    private Object pack;

    public Extension() {
        map = new HashMap();
    }
    
    public Extension(String n){
        this();
        name = n;
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
        if (getName() != null){
            sb.append("extension: ");
            sb.append(getName()); 
            sb.append(NL);
        }
        for (Expr exp : map.values()) {
            sb.append(exp);
            sb.append(NL);
            sb.append(NL);
        }
        return sb.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the pack
     */
    public Object getPackage() {
        return pack;
    }

    /**
     * @param pack the pack to set
     */
    public void setPackage(Object pack) {
        this.pack = pack;
    }
}
