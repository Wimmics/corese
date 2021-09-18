package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to external graph implementation
 */
public interface DataManager {
    
     
    // from provides union of triples  (select from where default graph semantics)
    default Iterable<Edge> getEdgeList(Node subject, Node predicate, Node object, List<Node> from) {
        return new ArrayList<>(0);
    }
    
    default Iterable<Node> getGraphList() {
        return new ArrayList<>(0);
    }
    
    default Iterable<Node> getPropertyList() {
        return new ArrayList<>(0);
    }
    
    /**
     * @return set of subject/object of default graph
     */
    default Iterable<Node> getDefaultNodeList() {
        return new ArrayList<>(0);
    }

    /**
     * @return set of subject/object of named graph
     */
    default Iterable<Node> getGraphNodeList(Node node) {
        return new ArrayList<>(0);
    }
    
}
