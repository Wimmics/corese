package fr.inria.corese.kgraph.api;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgraph.core.Index;

public interface IGraph {

	Iterable<Index> getIndexList();

	boolean hasEntailment();

	boolean isType(Edge edge);

	Iterable<Entity> getEdges(Node predicate, Node node, Node node2, int n);

	Iterable<Entity> getEdges(Node predicate, Node node, int n);

	boolean isGraphNode(Node src);

	Node getGraphNode(String label);

	Node getPropertyNode(String name);

	Iterable<Node> getProperties();

	Node copy(Node node);

	Iterable<Entity> getNodes(Node gNode);

	Iterable<Entity> getAllNodes();

	Iterable<Node> getGraphNodes();

	Node getNode(IDatatype dt, boolean b, boolean c);

	void init();


}
