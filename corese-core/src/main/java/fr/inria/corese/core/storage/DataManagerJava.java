package fr.inria.corese.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.proxy.GraphSpecificFunction;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.HashMapList;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLServer;

/**
 * DataManager on top of json (or xml) document
 *
 * a) implemented in java as graph data manager
 * path = path of insert where query that creates a graph (from json, xml, etc.)
 * update query creates graph data manager
 * 
 * b) implemented in ldscript when path contains ldscript (draft for testing)
 * path = path of ldscript function definition
 * 1- read (json) file with us:read() ldscript determine json file
 * 2- iterate edges with ldscript function us:iterate()
 */
public class DataManagerJava extends CoreseGraphDataManager {
    private static Logger logger = LoggerFactory.getLogger(DataManagerJava.class);
    private static final String QUERY = "query";
    private static final String PATH = "path";
    private static final String PARAM = "param";
    private static final String MODE = "mode";
    private static final String LOAD = "load";
    private static final String LDSCRIPT = "ldscript";

    String iterateFunction = NSManager.USER + "iterate";
    String readFunction = NSManager.USER + "read";
    // json document
    private IDatatype jsonDocument;
    IDatatype joker;
    private QueryProcess queryProcess;
    private boolean ldscript = false;
    String queryPath;
    private String query;
    private HashMapList<String> map;
    private List<String> load;

    // path of insert where query that creates a graph (from json or xml)
    // or
    // path of ldscript us:read and us:iterate functions
    public DataManagerJava(String path) {
        URLServer url = new URLServer(path);
        // remove parameter if any
        setPath(url.getServer());
        setQueryPath(url.getServer());
    }

    // creation time in StorageFactory
    @Override
    public void start(HashMapList<String> map) {
        logger.info("Start data manager: " + getStoragePath());
        if (map == null) {
            init();
        } else {
            boolean isInit = localInit(map);
            if (!isInit) {
                init();
            }
        }
    }

    // path parameter map
    // called by service store clause in ProviderService
    @Override
    public void init(HashMapList<String> map) {
        localInit(map);
    }

    boolean localInit(HashMapList<String> map) {
        super.init(map);
        return parameter(map);
    }

    // return true when init() is performed
    boolean parameter(HashMapList<String> map) {
        setMap(map);
        String queryPath = map.getFirst(PATH);
        if (map.containsKey(MODE) && map.get(MODE).contains(LDSCRIPT)) {
            setLdscript(true);
        }
        if (map.containsKey(LOAD)) {
            setLoad(map.get(LOAD));
        }
        if (queryPath != null) {
            setQueryPath(queryPath);
            setQuery(null);
            logger.info("Service query path= " + queryPath);
            init();
            return true;
        } else {
            String query = map.getFirst(QUERY);
            if (query != null) {
                query = clean(query);
                setQuery(query);
                setQueryPath(null);
                logger.info("Service query = " + query);
                init();
                return true;
            }
        }
        return false;
    }

    String clean(String str) {
        return str.replace("%20", " ");
    }

    //@Override
    void init() {
        if (isLdscript()) {
            initldscript();
        } else {
            initgraph();
        }
    }

    // create graph from json using update query located at path
    // set this data manager graph
    void initgraph() {
        logger.info("Mode graph");
        // graph to be created by update query
        setGraph(Graph.create());
        //setQueryProcess(QueryProcess.create(getGraph()));
        setQueryProcess(QueryProcess.create(this));
        QueryLoad ql = QueryLoad.create();
        Load ld = Load.create(getGraph());
        ld.setDataManager(this);
        // temporary authorize xt:read file to read e.g. json document 
        Level read     = Access.setValue(Feature.READ, Level.DEFAULT);
        Level readFile = Access.setValue(Feature.READ_FILE, Level.DEFAULT);

        try {
            if (getLoad()!=null) {
                for (String name : getLoad()) {
                    logger.info("Load " + name);
                    ld.parse(name);
                }
            }
            // query who creates rdf graph (from json)
            String q;
            if (getQueryPath() != null) {
                logger.info("Load " + getQueryPath());
                q = ql.readWE(getQueryPath());
            } else if (getQuery() != null) {
                q = getQuery();
            } else {
                return;
            }

            if (getMap() != null && getMap().containsKey(PARAM)) {
                q = String.format(q, getMap().getFirst(PARAM));
            }
            logger.info("Process query:\n" + q);
            // update query creates rdf graph (from json)
            // this is graph of current DataManager
            Mappings map = getQueryProcess().query(q);
            if (map.getGraph() != null) {
                // construct where query
                setGraph((Graph) map.getGraph());
            }
            getGraph().init();
        } catch (LoadException | EngineException ex) {
            logger.error(ex.getMessage());
        }
        finally {
            Access.set(Feature.READ, read);
            Access.set(Feature.READ_FILE, readFile);
        }
    }

    // load json using ldscript us:read() function
    // defined at path
    // iterate edges from json using ldscript us:iterate() function
    void initldscript() {
        logger.info("Mode json");
        try {
            setGraph(Graph.create());
            setQueryProcess(QueryProcess.create(Graph.create()));
            getQueryProcess().imports(getQueryPath());
            setJsonDocument(getQueryProcess().funcall(readFunction));
            joker = DatatypeMap.newInstance(GraphSpecificFunction.JOKER);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public Iterable<Edge> getEdges(Node s, Node p, Node o, List<Node> graphList) {
        if (isLdscript()) {
            return iterateJson(s, p, o, graphList);
        }
        return super.getEdges(s, p, o, graphList);
    }

    Iterable<Edge> iterateJson(Node s, Node p, Node o, List<Node> graphList) {
        try {
            IDatatype dt = getQueryProcess().funcall(iterateFunction, getJsonDocument(), value(s), value(p), value(o));
            if (dt == null) {
                return new ArrayList<>(0);
            }
            return cast(dt);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return new ArrayList<>(0);
    }

    IDatatype value(Node n) {
        if (n == null) {
            return joker;
        }
        return n.getDatatypeValue();
    }

    // list of triple reference, result of triple(s, p, o)
    Iterable<Edge> cast(IDatatype list) {
        ArrayList<Edge> edgeList = new ArrayList<>();
        for (IDatatype dt : list) {
            edgeList.add(dt.getEdge());
        }
        return edgeList;
    }

    boolean filter(IDatatype dt, Node s, Node p, Node o) {
        Edge e = dt.getEdge();
        if (isJoker(p)) {
            return true;
        }
        return e.getPropertyNode().equals(p);
    }

    boolean isJoker(Node n) {
        return n.getDatatypeValue().equals(joker);
    }

    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

    public IDatatype getJsonDocument() {
        return jsonDocument;
    }

    public void setJsonDocument(IDatatype json) {
        this.jsonDocument = json;
    }

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String path) {
        queryPath = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public HashMapList<String> getMap() {
        return map;
    }

    public void setMap(HashMapList<String> map) {
        this.map = map;
    }

    public boolean isLdscript() {
        return ldscript;
    }

    public void setLdscript(boolean ldscript) {
        this.ldscript = ldscript;
    }

    public List<String> getLoad() {
        return load;
    }

    public void setLoad(List<String> load) {
        this.load = load;
    }

}
