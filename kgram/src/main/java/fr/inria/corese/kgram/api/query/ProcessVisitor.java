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
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public interface ProcessVisitor extends Pointerable {

    int SLICE_DEFAULT = 20;
    
    default void trace(String name) {
        System.out.println("Visitor event " + name);
    }
    
    default void setActive(boolean b) {}
    
    default boolean isActive() {return true;}
    
    default boolean isShareable() { return false; }
    
    default void setProcessor(Eval e) {}
    
    default void setDefaultValue(DatatypeValue val) {}
    
    default DatatypeValue defaultValue() {return null;}
    
    default DatatypeValue init() {return defaultValue();}
    
    default DatatypeValue init(Query q) {return defaultValue();}
    
    default DatatypeValue before(Query q) { return defaultValue(); }
    
    default DatatypeValue after(Mappings map) { return defaultValue(); }
    
    default DatatypeValue beforeUpdate(Query q) { return defaultValue(); }
    
    default DatatypeValue afterUpdate(Mappings map) { return defaultValue(); }
    
    default DatatypeValue beforeLoad(DatatypeValue path) { return defaultValue(); }
    
    default DatatypeValue afterLoad(DatatypeValue path) { return defaultValue(); }
    
    default DatatypeValue beforeEntailment(DatatypeValue path) { return defaultValue(); }
    
    default DatatypeValue afterEntailment(DatatypeValue path) { return defaultValue(); }
    
    default DatatypeValue loopEntailment(DatatypeValue path) { return defaultValue(); }

    default DatatypeValue prepareEntailment(DatatypeValue path) { return defaultValue(); }
    
    default boolean entailment() { return false; }
    
    default DatatypeValue entailment(Query rule, List<Edge> construct, List<Edge> where) { return defaultValue(); }

    default DatatypeValue beforeRule(Query q) { return defaultValue(); }
    
    default DatatypeValue afterRule(Query q, Object res) { return defaultValue(); }
    
    default DatatypeValue start(Query q) { return defaultValue(); }
    
    default DatatypeValue finish(Mappings map) { return defaultValue(); }
    
    default DatatypeValue orderby(Mappings map) { return defaultValue(); }
    
    default boolean distinct(Eval eval, Query q, Mapping map) { return true; }
    
    default boolean limit(Mappings map) { return true;}
    
    default int timeout(Node serv) { return 0; }
    
    default int slice(Node serv, Mappings map) { return SLICE_DEFAULT; }
    
    default void setSlice(int n) {}

    default DatatypeValue produce(Eval eval, Node g, Edge edge) { return defaultValue(); }
    
    default DatatypeValue candidate(Eval eval, Node g, Edge q, Edge e) { return defaultValue();}
    
    default DatatypeValue path(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return defaultValue();}
    
    default boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return true;}

    default boolean result(Eval eval, Mappings map, Mapping m) { return true; }

    default DatatypeValue statement(Eval eval, Node g, Exp e) { return defaultValue(); }
    

    default DatatypeValue bgp(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); } 

    default DatatypeValue join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 

    default DatatypeValue optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
       
    default DatatypeValue minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
    
    default DatatypeValue union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
    
    default DatatypeValue graph(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }
    
    default DatatypeValue query(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }    

    default DatatypeValue service(Eval eval, Node s, Exp e, Mappings m) { return defaultValue(); }  
       
    default DatatypeValue values(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }  
    
    default boolean filter(Eval eval, Node g, Expr e, boolean b) { return b; } 
    
    default boolean having(Eval eval, Expr e, boolean b) { return b; } 
       
    default DatatypeValue bind(Eval eval, Node g, Exp e, DatatypeValue val) { return val; }  

    default DatatypeValue select(Eval eval, Expr e, DatatypeValue val) { return val; } 
    
    default DatatypeValue aggregate(Eval eval, Expr e, DatatypeValue val) { return val; } 
    
    default DatatypeValue function(Eval eval, Expr funcall, Expr fundef) { return defaultValue(); }    
    
    
    default DatatypeValue error(Eval eval, Expr exp, DatatypeValue... param) { return null; }
        
    default DatatypeValue overload(Eval eval, Expr exp, DatatypeValue res, DatatypeValue... param) { return null; }

    default boolean produce() { return false; }
                
    default boolean statement() { return false; }
        
    default boolean candidate() { return false; }
    
    default boolean filter() { return false; }
    
    default boolean overload(Expr exp, DatatypeValue res, DatatypeValue dt1, DatatypeValue dt2) { return false; }
    
    default int compare(Eval eval, int res, DatatypeValue dt1, DatatypeValue dt2) { return res ;}
    
    default DatatypeValue datatype(DatatypeValue type, DatatypeValue sup) { return type ;};
    
    default DatatypeValue insert(DatatypeValue path, Edge edge) { return defaultValue();}
    
    default DatatypeValue delete(Edge edge) { return defaultValue();}

    default DatatypeValue update(Query q, List<Edge> delete, List<Edge> insert) { return defaultValue();}


}
