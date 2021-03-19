package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class TripleStore {

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    private static Logger logger = LogManager.getLogger(TripleStore.class);
    static HashMap<String, Integer> metaMap ;
    GraphStore graph = GraphStore.create(false);
    //QueryProcess exec;// = QueryProcess.create(graph);
    boolean rdfs = false;
    boolean owl = false;
    private boolean match   = false;
    private boolean protect = false;
    private String name = Manager.DEFAULT;
    
    static {
       init();
    }
    
    static void init() {
        metaMap = new HashMap<>();
        metaMap.put(SPARQLRestAPI.SPARQL, Metadata.SPARQL);
        metaMap.put("trace", Metadata.TRACE);
        metaMap.put("index", Metadata.INDEX);
        metaMap.put("move", Metadata.MOVE);
        metaMap.put("variable", Metadata.VARIABLE);
        metaMap.put("provenance", Metadata.VARIABLE);
        metaMap.put("count", Metadata.SERVER);
    }

    TripleStore(boolean rdfs, boolean owl) {
       this(rdfs, owl, true);
    }
    
    
    TripleStore(boolean rdfs, boolean owl, boolean b) {
        graph = GraphStore.create(rdfs);
        init(graph);
        setMatch(b);
        //exec  = QueryProcess.create(graph, b);
        this.owl = owl;
    }
    
    TripleStore(GraphStore g){
        graph = g;
        init(g);
        setMatch(false);
        //exec = QueryProcess.create(g);
    }
    
    TripleStore(GraphStore g, boolean b){
        graph = g;
        init(g);
    }
    
    void init(GraphStore g){
        if (EmbeddedJettyServer.isDebug()) {
            g.setVerbose(true);
        }
    }
    
    void finish(boolean b){
        setMatch(true);
        //exec = QueryProcess.create(graph, true);
        init(b);
    }
    
    @Override
    public String toString(){
        return graph.toString();
    }
    
    QueryProcess getQueryProcess(){
        QueryProcess exec = QueryProcess.create(graph, isMatch());
        return exec;
    }
    
    GraphStore getGraph(){
        return graph;
    }
    
    void setGraph(GraphStore g){
        graph = g;
    }
    
     void setGraph(Graph g){
         if (g instanceof GraphStore){
            graph = (GraphStore) g;
         }
    }
    
//    int getMode(){
//        return exec.getMode();
//    }
    
//    void setMode(int m){
//        exec.setMode(m);                              
//    }
    
    void setOWL(boolean b){
        owl = b;
    }
    
    void init(boolean b) {
        setProtect(b);
//        if (b){
//            exec.setMode(QueryProcess.PROTECT_SERVER_MODE);
//        }

        if (rdfs) {
            logger.info("Endpoint successfully reset with RDFS entailments.");
        }

        if (owl) {
            RuleEngine re = RuleEngine.create(graph);
            re.setProfile(RuleEngine.OWL_RL);
            graph.addEngine(re);
            if (owl) {
                logger.info("Endpoint successfully reset with OWL RL entailments.");
            }

        }
        
    }
    
    
    void load(String[] load) {
        Load ld = Load.create(graph);
        for (String f : load) {
            try {
                logger.info("Load: " + f);
                //ld.loadWE(f, f, Load.TURTLE_FORMAT);
                ld.parse(f, Load.TURTLE_FORMAT);
            } catch (LoadException ex) {
                LogManager.getLogger(SPARQLRestAPI.class.getName()).log(Level.ERROR, "", ex);
            }
        }
    }
    
    void load(String path, String src) throws LoadException{
        Load ld = Load.create(graph);
        ld.parse(path, src, Load.TURTLE_FORMAT);
    }
    
    // SPARQL Endpoint
               
    Mappings query(HttpServletRequest request, String query, Dataset ds) throws EngineException {
        if (ds == null) {
            ds = new Dataset();
        }
        Context c = ds.getCreateContext();
        c.setService(getName());
        c.setUserQuery(true);
        c.setRemoteHost(request.getRemoteHost());
        Profile.getEventManager().call(ds.getContext());
        QueryProcess exec = getQueryProcess();
        exec.setDebug(c.isDebug());
        // prevent Binding debug true to prevent systematic filter tracing
        c.setDebug(false);
        
        Mappings map;
        if (isFederate(ds)) {
            // federate sparql query with @federate uri
            ASTQuery ast = federate(query, ds);
            map = exec.query(ast, ds);
        } else {
            map = exec.query(query, ds);
        }
        
        return map;
    }
    
    boolean isFederate(Dataset ds) {
        Context c = ds.getContext();
        return (c.hasValue(SPARQLRestAPI.FEDERATE) || c.hasValue("federation")) && c.hasValue("uri");
    }
    
    ASTQuery federate(String query, Dataset ds) throws EngineException {
        QueryProcess exec = getQueryProcess();
        ASTQuery ast = exec.parse(query);
        ast.setAnnotation(metadata(ast, ds));
        return ast;
    }
    
    /**
     * SPARQL query executed as federated query on a federation of endpoints 
     * Generate appropriate Metadata for AST with federation information
     */
    Metadata metadata(ASTQuery ast, Dataset ds) {
        Metadata meta = new Metadata();
        // one URI: URI of federation
        // several URI: list of endpoint URI
        int type = (ds.getUriList().size() > 1) ? Metadata.FEDERATE : Metadata.FEDERATION;
        meta.set(type, ds.getUriList());
                
        for (String key : metaMap.keySet()) {
            if (ds.getContext().hasValue(key)) {
                meta.add(metaMap.get(key));
            }
        }
        
        return meta;
    }
    
    void trace(HttpServletRequest request) {
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            System.out.println("param: " + name + " " + request.getParameter(name));
        }
        for (String name : request.getParameterMap().keySet()) {
            System.out.println("server TS: " + name + " " + request.getParameter(name));
        }
        System.out.println("server TS: query " + request.getParameter("query"));
        System.out.println("server TS: access " + request.getParameter("access"));
    }
    
   

//    Mappings query(String query) throws EngineException{
//        return query(query, new Dataset());
//    }
    
    Mappings query(HttpServletRequest request, String query) throws EngineException{
        return query(request, query, new Dataset());
    }

    /**
     * @return the match
     */
    public boolean isMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public void setMatch(boolean match) {
        this.match = match;
    }

    /**
     * @return the protect
     */
    public boolean isProtect() {
        return protect;
    }

    /**
     * @param protect the protect to set
     */
    public void setProtect(boolean protect) {
        this.protect = protect;
    }
}
