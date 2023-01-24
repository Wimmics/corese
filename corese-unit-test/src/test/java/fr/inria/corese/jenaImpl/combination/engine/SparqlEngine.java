package fr.inria.corese.jenaImpl.combination.engine;

import org.apache.jena.query.Dataset;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;

public class SparqlEngine {

    /**
     * Execute a select query with Corese SPARQL engine
     */

    public static SelectResults selectQuery(String query, Graph graph) {
        return SparqlEngine.coreseSelectQuery(query, QueryProcess.create(graph));
    }

    public static SelectResults selectQuery(String query, Dataset dataset) {
        DataManager dataManage = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
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

    public static Graph constructQuery(String query, Graph graph) {
        return SparqlEngine.coreseConstructQuery(query, QueryProcess.create(graph));
    }

    public static Graph constructQuery(String query, Dataset dataset) {
        DataManager dataManage = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
        return SparqlEngine.coreseConstructQuery(query, QueryProcess.create(dataManage));
    }

    private static Graph coreseConstructQuery(String query, QueryProcess exec) {
        Mappings map = new Mappings();
        try {
            map = exec.query(query);
        } catch (EngineException e) {
            System.err.println("Error: Unable to run query" + query);
            e.printStackTrace();
        }
        return (Graph) map.getGraph();
    }

    /**
     * Execute a ask query with Corese SPARQL engine
     */

    public static Boolean askQuery(String query, Graph graph) {
        return SparqlEngine.coreseAskQuery(query, QueryProcess.create(graph));
    }

    public static Boolean askQuery(String query, Dataset dataset) {
        DataManager dataManage = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
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

    public static Graph udateQuery(String query, Graph graph) {
        SparqlEngine.coreseUpdateQuery(query, QueryProcess.create(graph));
        return graph;

    }

    public static Dataset updateQuery(String query, Dataset dataset) {
        DataManager dataManage = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();
        SparqlEngine.coreseUpdateQuery(query, QueryProcess.create(dataManage));
        return dataset;
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
