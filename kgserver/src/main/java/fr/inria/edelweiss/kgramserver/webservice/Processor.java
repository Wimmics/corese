package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("process/{serv}")
public class Processor {

    private static final String headerAccept = "Access-Control-Allow-Origin";
    public static final String SERVICE  = "/typecheck";
    public static final String QUERY  = Profile.QUERY;
    
    static HashMap<String, String> map;
    
    static {
        map = new HashMap();
        map.put("owl",      QUERY + "owl.rq");
        map.put("owlrl",    QUERY + "owlrl.rq");
        map.put("sparql",     QUERY + "spintc.rq");
        
        map.put("turtle",   QUERY + "turtle.rq");
//        map.put("sparql",   QUERY + "sparql.rq");
    }
        
    @POST
    @Produces("text/html")
    public Response typecheckPost(@FormParam("uri") String uri,
                            @PathParam("serv") String serv) {
        return typecheck(uri, serv);
    }

    @GET
    @Produces("text/html")
    public Response typecheck(@QueryParam("uri") String uri,
                            @PathParam("serv") String serv) {
        GraphStore g;
        try {            
            if (serv.equals("sparql") || serv.equals("spin")){
                g = loadSPARQL(uri);
            }
            else {
               g = load(uri); 
            }                       
        }  catch (LoadException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
        } catch (EngineException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
       }
        
         String temp = map.get(serv);
         if (temp == null){
             temp = QUERY+"turtle.rq";
         }
         Param par = new Param("/process", null, null, null, temp, null);
         
         return new Transformer().template(new TripleStore(g), par);
    }
    
     String error(String err, String q){
        String mes = "<html><head><link href=\"/style.css\" rel=\"stylesheet\" type=\"text/css\" /></head>"
                + "<body><h3>Error</h3>";
        mes += "<pre>" + clean(err) + "</pre>";
        if (q != null){
            mes += "<pre>" + clean(q) + "</pre>";
        }
        mes += "</body></html>";
        return mes;
    }

   String clean(String s){
       return s.replace("<", "&lt;");
   }
    

    GraphStore load(String uri) throws LoadException {
        GraphStore g = GraphStore.create();
        Load ld = Load.create(g);
        ld.setLimit(100000);       
        ld.loadWE(uri);
        return g;
    }
    
    GraphStore loadSPARQL(String uri) throws EngineException{
        GraphStore g = GraphStore.create();
        QueryLoad ql = QueryLoad.create();
        String str = ql.read(uri);
        if (str != null){
            SPINProcess sp = SPINProcess.create();
            sp.toSpinGraph(str, g);        
        }              
        return g;
    }


}