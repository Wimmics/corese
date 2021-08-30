package fr.inria.corese.rdf4j.ModelApiImpl.ApiImpl;

import fr.inria.corese.core.Graph;

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

}
