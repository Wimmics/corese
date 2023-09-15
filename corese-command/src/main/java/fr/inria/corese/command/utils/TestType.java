package fr.inria.corese.command.utils;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 * Utility class to test the type of a string.
 */
public class TestType {

    /**
     * Detect if a string is a possible SPARQL query.
     *
     * @param input String to check.
     * @return True if the string seems to be a SPARQL query, false otherwise.
     */
    public static boolean isSparqlQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        try {
            Query query = exec.compile(input);
            return query != null;
        } catch (EngineException e) {
            return false;
        }
    }

    /**
     * Detect if a graph contains SHACL shapes.
     *
     * @param graph Graph to check.
     * @return True if the graph contains SHACL shapes, false otherwise.
     */
    public static boolean isShacl(Graph graph) {
        if (graph == null || graph.size() == 0) {
            return false;
        }

        graph.init();
        Node nodeShape = DatatypeMap.createResource("http://www.w3.org/ns/shacl#NodeShape");
        Node propertyShape = DatatypeMap.createResource("http://www.w3.org/ns/shacl#PropertyShape");

        return graph.getEdgesRDF4J(null, null, nodeShape).iterator().hasNext() ||
                graph.getEdgesRDF4J(null, null, propertyShape).iterator().hasNext();
    }
}
