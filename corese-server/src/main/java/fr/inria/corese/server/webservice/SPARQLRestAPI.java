package fr.inria.corese.server.webservice;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.CSVFormat;
import fr.inria.corese.core.print.JSOND3Format;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.print.TSVFormat;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.URLParam;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * KGRAM SPARQL endpoint exposed as a rest web service.
 * <p>
 * The engine can be remotely initialized, populated with an RDF file, and queried through SPARQL requests.
 *
 * @author Eric TOGUEM, eric.toguem@uy1.uninet.cm
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 * @author Olivier Corby
 */
@Path("sparql")
public class SPARQLRestAPI implements ResultFormatDef, URLParam {
    private static final String ERROR_ENDPOINT = "Error while querying Corese SPARQL endpoint";

    private static final String headerAccept = "Access-Control-Allow-Origin";
    static final String SPARQL_RESULTS_XML  = ResultFormat.SPARQL_RESULTS_XML;
    static final String SPARQL_RESULTS_JSON = ResultFormat.SPARQL_RESULTS_JSON;
    static final String SPARQL_RESULTS_CSV  = ResultFormat.SPARQL_RESULTS_CSV;
    static final String SPARQL_RESULTS_TSV  = ResultFormat.SPARQL_RESULTS_TSV;
    
    static final String XML         = ResultFormat.XML;
    static final String RDF_XML     = ResultFormat.RDF_XML;
    static final String TURTLE      = ResultFormat.TURTLE;
    static final String TURTLE_TEXT = ResultFormat.TURTLE_TEXT; 
    static final String JSON_LD     = ResultFormat.JSON_LD;
    static final String JSON        = ResultFormat.JSON;
    static final String TRIG        = ResultFormat.TRIG;
    static final String TRIG_TEXT   = ResultFormat.TRIG_TEXT; 
    static final String NT_TEXT     = ResultFormat.NT_TEXT; 
    static final String TEXT        = ResultFormat.TEXT; 
    static final String HTML        = ResultFormat.HTML;
    
    public static final String PROFILE_DEFAULT = "profile.ttl";
    public static final String DEFAULT = NSManager.STL + "default";
       
    private static final String URL = "url";   
    private static final String OPER = "operation";    
    
    static final int ERROR = 500;

    private static boolean isDebug = false;
    private static boolean isDetail = false;
    // set true to prevent update/load
    static boolean isProtected = !true;
    // true when Ajax
    static boolean isAjax = true;

    static String localProfile;

    static TripleStore store = new TripleStore(false, false);

    private static Profile mprofile;

    static private final Logger logger = LogManager.getLogger(SPARQLRestAPI.class);
    private static String key;
    
    static {
    }
    
    QuerySolverVisitorServer visitor;

    public SPARQLRestAPI() {
        setVisitor(QuerySolverVisitorServer.create(createEval()));
    }
    
