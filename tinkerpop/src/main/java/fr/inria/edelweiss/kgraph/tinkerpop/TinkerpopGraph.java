/*
 * Copyright Inria 2016
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeQuad;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/**
 * Bridge to make a Neo4j database accessible from Corese.
 *
 * @author edemairy
 */
public class TinkerpopGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	private Graph tGraph;
	private final static Logger LOGGER = Logger.getLogger(TinkerpopGraph.class.getSimpleName());

	private class GremlinIterable<T extends Entity> implements Iterable<Entity> {

		// @TODO à dédupliquer d'avec RdfToGraph
		public static final String LITERAL = "literal";
		public static final String IRI = "IRI";
		public static final String BNODE = "bnode";
		public static final String CONTEXT = "context";
		public static final String KIND = "kind";
		public static final String LANG = "lang";
		public static final String TYPE = "type";
		public static final String VALUE = "value";

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
				// \todo 
				// 1. context à aller chercher sur l'arc
				// 2.création des Nodes à faire en fonction de "kind" 
				Edge gremlinCurrent = edges.next();
				String context = gremlinCurrent.value(CONTEXT);
				Entity result = EdgeQuad.create(
					createNode(context),
					unmapNode(gremlinCurrent.outVertex()),
					createNode((String) gremlinCurrent.value(VALUE)),
					unmapNode(gremlinCurrent.inVertex())
				);
				return result;
			}

			private Node unmapNode(Vertex node) {
				switch ((String) node.value(KIND)) {
					case IRI:
						return createNode((String) node.value(VALUE));
					case BNODE:
						return createBlank((String) node.value(VALUE));
					case LITERAL:
						String label = (String) node.value(VALUE);
						String type = (String) node.value(TYPE);
						VertexProperty<String> lang = node.property(LANG);
						if (lang.isPresent()) {
							return addLiteral(label, type, lang.value());
						} else {
							return addLiteral(label, type);
						}
					default:
						throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
				}
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

	public TinkerpopGraph(Graph tGraph) {
		super();
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
	public static TinkerpopGraph create(String driverName, Configuration config) {
		try {
			Class gclass = Class.forName(driverName);
			Method factoryMethod = gclass.getMethod("open", Configuration.class);
			Graph actualGraph = (Graph) factoryMethod.invoke(null, config);
			return new TinkerpopGraph(actualGraph);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			Logger.getLogger(TinkerpopGraph.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public Iterable<Entity> getEdges() {
		Iterator<Edge> edges = tGraph.edges();
		GraphTraversalSource g = tGraph.traversal();
		System.out.println(g.V().count().value());
		return new GremlinIterable<>(edges);
	}

	@Override
	public void clean() {
		super.clean();
		tGraph.tx().commit();
	}
}
