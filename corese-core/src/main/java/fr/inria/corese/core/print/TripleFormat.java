package fr.inria.corese.core.print;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * Turtle & Trig Format
 * 
 * Olivier Corby, Wimmics INRIA 2013
 */
public class TripleFormat extends RDFFormat {
    public static boolean DISPLAY_GRAPH_KEYWORD = false;

    static final String PREFIX = "@prefix";
    static final String PV = " ;";
    static final String DOT = " .";
    static final String OPEN = "<";
    static final String CLOSE = ">";
    static final String GRAPH = "graph";
    static final String OGRAPH = "{";
    static final String CGRAPH = "}";
    static final String RDF_TYPE = "rdf:type";
    static final String TAB = "  ";

    static final boolean addPrefix = true;

    boolean isGraph = false;
    // when true: display default graph kg:default with embedding graph kg:default
    // {}
    // when false: display default graph in turtle (without graph kg:default {})
    private boolean displayDefaultGraphURI = false;
    // true when this pretty print is for a future translation into sparql select
    // where
    private boolean graphQuery = false;
    private Mappings mappings;
    int tripleCounter = 0;

    private boolean useCompactBlankNodeSyntax = false; //

    TripleFormat(Graph g, NSManager n) {
        super(g, n);
    }

    public void enableCompactBlankNodeSyntax() {
        this.useCompactBlankNodeSyntax = true;
    }

    public void disableCompactBlankNodeSyntax() {
        this.useCompactBlankNodeSyntax = false;
    }

    public static TripleFormat create(Graph g, NSManager n) {
        return new TripleFormat(g, n);
    }

