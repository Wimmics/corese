package fr.inria.corese.jenaImpl.combination.engine;

import org.apache.jena.query.Dataset;

import fr.inria.corese.core.Graph;

public class Combination {

    public static boolean selectQuery(String query_string, LoadableFile... load_files) {

        System.out.println("=================================================");
        System.out.println(query_string);
        System.out.println("=================================================");

        // build dataset and graph
        Dataset jena_dataset = Load.JenaDataset(load_files);
        Graph corese_graph = Load.coreseGraph(load_files);

        // get results of all combinations
        System.out.println("SPARQL engine: Corese, Model: Jena Dataset");
        SelectResults result_dataset = SparqlEngine.selectQuery(query_string, jena_dataset);
        System.out.println(result_dataset);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        SelectResults result_graph = SparqlEngine.selectQuery(query_string, corese_graph);
        System.out.println(result_graph);

        // Compare results
        return result_dataset.equals(result_graph);
    }

    public static boolean constructQuery(String query_string, LoadableFile... load_files) {

        // get results of all combinations
        System.out.println("SPARQL engine: Corese, Model: Jena Dataset");
        Graph result_dataset = SparqlEngine.constructQuery(query_string, Load.JenaDataset(load_files));
        System.out.println(result_dataset);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Graph result_graph = SparqlEngine.constructQuery(query_string, Load.coreseGraph(load_files));
        System.out.println(result_graph);

        return CompareGraphDataset.compareGraph(result_dataset, result_graph);
    }

    public static boolean describeQuery(String query_string, LoadableFile... load_files) {
        return constructQuery(query_string, load_files);
    }

    public static boolean askQuery(String query_string, LoadableFile... load_files) {

        // build dataset and graph
        Dataset jena_dataset = Load.JenaDataset(load_files);
        Graph corese_graph = Load.coreseGraph(load_files);

        // get results of all combinations
        System.out.println("SPARQL engine: Corese, Model: Jena Dataset");
        Boolean result_dataset = SparqlEngine.askQuery(query_string, jena_dataset);
        System.out.println(result_dataset);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Boolean result_graph = SparqlEngine.askQuery(query_string, corese_graph);
        System.out.println(result_graph);

        return result_dataset.equals(result_dataset.equals(result_graph));
    }

    public static boolean updateQuery(String query_string, LoadableFile... load_files) {

        System.out.println(Load.coreseGraph(load_files).size());

        // get results of all combinations
        System.out.println("SPARQL engine: Corese, Model: Jena Dataset");
        Dataset result_dataset = SparqlEngine.updateQuery(query_string, Load.JenaDataset(load_files));

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Graph result_graph = SparqlEngine.udateQuery(query_string, Load.coreseGraph(load_files));

        return CompareGraphDataset.compareGraph(result_dataset, result_graph);
    }

}
