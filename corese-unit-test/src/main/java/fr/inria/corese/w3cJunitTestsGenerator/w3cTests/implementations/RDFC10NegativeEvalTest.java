package fr.inria.corese.w3cJunitTestsGenerator.w3cTests.implementations;

import java.net.URI;
import java.util.Set;

import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.IW3cTest;

/**
 * Represents a test for the RDFC10NegativeEvalTest type.
 */
public class RDFC10NegativeEvalTest implements IW3cTest {

    private String test;
    private String name;
    private String comment;

    private URI actionFile;

    public RDFC10NegativeEvalTest(String testUri, String name, String comment, URI actionUri) {
        this.test = testUri.split("#")[1];
        this.name = name;
        this.comment = comment;
        this.actionFile = actionUri;
    }

    @Override
    public Set<String> getImports() {
        return Set.of(
                "fr.inria.corese.core.print.rdfc10.CanonicalRdf10.CanonicalizationException",
                "java.io.IOException",
                "fr.inria.corese.core.load.LoadException",
                "fr.inria.corese.core.Graph",
                "fr.inria.corese.core.load.Load",
                "fr.inria.corese.core.print.CanonicalRdf10Format",
                "org.junit.Test");
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder();

        // Header of the test
        sb.append("    // ").append(this.name).append("\n");
        if (!this.comment.isEmpty()) {
            sb.append("    // ").append(this.comment).append("\n");
        }
        sb.append("    @Test(expected = CanonicalizationException.class)\n");
        sb.append("    public void ").append(test);
        sb.append("() throws IOException, LoadException {\n");

        // Test body
        sb.append("        // Create graph and load action file\n");
        sb.append("        Graph graph = Graph.create();\n");
        sb.append("        Load ld = Load.create(graph);\n");
        sb.append("        ld.parse(\"").append(actionFile).append("\");\n");
        sb.append("\n");
        sb.append("        // Attempt to create canonical RDF 1.0 format, expecting a failure\n");
        sb.append("        CanonicalRdf10Format rdfc10 = CanonicalRdf10Format.create(graph);\n");
        sb.append("        // This line should trigger the CanonicalizationException\n");
        sb.append("        rdfc10.toString();\n");

        // Footer of the test
        sb.append("    }\n");

        return sb.toString();
    }

}
