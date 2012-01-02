package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Node;

public class EdgePredicate extends EdgeImpl {

	private Node predicate;

	public void setEdgeNode(Node predicate) {
		this.predicate = predicate;
	}

	public Node getEdgeNode() {
		return predicate;
	}
}
