package fr.inria.corese.rdf4jImpl.combination.engine;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.rdf4j.CoreseGraphModel;
import fr.inria.corese.rdf4j.Rdf4jModelDataManagerBuilder;
import fr.inria.corese.sparql.exceptions.EngineException;

public class SparqlEngine {

    /**
     * Execute a select query with RDF4J SPARQL engine
     */

    public static SelectResults rdf4jSelectQuery(String query_string, Model model) {
        SelectResults r;
        Repository db = new SailRepository(new MemoryStore());

        try (RepositoryConnection conn = db.getConnection()) {
            // add the model
            conn.add(model);

            TupleQuery query = conn.prepareTupleQuery(query_string);

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed
            // when done.
            try (TupleQueryResult result = query.evaluate()) {
                r = new SelectResults(result);
            }
        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }

        return r;
    }

    /**
     * Execute a construct query with RDF4J SPARQL engine
     */

    public static Model rdf4jConstructQuery(String query_string, Model model) {
        Model result_model = new TreeModel();
        Repository db = new SailRepository(new MemoryStore());

        try (RepositoryConnection conn = db.getConnection()) {
            // add the model
            conn.add(model);

            GraphQuery query = conn.prepareGraphQuery(query_string);

            try (GraphQueryResult result = query.evaluate()) {
                for (Statement st : result) {
                    result_model.add(st);
                }
            }
        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }

        return result_model;
    }

    /**
     * Execute a ask query with RDF4J SPARQL engine
     */

    public static boolean rdf4jAskQuery(String query_string, Model model) {
        boolean result_boolean;
        Repository db = new SailRepository(new MemoryStore());

        try (RepositoryConnection conn = db.getConnection()) {
            // add the model
            conn.add(model);
            BooleanQuery query = conn.prepareBooleanQuery(query_string);

            result_boolean = query.evaluate();
        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }

        return result_boolean;
    }

    /**
     * Execute a update query with RDF4J SPARQL engine
     */

    public static Model rdf4jUpdateQuery(String query_string, Model model) {
        Repository db = new SailRepository(new MemoryStore());

        try (RepositoryConnection conn = db.getConnection()) {
            // add the model
            conn.add(model);

            Update query = conn.prepareUpdate(query_string);
            query.execute();
            return QueryResults.asModel(conn.getStatements(null, null, null));
        } finally {
            // before our program exits, make sure the database is properly shut down.
            db.shutDown();
        }
    }

    /**
     * Execute a select query with Corese SPARQL engine
     */

    public static SelectResults coreseSelectQuery(String query, Graph graph) {
        return SparqlEngine.coreseSelectQuery(query, QueryProcess.create(graph));
    }

    public static SelectResults coreseSelectQuery(String query, Model model) {
        DataManager dataManage = new Rdf4jModelDataManagerBuilder().model(model).build();
        return SparqlEngine.coreseSelectQuery(query, QueryProcess.create(dataManage));
    }

    private static SelectResults coreseSelectQuery(String query, QueryProcess exec) {
        Mappings map = null;
        try {
            map = exec.query(query);
        } catch (EngineException e) {
            System.err.println("Error: Unable to run query" + query);
            e.printStackTrace();
        }

        return new SelectResults(map);
    }

    /**
     * Execute a construct query with Corese SPARQL engine
     */

    public static Model coreseConstructQuery(String query, Graph graph) {
        return SparqlEngine.coreseConstructQuery(query, QueryProcess.create(graph));
    }

    public static Model coreseConstructQuery(String query, Model model) {
        DataManager dataManage = new Rdf4jModelDataManagerBuilder().model(model).build();
        return SparqlEngine.coreseConstructQuery(query, QueryProcess.create(dataManage));
    }

    private static Model coreseConstructQuery(String query, QueryProcess exec) {
        Mappings map = new Mappings();
        try {
            map = exec.query(query);
        } catch (EngineException e) {
            System.err.println("Error: Unable to run query" + query);
            e.printStackTrace();
        }

        return new CoreseGraphModel((Graph) map.getGraph());
    }

    /**
     * Execute a ask query with Corese SPARQL engine
     */

    public static Boolean coreseAskQuery(String query, Graph graph) {
        return SparqlEngine.coreseAskQuery(query, QueryProcess.create(graph));
    }

    public static Boolean coreseAskQuery(String query, Model model) {
        DataManager dataManage = new Rdf4jModelDataManagerBuilder().model(model).build();
        return SparqlEngine.coreseAskQuery(query, QueryProcess.create(dataManage));
    }

    private static Boolean coreseAskQuery(String query, QueryProcess exec) {
        Mappings map = new Mappings();
        try {
            map = exec.query(query);
        } catch (EngineException e) {
            System.err.println("Error: Unable to run query" + query);
            e.printStackTrace();
        }

        return !map.isEmpty();
    }

    /**
     * Execute a update query with Corese SPARQL engine
     */

    public static Model coreseUpdateQuery(String query, Graph graph) {
        SparqlEngine.coreseUpdateQuery(query, QueryProcess.create(graph));
        return new CoreseGraphModel(graph);

    }

    public static Model coreseUpdateQuery(String query, Model model) {
        DataManager dataManage = new Rdf4jModelDataManagerBuilder().model(model).build();
        SparqlEngine.coreseUpdateQuery(query, QueryProcess.create(dataManage));
        return new CoreseGraphModel(model);
    }

    private static void coreseUpdateQuery(String query, QueryProcess exec) {
        try {
            exec.query(query);
        } catch (EngineException e) {
            System.err.println("Error: Unable to run query" + query);
            e.printStackTrace();
        }
    }
}
