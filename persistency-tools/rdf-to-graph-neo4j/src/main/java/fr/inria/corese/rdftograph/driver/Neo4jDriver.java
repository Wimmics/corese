/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class Neo4jDriver extends GdbDriver {

	Neo4jGraph graph;
	private static final Logger LOGGER = Logger.getLogger(Neo4jDriver.class.getName());

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
			graph.cypher("CREATE INDEX ON :rdf_edge(e_value)");
			graph.cypher("CREATE INDEX ON :rdf_vertex(v_value)");
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

	/**
	 * Returns a new node if v does not exist yet.
	 *
	 * @param v
	 * @param context
	 * @return
	 */
	@Override
	public Vertex createOrGetNode(Value v) {
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
		Object result = null;
		Vertex vSource = createOrGetNode(sourceId);
		Vertex vObject = createOrGetNode(objectId);

		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(EDGE_P);
		p.add(predicate);
		Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
		result = e.id();
		return result;
		//properties.put(EDGE_P, predicate);
		//return g.createRelationship((Long) source, (Long) object, rdfEdge, properties);
	}

	@Override
	public void commit() {
		graph.tx().commit();
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> getFilter(String key, String s, String p, String o, String g) {
		Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter;
		switch (key.toString()) {
			case "?g?sPO":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_O, o);
				};
				break;
			case "?g?sP?o":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
				};
				break;
			case "?g?s?pO":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_O, o);
				};
				break;
			case "?gSPO":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_S, s).has(EDGE_O, o);
				};
				break;
			case "?gSP?o":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_S, s);
				};
				break;
			case "?gS?pO":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_S, s).has(EDGE_O, o);
				};
				break;
			case "?gS?p?o":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_S, s);
				};
				break;
			case "G?sP?o":
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
				};
				break;
			case "?g?s?p?o":
			default:
				filter = t -> {
					return t.E().hasLabel(RDF_EDGE_LABEL);
				};
		}
		return filter;
	}
}
