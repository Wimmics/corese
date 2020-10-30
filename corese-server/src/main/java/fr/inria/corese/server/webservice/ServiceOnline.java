package fr.inria.corese.server.webservice;

import static fr.inria.corese.server.webservice.Utility.toStringList;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;

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
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/html")
    public Response post(
     @javax.ws.rs.core.Context HttpServletRequest request,
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
     @javax.ws.rs.core.Context HttpServletRequest request,
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
            @javax.ws.rs.core.Context HttpServletRequest request,
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
     @javax.ws.rs.core.Context HttpServletRequest request,
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

    	// Dataset URI of the service
        String uri = getManager().getURI(serv);        
        Param par = new Param(SERVICE + serv, getProfile(uri, profile, transform), transform, resource, name, query);
        par.setValue(value);
        par.setServer(uri);
        par.setMode(mode);
        par.setParam(param);
        par.setArg(arg);
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
