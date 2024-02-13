package fr.inria.corese.core.print.rdfc10;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

import fr.inria.corese.core.EdgeFactory;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;

/**
 * The {@code CanonicalRdf10Format} class extends {@code RDFFormat} to provide
 * RDF canonicalization in alignment with the RDF 1.0 specification. This class
 * manages the process of transforming RDF graphs into a canonical form.
 * 
 * @see <a href="https://www.w3.org/TR/rdf-canon/">RDF Dataset
 *      Canonicalization</a>
 * 
 */
public class CanonicalRdf10 {

    private CanonicalizationState canonicalizationState;
    private CanonicalizedDataset canonicalizedDataset;

    //
    private EdgeFactory edgeFactory = Graph.create().getEdgeFactory();

    //////////////////
    // Constructors //
    //////////////////

    /**
     * Constructs a new {@code CanonicalRdf10Format} with the specified RDF graph.
     * Initializes the canonicalization state and dataset for the graph.
     * 
     * @param graph the RDF graph to be canonicalized
     */
    private CanonicalRdf10(Graph graph) {
        this.canonicalizationState = new CanonicalizationState();
        this.canonicalizedDataset = new CanonicalizedDataset(graph);
        canonicalRdf10();
    }

    /**
     * Constructs a new {@code CanonicalRdf10Format} with the specified RDF graph
     * and a map of blank nodes to identifiers. Initializes the canonicalization
     * state and dataset for the graph.
     * 
     * @param graph                   the RDF graph to be canonicalized
     * @param blankNodesToIdentifiers a map of blank nodes to their identifiers
     */
    private CanonicalRdf10(Graph graph, Map<Node, String> blankNodesToIdentifiers) {
        this.canonicalizationState = new CanonicalizationState();
        this.canonicalizedDataset = new CanonicalizedDataset(graph, blankNodesToIdentifiers);
        canonicalRdf10();
    }

    /////////////////////
    // Factory methods //
    /////////////////////

    /**
     * Creates a new {@code CanonicalRdf10Format} instance for the given graph.
     * 
     * @param graph the RDF graph to be canonicalized
     * @return a new instance of {@code CanonicalRdf10Format}
     */
    public static CanonicalRdf10 create(Graph graph) {
        return new CanonicalRdf10(graph);
    }

    /**
     * Creates a new {@code CanonicalRdf10Format} instance for the graph associated
     * with the given mappings.
     * 
     * @param map the mappings containing the RDF graph to be canonicalized
     * @return a new instance of {@code CanonicalRdf10Format}
     */
    public static CanonicalRdf10 create(Mappings map) {
        return new CanonicalRdf10((Graph) map.getGraph());
    }

    /**
     * Creates a new {@code CanonicalRdf10Format} instance for the given graph and
     * map of blank nodes to identifiers.
     * 
     * @param graph                   the RDF graph to be canonicalized
     * @param blankNodesToIdentifiers a map of blank nodes to their identifiers
     * @return a new instance of {@code CanonicalRdf10Format}
     */
    public static CanonicalRdf10 create(Graph graph, Map<Node, String> blankNodesToIdentifiers) {
        return new CanonicalRdf10(graph, blankNodesToIdentifiers);
    }

    /**
     * Creates a new {@code CanonicalRdf10Format} instance for the graph associated
     * with the given mappings and map of blank nodes to identifiers.
     * 
     * @param map                     the mappings containing the RDF graph to be
     *                                canonicalized
     * @param blankNodesToIdentifiers a map of blank nodes to their identifiers
     * @return a new instance of {@code CanonicalRdf10Format}
     */
    public static CanonicalRdf10 create(Mappings map, Map<Node, String> blankNodesToIdentifiers) {
        return new CanonicalRdf10((Graph) map.getGraph(), blankNodesToIdentifiers);
    }

    ////////////////////
    // Main algorithm //
    ////////////////////

