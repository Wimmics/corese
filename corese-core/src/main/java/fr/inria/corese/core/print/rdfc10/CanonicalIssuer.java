package fr.inria.corese.core.print.rdfc10;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class manages the issuance of canonical identifiers for blank nodes.
 */
public class CanonicalIssuer {

    private final String IDPREFIX;
    private int idCounter;
    // Maps blank node identifiers to their canonical identifiers
    // Use LinkedHashMap to preserve insertion order
    private final LinkedHashMap<String, String> issuedIdentifierMap;

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructs a new CanonicalIssuer instance.
     * 
     * @param idPrefix The prefix to be used for identifiers issued by this
     */
    public CanonicalIssuer(String idPrefix) {
        this.IDPREFIX = idPrefix;
        this.idCounter = 0;
        this.issuedIdentifierMap = new LinkedHashMap<>();
    }

    /**
     * Constructs a new CanonicalIssuer instance as a copy of another.
     * 
     * @param ci The CanonicalIssuer to copy.
     */
    public CanonicalIssuer(CanonicalIssuer ci) {
        this.IDPREFIX = ci.IDPREFIX;
        this.idCounter = ci.idCounter;
        this.issuedIdentifierMap = new LinkedHashMap<>(ci.issuedIdentifierMap);
    }

    /////////////
    // Methods //
    /////////////

    /**
     * Issues a new canonical identifier for a blank node or returns an existing one
     * if already issued.
     * 
     * @return The canonical identifier for the blank node.
     */
    public String issueCanonicalIdentifier(String blankNodeId) {
        if (this.issuedIdentifierMap.containsKey(blankNodeId)) {
            return this.issuedIdentifierMap.get(blankNodeId);
        }
        String issuedIdentifier = this.IDPREFIX + this.idCounter;
        this.idCounter++;
        this.issuedIdentifierMap.put(blankNodeId, issuedIdentifier);
        return issuedIdentifier;
    }

    /**
     * Retrieves the canonical identifier for a blank node.
     * 
     * @param blankNodeId The identifier of the blank node.
     * @return The canonical identifier for the blank node.
     */
    public String getCanonicalIdentifier(String blankNodeId) {
        return this.issuedIdentifierMap.get(blankNodeId);
    }

    /**
     * Retrieves a set of all issued blank node identifiers.
     * 
     * @return A set of all issued blank node identifiers.
     */
    public Set<String> getBlankNodeIdentifiers() {
        return Collections.unmodifiableSet(this.issuedIdentifierMap.keySet());
    }

    /**
     * Tests whether a blank node has a canonical identifier.
     * 
     * @param blankNodeId The identifier of the blank node.
     * @return True if the blank node has a canonical identifier
     *         false otherwise.
     */
    public boolean hasCanonicalIdentifier(String blankNodeId) {
        return this.issuedIdentifierMap.containsKey(blankNodeId);
    }

    /**
     * Retrieves the issued identifier map.
     * 
     * @return The issued identifier map.
     */
    public Map<String, String> getIssuedIdentifierMap() {
        return Collections.unmodifiableMap(this.issuedIdentifierMap);
    }

    @Override
    public String toString() {
        return this.issuedIdentifierMap.toString();
    }

}
