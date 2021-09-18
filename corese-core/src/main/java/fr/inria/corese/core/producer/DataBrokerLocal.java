package fr.inria.corese.core.producer;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import java.util.List;

/**
 * Broker between ProducerImpl and corese graph 
 */
public class DataBrokerLocal implements DataBroker {
    
    private Graph graph;
    
    public DataBrokerLocal(Graph g) {
        setGraph(g);
    }
    
    /**
     * used in DataManager mode only; not in corese std mode
     * @return 
     */
    @Override
    public DataManager getDataManager() {
        return getGraph().getDefault();
    }      
    
    @Override
    public Node getNode(Node node) {
        return getGraph().getExtNode(node);
    }
    
    @Override
    public Node getProperty(Node node) {
        return getGraph().getPropertyNode(node);
    }
    
    @Override
    public  Node getGraph(Node node) {
        return getGraph().getGraphNode(node);
    }
    
    @Override
    public Node getProperty(String label) {
        return getGraph().getPropertyNode(label);
    }
    
    @Override
    public Iterable<Node> getPropertyList() {
        return getGraph().getSortedProperties();
    }
    
    @Override
    public Iterable<Node> getGraphList(List<Node> from) {
        return getGraph().getGraphNodes(from);
    }
    
    @Override
    public boolean isTypeProperty(Query query, Edge edge) {
        return (getGraph().isType(edge) || query.isRelax(edge)) && getGraph().hasEntailment();
    }
    
    
    
    @Override
    public Node getNodeCopy(Node node) {
        return getGraph().copy(node);
    }
    
    
    
    @Override
    public Iterable<Node> getDefaultNodeList() {
        return getGraph().getAllNodeIterator();
    }
    
    @Override
    public Iterable<Node> getGraphNodeList(Node node) {
        return getGraph().getNodeGraphIterator(node);
    }
    
    
    

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    
}
