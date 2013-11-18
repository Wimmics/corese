package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.HashMap;
import java.util.Map;

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
            NSManager nsm = ((ASTQuery) q.getAST()).getNSM();
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
            NSManager nsm = ((ASTQuery) q.getAST()).getNSM();
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
        
        for (Entity e : graph.getRBNodes()) {
            Node node = e.getNode();
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
            
            sdisplay(node.toString().replace("\"", "\\\""));
            sdisplay(DQUOTE);
            sdisplay(V);
            sdisplay("\"group\" : " + group + " ");
            sdisplay(COBJ);
            sdisplay(V);
            sdisplay(NL);

            nodeIndex.put(node.toString(), cpt);
            cpt++;
        }

        for (Entity e : graph.getLiteralNodes()) {
//        for (Entity e :  graph.getRBNodes()) {
            Node node = e.getNode();
            sdisplay(TAB);
            sdisplay(OOBJ);
            sdisplay("\"name\" : ");
            sdisplay(DQUOTE);
            
            sdisplay(Constant.addEscapes(node.toString()));
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

        for (Entity e : graph.getEdges()) {

            Edge edge = e.getEdge();

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
            sdisplay(edge.getEdgeNode().toString().replace("\"", "\\\""));
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
        for (Entity ent : getNodes()) {
            Node node = ent.getNode();
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
                for (Entity ent : graph.getNodes(gNode)) {
                    Node node = ent.getNode();
                    print(gNode, node);
                }
                display(SPACE);
                display(COBJ);
                display(V);
            }
        }
    }

//    void header(StringBuilder bb) {
//        boolean first = true;
//        for (String p : nsm.getPrefixSet()) {
//
//            if (first) {
//                first = false;
//            } else {
//                bb.append(NL);
//            }
//
//            String ns = nsm.getNamespace(p);
//            bb.append(PREFIX + SPACE + p + ": <" + toXML(ns) + "> .");
//        }
//    }
    void print(Node gNode, Node node) {
        boolean first = true;

        for (Entity ent : getEdges(gNode, node)) {

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

    Iterable<Entity> getEdges(Node gNode, Node node) {
        if (isGraph) {
            return graph.getNodeEdges(gNode, node);
        } else {
            return graph.getNodeEdges(node);
        }
    }

    void subject(Entity ent) {
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

    void edge(Entity ent) {
        Edge edge = ent.getEdge();

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
