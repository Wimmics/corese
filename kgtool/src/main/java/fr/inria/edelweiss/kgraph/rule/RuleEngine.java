package fr.inria.edelweiss.kgraph.rule;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.core.Sorter;
import fr.inria.edelweiss.kgram.tool.EnvironmentImpl;
import fr.inria.edelweiss.kgraph.api.Engine;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.QueryProcess;


/**
 * Forward Rule Engine 
 * Use construct {} where {} SPARQL Query as Rule
 * 
 * TODO:
 * This engine creates target blank nodes for rule blank nodes
 * hence it may loop:
 * 
 * construct {?x ex:rel _:b2} 
 * where {?x ex:rel ?y}
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 */
public class RuleEngine implements Engine {
	private static final String UNKNOWN = "unknown";


	private static Logger logger = Logger.getLogger(RuleEngine.class);	

	
	Graph graph;
	QueryProcess exec;
	List<Rule> rules;
	
	RTable rtable;
	
	boolean debug = false, isOptim = false;
	int loop = 0;


	private boolean isActivate = true;
	
	
	RuleEngine(){
		rules = new ArrayList<Rule>();
	}
	
	void set(Graph g){
		graph = g;
	}
	
	void set(QueryProcess p){
		exec = p;
	}
        
        public QueryProcess getQueryProcess(){
            return exec;
        }
	
	public void set(Sorter s){
		if (exec!=null){
			exec.set(s);
		}
	}
	
	public void setOptimize(boolean b){
		isOptim = b;
	}
	
	public static RuleEngine create(Graph g){
		RuleEngine eng = new RuleEngine();
		eng.set(g);
		eng.set(QueryProcess.create(g));
		return eng;
	}
	
	public static RuleEngine create(QueryProcess q){
		RuleEngine eng = new RuleEngine();
		eng.set(q);
		return eng;
	}

	public static RuleEngine create(Graph g, QueryProcess q){
		RuleEngine eng = new RuleEngine();
		eng.set(g);
		eng.set(q);
		return eng;
	}
	
	public boolean process(){
		if (graph==null){
			set(Graph.create());
		}
		//OC:
		//synEntail();
		int size = graph.size();
		entail();
		return graph.size() > size;
	}
	
	public int process(Graph g){
		set(g);
		if (exec==null){
			set(QueryProcess.create(g));
		}
		return entail();
	}
	
	public int process(Graph g, QueryProcess q){
		set(g);
		set(q);
		return entail();
	}
	
	public int process(QueryProcess q){
		if (graph==null){
			set(Graph.create());
		}
		set(q);
		return entail();
	}
	
	public Graph getGraph(){
		return graph;
	}

	
	public void setDebug(boolean b){
		debug = b;
	}
	
	public void clear(){
		rules.clear();
	}
	
	/**
	 * Define a construct {} where {} rule
	 */
	
	public Query defRule(String rule) throws EngineException {
		return defRule(UNKNOWN, rule);
	}
	
	public void defRule(Query rule)  {
		rules.add(Rule.create(UNKNOWN, rule));
	}
	
