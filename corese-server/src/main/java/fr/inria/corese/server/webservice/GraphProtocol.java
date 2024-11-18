package fr.inria.corese.server.webservice;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.sparql.triple.parser.NSManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * SPARQL 1.1 Graph Store HTTP Protocol
 * 
 * @author Olivier Corby
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 */
@Path("rdf-graph-store")
public class GraphProtocol {
    static private final Logger logger = LogManager.getLogger(GraphProtocol.class);

    static final String NAMED_GRAPH_QUERY = "CONSTRUCT {?s ?p ?o} WHERE { GRAPH <%s> {?s ?p ?o}}";
    static final String DEFAULT_GRAPH_QUERY = "CONSTRUCT {?s ?p ?o}";

    static final String NAMED_GRAPH_INSERT = "INSERT DATA { GRAPH <%s> {%s}}";
    static final String DEFAULT_GRAPH_INSERT = "INSERT DATA {%s}";

    Response get(HttpServletRequest request, String name, String graph, String pattern, String access, int format) {
        String query = pattern;
        if (name != null) {
            query = String.format(pattern, NSManager.nsm().toNamespace(graph));
        }
        return new SPARQLRestAPI().getResultFormat(request, name, null, null, null, null, query, access, null, null,
                format);
    }

    Response post(HttpServletRequest request, String name, String graph, String pattern, String access, int format) {
        String query;
        if (graph == null) {
            query = String.format(DEFAULT_GRAPH_INSERT, pattern);
        } else {
            query = String.format(NAMED_GRAPH_INSERT, NSManager.nsm().toNamespace(graph), pattern);
        }
        return new SPARQLRestAPI().getResultFormat(request, name, null, null, null, null, query, access, null, null,
                format);
    }

    String getQuery(String name) {
        if (name == null) {
            return DEFAULT_GRAPH_QUERY;
        }
        return NAMED_GRAPH_QUERY;
    }

    /**
     * 
     * @param request
     * @param name:   TripleStore name or null
     * @param graph:  named graph URI
     * @param access: access key
     * @param mode
     * @return
     */
    @GET
    @Produces({ ResultFormat.TURTLE_TEXT, ResultFormat.TURTLE, ResultFormat.NT_TEXT })
    public Response getTurtle(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @QueryParam("graph") String graph,
            @QueryParam("access") String access,
            @QueryParam("mode") List<String> mode) {

        logger.info("getTurtle");
        return get(request, name, graph, getQuery(graph), access, ResultFormat.TURTLE_FORMAT);
    }

    @GET
    @Produces({ ResultFormat.RDF_XML })
    public Response getXML(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @QueryParam("graph") String graph,
            @QueryParam("access") String access,
            @QueryParam("mode") List<String> mode) {

        logger.info("getXML");
        return get(request, name, graph, getQuery(graph), access, ResultFormat.RDF_XML_FORMAT);
    }

    @GET
    @Produces({ ResultFormat.JSON_LD })
    public Response getJSON(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @QueryParam("graph") String graph,
            @QueryParam("access") String access,
            @QueryParam("mode") List<String> mode) {

        logger.info("getJSON");
        return get(request, name, graph, getQuery(graph), access, ResultFormat.JSONLD_FORMAT);
    }

    @PUT
    @Produces({ ResultFormat.SPARQL_RESULTS_XML, ResultFormat.XML})
    public Response put(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @QueryParam("graph") String graph,
            @QueryParam("access") String access,
            @QueryParam("mode") List<String> mode) {

        logger.info("put");
        return post(request, name, graph, getQuery(graph), access, ResultFormat.XML_FORMAT);
    }

    @POST
    public Response post(@jakarta.ws.rs.core.Context HttpServletRequest request,
            @PathParam("name") String name,
            @QueryParam("graph") String graph,
            @QueryParam("access") String access,
            @QueryParam("mode") List<String> mode) {

        logger.info("post");
        return post(request, name, graph, getQuery(graph), access, ResultFormat.XML_FORMAT);
    }

}
