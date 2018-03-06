package fr.inria.corese.kgraph.core.edge;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

/**
 * Edge entailed by a Rule
 * index and provenance
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
