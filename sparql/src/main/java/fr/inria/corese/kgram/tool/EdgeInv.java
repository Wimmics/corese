package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.TRIPLE;
import fr.inria.corese.kgram.core.PointerObject;

public class EdgeInv extends PointerObject implements Edge{

    Edge edge;
    Edge ent;

//    public EdgeInv(Edge e) {
//        edge = e;
//    }

    public EdgeInv(Edge e) {
        ent = e;
        edge = e;
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
    public int nbGraphNode() {
        return edge.nbGraphNode();
    }

    @Override
    public Edge getEdge() {
        return edge;
    }

    @Override
    public Node getGraph() {
        return ent.getGraph();
    }

    public Edge getEdgeEntity() {
        return ent;
    }

    @Override
    public Node getEdgeNode() {
        return edge.getEdgeNode();
    }

    @Override
    public int getEdgeIndex() {
        return edge.getEdgeIndex();
    }

    @Override
    public String getEdgeLabel() {
        return edge.getEdgeLabel();
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
    public void setNode(int i, Node n) {
        
    }

    @Override
    public Node getEdgeVariable() {
        return edge.getEdgeVariable();
    }
    
    @Override
    public Node getProperty() {
        return edge.getProperty();
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
    public PointerType pointerType() {
        return TRIPLE;
    }
}
