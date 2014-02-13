package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

public class EntityImpl implements Entity {
	Edge edge;
	Node node, graph;
	
	
	EntityImpl(Node g, Edge e){
		edge = e;
		graph = g;
	}
	
	EntityImpl(Node g, Node n){
		node = n;
		graph = g;
	}
	
	public static EntityImpl create(Node g, Edge e){
		return new EntityImpl(g, e);
	}
	
	public static EntityImpl create(Node g, Node n){
		return new EntityImpl(g, n);
	}

	@Override
	public Edge getEdge() {
		return edge;
	}

	@Override
	public Node getGraph() {
		return graph;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public Node getNode(int i) {
		if (edge != null){
			return edge.getNode(i);
		}
		return null;
	}

	@Override
	public int nbNode() {
		if (edge != null){
			return edge.nbNode();
		}
		return 0;
	}

    @Override
    public Object getProvenance() {
        return null;
        }
    
    public void setProvenance(Object obj){
        
    }

}
