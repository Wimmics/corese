package fr.inria.corese.rdf4j;

import java.util.ArrayList;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.rdf4j.CoreseDatatypeToRdf4jValue;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class Convert {

    private Convert() {
    }

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
     * Convert a RDF4J context to a Corese graph.
     * 
     * @param rdf4j_context RDF4J context to convert.
     * @return Equivalent Corese graph or the default graph if RDF4J context is
     *         null.
     */
    public static Node rdf4jContextToCoreseGraph(Node default_graph, Resource rdf4j_context) {
        if (rdf4j_context == null) {
            return default_graph;
        } else {
            return Convert.rdf4jValueToCoreseNode(rdf4j_context);
        }
    }

    /**
     * Convert a array of RDF4J context to an equivalent array of Corese node graph.
     * 
     * @param default_graph  Value of the default graph.
     * @param rdf4j_contexts Array of RDF4J context to convert.
     * @return Equivalent array of Corese node, null if RDF4J context is empty, the
     *         default graph if RDF4J context is null.
     */
    public static Node[] rdf4jContextsToCoreseGraphs(Node default_graph, Resource... rdf4j_contexts) {

        // if contexts is null then the equivalent array of Corese is the default graph
        if (rdf4j_contexts == null || (rdf4j_contexts.length == 1 && rdf4j_contexts[0] == null)) {
            return new Node[] { default_graph };
        }

        // if contexts is empty then the equivalent array of Corese is null
        if (rdf4j_contexts.length == 0) {
            return null;
        }

        // convert rdf4j context to Corese graph
        Node[] corese_graphs = new Node[rdf4j_contexts.length];
        for (int i = 0; i < rdf4j_contexts.length; i++) {
            Resource rdf4j_context = rdf4j_contexts[i];
            corese_graphs[i] = Convert.rdf4jContextToCoreseGraph(default_graph, rdf4j_context);
        }

        return corese_graphs;
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
    public static Edge statementToEdge(Node default_graph, Statement statement) {
        Node subject_corese = Convert.rdf4jValueToCoreseNode(statement.getSubject());
        Node predicate_corese = Convert.rdf4jValueToCoreseNode(statement.getPredicate());
        Node object_corese = Convert.rdf4jValueToCoreseNode(statement.getObject());
        Node context_corese = Convert.rdf4jContextToCoreseGraph(default_graph, statement.getContext());

        return EdgeImpl.create(context_corese, subject_corese, predicate_corese, object_corese);
    }

    /**
     * Convert a list of Statement to equivalent list of Edge.
     * 
     * @param statements List of Statemet to convert.
     * @return Equivalent list of Edge.
     */
    public static Iterable<Edge> statementsToEdges(Node default_graph, Iterable<Statement> statements) {

        ArrayList<Edge> edges = new ArrayList<>();
        for (Statement statement : statements) {
            edges.add(Convert.statementToEdge(default_graph, statement));
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
     * Convert a Corese graph to a RDF4J context.
     * 
     * @param corese_graph Corese graph to convert, not be null.
     * @return Equivalent RDF4J context or null if Corese graph is the default
     *         graph.
     */
    public static Resource coreseGraphToRdf4jContext(Node default_graph, Node corese_graph) {
        if (corese_graph.equals(default_graph)) {
            return null;
        } else {
            return (Resource) Convert.coreseNodeToRdf4jValue(corese_graph);
        }
    }

    /**
     * Convert an array of Corese node graph to an equivalent array of RDF4J
     * context.
     * 
     * @param default_graph Value of the default graph.
     * @param corese_graphs Array of Corese node graph to convert.
     * @return Equivalent array of RDF4J context, RDF4J context empty if Corese node
     *         graph is null, null if Corese node graph is the default graph.
     */
    public static Resource[] coreseGraphsToRdf4jContexts(Node default_graph, Node... corese_graphs) {

        // if graphs is null then the equivalent RDF4J context is empty
        if (corese_graphs == null || (corese_graphs.length == 1 && corese_graphs[0] == null)) {
            return new Resource[] {};
        }

        // convert Corese graph to rdf4j context
        Resource[] rdf4j_contexts = new Resource[corese_graphs.length];
        for (int i = 0; i < corese_graphs.length; i++) {
            Node corese_graph = corese_graphs[i];
            if (corese_graph != null) {
                rdf4j_contexts[i] = Convert.coreseGraphToRdf4jContext(default_graph, corese_graph);
            }
        }

        return rdf4j_contexts;
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
        ValueFactory vf = SimpleValueFactory.getInstance();
        return vf.createStatement(edge.getSubject(), edge.getPredicate(), edge.getObject(), edge.getContext());
    }
}