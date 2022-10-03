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
    
    
    @Override
    public void startReadTransaction() {
        getGraph().init();
        trace("start read transaction " + getPath());
    }


    @Override
    public void endReadTransaction() {
        trace("end read transaction "+ getPath());
    }

  
    @Override
    public void startWriteTransaction() {
        trace("start write transaction "+ getPath());
        getGraph().init();
    }

   
    @Override
    public void endWriteTransaction() {
        getGraph().init();
        trace("end write transaction "+ getPath());
    }
    
    
    @Override
    public String getStoragePath() {
        return getPath();
    }

       
    
    

    // from.size == 1 -> named graph semantics
    // else           -> default graph semantics
    @Override
    public Iterable<Edge> getEdges(
            Node subject, Node predicate, Node object, List<Node> from) {
        return getGraph().iterate(subject, predicate, object, from);
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
    public Iterable<Node> getNodes(Node context) {
        if (context == null) {
            return getGraph().getNodeGraphIterator();
        }
        return getGraph().getNodeGraphIterator(getGraph().getNode(context));
    }
    
    

    @Override
    public Iterable<Edge> insert(Node s, Node p, Node o, List<Node> contexts) {
        return getGraph().insert(s, p, o, contexts);
    }


    // @todo: rdf star
    @Override
    public Edge insert(Edge edge) {
        Edge res = getGraph().insertEdgeWithTargetNode(edge);
        return res;
    }

    
    @Override
    public Iterable<Edge> delete(Node s, Node p, Node o, List<Node> contexts) {
        return getGraph().delete(s, p, o, contexts);
    }

    
    @Override
    public Iterable<Edge> delete(Edge edge) {
        return getGraph().deleteEdgeWithTargetNode(edge);
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
