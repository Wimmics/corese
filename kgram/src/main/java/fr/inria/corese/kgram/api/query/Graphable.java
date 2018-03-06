
package fr.inria.corese.kgram.api.query;

/**
 *
 * Olivier Corby - Wimmics INRIA I3S - 2014
 */
public interface Graphable {
    
    String toGraph();
    
    void setGraph(Object obj);
    
    Object getGraph();
    
    
    
}
