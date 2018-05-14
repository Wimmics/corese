package fr.inria.corese.core.api;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Index;
import fr.inria.corese.kgram.api.core.Edge;

public interface IGraph {

	Iterable<Index> getIndexList();

	boolean hasEntailment();

	boolean isType(Edge edge);

	Iterable<Edge> getEdges(Node predicate, Node node, Node node2, int n);

	Iterable<Edge> getEdges(Node predicate, Node node, int n);

	boolean isGraphNode(Node src);

	Node getGraphNode(String label);

	Node getPropertyNode(String name);

	Iterable<Node> getProperties();

	Node copy(Node node);

	Iterable<Edge> getNodes(Node gNode);

	Iterable<Edge> getAllNodes();

	Iterable<Node> getGraphNodes();

	Node getNode(IDatatype dt, boolean b, boolean c);

	void init();


}
