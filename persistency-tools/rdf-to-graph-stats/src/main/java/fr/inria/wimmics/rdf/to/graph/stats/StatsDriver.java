/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.wimmics.rdf.to.graph.stats;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.model.Value;


/**
 * @author edemairy
 */
public class StatsDriver extends GdbDriver  {
	private HashSet<Integer> nodeHash = new HashSet<>();
	@Override
	public Graph openDatabase(String dbPath) {
		return null;
	}

	@Override
	public void closeDatabase() throws Exception {
		System.out.println("Size of the node hash set = "+nodeHash.size());
	}

	@Override
	public void commit() {
	}

	@Override
	public Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties) {
		nodeHash.add(sourceId.hashCode());
		nodeHash.add(objectId.hashCode());
		return null;
	}

	@Override
	public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(String key, String s, String p, String o, String g) {
		return null;
	}

	@Override
	public EdgeQuad buildEdge(Element e) {
		return null;
	}

	@Override
	public Node buildNode(Element e) {
		return null;
	}

	@Override
	public boolean isGraphNode(String label) {
		return false;
	}

}
