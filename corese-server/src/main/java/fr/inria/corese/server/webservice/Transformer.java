package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import static fr.inria.corese.server.webservice.Utility.toStringList;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * HTML SPARQL endpoint 
 * eval query and/or apply transformation (on query result) or execute a Workflow and return HTML
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("template")
public class Transformer {

    private static Logger logger = LogManager.getLogger(Transformer.class);
    private static final String headerAccept = "Access-Control-Allow-Origin";
    private static final String TEMPLATE_SERVICE = "/template";
    private static final String RESULT = NSManager.STL + "result";
    private static final String LOAD   = NSManager.STL + "load";
    private static NSManager nsm;
    boolean isDebug, isDetail;
    static boolean isTest = false;
    static HashMap<String, String> contentType;
    
    static {
        init();
    }

    static void init() {
        nsm = NSManager.create();
        contentType = new HashMap<>();
        contentType.put(fr.inria.corese.core.transform.Transformer.TURTLE, "text/turtle; charset=utf-8");
        contentType.put(fr.inria.corese.core.transform.Transformer.RDFXML, "application/rdf+xml; charset=utf-8");
        contentType.put(fr.inria.corese.core.transform.Transformer.JSON,   "application/ld+json; charset=utf-8");    
    }
    

    static TripleStore getTripleStore() {
        return SPARQLRestAPI.getTripleStore();
    }
   
    Profile getProfile(){
        return Profile.getProfile();
    }

    // Template generate HTML
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/html")
    public Response queryPOSTHTML(
     @javax.ws.rs.core.Context HttpServletRequest request,
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("mode") String mode, 
            @FormParam("param") String param, 
            @FormParam("arg")       String arg,
            @FormParam("format") String format, 
            @FormParam("access") String access, 
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> from,
            @FormParam("named-graph-uri") List<String> named) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
        par.setArg(arg);
        par.setFormat(format);
        par.setKey(access);
        par.setDataset(from, named);
        par.setRequest(request);
        return template(getTripleStore(), par);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html")
    public Response queryPOSTHTML_MD(
            @javax.ws.rs.core.Context HttpServletRequest request,
            @FormDataParam("profile") String profile, // query + transform
            @FormDataParam("uri") String resource,
            @FormDataParam("mode") String mode,
            @FormDataParam("param") String param,
            @FormDataParam("arg")       String arg,
            @FormDataParam("format") String format,
            @FormDataParam("access") String access,
            @FormDataParam("query") String query, // SPARQL query
            @FormDataParam("name") String name, // SPARQL query name (in webapp/query)
            @FormDataParam("value") String value, // values clause that may complement query           
            @FormDataParam("transform") String transform, // Transformation URI to post process result
            @FormDataParam("default-graph-uri") List<FormDataBodyPart> from,
            @FormDataParam("named-graph-uri") List<FormDataBodyPart> named) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
        par.setArg(arg);
        par.setFormat(format);
        par.setKey(access);
        par.setDataset(toStringList(from), toStringList(named));
        par.setRequest(request);
        return template(getTripleStore(), par);
    }
    
    @GET
    @Produces("text/html")
    public Response queryGETHTML(
     @javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("mode") String mode, 
            @QueryParam("param") String param, 
            @QueryParam("arg") String arg, 
            @QueryParam("format") String format, 
            @QueryParam("access") String access, 
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
        par.setArg(arg);
        par.setFormat(format);
        par.setKey(access);
        par.setDataset(namedGraphUris, namedGraphUris);
        par.setRequest(request);
        return template(getTripleStore(), par);
    }

     public Response template(TripleStore store, Param par) {        
        Context context = null;
        try {

            par = getProfile().complete(par);  
            par.setAjax(SPARQLRestAPI.isAjax);
            
            TransformerEngine engine = new TransformerEngine(store.getGraph(), Profile.getProfile().getProfileGraph(), par);
            engine.setDebug(EmbeddedJettyServer.isDebug());
            engine.setEventManager(Profile.getEventManager());
            context = engine.getContext();
            
            Level level = par.getLevel(); //Access.getQueryAccessLevel(true);
            String prof = context.getProfile();            
            if (prof != null && !Access.acceptNamespace(Feature.LINKED_TRANSFORMATION, level, prof)) {
                return Response.status(500).header(headerAccept, "*").entity("Undefined profile: " + prof).build();
            }
            String trans = context.getTransform();
            if (trans != null && !Access.acceptNamespace(Feature.LINKED_TRANSFORMATION, level, trans)) {
                return Response.status(500).header(headerAccept, "*").entity("Undefined transformation: " + trans).build();
            }
                                              
            Data data = engine.process();
            return process(data, par, context);           
        } catch (Exception ex) {
            logger.error("Error while querying the remote corese server");
            ex.printStackTrace();
            String err = ex.toString();
            String q = null;
            if (context != null && context.getQueryString() != null){
                q = context.getQueryString();
            }
            return Response.status(500).header(headerAccept, "*").entity(error(err, q)).build();
        }
    }
    
    
   
    
    public Response process(Data data, Param par, Context ctx) {
        try {
            ResponseBuilder rb = Response.status(200).header(headerAccept, "*").entity(result(par, data.stringValue()));
            String format = getContentType(data);
            if (format != null && !ctx.getService().contains("srv")) {
                rb.header("Content-type", format);
            }
            return rb.build();
        } 
        catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            String err = ex.toString();
            String q = null;
            if (ctx != null && ctx.getQueryString() != null){
                q = ctx.getQueryString();
            }
            return Response.status(500).header(headerAccept, "*").entity(error(err, q)).build();
        }
    }

    String getContentType(Data data){
        String trans = data.getProcess().getTransformation();
        if (trans != null){
            return contentType.get(trans);          
        }
        return null;
    }
    
 
    
    /**
     * Return transformation result as a HTML textarea
     * hence it is protected wrt img ...
     */
    String protect(Param p, String ft){
        	fr.inria.corese.core.transform.Transformer t = 
            	fr.inria.corese.core.transform.Transformer.create(RESULT);
        Context c = t.getContext();
        c.set(RESULT, ft);
        c.set(LOAD, (p.getLoad() == null) ? "" : p.getLoad());
        c.setTransform((p.getTransform()== null) ? "" : p.getTransform());  
        complete(c, p);
        IDatatype res;
        try {
            res = t.process();
            return res.stringValue();
        } catch (EngineException ex) {
            return ex.getMessage();
        }
    }
    
    String result(Param p, String ft){
        if (p.isProtect()){
            return protect(p, ft);
        }
        return ft;
    }
    
  
    String error(String err, String q){
        String mes = "";
        //mes += "<html><head><link href=\"/style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body>";
        mes +="<h3>Error</h3>";
        mes += "<pre>" + clean(err) + "</pre>";
        if (q != null){
            mes += "<pre>" + clean(q) + "</pre>";
        }
        //mes += "</body></html>";
        return mes;
    }

   String clean(String s){
       return s.replace("<", "&lt;");
   }

     
    Context complete(Context c, Param par){
        if (par.isAjax()){
            c.setProtocol(Context.STL_AJAX);
            c.export(Context.STL_PROTOCOL, c.get(Context.STL_PROTOCOL));
        }
        return c;
    }

   
   
}
