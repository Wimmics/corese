package fr.inria.corese.server.webservice;

import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mappings;
import static fr.inria.corese.server.webservice.Utility.toStringList;
import fr.inria.corese.sparql.exceptions.EngineException;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTML SPARQL endpoint apply transformation (like Transformer)
 * Each service manage it's own TripleStore
 * TripleStore content is defined in profile.ttl
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("service/{serv}")
public class ServiceOnline {

    static private final Logger logger = LogManager.getLogger(ServiceOnline.class);

    static final String SERVICE = "/service/"; 
    
    static {
        Manager.getManager().init();
    }
    
    QuerySolverVisitorServer visitor;

    public ServiceOnline() {
        //setVisitor(QuerySolverVisitorServer.create(createEval()));
    }
    
    QuerySolverVisitorServer getVisitor() {
        return visitor;
    }
    
    void setVisitor(QuerySolverVisitorServer vis) {
        visitor = vis;
    }

  /**
     * Current graph is SPARQL endpoint graph.
     */
    Eval createEval(TripleStore store) {
        QueryProcess exec = QueryProcess.create(store.getGraph());
        try {
            return exec.getCreateEval();
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
    
    void beforeRequest(HttpServletRequest request, String query) {
        getVisitor().beforeRequest(request, query);
    }
    
    void afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res) {
        getVisitor().afterRequest(request, resp, query, map, res);
    }
   
   TripleStore getTripleStore(String serv){
       TripleStore ts = getManager().getTripleStoreByService(serv);       
       if (ts == null){
           return Transformer.getTripleStore();
       }
       return ts;
   }
   
   Manager getManager(){
       return Manager.getManager();
   }
   
    @POST
    @Produces({"application/sparql-results+xml", "application/xml", "text/plain"})
    @Consumes("application/sparql-query")
    public Response getXMLForPost(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @QueryParam("profile")  String profile, // query + transform
            @QueryParam("uri")      String resource, // URI of resource focus
            @QueryParam("mode")     String mode, 
            @QueryParam("param")    String param, 
            @QueryParam("arg")      String arg,
            @QueryParam("format")   String format, 
            @QueryParam("access")    String access, 
            @QueryParam("query")    String query, // SPARQL query
            @QueryParam("name")     String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value")    String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri")  List<String> namedGraphUris) {
        
            logger.info("getXMLForPost");
            return get(request, serv, profile, resource, mode, param, arg, format, access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    @POST
    @Produces("application/sparql-results+json")
    @Consumes("application/sparql-query")
    public Response getJSONForPost(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @QueryParam("profile")  String profile, // query + transform
            @QueryParam("uri")      String resource, // URI of resource focus
            @QueryParam("mode")     String mode, 
            @QueryParam("param")    String param, 
            @QueryParam("arg")      String arg,
            @QueryParam("format")   String format, 
            @QueryParam("access")    String access, 
            @QueryParam("query")    String query, // SPARQL query
            @QueryParam("name")     String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value")    String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri")  List<String> namedGraphUris) {
        
            logger.info("getJSONForPost");
            return get(request, serv, profile, resource, mode, param, arg, "json", access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/html")
    public Response post(
     @jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @FormParam("profile")   String profile, 
            @FormParam("uri")       String resource, 
            @FormParam("mode")      String mode, 
            @FormParam("param")     String param, 
            @FormParam("arg")       String arg,
            @FormParam("format")    String format, 
            @FormParam("access")    String access, 
            @FormParam("query")     String query, // SPARQL query
            @FormParam("name")      String name, // SPARQL query name (in webapp/query)
            @FormParam("value")     String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri")   List<String> namedGraphUris) {

    	//if (logger.isDebugEnabled())
    		logger.info("POST media: application/x-www-form-urlencoded. serv: " + serv + ", profile: " + profile + ", uri: " + resource + ", mode: " + mode 
    				+ ", param: " + param + ", query: " + query + ", name: " + name + ", value: " + value 
    				+ ", transform: " + transform + ", defaultGraphUris: " + defaultGraphUris + ", namedGraphUris: " + namedGraphUris);
        return get(request, serv, profile, resource, mode, param, arg, format, access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/rdf+xml")
    public Response postSPARQLEndpoint(
     @jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @FormParam("profile")   String profile, 
            @FormParam("uri")       String resource, 
            @FormParam("mode")      String mode, 
            @FormParam("param")     String param, 
            @FormParam("arg")       String arg,
            @FormParam("format")    String format, 
            @FormParam("access")    String access, 
            @FormParam("query")     String query, // SPARQL query
            @FormParam("name")      String name, // SPARQL query name (in webapp/query)
            @FormParam("value")     String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri")   List<String> namedGraphUris) {

    	//if (logger.isDebugEnabled())
    		logger.info("POST media: application/x-www-form-urlencoded. serv: " + serv + ", profile: " + profile + ", uri: " + resource + ", mode: " + mode 
    				+ ", param: " + param + ", query: " + query + ", name: " + name + ", value: " + value 
    				+ ", transform: " + transform + ", defaultGraphUris: " + defaultGraphUris + ", namedGraphUris: " + namedGraphUris);
        return get(request, serv, profile, resource, mode, param, arg, format, access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html")
    public Response postMD(
            @jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @FormDataParam("profile")   String profile, // query + transform
            @FormDataParam("uri")       String resource, // query + transform
            @FormDataParam("mode")      List<String> modeList,
            @FormDataParam("param")     List<String> paramList,
            @FormDataParam("arg")       List<String> argList,
            @FormDataParam("format")    String format,
            @FormDataParam("access")    String access,
            @FormDataParam("query")     String query, // SPARQL query
            @FormDataParam("name")      String name, // SPARQL query name (in webapp/query)
            @FormDataParam("value")     String value, // values clause that may complement query
            @FormDataParam("transform") String transform, // Transformation URI to post process result
            @FormDataParam("default-graph-uri") List<FormDataBodyPart> defaultGraphUris,
            @FormDataParam("named-graph-uri")   List<FormDataBodyPart> namedGraphUris) {

    	//if (logger.isDebugEnabled())
    	logger.info(
        "POST multipart/form-data serv: %s profile: %s uri: %s mode: %s param: %s arg: %s query: %s name: %s value: %s"
                + "transform: %s from: %s named: %s", serv, profile, resource, modeList, 
                paramList, argList, query, name, value, transform, defaultGraphUris, namedGraphUris);
        
        return process(request, serv, profile, resource, 
                null, modeList, null, paramList, null, argList,
                format, access, query, name, value, transform, toStringList(defaultGraphUris), toStringList(namedGraphUris));
    }
    
    
    public Response postMD(
            @jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @FormDataParam("profile")   String profile, // query + transform
            @FormDataParam("uri")       String resource, // query + transform
            @FormDataParam("mode")      String mode,
            @FormDataParam("param")     String param,
            @FormDataParam("arg")       String arg,
            @FormDataParam("format")    String format,
            @FormDataParam("access")    String access,
            @FormDataParam("query")     String query, // SPARQL query
            @FormDataParam("name")      String name, // SPARQL query name (in webapp/query)
            @FormDataParam("value")     String value, // values clause that may complement query
            @FormDataParam("transform") String transform, // Transformation URI to post process result
            @FormDataParam("default-graph-uri") List<FormDataBodyPart> defaultGraphUris,
            @FormDataParam("named-graph-uri")   List<FormDataBodyPart> namedGraphUris) {

    	//if (logger.isDebugEnabled())
    		logger.info("POST media: multipart/form-data. serv: " + serv + ", profile: " + profile + ", uri: " + resource + ", mode: " + mode 
    				+ ", param: " + param + ", arg: " + arg + ", query: " + query + ", name: " + name + ", value: " + value 
    				+ ", transform: " + transform + ", defaultGraphUris: " + defaultGraphUris + ", namedGraphUris: " + namedGraphUris);
        return get(request, serv, profile, resource, mode, param, arg, format, access, query, name, value, transform, toStringList(defaultGraphUris), toStringList(namedGraphUris));
    }
    
    

    @GET
    //@Produces(MediaType.TEXT_HTML)
    public Response get(
     @jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("serv")      String serv,
            @QueryParam("profile")  String profile, // query + transform
            @QueryParam("uri")      String resource, // URI of resource focus
            @QueryParam("mode")     String mode, 
            @QueryParam("param")    String param, 
            @QueryParam("arg")      String arg,
            @QueryParam("format")   String format, 
            @QueryParam("access")    String access, 
            @QueryParam("query")    String query, // SPARQL query
            @QueryParam("name")     String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value")    String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri")  List<String> namedGraphUris) {
    	
    	//if (logger.isDebugEnabled())
    		logger.debug("GET. serv: " + serv + ", profile: " + profile + ", uri: " + resource + ", mode: " + mode 
    				+ ", param: " + param + ", arg: " + arg +", query: " + query + ", name: " + name + ", value: " + value 
    				+ ", transform: " + transform + ", defaultGraphUris: " + defaultGraphUris + ", namedGraphUris: " + namedGraphUris);
         
         return process(request, serv, profile, resource, mode, param, arg, format, access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    Response process(
            HttpServletRequest request,
            String serv,
            String profile, // query + transform
            String resource, // URI of resource focus
            String mode, 
            String param, 
            String arg,
            String format, 
            String access, 
            String query, // SPARQL query
            String name, // SPARQL query name (in webapp/query or path or URL)
            String value, // values clause that may complement query           
            String transform, // Transformation URI to post process result
            List<String> defaultGraphUris,
            List<String> namedGraphUris) 
    {
        return process(request, serv, profile, resource, 
                mode, null, param, null, arg, null,
                format, access, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    
    Response process(
            HttpServletRequest request,
            String serv,
            String profile, // query + transform
            String resource, // URI of resource focus
            String mode,
            List<String> modeList,
            String param, 
            List<String> paramList,
            String arg,
            List<String> argList,
            String format, 
            String access, 
            String query, // SPARQL query
            String name, // SPARQL query name (in webapp/query or path or URL)
            String value, // values clause that may complement query           
            String transform, // Transformation URI to post process result
            List<String> defaultGraphUris,
            List<String> namedGraphUris) 
    {
        if (mode == null && modeList!=null && ! modeList.isEmpty()) {
            mode = modeList.get(0);
        }
        if (param == null && paramList!=null && ! paramList.isEmpty()) {
            param = paramList.get(0);
        } 
        if (arg == null && argList!=null && ! argList.isEmpty()) {
            arg = argList.get(0);
        }        
    	// Dataset URI of the service
        String uri = getManager().getURI(serv);        
        Param par = new Param(SERVICE + serv, getProfile(uri, profile, transform), transform, resource, name, query);
        par.setValue(value);
        par.setServer(uri);
        par.setMode(mode);
        par.setModeList(modeList);
        par.setParam(param);
        par.setParamList(paramList);
        par.setArg(arg);
        par.setArgList(argList);
        // when URL parameter, write: format=application%2Fsparql-results%2Bjson
        par.setFormat(format);
        par.setKey(access);
        par.setDataset(namedGraphUris, namedGraphUris);
        par.setRequest(request);
        TripleStore store = getTripleStore(serv);
        setVisitor(QuerySolverVisitorServer.create(createEval(store)));
        beforeRequest(request, query);
        Response resp = new Transformer().template(store, par);
        afterRequest(request, resp, query, new Mappings(), resp.getEntity().toString());
        return resp;
    }

    /**
     * When no profile neither transform is given
     * the Dataset URI may be a st:Profile with a st:transform
     * in this case, use it as default profile
     */
   String getProfile(String uri, String profile, String transform){
       if (uri == null || profile != null || transform != null){
           return profile;
       }
       Service s = Profile.getProfile().getService(uri);
       if (s != null){
           return uri;
       }
       return profile;
	}

}
