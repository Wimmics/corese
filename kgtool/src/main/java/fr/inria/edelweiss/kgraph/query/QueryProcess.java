package fr.inria.edelweiss.kgraph.query;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.Transformer;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;


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
	ReentrantReadWriteLock lock;
	// Producer may perform match locally
	boolean isMatch = false;
		
	public QueryProcess (){
	}
	
	
	protected QueryProcess (Producer p, Evaluator e, Matcher m){
		super(p, e, m);
		init();
	}
	
	void init(){
		if (isSort && producer instanceof ProducerImpl){
			ProducerImpl pp = (ProducerImpl) producer;
			set(SorterImpl.create(pp.getGraph()));
		}
		
		Graph g = getGraph();
		if (g != null){
			lock = g.getLock();
		}
		else {
			// TODO: the lock should be unique to all calls
			// hence it should be provided by Producer
			lock = new ReentrantReadWriteLock();
		}
	}


	public static QueryProcess create(Graph g){
		return create(g, false);
	}
	
	/**
	 * isMatch = true: 
	 * Each Producer perform local Matcher.match() on its own graph for subsumption
	 * Hence each graph can have its own ontology 
	 * isMatch = false: (default)
	 * Global producer perform Matcher.match()
	 */
	public static QueryProcess create(Graph g, boolean isMatch){
		ProducerImpl p =  ProducerImpl.create(g);
		p.setMatch(isMatch);
		QueryProcess exec = QueryProcess.create(p);
		exec.setMatch(isMatch);
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
	
	void setMatch(boolean b){
		isMatch = b;
	}
        
	public Producer add(Graph g){
		ProducerImpl p = ProducerImpl.create(g);
		Matcher match  =  MatcherImpl.create(g);
		p.set(match);
		if (isMatch){
			p.setMatch(true);
		}
		add(p);
		return p;
	}
	
	public static QueryProcess create(ProducerImpl prod){
		Matcher match =  MatcherImpl.create(prod.getGraph());
		prod.set(match);
		if (prod.isMatch()){
			// there is local match in Producer
			// create global match with Relax mode 
			match =  MatcherImpl.create(prod.getGraph());
			match.setMode(Matcher.RELAX);
		}
		QueryProcess exec = QueryProcess.create(prod,  match);
		return exec;
	}
	
	public static QueryProcess create(Producer prod, Matcher match){
		Interpreter eval  = createInterpreter(prod, match);
		QueryProcess exec = new QueryProcess(prod, eval, match);
		exec.set(ProviderImpl.create());
 		return exec;
	}
	
	public static QueryProcess create(Producer prod, Evaluator eval, Matcher match){
		QueryProcess exec = new QueryProcess(prod, eval, match);
		exec.set(ProviderImpl.create());
		return exec;
	}
	
	public static Interpreter createInterpreter(Producer p, Matcher m){
		Interpreter eval  = interpreter(p);
		Graph g = sGetGraph(p);
		if (g != null){
			eval.getProxy().setPlugin(PluginImpl.create(g, m));
		}
		return eval;
	}
	
	
	/****************************************************************
	 * 
	 * API for query
	 * 
	 ****************************************************************/
	
	public Mappings update(String squery) throws EngineException{
		return query(squery, null, null);
	}
	
	public Mappings query(String squery) throws EngineException{
		return query(squery, null, null);
	}
	
	/**
	 * Prefer Dataset below
	 * @deprecated
	 */
	public Mappings query(String squery, Mapping map, List<String> defaut, List<String> named) throws EngineException{
		Dataset ds = Dataset.create(defaut, named);
		return query(squery, map, ds);
	}	
	
	/**
	 * defaut and named specify a Dataset
	 * if the query has no from/using (resp. named), kgram use defaut (resp. named) if it exist
	 * for update, defaut is also used in the delete clause (when there is no with in the query)
	 * W3C sparql test cases use this function
	 */
	public Mappings query(String squery, Mapping map, Dataset ds) throws EngineException{
		Query q = compile(squery, ds);
		return query(q, map, ds);
	}	
	
	public Mappings query(String squery, Dataset ds) throws EngineException{
		return query(squery, null, ds);
	}
	
	public Mappings query(String squery, Mapping map) throws EngineException{
		return query(squery, map, null);
	}
	
	
	/**
	 * defaut and named specify a Dataset
	 * if the query has no from/using (resp. using named), kgram use this defaut (resp. named) if it exist
	 * for update, this using is *not* used in the delete clause 
	 * W3C sparql protocol use this function
	 */

	
	public Mappings query(Query q) {
		return qquery(q, null);
	}

	public Mappings qquery(Query q, Mapping map) {
		try {
			return query(q, map, null);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Mappings.create(q);
	}
	
	
	
	/**
	 * KGRAM + full SPARQL compliance :
	 * - type of arguments of functions (e.g. sparql regex require string)
	 * - variable in select with group by
	 * - specify the dataset
	 */
	public Mappings sparql(String squery, Dataset ds) throws EngineException{
		return sparqlQueryUpdate(squery, ds, STD_ENTAILMENT);
	}
	
	public Mappings sparql(String squery, Dataset ds, int entail) throws EngineException{
		return sparqlQueryUpdate(squery, ds, entail);
	}
	
	
	public Mappings query(ASTQuery ast){
		if (ast.isUpdate()){
			return update(ast);
		}
		return synQuery(ast);
	}
	
	/**
	 * equivalent of std query(ast) but for update
	 */
	public Mappings update(ASTQuery ast){
		Transformer transformer =  transformer();
		Query query = transformer.transform(ast);
		return query(query);
	}
	
	
	/******************************************
	 * 
	 * Secure Query OR Update
	 * 
	 ******************************************/
	
	public Mappings sparqlQuery(String squery) throws EngineException{
		Query q = compile(squery, null);
		if (q.isUpdate()){
			throw new EngineException("Unauthorized Update in SPARQL Query:\n" + squery);
		}
		return query(q);
	}
	
	public Mappings sparqlUpdate(String squery) throws EngineException{
		Query q = compile(squery, null);
		if (! q.isUpdate()){
			throw new EngineException("Unauthorized Query in SPARQL Update:\n" + squery);
		}
		return query(q);
	}

	public Mappings sparqlQueryUpdate(String squery) throws EngineException{
		return query(squery);
	}

	
	/****************************************************************************
	 * 
	 * 
	 ****************************************************************************/
	
	Mappings query(Query q, Mapping map, Dataset ds) throws EngineException{
		
		pragma(q);

		if (q.isUpdate()){
			log(Log.UPDATE, q);
			return synUpdate(q, ds);
		}
		else {
			Mappings lMap =  synQuery(q, map);

			if (q.isConstruct()){
				// construct where
				construct(lMap);
			}
			log(Log.QUERY, q, lMap);
			return lMap;
		}
	}
	
	Mappings synQuery(Query query, Mapping m) {
		try {
			readLock();
			return query(query, m);
		}
		finally {
			readUnlock();
		}
	}
	
	void log(int type, Query q){
		Graph g = getGraph();
		if (g != null){
			g.log(type, q);
		}
	}
	
	void log(int type, Query q, Mappings m){
		Graph g = getGraph();
		if (g != null){
			g.log(type, q, m);
		}
	}
	
	
	
	Mappings synUpdate(Query query,  Dataset ds) throws EngineException{
		try {
			writeLock();
			return update(query, ds);
		}
		finally {
			writeUnlock();
		}
	}
	
	/**
	 * from and named (if any) specify the Dataset over which update take place
	 * where {}  clause is computed on this Dataset
	 * delete {} clause is computed on this Dataset
	 * insert {} take place in Entailment.DEFAULT, unless there is a graph pattern or a with
	 * 
	 * This explicit Dataset is introduced because Corese manages the default graph as the union of
	 * named graphs whereas in some case (W3C test case, protocol) there is a specific default graph
	 * hence, ds.getFrom() represents the explicit default graph
	 * 
	 */
	Mappings update(Query query,  Dataset ds) throws EngineException{
		if (ds!=null && ds.isUpdate()) {
			// TODO: check complete() -- W3C test case require += default + entailment + rule
			complete(ds);
		}
		ManagerImpl man = ManagerImpl.create(this, ds);
		UpdateProcess up = UpdateProcess.create(man);
		up.setDebug(isDebug());
		Mappings lMap = up.update(query);
		lMap.setGraph(getGraph());
		return lMap;
	}
	
	
	void complete(Dataset ds){
		if (ds != null && ds.getFrom() != null){
			// add the default graphs where insert or entailment may have been done previously
			for (String src : Entailment.GRAPHS){
				ds.addFrom(src);
			}		
		}
	}
	
	

	
	Mappings synQuery(ASTQuery ast){
		try {
			readLock();
			return super.query(ast);
		}
		finally {
			readUnlock();
		}
	}
	

	
	/**
	 * Called by Manager (delete/insert operations)
	 * query is the global Query
	 * ast is the current update action
	 */
	public Mappings update(Query query, ASTQuery ast, Dataset ds) {
		Mappings lMap = super.query(ast, ds);
		Query q = lMap.getQuery();
		
		// PRAGMA: update can be both delete & insert
		if (q.isDelete()){
			delete(lMap, ds);
		}
		if (q.isConstruct()){ 
			// insert
			construct(lMap);
		}
		
		return lMap;
	}
	

		
	/**
	 * Implement SPARQL compliance
	 */
	Mappings sparqlQueryUpdate(String squery, Dataset ds, int entail) throws EngineException{
		getEvaluator().setMode(Evaluator.SPARQL_MODE);

		if (entail != STD_ENTAILMENT){
			// include RDF/S entailments in the default graph
			if (ds == null){
				ds = Dataset.create();
			}
			if (ds.getFrom() == null){
				ds.defFrom();
			}
			complete(ds);
		}
		
		// SPARQL compliance
		ds.complete();
		
		Mappings map =  query(squery, null, ds);
		
		if (! map.getQuery().isCorrect()){
			map.clear();
		}
		return map;
	}
	
	public Graph getGraph(Mappings map){
		return (Graph) map.getGraph();
	}
	
	public Graph getGraph(){
		return sGetGraph(getProducer());
	}
				
	static Graph sGetGraph(Producer p){
		Graph g = getGraph(p);
		if (g != null){
			return g;
		}
		else if (p instanceof MetaProducer){
			return getGraph(((MetaProducer)p).getProducer());
		}
		return null;	
	}
	
	static Graph getGraph(Producer p){
		if (p instanceof ProducerImpl){
			return ((ProducerImpl) p).getGraph();
		}
		return null;
	}
	
	
	/**
	 * construct {} where {} 			

	 */
	
	public void construct(Mappings lMap){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph gg;
		if (getAST(query).isAdd()){
			Graph g = getGraph();
			gg = cons.insert(lMap, g);
		}
		else {
			gg = cons.construct(lMap);
		}
		lMap.setGraph(gg);
	}
	
	
	void delete(Mappings lMap, Dataset ds){
		Query query = lMap.getQuery();
		Construct cons =  Construct.create(query);
		cons.setDebug(isDebug() || query.isDebug());
		Graph g = getGraph();
		Graph gg = cons.delete(lMap, g, ds);
		lMap.setGraph(gg);
	}
	
	
	
	
	
	void pragma(Query query){
		ASTQuery ast = (ASTQuery) query.getAST();
		if (ast!=null && ast.getPragma() != null){
			 PragmaImpl.create(this, query).parse();
		}
	}
	
	
	
	/*************************************************/
	
	private Lock getReadLock(){
		return lock.readLock(); 
	}
	
	private Lock getWriteLock(){
		return lock.writeLock(); 
	}
	
	private void readLock(){
		getReadLock().lock();
	}

	private void readUnlock(){
		getReadLock().unlock();
	}

	private void writeLock(){
		getWriteLock().lock();
	}

	private void writeUnlock(){
		getWriteLock().unlock();
	}


	
}
