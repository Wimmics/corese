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
public class EdgeStaticPG2 extends EdgeImpl {
	protected static Node predicateNode, graph;

	
	public EdgeStaticPG2(){}
	
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
