/*

 * Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.corese.tinkerpop.mapper.TinkerpopToCorese;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;

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
	public static Optional<TinkerpopGraph> create(String driverName, Object config) {
		try {
			// call actualGraph = driverName.open(null, config)
			Class gclass = Class.forName(driverName);
			Method factoryMethod = gclass.getMethod("open", config.getClass());
			org.apache.tinkerpop.gremlin.structure.Graph actualGraph = (org.apache.tinkerpop.gremlin.structure.Graph) factoryMethod.invoke(null, config);
			
			TinkerpopGraph result = new TinkerpopGraph();
			result.setTinkerpopGraph(actualGraph);
			return Optional.of(result);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Helper that use a String array to generate the configuration used to
	 * initialize the tinkerpop driver.
	 *
	 * @param driverName Name of the class of the driver to instanciate.
	 * @param configArray Array following the pattern { key_1, value_1,
	 * key_2, value_2, etc. }
	 * @return
	 */
	public static Optional<TinkerpopGraph> create(String driverName, String[] configArray) {
		if (configArray.length == 1) {
			return create(driverName, configArray[0]);
		} else {
			Configuration config = new BaseConfiguration();
			for (int i = 0; i < configArray.length; i += 2) {
				String key = configArray[i];
				String value = configArray[i + 1];
				config.setProperty(key, value);
			}
			return create(driverName, config);
		}
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

	public boolean isGraphNode(Node node) {
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
