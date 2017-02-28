package fr.inria.corese.rdftograph.driver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.SchemaViolationException;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.database.management.GraphIndexStatusReport;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class TitanDriver extends GdbDriver {

	static final Logger logger = Logger.getLogger(TitanDriver.class.getName());

	TitanGraph g;

	String dbPath;

	@Override
	public void openDb(String dbPathTemp) {
		File f = new File(dbPathTemp);
		dbPath = f.getAbsolutePath();
		PropertiesConfiguration configuration = null;
		File confFile = new File(dbPath + "/conf.properties");
		try {
			configuration = new PropertiesConfiguration(confFile);

		} catch (ConfigurationException ex) {
			Logger.getLogger(TitanDriver.class
				.getName()).log(Level.SEVERE, null, ex);
		}
		configuration.setProperty("schema.default", "none");
		configuration.setProperty("storage.batch-loading", true);
//		configuration.setProperty("storage.batch-loading", false);
		configuration.setProperty("storage.backend", "berkeleyje");
		configuration.setProperty("storage.directory", dbPath + "/db");
		configuration.setProperty("storage.buffer-size", 50_000);
		configuration.setProperty("storage.berkeleyje.cache-percentage", 50);
//		configuration.setProperty("storage.read-only", true);
		configuration.setProperty("index.search.backend", "elasticsearch");
		configuration.setProperty("index.search.directory", dbPath + "/es");
		configuration.setProperty("index.search.elasticsearch.client-only", false);
		configuration.setProperty("index.search.elasticsearch.local-mode", true);
		configuration.setProperty("index.search.refresh_interval", 600);
		configuration.setProperty("ids.block-size", 50_000);

		configuration.setProperty("cache.db-cache", true);
		configuration.setProperty("cache.db-cache-size", 0.3);
		configuration.setProperty("cache.db-cache-time", 0);
		configuration.setProperty("cache.tx-dirty-size", 10_000);
		// to make queries faster
		configuration.setProperty("query.batch", true);
		configuration.setProperty("query.fast-property", true);
		configuration.setProperty("query.force-index", false);
		configuration.setProperty("query.ignore-unknown-index-key", true);
		try {
			configuration.save();

		} catch (ConfigurationException ex) {
			Logger.getLogger(TitanDriver.class
				.getName()).log(Level.SEVERE, null, ex);
		}
		g = TitanFactory.open(configuration);

		TitanManagement mgmt = g.openManagement();
		if (!mgmt.containsVertexLabel(RDF_VERTEX_LABEL)) {
			mgmt.makeVertexLabel(RDF_VERTEX_LABEL).make();
		}
		if (!mgmt.containsEdgeLabel(RDF_EDGE_LABEL)) {
			mgmt.makeEdgeLabel(RDF_EDGE_LABEL).multiplicity(Multiplicity.MULTI).make();
		}
		mgmt.commit();

		makeIfNotExistProperty(EDGE_P);
		makeIfNotExistProperty(VERTEX_VALUE);
		makeIfNotExistProperty(VERTEX_LARGE_VALUE);
		makeIfNotExistProperty(EDGE_G);
		makeIfNotExistProperty(EDGE_S);
		makeIfNotExistProperty(EDGE_O);
		makeIfNotExistProperty(KIND);
		makeIfNotExistProperty(TYPE);
		makeIfNotExistProperty(LANG);

		createIndexes();
	}

	void makeIfNotExistProperty(String propertyName) {
		makeIfNotExistProperty(propertyName, String.class);
	}

	void makeIfNotExistProperty(String propertyName, Class<?> c) {
		ManagementSystem manager = (ManagementSystem) g.openManagement();
		if (!manager.containsPropertyKey(propertyName)) {
			manager.makePropertyKey(propertyName).dataType(c).make();
			System.out.println("adding key " + propertyName);
			manager.commit();
		} else {
			manager.rollback();
		}
	}

	private void createIndexes() {
		ManagementSystem manager = (ManagementSystem) g.openManagement();
		if (!manager.containsGraphIndex("vertices") && !manager.containsGraphIndex("allIndex")) {
			PropertyKey vertexValue = manager.getPropertyKey(VERTEX_VALUE);
			PropertyKey kindValue = manager.getPropertyKey(KIND);
			PropertyKey typeValue = manager.getPropertyKey(TYPE);
			PropertyKey langValue = manager.getPropertyKey(LANG);

			PropertyKey graphKey = manager.getPropertyKey(EDGE_G);
			PropertyKey subjectKey = manager.getPropertyKey(EDGE_S);
			PropertyKey predicateKey = manager.getPropertyKey(EDGE_P);
			PropertyKey objectKey = manager.getPropertyKey(EDGE_O);
			TitanGraphIndex vIndex = manager.
				buildIndex("vertices", Vertex.class).
				addKey(vertexValue).
				addKey(kindValue).
				buildCompositeIndex();
			manager.
				buildIndex("allIndex", Edge.class).
				addKey(predicateKey, Mapping.STRING.asParameter()).
				addKey(subjectKey, Mapping.STRING.asParameter()).
				addKey(objectKey, Mapping.STRING.asParameter()).
				addKey(graphKey, Mapping.STRING.asParameter()).
				buildMixedIndex("search");
//g.traversal().E().has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value());
			manager.
				buildIndex("spoIndex", Edge.class).
				addKey(subjectKey).
				addKey(predicateKey).
				addKey(objectKey).
				buildCompositeIndex();

			manager.commit();
			String[] indexNames = {
				"vertices",
				"allIndex",
				"spoIndex"
			};
			for (String indexName : indexNames) {
				try {
					GraphIndexStatusReport indexStatus = ManagementSystem.awaitGraphIndexStatus(g, indexName).status(SchemaStatus.REGISTERED).call();
					logger.log(Level.INFO, "status for {0} {1}", new Object[]{indexName, indexStatus});
					manager = (ManagementSystem) g.openManagement();
					manager.updateIndex(manager.getGraphIndex(indexName), SchemaAction.REINDEX).get();
				} catch (Exception ex) {
					logger.log(Level.SEVERE, null, ex);

				} finally {
					manager.commit();
				}
			}
		}

	}

	@Override
	public void closeDb() {
		g.close();
	}

	public void reopen() {
		g.close();
		g = TitanFactory.open(dbPath + "/conf.properties");
	}

	String nodeId(Value v) {
		StringBuilder result = new StringBuilder();
		String kind = RdfToGraph.getKind(v);
		switch (kind) {
			case IRI:
			case BNODE:
				result.append("label=").append(v.stringValue()).append(";");
				result.append("value=").append(v.stringValue()).append(";");
				result.append("kind=").append(kind);
				break;
			case LARGE_LITERAL:
				Literal l = (Literal) v;
				result.append("label=").append(l.getLabel()).append(";");
				result.append("value=").append(Integer.toString(l.getLabel().hashCode()));
				result.append("large_value=").append(l.getLabel()).append(";");
				result.append("type=").append(l.getDatatype().toString()).append(";");
				result.append("kind=").append(kind);
				if (l.getLanguage().isPresent()) {
					result.append("lang=").append(l.getLanguage().get()).append(";");
				}
				break;
			case LITERAL:
				l = (Literal) v;
				result.append("label=").append(l.getLabel()).append(";");
				result.append("value=").append(l.getLabel()).append(";");
				result.append("type=").append(l.getDatatype().toString()).append(";");
				result.append("kind=").append(kind);
				if (l.getLanguage().isPresent()) {
					result.append("lang=").append(l.getLanguage().get()).append(";");
				}
				break;
		}
		return result.toString();
	}

	public void createNode(Value v) {
	}

	public Vertex createOrGetNode(Value v) {
		TitanVertex newVertex = null;
		Vertex result = getNode(v);
		if (result != null) {
			return result;
		}
		try {
			switch (RdfToGraph.getKind(v)) {
				case IRI:
				case BNODE: {
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, v.stringValue());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					break;
				}
				case LITERAL: {
					Literal l = (Literal) v;
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, l.getLabel().toString());
					newVertex.property(TYPE, l.getDatatype().toString());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						newVertex.property(LANG, l.getLanguage().get());
					}
					break;
				}
				case LARGE_LITERAL: {
					Literal l = (Literal) v;
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode()));
					newVertex.property(VERTEX_LARGE_VALUE, l.getLabel());
					newVertex.property(TYPE, l.getDatatype().toString());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						newVertex.property(LANG, l.getLanguage().get());
					}
					break;
				}
			}
			return newVertex;
		} catch (SchemaViolationException ex) {
			logger.info("ignoring a new occurence of vertex " + v);
		}
		return null;
	}

	HashMap<Value, Vertex> cache = new HashMap<>();
	int removedNodes = 0;

	public Vertex getNode(Value v) {
		GraphTraversal<Vertex, Vertex> it = null;
		if (cache.containsKey(v)) {
			return cache.get(v);
		}
		Vertex result = null;
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				it = g.traversal().V().has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
		}

		if (cache.size() > 10_000) {
			logger.info("Cleaning cache");
			cache.clear();
		}
		if (it.hasNext()) {
			result = it.next();
			cache.put(v, result);
		} else {
			result = null;
		}

		return result;
	}

	@Override
	public Object createRelationship(Value source, Value object, String predicate, Map<String, Object> properties) {
		Object result = null;
		Vertex vSource = createOrGetNode(source);
		Vertex vObject = createOrGetNode(object);

		ArrayList<String> p = new ArrayList<>();
		properties.keySet().stream().forEach((key) -> {
			p.add(key);
			p.add(properties.get(key).toString());
		});
		p.add(EDGE_S);
		String s_value = vSource.property(VERTEX_VALUE).value().toString();
		p.add(s_value);
		p.add(EDGE_P);
		p.add(predicate);
		p.add(EDGE_O);
		String o_value = vObject.property(VERTEX_VALUE).value().toString();
		p.add(o_value);

// 		@TODO: investigate wether the while loop can be replaced by a search with an index.
		GraphTraversal<Vertex, Edge> alreadyExist = g.traversal().V(vSource.id()).outE().has(EDGE_P, predicate).as("e").inV().hasId(vObject.id()).select("e");
		try {
			result = alreadyExist.next();
		} catch (NoSuchElementException ex) {
			result = null;
		}
		if (result == null) {
//		Iterator<Edge> it = vSource.edges(Direction.OUT, RDF_EDGE_LABEL);
//		GraphTraversal<Vertex, Vertex> found = g.traversal().V(vSource.id()).outE(RDF_EDGE_LABEL).has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value()).as("edge").inV().hasId(vObject.id()).select("edge");
//		if (found.hasNext()) {
//			result = found.next();
//		} else {
//			Transaction transaction = g.tx();
			Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
			result = e.id();
//			transaction.commit();
		}
		return result;
	}

	@Override
	public void commit() {
		logger.fine("#transactions = " + ((StandardTitanGraph) g).getOpenTransactions().size());
		g.tx().commit();
	}

	private void cleanDuplicates(GraphTraversal<Vertex, Vertex> it) {
		if (true) {
			return;
		}
		while (it.hasNext() && removedNodes < 100_000) {
			removedNodes++;

			it.next().remove();
		}
		if (removedNodes > 0) {
			logger.info("Nodes removed: " + removedNodes);
			g.tx().commit();
			removedNodes = 0;
		}
	}
}
