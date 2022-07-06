package fr.inria.corese.jenaImpl.combination.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.query.Dataset;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.storage.jenatdb1.JenaDataManager;

public class CompareGraphDataset {

    public static boolean compareGraph(Dataset jena_dataset, Graph corese_graph) {

        List<Edge> jena_edges;
        try (JenaDataManager dm = new JenaDataManager(jena_dataset)) {
            // Get edges from Jena
            jena_edges = StreamSupport
                    .stream(dm.choose(null, null, null, null).spliterator(), false)
                    .collect(Collectors.toList());
        }

        // Get edges from Corese
        Iterable<Edge> corese_edges_iter = corese_graph.getEdgesRDF4J(null, null, null);

        // Create a new clean iterable (because corse iterable does not have a perfectly
        // defined behavior for optimization reasons)
        ArrayList<Edge> corese_edges = new ArrayList<>();
        for (Edge edge : corese_edges_iter) {
            if (edge != null) {
                corese_edges.add(corese_graph.getEdgeFactory().copy(edge));
            }
        }

        // Compare
        return corese_edges.containsAll(jena_edges) && jena_edges.containsAll(corese_edges);
    }


    public static boolean compareGraph(Graph graph1, Graph graph2) {

        // Get edges from Graph1
        Iterable<Edge> graph1_edges_iter = graph1.getEdgesRDF4J(null, null, null);

        // Create a new clean iterable (because corse iterable does not have a perfectly
        // defined behavior for optimization reasons)
        ArrayList<Edge> graph1_edges = new ArrayList<>();
        for (Edge edge : graph1_edges_iter) {
            if (edge != null) {
                graph1_edges.add(graph1.getEdgeFactory().copy(edge));
            }
        }

        // Get edges from Graph2
        Iterable<Edge> graph2_edges_iter = graph2.getEdgesRDF4J(null, null, null);

        // Create a new clean iterable (because corse iterable does not have a perfectly
        // defined behavior for optimization reasons)
        ArrayList<Edge> graph2_edges = new ArrayList<>();
        for (Edge edge : graph2_edges_iter) {
            if (edge != null) {
                graph2_edges.add(graph2.getEdgeFactory().copy(edge));
            }
        }

        // Compare
        return graph2_edges.containsAll(graph1_edges) && graph1_edges.containsAll(graph2_edges);
    }
}
