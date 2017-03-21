/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.orientechnologies.orient.core.metadata.schema.OType;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;
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
	private OrientGraph g;

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
		BaseConfiguration nodeConfig = new BaseConfiguration();
		nodeConfig.setProperty("type", "NOTUNIQUE");
		nodeConfig.setProperty("keytype", OType.STRING);
		graph.getTx().createVertexIndex(VERTEX_VALUE, RDF_VERTEX_LABEL, nodeConfig);
		graph.getTx().createEdgeIndex(EDGE_P, RDF_EDGE_LABEL, nodeConfig);
		g = graph.getTx();
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
}
