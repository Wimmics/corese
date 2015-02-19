/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.HTMLFormat;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

/**
 * SPARQL endpoint 
 * eval query and/or apply transformation (on query result) and return HTML
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("template")
public class Transformer {

    private static Logger logger = Logger.getLogger(Transformer.class);
    private static final String headerAccept = "Access-Control-Allow-Origin";
    private static final String TEMPLATE_SERVICE = "/template";
    private static Profile mprofile;
    private static NSManager nsm;
    boolean isDebug, isDetail;

    static {
        init();
    }

    static void init() {
        nsm = NSManager.create();
        mprofile = SPARQLRestAPI.getProfile();
    }
    
    static Profile getProfile(){
        return mprofile;
    }

    static TripleStore getTripleStore() {
        return SPARQLRestAPI.getTripleStore();
    }

    // Template generate HTML
    @POST
    @Produces("text/html")
    public Response queryPOSTHTML(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> from,
            @FormParam("named-graph-uri") List<String> named) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(from, named);
        return template(getTripleStore(), par);
    }

    @GET
    @Produces("text/html")
    public Response queryGETHTML(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return template(getTripleStore(), par);
    }

    public Response template(TripleStore store, Param par) {

        
        Context ctx = null;
        try {

            par = mprofile.complete(par);
            ctx = create(par);
                       
            if (store != null && store.getMode() == QueryProcess.SERVER_MODE) {
                // check profile, transform and query
                String prof = ctx.getProfile();
                if (prof != null && !nsm.toNamespace(prof).startsWith(NSManager.STL)) {
                    return Response.status(500).header(headerAccept, "*").entity("Undefined profile: " + prof).build();
                }
                String trans = ctx.getTransform();
                if (trans != null && !nsm.toNamespace(trans).startsWith(NSManager.STL)) {
                    return Response.status(500).header(headerAccept, "*").entity("Undefined transform: " + trans).build();
                }
            }

            String squery = par.getQuery();
            
            if (isDebug) {
                System.out.println("query: \n" + squery);
            }
            // servlet context given to query process and transformation
            Mappings map = null;

            if (squery != null && store != null) {
                Dataset ds = createDataset(par.getFrom(), par.getNamed(), ctx);
                map = store.query(squery, ds);

                if (isDetail) {
                    System.out.println(map);
                }
                if (isDebug) {
                    System.out.println("map: " + map.size());
                }
            }

            HTMLFormat ft = HTMLFormat.create(store.getGraph(), map, ctx);

            return Response.status(200).header(headerAccept, "*").entity(ft.toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            String err = ex.toString();
            String q = null;
            if (ctx != null && ctx.getQuery() != null){
                q = ctx.getQuery();
            }
            return Response.status(500).header(headerAccept, "*").entity(error(err, q)).build();
        }
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

    Context create(Param par) {
        Context ctx = new Context();
        if (par.getProfile() != null) {
            ctx.setProfile(nsm.toNamespace(par.getProfile()));
        }
        if (par.getTransform() != null) {
            ctx.setTransform(nsm.toNamespace(par.getTransform()));
        }
        if (par.getUri() != null) {
            ctx.setURI(nsm.toNamespace(par.getUri()));
        }
        if (par.getQuery() != null) {
            ctx.setQuery(par.getQuery());
        }
        if (par.getName() != null) {
            ctx.setName(par.getName());
        }
        if (par.getService() != null){
            ctx.setService(par.getService());
        }
         if (par.getLang() != null){
            ctx.setLang(par.getLang());
        }
         ctx.setServer(Profile.SERVER);
        return ctx;
    }

    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
        return createDataset(defaultGraphUris, namedGraphUris, null);
    }

    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris, Context c) {
        if (c != null
                || ((defaultGraphUris != null) && (!defaultGraphUris.isEmpty()))
                || ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
            Dataset ds = Dataset.instance(defaultGraphUris, namedGraphUris);
            ds.setContext(c);
            return ds;
        } else {
            return null;
        }
    }

   
}
