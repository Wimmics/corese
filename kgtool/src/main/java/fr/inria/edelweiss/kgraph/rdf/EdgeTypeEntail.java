package fr.inria.edelweiss.kgraph.rdf;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;

/**
 * Edge class where the  property and graph node are in the class
 * e.g. rdf:type
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class EdgeTypeEntail extends EdgeImpl {
	protected static Node predicateNode, graph;

	
	public EdgeTypeEntail(){}
	
	 EdgeTypeEntail (Node g, Node subject, Node pred, Node object){
		setNode(0, subject);
		setNode(1, object);
		setEdgeNode(pred);
		setGraph(g);
	}
	 
	public static EdgeTypeEntail create(Node g, Node subject, Node pred, Node object){
		return new EdgeTypeEntail(g, subject, pred, object);
	}
	
	public Node getEdgeNode() {
		return predicateNode;
	}
	
	public void setEdgeNode(Node node){
		predicateNode = node;
	}
	
	public String getLabel() {
		return predicateNode.getLabel();
	}
	
	public Node getGraph() {
		return graph;
	}
	
	public void setGraph(Node gNode){
		graph =  gNode;
	}
	
}
