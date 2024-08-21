package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.JSON_LD;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
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

    private static final QueryProcess exec = SPARQLRestAPI.getTripleStore().getQueryProcess();
    private static final String HEADER_ACCESS_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_ACCEPT_POST = "Accept-Post";
    private static final String HEADER_LINK = "Link";
    private static final String HEADER_SLUG = "Slug";
    private static final String NAMESPACE_LDP = "http://www.w3.org/ns/ldp#";
    private static final String URI_LDP_CONTAINS = NAMESPACE_LDP + "contains";
    private static final String URI_LDP_MEMBER = NAMESPACE_LDP + "member";
    private static final String URI_LDP_MEMBERSHIP_RESOURCE = NAMESPACE_LDP + "membershipResource";
    private static final String URI_LDP_HAS_MEMBERSHIP_RELATION = NAMESPACE_LDP + "hasMemberRelation";
    private static final String URI_LDP_IS_MEMBER_OF_RELATION = NAMESPACE_LDP + "isMemberOfRelation";
    private static final String URI_LDP_INSERTED_CONTENT_RELATION = NAMESPACE_LDP + "insertedContentRelation";
    private static final String URI_LDP_RESOURCE = NAMESPACE_LDP + "Resource";
    private static final String URI_LDP_CONTAINER = NAMESPACE_LDP + "Container";
    private static final String URI_LDP_BASIC_CONTAINER = NAMESPACE_LDP + "BasicContainer";
    private static final String URI_LDP_DIRECT_CONTAINER = NAMESPACE_LDP + "DirectContainer";
    private static final String URI_LDP_INDIRECT_CONTAINER = NAMESPACE_LDP + "IndirectContainer";
    private static final String URI_LDP_RDF_SOURCE = NAMESPACE_LDP + "RDFSource";
    private static final Node rdfTypeProperty = SPARQLRestAPI.getTripleStore().getGraph().createNode(RDF.TYPE);
    private static final Node ldpContainsProperty = SPARQLRestAPI.getTripleStore().getGraph()
            .createNode(URI_LDP_CONTAINS);
    private static final Node ldpMemberProperty = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_MEMBER);
    private static final Node ldpMembershipResourceProperty = SPARQLRestAPI.getTripleStore().getGraph()
            .createNode(URI_LDP_MEMBERSHIP_RESOURCE);
    private static final Node ldpHasMemberRelationProperty = SPARQLRestAPI.getTripleStore().getGraph()
            .createNode(URI_LDP_HAS_MEMBERSHIP_RELATION);
    private static final Node ldpIsMemberOfRelationProperty = SPARQLRestAPI.getTripleStore().getGraph()
            .createNode(URI_LDP_IS_MEMBER_OF_RELATION);

    private static final String SPARQL_DESCRIBE_QUERY = "CONSTRUCT { ?s ?p ?o } { { GRAPH <%1$s> { ?s ?p ?o } } UNION { GRAPH ?g { { ?s ?p ?o . FILTER(?s = <%1$s> ) } UNION { ?s ?p ?o . FILTER(?o = <%1$s> ) } } } UNION { { ?s ?p ?o . FILTER(?s = <%1$s> ) } UNION { ?s ?p ?o . FILTER(?o = <%1$s> ) } } }";
    private static final String SPARQL_TYPE_QUERY = "SELECT DISTINCT ?type WHERE { <%1$s> a ?type }";
    private static final String SPARQL_EXISTS_QUERY = "ASK { { <%1$s> ?p ?o } UNION { ?s ?p <%1$s> } }";
    private static final String SPARQL_CONTAINER_IS_BASIC_QUERY = "ASK { GRAPH <%1$s> { <%1$s> a <"
            + URI_LDP_BASIC_CONTAINER + "> } }";
    private static final String SPARQL_CONTAINER_IS_DIRECT_QUERY = "ASK { GRAPH <%1$s> { <%1$s> a <"
            + URI_LDP_DIRECT_CONTAINER
            + "> } }";
    private static final String SPARQL_CONTAINER_IS_INDIRECT_QUERY = "ASK { GRAPH <%1$s> { <%1$s> a <"
            + URI_LDP_INDIRECT_CONTAINER
            + "> } }";
    private static final String SPARQL_IS_LDP_RESOURCE_QUERY = "ASK { <%1$s> a <" + URI_LDP_RESOURCE + "> }";
    private static final String SPARQL_IS_CONTAINER_QUERY = "ASK { <%1$s> a ?type . VALUES ?type { <"
            + URI_LDP_CONTAINER + "> <" + URI_LDP_BASIC_CONTAINER + "> <" + URI_LDP_DIRECT_CONTAINER + "> <"
            + URI_LDP_INDIRECT_CONTAINER + "> } }";
    private static final String SPARQL_IS_CONTAINED_QUERY = "ASK { { ?s <" + URI_LDP_CONTAINS
            + "> <%1$s> } UNION { ?s <" + URI_LDP_MEMBER + "> <%1$s> } UNION { ?s " + URI_LDP_HAS_MEMBERSHIP_RELATION
            + " ?p . ?s ?p <%1$s> } UNION { ?container " + URI_LDP_HAS_MEMBERSHIP_RELATION + " ?p ; <"
            + URI_LDP_MEMBERSHIP_RESOURCE + "> ?s . ?s ?p <%1$s> . } UNION { ?p <" + URI_LDP_IS_MEMBER_OF_RELATION
            + "> ?s . ?s ?p <%1$s> } UNION { ?p <" + URI_LDP_IS_MEMBER_OF_RELATION + "> ?container ; <"
            + URI_LDP_MEMBERSHIP_RESOURCE + "> ?s . ?s ?p <%1$s> . } }";
    private static final String SPARQL_MEMBERSHIP_TRIPLES_LIST_FOR_RESOURCE_QUERY = "PREFIX ldp: <http://www.w3.org/ns/ldp#> SELECT DISTINCT ?s ?p ?o { { { ?s a ?container . VALUES ?container { ldp:BasicContainer ldp:DirectContainer ldp:IndirectContainer } VALUES ?p { ldp:member ldp:contains } } UNION { ?c a ?container . VALUES ?container { ldp:BasicContainer ldp:DirectContainer ldp:IndirectContainer } ?c ldp:membershipResource ?s .  VALUES ?p { ldp:member ldp:contains } }  UNION { ?s a ?container . VALUES ?container { ldp:BasicContainer ldp:DirectContainer ldp:IndirectContainer } { ?s ldp:hasMemberRelation ?p . } UNION { ?p ldp:isMemberOfRelation ?s . } }  UNION { ?c a ?container . VALUES ?container { ldp:BasicContainer ldp:DirectContainer ldp:IndirectContainer } ?c ldp:membershipResource ?s .  { ?c ldp:hasMemberRelation ?p . } UNION { ?p ldp:isMemberOfRelation ?c . } FILTER(?o = <%1$s>) } UNION { ?c a ?container . { ?c ldp:insertedContentRelation ?insertP . } UNION { ?insertP ldp:isMemberOfRelation ?c } VALUES ?container { ldp:BasicContainer ldp:DirectContainer ldp:IndirectContainer } ?c ldp:membershipResource ?s . { ?c ldp:hasMemberRelation ?p . } UNION { ?p ldp:isMemberOfRelation ?c . } ?res ?insertP ?s . FILTER(?res = <%1$s>) } ?s ?p ?o . } }";

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
            String rawContent) {
        return createResourceResponse(request, rawContent, TURTLE_TEXT);
    }

    @PUT
    @Path("{path:.+}")
    @Consumes(JSON_LD)
    public Response putJsonld(
            @Context HttpServletRequest request,
            @PathParam("path") String res,
            String rawContent) {
        return createResourceResponse(request, rawContent, JSON_LD);
    }

    @POST
    @Path("{path:.+}")
    @Consumes(TURTLE_TEXT)
    public Response postTurtle(
            @Context HttpServletRequest request,
            @PathParam("path") String res,
            String rawContent) {
        return createResourceResponse(request, rawContent, TURTLE_TEXT);
    }

    @POST
    @Path("{path:.+}")
    @Consumes(JSON_LD)
    public Response postJsonld(
            @Context HttpServletRequest request,
            @PathParam("path") String res,
            String rawContent) {
        return createResourceResponse(request, rawContent, JSON_LD);
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
    public Response getResourceOPTIONS(HttpServletRequest request, @PathParam("path") String resource) {
        String resourceURI = request.getRequestURL().toString();
        try {
            return getResourceResponse(resourceURI);
        } catch (EngineException ex) {
            logger.error(ex);
            return Response.serverError()
                    .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                    .entity(ex)
                    .build();
        }
    }

    private Response createResourceResponse(HttpServletRequest request, String rawContent, String format) {
        String resourceURI = request.getRequestURL().toString();
        String linkTypeHeaderString = "";
        int successStatusCode = 201;
        String link = request.getHeader(HEADER_LINK);

        try {
            // Check if the resource already exists
            if (resourceExists(resourceURI)) {
                // Resource already exists, so return code should be 200 SUCCESS instead of 201
                // CREATED
                successStatusCode = 200;
            } else {
                successStatusCode = 201;
            }

            // Check if there is any type for the resource
            boolean knownTypeFound = false;
            if (link != null) {
                // If there are any valid type in the link header, then its a container
                if (isAValidContainerType(link)) {
                    createContainer(request, rawContent, format);
                    knownTypeFound = true;
                }
                linkTypeHeaderString = getResourceLinkHeaderString(resourceURI);
            }
            if (!knownTypeFound) {
                // If there are no types, and there is a slug header, then its a resource
                if (request.getHeader(HEADER_SLUG) != null) {
                    String slug = request.getHeader(HEADER_SLUG);
                    String newResourceURI = resourceURI + "/" + slug;

                    createResource(newResourceURI, rawContent, format);
                    addResourceToContainer(resourceURI, newResourceURI, rawContent, format);
                    successStatusCode = 201;
                } else {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("No container type given and no resource URI given via the Slug header")
                            .build();
                }
            }
        } catch (EngineException e) {
            logger.error(e);
            return Response.serverError()
                    .entity(e)
                    .build();
        }
        return initialResponseBuilder(successStatusCode)
                .header(HEADER_LINK, linkTypeHeaderString)
                .build();
    }

    @DELETE
    @Path("{path:.+}")
    public Response deleteResource(@Context HttpServletRequest request) {
        String resourceURI = request.getRequestURL().toString();
        // Verifying that the target is a resource, not a container
        String existsSparqlQuery = String.format(SPARQL_EXISTS_QUERY, resourceURI);
        try {
            Mappings m = exec.query(existsSparqlQuery);
            if (m.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Resource not found")
                        .build();
            } else {
                // Checking if the resource is a container
                String isContainerSparqlQuery = String.format(SPARQL_IS_CONTAINER_QUERY, resourceURI);
                Mappings isContainerMappings = exec.query(isContainerSparqlQuery);
                if (isContainerMappings.size() > 0) {
                    return Response.status(Response.Status.NOT_IMPLEMENTED)
                            .entity("Delete not implemented for containers")
                            .build();
                } else {
                    // Removing the resource if it is not a container
                    // Removing membership triples
                    String memberRelationShipQueryString = String
                            .format(SPARQL_MEMBERSHIP_TRIPLES_LIST_FOR_RESOURCE_QUERY, resourceURI);
                    Mappings memberRelationShipMappings = exec.query(memberRelationShipQueryString);
                    memberRelationShipMappings.forEach(memberRelationShipMapping -> {
                        Node s = memberRelationShipMapping.getNode("?s");
                        Node p = memberRelationShipMapping.getNode("?p");
                        Node o = memberRelationShipMapping.getNode("?o");
                        SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resourceURI).delete(s, p, o);
                    });
                    SPARQLRestAPI.getTripleStore().getGraph().deleteGraph(resourceURI);

                    return Response.status(Response.Status.NO_CONTENT)
                            .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                            .build();
                }
            }
        } catch (EngineException e) {
            logger.error(e);
            return Response.serverError()
                    .entity(e)
                    .build();
        }
    }

    /**
     * Creates the given resource in the triplestore default graph and load its
     * content
     * 
     * @param resURI
     * @param rawContent
     * @param format
     * @throws EngineException
     */
    private void createResource(String resURI, String rawContent, String format)
            throws EngineException {
        Node resource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_RESOURCE);

        if (SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resURI) == null) {
            SPARQLRestAPI.getTripleStore().getGraph().createNamedGraph(resURI);
        }

        SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resURI).insert(resource, rdfTypeProperty, ldpResource);
        Load load = Load.create(SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resURI));
        try {
            if (format.equals(TURTLE_TEXT)) {
                load.loadString(rawContent, resURI, Load.TURTLE_FORMAT);
            } else if (format.equals(JSON_LD)) {
                load.loadString(rawContent, resURI, Load.JSONLD_FORMAT);
            } else {
                throw new EngineException("Unsupported format: " + format);
            }
        } catch (LoadException e) {
            logger.error(e);
            throw new EngineException(e);
        }
    }

    /**
     * Creates a container in the triplestore default graph and load its content.
     * Will throw exceptions if the content does not contain the required triples
     * for the container type.
     * 
     * @param request
     * @param rawContent
     * @param format
     * @throws EngineException
     */
    private void createContainer(HttpServletRequest request, String rawContent, String format) throws EngineException {
        String resURI = request.getRequestURL().toString();
        String link = request.getHeader(HEADER_LINK);

        if (link.contains(URI_LDP_BASIC_CONTAINER) || link.contains(URI_LDP_DIRECT_CONTAINER)
                || link.contains(URI_LDP_INDIRECT_CONTAINER)) {

            Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
            Node ldpContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_CONTAINER);

            createResource(resURI, rawContent, format);

            if (link.contains("<" + URI_LDP_BASIC_CONTAINER + ">")) {
                createBasicContainer(resURI);
            }
            if (link.contains("<" + URI_LDP_DIRECT_CONTAINER + ">")) {
                createDirectContainer(request, rawContent, format);
            }
            if (link.contains("<" + URI_LDP_INDIRECT_CONTAINER + ">")) {
                createIndirectContainer(request, rawContent, format);
            }
            SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resURI).insert(containerResource, rdfTypeProperty,
                    ldpContainer);
        }
    }

    /**
     * Creates a basic container in the triplestore default graph.
     * No restriction on the content of the container.
     */
    private void createBasicContainer(String resURI)
            throws EngineException {
        logger.info("Creating basic container: " + resURI);
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpBasicContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_BASIC_CONTAINER);
        SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(resURI).insert(containerResource, rdfTypeProperty,
                ldpBasicContainer);
    }

    /**
     * Content must contain a triple with ldp:membershipResource and
     * ldp:hasMemberRelation or ldp:isMemberOfRelation. They must be defined in the
     * rawContent.
     * 
     * @param request
     * @param rawContent RDF content of the container. The empty resource identifier
     *                   ("<>") corresponds to the container URI.
     * @param format     Either turtle or json-ld
     * @throws EngineException
     */
    private void createDirectContainer(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String containerURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(containerURI);
        Node ldpDirectContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_DIRECT_CONTAINER);

        createResource(containerURI, rawContent, format);
        SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI).insert(containerResource, rdfTypeProperty,
                ldpDirectContainer);

        Graph directContainerGraph = SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI);

        ArrayList<Node> membershipResourceList = new ArrayList<>();
        QueryProcess directContainerContentExec = QueryProcess.create(directContainerGraph);

        Mappings membershipResourceMappings = directContainerContentExec.query("SELECT DISTINCT ?membershipResource { <"
                + containerURI + "> <" + URI_LDP_MEMBERSHIP_RESOURCE + "> ?membershipResource }");
        membershipResourceMappings.forEach(membershipResourceMapping -> {
            Node membershipResource = membershipResourceMapping.getNode("?membershipResource");
            membershipResourceList.add(membershipResource);
        });

        if (membershipResourceList.size() > 1) {
            throw new EngineException("Direct Container must have one or more ldp:membershipResource");
        }

        ArrayList<Node> hasMemberRelationList = new ArrayList<>();
        Mappings hasMemberRelationMappings = directContainerContentExec.query("SELECT DISTINCT ?hasMemberRelation { <"
                + containerURI + "> <" + URI_LDP_HAS_MEMBERSHIP_RELATION + "> ?hasMemberRelation }");
        hasMemberRelationMappings.forEach(hasMemberRelationMapping -> {
            Node hasMemberRelation = hasMemberRelationMapping.getNode("?hasMemberRelation");
            hasMemberRelationList.add(hasMemberRelation);
        });

        ArrayList<Node> isMemberOfRelationList = new ArrayList<>();
        Mappings isMemberOfRelationMappings = directContainerContentExec
                .query("SELECT DISTINCT ?isMemberOfRelation { ?isMemberOfRelation <" + URI_LDP_IS_MEMBER_OF_RELATION
                        + "> <" + containerURI + "> }");
        isMemberOfRelationMappings.forEach(isMemberOfRelationMapping -> {
            Node isMemberOfRelation = isMemberOfRelationMapping.getNode("?isMemberOfRelation");
            isMemberOfRelationList.add(isMemberOfRelation);
        });
        if (hasMemberRelationList.size() > 1 && isMemberOfRelationList.size() > 1) {
            throw new EngineException(
                    "Direct Container must have zero or one ldp:hasMemberRelation or ldp:isMemberOfRelation");
        } else {
            if (hasMemberRelationList.size() == 1 && isMemberOfRelationList.size() == 1) {
                Node hasMemberRelation = hasMemberRelationList.get(0);
                Node isMemberOfRelation = isMemberOfRelationList.get(0);
                if (!hasMemberRelation.equals(isMemberOfRelation)) {
                    throw new EngineException(
                            "Direct Container must have exactly one ldp:hasMemberRelation or ldp:isMemberOfRelation");
                }
            }
        }

        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpDirectContainer);
    }

    private void createIndirectContainer(HttpServletRequest request, String rawContent, String format)
            throws EngineException {
        String resURI = request.getRequestURL().toString();
        Node containerResource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resURI);
        Node ldpIndirectContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_INDIRECT_CONTAINER);
        Node ldpDirectContainer = SPARQLRestAPI.getTripleStore().getGraph().createNode(URI_LDP_DIRECT_CONTAINER);
        createDirectContainer(request, rawContent, format);

        SPARQLRestAPI.getTripleStore().getGraph().delete(containerResource, rdfTypeProperty, ldpDirectContainer);
        SPARQLRestAPI.getTripleStore().getGraph().insert(containerResource, rdfTypeProperty, ldpIndirectContainer);

    }

    private Response getHeadResourceResponse(String res) throws EngineException {
        return getResourceResponse(res, TURTLE_TEXT);
    }

    /**
     * Returns the response for a given resource without the content or description
     * of the resource
     * 
     * @param res
     * @return
     * @throws EngineException
     */
    private Response getResourceResponse(String res) throws EngineException {
        return getResourceResponse(res, null);
    }

    /**
     * Returns the response for a given resource with the content or description of
     * the resource in the given format
     * 
     * @param res
     * @param format
     * @return
     * @throws EngineException
     */
    private Response getResourceResponse(String res, String format) throws EngineException {
        String root = this.getBaseURL();
        String subject = root + res;

        // Does the resource exists ?
        String existsSparqlQuery = String.format(SPARQL_EXISTS_QUERY, subject);
        Mappings resourceExists;
        try {
            resourceExists = exec.query(existsSparqlQuery);
            if (resourceExists.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Resource not found")
                        .build();
            }

            // Is it a container ?
            String isContainerSparqlQuery = String.format(SPARQL_IS_CONTAINER_QUERY, subject);
            Mappings isContainerMappings = exec.query(isContainerSparqlQuery);
            if (isContainerMappings.size() > 0) {
                // If its is a resource, is it linked to a container ?
                String isContainedSparqlQuery = String.format(SPARQL_IS_CONTAINED_QUERY, subject);
                Mappings isContainedMappings = exec.query(isContainedSparqlQuery);
                if (isContainedMappings.size() == 0) {
                    return Response.status(Response.Status.GONE)
                            .entity("Resource not member of any container")
                            .build();
                }
            }

        } catch (EngineException e) {
            logger.error(e);
        }

        String content = "";
        if (format != null) {
            content = getResourceContent(res, format);
        }
        String linkTypeHeaderString = getResourceLinkHeaderString(subject);

        return initialResponseBuilder(200)
                .tag("W/" + content.hashCode())
                .header(HEADER_ACCESS_ALLOW_ORIGIN, "*")
                .header("Content-type", format + "; charset=utf-8")
                .header("Link", linkTypeHeaderString)
                .build();
    }

    /**
     * Get the content of a resource as a String in the given format
     * 
     * @param res
     * @param format
     * @return
     */
    private String getResourceContent(String res, String format) {
        String content = "";
        String root = this.getBaseURL();
        String subject = root + res;

        String sparql = String.format(SPARQL_DESCRIBE_QUERY, subject);
        try {
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
        } catch (EngineException ex) {
            logger.error(ex);
        }
        return content;
    }

    /**
     * Create a response builder with the given status code. Fill the headers with
     * the allowed methods and the allowed content types.
     * 
     * @param status
     * @return
     */
    private ResponseBuilder initialResponseBuilder(int status) {
        ResponseBuilder result = Response.status(status)
                .header(HEADER_ACCESS_ALLOW_ORIGIN, "GET, HEAD, OPTIONS");
        if (!SPARQLRestAPI.isProtected) {
            result.header(HEADER_ALLOW, "GET, HEAD, OPTIONS, PUT, POST, DELETE");
            result.header(HEADER_ACCEPT_POST, TURTLE_TEXT + ", " + JSON_LD);
        }
        return result;
    }

    /**
     * Get the Link header string for the given resource URI. Fills the Link header
     * with the LDP types of the resource.
     * 
     * @param resURI
     * @return
     * @throws EngineException
     */
    private String getResourceLinkHeaderString(String resURI) throws EngineException {
        String linkTypeHeaderString = "";
        Mappings resourceTypes = exec.query(String.format(SPARQL_TYPE_QUERY, resURI));

        if (resourceTypes.size() > 0) {
            if (isAValidType(resourceTypes.get(0).getValue("?type").toString())) {
                linkTypeHeaderString = resourceTypes.get(0).getValue("?type") + "; rel=\"type\"";
            }
            for (int i = 1; i < resourceTypes.size(); i++) {
                if (isAValidType(resourceTypes.get(i).getValue("?type").toString())) {
                    linkTypeHeaderString += ", " + resourceTypes.get(i).getValue("?type") + "; rel=\"type\"";
                }
            }
        }
        return linkTypeHeaderString;
    }

    private String getBaseURL() {
        return context.getAbsolutePath().toString();
    }

    /**
     * Returns the types of the given resource URI
     */
    private Set<String> getResourceTypes(String resourceURI) {
        Set<String> types = new HashSet<>();
        String sparql = String.format(SPARQL_TYPE_QUERY, resourceURI);
        try {
            Mappings m = exec.query(sparql);
            for (Mapping map : m) {
                String type = map.getValue("?type").getLabel();
                types.add(type);
            }
        } catch (EngineException ex) {
            logger.error(ex);
        }
        return types;
    }

    /**
     * Check if the given resource exists in the triplestore default graph. Uses a
     * SPARQL ASK query to check if the resource exists.
     * 
     * @param resourceURI
     * @return
     */
    private boolean resourceExists(String resourceURI) {
        String sparql = String.format(SPARQL_EXISTS_QUERY, resourceURI);
        try {
            Mappings m = exec.query(sparql);
            return m.size() > 0;
        } catch (EngineException ex) {
            logger.error(ex);
            return false;
        }
    }

    private boolean containerIsBasic(String containerURI) {
        String sparql = String.format(SPARQL_CONTAINER_IS_BASIC_QUERY, containerURI);
        try {
            Mappings m = exec.query(sparql);
            return m.size() > 0;
        } catch (EngineException ex) {
            logger.error(ex);
            return false;
        }
    }

    private boolean containerIsDirect(String containerURI) {
        String sparql = String.format(SPARQL_CONTAINER_IS_DIRECT_QUERY, containerURI);
        try {
            Mappings m = exec.query(sparql);
            return m.size() > 0;
        } catch (EngineException ex) {
            logger.error(ex);
            return false;
        }
    }

    private boolean containerIsIndirect(String containerURI) {
        String sparql = String.format(SPARQL_CONTAINER_IS_INDIRECT_QUERY, containerURI);
        try {
            Mappings m = exec.query(sparql);
            return m.size() > 0;
        } catch (EngineException ex) {
            logger.error(ex);
            return false;
        }
    }

    private void addResourceToContainer(String containerURI, String resourceURI, String rawContent, String format)
            throws EngineException {
        Graph content = new Graph();
        Load load = Load.create(content);
        try {
            if (format.equals(TURTLE_TEXT)) {
                load.loadString(rawContent, Load.TURTLE_FORMAT);
            } else if (format.equals(JSON_LD)) {
                load.loadString(rawContent, Load.JSONLD_FORMAT);
            } else {
                throw new EngineException("Unsupported format: " + format);
            }
        } catch (LoadException e) {
            throw new EngineException(e);
        }
        addResourceToContainer(containerURI, resourceURI, content);
    }

    /**
     * Add a resource to a container.
     * 
     * @throws EngineException
     */
    private void addResourceToContainer(String containerURI, String resourceURI, Graph content) throws EngineException {
        Node container = SPARQLRestAPI.getTripleStore().getGraph().createNode(containerURI);
        Node resource = SPARQLRestAPI.getTripleStore().getGraph().createNode(resourceURI);
        if (containerIsBasic(containerURI)) {
            SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI).insert(container, ldpMemberProperty,
                    resource);
        } else if (containerIsDirect(containerURI)) {
            addResourceToDirectContainer(container, resource);
        } else if (containerIsIndirect(containerURI) && content != null) {
            addResourceToIndirectContainer(container, resource, content);
        } else {
            throw new EngineException("Container is not a valid LDP container");
        }
    }

    /**
     * Resources added to a DirectContainer must be link to the containers's
     * membership resource by ldp:member and by the container's membership property.
     * 
     * @param container
     * @param resource
     * @throws EngineException
     */
    private void addResourceToDirectContainer(Node container, Node resource) throws EngineException {
        String resURI = resource.getLabel();
        String containerURI = container.getLabel();
        logger.info("Adding resource " + resURI + " to Direct Container " + containerURI);

        ArrayList<Node> membershipResourceList = new ArrayList<>();

        Mappings membershipResourceMappings = exec
                .query("SELECT DISTINCT ?membershipResource { GRAPH <" + containerURI + "> { <" + containerURI + "> <"
                        + URI_LDP_MEMBERSHIP_RESOURCE + "> ?membershipResource } }");
        membershipResourceMappings.forEach(membershipResourceMapping -> {
            Node membershipResource = membershipResourceMapping.getNode("?membershipResource");
            membershipResourceList.add(membershipResource);
        });

        Node membershipResource = container;
        if (membershipResourceList.size() > 1) {
            throw new EngineException("Direct Container must have zero or one ldp:membershipResource");
        } else if (membershipResourceList.size() == 1) {
            membershipResource = membershipResourceList.get(0);
        }
        logger.info(containerURI + " has membershipResource: " + membershipResource.getLabel());
        SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI).insert(membershipResource,
                ldpMemberProperty, resource);

        Node membershipProperty = ldpMemberProperty;

        ArrayList<Node> hasMemberRelationList = new ArrayList<>();
        String selectHasMemberRelationQueryString = "SELECT DISTINCT ?hasMemberRelation { GRAPH <" + containerURI
                + "> { <" + containerURI + "> <"
                + URI_LDP_HAS_MEMBERSHIP_RELATION + "> ?hasMemberRelation } }";
        Mappings hasMemberRelationMappings = exec.query(selectHasMemberRelationQueryString);
        hasMemberRelationMappings.forEach(hasMemberRelationMapping -> {
            Node hasMemberRelation = hasMemberRelationMapping.getNode("?hasMemberRelation");
            hasMemberRelationList.add(hasMemberRelation);
        });

        ArrayList<Node> isMemberOfRelationList = new ArrayList<>();
        String selectIsMemberOfRelationQueryString = "SELECT DISTINCT ?isMemberOfRelation { GRAPH <" + containerURI
                + "> { ?isMemberOfRelation <"
                + URI_LDP_IS_MEMBER_OF_RELATION + "> <" + containerURI + "> } }";
        Mappings isMemberOfRelationMappings = exec.query(selectIsMemberOfRelationQueryString);
        isMemberOfRelationMappings.forEach(isMemberOfRelationMapping -> {
            Node isMemberOfRelation = isMemberOfRelationMapping.getNode("?isMemberOfRelation");
            isMemberOfRelationList.add(isMemberOfRelation);
        });

        if (hasMemberRelationList.size() == 1) {
            membershipProperty = hasMemberRelationList.get(0);
            SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI).insert(membershipResource,
                    membershipProperty, resource);
            logger.info("Adding resource " + resource.getLabel() + " to Direct Container membershipResource "
                    + membershipResource.getLabel() + " with hasMemberRelation: " + membershipProperty.getLabel());
        } else if (isMemberOfRelationList.size() == 1) {
            membershipProperty = isMemberOfRelationList.get(0);
            SPARQLRestAPI.getTripleStore().getGraph().getNamedGraph(containerURI).insert(resource, membershipProperty,
                    membershipResource);
        } else {
            logger.info("No hasMemberRelation or isMemberOfRelation found for Direct Container membershipResource "
                    + membershipResource.getLabel());
        }
    }

    /**
     * Resources added to a IndirectContainer must be link to the containers's
     * membership resource by ldp:member and by the container's membership property.
     * All resources object of the property object of the
     * ldp:insertedContentRelation must be linked to the container's membership
     * resource by the container's membership property. More than one
     * ldp:insertedContentRelation can be defined.
     * 
     * @param container
     * @param resource
     * @throws EngineException
     */
    private void addResourceToIndirectContainer(Node container, Node resource, Graph content) throws EngineException {
        String containerURI = container.getLabel();

        ArrayList<Node> insertedContentRelation = new ArrayList<>();
        String selectInsertedContentRelationQueryString = "SELECT DISTINCT ?insertedContentRelation {  GRAPH <"
                + containerURI
                + "> { <" + containerURI
                + "> <"
                + URI_LDP_INSERTED_CONTENT_RELATION + "> ?insertedContentRelation } }";
        Mappings insertedContentRelationMappings = exec.query(selectInsertedContentRelationQueryString);

        if (insertedContentRelationMappings.size() == 0) {
            throw new EngineException("Indirect Container must have at least one ldp:insertedContentRelation");
        }
        insertedContentRelationMappings.forEach(insertedContentRelationMapping -> {
            Node insertedContentRelationNode = insertedContentRelationMapping.getNode("?insertedContentRelation");
            insertedContentRelation.add(insertedContentRelationNode);
        });

        insertedContentRelation.forEach(insertedContentRelationNode -> {
            ArrayList<Node> membershipResourceList = new ArrayList<>();
            Mappings membershipResourceMappings;
            try {
                String resourceLinkedWithInsertedContentRelation = "SELECT DISTINCT ?membershipResource { GRAPH <"
                        + resource.getLabel()
                        + "> { ?any <"
                        + insertedContentRelationNode.getLabel() + "> ?membershipResource } }";
                membershipResourceMappings = exec.query(resourceLinkedWithInsertedContentRelation);
                membershipResourceMappings.forEach(membershipResourceMapping -> {
                    Node membershipResource = membershipResourceMapping.getNode("?membershipResource");
                    membershipResourceList.add(membershipResource);
                });

                membershipResourceList.forEach(membershipResource -> {
                    try {
                        addResourceToDirectContainer(container, membershipResource);
                    } catch (EngineException e) {
                        logger.error(e);
                    }
                });
            } catch (EngineException e) {
                logger.error(e);
            }
        });

    }

    /**
     * Check if the given type is a valid LDP type
     * 
     * @param typeUri
     * @return
     */
    private boolean isAValidType(String typeUri) {
        boolean isValidHeaderType = Stream
                .of(URI_LDP_RESOURCE)
                .anyMatch(validType -> typeUri.contains(validType))
                || isAValidContainerType(typeUri);
        return isValidHeaderType;
    }

    /**
     * Check if the given type is a valid LDP container type
     * 
     * @param typeUri
     * @return
     */
    private boolean isAValidContainerType(String typeUri) {
        boolean isValidHeaderType = Stream
                .of(URI_LDP_BASIC_CONTAINER, URI_LDP_DIRECT_CONTAINER, URI_LDP_INDIRECT_CONTAINER, URI_LDP_RESOURCE)
                .anyMatch(validType -> typeUri.contains(validType));
        return isValidHeaderType;
    }

    /**
     * Check if the given property is a protected LDP property that must not be
     * modified by the user. To be used with PATCH.
     * 
     * @param propertyUri
     * @return
     */
    private boolean isAProtectedLDPProperty(String propertyUri) {
        return Stream
                .of(URI_LDP_CONTAINS, URI_LDP_MEMBER, URI_LDP_MEMBERSHIP_RESOURCE, URI_LDP_HAS_MEMBERSHIP_RELATION,
                        URI_LDP_IS_MEMBER_OF_RELATION)
                .anyMatch(validProperty -> propertyUri.contains(validProperty));
    }

}