    /**
     * Performs the canonicalization of an RDF 1.0 dataset.
     * 
     * @see <a href=
     *      "https://w3c.github.io/rdf-canon/spec/#canon-algo-algo/">CanonicalizationAlgorithm
     */
    public CanonicalizedDataset canonicalRdf10() {
        // Build blank nodes to identifiers if not already done
        // Build blank nodes identifiers to quads
        // 4.4.3) Step 1, 2
        this.extractQuadsForBlankNodes();

        // Build first degree hash for each blank node
        // 4.4.3) Step 3
        for (String blankNodeIdentifier : this.canonicalizedDataset.getBlankNodeIdentifiers()) {
            // 4.4.3) Step 3.1
            String hash = this.hashFirstDegreeQuads(blankNodeIdentifier);
            // 4.4.3) Step 3.2
            this.canonicalizationState.associateHashWithBlankNode(hash, blankNodeIdentifier);
        }

        // Generate canonical identifiers for blank nodes with a unique first degree
        // hash
        // 4.4.3) Step 4
        for (String hash : this.canonicalizationState.getHashesSorted()) {
            // 4.4.3) Step 4.1
            if (this.canonicalizationState.getBlankNodeForHash(hash).size() > 1) {
                continue;
            }

            // 4.4.3) Step 4.2
            String blankNodeIdentifier = this.canonicalizationState.getBlankNodeForHash(hash).get(0);
            // 4.5.2) Step 1 2, 3, 4, 5
            this.canonicalizationState.issueCanonicalBlankNodeIdFor(blankNodeIdentifier);

            // 4.4.3) Step 4.3
            this.canonicalizationState.removeHash(hash); // Can be removed inside the loop because
                                                         // this.canonicalizationState.getHashesSorted() is a copy of
                                                         // the original list
        }

        // Generate canonical identifiers for blank nodes with multiple first degree
        // hashes
        // 4.4.3) Step 5
        for (String hash : this.canonicalizationState.getHashesSorted()) {
            // 4.4.3) Step 5.1
            List<Pair<String, CanonicalIssuer>> hashPathList = new ArrayList<>();

            // 4.4.3) Step 5.2
            for (String blankNodeIdentifier : this.canonicalizationState.getBlankNodeForHash(hash)) {

                // 4.4.3) Step 5.2.1
                if (this.canonicalizationState.hasCanonicalIdentifier(blankNodeIdentifier)) {
                    continue;
                }

                // 4.4.3) Step 5.2.2
                CanonicalIssuer tempIssuer = new CanonicalIssuer("b");

                // 4.4.3) Step 5.2.3
                tempIssuer.issueCanonicalIdentifier(blankNodeIdentifier);

                // 4.4.3) Step 5.2.4
                Pair<String, CanonicalIssuer> result = this.hashNdegreeQuads(tempIssuer, blankNodeIdentifier);
                hashPathList.add(result);
            }

            // 4.4.3) Step 5.3

            // sort the list by the hash
            hashPathList.sort((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()));

            for (Pair<String, CanonicalIssuer> result : hashPathList) {
                CanonicalIssuer issuer = result.getRight();

                // 4.4.3) Step 5.3.1
                for (String existingIdentifier : issuer.getBlankNodeIdentifiers()) {
                    this.canonicalizationState.issueCanonicalBlankNodeIdFor(existingIdentifier);
                }
            }
        }

        // 4.4.3) Step 6
        // Add the issued identifiers map from the canonical issuer to the canonicalized
        // dataset.
        this.canonicalizedDataset.setIssuedIdentifierMap(this.canonicalizationState.getIssuedIdentifierMap());

        return this.canonicalizedDataset;
    }

    ////////////////////
    // Initialization //
    ////////////////////

    /**
     * Extracts the quads for blank nodes from the RDF graph and adds them to the
     * canonicalization state (BlankNodeIdentifier -> List<Quad>).
     * Also adds the blank nodes to identifiers to the canonicalized dataset if not
     * already done in the constructor of the class (BlankNode ->
     * BlankNodeIdentifier).
     */
    private void extractQuadsForBlankNodes() {
        Iterable<Edge> edges = this.canonicalizedDataset.getDataset().getEdges();

        for (Edge e : edges) {

            // Create a new clean iterable (because corse iterable does not have a perfectly
            // defined behavior for optimization reasons)
            Edge edge = this.edgeFactory.copy(e);

            Node subject = edge.getSubjectNode();
            Node object = edge.getObjectNode();
            Node graph = edge.getGraph();
            processAndMapBlankNode(subject, edge);
            processAndMapBlankNode(object, edge);
            processAndMapBlankNode(graph, edge);
        }
    }

