package fr.inria.corese.core.edge;

import static fr.inria.corese.kgram.api.core.PointerType.TRIPLE;

import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.rdf4j.model.Statement;

import fr.inria.corese.core.GraphObject;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop extends GraphObject implements Edge {
    private byte level = AccessRight.DEFAULT;

    public Edge copy() {
        return create(getGraph(), getNode(0), getEdgeNode(), getNode(1));
    }

    public static Edge create(Node source, Node subject, Node predicate, Node objet) {
        return null;
    }

    // manage access right
    @Override
    public byte getLevel() {
        // return -1;
        return level;
    }

    @Override
    public Edge setLevel(byte b) {
        level = b;
        return this;
    }

    public Edge setLevel(int b) {
        level = (byte) b;
        return this;
    }

    @Override
    public String getDatatypeLabel() {
        return toString();
    }

    @Override
    public Node getEdgeNode() {
        return null;
    }

    public void setEdgeNode(Node pred) {
    }
    
    @Override
    public void setProperty(Node pred) {
        setEdgeNode(pred);
    }

    @Override
    public Node getProperty() {
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

    public void replicate(Edge cur) {
    }

    public void duplicate(Edge cur) {
    }

    @Override
    public Iterable<IDatatype> getLoop() {
        return getNodeList();
    }

    ArrayList<IDatatype> getNodeList() {
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
    public PointerType pointerType() {
        return TRIPLE;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public TripleStore getTripleStore() {
        return getNode(0).getTripleStore();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSubject(), this.getPredicate(), this.getObject(), this.getContext());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Statement)) {
            return false;
        }

        Statement other = (Statement) o;

        if (!this.getSubject().equals(other.getSubject())) {
            return false;
        }

        if (!this.getPredicate().equals(other.getPredicate())) {
            return false;
        }

        if (!this.getObject().equals(other.getObject())) {
            return false;
        }

        if (!Objects.equals(this.getContext(), other.getContext())) {
            return false;
        }

        return true;
    }
}
