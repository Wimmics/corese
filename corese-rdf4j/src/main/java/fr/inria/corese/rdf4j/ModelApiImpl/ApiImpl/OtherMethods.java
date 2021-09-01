package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.Statement;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

public class OtherMethods {

    /**
     * Test is the graph is empty.
     * 
     * @param corese_graph The graph to test.
     * @return True if the graph is empty, else false.
     */
    public static boolean isEmpty(Graph corese_graph) {
        return !ContainsMethods.containsSPO(corese_graph, null, null, null);
    }

    /**
     * Return the size of the graph.
     * 
     * @param corese_graph The graph to evaluate.
     * @return Size of the graph.
     */
    public static int size(Graph corese_graph) {
        return corese_graph.size();
    }

    /**
     * Returns an iterator over the elements in this graph.
     * 
     * @param corese_graph The graph to iterate.
     * @return Iterator over the elements in this graph.
     */
    public static Iterator<Statement> iterator(Graph corese_graph) {
        Iterator<Edge> statements = corese_graph.iterator();

        List<Statement> result = new ArrayList<>();
        while (statements.hasNext()) {
            Edge edge_copy = corese_graph.getEdgeFactory().copy(statements.next());
            result.add(edge_copy);
        }

        return result.iterator();
    }

}
