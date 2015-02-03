package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("sdk")
public class SDK {
    
    
    @GET
    @Produces("text/html")
    public Response sdk(
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name")  String name,  // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value) // values clause that may complement query           
     {
         //GraphStore g =  new Profile().getGraph("sdk.ttl");
         GraphStore g;
        try {
            g = new Profile().loadServer("sdk.ttl");
        } catch (IOException ex) {
            Logger.getLogger(SDK.class.getName()).log(Level.SEVERE, null, ex);
            g = GraphStore.create();
        } catch (LoadException ex) {
            Logger.getLogger(SDK.class.getName()).log(Level.SEVERE, null, ex);
            g = GraphStore.create();
        }
         TripleStore st = new TripleStore(g);
         Param par = new Param("/sdk", null, null, null, Profile.QUERY + "sdk.rq", null);
         par.setValue(value);
         
         return new Transformer().template(st, par);
    }
    

}
