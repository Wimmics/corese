package fr.inria.corese.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class NodeGraph extends NodeImpl {
    Node node, graph;

    public NodeGraph(IDatatype val) {
        super(val);
    }
    
    public NodeGraph(Node node, Node graph) {
        this((IDatatype) node.getDatatypeValue());
        this.node = node;
        this.graph = graph;
    }
    
    @Override
    public Node getNode() {
        return node;
    }
    
     @Override
    public Node getGraph() {
        return graph;
    }

}
