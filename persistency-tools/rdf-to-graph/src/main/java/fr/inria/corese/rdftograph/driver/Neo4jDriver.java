/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDb the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.corese.rdftograph.RdfToGraph.BNODE;
import static fr.inria.corese.rdftograph.RdfToGraph.IRI;
import static fr.inria.corese.rdftograph.RdfToGraph.KIND;
import static fr.inria.corese.rdftograph.RdfToGraph.LANG;
import static fr.inria.corese.rdftograph.RdfToGraph.LITERAL;
import static fr.inria.corese.rdftograph.RdfToGraph.TYPE;
import static fr.inria.corese.rdftograph.RdfToGraph.VALUE;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.neo4j.tinkerpop.api.impl.Neo4jFactoryImpl;

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
					result &= endNode.getProperty(VALUE).equals(object.stringValue());
					break;
				case LITERAL:
					Literal l = (Literal) object;
					result &= endNode.getProperty(VALUE).equals(l.getLabel());
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
	public Object createNode(Value v
	) {
//		Graph g = graph.getTx();
		Object result = null;
		String nodeId = nodeId(v);
		if (alreadySeen.containsKey(nodeId)) {
			return alreadySeen.get(nodeId);
		}
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				Vertex newVertex = graph.addVertex();
				newVertex.property(VALUE, v.stringValue());
				newVertex.property(KIND, RdfToGraph.getKind(v));
				result = newVertex.id();
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				Vertex newVertex = graph.addVertex();
				newVertex.property(VALUE, l.getLabel());
				newVertex.property(TYPE, l.getDatatype().toString());
				newVertex.property(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					newVertex.property(LANG, l.getLanguage().get());
				}
				result = newVertex.id();
				break;
			}
		}
//		graph.commit();
		alreadySeen.put(nodeId, result);
		return result;
	}

	static RelationshipType rdfEdge = DynamicRelationshipType.withName("rdf_edge");

	@Override
	public Object createRelationship(Object source, Object object, String predicate, Map<String, Object> properties
	) {
		Object result = null;
//		OrientGraph g = graph.getTx();
		Vertex vSource = graph.vertices(source).next();
		Vertex vObject = graph.vertices(object).next();
		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(VALUE);
		p.add(predicate);
		Edge e = vSource.addEdge("rdf_edge", vObject, p.toArray());
		result = e.id();
//		g.commit();
		return result;
		//properties.put(VALUE, predicate);
		//return g.createRelationship((Long) source, (Long) object, rdfEdge, properties);
	}
}
