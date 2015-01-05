
package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramserver.webservice.Profile.Service;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.HTMLFormat;
import fr.inria.edelweiss.kgtool.print.JSOND3Format;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
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
import org.apache.log4j.Logger;

/**
 * KGRAM engine exposed as a rest web service. The engine can be remotely
 * initialized, populated with an RDF file, and queried through SPARQL requests.
 *
 * @author Eric TOGUEM, eric.toguem@uy1.uninet.cm
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 * @author Olivier Corby
 */
@Path("sparql")
public class SPARQLRestAPI {
    private static final String headerAccept = "Access-Control-Allow-Origin";
    private static final String DEFAULT_TRANSFORM = Transformer.HTML;    
    private static final String TEMPLATE_SERVICE = "/sparql/template";
    public static final String TOSPIN_SERVICE    = "/sparql/tospin";
    public static final String TOSPARQL_SERVICE  = "/sparql/tosparql";
    public static final String SDK_SERVICE       = "/sparql/sdk";
    
    private static Logger logger = Logger.getLogger(SPARQLRestAPI.class);
    private static boolean isDebug = false;
    private static boolean isDetail = false;
    
    private static GraphStore graph  = GraphStore.create(false);
    private static QueryProcess exec = QueryProcess.create(graph);
   
    private static Profile mprofile;
    private static NSManager nsm;
  
    
    
    public SPARQLRestAPI(){
        
    }
            
    QueryProcess getQueryProcess(){
        return exec; //QueryProcess.create(graph);
    }

    /**
     * This webservice is used to reset the endpoint. This could be useful if we
     * would like our endpoint to point on another dataset
     */
    @POST
    @Path("/reset")
    public Response initRDF(
            @DefaultValue("false") @FormParam("owlrl") String owlrl,
            @DefaultValue("false") @FormParam("entailments") String entailments) {
        String output;
        //exec.getGraph().remove();
        boolean ent = entailments.equals("true");
        boolean owl = owlrl.equals("true");
        graph = GraphStore.create(ent);
        exec = QueryProcess.create(graph);
        //exec.setMode(QueryProcess.SERVER_MODE);
        if (ent) {            
            logger.info(output = "Endpoint successfully reset *with* RDFS entailments.");
        } else {
            logger.info(output = "Endpoint successfully reset *without* RDFS entailments.");
        }
        
        if (owl){
            RuleEngine re = RuleEngine.create(graph);
            re.setProfile(RuleEngine.OWL_RL_LITE);
            graph.addEngine(re);
        }
        
        logger.info("OWL RL: " + owl);
        init();
        return Response.status(200).header(headerAccept, "*").entity(output).build();
    }
    
