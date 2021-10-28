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
    private static final long serialVersionUID = 2087591563645988076L;

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
        return n.getDatatypeValue();
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
        // We check object equality first since it's most likely to be different. In
        // general the number of different
        // predicates and contexts in sets of statements are the smallest (and therefore
        // most likely to be identical),
        // so these are checked last.

        return this == o || o instanceof Statement && this.getObject().equals(((Statement) o).getObject())
                && this.getSubject().equals(((Statement) o).getSubject())
                && this.getPredicate().equals(((Statement) o).getPredicate())
                && Objects.equals(this.getContext(), ((Statement) o).getContext());
    }

    @Override
    public String toString() {
        return toRDF4JString();
    }
    
    public String toRDFString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getGraphValue()).append(" ");
        sb.append(getSubjectValue()).append(" ");
        sb.append(getPredicateValue()).append(" ");
        sb.append(getObjectValue());                
        return sb.toString();
    }
    
    public String toRDF4JString() {
        StringBuilder sb = new StringBuilder(256);

        sb.append("(" + getSubject() + ", " + getPredicate() + ", " + getObject()
                + (getContext() == null ? "" : ", " + getContext()) + ")");

        if (getContext() != null) {
            sb.append(" [").append(getContext()).append("]");
        }

        return sb.toString();
    }
}
