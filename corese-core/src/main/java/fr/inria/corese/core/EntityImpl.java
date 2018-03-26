package fr.inria.corese.core;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.PointerObject;

public class EntityImpl extends PointerObject implements Entity {

    Node graph, node;

    EntityImpl(Node g, Node n) {
        graph = g;
        node = n;
    }

    public static EntityImpl create(Node g, Node n) {
        return new EntityImpl(g, n);
    }

    @Override
    public Edge getEdge() {
        return null;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public Node getGraph() {

        return graph;
    }

    @Override
    public Node getNode(int i) {
        return null;
    }

    @Override
    public int nbNode() {
        return 0;
    }

    @Override
    public int nbGraphNode() {
        return 0;
    }

    @Override
    public Object getProvenance() {
        return null;
    }

    @Override
    public void setProvenance(Object obj) {

    }

    @Override
    public Iterable getLoop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
