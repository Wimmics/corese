package fr.inria.edelweiss.kgram.api.core;

import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 *
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable {
    
    public static final int UNDEF    = -1;
    public static final int MAPPINGS = 1;
    public static final int MAPPING  = 2;
    public static final int ENTITY   = 3;
    
    int pointerType();
    
    Mappings getMappings();
    
    Mapping getMapping();
    
    Entity getEntity();
    
    // let ((?x, ?y) = ?m)
    // ->
    // let (?x = xt:gget(?m, "?x", 0))
    Object getValue(String var, int n);
    
    int size();
   
}
