package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.TripleFormat;

import java.io.IOException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A simple version of Linked Data Platform 1.0 (LDP) Server, according to W3C
 * recommendation (WD 11 March 2014) http://www.w3.org/TR/ldp/ it supports to
 * add triples as resources and return rdf resources by request-uri in
 * text/turtle format
 * <p>
 * HTTP methods @GET @HEAD @OPTIONS @POST have been implemented.
 * <p>
 * default base url: http://localhost:8080/kgram/ldp
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 avr. 2014
 */
@Path("ldp")
public class LdpRequestAPI {
    private final Logger logger = LogManager.getLogger( "==LDP== " + LdpRequestAPI.class );
    public static final String LDP_QUERY =
            "construct { <%1$s> ?p ?o  ?x ?q <%1$s> } "
                    + "where {{<%1$s> ?p ?o} union {?x ?q <%1$s>}}";

    private static final TripleStore store = new TripleStore();
    private static final QueryProcess exec = QueryProcess.create( store.graph );
    private final String headerAccept = "Access-Control-Allow-Origin";
    private final static String SERVER = "http://localhost:8080/ldp/";
    private final static String LDP_NAME = NSManager.STL + "ldp";

    //^(?!upload|create)(.*)$
    @GET
    @Path("{path:.+}")
    @Produces("text/turtle")
    public Response getResourceGET(@PathParam("path") String res)
    {
        try
        {
            return getResourceResponse( res, false );
        } catch (EngineException ex)
        {
            logger.error( ex );
            return Response.serverError().header( headerAccept, "*" ).entity( ex ).build();
        }
    }

    @GET
    @Produces("application/sparql-results+json")
    public Response getResourceList(@QueryParam("request") String cmd)
    {
        try
        {
            String query = "";
            if ( "list".equalsIgnoreCase( cmd ) )
            {
                query = "SELECT  ?resource (COUNT(?resource ) AS ?count ) { ?resource ?p ?o } GROUP BY ?resource ORDER BY DESC( ?count)";

            } else if ( "count".equalsIgnoreCase( cmd ) )
            {
                query = "SELECT (COUNT(DISTINCT ?s ) AS ?ResourceCount) { ?s ?p ?o }";

            } else
            {//null|empty| other command
                return Response.status( Response.Status.BAD_REQUEST ).entity( "Please specify correctly request type:list | count | resource name (ex. /Monaco)" ).build();
            }
            logger.info( query );
            String json = JSONFormat.create( exec.query( query ) ).toString();
            return Response.status( 200 ).header( headerAccept, "*" ).entity( json ).build();
        } catch (EngineException ex)
        {
            logger.error( ex );
            return Response.serverError().header( headerAccept, "*" ).entity( ex ).build();
        }
    }

    @HEAD
    @Path("{path:.+}")
    public Response getResourceHEAD(@PathParam("path") String resource)
    {
        try
        {
            return getResourceResponse( resource, false );
        } catch (EngineException ex)
        {
            logger.error( ex );
            return Response.serverError().header( headerAccept, "*" ).entity( ex ).build();
        }
    }

    @OPTIONS
    @Path("{path:.+}")
    public Response getResourceOPTIONS(@PathParam("path") String resource)
    {
        return Response.ok().header( headerAccept, "*" ).header( "Allow", "GET, HEAD, OPTIONS" ).build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesPOST(@FormParam("update") String query)
    {
        try
        {
            logger.info( query );
            if ( query != null )
            {
                exec.query( query );
            } else
            {
                logger.warn( "Null update query !" );
            }

            return Response.ok().header( headerAccept, "*" ).build();
        } catch (EngineException ex)
        {
            logger.error( ex );
            return Response.serverError().header( headerAccept, "*" ).entity( ex ).build();
        }
    }

    @OPTIONS
    @Consumes("application/x-www-form-urlencoded")
    @Path("/upload")
    public Response uploadTriplesOPTIONS()
    {
        return Response.ok().header( headerAccept, "*" ).header( "Allow", "POST, OPTIONS" ).build();
    }

    Context getContext()
    {
        try
        {
            Param par = new Param( Manager.DEFAULT );
            par.setServer( Manager.DEFAULT );
            Profile.getProfile().complete( par );
            Context ctx = par.createContext();
            return ctx;
        } catch (IOException ex)
        {
            logger.error(ex.getMessage());
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
        }
        return new Context();
    }

    private Response getResourceResponse(String res, boolean head) throws EngineException
    {
        String content = "";
        if ( !head )
        {
            Context ctx = getContext();
            logger.info( ctx );
            logger.info( "URI: " + res );
            IDatatype dt = ctx.get( LDP_NAME );
            String root = (dt == null) ? SERVER : dt.getLabel();
            String subject = root + res;
            logger.info( "URI: " + subject );
            String sparql = String.format( LDP_QUERY, subject );
            QueryProcess ex = SPARQLRestAPI.getTripleStore().getQueryProcess();
            Mappings m = ex.query( sparql );
            logger.info( sparql );

            if ( m.size() > 0 )
            {
                content = TripleFormat.create( m ).toString();
            }
        }

        ResponseBuilder rb = Response.ok( content );
        rb.tag( "" + res.hashCode() );//eTag
        rb.header( "Content-type", "text/turtle; charset=utf-8" );
        //TODO, check the resource type:LDPR, LDP-NR, LDPC, etc..
        rb.header( "Link", "<http://www.w3.org/ns/ldp#RDFResource>; rel = \"type\"" );
        rb.header( "Preference-Applied", "return=presentation" );
        rb.header( headerAccept, "*" );

        return rb.build();
    }

    // Inner class for ecapsulating a graph as a triple store
    // this part needs to be extended in future
    private static class TripleStore {

        private Graph graph = Graph.create();

        TripleStore()
        {

        }
    }
}
