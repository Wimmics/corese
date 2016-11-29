/*
 *  Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

//import static fr.inria.corese.tinkerpop.MappingRdf.*;
import fr.inria.corese.tinkerpop.mapper.TinkerpopToCorese;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.List;
import java.util.function.Function;
import org.apache.log4j.Logger;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outV;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer extends ProducerImpl {

	private final Logger LOGGER = Logger.getLogger(TinkerpopProducer.class.getName());
	private TinkerpopGraph tpGraph;

	public TinkerpopProducer(Graph graph) {
		super(graph);
	}

	public void setTinkerpopGraph(TinkerpopGraph tpGraph) {
		this.tpGraph = tpGraph;
	}

	/**
	 * @todo use env to obtain values given by Corese
	 *
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
		key.append((gNode == null) || (gNode.getLabel().compareTo("?g") == 0) ? "?g" : "G");
		key.append((subject.isVariable()) ? "?s" : "S");
		key.append((isPredicateFree(qEdge)) ? "?p" : "P");
		key.append((object.isVariable()) ? "?o" : "O");

		String g = (gNode == null) ? "" : gNode.getLabel();
		String s = (subject.isVariable()) ? "" : subject.getLabel();
		String p = (isPredicateFree(qEdge)) ? "" : qEdge.getEdgeNode().getLabel();
		String o = (object.isVariable()) ? "" : object.getLabel();

		switch (key.toString()) {
			case "?g?sPO":
				filter = t -> {
					return t.E().has(EDGE_P, p).where(inV().has(VERTEX_VALUE, o));
				};
				break;
			case "?g?sP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p);
				};
				break;
			case "?g?s?pO":
				filter = t -> {
					return t.V().has(VERTEX_VALUE, o).inE();
				};
				break;
			case "?gSPO":
				filter = t -> {
					return t.E().has(EDGE_P, p).where(inV().has(VERTEX_VALUE, o)).where(outV().has(VERTEX_VALUE, s));
				};
				break;
			case "?gSP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p).where(outV().has(VERTEX_VALUE, s));
				};
				break;
			case "?gS?pO":
				filter = t -> {
					return t.V().has(VERTEX_VALUE, s).outE().where(inV().has(VERTEX_VALUE, o));
				};
				break;
			case "?gS?p?o":
				filter = t -> {
					return t.V().has(VERTEX_VALUE, s).outE();
				};
				break;
			case "G?sP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p).has(EDGE_G, g);
				};
				break;
			case "?g?s?p?o":
			default:
				filter = t -> {
//					return t.E().has(EDGE_P, gt(""));
					return t.E().order().by(EDGE_P, decr).by(EDGE_S, decr).by(EDGE_O, decr).by(EDGE_G, decr); //.has(EDGE_P, gt(""));
				};
		}
		return tpGraph.getEdges(filter);
	}

	private boolean isPredicateFree(Edge edge) {
		Node predicate = edge.getEdgeNode();
		String name = predicate.getLabel();
		return name.equals(Graph.TOPREL);
	}

	@Override
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
		Node node = env.getNode(gNode);
		if (!tpGraph.isGraphNode(node)) {
			return false;
		}
		if (from.isEmpty()) {
			return true;
		}
		// @TODO what should be done.
		LOGGER.error("behaviour not defined in that case");
		return false;
		//return ei.getCreateDataFrom().isFrom(from, node);

//		Node node = env.getNode(gNode);
//		if (!graph.isGraphNode(node)) {
//			return false;
//		}
//		if (from.isEmpty()) {
//			return true;
//		}
//
//		return ei.getCreateDataFrom().isFrom(from, node);
	}

	public void close() {
		tpGraph.close();
	}
}
