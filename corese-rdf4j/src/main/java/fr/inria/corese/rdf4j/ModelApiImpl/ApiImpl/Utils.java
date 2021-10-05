package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.rdf4j.ModelApiImpl.CoreseModel;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class Utils {

    private static final Utils instance = new Utils();

    private Utils() {
    }

    public static Utils getInstance() {
        return instance;
    }

    /**
     * Convert a RDF4J value to a Corese node.
     * 
     * @param rdf4j_value RDF4J value to convert.
     * @return Equivalent Corese node or null if RDF4J value is null.
     */
    private Node convertRdf4jValueToCoreseNode(Value rdf4j_value) {
        if (rdf4j_value == null) {
            return null;
        }

        return NodeImpl.create(Rdf4jValueToCoreseDatatype.convert(rdf4j_value));
    }

    /**
     * Convert an array of RDF4J context to an equivalent array of Corese node
     * graph.
     * 
     * @param default_graph Value of the default graph.
     * @param contexts      Array of RDF4J context to convert.
     * @return Equivalent array of Corese node, null if RDF4J context is empty, the
     *         default graph if RDF4J context is null.
     */
    private Node[] convertRdf4jContextsToCorese(Node default_graph, Resource... contexts) {

        // if the contexts is null then the context is the default context
        if (contexts == null) {
            return new Node[] { default_graph };
        }

        // if the contexts is empty then there is no context specified
        if (contexts.length == 0) {
            return null;
        }

        // convert rdf4j context to Corese graph
        Set<Node> corese_contexts = new HashSet<Node>();
        for (Resource context : contexts) {
            if (context != null) {
                corese_contexts.add(this.convertRdf4jValueToCoreseNode(context));
            } else {
                corese_contexts.add(default_graph);
            }
        }

        // convert arraylist to java array
        Node[] corese_contexts_jarray = new Node[corese_contexts.size()];
        return corese_contexts.toArray(corese_contexts_jarray);
    }

    /**
     * Return statements with the specified subject, predicate, object and
     * (optionally) context exist in this model. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match. Note:
     * to match statements without an associated context, specify the value null and
     * explicitly cast it to type Resource.
     * 
     * @param corese_model Corese model in which statements are searched.
     * @param subj         The subject of the statements to match, null to match
     *                     statements with any subject.
     * @param predThe      Predicate of the statements to match, null to match
     *                     statements with any predicate.
     * @param objThe       Object of the statements to match, null to match
     *                     statements with any object.
     * @param contextsThe  Contexts of the statements to match. If no contexts are
     *                     specified, statements will match disregarding their
     *                     context. If one or more contexts are specified,
     *                     statements with a context matching one of these will
     *                     match.
     * @return Iterable of Statement that match the specified pattern.
     */
    public Iterable<Statement> getEdges(Graph corese_graph, Resource subj, IRI pred, Value obj, Resource... contexts) {

        // convert subject, predicate, object into Corese Node
        Node subj_node = this.convertRdf4jValueToCoreseNode(subj);
        Node pred_node = this.convertRdf4jValueToCoreseNode(pred);
        Node obj_node = this.convertRdf4jValueToCoreseNode(obj);

        // convert contexts into Corese Nodes
        Node[] contexts_node = this.convertRdf4jContextsToCorese(corese_graph.getDefaultGraphNode(), contexts);

        // get edges
        Iterable<Edge> corese_iterable;
        if (contexts_node == null) {
            corese_iterable = corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node);
        } else {
            corese_iterable = corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node, contexts_node);
        }

        // Create a new clean iterable (because corse iterable does not have a perfectly
        // defined behavior for optimization reasons)
        ArrayList<Statement> result = new ArrayList<>();
        for (Edge edge : corese_iterable) {
            if (edge != null) {
                result.add(corese_graph.getEdgeFactory().copy(edge));
            }
        }

        return result;
    }

    /**
     * Get a Corese model iterator with Statements that match the specified subject,
     * predicate, object and (optionally) context. The subject, predicate and object
     * parameters can be null to indicate wildcards. The contexts parameter is a
     * wildcard and accepts zero or more values. If no contexts are specified,
     * statements will match disregarding their context. If one or more contexts are
     * specified, statements with a context matching one of these will match. Note:
     * to match statements without an associated context, specify the value null and
     * explicitly cast it to type Resource.
     * 
     * @param corese_model Corese model on which iterated.
     * @param subj         The subject of the statements to match, null to match
     *                     statements with any subject.
     * @param pred         The Predicate of the statements to match, null to match
     *                     statements with any predicate.
     * @param obj          The Object of the statements to match, null to match
     *                     statements with any object.
     * @param contexts     The Contexts of the statements to match. If no contexts
     *                     are specified, statements will match disregarding their
     *                     context. If one or more contexts are specified,
     *                     statements with a context matching one of these will
     *                     match.
     * @return Corese model iterator with Statements that match the specified
     *         subject, predicate, object and (optionally) context.
     */
    public Iterator<Statement> getFilterIterator(CoreseModel corese_model, Resource subj, IRI pred, Value obj,
            Resource... contexts) {

        /**
         * Iterator for the Corese model
         */
        class CoreseModelIterator implements Iterator<Statement> {

            private Iterator<Statement> iter;

            private Statement last;

            public CoreseModelIterator(Iterator<Statement> iter) {
                this.iter = iter;
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Statement next() {
                return last = iter.next();
            }

            @Override
            public void remove() {
                if (last == null) {
                    throw new IllegalStateException();
                }
                corese_model.remove(last);
                iter.remove();
            }
        }

        // get edges
        Iterable<Statement> edges = Utils.getInstance().getEdges(corese_model.getCoreseGraph(), subj, pred, obj,
                contexts);
        return new CoreseModelIterator(edges.iterator());
    }

}
