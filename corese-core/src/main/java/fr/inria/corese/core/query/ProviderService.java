package fr.inria.corese.core.query;

import static fr.inria.corese.core.util.Property.Value.SERVICE_GRAPH;
import static fr.inria.corese.core.util.Property.Value.SERVICE_HEADER;
import static fr.inria.corese.core.util.Property.Value.SERVICE_PARAMETER;
import static fr.inria.corese.core.util.Property.Value.SERVICE_SLICE;
import static fr.inria.corese.core.util.Property.Value.SERVICE_TIMEOUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.load.ServiceReport;
import fr.inria.corese.core.storage.CoreseGraphDataManagerBuilder;
import fr.inria.corese.core.storage.DataManagerJava;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Cookie;

/**
 * Service call
 * Service Report is recorded by ServiceParser -> ServiceReport
 *
 * @author Olivier Corby, Edelweiss INRIA 2021
 */
public class ProviderService implements URLParam {

    static Logger logger = LoggerFactory.getLogger(ProviderService.class);
    // pseudo service: call current QueryProcess on current dataset (db or graph)
    public static final String LOCAL_SERVICE = "http://ns.inria.fr/corese/sparql";
    // pseudo service: call QueryProcess on graph dataset (if it was db, switch to
    // graph dataset)
    public static final String DATASET_SERVICE = "http://ns.inria.fr/corese/dataset";

    public static final String LOCAL_SERVICE_NS = LOCAL_SERVICE + "/%s";
    public static final String UNDEFINED_SERVICE = "http://example.org/undefined/sparql";
    private static final String SERVICE_ERROR = "Service error: ";
    private static final String DB = "db:";
    public static int SLICE_DEFAULT = 100;
    public static int TIMEOUT_DEFAULT = 10000;
    public static int DISPLAY_RESULT_MAX = 10;
    private static final String TIMEOUT_EXCEPTION = "SocketTimeoutException";
    private static final String READ_TIMEOUT_EXCEPTION = "SSLProtocolException: Read timed out";
    private static final String LOOP_RETURN_PARTIAL_SOLUTION_AFTER_TIMEOUT = "Loop return partial solution after timeout";
    private static final String RETURN_PARTIAL_SOLUTION_AFTER_TIMEOUT = "Return partial solution after timeout";

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
    private boolean bind = true;
    private int displayResultMax = DISPLAY_RESULT_MAX;

    /**
     *
     * @param p
     * @param q    query inside service statement
     * @param map
     * @param eval
     */
    ProviderService(ProviderImpl p, Query q, Mappings map, Eval eval) {
        setProvider(p);
        setQuery(q);
        setGlobalAST(q.getGlobalQuery().getAST());
        setAST(q.getAST());
        setMappings(map);
        setEval(eval);
        // after setEval:
        setBinding(getEnvironment().getBind());
        // after setBinding:
        init();
    }

    void init() {
        setCompiler(new CompileService(this, getEnvironment()));
        // federate visitor may have recorded data in AST Context
        // share it with Binding Context Log
        getLog().share(getGlobalAST().getLog());
        if (getGlobalAST().hasMetadata(Metadata.BINDING, Metadata.SKIP_STR)) {
            setBind(false);
        }
    }

    /**
     * serv: servive variable/URI, null when variable is unbound exp: service
     * statement
     */
    Mappings send(Node serv, Exp exp) throws EngineException {
        Mappings res = basicSend(serv, exp);

        if (getServiceExp().getNumber() > 0 && res != null && res.isEmpty() && isBind()
                && hasValue(WHY)) {
            // generate a log, skip the result
            // debugSend(serv, exp);
        }
        return res;
    }

    Mappings basicSend(Node serv, Exp exp) throws EngineException {
        setServiceExp(exp);
        // share prefix
        int slice = getSlice(serv, getMappings());
        // when servive variable is unbound, get service URL (list) from Environment or
        // Mappings
        List<Node> serviceList = getServerList(exp, getMappings());
        boolean bslice = !(getMappings() == null || slice <= 0 || !isBind());
        // slice by default
        Mappings res = send(serviceList, serv, (isBind()) ? getMappings() : null, bslice, slice);
        restore(getAST());
        // if (res != null) {
        // res.limit(getAST().getLimit());
        // }
        return res;
    }

