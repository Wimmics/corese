package fr.inria.corese.core.print.rdfc10;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Represents a dataset that has undergone canonicalization.
 * This class manages a graph and the mapping of blank nodes to their
 * identifiers.
 */
public class CanonicalizedDataset {

    private final Graph dataset;
    private Map<Node, String> blankNodesToIdentifiers = new HashMap<>();
    private final boolean blankNodesToIdentifiersInitialized;
    private Map<String, String> issuedIdentifierMap = new HashMap<>();

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructs a CanonicalizedDataset with a given graph.
     * Initializes the blank node to identifier mapping as uninitialized.
     * 
     * @param graph The graph to be associated with this dataset.
     */
    public CanonicalizedDataset(Graph graph) {
        this.dataset = graph;
        this.blankNodesToIdentifiersInitialized = false;
    }

    /**
     * Constructs a CanonicalizedDataset with a given graph and a pre-defined
     * mapping of blank nodes to identifiers.
     * 
     * @param graph                   The graph to be associated with this dataset.
     * @param blankNodesToIdentifiers The pre-defined mapping of blank nodes to
     *                                their identifiers.
     */
    public CanonicalizedDataset(Graph graph, Map<Node, String> blankNodesToIdentifiers) {
        this.dataset = graph;
        this.blankNodesToIdentifiers = blankNodesToIdentifiers;
        this.blankNodesToIdentifiersInitialized = true;
    }

    ////////////////////////
    // Dataset Management //
    ////////////////////////

    /**
     * Retrieves the dataset associated with this CanonicalizedDataset.
     * 
     * @return The associated graph.
     */
    public Graph getDataset() {
        return dataset;
    }

    ///////////////////////////////////////////////////
    // Blank Nodes to Identifiers Mapping Management //
    ///////////////////////////////////////////////////

    /**
     * Adds a blank node and its identifier to the mapping.
     * Only adds the blank node identifier if the mapping has not been initialized.
     * 
     * @param blankNode The blank node to be added.
     * @throws IllegalArgumentException if the node is not a blank node.
     */
    public void associateBlankNodeWithIdentifier(Node blankNode) {
        if (!blankNode.isBlank()) {
            throw new IllegalArgumentException("Node is not blank");
        }

        if (this.blankNodesToIdentifiersInitialized) {
            return;
        }

        String identifier = blankNode.getLabel();
        this.blankNodesToIdentifiers.put(blankNode, identifier);
    }

    /**
     * Retrieves the identifier associated with a given blank node.
     * 
     * @param blankNode The blank node.
     * @return The identifier associated with the blank node.
     */
    public String getIdentifierForBlankNode(Node blankNode) {
        return blankNodesToIdentifiers.get(blankNode);
    }

    /**
     * Retrieves the mapping of blank nodes to identifiers.
     * 
     * @return The mapping of blank nodes to identifiers.
     */
    public Collection<String> getBlankNodeIdentifiers() {
        return Collections.unmodifiableCollection(blankNodesToIdentifiers.values());
    }

    //////////////////////////////////////
    // Issued Identifier Map Management //
    //////////////////////////////////////

    /**
     * Sets the issued identifier map.
     * 
     * @param issuedIdentifierMap The issued identifier map.
     */
    public void setIssuedIdentifierMap(Map<String, String> issuedIdentifierMap) {
        this.issuedIdentifierMap = issuedIdentifierMap;
    }

    /**
     * Retrieves the issued identifier map.
     * 
     * @return The issued identifier map.
     */
    public String getIssuedIdentifier(String blankNodeId) {
        return issuedIdentifierMap.get(blankNodeId);
    }

    ///////////////
    // To String //
    ///////////////

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Dataset: \n");
        sb.append(dataset.size());
        sb.append(" triples\n");

        sb.append("\n");

        sb.append("Blank Nodes to Identifiers Mapping: \n");
        blankNodesToIdentifiers.forEach((blankNode, identifier) -> {
            sb.append(blankNode);
            sb.append(" -> ");
            sb.append(identifier);
            sb.append("\n");
        });

        sb.append("\n");

        sb.append("Issued Identifier Map: \n");
        issuedIdentifierMap.forEach((blankNodeId, issuedIdentifier) -> {
            sb.append(blankNodeId);
            sb.append(" -> ");
            sb.append(issuedIdentifier);
            sb.append("\n");
        });

        return sb.toString();
    }
}
