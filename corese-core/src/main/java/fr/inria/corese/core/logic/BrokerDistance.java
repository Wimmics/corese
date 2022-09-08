package fr.inria.corese.core.logic;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import java.util.List;

/**
 * Provide access to graph for distance processing
 */
public class BrokerDistance {
    
    private Graph graph;
    
    BrokerDistance() {}
    
    BrokerDistance(Graph g) {
        graph = g;
    }
    
    
    Node getPropertyNode(String name) {
        return getGraph().getPropertyNode(name);
    }
    
    // owl:Thing or rdfs:Resource
    Node getTopLevel(String defaut, String... nameList) {
        return getGraph().getTopClass(defaut, nameList);
    }
    
    // top level classes y s.t. 
    // x subClassOf y and not(y subEntityOf z)
    List<Node> getTopLevelList(Node predicate) {
        return getGraph().getTopLevel(predicate);
    }
    
    // retrieve edges of predicate where node is node at index
    // return opposite nodes
    Iterable<Node> getNodeList(Node predicate, Node node, int index) {
        return getGraph().getNodes(predicate, node, index);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    
}
