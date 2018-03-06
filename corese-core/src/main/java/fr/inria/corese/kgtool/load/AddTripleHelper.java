package fr.inria.corese.kgtool.load;

import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import java.util.ArrayList;
import java.util.Hashtable;
import org.semarglproject.vocab.RDF;

/**
 * Helper class to aid the parsers (ex.jsonld, rdfa) for adding triples to
 * corese graph
 *
 * @author Fuqi Song, wimmics inria i3s
 * @date Jan 28 2014 new
 * @date Feb 12 2014 re-factored
 */
public class AddTripleHelper implements ILoadSerialization {

    protected Graph graph;
    Node source;
    Stack stack;
    NSManager nsm;
    private String resource = null;
    private Node node = null;
    private boolean renameBlankNode = true;
    private Hashtable<String, String> blank = null;
    private int limit = Integer.MAX_VALUE;

    public void graph(String src) {
        source = graph.addGraph(src);
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
        this.blank = new Hashtable<String, String>();
        nsm = NSManager.create();
        this.stack = new Stack();
    }

    public static AddTripleHelper create(Graph graph) {
        return new AddTripleHelper(graph);
    }

    @Override
    public void addTriple(String subj, String pred, String obj, String lang, String type, int literalType, Node source) {
        if (source == null) {
            source = graph.addDefaultGraphNode();
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

        Entity e = graph.create(source, s, p, o);
        graph.addEdge(e);
    }

    // get the node of a literal according to the content, lang and type
    private Node getLiteral(String property, String value, String lang, String type) {

        type = isEmpty(type) ? null : nsm.toNamespace(type);
        lang = isEmpty(lang) ? null : lang;

        return graph.addLiteral(property, value, type, lang);
    }

    //get the node of "object"
    private Node getNode(String obj) {
        if (isBlankNode(obj)) {
            return graph.addBlank(getID(obj));
        } else {
            return graph.addResource(obj);
        }
    }

    // get the node of property (precidate)
    private Node getProperty(String pred) {
        return graph.addProperty(pred);
    }

    //get the node of subject
    private Node getSubject(String subj) {
        if (isBlankNode(subj)) {
            return graph.addBlank(getID(subj));
        } else {
            if (null == resource || !subj.equals(resource)) {
                resource = subj;
                node = graph.addResource(resource);
            }
            return node;
        }
    }

    //Check if a given string is empty (null or length ==0)
    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    //Check if a node is blank (starting with "_:")
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
                id = graph.newBlankID();
                blank.put(b, id);
            }
        }
        return id;
    }

    /**
     * Get the default graph source according to the status of graph and source
     *
     * @param graph Graph that will be filled in
     * @param source The particular name to add nodes to
     *
     * @return
     */
    public Node getGraphSource(Graph graph, String source) {
        if (source == null) {
            return graph.addDefaultGraphNode();
        } else {
            return graph.addGraph(source);
        }
    }

    public Node getGraphSource2(Graph graph, String source) {
        Node defaultGraphSource;

        if (!hasGraphsOrDefault(this.graph)) {
            defaultGraphSource = this.graph.addDefaultGraphNode();
        } else {
            if (source == null) {
                defaultGraphSource = this.graph.addDefaultGraphNode();
            } else {
                defaultGraphSource = this.graph.addGraph(source);
            }
        }
        
        return defaultGraphSource;
    }

    //check if one graph contains graphs or default graph
    private boolean hasGraphsOrDefault(Graph g) {
        boolean hasGraphs = false, hasDefault = false;
        for (Object n : g.getGraphNodes()) {
            hasGraphs = true;
            break;
        }
        hasDefault = g.getDefaultGraphNode() != null;

        return hasGraphs || hasDefault;
    }
}
