package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.Iterator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

public class OtherMethods {

    private static final OtherMethods instance = new OtherMethods();

    private OtherMethods() {
    }

    public static OtherMethods getInstance() {
        return instance;
    }

    /**
     * Test is the graph is empty.
     * 
     * @param corese_graph The graph to test.
     * @return True if the graph is empty, else false.
     */
    public boolean isEmpty(Graph corese_graph) {
        return !ContainsMethods.getInstance().containsSPO(corese_graph, null, null, null);
    }

    /**
     * Return the size of the graph.
     * 
     * @param corese_graph The graph to evaluate.
     * @return Size of the graph.
     */
    public int size(Graph corese_graph) {
        return corese_graph.size();
    }

    /**
     * Returns an iterator over the elements in this graph.
     * 
     * @param corese_graph The graph to iterate.
     * @return Iterator over the elements in this graph.
     */
    public Iterator<Statement> iterator(Graph corese_graph) {
        Iterator<Edge> statements = corese_graph.iterator();
        return Utils.getInstance().convertItEdgeToItStatement(corese_graph.getEdgeFactory(), statements).iterator();
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
     * @param pred         The predicate of the statements to match, null to match
     *                     statements with any predicate.
     * @param obj          The object of the statements to match, null to match
     *                     statements with any object.
     * @param contexts     The contexts of the statements to match. If no contexts
     *                     are specified, statements will match disregarding their
     *                     context. If one or more contexts are specified,
     *                     statements with a context matching one of these will
     *                     match.
     * @return Iterator of statement that match the specified pattern.
     */
    public Iterable<Statement> getStatements(Graph corese_graph, Resource subj, IRI pred, Value obj,
            Resource... contexts) {
        Iterable<Edge> edges = Utils.getInstance().getEdges(corese_graph, subj, pred, obj, contexts);
        return Utils.getInstance().convertItEdgeToItStatement(corese_graph.getEdgeFactory(), edges.iterator());
    }

}
