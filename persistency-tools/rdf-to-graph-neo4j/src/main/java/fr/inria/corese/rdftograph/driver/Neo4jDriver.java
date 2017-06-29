/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.rdftograph.RdfToGraph;

import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.Logger;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;

import java.util.concurrent.TimeUnit;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

import org.apache.tinkerpop.gremlin.structure.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * @author edemairy
 */
public class Neo4jDriver extends GdbDriver {

	Neo4jGraph graph;
	private static final Logger LOGGER = Logger.getLogger(Neo4jDriver.class.getName());
	private LoadingCache<Value, Vertex> cache;

	public Neo4jDriver() {
		super();
		this.cache = CacheBuilder.newBuilder().
			maximumSize(1000000).
			expireAfterAccess(100, TimeUnit.DAYS).
			build(new CacheLoader<Value, Vertex>() {
				@Override
				public Vertex load(Value v) throws Exception {
					Vertex result = createOrGetNodeIntern(v);
					return result;
				}

			});
	}

	@Override
	public Graph openDatabase(String databasePath) {
		LOGGER.entering(getClass().getName(), "openDatabase");
		graph = Neo4jGraph.open(databasePath);
		return graph;
	}

	@Override
	public Graph createDatabase(String databasePath) throws IOException {
		LOGGER.entering(getClass().getName(), "createDatabase");
		super.createDatabase(databasePath);
		try {
			graph = Neo4jGraph.open(databasePath);
			graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_EDGE_LABEL, EDGE_P));
			graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_EDGE_LABEL, EDGE_G));
			graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_VERTEX_LABEL, VERTEX_VALUE));
			graph.tx().commit();
			return graph;
		} catch (Exception e) {
			LOGGER.severe(e.toString());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void closeDb() throws Exception {
		LOGGER.entering(getClass().getName(), "closeDb");
		try {
			while (graph.tx().isOpen()) {
				graph.tx().commit();
			}
		} finally {
			graph.close();
		}
	}

	protected boolean nodeEquals(Node endNode, Value object) {
		boolean result = true;
		result &= endNode.getProperty(KIND).equals(RdfToGraph.getKind(object));
		if (result) {
			switch (RdfToGraph.getKind(object)) {
				case BNODE:
				case IRI:
					result &= endNode.getProperty(EDGE_P).equals(object.stringValue());
					break;
				case LITERAL:
					Literal l = (Literal) object;
					result &= endNode.getProperty(EDGE_P).equals(l.getLabel());
					result &= endNode.getProperty(TYPE).equals(l.getDatatype().stringValue());
					if (l.getLanguage().isPresent()) {
						result &= endNode.hasProperty(LANG) && endNode.getProperty(LANG).equals(l.getLanguage().get());
					} else {
						result &= !endNode.hasProperty(LANG);
					}
			}
		}
		return result;
	}

	@Override
	public boolean isGraphNode(String label) {
		return graph.traversal().V().hasLabel(RDF_EDGE_LABEL).has(EDGE_G, label).hasNext();
	}

	private static enum RelTypes implements RelationshipType {
		CONTEXT
	}

	Map<String, Object> alreadySeen = new HashMap<>();

	/**
	 * Returns a unique id to store as the key for alreadySeen, to prevent
	 * creation of duplicates.
	 *
	 * @param v
	 * @return
	 */
	String nodeId(Value v) {
		StringBuilder result = new StringBuilder();
		String kind = RdfToGraph.getKind(v);
		switch (kind) {
			case IRI:
			case BNODE:
				result.append("label=" + v.stringValue() + ";");
				result.append("value=" + v.stringValue() + ";");
				result.append("kind=" + kind);
				break;
			case LITERAL:
				Literal l = (Literal) v;
				result.append("label=" + l.getLabel() + ";");
				result.append("value=" + l.getLabel() + ";");
				result.append("type=" + l.getDatatype().toString() + ";");
				result.append("kind=" + kind);
				if (l.getLanguage().isPresent()) {
					result.append("lang=" + l.getLanguage().get() + ";");
				}
				break;
		}
		return result.toString();
	}

	@Override
	public Vertex createOrGetNode(Value v) {
		return cache.getUnchecked(v);
	}

	/**
	 * Returns a new node if v does not exist yet.
	 *
	 * @param v
	 * @return
	 */
	public Vertex createOrGetNodeIntern(Value v) {
		GraphTraversal<Vertex, Vertex> it;
		Vertex result = null;
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
				if (it.hasNext()) {
					result = it.next();
				} else {
					result = graph.addVertex(RDF_VERTEX_LABEL);
					result.property(VERTEX_VALUE, v.stringValue());
					result.property(KIND, RdfToGraph.getKind(v));
				}
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				if (it.hasNext()) {
					result = it.next();
				} else {
					result = graph.addVertex(RDF_VERTEX_LABEL);
					result.property(VERTEX_VALUE, l.getLabel());
					result.property(TYPE, l.getDatatype().toString());
					result.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						result.property(LANG, l.getLanguage().get());
					}
				}

				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				if (it.hasNext()) {
					result = it.next();
				} else {
					result = graph.addVertex(RDF_VERTEX_LABEL);
					result.property(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode()));
					result.property(VERTEX_LARGE_VALUE, l.getLabel());
					result.property(TYPE, l.getDatatype().toString());
					result.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						result.property(LANG, l.getLanguage().get());
					}
				}
				break;
			}
		}
		return result;
	}

	@Override
	public Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties) {
		Object result;
		Vertex vSource = createOrGetNode(sourceId);
		Vertex vObject = createOrGetNode(objectId);
		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(EDGE_P);
		p.add(predicate);
		p.add(T.label);
		p.add(RDF_EDGE_LABEL);
		Vertex e = graph.addVertex(p.toArray());
		e.addEdge(SUBJECT_EDGE, vSource);
		e.addEdge(OBJECT_EDGE, vObject);
		result = e.id();
		return result;
	}

	@Override
	public void commit() {
		graph.tx().commit();
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> getFilter(String key, String s, String p, String o, String g) {
		Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> filter;
		switch (key) {
			case "?g?sPO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
				break;
			case "?g?sP?o":
				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
				break;
			case "?g?s?pO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL);
				break;
			case "?gSPO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
				break;
			case "?gSP?o":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
				break;
			case "GSP?o":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
				break;
			case "?gS?pO":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
				break;
			case "?gS?p?o":
				filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV();
				break;
			case "G?sP?o":
				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
				break;
			case "?g?s?p?o":
			default:
				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL);
		}
		return filter;
	}

	@Override
	public Entity buildEdge(Element e) {
		Vertex nodeEdge = (Vertex) e;
		Entity result = EdgeQuad.create(
			DatatypeMap.createResource(nodeEdge.value(EDGE_G)),
			buildNode(nodeEdge.edges(Direction.OUT, SUBJECT_EDGE).next().inVertex()),
			DatatypeMap.createResource(nodeEdge.value(EDGE_P)),
			buildNode(nodeEdge.edges(Direction.OUT, OBJECT_EDGE).next().inVertex())
		);
		return result;
	}

	@Override
	public fr.inria.edelweiss.kgram.api.core.Node buildNode(Element e) {
		Vertex node = (Vertex) e;
		String id = (String) node.value(VERTEX_VALUE);
		switch ((String) node.value(KIND)) {
			case IRI:
				return DatatypeMap.createResource(id);
			case BNODE:
				return DatatypeMap.createBlank(id);
			case LITERAL:
				String label = (String) node.value(VERTEX_VALUE);
				String type = (String) node.value(TYPE);
				VertexProperty<String> lang = node.property(LANG);
				if (lang.isPresent()) {
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
					return DatatypeMap.createLiteral(label, type);
				}
			case LARGE_LITERAL:
				label = (String) node.value(VERTEX_LARGE_VALUE);
				type = (String) node.value(TYPE);
				lang = node.property(LANG);
				if (lang.isPresent()) {
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
					return DatatypeMap.createLiteral(label, type);
				}
			default:
				throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
		}
	}

}
