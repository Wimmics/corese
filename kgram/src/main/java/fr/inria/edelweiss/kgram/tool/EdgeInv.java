package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.core.PointerObject;

public class EdgeInv extends PointerObject implements Edge, Entity {

    Edge edge;
    Entity ent;

    public EdgeInv(Edge e) {
        edge = e;
    }

    public EdgeInv(Entity e) {
        ent = e;
        edge = e.getEdge();
    }

    @Override
    public String toString() {
        return "inverse(" + edge.toString() + ")";
    }

    @Override
    public boolean contains(Node n) {

        return edge.contains(n);
    }

    @Override
    public int nbNode() {

        return edge.nbNode();
    }

    @Override
    public Edge getEdge() {
        return edge;
    }

    @Override
    public Node getGraph() {
        return ent.getGraph();
    }

    @Override
    public Entity getEntity() {
        return ent;
    }

    @Override
    public Node getEdgeNode() {
        return edge.getEdgeNode();
    }

    @Override
    public int getIndex() {
        return edge.getIndex();
    }

    @Override
    public String getLabel() {
        return edge.getLabel();
    }

    @Override
    public Node getNode(int n) {
        switch (n) {
            case 0:
                return edge.getNode(1);
            case 1:
                return edge.getNode(0);
            default:
                return edge.getNode(n);
        }
    }

    @Override
    public void setIndex(int n) {
    }

    @Override
    public Node getEdgeVariable() {
        return edge.getEdgeVariable();
    }
    
    @Override
    public Node getPredicate() {
        return edge.getPredicate();
    }


    @Override
    public Node getNode() {
        return null;
    }

    @Override
    public Object getProvenance() {
        return ent.getProvenance();
    }

    @Override
    public void setProvenance(Object obj) {
        ent.setProvenance(obj);
    }

    @Override
    public Iterable getLoop() {
        return ent.getLoop();
    }

    @Override
    public int pointerType() {
        return Pointerable.ENTITY_POINTER;
    }
}
