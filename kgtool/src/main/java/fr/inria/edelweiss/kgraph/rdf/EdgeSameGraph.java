package fr.inria.edelweiss.kgraph.rdf;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgePredicate;

public class EdgeSameGraph extends EdgePredicate {
	
	private static Node graph;
		
	public EdgeSameGraph(){}
	
	EdgeSameGraph (Node source, Node subject, Node predicate, Node object){
		setNode(0, subject);
		setNode(1, object);
		setEdgeNode(predicate);
		setGraph(source);
	}
	
	public static EdgeSameGraph create(Node source, Node subject, Node predicate, Node object){
		return new EdgeSameGraph(source, subject, predicate, object);
	}

	public void setGraph(Node graph) {
		EdgeSameGraph.graph = graph;
	}

	public Node getGraph() {
		return graph;
	}
	
	

}
