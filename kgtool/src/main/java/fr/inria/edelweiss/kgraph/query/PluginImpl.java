package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.Entailment;


/**
 * Plugin for filter evaluator 
 * Compute semantic similarity of classes and solutions for KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {
	
	Graph graph;
	Distance distance;
	
	PluginImpl(Graph g){
		graph = g;
	}
	
	
	public static PluginImpl create(Graph g){
		return new PluginImpl(g);
	}
	
	
	
	public Object eval(Expr exp, Environment env, Object[] args) {
		switch (exp.oper()){
		
		case SIM:
			if (distance == null){
				distance = Distance.create(graph);
			}
			
			if (args.length == 2){
				// class similarity
				return similarity((IDatatype) args[0], (IDatatype) args[1]);
			}
			else {
				// solution similarity
				return similarity(env);
			}

		}
		
		return null;
	}
	
	
	
	IDatatype similarity(IDatatype dt1, IDatatype dt2 ){		
		Node n1 = graph.getNode(dt1.getLabel());
		Node n2 = graph.getNode(dt2.getLabel());
		if (n1 == null || n1 == null) return null;
		
		double dd = distance.similarity(n1, n2);
		return getValue(dd);
	}
	
	
	/**
	 * Similarity of a solution with Corese method
	 * Sum distance of approximate types
	 * Divide by number of nodes and edge
	 * 
	 */
	public IDatatype similarity(Environment env){
		if (! (env instanceof Memory)) return getValue(0);
		Memory memory = (Memory) env;
		Entailment ee = graph.getEntailment();
		Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
		
		// number of node + edge in the answer
		int count = 0;
		float dd = 0;
		
		for (Edge qEdge : memory.getQueryEdges()){

			if (qEdge != null){
				Edge edge = memory.getEdge(qEdge);
				
				if (edge != null){
					count += 1 ;
					
					for (int i=0; i<edge.nbNode(); i++){
						// count nodes only once
						Node n = edge.getNode(i);
						if (! visit.containsKey(n)){
							count += 1;
							visit.put(n, true);
						}
					}
					
					if (qEdge.getLabel().equals(RDFTYPE) && qEdge.getNode(1).isConstant()){

						Node qtype = graph.getNode(qEdge.getNode(1).getLabel());
						Node ttype = graph.getNode(edge.getNode(1).getLabel());

						if (qtype == null){
							// query type is undefined in ontology
							if (ttype == null){

							}
							else if (qtype.getLabel().equals(ttype.getLabel())){
								// OK
							}
							else {
								// distance max ?
							}
						}
						else if (ttype != null){ 

							if (! ee.isSubClassOf(ttype, qtype)){
								dd += distance.distance(ttype, qtype);
							}
						}
					}
				}
			}
		}
		
		if (dd == 0) return getValue(1);
		
		double sim = distance.similarity(dd , count);
		
		return getValue(sim);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

}
