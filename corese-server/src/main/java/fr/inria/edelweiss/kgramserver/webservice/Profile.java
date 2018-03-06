package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import static fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer.port;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.core.GraphStore;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;
import fr.inria.corese.kgtool.transform.ContextBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.logging.log4j.Level;
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

    static final String NL = System.getProperty("line.separator");
    
    private static  Profile profileManager;
    //static  String SERVER, DATA, QUERY;

  
    String server, data, query;
      
    HashMap<String,  Service> services, servers; 
    NSManager nsm;
    IDatatype profileDatatype;
    GraphStore profileGraph;

    boolean isProtected = false;

//    static {
//        initServer();   
//    }
    
    private static String stdLocalhost() {
        return "http://localhost:" + EmbeddedJettyServer.port;
    }
    
    private static String getLocalhost() throws UnknownHostException {
         String s = "http://" + InetAddress.getLocalHost().getCanonicalHostName();
         s += (port == 80) ? "" : ":" + port;
         return s;
    }
        
//    private static void initServer(){
//         try {
//            SERVER = getLocalhost();
//        } catch (UnknownHostException ex) {
//            SERVER = stdLocalhost();
//        }
//        completeHost();     
//    }
    

//    private static void completeHost(){
//        DATA = SERVER + "/data/";
//        QUERY = DATA + "query/";
//    }
    
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
    
    
//    String getServer(){
//        return SERVER;
//    }
//    
//    String getQueryPath(String name){
//        return QUERY + name;
//    }  
//    
//    String getDataPath(String name){
//        return DATA + name;
//    }
   
    

    public Profile() {
        this(false);
    }

    Profile(boolean localhost) {
        initServerData(localhost);
        services = new HashMap();
        servers = new HashMap();
        nsm = NSManager.create();
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
    Param complete(Param par) throws IOException {
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
        else if (isProtected) {
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
                LogManager.getLogger(Profile.class.getName()).warn("Load: " + name);
                load(g, name);
                // focus process on ?p = name
                initService(g, name);
            } catch (LoadException ex) {
                Logger.getLogger(Profile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Profile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (EngineException ex) {
                Logger.getLogger(Profile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            GraphStore g = load(path);
            if (local != null){
            LogManager.getLogger(Profile.class.getName()).warn("Load: " + local);
                load(g, local);
            }
            setProfile(g);
            process(g);
            initFunction();
        } catch (IOException ex) {
            LogManager.getLogger(Profile.class.getName()).log(Level.ERROR, "", ex);
        } catch (LoadException ex) {
            LogManager.getLogger(Profile.class.getName()).log(Level.ERROR, "", ex);
        } catch (EngineException ex) {
            LogManager.getLogger(Profile.class.getName()).log(Level.ERROR, "", ex);
        }
    }
    
    void process(Graph g) throws IOException, EngineException {
        initService(g);
        initServer(g);
    }
    
    

    GraphStore loadServer(String name) throws IOException, LoadException {
        return load(getDataPath(name));
    }

    GraphStore load(String path) throws IOException, LoadException {
        GraphStore g = GraphStore.create();
        load(g, path);
        return g;
    }
    
    void load(GraphStore g, String path) throws LoadException{
        Load load = Load.create(g);
        load.parse(path, Load.TURTLE_FORMAT);       
    }

    String read(String path) throws IOException {
        QueryLoad ql = QueryLoad.create();
        String res = ql.read(path);
        if (res == null) {
            throw new IOException(path);
        }
        return res;
    }

    String loadQuery(String path) throws IOException {
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
    
     void initFunction2() throws IOException, EngineException{
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

}
