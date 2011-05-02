package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;

public class EdgeInv implements Edge {
	
	Edge edge;
	
	public EdgeInv(Edge e){
		edge = e;
	}
	
	public String toString(){
		return "inverse(" + edge.toString() +")";
	}

	@Override
	public boolean contains(Node n) {
		// TODO Auto-generated method stub
		return edge.contains(n);
	}

	@Override
	public int nbNode() {
		// TODO Auto-generated method stub
		return edge.nbNode();
	}
	
//	public int nbArg() {
//		// TODO Auto-generated method stub
//		return edge.nbArg();
//	}
	
	public Edge getEdge(){
		return edge;
	}

	@Override
	public Node getEdgeNode() {
		// TODO Auto-generated method stub
		return edge.getEdgeNode();
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return edge.getIndex();
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return edge.getLabel();
	}

	@Override
	public Node getNode(int n) {
		// TODO Auto-generated method stub
		switch(n){
		case 0:  return edge.getNode(1);
		case 1:  return edge.getNode(0);
		default: return edge.getNode(n);
		}
	}

//	@Override
//	public boolean match(Edge edge) {
//		// TODO Auto-generated method stub
//		return true;
//	}

	@Override
	public void setIndex(int n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node getEdgeVariable() {
		// TODO Auto-generated method stub
		return edge.getEdgeVariable();
	}

}
