/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDb the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
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
	public void openDb(String dbPath) {
		try {
			File dbDir = new File(dbPath);
			if (getWipeOnOpen()) {
				delete(dbPath);
			}
			graph = Neo4jGraph.open(dbPath);
			graph.cypher("CREATE INDEX ON :rdf_edge(e_value)");
			graph.cypher("CREATE INDEX ON :rdf_vertex(v_value)");
			graph.tx().commit();
		} catch (Exception e) {
			LOGGER.severe(e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void closeDb() {
		try {
			graph.tx().commit();
			graph.close();
		} catch (Exception ex) {
			Logger.getLogger(Neo4jDriver.class.getName()).log(Level.SEVERE, null, ex);
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
	public Vertex createOrGetNode(Value v) {
		GraphTraversal<Vertex, Vertex> it = null;
		Vertex result = null;
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				it = graph.traversal().V().has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
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
				it = graph.traversal().V().has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
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
				it = graph.traversal().V().has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
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
}
