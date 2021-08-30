package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import org.eclipse.rdf4j.model.Resource;

import fr.inria.corese.core.Graph;

public abstract class ClearMethods {

    /**
     * Removes all statements in graph.
     * 
     * @param corese_graph The context of the statements to remove.
     */
    public static void clearAll(Graph corese_graph) {
        corese_graph.clear();
        corese_graph.dropGraphNames();
    }

    /**
     * Removes statements with the specified context exist in graph.
     * 
     * @param corese_graph Graph to clear.
     * @param contexts     The context of the statements to remove.
     * @return true if one or more statements have been removed.
     */
    public static boolean clearGraph(Graph corese_graph, Resource... contexts) {

        Boolean result = false;

        for (Resource context : contexts) {
            String graph_name = context.stringValue();
            result |= corese_graph.clear(graph_name);
            corese_graph.deleteGraph(graph_name);
        }
        return result;
    }

}