/*
 * Copyright Inria 2016
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.tinkerpop.mapper.TinkerpopToCorese;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;

/**
 * Bridge to make a Neo4j database accessible from Corese.
 *
 * @author edemairy
 */
public class TinkerpopGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	private org.apache.tinkerpop.gremlin.structure.Graph tGraph;
	private TinkerpopToCorese unmapper;

	private final static Logger LOGGER = Logger.getLogger(TinkerpopGraph.class.getSimpleName());

	private class GremlinIterable<T extends Entity> implements Iterable<Entity> {

		private final Iterator<Edge> edges;

		private class GremlinIterator<T> implements Iterator<Entity> {

			private final Iterator<Edge> edges;

			GremlinIterator(Iterator<Edge> edges) {
				this.edges = edges;
			}

			@Override
			public boolean hasNext() {
				return edges.hasNext();
			}

			@Override
			public Entity next() {
				Edge gremlinCurrent = edges.next();
				return unmapper.buildEntity(gremlinCurrent);
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
		LOGGER.log(Level.INFO, "#vertices = {0}", new Object[]{tGraph.traversal().V().count().next()});
		LOGGER.log(Level.INFO, "#edges = {0}", new Object[]{tGraph.traversal().E().count().next()});
		LOGGER.info("** Variables of the graph **");
		try {
			for (String key : tGraph.variables().keys()) {
				LOGGER.info("key = " + key);
			}
		} catch (Exception ex) {
			LOGGER.info("Impossible to show graph variables. Cause: " + ex.toString());
		}
		LOGGER.info("****************************");
		LOGGER.info("** configuration **");
		Configuration config = tGraph.configuration();
		for (Iterator<String> c = config.getKeys(); c.hasNext();) {
			String key = c.next();
			LOGGER.log(Level.INFO, "{0} {1}", new Object[]{key, config.getString(key)});
		}
		LOGGER.info("****************************");
	}

	@Override
	public void finalize() throws Throwable {
		tGraph.close();
		super.finalize();
	}

	/**
	 *
	 * @param dbPath
	 * @return
	 */
	public static Optional<TinkerpopGraph> create(String driverName, Configuration config) {
		try {
			Class gclass = Class.forName(driverName);
			Method factoryMethod = gclass.getMethod("open", Configuration.class);
			org.apache.tinkerpop.gremlin.structure.Graph actualGraph = (org.apache.tinkerpop.gremlin.structure.Graph) factoryMethod.invoke(null, config);
			TinkerpopGraph result = new TinkerpopGraph();
			result.setTinkerpopGraph(actualGraph);
			return Optional.of(result);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			Logger.getLogger(TinkerpopGraph.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Helper that use a String array to generate the configuration used to
	 * inialize the tinkerpop driver.
	 *
	 * @param driverName Name of the class of the driver to instanciate.
	 * @param configArray Array following the pattern { key_1, value_1,
	 * key_2, value_2, etc. }
	 * @return
	 */
	public static Optional<TinkerpopGraph> create(String driverName, String[] configArray) {
		Configuration config = new BaseConfiguration();
		for (int i = 0; i < configArray.length; i += 2) {
			String key = configArray[i];
			String value = configArray[i + 1];
			config.setProperty(key, value);
		}
		return create(driverName, config);
	}

	@Override
	public Iterable<Entity> getEdges() {
		Iterator<Edge> edges = tGraph.edges();
		GraphTraversalSource g = tGraph.traversal();
		System.out.println(g.V().count().value());
		return new GremlinIterable<>(edges);
	}

	/**
	 * @param edgeName
	 * @return 
	 */
	@Override
	public Iterable<Entity> getEdges(String edgeName) {
		GraphTraversalSource traversal = tGraph.traversal();
		Iterable<Entity> result = traversal.E().has("value", edgeName).map(e->unmapper.buildEntity(e.get())).toList();
		return result;
	}

	@Override
	public void clean() {
		super.clean();
		tGraph.tx().commit();
	}
}
