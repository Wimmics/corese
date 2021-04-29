package fr.inria.corese.core.edge.internal;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.edge.EdgeBinary;

/**
 * Graph Edge for internal storage
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public  class EdgeInternalRule extends EdgeBinary {

    public EdgeInternalRule() {
    }

  
    EdgeInternalRule(Node subject, Node object) {
        super(subject, object);
    }  
    
    public static EdgeInternalRule create(Node subject, Node object){
        return new EdgeInternalRule(subject, object);
    }
    
     @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.RULE_INDEX);
    }
    
}

