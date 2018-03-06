package fr.inria.corese.kgraph.core.edge;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

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
    
     @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.ENTAIL_INDEX);
    }
    
}

