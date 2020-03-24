package fr.inria.corese.core.load;

import java.util.Hashtable;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Graph creation Methods are public, Design to be refined
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class BuildImpl extends CreateTriple implements Build {


    Graph graph;
    Node gnode;
    Hashtable<String, String> blank;

    private String resource, source;
    private Node node;
    Load load;

    public BuildImpl() {
    }

    public BuildImpl(Graph g, Load ld) {
        super(g, ld);
        graph = g;
        blank = new Hashtable<>();
        load = ld;
    }

    public static BuildImpl create(Graph g, Load ld) {
        return new BuildImpl(g, ld);
    }

    @Override
    public void statement(AResource subj, AResource pred, ALiteral lit) {
        if (accept(pred.getURI())) {
            Node subject = getSubject(subj);
            Node predicate = getProperty(pred);
            Node value = getLiteral(pred, lit);
            if (value == null) {
                return;
            }
            Edge edge = getEdge(gnode, subject, predicate, value);
            process(gnode, edge);
        }
    }

    @Override
    public void statement(AResource subj, AResource pred, AResource obj) {
        if (accept(pred.getURI())) {
            Node subject = getSubject(subj);
            Node predicate = getProperty(pred);
            Node value = getNode(obj);
            Edge edge = getEdge(gnode, subject, predicate, value);
            process(gnode, edge);
        }
    }

    @Override
    public void setSource(String src) {
        if (source == null || !src.equals(source)) {
            source = src;
            gnode = graph.addGraph(src);
        }
    }
    
    @Override
    public void start() {
        super.start();
        blank.clear();
    }

    @Override
    public void setSkip(boolean b) {
        setSkip(b);
    }

  

    public void process(Node gNode, Edge edge) {
        add(edge);
    }

    public Edge getEdge(Node source, Node subject, Node predicate, Node value) {
        if (source == null) {
            source = graph.addDefaultGraphNode();
        }

        return graph.create(source, subject, predicate, value);

    }

    public Node getLiteral(AResource pred, ALiteral lit) {
        String lang = lit.getLang();
        String datatype = lit.getDatatypeURI();
        if (lang == "") {
            lang = null;
        }
        return graph.addLiteral(pred.getURI(), lit.toString(), datatype, lang);
    }

    public Node getProperty(AResource res) {
        return graph.addProperty(res.getURI());
    }

    Node getSubject(AResource res) {
        if (res.isAnonymous()) {
            return graph.addBlank(getID(res.getAnonymousID()));
        } else {
            return getResource(res.getURI());
        }
    }

    public Node getNode(AResource res) {
        if (res.isAnonymous()) {
            return graph.addBlank(getID(res.getAnonymousID()));
        } else {
            return graph.addResource(res.getURI());
        }
    }

    Node getResource(String uri) {
        if (resource == null || !resource.equals(uri)) {
            resource = uri;
            node = graph.addResource(uri);
        }
        return node;
    }

    public String getID(String b) {
        String id = blank.get(b);
        if (id == null) {
            id = graph.newBlankID();
            blank.put(b, id);
        }
        return id;
    }

 

    public int nbBlank() {
        return blank.size();
    }

}
