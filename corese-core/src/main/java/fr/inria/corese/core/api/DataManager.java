package fr.inria.corese.core.api;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface to adapt an external graph implementation to Corese.
 * 
 * {@code DataManager} for {@code select where} SPARQL queries.
 * {@code DataManagerUpdate} for {@code update} and {@code construct} queries.
 * 
 * This interface is not used by the internal Corese graph, specific DataBroker
 * directly process it.
 * 
 * @author Olivier Corby
 * @author Rémi ceres
 */
public interface DataManager extends DataManagerUpdate {

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
     * @param predicate The predicate to count
     * @return Number of edges with a specific predicate
     */
    default int countEdges(Node predicate) {
        return 0;
    }

    /**
     * Returns an {@link Iterable} over all {@link Edge}s in the graph that match
     * the supplied criteria. If several edges have the same subject, predicate and
     * object then only one is returned.
     * 
     * @param subject   The subject of the edges to match, {@code null} to match
     *                  edges with any subject.
     * @param predicate The predicate of the edges to match, {@code null} to match
     *                  edges with any predicate.
     * @param object    The object of the edges to match, {@code null} to match
     *                  edges with any object.
     * @param contexts  The contexts of the edges to match, {@code null} to match
     *                  edges with any contexts. If one or more contexts are
     *                  specified, edges with a context matching any one of these
     *                  will match.
     * @return An {@link Iterable} over all the edges in the graph that match the
     *         specified pattern.
     */
    default Iterable<Edge> getEdges(Node subject, Node predicate, Node object, List<Node> contexts) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all subjects of edges that match the
     * context.
     * 
     * @param context Context to match, {@code null} to match with any contexts.
     * @return An {@link Iterable} over all subjects of edges that match the
     *         context.
     */
    default Iterable<Node> subjects(Node context) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all predicates of edges that match the
     * context.
     * 
     * @param context Context to match, {@code null} to match with any contexts.
     * @return An {@link Iterable} over all predicates of edges that match the
     *         context.
     */
    default Iterable<Node> predicates(Node context) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all objects of edges that match the context.
     * 
     * @param context Context to match, {@code null} to match with any contexts.
     * @return An {@link Iterable} over all objects of edges that match the context.
     */
    default Iterable<Node> objects(Node context) {
        return new ArrayList<>(0);
    }

    /**
     * Returns an {@link Iterable} over all contexts contained in this graph.
     * 
     * @return An {@link Iterable} over all contexts contained in this graph.
     */
    default Iterable<Node> contexts() {
        return new ArrayList<>(0);
    }
}
