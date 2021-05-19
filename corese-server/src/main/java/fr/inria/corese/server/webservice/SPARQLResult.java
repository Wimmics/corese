package fr.inria.corese.server.webservice;

import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.core.transform.Transformer;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
public class SPARQLResult implements ResultFormatDef, URLParam    {
    
    static private final Logger logger = LogManager.getLogger(SPARQLResult.class);
    private static final String headerAccept = "Access-Control-Allow-Origin";
    private static final String ERROR_ENDPOINT = "Error while querying Corese SPARQL endpoint";
    private static final String OPER = "operation";    
    private static final String URL = "url";   
    static final int ERROR = 500;
    private static SPARQLResult singleton;
    
    private HttpServletRequest request;
    QuerySolverVisitorServer visitor;
    
    static {
        setSingleton(new SPARQLResult());                
    }
    
    
    SPARQLResult(){}
    
    SPARQLResult(HttpServletRequest request) {
        setRequest(request);
    }

    static TripleStore getTripleStore() {
        return SPARQLRestAPI.getTripleStore();
    }
    
    static TripleStore getTripleStore (String name) {
           if (name == null) {
               return getTripleStore();
           }
           return Manager.getEndpoint(name);
    }
    
    
    /**
     * Specific endpoint function where format can be specified by format parameter
     * Content-Type is set according to format parameter and what is returned by ResultFormat.
     */
    public Response getResultFormat(String name, String oper, List<String> uri, List<String> param, List<String> mode,
            String query, String access, 
            List<String> defaut, List<String> named,
            String format, int type, List<String> transform) { 
           
        try {  
            logger.info("Endpoint URL: " + getRequest().getRequestURL());
            if (query == null)
                throw new Exception("No query");

            beforeRequest(getRequest(), query);
            Dataset ds = createDataset(getRequest(), defaut, named, access);
                                  
            beforeParameter(ds, oper, uri, param, mode);
            Mappings map = getTripleStore(name).query(getRequest(), query, ds);  
            afterParameter(ds, map);
            
            ResultFormat rf = getFormat(map, ds, format, type, transform);            
            String res = rf.toString();
                       
            ResponseBuilder rb = Response.status(Response.Status.OK).header(headerAccept, "*");
            
            if (format != null) {
                // real content type of result, possibly different from @Produces
                rb = rb.header("Content-Type", rf.getContentType());
            }
            Response resp = rb.entity(res).build();
            
            afterRequest(getRequest(), resp, query, map, res);  
                                 
            return resp;
        } catch (Exception ex) {
            logger.error(ERROR_ENDPOINT, ex);
            return Response.status(ERROR).header(headerAccept, "*").entity(ERROR_ENDPOINT).build();
        }
    }
    
