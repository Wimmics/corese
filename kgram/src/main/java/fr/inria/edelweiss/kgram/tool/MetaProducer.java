package fr.inria.edelweiss.kgram.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;

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
		
	}
	
	public Iterator<Producer> iterator(){
		return lProducer.iterator();
	}
	
	public List<Producer> getProducers(){
		return lProducer;
	}
	
	public static MetaProducer create(){
		return new MetaProducer();
	}
	
	public void add(Producer p){
		if (producer == null) producer = p;
		lProducer.add(p);
	}
	
	public void init(int nbNode, int nbEdge){
		for (Producer p : lProducer){
			p.init(nbNode, nbEdge);
		}
	}
	
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge,  Environment env){
		MetaIterator<Entity> meta = null;
		for (Producer p : lProducer){
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
		for (Producer p : lProducer){
			meta = add(meta, p.getNodes(gNode, from, node, env));
		}
		return meta;
	}
	
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env){
		for (Producer p : lProducer){
			if (p.isGraphNode(gNode, from, env)){
				return true;
			}
		}
		return false;
	}
	
	public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env){
		MetaIterator<Node> meta = null;
		for (Producer p : lProducer){
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
		for (Producer p : lProducer){
			p.initPath(edge, index);
		}
	}

	
	
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, 
			Regex exp, Node src, Node start, int index){
		MetaIterator<Entity> meta = null;
		for (Producer p : lProducer){
			meta = add(meta, p.getEdges(gNode, from, qEdge, env, exp, src, start, index));
		}
		return meta;
	}
	
	
	@Override
	// TODO: eliminate duplicate nodes from different Producers
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge edge,  Environment env, List<Regex> list, 
			int index){
		MetaIterator<Entity> meta = null;
		for (Producer p : lProducer){
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

}
