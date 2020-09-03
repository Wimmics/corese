package fr.inria.corese.server.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author corby
 */
@Path("agent")
public class Agent {
    private static final String headerAccept = "Access-Control-Allow-Origin";
    
    QuerySolverVisitorServer visitor;

    public Agent() {
        visitor = new QuerySolverVisitorServer();
    }

  
    QuerySolverVisitorServer getVisitor() {
        return visitor;
    }
    
    
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
