package fr.inria.corese.core.print;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
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
        return ((ASTQuery) q.getAST()).getNSM();
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
        for (Node node : getNodes()) {
            print(null, node);
        }
    }

    void graphNodes() {
        for (Node gNode : graph.getGraphNodes()) {
            if (accept(gNode)) {
                sdisplay(GRAPH);
                sdisplay(SPACE);
                subject(gNode);
                sdisplay(OGRAPH);
                display();
                for (Node node : graph.getNodeGraphIterator(gNode)) {
                    print(gNode, node);
                }
                display(CGRAPH);
            }
        }
    }

    void header2(StringBuilder bb) {
        boolean first = true;
        for (String p : nsm.getPrefixSet()) {

            if (first) {
                first = false;
            } else {
                bb.append(NL);
            }

            String ns = nsm.getNamespace(p);
            //bb.append(PREFIX + SPACE + p + ": <" + toXML(ns) + "> .");
            bb.append(String.format("@prefix %s: <%s>", p, toXML(ns)));
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

        for (Edge ent : getEdges(gNode, node)) {

            if (ent != null && accept(ent)) {

                if (first) {
                    first = false;
                    subject(ent);
                } else {
                    sdisplay(PV);
                    sdisplay(NL);
                }

                edge(ent);
            }
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
        subject(ent.getNode(0));
    }

    void subject(Node node) {
        IDatatype dt0 = getValue(node);

        if (dt0.isBlank()) {
            String sub = dt0.getLabel();
            sdisplay(sub);
        } else {
            uri(dt0.getLabel());
        }

        sdisplay(SPACE);
    }

    void uri(String label) {
        String str = nsm.toPrefixURI(label);
        sdisplay(str);
    }

    void edge(Edge edge) {

        String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());

        sdisplay(pred);
        sdisplay(SPACE);

        String obj;

        IDatatype dt1 = getValue(edge.getNode(1));

        if (dt1.isLiteral()) {
            obj = dt1.toSparql();
            sdisplay(obj);
        } else if (dt1.isBlank()) {
            obj = dt1.getLabel();
            sdisplay(obj);
        } else {
            uri(dt1.getLabel());
        }

    }

    public Mappings getMappings() {
        return mappings;
    }

    public TripleFormat setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }

}
