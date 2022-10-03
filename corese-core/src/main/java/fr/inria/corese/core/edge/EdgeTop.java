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
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public abstract class EdgeTop extends GraphObject implements Edge {
    private byte level = AccessRight.DEFAULT;
    private static final long serialVersionUID = 2087591563645988076L;
    public static boolean DISPLAY_EDGE_AS_RDF4J = false;
    public static final String NL = System.getProperty("line.separator");
    private boolean nested = false;
    // created by values, bind or triple()
    private boolean created = false;

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

    @Override
    public Node getProperty() {
        return getEdgeNode();
    }

    public void setEdgeNode(Node pred) {
    }

    @Override
    public void setProperty(Node pred) {
        setEdgeNode(pred);
    }

    public void setTag(Node node) {
    }

    @Override
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
        ArrayList<IDatatype> list = new ArrayList<>();
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
    public Node getNode() {
        return DatatypeMap.createObject(this.toString(), this);
    }

    @Override
    public boolean contains(Node node) {
        return getNode(0).same(node) || getNode(1).same(node);
    }

    @Override
    public int nbGraphNode() {
        return 2;
    }

    @Override
    public int nbNode() {
        return 2;
    }

    @Override
    public String getEdgeLabel() {
        return getEdgeNode().getLabel();
    }

    @Override
    public Node getEdgeVariable() {
        return null;
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
        if (this == o) {
            return true;
        }
        if (o instanceof Edge) {
            return equals((Edge) o);
        } else if (o instanceof Statement) {
            return equals((Statement) o);
        }
        return false;
    }

    public boolean equals(Edge t) {
        return getObjectNode().equals(t.getObjectNode())
                && getSubjectNode().equals(t.getSubjectNode())
                && getPropertyNode().equals(t.getPropertyNode())
                && Objects.equals(this.getGraphNode(), t.getGraphNode());
    }

    public boolean equals(Statement t) {
        return getObject().equals(t.getObject())
                && getSubject().equals(t.getSubject())
                && getPredicate().equals(t.getPredicate())
                && Objects.equals(this.getContext(), t.getContext());
    }

    @Override
    public String toString() {
        if (DISPLAY_EDGE_AS_RDF4J) {
            return toRDF4JString();
        }
        return toRDFString();
    }

    public String toRDFString() {
        return String.format("%s %s %s %s", getGraphValue(), getSubjectValue(), getPredicateValue(), getObjectValue());
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

    @Override
    public boolean isNested() {
        return nested;
    }

    @Override
    public void setNested(boolean nested) {
        this.nested = nested;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public void setCreated(boolean created) {
        this.created = created;
    }
}
