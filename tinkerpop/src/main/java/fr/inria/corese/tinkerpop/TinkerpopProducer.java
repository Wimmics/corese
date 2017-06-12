/*
 *  Copyright Inria 2016
 */
package fr.inria.corese.tinkerpop;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

/**
 *
 * @author edemairy
 */
public class TinkerpopProducer extends ProducerImpl {

	private final Logger LOGGER = LogManager.getLogger(TinkerpopProducer.class.getName());
	private GdbDriver databaseDriver;
	private TinkerpopGraph graph;

	public TinkerpopProducer(TinkerpopGraph graph, GdbDriver databaseDriver) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super(graph);
		this.databaseDriver = databaseDriver;
		this.graph = graph;
	}

	/**
	 * @return An iterable
	 * @param gNode @TODO Not used for the moment
	 * @param from @TODO Not used for the moment
	 * @param qEdge Requested edge.
	 * @param env Provided values that can set the values for all the Nodes
	 * of the request.
	 */
	@Override
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {
            Exp exp = env.getExp();
            Query q = env.getQuery();
            boolean isDebug = q.isDebug();
		Node subject    = qEdge.getNode(0);
		Node object     = qEdge.getNode(1);
                Node predicate  = qEdge.getEdgeVariable();
                Node property   = (predicate==null) ? qEdge.getEdgeNode() : predicate;
                boolean propVar = predicate!=null || isPredicateFree(qEdge);

		Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter;
		StringBuilder key = new StringBuilder();

		String g = (gNode == null) ? "" : gNode.getLabel();
		key.append((gNode == null) || (gNode.getLabel().compareTo("?g") == 0) ? "?g" : "G");

		String s = updateVariable(exp, Exp.SUBJECT, subject.isVariable(), subject, env, key, isDebug);
		String p = updateVariable(exp, Exp.PREDICATE, propVar, property, env, key, isDebug);
		String o = updateVariable(exp, Exp.OBJECT, object.isVariable(), object, env, key, isDebug);
		//LOGGER.trace("in case " + key.toString());
		filter = databaseDriver.getFilter(key.toString(), s, p, o, g);
		Iterable<Entity> result = graph.getEdges(filter);
		return result;
	}
        
        /**
         * Property Path
         * n = 0 : focusNode is subject
         * n = 1 : focusNode is object
         */
        @Override
        public Iterable<Entity> getEdges(Node gNode, Node sNode, List<Node> from,
            Node predicate, Node focusNode, Node objectNode, int n) {
            String key = "?gSP?o";
            if (n == 1){
                key = "?g?sPO";
            }
            String s = (n==0) ? focusNode.getLabel() : "?s";
            String o = (n==1) ? focusNode.getLabel() : "?o";
            
            Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter;            
            filter = databaseDriver.getFilter(key, s, predicate.getLabel(), o, "?g");
            Iterable<Entity> result = graph.getEdges(filter);
            return result;   
        }

	/**
	 *
	 * @param edge
	 * @return
	 */
	private boolean isPredicateFree(Edge edge) {
		Node predicate = edge.getEdgeNode();
		String name = predicate.getLabel();
		return name.equals(Graph.TOPREL);
	}

	@Override
	public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
		Node node = env.getNode(gNode);
		if (!graph.containsCoreseNode(node)) {
			return false;
		}
		if (from.isEmpty()) {
			return true;
		}
		// @TODO what should be done.
		LOGGER.error("behaviour not defined in that case");
		return false;
		//return ei.getCreateDataFrom().isFrom(from, node);
	}

        @Override
	public void close() {
		graph.close();
	}

        String free(int rank){
            switch (rank){
                case Exp.SUBJECT: return "?s";
                case Exp.OBJECT: return "?o";
                case Exp.PREDICATE: return "?p";
            }
            return "";
        }
        
        String bound(int rank){
             switch (rank){
                case Exp.SUBJECT: return "S";
                case Exp.OBJECT: return "O";
                case Exp.PREDICATE: return "P";
            }
            return "";
        }
        
	private String updateVariable(Exp exp, int rank, boolean isVariable, Node node, Environment env, StringBuilder key, boolean isDebug) {
		String result = "";
		if (isVariable) {
			if (env.getNode(node.getLabel()) != null) {
                            // var bound
				key.append(bound(rank));
				result = env.getNode(node.getLabel()).getLabel();
			} else if (exp != null) {
                            // filter list for this edge node rank available in tinkerpop
                            // filter such as: var = cst where var is node rank and cst is constant
                            // boolean connector filter: f && f || ! f
                            List<Filter> list = exp.getFilters(rank, ExprType.TINKERPOP);
                            if (isDebug && ! list.isEmpty()){
                                System.out.println(list);
                            }
                            
                            boolean ok = false;
                            for (Filter f : list) {
                                Expr e = f.getExp();
                                if (e.match(ExprType.EQ_SAME)){
                                    // filter (var = cst) or sameTerm(var, cst)
                                    key.append(bound(rank));                                   
                                    result = e.getExp(1).getDatatypeValue().stringValue();
                                    ok = true;
                                    break;
                                }
                            }
                            if (!ok) {
                                key.append(free(rank));
                            }
			}
                        else {
                            key.append(free(rank));
                        }
		} else {
			key.append(bound(rank));
			result = node.getLabel();
		}
		return result;
	}
}
