package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.HashMap;

/**
 * SPARQL Statements implemented as SPARQL Algebra on Mappings
 * Alternative interpreter not used
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EvalSPARQL {
    
    Eval eval;
    Query query;
    private boolean debug = false;
    
    EvalSPARQL(Query q, Eval e){
        eval = e;
        query = q;
        debug = q.isDebug();
    }
    
    Mappings eval(Node graph,  Producer p, Exp exp){
        return eval(graph, p, exp, null);
    }
    
    Mappings eval(Node graph,  Producer p, Exp exp, Mapping m){
        Mappings map;
        if (debug){
            System.out.println("SPARQL: " + exp);
            System.out.println(m);
            System.out.println();
        }

        switch (exp.type()){
            
            case Exp.BGP:       map = bgp(graph, p, exp, m); break;
                
            case Exp.JOIN:      map = join(graph, p, exp, m); break;    
                
            case Exp.UNION:     map = union(graph, p, exp, m); break;
                
            case Exp.MINUS:     map = minus(graph, p, exp, m); break;
                
            case Exp.OPTIONAL:  map = optional(graph, p, exp, m); break;
                
            case Exp.GRAPH:     map = graph(graph, p, exp, m); break;
                
            default: map = Mappings.create(query);
                                               
        }
        
        if (debug){
            System.out.println("Result: " + map.size());
            System.out.println(map);
        }
        
        return map;
    }
    

    
    Mappings join(Node graph, Producer p, Exp exp, Mapping m) {
        Mappings m1 = eval(graph, p, exp.first(), m);
        if (exp.rest().isBGPFilter()) {
            return filter(p, exp.rest(), m1);
        } else {
            Mappings m2 = eval(graph, p, exp.rest(), m);
            return join(m1, m2);
        }
    }
    
     Mappings join(Mappings map1, Mappings map2){
        if (map1.size() == 0){
            return map1;
        }
        if (map2.size() == 0){
            return map2;
        }
        
        Mapping m1 = map1.get(0);
        Mapping m2 = map2.get(0);
        
        // common variable
        Node cmn = m1.getCommonNode(m2);
               
        if (cmn == null){
            return map1.joiner(map2);
        }
        
        // sort map2 according to common variable, null value first
        map2.sort(eval, cmn);
        return map1.joiner(map2, cmn); 
    }
     
     

    
    Mappings graph(Node graph,  Producer p, Exp exp, Mapping m){
        return bgp(exp.getGraphName(), p, exp.rest(), m);        
    }
    
    Mappings optional(Node graph,  Producer p, Exp exp, Mapping mm){
         Mappings m1 = bgp(graph, p, exp.first(), mm);
         Mappings m2 = bgp(graph, p, exp.rest());
         Mappings res = Mappings.create(query);
         HashMap hm = new HashMap();
         
         for (Mapping ma : m1){
             int nbsuc = 0;
             for (Mapping mb : m2){
                 boolean success = false;
                 Mapping m = ma.merge(mb);
                 if (m != null){
                     success = true;
                     if (exp.isPostpone()){
                         m.setQuery(query);
                         m.setMap(hm);
                         hm.clear();
                         if (! postpone(graph, exp, m, p)){
                             success = false;
                         }
                     }
                     if (success){
                         res.add(m);
                         nbsuc++;
                     }
                 }
             }
             
             if (nbsuc == 0){
                 res.add(ma);
             }
         }
         return res;
    }
    
    boolean postpone(Node gNode, Exp exp, Mapping m, Producer p){
        for (Exp e : exp.getPostpone()){
            try {
                if (! eval.test(gNode, e.getFilter(), m, p)){
                    return false;
                }
            } catch (SparqlException ex) {
                    return false;
            }
        }
        return true;
    }
    
    
    Mappings optional2(Node graph,  Producer p, Exp exp, Mapping mm){
         Mappings m1 = bgp(graph, p, exp.first(), mm);
         Mappings res = Mappings.create(query);
         for (Mapping m : m1){
             Mappings m2 = bgp(graph, p, exp.rest(), m);
             if (m2.size() == 0){
                 res.add(m);
             }
             else {
                res.add(m2); 
             }
         }
         return res;
    }
    
    Mappings minus(Node graph,  Producer p, Exp exp, Mapping mm){
        Mappings m1 = bgp(graph, p, exp.first(), mm);
        Mappings m2 = bgp(graph, p, exp.rest());
        Mappings res = Mappings.create(m1.getQuery());
        
        for (Mapping m : m1) {
            boolean ok = true;
            
            for (Mapping minus : m2) {                   
                if (m.compatible(minus)) {
                    ok = false;
                    break;
                }
            }
            
            if (ok) {
                res.add(m);
            }
        }
        return res;
    }
    
    
    Mappings union(Node graph,  Producer p, Exp exp, Mapping m){
        Mappings m1 = bgp(graph, p, exp.first(), m);
        Mappings m2 = bgp(graph, p, exp.rest(), m);
        Mappings res = m1.union(m2);
        res.setQuery(m1.getQuery());
        return res;
    }
    
    Mappings bgp(Node graph,  Producer p, Exp exp){
        return bgp(graph, p, exp, null);
    }
    
    Mappings bgp(Node graph, Producer p, Exp exp, Mapping m) {
        if (exp.size() == 1){ 
            Exp body = exp.get(0);
            if (body.isBGP()) {
                return bgp(graph, p, body, m);
            }
            else if (body.isStatement() && ! body.isQuery()){
                return eval(graph, p, body, m);
            }
        } 
        return basic(graph, p, exp, m);
    }
    
    Mappings basic(Node graph, Producer p, Exp exp, Mapping m) {
        exp.setType(Exp.AND);
        Mappings map =null;
        try {
            map = eval.exec(graph, p, exp, m);
        } catch (SparqlException ex) {
            ex.printStackTrace();
        }
        exp.setType(Exp.BGP);
        return map;
    }

    private Mappings filter(Producer p, Exp exp, Mappings map) {
        Mappings res = Mappings.create(map.getQuery());
        HashMap bnode = new HashMap();
        for (Mapping m : map){
            m.setMap(bnode);
            bnode.clear();
            m.setQuery(map.getQuery());
            if (test(p, exp, m)){
                res.add(m);
            }
        }
        return res;
    }
    
    private boolean test(Producer p, Exp exp, Mapping m){
        for (Exp f : exp){
            try {
                if (! eval.test(null, f.getFilter(), m, p)){
                    return false;
                }
            } catch (SparqlException ex) {
                    return false;
            }
        }
        return true;
    }
    

}
