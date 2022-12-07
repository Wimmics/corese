package fr.inria.corese.rdf4jImpl.combination.engine;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;

import fr.inria.corese.core.Graph;
import fr.inria.corese.rdf4j.CoreseGraphModel;

public class Combination {

    public static boolean selectQuery(String query_string, LoadableFile... load_files) {

        // build models and graphs
        TreeModel tree_model = Load.treeModel(load_files);
        CoreseGraphModel corese_model = Load.coreseModel(load_files);
        Graph graph = Load.coreseGraph(load_files);

        // get results of all combinations
        System.out.println("SPARQL engine: rdf4j, Model: RDF4J TreeModel");
        SelectResults rdf4j_treeModel = SparqlEngine.rdf4jSelectQuery(query_string, tree_model);
        System.out.println(rdf4j_treeModel);

        System.out.println("SPARQL engine: rdf4j, Model: Corese CoreseModel");
        SelectResults rdf4j_coreseModel = SparqlEngine.rdf4jSelectQuery(query_string, corese_model);
        System.out.println(rdf4j_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: Corese CoreseModel");
        SelectResults corese_coreseModel = SparqlEngine.coreseSelectQuery(query_string, corese_model);
        System.out.println(corese_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: RDF4J TreeModel");
        SelectResults corese_treeModel = SparqlEngine.coreseSelectQuery(query_string, tree_model);
        System.out.println(corese_treeModel);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        SelectResults corese_graph = SparqlEngine.coreseSelectQuery(query_string, graph);
        System.out.println(corese_graph);

        // Compare results
        return rdf4j_treeModel.equals(rdf4j_coreseModel, corese_coreseModel, corese_treeModel, corese_graph);
    }

    public static boolean constructQuery(String query_string, LoadableFile... load_files) {
        // get results of all combinations
        System.out.println("SPARQL engine: rdf4j, Model: RDF4J TreeModel");
        Model rdf4j_treeModel = SparqlEngine.rdf4jConstructQuery(query_string, Load.treeModel(load_files));
        System.out.println(rdf4j_treeModel);

        System.out.println("SPARQL engine: rdf4j, Model: Corese CoreseModel");
        Model rdf4j_coreseModel = SparqlEngine.rdf4jConstructQuery(query_string, Load.coreseModel(load_files));
        System.out.println(rdf4j_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: Corese CoreseModel");
        Model corese_coreseModel = SparqlEngine.coreseConstructQuery(query_string, Load.coreseModel(load_files));
        System.out.println(corese_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: RDF4J TreeModel");
        Model corese_treeModel = SparqlEngine.coreseConstructQuery(query_string, Load.treeModel(load_files));
        System.out.println(corese_treeModel);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Model corese_graph = SparqlEngine.coreseConstructQuery(query_string, Load.coreseGraph(load_files));
        System.out.println(corese_graph);

        return rdf4j_treeModel.equals(rdf4j_coreseModel) && rdf4j_coreseModel.equals(corese_coreseModel)
                && corese_coreseModel.equals(corese_treeModel) && corese_treeModel.equals(corese_graph);
    }

    public static boolean describeQuery(String query_string, LoadableFile... load_files) {
        return constructQuery(query_string, load_files);
    }

    public static boolean askQuery(String query_string, LoadableFile... load_files) {

        // build models and graphs
        TreeModel tree_model = Load.treeModel(load_files);
        CoreseGraphModel corese_model = Load.coreseModel(load_files);
        Graph graph = Load.coreseGraph(load_files);

        // get results of all combinations
        System.out.println("SPARQL engine: rdf4j, Model: RDF4J TreeModel");
        Boolean rdf4j_treeModel = SparqlEngine.rdf4jAskQuery(query_string, tree_model);
        System.out.println(rdf4j_treeModel);

        System.out.println("SPARQL engine: rdf4j, Model: Corese CoreseModel");
        Boolean rdf4j_coreseModel = SparqlEngine.rdf4jAskQuery(query_string, corese_model);
        System.out.println(rdf4j_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: Corese CoreseModel");
        Boolean corese_coreseModel = SparqlEngine.coreseAskQuery(query_string, corese_model);
        System.out.println(corese_coreseModel);

        System.out.println("SPARQL engine: Corese, Model: RDF4J TreeModel");
        Boolean corese_treeModel = SparqlEngine.coreseAskQuery(query_string, tree_model);
        System.out.println(corese_treeModel);

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Boolean corese_graph = SparqlEngine.coreseAskQuery(query_string, graph);
        System.out.println(corese_graph);

        return rdf4j_treeModel.equals(rdf4j_coreseModel) && rdf4j_coreseModel.equals(corese_coreseModel)
                && corese_coreseModel.equals(corese_treeModel) && corese_treeModel.equals(corese_graph);
    }

    public static boolean updateQuery(String query_string, LoadableFile... load_files) {

        System.out.println(Load.coreseGraph(load_files).size());

        // get results of all combinations
        System.out.println("SPARQL engine: rdf4j, Model: RDF4J TreeModel");
        Model rdf4j_treeModel = SparqlEngine.rdf4jUpdateQuery(query_string, Load.treeModel(load_files));
        System.out.println(rdf4j_treeModel.size());

        System.out.println("SPARQL engine: rdf4j, Model: Corese CoreseModel");
        Model rdf4j_coreseModel = SparqlEngine.rdf4jUpdateQuery(query_string, Load.coreseModel(load_files));
        System.out.println(rdf4j_coreseModel.size());

        System.out.println("SPARQL engine: Corese, Model: Corese CoreseModel");
        Model corese_coreseModel = SparqlEngine.coreseUpdateQuery(query_string, Load.coreseModel(load_files));
        System.out.println(corese_coreseModel.size());

        System.out.println("SPARQL engine: Corese, Model: RDF4J TreeModel");
        Model corese_treeModel = SparqlEngine.coreseUpdateQuery(query_string, Load.treeModel(load_files));
        System.out.println(corese_treeModel.size());

        System.out.println("SPARQL engine: Corese, Model: Corese Graph");
        Model corese_graph = SparqlEngine.coreseUpdateQuery(query_string, Load.coreseGraph(load_files));
        System.out.println(corese_graph.size());

        return rdf4j_treeModel.equals(rdf4j_coreseModel) && rdf4j_coreseModel.equals(corese_coreseModel)
                && corese_coreseModel.equals(corese_treeModel) && corese_treeModel.equals(corese_graph);
    }

}
