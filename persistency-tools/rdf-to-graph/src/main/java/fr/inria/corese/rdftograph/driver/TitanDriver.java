/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.corese.rdftograph.RdfToGraph.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class TitanDriver extends GdbDriver {

	TitanGraph g;

	@Override
	public void openDb(String dbPath) {
		PropertiesConfiguration configuration = null;
		File confFile = new File(dbPath + "/conf.properties");
		try {
			configuration = new PropertiesConfiguration(confFile);
		} catch (ConfigurationException ex) {
			Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
//		configuration.setProperty("storage.batch-loading", false);
		configuration.setProperty("storage.backend", "berkeleyje");
		configuration.setProperty("storage.directory", dbPath + "/db");
		configuration.setProperty("index.search.backend", "elasticsearch");
		configuration.setProperty("index.search.directory", dbPath + "/es");
		configuration.setProperty("index.search.elasticsearch.client-only", false);
		configuration.setProperty("index.search.elasticsearch.local-mode", true);
		configuration.setProperty("storage.buffer-size", 50_000);
		configuration.setProperty("ids.block-size", 1_000_000);
		try {
			configuration.save();
		} catch (ConfigurationException ex) {
			Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
		}

		g = TitanFactory.open(configuration);
		makeIfNotExistProperty(EDGE_P);
		makeIfNotExistProperty(VERTEX_VALUE);
		makeIfNotExistProperty(EDGE_G);
		createIndexes();
	}

	void makeIfNotExistProperty(String propertyName) {
		g.tx().rollback();
		ManagementSystem manager = (ManagementSystem) g.openManagement();
		if (!manager.containsPropertyKey(propertyName)) {
			manager.makePropertyKey(propertyName).dataType(String.class).make();
		}
		manager.commit();
	}

	private void createIndexes() {
		try {
			g.tx().commit();
			g.tx().rollback();
			ManagementSystem manager = (ManagementSystem) g.openManagement();
			if (!manager.containsGraphIndex("byVertexValue") && !manager.containsGraphIndex("byEdgeValue")) {
//				PropertyKey vertexValue = manager.getPropertyKey(VERTEX_VALUE);
				PropertyKey graphKey = manager.getPropertyKey(EDGE_G);
				PropertyKey subjectKey = manager.getPropertyKey(EDGE_S);
				PropertyKey predicateKey = manager.getPropertyKey(EDGE_P);
				PropertyKey objectKey = manager.getPropertyKey(EDGE_O);

//				manager.buildIndex("byVertexValue", Vertex.class).addKey(vertexValue, Mapping.STRING.asParameter()).buildMixedIndex("search");
//				manager.buildIndex("byEdgeValue", Edge.class).addKey(graphKey, Mapping.STRING.asParameter()).buildMixedIndex("search");
//				manager.buildIndex("byContextValue", Edge.class).addKey(predicateKey, Mapping.STRING.asParameter()).buildMixedIndex("search");
				manager.buildIndex("allIndex", Edge.class).
					addKey(predicateKey, Mapping.STRING.asParameter()).
					addKey(subjectKey, Mapping.STRING.asParameter()).
					addKey(objectKey, Mapping.STRING.asParameter()).
					addKey(graphKey, Mapping.STRING.asParameter()).
					buildMixedIndex("search");
				manager.commit();

				String[] indexNames = {
//					"byVertexValue", 
//					"byEdgeValue", 
//					"byContextValue", 
					"allIndex"
				};
				for (String indexName : indexNames) {
					manager.awaitGraphIndexStatus(g, indexName).call();
					manager = (ManagementSystem) g.openManagement();
					manager.updateIndex(manager.getGraphIndex(indexName), SchemaAction.REINDEX).get();
					manager.commit();

				}
			}

		} catch (Exception ex) {
			Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void closeDb() {

		g.close();
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
		Object result = null;
		String nodeId = nodeId(v);
		if (alreadySeen.containsKey(nodeId)) {
			return alreadySeen.get(nodeId);
		}
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				Vertex newVertex = g.addVertex(RDF_VERTEX_LABEL);
				newVertex.property(VERTEX_VALUE, v.stringValue());
				newVertex.property(KIND, RdfToGraph.getKind(v));
				result = newVertex.id();
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				Vertex newVertex = g.addVertex(RDF_VERTEX_LABEL);
				newVertex.property(VERTEX_VALUE, l.getLabel());
				newVertex.property(TYPE, l.getDatatype().toString());
				newVertex.property(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					newVertex.property(LANG, l.getLanguage().get());
				}
				result = newVertex.id();
				break;
			}
		}
		alreadySeen.put(nodeId, result);
		return result;
	}

	@Override
	public Object createRelationship(Object source, Object object, String predicate, Map<String, Object> properties) {
		Object result = null;
		Vertex vSource = g.vertices(source).next();
		Vertex vObject = g.vertices(object).next();
		ArrayList<Object> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key));
		});
		p.add(EDGE_P);
		p.add(predicate);
		p.add(EDGE_S);
		p.add(vSource.property(VERTEX_VALUE).toString());
		p.add(EDGE_O);
		p.add(vObject.property(VERTEX_VALUE).toString());

		Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
		result = e.id();
		return result;
	}

	@Override
	public void commit() {
		g.tx().commit();
	}
}
