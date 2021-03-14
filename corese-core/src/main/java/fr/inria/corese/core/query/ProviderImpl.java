package fr.inria.corese.core.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.compiler.result.XMLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.SPARQLResult;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.util.URLServer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Implements service expression There may be local QueryProcess for some URI
 * (use case: W3C test case) Send query to sparql endpoint using HTTP POST query
 * There may be a default QueryProcess
 *
 * TODO: check use same ProducerImpl to generate Nodes ?
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderImpl implements Provider {

    private static final String DB = "db:";
    private static final String SERVICE_ERROR = "Service error: ";
    static Logger logger = LoggerFactory.getLogger(ProviderImpl.class);
    private static final String LOCAL_SERVICE = "http://example.org/sparql";
    static final String LOCALHOST = "http://localhost:8080/sparql";
    static final String LOCALHOST2 = "http://localhost:8090/sparql";
    static final String DBPEDIA = "http://fr.dbpedia.org/sparql";
    HashMap<String, QueryProcess> table;
    Hashtable<String, Double> version;
    private QueryProcess defaut;
    private int limit = 30;

    private ProviderImpl() {
        table = new HashMap<String, QueryProcess>();
        version = new Hashtable<String, Double>();
    }

    public static ProviderImpl create() {
        ProviderImpl p = new ProviderImpl();
        p.set(LOCALHOST, 1.1);
        p.set(LOCALHOST2, 1.1);
        p.set("https://data.archives-ouvertes.fr/sparql", 1.1);
        p.set("http://corese.inria.fr/sparql", 1.1);
        return p;
    }
    
    public static ProviderImpl create(QueryProcess exec) {
        ProviderImpl pi = ProviderImpl.create();
        pi.setDefault(exec);
        return pi;
    }

    @Override
    public void set(String uri, double version) {
        this.version.put(uri, version);
    }

    @Override
    public boolean isSparql0(Node serv) {
        if (serv.getLabel().startsWith(LOCALHOST)) {
            return false;
        }
        Double f = version.get(serv.getLabel());
        return (f == null || f == 1.0);
    }

    /**
     * Define a QueryProcess for this URI
     */
    public void add(String uri, Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        table.put(uri, exec);
    }

    /**
     * Define a default QueryProcess
     */
    public void add(Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        setDefault(exec);
    }

    /**
     * If there is a QueryProcess for this URI, use it Otherwise send query to
     * spaql endpoint If endpoint fails, use default QueryProcess if it exists
     * When service URL is a constant or a bound variable, serv = URL
     * otherwise serv = NULL
     */
    @Override
    public Mappings service(Node serv, Exp exp, Mappings lmap, Eval eval) 
            throws EngineException {
        Binding b = (Binding) eval.getEnvironment().getBind();
        if (Access.reject(Feature.SPARQL_SERVICE, b.getAccessLevel())) {
                throw new SafetyException(TermEval.SERVICE_MESS);
        }
        Mappings map = serviceBasic(serv, exp, lmap, eval);
//        System.out.println("service result");
//        System.out.println(map.toString(true, true, 10));
        return map;
    }
    
    /**
     * exp: service statement
     */
    public Mappings serviceBasic(Node serv, Exp exp, Mappings lmap, Eval eval) 
            throws EngineException
    {
        Query qq = eval.getEnvironment().getQuery();
        Exp body = exp.rest();
        // select query inside service statement
        Query q = body.getQuery();
        
        QueryProcess exec = null ;
        
        if (serv != null) {
            exec = table.get(serv.getLabel());
        }

        if (exec == null) {
            
            Mappings map = globalSend(serv, q, exp, lmap, eval);
           
            if (map != null) {
                return map;
            }

            if (getDefault() == null) {
                map = Mappings.create(q);
                if (q.isSilent()) {
                    map.add(Mapping.create());
                }
                return map;
            } else {
                exec = getDefault();
            }
        }

        ASTQuery ast = exec.getAST(q);
        Mappings map;
        try {
            map = exec.query(ast);
            return map;
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return new Mappings();
    }
    
    Graph getGraph(Producer p) {
        return (Graph) p.getGraph();
    }

    /**
     * serv: servive variable/URI, null when variable is unbound
     * q: select where query inside service statement
     * exp: service statement
     * map: current mappings for service candidate variable bindings
     */
    Mappings globalSend(Node serv, Query q, Exp exp, Mappings map, Eval eval) throws EngineException {
        CompileService compiler = new CompileService(this);
        // share prefix
        compiler.prepare(q);
        int slice = getSlice(q, serv, eval.getEnvironment(), map); 
        ASTQuery ast = getAST(q);
        //boolean hasValues = ast.getValues() != null;
        boolean skipBind = ast.getGlobalAST().hasMetadata(Metadata.BINDING, Metadata.SKIP_STR);
        Mappings res = null;
        Graph g = getGraph(eval.getProducer());
        // when servive variable is unbound, get service URL (list) from Environment or Mappings
        List<Node> serviceList = getServerList(exp, map, eval.getEnvironment());
        boolean bslice = ! (map == null || slice <= 0  || skipBind); // || hasValues);
        res = sliceSend(serviceList, g, compiler, serv, q, exp, (skipBind) ? null : map, eval, bslice, slice);
        restore(ast);
        return res;
    }
    
    void restore(ASTQuery ast) {
        if (ast.getSaveBody() != null) {
            ast.setBody(ast.getSaveBody());
        }
    }
    
    
    /**
     * Generalized service clause with possibly several service URIs
     * If service is unbound variable, retrieve service URI in Mappings 
     * Take Mappings variable binding into account when sending service
     * Split Mappings into buckets with size = slice
     * Iterate service on each bucket
     * When several services, they are evaluated in parallel by default, unless @sequence metadata
     * When several services, return *distinct* Mappings, unless @duplicate metadata.
     */
    Mappings sliceSend(List<Node> serverList, Graph g, CompileService compiler, Node serviceNode, Query q, Exp exp, Mappings map, Eval eval, boolean slice, int length) throws EngineException {
        
        g.getEventManager().start(Event.Service, serverList);
       
        if (serverList.isEmpty()) {
            logger.error("Undefined service: " + exp.getServiceNode());
        }

        ArrayList<Mappings> mapList     = new ArrayList<>();
        ArrayList<ProviderThread> pList = new ArrayList<>();
        int timeout = getTimeout(q, serviceNode, eval.getEnvironment());
        // by default in parallel 
        boolean parallel = q.getOuterQuery().isParallel();
        Binding b = (Binding) eval.getEnvironment().getBind();
        
        for (Node service : serverList) {
            URLServer url = new URLServer(service);
            
            if (Access.reject(Feature.SPARQL_SERVICE, b.getAccessLevel(), service.getLabel())) {
                throw new SafetyException(TermEval.SERVICE_MESS, service.getLabel());
            }
            if (eval.isStop()) {
                break;
            }
            g.getEventManager().process(Event.Service, service);

            Mappings input = map;
            
            if (slice) {
                // select appropriate subset of distinct Mappings with service URI 
                input = getMappings(q, exp, exp.getServiceNode(), service, map, eval.getEnvironment());
                if (input.size() > 0) {
                    g.getEventManager().process(Event.Service, "input: \n" + input.toString(true, false, 10));
                }
                else {
                    g.getEventManager().process(Event.Service, "no input" );
                }
            }
            
            Mappings sol = new Mappings();
            mapList.add(sol);
            
            // draft: local slice
            //int mySlice = url.intValue("slice");
            
            if (parallel) {
                ProviderThread p = parallelProcess(q, url, exp, input, sol, eval, compiler, slice, length, timeout);
                pList.add(p);
            }
            else {
                process(q, url, exp, input, sol, eval, compiler, slice, length, timeout);
            }
        }
        
        // Wait for parallel threads to stop
        for (ProviderThread p : pList) {
            try {
                p.join(timeout);
            } catch (InterruptedException ex) {
                logger.warn(ex.toString());
            }
        }
        
        Mappings res = getResult(q, mapList);
        
        if (serverList.size() > 1) {
            eval.getVisitor().service(eval, DatatypeMap.toList(serverList), exp, res);
        }
        g.getEventManager().finish(Event.Service, "result: " + ((res!= null) ? res.size(): 0));
        return res;
    }
    
    /**
     * Execute service in a parallel thread 
     */
    ProviderThread parallelProcess(Query q, URLServer service, Exp exp, Mappings map, Mappings sol, Eval eval, CompileService compiler, boolean slice, int length, int timeout) {
        ProviderThread thread = new ProviderThread(this, q, service, exp, map, sol, eval, compiler, slice, length, timeout);
        thread.start();
        return thread;
    }
       
    /**
     * Execute one service with possibly input Mappings map and possibly slicing map into packet of size length
     * Add results into Mappings sol which is empty when entering
     * Several such process may run in parallel in case of several service URL
     */
    void process(Query q, URLServer service, Exp exp, Mappings map, Mappings sol, Eval eval, CompileService compiler, boolean slice, int length, int timeout) throws EngineException {
        int size = 0;
        if (slice) {
            while (size < map.size()) {
                if (eval.isStop()) {
                    break;
                }
                // consider subset of Mappings of size slice
                // it may produce bindings for target service
                Mappings res = send(compiler, service, q, map, eval.getEnvironment(), size, size + length, timeout);
                // join (serviceNode = serviceURI)
                complete(exp.getServiceNode(), service.getNode(), res, eval.getEnvironment());
                addResult(sol, res);
                size += length;
            }
        } else {
            Mappings res = send(compiler, service, q, map, eval.getEnvironment(), 0, 0, timeout);
            // join (serviceNode = serviceURI)
            complete(exp.getServiceNode(), service.getNode(), res, eval.getEnvironment());
            addResult(sol, res);
        }

        synchronized (eval.getEnvironment().getBind()) {
            eval.getVisitor().service(eval, service.getNode(), exp, sol);
        }
    }
    

    /**
     * Send query to sparql endpoint using HTTP request
     * Generate variable binding from map or env if any and possibly modify the AST 
     * with these bindings (filter by default or values)
     * Handle annotation @binding kg:values kg:filter 
     */
    Mappings send(CompileService compiler, URLServer serv, Query q, Mappings map, Environment env, int start, int limit, int timeout) throws EngineException {
        Query gq = q.getGlobalQuery();
        // use case: ldscript nested query
        boolean debug = q.isRecDebug();       
        try {

            // oririnal ast
            ASTQuery aa = getAST(q);
            // ast possibly modified with variable bindings from map/env 
            ASTQuery ast = compiler.compile(serv, q, map, env, start, limit);
            if (aa == ast) {
                // no binding
                if (start > 0) {
                    // this is not the first slice and there is no more bindings: skip it
                    if (debug) {
                        logger.info("Skip slice for absence of relevant binding");
                    }
                    return Mappings.create(q);
                }               
            }

            if (debug) {
                logger.info(String.format("** Service %s \n%s", serv, ast));
            }
            Mappings res = eval(q, ast, serv, env, timeout);
            
            if (debug) {
                trace(serv, res);
            }
            if (res != null && res.isError()) {
                logger.info("Parse error in result of: " + serv.getURL());
            }
            return res;
        } catch (IOException e) {
            logger.error(q.getAST().toString(), e);
            gq.addError(SERVICE_ERROR, e);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        if (gq.isDebug()) {
            logger.info("** Service error");
        }
        return null;
    }
    
    ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }
    
    void trace(URLServer serv, Mappings res) {
        if (res.size() > 0) {
            logger.info(String.format("** Service %s result: \n%s", serv, res.toString(false, false, 10)));
        } else {
            logger.info(String.format("** Service %s result size: %s", serv, res.size()));
        }
    }
    
    void addResult(Mappings sol, Mappings res) {
        if (res != null) {
            sol.add(res);
            if (sol.getQuery() == null) {
                sol.setQuery(res.getQuery());
                sol.init(res.getQuery());
            }
        }
    }
    
    /**
     * Return final result Mappings
     * mapList is the list of result Mappings of each service
     * When there are *several* services, return *distinct* Mappings
     * unless @duplicate metadata 
     */
    Mappings getResult(Query q, List<Mappings> mapList) {
        if (mapList.size() == 1) {
            return mapList.get(0);
        }
        Mappings res   = null;
        int n = 0;
        
        for (Mappings m : mapList) {
            if (res == null) {
                res = m;
            }
            else {
                res.add(m);
            }
        }
        ASTQuery ast = getAST(q.getGlobalQuery());
        boolean distinct = ! ast.hasMetadata(Metadata.DUPLICATE);
        // TODO: if two Mappings have their own duplicates, they are removed
        if (res != null && res.getSelect() != null && distinct){
            res = res.distinct(res.getSelect(), res.getSelect());
        }
        return res;
    }
       
    /**
     * service ?s { BGP }
     * When ?s is unbound, join (?s = URI) to Mappings, reject those that are incompatible 
     * TODO: optimize map.join()
     */
    void complete(Node serviceNode, Node serviceURI, Mappings map, Environment env){
        if (map != null && serviceNode.isVariable() && ! env.isBound(serviceNode)) {
           map.join(serviceNode, serviceURI);
        }
    }
    
    /**
     * Select subset of distinct Mappings where serviceNode = serviceURI
     */
    Mappings getMappings(Query q, Exp exp, Node serviceNode, Node serviceURI, Mappings map, Environment env) {
        if (exp.isGenerated()) {
            // generated by federated compiler
            return map.getMappings(q);
        }
        if (serviceNode.isVariable() && ! env.isBound(serviceNode)) {
            // service var is not bound ; map defines values for var
            // select Mapping in map where var = uri
           return map.getMappings(q, serviceNode, serviceURI);
        }
        return map;
    }
    
    /**
     * 
     * Determine service URIs
     */
    List<Node> getServerList(Exp exp, Mappings map, Environment env) {
        if (exp.getNodeSet() == null) {
            Node serviceNode = exp.getServiceNode();
            List<Node> list = new ArrayList<>();
            if (serviceNode.isVariable()) {
                Node value = env.getNode(serviceNode);
                if (value == null){
                    return getServerList(serviceNode, map);
                }
                else {
                    list.add(value);
                }
            }
            else {
                list.add(serviceNode);
            }
            return list;
        } else {
            // service <uri1> <uri2> {}
            return exp.getNodeSet();
        }
    }
    
    /**
     * service ?s { }
     * Retrieve service URIs for ?s in Mappings
     */
    List<Node> getServerList(Node serviceNode, Mappings map) {
        if (map == null) {
            logger.error("Unbound variable: " + serviceNode);
            return new ArrayList<>();
        }
        return map.aggregate(serviceNode);
    }
    
    
    int getTimeout(Query q, Node serv, Environment env) {
        Integer time = (Integer) q.getGlobalQuery().getPragma(Pragma.TIMEOUT);
        if (time == null) {
            return env.getEval().getVisitor().timeout(serv);
        }
        return time;
    }
    
    int getSlice(Query q, Node serv, Environment env, Mappings map) {
        // former: 
        q.getGlobalQuery().getSlice();
        int slice = env.getEval().getVisitor().slice(serv, map==null?Mappings.create(q):map);
        Binding bind = (Binding) env.getBind();
        IDatatype dt = bind.getGlobalVariable(Binding.SLICE_SERVICE);
        if (dt == null) {
            return slice;
        }
        return dt.intValue();
    }
    
    Mappings eval(Query q, ASTQuery ast, URLServer serv, Environment env, int timeout) throws IOException, ParserConfigurationException, SAXException, EngineException {
        if (isDB(serv.getNode())){
            return db(q, serv.getNode());
        }
        if (serv.getServer().equals(LOCAL_SERVICE)) {
            return getDefault().query(ast);
        }
        return send(q, ast, serv, env, timeout);
    }
    
    /**
     * service <db:/tmp/human_db> { GP }
     * service overloaded to query a database
     */
    Mappings db(Query q, Node serv) throws EngineException{
        QueryProcess exec = QueryProcess.dbCreate(Graph.create(), true, QueryProcess.DB_FACTORY, serv.getLabel().substring(DB.length()));
        return exec.query(getAST(q));
    }
    
    boolean isDB(Node serv){
        return serv.getLabel().startsWith(DB);
    }
    
    Mappings send(Query q, ASTQuery ast, URLServer serv, Environment env, int timeout) 
            throws IOException, ParserConfigurationException, SAXException {
        ASTQuery aa =  getAST(q.getGlobalQuery());
        if (aa.hasMetadata(Metadata.OLD_SERVICE)) {
            return post1(q, ast, serv, env, timeout);        
        }
        else {
            return post2(q, ast, serv, env, timeout);
        }
    }
    
    Mappings post1(Query q, ASTQuery ast, URLServer serv, Environment env, int timeout) 
            throws IOException, ParserConfigurationException, SAXException {
        ASTQuery aa  = getAST(q.getOuterQuery());
        boolean trap = ast.isFederate() || ast.getGlobalAST().hasMetadata(Metadata.TRAP);
        boolean show = ast.getGlobalAST().hasMetadata(Metadata.SHOW);
        String query = ast.toString();
        Binding b = (Binding) env.getBind();
        InputStream stream = doPost(aa.getMetadata(), serv.getURL(), query, timeout, b.getAccessLevel()); 
        return parse(stream, trap, show);
    }
    
    
    Mappings post2(Query q, ASTQuery ast, URLServer serv, Environment env, int timeout) throws IOException {
        try {
            Binding b = (Binding) env.getBind();
            Service service = new Service(serv) ;
            service.setLevel(b.getAccessLevel());
            Mappings map = service.query(q, ast, null);
            return map;
        } catch (LoadException ex) {
            throw (new IOException(ex.getMessage() + " " + serv.getURL()));
        }
    }

    /**
     * ********************************************************************
     *
     * SPARQL Protocol client
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    Mappings parse(StringBuffer sb) throws ParserConfigurationException, SAXException, IOException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = XMLResult.create(p);
        Mappings map = r.parseString(sb.toString());
        return map;
    }

    Mappings parse(InputStream stream, boolean trap, boolean show) throws ParserConfigurationException, SAXException, IOException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = SPARQLResult.create(p);
        r.setTrapError(trap);
        r.setShowResult(show);
        Mappings map = r.parse(stream);
        return map;
    }

    public StringBuffer doPost2(Metadata meta, String server, String query) throws IOException {
        URLConnection cc = post(meta, server, query, 0, Level.DEFAULT);
        return getBuffer(cc.getInputStream());
    }

    public InputStream doPost(Metadata meta, String server, String query, int timeout, Level level) throws IOException {
        URLConnection cc = post(meta, server, query, timeout, level);
        return cc.getInputStream();
    }

    URLConnection post(Metadata meta, String server, String query, int timeout, Level level) throws IOException {
        URLServer url = new URLServer(server);
        String param = url.getParam();
        server = url.getServer();
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");
        if (param !=null) {
            // check param != access=...
            qstr = param.concat("&").concat(qstr);
        }
//        if (level.equals(Level.USER)) {
//            qstr = "access=".concat(level.toString()).concat("&").concat(qstr);
//        }
        List<String> graphList = getGraphList(server, meta);
        qstr = complete(qstr, server, graphList);
        URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Accept", "application/sparql-results+xml,application/rdf+xml");
        urlConn.setRequestProperty("Accept-Charset", "UTF-8");
        urlConn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));
        urlConn.setReadTimeout(timeout);

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();

        return urlConn;
    }
    
    
    // default-graph-uri
    String complete(String qstr, String server, List<String> graphList) {
        if (!graphList.isEmpty()) {
            //System.out.println("Federate: " + server + " " + graphList);
            String graph="";
            for (String name : graphList) {
                graph += "&default-graph-uri=" + name;
            }
            qstr += graph;
        }
        return qstr;
    }
    
    List<String> getGraphList(String server, Metadata meta) {
        if (meta == null) {
            return new ArrayList<>(0);
        }
        return meta.getGraphList(server);
    }

    StringBuffer getBuffer(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();
        String str = null;

        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }

        return sb;
    }
//	public String callSoapEndPoint() {
//		SparqlSoapClient client = new SparqlSoapClient();
//		SparqlResult result = client.sparqlQuery("http://dbpedia.inria.fr/sparql", "select ?x ?r ?y where { ?x ?r ?y} limit 100");
//		String stringResult = result.toString();
//		return stringResult;
//	}
//
//	public static void main(String[] args) {
//		ProviderImpl impl = new ProviderImpl();
//		System.out.println(impl.callSoapEndPoint());
//	}

    /**
     * @return the defaut
     */
    public QueryProcess getDefault() {
        return defaut;
    }

    /**
     * @param defaut the defaut to set
     */
    public void setDefault(QueryProcess defaut) {
        this.defaut = defaut;
    }
}
