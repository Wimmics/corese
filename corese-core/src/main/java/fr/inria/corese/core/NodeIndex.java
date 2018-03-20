package fr.inria.corese.core;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.tool.MetaIterator;

/**
 * 
 * Manage Nodes of a graph, for each graph name
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class NodeIndex {
	
	class NodeTable extends Hashtable<Node, Entity> {}
	
	class GraphTable extends Hashtable <Node, NodeTable> {}
		
	GraphTable graph;
	
	NodeIndex(){
		graph = new GraphTable();
	}
	
	static NodeIndex create(){
		return new NodeIndex();
	}
	
	int size(){
		return graph.size();
	}
	
	void clear(){
		graph.clear();
	}
	
	void add(Entity ent){
		Edge edge = ent.getEdge();
		for (int i=0; i<edge.nbNode(); i++){
			add(ent.getGraph(), edge.getNode(i));
		}
	}
	
	boolean elseWhere(Node gNode, Node node){
		return false;
	}
	
	void add(Node gNode, Node node){
		NodeTable gt = graph.get(gNode);
		if (gt == null){
			gt = new NodeTable();
			graph.put(gNode, gt);
		}
		if (! gt.containsKey(node)){
			gt.put(node, EntityImpl.create(gNode, node));
		}
	}
	
	Iterable<Entity> getNodes(Node gNode) {
		NodeTable gt = graph.get(gNode);
		if (gt == null) return new ArrayList<Entity>();
		return gt.values();
	}
	
	Iterable<Entity> getNodes() {
		MetaIterator<Entity> meta = null;
		for (NodeTable gt : graph.values()){
			if (meta == null) meta = new MetaIterator<Entity>(gt.values());
			else meta.next(gt.values());
		}
		if (meta == null) return new ArrayList<Entity>();
		return meta;
	}

}
