package fr.inria.corese.server.webservice;


import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTML SPARQL endpoint apply transformation (like Transformer)
 * Each service manage it's own TripleStore
 * TripleStore content is defined in profile.ttl
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("myservice/{serv}")
public class ServiceOnline2 {

    private final Logger logger = LogManager.getLogger(this.getClass());

    static final String SERVICE = "/service/"; 
    
    static {
        Manager.getManager().init();
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
    	
            logger.info("get");

    	// Dataset URI of the service
        String uri = getManager().getURI(serv);        
        Param par = new Param(SERVICE + serv, getProfile(uri, profile, transform), transform, resource, name, query);
        par.setValue(value);
        par.setServer(uri);
        par.setMode(mode);
        par.setParam(param);
        par.setArg(arg);
        // when URL parameter, write: format=application%2Fsparql-results%2Bjson
        par.setFormat(format);
        par.setKey(access);
        par.setDataset(namedGraphUris, namedGraphUris);
        par.setRequest(request);
        return new Transformer().template(getTripleStore(serv), par);
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
