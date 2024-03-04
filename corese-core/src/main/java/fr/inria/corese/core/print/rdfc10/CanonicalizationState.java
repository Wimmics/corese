package fr.inria.corese.core.print.rdfc10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.inria.corese.kgram.api.core.Edge;

/**
 * This class manages the state of canonicalization, particularly handling
 * the associations between blank nodes and their corresponding quads,
 * maintaining a mapping from hash values to blank nodes and maintaining a
 * mapping from blank node identifiers to canonical blank node identifiers.
 */
public class CanonicalizationState {

    private final ListMap<String, Edge> blankNodesToQuad = new ListMap<>();
    private final ListMap<String, String> hashToBlankNode = new ListMap<>();
    private final CanonicalIssuer canonicalIssuer = new CanonicalIssuer("c14n");

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructs a new CanonicalizationState instance.
     */
    public CanonicalizationState() {
    }

    ///////////////////////////////////
    // Quad to Blank Node Management //
    ///////////////////////////////////

    /**
     * Maps a blank node identifier to a specific quad.
     * 
     * @param blankNodeId The identifier of the blank node.
     * @param quad        The quad to be associated with the blank node.
     */
    public void associateBlankNodeWithQuad(String blankNodeId, Edge quad) {
        this.blankNodesToQuad.add(blankNodeId, quad);
    }

    /**
     * Retrieves the list of quads associated with a specific blank node.
     * 
     * @param blankNodeId The identifier of the blank node.
     * @return A list of quads associated with the blank node.
     */
    public List<Edge> getQuadsForBlankNode(String blankNodeId) {
        return Collections.unmodifiableList(this.blankNodesToQuad.get(blankNodeId));
    }

    ///////////////////////////////////
    // Hash to Blank Node Management //
    ///////////////////////////////////

    /**
     * Maps a hash value to a specific blank node identifier.
     * 
     * @param hash        The hash value.
     * @param blankNodeId The identifier of the blank node.
     */
    public void associateHashWithBlankNode(String hash, String blankNodeId) {
        this.hashToBlankNode.add(hash, blankNodeId);
    }

    /**
     * Retrieves blanks nodes associated with a specific hash value.
     * 
     * @param hash The hash value.
     * @return A list of blank nodes associated with the hash value.
     */
    public void removeHash(String hash) {
        this.hashToBlankNode.remove(hash);
    }

    /**
     * Retrieves sorted list of blank nodes identifiers associated with a specific
     * hash value.
     * 
     * @param hash The hash value.
     * @return A list of blank nodes associated with the hash value.
     */
    public List<String> getBlankNodeForHash(String hash) {
        List<String> list = this.hashToBlankNode.get(hash);
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    /**
     * Retrieves the hash value associated with a specific blank node identifier.
     * 
     * @param blankNodeId The identifier of the blank node.
     * @return The hash value associated with the blank node or null if no hash
     *         value is associated.
     */
    public String getHashForBlankNode(String blankNodeId) {
        for (String hash : hashToBlankNode.keySet()) {
            if (hashToBlankNode.get(hash).contains(blankNodeId)) {
                return hash;
            }
        }
        return null;
    }

    /**
     * Retrieves a sorted list of hashes.
     * 
     * @return A copy of the list of hashes sorted in code point order.
     */
    public List<String> getHashesSorted() {
        // hash are sorted in code point order by the ListMap implementation
        List<String> sortedHashes = new ArrayList<>(hashToBlankNode.keySet());
        return Collections.unmodifiableList(sortedHashes);
    }

    ////////////////////////////////////////
    // Canonical Blank Node ID Management //
    ////////////////////////////////////////

    /**
     * Issues a canonical blank node identifier for a given blank node identifier.
     * If a canonical blank node identifier has already been issued for the given
     * blank node identifier, the previously issued identifier is returned.
     * 
     * @param blankNodeId The blank node identifier.
     * @return The canonical blank node identifier.
     */
    public String issueCanonicalBlankNodeIdFor(String blankNodeId) {
        return this.canonicalIssuer.issueCanonicalIdentifier(blankNodeId);
    }

    /**
     * Tests whether a canonical blank node identifier has been issued for a given
     * blank node identifier.
     * 
     * @param blankNodeId The blank node identifier.
     * @return True if a canonical blank node identifier has been issued for the
     *         given
     */
    public boolean hasCanonicalIdentifier(String blankNodeId) {
        return this.canonicalIssuer.hasCanonicalIdentifier(blankNodeId);
    }

    /**
     * Retrieves the canonical blank node identifier for a given blank node
     * identifier.
     * 
     * @param blankNodeId The blank node identifier.
     * @return The canonical blank node identifier.
     */
    public String getCanonicalIdentifierFor(String blankNodeId) {
        return this.canonicalIssuer.getCanonicalIdentifier(blankNodeId);
    }

    /**
     * Retrieves the issued identifier map.
     * 
     * @return A unmodifiable map of issued blank node identifiers to canonical
     */
    public Map<String, String> getIssuedIdentifierMap() {
        return this.canonicalIssuer.getIssuedIdentifierMap();
    }

    ///////////////
    // To String //
    ///////////////

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Blank Nodes to Quads Mapping: \n");
        this.blankNodesToQuad.forEach((key, value) -> sb.append(key).append(" -> ").append(value).append("\n"));

        sb.append("\n");

        sb.append("Hash to Blank Node Mapping: \n");
        this.hashToBlankNode.forEach((key, value) -> sb.append(key).append(" -> ").append(value).append("\n"));

        sb.append("\n");

        sb.append("Blank Node to Canonical Blank Node Mapping: \n");
        this.canonicalIssuer.getBlankNodeIdentifiers().forEach(identifier -> {
            sb.append(identifier);
            sb.append(" -> ");
            sb.append(this.canonicalIssuer.issueCanonicalIdentifier(identifier));
            sb.append("\n");
        });

        return sb.toString();
    }
}
