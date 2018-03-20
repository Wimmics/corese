package fr.inria.corese.core.logic;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.Rule;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;


/**
 * Generate rules for some OWL statements such as:
 * property chain
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 * 
 */
public class OWLRule {
	
	private static final String RULE = "?rule";
	static String data = "/home/corby/workspace/kgtool/src/test/resources/data/";
		
	String rules = data + "owlrule";
	
	Graph  graph;
	RuleEngine re;
	
	boolean debug = true;
	
	
	
	
	
	OWLRule(Graph g){
		graph = g;
		re = RuleEngine.create(graph);
	}
	
	public static OWLRule create(Graph g){
		return new OWLRule(g);
	}
	
	public void process() throws EngineException, LoadException{
		process(rules);
	}
	
	/**
	 * src contains a query or is a directory of queries
	 * Load the queries
	 * Process queries (query or update)
	 * Query may compute and return an argument of name RULE
	 * In this case the rule is loaded into a RuleEngine
	 * Then rules are processed 
	 * 
	 * TODO: dependency with load
	 * 
	 */
	public void process(String src) throws EngineException, LoadException{
				
		Load ld = Load.create(graph);
		ld.setEngine(re);
		ld.parse(src);		
		QueryEngine qe = ld.getQueryEngine();
		
		for (Rule r : re.getRules()){
			trace (r.getQuery().getAST());
		}
				
		if (qe != null){
			for (Query query : qe.getQueries()){
				trace(query);
				process(query);
			}
		}
								
		re.process();
	}
	
	
	
	
	
	/**
	 * Process a query or update
	 * Query may return a RULE
	 * in this case, load the rule in RuleEngine
	 */
	void process(Query query) throws EngineException{

		QueryProcess exec = QueryProcess.create(graph);
		Mappings map = exec.query(query);
		NSManager ns = exec.getAST(map).getNSM();

		for (Mapping m : map){
			Node nrule = m.getNode(RULE);
			
			if (nrule != null){
				String srule = nrule.getLabel();
				srule = ns.toString() + srule;
				re.defRule(srule);
				trace(srule);
			}
		}
	}
	
	

	void trace(Object o){
		if (debug){
			System.out.println(o);
			System.out.println();
		}
	}

}