    /**
     * Processes a given blank node by mapping to an identifier if not already done
     * and adding the associated quad to the canonicalization state. If the node is
     * not a blank node, the method does nothing.
     *
     * @param node the node to be processed and mapped
     * @param edge the edge associated with the node
     */
    private void processAndMapBlankNode(Node node, Edge edge) {
        if (node.isBlank()) {
            // Add blank node to identifiers if not already done
            this.canonicalizedDataset.associateBlankNodeWithIdentifier(node);

            // Add quad to blank node identifier
            // 4.4.3) Step 2.1
            this.canonicalizationState
                    .associateBlankNodeWithQuad(this.canonicalizedDataset.getIdentifierForBlankNode(node), edge);
        }
    }

    //////////////////////////
    // HashFirstDegreeQuads //
    //////////////////////////

    /**
     * Hashes the first degree quads for a given blank node identifier.
     * 
     * @param blankNodeIdentifier the identifier of the blank node
     * @return the hash of the first degree quads for the given blank node
     * 
     * @see <a href=https://w3c.github.io/rdf-canon/spec/#hash-1d-quads>Hashing the
     *      First Degree Quads</a>
     */
    private String hashFirstDegreeQuads(String blankNodeIdentifier) {
        // 4.6.3) Step 1
        List<String> nquads = new ArrayList<>();

        // 4.6.3) Step 2, 3
        for (Edge quad : this.canonicalizationState.getQuadsForBlankNode(blankNodeIdentifier)) {
            nquads.add(serializeQuad(quad, blankNodeIdentifier));
        }

        // 4.6.3) Step 4
        nquads.sort(String::compareTo);
        return HashingUtility.sha256(String.join("\n", nquads) + "\n");
    }

    /**
     * Serializes a quad in N-Quads format. The method replaces the blank node
     * identifier of the reference blank node with "_:a" and all other blank node
     * identifiers with "_:z".
     * 
     * @param quad                         the quad to be serialized
     * @param referenceBlankNodeIdentifier the identifier of the blank node to be
     *                                     referenced
     * @return the serialized quad
     */
    private String serializeQuad(Edge quad, String referenceBlankNodeIdentifier) {
        Node subject = quad.getSubjectNode();
        Node predicate = quad.getEdgeNode();
        Node object = quad.getObjectNode();
        Node graph = quad.getGraph();

        boolean isDefaultGraph = graph.getLabel().equals(ExpType.DEFAULT_GRAPH);

        String subjectString = getNodeString(subject, referenceBlankNodeIdentifier);
        String predicateString = predicate.toString(); // Predicates cannot be blank nodes
        String objectString = getNodeString(object, referenceBlankNodeIdentifier);
        String graphString = isDefaultGraph ? "" : getNodeString(graph, referenceBlankNodeIdentifier);

        return subjectString + " " + predicateString + " " + objectString + (isDefaultGraph ? "" : " " + graphString)
                + " .";
    }

    /**
     * Returns the string representation of a node. If the node is a blank node, the
     * method returns "_:a" if the node is the reference blank node identifier and
     * "_:z" otherwise.
     * 
     * @param node                         the node to be serialized
     * @param referenceBlankNodeIdentifier the identifier of the blank node to be
     *                                     referenced
     * @return the string representation of the node
     */
    private String getNodeString(Node node, String referenceBlankNodeIdentifier) {
        if (node.isBlank()) {
            return this.canonicalizedDataset.getIdentifierForBlankNode(node).equals(referenceBlankNodeIdentifier)
                    ? "_:a"
                    : "_:z";
        } else {
            return node.toString();
        }

    }

    ///////////////////////
    // HashN-DegreeQuads //
    ///////////////////////

