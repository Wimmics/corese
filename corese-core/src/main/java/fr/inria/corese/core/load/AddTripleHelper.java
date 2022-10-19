package fr.inria.corese.core.load;

import java.util.ArrayList;
import java.util.Hashtable;

import org.semarglproject.vocab.core.RDF;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * Helper class to aid the parsers (ex.jsonld, rdfa) for adding triples to
 * corese graph
 *
 * @author Fuqi Song, wimmics inria i3s
 * @date Jan 28 2014 new
 * @date Feb 12 2014 re-factored
 */
public class AddTripleHelper implements ILoadSerialization {
    private final static String JSONLD_BNODE_PREFIX = ":_";

    private Graph graph;
    Node source;
    Stack stack;
    NSManager nsm;
    private String resource = null;
    private Node node = null;
    private boolean renameBlankNode = true;
    private Hashtable<String, String> blank = null;
    private int limit = Integer.MAX_VALUE;

    public void graph(String src) {
        source = addGraph(src);
    }

    public void setRenameBlankNode(boolean b) {
        this.renameBlankNode = b;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isRenameBlankNode() {
        return renameBlankNode;
    }

    class Stack extends ArrayList<Node> {

        Node pop() {
            if (size() > 0) {
                return remove(size() - 1);
            }
            return null;
        }
    }

    public AddTripleHelper(Graph graph) {
        this.graph = graph;
        this.blank = new Hashtable<>();
        nsm = NSManager.create();
        this.stack = new Stack();
    }

    public static AddTripleHelper create(Graph graph) {
        return new AddTripleHelper(graph);
    }

    @Override
    public void addTriple(String subj, String pred, String obj, String lang, String type, int literalType,
            Node source) {
        if (source == null) {
            source = addDefaultGraphNode();
        }

        Node s = getSubject(subj);
        Node p = getProperty(pred);
        Node o = null;
        switch (literalType) {
            case NON_LITERAL:
                o = getNode(obj);
                break;
            case LITERAL:
                o = getLiteral(pred, obj, lang, type);
                break;
            default:
                break;
        }

        Edge e = create(source, s, p, o);
        addEdge(e);
    }

    // get the node of a literal according to the content, lang and type
    private Node getLiteral(String property, String value, String lang, String type) {

        type = isEmpty(type) ? null : nsm.toNamespace(type);
        lang = isEmpty(lang) ? null : lang;

        return addLiteral(property, value, type, lang);
    }

    // get the node of "object"
    private Node getNode(String obj) {
        if (isBlankNode(obj)) {
            return addBlank(getID(obj));
        } else {
            return addResource(obj);
        }
    }

    // get the node of property (precidate)
    private Node getProperty(String pred) {
        return addProperty(pred);
    }

    // get the node of subject
    private Node getSubject(String subj) {
        if (isBlankNode(subj)) {
            return addBlank(getID(subj));
        } else {
            if (null == resource || !subj.equals(resource)) {
                resource = subj;
                node = addResource(resource);
            }
            return node;
        }
    }

    public Node graphNode(String graphName) {
        Node graphSource;
        if (graphName.startsWith(JSONLD_BNODE_PREFIX)) {
            graphSource = addBlank(getID(graphName));
            addGraphNode(graphSource);
        } else {
            graphSource = addGraph(graphName);
        }
        return graphSource;
    }

    // Check if a given string is empty (null or length ==0)
    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    // Check if a node is blank (starting with "_:")
    private boolean isBlankNode(String node) {
        return node.startsWith(RDF.BNODE_PREFIX);
    }

    /**
     * Generate/Rename an ID for blank node
     *
     * @param b Value of BNode
     * @return Value of generated/renamed bnode
     */
    public String getID(String b) {
        String id = b;
        if (isRenameBlankNode()) {
            id = blank.get(b);
            if (id == null) {
                id = blankNode();
                blank.put(b, id);
            }
        }
        return id;
    }

    String blankNode() {
        return getGraph().newBlankID();
    }

    /**
     * Get the default graph source according to the status of graph and source
     *
     * @param graph  Graph that will be filled in
     * @param source The particular name to add nodes to
     *
     * @return
     */
    public Node getGraphSource(String source) {
        if (source == null) {
            return addDefaultGraphNode();
        } else {
            return addGraph(source);
        }
    }

    Node addGraph(String name) {
        return getGraph().addGraph(name);
    }

    Node addDefaultGraphNode() {
        return getGraph().addDefaultGraphNode();
    }

    void addGraphNode(Node node) {
        getGraph().addGraphNode(node);
    }

    Edge create(Node g, Node s, Node p, Node o) {
        return getGraph().create(g, s, p, o);
    }

    Edge addEdge(Edge e) {
        return getGraph().addEdge(e);
    }

    Node addProperty(String p) {
        return getGraph().addProperty(p);
    }

    Node addLiteral(String predicate, String value, String type, String lang) {
        return getGraph().addLiteral(predicate, value, type, lang);
    }

    Node addResource(String name) {
        return getGraph().addResource(name);
    }

    Node addBlank(String id) {
        return getGraph().addBlank(id);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

}
