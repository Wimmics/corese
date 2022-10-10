package fr.inria.corese.core.producer;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.proxy.GraphSpecificFunction;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DataManagerJava implements DataManager {
    private static Logger logger = LoggerFactory.getLogger(QueryProcess.class);
    
    String path;
    String iterateFunction = NSManager.USER+"iterate";
    String readFunction = NSManager.USER+"read";
    private IDatatype json;
    IDatatype joker;
    private MetadataManager metadataManager;
    private QueryProcess queryProcess;
    private Graph graph;
    
    // path of ldscript us:read and us:iterate functions
    public DataManagerJava(String path) {
        setStoragePath(path);
    }
    
    void init() {
        boolean b = false;
        try {
            setGraph(Graph.create());
            setQueryProcess(QueryProcess.create(Graph.create()));
            getQueryProcess().imports(getStoragePath());
            b = Access.skip(true);
            setJson(getQueryProcess().funcall(readFunction));
            joker = DatatypeMap.newInstance(GraphSpecificFunction.JOKER);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        finally {
            Access.skip(b);
        }
    }
    
    @Override
    public void start () {
        logger.info("Start data manager: " + getStoragePath());
        init();
    }
    
    @Override
    public Iterable<Edge> getEdges(Node s, Node p, Node o, List<Node> graphList) {
        try {
            IDatatype dt = 
            getQueryProcess().funcall(iterateFunction, getJson(), value(s), value(p), value(o));
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
    
    // list = list(list(s, p, o))
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
    
    Iterable<Edge> cast2(IDatatype list) {
        ArrayList<Edge> edgeList = new ArrayList<>();
        for (IDatatype dt : list) {
            Edge e = getGraph().create(dt.get(0), dt.get(1), dt.get(2));
            edgeList.add(e);
        }
        return edgeList;
    }
    Iterable<Edge> iterate(Node s, Node p, Node o, List<Node> list) {
        return new ArrayList<>(0);
    }
    
    
    
    public void setStoragePath(String path) {
        this.path = path;
    }
    
    @Override
    public String getStoragePath() {
        return path;
    }

    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
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

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    
}
