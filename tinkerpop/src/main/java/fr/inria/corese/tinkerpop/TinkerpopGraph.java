/*
 * Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import static fr.inria.corese.rdftograph.RdfToGraph.*;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.corese.tinkerpop.mapper.TinkerpopToCorese;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
			private ArrayList<Entity> previousEntity = new ArrayList<>(); // @TODO Must be removed after debugging.
			private Optional<Edge> nextEdge = Optional.empty();
			private boolean nextSearched = false; // flag to know whether a hasNext() has launched and find a nextEdge element before nextEdge() was called.

			GremlinIterator(Iterator<Edge> edges) {
				this.edges = edges;
			}

			@Override
			public boolean hasNext() {
				if (!edges.hasNext()) {
					return false;
				} else {
					return findNext();
				}
			}

			@Override
			public Entity next() {
				if (!nextSearched) {
					if (!findNext()) {
						throw new NoSuchElementException();
					}
				}
				nextSearched = false;
				Entity nextEntity = unmapper.buildEntity(nextEdge.get());

				previousEntity.add(nextEntity);

				previousEdge = Optional.of(nextEdge.get());
				return nextEntity;
			}

			private boolean findNext() {
				do {
					nextEdge = Optional.of(edges.next());
					// @TODO to be removed begin
					String searched = "8.12";
					if (nextEdge.get().property(EDGE_O).value().toString().contains(searched)) {
						LOGGER.info("{} found. equality with previous edge = {} ", searched, Boolean.toString(edgeEquals(nextEdge, previousEdge)));
					}
					// @TODO to be removed end
				} while (edgeEquals(nextEdge, previousEdge) && edges.hasNext());
				if (!edgeEquals(nextEdge, previousEdge)) {
					nextSearched = true;
					return true;
				} else {
					return false;
				}
			}

			private boolean edgeEquals(Optional<Edge> current, Optional<Edge> other) {
				boolean result;
				if (current.isPresent() && other.isPresent()) {
					Edge currentEdge = current.get();
					Edge otherEdge = other.get();
					result = current.get().property(EDGE_P).value().equals(other.get().property(EDGE_P).value())
						&& nodeEquals(currentEdge.inVertex(), otherEdge.inVertex())
						&& nodeEquals(currentEdge.outVertex(), otherEdge.outVertex());
				} else {
					result = !(current.isPresent() ^ other.isPresent());
				}
				return result;
			}

			private boolean nodeEquals(Vertex v1, Vertex v2) {
				if (v1.property(KIND).value().equals(v2.property(KIND).value())) {
					switch (v1.property(KIND).value().toString()) {
						case BNODE:
						case IRI:
							return (v1.property(VERTEX_VALUE).value().equals(v2.property(VERTEX_VALUE).value()));
						case LITERAL:
							IDatatype literal1 = makeLiteral(v1);
							IDatatype literal2 = makeLiteral(v2);
							return literal1.compareTo(literal2) == 0;
					}

				}
				return false;
			}

			private IDatatype makeLiteral(Vertex v) {
				String value = v.property(VERTEX_VALUE).value().toString();
				String kind = v.property(TYPE).value().toString();
				VertexProperty<Vertex> lang = v.property(LANG);
				IDatatype result = lang.isPresent() ? DatatypeMap.createLiteral(value, kind, v.property(LANG).value().toString()) : DatatypeMap.createLiteral(value, kind);
				return result;
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
//		LOGGER.log(Level.INFO, "#vertices = {0}", new Object[]{tGraph.traversal().V().count().nextEdge()});
//		LOGGER.log(Level.INFO, "#edges = {0}", new Object[]{tGraph.traversal().E().count().nextEdge()});
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
			LOGGER.info("{0} {1}", key, config.getString(key));
		}
		LOGGER.info("****************************");
	}

	@Override
	public void finalize() throws Throwable {
		LOGGER.info("calling close");
		tGraph.close();
		LOGGER.info("close called");
		super.finalize();
		LOGGER.info("after finalize");
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
			ex.printStackTrace();
//			Logger.getLogger(TinkerpopGraph.class.getName()).log(Level.SEVERE, ex.getMessage());
		}
		return null;
	}

	public static Optional<TinkerpopGraph> create(String driverName, String config) {
		try {
			Class gclass = Class.forName(driverName);
			Method factoryMethod = gclass.getMethod("open", String.class);
			org.apache.tinkerpop.gremlin.structure.Graph actualGraph = (org.apache.tinkerpop.gremlin.structure.Graph) factoryMethod.invoke(null, config);
			TinkerpopGraph result = new TinkerpopGraph();
			result.setTinkerpopGraph(actualGraph);
			return Optional.of(result);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			LOGGER.error(ex.getMessage());
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
