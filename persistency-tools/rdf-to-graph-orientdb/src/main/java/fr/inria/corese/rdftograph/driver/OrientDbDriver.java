/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.orientechnologies.orient.core.metadata.schema.OType;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class OrientDbDriver extends GdbDriver {

	OrientGraphFactory graph;

	@Override
	public Graph openDatabase(String databasePath) {
		graph = new OrientGraphFactory(databasePath);
		g = graph.getTx();
		return g;
	}

	@Override
	public Graph createDatabase(String databasePath) throws IOException {
		String path = databasePath.replaceFirst("plocal:", "");
		super.createDatabase(path);
		graph = new OrientGraphFactory(databasePath);
		BaseConfiguration nodeConfig = new BaseConfiguration();
		nodeConfig.setProperty("type", "NOTUNIQUE");
		nodeConfig.setProperty("keytype", OType.STRING);
		graph.getTx().createVertexIndex(VERTEX_VALUE, RDF_VERTEX_LABEL, nodeConfig);
		nodeConfig = new BaseConfiguration();
		nodeConfig.setProperty("type", "NOTUNIQUE");
		nodeConfig.setProperty("keytype", OType.STRING);
		graph.getTx().createVertexIndex(KIND, RDF_VERTEX_LABEL, nodeConfig);
		nodeConfig = new BaseConfiguration();
		nodeConfig.setProperty("type", "NOTUNIQUE");
		nodeConfig.setProperty("keytype", OType.STRING);
		graph.getTx().createEdgeIndex(EDGE_P, RDF_EDGE_LABEL, nodeConfig);
		g = graph.getTx();
		return g;
	}

	@Override
	public void closeDb() {
		try {
			graph.getTx().close();
		} catch (Exception ex) {
			Logger.getLogger(OrientDbDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	Map<Value, Vertex> alreadySeen = new HashMap<>();

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
	public Object createRelationship(Value source, Value object, String predicate, Map<String, Object> properties) {
		Object result = null;
		Vertex vSource = createOrGetNode(source);
		Vertex vObject = createOrGetNode(object);
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
	}

	@Override
	public void commit() {
		graph.getTx().commit();
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> getFilter(String key, String s, String p, String o, String g) {
		Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> filter;
		switch (key.toString()) {
			case "?g?sPO":
				filter = t -> {
					return t.E().has(EDGE_P, p).has(EDGE_O, o);
				};
				break;
			case "?g?sP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p);
				};
				break;
			case "?g?s?pO":
				filter = t -> {
					return t.E().has(EDGE_O, o);
				};
				break;
			case "?gSPO":
				filter = t -> {
					return t.E().has(EDGE_P, p).has(EDGE_S, s).has(EDGE_O, o);
				};
				break;
			case "?gSP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p).has(EDGE_S, s);
				};
				break;
			case "?gS?pO":
				filter = t -> {
					return t.E().has(EDGE_S, s).has(EDGE_O, o);
				};
				break;
			case "?gS?p?o":
				filter = t -> {
					return t.E().has(EDGE_S, s);
				};
				break;
			case "G?sP?o":
				filter = t -> {
					return t.E().has(EDGE_P, p).has(EDGE_G, g);
				};
				break;
			case "?g?s?p?o":
			default:
				filter = t -> {
					return t.E();
				};
		}
		return filter;
	}

	@Override
	public Entity buildEdge(Element e) {
		Edge edge = (Edge) e;
		String namedGraph = edge.value(EDGE_G);
		Entity result = EdgeQuad.create(DatatypeMap.createResource(namedGraph),
			buildNode(edge.outVertex()),
			DatatypeMap.createResource((String) e.value(EDGE_P)),
			buildNode(edge.inVertex())
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
