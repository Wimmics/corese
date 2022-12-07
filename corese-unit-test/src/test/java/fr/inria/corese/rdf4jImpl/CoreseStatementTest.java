package fr.inria.corese.rdf4jImpl;

import org.junit.BeforeClass;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

public class CoreseStatementTest {

    public static Node edith_piaf;

    public static Edge edge_edit_singer;
    public static Edge edge_edit_fname;
    public static Edge edge_edit_lname;

    public static Node predicate_rdfType;
    public static Node predicate_firstName;
    public static Node predicate_lastName;

    public static Node object_singer;
    public static Node object_edith;
    public static Node object_piaf;

    public static Node context_1;
    public static Node context_2;

    @BeforeClass
    public static void buildGraph() {
        // Define the namespace ex
        String ex = "http://example.org/";

        // Create a new empty Graph
        Graph graph = Graph.create();

        // Create and add IRIs to Graph
        edith_piaf = graph.addResource(ex + "EdithPiaf");
        object_singer = graph.addResource(ex + "Singer");

        // Create and add properties to Graph
        predicate_rdfType = graph.addProperty(RDF.TYPE);
        predicate_firstName = graph.addProperty(ex + "firstName");
        predicate_lastName = graph.addProperty(ex + "lastName");

        // Create and add contexts to graph
        context_1 = graph.addGraph(ex + "context1");
        context_2 = graph.addGraph(ex + "context2");

        // Add first statement : Edith Piaf is an Singer
        edge_edit_singer = graph.addEdge(edith_piaf, predicate_rdfType, object_singer);

        // Add second statement : Edith Piaf's first name is Edith
        object_edith = graph.addLiteral("Edith");
        edge_edit_fname = graph.addEdge(context_1, edith_piaf, predicate_firstName, object_edith);

        // Add third statement : Edith Piaf's last name is Piaf
        object_piaf = graph.addLiteral("Piaf");
        edge_edit_lname = graph.addEdge(context_2, edith_piaf, predicate_lastName, object_piaf);
    }

}