    /**
     * Hashes the N-degree quads for a given blank node identifier.
     * 
     * @param issuer      the canonical issuer
     * @param blankNodeId the identifier of the blank node
     * @return a pair containing the hash of the N-degree quads for the given blank
     *         node and the canonical issuer
     * 
     * @see <a
     *      href=https://w3c.github.io/rdf-canon/spec/#hash-nd-quads-algorithm>Hashing
     *      N-degree Quads</a>
     * 
     */
    private Pair<String, CanonicalIssuer> hashNdegreeQuads(CanonicalIssuer issuer, String blankNodeId) {
        // Reference issuer because issuer is modified in the method body
        // in step 4.8.3) Step 5.6
        CanonicalIssuer refIssuer = issuer;

        // 4.8.3) Step 1
        // Use a tree map to ensure that the hashes are sorted
        Map<String, String> relatedHashToRelatedBNIdMap = new TreeMap<>();

        // 4.8.3) Step 2
        List<Edge> quads = this.canonicalizationState.getQuadsForBlankNode(blankNodeId);

        // 4.8.3) Step 3
        for (Edge quad : quads) {
            processQuadEntry(quad, refIssuer, blankNodeId, relatedHashToRelatedBNIdMap, "s", quad.getSubjectNode());
            processQuadEntry(quad, refIssuer, blankNodeId, relatedHashToRelatedBNIdMap, "o", quad.getObjectNode());
            processQuadEntry(quad, refIssuer, blankNodeId, relatedHashToRelatedBNIdMap, "g", quad.getGraph());
        }

        // 4.8.3) Step 4
        StringBuilder data = new StringBuilder();

        // 4.8.3) Step 5
        // Hash are sorted by the tree map
        for (String hash : relatedHashToRelatedBNIdMap.keySet()) {

            // 4.8.3) Step 5.1
            data.append(hash);

            // 4.8.3) Step 5.2
            String chosenPath = "";

            // 4.8.3) Step 5.3
            CanonicalIssuer chosenIssuer = null;

            // 4.8.3) Step 5.4
            for (List<String> permutation : this.permute(new ArrayList<>(relatedHashToRelatedBNIdMap.values()))) {

                // 4.8.3) Step 5.4.1
                CanonicalIssuer issuerCopy = new CanonicalIssuer(refIssuer);

                // 4.8.3) Step 5.4.2
                String path = "";

                // 4.8.3) Step 5.4.3
                List<String> recursionList = new ArrayList<>();

                // 4.8.3) Step 5.4.4
                for (String relatedBNId : permutation) {

                    // 4.8.3) Step 5.4.4.1
                    if (this.canonicalizationState.hasCanonicalIdentifier(relatedBNId)) {
                        path += "_:" + this.canonicalizationState.getCanonicalIdentifierFor(relatedBNId);
                    }
                    // 4.8.3) Step 5.4.4.2
                    else {
                        // 4.8.3) Step 5.4.4.2.1
                        if (!issuerCopy.hasCanonicalIdentifier(relatedBNId)) {
                            recursionList.add(relatedBNId);
                        }
                        // 4.8.3) Step 5.4.4.2.2
                        path += "_:" + issuerCopy.issueCanonicalIdentifier(relatedBNId);
                    }

                    // 4.8.3) Step 5.4.4.3
                    if (!chosenPath.isEmpty() && path.length() >= chosenPath.length()
                            && path.compareTo(chosenPath) > 0) {
                        break;
                    }
                }

                // 4.8.3) Step 5.4.5
                for (String relatedBNId : recursionList) {
                    // 4.8.3) Step 5.4.5.1
                    Pair<String, CanonicalIssuer> result = this.hashNdegreeQuads(issuerCopy, relatedBNId);

                    // 4.8.3) Step 5.4.5.2
                    path += "_:" + issuerCopy.issueCanonicalIdentifier(relatedBNId);

                    // 4.8.3) Step 5.4.5.3
                    path += "<" + result + ">";

                    // 4.8.3) Step 5.4.5.4
                    issuerCopy = result.getRight();

                    // 4.8.3) Step 5.4.5.5
                    if (!chosenPath.isEmpty() && path.length() >= chosenPath.length()
                            && path.compareTo(chosenPath) > 0) {
                        break;
                    }
                }

                // 4.8.3) Step 5.4.6
                if (chosenPath.isEmpty() || path.compareTo(chosenPath) < 0) {
                    chosenPath = path;
                    chosenIssuer = issuerCopy;
                }
            }

            // 4.8.3) Step 5.5
            data.append(chosenPath);

            // 4.8.3) Step 5.6
            refIssuer = chosenIssuer;
        }

        // 4.8.3) Step 6
        return Pair.of(HashingUtility.sha256(data.toString()), refIssuer);
    }

