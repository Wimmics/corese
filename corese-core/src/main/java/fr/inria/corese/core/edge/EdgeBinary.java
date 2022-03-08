package fr.inria.corese.core.edge;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Graph Edge for the defaultGraph
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public abstract class EdgeBinary extends EdgeTop implements Edge {
    public static boolean displayGraph = true;
    protected Node subject, object;

    public EdgeBinary() {
    }

    public EdgeBinary(Node subject, Node object) {
        this.subject = subject;
        this.object = object;
    }

    @Override
    public boolean contains(Node node) {
        return getNode(0).same(node) || getNode(1).same(node);
    }
   
    @Override
    public Node getNode(int n) {
        switch (n) {
            case Graph.IGRAPH:
                return getGraph();
            case 0:
                return subject;
            case 1:
                return object;
        }
        return null;
    }

    @Override
    public void setNode(int i, Node n) {
        switch (i) {
            case 0:
                subject = n;
                break;
            case 1:
                object = n;
                break;
        }
    }

    @Override
    public int nbNode() {
        return 2;
    }

    @Override
    public int nbGraphNode() {
        return 2;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public Object getProvenance() {
        return null;
    }

    @Override
    public void setProvenance(Object obj) {
    }

    @Override
    public Node getGraph() {
        return subject.getTripleStore().getNode(Graph.DEFAULT_INDEX);
    }

}
