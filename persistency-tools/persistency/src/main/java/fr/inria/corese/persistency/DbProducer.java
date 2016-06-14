/*
 *  Copyright Inria 2016
 */
package fr.inria.corese.persistency;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.log4j.Logger;

/**
 *
 * @author edemairy
 */
public class DbProducer extends ProducerImpl {

	private final Logger LOGGER = Logger.getLogger(DbProducer.class.getName());
	private DbGraph dbGraph;

	public DbProducer(Graph graph) {
		super(graph);
	}

	public void setDbGraph(DbGraph dbGraph) {
		this.dbGraph = dbGraph;
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

		ArrayList< Function<String, Boolean> > edgeFilters = new ArrayList<>();
//		if (!isPredicateFree(qEdge)) {
//			edgeFilters.add(e -> e.get().value(VALUE).equals(qEdge.getEdgeNode().getLabel()));
//		}
//		if (!subject.isVariable()) {
//			edgeFilters.add(e -> e.get().outVertex().value(VALUE).toString().equals(subject.getLabel()));
//		}
//		if (!object.isVariable()) {
//			edgeFilters.add(e -> e.get().inVertex().value(VALUE).toString().equals(object.getLabel()));
//		}
		return dbGraph.getEdges(edgeFilters);
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
