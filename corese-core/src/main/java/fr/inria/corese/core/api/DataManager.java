package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 *
 */
public interface DataManager {
    
    /**
     * 
     * @param predicate : property name URI or Graph.TOPREL when unbound variable
     * @param node : subject/object/graph when it is known
     * @param n : index of node in edge: subject=0, object=1, graph=Graph.IGRAPH
     * @return 
     */
    Iterable<Edge> iterate(Node predicate, Node node, int n) ;
    
}
