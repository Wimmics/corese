package fr.inria.corese.core.edge.binary;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.edge.EdgeBinary;

/**
 * Graph Edge for the defaultGraph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeBinarySubclass extends EdgeBinary {

    EdgeBinarySubclass(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeBinarySubclass create(Node source, Node subject, Node predicate, Node object){
        return new EdgeBinarySubclass(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.SUBCLASS_INDEX);
    }
}
