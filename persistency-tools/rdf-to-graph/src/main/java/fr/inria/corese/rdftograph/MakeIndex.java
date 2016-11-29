/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import static com.thinkaurelius.titan.core.attribute.Text.textContains;
import com.thinkaurelius.titan.core.schema.Mapping;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.VERTEX_VALUE;
import java.util.concurrent.ExecutionException;
import static org.apache.tinkerpop.gremlin.process.traversal.P.inside;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 *
 * @author edemairy
 */
public class MakeIndex {

	public static void main3(String[] args) throws InterruptedException, ExecutionException {
		String dbPath = "/Users/edemairy/tp2/btc_titandb_1000/";
		TitanGraph graph = TitanFactory.open(dbPath + "/conf.properties");
		GraphOfTheGodsFactory.load(graph);
		GraphTraversalSource g = graph.traversal();
		graph.tx().rollback();
		
		ManagementSystem mgmt = (ManagementSystem) graph.openManagement();
		PropertyKey name = mgmt.getPropertyKey("name");
		PropertyKey age = mgmt.getPropertyKey("age");
		mgmt.buildIndex("nameAndAge", Vertex.class).addKey(name).addKey(age).buildMixedIndex("search");
		mgmt.commit();
		
		mgmt.awaitGraphIndexStatus(graph, "nameAndAge").call();
		mgmt = (ManagementSystem) graph.openManagement();
		mgmt.updateIndex(mgmt.getGraphIndex("nameAndAge"), SchemaAction.REINDEX).get();
		mgmt.commit();
		g.V().has("name", textContains("hercules")).has("age", inside(20, 50));
		graph.close();
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		String dbPath = "/Users/edemairy/tp2/btc_titandb_1001/";
		TitanGraph g = TitanFactory.open(dbPath + "/conf.properties");
		ManagementSystem manager = (ManagementSystem) g.openManagement();
		PropertyKey vertexValue = manager.makePropertyKey(VERTEX_VALUE).dataType(String.class).make();
		manager.commit();

		TitanVertex newV = g.addVertex("test");
		newV.property(VERTEX_VALUE, "vertex1");
		g.tx().commit();
		g.tx().rollback();
		
		manager = (ManagementSystem) g.openManagement();
		vertexValue = manager.getPropertyKey(VERTEX_VALUE);
		manager.buildIndex("byVertexValue", Vertex.class).addKey(vertexValue, Mapping.STRING.asParameter()).buildMixedIndex("search");//CompositeIndex();
		manager.commit();

		manager.awaitGraphIndexStatus(g, "byVertexValue").call();
		manager = (ManagementSystem) g.openManagement();
		manager.updateIndex(manager.getGraphIndex("byVertexValue"), SchemaAction.REINDEX).get();
		manager.commit();
		
		g.close();
	}
}
