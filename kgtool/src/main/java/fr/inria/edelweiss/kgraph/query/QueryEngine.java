package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Equivalent of RuleEngine for Query
 * Run a set of query
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryEngine {
	private static Logger logger = Logger.getLogger(QueryEngine.class);	
	
	Graph graph;
	QueryProcess exec;
	ArrayList<Query> list;
	
	boolean isDebug = false;
	
	QueryEngine(Graph g){
		graph = g;
		exec = QueryProcess.create(g);
		list = new ArrayList<Query>();
	}
	
	public static QueryEngine create(Graph g){
		return new QueryEngine(g);
	}

	public void setDebug(boolean b){
		isDebug = b;
	}
	
	public void addQuery(String q)  {
		 try {
			defQuery(q);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Query defQuery(String q) throws EngineException {
		Query qq = exec.compile(q);
		if (qq != null) {
			list.add(qq);
			return qq;
		}
		return null;
	}
	
	public List<Query> getQueries(){
		return list;
	}
	
	
	public void process(){
		for (Query q : list){
			if (isDebug){
				q.setDebug(isDebug);
			}
			Mappings map = exec.query(q);
			if (isDebug){
				logger.debug(map + "\n");
			}
		}
	}
	
	
	
	public Mappings process(Query q, Mapping m){
		try {
			Mappings map = exec.query(q, m, null);
			return map;
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Mappings.create(q);
	}

	/**
	 * pname is property name
	 * queries are construct where
	 * find a query with construct {?x pname ?y}
	 * process the query
	 * use case: ProducerImpl getEdges() computed by construct-where 
	 */
	Mappings process(Node start, String pname, int index){
		for (Query q : getQueries()){
			
			if (q.isConstruct()){
				Exp cons = q.getConstruct();
				for (Exp ee : cons.getExpList()){

					if (ee.isEdge()){
						Edge edge = ee.getEdge();
						if (edge.getLabel().equals(pname)){

							Mapping bind = null;
							if (start != null) {
								bind = Mapping.create(edge.getNode(index), start);
							}

							Mappings map = process(q, bind);
							return map;
						}
					}
				}
			}
		}
		return null;
	}

	
	
	

}
