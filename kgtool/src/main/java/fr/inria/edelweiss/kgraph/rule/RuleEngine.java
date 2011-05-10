package fr.inria.edelweiss.kgraph.rule;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.EnvironmentImpl;
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
public class RuleEngine {
	Graph graph;
	QueryProcess exec;
	List<Rule> rules;
	boolean debug = false;
	int loop = 0;
	
	
	RuleEngine(){
		rules = new ArrayList<Rule>();
	}
	
	void set(Graph g){
		graph = g;
	}
	
	void set(QueryProcess p){
		exec = p;
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
	
	public int process(){
		if (graph==null){
			set(Graph.create());
		}
		return entail();
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
		return defRule("unknown", rule);
	}
	
	public void addRule(String rule)  {
		 try {
			defRule(rule);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Query defRule(String name, String rule) throws EngineException {
		Query qq = exec.compile(rule);
		if (qq != null && qq.isConstruct()) {
			rules.add(Rule.create(name, qq));
			return qq;
		}
		return null;
	}
	
	
	
	
	/**
	 *  Process rule base at saturation 
	 */
	int entail(){

		int size = graph.size(),
		start = size;
		loop = 0;
		boolean go = true;

		// Entailment 
		graph.init();
		
		List<Entity> list = null, current;

		while (go){
			
			exec.getProducer().setMode(loop);
			
			// List of edges created by rules in this step (or by entailment if any)
			current = list;
			list = new ArrayList<Entity>();
			
			for (Rule rule : rules){
				// graph.entail() is done once before rule application
				graph.setUpdate(false);
				Query qq = rule.getQuery();
				if (debug || qq.isDebug()){
					//System.out.println(qq);
					qq.setDebug(true);
				}
				
				process(rule, list);
				
			}
			
			if (graph.size() > size){
				// There are new edges: entailment again
				graph.entail(list);
				size = graph.size();
				loop++;
			}
			else {
				go = false;
			}
		}
		
		if (debug) System.out.println("** Rule: " + (graph.size() - start));
		
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
		cons.setList(list);

		int start = graph.size();

		Mappings lMap =  exec.query(qq, map);
		cons.insert(lMap, graph);	

		if (debug || qq.isDebug()){
			System.out.println("** Mappings: " + lMap.size());
			System.out.println("** Graph: "  + graph.size());
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
	
	

}
