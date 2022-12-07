package fr.inria.corese.rdf4jImpl.combination.engine;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.rdf4j.CoreseGraphModel;

public class CombinationTests {

    private static final Logger logger = LogManager.getLogger(CombinationTests.class.getName());

    @Test
    public void LoadFile() {
        LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
        LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
                "http://example.org/Context1", "http://example.org/Context2", "http://example.org/Context3");

        Graph graph = Load.coreseGraph(loadable_file_1, loadable_file_2);
        logger.debug("Graph (Corese): " + new CoreseGraphModel(graph));

        TreeModel treeModel = Load.treeModel(loadable_file_1, loadable_file_2);
        logger.debug("TreeModel (RDF4J): " + treeModel);

        CoreseGraphModel coreseModel = Load.coreseModel(loadable_file_1, loadable_file_2);
        logger.debug("CoreseModel (Corese): " + coreseModel);

        assertEquals(new CoreseGraphModel(graph), treeModel);
        assertEquals(new CoreseGraphModel(graph), coreseModel);
        assertEquals(treeModel, coreseModel);
    }

    @Test
    public void compareResults() {
        LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
        LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
                "http://example.org/Context1", "http://example.org/Context2", "http://example.org/Context3");
        String select_where_SPO = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";

        // CoreseSPAQL graph
        Graph graph = Load.coreseGraph(loadable_file_1, loadable_file_2);
        SelectResults results_1 = SparqlEngine.coreseSelectQuery(select_where_SPO, graph);

        // Rdf4jSPARQL treeModel
        TreeModel treeModel = Load.treeModel(loadable_file_1, loadable_file_2);
        SelectResults results_2 = SparqlEngine.rdf4jSelectQuery(select_where_SPO, treeModel);

        System.out.println(results_1.equals(results_2));
        System.out.println(results_1.equals(results_1));

        TreeModel tree_model = Load.treeModel(loadable_file_1);
        SelectResults not_equal = SparqlEngine.coreseSelectQuery(select_where_SPO, tree_model);
        System.out.println(results_1.equals(results_1, results_2, results_1));
        System.out.println(results_1.equals(results_1, results_2, not_equal));
    }
}
