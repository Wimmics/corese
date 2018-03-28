package fr.inria.corese.server.webservice;

import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.LoadException;
import java.io.IOException;
import org.apache.logging.log4j.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
            //g = new Profile().loadServer("sdk.ttl");
            g = Profile.getProfile().loadServer("sdk.ttl");
        } catch (IOException ex) {
            LogManager.getLogger(SDK.class.getName()).log(Level.ERROR, "", ex);
            g = GraphStore.create();
        } catch (LoadException ex) {
            LogManager.getLogger(SDK.class.getName()).log(Level.ERROR, "", ex);
            g = GraphStore.create();
        }
         TripleStore st = new TripleStore(g);
         Param par = new Param("/sdk", null, null, null, Profile.getProfile().getQueryPath("sdk.rq"), null);
         par.setValue(value);
         
         return new Transformer().template(st, par);
    }
    
    @POST
    @Produces("text/html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response sdkPostMD(
            @FormDataParam("query") String query, // SPARQL query
            @FormDataParam("name")  String name,  // SPARQL query name (in webapp/query or path or URL)
            @FormDataParam("value") String value){ // values clause that may complement query          
         return this.sdk(query, name, value);
    }
}
