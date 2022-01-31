package fr.inria.corese.core.print;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Turtle Format
 * 
 * Olivier Corby, Wimmics INRIA 2013
 */
public class TripleFormat extends RDFFormat {

    static final String PREFIX = "@prefix";
    static final String PV = " ;";
    static final String DOT = " .";
    static final String OPEN = "<";
    static final String CLOSE = ">";
    static final String GRAPH = "graph";
    static final String OGRAPH = "{";
    static final String CGRAPH = "}";

    boolean isGraph = false;
    private Mappings mappings;

    TripleFormat(Graph g, NSManager n) {
        super(g, n);
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
        return  q.getAST().getNSM();
    }

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
        }
        else if (isGraph) {
            graphNodes();
        } else {
            nodes();
        }

        StringBuilder bb = new StringBuilder();
        header(bb);
        bb.append(NL);
        bb.append(NL);
        bb.append(sb);
        return bb;
    }

    void nodes() {
        for (Node node : getSubjectNodes()) {
            print(null, node);
        }
    }

    void graphNodes() {
        for (Node gNode : graph.getGraphNodes()) {
            if (accept(gNode)) {
                sdisplay(GRAPH);
                sdisplay(SPACE);
                node(gNode);
                sdisplay(OGRAPH);
                display();
                for (Node node : graph.getNodeGraphIterator(gNode)) {
                    print(gNode, node);
                }
                display(CGRAPH);
            }
        }
    }
   
    @Override
    void header(StringBuilder bb) {
        link(bb);
        bb.append(nsm.toString(PREFIX, false, false));
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

    void print(Node gNode, Node node) {
        boolean first = true;
        boolean annotation = false;
        
        for (Edge edge : getEdges(gNode, node)) {
            if (edge != null && accept(edge) && edge.isAsserted()) {
                if (first) {
                    first = false;
                    subject(edge);
                    sdisplay(SPACE);
                    if (annotation(edge)) {
                        annotation = true;
                        sdisplay("{| ");
                    }
                } else {
                    sdisplay(PV);
                    sdisplay(NL);
                }
                edge(edge);
            }
        }
        if (annotation){
            sdisplay(" |}");
        }

        if (!first) {
            sdisplay(DOT);
            sdisplay(NL);
            sdisplay(NL);
        }
    }

    Iterable<Edge> getEdges(Node gNode, Node node) {
        if (isGraph) {
            return graph.getNodeEdges(gNode, node);
        } else {
            return graph.getNodeEdges(node);
        }
    }

    void subject(Edge ent) {
        node(ent.getSubjectValue());
    }


    
    boolean hasNestedTriple(Edge edge) {
        return edge.getSubjectValue().isTripleWithEdge() || edge.getObjectValue().isTripleWithEdge();
    }
    
    void triple(Edge edge) {
        triple(edge, false);
    }
    
    void triple(Edge edge, boolean rec) {
        if (edge.isNested() || hasNestedTriple(edge) || rec) {
            sdisplay("<<");
            basicTriple(edge, rec);
            sdisplay(">>");
        } else {
            basicTriple(edge, rec);
        }
    }
    
    void basicTriple(Edge edge) {
        basicTriple(edge, false);
    }

    void basicTriple(Edge edge, boolean rec) {
        node(edge.getSubjectNode(), true);
        sdisplay(SPACE);
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        node(edge.getObjectNode(), true);
    }
       
    void predicate(Node node) {
        String pred = nsm.toPrefix(node.getLabel());
        sdisplay(pred);
    }
    
    void node(Node node) {
        node(node, false);
    }
    
    void node(Node node, boolean rec) {
        IDatatype dt = node.getValue();
        if (dt.isTripleWithEdge()) {
            triple(dt.getEdge(), rec);
        }
        else if (dt.isLiteral()) {
            sdisplay(dt.toSparql(true, false, nsm));
        } else if (dt.isBlank()) {
            sdisplay(dt.getLabel());
        } else {
            uri(dt.getLabel());
        }
    }

    void uri(String label) {
        sdisplay(nsm.toPrefixURI(label));
    }

    @Override
    void edge(Edge edge) {        
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        node(edge.getObjectNode());
    }
    
    boolean annotation(Edge edge) {
        return annotation(edge.getSubjectNode());
    }
    
    boolean annotation(Node node) {
        return node.isTripleWithEdge() && 
                node.getEdge().isAsserted() && 
                ! hasNestedTriple(node.getEdge());
    }

    public Mappings getMappings() {
        return mappings;
    }

    public TripleFormat setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }

}
