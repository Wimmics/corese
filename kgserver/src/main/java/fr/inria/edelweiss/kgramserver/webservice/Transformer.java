package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mappings;
import static fr.inria.edelweiss.kgramserver.webservice.Utility.toStringList;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.HTMLFormat;
import fr.inria.corese.kgtool.workflow.Data;
import fr.inria.corese.kgtool.workflow.WorkflowParser;
import fr.inria.corese.kgtool.workflow.SemanticWorkflow;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
    private static final String RESULT = NSManager.STL + "result";
    private static final String LOAD   = NSManager.STL + "load";
    private static Profile mprofile;
    private static NSManager nsm;
    boolean isDebug, isDetail;
    static boolean isTest = false;
    static HashMap<String, String> contentType;

    static {
        init();
    }

    static void init() {
        nsm = NSManager.create();
        mprofile = SPARQLRestAPI.getProfile();
        contentType = new HashMap<String, String>();
        contentType.put(fr.inria.edelweiss.kgtool.transform.Transformer.TURTLE, "text/turtle; charset=utf-8");
        contentType.put(fr.inria.edelweiss.kgtool.transform.Transformer.RDFXML, "application/rdf+xml; charset=utf-8");
        contentType.put(fr.inria.edelweiss.kgtool.transform.Transformer.JSON,   "application/ld+json; charset=utf-8");    
    }
    
    static Profile getProfile(){
        return mprofile;
    }

    static TripleStore getTripleStore() {
        return SPARQLRestAPI.getTripleStore();
    }
   

    // Template generate HTML
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/html")
    public Response queryPOSTHTML(
            @FormParam("profile") String profile, // query + transform
            @FormParam("uri") String resource, // query + transform
            @FormParam("mode") String mode, 
            @FormParam("param") String param, 
            @FormParam("query") String query, // SPARQL query
            @FormParam("name") String name, // SPARQL query name (in webapp/query)
            @FormParam("value") String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> from,
            @FormParam("named-graph-uri") List<String> named) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
        par.setDataset(from, named);
        return template(getTripleStore(), par);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/html")
    public Response queryPOSTHTML_MD(
            @FormDataParam("profile") String profile, // query + transform
            @FormDataParam("uri") String resource, 
            @FormDataParam("mode") String mode, 
            @FormDataParam("param") String param, 
            @FormDataParam("query") String query, // SPARQL query
            @FormDataParam("name") String name, // SPARQL query name (in webapp/query)
            @FormDataParam("value") String value, // values clause that may complement query           
            @FormDataParam("transform") String transform, // Transformation URI to post process result
            @FormDataParam("default-graph-uri") List<FormDataBodyPart> from,
            @FormDataParam("named-graph-uri") List<FormDataBodyPart> named) {
        
        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
        par.setDataset(toStringList(from), toStringList(named));
        return template(getTripleStore(), par);
    }
    
    @GET
    @Produces("text/html")
    public Response queryGETHTML(
            @QueryParam("profile") String profile, // query + transform
            @QueryParam("uri") String resource, // URI of resource focus
            @QueryParam("mode") String mode, 
            @QueryParam("param") String param, 
            @QueryParam("query") String query, // SPARQL query
            @QueryParam("name") String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value") String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {

        Param par = new Param(TEMPLATE_SERVICE, profile, transform, resource, name, query);
        par.setValue(value);
        par.setMode(mode);
        par.setParam(param);
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
            if (par.getParam() != null){
                isTest = par.getParam().equals("true");
            }
            
            complete(store.getGraph(), ctx);
            Dataset ds = createDataset(par.getFrom(), par.getNamed());
            Data data = workflow(store.getGraph(), ctx, ds, mprofile.getProfileGraph(), squery, ctx.getTransform());
            ResponseBuilder rb = Response.status(200).header(headerAccept, "*").entity(result(par, data.stringValue()));
            String format = getContentType(data);
            if (format != null && ! ctx.getService().contains("srv")){
                 rb.header("Content-type", format);
            }
            return rb.build();
            
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            String err = ex.toString();
            String q = null;
            if (ctx != null && ctx.getQueryString() != null){
                q = ctx.getQueryString();
            }
            return Response.status(500).header(headerAccept, "*").entity(error(err, q)).build();
        }
    }
    
    String getContentType(Data data){
        String trans = data.getProcess().getTransformation();
        if (trans != null){
            return contentType.get(trans);          
        }
        return null;
    }
    
    Data workflow(Graph g, Context c, Dataset ds, Graph profile, String q, String t)
            throws LoadException, EngineException {
        SemanticWorkflow w = create(c, ds, profile, q, t);
        if (!w.isDebug()) {
            w.setDebug(isTest);
        }
        Data data = w.process(new Data(g));
        return data;
    }
    
    /**
     * Create a Workflow to process service
     * If there is no explicit workflow specification, i.e no st:workflow [  ]
     * create a Workflow with query/transform.
     */
     SemanticWorkflow create(Context context, Dataset dataset, Graph profile, String query, String transform) throws LoadException {
        SemanticWorkflow wp = new SemanticWorkflow();
        wp.setContext(context);
        wp.setDataset(dataset);
        IDatatype swdt = context.get(Context.STL_WORKFLOW); 
        if (swdt != null) {
            WorkflowParser parser = new WorkflowParser(wp, profile);
            parser.parse(profile.getNode(swdt));
        } 
        else if (query != null) {
            // select where return Graph Mappings
            wp.addQueryGraph(query);
        }
        defaultTransform(wp, transform);
        return wp;
    }
     
     /**
      * If transform = null and workflow does not end with transform:
      * use st:sparql as default transform
      */
    void defaultTransform(SemanticWorkflow wp, String transform) {
        boolean isDefault = false;
        if (transform == null
                && (wp.getProcessList().isEmpty() || ! wp.getProcessLast().isTransformation())) {
            isDefault = true;
            transform = fr.inria.edelweiss.kgtool.transform.Transformer.SPARQL;
        }
        if (transform != null) {
            wp.addTemplate(transform, isDefault);
            wp.getContext().setTransform(transform);
        }
    }
             
    void complete(Graph graph, Context context) {
        Graph cg = graph.getNamedGraph(Context.STL_CONTEXT);
        if (cg != null) {
            context.set(Context.STL_CONTEXT, DatatypeMap.createObject(Context.STL_CONTEXT, cg));
        }
        context.set(Context.STL_DATASET, DatatypeMap.createObject(Context.STL_DATASET, graph));
        context.set(Context.STL_SERVER_PROFILE, mprofile.getProfile());
    }
    
    /**
     * Return transformation result as a HTML textarea
     * hence it is protected wrt img ...
     */
    String protect(Param p, String ft){
        fr.inria.edelweiss.kgtool.transform.Transformer t = 
            fr.inria.edelweiss.kgtool.transform.Transformer.create(RESULT);
        Context c = t.getContext();
        c.set(RESULT, ft);
        c.set(LOAD, (p.getLoad() == null) ? "" : p.getLoad());
        c.setTransform((p.getTransform()== null) ? "" : p.getTransform());  
        complete(c, p);
        IDatatype res = t.process();
        return res.stringValue();
    }
    
    String result(Param p, String ft){
        if (p.isProtect()){
            return protect(p, ft);
        }
        return ft;
    }
    
    String get(String name){
        fr.inria.edelweiss.kgtool.transform.Transformer t = 
            fr.inria.edelweiss.kgtool.transform.Transformer.create(NSManager.STL + "sparql#" + name); 
        return t.stransform();
    }

    String error(String err, String q){
        String mes = "";
        //mes += "<html><head><link href=\"/style.css\" rel=\"stylesheet\" type=\"text/css\" /></head><body>";
        mes +="<h3>Error</h3>";
        mes += "<pre>" + clean(err) + "</pre>";
        if (q != null){
            mes += "<pre>" + clean(q) + "</pre>";
        }
        //mes += "</body></html>";
        return mes;
    }

   String clean(String s){
       return s.replace("<", "&lt;");
   }

    Context create(Param par) {
        Context ctx= par.createContext();        
        complete(ctx, par);         
        return ctx;
    }
    
    Context complete(Context c, Param par){
        if (SPARQLRestAPI.isAjax){
            c.setProtocol(Context.STL_AJAX);
        }
        c.setUserQuery(par.isUserQuery());
        //c.setServerProfile(mprofile.getProfile());
        return c;
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
