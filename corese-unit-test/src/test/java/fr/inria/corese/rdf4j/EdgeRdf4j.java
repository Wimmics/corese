package fr.inria.corese.rdf4j;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.Value;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

public class EdgeRdf4j {

    public static Node edith_piaf;

    public static Edge edge_edit_singer;
    public static Edge edge_edit_fname;
    public static Edge edge_edit_lname;

    public static Node predicate_rdfType;
    public static Node predicate_firstName;
    public static Node predicate_lastName;

    public static Node context_singer;
    public static Node context_edith;
    public static Node context_piaf;
    
    @BeforeClass
    public static void buildGraph() {
        // Define the namespace ex
        String ex = "http://example.org/";

        // Create a new empty Graph
        Graph graph = Graph.create();

        // Create and add IRIs to Graph
        edith_piaf = graph.addResource(ex + "EdithPiaf");
        context_singer = graph.addResource(ex + "Singer");

        // Create and add properties to Graph
        predicate_rdfType = graph.addProperty(RDF.TYPE);
        predicate_firstName = graph.addProperty(ex + "firstName");
        predicate_lastName = graph.addProperty(ex + "lastName");

        // Add first statement : Edith Piaf is an Singer
        edge_edit_singer = graph.addEdge(edith_piaf, predicate_rdfType, context_singer);

        // Add second statement : Edith Piaf's first name is Edith
        context_edith = graph.addLiteral("Edith");
        edge_edit_fname = graph.addEdge(edith_piaf, predicate_firstName, context_edith);

        // Add third statement : Edith Piaf's last name is Piaf
        context_piaf = graph.addLiteral("Piaf");
        edge_edit_lname = graph.addEdge(edith_piaf, predicate_lastName, context_piaf);
    }

    @Test
    public void getSubject() {
        Value subject = edith_piaf.getDatatypeValue().getRdf4jValue();
        
        assertEquals(subject, edge_edit_singer.getSubject());
        assertEquals(subject, edge_edit_fname.getSubject());
        assertEquals(subject, edge_edit_lname.getSubject());
    }

    @Test
    public void getPredicate() {
        Value rdfType = predicate_rdfType.getDatatypeValue().getRdf4jValue();
        Value firstName = predicate_firstName.getDatatypeValue().getRdf4jValue();
        Value lastName = predicate_lastName.getDatatypeValue().getRdf4jValue();

        assertEquals(rdfType, edge_edit_singer.getPredicate());
        assertEquals(firstName, edge_edit_fname.getPredicate());
        assertEquals(lastName, edge_edit_lname.getPredicate());
    }

    @Test
    public void getContext() {
        Value singer = context_singer.getDatatypeValue().getRdf4jValue();
        Value edith = context_edith.getDatatypeValue().getRdf4jValue();
        Value piaf = context_piaf.getDatatypeValue().getRdf4jValue();

        assertEquals(singer, edge_edit_singer.getObject());
        assertEquals(edith, edge_edit_fname.getObject());
        assertEquals(piaf, edge_edit_lname.getObject());
    }
    

    
}
