package fr.inria.edelweiss.kgraph.core;


import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Abstract Edge
 * predicate in static Node in subclasses (see package rdf)
 * This is an abstract class refined by EdgeCore
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public  class EdgeImpl implements Edge, Entity {
	public static boolean displayGraph = true;
	private static int MAX = 2;
	private static int TAGINDEX = 2;

	protected Node subject, object;
	
	public EdgeImpl(){
	}
	
	void add(Node node){
		
	}
	
	public  EdgeImpl copy(){
		return  EdgeCore.create(getGraph(), getNode(0), getEdgeNode(), getNode(1));
	}
	
	public void setNode(int i , Node node){
		switch(i){
		case 0: subject = node;
		case 1: object = node;
		}
	}
	
	public String toString(){
		String str = "";
		if (displayGraph) str += getGraph() + " " ;
		str +=  getNode(0) + " " + getEdgeNode() + " " +  getNode(1);
		return str;
	}

	@Override
	public boolean contains(Node node) {
		// TODO Auto-generated method stub
		for (int i=0; i<nbNode(); i++){
			if (getNode(i).same(node)){
				return true;
			}
		}
		return false;
	}

	@Override
	public Node getEdgeNode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setEdgeNode(Node node){
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return getEdgeNode().getLabel();
	}

	@Override
	public Node getNode(int n) {
		// TODO Auto-generated method stub
		switch (n) {
		case 0: return subject;
		case 1: return object;
		default:
			return getGraph();
		}
	}

	@Override
	public int nbNode() {
		// TODO Auto-generated method stub
		return MAX;
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
		return null;
	}
	
	public void setGraph(Node gNode){
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
