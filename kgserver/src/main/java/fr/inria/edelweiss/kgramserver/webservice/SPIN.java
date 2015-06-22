package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.HTMLFormat;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("spin")
public class SPIN {

    private static final String headerAccept = "Access-Control-Allow-Origin";
    public static final String TOSPIN_SERVICE    = "/spin/tospin";
    public static final String TOSPARQL_SERVICE  = "/spin/tosparql";
        
    @POST
    @Produces("text/html")
    @Path("tospin")
    public Response toSPINPOST(@FormParam("query") String query) {
        return toSPIN(query);
    }

    @GET
    @Produces("text/html")
    @Path("tospin")
    public Response toSPIN(@QueryParam("query") String query) {
        SPINProcess sp = SPINProcess.create();
        Graph g;
        try {
            if (query == null) {
                query = "select * where {"
                        + "?x ?p ?y"
                        + "}";
            }
            g = sp.toSpinGraph(query);

            Context c = new Context().setTransform(Transformer.TOSPIN).setQuery(query).setService(TOSPIN_SERVICE);
            complete(c);
            HTMLFormat ft = HTMLFormat.create(g, c);

            return Response.status(200).header(headerAccept, "*").entity(ft.toString()).build();

        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(SPARQLRestAPI.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
    
     Context complete(Context c){
        if (SPARQLRestAPI.isAjax){
            c.setProtocol(Context.STL_AJAX);
        }
        return c;
    }

    @POST
    @Produces("text/html")
    @Path("tosparql")
    public Response toSPARQLPOST(@FormParam("query") String query) {
        return toSPARQL(query);
    }

    @GET
    @Produces("text/html")
    @Path("tosparql")
    public Response toSPARQL(@QueryParam("query") String query) {
        Graph g = Graph.create();
        try {
            Load ld = Load.create(g);
            if (query != null) {
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
}
