package fr.inria.corese.core.print;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.OWL;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * RDF Format for Graph construct-where result RDF Format for Mapping edges
 *
 * Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class RDFFormat {

    private static final String XMLDEC = "<?xml version=\"1.0\"?>";
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
    static final String INDENTATION = "  ";
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
    private int nbTriple = Integer.MAX_VALUE;

    RDFFormat(NSManager n) {
        with = new ArrayList<>();
        without = new ArrayList<>();
        nsm = n;
    }

    RDFFormat(Graph g, NSManager n) {
        this(n);
        if (g != null) {
            graph = g;
            // graph.prepare();
            g.getEventManager().start(Event.Format);
        }
    }

    RDFFormat(Graph g, Query q) {
        this(getAST(q).getNSM());
        if (g != null) {
            graph = g;
            // graph.prepare();
            g.getEventManager().start(Event.Format);
        }
        ast = getAST(q);
        query = q;
    }

    static ASTQuery getAST(Query q) {
        return q.getAST();
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

    static NSManager nsm() {
        return NSManager.create().setRecord(true);
    }

    public static RDFFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {
            return create(g, map.getQuery());
        }
        return create(map, nsm());
    }

    public static RDFFormat create(Graph g) {
        return new RDFFormat(g, nsm());
    }

    public static RDFFormat create(Mappings lm, NSManager m) {
        RDFFormat f = RDFFormat.create(m);
        for (Mapping map : lm) {
            f.add(map);
        }
        return f;
    }

    public static RDFFormat create(Mapping m) {
        return new RDFFormat(m, nsm());
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

    Iterable<Node> getSubjectNodes() {
        if (map != null) {
            return map.getMapNodes();
        }
        return graph.getSubjectNodes();
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
        Writer out = new OutputStreamWriter(fos); // , "UTF8");
        out.write(sb.toString());
        out.close();
    }

    public void write(OutputStream out) throws IOException {
        StringBuilder sb = getStringBuilder();
        out.write(sb.toString().getBytes());
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
        boolean rdf = false;

        for (String p : nsm.getPrefixSet()) {
            String ns = nsm.getNamespace(p);
            if (nsm.isDisplayable(ns)) {
                if (p.equals("rdf")) {
                    rdf = true;
                }
                if (first) {
                    first = false;
                } else {
                    bb.append(NL);
                }

                defPrefix(bb, p, ns);
            }
        }
        if (!rdf) {
            if (!first) {
                bb.append(NL);
            }
            defPrefix(bb, "rdf", NSManager.RDF);
        }
    }

    void defPrefix(StringBuilder bb, String p, String ns) {
        bb.append(INDENTATION);
        bb.append(XMLNS);
        if (!p.equals("")) {
            bb.append(":");
        }
        bb.append(p).append("='").append(toXML(ns)).append("'");
    }

    void print(Node node) {
        Iterator<Edge> it = getEdges(node).iterator();
        boolean typeSpecified = false;

        // vérifiez si rdf:type est déjà spécifié
        while (it.hasNext()) {
            Edge ent = it.next();
            if (ent != null && ent.getEdgeNode().getLabel().equals(RDF.TYPE)) {
                typeSpecified = true;
                break;
            }
        }

        it = getEdges(node).iterator(); // réinitialisez l'itérateur

        if (it.hasNext()) {
            IDatatype dt = getValue(node);
            String id = ID;
            if (dt.isBlank()) {
                id = NODEID;
            }
            String type = nsm.toPrefixXML(type(node));
            String open = INDENTATION + "<" + type;
            String close = INDENTATION + "</" + type + ">";

            if (dt.isBlank()) {
                display(open + id + toXML(node.getLabel()) + "'>");
            } else {
                display(open + id + toXML(node.getLabel()) + "'>");
            }

            for (; it.hasNext();) {
                Edge ent = it.next();
                if (ent != null && !(typeSpecified && ent.getEdgeNode().getLabel().equals(RDF.TYPE))) {
                    sb.append(INDENTATION);
                    wprint(ent);
                }
            }

            display(close);
            display();
        }
    }

    String type(Node node) {
        Node type = getType(node);
        if (type != null) {
            String name = type.getLabel();

            if (name.equals(RDFS.CLASS)) {
                return RDFSCLASS;
            } else if (name.equals(OWL.CLASS)) {
                return OWLCLASS;
            } else if (name.equals(RDF.PROPERTY)) {
                return RDFPROPERTY;
            } else {
                return name;
            }
        }
        return DESCRIPTION; // retourner "rdf:Description" si le type de noeud est null
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
                display(INDENTATION + open + LANG + dt.getLang() + "'>" + lit + close);
            } else if (dt.getDatatype() != null) {
                display(INDENTATION + open + DATATYPE + dt.getDatatypeURI() + "'>" + lit + close);
            } else {
                display(INDENTATION + open + ">" + lit + close);
            }
        } else {
            String uri = toXML(dt.getLabel());
            String id = RESOURCE;
            if (dt.isBlank()) {
                id = NODEID;
            }
            display(INDENTATION + open + id + uri + "' />");
        }
    }

    String toXML(String str) {
        return StringEscapeUtils.escapeXml(str);
    }

    IDatatype getValue(Node node) {
        return node.getValue();
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

    public int getNbTriple() {
        return nbTriple;
    }

    public RDFFormat setNbTriple(int nbTriple) {
        this.nbTriple = nbTriple;
        return this;
    }
}
