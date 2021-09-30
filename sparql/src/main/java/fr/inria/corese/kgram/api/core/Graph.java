package fr.inria.corese.kgram.api.core;

/**
 *
 */
public interface Graph {
    
    Edge getEdge(Node predicate, Node node, int n);
    
    Node value(Node subject, Node predicate, int n);
    
    Node getPropertyNode(Node dt);
    
    Node getNode(Node dt);
    
    Node getVertex(Node dt);
 
}