	public void addRule(String rule)  {
		 try {
			defRule(rule);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Rule> getRules(){
		return rules;
	}
	
	public Query defRule(String name, String rule) throws EngineException {
		Query qq = exec.compileRule(rule);
		
		if (! qq.isConstruct()){
			// template
			qq.setRule(false);
			ASTQuery ast = (ASTQuery) qq.getAST();
			ast.setRule(false);
		}
		
		if (qq != null){ // && qq.isConstruct()) {
			rules.add(Rule.create(name, qq));
			return qq;
		}
		return null;
	}
	
	
	int synEntail(){
		try {
			graph.writeLock().lock();
			return entail();
		}
		finally {
			graph.writeLock().unlock();
		}
	}
	
	
	/**
	 *  Process rule base at saturation 
	 *  PRAGMA: not synchronized on write lock
	 */
	public int entail(){
		
		if (isOptim){
			start();
		}

		int size = graph.size(),
		start = size;
		loop = 0;
		boolean go = true;

		// Entailment 
		graph.init();
		
		List<Entity> list = null, current;
		
		ITable t = null;		
		
		while (go){
			
			exec.getProducer().setMode(loop);
			
			// List of edges created by rules in this step (or by entailment if any)
			current = list;
			list = new ArrayList<Entity>();

			for (Rule rule : rules){
				// graph.entail() is done once before rule application
				
				if (isOptim) {
					t = record(rule);
				}
				
				if (! isOptim  || 
					(loop == 0 || accept(rule, getRecord(rule), t))){
					//OC:
					//graph.setUpdate(false);
					if (debug){
						rule.getQuery().setDebug(true);
					}
									
					process(rule, list);
				}
				
				if (isOptim) {
					setRecord(rule, t);
				}	
				
			}
			

			
			if (graph.size() > size){
				// There are new edges: entailment again
				//OC:
				//graph.entail(list);
				size = graph.size();
				loop++;
			}
			else {
				go = false;
			}
		}
		
		if (debug) logger.debug("** Rule: " + (graph.size() - start));
		
		return graph.size() - start;
		
	}
	
	

	
	/**
	 * Process one rule
	 * Store created edges into list 
	 */
	int process(Rule rule, List<Entity> list) {
		return run(rule, null, list);
	}
	
	
	int run(Rule rule, Mapping map, List<Entity> list) {

		Query qq = rule.getQuery();
		Construct cons =  Construct.create(qq, Entailment.RULE);
		cons.setRule(rule, rule.getIndex());
		cons.setInsertList(list);

		int start = graph.size();

		Mappings lMap =  exec.query(qq, map);
		cons.insert(lMap, graph, null);	

		if (debug || qq.isDebug()){
			logger.info("** Mappings: " + lMap.size());
			logger.info("** Graph: "  + graph.size());
		}

		return graph.size() - start;
	}

	
	/**
	 * current: list of newly created edges
	 * 
	 */
	int process(Rule rule, List<Entity> list, List<Entity> current) {

		int start = graph.size();
		Query qq = rule.getQuery();
		Environment env = EnvironmentImpl.create(qq);
		
		for (Entity ent : current){
			Edge qEdge = match(qq, ent.getEdge(), env);
			if (qEdge!=null){
				Mapping map = Mapping.create(qEdge, ent.getEdge());
				run(rule, map, list);
			}
		}

		return graph.size() - start;
	}
	
	
	Edge  match(Query q, Edge edge, Environment env){
		Exp body = q.getBody();
		for (Exp exp : body){
			if (exp.isEdge()){
				if (exec.getMatcher().match(exp.getEdge(), edge, env)){
					return exp.getEdge();
				}
			}
		}
		return null;
	}
	
	
	
	/****************************************************
	 * 
	 * Compute rule predicates
	 * Accept rule if some predicate has new triple in graph
	 * 
	 * **************************************************/
	
	/**
	 * Compute table of rule predicates, for all rules
	 */
	void start(){
		rtable = new RTable();
		
		for (Rule rule : rules){
			init(rule);
		}
		
	}
	
	/**
	 * Store list of predicates of this rule
	 */
	void init(Rule rule){	
		rule.set(rule.getQuery().getNodeList());
	}
	
	
	
	class ITable extends Hashtable<String, Integer>{
		
	}
	
	class RTable extends Hashtable<Rule, ITable>{
		
	}
	
	
	/**
	 * Record predicates cardinality in graph
	 */
	ITable record(Rule r){
		ITable itable = new ITable();
		
		for (Node pred : r.getPredicates()){
			int size = graph.size(pred);
			itable.put(pred.getLabel(), size);
		}
		
		return itable;
	}
	
	
	/**
	 * Rule is accepted if one of its predicate has a new triple in graph
	 */
	boolean accept(Rule rule, ITable itable, ITable iitable){
		for (Node pred : rule.getPredicates()){
			String name = pred.getLabel();
			if (iitable.get(name) > itable.get(name)){
				return true;
			}
		}
		//System.out.println("** RE: " + rule.getQuery().getAST());
		return false;
	}
	
	ITable getRecord(Rule r){
		return rtable.get(r);
	}
	
	void setRecord(Rule r, ITable t){
		rtable.put(r, t);
	}

	public void init(){
	}
	
	public void onDelete() {		
	}

	public void onInsert(Node gNode, Edge edge) {		
	}

	public void onClear() {		
	}

	public void setActivate(boolean b) {		
		isActivate = b;
	}
	
	
	public boolean isActivate(){
		return isActivate;
	}

	public void remove() {
		graph.clear(Entailment.RULE, true);
	}

	public int type() {
		return RULE_ENGINE;
	}
	
	

}