    void init(){
        nsm = NSManager.create();
        mprofile = new Profile();
        
        for (Service s : mprofile.getServices()){
            String[] load = s.getLoad();
            if (load != null){
                Load ld = Load.create(graph);
                for (String f : load){
                    try {
                        logger.info("Load: " + f);
                        ld.loadWE(f, f, Load.TURTLE_FORMAT);
                    } catch (LoadException ex) {
                        java.util.logging.Logger.getLogger(SPARQLRestAPI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
//    @POST
//    @Path("/upload")
//    @Consumes("multipart/form-data")
//    public Response uploadFile(@FormDataParam("file") InputStream f) {
//
//        // your code here to copy file to destFile
//        System.out.println("Received file " + f);
//
//        String output;
//        logger.info(output = "File uploaded.");
//        return Response.status(200).header(headerAccept, "*").entity(output).build();
//    }

    /**
     * This webservice is used to load a dataset to the endpoint. Therefore, if
     * we have many files for our datastore, we could load them by recursivelly
     * calling this webservice
     */
    @POST
    @Path("/load")
    public Response loadRDF(@FormParam("remote_path") String remotePath, @FormParam("source") String source) {

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
        
        
        Load ld = Load.create(graph);
        try {
            // path with extension : use extension
            // path with no extension : load as turtle 
            // use case: rdf: is in Turtle
            if (exec.getMode() != QueryProcess.SERVER_MODE){
                ld.loadWE(remotePath, source, Load.TURTLE_FORMAT);
            }
        } catch (LoadException ex) {
            logger.error(ex);
            return Response.status(404).header(headerAccept, "*").entity(output).build();
        }
        
        logger.info(output = "Successfully loaded " + remotePath);
        return Response.status(200).header(headerAccept, "*").entity(output).build();

//        if (remotePath.startsWith("http")) {
//            if (remotePath.endsWith(".rdf") || remotePath.endsWith(".ttl") || remotePath.endsWith(".rdfs") || remotePath.endsWith(".owl")) {
//                Load ld = Load.create(graph);
//                ld.load(remotePath, source);
//            } else {
//                //TODO loading of .n3 or .nt
//                logger.error("TODO loading of .n3 or .nt");
//                return Response.status(404).header(headerAccept, "*").entity(output).build();
//            }
//
//        } else {
//            logger.info("Loading " + remotePath);
//            File f = new File(remotePath);
//            if (!f.exists()) {
//                logger.error(output = "File " + remotePath + " not found on the server!");
//                return Response.status(404).header(headerAccept, "*").entity(output).build();
//            }
//            if (f.isDirectory()) {
//                Load ld = Load.create(graph);
//                ld.load(remotePath, source);
//            } else if (remotePath.endsWith(".rdf") || remotePath.endsWith(".rdfs") || remotePath.endsWith(".ttl") || remotePath.endsWith(".owl")) {
//                Load ld = Load.create(graph);
//                ld.load(remotePath, source);
//            } else if (remotePath.endsWith(".n3") || remotePath.endsWith(".nt")) {
//                FileInputStream fis = null;
//                logger.warn("NOT Loaded " + f.getAbsolutePath());
//            }
//        }
//        
//        logger.info(output = "Successfully loaded " + remotePath);
//        return Response.status(200).header(headerAccept, "*").entity(output).build();
    }

    // DQP query for triple store index
    @GET
    @Produces("application/sparql-results+xml")
    public Response getTriplesXMLForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            //System.out.println("Rest: " + query);
            if (query == null){
                throw new Exception("No query");
            }
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println("Rest: " + map);
//            System.out.println("Rest: " + map.size());
            return Response.status(200).header(headerAccept, "*").entity(
                    ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    // Query Corese Server here
    @GET
    @Produces("application/sparql-results+json")
    public Response getTriplesJSONForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println("Rest JSON");
//            System.out.println(map);
//            System.out.println(map.size());
            return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
    
    // Template generate HTML
    
    @POST
    @Produces("text/html")
    @Path("template")
    public Response queryPOSTHTML(
            @FormParam("profile")  String profile,  // query + transform
            @FormParam("uri")  String resource,  // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name")  String name,  // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform")  String transform,  // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri")   List<String> namedGraphUris) {
        return queryGETHTML(profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    
    @GET
    @Produces("text/html")
    @Path("sdk")
    public Response sdk(
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name")  String name,  // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value) // values clause that may complement query           
     {
         Graph g =  new Profile().getGraph("/webapp/data/", "sdk.ttl");
         QueryProcess exec = QueryProcess.create(g);
         return template(exec, g, null, null, query, name, value, null, SDK_SERVICE, null, null);
    }
    
    
     @POST
    @Produces("text/html")
    @Path("tospin")
    public Response toSPINPOST(@FormParam("query") String query){
         return toSPIN(query);
     }
    
    @GET
    @Produces("text/html")
    @Path("tospin")
    public Response toSPIN(@QueryParam("query") String query) {
        SPINProcess sp = SPINProcess.create();       
        Graph g;
        try {
            if (query == null){
                query = "select * where {"
                        + "?x ?p ?y"
                        + "}";
            }
            g = sp.toSpinGraph(query);
            
            Context c = new Context().setTransform(Transformer.TOSPIN).setQuery(query).setService(TOSPIN_SERVICE);            
            HTMLFormat ft = HTMLFormat.create(g, c);                           
            
            return Response.status(200).header(headerAccept, "*").entity(ft.toString()).build();    
 
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(SPARQLRestAPI.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }       
     }
    
    
    @POST
    @Produces("text/html")
    @Path("tosparql")
    public Response toSPARQLPOST(@FormParam("query") String query) 
    {
         return toSPARQL(query);
     }
    
    @GET
    @Produces("text/html")
    @Path("tosparql")
    public Response toSPARQL(@QueryParam("query") String query) {
        Graph g = Graph.create();
        try {
            Load ld = Load.create(g);
            if (query != null){
                ld.loadString(query, Load.TURTLE_FORMAT);
            }
            
            Context c = new Context().setTransform(Transformer.TOSPIN).setQuery(query).setService(TOSPARQL_SERVICE);            
            HTMLFormat ft = HTMLFormat.create(g, c);                           
            
            return Response.status(200).header(headerAccept, "*").entity(ft.toString()).build();              
            
        } catch (LoadException ex) {
            java.util.logging.Logger.getLogger(SPARQLRestAPI.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }       
     }
    
    
    
    
    @GET
    @Produces("text/html")
    @Path("resource")
    public Response ldp(){
        return Response.status(200).header(headerAccept, "*").entity("resource").build();               
     }
    
    @GET
    @Produces("text/html")
    @Path("template")
    public Response queryGETHTML(
            @QueryParam("profile")  String profile,  // query + transform
            @QueryParam("uri")  String resource,  // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name")  String name,  // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform")  String transform,  // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri")   List<String> namedGraphUris) {
        
        return template(getQueryProcess(), graph, 
                profile, resource, query, name, value, transform, TEMPLATE_SERVICE,
                defaultGraphUris, namedGraphUris);       
    }
    
    public Response template(
            QueryProcess exec, Graph g,
            String profile,  // query + transform
            String resource,  // URI of resource focus
            String query, // SPARQL query
            String name,  // SPARQL query name (in webapp/query or path or URL)
            String value, // values clause that may complement query           
            String transform,  // Transformation URI to post process result
            String service,
            List<String> defaultGraphUris,
            List<String> namedGraphUris) {
        try {
            String squery = query;
            
            if (profile != null){
                // prodile declare a construct where query followed by a transformation
                String uprofile = nsm.toNamespace(profile);
                // may load a new profile
                mprofile.define(uprofile);
                if (name == null) {
                    // parameter name overload profile name
                    name = mprofile.getQuery(uprofile);
                }
                if (transform == null){
                    // transform parameter overload profile transform
                    transform = mprofile.getTransform(uprofile);  
                }            
                if (resource != null){
                    // resource given as a binding value to the query
                    value = mprofile.getValues(uprofile, resource);                   
                }
            }
                            
            if (query == null && name != null) {
                 // name of a query
                 squery = getQuery(name);
            }
            
            if (value != null && squery != null){
                // additional values clause
               squery += value;               
            }
            
            if (exec != null && exec.getMode() == QueryProcess.SERVER_MODE){
                // check profile, transform and query
                if (profile != null && ! nsm.toNamespace(profile).startsWith(NSManager.STL)){
                    return Response.status(500).header(headerAccept, "*").entity("Undefined profile: " + profile).build();                        
                }
                if (transform != null && ! nsm.toNamespace(transform).startsWith(NSManager.STL)){
                    return Response.status(500).header(headerAccept, "*").entity("Undefined transform: " + transform).build();                                            
                }
            }
 
            
            if (isDebug){
                System.out.println("query: \n" + squery);
            }         
            // servlet context given to query process and transformation
            Context ctx = createContext(resource, profile, transform, squery, name, service); // /kgram/sparql/template"); 
            Mappings map = null;
            
            if (squery != null && exec != null){
                Dataset ds  = createDataset(defaultGraphUris, namedGraphUris, ctx);
                map = exec.query(squery, ds);

                if (isDetail) {
                    System.out.println(map);
                }
                if (isDebug) {
                    System.out.println("map: " + map.size());
                }
            }
            
            HTMLFormat ft = HTMLFormat.create(g, map, ctx);                           
            
            return Response.status(200).header(headerAccept, "*").entity(ft.toString()).build();               
           } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
    
    
    @GET
    @Path("/debug")
    public Response setDebug(@QueryParam("value") String debug, @QueryParam("detail") String detail) {
        if (debug != null){
             isDebug = debug.equals("true");
        }
        if (detail != null){
            isDetail = detail.equals("true");
        }
        return Response.status(200).header(headerAccept, "*").entity("debug: " + isDebug + " ; " + "detail: " + isDetail).build();
    }
    
    
    Context createContext(String uri, String profile, String trans, String query, String name, String service) {
        Context ctx = new Context();
        if (profile != null) {
            ctx.setProfile(nsm.toNamespace(profile));
        }
        if (trans != null) {
            ctx.setTransform(nsm.toNamespace(trans));
        }  
        if (uri != null){
            ctx.setURI(nsm.toNamespace(uri)); 
        }
        if (query != null) {
            ctx.setQuery(query);
        }
        if (name != null) {
            ctx.setName(name);
        }
        ctx.setService(service);
        return ctx;
    }
          
    String getTemplate(String name) {
        String sep = "";
        if (name.contains("/")) {
            sep = "'";
        }
        String query = "template { st:atw("
                + sep + name + sep
                + ")} where {}";
        return query;
    }
    
    
    
    
    
    @GET
    @Produces("application/sparql-results+json")
    @Path("/draw")
    public Response getJSON(@QueryParam("query") String query) {

        logger.info("Querying: " + query);

        try {
           
            Mappings maps = exec.query(query);
            logger.info(maps.size());

            Graph g =  (Graph) maps.getGraph();
                   
            String mapsProvJson = "{ \"mappings\" : "
                    + JSONFormat.create(maps).toString()
                    + " , "
                    + "\"d3\" : "
                    + JSOND3Format.create(g).toString()
                    + " }";

//            System.out.println(mapsProvJson);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsProvJson).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    
    
    
    
    
    
    
    

    @GET
    @Produces("application/sparql-results+json")
    @Path("/d3")
    public Response getTriplesJSONForGetWithGraph(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {

            Mappings m = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));

            String mapsD3 = "{ \"mappings\" : "
                    + JSONFormat.create(m).toString()
                    + " , "
                    + "\"d3\" : "
                    + JSOND3Format.create((Graph) m.getGraph()).toString()
                    + " }";

//            System.out.println(mapsD3);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsD3).build();

        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+csv")
    public Response getTriplesCSVForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+tsv")
    public Response getTriplesTSVForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/rdf+xml")
    public Response getRDFGraphXMLForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            if (query == null){
                throw new Exception("No query");
            }
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println("Rest RDF XML Get");
//            System.out.println(map);
//            System.out.println(map.size());
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("text/turtle")
    public Response getRDFGraphNTripleForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            Mappings maps = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println("Rest Turtle Get");
//            System.out.println(maps);
//            System.out.println(maps.size());
            String ttl = TripleFormat.create(maps, true).toString();
            logger.debug(query);
            logger.debug(ttl);
            return Response.status(200).header(headerAccept, "*").entity(ttl).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+xml")
    public Response getTriplesXMLForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            //System.out.println("Rest Post RDF XML: "+ query);
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println("Rest: " + map);
//            System.out.println("Rest: " + map.size());
            
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+json")
    public Response getTriplesJSONForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            //System.out.println("Rest Post JSON: "+ query);
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println(map);
//            System.out.println(map.size());
            return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+csv")
    public Response getTriplesCSVForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+tsv")
    public Response getTriplesTSVForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/rdf+xml")
    public Response getRDFGraphXMLForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
           // System.out.println("Rest Post RDF XML");
            Mappings map = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
//            System.out.println(map);
//            System.out.println(map.size());
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("text/nt")
    public Response getRDFGraphNTripleForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(TripleFormat.create(getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /// SPARQL 1.1 Update ///
    //update via URL-encoded POST
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/update")
    public Response updateTriplesEncoded(@FormParam("update") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            logger.info(query);
            if (query != null) {
                getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
            } else {
                logger.warn("Null update query !");
            }

            return Response.status(200).header(headerAccept, "*").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
        }
    }

    /// SPARQL 1.1 Update ///
    //Direct update 
    @POST
    @Consumes("application/sparql-update")
    @Path("/update")
    public Response updateTriplesDirect(String message,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            logger.info(message);
            if (message != null) {
                getQueryProcess().query(message, createDataset(defaultGraphUris, namedGraphUris));
            } else {
                logger.warn("Null update query !");
            }

            return Response.status(200).header(headerAccept, "*").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
        }
    }

    @HEAD
    public Response getTriplesForHead(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            Mappings mp = getQueryProcess().query(query, createDataset(defaultGraphUris, namedGraphUris));
            return Response.status(mp.size() > 0 ? 200 : 400).header(headerAccept, "*").entity("Query has no response").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Creates a Corese/KGRAM Dataset based on a set of default or named graph
     * URIs. For *strong* SPARQL compliance, use dataset.complete() before
     * returning the dataset.
     *
     * @param defaultGraphUris
     * @param namedGraphUris
     * @return a dataset if the parameters are not null or empty.
     */
        private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
            return createDataset(defaultGraphUris, namedGraphUris, null);
        }
        
    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris, Context c) {
        if (c != null ||
                ((defaultGraphUris != null) && (!defaultGraphUris.isEmpty())) || 
                ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
            Dataset ds = Dataset.instance(defaultGraphUris, namedGraphUris);
            ds.setContext(c);
            return ds;
        } else {
            return null;
        }
    }

    /**
     * This function is used to copy the InputStream into a local file.
     */
    private void writeToFile(InputStream uploadedInputStream,
            File uploadedFile) throws IOException {
        OutputStream out = new FileOutputStream(uploadedFile);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
    
    String getQuery(String name) throws IOException {
        if (exec.getMode() == QueryProcess.SERVER_MODE){
            // only predefined queries
            return mprofile.getResource("/webapp/query/" + name);
        }
        // local or external query 
        return mprofile.getResource("/webapp/query/", name);
    }
    
    
}
