package fr.inria.edelweiss.kgtool.load;

import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import static fr.inria.edelweiss.kgtool.load.AbstractCoreseSink.NON_LITERAL;
import java.util.ArrayList;
import java.util.Hashtable;
import org.semarglproject.vocab.RDF;

/**
 * Delegate class to extend AbstractCoreseSink and implement interface Creator
 * This class aims to implement the method for adding triples to graph
 * 
 * @author Fuqi Song, wimmics inria i3s
 * @date Jan 28 2014 new
 */
public class RDFaLoaderDelegate extends AbstractCoreseSink{

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

    public RDFaLoaderDelegate(Graph graph) {
        this.graph = graph;
        this.blank = new Hashtable<String, String>();
        nsm = NSManager.create();
        this.stack = new Stack();
    }

    public static RDFaLoaderDelegate create(Graph graph) {
        return new RDFaLoaderDelegate(graph);
    }

    @Override
    protected void addTriple(String subj, String pred, String obj, String lang, String type, int literalType) {
        if (source == null) {
            source = graph.addGraph(Entailment.DEFAULT);
        }

        Node s = getSubject(subj);
        Node p = getProperty(pred);
        Node o = null;
        switch (literalType) {
            case NON_LITERAL:
                o = getNode(obj);
                break;
            case PLAIN_LITERAL:
                o = getLiteral(pred, obj, lang, null);
                break;
            case TYPED_LITERAL:
                o = getLiteral(pred, obj, null, type);
                break;
            default:
                break;
        }

        EdgeImpl e = graph.create(source, s, p, o);
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

    //Generate an ID for blank node
    private String getID(String b) {
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

}