    /**
     * Generates all possible permutations of a given list.
     *
     * @param original The original list to be permuted.
     * @param <T>      The type of elements in the list.
     * @return A list of lists, where each inner list represents a permutation of
     *         the original list.
     */
    private <T> List<List<T>> permute(List<T> original) {
        if (original.isEmpty()) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }

        T firstElement = original.remove(0);
        List<List<T>> returnValue = new ArrayList<>();
        List<List<T>> permutations = permute(original);

        for (List<T> smallerPermutated : permutations) {
            for (int index = 0; index <= smallerPermutated.size(); index++) {
                List<T> temp = new ArrayList<>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
            }
        }
        return returnValue;
    }

    /**
     * Processes a quad entry by generating a hash for the related blank node and
     * updating the hash-to-blank-node map.
     *
     * @param quad                        The quad edge to process.
     * @param issuer                      The canonical issuer.
     * @param blankNodeId                 The identifier for the current blank node.
     * @param relatedHashToRelatedBNIdMap The map that stores the hash-to-blank-node
     *                                    mappings.
     * @param position                    The position of the quad entry.
     * @param relatedBN                   The related blank node.
     */
    private void processQuadEntry(Edge quad, CanonicalIssuer issuer, String blankNodeId,
            Map<String, String> relatedHashToRelatedBNIdMap, String position, Node relatedBN) {
        String relatedBNId = this.canonicalizedDataset.getIdentifierForBlankNode(relatedBN);

        if (relatedBN.isBlank() && !relatedBNId.equals(blankNodeId)) {
            // 4.8.3) Step 3.1.1
            String relatedHash = this.hashRelatedBlankNode(relatedBNId, quad, issuer, position);

            // 4.8.3) Step 3.1.2
            relatedHashToRelatedBNIdMap.put(relatedHash, relatedBNId);
        }
    }

    //////////////////////////
    // HashRelatedBlankNode //
    //////////////////////////

    /**
     * Hashes a related blank node.
     * 
     * @param relatedBNId the identifier of the related blank node
     * @param quad        the quad to be associated with the blank node
     * @param issuer      the canonical issuer
     * @param position    the position of the related blank node
     * @return the related hash for the related blank node
     * 
     * @see <a
     *      href=https://w3c.github.io/rdf-canon/spec/#hash-related-blank-node>Hashing
     *      a Related Blank Node</a>
     * 
     */
    private String hashRelatedBlankNode(String relatedBNId, Edge quad, CanonicalIssuer issuer,
            String position) {
        // 4.7.3) Step 1
        StringBuilder input = new StringBuilder();
        input.append(position);

        // 4.7.3) Step 2
        // Append predicate value if position is not 'g'
        if (!position.equals("g")) {
            input.append(quad.getPredicateValue().toString());
        }

        // 4.7.3) Step 3
        // If there is a canonical identifier for relatedBNId, use it; otherwise, use
        // the issuer's identifier.
        if (this.canonicalizationState.hasCanonicalIdentifier(relatedBNId)
                || issuer.hasCanonicalIdentifier(relatedBNId)) {

            input.append("_:" + (this.canonicalizationState.hasCanonicalIdentifier(relatedBNId)
                    ? this.canonicalizationState.getCanonicalIdentifierFor(relatedBNId)
                    : issuer.getCanonicalIdentifier(relatedBNId)));
        }
        // 4.7.3) Step 4
        // Append hash for blank node as fallback
        else {
            input.append(this.canonicalizationState.getHashForBlankNode(relatedBNId));
        }

        // 4.7.3) Step 5
        return HashingUtility.sha256(input.toString());
    }

    /////////////////////////
    // Overriding toString //
    /////////////////////////

    /**
     * Returns a string representation of the RDF graph in canonical form.
     * 
     * @return a string representation of the RDF graph in canonical form
     */
    @Override
    public String toString() {
        return super.toString();
    }

}
