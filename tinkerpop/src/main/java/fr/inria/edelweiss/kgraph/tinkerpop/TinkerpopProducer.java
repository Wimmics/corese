/*
 *  Copyright Inria 2016
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import java.util.List;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer implements Producer {

	TinkerpopGraph graph;

	public TinkerpopProducer() {

	}

	@Override
	public void init(int nbNodes, int nbEdges) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setMode(int n) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getMode() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * @param gNode @TODO Not used for the moment
	 * @param from @TODO Not used for the moment
	 * @param qEdge Requested edge.
	 * @param env Provided values that can set the values for all the Nodes
	 * of the request.
	 */
	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
		Node predicate = getPredicate(qEdge, env);
		// Code suspect : aucune idée si c'est la bonne idée pour utiliser l'API de qEdge
		Node subject = qEdge.getNode(0);
		Node object = qEdge.getNode(1);
		if (predicate.isVariable() && subject.isVariable() && object.isVariable()) {
			return graph.getEdges();
		} else {
			throw new UnsupportedOperationException("not supported yet");
		}
	}

	private Node getPredicate(Edge edge, Environment env) {
		Node predicate;
		if (isVariable(edge)) {
			Node var = edge.getEdgeVariable();
			if (existNode(var, env)) {
				predicate = env.getNode(var);
			} else {
				predicate = edge.getEdgeNode();
			}
		} else {
			predicate = edge.getEdgeNode();
		}
		String name = predicate.getLabel();
		predicate = graph.getPropertyNode(name);
		return predicate;
	}

	private boolean isVariable(Edge edge) {
		return edge.getEdgeVariable() != null;
	}

	private boolean existNode(Node var, Environment env) {
		return env.getNode(var) != null;
	}

	@Override
	public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode, Environment env) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void initPath(Edge qEdge, int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp, int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start, int index) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Node getNode(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object getValue(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public DatatypeValue getDatatypeValue(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isBindable(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<Node> toNodeList(Object value) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Mappings map(List<Node> qNodes, Object object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isProducer(Node node) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Producer getProducer(Node node, Environment env) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Query getQuery() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Object getGraph() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setGraphNode(Node n) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Node getGraphNode() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
