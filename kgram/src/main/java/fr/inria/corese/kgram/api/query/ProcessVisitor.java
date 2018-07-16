package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
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
public interface ProcessVisitor extends Pointerable {
    
    default boolean isShareable() { return false; }
    
    default void setProcessor(Eval e) {}
    
    default void init(Query q) {}
    
    default DatatypeValue before(Query q) { return null; }
    
    default DatatypeValue after(Mappings map) { return null; }
    
    default DatatypeValue start(Query q) { return null; }
    
    default DatatypeValue finish(Mappings map) { return null; }
    
    default DatatypeValue orderby(Mappings map) { return null; }
    
    default boolean distinct(Eval eval, Query q, Mapping map) { return true; }
    
    default boolean limit(Mappings map) { return true;}
    
    default int timeout(Node serv) { return 0; }

    default DatatypeValue produce(Eval eval, Node g, Edge edge) { return null; }
    
    default DatatypeValue candidate(Eval eval, Node g, Edge q, Edge e) { return null;}
    
    default DatatypeValue path(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return null;}
    
    default boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return true;}

    default boolean result(Eval eval, Mappings map, Mapping m) { return true; }

    default DatatypeValue statement(Eval eval, Node g, Exp e) { return null; }
    

    default DatatypeValue bgp(Eval eval, Node g, Exp e, Mappings m) { return null; } 

    default DatatypeValue join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return null; } 

    default DatatypeValue optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return null; } 
       
    default DatatypeValue minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return null; } 
    
    default DatatypeValue union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return null; } 
    
    default DatatypeValue graph(Eval eval, Node g, Exp e, Mappings m) { return null; }
    
    default DatatypeValue query(Eval eval, Node g, Exp e, Mappings m) { return null; }    

    default DatatypeValue service(Eval eval, Node s, Exp e, Mappings m) { return null; }  
       
    default DatatypeValue values(Eval eval, Node g, Exp e, Mappings m) { return null; }  
    
    default boolean filter(Eval eval, Node g, Expr e, boolean b) { return b; } 
    
    default boolean having(Eval eval, Expr e, boolean b) { return b; } 
       
    default DatatypeValue bind(Eval eval, Node g, Exp e, DatatypeValue val) { return val; }  

    default DatatypeValue select(Eval eval, Expr e, DatatypeValue val) { return val; } 
    
    default DatatypeValue aggregate(Eval eval, Expr e, DatatypeValue val) { return val; } 
    
    default DatatypeValue function(Eval eval, Expr funcall, Expr fundef) { return null; }    
    
    
    default DatatypeValue error(Eval eval, Expr exp, DatatypeValue... param) { return null; }
        
    default DatatypeValue overload(Eval eval, Expr exp, DatatypeValue res, DatatypeValue... param) { return null; }

    default boolean produce() { return false; }
                
    default boolean statement() { return false; }
        
    default boolean candidate() { return false; }
    
    default boolean filter() { return false; }
    
    default boolean overload(Expr exp, DatatypeValue res, DatatypeValue dt1, DatatypeValue dt2) { return false; }

}
