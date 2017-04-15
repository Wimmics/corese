/*
 *  Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer extends ProducerImpl {

	private final Logger LOGGER = LogManager.getLogger(TinkerpopProducer.class.getName());
	private GdbDriver databaseDriver;
	private TinkerpopGraph graph;

	public TinkerpopProducer(TinkerpopGraph graph, GdbDriver databaseDriver) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(graph);
		this.databaseDriver = databaseDriver;
		this.graph = graph;
	}

	/**
	 * @return An iterable
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

		Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter;
		StringBuilder key = new StringBuilder();

		String g = (gNode == null) ? "" : gNode.getLabel();
		key.append((gNode == null) || (gNode.getLabel().compareTo("?g") == 0) ? "?g" : "G");

		String s = updateVariable(subject.isVariable(), subject, env, key, "?s", "S");
		String p = updateVariable(isPredicateFree(qEdge), qEdge.getEdgeNode(), env, key, "?p", "P");
//		(IDatatype) dtObject = object.getValue(); pour obtenir le type des données demandées.
		String o = updateVariable(object.isVariable(), object, env, key, "?o", "O");
		LOGGER.trace("in case " + key.toString());
		filter = databaseDriver.getFilter(key.toString(), s, p, o, g);
		Iterable<Entity> result = graph.getEdges(filter);
		return result;
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	private boolean isPredicateFree(Edge edge) {
		Node predicate = edge.getEdgeNode();
		String name = predicate.getLabel();
		return name.equals(Graph.TOPREL);
	}

	@Override
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
		Node node = env.getNode(gNode);
		if (!graph.containsCoreseNode(node)) {
			return false;
		}
		if (from.isEmpty()) {
			return true;
		}
		// @TODO what should be done.
		LOGGER.error("behaviour not defined in that case");
		return false;
		//return ei.getCreateDataFrom().isFrom(from, node);
	}

	public void close() {
		graph.close();
	}

	private String updateVariable(boolean isVariableFree, Node node, Environment env, StringBuilder key, String freeVar, String boundVar) {
		String result;
		if (isVariableFree) {
			if (env.getNode(node.getLabel()) != null) {
				key.append(boundVar);
				result = env.getNode(node.getLabel()).getLabel();
			} else {
				key.append(freeVar);
				result = "";
			}
		} else {
			key.append(boundVar);
			result = node.getLabel();
		}
		return result;
	}
}
