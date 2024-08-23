package fr.inria.corese.w3cJunitTestsGenerator.w3cTests.implementations;

import java.net.URI;
import java.util.Set;

import fr.inria.corese.core.print.rdfc10.HashingUtility.HashAlgorithm;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.IW3cTest;

/**
 * Represents a test for the RDFC10MapTest type.
 */
public class RDFC10MapTest implements IW3cTest {

    private String test;
    private String name;
    private String comment;

    private URI actionFile;

    private URI resultFile;

    private HashAlgorithm hashAlgorithm;

    public RDFC10MapTest(String testUri, String name, String comment, URI actionUri, URI resultUri,
            HashAlgorithm hashAlgorithm) {
        this.test = testUri.split("#")[1];
        this.name = name;
        this.comment = comment;
        this.actionFile = actionUri;
        this.resultFile = resultUri;
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public Set<String> getImports() {
        return Set.of("fr.inria.corese.core.Graph",
                "fr.inria.corese.core.load.Load",
                "fr.inria.corese.core.print.CanonicalRdf10Format",
                "fr.inria.corese.core.print.rdfc10.HashingUtility.HashAlgorithm",
                "java.net.URL",
                "com.fasterxml.jackson.core.type.TypeReference",
                "com.fasterxml.jackson.databind.ObjectMapper",
                "fr.inria.corese.core.load.LoadException",
                "java.io.IOException",
                "org.junit.Test",
                "static org.junit.Assert.assertEquals",
                "java.util.Map");

    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder();

        // Header of the test
        sb.append("    // ").append(this.name).append("\n");
        if (!this.comment.isEmpty()) {
            sb.append("    // ").append(this.comment).append("\n");
        }
        sb.append("    @Test\n");
        sb.append("    public void ").append(test);
        sb.append("() throws LoadException, IOException {\n");

        // Test body
        sb.append("        // Create graph and load action file\n");
        sb.append("        Graph graph = Graph.create();\n");
        sb.append("        Load ld = Load.create(graph);\n");
        sb.append("        ld.setRenameBlankNode(false);\n");
        sb.append("        ld.parse(\"").append(actionFile).append("\");\n");
        sb.append("\n");
        sb.append("        // Create canonical RDF 1.0 format and get map of issued identifiers\n");
        if (hashAlgorithm != null && hashAlgorithm != HashAlgorithm.SHA_256) {
            sb.append("        CanonicalRdf10Format rdfc10 = CanonicalRdf10Format.create(graph, HashAlgorithm.")
                    .append(hashAlgorithm).append(");\n");
        } else {
            sb.append("        CanonicalRdf10Format rdfc10 = CanonicalRdf10Format.create(graph);\n");
        }
        sb.append("        Map<String, String> result = rdfc10.getIssuedIdentifiersMap();\n");
        sb.append("\n");
        sb.append("        // Load map from result json file\n");
        sb.append("        URL url = new URL(\"").append(resultFile).append("\");\n");
        sb.append(
                "        Map<String, String> expect = new ObjectMapper().readValue(url, new TypeReference<Map<String, String>>(){});\n");
        sb.append("\n");
        sb.append("        // Compare the two maps\n");
        sb.append("        assertEquals(expect, result);\n");
        sb.append("    }\n");

        return sb.toString();
    }

}
