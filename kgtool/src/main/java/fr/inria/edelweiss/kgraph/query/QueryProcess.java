package fr.inria.edelweiss.kgraph.query;


import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.Transformer;
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
import java.net.URL;


/**
 * Evaluator of SPARQL query by KGRAM
 * Implement KGRAM  as a lightweight version with KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QueryProcess extends QuerySolver {
	
	//sort query edges taking cardinality into account
	static boolean isSort = false;

	Construct constructor;
	Loader load;
		
	public QueryProcess (){
	}
	
	
	protected QueryProcess (Producer p, Evaluator e, Matcher m){
		super(p, e, m);
		if (isSort && p instanceof ProducerImpl){
			ProducerImpl pp = (ProducerImpl) p;
			set(SorterImpl.create(pp.getGraph()));
		}
	}


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
	
	public static void setSort(boolean b){
		isSort = b;
	}

	public void setLoader(Loader ld){
		load = ld;
	}
	
	public Loader getLoader(){
		return load;
	}
        
	public void add(Graph g){
		add(ProducerImpl.create(g));
	}
        
        public void addRemote(URL url){
		add(new RemoteProducerImpl(url));
	}
	
	public static QueryProcess create(ProducerImpl prod){
		Matcher match =  MatcherImpl.create(prod.getGraph());
		QueryProcess exec = QueryProcess.create(prod,  match);
		return exec;
	}
	
	public static QueryProcess create(Producer prod, Matcher match){
		Interpreter eval  = createInterpreter(prod);
		QueryProcess exec = new QueryProcess(prod, eval, match);
 		return exec;
	}
	
	public static QueryProcess create(Producer prod, Evaluator ev, Matcher match){
		QueryProcess exec = new QueryProcess(prod, ev, match);
		return exec;
	}
	
	static Interpreter createInterpreter(Producer p){
		Interpreter eval  = interpreter(p);
		if (p instanceof ProducerImpl){
			ProducerImpl pp = (ProducerImpl) p;
			eval.getProxy().setPlugin(PluginImpl.create(pp.getGraph()));
		}
		return eval;
	}
	
	public Mappings query(String squery) throws EngineException{
		return query(squery, null, null, null);
	}
	
	public Mappings query(String squery, List<String> from, List<String> named) throws EngineException{
		return query(squery, null, from, named);
	}
	
	public Mappings query(String squery, Mapping map, List<String> from, List<String> named) throws EngineException{
		Query q = compile(squery, from, named);
		return query(q, map, from, named);
	}
	
	public Mappings query(Query q) {
		try {
			return query(q, null, null, null);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Mappings.create(q);
	}
		
	public Mappings query(Query q, Mapping map, List<String> from, List<String> named) throws EngineException{
		
		pragma(q);
		
		if (q.isUpdate()){
			return update(q, from, named);
		}
		else {
			Mappings lMap =  query(q, map);

			if (q.isConstruct()){
				// construct where
				construct(lMap);
			}
			return lMap;
		}
	}
	
	
	public Mappings update(Query query,  List<String> from, List<String> named) throws EngineException{
		complete(from);
		ManagerImpl man = ManagerImpl.create(this, from, named);
		UpdateProcess up = UpdateProcess.create(man);
		up.setDebug(isDebug());
		Mappings lMap = up.update(query);
		lMap.setObject(getGraph());
		return lMap;
	}
	
	
	void complete(List<String> from){
		if (from != null){
			// add the default graphs where insert or entailment may have been done previously
			for (String src : Entailment.GRAPHS){
				if (! from.contains(src)){
					from.add(src);
				}
			}		
		}
	}
	
	/**
	 * Called by sparql update
	 */
	public Mappings query(ASTQuery ast){
		if (ast.isUpdate()){
			return update(ast);
		}
		return query(ast, null, null);
	}
	
	/**
	 * equivalent of std query(ast) but for update
	 */
	public Mappings update(ASTQuery ast){
		Transformer transformer =  transformer();
		Query query = transformer.transform(ast);
		return query(query);
	}
	
	/**
	 * Called by Manager (delete/insert operations)
	 */
	public Mappings query(ASTQuery ast, List<String> from, List<String> named) {
		Mappings lMap = super.query(ast, from, named);
		Query q = lMap.getQuery();
		
		// PRAGMA: update can be both delete & insert
		if (q.isDelete()){
			delete(lMap, from, named);
		}
		if (q.isConstruct()){ 
			// insert
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
			complete(from);
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
	
	
	
	
	
	void pragma(Query query){
		ASTQuery ast = (ASTQuery) query.getAST();
		if (ast!=null && ast.getPragma() != null){
			new PragmaImpl(this, query).parse();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
}
