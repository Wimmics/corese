package fr.inria.corese.core.query;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ResponseProcessingException;

/**
 * Service call
 * 
 * @author Olivier Corby, Edelweiss INRIA 2021
 */
public class ProviderService implements URLParam {
    
    static Logger logger = LoggerFactory.getLogger(ProviderService.class);
    private static final String LOCAL_SERVICE = "http://example.org/sparql";
    private static final String SERVICE_ERROR = "Service error: ";
    private static final String DB = "db:";
    
    private QueryProcess defaut;
    private ProviderImpl provider;
    private Query query;
    private ASTQuery globalAST;
    private ASTQuery ast;
    private Exp serviceExp;
    private Mappings mappings; 
    private Eval eval;
    private CompileService compiler;
    private Binding binding;

    /**
     * 
     * @param p
     * @param q query inside service statement
     * @param map
     * @param eval 
     */
    ProviderService(ProviderImpl p, Query q, Mappings map, Eval eval) {
        setProvider(p);
        setQuery(q);
        setGlobalAST(getAST(q.getGlobalQuery()));
        setAST(getAST(q));
        setMappings(map);
        setEval(eval);
        // after setEval:
        setBinding((Binding) getEnvironment().getBind());
        // after setBinding:
        init();
    }
    
    void init() {
        setCompiler(new CompileService(this, getEnvironment()));
        // federate visitor may have recorded data in AST Context
        // share it with Binding Context Log
        getLog().share(getGlobalAST().getLog());
    }
    
    /**
     * serv: servive variable/URI, null when variable is unbound
     * exp: service statement
     */
    Mappings send(Node serv, Exp exp) throws EngineException {
        setServiceExp(exp);
        // share prefix
        int slice = getSlice(serv, getMappings()); 
        //boolean hasValues = ast.getValues() != null;
        boolean skipBind = getGlobalAST().hasMetadata(Metadata.BINDING, Metadata.SKIP_STR);
        // when servive variable is unbound, get service URL (list) from Environment or Mappings
        List<Node> serviceList = getServerList(exp, getMappings());
        boolean bslice = ! (getMappings() == null || slice <= 0  || skipBind); 
        // slice by default
        Mappings res = send(serviceList, serv, (skipBind) ? null : getMappings(), bslice, slice);
        restore(getAST());
        if (res != null) {
            res.limit(getAST().getLimit());
        }
        return res;
    }
    
    void restore(ASTQuery ast) {
        if (ast.getSaveBody() != null) {
            ast.setBody(ast.getSaveBody());
        }
    }
    
