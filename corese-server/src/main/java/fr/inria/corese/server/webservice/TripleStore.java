package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Context;
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
    GraphStore graph = GraphStore.create(false);
    QueryProcess exec = QueryProcess.create(graph);
    boolean rdfs = false, owl = false;
    private String name = Manager.DEFAULT;

    TripleStore(boolean rdfs, boolean owl) {
       this(rdfs, owl, true);
    }
    
    // 
    TripleStore(boolean rdfs, boolean owl, boolean b) {
        graph = GraphStore.create(rdfs);
        init(graph);
        exec  = QueryProcess.create(graph, b);
        this.owl = owl;
    }
    
    TripleStore(GraphStore g){
        graph = g;
        init(g);
        exec = QueryProcess.create(g);
    }
    
    TripleStore(GraphStore g, boolean b){
        graph = g;
        init(g);
        //exec = QueryProcess.create(g, b);
    }
    
    void init(GraphStore g){
        if (EmbeddedJettyServer.isDebug()) {
            g.setVerbose(true);
        }
    }
    
    void finish(boolean b){
        exec = QueryProcess.create(graph, true);
        init(b);
    }
    
    @Override
    public String toString(){
        return graph.toString();
    }
    
    QueryProcess getQueryProcess(){
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
    
    int getMode(){
        return exec.getMode();
    }
    
    void setMode(int m){
        exec.setMode(m);                              
    }
    
    void setOWL(boolean b){
        owl = b;
    }
    
    void init(boolean b) {

        if (b){
            exec.setMode(QueryProcess.PROTECT_SERVER_MODE);
        }

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
    
    Mappings query(String query, Dataset ds) throws EngineException {
            return query(null, query, ds);
    }
        
    Mappings query(HttpServletRequest request, String query, Dataset ds) throws EngineException {
        if (ds == null) {
            ds = new Dataset();
        }
        if (ds.getContext() == null) {
            ds.setContext(new Context());
        }
        Context c = ds.getContext();
        c.setService(getName());
        c.setUserQuery(true);
        c.setLevel(Access.getQueryAccessLevel(true, false));
        if (request!=null) {
            c.setRemoteHost(request.getRemoteHost());
        }
        Profile.getEventManager().call(ds.getContext());
        return exec.query(query, ds);
    }

    Mappings query(String query) throws EngineException{
        return query(query, new Dataset());
    }
}
