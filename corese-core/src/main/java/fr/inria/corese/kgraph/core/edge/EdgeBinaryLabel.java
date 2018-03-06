package fr.inria.corese.kgraph.core.edge;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;

/**
 * Edge entailed by a Rule
 * index and provenance
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class EdgeBinaryLabel extends EdgeBinary {

    EdgeBinaryLabel(Node subject, Node object) {
        super(subject, object);
    }
    
    public static EdgeBinaryLabel create(Node source, Node subject, Node predicate, Node object){
        return new EdgeBinaryLabel(subject, object);
    }

    @Override
    public Node getEdgeNode() {
        return subject.getTripleStore().getNode(Graph.LABEL_INDEX);
    }
}
