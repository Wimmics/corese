package fr.inria.edelweiss.kgraph.rdf;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgePredicate;

public class EdgeEntail extends EdgePredicate {
	
	private static Node graph;
		
	
	EdgeEntail (Node source, Node subject, Node predicate, Node object){
		setNode(0, subject);
		setNode(1, object);
		setEdgeNode(predicate);
		setGraph(source);
	}
	
	public static EdgeEntail create(Node source, Node subject, Node predicate, Node object){
		return new EdgeEntail(source, subject, predicate, object);
	}

	public void setGraph(Node graph) {
		EdgeEntail.graph = graph;
	}

	public Node getGraph() {
		return graph;
	}
	
	

}
