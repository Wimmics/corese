package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public interface ProcessVisitor {
    
    default void setProcessor(Eval e) {}
    
    default void before() {}
    
    default void after(Mappings map) {}

}
