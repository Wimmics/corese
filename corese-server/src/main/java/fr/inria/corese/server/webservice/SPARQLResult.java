package fr.inria.corese.server.webservice;

import fr.inria.corese.server.webservice.message.LinkedResult;
import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Access;
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
 * Process sparql query, post process query result, generate query result format
 */
public class SPARQLResult implements ResultFormatDef, URLParam    {
    
    static private final Logger logger = LogManager.getLogger(SPARQLResult.class);
    private static final String headerAccept = "Access-Control-Allow-Origin";
    private static final String ERROR_ENDPOINT = "Error while querying Corese SPARQL endpoint";
    private static final String OPER = "operation";    
    private static final String URL = Context.URL;   
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
     * Content-Type is set according to format parameter and what is returned by ResultFormat
     * 
     * @name is a) the name of a specific triple store, b) undefined for standard sparql endpoint
     * @oper is sparql | federate | symbolic name defined in urlprofile.ttl
     * @uri is optional list of URI. use case: URL of shacl shape
     * @param is optional parameter in format: param=key~val;val
     * @mode is such as  mode=debug;link;log
     * @access is a key that may give access to protected features
     * @defaut and @named are graph name URI 
     * @format is json|xml to specify return format when there is no http header content
     * @type is format specified by content negotiation http header (consider type otherwise format)
     * @transform is list of transformation such as st:map
     */
    public Response getResultFormat(String name, String oper, 
            List<String> uri, List<String> param, List<String> mode,
            String query, String access, 
            List<String> defaut, List<String> named,
            String format, int type, List<String> transform) { 
           
        try {  
            logger.info("Endpoint URL: " + getRequest().getRequestURL());
            
            query = getQuery(query, mode);
            if (query == null) {
                throw new EngineException("Undefined query parameter ");
            }
            logger.info("Query: " + query);

            beforeRequest(getRequest(), query);
            Dataset ds = createDataset(getRequest(), defaut, named, access);
                                  
            beforeParameter(ds, oper, uri, param, mode, transform);
            Mappings map = getTripleStore(name).query(getRequest(), query, ds);
            complete(map, ds.getContext());
            afterParameter(ds, map);
            
            ResultFormat rf = getFormat(map, ds, format, type, transform);            
            String res = rf.toString();
                       
            ResponseBuilder rb = Response.status(Response.Status.OK).header(headerAccept, "*");
            
            if (format != null) {
                // real content type of result, possibly different from @Produces
                rb = rb.header("Content-Type", rf.getContentType());
            }
            Response resp = rb.entity(res).build();
            
            afterRequest(getRequest(), resp, query, map, res, ds);  
                                 
            return resp;
        } catch (EngineException ex) {
            logger.error("query:");
            logger.error(query);
            logger.error(ERROR_ENDPOINT, ex);
            String message = String.format("%s\n%s", ERROR_ENDPOINT, ex.getMessage());
            return Response.status(ERROR).header(headerAccept, "*").entity(message).build();
        }
    }
    
    String getQuery(String query, List<String> mode) {
        if (query == null && mode != null) {
            query = getContext().getDefaultValue(mode, QUERY);
        }
        return query;
    }
    
    /**
     * Post processing
     */
    void complete(Mappings map, Context c) {
        if (c.hasValue(DOCUMENT)) {
            // could be a graph style sheet for ldviz
            for (IDatatype dt : c.get(DOCUMENT)) {
                map.addLink(dt.getLabel());
            }
        }
        if (c.hasValue(EXPLAIN)) {
            Binding bind = (Binding) map.getBinding();          
            if (bind != null && bind.getTrace().length()>0) {
                LinkedResult lr = linkedResult(c, "explain");
                lr.write(bind.getTrace().toString());
                map.addLink(lr.getURL());
            }
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
        boolean b = SPARQLRestAPI.hasKey(request, access);
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
     * http://corese.fr/sparql?mode=debug&format=json
     * 
     * http://corese.fr/d2kab/sparql
     * http://corese.fr/d2kab/federate
     * name = d2kab ; oper = sparql|federate
     * parameter recorded in context 
     */
    Dataset beforeParameter(Dataset ds, String oper, List<String> uri, 
            List<String> param, List<String> mode, List<String> transform) {
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
                    federation.add(ds.getContext().get(URL).getLabel());
                    defineFederation(ds, federation);
                    // additional parameters attached to URL in urlparameter.ttl 
                    break;
                    
                case COMPILE:
                    //    /test/compile?uri=http://myendpoint/sparql
                    mode = leverage(mode);
                    mode.add(FEDERATE);
                    mode.add(COMPILE);
                    federation.addAll(uri);
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
                    //  /map/sparql
                    
            }
            //  get additional parameters attached to URL in urlprofile.ttl 
            context(ds.getContext(), getContext());
        }
        
        // get default parameters attached to joker mode * in urlprofile.ttl 
        //ds.getContext().context(getContext(), STAR);
        
        if (uri!=null && !uri.isEmpty()) {
            // list of URI given as parameter uri= 
            ds.getContext().set(URI, DatatypeMap.listResource(uri));
        }
        
        if (param != null) {
            for (String kw : param) {
                // decode param=key~val;val
                ds.getContext().mode(getContext(), PARAM, decode(kw));
            }
        }
        
        if (mode != null) {
            for (String kw : mode) {
                // decode mode=map
                ds.getContext().mode(getContext(), MODE, decode(kw));
            }
        }
        
        if (!ds.getContext().hasValue(USER)) {
            // mode=user means skip mode=*
            // get default parameters attached to joker mode * in urlprofile.ttl 
            ds.getContext().context(getContext(), STAR);
        }
        
        if (transform != null && ! transform.isEmpty()) {
            ds.getContext().set(URLParam.TRANSFORM, DatatypeMap.newStringList(transform));
        }
        
        beforeParameter(ds);
        
        return ds;
    }
    
    /**
     * urlprofile.ttl may predefine parameters for endpoint URL eg /psparql
     * complete Context accordingly as if it were URL parameters
     */
    void context(Context c, Context gc) {
        c.context(gc, c.get(URL).getLabel());
    }
    
   
       

    /**
     * Server Context build from urlprofile.ttl
     * Define mode and endpoint URL
     */
    Context getContext() {
        return Profile.getProfile().getContext();
    }
    
    
    void defineFederation(Dataset ds, List<String> federation) {
        ds.setUriList(federation);
        //ds.getContext().set(FEDERATION, DatatypeMap.listResource(federation));
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
    
    void afterRequest(HttpServletRequest request, Response resp, String query, Mappings map, String res, Dataset ds) {
        afterRequest(map, ds, res);
        getVisitor().afterRequest(request, resp, query, map, res);
    }
    
    void afterRequest(Mappings map, Dataset ds, String res) {
        if (ds.getContext().hasValue(TRACE)) {
            System.out.println(res);
        }
    }
    
         
    ResultFormat getFormat(Mappings map, Dataset ds, String format, int type, List<String> transformList) {
        // predefined parameter associated to URL/mode in urlparameter.ttl
        transformList = selectTransformation(ds.getContext(), getValue(ds.getContext(), TRANSFORM, transformList));
        
        if (transformList == null || transformList.isEmpty()) {
            return getFormatSimple(map, ds, format, type);
        } else {
            return getFormatTransform( map,  ds,  format,  type, transformList);           
        }
    }
    
    /**
     * Post process query result map with transformation(s)
     * Return either 
     * a) when mode=link : query result with link url to transformation result document
     * b) otherwise transformation result
     */
    ResultFormat getFormatTransform(Mappings map, Dataset ds, String format, int type, List<String> transformList) {
            logger.info("Transform: " + transformList);
            
            boolean link = ds.getContext().hasAnyValue(LINK, LINK_REST);           
            ResultFormat std ;
            LinkedResult lr = null;
            
            if (link) {
                lr = linkedResult(ds.getContext(), "std");
                // prepare (and return) std result with link to transform
                // map will record link url of transform in function getFormatTransformList
                // result format will be generated when returning HTTP result
                std = getFormatSimple(map, ds, format, type);
                // record url of std result document in case transform generate link to std result (cf mapper)
                ds.getContext().add(Context.STL_LINK, DatatypeMap.newResource(lr.getURL()));
            }
            else {
                // return transform result 
                // record std result in href document in case transform generate link href
                int mytype = (type==ResultFormat.HTML_FORMAT) ? ResultFormat.UNDEF_FORMAT : type;
                std = getFormatSimple(map, ds, format, mytype);
            }
            
            Optional<ResultFormat> res = getFormatTransformList(map, ds, format, type, transformList);
            if (res.isPresent()) {
                // no link: return transformation result 
                return res.get();
            }
            
            if (link) {
                // do it only now because map has recorded transform link
                // generate std result document in case transform manage link (cf mapper)
                lr.write(std.toString());
            }
            // link: return query result
            return std;
    }
    
    /**
     * URLs of one request share the same key file name
     */
    LinkedResult linkedResult(Context c, String name) {
        return new LinkedResult(name, "", c.getCreateKey());
    }
    
    /**
     * Process transformations
     * When mode=link, add url of transformation result in map query result link
     * and return empty 
     * Otherwise return result of (first) transformation
     */
    Optional<ResultFormat> getFormatTransformList(Mappings map, Dataset ds, String format, int type, List<String> transformList) {
        ResultFormat fst = null;
        Context c = ds.getContext();
        // prepare the list of linked result URL before all
        // each result may then contain link to these URLs
        List<LinkedResult> linkedResult = getLinkedResult(map, c, transformList);
        int i = 0;
        
        for (String transform : transformList) {
            ResultFormat res = getFormatTransform(map, ds, format, type, transform);
            if (fst == null) {
                fst = res;
            }
            if (c.hasValue(TRACE)) {
                logger.info(transform);
                logger.info(res);
            }

            if (c.hasAnyValue(LINK, LINK_REST)) {
                // mode=link
                // save transformation result in document and record URL of document in map result link
                LinkedResult lr = linkedResult.get(i++);
                lr.write(res.toString());                
                logger.info(String.format("Transformation %s result in: %s", 
                        c.nsm().toPrefix(transform), lr.getURL()));
            } else {
                // no link: return result of first transformation
                return Optional.of(res);
            }
        }
        
        if (c.hasValue(LINK_REST)) {
            // return result of first transformation (it may have generated links to other transformations)
            return Optional.of(fst);
        }
        else {
            // query result will be returned with link url to transformation result
            return Optional.empty();
        }
    }
    
    /**
     * Prepare LinkedResult place holder list with file name and URL
     * Each LinkedResult will be used to store a result in a document accessible by URL
     * PRAGMA: map record link url 
     * It will be considered by ResultFormat std in
     * function getFormatTransform above
     *
     */
    List<LinkedResult> getLinkedResult(Mappings map, Context c, List<String> transformList) {
        if (c.hasAnyValue(LINK, LINK_REST)) {
            List<LinkedResult> list = new ArrayList<>();
            
            for (String name : transformList) {
                LinkedResult lr = linkedResult(c, getName(name));
                list.add(lr);
                map.addLink(lr.getURL());
            }
            return list;
        }
        return null;
    }
    
    ResultFormat getFormatTransform(Mappings map, Dataset ds, String format, int type, String transform) {
        ResultFormat ft;
        if (type == UNDEF_FORMAT) {
            ft = ResultFormat.create(map, format, transform).init(ds);
        } else {
            ft = ResultFormat.create(map, type, transform).init(ds);
        }
        if (map.getBinding()!=null && ft.getBind()==null) {
            // share ldscript binding environment with transformer
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
    
    /**
     * predefined parameter associated to url/mode in urlprofile.ttl
     */
    List<String> getValue(Context ct, String name, List<String> value) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return ct.getStringList(name);
    }
    
    /**
     * select authorized transformations
     */
    List<String> selectTransformation(Context ct, List<String> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        // predefined transform st:all = (st:xml st:json)
        List<String> alist = prepare(list);
        // check authorized transformation
        return Access.selectNamespace(Access.Feature.LINKED_TRANSFORMATION, ct.getLevel(), alist);
    }
    
    
    
    /**
     * trans;trans -> list of trans
     * st:all -> st:xml st:json
     */
    List<String> prepare(List<String> transformList) {
        List<String> list = new ArrayList<>();
        
        for (String name : transformList) {
            if (name.contains(";")) {
                for (String key : name.split(";")) {
                    getContext().prepare(key, list);
                }
            }
            else {
                getContext().prepare(name, list);
            }
        }

        return list;
    }
    
  
    String getName(String transform) {
        if (transform.contains("#")) {
            return transform.substring(1+transform.indexOf("#"));
        }
        return transform.substring(1+transform.lastIndexOf("/"));
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