     /**
     * Current graph is SPARQL endpoint graph.
     */
    static Eval createEval() {
        QueryProcess exec = QueryProcess.create(getTripleStore().getGraph());
        try {
            return exec.getEval();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
    

    QueryProcess getQueryProcess() {
        return getTripleStore().getQueryProcess();
    }

    static TripleStore getTripleStore() {
        return store;
    }
    
    static TripleStore getTripleStore (String name) {
           if (name == null) {
               return getTripleStore();
           }
           return Manager.getEndpoint(name);
    }
    
    QuerySolverVisitorServer getVisitor() {
        return visitor;
    }
    
    void setVisitor(QuerySolverVisitorServer vis) {
        visitor = vis;
    }

    /**
     * This webservice is used to reset the endpoint. This could be useful if we would like our endpoint to point on another dataset
     */
    @POST
    @Path("/reset")
    public Response initRDF(
            @DefaultValue("false") @FormParam("owlrl") String owlrl,
            @DefaultValue("false") @FormParam("entailments") String entailments,
            @DefaultValue("false") @FormParam("load") String load,
            @FormParam("profile") String profile,
            @DefaultValue("false") @FormParam("localhost") String localhost
    ) {
        logger.info("entering initRDF");
        boolean ent = entailments.equals("true");
        boolean owl = owlrl.equals("true");
        boolean ld = load.equals("true");
        localProfile = profile;
        System.out.println("entailment: " + ent);
        store = new TripleStore(ent, owl);
        init(localhost.equals("true"));
        if (ld) {
            // loadProfileData();
            Manager.getManager().init(store);
        }
        store.init(isProtected);
        setVisitor( QuerySolverVisitorServer.create(createEval()));
        getVisitor().initServer(EmbeddedJettyServer.BASE_URI);
        init();
        return Response.status(200).header(headerAccept, "*").entity("Endpoint reset").build();
    }
    
    void init(){
        if (getKey() == null) {
            setKey(genkey());
        }
        logger.info("key: "+ getKey());
    }
    
    String genkey() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
        // access key gives special access level (RESTRICTED vs PUBLIC)
    static boolean hasKey(String access) {
        return access!=null && getKey() != null && getKey().equals(access);
    }
    
    
    void init(boolean localhost) {
        mprofile = new Profile(localhost);
        mprofile.setProtect(isProtected);
        Profile.setProfile(mprofile);
        if (localProfile != null) {
            localProfile = NSManager.toURI(localProfile);
            logger.info( "Load: " + localProfile);
        }
        mprofile.initServer(PROFILE_DEFAULT, localProfile);
    }

    /**
     * This webservice is used to load a dataset to the endpoint. Therefore, if we have many files for our datastore, we could load them by recursivelly calling this webservice
     */
    @POST
    @Path("/load")
    public Response loadRDF(
            @FormParam("remote_path") String remotePath,
            @FormParam("source") String source
    ) {
        logger.traceEntry("loadRDF");
        String output = "File Uploaded";
        if (source != null) {
            if (source.isEmpty()) {
                source = null;
            } else if (!source.startsWith("http://")) {
                source = "http://" + source;
            }
        }

        if (remotePath == null) {
            String error = "Null remote path";
            logger.error(error);
            return Response.status(404).header(headerAccept, "*").entity(error).build();
        }

        logger.debug(remotePath);

        try {
            // path with extension : use extension
            // path with no extension : load as turtle
            // use case: rdf: is in Turtle
            if (! getTripleStore().isProtect()) { //getMode() != QueryProcess.PROTECT_SERVER_MODE) {
                getTripleStore().load(remotePath, source);
            }
        } catch (LoadException ex) {
            logger.error(ex);
            return Response.status(404).header(headerAccept, "*").entity(output).build();
        }

        logger.info(output = "Successfully loaded " + remotePath);
        return Response.status(200).header(headerAccept, "*").entity(output).build();
    }

    @GET
    @Path("/debug")
    public Response setDebug(@QueryParam("value") String debug, @QueryParam("detail") String detail) {
        if (debug != null) {
            isDebug = debug.equals("true");
        }
        if (detail != null) {
            isDetail = detail.equals("true");
        }
        return Response.status(200).header(headerAccept, "*").entity("debug: " + isDebug + " ; " + "detail: " + isDetail).build();
    }



    // ----------------------------------------------------
    // SPARQL QUERY - SELECT and ASK with HTTP GET
    // ----------------------------------------------------


    
    /**
     * Default function for HTTP GET:
     * This function is called when there is no header Accept
     * Additional HTTP parameter format: eg application/sparql-results+xml
     * Return Content-Type according to what is really returned
     * 
     * PathParam name may come from SPARQLService 
     * with URL http://corese.inria.fr/name/sparql
     * In this case, name is the name of the service (eg ai4eu) which manages the RDF graph to query
     * instead of default SPARQL endpoint
     * param oper may specify oper=federate or oper=sparql in federation mode
     * .
     */
    @GET
    @Produces({SPARQL_RESULTS_XML, XML})
    public Response getTriplesXMLForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            // name of server from SPARQLService
            @PathParam("name") String name,
            // name of federation from SPARQLService
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri")   List<String> named,
            @QueryParam("format")           String format,
            @QueryParam("transform")        List<String> transform,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
        
        logger.info("getTriplesXMLForGet");
        String ft = request.getHeader("Accept");
        System.out.println("accept: " + ft);
        if (ft.contains(SPARQL_RESULTS_XML) || ft.contains(XML)) {
            // Explicit @Produces, skip format parameter 
            format = SPARQL_RESULTS_XML;
        }
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, format, UNDEF_FORMAT, transform);
    }
      
    
    public Response getResultFormat(HttpServletRequest request,
            String name, String oper, List<String> uri, List<String> param, List<String> mode,
            String query, String access, 
            List<String> defaut, List<String> named,
            String format, int type, List<String> transform) { 
        return new SPARQLResult(request).setVisitor(getVisitor())
        .getResultFormat(name, oper, uri, param, mode, query, access, defaut, named, format, type, transform);
    }
    
   
    /**
     * Std endpoint function
     * type is the return format, eg JSON format
     * Content-Type is set by @Produces annotation of the calling function.
     * 
     */
    Response myGetResult(HttpServletRequest request, String name,
            String oper, List<String> uri, List<String> param, List<String> mode,
            String query, String access,
            List<String> defaut, List<String> named,
            int type) {
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, null, type, null);
    }
            
     
    /**
     * parameter format=application/sparql-results+json
     * format may be null: return default format wrt map kind
     * template with format return format
     * template without format return text/plain
     * 
     */
    ResultFormat getResultFormat(Mappings map, String format) {        
        return ResultFormat.create(map, format);
    }
    
    String getResult(Mappings map, String format) {
        return getResultFormat(map, format).toString();
    }
    
    
    @GET
    @Produces({HTML})
    public Response getHTMLForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            // name of server from SPARQLService
            @PathParam("name") String name,
            // name of federation from SPARQLService
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri")   List<String> named,
            @QueryParam("format")           String format,
            @QueryParam("transform")        List<String> transform,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
        
        logger.info("getHTMLForGet");
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, null, HTML_FORMAT, transform);
    }
    
    @GET
    @Produces({"text/plain"})
    public Response getTriplesXMLForGet2(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri")   List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
        
        logger.info("getTriplesXMLForGet2");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, TEXT_FORMAT);
    }
  

    @GET
    @Produces(SPARQL_RESULTS_JSON)
    public Response getTriplesJSONForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("transform")        List<String> transform,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
       
        logger.info("getTriplesJSONForGet");
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, null, JSON_FORMAT, transform);
    }
    


    @GET
    @Produces(SPARQL_RESULTS_CSV)
    public Response getTriplesCSVForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
       
        logger.info("getTriplesCSVForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, CSV_FORMAT);
    }

    @GET
    @Produces(SPARQL_RESULTS_TSV)
    public Response getTriplesTSVForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
       
        logger.info("getTriplesTSVForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, TSV_FORMAT);
        }

    // ----------------------------------------------------
    // SPARQL QUERY - DESCRIBE and CONSTRUCT with HTTP GET
    // ----------------------------------------------------
    

    @GET
    @Produces(RDF_XML)
    public Response getRDFGraphXMLForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
        
        logger.info("getRDFGraphXMLForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, RDF_XML_FORMAT);
    }

    @GET
    @Produces({TURTLE_TEXT, TURTLE, NT_TEXT})
    public Response getRDFGraphNTripleForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
       
        logger.info("getRDFGraphNTripleForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, TURTLE_FORMAT);
    }
    
    @GET
    @Produces({TRIG_TEXT, TRIG})
    public Response getRDFGraphTrigForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
    
        logger.info("getRDFGraphTrigForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, TRIG_FORMAT);
    }

    @GET
    @Produces({JSON, JSON_LD})
    public Response getRDFGraphJsonLDForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
       
        logger.info("getRDFGraphJsonLDForGet");
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, JSON_LD_FORMAT);
    }

    // ----------------------------------------------------
    // SPARQL QUERY - SELECT and ASK with HTTP POST
    // ----------------------------------------------------

    String getQuery(String query, String message) {
        return (query.isEmpty()) ? message : query;
    }

    @POST
    @Produces({SPARQL_RESULTS_XML, XML})
    @Consumes("application/sparql-query")
    public Response getXMLForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @PathParam("oper") String oper,
            @DefaultValue("") @QueryParam("query") String query,
            @QueryParam("access") String access,
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            String message,
            @QueryParam("param") List<String> param,
            @QueryParam("mode") List<String> mode,
            @QueryParam("uri") List<String> uri) {

        logger.info("getXMLForPost");

        query = getQuery(query, message);

        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, XML_FORMAT);
    }

    
    @POST
    @Produces({TEXT})
    @Consumes("application/sparql-query")
    public Response getXMLForPostText(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named, 
            String message,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri) {
        
        logger.info("getXMLForPostText");

        query = getQuery(query, message);

        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, TEXT_FORMAT);
    }
        

    /**
     * Default POST function (ie when there is no header Accept)
     * SPARQL service clause executed here by corese
     */
    @POST
    @Produces({SPARQL_RESULTS_XML, XML})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesXMLForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            // name of server with a specific rdf graph (SPARQLService)
            @PathParam("name") String name, 
            // name of federation  (SPARQLFederate)
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named, 
            @FormParam("format")  String format,
            @FormParam("transform")  List<String> transform,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri,
            String message) {
        
        logger.info("getTriplesXMLForPost");
        query = getQuery(query, message);

        String ft = request.getHeader("Accept");
        if (ft.contains(SPARQL_RESULTS_XML) || ft.contains(XML)) {
            format = SPARQL_RESULTS_XML;
        }   
                
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, format, UNDEF_FORMAT, transform);
    }

    
    @POST
    @Produces(TEXT)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesTEXTForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri, 
            String message) {
            query = getQuery(query, message);

            logger.info("getTriplesTEXTForPost");       
        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, TEXT_FORMAT);
    }
    
    
    @POST
    @Produces(SPARQL_RESULTS_JSON)
    @Consumes("application/sparql-query")
    public Response getTriplesJSONForPostNew(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named,
            @QueryParam("param")  List<String> param,
            @QueryParam("mode")   List<String> mode,
            @QueryParam("uri")    List<String> uri, 
            String message) {
        logger.info("getTriplesJSONForPostNew");
        query = getQuery(query, message);

        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, JSON_FORMAT);
    }


    @POST
    @Produces(SPARQL_RESULTS_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesJSONForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("transform") List<String> transform,  
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri,  
            String message) {

        query = getQuery(query, message);
        logger.info("getTriplesJSONForPost");       
        //return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, ResultFormat.JSON_FORMAT);
        return getResultFormat(request, name, oper, uri, param, mode, query, access, defaut, named, null, JSON_FORMAT, transform);    
    }
    
    
    Response getResultForPost(HttpServletRequest request,
            String name, 
            String oper,
            List<String> uri,              
            List<String> param,
            List<String> mode,
            String query, String access, 
            List<String> defaut,
            List<String> named,
            int format) {
        return myGetResult(request, name, oper, uri, param, mode, query, access, defaut, named, format);
    }
    


    @POST
    @Produces(SPARQL_RESULTS_CSV)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesCSVForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named, 
            String message) {
        try {
            query = getQuery(query, message);
            logger.info("getTriplesCSVForPost");

            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getTripleStore(name)
                    .query(request, query, createDataset(request, defaut, named, access))).toString()).build();
        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }
    

    
    @POST
    @Produces(SPARQL_RESULTS_TSV)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesTSVForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named, String message) {
        try {
            query = getQuery(query, message);
            logger.info("getTriplesTSVForPost");

            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getTripleStore(name)
                    .query(request, query, createDataset(request, defaut, named, access))).toString()).build();
        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }

    // ----------------------------------------------------
    // SPARQL QUERY - DESCRIBE and CONSTRUCT with HTTP POST
    // ----------------------------------------------------

    @POST
    @Produces(RDF_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphXMLForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri,  
            
            String message) {

        query = getQuery(query, message);

        logger.info("getRDFGraphXMLForPost");            
        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, RDF_XML_FORMAT);
    }

    @POST
    @Produces({TURTLE_TEXT, TURTLE, NT_TEXT})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphNTripleForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query,
            @FormParam("access") String access,
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri,  
            String message) {

        query = getQuery(query, message);
        logger.info("getRDFGraphNTripleForPost");
        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, TURTLE_FORMAT);        
    }

    @POST
    @Produces({JSON, JSON_LD})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphJsonLDForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name, 
            @PathParam("oper") String oper, 
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaut,
            @FormParam("named-graph-uri") List<String> named,
            @FormParam("param")  List<String> param,
            @FormParam("mode")   List<String> mode,
            @FormParam("uri")    List<String> uri,  
            String message) {
        query = getQuery(query, message);
        logger.info("getRDFGraphJsonLDForPost");
        return getResultForPost(request, name, oper, uri, param, mode, query, access, defaut, named, JSON_LD_FORMAT);        
        
    }


    // ----------------------------------------------------
    // SPARQL UPDATE with HTTP POST
    // ----------------------------------------------------

    Dataset createDataset(HttpServletRequest request, List<String> defaut, List<String> named, String access) {
        return SPARQLResult.getSingleton().createDataset(request, defaut, named, access);
    }
    
    void beforeRequest(HttpServletRequest request, String query) {
        getVisitor().beforeRequest(request, query);
    }
    
    void afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res) {
        getVisitor().afterRequest(request, resp, query, map, res);
    }
    
    @POST
    @Consumes("application/sparql-update")
    public Response updateTriplesDirect(@javax.ws.rs.core.Context HttpServletRequest request,
            String message, // standard parameter, do not add @QueryParam()
            @PathParam("name") String name, 
            @QueryParam("access") String access, 
            @QueryParam("using-graph-uri") List<String> defaut, 
            @QueryParam("using-named-graph-uri") List<String> named) {
        try {
            //request.
            logger.info("updateTriplesDirect");  
            Mappings map = null;
            if (message != null) {
                logger.debug(message);
                beforeRequest(request, message);
                map = getTripleStore(name).query(request, message, createDataset(request, defaut, named, access));
            } else {
                logger.warn("Null update query !");
            }

            Response resp = Response.status(200).header(headerAccept, "*").build();
            afterRequest(request, resp, message, map, resp.getEntity().toString());
            return resp;
        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }

    @HEAD
    public Response getTriplesForHead(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @PathParam("name") String name, 
            @QueryParam("default-graph-uri") List<String> defaut, 
            @QueryParam("named-graph-uri") List<String> named) {
        try {
            logger.info("getTriplesForHead");
            Mappings mp = getTripleStore(name).query(request, query, createDataset(request, defaut, named, access));
            return Response.status(mp.size() > 0 ? 200 : 400).header(headerAccept, "*").entity("Query has no response").build();
        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }
    
        @GET
    @Path("/draw")
    @Produces(SPARQL_RESULTS_JSON)
    public Response getJSON(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query) {
        if (logger.isDebugEnabled())
            logger.debug("Querying: " + query);
        try {
            if (query == null)
                throw new Exception("No query");
            Mappings maps = getTripleStore().query(request, query);
            logger.info(maps.size());

            Graph g = (Graph) maps.getGraph();
            String mapsProvJson = "{ \"mappings\" : " + JSONFormat.create(maps).toString() + " , " + "\"d3\" : " + JSOND3Format.create(g).toString() + " }";
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsProvJson).build();

        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header("Access-Control-Allow-Origin", "*").entity(ERROR_ENDPOINT).build();
        }
    }

    @GET
    @Path("/d3")
    @Produces(SPARQL_RESULTS_JSON)
    public Response getTriplesJSONForGetWithGraph(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaut,
            @QueryParam("named-graph-uri") List<String> named) {
        try {
            if (logger.isDebugEnabled())
                logger.debug("Querying: " + query);
            if (query == null)
                throw new Exception("No query");
            Mappings m = getTripleStore().query(request, query, createDataset(request, defaut, named, access));

            String mapsD3 = "{ \"mappings\" : " + JSONFormat.create(m).toString() + " , " + "\"d3\" : " + JSOND3Format.create((Graph) m.getGraph()).toString() + " }";
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsD3).build();

        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }

  

    /**
     * @return the key
     */
    public static String getKey() {
        return key;
    }

    /**
     * @param aKey the key to set
     */
    public static void setKey(String aKey) {
        key = aKey;
    }
  
}
