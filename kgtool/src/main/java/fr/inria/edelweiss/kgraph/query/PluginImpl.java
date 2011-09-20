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
	Loader ld;
	
	PluginImpl(Graph g){
		graph = g;
	}
	
	
	public static PluginImpl create(Graph g){
		return new PluginImpl(g);
	}
	
	
	
	public Object function(Expr exp, Environment env) {
		
		switch (exp.oper()){
		
		case GRAPH:
			return graph();	
			
		case SIM:						
			// solution similarity
			return similarity(env);						
		}
		
		return null;
	}
	
	
	
	public Object function(Expr exp, Environment env, Object o) {
		
		switch (exp.oper()){
		
		case KGRAM:
			return kgram(o);
			
		case NODE:
			return node(o);
			
		case GET:
			return getObject(o);	
			
		case SET:
			 return setObject(o);	
			 
		case LOAD:
			return load(o);
		
		case DEPTH:
			return depth(o);
			 						
		}
		return null;
	}
	
	public Object function(Expr exp, Environment env, Object o1, Object o2) {
		
		switch (exp.oper()){
		
		case SET:
			return setObject(o1, o2);	
			
		case SIM:
				// class similarity
			return similarity((IDatatype) o1, (IDatatype) o2);
						
		}
		
		return null;
	}
	
	
	public Object eval(Expr exp, Environment env, Object[] args) {
		
		switch (exp.oper()){
		
		}
		
		return null;
	}
	
	
	
	IDatatype similarity(IDatatype dt1, IDatatype dt2 ){		
		Node n1 = graph.getNode(dt1.getLabel());
		Node n2 = graph.getNode(dt2.getLabel());
		if (n1 == null || n2 == null) return null;
		
		Distance distance = graph.setDistance();
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
		Distance distance = graph.setDistance();

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
		if (n == null || n.getObject()== null) return null;
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
