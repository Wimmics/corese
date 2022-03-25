package fr.inria.corese.core.load;

import java.util.Hashtable;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import static fr.inria.corese.core.load.Load.IMPORTS;
import fr.inria.corese.kgram.api.core.Edge;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Graph creation Methods are public, Design to be refined
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class BuildImpl extends CreateTriple 
        implements Build, StatementHandler, org.xml.sax.ErrorHandler {


    Graph graph;
    private Node graphNode;
    Hashtable<String, String> blank;

    private String resource, namedGraphURI;
    private Node node;
    private Load load;

    public BuildImpl() {
    }

    BuildImpl(Graph g, Load ld) {
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
            Edge edge = getEdge(getGraphNode(), subject, predicate, value);
            process(getGraphNode(), edge);
        }
    }

    @Override
    public void statement(AResource subj, AResource pred, AResource obj) {
        if (accept(pred.getURI())) {
            Node subject = getSubject(subj);
            Node predicate = getProperty(pred);
            Node value = getNode(obj);
            Edge edge = getEdge(getGraphNode(), subject, predicate, value);
            process(getGraphNode(), edge);
            
            if (pred.getURI().equals(IMPORTS)) {
                getLoad().imports(obj.getURI());
            }
        }
    }

    @Override
    public void setSource(String src) {
        basicSetSource(src);
    }
    
    public String getSource() {
        return getNamedGraphURI();
    }
    
        
    void basicSetSource(String src) {
        if (getNamedGraphURI() == null || !src.equals(getNamedGraphURI())) {
            setNamedGraphURI(src);
            setGraphNode(addGraph(src));
        }
    }
    
    @Override
    public void start() {
        super.start();
        blank.clear();
    }

    public void process(Node gNode, Edge edge) {
        add(edge);
    }

    public Edge getEdge(Node source, Node subject, Node predicate, Node value) {
        if (source == null) {
            source = addDefaultGraphNode();
        }
        return create(source, subject, predicate, value);
    }

    public Node getLiteral(AResource pred, ALiteral lit) {
        String lang = lit.getLang();
        String datatype = lit.getDatatypeURI();
        if (lang == "") {
            lang = null;
        }
        return addLiteral(pred.getURI(), lit.toString(), datatype, lang);
    }

    public Node getProperty(AResource res) {
        return addProperty(res.getURI());
    }

    Node getSubject(AResource res) {
        if (res.isAnonymous()) {
            return addBlank(getID(res.getAnonymousID()));
        } else {
            return getResource(res.getURI());
        }
    }

    public Node getNode(AResource res) {
        if (res.isAnonymous()) {
            return addBlank(getID(res.getAnonymousID()));
        } else {
            return addResource(res.getURI());
        }
    }

    Node getResource(String uri) {
        if (resource == null || !resource.equals(uri)) {
            resource = uri;
            node = addResource(uri);
        }
        return node;
    }

    public String getID(String b) {
        String id = blank.get(b);
        if (id == null) {
            id = newBlankID();
            blank.put(b, id);
        }
        return id;
    }

 

    public int nbBlank() {
        return blank.size();
    }

    public Node getGraphNode() {
        return graphNode;
    }

    public void setGraphNode(Node graphNode) {
        this.graphNode = graphNode;
    }

    public String getNamedGraphURI() {
        return namedGraphURI;
    }

    public void setNamedGraphURI(String namedGraphURI) {
        this.namedGraphURI = namedGraphURI;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }
    
    
       
    @Override
    public void error(SAXParseException exception) throws SAXException {
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
    }
    

}
