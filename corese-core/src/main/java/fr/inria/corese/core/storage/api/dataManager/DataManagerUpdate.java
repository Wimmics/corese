package fr.inria.corese.core.storage.api.dataManager;

import java.util.HashSet;
import java.util.List;

import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 * Interface to adapt an external storage system to Corese.
 * 
 * {@code DataManagerUpdate} for {@code update} and {@code construct} queries.
 * 
 * This interface is also used by the internal Corese graph.
 * 
 * @author Olivier Corby
 * @author Rémi ceres
 */
public interface DataManagerUpdate {

    /**
     * Generate a unique ID for the blanck node.
     * 
     * @return Unique ID.
     */
    default String blankNode() {
        return DatatypeMap.blankID();
    }

    /**
     * Adds one or more edges to the graph. This method creates a edge for each
     * specified context and adds those to the graph.
     * 
     * @param subject   The edge's subject, not be {@code null}.
     * @param predicate The edge's predicate, not be {@code null}.
     * @param object    The edge's object, not be {@code null}.
     * @param contexts  The contexts to add edges to, not be {@code null}.
     * @return An {@link Iterable} over all inserted edges.
     */
    default Iterable<Edge> insert(Node subject, Node predicate, Node object, List<Node> contexts) {
        if (subject == null || predicate == null || object == null || contexts == null) {
            throw new UnsupportedOperationException("Incomplete statement");
        }

        HashSet<Edge> added = new HashSet<>();

        for (Node context : contexts) {

            if (context == null) {
                throw new UnsupportedOperationException("Context can't be null");
            }

            Edge corese_edge = EdgeImpl.create(context, subject, predicate, object);
            if (this.insert(corese_edge) != null) {
                added.add(corese_edge);
            }
        }
        return added;
    }

    /**
     * Add an edges to the graph
     * 
     * In case of rdf star triple :
     * subject (resp object) may be a triple
     * edge.getSubjectNode().isTriple() == true
     * Edge triple = edge.getSubjectNode().getEdge()
     * triple may be asserted or not
     * triple.isAsserted() == true|false
     * 
     * @param edge Edge to add to the graph.
     * @return The edge inserted, null if no inserted.
     */
    default Edge insert(Edge edge) {
        return null;
    }

    /**
     * Removes edges with the specified subject, predicate, object and context exist
     * in this graph. The subject, predicate, object and contexts parameters can be
     * null to indicate wildcards. If one or more contexts are specified, edges with
     * a context matching one of these will be removed.
     * 
     * @param subject   The subject of the edges to remove, null to remove edges
     *                  with any subject.
     * @param predicate The predicate of the edges to remove, null to remove edges
     *                  with any predicate.
     * @param object    The object of the edges to remove, null to remove edges with
     *                  any object.
     * @param contexts  The contexts of the edges to remove. If contexts is
     *                  {@code null}, edges will be removed disregarding their
     *                  context. If one or more contexts are specified, edges with a
     *                  context matching one of these will be removed.
     * @return An {@link Iterable} over all removed edges.
     */
    default Iterable<Edge> delete(Node subject, Node predicate, Node object, List<Node> contexts) {
        return null;
    }

    /**
     * Removes edges with the specified subject, predicate, object and context exist
     * in this graph. The subject, predicate, object and contexts parameters can be
     * null to indicate wildcards.
     * 
     * @param edge The edge or partial edge to remove.
     * @return An {@link Iterable} over all removed edges.
     */
    default Iterable<Edge> delete(Edge edge) {
        Node graph = edge.getGraph();
        if (graph == null) {
            return this.delete(edge.getSubjectNode(), edge.getPropertyNode(), edge.getObjectNode(),
                    null);
        } else {
            return this.delete(edge.getSubjectNode(), edge.getPropertyNode(), edge.getObjectNode(),
                    List.of(edge.getGraphNode()));

        }
    }

    /**
     * Removes edges with the specified contexts. Contexts parameters can be null to
     * indicate wildcards.
     * 
     * @param contexts List of contexts to clear.
     * @param silent   If true the operation will still return success.
     * @return True if the graph has been modified or {@code silent} parameter is
     *         true, else false.
     */
    default boolean clear(List<Node> contexts, boolean silent) {
        Iterable<Edge> deleted = this.delete(null, null, null, contexts);
        Boolean succes = deleted.iterator().hasNext();

        if (silent) {
            return true;
        } else {
            return succes;
        }
    }

    /**
     * Removes all edges in graph.
     */
    default void clear() {
        this.clear(null, false);
    }

    /**
     * Add All edges from the source context to the target context.
     * 
     * @param source_context Source context.
     * @param target_context Target context where edges will be add.
     * @param silent         If true the operation will still return success.
     * @return True if the graph has been modified or {@code silent} parameter is
     *         true, else false.
     */
    default boolean addGraph(Node source_context, Node target_context, boolean silent) {
        return false;
    }

    /**
     * Clear the target context, add All edges from the source context to the target
     * context.
     * 
     * @param source_context Source context.
     * @param target_context Target context where edges will be copy.
     * @param silent         If true the operation will still return success.
     * @return True if the graph has been modified or {@code silent} parameter is
     *         true, else false.
     */
    default boolean copyGraph(Node source_context, Node target_context, boolean silent) {
        this.clear(List.of(target_context), silent);
        Boolean result = this.addGraph(source_context, target_context, silent);
        return result;
    }

    /**
     * Clear the target context, add All edges from the source context to the target
     * context and clear the source context.
     * 
     * @param source_context Source context.
     * @param target_context Target context where edges will be move.
     * @param silent         If true the operation will still return success.
     * @return True if the graph has been modified or {@code silent} parameter is
     *         true, else false.
     */
    default boolean moveGraph(Node source_context, Node target_context, boolean silent) {
        Boolean result = this.copyGraph(source_context, target_context, silent);
        this.clear(List.of(source_context), silent);
        return result;
    }

    /**
     * Declare a new context in graph.
     * 
     * @param context New context to declare.
     */
    default void declareContext(Node context) {
    }

    /**
     * Clear and undeclare a context in graph.
     * 
     * @param context
     */
    default void unDeclareContext(Node context) {
        this.clear(List.of(context), false);
    }

    /**
     * Clear and undeclare all contexts in graph.
     */
    default void unDeclareAllContexts() {
        this.clear();
    }
}
