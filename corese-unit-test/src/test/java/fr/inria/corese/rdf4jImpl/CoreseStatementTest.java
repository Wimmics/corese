package fr.inria.corese.rdf4jImpl;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.Value;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @Test
    public void getSubject() {
        Value subject_rdf4j = edith_piaf.getDatatypeValue().getRdf4jValue();

        assertEquals(subject_rdf4j, edge_edit_singer.getSubject());
        assertEquals(subject_rdf4j, edge_edit_fname.getSubject());
        assertEquals(subject_rdf4j, edge_edit_lname.getSubject());
    }

    @Test
    public void getPredicate() {
        Value rdfType_rdf4j = predicate_rdfType.getDatatypeValue().getRdf4jValue();
        Value firstName_rdf4j = predicate_firstName.getDatatypeValue().getRdf4jValue();
        Value lastName_rdf4j = predicate_lastName.getDatatypeValue().getRdf4jValue();

        assertEquals(rdfType_rdf4j, edge_edit_singer.getPredicate());
        assertEquals(firstName_rdf4j, edge_edit_fname.getPredicate());
        assertEquals(lastName_rdf4j, edge_edit_lname.getPredicate());
    }

    @Test
    public void getObject() {
        Value singer_rdf4j = object_singer.getDatatypeValue().getRdf4jValue();
        Value edith_rdf4j = object_edith.getDatatypeValue().getRdf4jValue();
        Value piaf_rdf4j = object_piaf.getDatatypeValue().getRdf4jValue();

        assertEquals(singer_rdf4j, edge_edit_singer.getObject());
        assertEquals(edith_rdf4j, edge_edit_fname.getObject());
        assertEquals(piaf_rdf4j, edge_edit_lname.getObject());
    }

    @Test
    public void getContext() {
        Value context1_rdf4j = context_1.getDatatypeValue().getRdf4jValue();
        Value context2_rdf4j = context_2.getDatatypeValue().getRdf4jValue();

        assertEquals(null, edge_edit_singer.getContext());
        assertEquals(context1_rdf4j, edge_edit_fname.getContext());
        assertEquals(context2_rdf4j, edge_edit_lname.getContext());
    }
}