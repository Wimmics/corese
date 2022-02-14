package fr.inria.corese.core;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Node that contain the named graph of node, only for PP query.
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
        this(node.getDatatypeValue());
        this.node = node;
        this.graph = graph;
        setIndex(node.getIndex());
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
