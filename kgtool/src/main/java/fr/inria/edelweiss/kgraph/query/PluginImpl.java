package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgtool.load.LoadException;


/**
 * Plugin for filter evaluator 
 * Compute semantic similarity of classes and solutions for KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {
	
	static Logger logger = Logger.getLogger(PluginImpl.class);
	
	Graph graph;
	Distance distance;
	Loader ld;
	
	PluginImpl(Graph g){
		graph = g;
	}
	
	
	public static PluginImpl create(Graph g){
		return new PluginImpl(g);
	}
	
	
	
	public Object eval(Expr exp, Environment env, Object[] args) {
		switch (exp.oper()){
		
		case KGRAM:
			return kgram(args[0]);
		
		case GRAPH:
			return graph();
			
		case NODE:
			return node(args[0]);
			
		case GET:
			return getObject(args[0]);	
			
		case SET:
			if (args.length==2)
				 return setObject(args[0], args[1]);	
			else return setObject(args[0]);	
			
		case LOAD:
			return load(args[0]);
		
		case DEPTH:
			return depth(args[0]);
		
		case SIM:
			if (distance == null){
				distance = graph.setDistance();
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
							qtype = qEdge.getNode(1);
						}
						if (ttype == null){
							// target type is undefined in ontology
							ttype = edge.getNode(1);
						}
						
						if (! ee.isSubClassOf(ttype, qtype)){
							dd += distance.distance(ttype, qtype);
						}						
					}
				}
			}
		}
		
		if (dd == 0) return getValue(1);
		
		double sim = distance.similarity(dd , count);
		
		return getValue(sim);
		
	}
	
	Object getObject(Object o){
		Node n = node(o);
		return n.getObject();
	}
	
	IDatatype setObject(Object o, Object v){
		Node n = node(o);
		n.setObject(v);
		return TRUE;
	}
	
	IDatatype setObject(Object o){
		Node n = node(o);
		n.setObject(null);
		return TRUE;
	}
	
	Node node(Object o){
		IDatatype dt = (IDatatype) o;
		Node n = graph.getNode(dt.getLabel());
		return n;
	}
	
	IDatatype depth(Object o){
		Node n = node(o);
		if (n == null) return getValue(-1);
		IDatatype d = getValue((Integer) n.getObject());
		return d;
	}
	
	Graph graph(){
		return graph;
	}
	
	IDatatype load(Object o){
		loader();
		IDatatype dt = (IDatatype) o;
		try {
			ld.loadWE(dt.getLabel());
		} catch (LoadException e) {
			logger.error(e);
			return FALSE;
		}
		return TRUE;
	}
	
	void loader(){
		if (ld == null){
			ld = ManagerImpl.getLoader();
			ld.init(graph);
		}
	}
	
	Mappings kgram(Object o){
		IDatatype dt = (IDatatype) o;
		String query = dt.getLabel();
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			return map;
		} catch (EngineException e) {
			return new Mappings();
		}
	}
	

}
