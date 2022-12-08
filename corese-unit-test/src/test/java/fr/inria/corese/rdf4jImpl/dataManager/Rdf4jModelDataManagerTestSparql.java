package fr.inria.corese.rdf4jImpl.dataManager;

import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;

import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.rdf4j.CoreseGraphModel;
import fr.inria.corese.rdf4j.Rdf4jModelDataManagerBuilder;
import fr.inria.corese.sparql.exceptions.EngineException;

public class Rdf4jModelDataManagerTestSparql {

    private Model model;

    @Before
    public void init() {

        String ex = "http://example.org/";

        // statement zero
        IRI edithPiafNode = Values.iri(ex, "EdithPiaf");
        IRI isaProperty = Values.iri(RDF.TYPE.stringValue());
        IRI singerNode = Values.iri(ex, "Singer");

        // first statement
        IRI firstNameProperty = Values.iri(ex, "firstName");
        Literal edithLiteral = Values.literal("Édith");
        IRI context1 = Values.iri(ex, "context1");

        // second statement
        IRI context2 = Values.iri(ex, "context2");

        // third statement
        IRI context3 = Values.iri(ex, "context3");

        /////////////////
        // Build graph //
        /////////////////
        this.model = new TreeModel();
        this.model.add(edithPiafNode, isaProperty, singerNode);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context1);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context2);
        this.model.add(edithPiafNode, firstNameProperty, edithLiteral, context3);
    }

    @Test
    public void selectWhereSpo() throws EngineException, IOException {
        DataManager dataManager = new Rdf4jModelDataManagerBuilder().model(this.model).build();

        // Sparql query
        QueryProcess exec = QueryProcess.create(dataManager);
        Mappings map = exec.query("select * where { ?s ?p ?o }");

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void selectWhereSpoTemoin() throws EngineException, IOException {
        // Sparql query
        QueryProcess exec = QueryProcess.create(new CoreseGraphModel(this.model).getCoreseGraph());
        Mappings map = exec.query("prefix ex: <http://example.org/> select * where { ?s ?p ?o }");

        // Print result
        for (Mapping m : map) {
            System.out.println(m);
        }
    }

    @Test
    public void selectWhereSpoRDF4J() {

        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        Repository db = new SailRepository(new MemoryStore());

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            // add the model
            conn.add(new TreeModel(this.model));

            // We do a simple SPARQL SELECT-query
            String queryString = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";
            TupleQuery query = conn.prepareTupleQuery(queryString);

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed
            // when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?o
                    System.out.println("?s = " + solution.getValue("s"));
                    System.out.println("?p = " + solution.getValue("p"));
                    System.out.println("?o = " + solution.getValue("o"));
                    System.out.println("----------");
                }
            }

        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

    @Test
    public void insertData() throws EngineException {
        CoreseGraphModel corese_model = new CoreseGraphModel(this.model);
        DataManager dataManager = new Rdf4jModelDataManagerBuilder().model(corese_model).build();

        // Sparql query
        QueryProcess exec = QueryProcess.create(dataManager);
        exec.query("PREFIX dc: <http://purl.org/dc/elements/1.1/>" + "INSERT DATA" + "{ "
                + "<http://example/book1> dc:title \"A new book\" ;" + "dc:creator \"A.N.Other\" ." + "}");

        // Print result
        System.out.println(corese_model.toString());
    }

    @Test
    public void insertDataTemoin() throws EngineException {
        // Sparql query
        CoreseGraphModel corese_model = new CoreseGraphModel(this.model);
        QueryProcess exec = QueryProcess.create(corese_model.getCoreseGraph());
        exec.query("PREFIX dc: <http://purl.org/dc/elements/1.1/>" + "INSERT DATA" + "{ "
                + "<http://example/book1> dc:title \"A new book\" ;" + "dc:creator \"A.N.Other\" ." + "}");

        // Print result
        System.out.println(corese_model);
    }
}
