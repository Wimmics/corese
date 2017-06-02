/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.tinkerpop.mapper;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author edemairy
 */
public class TinkerpopToCorese {

	LoadingCache<Edge, Entity> cache;

	public TinkerpopToCorese(Graph g) {
		this.cache = CacheBuilder.newBuilder().
			maximumSize(1000000).
			expireAfterAccess(100, TimeUnit.DAYS).
			build(new CacheLoader<Edge, Entity>() {
				@Override
				public Entity load(Edge e) throws Exception {
					String graph = e.value(EDGE_G);
					Entity result = EdgeQuad.create(DatatypeMap.createResource(graph),
						unmapNode(e.outVertex()),
						DatatypeMap.createResource((String) e.value(EDGE_P)),
						unmapNode(e.inVertex())
					);
					return result;
				}
			});
	}

	/**
	 * Returns a Corese Entity from a Tinkerpop edge.
	 *
	 * @param e
	 */
	public Entity buildEntity(Edge e) {
		return cache.getUnchecked(e);
	}

	public Node unmapNode(Vertex node) {
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
