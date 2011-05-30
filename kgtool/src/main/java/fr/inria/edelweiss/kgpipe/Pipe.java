package fr.inria.edelweiss.kgpipe;


import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.load.RuleLoad;

/**
 * Pipeline described using RDF, interpreted using SPARQL
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class Pipe {
	
	static final String KGRAM = ExpType.KGRAM ;
	
	static final String LOAD 	= KGRAM + "Load";
	static final String QUERY 	= KGRAM + "Query";
	static final String UPDATE 	= KGRAM + "Update";
	static final String RULE  	= KGRAM + "Rule";
	static final String PIPE  	= KGRAM + "Pipe";
	static final String TEST  	= KGRAM + "Test";
	static final String AND   	= KGRAM + "And";
	
	static final String QNAME   = "?q";
	static final String TNAME   = "?t";
	static final String AQNAME  = "?qa";
	static final String ATNAME  = "?ta";
	
	static final String IQNAME  = "?qi";
	static final String TQNAME  = "?qt";
	static final String TTNAME  = "?tt";
	static final String EQNAME  = "?qe";
	static final String ETNAME  = "?te";



	static final String QACTION = 
		"select  * where {" +
		"?p rdf:type kg:Pipeline " +
		"{?p kg:body ?q ?q rdf:type ?t " +
		"minus {?q rdf:type rdf:List}" +
		"} union {" +
		"?p kg:body ?a " +
		"?a rdf:rest*/rdf:first ?q " +
		"?q rdf:type ?t}" +
		"}"; 
	
	static final String QIF = 
		"select debug * where {" +
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
	
	
	
	Graph graph, pipe;
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
	
	/**
	 * 
	 * @param name: path or URL of pipeline to execute
	 */
	public void process(String name){
		Mappings pipe = get(name);
		try {
			run(pipe);
		} catch (EngineException e) {
			e.printStackTrace();
			System.out.println("** Error: " + e.getMessage());
		}
	}
	
	/**
	 * Load the pipeline and return the body
	 * name is a path (or URL)
	 */
	Mappings get(String name){
		pipe = Graph.create(true);
		Loader load = Load.create(pipe);
		load.load(name);
		pipeExec = QueryProcess.create(pipe);
		pipeExec.add(graph);
		try {
			Mappings lMap = pipeExec.query(QACTION);
			return lMap;
		} catch (EngineException e) {
			e.printStackTrace();
			System.out.println("** Error: " + e.getMessage());
		}
		return new Mappings();
	}
	

	/**
	 * Each Mapping is an instruction of the pipeline
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
		
		String t = map.getNode(TNAME).getLabel();
		String q = map.getNode(QNAME).getLabel();

		if (isDebug){
			System.out.println(t);
			System.out.println(q);
		}
		
		if (t.equals(LOAD)){
			load(q);
		}
		else if (t.equals(AND)){
			and(map);
		}
		else if (t.equals(QUERY) || t.equals(UPDATE)){
			query(q);
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
			System.out.println("** Pipe: unknown: " + t);
		}
	}
	
	
	void load(String name){
		load.load(name);
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

		if (isDebug) System.out.println(qq);

		Mappings res = exec.query(qq);

		if (isDebug) System.out.println(res);
		
		return res;
	}
	
	/**
	 * Load and run a Rule Base
	 */
	void rule(String q){
		RuleEngine re = RuleEngine.create(graph);
		re.setDebug(true);
		RuleLoad rl = RuleLoad.create(re);
		rl.load(q);
		re.process();
	}
	
	/**
	 * Process another pipe
	 */
	void pipe(String q){
		Pipe pipe = Pipe.create(graph);
		pipe.setDebug(isDebug);
		pipe.process(q);
	}
	
	/**
	 * if ask then action else action
	 */
	void test(Mapping map) throws EngineException{
		Mappings lm = pipeExec.query(QIF, map);
		Mapping m = lm.get(0);
		
		Node nif   = m.getNode(IQNAME);
		Node nelse = m.getNode(EQNAME);

		if (isDebug) System.out.println(nif);

		Mappings res = query(nif.getLabel());

		if (res.size() > 0){
			if (isDebug) System.out.println("then");
			Mapping mm = Mapping.create();
			mm.bind(qNode, m.getNode(TQNAME));
			mm.bind(tNode, m.getNode(TTNAME));
			run(mm);
		}
		else if (nelse != null){
			if (isDebug) System.out.println("else");
			Mapping mm = Mapping.create();
			mm.bind(qNode, m.getNode(EQNAME));
			mm.bind(tNode, m.getNode(ETNAME));
			run(mm);
		}
	}

}
	


