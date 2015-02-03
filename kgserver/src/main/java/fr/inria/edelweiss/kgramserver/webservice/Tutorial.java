package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgramserver.webservice.Service.Doc;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * SPARQL endpoint apply transformation (like Transformer)
 * Each service manage it's own TripleStore
 * TripleStore content is defined by server in profile.ttl
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("tutorial")
public class Tutorial {

    static final String TUTORIAL_SERVICE  = "/tutorial";
    static final String TUTORIAL1_SERVICE = "/tutorial/rdf";
    static final String TUTORIAL2_SERVICE = "/tutorial/rdfs";
    static final String TUTORIAL3_SERVICE = "/tutorial/huto";
    static final String CDN_SERVICE       = "/tutorial/cdn";
    
    static final String TUTORIAL1 = NSManager.STL + "tutorial1";
    static final String TUTORIAL2 = NSManager.STL + "tutorial2";
    static final String HUTO      = NSManager.STL + "huto";
    static final String CDN       = NSManager.STL + "cdn";
    static final String WEB       = NSManager.STL + "web";
    
    static final String STCONTEXT = Context.STL_CONTEXT;
    static HashMap<String, TripleStore> map;
    static NSManager nsm;

    static {
        init();
    }

    /**
     * Create a TripleStore for each server definition from profile
     * and load its content
     */
    static void init() {
        map = new HashMap<String, TripleStore>();
        nsm = NSManager.create();
        Profile p = getProfile();
        for (Service s : p.getServers()){
            System.out.println("Load: " + s.getName());
            try {
                createTripleStore(p, s);
            } catch (LoadException ex) {
                Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        complete();
    }
    
    static Profile getProfile(){
        return Transformer.getProfile();
    }
    
    
    static TripleStore createTripleStore(Profile p, Service s) throws LoadException{
        TripleStore store = new TripleStore(create(s), true);
        store.init(p.isProtected());
        map.put(s.getName(), store);
        return store;
    }

    /**
     * Create TripleStore and Load data from profile service definitions
     */
    static GraphStore create(Service s) throws LoadException {
        GraphStore g = GraphStore.create(s.isRDFSEntailment());
        Load ld = Load.create(g);

        for (Doc d : s.getData()) {
            ld.load(d.getUri(), d.getUri(), d.getName());
        }
        for (Doc d : s.getSchema()) {
            ld.load(d.getUri(), d.getUri(), d.getName());
        }
        
        if (s.getContext().size() > 0) {
            Graph gg = Graph.create();
            g.setNamedGraph(STCONTEXT, gg);
            Load lq = Load.create(gg);

            for (Doc d : s.getContext()) {
                lq.load(d.getUri(), d.getUri(), d.getName());
            }
            
            init(gg);
        }
        return g;
    }
    
    // context graph may contain service definition
    // add them to profile
    static void complete(){
        for (TripleStore ts : map.values()){
            Graph g = ts.getGraph().getNamedGraph(STCONTEXT);
            if (g != null){
                try {
                    getProfile().initLoader(g);
                } catch (EngineException ex) {
                    Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    static void init(Graph g){
        String init = 
                "insert { ?q st:index ?n }"
              + "where  { ?q a st:Query bind (kg:number()+1 as ?n) }";
        QueryProcess exec = QueryProcess.create(g);
        try {
            exec.query(init);
        } catch (EngineException ex) {
            Logger.getLogger(Tutorial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

   TripleStore getTripleStore(String name){
       return map.get(name);
   }
   
   /*
    * try uri, then name
    * uri is the URI of a query. If a dataset is assigned to uri, use the dataset
    * otherwise use default triple store
    * */
    TripleStore getTripleStore(String uri, String name) throws LoadException {
        if (uri != null) {
            String extURI = nsm.toNamespace(uri);
            TripleStore t = getTripleStore(extURI);
            if (t != null) {
                return t;
            }
            Service s = getProfile().getService(extURI);
            if (s != null) {              
                t = createTripleStore(getProfile(), s);
                TripleStore tt = getTripleStore(name);
                t.getGraph().setNamedGraph(STCONTEXT, tt.getGraph().getNamedGraph(STCONTEXT));
                return t;
            }
        }
        return getTripleStore(name);
    }
    
    
    
   
   // draft
     TripleStore getServer(String uri, String profile){
        if (profile == null){
            return Transformer.getTripleStore();
        }
        TripleStore st = getStore(profile);       
        if (st == null){
            st = Transformer.getTripleStore();
        }
        return st;
    }
     
     
    TripleStore getStore(String name){
        Service s = getProfile().getService(nsm.toNamespace(name));
        if (s == null || s.getServer() == null){
            return null;
        }
        return getTripleStore(s.getServer());
    }

  

    @POST
    @Produces("text/html")
    @Path("rdf")
    public Response tuto1Post(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL1_SERVICE, (profile==null)?WEB:profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(TUTORIAL1), par);
    }

    @GET
    @Produces("text/html")
    @Path("rdf")
    public Response tuto1Get(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL1_SERVICE, (profile==null)?WEB:profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(TUTORIAL1), par);
    }

    @POST
    @Produces("text/html")
    @Path("rdfs")
    public Response tuto2Post(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL2_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(TUTORIAL2), par);
    }

    @GET
    @Produces("text/html")
    @Path("rdfs")
    public Response tuto2Get(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL2_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(TUTORIAL2), par);
    }
    
    
     @POST
    @Produces("text/html")
    @Path("huto")
    public Response hutoPost(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL3_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(HUTO), par);
    }

    @GET
    @Produces("text/html")
    @Path("huto")
    public Response hutoGet(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL3_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(HUTO), par);
    }
    
    
    
    
     @POST
    @Produces("text/html")
    @Path("cdn")
    public Response cdnPost(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(CDN_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(CDN), par);
    }

    @GET
    @Produces("text/html")
    @Path("cdn")
    public Response cdnGet(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(CDN_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(CDN), par);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
     // draft
    @POST
    @Produces("text/html")
    public Response tutoPost(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);        
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getServer(resource, profile), par);
    }
    
    
  
    @GET
    @Produces("text/html")
    public Response tutoGet(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TUTORIAL_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getServer(resource, profile), par);
    }
   
   
    
}
