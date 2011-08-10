package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Node;

public class EdgeGraph extends EdgeImpl {
	
	protected Node graph;
	
	public Node getGraph() {
		return graph;
	}
	
	public void setGraph(Node gNode){
		graph =  gNode;
	}
	
}
