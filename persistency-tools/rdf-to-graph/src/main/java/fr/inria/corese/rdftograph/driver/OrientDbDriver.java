/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class OrientDbDriver extends GdbDriver {

	OrientGraphFactory graph;

	@Override
	public void openDb(String dbPath) {
		try {
			if (getWipeOnOpen()) {
				String path = dbPath.replaceFirst("plocal:", "");
				if (Files.exists(Paths.get(path))) {
					delete(path);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(OrientDbDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
		graph = new OrientGraphFactory(dbPath);
	}

	@Override
	public void closeDb() {
		graph.close();
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

	@Override
	public Object createNode(Value v) {
		OrientGraph g = graph.getTx();
		Object result = null;
		String nodeId = nodeId(v);
		if (alreadySeen.containsKey(nodeId)) {
			return alreadySeen.get(nodeId);
		}
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				Vertex newVertex = g.addVertex();
				newVertex.property(VALUE, v.stringValue());
				newVertex.property(KIND, RdfToGraph.getKind(v));
				result = newVertex.id();
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				Vertex newVertex = g.addVertex();
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
		g.commit();
		alreadySeen.put(nodeId, result);
		return result;
	}

	@Override
	public Object createRelationship(Object source, Object object, String predicate, Map<String, Object> properties) {
		Object result = null;
		OrientGraph g = graph.getTx();
		Vertex vSource = g.vertices(source).next();
		Vertex vObject = g.vertices(object).next();
		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(VALUE);
		p.add(predicate);
		Edge e = vSource.addEdge("rdf_edge", vObject, p.toArray());
		result = e.id();
		g.commit();
		return result;
	}

}
