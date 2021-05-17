package fr.inria.corese.server.webservice;

import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import static fr.inria.corese.server.webservice.EmbeddedJettyServer.port;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.transform.ContextBuilder;
import fr.inria.corese.core.util.Parameter;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Parser of RDF profile that defines: 1- a set of profile (eg specifying a
 * Transformation) 2- a set of server with specific data to be loaded in
 * dedicated TripleStore profile is used with profile argument:
 * /template?profile=st:sparql server is used by Tutorial service that manage
 * specific TripleStores each server specify the RDF content of a TripleStore
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Profile {
    static final String ACCESS = NSManager.STL+"access";
    static final String NS = NSManager.STL+"namespace";
    private static Logger logger = LogManager.getLogger(Profile.class);
    /**
     * @return the eventManager
     */
    public static EventManager getEventManager() {
        return eventManager;
    }

    /**
     * @param aEventManager the eventManager to set
     */
    public static void setEventManager(EventManager aEventManager) {
        eventManager = aEventManager;
    }

    static final String NL = System.getProperty("line.separator");
    
    private static  Profile profileManager;
    //static  String SERVER, DATA, QUERY;
    private static EventManager eventManager;
  
    String server, data, query;
      
    HashMap<String,  Service> services, servers; 
    NSManager nsm;
    IDatatype profileDatatype;
    GraphStore profileGraph;
    private Context context;

    boolean isProtected = false;

    static {
        setEventManager(new EventManager());
    }
    
    static String stdLocalhost() {
        return "http://localhost:" + EmbeddedJettyServer.port;
    }
    
    static String getLocalhost() throws UnknownHostException {
         String s = "http://" + InetAddress.getLocalHost().getCanonicalHostName();
         s += (port == 80) ? "" : ":" + port;
         return s;
    }
        
    private void initServerData(boolean localhost) {
        if (localhost) {
            // localhost
            server = stdLocalhost();
        } else {
            try {
             // server.example.org
                server = getLocalhost();
            } catch (UnknownHostException ex) {
                server = stdLocalhost();
            }
        }
        completeHostData();
    }
     
    private  void completeHostData(){
        data = server + "/data/";
        query = data + "query/";
    }
    
    String getServer(){
        return server;
    }
    
    String getQueryPath(String name){
        return query + name;
    }  
    
    String getDataPath(String name){
        return data + name;
    }
    

    public Profile() {
        this(false);
    }

    Profile(boolean localhost) {
        initServerData(localhost);
        services = new HashMap();
        servers = new HashMap();
        nsm = NSManager.create();
        setContext(new Context());
    }
    

    URI resolve(String uri) throws URISyntaxException{        
        return new URI(getServer()).resolve(uri);
    }
    

    void setProtect(boolean b) {
        isProtected = b;
    }

    boolean isProtected() {
        return isProtected;
    }

    /**
     * Complete service parameters according to a profile e.g. get
     * transformation from profile
     */
    Param complete(Param par) throws IOException, LoadException {
        Context serverContext = null;
        if (par.getServer() != null){           
            Service server = getServer(par.getServer());
            if (server.getParam() != null){
                // may set a profile according to URI
                serverContext = server.getParam(); //.copy();
                complete(par, serverContext);
            }
        }
        par.setHostname(getServer());
        String value = par.getValue();
        String profile = par.getProfile();
        String uri = par.getUri();
        Service service = null;  
        if (profile != null) {
            // profile declare a construct where query followed by a transformation
            String uprofile = nsm.toNamespace(profile);
            // may load a new profile            
            define(uprofile);
            service = getService(uprofile);
            if (service != null) {
                if (par.getName() == null) {
                    // parameter name overload profile name
                    par.setName(service.getQuery());
                }
                if (par.getTransform() == null) {
                    // transform parameter overload profile transform
                    par.setTransform(service.getTransform());
                }               
                if (uri != null) {
                    // resource given as a binding value to the query
                    // generate values clause if profile specify variable
                    value = getValues(service.getVariable(), uri);
                }                     
                if (service.getParam() != null){
                    par.setContext(service.getParam().copy());
                }                
            }
        }
        
        if (par.getContext() == null && serverContext != null){
            // use case: profile without st:param, server with st:param
            // import server st:param
           par.setContext(serverContext.copy());
        }
        
        if (service != null){
            
        }
       
        String query = par.getQuery();
        if (query == null){ 
            if (par.getName() != null){
            // load query definition
                query = loadQuery(par.getName());
            }
        }
        else { //if (isProtected) {
            par.setUserQuery(true);
        }

        if (value != null && query != null) {
            // additional values clause
            query += value;
        }
        
        par.setQuery(query);     
                     
        return par;

    }
         
    
    /**
     * Complete Param by Context 
     */
    void complete(Param p, Context c){
      if (p.getUri() != null && p.getProfile() == null && p.getTransform() == null){
            completeLOD(p, c);
        }
    }
    
    /**
     * st:lodprofile ((<http://fr.dbpedia.org/resource> st:dbpedia))  
     * If URI match lodprofile, use profile
     */
    void completeLOD(Param p, Context c){
        String uri = p.getUri();
        IDatatype lod = c.get(Context.STL_LOD_PROFILE);
        if (lod != null && lod.isList()){
            for (IDatatype def : lod.getValues()){
                if (! def.isList()){
                    continue;
                }
                String ns = def.getValues().get(0).getLabel();
                if (ns.equals("*") || uri.startsWith(ns)){
                       // def = (ns profile) 
                       p.setProfile(def.getValues().get(1).getLabel());
                       break;                    
                }
            }
        }
    }

    public Collection<Service> getServices() {
        return services.values();
    }

    public Collection<Service> getServers() {
        return servers.values();
    }

    /**
     * Linked Profile:  load it
     * 
     */
    synchronized void define(String name) {
        if (isProtected){
            return ;
        }
        Service service = getService(name);
        if (service == null) {
            GraphStore g = getProfileGraph();
            try {
                Level level = Access.getQueryAccessLevel(true);               
                Access.check(Feature.LINKED_TRANSFORMATION, level, name, TermEval.LINKED_TRANSFORMATION_MESS);
                logger.info("Load: " + name);
                load(g, name);
                // focus process on ?p = name
                initService(g, name);
            } catch (LoadException ex) {
                logger.error("Load error: "+ ex.getMessage());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            } catch (EngineException ex) {
                logger.error(ex.getMessage());
            }
            
        }
    }

    void initServer(String name, String local) {
        init(getDataPath(name), local);
    }
    
    void setProfile(GraphStore g){
        if (profileDatatype == null){
            profileGraph = g;
            profileDatatype = DatatypeMap.createObject(Context.STL_SERVER_PROFILE, g);
        }
    }
    
    IDatatype getProfileDatatype(){
        return profileDatatype;
    }
    
    GraphStore getProfileGraph(){
        return profileGraph;
    }

    /**
     *
     * path = DATA + profile.ttl
     */
    void init(String path, String local) {
        try {
            LogManager.getLogger(Profile.class.getName()).warn("Load: " + path);
            GraphStore g = load(path, Parameter.PROFILE_EVENT);
            if (local != null){
            LogManager.getLogger(Profile.class.getName()).warn("Load: " + local);
                load(g, local, Parameter.PROFILE_EVENT);
            }
            setProfile(g);
            process(g);
            initFunction();
        } catch (IOException ex) {
                logger.error(ex.getMessage());
        } catch (LoadException ex) {
                logger.error(ex.getMessage());
        } catch (EngineException ex) {
                logger.error(ex.getMessage());
        }
    }
    
    void process(Graph g) throws IOException, EngineException {
        initService(g);
        initServer(g);
        initFederation(g);
        localFederation(g);
        initParameter(g);
        defNamespace(g);
    }
    
    /**
     * In protected mode, service is unauthorized by default
     * Authorize service on specific SPARQL endpoints 
     * profile.ttl may contain:
     * st:access st:namespace <http://dbpedia.org/sparql>
     */
    void defNamespace(Graph g) {
        Node n = g.getResource(ACCESS);
        if (n != null) {
            Access.define(Access.Feature.SPARQL_SERVICE, Access.Level.USER);
            for (Edge edge : g.getEdges(NS, n, 0)) {
                System.out.println("access: " + edge.getNode(1).getLabel());
                Access.define(edge.getNode(1).getLabel(), true);
            }
        }
    }

    GraphStore loadServer(String name) throws IOException, LoadException {
        return load(getDataPath(name));
    }

    GraphStore load(String path) throws IOException, LoadException {
        return load(path, true);
    }
    
    GraphStore load(String path, boolean event) throws IOException, LoadException {
        GraphStore g = GraphStore.create();
        load(g, path, event);
        return g;
    }
    
    void load(GraphStore g, String path) throws LoadException{
        load(g, path, true);
    }

    void load(GraphStore g, String path, boolean event) throws LoadException{
        Load load = Load.create(g);
        load.setEvent(event);
        load.parse(path, Load.TURTLE_FORMAT);       
    }

    String read(String path) throws IOException, LoadException {
        QueryLoad ql = QueryLoad.create();
        String res = ql.readURL(path);
        if (res == null) {
            throw new IOException(path);
        }
        return res;
    }

    String loadQuery(String path) throws IOException, LoadException {
        if (isProtected && !path.startsWith(getServer())) {
            throw new IOException(path);
        }
        return read(path);
    }

    String getValues(String var, String resource) {
        if (var != null) {
            return "values " + var + " { <" + resource + "> }";
        }
        return null;
    }

    void initService(Graph g) throws IOException, EngineException {
        initService(g, null);
    }

    
    void initService(Graph g, String name) throws IOException, EngineException {
        //String str = read(QUERY + "profile.rq");
        String str = getResource("query/profile.rq");
        if (name != null){
            str += String.format("\nvalues ?p { <%s> }", name);
        }
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        for (Mapping m : map) {
            initServiceMap(g, m);
        }
    }
    
    
    void initParameter(Graph g) throws IOException, EngineException {
        String str = getResource("query/urlparameter.rq");
        
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        
        for (Mapping m : map) {
            IDatatype dt   = getValue(m, "?url");
            IDatatype list = getValue(m, "?list");
            if (dt != null) {
                getContext().set(dt.getLabel(), list);
            }
        }
        logger.info("Parameter Context");
        logger.info(getContext());
    }
    
    void initFederation(Graph g) throws IOException, EngineException {
        String str = getResource("query/federation.rq");
        
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        
        for (Mapping m : map) {
            IDatatype dt = getValue(m, "?uri");
            IDatatype list = getValue(m, "?list");
            if (dt != null) {
                System.out.println("federation: " + dt + " : " + list);
                FederateVisitor.declareFederation(dt.getLabel(), list.getValueList());

                for (IDatatype serv : list.getValueList()) {
                    System.out.println("access: " + serv.getLabel());
                    Access.define(serv.getLabel(), true);
                }
            }
        }
    }
    
    void localFederation(Graph g) throws IOException, EngineException {
        String str = getResource("query/federationlocal.rq");
        String local1 = "http://corese.inria.fr/local/federate";
        String local2 = "http://localhost:8080/local/federate";
        String local3 = "http://antipolis:8080/local/federate";
        
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        
        for (Mapping m : map) {
            IDatatype list = getValue(m, "?list");
            ArrayList<String> alist = new ArrayList<>();
            for (IDatatype dt : list) {
                if (!dt.getLabel().contains("sparql")) {
                    alist.add(String.format("%s/%s/sparql", getServer(), dt.getLabel()));
                }
            }
            System.out.println("federation: " + local1 + " : " + alist);
            FederateVisitor.defineFederation(local1, alist);           
            FederateVisitor.defineFederation(local2, alist);           
            FederateVisitor.defineFederation(local3, alist);           
        }
    }
    
    IDatatype getValue(Mapping m, String var) {
        return (IDatatype) m.getValue(var);
    }
    
    /**
     * A service profile may have a transform, a workflow or both
     * If both, there are two Mapping, one for each
     * Build *one* service description 
     */
    void initServiceMap(Graph g, Mapping m) {
        Node prof   = m.getNode("?p");
        Node query  = m.getNode("?q");
        Node var    = m.getNode("?v");
        Node trans  = m.getNode("?t");
        Node serv   = m.getNode("?s");
        Node ctx    = m.getNode("?c");
        Node sw     = m.getNode("?w");

        Service s = getService(prof.getLabel());
        if (s == null){
            s = new Service(prof.getLabel());
        }
        if (trans != null) {
            s.setTransform(trans.getLabel());
        }
        if (query != null) {
            s.setQuery(query.getLabel());
        }
        if (var != null) {
            s.setVariable(var.getLabel());
        }
        if (serv != null) {
            s.setServer(serv.getLabel());
        }      
        if (ctx != null && s.getParam() == null){
            // parse Context only once
            s.setParam(new ContextBuilder(g).process(ctx));
        }
        if (sw != null){
            // PRAGMA: this MUST be done AFTER ctx case just above 
            // set st:workflow in st:param Context
            if (s.getParam() == null){
                s.setParam(new Context());
            }
            s.getParam().set(Context.STL_WORKFLOW, (IDatatype) sw.getValue());
        }
        services.put(prof.getLabel(), s);
    }
       
    /**
     * Initialize Server definitions: get RDF/S documents URI to be loaded (later)
     * Create Server definitions (in addition to Service)
     * @param g
     * @throws EngineException 
     * The load specification part is deprecated, use a st:content [ a sw:Workflow ; .. ] instead
     */
    void initServer(Graph g) throws EngineException {
        String str = "select * where {"
                + "?s a st:Server "
                + "values ?p { st:data st:schema st:context }"
                + "optional { ?s ?p ?d . ?d st:uri ?u  optional { ?d st:name ?n }}"
                + "optional { ?s st:service ?sv } "
                + "optional { ?s st:param ?c } "
                + "}";
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(str);
        for (Mapping m : map) {
            Node sn = m.getNode("?s");
            Node sv = m.getNode("?sv");
            Node p = m.getNode("?p");
            Node uri = m.getNode("?u");
            Node n = m.getNode("?n");
            Node c = m.getNode("?c");
            
           Service server = findServer(sn.getLabel());
           String service = (sv == null) ? null : sv.getLabel();
           server.setService(service);
           if (uri != null){
              String name = (n != null) ? n.getLabel() : null;
              server.add(p.getLabel(), uri.getLabel(), name);
           }
           if (c != null && server.getParam() == null){
               server.setParam(new ContextBuilder(g).process(c));
           }
        }

    }

    /**
     * Create Server if not exists
     * @param name
     * @return 
     */
    Service findServer(String name) {
        Service server = getServer(name);
        if (server == null) {
            server = new Service(name);
            servers.put(name, server);
        }
        return server;
    }
    
      /**
     * Functions shared by server STTL transformations
     */
    public void initFunction() throws IOException, EngineException{
        String str = getResource("query/function.rq");
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query q = exec.compile(str);
    }
    
    String getResource(String name) throws IOException{
        QueryLoad ql = QueryLoad.create();
        String str = ql.getResource("/webapp/data/" + name);
        return str;
    }
    
     void initFunction2() throws IOException, EngineException, LoadException{
        String str = read(getQueryPath("function.rq"));
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query q = exec.compile(str);
    }

    
    /**
     * 
     * Service that defines a transformation
     */
    Service getService(String name) {
        return services.get(name);
    }
    
    /**
     * Service that defines a Server 
     */
    Service getServer(String name){
        return servers.get(name);
    }
    
 
      /**
     * 
     * @param g
     * @throws IOException
     * @throws EngineException 
     * 
     */
//    void initLoad(Graph g) throws IOException, EngineException {
//        String str = read(getQueryPath("profileLoad.rq"));
//        QueryProcess exec = QueryProcess.create(g);
//        Mappings map = exec.query(str);
//        for (Mapping m : map) {
//            initLoad(m);
//        }
//    }

    /**
     * 
     * @param m 
     *
     */
//    void initLoad(Mapping m) {
//        Node profile = m.getNode("?p");
//        Node load = m.getNode("?ld");
//        if (load == null) {
//            return;
//        }
//        String[] list = load.getLabel().split(";");
//        Service s = new Service(profile.getLabel());
//        s.setLoad(list);
//        services.put(profile.getLabel(), s);
//    }
    
      /**
     * @return the profileManager
     */
    public static Profile getProfile() {
        return profileManager;
    }

    /**
     * @param aProfileManager the profileManager to set
     */
    public static void setProfile(Profile aProfileManager) {
        profileManager = aProfileManager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
