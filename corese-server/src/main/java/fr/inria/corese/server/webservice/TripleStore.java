package fr.inria.corese.server.webservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.server.webservice.message.TripleStoreLog;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.datatype.RDF;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class TripleStore implements URLParam {
    private static final String LOG_DIR = "/log/";

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(TripleStore.class);
    static HashMap<String, Integer> metaMap;
    GraphStore graph = GraphStore.create(false);
    // QueryProcess exec;// = QueryProcess.create(graph);
    boolean rdfs = false;
    boolean owl = false;
    private boolean match = false;
    private boolean protect = false;
    private String name = Manager.DEFAULT;
    private DataManager dataManager;

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
        this.owl = owl;
    }

    TripleStore(GraphStore g) {
        graph = g;
        init(g);
        setMatch(false);
    }

    TripleStore(GraphStore g, boolean b) {
        graph = g;
        init(g);
    }

    void init(GraphStore g) {
        if (EmbeddedJettyServer.isDebug()) {
            g.setVerbose(true);
        }
    }

    void finish(boolean b) {
        setMatch(true);
        init(b);
    }

    @Override
    public String toString() {
        return graph.toString();
    }

    QueryProcess getQueryProcess() {
        if (getDataManager() != null) {
            logger.info("QueryProcess DataManager: " + getDataManager());
            return QueryProcess.create(getGraph(), getDataManager());
        }
        return getLocalQueryProcess();
    }

    QueryProcess getLocalQueryProcess() {
        QueryProcess exec = QueryProcess.create(getGraph(), isMatch());
        return exec;
    }

    GraphStore getGraph() {
        return graph;
    }

    void setGraph(GraphStore g) {
        graph = g;
    }

    void setGraph(Graph g) {
        if (g instanceof GraphStore) {
            graph = (GraphStore) g;
        }
    }

    void setOWL(boolean b) {
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

    // void load(String[] load) {
    // Load ld = Load.create(graph);
    // for (String f : load) {
    // try {
    // logger.info("Load: " + f);
    // //ld.loadWE(f, f, Load.TURTLE_FORMAT);
    // ld.parse(f, Load.TURTLE_FORMAT);
    // } catch (LoadException ex) {
    // logger.error(ex.getMessage());
    // }
    // }
    // }

    void load(String path, String src) throws LoadException {
        Load ld = Load.create(getGraph());
        ld.setDataManager(getDataManager());
        ld.parse(path, src, Load.TURTLE_FORMAT);
    }

    /**
     * Extended SPARQL Endpoint
     * 
     * SPARQL endpoint: /sparql?query=
     * Federated endpoint: /federate?query=
     * Load and evaluate shape, execute query on shacl validation report
     * SHACL endpoint: /sparql?mode=shacl&uri=shape&query=select * where { ?sh
     * sh:conforms ?b }
     */
    Mappings query(HttpServletRequest request, String query, Dataset ds) throws EngineException {
        if (ds.getCreateContext().hasValue(TRACE)) {
            trace(request);
        }
        if (ds == null) {
            ds = new Dataset();
        }
        Context c = ds.getContext();
        complete(c, request);
        EventManager.getSingleton().call(ds.getContext());
        QueryProcess exec = getQueryProcess();
        exec.setDebug(c.isDebug());

        Mappings map = null;
        try {
            before(exec, query, ds);
            TripleStoreLog tsl = new TripleStoreLog(exec, c);

            Date d1 = new Date();

            try {
                if (isFederate(ds)) {
                    // federate sparql query with @federate uri
                    if (isCompile(c)) {
                        Query qq = exec.compile(federate(query, ds), ds);
                        map = Mappings.create(qq);
                        // exec.getLog().share(qq.getAST().getLog());
                    } else {
                        map = exec.query(federate(query, ds), ds);
                    }
                } else if (isShacl(c)) {
                    map = shacl(query, ds);
                } else if (isProbabilisticShacl(c)) {
                    map = probabilisticShacl(query, ds);
                } else if (isConstruct(c)) {
                    map = construct(query, ds);
                } else if (isSpin(c)) {
                    map = spin(query, ds);
                } else {
                    map = exec.query(query, ds);
                }
            } catch (EngineException e) {
                if (c.hasEveryValue(MES, CATCH)) {
                    // mode=message;error
                    // return empty Mappings with message/log etc.
                    Query q = exec.compile(query, ds);
                    map = Mappings.create(q);
                } else {
                    throw e;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // add param=value parameter to Context
            // Context is sent back to client as JSON message Linked Result
            // when mode=message
            Date d2 = new Date();
            double time = (d2.getTime() - d1.getTime()) / 1000.0;

            c.set(URLParam.TIME, DatatypeMap.newInstance(time));
            c.set(URLParam.DATE, DatatypeMap.newDate());

            // generate log, explanation and message as LinkedResult in map
            tsl.log(map);
            tsl.logQuery(map);

            after(exec, query, ds);
        } catch (LoadException ex) {
            throw new EngineException(ex);
        }
        return map;
    }

    void complete(Context c, HttpServletRequest request) {
        c.setService(getName());
        c.setUserQuery(true);
        c.setRemoteHost(request.getRemoteHost());
        c.set(REQUEST, DatatypeMap.createObject(request));
        String platform = getCookie(request, PLATFORM);
        if (platform != null) {
            // calling platform
            c.set(PLATFORM, platform);
        }
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
        if (ds.getContext().hasValue(EXPLAIN)) {
            exec.setDebug(true);
        }
        ds.getContext().set(QUERY, query);
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
        return c.get(URI).get(c.get(URI).size() - 1);
    }

    boolean isSpin(Context c) {
        return c.hasValue(TO_SPIN);
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

    boolean isProbabilisticShacl(Context c) {
        if(c.hasValue(PROB_SHACL) && c.hasValue(CONTENT)) return true;
        else return c.hasValue(PROB_SHACL) && hasValueList(c, URI);
    }

    boolean isConstruct(Context c) {
        return c.hasValue(CONSTRUCT);
    }

    boolean hasValueList(Context c, String name) {
        return c.hasValue(name) && c.get(name).isList() && c.get(name).size() > 0;
    }

    // federate?query=select where {}
    boolean isFederate(Dataset ds) {
        Context c = ds.getContext();
        return c.hasValue(FEDERATE);
        // && ds.getUriList() != null && !ds.getUriList().isEmpty();
    }

    Mappings spin(String query, Dataset ds) throws EngineException {
        Query q = getQueryProcess().compile(query);
        SPINProcess sp = SPINProcess.create();
        Graph g = sp.toSpinGraph(q.getAST());
        Mappings map = Mappings.create(q);
        map.setGraph(g);
        return map;
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
            throw new EngineException(ex);
        }
        Shacl sh = new Shacl(getGraph());
        sh.setDataManager(getDataManager());
        Graph res = sh.eval(shacl);
        QueryProcess exec = QueryProcess.create(res);
        exec.setDebug(ds.getContext().isDebug());
        Mappings map = exec.query(query);
        return map;
    }

    /**
     * sparql?mode=prob-shacl&param=p:[value],n-triples:[value]&query=select * where { ?s sh:conforms ?b }
     * sparql?mode=prob-shacl&uri=uri&query=select * where { ?s sh:conforms ?b }
     * Load shacl shape
     * Evaluate shape
     * Execute query on shacl validation report
     */ 
    Mappings probabilisticShacl(String query, Dataset ds) throws EngineException, IOException {
        Graph shacl = Graph.create();
        Load ld = Load.create(shacl);
        double p = 0;
        Integer nTriples = null;
        try {
            if(ds.getContext().get(URLParam.CONTENT) != null) {
                InputStream stream = new ByteArrayInputStream(ds.getContext().get(URLParam.CONTENT).stringValue().getBytes(StandardCharsets.UTF_8));
                ld.parse(stream, "", Load.TURTLE_FORMAT);
                stream.close();
            } else {
                for (IDatatype dt : ds.getContext().get(URLParam.URI)) {
                    ld.parse(dt.getLabel());
                }
            }
            // Manage params
            if(ds.getContext().get(PARAM) != null) {
                // parsing params as: p=[value];n-triples=[value]
                for (IDatatype paramDt : ds.getContext().get(PARAM).getValueList()) {
                    String param = paramDt.getLabel();
                    // p-value
                    if(param.contains(PROB_SHACL_P)) {
                        p = Double.parseDouble(param.replace(PROB_SHACL_P + ":", ""));
                    }
                    // n-triples
                    if(param.contains(PROB_SHACL_N_TRIPLES)) {
                        nTriples = Integer.parseInt(param.replace(PROB_SHACL_N_TRIPLES + ":", ""));
                    } 
                }
            }

        } catch (LoadException ex) {
            logger.error(ex.getMessage());
            throw new EngineException(ex) ;
        }
        logger.info("The prob-shacl validator will use the default p-value: p=" + p);
        Shacl sh = new Shacl(getGraph());
        sh.setDataManager(getDataManager());
        Graph res;
        if (nTriples != null) {
            logger.info("The prob-shacl validator will consider the total number of RDF triples: n-triples=" + nTriples);
            res = sh.eval(shacl, Shacl.PROBABILISTIC_MODE,
                DatatypeMap.createLiteral(String.valueOf(p), RDF.xsddouble),
                DatatypeMap.createLiteral(String.valueOf(nTriples), RDF.xsdinteger));
        } else {
            res = sh.eval(shacl, Shacl.PROBABILISTIC_MODE, DatatypeMap.createLiteral(String.valueOf(p), RDF.xsddouble));
        }
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
            throw new EngineException(ex);
        }
    }

    ASTQuery federate(String query, Dataset ds) throws EngineException {
        QueryProcess exec = getQueryProcess();
        ASTQuery ast = exec.parse(query, ds);
        // ast.setAnnotation(metadata(ast, ds));
        ast.addMetadata(metadata(ast, ds));
        logger.info("metadata: " + ast.getMetadata());
        return ast;
    }

    /**
     * SPARQL query executed as federated query on a federation of endpoints
     * Generate appropriate Metadata for AST with federation information
     */
    Metadata metadata(ASTQuery ast, Dataset ds) {
        Metadata meta = new Metadata();
        if (ds.getContext().hasValue(FEDERATE)) {
            if (ds.getUriList() == null) {
                meta.add(Metadata.FEDERATION);
            } else {
                meta.set(Metadata.FEDERATION, ds.getUriList());
            }
        }
        if (ds.getContext().hasValue(MERGE)) {
            // heuristic to merge services on the intersection of service URLs
            meta.add(Metadata.MERGE_SERVICE);
        }

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
        Enumeration<String> enh = request.getHeaderNames();
        while (enh.hasMoreElements()) {
            String name = enh.nextElement();
            System.out.println("header: " + name + ": " + request.getHeader(name));
        }
        for (Object obj : request.getParameterMap().keySet()) {
            String name = obj.toString();
            System.out.println("param: " + name + "=" + request.getParameter(name));
        }
    }

    Mappings query(HttpServletRequest request, String query) throws EngineException {
        return query(request, query, new Dataset());
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public boolean isProtect() {
        return protect;
    }

    public void setProtect(boolean protect) {
        this.protect = protect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}
