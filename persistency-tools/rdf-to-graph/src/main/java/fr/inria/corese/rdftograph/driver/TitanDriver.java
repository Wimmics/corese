/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import fr.inria.corese.rdftograph.RdfToGraph;
import static fr.inria.corese.rdftograph.RdfToGraph.BNODE;
import static fr.inria.corese.rdftograph.RdfToGraph.CONTEXT;
import static fr.inria.corese.rdftograph.RdfToGraph.EDGE_VALUE;
import static fr.inria.corese.rdftograph.RdfToGraph.IRI;
import static fr.inria.corese.rdftograph.RdfToGraph.KIND;
import static fr.inria.corese.rdftograph.RdfToGraph.LANG;
import static fr.inria.corese.rdftograph.RdfToGraph.LITERAL;
import static fr.inria.corese.rdftograph.RdfToGraph.RDF_EDGE_LABEL;
import static fr.inria.corese.rdftograph.RdfToGraph.RDF_VERTEX_LABEL;
import static fr.inria.corese.rdftograph.RdfToGraph.TYPE;
import static fr.inria.corese.rdftograph.RdfToGraph.VERTEX_VALUE;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.structure.Direction;
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
		BaseConfiguration configuration = new BaseConfiguration();
//		configuration.setProperty("storage.batch-loading", false);
		configuration.setProperty("storage.backend", "berkeleyje");
		configuration.setProperty("storage.directory", dbPath);
//		configuration.setProperty("schema.default", "default");
		configuration.setProperty("storage.buffer-size", 50_000);
		configuration.setProperty("ids.block-size", 1_000_000);
		configuration.setProperty("index.search.backend", "elasticsearch");
		configuration.setProperty("index.search.directory", dbPath + "/es");
		configuration.setProperty("index.search.elasticsearch.client-only", false);
		configuration.setProperty("index.search.elasticsearch.local-mode", true);

		g = TitanFactory.open(dbPath + "/conf.properties");

		g.tx().rollback();
		ManagementSystem manager = (ManagementSystem) g.openManagement();
		manager.makePropertyKey(EDGE_VALUE).dataType(String.class).make();
		manager.makePropertyKey(VERTEX_VALUE).dataType(String.class).make();
		manager.commit();

	}

	private void createIndexes() {
		g.tx().rollback();
		ManagementSystem manager = (ManagementSystem) g.openManagement();
//		if (!manager.containsEdgeLabel(RDF_EDGE_LABEL)) {
		if (false) {
			EdgeLabel rdfLabel = manager.makeEdgeLabel(RDF_EDGE_LABEL).multiplicity(Multiplicity.MULTI).make();
			PropertyKey edgeValue = manager.makePropertyKey(EDGE_VALUE).dataType(String.class).make();
			PropertyKey vertexValue = manager.makePropertyKey(VERTEX_VALUE).dataType(String.class).make();
			manager.makePropertyKey(CONTEXT).dataType(String.class).make();
			manager.makePropertyKey(KIND).dataType(String.class).make();
			manager.makePropertyKey(LANG).dataType(String.class).make();
			manager.makePropertyKey(TYPE).dataType(String.class).make();
			manager.buildIndex("byEdgeValue", Edge.class).addKey(edgeValue).buildMixedIndex("search");
			manager.buildIndex("byVertexValue", Vertex.class).addKey(vertexValue).buildMixedIndex("search");//CompositeIndex();
			manager.commit();
			boolean registered = false;
			long before = System.currentTimeMillis();
			while (!registered) {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
				}
				TitanManagement mgmt = g.openManagement();
				TitanGraphIndex idx = mgmt.getGraphIndex("byVertexValue");
				registered = true;
				for (PropertyKey k : idx.getFieldKeys()) {
					SchemaStatus s = idx.getIndexStatus(k);
					registered &= s.equals(SchemaStatus.REGISTERED);
				}
				mgmt.rollback();
			}
		}
	}

	@Override
	public void closeDb() {
		try {
			g.tx().commit();
			g.tx().rollback();
			ManagementSystem manager = (ManagementSystem) g.openManagement();
//			PropertyKey vertexValue = manager.makePropertyKey(VERTEX_VALUE).dataType(String.class).make();
			PropertyKey vertexValue = manager.getPropertyKey(VERTEX_VALUE);
//			PropertyKey edgeValue = manager.makePropertyKey(EDGE_VALUE).dataType(String.class).make();
			PropertyKey edgeValue = manager.getPropertyKey(EDGE_VALUE);
//
			manager.buildIndex("byVertexValue", Vertex.class).addKey(vertexValue, Mapping.STRING.asParameter()).buildMixedIndex("search");//CompositeIndex();
			manager.buildIndex("byEdgeValue", Edge.class).addKey(edgeValue, Mapping.STRING.asParameter()).buildMixedIndex("search");
			manager.commit();

			manager.awaitGraphIndexStatus(g, "byVertexValue").call();
			manager.awaitGraphIndexStatus(g, "byEdgeValue").call();
			manager = (ManagementSystem) g.openManagement();
			manager.updateIndex(manager.getGraphIndex("byVertexValue"), SchemaAction.REINDEX).get();
			manager.commit();
			manager = (ManagementSystem) g.openManagement();
			manager.updateIndex(manager.getGraphIndex("byEdgeValue"), SchemaAction.REINDEX).get();
			manager.commit();
		} catch (Exception ex) {
			Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
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
		p.add(EDGE_VALUE);
		p.add(predicate);
		Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
		result = e.id();
		return result;
	}

	@Override
	public void commit() {
		g.tx().commit();
	}
}
