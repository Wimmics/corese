package fr.inria.corese.rdf4j;

import java.util.ArrayList;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.rdf4j.CoreseDatatypeToRdf4jValue;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class Convert {

    private Convert() {
    }

    private static Node corese_default_context = DatatypeMap.createResource(ExpType.DEFAULT_GRAPH);

    /***************************
     * Value : Rdf4j to Corese *
     ***************************/

    /**
     * Convert a RDF4J value to a Corese node.
     * 
     * @param rdf4j_value RDF4J value to convert.
     * @return Equivalent Corese node or null if RDF4J value is null.
     */
    public static Node rdf4jValueToCoreseNode(Value rdf4j_value) {
        if (rdf4j_value == null) {
            return null;
        }
        return NodeImpl.create(Rdf4jValueToCoreseDatatype.convert(rdf4j_value));
    }

    /**
     * Convert an array of RDF4J value to a equivalent array of Corese node.
     * 
     * @param values Array of RDF4J value to convert.
     * @return Equivalent array of Corese node.
     */
    public static Iterable<Node> rdf4jvaluestoCoresNodes(Iterable<Value> values) {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Value context : values) {
            nodes.add(Convert.rdf4jValueToCoreseNode(context));
        }

        return nodes;
    }

    /*****************************
     * Context : Rdf4j to Corese *
     *****************************/

    /**
     * Convert a RDF4J context to a Corese context.
     * 
     * @param rdf4j_context RDF4J context to convert.
     * @return Equivalent Corese context or the corese default context if RDF4J
     *         context is null.
     */
    public static Node rdf4jContextToCoreseContext(Resource rdf4j_context) {
        if (rdf4j_context == null) {
            return corese_default_context;
        } else {
            return Convert.rdf4jValueToCoreseNode(rdf4j_context);
        }
    }

    /**
     * Convert a array of RDF4J context to an equivalent array of Corese node
     * context.
     * 
     * @param corese_default_context Value of the default corese context.
     * @param rdf4j_contexts         Array of RDF4J context to convert.
     * @return Equivalent array of Corese node, null if RDF4J context is empty, the
     *         default corese context if RDF4J context is null.
     */
    public static Node[] rdf4jContextsToCoreseContexts(Resource... rdf4j_contexts) {

        // if contexts is null then the equivalent array of Corese is the default corese
        // context
        if (rdf4j_contexts == null || (rdf4j_contexts.length == 1 && rdf4j_contexts[0] == null)) {
            return new Node[] { corese_default_context };
        }

        // if contexts is empty then the equivalent array of Corese is null
        if (rdf4j_contexts.length == 0) {
            return null;
        }

        // convert rdf4j context to Corese context
        Node[] corese_contexts = new Node[rdf4j_contexts.length];
        for (int i = 0; i < rdf4j_contexts.length; i++) {
            Resource rdf4j_context = rdf4j_contexts[i];
            corese_contexts[i] = Convert.rdf4jContextToCoreseContext(rdf4j_context);
        }

        return corese_contexts;
    }

    /*******************************
     * Statement : Rdf4j to Corese *
     *******************************/

    /**
     * Convert Statement to equivalent Edge.
     * 
     * @param statement Statement to convert.
     * @return Equivalent Edge.
     */
    public static Edge statementToEdge(Statement statement) {
        Node subject_corese = Convert.rdf4jValueToCoreseNode(statement.getSubject());
        Node predicate_corese = Convert.rdf4jValueToCoreseNode(statement.getPredicate());
        Node object_corese = Convert.rdf4jValueToCoreseNode(statement.getObject());
        Node context_corese = Convert.rdf4jContextToCoreseContext(statement.getContext());

        return EdgeImpl.create(context_corese, subject_corese, predicate_corese, object_corese);
    }

    /**
     * Convert a list of Statement to equivalent list of Edge.
     * 
     * @param statements List of Statemet to convert.
     * @return Equivalent list of Edge.
     */
    public static Iterable<Edge> statementsToEdges(Iterable<Statement> statements) {

        ArrayList<Edge> edges = new ArrayList<>();
        for (Statement statement : statements) {
            edges.add(Convert.statementToEdge(statement));
        }
        return edges;
    }

    /***************************
     * Value : Corese to RDF4J *
     ***************************/

    /**
     * Convert a Corese node to a RDF4J value .
     * 
     * @param corese_node Corese node to convert.
     * @return Equivalent RDF4J value or null if Corese node is null.
     */
    public static Value coreseNodeToRdf4jValue(Node corese_node) {
        if (corese_node == null) {
            return null;
        }
        return CoreseDatatypeToRdf4jValue.convert(corese_node.getDatatypeValue());
    }

    /*****************************
     * Context : Corese to RDF4J *
     *****************************/

    /**
     * Convert a Corese context to a RDF4J context.
     * 
     * @param corese_context Corese context to convert, not be null.
     * @return Equivalent RDF4J context or null if Corese context is the default
     *         context.
     */
    public static Resource coreseContextToRdf4jContext(Node corese_context) {
        if (corese_context.equals(corese_default_context)) {
            return null;
        } else {
            return (Resource) Convert.coreseNodeToRdf4jValue(corese_context);
        }
    }

    /**
     * Convert an array of Corese node context to an equivalent array of RDF4J
     * context.
     * 
     * @param corese_default_context Value of the default corese context.
     * @param corese_contexts        Array of Corese node context to convert.
     * @return Equivalent array of RDF4J context, RDF4J context empty if Corese node
     *         context is null, null if corese node context is the default context.
     */
    public static Resource[] coreseContextsToRdf4jContexts(Node... corese_contexts) {

        // if contexts is null then the equivalent RDF4J context is empty
        if (corese_contexts == null || (corese_contexts.length == 1 && corese_contexts[0] == null)) {
            return new Resource[] {};
        }

        // convert Corese context to rdf4j context
        ArrayList<Resource> rdf4j_contexts = new ArrayList<>();
        for (int i = 0; i < corese_contexts.length; i++) {
            Node corese_context = corese_contexts[i];
            if (corese_context != null) {
                rdf4j_contexts.add(Convert.coreseContextToRdf4jContext(corese_context));
            }
        }

        return rdf4j_contexts.toArray(new Resource[rdf4j_contexts.size()]);
    }

    /*******************************
     * Statement : Corese to RDF4J *
     *******************************/

    /**
     * Convert Edge to equivalent Statement.
     * 
     * @param edge Edge to convert.
     * @return Equivalent Statement.
     */
    public static Statement EdgeToStatement(Edge edge) {
        return edge;
    }
}