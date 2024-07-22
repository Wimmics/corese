package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.TripleFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriInfo;

import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static fr.inria.corese.core.print.ResultFormat.JSON_LD;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A simple version of Linked Data Platform 1.0 (LDP) Server, according to W3C
 * recommendation (WD 11 March 2014) http://www.w3.org/TR/ldp/ it supports to
 * add triples as resources and return rdf resources by request-uri in
 * text/turtle format
 * 
 * Missing PUT, container management, changeset handling, etc.
 * 
 * default base url: http://localhost:8080/ldp
 *
 * @author Fuqi Song, Wimmics Inria I3S, 2014
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 */
@Path("ldp")
public class LdpRequestAPI {
    private final Logger logger = LogManager.getLogger(LdpRequestAPI.class);
    public static final String LDP_QUERY = "DESCRIBE <%1$s>";

    private static final QueryProcess exec = SPARQLRestAPI.getTripleStore().getQueryProcess();
    private static final String HEADER_ACCESS_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_LINK = "Link";
    private static final String NAMESPACE_LDP = "http://www.w3.org/ns/ldp#";
    private static final String URI_LDP_RESOURCE = NAMESPACE_LDP + "Resource";
    private static final String URI_LDP_CONTAINER = NAMESPACE_LDP + "Container";
    private static final String URI_LDP_BASIC_CONTAINER = NAMESPACE_LDP + "BasicContainer";
    private static final String URI_LDP_DIRECT_CONTAINER = NAMESPACE_LDP + "DirectContainer";
    private static final String URI_LDP_INDIRECT_CONTAINER = NAMESPACE_LDP + "IndirectContainer";
    private static Node rdfTypeProperty = SPARQLRestAPI.getTripleStore().getGraph().createNode(RDF.TYPE);

    @Context
    private UriInfo context;

    @GET
    @Path("{path:.+}")
    @Produces(TURTLE_TEXT)
    public Response getResourceGETTurtle(@PathParam("path") String res) {
        try {
            return getResourceResponse(res, TURTLE_TEXT);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError()
                    .entity(ex)
                    .build();
        }
    }

    @GET
    @Path("{path:.+}")
    @Produces(JSON_LD)
    public Response getResourceGETJsonld(@PathParam("path") String res) {
        try {
            return getResourceResponse(res, JSON_LD);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError()
                    .entity(ex)
                    .build();
        }
    }

    @PUT
    @Path("{path:.+}")
    @Consumes(TURTLE_TEXT)
    public Response putTurtle(
            @Context HttpServletRequest request,
            @PathParam("path") String res,
            @HeaderParam("Link") String link,
            String rawContent) {
        String containerURI = request.getRequestURL().toString();
        String linkTypeHeaderString = "";
        try {
            if (link.contains("<" + URI_LDP_CONTAINER + ">")) {
                createContainer(request, rawContent, TURTLE_TEXT);
            } else if (link.contains("<" + URI_LDP_BASIC_CONTAINER + ">")) {
                createBasicContainer(request, rawContent, TURTLE_TEXT);
            } else if (link.contains("<" + URI_LDP_DIRECT_CONTAINER + ">")) {
                createDirectContainer(request, rawContent, TURTLE_TEXT);
            } else if (link.contains("<" + URI_LDP_INDIRECT_CONTAINER + ">")) {
                createIndirectContainer(request, rawContent, TURTLE_TEXT);
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unsupported type: " + link)
                        .build();
            }
            linkTypeHeaderString = getResourceLinkHeaderString(containerURI);
        } catch (EngineException e) {
            logger.error(e);
            return Response.serverError()
                    .entity(e)
                    .build();
        }
        logger.info("PUT: " + res + " Response Link: " + linkTypeHeaderString);
        return initialResponseBuilder(201)
                .header(HEADER_LINK, linkTypeHeaderString)
                .build();
    }

    @PUT
    @Path("{path:.+}")
    @Consumes(JSON_LD)
    public Response putJsonld(
            @Context HttpServletRequest request,
            @PathParam("path") String res,
            @HeaderParam("Link") String link,
            String rawContent) {
        String containerURI = request.getRequestURL().toString();
        String linkTypeHeaderString = "";
        try {
            if (link.contains(URI_LDP_CONTAINER)) {
                createContainer(request, rawContent, JSON_LD);
            } else if (link.contains(URI_LDP_BASIC_CONTAINER)) {
                createBasicContainer(request, rawContent, JSON_LD);
            } else if (link.contains(URI_LDP_DIRECT_CONTAINER)) {
                createDirectContainer(request, rawContent, JSON_LD);
            } else if (link.contains(URI_LDP_INDIRECT_CONTAINER)) {
                createIndirectContainer(request, rawContent, JSON_LD);
            } else {
                logger.error("Unsupported type: " + link);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unsupported type: " + link)
                        .build();
            }
            linkTypeHeaderString = getResourceLinkHeaderString(containerURI);

        } catch (EngineException e) {
            logger.error(e);
            return Response.serverError()
                    .entity(e)
                    .build();
        }
        logger.info("PUT: " + res + " Response Link: " + linkTypeHeaderString);
        return initialResponseBuilder(201)
                .header(HEADER_LINK, linkTypeHeaderString)
                .build();
    }

