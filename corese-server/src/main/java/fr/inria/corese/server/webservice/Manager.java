package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.core.workflow.SemanticWorkflow;
import fr.inria.corese.core.workflow.WorkflowParser;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * TripleStore Manager Load Profile Datasets into TripleStores Manage a map of
 * TripleStore
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Manager {
    private final static Logger logger = LogManager.getLogger(Manager.class);
    static final String STCONTEXT = Context.STL_CONTEXT;
    static String DEFAULT = NSManager.STL + "default";
    static String USER    = NSManager.STL + "user";
    private static String CONTENT = NSManager.STL + "content";
    private static String CONTENT_SHARE = NSManager.STL + "shareContent";
    private static String SCHEMA = NSManager.STL + "schema";
    private static String NAME = NSManager.SWL + "name";
    static final String SKOLEM = NSManager.STL + "skolem";
    static HashMap<String, TripleStore> 
            // by dataset URI; e.g. st:cdn
            mapURI, 
            // by shareContent URI
            mapShare;
    // name to URI (e.g. /template/cdn, cdn is the name of the service)
    // cdn -> st:cdn
    static HashMap<String, String> mapService;
    static NSManager nsm;
    static Manager manager;

    static {
        //init();
        mapShare = new HashMap<String, TripleStore>();
        manager = new Manager();
    }

    static Manager getManager() {
        return manager;
   }

    /**
     * Create a TripleStore for each server definition from profile and load its
     * content
     */
    void init() {
        mapURI = new HashMap<String, TripleStore>();
        mapService = new HashMap<String, String>();
        nsm = NSManager.create();
        Profile p = getProfile();
        for (Service s : p.getServers()) {
            if (!s.getName().equals(DEFAULT) && !s.getName().equals(USER)) {
                // default/user if the sparql endpoint
                logger.info("Load: " + s.getName());
                try {
                    initTripleStore(p, s);
                } catch (LoadException ex) {
                    LogManager.getLogger(Manager.class.getName()).log(Level.ERROR, "", ex);
                } catch (EngineException ex) {
                    LogManager.getLogger(Manager.class.getName()).log(Level.ERROR, "", ex);
                }
            }
        }
        // draft
        // complete();
    }

    TripleStore getTripleStore(String name) {
        return mapURI.get(name);
    }

    TripleStore getTripleStoreByService(String name) {
        String uri = getURI(name);
        if (uri == null) {
            return null;
        }
        return getTripleStore(uri);
    }

    String getURI(String name) {
        return mapService.get(name);
    }

    Profile getProfile() {
        return Profile.getProfile();
    }

    TripleStore initTripleStore(Profile p, Service s) throws LoadException, EngineException {
        TripleStore store = createTripleStore(p, s);
        mapURI.put(s.getName(), store);
        if (s.getService() != null) {
            mapService.put(s.getService(), s.getName());
        }
        return store;
    }

    TripleStore createTripleStore(Profile p, Service s) throws LoadException, EngineException {
        GraphStore g = GraphStore.create(); //GraphStore.create(s.isRDFSEntailment());
//        if (s.getParam() != null) {
//            IDatatype dt = s.getParam().get(SKOLEM);
//            if (dt != null && dt.booleanValue()) {
//                g.setSkolem(true);
//            }
//        }
        TripleStore store = new TripleStore(g, true);
        //store.setOWL(s.isOWLEntailment());
        init(store, s);
        return store;
    }
    
    void tune(TripleStore ts, Service s) {
        if (s.getParam() != null) {
            IDatatype dt = s.getParam().get(SKOLEM);
            if (dt != null && dt.booleanValue()) {
                ts.getGraph().setSkolem(true);
            }
        }
    }

    /**
     * Load server content from st:content [ a sw:Workflow ; etc ]
     * Workflow is retrieved from the profile graph.
     */
    void init(TripleStore ts, Service service) throws LoadException, EngineException {
        tune(ts, service);
        Graph g = getProfile().getProfileGraph();
        Node serv = g.getNode(service.getName());
        Node cont = g.getNode(CONTENT, serv);
        boolean share = false;
        if (cont == null){
            share = true;
            cont = g.getNode(CONTENT_SHARE, serv);
        }
        if (cont != null) {
             initService(ts, g, serv, cont, share);
        } 
        else {
             initService(ts, service);
       }
    }

   

    /**
     * Init service dataset with Workflow of Load
     */
    void initService(TripleStore ts, Graph profile, Node server, Node swnode, boolean share) throws LoadException, EngineException {
        initContent(ts, profile, server, swnode, share);
        ts.finish(getProfile().isProtected());
    }
    
    void initContent(TripleStore ts, Graph profile, Node server, Node swnode, boolean share) throws LoadException, EngineException {
        Graph gg = null;
        if (share && mapShare.containsKey(swnode.getLabel())){
            gg = mapShare.get(swnode.getLabel()).getGraph();            
        }
        else {
            gg = createContent(ts, profile, server, swnode);
            if (share){
                mapShare.put(swnode.getLabel(), ts);
            }
        }
        if (gg != null && gg != ts.getGraph()){
            ts.setGraph(gg);
        }
    }
    
    Graph createContent(TripleStore ts, Graph profile, Node server, Node swnode) throws LoadException, EngineException {
        SemanticWorkflow sw = new WorkflowParser(profile).parse(swnode);
        Data res = sw.process(new Data(ts.getGraph()));
        return res.getGraph();
    }

    void init(TripleStore ts) {
        Service s = getProfile().getServer(USER);
        if (s == null) {
            s = getProfile().getServer(DEFAULT);
        }
        if (s != null) {
            try {
                init(ts, s);
            } catch (LoadException ex) {
                LogManager.getLogger(Manager.class.getName()).log(Level.ERROR, "", ex);
            } catch (EngineException ex) {
                LogManager.getLogger(Manager.class.getName()).log(Level.ERROR, "", ex);
            }
        }
    }
    
    
    
    
    
     /**
     * Create TripleStore and Load data from profile service definitions
     */
    @Deprecated
    GraphStore initService(TripleStore ts, Service s) throws LoadException {
        GraphStore g = ts.getGraph();
        Load ld = Load.create(g);

        for (Service.Doc d : s.getData()) {
            //ld.load(d.getUri(), d.getUri(), d.getName());
            ld.parse(d.getUri(), d.getName());
        }
        for (Service.Doc d : s.getSchema()) {
            //ld.load(d.getUri(), d.getUri(), d.getName());
            ld.parse(d.getUri(), d.getName());
        }

        if (s.getContext().size() > 0) {
            Graph gg = Graph.create();
            g.setNamedGraph(STCONTEXT, gg);
            Load lq = Load.create(gg);

            for (Service.Doc d : s.getContext()) {
                //lq.load(d.getUri(), d.getUri(), d.getName());
                lq.parse(d.getUri(), d.getName());

            }

            init(gg);
        }
        return g;
    }

    /**
     * Complete context graph by: 1) add index to queries 2) load query from
     * st:queryURI and insert st:query
     * @deprecated
     */
    void init(Graph g) {
        String init =
                "insert { ?q st:index ?n }"
                + "where  { ?q a st:Query bind (kg:number() as ?n) }";

        String init2 =
                "insert { ?q st:query ?query }"
                + "where  { ?q a st:Query ; st:queryURI ?uri . bind (kg:read(?uri) as ?query) }";

        QueryProcess exec = QueryProcess.create(g);
        try {
            exec.query(init);
            exec.query(init2);
        } catch (EngineException ex) {
            LogManager.getLogger(Manager.class.getName()).log(Level.ERROR, "", ex);
        }
    }

}
