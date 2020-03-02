package fr.inria.corese.core.print;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.OWL;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.RDFS;
import java.io.OutputStreamWriter;
import java.io.Writer;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * RDF Format for Graph construct-where result RDF Format for Mapping edges
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class RDFFormat {

    private static final String XMLDEC = "<?xml version=\"1.0\" ?>";
    private static final String RDF_OPEN = "<rdf:RDF";
    private static final String RDF_CLOSE = "</rdf:RDF>";
    static final String XMLNS = "xmlns";
    static final String DESCRIPTION = "rdf:Description";
    static final String ID = " rdf:about='";
    static final String NODEID = " rdf:nodeID='";
    static final String RESOURCE = " rdf:resource='";
    static final String LANG = " xml:lang='";
    static final String DATATYPE = " rdf:datatype='";
    static final String RDFSCLASS = "rdfs:Class";
    static final String RDFPROPERTY = "rdf:Property";
    static final String OWLCLASS = "owl:Class";
    static final String SPACE = " ";
    static final String NL = System.getProperty("line.separator");
    private static final String OCOM = "<!--";
    private static final String CCOM = "-->";
    private static final String LT = "<";
    private static final String XLT = "&lt;";
    private static final String AMP = "&(?!amp;)";
    private static final String XAMP = "&amp;";
    Graph graph;
    Mapper map;
    NSManager nsm;
    StringBuilder sb;
    Query query;
    ASTQuery ast;
    List<String> with, without;

    RDFFormat(NSManager n) {
        with = new ArrayList<String>();
        without = new ArrayList<String>();
        nsm = n;
    }

    RDFFormat(Graph g, NSManager n) {
        this(n);
        if (g != null) {
            graph = g;
            //graph.prepare();
            g.getEventManager().start(Event.Format);
        }
    }

    RDFFormat(Graph g, Query q) {
        this(getAST(q).getNSM());
        if (g != null) {
            graph = g;
            //graph.prepare();
            g.getEventManager().start(Event.Format);
        }
        ast = getAST(q);
        query = q;
    }

    static ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }

    RDFFormat(Mapping m, NSManager n) {
        this(n);
        add(m);
    }

    public static RDFFormat create(Graph g, NSManager n) {
        return new RDFFormat(g, n);
    }

    public static RDFFormat create(Graph g, Query q) {
        return new RDFFormat(g, q);
    }

    public static RDFFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            return create(g, map.getQuery());
        }
        return create(map, NSManager.create());
    }

    public static RDFFormat create(Graph g) {
        return new RDFFormat(g, NSManager.create());
    }

    public static RDFFormat create(Mappings lm, NSManager m) {
        RDFFormat f = RDFFormat.create(m);
        for (Mapping map : lm) {
            f.add(map);
        }
        return f;
    }

    public static RDFFormat create(Mapping m) {
        return new RDFFormat(m, NSManager.create());
    }

    public static RDFFormat create(Mapping m, NSManager n) {
        if (n == null) {
            return create(m);
        }
        return new RDFFormat(m, n);
    }

    public static RDFFormat create(NSManager n) {
        return new RDFFormat(n);
    }

    public void add(Mapping m) {
        if (map == null) {
            map = Mapper.create();
        }
        map.add(m);
    }

    public void with(String name) {
        with.add(name);
    }

    public void without(String name) {
        without.add(name);
    }

    Iterable<Node> getNodes() {
        if (map != null) {
            return map.getMapNodes();
        }
        return graph.getRBNodes();
    }

    Iterable<Edge> getEdges(Node node) {
        if (map != null) {
            return map.getMapEdges(node);
        }
        return graph.getNodeEdges(node);
    }

    Node getType(Node node) {
        if (map != null) {
            return map.getMapType(node);
        }
        Node type = graph.getPropertyNode(RDF.TYPE);
        if (type == null) {
            return null;
        }
        Edge edge = graph.getEdge(type, node, 0);
        if (edge == null) {
            return null;
        }
        return edge.getNode(1);
    }

    @Override
    public String toString() {
        StringBuilder bb = getStringBuilder();
        return bb.toString();
    }

    public void write(String name) throws IOException {
        StringBuilder sb = getStringBuilder();
        FileOutputStream fos = new FileOutputStream(name);
        Writer out = new OutputStreamWriter(fos); //, "UTF8");
        out.write(sb.toString());
        out.close();
    }

    public StringBuilder getStringBuilder() {
        sb = new StringBuilder();
        if (graph == null && map == null) {
            return sb;
        }

        error();

        for (Node node : getNodes()) {
            print(node);
        }

        StringBuilder bb = new StringBuilder();

        bb.append(XMLDEC);
        bb.append(NL);
        bb.append(RDF_OPEN);
        bb.append(NL);
        header(bb);
        bb.append(">");
        bb.append(NL);
        bb.append(NL);
        bb.append(sb);
        bb.append(RDF_CLOSE);

        return bb;
    }

    void header(StringBuilder bb) {
        boolean first = true;
        for (String p : nsm.getPrefixSet()) {

            if (first) {
                first = false;
            } else {
                bb.append(NL);
            }

            String ns = nsm.getNamespace(p);

            bb.append(XMLNS);
            if (!p.equals("")) {
                bb.append(":");
            }
            bb.append(p).append("='").append(toXML(ns)).append("'");
        }
    }

    void print(Node node) {

        Iterator<Edge> it = getEdges(node).iterator();

        if (it.hasNext()) {

            IDatatype dt = getValue(node);

            String id = ID;
            if (dt.isBlank()) {
                id = NODEID;
            }
            String type = type(node);

            String open = "<" + type;
            String close = "</" + type + ">";

            if (dt.isBlank()) {
                display(open + id + toXML(node.getLabel()) + "'>");
                //display(open + ">");
            } else {
                display(open + id + toXML(node.getLabel()) + "'>");
            }

            for (; it.hasNext();) {
                Edge ent = it.next();
                if (ent != null) {
                    wprint(ent);
                }
            }

            display(close);
            display();
        }
    }

    String type(Node node) {
        String open = DESCRIPTION;
        Node type = getType(node);
        if (type != null) {
            String name = type.getLabel();
            if (name.equals(RDFS.CLASS)) {
                open = RDFSCLASS;
            } else if (name.equals(OWL.CLASS)) {
                open = OWLCLASS;
            } else if (name.equals(RDF.PROPERTY)) {
                open = RDFPROPERTY;
            }
        }
        return open;
    }

    void wprint(Edge ent) {
        if (accept(ent)) {
            edge(ent);
        }
    }

    boolean accept(Edge ent) {
        return accept(ent.getGraph());
    }

    boolean accept(Node gname) {
        if (without.contains(gname.getLabel())) {
            return false;
        }

        if (with.size() > 0) {
            if (!with.contains(gname.getLabel())) {
                return false;
            }
        }

        return true;
    }

    void edge(Edge ent) {
        Edge edge = ent;
        String pred = nsm.toPrefixXML(edge.getEdgeNode().getLabel());
        IDatatype dt = getValue(edge.getNode(1));
        String open = "<" + pred;
        String close = "</" + pred + ">";

        if (dt.isLiteral()) {

            String lit = toXML(dt.getLabel());

            if (dt.hasLang()) {
                display(SPACE + open + LANG + dt.getLang() + "'>" + lit + close);
            } else if (dt.getDatatype() != null) {
                display(SPACE + open + DATATYPE + dt.getDatatypeURI() + "'>" + lit + close);
            } else {
                display(SPACE + open + ">" + lit + close);
            }
        } else {
            String uri = toXML(dt.getLabel());
            String id = RESOURCE;
            if (dt.isBlank()) {
                id = NODEID;
            }
            display(SPACE + open + id + uri + "'/>");
        }
    }

    String toXML(String str) {
        return StringEscapeUtils.escapeXml(str);
    }

    IDatatype getValue(Node node) {
        return (IDatatype) node.getValue();
    }

    void display(String mes, Object obj) {
        sb.append(mes);
        sb.append(obj);
        sb.append(NL);
    }

    void display(Object obj) {
        sb.append(obj);
        sb.append(NL);
    }

    void sdisplay(Object obj) {
        sb.append(obj);
    }

    void display() {
        sb.append(NL);
    }

    void error() {
        boolean b1 = ast != null && ast.getErrors() != null;
        boolean b2 = query != null && query.getErrors() != null;

        if (b1 || b2) {

            display(OCOM);
            if (ast.getText() != null) {
                display(ast.getText());
            }
            display();

            if (b1) {
                for (String mes : ast.getErrors()) {
                    display(mes);
                }
            }
            if (b2) {
                for (String mes : query.getErrors()) {
                    display(mes);
                }
            }
            display(CCOM);
            display();
        }
    }
}
