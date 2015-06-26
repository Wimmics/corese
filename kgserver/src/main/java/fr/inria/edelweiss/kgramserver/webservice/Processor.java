package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.multipart.FormDataParam;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
    static URI base;
    
    static {
        try {
            base = new URI(Profile.SERVER);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        }
        map = new HashMap();
        map.put("owl",      QUERY + "owl.rq");
        map.put("owlrl",    QUERY + "owlrl.rq");
        map.put("sparqltc", QUERY + "spintc.rq");        
        map.put("sparql",   QUERY + "sparql.rq");        
        map.put("mix",      QUERY + "mix.rq");
    }
        
    @POST
    @Produces("text/html")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response typecheckPost(
            @FormParam("uri") String uri,
            @FormParam("transform") String trans,
            @PathParam("serv") String serv) {
        return typecheck(uri, trans, serv);
    }

    @POST
    @Produces("text/html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response typecheckPost_MD(
            @FormDataParam("uri") String uri,
            @FormDataParam("transform") String trans,
            @PathParam("serv") String serv) {
        return typecheck(uri, trans, serv);
    }
    
    @GET
    @Produces("text/html")
    public Response typecheck(
            @QueryParam("uri") String uri,
            @QueryParam("transform") String trans,
            @PathParam("serv") String serv) {
        GraphStore g;
        
        try {            
            URI url = new URI(uri);
            if (! url.isAbsolute()){
                url = base.resolve(uri);
            }
            if (serv.startsWith("sparql") || serv.equals("spin") || serv.equals("mix")){
                g = loadSPARQL(url.toString());
            }
            else {
               g = load(url.toString()); 
            }                       
        }  catch (LoadException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
        } catch (EngineException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
       } catch (URISyntaxException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
        }
        
         String temp = null;
         if (trans == null){
            temp = map.get(serv);
            if (temp == null){
                temp = QUERY+"turtle.rq";
            }
         }
        
         Param par = new Param("/process", null, trans, null, temp, null);
         par.setLoad(uri);
         if (trans != null){
             par.setProtect(true);
         }
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