package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.multipart.FormDataParam;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.corese.kgtool.workflow.ShapeWorkflow;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

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
import org.apache.logging.log4j.LogManager;

/**
 * Deprecated
 * Use Workflow in profile.ttl instead
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("process/{serv}")
@Deprecated
public class Processor {

    private static final String headerAccept = "Access-Control-Allow-Origin";
    public static final String SERVICE  = "/typecheck";
    public static final String GRAPHIC  = "graphic";
    
    static HashMap<String, String> map;
    
    static {        
        define();
    }
    

    static void define(){
        map = new HashMap();
        map.put("owl",      getQueryPath("owl.rq"));
        map.put("owlrl",    getQueryPath("owlrl.rq"));
        map.put("owlql",    getQueryPath("owlql.rq"));
        map.put("owlel",    getQueryPath("owlel.rq"));
        map.put("owltc",    getQueryPath("owltc.rq"));
        map.put("owlall",   getQueryPath("owltc2.rq"));
        map.put("sparqltc", getQueryPath("spintc.rq"));        
        map.put("sparql",   getQueryPath("sparql.rq"));        
        map.put("mix",      getQueryPath("mix.rq"));
    }
        
    @POST
    @Produces("text/html")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response typecheckPost(
            @FormParam("uri") String uri,
            @FormParam("entailment") String entail,
            @FormParam("transform") String trans,
            @FormParam("query") String query,
            @PathParam("serv") String serv) {
        return typecheck(uri,  entail, trans, query, serv);
    }

    @POST
    @Produces("text/html")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response typecheckPost_MD(
            @FormDataParam("uri") String uri,
            @FormDataParam("entailment") String entail,            
            @FormDataParam("transform") String trans,
            @FormDataParam("query") String query,
            @PathParam("serv") String serv) {
        return typecheck(uri, entail, trans, query, serv);
    }
    
    URI resolve(String uri) throws URISyntaxException{
        return Profile.getProfile().resolve(uri);
    }
    
    static String getQueryPath(String name){
        return Profile.getProfile().getQueryPath(name);
    }
    
    
    @GET
    @Produces("text/html")
    public Response typecheck(
            @QueryParam("uri") String uri,
            @QueryParam("entailment") String entail,            
            @QueryParam("transform") String trans,
            @QueryParam("query") String query,
            @PathParam("serv") String serv) {
                    
        boolean rdfs = entail != null && entail.equals("rdfs");
        GraphStore g = GraphStore.create(rdfs);
        
        String[] lstr = uri.split(";");
        for (String s : lstr) {
            try {
                URI url = new URI(s);
                if (!url.isAbsolute()) {
                    url = resolve(s);
                }
                if (serv.startsWith("sparql") || serv.equals("spin") || serv.equals("mix")) {
                    g = loadSPARQL(url.toString());
                } else {
                    load(url.toString(), g);
                }
            } catch (LoadException ex) {
                LogManager.getLogger(Processor.class.getName()).log(Level.ERROR, "", ex);
                return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
            } catch (EngineException ex) {
                LogManager.getLogger(Processor.class.getName()).log(Level.ERROR, "", ex);
                return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
            } catch (URISyntaxException ex) {
                LogManager.getLogger(Processor.class.getName()).log(Level.ERROR, "", ex);
                return Response.status(500).header(headerAccept, "*").entity(error(ex.toString(), null)).build();
            }
        }
        
         String temp = null;
         if (trans == null){
            temp = map.get(serv);
            if (temp == null){
                temp = getQueryPath("turtle.rq");
            }
         }

         Param par = new Param("/process", null, trans, null, temp, query);
         par.setLoad(uri);
         if (serv.equals(GRAPHIC)){
             
         }
         else if (trans != null){
             par.setProtect(true);
         }
         return new Transformer().template(new TripleStore(g), par);
    }
        
    
//    String resolve(String uri){
//        URI url;
//        try {
//            url = new URI(uri);
//            if (!url.isAbsolute()) {
//                url = base.resolve(uri);
//                uri= url.toString();
//            }
//        } catch (URISyntaxException ex) {
//            LogManager.getLogger(Processor.class.getName()).log(Level.ERROR, "", ex);
//        }
//        return uri;
//    }
    
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
    

    GraphStore load(String uri, GraphStore g) throws LoadException {
        Load ld = Load.create(g);
        //ld.setLimit(100000);       
        ld.parse(uri);
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