package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Meta Producer that manages several Producer
 * Uses a generic MetaIterator that iterates over Producer iterators
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class MetaProducer implements Producer, Iterable<Producer> {
	
	Producer  producer;
	List<Producer> lProducer;

	protected MetaProducer() {
		lProducer = new ArrayList<Producer>();
	}
	
	public void setMode(int n){
            producer.setMode(n);
	}
        
        @Override
        public int getMode() {
            return producer.getMode();
        }
	
	public Iterator<Producer> iterator(){
		return getProducerList().iterator();
	}
	
	public Producer getProducer(){
		return producer;
	}
        
        @Override
        public Query getQuery(){
            return producer.getQuery();
        }
	
	public static MetaProducer create(){
		return new MetaProducer();
	}
	
	
	/**
	 * add and get must be synchronized
	 * That is, there should not happen a query and a add in parallel
	 */
	public void add(Producer p){
		if (producer == null) producer = p;
		getProducerList().add(p);
	}
	
	List<Producer> getProducerList(){
		return lProducer;
	}
	
	
	
	public List<Producer> getProducers(){
		return getProducerList();
	}
	
	public void init(int nbNode, int nbEdge){
		for (Producer p : getProducerList()){
			p.init(nbNode, nbEdge);
		}
	}
	
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge,  Environment env){
		MetaIterator<Entity> meta = null;
		for (Producer p : getProducerList()){
			meta = add(meta, p.getEdges(gNode, from, edge, env));
		}
		return meta;
	}
	
	MetaIterator<Entity> add(MetaIterator<Entity> meta, Iterable<Entity> it){
		MetaIterator<Entity> m = new MetaIterator<Entity>(it);
		if (meta == null) meta = m;
		else meta.next(m);
		return meta;
	}
	
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node node,  Environment env){
		MetaIterator<Entity> meta = null;
		for (Producer p : getProducerList()){
			meta = add(meta, p.getNodes(gNode, from, node, env));
		}
		return meta;
	}
	
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env){
		for (Producer p : getProducerList()){
			if (p.isGraphNode(gNode, from, env)){
				return true;
			}
		}
		return false;
	}
	
	public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env){
		MetaIterator<Node> meta = null;
		for (Producer p : getProducerList()){
			Iterable<Node> it = p.getGraphNodes(gNode, from, env);
			MetaIterator<Node> m = new MetaIterator<Node>(it);
			if (meta == null) meta = m;
			else meta.next(m);
		}
		return meta;
	}
	
	
	/**
	 * PATH
	 */
	
	public void initPath(Edge edge, int index){
		for (Producer p : getProducerList()){
			p.initPath(edge, index);
		}
	}

	
	
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, 
			Regex exp, Node src, Node start, int index){
		MetaIterator<Entity> meta = null;
		for (Producer p : getProducerList()){
			meta = add(meta, p.getEdges(gNode, from, qEdge, env, exp, src, start, index));
		}
		return meta;
	}
	
	
	@Override
	// TODO: eliminate duplicate nodes from different Producers
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge edge,  Environment env, List<Regex> list, 
			int index){
		MetaIterator<Entity> meta = null;
		for (Producer p : getProducerList()){
			meta = add(meta, p.getNodes(gNode, from, edge, env, list, index));
		}
		return meta;
	}
	

	public Node getNode(Object value) {
		return producer.getNode(value);
	}

	public Mappings map(List<Node> lNodes, Object object) {
		return producer.map(lNodes, object);
	}

	public List<Node> toNodeList(Object value) {
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
    public Object getGraph() {
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
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
        Mappings meta = new Mappings();
        for (Producer p : getProducerList()){
                meta.add(p.getMappings(gNode, from, exp, env));
        }
        return meta;
    }


    @Override
    public Object getValue(Object value) {
        return producer.getValue(value);
    }

    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        return producer.getDatatypeValue(value);
    }
    
    @Override 
    public Entity copy(Entity ent){
        return producer.copy(ent);
    }

	@Override
	public void close() {

	}

}
