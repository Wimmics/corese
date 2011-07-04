package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
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
	
	
	
	
	
	
	
	
	

}
