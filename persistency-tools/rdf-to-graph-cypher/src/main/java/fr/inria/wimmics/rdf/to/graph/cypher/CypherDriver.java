/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.rdf.to.graph.cypher;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.corese.rdftograph.driver.Neo4jDriver;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class CypherDriver extends GdbDriver {

	private final Neo4jDriver neo4jDriver;

	public CypherDriver() throws IOException {
		neo4jDriver = new Neo4jDriver();
	}

	@Override
	public Graph openDatabase(String dbPath) {
		return neo4jDriver.openDatabase(dbPath);
	}

	@Override
	public void closeDatabase() throws Exception {
		neo4jDriver.closeDatabase();
	}

	@Override
	public void commit() {
		neo4jDriver.commit();
	}

	@Override
	public Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties) {
		return neo4jDriver.createRelationship(sourceId, objectId, predicate, properties);
	}

	/**
	 * Build the properties map for cypher from a RDF node.
	 *
	 * @param v
	 * @return Map to use in cypher when creating the node.
	 */
	public Map<String, Object> buildVertexProperties(Value v) {
		Map<String, Object> properties = new HashMap<>();
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				properties.put(VERTEX_VALUE, v.stringValue());
				properties.put(KIND, RdfToGraph.getKind(v));
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				properties.put(VERTEX_VALUE, l.getLabel());
				properties.put(TYPE, l.getDatatype().toString());
				properties.put(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					properties.put(LANG, l.getLanguage().get());
				}
				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				properties.put(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode()));
				properties.put(VERTEX_LARGE_VALUE, l.getLabel());
				properties.put(TYPE, l.getDatatype().toString());
				properties.put(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					properties.put(LANG, l.getLanguage().get());
				}
				break;
			}
		}
		return properties;
	}

	@Override
	public Function<GraphTraversalSource, Iterator<? extends Element>> getFilter(String key, String s, String p, String o, String g) {
		Function<GraphTraversalSource, Iterator<? extends Element>> filter;
		switch (key) {
//			case "GSPO":
//				filter = t -> g.cyt.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p).has(EDGE_O, o).has(EDGE_G, g);
//				break;
//			case "GSP?o":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p).has(EDGE_G, g);
//				break;
//			case "?g?sPO":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_P, p).has(EDGE_O, o);
//				break;
//			case "?g?sP?o":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_P, p);
//				break;
//			case "?g?s?pO":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_O, o);
//				break;
//			case "?gSPO":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p).has(EDGE_O, o);
//				break;
//			case "?gSP?o":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_P, p);
//				break;
//			case "?gS?pO":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s).has(EDGE_O, o);
//				break;
//			case "?gS?p?o":
//				filter = t -> t.V().has(RDF_EDGE_LABEL, EDGE_S, s);
//				break;
//			case "G?sP?o":
//				filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
//				break;
			case "?g?s?p?o":
			default:
				filter = (GraphTraversalSource t) -> {
					GraphTraversal<? extends Element, HashMap<String, Neo4jVertex>> result = neo4jDriver.getNeo4jGraph().cypher("MATCH (edge: rdf_edge) return edge");
					HashMap<String, Neo4jVertex> map = result.next();
					return new ArrayList<Neo4jVertex>(map.values()).iterator();
		};
		}
		return filter;
	}

	@Override
	public EdgeQuad buildEdge(Element e) {
		return neo4jDriver.buildEdge(e);
	}

	@Override
	public Node buildNode(Element e) {
		return neo4jDriver.buildNode(e);
	}

	@Override
	public boolean isGraphNode(String label) {
		return neo4jDriver.isGraphNode(label);
	}

}
