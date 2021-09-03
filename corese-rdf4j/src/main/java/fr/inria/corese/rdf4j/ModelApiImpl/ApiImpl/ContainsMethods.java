package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * ContainsMethods
 */
public class ContainsMethods {

    private static final ContainsMethods instance = new ContainsMethods();

    private ContainsMethods() {
    }

    public static ContainsMethods getInstance() {
        return instance;
    }

    /**
     * Determines if statements with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match. Note:
     * to match statements without an associated context, specify the value null and
     * explicitly cast it to type Resource.
     * 
     * @param corese_graph Graph in which the statement is searched.
     * @param subj         The subject of the statements to match, null to match
     *                     statements with any subject.
     * @param predThe      predicate of the statements to match, null to match
     *                     statements with any predicate.
     * @param objThe       object of the statements to match, null to match
     *                     statements with any object.
     * @param contextsThe  contexts of the statements to match. If no contexts are
     *                     specified, statements will match disregarding their
     *                     context. If one or more contexts are specified,
     *                     statements with a context matching one of these will
     *                     match.
     * @return true if statements match the specified pattern.
     */
    public boolean containsSPO(Graph corese_graph, Resource subj, IRI pred, Value obj, Resource... contexts) {

        // get edges
        Iterable<Edge> edges = Utils.getInstance().getEdges(corese_graph, subj, pred, obj, contexts);

        // test if result is empty
        if (edges == null || edges.iterator().next() == null) {
            return false;
        }
        return true;
    }

    /**
     * Determines if a statement exist in the graph
     * 
     * @param corese_graph Graph in which the statement is searched.
     * @param o            The statement to look for.
     * @return true if statements exist in the graph, else false.
     */
    public boolean containsStatement(Graph corese_graph, Object o) {
        if (o instanceof Statement) {
            Statement st = (Statement) o;
            return this.containsSPO(corese_graph, st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
        }
        return false;
    }

    /**
     * Determines if all statements exist in a graph
     * 
     * @param corese_graph Graph in which the statement is searched.
     * @param c            The statement to look for.
     * @return true if all statements exist in the graph, else false.
     */
    public boolean containsAllStatement(Graph corese_graph, Collection<?> c) {

        Iterator<?> statements = c.iterator();
        while (statements.hasNext()) {
            if (!this.containsStatement(corese_graph, statements.next())) {
                return false;
            }
        }
        return true;
    }

}