/*

 * Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.core.Entity;
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
import org.apache.tinkerpop.gremlin.structure.Element;

import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;

/**
 * Bridge to make a graph database accessible from Corese.
 *
 * @author edemairy
 */
public class TinkerpopGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	private org.apache.tinkerpop.gremlin.structure.Graph tGraph;
	private final GdbDriver driver;


	private final static Logger LOGGER = LogManager.getLogger(TinkerpopGraph.class.getSimpleName());

	private class GremlinIterable implements Iterable<Entity> {

		private final Iterator<? extends Element> edges;

		private class GremlinIterator implements Iterator<Entity> {

			private final Iterator<? extends Element> edges;

			GremlinIterator(Iterator<? extends Element> edges) {
				this.edges = edges;
			}

			@Override
			public boolean hasNext() {
				return edges.hasNext();
			}

			@Override
			public Entity next() {
				Entity nextEntity = driver.buildEdge(edges.next());
				return nextEntity;
			}

		}

		GremlinIterable(Iterator<? extends Element> edges) {
			this.edges = edges;
		}

		@Override
		public Iterator<Entity> iterator() {
			return new GremlinIterator(edges);
		}
	}

	private TinkerpopGraph(GdbDriver driver) {
		super();
		this.driver = driver;
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
			LOGGER.debug("{} {}", key, config.getString(key));
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
	 * @param driver
	 * @param databasePath
	 * @return
	 */
	public static Optional<TinkerpopGraph> create(GdbDriver driver, String databasePath) throws IOException {
		org.apache.tinkerpop.gremlin.structure.Graph actualGraph = driver.openDatabase(databasePath);
		TinkerpopGraph result = new TinkerpopGraph(driver);
		result.setTinkerpopGraph(actualGraph);
		return Optional.of(result);
	}

	public Iterable<Entity> getEdges(Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> filter) {
		try {
			GraphTraversalSource traversal = tGraph.traversal();
			GraphTraversal<? extends Element, ? extends Element> edges = filter.apply(traversal);
			return new GremlinIterable(edges);
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
		Iterable<Entity> result = traversal.E().has("value", edgeName).map(e -> driver.buildEdge(e.get())).toList();
		return result;
	}

	@Override
	public void clean() {
		super.clean();
		tGraph.tx().commit();
	}

	@Override
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