        /**
     * Creates a Dataset based on a set of default or named graph URIs. 
     * For *strong* SPARQL compliance, use dataset.complete() before returning the dataset.
     *
     * @return a dataset
     */    
    Dataset createDataset(HttpServletRequest request, List<String> defaut, List<String> named, String access) {
        Dataset ds = null;
        if (((defaut != null) && (!defaut.isEmpty())) 
                || ((named != null) && (!named.isEmpty()))) {
            ds = Dataset.instance(defaut, named);
        } 
        else {
            ds = new Dataset();
        }
        boolean b = SPARQLRestAPI.hasKey(access);
        if (b) {
            System.out.println("has key access");
        }
        Level level = Access.getQueryAccessLevel(true, b);
        ds.getCreateContext().setLevel(level);
        ds.getContext().setURI(URL, request.getRequestURL().toString());
        return ds;
    }
    
    
    /**
     * Parameters of sparql service URL: 
     * http://corese.fr/sparql?mode=debug&param=format:rdf;test:12
     * 
     * http://corese.fr/d2kab/sparql
     * http://corese.fr/d2kab/federate
     * name = d2kab ; oper = sparql|federate
     * parameter recorded in context and as ldscript global variable
     */
    Dataset beforeParameter(Dataset ds, String oper, List<String> uri, List<String> param, List<String> mode) {
        if (oper != null) {
            ds.getContext().set(OPER, oper);
            List<String> federation = new ArrayList<>();
            switch (oper) {
                
                case FEDERATE:
                    // From SPARQLService: var name is bound to d2kab
                    // URL = http://corese.inria.fr/d2kab/federate
                    // sparql query processed as a federated query on list of endpoints
                    // From SPARQL endpoint (alternative) mode and uri are bound
                    // http://corese.inria.fr/sparql?mode=federate&uri=http://ns.inria.fr/federation/d2kab
                    mode = leverage(mode);
                    //uri  = leverage(uri);
                    // declare federate mode for TripleStore query()
                    mode.add(FEDERATE);
                    // federation URL defined in /webapp/data/demo/fedprofile.ttl
                    //uri.add(ds.getContext().get(URL).getLabel());
                    federation.add(ds.getContext().get(URL).getLabel());
                    defineFederation(ds, federation);
                    break;
                    
                case SPARQL:
                    // URL = http://corese.inria.fr/id/sparql
                    // when id is a federation: union of query results of endpoint of id federation
                    // otherwise query triple store with name=id
                    String surl = ds.getContext().get(URL).getLabel();
                    String furl = surl;
                    
                    if (FederateVisitor.getFederation(furl) == null) {
                        furl = surl.replace("/sparql", "/federate");
                    }
                    
                    if (FederateVisitor.getFederation(furl) != null) {
                        // federation is defined 
                        mode = leverage(mode);
                        //uri = leverage(uri);
                        mode.add(FEDERATE);
                        mode.add(SPARQL);
                        // record the name of the federation
                        //uri.add(furl);
                        federation.add(furl);
                        defineFederation(ds, federation);
                    }
                    break;
                    
                // default:
                // other operations considered as sparql endpoint
                // with server name if any  
                default:
                    context(ds);
            }
        }
        
        if (uri!=null && !uri.isEmpty()) {
            // list of URI given as parameter uri= 
            ds.getContext().set(URI, DatatypeMap.listResource(uri));
            //ds.setUriList(uri);
        }
        
        if (param != null) {
            for (String kw : param) {
                mode(ds, PARAM, decode(kw));
            }
        }
        
        if (mode != null) {
            for (String kw : mode) {
                mode(ds, MODE, decode(kw));
            }
        }
        
        beforeParameter(ds);
        
        return ds;
    }
    
    /**
     * urlprofile.ttl may predefine parameters for endpoint URL eg /psparql
     */
    void context(Dataset ds) {
        Context ct = ds.getContext();
        IDatatype dt = Profile.getProfile().getContext().get(ct.get(URL).getLabel());
        
        if (dt != null) {
            
            for (IDatatype pair : dt) {
                String key = pair.get(0).getLabel();
                IDatatype val = pair.get(1);
                if (key.equals(MODE)) {
                    mode(ds, key, val.getLabel());
                }
                else {
                    ct.add(key, val);
                }
            }
            System.out.println("Context:\n" + ct);
        }
    }
    
    void defineFederation(Dataset ds, List<String> federation) {
        ds.setUriList(federation);
        ds.getContext().set(FEDERATION, DatatypeMap.listResource(federation));
    }
    
