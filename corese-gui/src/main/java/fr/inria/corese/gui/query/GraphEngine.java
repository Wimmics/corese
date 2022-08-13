package fr.inria.corese.gui.query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.compiler.eval.QuerySolverVisitor;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Build;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.Cleaner;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.util.Parameter;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.*;
import fr.inria.corese.gui.core.Command;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Constant;
import static fr.inria.corese.core.util.Property.Value.ACCESS_LEVEL;
import fr.inria.corese.core.util.Tool;
import java.io.IOException;

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
    QueryProcess exec;
    private QuerySolverVisitor visitor;
    Build build;
    // manage db or dataset storage access
    private DatasetManager datasetManager;
    
    private boolean isListGroup = false,
            isDebug = false, linkedFunction = false;

    GraphEngine(boolean b) {
        graph = GraphStore.create(b);
        qengine = QueryEngine.create(graph);        
        init();
    }
    
    void init() {
        datasetManager = new DatasetManager().init();
        exec = createQueryProcess();

        try {
            setVisitor(new QuerySolverVisitor(exec.getCreateEval()));
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
    
    public void init(Command cmd) {
        setOption(cmd);
        Property.init(getGraph());
    }


    public void setOption(Command cmd) {
        for (String key : cmd.keySet()) {
            System.out.println("Command: " + key);
            switch (key) {
                case Command.VERBOSE:
                    graph.setVerbose(true);
                    break;
                case Command.DEBUG:
                    graph.setDebugMode(true);
                    break;
                case Command.METADATA:
                    graph.setMetadata(true);
                    break;
                    
                case Command.LINKED_FUNCTION:
                    setLinkedFunction(true);
                    break;
                case Command.READ_FILE:
                    setReadFile(true);
                    break;
                    
                case Command.STRING:
                    Constant.setString(true);
                    break;                               
                case Command.PARAM:
                    param(cmd.get(key));
                    break;
               
                case Command.LOAD:
                    System.out.println("load: " + cmd.get(key));
                    loadDirProtect(cmd.get(key));
                    break;

                    
                case Command.REENTRANT:
                    Property.set(REENTRANT_QUERY, true);
                    break;
                case Command.ACCESS:
                    Property.set(ACCESS_RIGHT, true);
                    break;
                case Command.LOAD_DEFAULT_GRAPH:
                    Property.set(LOAD_IN_DEFAULT_GRAPH, true);
                    break;
                case Command.NODE_AS_DATATYPE:
                    Property.set(GRAPH_NODE_AS_DATATYPE, true);
                case Command.RDF_STAR:
                    Property.set(RDF_STAR, true);
                    break;                   
                case Command.SUPER_USER:
                    Property.set(ACCESS_LEVEL, false);
                    break;
                
            }
        }
    }

    void param(String path) {
        try {
            new Parameter().load(path).process();
            IDatatype dt = getVisitor().initServer("http://ns.inria.fr/corese/gui");
        } catch (LoadException ex) {
            logger.error(ex);
        }
    }

    public static GraphEngine create() {
        return new GraphEngine(true);
    }

    public static GraphEngine create(boolean rdfs) {
        return new GraphEngine(rdfs);
    }
    
    public void graphIndex() {
        int max = 10;
        if (Property.intValue(GUI_INDEX_MAX) !=null) {
            max = Property.intValue(GUI_INDEX_MAX);
        }
        Graph g = getGraph();
        System.out.println(g.display(max));
        System.out.println(g.getNodeManager().display(max));
        System.out.println(g.getIndex());
        Tool.trace("Memory used: %s", Tool.getMemoryUsageMegabytes());
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
    
    public void cleanOWL() {
        try {
            Cleaner clean = new Cleaner(getGraph());
            clean.process();
        } catch (IOException | EngineException | LoadException ex) {           
            logger.error(ex.getMessage());
        }
    }
    
    public QueryProcess createQueryProcess() {
        QueryProcess qp;
        
        if (getDatasetManager()==null||getDatasetManager().isDataset()) {
            logger.info("std dataset");
            qp = createBasicQueryProcess();
        }
        else {
            qp = createStorageQueryProcess();
        }
        
        return qp;
    }
    
    // db storage mode
    public QueryProcess createStorageQueryProcess() {
        QueryProcess qp = getDatasetManager().createQueryProcess(graph);
        Load load = Load.create();
        load.setDataManager(getDatasetManager().getDataManager());
        qp.setLoader(load);
        return qp;
    }

    // graph dataset mode
    public QueryProcess createBasicQueryProcess() {
        QueryProcess qp = QueryProcess.create(graph, true);
        qp.setLoader(loader());
        qp.setListGroup(isListGroup);
        qp.setDebug(isDebug);
        return qp;
    }

    public Load loader() {
        Load load = Load.create(graph);
        if (getDatasetManager()!=null) {
            load = getDatasetManager().createLoad(graph);
        }
        load.setEngine(qengine);
        return load;
    }


    public void load(String path) throws EngineException, LoadException {
        Load ld = loader();
        ld.parse(path);
        // in case of load rule
        if (ld.getRuleEngine() != null) {
            setRuleEngine(ld.getRuleEngine());
        }
    }
    
    public void loadString(String rdf) throws EngineException, LoadException {
        Load ld = loader();
        ld.loadString(rdf, ld.TURTLE_FORMAT);       
    }

    public void loadDirProtect(String path) {
        try {
            Load ld = loader();
            if (path.contains(";")) {
                for (String name : path.split(";")) {
                    ld.parseDir(name);
                }
            } else {
                ld.parseDir(path);
            }
        } catch (LoadException ex) {
            logger.error(ex);
        }
    }

    public void loadDir(String path) throws EngineException, LoadException {
        load(path);
    }

    public boolean runRuleEngine() throws EngineException {
        return runRuleEngine(false, false);
    }

    public boolean runRuleEngine(boolean opt, boolean trace) throws EngineException {
        if (getRuleEngine() == null) {
            logger.error("No rulebase available yet");
            return false;
        }
        getRuleEngine().setDebug(isDebug);
        if (opt) {
            getRuleEngine().setSpeedUp(opt);
            getRuleEngine().setTrace(trace);
        }
        graph.process(getRuleEngine());
        return true;
    }
    
    public boolean runRule(String path) throws EngineException, LoadException {
        logger.info("Load rule: " + path);
        load(path);
        if (getRuleEngine() != null) {
            return getRuleEngine().process();
        }
        return true;
    }

    // TODO: clean timestamp, clean graph index
    public void setOWLRL(int owl, boolean trace) throws EngineException {
        setOwlEngine(getDatasetManager().createRuleEngine(graph));
        getOwlEngine().setProfile(owl);
        getOwlEngine().setTrace(trace);
        Date d1 = new Date();
        // disconnect RDFS entailment during OWL processing
        getOwlEngine().processWithoutWorkflow();
        Date d2 = new Date();
        System.out.println("Time: " + (d2.getTime() - d1.getTime()) / (1000.0));
    }

    public void runQueryEngine() {
        qengine.setDebug(isDebug);
        try {
            qengine.process();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
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

    public void loadRDF(String rdf, int format) throws EngineException, LoadException {

        InputStream stream = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));

        Load ld = this.loader();
        ld.parse(stream, "", format);
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

    public void setReadFile(boolean b) {
        Access.setReadFile(b);
    }
    
    public RuleEngine getRuleEngine(String path) {
        if (path == null) {
            return getOwlEngine();
        }
        return getRuleEngine();
    }

    public RuleEngine getOwlEngine() {
        return owlEngine;
    }

    public void setOwlEngine(RuleEngine owlEngine) {
        this.owlEngine = owlEngine;
    }

    public RuleEngine getRuleEngine() {
        return rengine;
    }

    public void setRuleEngine(RuleEngine rengine) {
        this.rengine = rengine;
    }

    public DatasetManager getDatasetManager() {
        return datasetManager;
    }

    public void setDatasetManager(DatasetManager datasetManager) {
        this.datasetManager = datasetManager;
    }

}
