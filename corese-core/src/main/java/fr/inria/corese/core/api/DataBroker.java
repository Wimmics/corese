package fr.inria.corese.core.api;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 * Broker between ProducerImpl and graph DataManager 
 * for sparql query for the where part
 * Refined by core.producer.DataBrokerLocal  for corese graph
 * Refined by core.producer.DataBrokerExtern for external DataManager
 */
public interface DataBroker {

    default DataManager getDataManager() {
        return new DataManager() {
        };
    }

    default int graphSize() {
        return getDataManager().graphSize();
    }

    default int graphSize(Node pred) {
        return getDataManager().countEdges(pred);
    }

    // from provides union of triples (select from where default graph semantics)
    default Iterable<Edge> getEdgeList(Node subject, Node predicate, Node object, List<Node> from) {
        return getDataManager().getEdges(subject, predicate, object, from);
    }

    default Node getNode(Node node) {
        return node;
    }

    default Node getProperty(Node node) {
        return node;
    }

    default Node getGraph(Node node) {
        return node;
    }

    default Node getProperty(String label) {
        return NodeImpl.create(DatatypeMap.newResource(label));
    }

    default Iterable<Node> getPropertyList() {
        return getDataManager().predicates(null);
    }

    // Named graph node iterator
    default Iterable<Node> getGraphList(List<Node> from) {
        if (from.isEmpty()) {
            return getDataManager().contexts();
        }
        return from;
    }

    default boolean isTypeProperty(Query query, Edge edge) {
        return false;
    }

    default Node getNodeCopy(Node node) {
        return node;
    }

    /**
     * @return set of subject/object of default graph
     */
    default Iterable<Node> getDefaultNodeList() {
        Iterable<Node> subjects = getDataManager().subjects(null);
        Iterable<Node> objects = getDataManager().objects(null);

        List<Node> result = new ArrayList<>();
        subjects.forEach(result::add);
        objects.forEach(result::add);

        return result;
    }

    /**
     * @return set of subject/object of named graph
     */
    default Iterable<Node> getGraphNodeList(Node graph) {
        Iterable<Node> subjects = getDataManager().subjects(graph);
        Iterable<Node> objects = getDataManager().objects(graph);

        List<Node> result = new ArrayList<>();
        subjects.forEach(result::add);
        objects.forEach(result::add);

        return result;
    }

}
