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
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.AttributeSerializer;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.diskstorage.ScanBuffer;
import com.thinkaurelius.titan.diskstorage.WriteBuffer;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.database.management.GraphIndexStatusReport;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class TitanDriver extends GdbDriver {

	static final Logger logger = Logger.getLogger(TitanDriver.class.getName());

	static class VertexValue implements Comparable<VertexValue>, AttributeSerializer<VertexValue> {

		private String kind;
		private String value;
		private Optional<String> largeValue;
		private Optional<String> type;
		private Optional<String> lang;

		public VertexValue() {
		}

		public VertexValue(Value v) {
			this.kind = RdfToGraph.getKind(v);
			switch (this.kind) {
				case BNODE:
				case IRI:
					value = v.stringValue();
					type = Optional.empty();
					lang = Optional.empty();
					break;
				case LITERAL:
					Literal l = (Literal) v;
					value = l.getLabel();
					type = Optional.of(l.getDatatype().toString());
					if (l.getLanguage().isPresent()) {
						lang = Optional.of(l.getLanguage().get());
					} else {
						lang = Optional.empty();
					}
					break;
				case LARGE_LITERAL:
					l = (Literal) v;
					value = Integer.toString(l.getLabel().hashCode());
					largeValue = Optional.of(l.getLabel());
					type = Optional.of(l.getDatatype().toString());
					if (l.getLanguage().isPresent()) {
						lang = Optional.of(l.getLanguage().get());
					} else {
						lang = Optional.empty();
					}
					break;
			}
		}

		public VertexValue setKind(String kind) {
			this.kind = kind;
			return this;
		}

		public VertexValue setValue(String value) {
			this.value = value;
			return this;
		}

		public VertexValue setLang(String lang) {
			this.lang = Optional.of(lang);
			return this;
		}

		public VertexValue setType(String type) {
			this.type = Optional.of(type);
			return this;
		}

		@Override
		public int compareTo(VertexValue o) {
			if (this.kind.compareTo(o.kind) != 0) {
				return this.kind.compareTo(o.kind);
			} else {
				switch (kind) {
					case BNODE:
					case IRI:
						return this.value.compareTo(o.value);
					case LITERAL:
						IDatatype data_this = lang.isPresent() ? DatatypeMap.createLiteral(value, kind, lang.get()) : DatatypeMap.createLiteral(value, kind);
						IDatatype data_o = o.lang.isPresent() ? DatatypeMap.createLiteral(o.value, o.kind, o.lang.get()) : DatatypeMap.createLiteral(o.value, o.kind);
						return data_this.compareTo(data_o);
					case LARGE_LITERAL:
						data_this = lang.isPresent() ? DatatypeMap.createLiteral(largeValue.get(), kind, lang.get()) : DatatypeMap.createLiteral(largeValue.get(), kind);
						data_o = o.lang.isPresent() ? DatatypeMap.createLiteral(o.largeValue.get(), o.kind, o.lang.get()) : DatatypeMap.createLiteral(o.largeValue.get(), o.kind);
				}
			}
			throw new IllegalArgumentException(format("{0} and {1} vertex values seem incomparable.", this, o));
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof VertexValue)) {
				return false;
			}
			return this.compareTo((VertexValue) o) == 0;
		}

		@Override
		public VertexValue read(ScanBuffer buffer) {
			try {
				VertexValue result = new VertexValue();
				int nbAttributes = buffer.getInt();
				result.setKind(readString(buffer));
				result.setValue(readString(buffer));
				if (result.kind.equals(LITERAL)) {
					result.setType(readString(buffer));
					if (nbAttributes == 4) {
						result.setLang(readString(buffer));
					}
				}
				return result;
			} catch (UnsupportedEncodingException ex) {
				Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
			}
			return null;
		}

		private String readString(ScanBuffer buffer) throws UnsupportedEncodingException {
			int length = buffer.getInt();
			byte[] bytes = buffer.getBytes(length);
			return new String(bytes, "UTF-8");
		}

		@Override
		public int hashCode() {
			switch (kind) {
				case BNODE:
				case IRI:
					return kind.hashCode() ^ value.hashCode();
				case LITERAL:
					if (lang.isPresent()) {
						return kind.hashCode() ^ value.hashCode() ^ type.hashCode() ^ lang.hashCode();
					} else {
						return kind.hashCode() ^ value.hashCode() ^ type.hashCode();
					}
			}
			return -1;
		}

		@Override
		public void write(WriteBuffer buffer, VertexValue attribute) {
			if (!(attribute instanceof VertexValue)) {
				throw new IllegalArgumentException();
			}
			switch (kind) {
				case BNODE:
				case IRI:
					buffer.putInt(2);
					buffer.putInt(kind.getBytes().length);
					buffer.putBytes(kind.getBytes());
					buffer.putInt(value.getBytes().length);
					buffer.putBytes(value.getBytes());
					break;
				case LITERAL:
					if (lang.isPresent()) {
						buffer.putInt(4);
					} else {
						buffer.putInt(3);
					}
					buffer.putInt(kind.getBytes().length);
					buffer.putBytes(kind.getBytes());
					buffer.putInt(value.getBytes().length);
					buffer.putBytes(value.getBytes());
					buffer.putInt(type.get().getBytes().length);
					buffer.putBytes(type.get().getBytes());
					if (lang.isPresent()) {
						buffer.putInt(lang.get().getBytes().length);
						buffer.putBytes(lang.get().getBytes());
					}
					break;
			}
		}

	}

	TitanGraph g;

	@Override
	public void openDb(String dbPathTemp) {
		File f = new File(dbPathTemp);
		String dbPath = f.getAbsolutePath();
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
		configuration.setProperty("storage.backend", "berkeleyje");
		configuration.setProperty("storage.directory", dbPath + "/db");
//		configuration.setProperty("storage.read-only", true);
		configuration.setProperty("index.search.backend", "elasticsearch");
		configuration.setProperty("index.search.directory", dbPath + "/es");
		configuration.setProperty("index.search.elasticsearch.client-only", false);
		configuration.setProperty("index.search.elasticsearch.local-mode", true);
		configuration.setProperty("index.search.refresh_interval", 600);
		configuration.setProperty("storage.buffer-size", 50_000);
		configuration.setProperty("ids.block-size", 50_000);
		configuration.setProperty("cache.db-cache-size", 0.95);
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
		mgmt.makeVertexLabel(RDF_VERTEX_LABEL).make();
		mgmt.makeEdgeLabel(RDF_EDGE_LABEL).multiplicity(Multiplicity.MULTI).make();
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
				buildCompositeIndex();
			manager.
				buildIndex("allIndex", Edge.class).
				addKey(predicateKey, Mapping.STRING.asParameter()).
				addKey(subjectKey, Mapping.STRING.asParameter()).
				addKey(objectKey, Mapping.STRING.asParameter()).
				addKey(graphKey, Mapping.STRING.asParameter()).
				buildMixedIndex("search");
			manager.
				buildIndex("pIndex", Edge.class).
				addKey(predicateKey).
				buildCompositeIndex();

			manager.commit();
			String[] indexNames = {
				"vertices",
				"allIndex",
				"pIndex"
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

	@Override
	public void createNode(Value v) {
//		StandardTitanGraph stg = (StandardTitanGraph) g;
		Vertex newVertex = null;
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
		} catch (SchemaViolationException ex) {
			logger.info("ignoring a new occurence of vertex " + v);
		}
	}

	HashMap<Value, Vertex> cache = new HashMap<>();

	@Override
	public Object getNode(Value v) {
		if (cache.containsKey(v)) {
			return cache.get(v);
		}
		Vertex result = null;
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				GraphTraversal<Vertex, Vertex> it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
				result = it.next();
				while (it.hasNext()) {
					it.next().remove();
				}
				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				GraphTraversal<Vertex, Vertex> it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				result = it.next();
				while (it.hasNext()) {
					it.next().remove();
				}
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				GraphTraversal<Vertex, Vertex> it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				result = it.next();
				while (it.hasNext()) {
					it.next().remove();
				}
				break;
			}
		}
		if (cache.size() > 200_000) {
			logger.info("Cleaning cache");
			cache.clear();
		}
		cache.put(v, result);
		return result;
	}

	@Override
	public Object createRelationship(Object source, Object object, String predicate, Map<String, Object> properties) {
		Object result = null;
		Vertex vSource = (Vertex) source;
		Vertex vObject = (Vertex) object;

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
//		GraphTraversal<Edge, Edge> alreadyExist = g.traversal().E().has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value());
//		if (alreadyExist.hasNext()) {
//			result = alreadyExist.next();
//		} else {
//		Iterator<Edge> it = vSource.edges(Direction.OUT, RDF_EDGE_LABEL);
//		GraphTraversal<Vertex, Vertex> found = g.traversal().V(vSource.id()).outE(RDF_EDGE_LABEL).has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value()).as("edge").inV().hasId(vObject.id()).select("edge");
//		if (found.hasNext()) {
//			result = found.next();
//		} else {
//			Transaction transaction = g.tx();
		Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
		result = e.id();
//			transaction.commit();
//		}
		return result;
	}

	private String serializeNode(Vertex node) {
		StringBuilder result = new StringBuilder();
		result.append(node.property(KIND).value());
		result.append("|");
		result.append(node.property(VERTEX_VALUE).value());
		if (node.property(KIND).value().equals(LITERAL)) {
			result.append("|");
			result.append(node.property(TYPE).value());
			if (node.property(LANG).isPresent()) {
				result.append("|");
				result.append(node.property(LANG).value());
			}
		}
		return result.toString();
	}

	@Override
	public void commit() {
		g.tx().commit();
	}
}
