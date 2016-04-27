/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.EdgeQuad;
import java.util.Iterator;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/**
 * Bridge to make a Neo4j database accessible from Corese.
 *
 * @author edemairy
 */
public class TinkerpopGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	Graph neo4jGraph;

	private class GremlinIterable<T extends Entity> implements Iterable<Entity> {

		// @TODO à dédupliquer d'avec RdfToNeo4j
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
					createNode(gremlinCurrent.outVertex().value(VALUE).toString()),
					createNode(gremlinCurrent.label()),
					createNode(gremlinCurrent.inVertex().value(VALUE).toString())
				);
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

	public TinkerpopGraph(Graph neo4jGraph) {
		super();
		this.neo4jGraph = neo4jGraph;
		for (String key : neo4jGraph.variables().keys()) {
			System.out.println("key = " + key);
		}
	}

	@Override
	public void finalize() throws Throwable {
		neo4jGraph.close();
		super.finalize();
	}

	/**
	 *
	 * @param dbPath
	 * @return
	 */
	public static TinkerpopGraph create(String dbPath) {
		Configuration config = new BaseConfiguration();
		config.setProperty(Neo4jGraph.CONFIG_DIRECTORY, dbPath);
//		config.setProperty("gremlin.neo4j.conf.cache_type", "none");
//		config.setProperty("gremlin.neo4j.conf.allow_store_upgrade", "true");
		return new TinkerpopGraph(Neo4jGraph.open(config));
	}

	@Override
	public Iterable<Entity> getEdges() {
		Iterator<Edge> edges = neo4jGraph.edges();
		GraphTraversalSource g = neo4jGraph.traversal();
		System.out.println(g.V().count().value());
		return new GremlinIterable<>(edges);
	}

	@Override
	public void clean() {
		super.clean();
		neo4jGraph.tx().commit();
	}
}
