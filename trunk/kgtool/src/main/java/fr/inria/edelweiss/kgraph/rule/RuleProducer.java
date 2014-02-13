package fr.inria.edelweiss.kgraph.rule;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;

/**
 * Draft Producer for RuleEngine
 * Manage a graph for the rdf dataset and a graph for edges created by rules
 * Manage two Producers for the graphs
 * Manage the number of loops in the rule base
 * After first loop, it may look up only newly created edges
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 * 
 */
public class RuleProducer extends MetaProducer {
	
	ProducerImpl p1, p2;
	int loop = 0;
	List<Entity> list;

	RuleProducer() {
	}

	RuleProducer(Graph g, Graph ng) {
		p1 = ProducerImpl.create(g);
		p2 = ProducerImpl.create(ng);
		add(p1);
		add(p2);
	}
	
	public static RuleProducer create(Graph g, Graph ng){
		return new RuleProducer(g, ng);
	}
	
	public void setMode(int n){
		loop = n;
	}
	
	/**
	 * ** Rules: 24105 24105 2.614s  vs  1.805s
	 */
	public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge,  Environment env){
		Memory memory = (Memory) env;
		int n = env.getQuery().nbEdges();
		
		if (loop == 0){
			// first loop: rdf dataset only (new graph is empty)
			return p1.getEdges(gNode, from, qEdge, env);
		}
		else if (n == 3 && qEdge.getIndex() == 1 ){
			/**
			 use case:
			 construct {?x rdf:type ?d}
			 where {(0) ?p rdfs:domain ?d  (1) ?x ?p ?y  (2) not {(2) ?x rdf:type ?d}}
			 when loop >=1 consider only new edges for (1)
			 because old edges have been typed by loop 0
			 this works only if domain was not infered a loop before, 
			 in which case we should consider old edges
			 * 
			 */
			return p2.getEdges(gNode, from, qEdge, env);
		}

		// dataset and new edges:
		return super.getEdges(gNode, from, qEdge, env);
		
		
	}

}
