package fr.inria.edelweiss.kgraph.query;


import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;


/**
 * Evaluator of SPARQL query by KGRAM
 * Implement KGRAM  as a lightweight version with KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryProcess extends QuerySolver {

	Construct constructor;
	//Graph graph;
	Loader load;
		
	public QueryProcess (){
	}
	
	
	protected QueryProcess (Producer p, Evaluator e, Matcher m){
		super(p, e, m);
	}
	

//	public static QueryProcess create(){
//		return new QueryProcess();
//	}
	
	public static QueryProcess create(Graph g){
		ProducerImpl p =  ProducerImpl.create(g);
		QueryProcess exec = QueryProcess.create(p);
		return exec;
	}
	
	public static QueryProcess create(Graph g, Graph g2){
		QueryProcess qp = QueryProcess.create(g);
		qp.add(g2);
		return qp;
	}

	public void setLoader(Loader ld){
		((ProducerImpl)getProducer()).setLoad(ld);
		load = ld;
	}
	
	public Loader getLoader(){
		return load;
	}
	
	public void add(Graph g){
		add(ProducerImpl.create(g));
	}
	
	public static QueryProcess create(ProducerImpl prod){
		Matcher match =  MatcherImpl.create(prod.getGraph());
		QueryProcess exec = QueryProcess.create(prod,  match);
		return exec;
	}
	
	public static QueryProcess create(Producer prod, Matcher match){
		Interpreter eval  = interpreter(prod);
		QueryProcess exec = new QueryProcess(prod, eval, match);
		return exec;
	}
	
	public static QueryProcess create(Producer prod, Evaluator ev, Matcher match){
		QueryProcess exec = new QueryProcess(prod, ev, match);
		return exec;
	}
	
	public Mappings query(String squery) throws EngineException{
		return query(squery, null, null, null);
	}
	
	public Mappings query(String squery, List<String> from, List<String> named) throws EngineException{
		return query(squery, null, from, named);
	}
	
	public Mappings query(String squery, Mapping map, List<String> from, List<String> named) throws EngineException{
		Query q = compile(squery, from, named);
		
		if (q.isUpdate()){
			UpdateProcess up = UpdateProcess.create(this);
			Mappings lMap = up.update(q, from, named);
			return lMap;
		}
		else {
			Mappings lMap =  query(q, map);

			if (q.isConstruct()){
				construct(lMap);
			}
			return lMap;
		}
	}
	
	/**
	 * Called by sparql update
	 */
	public Mappings query(ASTQuery ast){
		return query(ast, null, null);
	}
	
	public Mappings query(ASTQuery ast, List<String> from, List<String> named) {
		Mappings lMap = super.query(ast, from, named);
		Query q = lMap.getQuery();
		
		// PRAGMA: update can be both delete & insert
		if (q.isDelete()){
			delete(lMap, from, named);
		}
		if (q.isConstruct()){ 
			construct(lMap);
		}
		
		return lMap;
	}
	
	/**
	 * KGRAM + some SPARQL constraints:
	 * - type of arguments of functions (e.g. sparql regex require string)
	 * - variable in select with group by
	 * - specify the dataset
	 */
	public Mappings sparql(String squery, List<String> from, List<String> named) throws EngineException{
		return sparql(squery, from, named, STD_ENTAILMENT);
	}
	
	public Mappings sparql(String squery, List<String> from, List<String> named, int entail) throws EngineException{
		getEvaluator().setMode(Evaluator.SPARQL_MODE);

		if (entail != STD_ENTAILMENT){
			if (from == null){
				from = new ArrayList<String>();
			}
			for (String src : Entailment.GRAPHS){
				// add graphs where entailments are stored
				if (! from.contains(src)){
					from.add(src);
				}
			}		
		}
		
		if (from != null && named == null){
			named = new ArrayList<String>();
			named.add("");
		}
		else if (from == null && named != null){
			from = new ArrayList<String>();
			from.add("");
		}
		Mappings map =  query(squery, null, from, named);
		if (! map.getQuery().isCorrect()){
			map.clear();
		}
		return map;
	}

	
	public Graph getGraph(Mappings map){
		return (Graph) map.getObject();
	}
	
	public Graph getGraph(){
		return ((ProducerImpl) getProducer()).getGraph();
	}
//	
//	void setGraph(Graph g){
//		graph = g;
//	}
	
	
	/**
	 * construct {} where {} 			

	 */
	
	void construct(Mappings lMap){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph gg;
		if (getAST(query).isAdd()){
			Graph g = ((ProducerImpl) getProducer()).getGraph();
			gg = cons.insert(lMap, g);
		}
		else {
			gg = cons.construct(lMap);
		}
		lMap.setObject(gg);
	}
	
	
	void delete(Mappings lMap, List<String> from, List<String> named){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph g = getGraph();
		Graph gg = cons.delete(lMap, g, from, named);
		lMap.setObject(gg);
	}
	
	
	
	
	
	


	
	
	


}