    Graph getGraph(Producer p) {
        return (Graph) p.getGraph();
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
    Mappings send(List<Node> serverList, Node serviceNode, Mappings map, boolean slice, int length) throws EngineException {
        Graph g = getGraph(getEval().getProducer());
        Query q = getQuery();
        g.getEventManager().start(Event.Service, serverList);
       
        if (serverList.isEmpty()) {
            logger.error("Undefined service: " + getServiceExp().getServiceNode());
        }

        ArrayList<Mappings> mapList     = new ArrayList<>();
        ArrayList<ProviderThread> pList = new ArrayList<>();
        int timeout = getTimeout(serviceNode);
        // by default in parallel 
        boolean parallel = q.getOuterQuery().isParallel(); 
        
        for (Node service : serverList) {
            URLServer url = new URLServer(service);
            url.setNumber(getServiceExp().getNumber());
            // /sparql?param={?this} 
            // get ?this=value in Binding global variable and set param=value
            url.complete(getBinding());
            url.encode();
            //getLog().addURL(url);
            getLog().add(LogKey.ENDPOINT, url.getLogURL());
            
            if (Access.reject(Feature.SPARQL_SERVICE, getBinding().getAccessLevel(), service.getLabel())) {
                logger.error(TermEval.SERVICE_MESS + " " + service.getLabel());
                SafetyException ex = new SafetyException(TermEval.SERVICE_MESS, service.getLabel());
                getLog().addException(ex.setURL(url));
                throw ex;
            }
            
            if (eval.isStop()) {
                break;
            }
            g.getEventManager().process(Event.Service, service);

            Mappings input = map;
            
            if (slice) {
                // default behaviour when map != null
                // select appropriate subset of distinct Mappings with service URL 
                input = getMappings(q, getServiceExp(), getServiceExp().getServiceNode(), service, map);
                if (input.size() > 0) {
                    g.getEventManager().process(Event.Service, "input: \n" + input.toString(true, false, 10));
                }
                else {
                    g.getEventManager().process(Event.Service, "no input" );
                }
            }
            
            Mappings sol = new Mappings();
            mapList.add(sol);
            
            // sparql?slice=20
            length = url.intValue(SLICE, length);
            // sparql?timeout=123
            timeout = url.intValue(TIMEOUT, timeout);
            
            if (parallel) {
                ProviderThread p = parallelProcess(url, input, sol, slice, length, timeout);
                pList.add(p);
            }
            else {
                process(url, input, sol, slice, length, timeout);
            }
        }
        
        // Wait for parallel threads to stop
        for (ProviderThread p : pList) {
            try {
                p.join();
            }            
            catch (InterruptedException ex) {
                logger.warn(ex.toString());
            }
        }
                
        Mappings res = getResult(mapList);
        
        if (serverList.size() > 1) {
            eval.getVisitor().service(eval, DatatypeMap.toList(serverList), getServiceExp(), res);
        }
        g.getEventManager().finish(Event.Service, "result: " + ((res!= null) ? res.size(): 0));
        return res;
    }
    
    /**
     * Execute service in a parallel thread 
     */
    ProviderThread parallelProcess(URLServer service, Mappings map, Mappings sol, boolean slice, int length, int timeout) {
        ProviderThread thread = new ProviderThread(this, service, map, sol, slice, length, timeout);
        thread.start();
        return thread;
    }
       
    /**
     * Execute one service with possibly input Mappings map and possibly slicing map into packet of size length
     * Add results into Mappings sol which is empty when entering
     * Several such process may run in parallel in case of several service URL
     */
    void process(URLServer service, Mappings map, Mappings sol, boolean slice, int length, int timeout) 
            throws EngineException {
        int size = 0, count = 0;        
        traceInput(service, map);
        Date d1 = new Date();
        if (slice) {
            boolean debug = service.hasParameter(MODE, DEBUG) || getQuery().isRecDebug();
            
            if (debug && map.isEmpty()) {
                logger.info("Candidate Mappings are empty: skip service " + service.getURL());
            }
            
            while (size < map.size()) {
                if (eval.isStop()) {
                    break;
                }
                // consider subset of Mappings of size slice
                // it may produce bindings for target service
                Mappings res = send(service, map, size, size + length, timeout, count);
                // join (serviceNode = serviceURI)
                complete(getServiceExp().getServiceNode(), service.getNode(), res);
                addResult(sol, res);
                size += length;
                count++;
                
                if (stop(service, sol, d1)) {
                    break;
                }
            }
            
        } else {
            Mappings res = send(service, map, 0, 0, timeout, count++);
            // join (serviceNode = serviceURI)
            complete(getServiceExp().getServiceNode(), service.getNode(), res);
            addResult(sol, res);
        }
        
        traceOutput(service, sol, count, (new Date().getTime()-d1.getTime())/1000.0);

        synchronized (getBinding()) {
            eval.getVisitor().service(eval, service.getNode(), getServiceExp(), sol);
        }
    }
    
    void log(URLServer serv, Mappings map) {
        System.out.println("service: " + serv + "; nb results: " + map.size());
    }

    /**
     * Send query to sparql endpoint using HTTP request
     * Generate variable binding from map or env if any 
     * Consider subset of Mappings map within start & limit 
     * Possibly modify the AST with these bindings (filter or values)
     */
    Mappings send(URLServer serv, Mappings map, 
            int start, int limit, int timeout, int count) throws EngineException {
        Query q = getQuery();
        // use case: ldscript nested query
        boolean debug = serv.hasParameter(MODE, DEBUG) || q.isRecDebug();
        ASTQuery targetAST = getAST();
        try {

            // oririnal ast
            ASTQuery aa = getAST();
            // ast possibly modified with variable bindings from map/env 
            ASTQuery ast = getCompiler().compile(serv, q, map, start, limit);
            
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
                   
            targetAST = ast;
            traceAST(serv, ast);
            Mappings res = send(serv, ast, map, start, limit, timeout, count);
                        
            if (debug) {
                traceResult(serv, res);
            }
            if (res != null && res.isError()) {
                logger.info("Parse error in result of service: " + serv.getURL());
            }
            return res;
        } 
        
        catch (ResponseProcessingException e) {
            logger.error("ResponseProcessingException: " + serv.getURL());
            getLog().addException(new EngineException(e, e.getMessage()).setURL(serv).setAST(targetAST).setObject(e.getResponse()));
            error(serv, q.getGlobalQuery(), getAST(), e);
        }
        catch (ProcessingException | IOException | SparqlException e) {
            logger.error("ProcessingException: " + serv.getURL());
            getLog().addException(new EngineException(e, e.getMessage()).setURL(serv).setAST(targetAST));            
            error(serv, q.getGlobalQuery(), getAST(), e);
        }
        
        return null;
    }
    
    void error(URLServer serv, Query gq, ASTQuery ast, Exception e) {
        logger.error("service error: " + serv.getServer());
        logger.error(e.getMessage());
        logger.error(ast.toString());
        gq.addError(SERVICE_ERROR.concat(serv.getServer()).concat("\n"), e);
    }
    
    
    Mappings send(URLServer serv, ASTQuery ast, Mappings map, 
            int start, int limit, int timeout, int count) 
            throws EngineException, IOException {
        
        Mappings res = null;
        Graph g;

        if (serv.getGraph() != null) {
            // reuse former graph from previous service call
            g = (Graph) serv.getGraph();
        } else {
            res = eval(ast, serv, timeout, count);
            if (!res.getLinkList().isEmpty()){
                getLog().addLink(res.getLinkList());
            }
            g = (Graph) res.getGraph();
        }

        if (g != null) {
            // service return RDF graph
            // evaluate query(graph) locally
            QueryProcess exec = QueryProcess.create(g);
            ast.inheritFunction(getGlobalAST());
            res = exec.query(ast, getBinding());
            // record graph for next loop step
            serv.setGraph(g);
        }
        
        return res;
    }

    boolean stop(URLServer service, Mappings sol, Date d) {
        if (service.hasParameter(TIME)) {
            double time = (new Date().getTime() - d.getTime()) / 1000.0;
            if (time >= service.doubleValue(TIME)) {
                logger.info("Service time limit: " + time + " >= " + service.doubleValue(TIME));
                return true;
            }
        }

        if (service.hasParameter(LIMIT)) {
            if (sol.size() >= service.intValue(LIMIT)) {
                logger.info("Service result limit: " + sol.size() + " >= " + service.intValue(LIMIT));
                return true;
            }
        }
        return false;
    }
    
    ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }
    
