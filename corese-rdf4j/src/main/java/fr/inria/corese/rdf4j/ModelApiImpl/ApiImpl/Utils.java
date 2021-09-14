package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.EdgeFactory;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
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
     * @param rdf4j_value Rdf4j value to translate.
     * @return Equivalent Corese node or null if Rdf4j value is null.
     */
    private Node convertRdf4jValueToCoreseNode(Value rdf4j_value) {

        if (rdf4j_value == null) {
            return null;
        }

        return NodeImpl.create(Rdf4jValueToCoreseDatatype.convert(rdf4j_value));
    }

    /**
     * Convert an array of RDF4J context to an array of Corese Node of graph.
     * 
     * @param default_graph Value of the default graph.
     * @param contexts      Array of RDF4J context to convert.
     * @return Equivalent array of Corese Node, null if RDF4J context is empty or
     *         the default graph is RDF4J context is null .
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
        ArrayList<Node> corese_contexts = new ArrayList<Node>();
        for (Resource context : contexts) {
            // i don't want null value into corese_contexts
            if (context != null) {
                corese_contexts.add(this.convertRdf4jValueToCoreseNode(context));
            }
        }

        // if all contexts are null then the context is the default context
        if (corese_contexts.isEmpty()) {
            return new Node[] { default_graph };
        } else {
            // convert arraylist to java array
            Node[] corese_contexts_jarray = new Node[corese_contexts.size()];
            return corese_contexts.toArray(corese_contexts_jarray);
        }
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
     * @return Iterable of Edge that match the specified pattern.
     */
    public Iterable<Edge> getEdges(Graph corese_graph, Resource subj, IRI pred, Value obj,
            Resource... contexts) {

        // convert subject, predicate, object into Corese Node
        Node subj_node = this.convertRdf4jValueToCoreseNode(subj);
        Node pred_node = this.convertRdf4jValueToCoreseNode(pred);
        Node obj_node = this.convertRdf4jValueToCoreseNode(obj);

        // convert contexts into Corese Nodes
        Node[] contexts_node = this.convertRdf4jContextsToCorese(corese_graph.getDefaultGraphNode(), contexts);

        // get edges
        Iterable<Edge> result;
        if (contexts_node == null) {
            result = corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node);
        } else {
            result = corese_graph.getEdgesRDF4J(subj_node, pred_node, obj_node, contexts_node);
        }
        return result;
    }

    /**
     * Convert an iterator of edge into iterable of statement.
     * 
     * @param edge_factory Edge factory.
     * @param edges        Iterator of edge to convert.
     * @return Iterable of statement equivalent.
     */
    public Iterable<Statement> convertItEdgeToItStatement(EdgeFactory edge_factory, Iterator<Edge> edges) {

        List<Statement> result = new ArrayList<>();
        while (edges.hasNext()) {
            Edge edge_copy = edge_factory.copy(edges.next());
            result.add(edge_copy);
        }

        return result;
    }
}
