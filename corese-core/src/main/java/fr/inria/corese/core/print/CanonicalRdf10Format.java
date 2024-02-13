package fr.inria.corese.core.print;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.rdfc10.CanonicalRdf10;
import fr.inria.corese.core.print.rdfc10.CanonicalizedDataset;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;

/**
 * This class provides functionality to convert a Graph object to a string in
 * Canonical RDF 1.0 format.
 */
public class CanonicalRdf10Format extends NQuadsFormat {

    private CanonicalizedDataset canonicalizedDataset;

    public CanonicalRdf10Format(Graph graph) {
        super(graph);
        this.canonicalizedDataset = CanonicalRdf10.create(graph).canonicalRdf10();
    }

    public static CanonicalRdf10Format create(Graph graph) {
        return new CanonicalRdf10Format(graph);
    }

    public static CanonicalRdf10Format create(Mappings map) {
        return new CanonicalRdf10Format((Graph) map.getGraph());
    }

    /**
     * Converts the graph to a string in Canonical RDF 1.0 format.
     * 
     * @return a string representation of the graph in Canonical RDF 1.0 format
     */
    @Override
    public String toString() {
        String nquads = super.toString();

        // Sort in codepoint order by line
        String[] lines = nquads.split("\n");
        java.util.Arrays.sort(lines);

        // Concatenate lines
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    @Override
    protected String printBlank(Node node) {
        String identifier = this.canonicalizedDataset.getIdentifierForBlankNode(node);
        return "_:" + this.canonicalizedDataset.getIssuedIdentifier(identifier);
    }

}
