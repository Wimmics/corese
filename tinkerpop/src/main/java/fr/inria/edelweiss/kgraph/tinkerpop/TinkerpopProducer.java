/*
 *  Copyright Inria 2016
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.List;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer extends ProducerImpl {

	private TinkerpopGraph tpGraph;

	public TinkerpopProducer(Graph graph) {
		super(graph);
	}

	public void setTinkerpopGraph(TinkerpopGraph tpGraph) {
		this.tpGraph = tpGraph;
	}

	/**
	 * @param gNode @TODO Not used for the moment
	 * @param from @TODO Not used for the moment
	 * @param qEdge Requested edge.
	 * @param env Provided values that can set the values for all the Nodes
	 * of the request.
	 */
	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
		Node subject = qEdge.getNode(0);
		Node object = qEdge.getNode(1);
		if (isPredicateFree(qEdge) && subject.isVariable() && object.isVariable()) {
			return tpGraph.getEdges();
		} else {
			throw new UnsupportedOperationException("not supported yet");
		}
	}

	private boolean isPredicateFree(Edge edge) {
		Node predicate = edge.getEdgeNode();
		String name = predicate.getLabel();
		return name.equals(TOPREL);
	}

	private boolean isVariable(Edge edge) {
		return edge.getEdgeVariable() != null;
	}

	private boolean existNode(Node var, Environment env) {
		return env.getNode(var) != null;
	}

}
