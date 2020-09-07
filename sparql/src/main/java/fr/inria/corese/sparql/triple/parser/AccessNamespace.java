package fr.inria.corese.sparql.triple.parser;

import java.util.HashMap;

/**
 * Accept/reject namespace for @import and LinkedFunction
 * define(ns, true|false)
 * if there are accept ns, other ns are rejected
 * if there is only reject ns, other ns are accepted
 * 
 * @author Olivier Corby, INRIA, I3S 2020
 */
public class AccessNamespace {
    
    NSMap accept, reject;
    
    class NSMap extends HashMap<String, Boolean> {

        // empty map return false
        boolean match(String ns) {
            for (String name : keySet()) {
                if (ns.startsWith(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    static AccessNamespace singleton;
    
    static {
        singleton = new AccessNamespace();
    }
    
    public static AccessNamespace singleton() {
        return singleton;
    }
    
    AccessNamespace() {
        accept = new NSMap();
        reject = new NSMap();
    }
    
    public static void define(String ns, boolean b) {
        singleton().access(ns, b);
    }
    
    public static boolean access(String ns) {
        return singleton().accept(ns);
    }
    
    public static void clean() {
         singleton().clear();
    }
    
    public void access(String ns, boolean b) {
        if (b) {
            accept.put(ns, b);
            reject.remove(ns);
        }
        else {
            reject.put(ns, b);
            accept.remove(ns);
        }
    }
    
    public void clear() {
        accept.clear();
        reject.clear();
    }
    
    public boolean accept(String ns) {
        if (reject.match(ns)) {
            return false;
        }
        if (accept.isEmpty() || accept.match(ns)) {
            return true;
        }
        return false;
    } 
    
    
    
}
