package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Edge
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeImpl implements Edge, Entity {
	
	List<NodeImpl> nodes;
	NodeImpl predicate;
	NodeImpl graph;
	
	EdgeImpl(Node g, Node subject, Node pred, Node object){
		nodes = new ArrayList<NodeImpl>();
		nodes.add((NodeImpl)subject);
		nodes.add((NodeImpl)object);
		predicate = (NodeImpl)pred;
		graph = (NodeImpl)g;
	}
	
	public static EdgeImpl create(Node g, Node subject, Node pred, Node object){
		return new EdgeImpl(g, subject, pred, object);
	}
	
	public  EdgeImpl copy(){
		return new EdgeImpl(getGraph(), getNode(0), getEdgeNode(), getNode(1));
	}
	
	public String toString(){
		String str = "";
		str += graph + " " + nodes.get(0) + " " + predicate + " " +  nodes.get(1);
		return str;
	}

	@Override
	public boolean contains(Node node) {
		// TODO Auto-generated method stub
		for (Node n : nodes){
			if (n.same(node)){
				return true;
			}
		}
		return false;
	}

	@Override
	public Node getEdgeNode() {
		// TODO Auto-generated method stub
		return predicate;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return predicate.getLabel();
	}

	@Override
	public Node getNode(int n) {
		// TODO Auto-generated method stub
		if (n>=nbNode()) return getGraph();
		return nodes.get(n);
	}

	@Override
	public int nbNode() {
		// TODO Auto-generated method stub
		return nodes.size();
	}

	@Override
	public void setIndex(int n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Edge getEdge() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Node getGraph() {
		// TODO Auto-generated method stub
		return graph;
	}
	
	public void setGraph(Node gNode){
		graph = (NodeImpl) gNode;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getEdgeVariable() {
		// TODO Auto-generated method stub
		return null;
	}

}
