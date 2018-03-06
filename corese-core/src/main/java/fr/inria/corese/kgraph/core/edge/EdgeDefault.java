package fr.inria.corese.kgraph.core.edge;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeDefault extends EdgeTriple {
    
    EdgeDefault(Node predicate, Node subject, Node object){
        super(predicate, subject, object);
    }
    
    public static EdgeDefault create(Node source, Node subject, Node predicate, Node object){
        return new EdgeDefault(predicate, subject, object);
    }
    
    @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.DEFAULT_INDEX);
    }

}