    /**
     * @draft: try again by relaxing the query 1) without binding to see the
     *         kind of results we could have and try to understand the failure
     *         2) @todo:
     *         remove filter if any
     *
     */
    Mappings debugSend(Node serv, Exp exp) throws EngineException {
        // no binding:
        setBind(false);
        // upgrade service number in case of log to keep service std log
        getServiceExp().setNumber(getServiceExp().getNumber() * 10);
        getAST().setLimit(20);
        Mappings res = basicSend(serv, exp);
        return res;
    }

    // draft
    void variableSend(Node serv, Exp exp) throws EngineException {
        setBind(true);
        getServiceExp().setNumber(getServiceExp().getNumber() * 10);
        fr.inria.corese.sparql.triple.parser.Exp body = getAST().getBody();

        if (body.size() > 0 && body.get(0).isTriple()) {
            Triple t = body.get(0).getTriple();
            if (t.getPredicate().isConstant()) {
                t.setPredicate(new Variable("?_pred_"));
                System.out.println("PS:\n" + getAST());
                Mappings res = basicSend(serv, exp);
            }
        }
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
     * Generalized service clause with possibly several service URIs If service
     * is unbound variable, retrieve service URI in Mappings Take Mappings
     * variable binding into account when sending service Split Mappings into
     * buckets with size = slice Iterate service on each bucket When several
     * services, they are evaluated in parallel by default, unless @sequence
     * metadata When several services, return distinct Mappings when
     *
     * @distinct metadata.
     */
    Mappings send(List<Node> serverList, Node serviceNode, Mappings map, boolean slice, int length)
            throws EngineException {
        Graph g = getGraph(getEval().getProducer());
        Query q = getQuery();
        g.getEventManager().start(Event.Service, serverList);
        boolean ok = true;

        if (serverList.isEmpty()) {
            logger.error("Undefined service: " + getServiceExp().getServiceNode());
            ok = false;
            serverList.add(NodeImpl.create(DatatypeMap.newResource(UNDEFINED_SERVICE)));
        }

        ArrayList<Mappings> mapList = new ArrayList<>();
        ArrayList<ProviderThread> pList = new ArrayList<>();
        int timeout = getTimeout(serviceNode, map);
        // by default in parallel (unless mode=sequence)
        boolean parallel = q.getOuterQuery().isParallel() && !hasValue(SEQUENCE);

        for (Node service : serverList) {

            String name = service.getLabel();
            URLServer url = new URLServer(name, Property.stringValue(SERVICE_PARAMETER));
            if (q.getGlobalQuery().isFederate()) {
                // federate rewrite service inherit from/from named
                url.setDataset(getGlobalAST().getDataset());
            }
            if (!ok) {
                url.setUndefined(true);
            }
            url.setNode(service);
            url.setNumber(getServiceExp().getNumber());
            // /sparql?param={?this}
            // get ?this=value in Binding global variable and set param=value
            url.complete(getBinding());
            // complete URL with timeout=1000 from context
            url.complete(getContext());
            url.encode();
            getLog().add(LogKey.ENDPOINT, url.getServer());
            getLog().add(LogKey.ENDPOINT_CALL, url.getLogURLNumber());
            getLog().set(url.getLogURLNumber(), LogKey.ENDPOINT_NUMBER, url.getNumber());
            getLog().set(url.getLogURLNumber(), LogKey.ENDPOINT_URL, url.getServer());

            if (ok) {
                if (getContext() != null && getContext().isFederateIndex()) {
                    // service clause accepted
                } else if (Access.reject(Feature.SPARQL_SERVICE, getBinding().getAccessLevel(), service.getLabel())) {
                    logger.error(TermEval.SERVICE_MESS + " " + service.getLabel());
                    SafetyException ex = new SafetyException(TermEval.SERVICE_MESS, service.getLabel());
                    getLog().addException(ex.setURL(url));
                    throw ex;
                }
            }

            if (eval.isStop()) {
                break;
            }
            g.getEventManager().process(Event.Service, service);

            Mappings input = map;

            if (slice) {
                // default behaviour when map != null
                // service is variable: select appropriate subset of Mappings with service URL
                // service is URL: consider all Mappings.
                // Hint: Mappings are already result of former select
                input = getMappings(q, getServiceExp(), getServiceExp().getServiceNode(), service, map);
                if (input.size() > 0) {
                    g.getEventManager().process(Event.Service, "input: \n" + input.toString(true, false, 5));
                } else {
                    g.getEventManager().process(Event.Service, "no input");
                }
            }

            Mappings sol = new Mappings();
            sol.setReport(DatatypeMap.newList());
            mapList.add(sol);

            // sparql?slice=20
            length = url.intValue(SLICE, length);
            // sparql?timeout=123
            timeout = url.intValue(TIMEOUT, timeout);
            // @todo: storage and dataset service url cannot run in //
            // because it is same ast and it is not reentrant
            if (parallel) {
                ProviderThread p = parallelProcess(url, input, sol, slice, length, timeout);
                pList.add(p);
            } else {
                process(url, input, sol, slice, length, timeout);
            }
        }

        // Wait for parallel threads to stop
        for (ProviderThread p : pList) {
            try {
                p.join();
            } catch (InterruptedException ex) {
                logger.warn(ex.toString());
            }
        }

        Mappings res = getResult(mapList);
        // trace(res);
        if (serverList.size() > 1) {
            eval.getVisitor().service(eval, DatatypeMap.toList(serverList), getServiceExp(), res);
        }
        g.getEventManager().finish(Event.Service, "result: " + ((res != null) ? res.size() : 0));
        return res;
    }

    void trace(Mappings map) {
        if (map.getSelect() != null) {
            for (Node node : map.getSelect()) {
                System.out.println("service select: " + node + " " + node.getIndex());
            }
        }
        for (Mapping m : map) {
            for (Node node : m.getQueryNodeList()) {
                System.out.println("service ql: " + node + " " + node.getIndex());
            }
        }
        System.out.println(map);
    }

    /**
     * Execute service in a parallel thread
     */
    ProviderThread parallelProcess(URLServer service, Mappings map, Mappings sol, boolean slice, int length,
            int timeout) {
        ProviderThread thread = new ProviderThread(this, service, map, sol, slice, length, timeout);
        thread.start();
        return thread;
    }

    /**
     * Execute one service with possibly input Mappings map and possibly slicing
     * map into packet of size length Add results into Mappings sol which is
     * empty when entering Several such process may run in parallel in case of
     * several service URL
     */
    void process(URLServer service, Mappings map, Mappings sol, boolean slice, int length, int timeout)
            throws EngineException {

        if (FederateVisitor.isBlackListed(service.getServer())) {
            logger.info(String.format("Endpoint %s is blacklisted", service.getServer()));
            return;
        }
        int size = 0, count = 0;
        traceInput(service, map);
        Date d1 = new Date();

        try {
            if (slice) {
                boolean debug = service.hasParameter(MODE, DEBUG) || getQuery().isRecDebug();

                if (map.isEmpty()) {
                    if (debug) {
                        logger.info("Candidate Mappings are empty: skip service " + service.getURL());
                    }
                    traceAST(service, getAST());
                }

                while (size < map.size() && !stop(service, size)) {
                    if (eval.isStop()) {
                        break;
                    }
                    // consider subset of Mappings of size slice
                    // it may produce bindings for target service

                    Mappings res = null;

                    try {
                        res = send(service, map, size, size + length, timeout, count);
                    } catch (ProcessingException e) {
                        if (isTimeout(e) && !sol.isEmpty()) {
                            logger.info(RETURN_PARTIAL_SOLUTION_AFTER_TIMEOUT);
                        } else {
                            throw e;
                        }
                    }

                    // join (serviceNode = serviceURI)
                    complete(getServiceExp().getServiceNode(), service.getNode(), res);
                    addResult(service, sol, res);
                    size += length;
                    count++;
                    if (getGlobalAST().hasMetadata(Metadata.TRACE)) {
                        logger.info(String.format(
                                "Service %s with %s parameters out of %s, results: %s, total results: %s",
                                service.getURL(), Math.min(size, map.size()),
                                map.size(), res == null ? 0 : res.size(), sol.size()));
                    }
                    if (stop(service, sol, d1)) {
                        break;
                    }
                }
                if (getGlobalAST().hasMetadata(Metadata.TRACE)) {
                    logger.info(String.format(
                            "Service %s total results: %s", service.getURL(), sol.size()));
                }
            } else {
                Mappings res = send(service, map, 0, 0, timeout, count++);
                // join (serviceNode = serviceURI)
                complete(getServiceExp().getServiceNode(), service.getNode(), res);
                addResult(service, sol, res);
            }
        } catch (ProcessingException e) {
            // timeout exception
            exception(e, service, getQuery().getGlobalQuery(), getQuery().getAST());
        }

        traceOutput(service, sol, count, (new Date().getTime() - d1.getTime()) / 1000.0);

        synchronized (getBinding()) {
            eval.getVisitor().service(eval, service.getNode(), getServiceExp(), sol);
        }
    }

    /**
     * Send query to sparql endpoint using HTTP request Generate variable
     * binding from map or env if any Consider subset of Mappings map within
     * start & limit Possibly modify the AST with these bindings (filter or
     * values)
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
            CompileServiceResult ares = new CompileServiceResult();
            ASTQuery ast = getCompiler().compile(serv, q, map, ares, start, limit);

            if (aa == ast) {

                // no binding
                if (start > 0 || ares.isBnode()) {
                    // no relevant binding: skip slice
                    if (ares.isBnode()) {
                        logger.info("Skip bindings with blank nodes");
                    }
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
            complete(ast);

            Mappings res = sendWithLoop(serv, ast, map, start, limit, timeout, count);
            if (res != null) {
                reportAST(ast, res, count);
            }
            if (debug && res != null) {
                traceResult(serv, res);
            }
            if (res != null && res.isError()) {
                logger.info("Parse error in result of service: " + serv.getURL());
            }
            return res;
        } catch (ResponseProcessingException e) {
            exception(e, serv, q.getGlobalQuery(), targetAST, e.getResponse());
        } catch (ProcessingException e) {
            // timeout -> ProcessingException
            if (isTimeout(e)) {
                logger.info("Service timeout detected");
                throw e;
            }
            exception(e, serv, q.getGlobalQuery(), targetAST);
        } catch (IOException | SparqlException e) {
            exception(e, serv, q.getGlobalQuery(), targetAST);
        }

        return null;
    }

    void exception(Exception e, URLServer serv, Query q, ASTQuery ast) {
        exception(e, serv, q, ast, null);
    }

    void exception(Exception e, URLServer serv, Query q, ASTQuery ast, Object obj) {
        logger.error(String.format("%s %s", e.getClass().getName(), serv.getURL()));
        getLog().addException(new EngineException(e, e.getMessage())
                .setURL(serv).setAST(ast).setObject(obj));
        error(serv, q, getAST(), e);
    }

    // logger.error("ProcessingException: " + serv.getURL());
    // getLog().addException(new EngineException(e, e.getMessage())
    // .setURL(serv).setAST(targetAST));
    // error(serv, q.getGlobalQuery(), getAST(), e);

    // @loop @limit 1000 @start 0 @until 9
    // for (i=0; i<until; i++) offset = i*limit
    Mappings sendWithLoop(URLServer serv, ASTQuery ast, Mappings map,
            int start, int limit, int timeout, int count)
            throws EngineException, IOException {

        if (getGlobalAST().hasMetadata(Metadata.LOOP)
                || serv.hasParameter(LOOP)
                || serv.hasParameter(MODE, LOOP)) {
            int begin = getValue(serv, Metadata.START, URLParam.START, 0);
            int end = getValue(serv, Metadata.UNTIL, URLParam.UNTIL, Integer.MAX_VALUE);
            int myLimit = getValue(serv, Metadata.LIMIT_STR, URLParam.LIMIT, ast.getLimit());
            logger.info(String.format("send loop: begin %s end %s limit %s", begin, end, myLimit));

            Mappings sol = new Mappings();

            for (int i = begin;; i++) {
                ast.setLimit(myLimit);
                ast.setOffset(myLimit * i);
                Mappings res = null;
                try {
                    res = sendStep(serv, ast, map, start, limit, timeout, count);
                } catch (ProcessingException e) {
                    if (isTimeout(e) && !sol.isEmpty()) {
                        logger.info(LOOP_RETURN_PARTIAL_SOLUTION_AFTER_TIMEOUT);
                    } else {
                        throw e;
                    }
                }

                if (res == null) {
                    break;
                }

                logger.info(
                        String.format(
                                "send loop: index %s limit %s offset %s results: %s, loop results: %s",
                                i, ast.getLimit(), ast.getOffset(), res.size(), res.size() + sol.size()));

                sol.initQuery(res.getQuery());
                if (res.isEmpty()) {
                    break;
                }

                addResult(serv, sol, res);
                if (i >= end || res.size() < myLimit) {
                    // less results than limit: no use to loop anymore
                    break;
                }
            }
            logger.info(String.format("send loop total results: %s", sol.size()));
            return sol;
        } else {
            Mappings res = sendStep(serv, ast, map, start, limit, timeout, count);
            if (getGlobalAST().hasMetadata(Metadata.TRACE)) {
                logger.info(String.format("service results: %s %s", serv, res.size()));
            }
            return res;
        }
    }

    boolean isTimeout(ProcessingException e) {
        return e.getMessage() != null
                && (e.getMessage().contains(TIMEOUT_EXCEPTION)
                        || e.getMessage().contains(READ_TIMEOUT_EXCEPTION));
    }

    // when query is select distinct and query body is a service:
    // service ast inherits select distinct
    void complete(ASTQuery ast) {
        if (getGlobalAST().isDistinct()
                && getGlobalAST().getBody().size() == 1
                && getGlobalAST().getBody().get(0).isService()) {
            ast.setDistinct(true);
        }
    }

    void error(URLServer serv, Query gq, ASTQuery ast, Exception e) {
        logger.error("service error: " + serv.getServer());
        if (e.getMessage().length() > 1000) {
            logger.error(e.getMessage().substring(0, 1000));
        } else {
            logger.error(e.getMessage());
        }
        logger.error(ast.toString());
        gq.addError(SERVICE_ERROR.concat(serv.getServer()).concat("\n"), e);
        submitError(serv);
    }

    void submitError(URLServer url) {
        if ((getContext() != null && getContext().isSelection())
                || getQuery().getGlobalQuery().isFederate()
                || getGlobalAST().hasMetadata(Metadata.FED_BLACKLIST)) {
            if (FederateVisitor.blacklist(url.getServer())) {
                logger.info("Blacklist: " + url.getServer() + " " + FederateVisitor.getBlacklist().size());
            }
        }
    }

    int getValue(URLServer url, String meta, String param, int n) {
        return getValue(url, getGlobalAST(), meta, param, n);
    }

    int getValue(URLServer url, ASTQuery ast, String meta, String param, int n) {
        if (url.hasParameter(param)) {
            int value = url.intValue(param);
            if (value != -1) {
                return value;
            }
        }
        if (ast.getMetaValue(meta) != null) {
            return ast.getMetaValue(meta).intValue();
        }
        return n;
    }

    /**
     * Intermediate send function with graph processing extension Endpoint may
     * return RDF graph as query result use case: format=turtle => return W3C
     * Query Results RDF Format => RDF Graph
     */
    Mappings sendStep(URLServer serv, ASTQuery ast, Mappings map,
            int start, int limit, int timeout, int count)
            throws EngineException, IOException {

        if (serv.isUndefined()) {
            return null;
        }
        if (Property.booleanValue(SERVICE_GRAPH)) {
            return sendWithGraph(serv, ast, map, start, limit, timeout, count);
        } else {
            return sendBasic(serv, ast, map, start, limit, timeout, count);
        }
    }

    Mappings sendBasic(URLServer serv, ASTQuery ast, Mappings map,
            int start, int limit, int timeout, int count)
            throws EngineException, IOException {

        Mappings res = eval(ast, serv, timeout, count);
        processLinkList(res.getLinkList());
        return res;
    }

    /**
     * Extension: service may return RDF graph Evaluate service query on graph
     */
    Mappings sendWithGraph(URLServer serv, ASTQuery ast, Mappings map,
            int start, int limit, int timeout, int count)
            throws EngineException, IOException {

        Mappings res = null;
        Graph g;

        if (serv.getGraph() != null) {
            // reuse former graph from previous service call
            g = (Graph) serv.getGraph();
        } else {
            res = sendBasic(serv, ast, map, start, limit, timeout, count);
            g = (Graph) res.getGraph();
        }
        if (g != null) {
            QueryProcess exec = QueryProcess.create(g);
            ast.inheritFunction(getGlobalAST());
            res = exec.query(ast, getBinding());
            // record graph for next loop step
            serv.setGraph(g);
        }

        return res;
    }

    void processLinkList(List<String> list) {
        if (!list.isEmpty()) {
            getLog().addLink(list);
        }
    }

    boolean processMessage(Mappings map) {
        String url = map.getLink(URLParam.MES);
        if (url != null) {
            return processMessage(url);
        }
        return true;
    }

    boolean processMessage(String url) {
        String text = new Service().getString(url);
        JSONObject obj = new JSONObject(text);

        System.out.println("PS: message");
        for (String key : obj.keySet()) {
            System.out.println(key + " " + obj.get(key));
        }

        return true;
    }

    // is there a limit on size of input
    boolean stop(URLServer serv, int size) {
        if (serv.hasParameter(INPUT)) {
            return size >= serv.intValue(INPUT);
        }
        return false;
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

    void traceAST(URLServer serv, ASTQuery ast) {
        getLog().traceAST(serv, ast);
    }

    void reportAST(ASTQuery ast, Mappings map, int count) {
        if (getGlobalAST().hasMetadata(Metadata.DETAIL)) {
            map.completeReport(URLParam.QUERY, ast.toString());
        }
        map.completeReport(CALL, count);
    }

    DatatypeMap map() {
        return DatatypeMap.getSingleton();
    }

    void traceInput(URLServer serv, Mappings map) {
        getLog().traceInput(serv, map);
    }

    /**
     *
     * @param serv
     * @param map:    final result Mappings of service serv
     * @param nbcall: number of service call to evaluate service serv
     * @param time
     */
    void traceOutput(URLServer serv, Mappings map, int nbcall, double time) {
        getLog().traceOutput(serv, map, nbcall, time);

        if (getGlobalAST().hasReportKey(FULL_SIZE)) {
            map.completeReport(FULL_SIZE, map.size());
            map.completeReport(NB_CALL, nbcall);
            map.completeReport(FULL_TIME, time);

            if (map.getReport() != null) {
                map.getReport().dispatch(REPORT);
            }
        }
    }

    void traceResult(URLServer serv, Mappings res) {
        if (res.size() > 0) {
            logger.info(String.format("** Service %s result: \n%s", serv,
                    res.toString(false, false, getDisplayResultMax())));
        }
        logger.info(String.format("** Service %s result size: %s", serv, res.size()));
    }

    /**
     *
     * @param sol: total result of service evaluation
     * @param res: partial result of service evaluation
     */
    void addResult(URLServer serv, Mappings sol, Mappings res) {
        if (res != null) {
            if (sol.getReport() != null && sol.getReport().getList() != null
                    && res.getReport() != null) {
                sol.getReport().getList().add(res.getReport());
            }
            sol.add(res);
            sol.setLength(sol.getLength() + res.getLength());
            sol.setQueryLength(sol.getQueryLength() + res.getQueryLength());
            if (sol.getQuery() == null) {
                sol.initQuery(res.getQuery());
            }
        }
    }

    void addResultBasic(Mappings sol, Mappings res) {
        if (res != null) {
            sol.add(res);
        }
    }

    /**
     * Return final result Mappings mapList is the list of result Mappings of
     * each service When there are *several* services, return distinct Mappings
     * with @distinct metadata
     */
    Mappings getResult(List<Mappings> mapList) {
        if (mapList.size() == 1) {
            return mapList.get(0);
        }
        Mappings res = null;

        if (!mapList.isEmpty()) {
            res = new Mappings();

            for (Mappings m : mapList) {
                if (res.getQuery() == null && m.getQuery() != null) {
                    res.initQuery(m.getQuery());
                }
                addResultBasic(res, m);
            }
        }

        boolean distinct = getGlobalAST().hasMetadata(Metadata.DISTINCT);
        // TODO: if two Mappings have their own duplicates, they are removed
        if (res != null && res.getSelect() != null && distinct) {
            res = res.distinct(res.getSelect(), res.getSelect());
        }
        return res;
    }

    /**
     * service ?s { BGP } When ?s is unbound, join (?s = URI) to Mappings,
     * reject those that are incompatible TODO: optimize map.join()
     */
    void complete(Node serviceNode, Node serviceURI, Mappings map) {
        if (map != null && serviceNode.isVariable() && !getEnvironment().isBound(serviceNode)) {
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
        if (serviceNode.isVariable() && !getEnvironment().isBound(serviceNode)) {
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
                if (value == null) {
                    return getServerList(serviceNode, map);
                } else {
                    list.add(value);
                }
            } else {
                list.add(serviceNode);
            }
            return list;
        } else {
            // service <uri1> <uri2> {}
            return exp.getNodeSet();
        }
    }

    /**
     * service ?s { } Retrieve service URIs for ?s in Mappings
     */
    List<Node> getServerList(Node serviceNode, Mappings map) {
        if (map == null) {
            logger.error("Unbound variable: " + serviceNode);
            return new ArrayList<>();
        }
        return map.aggregate(serviceNode);
    }

    int getTimeout(Node serv, Mappings map) {
        Integer timeout = TIMEOUT_DEFAULT;
        IDatatype dttimeout = getGlobalAST().getMetaValue(Metadata.TIMEOUT);
        if (dttimeout != null) {
            timeout = dttimeout.intValue();
        } else {
            timeout = Property.intValue(SERVICE_TIMEOUT);
            if (timeout == null) {
                timeout = TIMEOUT_DEFAULT;
            }
        }
        logger.info("Timeout: " + timeout);
        return timeout;
    }

    int getSlice(Node serv, Mappings map) {
        IDatatype dtslice = getGlobalAST().getMetadataDatatypeValue(Metadata.SLICE);
        if (dtslice != null) {
            return dtslice.intValue();
        }
        Integer slice = Property.intValue(SERVICE_SLICE);
        if (slice != null) {
            return slice;
        }
        return SLICE_DEFAULT;
    }

    Mappings eval(ASTQuery ast, URLServer serv, int timeout, int count)
            throws IOException, EngineException {
        if (isDB(serv.getNode())) {
            return db(getQuery(), serv.getNode());
        }
        if (serv.isStorage()) {
            // service store:path -> query db with data manager
            return storage(ast.acopy(), serv, getBinding());
        }
        if (serv.getServer().startsWith(LOCAL_SERVICE)) {
            // pseudo service call current QueryProcess
            return local(ast.acopy(), serv, getBinding());
        }
        if (serv.getServer().startsWith(DATASET_SERVICE)) {
            // switch to graph dataset (in case of db)
            return dataset(ast.acopy(), serv, getBinding());
        }
        return send(getQuery(), ast, serv, timeout, count);
    }

    // @todo: these 3 functions are synchronized because
    // ast query process does not support parallel threads
    // in particular exists {} record graph pattern inside filter
    // graph pattern is shared among threads and
    // node indexing of this graph pattern fails in parallel threads

    // pseudo service call current QueryProcess
    synchronized Mappings local(ASTQuery ast, URLServer url, Binding b) throws EngineException {
        logger.info("Local service: " + url);
        logger.info(ast.toString());
        return index(getDefault().query(ast, b));
    }

    // switch to graph dataset (in case where mode=db)
    synchronized Mappings dataset(ASTQuery ast, URLServer url, Binding b) throws EngineException {
        logger.info("Dataset service: " + url);
        logger.info(ast.toString());
        QueryProcess exec = QueryProcess.create(getDefault().getGraph());
        return index(exec.query(ast, b));
    }

    // pseudo service store:path to query db
    synchronized Mappings storage(ASTQuery ast, URLServer url, Binding b) throws EngineException {
        DataManager man = dataManager(url);
        QueryProcess exec = QueryProcess.create(man);
        //logger.info(String.format("storage: %s\n%s", url, ast));
        ast.inheritFunction(getGlobalAST());
        return index(exec.query(ast, b));
    }

    DataManager dataManager(URLServer url) throws EngineException {
        DataManager man = StorageFactory.getDataManager(url.getStoragePath());
        if (man == null) {
            if (url.hasParameter()) {
                if (url.hasParameter(MODE, "dataset")) {
                    man = new CoreseGraphDataManagerBuilder().graph(getGraph()).build();
                }
                else {
                    man = new DataManagerJava(url.getStoragePathWithParameter());
                }
                StorageFactory.defineDataManager(url.getStoragePathWithParameter(), man);
            } else {
                throw new EngineException(
                        String.format("Undefined storage manager: %s %s", url.getServer(), url.getStoragePath()));
            }
        } else if (url.hasParameter()) {
            man.init(url.getMap());
        }

        return man;
    }

    // clean service results query node
    // keep only select variables to prevent having bnodes
    Mappings index(Mappings map) {
        map.project();
        return map;
    }

    /**
     * service <db:/tmp/human_db> { GP } service overloaded to query a database
     */
    Mappings db(Query q, Node serv) throws EngineException {
        QueryProcess exec = QueryProcess.dbCreate(Graph.create(), true, QueryProcess.DB_FACTORY,
                serv.getLabel().substring(DB.length()));
        return exec.query(q.getAST());
    }

    boolean isDB(Node serv) {
        return serv.getLabel().startsWith(DB);
    }

    Mappings send(Query q, ASTQuery ast, URLServer serv, int timeout, int count)
            throws IOException {
        return post(q, ast, serv, timeout, count);
    }

    Mappings post(Query q, ASTQuery ast, URLServer serv, int timeout, int count) throws IOException {
        try {
            Binding b = getBinding();
            Service service = new Service(serv);
            service.setLevel(b.getAccessLevel());
            service.setTimeout(timeout);
            service.setCount(count);
            service.setBind(b);
            // service.setLog(getLog());
            service.setDebug(q.isRecDebug());
            Mappings map = service.query(q, ast, null);
            log(serv, service.getReport());
            return map;
        } catch (LoadException ex) {
            throw (new IOException(ex.getMessage() + " " + serv.getURL()));
        }
    }

    // record whole header
    void log(URLServer url, ServiceReport report) {
        if (report != null && report.getResponse() != null
                && report.getResponse().getHeaders() != null) {

            for (String name : report.getResponse().getHeaders().keySet()) {
                String res = report.getResponse().getHeaderString(name);
                if (res != null) {
                    getLog().defLabel(url.getLogURLNumber(), name, res);
                }
            }
            for (String name : report.getResponse().getCookies().keySet()) {
                Cookie res = report.getResponse().getCookies().get(name);
                if (res != null) {
                    getLog().defLabel(url.getLogURLNumber(), name.concat("-cookie"), res.toString());
                }
            }
        }
    }

    // record subset of header
    void log2(URLServer url, ServiceReport report) {
        if (report != null && report.getResponse() != null
                && report.getResponse().getHeaders() != null) {
            List<String> headerList = Property.listValue(SERVICE_HEADER);
            if (headerList != null) {

                for (String header : headerList) {
                    if (header.equals(Property.STAR)) {
                        for (String name : report.getResponse().getHeaders().keySet()) {
                            String res = report.getResponse().getHeaderString(name);
                            if (res != null) {
                                getLog().defLabel(url.getLogURLNumber(), name, res);
                            }
                        }
                        for (String name : report.getResponse().getCookies().keySet()) {
                            Cookie res = report.getResponse().getCookies().get(name);
                            if (res != null) {
                                getLog().defLabel(url.getLogURLNumber(), name.concat("-cookie"), res.toString());
                            }
                        }
                    } else {
                        String res = report.getResponse().getHeaderString(header);
                        if (res != null) {
                            // logger.info(String.format("%s %s=%s", url.toString(), header, res));
                            getLog().defLabel(url.getLogURLNumber(), header, res);
                        }
                    }
                }
            }
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

    // query inside service
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
    
    Graph getGraph() {
        return (Graph) getEval().getProducer().getGraph();
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

    ContextLog getLog() {
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

    Context getContext() {
        return getBinding().getContext();
    }

    public boolean isBind() {
        return bind;
    }

    public void setBind(boolean bind) {
        this.bind = bind;
    }

    boolean hasValue(String key) {
        return getContext() != null && getContext().hasValue(key);
    }

    public int getDisplayResultMax() {
        return displayResultMax;
    }

    public void setDisplayResultMax(int displayResultMax) {
        this.displayResultMax = displayResultMax;
    }
}
