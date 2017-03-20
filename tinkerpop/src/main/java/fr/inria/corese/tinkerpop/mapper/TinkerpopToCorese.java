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

/**
 *
 * @author edemairy
 */
public class TinkerpopToCorese {

	public TinkerpopToCorese(Graph g) {
	}

	/**
	 * Returns a Corese Entity from a Tinkerpop edge.
	 *
	 * @param e
	 */
	public Entity buildEntity(Edge e) {
		String graph = e.value(EDGE_G);
		Entity result = EdgeQuad.create(DatatypeMap.createResource(graph),
			unmapNode(e.outVertex()),
			DatatypeMap.createResource((String) e.value(EDGE_P)),
			unmapNode(e.inVertex())
		);
		return result;
	}

	public Node unmapNode(Vertex node) {
		String id = (String) node.value(VERTEX_VALUE);
		switch ((String) node.value(KIND)) {
			case IRI:
//				return coreseGraph.createNode((String) node.value(VERTEX_VALUE));
				return DatatypeMap.createResource(id);
			case BNODE:
//				return coreseGraph.createBlank((String) node.value(VERTEX_VALUE));
				return DatatypeMap.createBlank(id);
			case LITERAL:
				String label = (String) node.value(VERTEX_VALUE);
				String type = (String) node.value(TYPE);
				VertexProperty<String> lang = node.property(LANG);
				if (lang.isPresent()) {
//					return coreseGraph.addLiteral(label, type, lang.value());
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
//					return coreseGraph.addLiteral(label, type);
					return DatatypeMap.createLiteral(label, type);
				}
			case LARGE_LITERAL:
				label = (String) node.value(VERTEX_LARGE_VALUE);
				type = (String) node.value(TYPE);
				lang = node.property(LANG);
				if (lang.isPresent()) {
//					return coreseGraph.addLiteral(label, type, lang.value());
					return DatatypeMap.createLiteral(label, type, lang.value());
				} else {
//					return coreseGraph.addLiteral(label, type);
					return DatatypeMap.createLiteral(label, type);
				}
			default:
				throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
		}
	}
}
