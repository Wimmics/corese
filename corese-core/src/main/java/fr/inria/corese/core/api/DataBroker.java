package fr.inria.corese.core.api;

import fr.inria.corese.core.Graph;
import java.util.List;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 * Broker between ProducerImpl and graph DataManager
 * for sparql query for the where part
 * Refined by core.producer.DataBrokerLocal for corese graph
 * Refined by core.producer.DataBrokerExtern for external DataManager
 */
public interface DataBroker {

    DataManager getDataManager();

    default int graphSize() {
        return this.getDataManager().graphSize();
    }

    default int graphSize(Node pred) {
        if (Graph.isTopRelation(pred)) {
            return graphSize();
        }
        return this.getDataManager().countEdges(pred);
    }

    // from provides union of triples (select from where default graph semantics)
    // when from.size != 1, it is used for default graph AND named graph semantics
    // and we cannot distinguish the case here
    // when from.size == 1, it MUST return quads with the uri of the named graph
    default Iterable<Edge> getEdgeList(Node subject, Node predicate, Node object, List<Node> from) {
        return this.getDataManager().getEdges(subject, predicate, object, from);
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
        return this.getDataManager().predicates(null);
    }

    // Named graph node iterator
    default Iterable<Node> getGraphList(List<Node> from) {
        if (from.isEmpty()) {
            return this.getDataManager().contexts();
        }
        return from;
    }

    // return true when edge is rdf:type and query is relax
    default boolean isTypeProperty(Query query, Edge edge) {
        return query.isRelax() && edge.getEdgeLabel().equals(RDF.TYPE);
    }

    default Node getNodeCopy(Node node) {
        return node;
    }

    /**
     * @return set of subject/object of default graph
     */
    default Iterable<Node> getDefaultNodeList() {
        return this.getDataManager().getNodes(null);
    }

    /**
     * @return set of subject/object of named graph
     */
    default Iterable<Node> getGraphNodeList(Node graph) {
        return this.getDataManager().getNodes(graph);
    }

}
