package fr.inria.corese.rdf4jImpl.coreseModel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.rdf4j.CoreseGraphModel;
import fr.inria.corese.sparql.exceptions.EngineException;

public class AddMethodsTest {

    @Test
    public void addSpoTest() throws EngineException {

        //////////////////////
        // Build statements //
        //////////////////////
        String ex = "http://example.org/";

        // first statement
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        // second statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Edith");
        IRI context1 = Values.iri(ex, "context1");

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        model.add(edithPiafNode, isaProperty, singerNode);
        model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);

        ////////////////
        // Test graph //
        ////////////////
        Graph corese_graph = model.getCoreseGraph();
        QueryProcess exec = QueryProcess.create(corese_graph);

        // check size of the graph
        Mappings map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(2, map.size());

        map = exec.query("select * from <http://example.org/context1> where { ?s ?p ?o }");
        assertEquals(1, map.size());

        // check first statement
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf rdf:type ex:Singer }");
        assertEquals(1, map.size());

        // check second statement
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf ex:firstName \"Edith\" }");
        assertEquals(1, map.size());
    }

    @Test
    public void addStatement() throws EngineException {

        String ex = "http://example.org/";

        /////////////////////
        // Build statement //
        /////////////////////
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        ValueFactory vf = SimpleValueFactory.getInstance();
        Statement statement = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        model.add(statement);

        ////////////////
        // Test graph //
        ////////////////
        Graph corese_graph = model.getCoreseGraph();
        QueryProcess exec = QueryProcess.create(corese_graph);

        // check size of the graph
        Mappings map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(1, map.size());

        // check statement
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf rdf:type ex:Singer }");
        assertEquals(1, map.size());
    }

    @Test
    public void addAllStatement() throws EngineException {

        String ex = "http://example.org/";

        //////////////////////
        // Build statements //
        //////////////////////
        ValueFactory vf = SimpleValueFactory.getInstance();

        // build first statement
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        Statement statement1 = vf.createStatement(edithPiafNode, isaProperty, singerNode);

        // build second statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Edith");

        Statement statement2 = vf.createStatement(edithPiafNode, firstNameProperty, edithLiteral);

        /////////////////
        // Build graph //
        /////////////////
        CoreseGraphModel model = new CoreseGraphModel();
        List<Statement> statements = Arrays.asList(statement1, statement2);
        model.addAll(statements);

        ////////////////
        // Test graph //
        ////////////////
        Graph corese_graph = model.getCoreseGraph();
        QueryProcess exec = QueryProcess.create(corese_graph);

        // check size of the graph
        Mappings map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(2, map.size());

        // check first statement
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf rdf:type ex:Singer }");
        assertEquals(1, map.size());

        // check second statement
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf ex:firstName \"Edith\" }");
        assertEquals(1, map.size());
    }

}
