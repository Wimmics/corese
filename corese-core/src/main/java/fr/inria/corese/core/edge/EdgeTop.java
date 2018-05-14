package fr.inria.corese.core.edge;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.core.GraphObject;
import java.util.ArrayList;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop extends GraphObject implements Edge {

    public Edge copy() {
        return create(getGraph(), getNode(0), getEdgeNode(), getNode(1));
    }

    public static Edge create(Node source, Node subject, Node predicate, Node objet) {
        return null;
    }

    @Override
    public Node getEdgeNode() {
        return null;
    }
    
    public void setEdgeNode(Node pred) {
    }
    
    @Override
    public Node getPredicate(){
        return getEdgeNode();
    }

    public void setTag(Node node) {
    }

    public void setGraph(Node node) {
    }

    @Override
    public Object getProvenance() {
        return null;
    }

    @Override
    public void setProvenance(Object o) {
    }
    
    public void replicate(Edge cur){}
    
    public void duplicate(Edge cur){}

    @Override
    public Iterable<IDatatype> getLoop() {
        return getNodeList();
    }

    public ArrayList<IDatatype> getNodeList() {
        ArrayList<IDatatype> list = new ArrayList();
        for (int i = 0; i < 4; i++) {
            list.add(getValue(null, i));
        }
        return list;
    }

    @Override
    public IDatatype getValue(String var, int n) {
        switch (n) {
            case 0:
                return nodeValue(getNode(0));
            case 1:
                return nodeValue(getEdgeNode());
            case 2:
                return nodeValue(getNode(1));
            case 3:
                return nodeValue(getGraph());
        }
        return null;
    }

    IDatatype nodeValue(Node n) {
        return (IDatatype) n.getDatatypeValue();
    }

    @Override
    public int pointerType() {
        return Pointerable.EDGE_POINTER;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public TripleStore getTripleStore() {
        return getNode(0).getTripleStore();
    }
}
