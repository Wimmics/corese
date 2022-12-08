package fr.inria.corese.jenaImpl.combination.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.Dataset;

import com.google.common.collect.Lists;

import fr.inria.corese.core.Graph;
import fr.inria.corese.jena.JenaTdb1DataManager;
import fr.inria.corese.jena.JenaTdb1DataManagerBuilder;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

public class CompareGraphDataset {

    @SuppressWarnings("unchecked")
    public static boolean compareGraph(Dataset dataset, Graph corese_graph) {

        ArrayList<Edge> jena_edges = new ArrayList<>();
        try (JenaTdb1DataManager dm = new JenaTdb1DataManagerBuilder().dataset(dataset).storagePath(null).build();) {
            // Get edges from Jena
            Method method = dm.getClass().getDeclaredMethod("chooseQuadDuplicatesWrite", Node.class, Node.class,
                    Node.class, List.class);
            method.setAccessible(true);

            Iterator<Edge> r = (Iterator<Edge>) method.invoke(dm, null, null, null, null);
            jena_edges = Lists.newArrayList(r);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
