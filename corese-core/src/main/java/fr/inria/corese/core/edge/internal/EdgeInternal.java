package fr.inria.corese.core.edge.internal;

import fr.inria.corese.core.edge.EdgeBinary;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Graph Edge for internal storage
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public  class EdgeInternal extends EdgeBinary {
    protected Node graph;

    public EdgeInternal() {
    }

  
    EdgeInternal(Node graph, Node subject, Node object) {
        this.graph = graph;
        this.subject = subject;
        this.object = object;
    }  
    
    public static EdgeInternal create(Node graph, Node subject, Node predicate, Node object){
        return new EdgeInternal(graph, subject, object);
    }
    
    public static EdgeInternal create(Edge edge){
        return new EdgeInternal(edge.getGraph(), edge.getNode(0), edge.getNode(1));
    }
    
     @Override
    public Node getGraph(){
        return graph;
    }
     
    @Override
     public void setGraph(Node g){
         graph = g;
     }
    
}

