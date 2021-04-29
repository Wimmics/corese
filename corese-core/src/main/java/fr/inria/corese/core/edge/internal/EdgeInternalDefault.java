package fr.inria.corese.core.edge.internal;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.edge.EdgeBinary;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Graph Edge for internal storage
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public  class EdgeInternalDefault extends EdgeBinary {

    public EdgeInternalDefault() {
    }

  
    EdgeInternalDefault(Node subject, Node object) {
        super(subject, object);
    }  
    
    public static EdgeInternalDefault create(Node graph, Node subject, Node predicate, Node object){
        return new EdgeInternalDefault(subject, object);
    }
    
     public static EdgeInternalDefault create(Edge edge){
        return new EdgeInternalDefault(edge.getNode(0), edge.getNode(1));
    }
    
     @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.DEFAULT_INDEX);
    }
    
}

