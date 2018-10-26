package fr.inria.corese.server.webservice;

import static fr.inria.corese.server.webservice.EmbeddedJettyServer.HOME_PAGE;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * This class enables to assign an URL to services because as services are
 * accessed using AJAX, they have no specific URL This service is triggered only
 * when using explicitely a /srv/ URL url = /srv/tutorial/rdf?uri=etc Executes
 * target service /tutorial/rdf?uri=etc reads the demo_new.html page replaces
 * the content of the #contentOfSite HTML div by the result of the target
 * service.
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 22 juin 2015
 */
@Path("/srv")
public class SrvWrapper {

	private static final String headerAccept = "Access-Control-Allow-Origin";
	static final String CONTENT_HTML = "<div class=\"content\" id=\"contentOfSite\">";
	private static final String pathRegex = "/{path:template|spin/tospin|spin/tosparql|sdk|tutorial/.*|service/.*|process/.*}";
	private static Logger logger = LogManager.getLogger(SrvWrapper.class);

	@GET
	@Path(pathRegex)
	@Produces("text/html")
	public Response transformGet(
		@Context HttpServletRequest request,
		@PathParam("path") String path,
		@QueryParam("profile") String profile, // query + transform
		@QueryParam("uri") String resource, // URI of resource focus
		@QueryParam("mode") String mode,
		@QueryParam("param") String param,
                @QueryParam("arg")      String arg,
		@QueryParam("format") String format,
		@QueryParam("query") String query, // SPARQL query
		@QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
		@QueryParam("value") String value, // values clause that may complement query           
		@QueryParam("transform") String transform, // Transformation URI to post process result
		@QueryParam("default-graph-uri") List<String> defaultGraphUris,
		@QueryParam("named-graph-uri") List<String> namedGraphUris) {

		Response rs;
		if (path.equalsIgnoreCase("template")) {
			rs = new Transformer().queryGETHTML(request, profile, resource, mode, param, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.equalsIgnoreCase("spin/tospin")) {
			rs = new SPIN().toSPIN(query);
		} else if (path.equalsIgnoreCase("spin/tosparql")) {
			rs = new SPIN().toSPARQL(query);
		} else if (path.equalsIgnoreCase("sdk")) {
			rs = new SDK().sdk(query, name, value);
		} else if (path.startsWith("tutorial")) {
			rs = new Tutorial().get(request, getService(path), profile, resource, mode, param, arg, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.startsWith("process")) {
			rs = new Processor().typecheck(resource, "std", transform, query, getService(path));
		} else {
			rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
		}

		return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path(pathRegex)
	@Produces("text/html")
	public Response transformPost(
		@Context HttpServletRequest request,
		@PathParam("path") String path,
		@FormParam("profile") String profile, // query + transform
		@FormParam("uri") String resource, // URI of resource focus
		@FormParam("mode") String mode,
		@FormParam("param") String param,
                @FormParam("arg")   String arg,
		@FormParam("format") String format,
		@FormParam("query") String query, // SPARQL query
		@FormParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
		@FormParam("value") String value, // values clause that may complement query           
		@FormParam("transform") String transform, // Transformation URI to post process result
		@FormParam("default-graph-uri") List<String> defaultGraphUris,
		@FormParam("named-graph-uri") List<String> namedGraphUris) {

		Response rs;

		if (path.equalsIgnoreCase("template")) {
			rs = new Transformer().queryPOSTHTML(request, profile, resource, mode, param, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.equalsIgnoreCase("spin/tospin")) {
			rs = new SPIN().toSPINPOST(query);
		} else if (path.equalsIgnoreCase("spin/tosparql")) {
			rs = new SPIN().toSPARQLPOST(query);
		} else if (path.equalsIgnoreCase("sdk")) {
			rs = new SDK().sdk(query, name, value);
		} else if (path.startsWith("tutorial")) {
			rs = new Tutorial().post(request, getService(path), profile, resource, mode, param, arg, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.startsWith("process")) {
			rs = new Processor().typecheck(resource, "std", transform, query, getService(path));
		} else {
			rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
		}

		return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path(pathRegex)
	@Produces("text/html")
	public Response transformPostMD(
		@Context HttpServletRequest request,
		@PathParam("path") String path,
		@FormDataParam("profile") String profile, // query + transform
		@FormDataParam("uri") String resource, // URI of resource focus
		@FormDataParam("mode") String mode, // URI of resource focus
		@FormDataParam("param") String param, // URI of resource focus
                @FormDataParam("arg")   String arg,
		@FormDataParam("format") String format, // URI of resource focus
		@FormDataParam("query") String query, // SPARQL query
		@FormDataParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
		@FormDataParam("value") String value, // values clause that may complement query           
		@FormDataParam("transform") String transform, // Transformation URI to post process result
		@FormDataParam("default-graph-uri") List<FormDataBodyPart> defaultGraphUris,
		@FormDataParam("named-graph-uri") List<FormDataBodyPart> namedGraphUris) {

		Response rs;

		if (path.equalsIgnoreCase("template")) {
			rs = new Transformer().queryPOSTHTML_MD(request, profile, resource, mode, param, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.equalsIgnoreCase("spin/tospin")) {
			rs = new SPIN().toSPINPOST_MD(query);
		} else if (path.equalsIgnoreCase("spin/tosparql")) {
			rs = new SPIN().toSPARQLPOST_MD(query);
		} else if (path.equalsIgnoreCase("sdk")) {
			rs = new SDK().sdkPostMD(query, name, value);
		} else if (path.startsWith("tutorial")) {
			rs = new Tutorial().postMD(request, getService(path), profile, resource, mode, param, arg, format, query, name, value, transform, defaultGraphUris, namedGraphUris);
		} else if (path.startsWith("process")) {
			rs = new Processor().typecheckPost_MD(resource, "std", transform, query, getService(path));
		} else {
			rs = Response.status(Response.Status.BAD_REQUEST).header(headerAccept, "*").entity("Can not get right service solver.").build();
		}

		return Response.status(rs.getStatus()).header(headerAccept, "*").entity(wrapper(rs).toString()).build();
	}

	//Put the response text in the #content of home page
	private String wrapper(Response rs) {
		//if not using ajax, donot wrap
		if (!SPARQLRestAPI.isAjax) {
			return rs.getEntity().toString();
		} else {
			String home = EmbeddedJettyServer.resourceURI.getPath() + "/" + HOME_PAGE;//get file path
			try {
				Document doc;
				doc = Jsoup.parse(new File(home), null);
				Element content = doc.getElementById("contentOfSite");
				content.html(rs.getEntity().toString());
				return doc.toString();
			} catch (IOException ex) {
				logger.error(ex);
				throw new RuntimeException(ex);
			}
		}
	}

	//get the string after first "/"
	private String getService(String s) {
		return (s == null || s.isEmpty()) ? "" : s.substring(s.indexOf("/") + 1);
	}
}
