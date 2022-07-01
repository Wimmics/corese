package fr.inria.corese.core.load;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.ResultFormat;
import static fr.inria.corese.core.print.ResultFormat.RDF_XML;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_XML;
import fr.inria.corese.core.query.CompileService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.SERVICE_LIMIT;
import static fr.inria.corese.core.util.Property.Value.SERVICE_SEND_PARAMETER;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.HashMapList;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Cookie;
import java.util.HashMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Send a SPARQL query to a SPARQL endpoint
 * Return a Mappings
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 */
public class Service implements URLParam {
    static Logger logger = LoggerFactory.getLogger(Service.class);
    static final String ENCODING = "UTF-8";
     // load take URL parameter into account, e.g. format=rdfxml
    public static boolean LOAD_WITH_PARAMETER = false;
    public static final String MIME_TYPE = "application/sparql-results+xml,application/rdf+xml";
    public static final String XML = SPARQL_RESULTS_XML;
    public static final String RDF = RDF_XML;
    private static final String NAMED_GRAPH_URI = "named-graph-uri";
    private static final String DEFAULT_GRAPH_URI = "default-graph-uri";
    public static final String NB_RESULT_MAX = "X-SPARQL-MaxRows";

    static HashMap<String, String> redirect;
    
    private ClientBuilder clientBuilder;

    private boolean isDebug = false;
    private boolean post = true;
    private boolean showResult = false;
    private boolean trap = false;
    private int timeout = 0;
    private int count = 0;
    private URLServer url;
    private Binding bind;
    private Access.Level level = Access.Level.DEFAULT;
    private String format;
    private ServiceParser parser;
    private ServiceReport report;
    private Response response;
    private boolean log = false;
    private double time = 0;
    
    static {
        redirect = new HashMap<>();
    }
    
    public Service() {
        clientBuilder = ClientBuilder.newBuilder();
    }

    public Service(URLServer serv) {
        this(serv, ClientBuilder.newBuilder());
    }
    public Service(String serv) {
        this(new URLServer(serv), ClientBuilder.newBuilder());
    }
    public Service(URLServer serv, ClientBuilder builder) {
        this.clientBuilder = builder;
        if (serv.getNumber()<0) {
            serv.setNumber(0);
        }
        setURL(serv);
        getCreateReport().setURL(serv);
    }


    public Mappings select(String query) throws LoadException, EngineException {
        Query q = QueryProcess.create().compile(query);
        return getCreateParser().parseMapping(q, query, process(query), ENCODING);
    }

    public Graph construct(String query) throws LoadException, EngineException {
        Query q = QueryProcess.create().compile(query);
        return getCreateParser().parseGraph(q, process(query));
    }

    public Mappings query(Query query, Mapping m) throws LoadException {
        return query(query, query.getAST(), m);

    }
    
    public Mappings query(Query query, ASTQuery ast, Mapping m) throws LoadException {
        if (isReport(query)) {
            return queryReport(query, ast, m);
        } else {
            return queryBasic(query, ast, m);
        }
    }
       
    /**
     * trap exception and return empty result with service detail 
     */
    Mappings queryReport(Query query, ASTQuery ast, Mapping m) throws 
            LoadException {
        try { 
            return queryBasic(query, ast, m);
        }
        catch (ResponseProcessingException ex) {
           return getCreateReport(query)
                   .setFormat(getFormat()).setAccept(getAccept())
                   .serviceReport(ex, null);           
        }
        catch (Exception ex) {
           return getCreateReport(query)
                   .setFormat(getFormat()).setAccept(getAccept())
                   .serviceReport(null, ex);
        }        
    }
             
    Mappings queryBasic(Query query, ASTQuery ast, Mapping m) throws LoadException {
        try {
            metadata(ast);
            Mappings map;
            if (m != null) {
                ast = mapping(query, m);
            }
            String astq = ast.toString();
            String accept = accept(ast);

            if (ast.isSelect() || ast.isAsk() || ast.isUpdate()) {
                map = getCreateParser().parseMapping(query, astq, process(astq, accept), encoding(ast));
                getCreateReport(query).setMappings(map);
            } else {
                Graph g = getCreateParser().parseGraph(query, process(astq, accept), encoding(ast));
                map = new Mappings();
                map.setGraph(g);
            }
            map.setQuery(query);
            map.init(query);
            // complete report with response and header
            // report was recorded in map by ServiceParser 
            // call to ServiceReport parserReport()
            getCreateReport(query).setAccept(accept)
                    .completeReport(map);
            //log(getCreateReport());
            return map;
        } catch (LoadException e) {
            // ServiceParser throw exception
            if (isReport(query)) {
                return getCreateReport(query).parserReport(e);
            }
            throw e;
        }
    }
    
