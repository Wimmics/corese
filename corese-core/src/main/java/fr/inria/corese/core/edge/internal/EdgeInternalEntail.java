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
public  class EdgeInternalEntail extends EdgeBinary {

    public EdgeInternalEntail() {
    }

  
    EdgeInternalEntail(Node subject, Node object) {
        super(subject, object);
    }  
    
    public static EdgeInternalEntail create(Node graph, Node subject, Node predicate, Node object){
        return new EdgeInternalEntail(subject, object);
    }
    
     public static EdgeInternalEntail create(Edge edge){
        return new EdgeInternalEntail(edge.getNode(0), edge.getNode(1));
    }
    
     @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.ENTAIL_INDEX);
    }
    
}

