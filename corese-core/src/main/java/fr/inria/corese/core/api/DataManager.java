package fr.inria.corese.core.api;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to corese graph OR external graph implementation
 */
public interface DataManager extends DataManagerUpdate {
    
     
    /**
     * Edge iterator for sparql
     * Parameter from provides union of triples 
     * select from where default graph semantics
     */
    default Iterable<Edge> getEdgeList(Node subject, Node predicate, Node object, List<Node> from) {
        return new ArrayList<>(0);
    }
    
    /**
     * Named graph name list as instances of type Node,
     * they are the set of URI of named graph in the dataset
     * Although of type Node, named graph nodes may, 
     * or may not, be subject/object in the dataset. 
     * 
     */
    default Iterable<Node> getGraphList() {
        return new ArrayList<>(0);
    }
    
    /**
     * Property list where properties are instances of type Node
     * Although of type Node, they are properties, 
     * they may, or may not, be subject/object of the dataset.
     */
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
     * @return set of subject/object of named graph with name = node
     */
    default Iterable<Node> getGraphNodeList(Node node) {
        return new ArrayList<>(0);
    }
    
}
