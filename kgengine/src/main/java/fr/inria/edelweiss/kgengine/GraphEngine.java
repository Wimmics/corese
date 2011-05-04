package fr.inria.edelweiss.kgengine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IModel;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;

/**
 * Lite implementation of IEngine using kgraph and kgram
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class GraphEngine implements IEngine {
	static final String BRUL = "brul";
	
	private Graph graph;
	private RuleEngine rengine;
	//private Engine bengine;
	private List<GraphEngine> lEngine;
	
	private boolean isListGroup = false,
	isDebug = false;
	
	GraphEngine (){
		DatatypeMap.setLiteralAsString(false);
		graph   = Graph.create(true);
		rengine = RuleEngine.create(graph);
		//bengine = Engine.create(QueryProcess.create(graph));
		lEngine = new ArrayList<GraphEngine>();
	}
	
	public static GraphEngine create(){
		return new GraphEngine();
	}
	
	public void definePrefix(String p, String ns){
		QueryProcess.definePrefix(p, ns);
	}
	
	public void setListGroup(boolean b){
		isListGroup = b;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	public Graph getGraph(){
		return graph;
	}
	
	public QueryProcess createQueryProcess(){
		QueryProcess qp = QueryProcess.create(graph);
		qp.setLoader(loader());
		qp.setListGroup(isListGroup);
		qp.setDebug(isDebug);
		return qp;
	}

	public Load loader(){
		Load load = Load.create(graph);
		load.setEngine(rengine);
		return load;
	}
	
	public void load(String path) throws EngineException {
		//System.out.println("** Load: " + path);
//		if (path.endsWith(BRUL)){
//			bengine.load(path);
//		}
//		else 
		{
			loader().load(path);
		}
	}
	
	public void loadDir(String path) throws EngineException {
		load(path);
	}
	
	public void runRuleEngine() {
		//rengine.setDebug(true);
		rengine.process();
	}
	
	
	

	
	public boolean validate(String path) {
		
		return false;
	}

	
//	public Iterable<Event> report() {
//		
//		return null;
//	}
//
//	
//	public Iterable<Event> report(int n) {
//		
//		return null;
//	}

	

	
	public void load(InputStream rdf, String source) throws EngineException {
		loader().load(rdf, source);
	}

	
	public void load(String path, String exclude) throws EngineException {
		
		
	}

	
	public void load(String path, String include, String exclude)
			throws EngineException {
		
		
	}

	
	public void load(String path, String include, String exclude,
			boolean bexclude) throws EngineException {
		
		
	}

	
	public void loadRDF(String rdf, String source) throws EngineException {
		
		
	}

	
	public void loadRDFRule(String rdf, String source) throws EngineException {
		
		
	}

	


	
	public void loadTriple(String triple) throws EngineException {
		
		
	}

	
	public void translate(String pathToRDF, String pathToTriples)
			throws EngineException {
		
		
	}

	


	
	public void runRuleEngine(boolean rdf, boolean owl) {
		
		
	}

	
	public IResults SPARQLProve(String query) throws EngineException {
		//return bengine.SPARQLProve(query);
		return null;
	}

	
	public IResults SPARQLProve(ASTQuery ast) throws EngineException {
		
		return null;
	}

	
	public IResults SPARQLQuery(String query) throws EngineException {		
		QueryExec exec = QueryExec.create(this);
		return exec.SPARQLQuery(query);
	}
	
	
	public IResults SPARQLQuery(ASTQuery ast) throws EngineException {
		QueryExec exec = QueryExec.create(this);
		return exec.SPARQLQuery(ast);
	}

	
	public IResultValue SimpleQuery(String query) throws EngineException {
		
		return null;
	}

	
	public IResults SPARQLQuery(String query, IModel model)
			throws EngineException {
		
		return null;
	}

	
	public IResults SPARQLQuery(String query, String[] from, String[] named)
			throws EngineException {
		QueryProcess exec = createQueryProcess();
		Mappings map = exec.query(query);
		QueryResults res = QueryResults.create(map);
		return res;
	}

	
	public ASTQuery parse(String query) throws EngineException {
		
		return null;
	}
	
	public IResults SPARQLQueryLoad(String query) throws EngineException {
		
		return null;
	}

	
	public boolean SPARQLValidate(String query) throws EngineException {
		
		return false;
	}

	
	public void setProperty(String name, String value) {
		
		
	}

	
	public String getProperty(String name) {
		
		return null;
	}

	
	public String emptyResult(IResults res) {
		
		return null;
	}

	
//	public IResource createResource(String type, String resourceId,
//			String source) throws EngineInitException {
//		
//		return null;
//	}
//
//	
//	public IRelation createRelation(String resource, String property,
//			String value, String source) throws EngineInitException {
//		
//		return null;
//	}
//
//	
//	public IRelation createAttribute(String resource, String property,
//			String literalValue, String source) throws EngineInitException {
//		
//		return null;
//	}

	
	public String getQuery(String uri) {
		
		return null;
	}

	
//	public void addEventListener(EventListener el) {
//		
//		
//	}
//
//	
//	public List<EventListener> getEventListeners() {
//		
//		return null;
//	}
//
//	
//	public void removeEventListener(EventListener el) {
//		
//		
//	}
//
//	
//	public EventManager getEventManager() {
//		
//		return null;
//	}

	
	public void start() {
		
		
	}

}