    void traceAST(URLServer serv, ASTQuery ast) {
        getLog().traceAST(serv, ast);
    }

    void traceInput(URLServer serv, Mappings map) {        
        getLog().traceInput(serv, map);
    }
    
    void traceOutput(URLServer serv, Mappings map, int nbcall, double time) {
        getLog().traceOutput(serv, map, nbcall, time);
    }
    
    void traceResult(URLServer serv, Mappings res) {
        if (res.size() > 0) {
            logger.info(String.format("** Service %s result: \n%s", serv, res.toString(false, false, 10)));
        } else {
            logger.info(String.format("** Service %s result size: %s", serv, res.size()));
        }
    }
    
    void addResult(Mappings sol, Mappings res) {
        if (res != null) {
            sol.add(res);
            sol.setLength(sol.getLength() + res.getLength());
            sol.setQueryLength(sol.getQueryLength()+ res.getQueryLength());
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
    Mappings getResult(List<Mappings> mapList) {
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
        boolean distinct = ! getGlobalAST().hasMetadata(Metadata.DUPLICATE);
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
    void complete(Node serviceNode, Node serviceURI, Mappings map){
        if (map != null && serviceNode.isVariable() && ! getEnvironment().isBound(serviceNode)) {
           map.join(serviceNode, serviceURI);
        }
    }
    
    /**
     * Select subset of distinct Mappings where serviceNode = serviceURI
     */
    Mappings getMappings(Query q, Exp exp, Node serviceNode, Node serviceURI, Mappings map) {
        if (exp.isGenerated()) {
            // generated by federated compiler
            return map.getMappings(q);
        }
        if (serviceNode.isVariable() && ! getEnvironment().isBound(serviceNode)) {
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
    List<Node> getServerList(Exp exp, Mappings map) {
        if (exp.getNodeSet() == null) {
            Node serviceNode = exp.getServiceNode();
            List<Node> list = new ArrayList<>();
            if (serviceNode.isVariable()) {
                Node value = getEnvironment().getNode(serviceNode);
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
    
    
    int getTimeout(Node serv) {
        Integer time = (Integer) getQuery().getGlobalQuery().getPragma(Pragma.TIMEOUT);
        if (time == null) {
            return getEval().getVisitor().timeout(serv);
        }
        return time;
    }
    
    int getSlice(Node serv, Mappings map) {
        // former: 
        getQuery().getGlobalQuery().getSlice();
        int slice = getEval().getVisitor().slice(serv, map==null?Mappings.create(getQuery()):map);
        IDatatype dt = getBinding().getGlobalVariable(Binding.SLICE_SERVICE);
        if (dt == null) {
            return slice;
        }
        return dt.intValue();
    }
    
    Mappings eval(ASTQuery ast, URLServer serv, int timeout, int count) 
            throws IOException,  EngineException {
        if (isDB(serv.getNode())){
            return db(getQuery(), serv.getNode());
        }
        if (serv.getServer().equals(LOCAL_SERVICE)) {
            return getDefault().query(ast);
        }
        return send(getQuery(), ast, serv, timeout, count);
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
    
    Mappings send(Query q, ASTQuery ast, URLServer serv,  int timeout, int count) 
            throws IOException {
        return post(q, ast, serv, timeout, count);
    }
    

    Mappings post(Query q, ASTQuery ast, URLServer serv,  int timeout, int count) throws IOException {
        try {
            Binding b = getBinding();
            Service service = new Service(serv) ;
            service.setLevel(b.getAccessLevel());
            service.setTimeout(timeout);
            service.setCount(count);
            service.setBind(b);
            Mappings map = service.query(q, ast, null);
            return map;
        } catch (LoadException ex) {
            throw (new IOException(ex.getMessage() + " " + serv.getURL()));
        }
    }
  
    public QueryProcess getDefault() {
        return defaut;
    }

    public void setDefault(QueryProcess defaut) {
        this.defaut = defaut;
    }

    public ProviderImpl getProvider() {
        return provider;
    }

    public void setProvider(ProviderImpl provider) {
        this.provider = provider;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Exp getServiceExp() {
        return serviceExp;
    }

    public void setServiceExp(Exp serviceExp) {
        this.serviceExp = serviceExp;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    public Eval getEval() {
        return eval;
    }

    public void setEval(Eval eval) {
        this.eval = eval;
    }

    public CompileService getCompiler() {
        return compiler;
    }

    public void setCompiler(CompileService compiler) {
        this.compiler = compiler;
    }
    
    Environment getEnvironment() {
        return getEval().getEnvironment();
    }
    
    Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public ASTQuery getGlobalAST() {
        return globalAST;
    }

    public void setGlobalAST(ASTQuery globalAST) {
        this.globalAST = globalAST;
    }
    
    synchronized ContextLog getLog() {
        return getBinding().getCreateLog();
    }

    public ASTQuery getAST() {
        return ast;
    }

    public void setAST(ASTQuery ast) {
        this.ast = ast;
    }
    
    boolean isSparql0(Node node) {
        return getProvider().isSparql0(node);
    }
    
}
