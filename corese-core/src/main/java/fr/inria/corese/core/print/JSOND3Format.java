package fr.inria.corese.core.print;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import java.util.HashMap;
import java.util.Map;
import fr.inria.corese.kgram.api.core.Edge;

public class JSOND3Format extends RDFFormat {

    private static final String PREFIX = "@prefix";
    private static final String PV = " ;";
    private static final String DOT = " .";
    private static final String OPEN = "<";
    private static final String CLOSE = ">";
    private static final String GRAPH = "graph";

    private static final String OOBJ = "{";
    private static final String COBJ = "}";

    private static final String SEP = ":";
    private static final String TAB = "\t";
    private static final String OARRAY = "[";
    private static final String CARRAY = "]";
    private static final String BLANK = "_:";
    private static final String V = ", ";
    private static final String DQUOTE = "\"";

    private Integer cpt = new Integer(0);
    boolean isGraph = false;

    Map<String, Integer> nodeIndex = new HashMap<String, Integer>();

    JSOND3Format(Graph g, NSManager n) {
        super(g, n);
    }

    public static JSOND3Format create(Graph g, NSManager n) {
        return new JSOND3Format(g, n);
    }

    public static JSOND3Format create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            Query q = map.getQuery();
            NSManager nsm = q.getAST().getNSM();
            return create(g, nsm);
        }
        return create(Graph.create());
    }

    public static JSOND3Format create(Graph g) {
        return new JSOND3Format(g, NSManager.create());
    }

    public static JSOND3Format create(Mappings map, boolean isGraph) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            Query q = map.getQuery();
            NSManager nsm = q.getAST().getNSM();
            JSOND3Format t = new JSOND3Format(g, nsm);
            t.setGraph(isGraph);
            return t;
        }
        return create(Graph.create());
    }

    public static JSOND3Format create(Graph g, boolean isGraph) {
        JSOND3Format t = new JSOND3Format(g, NSManager.create());
        t.setGraph(isGraph);
        return t;
    }

    public void setGraph(boolean b) {
        isGraph = b;
    }

    public StringBuilder getStringBuilder() {
        sb = new StringBuilder();
        if (graph == null && map == null) {
            return sb;
        }

//        if (isGraph) {
//            graphNodes();
//        } else {
//            nodes();
//        }
        StringBuilder bb = new StringBuilder();

//        header(bb);
        bb.append(OOBJ);
        bb.append(NL);
//        bb.append(TAB);
        bb.append(" \"nodes\" : [ ");
        bb.append(NL);
        d3Nodes();
        bb.append(sb);

        bb.append("] ,");
        bb.append(NL);
//        bb.append(TAB);
        bb.append(" \"edges\" : [ ");
        bb.append(NL);
        d3Edges();
        bb.append(sb);

        bb.append("] ");
        bb.append(NL);
        bb.append(COBJ);
        return bb;
    }

    void d3Nodes() {
        
        for (Node node : graph.getRBNodes()) {
            int group = 1;
            if (node.isBlank()) {
                group = 0;
            } else if (node.toString().contains("/sparql")) {
                group = 2;
            }
            
            sdisplay(TAB);
            sdisplay(OOBJ);
            sdisplay("\"name\" : ");
            sdisplay(DQUOTE);
            
            sdisplay(JSONFormat.addJSONEscapes(node.toString()));
            sdisplay(DQUOTE);
            sdisplay(V);
            sdisplay("\"group\" : " + group + " ");
            sdisplay(COBJ);
            sdisplay(V);
            sdisplay(NL);

            nodeIndex.put(node.toString(), cpt);
            cpt++;
        }

        for (Node node : graph.getLiteralNodes()) {
//        for (Entity e :  graph.getRBNodes()) {
            sdisplay(TAB);
            sdisplay(OOBJ);
            sdisplay("\"name\" : ");
            sdisplay(DQUOTE);
            
            sdisplay(JSONFormat.addJSONEscapes(node.toString()));
            sdisplay(DQUOTE);
            sdisplay(V);
            sdisplay("\"group\" : 3 ");
            sdisplay(COBJ);
            sdisplay(V);
            sdisplay(NL);

            nodeIndex.put(node.toString(), cpt);
            cpt++;
        }
        if (sb.toString().contains(V)) {
            sb.deleteCharAt(sb.lastIndexOf(V));
        }
    }

    void d3Edges() {
        sb = new StringBuilder();

        for (Edge e : graph.getEdges()) {

            Edge edge = e;

            sdisplay(TAB);
            sdisplay(OOBJ);
            sdisplay("\"source\" : ");
            sdisplay(nodeIndex.get(edge.getNode(0).toString()));
            sdisplay(V);
            sdisplay("\"target\" : ");
            sdisplay(nodeIndex.get(edge.getNode(1).toString()));
            sdisplay(V);
            sdisplay("\"label\" : ");
            sdisplay(DQUOTE);
            sdisplay(JSONFormat.addJSONEscapes(edge.getEdgeNode().toString()));
            sdisplay(DQUOTE);
            sdisplay(COBJ);
            sdisplay(V);
            sdisplay(NL);

        }
        if (sb.toString().contains(V)) {
            sb.deleteCharAt(sb.lastIndexOf(V));
        }
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
                sdisplay(OOBJ);
                for (Node node : graph.getNodeGraphIterator(gNode)) {
                    print(gNode, node.getNode());
                }
                display(SPACE);
                display(COBJ);
                display(V);
            }
        }
    }

    void print(Node gNode, Node node) {
        boolean first = true;

        for (Edge ent : getEdges(gNode, node)) {

            if (ent != null && accept(ent)) {

                if (first) {
                    first = false;
                    subject(ent);
                    sdisplay(NL);
                } else {
                    sdisplay(V);
                    sdisplay(NL);
                }
                edge(ent);
            }
        }

        if (!first) {
            sdisplay(COBJ);
            sdisplay(SPACE);
            sdisplay(V);
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
        sdisplay(SEP);
        sdisplay(SPACE);
        sdisplay(OOBJ);
    }

    void uri(String label) {
        String qname = nsm.toPrefix(label, true);
        if (qname.equals(label)) {
            sdisplay(DQUOTE);
            sdisplay(label);
            sdisplay(DQUOTE);
        } else {
            sdisplay(qname);
        }
    }

    void edge(Edge ent) {
        Edge edge = ent;

        String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());

        sdisplay(TAB);
        sdisplay(DQUOTE);
        sdisplay(pred);
        sdisplay(DQUOTE);

        sdisplay(SPACE);
        sdisplay(SEP);
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

}
