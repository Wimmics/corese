package fr.inria.corese.server.webservice;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.print.LogManager;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class TripleStore implements URLParam {
    private static final String LOG_DIR = "/log/";

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(QueryProcess.class);
    static HashMap<String, Integer> metaMap ;
    GraphStore graph = GraphStore.create(false);
    //QueryProcess exec;// = QueryProcess.create(graph);
    boolean rdfs = false;
    boolean owl = false;
    private boolean match   = false;
    private boolean protect = false;
    private String name = Manager.DEFAULT;
    
    static {
       init();
    }
    
    static void init() {
        metaMap = new HashMap<>();
        metaMap.put(URLParam.SPARQL, Metadata.SPARQL);
        metaMap.put(TRACE, Metadata.TRACE);
        metaMap.put(PROVENANCE, Metadata.VARIABLE);
        metaMap.put("index", Metadata.INDEX);
        metaMap.put("move", Metadata.MOVE);
        metaMap.put("count", Metadata.SERVER);
    }

    TripleStore(boolean rdfs, boolean owl) {
       this(rdfs, owl, true);
    }
    
    
    TripleStore(boolean rdfs, boolean owl, boolean b) {
        graph = GraphStore.create(rdfs);
        init(graph);
        setMatch(b);
        //exec  = QueryProcess.create(graph, b);
        this.owl = owl;
    }
    
    TripleStore(GraphStore g){
        graph = g;
        init(g);
        setMatch(false);
        //exec = QueryProcess.create(g);
    }
    
    TripleStore(GraphStore g, boolean b){
        graph = g;
        init(g);
    }
    
    void init(GraphStore g){
        if (EmbeddedJettyServer.isDebug()) {
            g.setVerbose(true);
        }
    }
    
    void finish(boolean b){
        setMatch(true);
        //exec = QueryProcess.create(graph, true);
        init(b);
    }
    
    @Override
    public String toString(){
        return graph.toString();
    }
    
    QueryProcess getQueryProcess(){
        QueryProcess exec = QueryProcess.create(graph, isMatch());
        return exec;
    }
    
    GraphStore getGraph(){
        return graph;
    }
    
    void setGraph(GraphStore g){
        graph = g;
    }
    
     void setGraph(Graph g){
         if (g instanceof GraphStore){
            graph = (GraphStore) g;
         }
    }
   
    
    void setOWL(boolean b){
        owl = b;
    }
    
    void init(boolean b) {
        setProtect(b);

        if (rdfs) {
            logger.info("Endpoint successfully reset with RDFS entailments.");
        }

        if (owl) {
            RuleEngine re = RuleEngine.create(graph);
            re.setProfile(RuleEngine.OWL_RL);
            graph.addEngine(re);
            if (owl) {
                logger.info("Endpoint successfully reset with OWL RL entailments.");
            }

        }
        
    }
    
    
    void load(String[] load) {
        Load ld = Load.create(graph);
        for (String f : load) {
            try {
                logger.info("Load: " + f);
                //ld.loadWE(f, f, Load.TURTLE_FORMAT);
                ld.parse(f, Load.TURTLE_FORMAT);
            } catch (LoadException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    void load(String path, String src) throws LoadException{
        Load ld = Load.create(graph);
        ld.parse(path, src, Load.TURTLE_FORMAT);
    }
    
    /**
     * Extended SPARQL Endpoint
     * 
     * SPARQL endpoint:    /sparql?query=
     * Federated endpoint: /federate?query=
     * Load and evaluate shape, execute query on shacl validation report
     * SHACL endpoint:     /sparql?mode=shacl&uri=shape&query=select * where { ?sh sh:conforms ?b }
     */
    Mappings query(HttpServletRequest request, String query, Dataset ds) throws EngineException {
        if (ds.getCreateContext().hasValue(TRACE)) {
            trace(request);
        }
        if (ds == null) {
            ds = new Dataset();
        }
        Context c = ds.getCreateContext();
        c.setService(getName());
        c.setUserQuery(true);
        c.setRemoteHost(request.getRemoteHost());
        c.set(REQUEST, DatatypeMap.createObject(request));
        String platform = getCookie(request, PLATFORM);
        if (platform != null) {
            // calling platform
            c.set(PLATFORM, platform);
        }
        Profile.getEventManager().call(ds.getContext());
        QueryProcess exec = getQueryProcess();
        exec.setDebug(c.isDebug());
        
        Mappings map;
        try {
            before(exec, query, ds);
            
            if (isFederate(ds)) {
                // federate sparql query with @federate uri
                if (isCompile(c)) {
                    Query qq = exec.compile(federate(query, ds), ds);
                    map = logCompile(exec);
                } else {
                    map = exec.query(federate(query, ds), ds);
                    log(exec, map, ds.getContext());
                }
            } else if (isShacl(c)) {
                map = shacl(query, ds);
            } else if (isConstruct(c)) {
                map = construct(query, ds);
            } else {
                map = exec.query(query, ds);
                log(exec, map, ds.getContext());
            }
            
            after(exec, query, ds);
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
        return map;
    }
    
    Mappings logCompile(QueryProcess exec) {
        ContextLog log = exec.getLog();
        Mappings map     = log.getSelectMap();
        ASTQuery select  = log.getASTSelect();
        ASTQuery rewrite = log.getAST();
        String uri1 = document(select.toString(),  "select");
        String uri2 = document(rewrite.toString(), "rewrite");
        map.addLink(uri1);
        map.addLink(uri2);
        return map;
    }
    
    /**
     * Generate RDF error report, write it in /log/
     * generate an URL for report and set URL as Mappings link
     */
    void log(QueryProcess exec, Mappings map, Context ct) {
        if (ct.hasValue(PROVENANCE) || ct.hasValue(LOG)) {
            if (exec.getLog(map).isEmpty()) {
               System.out.println("log is empty" );
            }
            else {
                LogManager log = new LogManager(exec.getLog(map));
                String uri = document(log.toString(), "log", ".ttl");
                map.addLink(uri);               
                System.out.println("server report: " + uri);
            }
        }
    }
    
    /**
     * Save content as document in HTTP server, return URL for this document 
     */
    static String document(String str) {
        return document(str, "", "");
    }

    static String document(String str, String name) {
        return document(str, name, "");
    }
    
    static String document(String str, String name, String ext) {
        String home = EmbeddedJettyServer.resourceURI.getPath() + LOG_DIR;
        String id = UUID.randomUUID().toString().concat(ext);
        if (name != null && !name.isEmpty()){
            id = name.concat("-").concat(id);
        }
        QueryLoad ql = QueryLoad.create();
        ql.write(home + id, str);
        String uri;
        try {
            uri = Profile.getLocalhost();
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage());
            uri = Profile.stdLocalhost();
        }
        uri += LOG_DIR + id;
        return uri;
    }
    
    void before(QueryProcess exec, String query, Dataset ds) throws LoadException, EngineException {
        if (isBefore(ds.getContext())) {
            IDatatype dt = getBefore(ds.getContext());
            QueryLoad ql = QueryLoad.create();
            ql.setAccessRight(ds.getContext().getAccessRight());
            String str = ql.readWithAccess(dt.getLabel());
            System.out.println("TS: before: " + str);
            Mappings map = exec.query(str, ds);
        }
    }
    
    void after(QueryProcess exec, String query, Dataset ds) throws LoadException, EngineException {
        if (isAfter(ds.getContext())) {
            IDatatype dt = getAfter(ds.getContext());
            QueryLoad ql = QueryLoad.create();
            ql.setAccessRight(ds.getContext().getAccessRight());
            String str = ql.readWithAccess(dt.getLabel());
            System.out.println("TS: after: " + str);
            Mappings map = exec.query(str, ds);
        }
    }
    
    
    IDatatype getBefore(Context c) {
        return c.get(URI).get(0);
    }
    
    IDatatype getAfter(Context c) {
        return c.get(URI).get(c.get(URI).size()-1);
    }
    
    boolean isParse(Context c) {
        return c.hasValue(PARSE);
    }
    
    boolean isCompile(Context c) {
        return c.hasValue(COMPILE);
    }
    
    boolean isBefore(Context c) {
        return c.hasValue(BEFORE) && hasValueList(c, URI);
    }
    
    boolean isAfter(Context c) {
        return c.hasValue(AFTER) && hasValueList(c, URI);
    }
    
    boolean isShacl(Context c) {
        return c.hasValue(SHACL) && hasValueList(c, URI);
    }
    
    boolean isConstruct(Context c) {
        return c.hasValue(CONSTRUCT);
    }
    
    boolean hasValueList(Context c, String name) {
        return c.hasValue(name) && c.get(name).isList() && c.get(name).size()>0;
    }
    
    // federate?query=select where {}
    boolean isFederate(Dataset ds) {
        Context c = ds.getContext();
        return (c.hasValue(FEDERATE)) && 
                ds.getUriList() != null && !ds.getUriList().isEmpty();
    }
    
    /**
     * sparql?mode=shacl&uri=shape&query=select * where { ?s sh:conforms ?b }
     * Load shacl shape
     * Evaluate shape
     * Execute query on shacl validation report
     */ 
    Mappings shacl(String query, Dataset ds) throws EngineException {
        Graph shacl = Graph.create();
        Load ld = Load.create(shacl);
        try {
            for (IDatatype dt : ds.getContext().get(URI)) {
                ld.parse(dt.getLabel());
            }
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
            throw new EngineException(ex) ;
        }
        Shacl sh = new Shacl(getGraph());
        Graph res = sh.eval(shacl);
        QueryProcess exec = QueryProcess.create(res);
        exec.setDebug(ds.getContext().isDebug());
        Mappings map = exec.query(query);
        return map;
    }
    
    Mappings construct(String query, Dataset ds) throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            for (IDatatype dt : ds.getContext().get(URI)) {
                ld.parse(dt.getLabel());       
            }
            g.init();
            Mappings map = new Mappings();
            Query q = getQueryProcess().compile(query);
            map.setGraph(g);
            map.setQuery(q);
            return map;
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
            throw new EngineException(ex) ;
        }
    }
    
    ASTQuery federate(String query, Dataset ds) throws EngineException {
        QueryProcess exec = getQueryProcess();
        ASTQuery ast = exec.parse(query, ds);
        ast.setAnnotation(metadata(ast, ds));
        return ast;
    }
    
    /**
     * SPARQL query executed as federated query on a federation of endpoints 
     * Generate appropriate Metadata for AST with federation information
     */
    Metadata metadata(ASTQuery ast, Dataset ds) {
        Metadata meta = new Metadata();
        // one URI: URI of federation
        // several URI: list of endpoint URI
        int type = (ds.getUriList().size() > 1) ? Metadata.FEDERATE : Metadata.FEDERATION;
        meta.set(type, ds.getUriList());
                
        for (String key : metaMap.keySet()) {
            if (ds.getContext().hasValue(key)) {
                meta.add(metaMap.get(key));
            }
        }
        
        return meta;
    }
    
    
    
    String getCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cook : req.getCookies()) {
            if (cook.getName().equals(name)) {
                return cook.getValue();
            }
        }
        return null;
    }
    
    void trace(HttpServletRequest request) {
        System.out.println("Endpoint HTTP Request");
        if (request.getCookies() != null) {
            for (Cookie cook : request.getCookies()) {
                System.out.println("cookie: " + cook.getName() + " " + cook.getValue()); // + " " + cook.getPath());
            }
        }
        Enumeration<String> enh =  request.getHeaderNames();
        while (enh.hasMoreElements()) {
            String name = enh.nextElement();
            System.out.println("header: " + name + ": " + request.getHeader(name));
        }
        for (String name : request.getParameterMap().keySet()) {
            System.out.println("param: " + name + "=" + request.getParameter(name));
        }
    }
    
    Mappings query(HttpServletRequest request, String query) throws EngineException{
        return query(request, query, new Dataset());
    }

    /**
     * @return the match
     */
    public boolean isMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public void setMatch(boolean match) {
        this.match = match;
    }

    /**
     * @return the protect
     */
    public boolean isProtect() {
        return protect;
    }

    /**
     * @param protect the protect to set
     */
    public void setProtect(boolean protect) {
        this.protect = protect;
    }
}
