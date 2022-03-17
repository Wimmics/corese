package fr.inria.corese.core.edge;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import java.util.Arrays;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.ArrayList;

/**
 * Graph Edge with n nodes (not only triple)
 * RDF star triple with reference node t
 * g <<s p o t>>
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeImpl extends EdgeTop
        implements Edge {

    public static boolean displayGraph = true;
    int index = -1;
    protected Node graph, predicate;
    Node[] nodes;
    private boolean metadata;

    public EdgeImpl() {
    }

    EdgeImpl(Node g, Node p) {
        graph = g;
        predicate = p;
    }

    EdgeImpl(Node g, Node pred, Node subject, Node object) {
        this(g, pred);
        nodes = new Node[2];
        nodes[0] = subject;
        nodes[1] = object;
    }

    public EdgeImpl(Node g, Node pred, Node subject, Node object, Node arg1) {
        this(g, pred);
        nodes = new Node[3];
        nodes[0] = subject;
        nodes[1] = object;
        nodes[2] = arg1;
    }

    EdgeImpl(Node g, Node p, Node[] args) {
        this(g, p);
        nodes = args;
    }

    public static EdgeImpl create(Node g, Node subject, Node pred, Node object) {
        return new EdgeImpl(g, pred, subject, object);
    }

    public static EdgeImpl create(Node g, Node subject, Node pred, Node object, Node value) {
        return new EdgeImpl(g, pred, subject, object, value);
    }

    public static EdgeImpl createMetadata(Node g, Node subject, Node pred, Node object, Node value) {
        EdgeImpl ent = new EdgeImpl(g, pred, subject, object, value);
        ent.setMetadata(true);
        return ent;
    }

    public static EdgeImpl create(Node g, Node pred, List<Node> list) {
        Node[] nodes = new Node[list.size()];
        list.toArray(nodes);
        EdgeImpl e = new EdgeImpl(g, pred, nodes);
        return e;
    }

    public static EdgeImpl create(Node g, Node pred, Node[] nodes) {
        return new EdgeImpl(g, pred, nodes);
    }

    public void add(Node node) {
        nodes = Arrays.copyOf(nodes, nodes.length + 1);
        nodes[nodes.length - 1] = node;
    }

    @Override
    public EdgeImpl copy() {
        EdgeImpl ent = new EdgeImpl(getGraph(), getEdgeNode(), Arrays.copyOf(getNodes(), nbNode()));
        ent.setMetadata(isMetadata());
        return ent;
    }

    public void setNodes(Node[] args) {
        nodes = args;
    }

    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public void setNode(int i, Node node) {
        nodes[i] = node;
    }

    @Override
    public void setTag(Node node) {
        add(node);
    }

    @Override
    public String toString() {
        if (isNested()) {
            return nestedTriple();
        }
        else if (nbNode() > 2 && ! DatatypeMap.DISPLAY_AS_TRIPLE) {
            return tuple();
        }
        return super.toString();
    }
    
    public String nestedTriple() {
        String str = String.format("<<%s %s %s>>", getNode(0), getEdgeNode(), getNode(1));
        if (!DatatypeMap.DISPLAY_AS_TRIPLE && hasReferenceNode()) {
            str = String.format("%s [%s]", str, getReferenceNode());
        }
        if (getGraphNode()!=null && !getGraphNode().getLabel().equals(Entailment.DEFAULT)) {
            str = String.format("%s %s", getGraphNode(), str);
        }
        return str;
    }

    public String tuple() {
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
        str += toParse(new StringBuilder());

        return str;
    }

    String toParse(StringBuilder sb) {        
        sb.append("tuple(");
        sb.append(getEdgeNode());
        int j = 0;
        for (Node n : nodes) {
            if (j++ == Edge.REF_INDEX && n.isTriple()) {
                if (!DatatypeMap.DISPLAY_AS_TRIPLE) {
                    sb.append(" ").append(n);
                }
            }
            else {
                sb.append(" ").append(n);
            }
        }
        sb.append(")");
              
        if (! DatatypeMap.DISPLAY_AS_TRIPLE) {
            int i = 0;
            for (Node n : nodes) {
                if (i++ < 2) {
                    // subject/object
                    if (n.isTripleWithEdge()) {
                        sb.append(NL).append(n).append(" : ").append(n.getEdge().toString());
                    }
                }
            }
        }
        
        return sb.toString();
    }

    @Override
    public boolean contains(Node node) {
        for (Node n : nodes) {
            if (n.same(node)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Node getEdgeNode() {
        return predicate;
    }

    @Override
    public void setEdgeNode(Node node) {
        predicate = node;
    }

    @Override
    public int getEdgeIndex() {
        return index;
    }

    @Override
    public String getEdgeLabel() {
        return getEdgeNode().getLabel();
    }

    @Override
    public Node getNode(int n) {
        switch (n) {
            case Graph.IGRAPH:
                return getGraph();
            default:
                return nodes[n];
        }
    }

    @Override
    public int nbNode() {
        return nodes.length;
    }

    @Override
    public int nbGraphNode() {
        if (isMetadata()) {
            return 2;
        }
        return nodes.length;
    }

    @Override
    public void setEdgeIndex(int n) {
        index = n;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public Node getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Node gNode) {
        graph = gNode;
    }

    @Override
    public Node getNode() {
        // TODO Auto-generated method stub
        return DatatypeMap.createObject(this.toString(), this);
    }

    @Override
    public Node getEdgeVariable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProvenance() {
        if (nodes.length > 2) {
            return nodes[nodes.length - 1].getNodeObject();
        }
        return null;
    }

    /**
     * Draft
     */
    @Override
    public void setProvenance(Object obj) {
        if (!(obj instanceof Node)) {
            Node prov = DatatypeMap.createObject("provenance", obj);
            obj = prov;
        }
        add((Node) obj);
    }

    @Override
    public void duplicate(Edge cur) {
        setEdgeNode(cur.getEdgeNode());
        setGraph(cur.getGraph());
        replicate(cur);
    }

    @Override
    public void replicate(Edge cur) {
        nodes = new Node[cur.nbNode()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = cur.getNode(i);
        }
        setEdgeIndex(cur.getEdgeIndex());
    }

    /**
     * @return the metadata
     */
    public boolean isMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    @Override
    ArrayList<IDatatype> getNodeList() {
        ArrayList<IDatatype> list = new ArrayList();
        for (int i = 0; i <= nodes.length + 1; i++) {
            list.add(getValue(null, i));
        }
        return list;
    }

    /**
     * return s p o v1 .. vn g
     */
    @Override
    public IDatatype getValue(String var, int n) {
        switch (n) {
            case 0:
                return nodeValue(getNode(0));
            case 1:
                return nodeValue(getEdgeNode());
            default:
                if (n <= nodes.length) {
                    return nodeValue(getNode(n - 1));
                } else {
                    return nodeValue(getGraph());
                }
        }
    }

}
