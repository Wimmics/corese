package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.corese.kgtool.workflow.Data;
import fr.inria.corese.kgtool.workflow.SemanticWorkflow;
import fr.inria.corese.kgtool.workflow.WorkflowParser;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dataset Manager Load Profile Datasets into TripleStores Manage a map of
 * TripleStore
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Manager {

    static final String STCONTEXT = Context.STL_CONTEXT;
    private static String DEFAULT = NSManager.STL + "default";
    private static String CONTENT = NSManager.STL + "content";
    private static String SCHEMA = NSManager.STL + "schema";
    private static String NAME = NSManager.SWL + "name";
    static final String SKOLEM = NSManager.STL + "skolem";
    static HashMap<String, TripleStore> // by dataset URI; e.g. st:cdn
            mapURI;
    // name to URI (e.g. /template/cdn, cdn is the name of the service)
    // cdn -> st:cdn
    static HashMap<String, String> mapService;
    static NSManager nsm;
    static Manager manager;

    static {
        //init();
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
            if (!s.getName().equals(DEFAULT)) {
                // default if the sparql endpoint
                System.out.println("Load: " + s.getName());
                try {
                    initTripleStore(p, s);
                } catch (LoadException ex) {
                    Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
                } catch (EngineException ex) {
                    Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
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
        return Transformer.getProfile();
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
        if (s.getParam() != null) {
            IDatatype dt = s.getParam().get(SKOLEM);
            if (dt != null && dt.booleanValue()) {
                g.setSkolem(true);
            }
        }
        TripleStore store = new TripleStore(g, true);
        //store.setOWL(s.isOWLEntailment());
        init(store, s);
        return store;
    }

    GraphStore init(TripleStore ts, Service service) throws LoadException, EngineException {
        Graph g = getProfile().getProfileGraph();
        Node serv = g.getNode(service.getName());
        Node cont = g.getNode(CONTENT, serv);
        if (cont != null) {
            return initService(ts, g, serv, cont);
        } 
        else {
             return initService(ts, service);
       }
    }

   

    /**
     * Init service dataset with Workflow of Load
     */
    GraphStore initService(TripleStore ts, Graph profile, Node server, Node swnode) throws LoadException, EngineException {
        GraphStore g = ts.getGraph();
        SemanticWorkflow sw = new WorkflowParser(profile).parse(swnode);
        Data res = sw.process(new Data(g));
        if (res.getGraph() != null && res.getGraph() != g){
            ts.setGraph(res.getGraph());
        }
        ts.finish(getProfile().isProtected());
        return g;
    }

    void init(TripleStore ts) {
        Service s = getProfile().getServer(DEFAULT);
        if (s != null) {
            try {
                init(ts, s);
            } catch (LoadException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EngineException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //****************************** DRAFT *****************
    /*
     * try uri, then name
     * uri is the URI of a query. If a dataset is assigned to uri, use the dataset
     * otherwise use default triple store
     * draft
     */
    @Deprecated
    TripleStore getTripleStore(String uri, String name) throws LoadException, EngineException {
        if (uri != null) {
            String extURI = nsm.toNamespace(uri);
            TripleStore t = getTripleStore(extURI);
            if (t != null) {
                return t;
            }
            Service s = getProfile().getService(extURI);
            if (s != null) {
                t = initTripleStore(getProfile(), s);
                TripleStore tt = getTripleStore(name);
                t.getGraph().setNamedGraph(STCONTEXT, tt.getGraph().getNamedGraph(STCONTEXT));
                return t;
            }
        }
        return getTripleStore(name);
    }

    // context graph may contain service definition
    // add them to profile
    // draft
    @Deprecated
    void complete() {
        for (TripleStore ts : mapURI.values()) {
            Graph g = ts.getGraph().getNamedGraph(STCONTEXT);
            if (g != null) {
                try {
                    getProfile().initServer(g);
                } catch (EngineException ex) {
                    Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    // draft
    @Deprecated
    TripleStore getServer(String uri, String profile) {
        if (profile == null) {
            return Transformer.getTripleStore();
        }
        TripleStore st = getStore(profile);
        if (st == null) {
            st = Transformer.getTripleStore();
        }
        return st;
    }

    // draft
    TripleStore getStore(String name) {
        Service s = getProfile().getService(nsm.toNamespace(name));
        if (s == null || s.getServer() == null) {
            return null;
        }
        return getTripleStore(s.getServer());
    }
}
