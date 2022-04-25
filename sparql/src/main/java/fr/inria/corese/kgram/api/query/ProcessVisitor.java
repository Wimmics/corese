package fr.inria.corese.kgram.api.query;

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
import fr.inria.corese.sparql.api.IDatatype;
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
    
    default Eval getProcessor() { return null;}
    
    default void setDefaultValue(IDatatype val) {}
    
    default IDatatype defaultValue() {return null;}
    
    default IDatatype initParam() {return defaultValue();}
    
    // before query and before lock graph
    default IDatatype prepare() {return defaultValue();}

    default IDatatype init() {return defaultValue();}
    
    default IDatatype init(Query q) {return defaultValue();}
    
    default IDatatype before(Query q) { return defaultValue(); }
    
    default IDatatype after(Mappings map) { return defaultValue(); }
    
    default IDatatype construct(Mappings map) { return defaultValue(); }
    
    default IDatatype beforeUpdate(Query q) { return defaultValue(); }
    
    default IDatatype afterUpdate(Mappings map) { return defaultValue(); }
    
    default IDatatype beforeLoad(IDatatype path) { return defaultValue(); }
    
    default IDatatype afterLoad(IDatatype path) { return defaultValue(); }
    
    default IDatatype beforeEntailment(IDatatype path) { return defaultValue(); }
    
    default IDatatype afterEntailment(IDatatype path) { return defaultValue(); }
    
    default IDatatype loopEntailment(IDatatype path) { return defaultValue(); }

    default IDatatype prepareEntailment(IDatatype path) { return defaultValue(); }
    
    default boolean entailment() { return false; }
    
    default IDatatype entailment(Query rule, List<Edge> construct, List<Edge> where) { return defaultValue(); }

    default IDatatype beforeRule(Query q) { return defaultValue(); }
    
    default IDatatype afterRule(Query q, Object res) { return defaultValue(); }
    
    // success = true when there is no solution 
    // because rule where part test condition where constraint fails
    // res = Mappings or List<Edge>
    default IDatatype constraintRule(Query q, Object res, IDatatype success) { return success; }

    default IDatatype start(Query q) { return defaultValue(); }
    
    default IDatatype finish(Mappings map) { return defaultValue(); }
    
    default IDatatype orderby(Mappings map) { return defaultValue(); }
    
    default boolean distinct(Eval eval, Query q, Mapping map) { return true; }
    
    default boolean limit(Mappings map) { return true;}
    
    default int timeout(Node serv) { return 0; }
    
    default int slice() { return SLICE_DEFAULT; }
    @Deprecated
    default int slice(Node serv, Mappings map) { return SLICE_DEFAULT; }
    
    // return result for ldscript call java:setSlice()
    default int setSlice(int n) { return SLICE_DEFAULT; }

    default IDatatype produce(Eval eval, Node g, Edge edge) { return defaultValue(); }
    
    default IDatatype candidate(Eval eval, Node g, Edge q, Edge e) { return defaultValue();}
    
    default IDatatype path(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return defaultValue();}
    
    default boolean step(Eval eval, Node g, Edge q, Path p, Node s, Node o) { return true;}

    default boolean result(Eval eval, Mappings map, Mapping m) { return true; }

    default IDatatype statement(Eval eval, Node g, Exp e) { return defaultValue(); }
    

    default IDatatype bgp(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); } 

    default IDatatype join(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 

    default IDatatype optional(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
       
    default IDatatype minus(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
    
    default IDatatype union(Eval eval, Node g, Exp e, Mappings m1, Mappings m2) { return defaultValue(); } 
    
    default IDatatype graph(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }
    
    default IDatatype query(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }    

    default IDatatype service(Eval eval, Node s, Exp e, Mappings m) { return defaultValue(); }  
       
    default IDatatype values(Eval eval, Node g, Exp e, Mappings m) { return defaultValue(); }  
    
    default boolean filter(Eval eval, Node g, Expr e, boolean b) { return b; } 
    
    default boolean having(Eval eval, Expr e, boolean b) { return b; } 
       
    default IDatatype bind(Eval eval, Node g, Exp e, IDatatype val) { return val; }  

    default IDatatype select(Eval eval, Expr e, IDatatype val) { return val; } 
    
    default IDatatype aggregate(Eval eval, Expr e, IDatatype val) { return val; } 
    
    default IDatatype function(Eval eval, Expr funcall, Expr fundef) { return defaultValue(); }    
    
    
    default IDatatype error(Eval eval, Expr exp, IDatatype... param) { return null; }
        
    default IDatatype overload(Eval eval, Expr exp, IDatatype res, IDatatype... param) { return null; }

    default boolean produce() { return false; }
                
    default boolean statement() { return false; }
        
    default boolean candidate() { return false; }
    
    default boolean filter() { return false; }
    
    default boolean overload(Expr exp, IDatatype res, IDatatype dt1, IDatatype dt2) { return false; }
    
    default int compare(Eval eval, int res, IDatatype dt1, IDatatype dt2) { return res ;}
    
    default IDatatype datatype(IDatatype type, IDatatype sup) { return type ;};
    
    default IDatatype insert(IDatatype path, Edge edge) { return defaultValue();}
    
    default IDatatype delete(Edge edge) { return defaultValue();}

    default IDatatype update(Query q, List<Edge> delete, List<Edge> insert) { return defaultValue();}


}
