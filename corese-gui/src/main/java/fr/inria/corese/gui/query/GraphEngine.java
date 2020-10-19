package fr.inria.corese.gui.query;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import java.io.InputStream;
import fr.inria.corese.sparql.datatype.DatatypeMap;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.gui.core.Command;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.core.pipe.Pipe;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Build;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadPlugin;
import fr.inria.corese.core.util.Parameter;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import java.util.Date;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Lite implementation of IEngine using kgraph and kgram
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class GraphEngine {

   
    
    private static Logger logger = LogManager.getLogger(GraphEngine.class);
    static final String BRUL = "brul";

    private Graph graph;
    private RuleEngine rengine, owlEngine;
    private QueryEngine qengine;
//	private Engine bengine;
    QueryProcess exec;
    private QuerySolverVisitor visitor;
    LoadPlugin plugin;
    Build build;

    private boolean isListGroup = false,
            isDebug = false, linkedFunction = false;

    GraphEngine(boolean b) {
        DatatypeMap.setLiteralAsString(false);
        graph = GraphStore.create(b);
        //rengine = RuleEngine.create(graph);
        qengine = QueryEngine.create(graph);
//		bengine = Engine.create(QueryProcess.create(graph, true));
        exec = QueryProcess.create(graph, true);
        try {
            setVisitor(new QuerySolverVisitor(exec.getEval()));
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(GraphEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Before creating a new Corese, tell the old one to finish
     */
    public void finish() {
        graph.getEventManager().process(Event.Finish);
    }

    public void setOption(Command cmd) {
        for (String key : cmd.keySet()) {
            System.out.println("Command: " + key);
            switch (key) {
                case Command.VERBOSE:
                    graph.setVerbose(true);
                    break;
                case Command.LINKED_FUNCTION:
                    setLinkedFunction(true);
                    break;
                case Command.DEBUG:
                    graph.setDebugMode(true);
                    break;
                case Command.MAX_LOAD:
                    int max = Integer.valueOf(cmd.get(key));
                    Load.setLimitDefault(max);
                    break;
                case Command.METADATA:
                    graph.setMetadata(true);
                    break;
                case Command.STRING:
                    Constant.setString(true);
                    break;
                case Command.REENTRANT:
                    QueryProcess.setOverwrite(true);
                    break;
                case Command.RDF_STAR:
                    Graph.setEdgeMetadataDefault(true);
                    break;
                case Command.ACCESS:
                    AccessRight.setActive(true);
                    break;
                case Command.PARAM:
                    param(cmd.get(key));
                    break;
            }
        }
    }
    
    void param(String path) {
        try {
            new Parameter().load(path).process();
            IDatatype dt = getVisitor().initServer("http://ns.inria.fr/corese/gui");
        } catch (LoadException ex) {
            java.util.logging.Logger.getLogger(GraphEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static GraphEngine create() {
        return new GraphEngine(true);
    }

    public static GraphEngine create(boolean rdfs) {
        return new GraphEngine(rdfs);
    }

    public void definePrefix(String p, String ns) {
        QueryProcess.definePrefix(p, ns);
    }

    public void setListGroup(boolean b) {
        isListGroup = b;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public Graph getGraph() {
        return graph;
    }

    public QueryProcess createQueryProcess() {
        QueryProcess qp = QueryProcess.create(graph, true);
        qp.setLoader(loader());
        qp.setListGroup(isListGroup);
        qp.setDebug(isDebug);
        return qp;
    }

    public Load loader() {
        Load load = Load.create(graph);
        //load.setEngine(rengine);
        load.setEngine(qengine);
        load.setPlugin(plugin);
        //load.setBuild(build);
        return load;
    }

    public void setPlugin(LoadPlugin p) {
        plugin = p;
    }

//	public void setBuild(Build b){
//		build = b;
//	}
    public void load(String path) throws EngineException, LoadException {
//		if (path.endsWith(BRUL)){
//			bengine.load(path);
//		}
//		else 
//		{
        Load ld = loader();
        ld.parse(path);
        // in case of load rule
        if (ld.getRuleEngine() != null) {
            rengine = ld.getRuleEngine();
        }
//		}

    }

    public void loadDir(String path) throws EngineException, LoadException {
        load(path);
    }

    public boolean runRuleEngine() throws EngineException {
        return runRuleEngine(false, false);
    }

    public boolean runRuleEngine(boolean opt, boolean trace) throws EngineException {
        if (rengine == null) {
            logger.error("No rulebase available yet");
            return false;
        }
        rengine.setDebug(isDebug);
        if (opt) {
            rengine.setSpeedUp(opt);
            rengine.setTrace(trace);
        }
        graph.process(rengine);
        return true;
    }

    // TODO: clean timestamp, clean graph index
    public void setOWLRL(boolean run, int owl, boolean trace) throws EngineException {
        if (run) {
            owlEngine = RuleEngine.create(graph);
            owlEngine.setProfile(owl);
            owlEngine.setTrace(trace);
            Date d1 = new Date();
            // disconnect RDFS entailment during OWL processing
            owlEngine.processWithoutWorkflow();
            Date d2 = new Date();
            System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));
        }
    }

    public void runQueryEngine() {
        qengine.setDebug(isDebug);
        try {
            qengine.process();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    public void runPipeline(String path) {
        Pipe pipe = Pipe.create(graph);
        pipe.setDebug(true);
        pipe.process(path);
    }

    public boolean validate(String path) {

        return false;
    }


    public void load(InputStream rdf, String source) throws EngineException {

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

//	public Mappings SPARQLProve(String query) throws EngineException {
//		LBind res = bengine.SPARQLProve(query);
//		if (res == null) return  null;
//		Mappings lMap = translate(res);
//		return lMap;
//	}
//	
//	
//	
//	Mappings translate(LBind lb){
//		ASTQuery ast = lb.getAST();
//		Query query = exec.compile(ast);
//		Mappings lMap =  Mappings.create(query);
//		for (Bind b : lb){
//			List<Node> list = new ArrayList<Node>();
//			
//			for (Node qNode : query.getSelect()){
//				IDatatype dt = b.getDatatypeValue(qNode.getLabel());
//				if (dt == null){
//					list.add(null);
//				}
//				else {
//					Node node = graph.getNode(dt, true, false);
//					list.add(node);
//				}
//			}
//			Mapping map = Mapping.create(query.getSelect(), list);
//			lMap.add(map);
//		}
//		return lMap;
//	}
    public Mappings SPARQLProve(ASTQuery ast) throws EngineException {

        return null;
    }

    public Mappings SPARQLQuery(String query) throws EngineException {
        QueryExec exec = QueryExec.create(this);
        return exec.SPARQLQuery(query);
    }

    public Mappings query(String query) throws EngineException {
        QueryExec exec = QueryExec.create(this);
        return exec.query(query);
    }

    public Mappings update(String query) throws EngineException {
        QueryExec exec = QueryExec.create(this);
        return exec.update(query);
    }

    public Mappings SPARQLQuery(ASTQuery ast) throws EngineException {
        QueryExec exec = QueryExec.create(this);
        return exec.SPARQLQuery(ast);
    }

    public Mappings SPARQLQuery(String query, String[] from, String[] named)
            throws EngineException {
        QueryProcess exec = createQueryProcess();
        Mappings map = exec.query(query);
        return map;
    }

    public ASTQuery parse(String query) throws EngineException {
        return null;
    }

    public Mappings SPARQLQueryLoad(String query) throws EngineException {

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

    public String emptyResult(Mappings res) {

        return null;
    }


    public String getQuery(String uri) {

        return null;
    }


    public void start() {

    }

    /**
     * @return the visitor
     */
    public QuerySolverVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(QuerySolverVisitor visitor) {
        this.visitor = visitor;
    }
    
     /**
     * @return the linkedFunction
     */
    public boolean isLinkedFunction() {
        return linkedFunction;
    }

    /**
     * @param linkedFunction the linkedFunction to set
     */
    public void setLinkedFunction(boolean linkedFunction) {
        this.linkedFunction = linkedFunction;
        Access.setLinkedFeature(linkedFunction);
    }

}
