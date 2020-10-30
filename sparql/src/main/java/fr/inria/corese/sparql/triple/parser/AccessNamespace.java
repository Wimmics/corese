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
    
    NSMap accept, reject, forbid;
    
    class NSMap extends HashMap<String, Boolean> {

        // empty map return false
        boolean match(String ns) {
            for (String name : keySet()) {
                if (ns.contains(name)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    static AccessNamespace singleton;
    
    static {
        singleton = new AccessNamespace();
        singleton.start();
        singleton.init();
    }
    
    void start() {
        singleton().forbid("/etc/passwd");
        singleton().forbid(".ssh/id_rsa");
    }
    
    void init() {
        for (String ns : forbid.keySet()) {
            singleton().setAccess(ns, false);
        }
    }
    
    public static void clean() {
         singleton().clear();
         singleton().init();      
    }
    
    public static AccessNamespace singleton() {
        return singleton;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (! singleton().accept.isEmpty()) {
            sb.append("accept:").append(NSManager.NL).append(singleton().accept.toString());
        }
        if (! singleton().reject.isEmpty()) {
            sb.append("reject:").append(NSManager.NL).append(singleton().reject.toString());
        }
        return sb.toString();
    }
    
    AccessNamespace() {
        accept = new NSMap();
        reject = new NSMap();
        forbid = new NSMap();
    }
    
    public static void define(String ns, boolean b) {
        singleton().setAccess(ns, b);
    }
    
    // use case: forbidden even to super user 
    public static boolean forbidden(String ns) {
        return singleton().forbid.match(ns);
    }
    
    public static boolean access(String ns) {
        return singleton().accept(ns);
    }
    public static boolean access(String ns, boolean resultWhenEmptyAccept) {
        return singleton().accept(ns, resultWhenEmptyAccept);
    }
    
    void forbid(String ns) {
        forbid.put(ns, false);
    }
    
    public void setAccess(String ns, boolean b) {
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
        return accept(ns, false);
    }
    
    
    public boolean accept(String ns, boolean resultWhenEmptyAccept) {
        if (reject.match(ns)) {
            return false;
        }
        if (accept.isEmpty()) {
            return resultWhenEmptyAccept;
        }
        if (accept.match(ns)) {
            return true;
        }
        return false;
    } 
    
    
    
}
