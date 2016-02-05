package fr.inria.edelweiss.kgram.api.core;

import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

/**
 *
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable extends Loopable {
    
    public static final int UNDEF_POINTER    = -1;
    public static final int MAPPINGS_POINTER = 1;
    public static final int MAPPING_POINTER  = 2;
    public static final int ENTITY_POINTER   = 3;
    public static final int GRAPH_POINTER    = 4;
    public static final int NSMANAGER_POINTER= 5;
    public static final int QUERY_POINTER    = 6;
    
    int pointerType();
    
    Mappings getMappings();
    
    Mapping getMapping();
    
    Entity getEntity();
    
    Query getQuery();

    Object getGraphStore();
    
    // let ((?x, ?y) = ?m)
    // ->
    // let (?x = xt:gget(?m, "?x", 0))
    Object getValue(String var, int n);
    
    int size();
   
}
