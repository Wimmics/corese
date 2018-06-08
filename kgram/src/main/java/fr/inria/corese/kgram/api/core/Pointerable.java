package fr.inria.corese.kgram.api.core;

import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.path.Path;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby - Wimmics Inria I3S - 2015
 */
public interface Pointerable extends Loopable {
    static final ArrayList empty = new ArrayList(0);
    
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
    public static final int DATAPRODUCER_POINTER  = 11;
    public static final int PATH_POINTER  = 12;
    
     
    default int pointerType() {
        return UNDEF_POINTER;
    }

    
    default Mappings getMappings() {
        return null;    
    }

    
    default Mapping getMapping() {
        return null;
    }
   
    default Edge getEdge() {
        return null;
    }
        
    default Query getQuery() {
        return null;
    }
    
    default Path getPathObject() {
        return null;
    }
       
    default TripleStore getTripleStore(){
        return null;
    }
       
    default int size(){
        return 0;
    }
       
    default Object getValue(String var, int n){
        return null;
    }
    
    
    @Override
    default Iterable getLoop(){
        return empty;
    }
    
    default String getDatatypeLabel() {
        return Integer.toString(hashCode());
    }
   
}
