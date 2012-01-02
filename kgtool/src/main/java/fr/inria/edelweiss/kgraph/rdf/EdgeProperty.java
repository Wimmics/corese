package fr.inria.edelweiss.kgraph.rdf;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeGraph;

/**
 * Edge class where the  property node is in the class
 * for target user domain
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeProperty extends EdgeGraph {
	protected static Node predicateNode;
	
	public EdgeProperty(){}
	
	public Node getEdgeNode() {
		return predicateNode;
	}
	
	public void setEdgeNode(Node node){
		predicateNode = node;
	}
	
}
