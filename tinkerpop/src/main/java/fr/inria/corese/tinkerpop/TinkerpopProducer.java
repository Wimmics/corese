/*
 *  Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import static fr.inria.corese.tinkerpop.MappingRdf.EDGE_VALUE;
import static fr.inria.corese.tinkerpop.MappingRdf.VERTEX_VALUE;
import fr.inria.corese.tinkerpop.mapper.TinkerpopToCorese;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer extends ProducerImpl {

	private final Logger LOGGER = Logger.getLogger(TinkerpopProducer.class.getName());
	private TinkerpopGraph tpGraph;
	private TinkerpopToCorese unmapper;

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

		Function<GraphTraversalSource, GraphTraversal<org.apache.tinkerpop.gremlin.structure.Edge, org.apache.tinkerpop.gremlin.structure.Edge>> filter;
		if (isPredicateFree(qEdge)) {
			filter = null;
		} else {
			filter =  t -> {
				return t.E().has(EDGE_VALUE, qEdge.getEdgeNode().getLabel());
			} ;
		}
//		if (subject.isVariable()) {
//			edgeFilters.add(e -> e.get().outVertex().value(VERTEX_VALUE).toString().equals(subject.getLabel()));
//		}
//		if (!object.isVariable()) {
//			edgeFilters.add(e -> e.get().inVertex().value(VERTEX_VALUE).toString().equals(object.getLabel()));
//		}
		return tpGraph.getEdges(filter);
	}

	private boolean isPredicateFree(Edge edge) {
		Node predicate = edge.getEdgeNode();
		String name = predicate.getLabel();
		return name.equals(Graph.TOPREL);
	}

	private boolean isVariable(Edge edge) {
		return edge.getEdgeVariable() != null;
	}

	private boolean existNode(Node var, Environment env) {
		return env.getNode(var) != null;
	}

}
