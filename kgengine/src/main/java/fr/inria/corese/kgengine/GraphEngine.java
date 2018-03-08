package fr.inria.corese.kgengine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.IModel;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.corese.engine.core.Engine;
import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.kgpipe.Pipe;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgengine.kgramenv.util.QueryExec;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.core.GraphStore;
import fr.inria.corese.kgraph.query.QueryEngine;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Build;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.LoadPlugin;
import java.util.Date;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Lite implementation of IEngine using kgraph and kgram
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class GraphEngine implements IEngine {
    private static Logger logger = LogManager.getLogger(GraphEngine.class);
	static final String BRUL = "brul";
	
	private Graph graph;
	private RuleEngine rengine, owlEngine;
	private QueryEngine qengine;
	private Engine bengine;
	QueryProcess exec;
	LoadPlugin plugin;
	Build build;
	
	private boolean isListGroup = false,
	isDebug = false;
	
	GraphEngine (boolean b){
		DatatypeMap.setLiteralAsString(false);
		graph   = GraphStore.create(b);
		//rengine = RuleEngine.create(graph);
		qengine = QueryEngine.create(graph);
		bengine = Engine.create(QueryProcess.create(graph, true));
		exec = QueryProcess.create(graph, true);
	}
	
	public static GraphEngine create(){
		return new GraphEngine(true);
	}
        
        public static GraphEngine create(boolean rdfs){
		return new GraphEngine(rdfs);
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
		QueryProcess qp = QueryProcess.create(graph, true);
		qp.setLoader(loader());
		qp.setListGroup(isListGroup);
		qp.setDebug(isDebug);
		return qp;
	}

	public Load loader(){
		Load load = Load.create(graph);
		//load.setEngine(rengine);
		load.setEngine(qengine);
		load.setPlugin(plugin);
		load.setBuild(build);
		return load;
	}
	
	public void setPlugin(LoadPlugin p){
		plugin = p;
	}
	
	public void setBuild(Build b){
		build = b;
	}
	
	public void load(String path) throws EngineException {
		if (path.endsWith(BRUL)){
			bengine.load(path);
		}
		else 
		{
                    Load ld = loader();
                    ld.load(path);
                    // in case of load rule
                    if (ld.getRuleEngine() != null){
                        rengine = ld.getRuleEngine();
                    }
		}
                
	}
	
	public void loadDir(String path) throws EngineException {
		load(path);
	}
	
	public boolean runRuleEngine() {
            return runRuleEngine(false, false);
        }
        
	public boolean runRuleEngine(boolean opt, boolean trace) {
            if (rengine == null){
                logger.error("No rulebase available yet");
                return false;
            }
		rengine.setDebug(isDebug);
		if (opt){
                    rengine.setSpeedUp(opt);
                    rengine.setTrace(trace);
                }
		graph.process(rengine);
                return true;
	}
        
        // TODO: clean timestamp, clean graph index
        public void setOWLRL(boolean run, boolean lite, boolean trace) {
            if (run){
                owlEngine = RuleEngine.create(graph);
                owlEngine.setProfile((lite)? RuleEngine.OWL_RL_LITE: RuleEngine.OWL_RL);
                owlEngine.setTrace(trace);
                Date d1 = new Date();
                owlEngine.process();
                Date d2 = new Date();
                System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));
            }           
        }
	
	public void runQueryEngine() {
		qengine.setDebug(isDebug);
		qengine.process();
	}
	
	public void runPipeline(String path){
		Pipe pipe = Pipe.create(graph);
		pipe.setDebug(true);
		pipe.process(path);
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
		try {
			loader().load(rdf, source);
		} catch (LoadException e) {
			throw new EngineException(e.toString());
		}
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

	


	public IResults SPARQLProve(String query) throws EngineException {
		LBind res = bengine.SPARQLProve(query);
		if (res == null) return  null;
		Mappings lMap = translate(res);
		return QueryResults.create(lMap);
	}
	
	
	
	Mappings translate(LBind lb){
		ASTQuery ast = lb.getAST();
		Query query = exec.compile(ast);
		Mappings lMap =  Mappings.create(query);
		for (Bind b : lb){
			List<Node> list = new ArrayList<Node>();
			
			for (Node qNode : query.getSelect()){
				IDatatype dt = b.getDatatypeValue(qNode.getLabel());
				if (dt == null){
					list.add(null);
				}
				else {
					Node node = graph.getNode(dt, true, false);
					list.add(node);
				}
			}
			Mapping map = Mapping.create(query.getSelect(), list);
			lMap.add(map);
		}
		return lMap;
	}

	
	public IResults SPARQLProve(ASTQuery ast) throws EngineException {
		
		return null;
	}
		
	public IResults SPARQLQuery(String query) throws EngineException {		
		QueryExec exec = QueryExec.create(this);
		return exec.SPARQLQuery(query);
	}
	
	public IResults query(String query) throws EngineException {		
		QueryExec exec = QueryExec.create(this);
		return exec.query(query);
	}
	
	public IResults update(String query) throws EngineException {		
		QueryExec exec = QueryExec.create(this);
		return exec.update(query);
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

	/**
	 * @deprecated
	 */
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
