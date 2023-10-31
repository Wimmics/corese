package fr.inria.corese.core.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.triple.api.Creator;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.RDFList;
import fr.inria.corese.sparql.triple.parser.Triple;

/**
 *
 * Create Edge on the fly for Turtle parser
 *
 * @author Olivier Corby, INRIA 2012
 *
 */
public class CreateImpl extends CreateTriple implements Creator {
    public static boolean USE_REFERENCE_ID = true;
    private static Logger logger = LoggerFactory.getLogger(CreateImpl.class);

    HashMap<String, String> blank;
    HashMap<String, Node> reference;
    NSManager nsm;
    Graph graph;
    Node source;
    Stack stack;
    String base;
    private boolean renameBlankNode = true;
    private String resource;
    private Node node;
    Load load;
    int count = 1;

    class Stack extends ArrayList<Node> {

        Node pop() {
            if (size() > 0) {
                return remove(size() - 1);
            }
            return null;
        }

    }

    CreateImpl(Graph g, Load ld) {
        super(g, ld);
        graph = g;
        load = ld;
        blank = new HashMap<>();
        reference = new HashMap<>();
        nsm = NSManager.create();
        stack = new Stack();
    }

    public static CreateImpl create(Graph g, Load ld) {
        return new CreateImpl(g, ld);
    }

    // init
    // TODO: check
    // public void graph(String src) {
    // source = addGraph(src);
    // }

    @Override
    public void graph(Atom src) {
        stack.add(source);
        source = addGraph(src);
    }

    @Override
    public void endGraph(Atom src) {
        source = stack.pop();
    }

    @Override
    public boolean accept(Atom subject, Atom property, Atom object) {
        return true;
    }

    @Override
    public void triple(Atom graph, Atom subject, Atom property, Atom object) {
        triple(getGraph(graph), subject, property, object);
    }

    Node getGraph(Atom graph) {
        return graph == null ? addDefaultGraphNode() : addGraph(graph);
    }

    @Override
    public void triple(Atom subject, Atom property, Atom object) {
        if (source == null) {
            source = addDefaultGraphNode();
        }
        triple(source, subject, property, object);
    }

    @Override
    public void triple(Atom property, List<Atom> termList, boolean nested) {
        if (source == null) {
            source = addDefaultGraphNode();
        }

        Node predicate = getProperty(property);

        ArrayList<Node> nodeList = new ArrayList<>();
        for (Atom at : termList) {
            Node n = getObject(at, predicate, nodeList);
            nodeList.add(n);
        }

        Edge e = create(source, predicate, nodeList, nested);
        add(e);
    }

    @Override
    public void triple(Atom property, List<Atom> termList) {
        triple(property, termList, false);
    }

    Edge triple(Node source, Atom subject, Atom property, Atom object) {
        if (accept(property.getLabel())) {
            Node s = getSubject(subject);
            Node p = getProperty(property);
            Node o;
            if (object.isLiteral()) {
                o = getLiteral(property, object.getConstant());
            } else {
                o = getNode(object);
            }

            Edge e = create(source, s, p, o);
            add(e);
            parseImport(property, object);
            return e;
        }
        return null;
    }

    void parseImport(Atom property, Atom object) {
        if (property.getLongName() != null && property.getLongName().equals(Load.IMPORTS)
                && !Property.booleanValue(Value.DISABLE_OWL_AUTO_IMPORT)) {
            try {
                load.parseImport(object.getLongName());
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public void list(RDFList l) {
        for (Exp exp : l.getBody()) {
            if (exp.isTriple()) {
                Triple t = exp.getTriple();
                triple(t.getSubject(), t.getProperty(), t.getObject());
            }
        }
    }

    Node getLiteral(Atom pred, Constant lit) {
        if (lit.getDatatypeValue().isList()) {
            return addNode(lit);
        }
        String lang = lit.getLang();
        String datatype = nsm.toNamespace(lit.getDatatype());
        if (lang == "") {
            lang = null;
        }
        return addLiteral(pred.getLabel(), lit.getLabel(), datatype, lang);
    }

    Node getLiteral(Constant lit) {
        if (lit.getDatatypeValue().isList()) {
            return addNode(lit);
        }
        return getLiteralBasic(lit);
    }

    Node getLiteralBasic(Constant lit) {
        String lang = lit.getLang();
        String datatype = nsm.toNamespace(lit.getDatatype());
        if (lang == "") {
            lang = null;
        }
        return addLiteral(lit.getLabel(), datatype, lang);
    }

    Node getObject(Atom object) {
        return getObject(object, null, null);
    }

    Node getObject(Atom object, Node predicate, List<Node> nodeList) {
        Node o;
        if (object.isLiteral()) {
            o = getLiteral(object.getConstant());
        } else {
            o = getNode(object, predicate, nodeList);
        }
        return o;
    }

    Node getNode(Atom c) {
        return getNode(c, null, null);
    }

    Node getNode(Atom c, Node predicate, List<Node> nodeList) {
        if (c.isTriple()) {
            return getTripleReference(c, predicate, nodeList);
        }
        if (c.isBlank() || c.isBlankNode()) {
            return getBlank(c);
        } else {
            return addResource(c.getLabel());
        }
    }

    Node getBlank(Atom c) {
        Node n = addBlank(getID(c.getLabel()));
        return n;
    }

    Node getTripleReference(Atom at, Node predicate, List<Node> nodeList) {
        if (nodeList == null || nodeList.size() < 2) {
            return addTripleReference(at);
        } else {
            return addTripleReference(at, nodeList.get(0), predicate, nodeList.get(1));
        }
    }

    Node addTripleReference(Atom at) {
        Node n = reference.get(at.getLabel());
        if (n == null) {
            // should not happen because references are created
            // before they are used
            n = addTripleReference(tripleID(at.getLabel()));
            reference.put(at.getLabel(), n);
        }
        return n;
    }

    Node addTripleReference(Atom at, Node s, Node p, Node o) {
        if (USE_REFERENCE_ID) {
            // gerenare unique ID for every occurrence of same s p o
            return addTripleReferenceNew(at, s, p, o);
        } else {
            return addTripleReference(at);
        }
    }

    Node addTripleReferenceNew(Atom at, Node s, Node p, Node o) {
        Node n = reference.get(at.getLabel());
        if (n == null) {
            n = getGraph().addTripleReference(s, p, o);
            reference.put(at.getLabel(), n);
        }
        return n;

    }

    Node getSubject(Atom c) {
        return getNode(c);
    }

    String getID(String b) {
        if (isRenameBlankNode()) {
            return basicID(b);
        }
        return b;
    }

    String basicID(String b) {
        String id = blank.get(b);
        if (id == null) {
            id = newBlankID();
            blank.put(b, id);
        }
        return id;
    }

    String tripleID(String b) {
        String id = blank.get(b);
        if (id == null) {
            id = getGraph().newTripleReferenceID();
            blank.put(b, id);
        }
        return id;
    }

    @Override
    public boolean isRenameBlankNode() {
        return renameBlankNode;
    }

    @Override
    public void setRenameBlankNode(boolean renameBlankNode) {
        this.renameBlankNode = renameBlankNode;
    }

}
