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
public class EdgeBinaryFirst extends EdgeBinary {

    EdgeBinaryFirst(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeBinaryFirst create(Node source, Node subject, Node predicate, Node object){
        return new EdgeBinaryFirst(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.FIRST_INDEX);
    }
}
