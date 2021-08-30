package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

public class RemoveMethods {

    /**
     * Removes statements with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will be removed disregarding their context. If one or more
     * contexts are specified, statements with a context matching one of these will
     * be removed. Note: to remove statements without an associated context, specify
     * the value null and explicitly cast it to type Resource.
     * 
     * @param corese_graph Graph in which statements are rermove.
     * @param subj         The subject of the statements to remove, null to remove
     *                     statements with any subject.
     * @param pred         The predicate of the statements to remove, null to remove
     *                     statements with any predicate.
     * @param pred
     * @param obj          The object of the statements to remove, null to remove
     *                     statements with any object.
     * @param contexts     The contexts of the statements to remove. If no contexts
     *                     are specified, statements will be removed disregarding
     *                     their context. If one or more contexts are specified,
     *                     statements with a context matching one of these will be
     *                     removed.
     * @return true if one or more statements have been removed.
     */
    public static boolean removeSPO(Graph corese_graph, Resource subj, IRI pred, Value obj, Resource... contexts) {

        // get edges
        ArrayList<Edge> edges = new ArrayList<>();
        for (Edge edge : Utils.getEdges(corese_graph, subj, pred, obj, contexts)) {
            if (edge != null) {
                edges.add(corese_graph.getEdgeFactory().copy(edge));
            }
        }

        // remove edges
        boolean change = false;
        for (Edge edge : edges) {
            List<Edge> delete_edge = corese_graph.delete(edge);
            change |= delete_edge.size() != 0;
        }
        return change;
    }

    /**
     * Remove one statement to the graph.
     * 
     * @param corese_graph Graph in which the statement is removed.
     * @param o            Statement to remove.
     * @return True if the graph is modify, else false.
     */
    public static boolean removeStatement(Graph corese_graph, Object o) {
        if (o instanceof Statement) {
            if (OtherMethods.isEmpty(corese_graph)) {
                return false;
            }
            Statement st = (Statement) o;
            return RemoveMethods.removeSPO(corese_graph, st.getSubject(), st.getPredicate(), st.getObject(),
                    st.getContext());
        }
        return false;
    }

    /**
     * Remove all statements to the graph.
     * 
     * @param corese_graph Graph in which statements are removed.
     * @param c            Collection of statements to remove
     * @return True if the graph is modify, else false.
     */
    public static boolean removeAll(Graph corese_graph, Collection<?> c) {
        boolean modified = false;
        Iterator<?> i = c.iterator();
        while (i.hasNext()) {
            modified |= RemoveMethods.removeStatement(corese_graph, i.next());
        }
        return modified;
    }
}