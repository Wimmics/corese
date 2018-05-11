package fr.inria.corese.kgram.api.core;

import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

/**
 *
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable extends Loopable {
    
    public static final int UNDEF_POINTER    = -1;
    public static final int MAPPINGS_POINTER = 1;
    public static final int MAPPING_POINTER  = 2;
    public static final int EDGE_POINTER     = 3;
    public static final int GRAPH_POINTER    = 4;
    public static final int NSMANAGER_POINTER= 5;
    public static final int CONTEXT_POINTER  = 6;
    public static final int QUERY_POINTER    = 7;
    public static final int METADATA_POINTER = 8;
    public static final int DATASET_POINTER  = 9;
    public static final int EXPRESSION_POINTER  = 10;
    
    int pointerType();
    
    Mappings getMappings();
    
    Mapping getMapping();
    
    Edge getEdge();
    
    Query getQuery();

    TripleStore getTripleStore();
    
    // let ((?x, ?y) = ?m)
    // ->
    // let (?x = xt:gget(?m, "?x", 0))
    Object getValue(String var, int n);
    
    int size();
   
}
