package fr.inria.corese.kgpipe;


import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.api.Loader;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;
import fr.inria.corese.kgtool.load.RuleLoad;
import org.apache.logging.log4j.Level;

/**
 * Pipeline described using RDF, interpreted using SPARQL queries
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class Pipe {
	private static Logger logger = LogManager.getLogger(Pipe.class);	

	static final String KGRAM = ExpType.KGRAM ;
	
	static final String LOAD 	= KGRAM + "Load";
	static final String QUERY 	= KGRAM + "Query";
	static final String UPDATE 	= KGRAM + "Update";
	static final String RULE  	= KGRAM + "Rule";
	static final String RULEBASE= KGRAM + "RuleBase";
	static final String PIPE  	= KGRAM + "Pipe";
	static final String TEST  	= KGRAM + "Test";
	static final String AND   	= KGRAM + "And";
	static final String THEN   	= KGRAM + "then";
	static final String ELSE   	= KGRAM + "else";

	
	static final String QNAME   = "?q";
	static final String TNAME   = "?t";
	static final String AQNAME  = "?qa";
	static final String ATNAME  = "?ta";
	
	static final String IQNAME  = "?qi";
	static final String TQNAME  = "?qt";
	static final String TTNAME  = "?tt";
	static final String EQNAME  = "?qe";
	static final String ETNAME  = "?te";

	static final String RQNAME  = "?qr";


	static final String QACTION = 
		"select  * where {" +
		"?p rdf:type kg:Pipeline " +
		"{?p kg:body ?q " +
		"?q rdf:type ?t " +
		"minus {?q rdf:type rdf:List}" +
		"} union {" +
		"?p kg:body ?a " +
		"?a rdf:rest*/rdf:first ?q " +
		"?q rdf:type ?t}" +
		"}"; 
	
	static final String QIF = 
		"select * where {" +
		"?q kg:if ?qi  " +
		"optional {?q kg:then ?qt ?qt rdf:type ?tt} " +
		"optional {?q kg:else ?qe ?qe rdf:type ?te}" +
		"}";

	static final String QAND = 
		"select * where {" +
		"?q kg:list ?l " +
		"?l rdf:rest*/rdf:first ?qa " +
		"?qa rdf:type ?ta" +
		"}";
	
	
	static final String QRULEBASE = 
		"select * where {" +
		"?q kg:body ?b " +
		"?b rdf:rest*/rdf:first ?qr " +
		"?qr rdf:type ?tr" +
		"}";
	
	
	
	Graph 
	// target graph on which the pipe is run
	graph, 
	// graph that contains the pipe as an RDF graph
	pipe;
	QueryProcess exec, pipeExec;
	Load load;
	
	boolean isDebug = false;
	
	Node qNode, tNode;
	
	
	Pipe(Graph g){
		graph = g;
		exec = QueryProcess.create(graph);
		load = Load.create(graph);
		exec.setLoader(load);
		
		qNode = NodeImpl.createVariable(QNAME);
		tNode = NodeImpl.createVariable(TNAME);
	}

	public static Pipe create(Graph g){
		return new Pipe(g);
	}
	
	public void setDebug(boolean b){
		isDebug = b;
		//exec.setDebug(b);
	}
	
	public void load(String name){
		pipe = Graph.create(true);
		Loader load = Load.create(pipe);
            try {
                load.parse(name);
            } catch (LoadException ex) {
                LogManager.getLogger(Pipe.class.getName()).log(Level.ERROR, "", ex);
            }
	}
	
	public void process(){
		long d1 = new Date().getTime();
		try {
			Mappings map = get();
			run(map);
		} catch (EngineException e) {
			e.printStackTrace();
			logger.error("** Error: " + e.getMessage());
		}
		long d2 = new Date().getTime();
		logger.debug("** Pipe: " + (d2-d1)/1000.0);
	}
	
	/**
	 * Load and run a pipeline
	 * @param name: path or URL of pipeline to execute
	 */
	public void process(String name){
		load(name);
		process();
	}
	
	
	
	/**
	 * Load the pipeline and return the body
	 * name is a path (or URL)
	 * Return one mapping for each operation of the pipeline body
	 * @throws EngineException 
	 */
	Mappings get() throws EngineException{
		pipeExec = QueryProcess.create(pipe);
		pipeExec.add(graph);			
		Mappings lMap = pipeExec.query(QACTION);
		return lMap;
	}
	

	/**
	 * Each Mapping is an operation of the pipeline
	 * ?q = name of file to process
	 * ?t = type of instruction
	 */
	void run(Mappings body) throws EngineException{		
		for (Mapping map : body){
			run(map);
		}
	}
	
	
	/**
	 * Process one instruction
	 */
	void run(Mapping map) throws EngineException{
		
		Node qn = map.getNode(QNAME);
		Node tn = map.getNode(TNAME);
		
		if (qn == null || tn == null) return;
		
		String t = tn.getLabel();
		String q = qn.getLabel();

		if (isDebug){
			logger.debug(t);
			logger.debug(q);
		}
		
		if (t.equals(LOAD)){
			pload(q);
		}
		else if (t.equals(AND)){
			and(map);
		}
		else if (t.equals(QUERY) || t.equals(UPDATE)){
			query(q);
		}
		else if (t.equals(RULEBASE)) {
			ruleBase(map);
		}
		else if (t.equals(RULE)) {
			rule(q);
		}
		else if (t.equals(PIPE)) {
			pipe(q);
		}
		else if (t.equals(TEST)) {
			test(map);
		}
		else {
			logger.warn("** Pipe: unknown: " + t);
		}
	}
	
	
	void pload(String name){
            try {
                load.parse(name);
            } catch (LoadException ex) {
                LogManager.getLogger(Pipe.class.getName()).log(Level.ERROR, "", ex);
            }
	}
	
	
	void and(Mapping map) throws EngineException{
		Mappings and = pipeExec.query(QAND, map);
		
		for (Mapping m : and){
			// rename ?qa as ?q and ?ta as ?t
			Mapping mm = Mapping.create();
			mm.bind(qNode, m.getNode(AQNAME));
			mm.bind(tNode, m.getNode(ATNAME));
			run(mm);
		}	
	}
	
	
	Mappings query(String q) throws EngineException{
		QueryLoad ql = QueryLoad.create();
		String qq = ql.read(q);

		if (isDebug) logger.debug(qq);

		Mappings res = exec.query(qq);

		if (isDebug) logger.debug(res);
		
		return res;
	}
	
	/**
	 * Load and run a Rule Base
	 * @throws EngineException 
	 */
	void ruleBase(Mapping map) throws EngineException{
		RuleEngine re = parseRuleBase(map);
		if (re != null){
			re.process();
		}
	}
		
		
		
	RuleEngine parseRuleBase(Mapping map) throws EngineException{
		Node rb = map.getNode(QNAME);
		if (rb == null) return null;
		
		if (rb.isBlank()){
			// there is a body with a list of rules
			return rules(map);
		}
		else {
			// there is a URI for loading the rule base
			RuleEngine re = RuleEngine.create(graph);
			re.setDebug(isDebug);
			RuleLoad rl = RuleLoad.create(re);
			rl.load(rb.getLabel());
			return re;
		}
	}
	
	/**
	 * Retrieve a list of rules
	 * <RuleBase>
	 *   <body parseType='Collection'>
	 *     <Rule rdf:about='r1.rq' />
	 *   </body>
	 * </RuleBase>
	 */
	RuleEngine rules(Mapping map) throws EngineException{
		QueryLoad  rl = QueryLoad.create();
		RuleEngine re = RuleEngine.create(graph);
		re.setDebug(isDebug);
		Mappings lm = pipeExec.query(QRULEBASE, map);
		for (Mapping m : lm){
			Node nr = m.getNode(RQNAME); 
			if (nr!=null){
				String rule = rl.read(nr.getLabel());
				if (isDebug){
					logger.debug(nr.getLabel());
					logger.debug(rule);
				}
				re.addRule(rule);
			}
		}
		return re;
	}
	
	
	/**
	 * Load and run one Rule 
	 */	
	void rule(String q){
		RuleEngine re = parseRule(q);
		if (re != null){
			re.process();
		}
	}
		
	
	RuleEngine parseRule(String q){	
		QueryLoad ql = QueryLoad.create();
		String rule = ql.read(q);
		if (isDebug) logger.debug(rule);
		if (rule == null) return null;
		
		RuleEngine re = RuleEngine.create(graph);
		re.addRule(rule);
		return re;
	}
	
	
	/**
	 * Process another pipe
	 */
	void pipe(String q){
		Pipe pipe = parsePipe(q);
		pipe.process();
	}
	
	Pipe parsePipe(String q){
		Pipe pipe = Pipe.create(graph);
		pipe.setDebug(isDebug);
		pipe.load(q);
		return pipe;
	}
	
	/**
	 * if ask then action else action
	 */
	void test(Mapping map) throws EngineException{
		Mappings lm = pipeExec.query(QIF, map);
		if (lm.size() == 0) return;
		
		Mapping m = lm.get(0);
		
		Node nif   = m.getNode(IQNAME);
		Node nelse = m.getNode(EQNAME);
		
		if (nif == null) return;

		if (isDebug) logger.debug(nif);

		Mappings res = query(nif.getLabel());
		
		Mapping mm = Mapping.create();

		if (res.size() > 0){
			if (isDebug) logger.debug(THEN);
			mm.bind(qNode, m.getNode(TQNAME));
			mm.bind(tNode, m.getNode(TTNAME));
			run(mm);
		}
		else if (nelse != null){
			if (isDebug) logger.debug(ELSE);
			mm.bind(qNode, m.getNode(EQNAME));
			mm.bind(tNode, m.getNode(ETNAME));
			run(mm);
		}
	}

}
	


