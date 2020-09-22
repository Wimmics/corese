package fr.inria.corese.server.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * Agent service enables SPARQL endpoint to answer to http request
 * /agent?action=trace&param=abc
 * Request processed by LDScript event function @message
 * Draft event function in webapp/data/demo/system/event.rq
 * 
 * @author Olivier Corby, Wimmics INRIA I3S 2020
 */
@Path("agent")
public class Agent {
    private static final String headerAccept = "Access-Control-Allow-Origin";
    
    QuerySolverVisitorServer visitor;

    public Agent() {
        visitor = QuerySolverVisitorServer.create(SPARQLRestAPI.createEval());
    }

  
    QuerySolverVisitorServer getVisitor() {
        return visitor;
    }
    
    /**
     * HTTP request processed by LDScript event function @message
     * /agent?action=trace&param=abc
     */
    @GET
    @Produces({"text/plain"})
    public Response message(@javax.ws.rs.core.Context HttpServletRequest request) {
        
        IDatatype dt = getVisitor().message(request);
        String mess = "undefined";
        if (dt != null) {
            mess = dt.getLabel();
        }
        return Response.status(200).header(headerAccept, "*").entity(mess).build();
    }
    
    
    
    
}