    // TODO: Implement DELETE
    // TODO Refactor la creation pour pouvoir envoyer status différent si la ressource existe déja

    private void createResource(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node resource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_RESOURCE);

        SPARQLRestAPI.getTripleStore().getGraph().insert(resource, rdfTypeProperty, ldpResource);

        Load load = Load.create(SPARQLRestAPI.getTripleStore().getGraph());
        try {
            if (format.equals(TURTLE_TEXT)) {
                load.loadString(rawContent, resURI, Load.TURTLE_FORMAT);
            } else if (format.equals(JSON_LD)) {
                load.loadString(rawContent, resURI, Load.JSONLD_FORMAT);
            } else {
                throw new EngineException("Unsupported format: " + format);
            }
        } catch (LoadException e) {
            throw new EngineException(e);
        }
    }

    private void createContainer(HttpServletRequest request, String rawContent, String format) throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_CONTAINER);

        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpContainer);
        createResource(request, rawContent, format);
    }

    private void createBasicContainer(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpBasicContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_BASIC_CONTAINER);
        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpBasicContainer);
        createContainer(request, rawContent, format);
    }

    private void createDirectContainer(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpDirectContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_DIRECT_CONTAINER);
        String initDataBasicContainerTurtleString = "<" + resURI + "> a <" + URI_LDP_DIRECT_CONTAINER + "> .";
        logger.info(initDataBasicContainerTurtleString);
        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpDirectContainer);
        createContainer(request, rawContent, format);
    }

    private void createIndirectContainer(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpIndirectContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_INDIRECT_CONTAINER);

        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpIndirectContainer);
        createContainer(request, rawContent, format);
    }

    @HEAD
    @Path("{path:.+}")
    public Response getResourceHEAD(@PathParam("path") String resource) {
        try {
            return getHeadResourceResponse(resource);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError()
                    .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                    .entity(ex)
                    .build();
        }
    }

    @OPTIONS
    @Path("{path:.+}")
    public Response getResourceOPTIONS(@PathParam("path") String resource) {
        return Response.ok()
                .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                .header(HEADER_ALLOW, "GET, HEAD, OPTIONS")
                .build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesPOST(@FormParam("update") String query) {
        try {
            if (query != null) {
                exec.query(query);
            } else {
                logger.warn("Null update query !");
            }

            return Response.ok().header(HEADER_ACCESS_ALLOW_ORIGIN, "*").build();
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError()
                    .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                    .entity(ex)
                    .build();
        }
    }

    @OPTIONS
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesOPTIONS() {
        return initialResponseBuilder(200)
                .build();
    }

    private Response getHeadResourceResponse(String res) throws EngineException {
        return getResourceResponse(res, TURTLE_TEXT);
    }

    private Response getResourceResponse(String res, String format) throws EngineException {
        String content = "";
        String root = this.getBaseURL();
        String subject = root + res;

        String sparql = String.format(LDP_QUERY, subject);
        Mappings m = exec.query(sparql);

        if (m.size() > 0) {
            if (format.equals(JSON_LD)) {
                content = JSONFormat.create(m).toString();
            } else if (format.equals(TURTLE_TEXT)) {
                content = TripleFormat.create(m).toString();
            } else {
                throw new EngineException("Unsupported format: " + format);
            }
        }
        String linkTypeHeaderString = getResourceLinkHeaderString(subject);

        return initialResponseBuilder(200)
                .tag("W/" + content.hashCode())
                .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                .header("Content-type", format + "; charset=utf-8")
                .header("Link", linkTypeHeaderString)
                .build();
    }

    private ResponseBuilder initialResponseBuilder(int status) {
        ResponseBuilder result = Response.status(status)
                .header(HEADER_ACCESS_ALLOW_ORIGIN, "GET, HEAD, OPTIONS");
        if (!SPARQLRestAPI.isProtected) {
            result.header("Allow", "GET, HEAD, OPTIONS, PUT, POST, DELETE");
        }
        return result;
    }

    private String getResourceLinkHeaderString(String resURI) throws EngineException {
        String linkTypeHeaderString = "";
        Mappings resourceTypes = exec.query("SELECT DISTINCT ?type WHERE { <" + resURI + "> a ?type }");

        if (resourceTypes.size() > 0) {
            linkTypeHeaderString = resourceTypes.get(0).getValue("?type") + "; rel=\"type\"";
            for (int i = 1; i < resourceTypes.size(); i++) {
                linkTypeHeaderString += ", " + resourceTypes.get(i).getValue("?type") + "; rel=\"type\"";
            }
        }
        logger.info("Link: " + linkTypeHeaderString);
        return linkTypeHeaderString;
    }

    private String getBaseURL() {
        return context.getAbsolutePath().toString();
    }

}