    String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            return value;
        }
    }
          
    List<String> leverage(List<String> name) {
        return (name == null) ? new ArrayList<>() : name;
    }
    
    /**
     * Record dataset from named in context for documentation purpose
     */
    void beforeParameter(Dataset ds) {
        IDatatype from = ds.getFromList();
        if (from.size() > 0) {
            ds.getContext().set(DEFAULT_GRAPH, from);
        }
        IDatatype named = ds.getNamedList();
        if (named.size() > 0) {
            ds.getContext().set(NAMED_GRAPH, named);
        }
    }   
    
    /**
     * name:  param | mode
     * value: debug | trace.
     */
    void mode(Dataset ds, String name, String value) {
        //System.out.println("URL mode: " + mode);
        ds.getContext().add(name, value);

        switch (name) {
            case MODE:
                switch (value) {
                    case DEBUG:
                        ds.getContext().setDebug(true);
                    // continue

                    default:
                        ds.getContext().set(value, true);
                        break;
                }
                break;

            case PARAM:
                URLServer.decode(ds.getContext(), value);
                break;
        }
    }
       
    
    void afterParameter(Dataset ds, Mappings map) {
        if (ds.getContext().hasValue(TRACE)) {
            System.out.println("SPARQL endpoint");
            System.out.println(map.getQuery().getAST());
            System.out.println(map.toString(false, true, 10));
        }
        // draft for testing
        if (ds.getContext().hasValue(FORMAT)) {
            ResultFormat ft = ResultFormat.create(map, ds.getContext().get(FORMAT).getLabel());
            System.out.println(ft);
        }
    }
    
    QuerySolverVisitorServer getVisitor() {
        return visitor;
    }
    
    SPARQLResult setVisitor(QuerySolverVisitorServer vis) {
        visitor = vis;
        return this;
    }
    
    /**
     * Visitor call LDScript event @beforeRequest @public function 
     * profile.ttl must load function definitions, 
     * e.g. <demo/system/event.rq>
     * 
     */
    void beforeRequest(HttpServletRequest request, String query) {
        getVisitor().beforeRequest(request, query);
    }
    
    void afterRequest(HttpServletRequest request, String query, Mappings map) {
        getVisitor().afterRequest(request, query, map);
    }
    
    void afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res) {
        getVisitor().afterRequest(request, resp, query, map, res);
    }
    
    /**
     * predefined parameter associated to URL in urlparameter.ttl
     */
    List<String> getValue(Context ct, String name, List<String> value) {
        if (value != null) {
            return value;
        }
        IDatatype dt = ct.get(name);
        if (dt == null) {
            return null;
        }
        return DatatypeMap.toStringList(dt);
    }
       
    ResultFormat getFormat(Mappings map, Dataset ds, String format, int type, List<String> transformList) {
        // predefined parameter associated to URL in urlparameter.ttl
        transformList = getValue(ds.getContext(), TRANSFORM, transformList);
        if (transformList == null || transformList.isEmpty()) {
            return getFormatSimple(map, ds, format, type);
        } else {
            Optional<ResultFormat> res = getFormatTransformList(map, ds, format, type, transformList);
            if (res.isPresent()) {
                // no link: return transformation result 
                return res.get();
            }
            // link: return query result
            return getFormatSimple(map, ds, format, type);
        }
    }
    
    Optional<ResultFormat> getFormatTransformList(Mappings map, Dataset ds, String format, int type, List<String> transformList) {
        for (String transform : transformList) {
            transform = NSManager.nsm().toNamespace(transform);
            List<String> list = getFormatList(transform);
            if (list!=null) {
                // st:all -> st:xml st:json ...
                getFormatTransformList(map, ds, format, type, list);
            } else {
                ResultFormat res = getFormatTransform(map, ds, format, type, transform);

                if (ds.getContext().hasValue(LINK)) {
                    // mode=link
                    // save transformation result in document and return URL of document in map link
                    String url = TripleStore.document(res.toString(), getName(transform));
                    map.addLink(url);
                    logger.info("Transformation result in: " + url);
                } else {
                    return Optional.of(res);
                }
            }
        }
        return Optional.empty();
    }
    
    List<String> getFormatList(String name) {
        return Transformer.getFormatList(name);
    }
    
    String getName(String transform) {
        if (transform.contains("#")) {
            return transform.substring(1+transform.indexOf("#"));
        }
        return transform.substring(1+transform.lastIndexOf("/"));
    }
    
    ResultFormat getFormatTransform(Mappings map, Dataset ds, String format, int type, String transform) {
        ResultFormat ft;
        if (type == UNDEF_FORMAT) {
            ft = ResultFormat.create(map, format, transform).init(ds);
        } else {
            ft = ResultFormat.create(map, type, transform).init(ds);
        }
        if (map.getBinding()!=null && ft.getBind()==null) {
            ft.setBind((Binding)map.getBinding());
        }
        return ft;
    }
    
    ResultFormat getFormatSimple(Mappings map, Dataset ds, String format, int type) {
        if (type == UNDEF_FORMAT) {
            return ResultFormat.create(map, format);
        } else {
            return ResultFormat.create(map, type);
        }
    }

    public static SPARQLResult getSingleton() {
        return singleton;
    }

    public static void setSingleton(SPARQLResult aSingleton) {
        singleton = aSingleton;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
}