    public static TripleFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            return create(g, getNSM(map)).setMappings(map);
        }
        return create(Graph.create()).setMappings(map);
    }

    public static TripleFormat create(Graph g) {
        return new TripleFormat(g, nsm());
    }

    public static TripleFormat create(Mappings map, boolean isGraph) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            TripleFormat t = new TripleFormat(g, getNSM(map));
            t.setGraph(isGraph);
            return t.setMappings(map);
        }
        return create(Graph.create()).setMappings(map);
    }

    static NSManager getNSM(Mappings map) {
        Query q = map.getQuery();
        if (q == null) {
            return nsm();
        }
        return q.getAST().getNSM();
    }

    // isGraph = true -> Trig
    public static TripleFormat create(Graph g, boolean isGraph) {
        TripleFormat t = new TripleFormat(g, nsm());
        t.setGraph(isGraph);
        return t;
    }

    public void setGraph(boolean b) {
        isGraph = b;
    }

    @Override
    public String toString() {
        StringBuilder bb = getStringBuilder();
        return bb.toString();
    }

    public String toString(Node node) {
        StringBuilder bb = getStringBuilder(node);
        return bb.toString();
    }

    @Override
    public StringBuilder getStringBuilder() {
        return getStringBuilder(null);
    }

    public StringBuilder getStringBuilder(Node node) {
        sb = new StringBuilder();
        if (graph == null && map == null) {
            return sb;
        }

        if (node != null) {
            print(null, node);
        } else if (isGraph) {
            graphNodes();
        } else {
            nodes();
        }

        StringBuilder bb = new StringBuilder();
        header(bb);
        bb.append(NL);
        // bb.append(NL);
        bb.append(sb);
        return bb;
    }

    // iterate on subject nodes and pprint their edges
    void nodes() {
        for (Node node : getSubjectNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            print(null, node);
        }
    }

    // iterate named graph nodes and pprint their content
    void graphNodes() {
        // start by default graph
        graphNodes(graph.getDefaultGraphNode());

        for (Node gNode : graph.getGraphNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            if (!graph.isDefaultGraphNode(gNode)) {
                graphNodes(gNode);
            }
        }
    }

    void graphNodes(Node gNode) {
        if (accept(gNode)) {
            if (graph.isDefaultGraphNode(gNode) && !isDisplayDefaultGraphURI()) {
                basicGraphNode(gNode);

            } else {
                graphNode(gNode);
            }
        }
    }

    void graphNodes2() {
        for (Node gNode : graph.getGraphNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            if (accept(gNode)) {
                if (graph.isDefaultGraphNode(gNode) && !isDisplayDefaultGraphURI()) {
                    basicGraphNode(gNode);

                } else {
                    graphNode(gNode);
                }
            }
        }
    }

    // pprint content of named graph with trig syntax: uri { }
    void graphNode(Node gNode) {
        if (DISPLAY_GRAPH_KEYWORD || isGraphQuery()) {
            // isGraphQuery() : trig format for AST query graph pattern
            sdisplay(GRAPH);
            sdisplay(SPACE);
        }
        node(gNode);
        sdisplay(SPACE);
        sdisplay(OGRAPH);
        display();

        basicGraphNode(gNode);

        display(CGRAPH);
        display();
    }

    // pprint content of named graph
    void basicGraphNode(Node gNode) {
        for (Node node : graph.getNodeGraphIterator(gNode)) {
            print(gNode, node.getNode());
        }
    }

    private boolean isRdfPrefixNeeded() {
        //for (Node node : graph.getGraphNodes()) {
            for (Edge edge : graph.getEdges()) {
                String pred = nsm.toPrefix(edge.getEdgeNode().getLabel(), !addPrefix);
                if (pred.startsWith("rdf:") && !pred.equals(RDF_TYPE)) {
                    return true;
                }
            }
        //}
        return false;
    }

    @Override
    void header(StringBuilder bb) {
        link(bb);
        bb.append(nsm.toString(PREFIX, false, false));
//        if (isRdfPrefixNeeded()) {
//            bb.append(nsm.toString(PREFIX, false, false));
//        } else {
//            // Si le préfixe rdf: n'est pas nécessaire, supprimez-le de la sortie
//            bb.append(nsm.toString(PREFIX, false, false).replaceAll("@prefix rdf:.*\n", ""));
//        }
    }

    void link(StringBuilder bb) {
        if (getMappings() != null && !getMappings().getLinkList().isEmpty()) {
            bb.append("#").append(NL);

            for (String link : getMappings().getLinkList()) {
                bb.append("# link href = ").append(link).append(NL);
            }

            bb.append("#").append(NL);
        }
    }

    // pprint edges where node is subject
    // when isGraph == true consider edges in named graph gNode
    // otherwise consider all edges
    void print(Node gNode, Node node) {
        boolean first = true;
        boolean annotation = false;
        boolean isBlankNode = useCompactBlankNodeSyntax && node.getValue().isBlank();

        for (Edge edge : getEdges(gNode, node)) {
            if (edge != null && accept(edge) && edge.isAsserted()) {
                if (tripleCounter++ > getNbTriple()) {
                    break;
                }
                if (first) {
                    first = false;
//                    if (isBlankNode) {
//                        sdisplay("[");
//                    } 
//                    else 
                    {
                        subject(edge);
                        sdisplay(SPACE);
                    }
                } else {
                    sdisplay(PV);
                    sdisplay(NL);
                    sdisplay(TAB); // Déplacer cette ligne ici pour l'indentation
                }

                edge(edge);
            }
        }

        if (!first) {
//            if (isBlankNode) {
//                sdisplay("]");
//            }
            sdisplay(DOT);
            sdisplay(NL);
            sdisplay(NL);
        }
    }

    // iterate edges where node is subject
    // when isGraph == true consider edges in gNode named graph
    Iterable<Edge> getEdges(Node gNode, Node node) {
        if (isGraph) {
            return graph.getNodeEdges(gNode, node);
        } else {
            return graph.getNodeEdges(node);
        }
    }

    void subject(Edge ent) {
        if (useCompactBlankNodeSyntax && ent.getSubjectValue().isBlank()) {
            sdisplay("[");
        } else {
            node(ent.getSubjectValue());
        }
    }

    void predicate(Node node) {
        String pred = nsm.toPrefix(node.getLabel(), !addPrefix);
        if (pred.equals(RDF_TYPE)) {
            sdisplay("a");
        } else if (pred.equals(node.getLabel())) { // Si l'URI n'est pas abrégée
            uri(node.getLabel()); // Utiliser la méthode uri pour ajouter des chevrons si nécessaire
        } else { // Si l'URI est abrégée
            sdisplay(pred);
        }
    }

    void node(Node node) {
        node(node, false);
    }

    void node(Node node, boolean rec) {
        IDatatype dt = node.getValue();
        if (dt.isTripleWithEdge()) {
            // rdf star nested triple
            nestedTriple(node, dt.getEdge(), rec);
        } else if (dt.isLiteral()) {
            sdisplay(dt.toSparql(true, false, nsm));
        } else if (dt.isBlank()) {
            sdisplay(dt.getLabel());
        } else {
            uri(dt.getLabel());
        }
    }

    // node is triple reference of edge
    // node is subject/object
    void triple(Node node, Edge edge) {
        triple(node, edge, false);
    }

    void triple(Node node, Edge edge, boolean rec) {
        nestedTriple(node, edge, rec);
    }

    // node is triple reference of edge
    // node is subject/object
    void nestedTriple(Node node, Edge edge, boolean rec) {
        sdisplay("<<");
        basicTriple(node, edge, rec);
        sdisplay(">>");
    }

    void basicTriple(Node node, Edge edge, boolean rec) {
        node(edge.getSubjectNode(), true);
        sdisplay(SPACE);
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        node(edge.getObjectNode(), true);
    }

    // void triple2(Node node, Edge edge, boolean rec) {
    // if (edge.isNested() || hasNestedTriple(edge) || rec) {
    // nestedTriple(node, edge, rec);
    // } else {
    // basicTriple(node, edge, rec);
    // }
    // }
    //

    // void basicTriple(Node node, Edge edge) {
    // basicTriple(node, edge, false);
    // }

    boolean hasNestedTriple(Edge edge) {
        return edge.getSubjectValue().isTripleWithEdge() || edge.getObjectValue().isTripleWithEdge();
    }

    void uri(String label) {
        String prefixedLabel = nsm.toPrefix(label, !addPrefix);
        if (prefixedLabel.equals(label)) { // Si l'URI n'est pas abrégée
            if (!label.startsWith("<")) {
                sdisplay("<" + label + ">");
            } else {
                sdisplay(label);
            }
        } else { // Si l'URI est abrégée
            sdisplay(prefixedLabel);
        }
    }

    @Override
    void edge(Edge edge) {
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        // object triple node displayed with << >>
        node(edge.getObjectNode(), true);
    }

    boolean annotation(Edge edge) {
        return annotation(edge.getSubjectNode());
    }

    boolean annotation(Node node) {
        return node.isTripleWithEdge() &&
                node.getEdge().isAsserted() &&
                !hasNestedTriple(node.getEdge());
    }

    public Mappings getMappings() {
        return mappings;
    }

    public TripleFormat setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }

    @Override
    public TripleFormat setNbTriple(int nbTriple) {
        super.setNbTriple(nbTriple);
        return this;
    }

    public boolean isDisplayDefaultGraphURI() {
        return displayDefaultGraphURI;
    }

    public TripleFormat setDisplayDefaultGraphURI(boolean displayDefaultGraphURI) {
        this.displayDefaultGraphURI = displayDefaultGraphURI;
        return this;
    }

    public boolean isGraphQuery() {
        return graphQuery;
    }

    public TripleFormat setGraphQuery(boolean graphQuery) {
        this.graphQuery = graphQuery;
        return this;
    }

}
