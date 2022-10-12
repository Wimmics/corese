package fr.inria.corese.core.producer;

import fr.inria.corese.core.Graph;
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
import fr.inria.corese.sparql.triple.parser.HashMapList;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DataManagerJava extends DataManagerGraph  {
    private static Logger logger = LoggerFactory.getLogger(QueryProcess.class);
    private static final String QUERY = "query";
    private static final String TRUE = "true";
    
    String iterateFunction = NSManager.USER+"iterate";
    String readFunction    = NSManager.USER+"read";
    // json document 
    private IDatatype json;
    IDatatype joker;
    private QueryProcess queryProcess;
    boolean isJson = true;
    String queryPath;
    
    // path of insert where query that creates a graph (from json or xml)  
    // or
    // path of ldscript us:read and us:iterate functions
    public DataManagerJava(String path) {
        URLServer url = new URLServer(path);
        // remove parameter if any
        setPath(url.getServer());
        setQueryPath(url.getServer());
        // draft trick to determine ldscript|graph
        isJson = path.contains("ldscript");
    }
    
    @Override
    public void start () {
        logger.info("Start data manager: " + getStoragePath());
        init();
    }
    
    // path parameter map 
    @Override
    public void init(HashMapList<String> map) {
        super.init(map);
        parameter(map);
    }
    
    void parameter(HashMapList<String> map) {
        String query = map.getFirst(QUERY);
        if (query!=null) {
            setQueryPath(query);
            logger.info("Service query = " + query);
            initgraph();
        }             
    }
    
    void init() {
        if (isJson) {
            initjson();
        }
        else {
            initgraph();
        }
    }
    
    
    // create graph from json using update query located at path 
    // set this data manager graph 
    void initgraph() {
        // graph to be created by update query
        setGraph(Graph.create());
        setQueryProcess(QueryProcess.create(getGraph()));
        QueryLoad ql = QueryLoad.create();
        try {
            // path of update query who creates rdf graph (from json) 
            String q = ql.readWE(getQueryPath());
            // update query creates rdf graph (from json)
            // this is graph of current DataManager
            Mappings map = getQueryProcess().query(q);
            if (map.getGraph() != null) {
                // construct where query
                setGraph((Graph) map.getGraph());
            }
            getGraph().init();
        } catch (LoadException|EngineException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    // load json using ldscript us:read() function
    // defined at path
    // iterate edges from json using ldscript us:iterate() function
    void initjson() {
        try {
            setGraph(Graph.create());
            setQueryProcess(QueryProcess.create(Graph.create()));
            getQueryProcess().imports(getQueryPath());
            setJson(getQueryProcess().funcall(readFunction));
            joker = DatatypeMap.newInstance(GraphSpecificFunction.JOKER);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }       
    }    
    
    
    
    @Override
    public Iterable<Edge> getEdges(Node s, Node p, Node o, List<Node> graphList) {
        if (isJson) {
            return iterateJson(s, p, o, graphList);
        }
        return super.getEdges(s, p, o, graphList);
    }
       
    Iterable<Edge> iterateJson(Node s, Node p, Node o, List<Node> graphList) {
        try {
            IDatatype dt
                    = getQueryProcess().funcall(iterateFunction, getJson(), value(s), value(p), value(o));
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
    
    boolean filter (IDatatype dt, Node s, Node p, Node o) {
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

    public IDatatype getJson() {
        return json;
    }

    public void setJson(IDatatype json) {
        this.json = json;
    }
    
    public String getQueryPath() {
        return queryPath;
    }   
    
    public void setQueryPath(String path) {
        queryPath = path;
    }   
    
}
