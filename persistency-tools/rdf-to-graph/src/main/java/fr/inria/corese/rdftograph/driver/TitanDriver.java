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
import com.thinkaurelius.titan.core.schema.TitanManagement;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.BaseConfiguration;
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
		configuration.setProperty("storage.batch-loading", false);
		configuration.setProperty("storage.backend", "berkeleyje");
//		configuration.setProperty("storage.backend", "cassandrathrift");
//		configuration.setProperty("storage.hostname", "127.0.0.1");
		configuration.setProperty("storage.directory", dbPath);
		configuration.setProperty("schema.default", "default");
		configuration.setProperty("storage.buffer-size", 50_000);
		configuration.setProperty("ids.block-size", 1_000_000);
		g = TitanFactory.open(configuration);
		TitanManagement manager = g.openManagement();
		if (!manager.containsEdgeLabel(RDF_EDGE_LABEL)) {
			EdgeLabel rdfLabel = manager.makeEdgeLabel(RDF_EDGE_LABEL).multiplicity(Multiplicity.MULTI).make();
			PropertyKey edgeValue = manager.makePropertyKey(EDGE_VALUE).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			PropertyKey vertexValue = manager.makePropertyKey(VERTEX_VALUE).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			manager.makePropertyKey(CONTEXT).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			manager.makePropertyKey(KIND).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			manager.makePropertyKey(LANG).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			manager.makePropertyKey(TYPE).dataType(String.class).cardinality(Cardinality.SINGLE).make();
			manager.buildIndex("byEdgeValue", Edge.class).addKey(edgeValue).buildCompositeIndex();
			manager.buildIndex("byVertexValue", Vertex.class).addKey(vertexValue).buildCompositeIndex();
			manager.commit();
		}
	}

	@Override
	public void closeDb() {
		try {
			g.close();
		} catch (Exception ex) {
			Logger.getLogger(TitanDriver.class.getName()).log(Level.SEVERE, null, ex);
		}
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
