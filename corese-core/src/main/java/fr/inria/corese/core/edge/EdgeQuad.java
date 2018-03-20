package fr.inria.corese.core.edge;

import fr.inria.corese.kgram.api.core.Node;

/**
 * Graph Edge as Quad
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public class EdgeQuad extends EdgeTriple {
    protected Node graph;

    public EdgeQuad() {
    }

    
    EdgeQuad(Node g, Node pred, Node subject, Node object) {
        super(pred, subject, object);
        this.graph = g;
    }
    
     
    public static EdgeQuad create(Node g, Node subject, Node pred, Node object) {
        return new EdgeQuad(g, pred, subject, object);
    }

    @Override
    public Node getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Node gNode) {
        graph = gNode;
    }

}

