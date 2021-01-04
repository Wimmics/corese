package fr.inria.corese.core.edge;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Graph Edge for the defaultGraph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeBinaryType extends EdgeBinary {

    EdgeBinaryType(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeBinaryType create(Node source, Node subject, Node predicate, Node object){
        return new EdgeBinaryType(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.TYPE_INDEX);
    }
}
