package fr.inria.corese.core.storage.api.dataManager;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface to adapt an external storage system to Corese.
 * 
 * {@code DataManagerRead} for {@code select where} SPARQL queries.
 * 
 * This interface is not used by the internal Corese graph, specific DataBroker
 * directly process it.
 * 
 * @author Olivier Corby
 * @author Rémi ceres
 */
public interface DataManagerRead {

    /**
     * Count the number of edges in the graph.
     * 
     * @return Number of edges in graph.
     */
    default int graphSize() {
        return this.countEdges(null);
    }

    /**
     * Count the number of edges with a specific predicate, {@code null} to match
     * any predicate.
     * 
     * @param predicate The predicate to count.
     * @return Number of edges with a specific predicate.
     */
    default int countEdges(Node predicate) {
        return 0;
    }

    /**
     * Retrieve occurrence of query edge in target storage.
     * Use case: edge is rdf star triple, purpose is to get its reference node
     * if any
     * 
     * @param edge The query edge to find in target storage
     * @return The target edge if any
     */
    default Edge find(Edge edge) {
        return edge;
    }

    /**
     * Returns an {@link Iterable} over all {@link Edge}s in the graph that match
     * the supplied criteria.
     * 
     * If several edges have the same subject, predicate and object then only one is
     * returned.
     * 
     * If the size of the contexts in parameter is not equal to 1, then the value of
     * Graph of Edges returned does not matter. It can be for example
     * {@code http://ns.inria.fr/corese/kgram/default}. Be careful, the value of
     * Graph of Edges cannot be {@code null}.
     * 
     * All {@code null} in context list are ignored. E.g. If the list contains only
     * null, it is handled as an empty list.
     * 
     * @param subject   The subject of the edges to match, {@code null} to match
     *                  edges with any subject.
     * @param predicate The predicate of the edges to match, {@code null} to match
     *                  edges with any predicate.
     * @param object    The object of the edges to match, {@code null} to match
     *                  edges with any object.
     * @param contexts  The contexts of the edges to match, {@code null} or empty
     *                  list to match edges with any contexts. If one or more
     *                  contexts are specified, edges with a context matching any
     *                  one of these will match.
     * @return An {@link Iterable} over all the edges in the graph that match the
     *         specified pattern.
     */
    default Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {
        return new ArrayList<>(0);
    }
    
    // with condition edge.index >= index
    // use case: rule engine transitive closure ClosureDataManager
    default Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts, int oper, int index) {
        return getEdges(subject, predicate, object, contexts);
    }
    
    // triple exist in any graph
    default boolean exist(Node subject, Node predicate, Node object) {
        for (Edge edge : getEdges(subject, predicate, object, null)) {
            return true;
        }
        return false;
    }

    /**
     * Returns an {@link Iterable} over all predicates of edges that match the
     * context without duplicates.
     * 
     * @param context Context to match, {@code null} to match with any contexts.
     * @return An {@link Iterable} over all predicates of edges that match the
     *         context.
     */
    default Iterable<Node> predicates(Node context) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all node in graph that match the context
     * without duplicates.
     * 
     * @param context Context to match, {@code null} to match with any contexts.
     * @return An {@link Iterable} over all node in graph that match the context.
     */
    default Iterable<Node> getNodes(Node context) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all contexts contained in this graph without
     * duplicates.
     * 
     * @return An {@link Iterable} over all contexts contained in this graph.
     */
    default Iterable<Node> contexts() {
        return new ArrayList<>(0);
    }
}
