package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

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

    default DatatypeValue result(Eval eval, Mapping m) { return null; }

    default DatatypeValue statement(Eval eval, Exp e) { return null; }

    

    default boolean produce() { return false; }
        
    default boolean result() { return false; }
        
    default boolean statement() { return false; }
        
    default boolean candidate() { return false;}
}
