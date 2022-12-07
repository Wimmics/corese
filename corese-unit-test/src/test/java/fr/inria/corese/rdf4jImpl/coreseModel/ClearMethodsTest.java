package fr.inria.corese.rdf4jImpl.coreseModel;

import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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

public class ClearMethodsTest {

    public static CoreseGraphModel buildModel() {
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

        return model;
    }

    @Test
    public void clearAllTest() throws EngineException {
        CoreseGraphModel model = ClearMethodsTest.buildModel();
        Graph corese_graph = model.getCoreseGraph();
        QueryProcess exec = QueryProcess.create(corese_graph);

        // check size of the graph before clear
        Mappings map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(2, map.size());

        model.clear();

        // check size of the graph after clear
        map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(0, map.size());
    }

    @Test
    public void clearGraph() throws EngineException {
        CoreseGraphModel model = ClearMethodsTest.buildModel();
        Graph corese_graph = model.getCoreseGraph();
        QueryProcess exec = QueryProcess.create(corese_graph);

        // check size of the graph before clear
        Mappings map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(2, map.size());

        ValueFactory rdf4j_factory = SimpleValueFactory.getInstance();
        IRI context1_iri = rdf4j_factory.createIRI("http://example.org/context1");
        model.clear(context1_iri);

        // check size of the graph after clear
        map = exec.query("select * where { ?s ?p ?o }");
        assertEquals(1, map.size());

        // Check remaning statement value
        map = exec.query("PREFIX ex:<http://example.org/>" + "ASK { ex:EdithPiaf rdf:type ex:Singer }");
        assertEquals(1, map.size());
    }

}
