package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

public class EdgeInv implements Edge, Entity {
	
	Edge edge;
	Entity ent;
	
	public EdgeInv(Edge e){
		edge = e;
	}
	
	public EdgeInv(Entity e){
		ent = e;
		edge = e.getEdge();
	}
	
	public String toString(){
		return "inverse(" + edge.toString() +")";
	}

	@Override
	public boolean contains(Node n) {

		return edge.contains(n);
	}

	@Override
	public int nbNode() {

		return edge.nbNode();
	}
	
//	public int nbArg() {
//
//		return edge.nbArg();
//	}
	
	public Edge getEdge(){
		return edge;
	}
	
	public Node getGraph(){
		return ent.getGraph();
	}
	
	public Entity getEntity(){
		return ent;
	}

	@Override
	public Node getEdgeNode() {

		return edge.getEdgeNode();
	}

	@Override
	public int getIndex() {

		return edge.getIndex();
	}

	@Override
	public String getLabel() {

		return edge.getLabel();
	}

	@Override
	public Node getNode(int n) {

		switch(n){
		case 0:  return edge.getNode(1);
		case 1:  return edge.getNode(0);
		default: return edge.getNode(n);
		}
	}

//	@Override
//	public boolean match(Edge edge) {
//
//		return true;
//	}

	@Override
	public void setIndex(int n) {

		
	}

	@Override
	public Node getEdgeVariable() {

		return edge.getEdgeVariable();
	}

	@Override
	public Node getNode() {

		return null;
	}

    @Override
    public Object getProvenance() {
        return ent.getProvenance();    
    }
    
    public void setProvenance(Object obj){
        ent.setProvenance(obj);
    }

}
