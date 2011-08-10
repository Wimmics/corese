package fr.inria.edelweiss.kgraph.core;


import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Concrete Edge
 * predicate in member Node
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeCore extends EdgeGraph {
	
	protected Node predicate;
	
	public EdgeCore(){
		
	}
	
	EdgeCore(Node g, Node subject, Node pred, Node object){
		setNode(0, subject);
		setNode(1, object);
		setEdgeNode(pred);
		setGraph(g);
	}
	
	public static EdgeCore create(Node g, Node subject, Node pred, Node object){
		return new EdgeCore(g, subject, pred, object);
	}
	
	public static EdgeCore create(Node g, Node subject, Node pred, Node object, Node arg){
		EdgeCore e = new EdgeCore(g, subject, pred, object);
		e.add(arg);
		return e;
	}
	
	
	public  EdgeCore copy(){
		return new EdgeCore(getGraph(), getNode(0), getEdgeNode(), getNode(1));
	}

	@Override
	public Node getEdgeNode() {
		// TODO Auto-generated method stub
		return predicate;
	}
	
	public void setEdgeNode(Node node){
		predicate = node;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return predicate.getLabel();
	}



}
