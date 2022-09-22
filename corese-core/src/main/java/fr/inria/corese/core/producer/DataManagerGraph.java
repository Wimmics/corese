package fr.inria.corese.core.producer;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataManager for corese Graph for testing purpose
 */
public class DataManagerGraph implements DataManager {
    static final String STORAGE_PATH = "http://ns.inria.fr/corese/dataset";
    private static Logger logger = LoggerFactory.getLogger(DataManagerGraph.class);
    
    List<Node> emptyNodeList;
    List<Edge> emptyEdgeList;
    private Graph graph;
    private MetadataManager metadataManager;
    private String path = STORAGE_PATH;

    DataManagerGraph() {
        emptyNodeList = new ArrayList<>(0);    
        emptyEdgeList = new ArrayList<>(0);    
    }
    
    public DataManagerGraph(Graph g) {
        this();
        init(g);
    }

    void init(Graph g) {
        setGraph(g);
    }
    
    void trace(String mes) {
        logger.info(mes);
    }
    
    @Override
    public void startReadTransaction() {
        trace("start read transaction");
    };


    @Override
    public void endReadTransaction() {
        trace("end read transaction");
    };

  
    @Override
    public void startWriteTransaction() {
        trace("start write transaction");
        getGraph().init();
    };

   
    @Override
    public void endWriteTransaction() {
        getGraph().init();
        trace("end write transaction");
    };
    
    
    @Override
    public String getStoragePath() {
        return null;
    }

    ;    
    
    

    // from.size == 1 -> named graph semantics
    // else           -> public graph semantics
    @Override
    public Iterable<Edge> getEdges(
            Node subject, Node predicate, Node object, List<Node> from) {
        DataProducer dp ;
        //trace(String.format("%s %s %s %s", from, subject, predicate, object));
        if (from!=null&&from.size()==1) {
            dp = getGraph().getDataStore().getNamed(from, null);
        } else {
            dp = getGraph().getDataStore().getDefault(from);
        }
        return dp.getEdges(subject, predicate, object, from);
    }
    
    

    @Override
    public int graphSize() {
        return getGraph().size();
    }


    @Override
    public int countEdges(Node predicate) {
        return getGraph().size(predicate);
    }
    
    /**
     * Retrieve occurrence of query edge in target storage.
     * Use case: edge is rdf star triple, purpose is to get its reference node
     * if any
     * @param edge The query edge to find in target storage
     * @return The target edge if any
     */
    @Override
    public Edge find(Edge edge) {
        return getGraph().find(edge);
    }


    // in practice, context is not used ...
    @Override
    public Iterable<Node> predicates(Node context) {
        return getGraph().getSortedProperties();
    }


    
    @Override
    public Iterable<Node> contexts() {
        return getGraph().getGraphNodes(emptyNodeList);
    }
    
    

    @Override
    public Iterable<Edge> insert(Node s, Node p, Node o, List<Node> contexts) {
        if (contexts==null||contexts.isEmpty()) {
            Edge edge = getGraph().insert(s, p, o);
        }
        else {
            for (Node g : contexts) {
                Edge edge = getGraph().insert(g, s, p, o);
            }
        }
        return emptyEdgeList;
    }


    @Override
    public Edge insert(Edge edge) {
        return getGraph().insert(
                edge.getGraphNode(),
                edge.getSubjectNode(), 
                edge.getPropertyNode(),
                edge.getObjectNode());
    }

    
    @Override
    public Iterable<Edge> delete(Node s, Node p, Node o, List<Node> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            List<Edge> edge = getGraph().delete(s, p, o);
        } else {
            for (Node g : contexts) {
                List<Edge> edge = getGraph().delete(g, s, p, o);
            }
        }
        return emptyEdgeList;
    }

    
    @Override
    public Iterable<Edge> delete(Edge edge) {
        return getGraph().delete(
                edge.getGraph(), edge.getSubjectNode(),
                edge.getPropertyNode(),
                edge.getObjectNode());
    }

    @Override
    public boolean clear(List<Node> contexts, boolean silent) {
        for (Node g : contexts) {
            getGraph().clear(g.getLabel(), silent);
        }
        return true;
    }

    /**
     * Removes all edges in graph.
     */
    @Override
    public void clear() {
        getGraph().clear();
        getGraph().dropGraphNames();
    }


    @Override
    public boolean add(Node source_context, Node target_context, boolean silent) {
        return getGraph().add(source_context.getLabel(), target_context.getLabel(), silent);
    }

    
    @Override
    public boolean copy(Node source_context, Node target_context, boolean silent) {
        return getGraph().copy(source_context.getLabel(), target_context.getLabel(), silent);
    }

    
    @Override
    public boolean move(Node source_context, Node target_context, boolean silent) {
        return getGraph().move(source_context.getLabel(), target_context.getLabel(), silent);
    }

    /**
     * Declare a new context in graph.
     * 
     * @param context New context to declare.
     */
    @Override
    public void declareContext(Node context) {
        getGraph().addGraphNode(context);
    }


    @Override
    public void unDeclareContext(Node context) {
        this.clear(List.of(context), false);
    }

   
    @Override
    public void unDeclareAllContexts() {
        this.clear();
    }
    


    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
    
    @Override
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }

    @Override
    public void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
