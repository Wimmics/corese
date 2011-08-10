package fr.inria.edelweiss.kgraph.rdf;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeGraph;

/**
 * Edge class where the  property node is in the class
 * e.g. rdfs:label
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeComment extends EdgeGraph {
	protected static Node predicateNode;
	
	public EdgeComment(){}
	
	public Node getEdgeNode() {
		return predicateNode;
	}
	
	public void setEdgeNode(Node node){
		predicateNode = node;
	}
	
}
