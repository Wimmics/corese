package fr.inria.corese.core.print;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.core.Mappings;

public class NQuadsFormat extends NTriplesFormat {

    public NQuadsFormat(Graph graph) {
        super(graph);
    }

    public static NQuadsFormat create(Graph graph) {
        return new NQuadsFormat(graph);
    }

    public static NQuadsFormat create(Mappings map) {
        return new NQuadsFormat((Graph) map.getGraph());
    }

    /**
     * Converts the graph to a string in N-Quads format.
     * 
     * @return a string representation of the graph in N-Quads format
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Edge e : graph.getEdges()) {

            // Create a new clean iterable (because corse iterable does not have a perfectly
            // defined behavior for optimization reasons)
            Edge edge = this.graph.getEdgeFactory().copy(e);

            sb.append(printNode(edge.getNode(0)))
                    .append(" ")
                    .append(printNode(edge.getEdgeNode()))
                    .append(" ")
                    .append(printNode(edge.getNode(1)))
                    .append(" ");

            if (edge.getGraph().getValue().stringValue() != ExpType.DEFAULT_GRAPH) {
                sb.append(printNode(edge.getGraph()));
                sb.append(" ");
            }

            sb.append(".\n");
        }

        return sb.toString();
    }

}