    void log(ServiceReport report) {
        log(report.getResponse());
    }
    
    void log(Response res) {
        if (res != null) {
            if (res.getHeaderString(NB_RESULT_MAX) != null) {
                logger.info(String.format("%s = %s", NB_RESULT_MAX, res.getHeaderString(NB_RESULT_MAX)));
            }
        }
    }
    
    public boolean isReport(Query query) {
        return getCreateReport(query).isReport();
    }
   
    public String process(ASTQuery ast, String query) {
        return process(query, accept(ast));
    }
       
    public String process(String query) {
        return process(query, getAccept());
    }
    
    public String process(String query, String mime) {
        if (isPost()) {
            return post(query, mime);
        }
        else {
            return get(query, mime);
        }
    }
    
    public String post(String url, String query, String mime) {
        if (redirect.containsKey(url)) {
            return post(redirect.get(url), query, mime);
        }
        else {
            return basicPost(url, query, mime);
        }
    }
    
    // https://docs.oracle.com/javaee/7/api/index.html
    public String basicPost(String url, String query, String mime) {
        //logger.info("Timeout: " + timeout);
        clientBuilder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout(timeout, TimeUnit.MILLISECONDS);
        Client client = clientBuilder.build(); 
        WebTarget target = client.target(url);
        Form form = getForm();
        form.param(QUERY, query);
        try {
            // request() return Invocation.Builder
            Cookie cook = new Cookie(COUNT, Integer.toString(getCount()), url, getURL().getServer());
            Cookie cook2 = new Cookie(PLATFORM, CORESE);
            String accept = getAccept(mime);
            Builder rb = target.request(accept); // .cookie(cook)
            //setHeader(rb);
            //logger.info("Header Accept: " + accept);
            
            Date d1 = new Date();
            //if (isDebug()) {
                logger.info("Post " + getURL().getURL());
            //}
            
            Response resp =  rb.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            
            if (isDebug()) {
                Date d2 = new Date();
                logger.info("After post: " + resp.getStatus() + " " + resp.getStatusInfo());
                logger.info("Time post: " + ((d2.getTime()-d1.getTime())/1000.0));
            }

            String res = resp.readEntity(String.class);
            
            Date d2 = new Date();
            double time = (d2.getTime() - d1.getTime()) / 1000.0;
            getCreateReport().setTime(time);
            if (isDebug()) {
                logger.info("Time read: " + time);
            }            
            
            if (resp.getStatus() == Response.Status.SEE_OTHER.getStatusCode() ||
                resp.getStatus() == Response.Status.MOVED_PERMANENTLY.getStatusCode()    ) {
                String myUrl = resp.getLocation().toString();
                logger.warn(String.format("Service redirection: %s to: %s", url, myUrl));
                if (myUrl.equals(url)) {
                    throw new RedirectionException(resp);
                }
                redirect(url, myUrl);
                getCreateReport().setLocation(myUrl);
                return post(myUrl, query, mime);
            }
               
            trace(resp);
            logger.info("Response status: " + resp.getStatus());
            //logger.info("From " + getURL().getURL());
            
            if (resp.getMediaType()!=null) {
                recordFormat(resp.getMediaType().toString());
            }
            getCreateReport().setResponse(resp);
            getCreateReport().setResult(res);
            
            if (resp.getStatus() >= Response.Status.BAD_REQUEST.getStatusCode()) {
                ResponseProcessingException ex = new ResponseProcessingException(resp, res);
                
                if (isLog() && getLog() != null){
                    // use case: @federate call not within ProviderService
                    // log here
                    getLog().addException(new EngineException(ex, ex.getMessage())
                        .setURL(getURL()).setObject(ex.getResponse()));
                }               
                throw ex;
            }
            
            trace(res);
            return res;
        } catch (RedirectionException ex) {
            String uri = ex.getLocation().toString();
            logger.warn(String.format("Service redirection: %s to: %s", url, uri));
            if (uri.equals(url)) {
                throw ex;
            }
            return post(uri, query, mime);
        }
        catch (Exception e) {
            logger.error(getURL().toString());
            logger.error(e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
    }
    
    void redirect(String url1, String url2) {
        logger.info(String.format("Record %s redirect to %s", url1, url2));
        redirect.put(url1, url2);
    }
    
    void trace(Response res) {
        if (getURL().hasParameter(DISPLAY, HEADER)) {
            System.out.println("service header: " + getURL().getURL());
            for (String name : res.getHeaders().keySet()) {
                System.out.println("header: " + name + "=" + res.getHeaderString(name));
            }
        }
    }
    
    void trace(String str) {
        if (isShowResult()) {
            System.out.println("Service string result");
            System.out.println(str);
        }
    }
    
    @Deprecated
    Builder setHeader(Builder rb) {
        if (getURL().hasParameter(HEADER)) {
            for (String header : getURL().getParameterList(HEADER)) {
                String[] pair = header.split(":");
                if (pair.length >= 2) {
                    logger.info("header: " +pair[0] + " = " + pair[1]); 
                    rb.header(pair[0], pair[1]);
                }
            }
        }
        return rb;
    }
    
    // URL header Accept if any else mime 
    String getAccept(String mime) {
        String accept = getParamHeader(HEADER_ACCEPT);
        return (accept==null)?mime:accept;
    }
    
    String getParamHeader(String name) {
        if (getURL().hasParameter(HEADER)) {
            for (String header : getURL().getParameterList(HEADER)) {
                String[] pair = header.split(":");
                if (pair.length >= 2) {
                    if (pair[0].equals(name)) {
                        return pair[1];
                    }
                }
            }
        }
        return null;
    }
    

    ASTQuery mapping(Query q, Mapping m) {
        Mappings map = new Mappings();
        map.add(m);
        CompileService cs = new CompileService();
        ASTQuery ast = cs.filter(getURL(), q, map, 0, 1);
        return ast;
    }


    //   /sparql?format=json
    String getAccept() {
        String ft = getURL().getParameter(FORMAT);
        if (ft == null) {
            return MIME_TYPE;
        }
        return ResultFormat.decode(ft);
    }
    
    String accept(ASTQuery ast) {
        String ft = getURL().getParameter(FORMAT);
        if (ft == null) {
            return format(ast);
        }
        logger.info("Accept: "+ ft + " " + ResultFormat.decode(ft));
        return ResultFormat.decode(ft);
    }
        

    String format(ASTQuery ast) {
        if (ast.isConstruct() || ast.isDescribe()) {
            return RDF_XML;
        }
        return XML;
    }

    public String post(String query, String mime) {
        // Server URL without parameters
        return post(getURL().getServer(), query, mime);
    }
    
    
    Form getForm() {
        if (sendParameter()) {
            if  (getURL().getMap() == null) {
                if (getURL().getDataset() == null) {
                    return new Form(); 
                }
                else {
                   return new Form(getMap(new HashMapList<>())); 
                }
            }
            else {
                return new Form(getMap(getURL().getMap()));
            }
        } else {
            return new Form();
        }
    }
    
    MultivaluedMap<String, String> getMap(HashMapList<String> map) {
        MultivaluedMap<String, String> amap = new MultivaluedHashMap<>();
        for (String key : map.keySet()) {
            amap.put(key, map.get(key));
        }
        complete(amap, getURL().getDataset());
        return amap;
    }
    
    void complete(MultivaluedMap<String, String> amap, Dataset ds) {
        if (ds != null) {
            if (!ds.getFrom().isEmpty()) {
                logger.info(String.format("%s\n%s %s", getURL().toString(), DEFAULT_GRAPH_URI, ds.getFromStringList()));
                amap.put(DEFAULT_GRAPH_URI, ds.getFromStringList());
            }
            if (!ds.getNamed().isEmpty()) {
                logger.info(getURL().toString());
                logger.info(String.format("%s\n%s %s", getURL().toString(), NAMED_GRAPH_URI, ds.getNamedStringList()));
                amap.put(NAMED_GRAPH_URI, ds.getNamedStringList());
            }
        }
    }


    public String get(String query, String mime) {
        // Server URL without parameters
        return get(getURL().getServer(), query, mime);
    }

    public String get(String uri, String query, String mime) {        
        String url;
        try {
            if (isDebug()) {
                logger.info(URLEncoder.encode(query, "UTF-8"));
            }

            url = complete(uri, URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            url = complete(uri, query);
        }
        return getBasic(url, mime);
    }
    
    String complete(String uri, String query) {
        if (getURL().hasParameter() && sendParameter()) {
            return String.format("%s?%s&query=%s", uri, param(), query);
        }
        if (getURL().getDataset()!=null && !getURL().getDataset().isEmpty()) {
            return String.format("%s?%s&query=%s", uri, dataset(), query);
        }
        return String.format("%s?query=%s", uri, query);
    }
    
    boolean sendParameter() {
       return Property.booleanValue(SERVICE_SEND_PARAMETER) && ! getURL().hasParameter(MODE, LOCAL);
    }
    
    /**
     * Generate the parameter string
     * We can skip some key=val parameters here.
     */
    String param() {
        StringBuilder sb = new StringBuilder();
        for (String key : getURL().getMap().keySet()) {
            for (String value : getURL().getMap().get(key)) {
                String format = sb.length() == 0 ? "%s=%s" : "&%s=%s";
                sb.append(String.format(format, key, value));
            }
        }  
        complete(sb, getURL().getDataset());
        return sb.toString();
    }
    
    void complete(StringBuilder sb, Dataset ds) {
        if (ds != null && !ds.isEmpty()) {
            if (sb.length()>0) {
                sb.append("&");
            }
            sb.append(ds.getURLParameter());
        }
    }
    
    String dataset() {
        StringBuilder sb = new StringBuilder();
        complete(sb, getURL().getDataset());
        return sb.toString();
    }
        
    public String getBasic(String url, String mime) {
        Response resp = getResponse(url, mime);
        String res = resp.readEntity(String.class);
        trace(res);
        getCreateReport().setResult(res);
        return res;
    }
            
    public InputStream getStream(String url, String mime)  {
        Response resp = getResponse(url, mime);
        InputStream res = resp.readEntity(InputStream.class);
        return res;
    }

    // may take URL parameter into account:    ?format=rdfxml
    public InputStream load(String url, String mime)  {
        //logger.info("load accept format: " + url + " " + mime);
        if (LOAD_WITH_PARAMETER) {
            return getStream(getURL().getServer(), getFormat(mime));
        }
        return getStream(url, mime);
    }
    
    // may take URL parameter into account:    ?format=rdfxml
    String getFormat(String defaut) {
        String format = getURL().getParameter(URLServer.FORMAT);
        if (format != null) {
            // format=rdfxml | application/rdf+xml
            String res = ResultFormat.decodeLoadFormat(format);
            if (res != null) {
                // res = application/rdf+xml
                return res;
            }
        }
        return defaut;
    }
    
    Response getResponse(String url, String mime) {
        if (redirect.containsKey(url)) {
            return getResponse(redirect.get(url), mime);
        }
        else {
            return basicGetResponse(url, mime);
        }
    }
        
    Response basicGetResponse(String url, String mime) {
        logger.info("Service:  " + url + " " + mime);
        clientBuilder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
        Client client = clientBuilder.build();
        WebTarget target = client.target(url);
        Builder build = target.request(mime);
        //Builder build = target.request();
        Response resp = build.get();
        if (resp.getMediaType()!=null) {
            recordFormat(resp.getMediaType().toString());
        }
        getCreateReport().setResponse(resp);
        
        if (resp.getStatus() == Response.Status.SEE_OTHER.getStatusCode()                
         || resp.getStatus() == Response.Status.MOVED_PERMANENTLY.getStatusCode()) {
            String myUrl = resp.getLocation().toString();
            logger.warn(String.format("Service redirection: %s to: %s", url, myUrl));
            if (myUrl.equals(url)) {
                throw new RedirectionException(resp);
            }
            redirect(url, myUrl);
            return getResponse(myUrl, mime);
        }
        
        if (resp.getStatus() >= Response.Status.BAD_REQUEST.getStatusCode()) {
            String res = resp.readEntity(String.class);
            ResponseProcessingException ex = new ResponseProcessingException(resp, res);

            if (isLog() && getLog() != null) {
                // use case: @federate call not within ProviderService
                // log here
                getLog().addException(new EngineException(ex, ex.getMessage())
                        .setURL(getURL()).setObject(ex.getResponse()));
            }
            logger.error("Response status: " + resp.getStatus());
            logger.info("message: "+ res);
            throw ex;
        }
         
        return resp;
    }
    
    
    public Response get(String uri) {
        Client client = clientBuilder.build();
        WebTarget target = client.target(uri);
        Response res = target.request().get();
        return res;
    }
    
    public String getString(String uri) {
        return get(uri).readEntity(String.class);
    }
    
    public JSONObject getJson(String uri) {
        String text = getString(uri);
        if (text != null && ! text.isEmpty()) {
            return new JSONObject(text);
        }
        return null;
    }

    String encoding(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.ENCODING)) {
            return ast.getMetadata().getStringValue(Metadata.ENCODING);
        }
        return ENCODING;
    }


   
    public Access.Level getLevel() {
        return level;
    }

    
    public void setLevel(Access.Level level) {
        this.level = level;
    }

    
    public boolean isPost() {
        return post;
    }

    
    public void setPost(boolean post) {
        this.post = post;
    }

   
    public boolean isShowResult() {
        return showResult;
    }

    
    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    
    public boolean isTrap() {
        return trap;
    }

   
    public void setTrap(boolean trap) {
        this.trap = trap;
    }

    
    public URLServer getURL() {
        return url;
    }

   
    public void setURL(URLServer url) {
        this.url = url;
    }
   
    public String getFormat() {
        return format;
    }

    public String getFormatText() {
        return (getFormat()==null)?"undefined":getFormat();
    }
    
    public void setFormat(String format) {
        this.format = format; 
    }
    
    /**
     * format = text/turtle;charset=UTF-8
     */
    void recordFormat(String str) {
        //logger.info("Content type: " + str);
        String format = clean(str);
        setFormat(format);
        getCreateReport().setFormat(format);
        if (getParser()!=null) {
            getParser().setFormat(format);
        }
        if (getLog() != null) {
            getLog().getFormatList().add(format);
        }
    }
    
    String clean(String format) {
        if (format.contains(";")) {
            return format.substring(0, format.indexOf(";"));
        }
        return format;
    }

    
    public int getTimeout() {
        return timeout;
    }

    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    
    public int getCount() {
        return count;
    }

    
    public void setCount(int count) {
        this.count = count;
    }
    
    ASTQuery getAST(Query q) {
        return  q.getAST();
    }
    
    void metadata(ASTQuery ast) {
        limit(ast);
        if (getURL().isGET() || ast.getGlobalAST().hasMetadata(Metadata.GET)) {
            setPost(false);
        }
        if (getURL().hasParameter(MODE, SHOW)) {
            setShowResult(true);
        }
        if (ast.getGlobalAST().isDebug()) {
            System.out.println(isPost()?"POST":"GET");
        }
        getCreateParser().setTrap(getURL().hasParameter(MODE, TRAP) || ast.getGlobalAST().hasMetadata(Metadata.TRAP));
        if (! isShowResult()) {
            setShowResult(ast.getGlobalAST().hasMetadata(Metadata.SHOW));
            getCreateParser().setShowResult(isShowResult());        
        }
    }
    
    // use case for limit: @federate with one URL -> direct service
    void limit(ASTQuery ast) {
        if (!ast.hasLimit()) {
            if (ast.getMetaValue(Metadata.LIMIT)!=null) {
                ast.setLimit(ast.getMetaValue(Metadata.LIMIT).intValue());
            } else {
                Integer lim = getURL().intValue(LIMIT);
                if (lim != -1) {
                    ast.setLimit(lim);
                }
                else {
                    lim = Property.intValue(SERVICE_LIMIT);
                    if (lim != null) {
                        ast.setLimit(lim);
                    }
                }
            }
        }
    }

   
    public Binding getBind() {
        return bind;
    }

    
    public void setBind(Binding bind) {
        this.bind = bind;
    }

    
    public ServiceParser getParser() {
        return parser;
    }
    
    ServiceParser getCreateParser() {
        if (getParser() == null) {
            setParser(new ServiceParser(getURL()));
            getParser().setBind(getBind());
            getParser().setLog(isLog());
            getParser().setReport(getCreateReport());
        }
        return getParser();
    }

    
    public void setParser(ServiceParser parser) {
        this.parser = parser;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    // @todo: synchronized wrt ProviderService & ServiceParser
    public ContextLog getLog() {
        if (getBind() == null) {
            return null;
        }
        return  getBind().getCreateLog();
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public ServiceReport getReport() {
        return report;
    }

    public void setReport(ServiceReport report) {
        this.report = report;
    }
    
    public ServiceReport getCreateReport() {
        if (getReport() == null) {
            setReport(new ServiceReport());
        }
        return getReport();
    }
    
    public ServiceReport getCreateReport(Query q) {
        return getCreateReport().setQuery(q);
    }

    
}
