package fr.inria.corese.jenaImpl.combination.engine;


import static org.junit.Assert.assertEquals;

import org.apache.jena.query.Dataset;
import org.junit.Test;

import fr.inria.corese.core.Graph;

public class CombinationTests {

        @Test
        public void LoadFile() {
                LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
                LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
                                "http://example.org/Context1", "http://example.org/Context2",
                                "http://example.org/Context3");

                Graph corese_graph = Load.coreseGraph(loadable_file_1, loadable_file_2);

                Dataset jena_dataset = Load.JenaDataset(loadable_file_1, loadable_file_2);

                assertEquals(true, CompareGraphDataset.compareGraph(jena_dataset, corese_graph));
        }

        @Test
        public void compareResults() {
                LoadableFile loadable_file_1 = new LoadableFile("edithPiaf/isa.ttl", RdfFormat.TURTLE);
                LoadableFile loadable_file_2 = new LoadableFile("edithPiaf/firstName.ttl", RdfFormat.TURTLE,
                                "http://example.org/Context1", "http://example.org/Context2",
                                "http://example.org/Context3");
                String select_where_SPO = "prefix ex: <http://example.org/> select * where { ?s ?p ?o }";

                // CoreseSPAQL graph
                Graph graph = Load.coreseGraph(loadable_file_1, loadable_file_2);
                SelectResults results_1 = SparqlEngine.selectQuery(select_where_SPO, graph);

                // Jena dataset
                Dataset dataset = Load.JenaDataset(loadable_file_1, loadable_file_2);
                SelectResults results_2 = SparqlEngine.selectQuery(select_where_SPO, dataset);

                System.out.println(results_1.equals(results_2));
                System.out.println(results_1.equals(results_1));

                Dataset other_dataset = Load.JenaDataset(loadable_file_1);
                SelectResults not_equal = SparqlEngine.selectQuery(select_where_SPO, other_dataset);
                System.out.println(results_1.equals(results_1, results_2, results_1));
                System.out.println(results_1.equals(results_1, results_2, not_equal));
        }
}
