package fr.inria.corese.core.producer;

import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataBroker;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;

/**
 * Broker between ProducerImpl and corese graph
 */
public class DataBrokerLocal implements DataBroker {

    private Graph graph;

    public DataBrokerLocal(Graph g) {
        setGraph(g);
    }

    @Override
    public int graphSize() {
        return getGraph().size();
    }

    @Override
    public int graphSize(Node pred) {
        return getGraph().size(pred);
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
    public Node getGraph(Node node) {
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

    // ?s rdf:type aClass with corese RDFS entailment
    // do not focus on aClass because RDFS entailment
    // does not generate transitive closure
    // hence another class may match aClass
    // in this case return true
    @Override
    public boolean isTypeProperty(Query query, Edge edge) {
        return (getGraph().isType(edge) || query.isRelax(edge))
                && (getGraph().hasEntailment() || query.isRelax());
    }

    @Override
    public Node getNodeCopy(Node node) {
        return getGraph().copy(node);
    }

    /**
     * iterable NodeGraph(node, graph)
     * return all pairs (node, graph) for sparql PP*
     * use case: graph ?g { s pp* o }
     * 
     */
    @Override
    public Iterable<Node> getDefaultNodeList() {
        return getGraph().getAllNodeIterator();
    }

    // iterable of NodeGraph(node, graph)
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

    @Override
    public DataManager getDataManager() {
        return null;
    }

}
