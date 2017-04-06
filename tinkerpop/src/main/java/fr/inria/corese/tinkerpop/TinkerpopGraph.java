/*

 * Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.corese.tinkerpop.mapper.TinkerpopToCorese;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Bridge to make a Neo4j database accessible from Corese.
 *
 * @author edemairy
 */
public class TinkerpopGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	private org.apache.tinkerpop.gremlin.structure.Graph tGraph;
	private TinkerpopToCorese unmapper;

	private final static Logger LOGGER = LogManager.getLogger(TinkerpopGraph.class.getSimpleName());

	private class GremlinIterable<T extends Entity> implements Iterable<Entity> {

		private final Iterator<Edge> edges;

		private class GremlinIterator<T> implements Iterator<Entity> {

			private final Iterator<Edge> edges;
			private Optional<Edge> previousEdge = Optional.empty();
			private Optional<Edge> nextEdge = Optional.empty();
			private boolean nextSearched = false; // flag to know whether a hasNext() has launched and find a nextEdge element before nextEdge() was called.

			GremlinIterator(Iterator<Edge> edges) {
				this.edges = edges;
			}

			@Override
			public boolean hasNext() {
				return edges.hasNext();
			}

			@Override
			public Entity next() {
				Entity nextEntity = unmapper.buildEntity(edges.next());
				return nextEntity;
			}

		}

		GremlinIterable(Iterator<Edge> edges) {
			this.edges = edges;
		}

		@Override
		public Iterator<Entity> iterator() {
			return new GremlinIterator<>(edges);
		}
	}

	public TinkerpopGraph() {
		super();
		unmapper = new TinkerpopToCorese(this);
	}

	public void setTinkerpopGraph(org.apache.tinkerpop.gremlin.structure.Graph tGraph) {
		this.tGraph = tGraph;
		LOGGER.debug("** Variables of the graph **");
		try {
			for (String key : tGraph.variables().keys()) {
				LOGGER.info("key = " + key);
			}
		} catch (Exception ex) {
			LOGGER.error("Impossible to show graph variables. Cause: " + ex.toString());
		}
		LOGGER.debug("****************************");
		LOGGER.debug("** configuration **");
		Configuration config = tGraph.configuration();
		for (Iterator<String> c = config.getKeys(); c.hasNext();) {
			String key = c.next();
			LOGGER.debug("{0} {1}", key, config.getString(key));
		}
		LOGGER.debug("****************************");
	}

	@Override
	public void finalize() throws Throwable {
		LOGGER.debug("calling close");
		tGraph.close();
		LOGGER.debug("close called");
		super.finalize();
		LOGGER.debug("after finalize");
	}

	/**
	 *
	 * @param dbPath
	 * @return
	 */
	public static Optional<TinkerpopGraph> create(GdbDriver driver, String databasePath) throws IOException {
		org.apache.tinkerpop.gremlin.structure.Graph actualGraph = driver.openDatabase(databasePath);
		TinkerpopGraph result = new TinkerpopGraph();
		result.setTinkerpopGraph(actualGraph);
		return Optional.of(result);
	}

	public Iterable<Entity> getEdges(Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter) {
		try {
			GraphTraversalSource traversal = tGraph.traversal();
			GraphTraversal<?, Edge> edges = filter.apply(traversal);
			return new GremlinIterable<Entity>(edges);//{
		} catch (Exception ex) {
			LOGGER.error("An error occurred: {}", ex.toString());
			return null;
		}
	}

	@Override
	public Iterable<Entity> getEdges() {
		return getEdges(t -> t.E());
	}

	/**
	 * @param edgeName
	 * @return
	 */
	@Override
	public Iterable<Entity> getEdges(String edgeName) {
		GraphTraversalSource traversal = tGraph.traversal();
		Iterable<Entity> result = traversal.E().has("value", edgeName).map(e -> unmapper.buildEntity(e.get())).toList();
		return result;
	}

	@Override
	public void clean() {
		super.clean();
		tGraph.tx().commit();
	}

	public boolean containsCoreseNode(Node node) {
		GraphTraversalSource traversal = tGraph.traversal();
		GraphTraversal<Edge, Edge> result = traversal.E().has(EDGE_G, node.getLabel());
		return result.hasNext();
	}

	public void close() {
		try {
			tGraph.close();
		} catch (Exception ex) {
			LOGGER.error("Exception when closing: {}", ex);
		}
	}
}
