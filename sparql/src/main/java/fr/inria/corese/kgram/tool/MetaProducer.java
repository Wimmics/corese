package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Meta Producer that manages several Producer Uses a generic MetaIterator that
 * iterates over Producer iterators
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class MetaProducer implements Producer, Iterable<Producer> {

    Producer producer;
    List<Producer> lProducer;

    protected MetaProducer() {
        lProducer = new ArrayList<>();
    }

    @Override
    public void setMode(int n) {
        producer.setMode(n);
    }

    @Override
    public int getMode() {
        return producer.getMode();
    }

    @Override
    public Iterator<Producer> iterator() {
        return getProducerList().iterator();
    }

    public Producer getProducer() {
        return producer;
    }

    @Override
    public Query getQuery() {
        return producer.getQuery();
    }

    public static MetaProducer create() {
        return new MetaProducer();
    }

    /**
     * add and get must be synchronized That is, there should not happen a query
     * and a add in parallel
     */
    public void add(Producer p) {
        if (producer == null) {
            producer = p;
        }
        getProducerList().add(p);
    }

    List<Producer> getProducerList() {
        return lProducer;
    }

    public List<Producer> getProducers() {
        return getProducerList();
    }

    @Override
    public void init(Query q) {
        for (Producer p : getProducerList()) {
            p.init(q);
        }
    }

    @Override
    public void start(Query q) {
        producer.start(q);
    }

    @Override
    public void finish(Query q) {
        producer.start(q);
    }

    @Override
    public Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge edge, Environment env) {
        MetaIterator<Edge> meta = null;
        for (Producer p : getProducerList()) {
            meta = add(meta, p.getEdges(gNode, from, edge, env));
        }
        return meta;
    }

    MetaIterator<Edge> add(MetaIterator<Edge> meta, Iterable<Edge> it) {
        MetaIterator<Edge> m = new MetaIterator<>(it);
        if (meta == null) {
            meta = m;
        } else {
            meta.next(m);
        }
        return meta;
    }

    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        for (Producer p : getProducerList()) {
            if (p.isGraphNode(gNode, from, env)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
        MetaIterator<Node> meta = null;
        for (Producer p : getProducerList()) {
            Iterable<Node> it = p.getGraphNodes(gNode, from, env);
            MetaIterator<Node> m = new MetaIterator<>(it);
            if (meta == null) {
                meta = m;
            } else {
                meta.next(m);
            }
        }
        return meta;
    }

    /**
     * PATH
     */
    @Override
    public void initPath(Edge edge, int index) {
        for (Producer p : getProducerList()) {
            p.initPath(edge, index);
        }
    }

    @Override
    public Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env,
            Regex exp, Node src, Node start, int index) {
        MetaIterator<Edge> meta = null;
        for (Producer p : getProducerList()) {
            meta = add(meta, p.getEdges(gNode, from, qEdge, env, exp, src, start, index));
        }
        return meta;
    }

    @Override
    public Iterable<Node> getNodes(Node gNode, List<Node> from, Edge edge,
            Environment env, List<Regex> exp, int index) {
        MetaIterator<Node> meta = null;
        for (Producer p : getProducerList()) {
            Iterable<Node> m = p.getNodes(gNode, from, edge, env, exp, index);
            if (meta == null) {
                meta = new MetaIterator<>(m);
            } else {
                meta.next(m);
            }
        }
        return meta;
    }

    @Override
    public Node getNode(Object value) {
        return producer.getNode(value);
    }

    @Override
    public Mappings map(List<Node> lNodes, IDatatype object) {
        return producer.map(lNodes, object);
    }
    
    @Override
    public Mappings map(List<Node> lNodes, IDatatype object, int n) {
        return producer.map(lNodes, object, n);
    }

    @Override
    public List<Node> toNodeList(IDatatype value) {
        return producer.toNodeList(value);
    }

    @Override
    public boolean isBindable(Node node) {
        // TODO Auto-generated method stub
        return producer.isBindable(node);
    }

    @Override
    public boolean isProducer(Node node) {
        return producer.isProducer(node);
    }

    @Override
    public Producer getProducer(Node node, Environment env) {
        return producer.getProducer(node, env);
    }

    @Override
    public Graph getGraph() {
        return producer.getGraph();
    }

    @Override
    public void setGraphNode(Node n) {
        producer.setGraphNode(n);
    }

    @Override
    public Node getGraphNode() {
        return producer.getGraphNode();
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) throws SparqlException {
        Mappings meta = new Mappings();
        for (Producer p : getProducerList()) {
            meta.add(p.getMappings(gNode, from, exp, env));
        }
        return meta;
    }

    @Override
    public IDatatype getValue(Object value) {
        return producer.getValue(value);
    }

    @Override
    public IDatatype getDatatypeValue(Object value) {
        return producer.getDatatypeValue(value);
    }

    @Override
    public Edge copy(Edge ent) {
        return producer.copy(ent);
    }

    @Override
    public void close() {

    }

    @Override
    public DatatypeValueFactory getDatatypeValueFactory() {
        return producer.getDatatypeValueFactory();
    }

    @Override
    public String blankNode() {
        return getProducer().blankNode();
    }

}
