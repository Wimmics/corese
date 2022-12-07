package fr.inria.corese.rdf4jImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.junit.Test;

import fr.inria.corese.rdf4j.CoreseGraphModel;

public class Rdf4jExperimentations {

    @Test
    public void testRio() throws IOException {
        InputStream input_stream = Rdf4jExperimentations.class.getResourceAsStream("peopleWork.ttl");

        CoreseGraphModel model = new CoreseGraphModel();

        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(model));

        try {
            rdfParser.parse(input_stream);
        } catch (IOException e) {
            // handle IO problems (e.g. the file could not be read)
        } catch (RDFParseException e) {
            // handle unrecoverable parse error
        } catch (RDFHandlerException e) {
            // handle a problem encountered by the RDFHandler
        } finally {
            try {
                input_stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        URL url_output = Rdf4jExperimentations.class.getResource("peopleWorkResult.ttl");
        FileOutputStream out = new FileOutputStream(url_output.getPath());
        try {
            Rio.write(model, out, RDFFormat.TURTLE);
        } finally {
            out.close();
        }
    }

    @Test
    public void testSparql() throws RDFParseException, UnsupportedRDFormatException, IOException {

        // Open a turtle file
        InputStream input_stream = Rdf4jExperimentations.class.getResourceAsStream("peopleWork.ttl");

        // Load file content in CoreseModel
        CoreseGraphModel model = new CoreseGraphModel();
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(model));
        rdfParser.parse(input_stream);
        input_stream.close();

        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        Repository db = new SailRepository(new MemoryStore());

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            // add the model
            conn.add(model);

            // We do a simple SPARQL SELECT-query
            String queryString = "PREFIX ex: <http://example.org/ns#> \n";
            queryString += "PREFIX rf: <" + RDF.NAMESPACE + "> \n";
            queryString += "SELECT ?s ?o \n";
            queryString += "WHERE { \n";
            queryString += "    ?s a ?o.";
            queryString += "}";
            TupleQuery query = conn.prepareTupleQuery(queryString);

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed
            // when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?o
                    System.out.println("?s = " + solution.getValue("s"));
                    System.out.println("?o = " + solution.getValue("o"));
                    System.out.println("---––––––––––––---");
                }
            }

        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

    @Test
    public void saveDateInFile() throws RDFParseException, UnsupportedRDFormatException, IOException {

        // Open a turtle file
        InputStream input_stream = Rdf4jExperimentations.class.getResourceAsStream("peopleWork.ttl");

        // Load file content in CoreseModel
        CoreseGraphModel model = new CoreseGraphModel();
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(model));
        rdfParser.parse(input_stream);

        // Create a new Repository.
        URL url_data = Rdf4jExperimentations.class.getResource("");
        File dataDir = new File(url_data.getPath() + "/data/");
        Repository db = new SailRepository(new NativeStore(dataDir));

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            // add the model
            conn.add(model);

        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

    @Test
    public void loadDataFromFile() throws RDFParseException, UnsupportedRDFormatException, IOException {

        // Create and load new Repository.
        URL url_data = Rdf4jExperimentations.class.getResource("");
        File dataDir = new File(url_data.getPath() + "/data/");
        Repository db = new SailRepository(new NativeStore(dataDir));

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {

            // We do a simple SPARQL SELECT-query
            String queryString = "PREFIX ex: <http://example.org/ns#> \n";
            queryString += "PREFIX rf: <" + RDF.NAMESPACE + "> \n";
            queryString += "SELECT ?s ?o \n";
            queryString += "WHERE { \n";
            queryString += "    ?s a ?o.";
            queryString += "}";
            TupleQuery query = conn.prepareTupleQuery(queryString);

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed
            // when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                System.out.println("Résult :");
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?o
                    System.out.println("?s = " + solution.getValue("s"));
                    System.out.println("?o = " + solution.getValue("o"));
                    System.out.println("---––––––––––––---");
                }
            }

        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

    @Test
    public void testGetStatements() throws RDFParseException, UnsupportedRDFormatException, IOException {
        InputStream input_stream = Rdf4jExperimentations.class.getResourceAsStream("peopleWork.ttl");

        CoreseGraphModel model = new CoreseGraphModel();
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(model));
        rdfParser.parse(input_stream);

        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        Repository db = new SailRepository(new MemoryStore());

        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {
            // add the model
            conn.add(model);

            // let's check that our data is actually in the database
            try (RepositoryResult<Statement> result = conn.getStatements(null, null, null)) {
                for (Statement st : result) {
                    System.out.println("db contains: " + st);
                }
            }
        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

}
