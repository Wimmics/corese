package fr.inria.corese.kgraph.core.edge;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeEntail extends EdgeTriple {
    
    EdgeEntail(Node predicate, Node subject, Node object){
        super(predicate, subject, object);
    }
    
    public static EdgeEntail create(Node source, Node subject, Node predicate, Node object){
        return new EdgeEntail(predicate, subject, object);
    }
    
    @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.ENTAIL_INDEX);
    }

}
