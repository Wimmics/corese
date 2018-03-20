package fr.inria.corese.core.stats;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface for calling Producer from kgraph to do statistics
 *
 * @author Fuqi Song, WImmics Inria I3S
 * @date 4 juin 2014
 * @deprecated 21.10.2014 F. Song
 */
public interface IStats {

    public static final int NA = 0;
    public static final int SUBJECT = 1;
    public static final int PREDICATE = 2;
    public static final int OBJECT = 3;
    public static final int TRIPLE = 4;

    /**
     * Get the number of all triples in a graph
     *
     * @return
     */
    int getAllTriplesNumber();

    /**
     * Get the number of distinct subjects
     *
     * @return
     */
    int getResourceNumber();

    /**
     * Get the number of distinct predicates in a graph
     *
     * @return
     */
    int getPropertyNumber();

    /**
     * Get the number of distinct objects
     * 
     * @return 
     */
    int getObjectNumber();

    /**
     * Get the number of triples that contains the given node
     * 
     * @param n Node
     * @param type indicates which one is constant SUBJECT | PREDICATE | OBJECT
     * @return number
     */
    int getCountByValue(Node n, int type);

    /**
     * Get the estimated number of triples according to whole triple pattern
     * 
     * @param e Edge that contains the expresion EDGE
     * @param type indicates which one is variable SUBJECT | PREDICATE | OBJECT
     * @return 
     */
    int getCountByTriple(Edge e, int type);

    /**
     * Get the status of statistics, enabled or not
     * @return 
     */
    boolean statsEnabled();

    /**
     * Check weather the meta data has been initlized if enabled
     * 
     * @return 
     */
    boolean statsInitialized();
    
    /**
     * Create the meta data instance
     */
    void createStatsInstance();
    
    
}
