/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.tinkerpop.mapper;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeQuad;
import fr.inria.edelweiss.kgraph.core.Graph;
import org.apache.tinkerpop.gremlin.structure.Edge;
import static fr.inria.corese.tinkerpop.mapper.Mapper.*;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

/**
 *
 * @author edemairy
 */
public class TinkerpopToCorese {

	private Graph coreseGraph;

	public TinkerpopToCorese(Graph g) {
		this.coreseGraph = g;
	}

	/**
	 * Returns a Corese Entity from a Tinkerpop edge.
	 *
	 * @param e
	 */
	public Entity buildEntity(Edge e) {
		String context = e.value(CONTEXT);
		Entity result = EdgeQuad.create(
			coreseGraph.createNode(context),
			unmapNode(e.outVertex()),
			coreseGraph.createNode((String) e.value(VALUE)),
			unmapNode(e.inVertex())
		);
		return result;
	}

	private Node unmapNode(Vertex node) {
		switch ((String) node.value(KIND)) {
			case IRI:
				return coreseGraph.createNode((String) node.value(VALUE));
			case BNODE:
				return coreseGraph.createBlank((String) node.value(VALUE));
			case LITERAL:
				String label = (String) node.value(VALUE);
				String type = (String) node.value(TYPE);
				VertexProperty<String> lang = node.property(LANG);
				if (lang.isPresent()) {
					return coreseGraph.addLiteral(label, type, lang.value());
				} else {
					return coreseGraph.addLiteral(label, type);
				}
			default:
				throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
		}
	}
}
