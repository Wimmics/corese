package fr.inria.corese.server.webservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.CSVFormat;
import fr.inria.corese.core.print.JSOND3Format;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.JSONLDFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.print.TSVFormat;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
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
public class SPARQLRestAPI {
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
    
    public static final String PROFILE_DEFAULT = "profile.ttl";
    public static final String DEFAULT = NSManager.STL + "default";
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
            logger.error("Error while querying the remote KGRAM-DQP engine", ex);
            return Response.status(ERROR).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Path("/d3")
    @Produces(SPARQL_RESULTS_JSON)
    public Response getTriplesJSONForGetWithGraph(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            if (logger.isDebugEnabled())
                logger.debug("Querying: " + query);
            if (query == null)
                throw new Exception("No query");
            Mappings m = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));

            String mapsD3 = "{ \"mappings\" : " + JSONFormat.create(m).toString() + " , " + "\"d3\" : " + JSOND3Format.create((Graph) m.getGraph()).toString() + " }";
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsD3).build();

        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    // ----------------------------------------------------
    // SPARQL QUERY - SELECT and ASK with HTTP GET
    // ----------------------------------------------------

    /**
     * Visitor call LDScript event @beforeRequest @public function 
     * profile.ttl must load function definitions, 
     * e.g. <demo/system/event.rq>
     * 
     */
    void beforeRequest(HttpServletRequest request, String query) {
        getVisitor().beforeRequest(request, query);
    }
    
    void afterRequest(HttpServletRequest request, String query, Mappings map) {
        getVisitor().afterRequest(request, query, map);
    }
    
    void afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res) {
        getVisitor().afterRequest(request, resp, query, map, res);
    }
    
    /**
     * Default function for HTTP GET:
     * This function is called when there is no header Accept
     * Additional HTTP parameter format: eg application/sparql-results+xml
     * Return Content-Type according to what is really returned
     * .
     */
    @GET
    @Produces({SPARQL_RESULTS_XML, XML})
    public Response getTriplesXMLForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("named-graph-uri")   List<String> namedGraphUris,
            @QueryParam ("format")           String format) {
        
        logger.info("getTriplesXMLForGet");
        String ft = request.getHeader("Accept");
        if (ft.contains(SPARQL_RESULTS_XML) || ft.contains(XML)) {
            format = SPARQL_RESULTS_XML;
        }
        return myTetTriplesXMLForGet(request, query, access, defaultGraphUris, namedGraphUris, format);
    }
           
    public Response myTetTriplesXMLForGet(HttpServletRequest request,
            String query, String access, 
            List<String> defaultGraphUris, List<String> namedGraphUris,
            String format) { 
    
        beforeRequest(request, query);
        
        try {
            if (logger.isDebugEnabled())
                logger.debug("Rest Get SPARQL Result XML/plain: " + query);
            if (query == null)
                throw new Exception("No query");

            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));                                
            ResultFormat rf = getResultFormat(map, format);
            String res = rf.toString();
            //System.out.println("format: " + rf.getContentType());
            Response resp = Response.status(200)
                    .header(headerAccept, "*")
                    // real content type of result, possibly different from @Produces
                    .header("Content-Type", rf.getContentType())
                    .entity(res).build(); 
            afterRequest(request, resp, query, map, res);            
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
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
    @Produces({"text/plain"})
    public Response getTriplesXMLForGet2(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("named-graph-uri")   List<String> namedGraphUris) {
        
        logger.info("getTriplesXMLForGet2");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TEXT_FORMAT);
    }
  
    Response myGetResult(HttpServletRequest request,
            String query, String access, 
            List<String> defaultGraphUris, List<String> namedGraphUris,
            int type) {
        try {
            if (query == null) {
                throw new Exception("No query");
            }
            beforeRequest(request, query);
            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
            ResultFormat ft = ResultFormat.create(map, type);
            System.out.println("Content-Type: " + ft.getContentType());
            String res = ft.toString();
            Response resp = Response.status(200).header(headerAccept, "*")
                    .entity(res).build();
            afterRequest(request, resp, query, map, res);
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces(SPARQL_RESULTS_JSON)
    public Response getTriplesJSONForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
       
        logger.info("getTriplesJSONForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.JSON_FORMAT);
    }
    


    @GET
    @Produces(SPARQL_RESULTS_CSV)
    public Response getTriplesCSVForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
       
        logger.info("getTriplesCSVForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.CSV_FORMAT);
    }

    @GET
    @Produces(SPARQL_RESULTS_TSV)
    public Response getTriplesTSVForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
       
        logger.info("getTriplesTSVForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TSV_FORMAT);
    }

    // ----------------------------------------------------
    // SPARQL QUERY - DESCRIBE and CONSTRUCT with HTTP GET
    // ----------------------------------------------------
    

    @GET
    @Produces(RDF_XML)
    public Response getRDFGraphXMLForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        
        logger.info("getRDFGraphXMLForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.RDF_XML_FORMAT);
    }

    @GET
    @Produces({TURTLE_TEXT, TURTLE, NT_TEXT})
    public Response getRDFGraphNTripleForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
       
        logger.info("getRDFGraphNTripleForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TURTLE_FORMAT);
    }
    
    @GET
    @Produces({TRIG_TEXT, TRIG})
    public Response getRDFGraphTrigForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
    
        logger.info("getRDFGraphTrigForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TRIG_FORMAT);
    }

    @GET
    @Produces({JSON, JSON_LD})
    public Response getRDFGraphJsonLDForGet(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
       
        logger.info("getRDFGraphJsonLDForGet");
        return myGetResult(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.JSON_LD_FORMAT);
    }

    // ----------------------------------------------------
    // SPARQL QUERY - SELECT and ASK with HTTP POST
    // ----------------------------------------------------

    String getQuery(String query, String message) {
        return (query.isEmpty()) ? message : query;
    }

    @POST
    @Produces({SPARQL_RESULTS_XML, XML, TEXT})
    @Consumes("application/sparql-query")
    public Response getXMLForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris, 
            @QueryParam ("format")  String format,
            String message) {
        try {
            logger.info("getXMLForPost");

            query = getQuery(query, message);
            if (logger.isDebugEnabled())
                logger.debug("Rest Post SPARQL Result XML/plain: " + query);
        
            beforeRequest(request, query);

            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
            String res = getResult(map, format);
            Response resp = Response.status(200).header(headerAccept, "*").entity(res).build();
            afterRequest(request, resp, query, map, res);
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Default POST function (ie when there is no header Accept)
     */
    @POST
    @Produces({SPARQL_RESULTS_XML, XML})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesXMLForPost(@Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, 
            @FormParam("format")  String format,
            String message) {
        try {
            logger.info("getTriplesXMLForPost");
            query = getQuery(query, message);

            if (logger.isDebugEnabled())
                logger.debug("Rest Post SPARQL Result XML/plain: " + query);
            
            beforeRequest(request, query);
            
            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
            String ft = request.getHeader("Accept");
            if (ft.contains(SPARQL_RESULTS_XML) || ft.contains(XML)) {
                format = SPARQL_RESULTS_XML;
            }
            ResultFormat rf = getResultFormat(map, format);
            String res = rf.toString();
            Response resp = Response.status(200)
                    .header(headerAccept, "*")
                    .header("Content-Type", rf.getContentType())
                    .entity(res).build();
            afterRequest(request, resp, query, map, res);
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
    
    
    @POST
    @Produces(TEXT)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesTEXTForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
            query = getQuery(query, message);

            logger.info("getTriplesTEXTForPost");       
        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TEXT_FORMAT);
    }
    
    
    @POST
    @Produces(SPARQL_RESULTS_JSON)
    @Consumes("application/sparql-query")
    public Response getTriplesJSONForPostNew(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris, 
            String message) {
//        try {            
//            logger.info("getTriplesJSONForPostNew");
//            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
//            return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(map).toString()).build();
//        } catch (Exception ex) {
//            logger.error("Error while querying the remote KGRAM engine", ex);
//            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
//        }
        logger.info("getTriplesJSONForPostNew");
        query = getQuery(query, message);

        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.JSON_FORMAT);
    }


    @POST
    @Produces(SPARQL_RESULTS_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesJSONForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {

        query = getQuery(query, message);
        logger.info("getTriplesJSONForPost");       
        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.JSON_FORMAT);
    }
    
    
    Response getResultForPost(HttpServletRequest request,
            String query, String access, 
            List<String> defaultGraphUris,
            List<String> namedGraphUris, int format) {
        try {           
            beforeRequest(request, query);
            Mappings map = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
            ResultFormat rf = ResultFormat.create(map, format);
            String res = rf.toString();
            Response resp = Response.status(200).header(headerAccept, "*").entity(res).build();
            afterRequest(request, resp, query, map, res);
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
    

    @POST
    @Produces(SPARQL_RESULTS_CSV)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesCSVForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
        try {
            query = getQuery(query, message);
            logger.info("getTriplesCSVForPost");

            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getTripleStore()
                    .query(request, query, createDataset(defaultGraphUris, namedGraphUris, access))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces(SPARQL_RESULTS_TSV)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getTriplesTSVForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
        try {
            query = getQuery(query, message);
            logger.info("getTriplesTSVForPost");

            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getTripleStore()
                    .query(request, query, createDataset(defaultGraphUris, namedGraphUris, access))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    // ----------------------------------------------------
    // SPARQL QUERY - DESCRIBE and CONSTRUCT with HTTP POST
    // ----------------------------------------------------

    @POST
    @Produces(RDF_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphXMLForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {

        query = getQuery(query, message);

        logger.info("getRDFGraphXMLForPost");            
        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.RDF_XML_FORMAT);
    }

    @POST
    @Produces({TURTLE_TEXT, TURTLE, NT_TEXT})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphNTripleForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query,
            @FormParam("access") String access,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {

        query = getQuery(query, message);
        logger.info("getRDFGraphNTripleForPost");
        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.TURTLE_FORMAT);        
    }

    @POST
    @Produces({JSON, JSON_LD})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getRDFGraphJsonLDForPost(@javax.ws.rs.core.Context HttpServletRequest request,
            @DefaultValue("") @FormParam("query") String query, 
            @FormParam("access") String access, 
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
        query = getQuery(query, message);
        logger.info("getRDFGraphJsonLDForPost");
        return getResultForPost(request, query, access, defaultGraphUris, namedGraphUris, ResultFormat.JSON_LD_FORMAT);        
        
    }

	/* Fix Franck: for some reason, this service tends to be selected for SPARQL queries, not updates.
	 * Temporary fix is to only allow an update using the application/sparql-update content type (below)
	/// SPARQL 1.1 Update ///
	// update via URL-encoded POST
	@POST
	@Consumes("application/x-www-form-urlencoded")
	//@Path("/update")
	public Response updateTriplesEncoded(@FormParam("update") String update, @FormParam("using-graph-uri") List<String> defaultGraphUris, @FormParam("using-named-graph-uri") List<String> namedGraphUris) {
		try {
			if (update != null) {
				logger.debug(update);
				getTripleStore().query(update, createDataset(defaultGraphUris, namedGraphUris));
			} else {
				logger.warn("Null update query !");
			}

			return Response.status(200).header(headerAccept, "*").build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
		}
	}
	*/

    // ----------------------------------------------------
    // SPARQL UPDATE with HTTP POST
    // ----------------------------------------------------

    @POST
    @Consumes("application/sparql-update")
    public Response updateTriplesDirect(@javax.ws.rs.core.Context HttpServletRequest request,
            String message, // standard parameter, do not add @QueryParam()
            @QueryParam("access") String access, 
            @QueryParam("using-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("using-named-graph-uri") List<String> namedGraphUris) {
        try {
            //request.
            logger.info("updateTriplesDirect");  
            Mappings map = null;
            if (message != null) {
                logger.debug(message);
                beforeRequest(request, message);
                map = getTripleStore().query(request, message, createDataset(defaultGraphUris, namedGraphUris, access));
            } else {
                logger.warn("Null update query !");
            }

            Response resp = Response.status(200).header(headerAccept, "*").build();
            afterRequest(request, resp, message, map, resp.getEntity().toString());
            return resp;
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
        }
    }

    @HEAD
    public Response getTriplesForHead(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("query") String query, 
            @QueryParam("access") String access, 
            @QueryParam("default-graph-uri") List<String> defaultGraphUris, 
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            logger.info("getTriplesForHead");
            Mappings mp = getTripleStore().query(request, query, createDataset(defaultGraphUris, namedGraphUris, access));
            return Response.status(mp.size() > 0 ? 200 : 400).header(headerAccept, "*").entity("Query has no response").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine", ex);
            return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Creates a Corese/KGRAM Dataset based on a set of default or named graph URIs. 
     * For *strong* SPARQL compliance, use dataset.complete() before returning the dataset.
     *
     * @param defaultGraphUris
     * @param namedGraphUris
     * @return a dataset if the parameters are not null or empty.
     */

//    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
//        return createDataset(defaultGraphUris, namedGraphUris, null);
//    }
    
    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris, String access) {
        Dataset ds = null;
        if (((defaultGraphUris != null) && (!defaultGraphUris.isEmpty())) 
                || ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
            ds = Dataset.instance(defaultGraphUris, namedGraphUris);
        } 
        else {
            ds = new Dataset();
        }
        Level level = Access.getQueryAccessLevel(true, hasKey(access));
        ds.getCreateContext().setLevel(level);
        return ds;
    }
    
    // access key gives special access level (RESTRICTED vs PUBLIC)
    static boolean hasKey(String access) {
        return access!=null && getKey() != null && getKey().equals(access);
    }
    
    
//    Level getLevel(String access) {
//        Access.Level level = Access.Level.DEFAULT;
//        if (access != null) {
//            try { 
//                level = Access.Level.valueOf(access);
//                level = level.min(Access.Level.DEFAULT);
//            }
//            catch (Exception e) {
//                logger.error(e.getMessage());
//            }
//        }
//        return level;
//    }
     

    /**
     * This function is used to copy the InputStream into a local file.
     */
    private void writeToFile(InputStream uploadedInputStream, File uploadedFile) throws IOException {
        OutputStream out = new FileOutputStream(uploadedFile);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    String getTemplate(String name) {
        String sep = "";
        if (name.contains("/")) {
            sep = "'";
        }
        String query = "template { st:atw(" + sep + name + sep + ")} where {}";
        return query;
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
