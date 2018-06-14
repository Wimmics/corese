package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public interface ProcessVisitor {
    
    default void setProcessor(Eval e) {}
    
    
    default DatatypeValue before(Query q) { return null; }
    
    default DatatypeValue after(Mappings map) { return null; }
    
    
    default DatatypeValue produce(Eval eval, Edge edge) { return null; }
    
    default DatatypeValue candidate(Eval eval, Edge q, Edge e) { return null;}
    
    default DatatypeValue path(Eval eval, Edge q, Path p, Node s, Node o) { return null;}
    
    default boolean step(Eval eval, Edge q, Path p, Node s, Node o) { return true;}

    default boolean result(Eval eval, Mappings map, Mapping m) { return true; }

    default DatatypeValue statement(Eval eval, Exp e) { return null; }
    

    default DatatypeValue optional(Eval eval, Exp e, Mappings m1, Mappings m2) { return null; } 
       
    default DatatypeValue minus(Eval eval, Exp e, Mappings m1, Mappings m2) { return null; } 
    
    default DatatypeValue union(Eval eval, Exp e, Mappings m1, Mappings m2) { return null; } 
    
    default DatatypeValue graph(Eval eval, Exp e, Mappings m) { return null; }
    
    default DatatypeValue query(Eval eval, Exp e, Mappings m) { return null; }    

    default DatatypeValue service(Eval eval, Exp e, Mappings m) { return null; }  
       
    
    default boolean filter(Eval eval, Expr e, boolean b) { return b; } 
    
    default boolean having(Eval eval, Expr e, boolean b) { return b; } 
    
    
    default DatatypeValue bind(Eval eval, Exp e, DatatypeValue val) { return val; }  

    default DatatypeValue select(Eval eval, Expr e, DatatypeValue val) { return val; } 
    
    default DatatypeValue aggregate(Eval eval, Expr e, DatatypeValue val) { return val; } 
    

    default boolean produce() { return false; }
                
    default boolean statement() { return false; }
        
    default boolean candidate() { return false;}
}
