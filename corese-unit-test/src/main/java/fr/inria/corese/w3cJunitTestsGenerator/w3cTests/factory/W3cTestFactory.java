package fr.inria.corese.w3cJunitTestsGenerator.w3cTests.factory;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import fr.inria.corese.core.print.rdfc10.HashingUtility.HashAlgorithm;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.IW3cTest;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.implementations.RDFC10EvalTest;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.implementations.RDFC10MapTest;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.implementations.RDFC10NegativeEvalTest;

/**
 * Factory for creating W3C tests.
 */
public class W3cTestFactory {

    /**
     * Map of test type URIs to test types.
     */
    private static final Map<String, TestType> typeMap = Map.of(
            "https://w3c.github.io/rdf-canon/tests/vocab#RDFC10EvalTest", TestType.RDFC10EvalTest,
            "https://w3c.github.io/rdf-canon/tests/vocab#RDFC10MapTest", TestType.RDFC10MapTest,
            "https://w3c.github.io/rdf-canon/tests/vocab#RDFC10NegativeEvalTest", TestType.RDFC10NegativeEvalTest);

    /**
     * Enumeration of test types.
     */
    public enum TestType {
        RDFC10EvalTest,
        RDFC10MapTest,
        RDFC10NegativeEvalTest
    }

    /**
     * Creates a W3C test from the specified test name, type URI, and query process.
     * 
     * @param test         The name of the test.
     * @param typeUri      The URI of the test type.
     * @param queryProcess The query process.
     * @return The W3C test.
     * @throws TestCreationException If an error occurs while creating the test.
     */
    public static IW3cTest createW3cTest(String test, String typeUri, QueryProcess queryProcess)
            throws TestCreationException {
        String query = buildTestDetailQuery(test);
        Mappings mappings = executeQuery(queryProcess, query)
                .orElseThrow(() -> new TestCreationException("Failed to retrieve test details for: " + test));

        TestType type = typeMap.get(typeUri);
        if (type == null) {
            throw new TestCreationException("Unsupported test type URI: " + typeUri);
        }

        String name = mappings.getValue("?name").getLabel();
        String comment = mappings.getValue("?comment") != null ? mappings.getValue("?comment").getLabel() : "";

        HashAlgorithm hashAlgorithm = null;

        if (mappings.getValue("?hashAlgorithm") != null) {
            switch (mappings.getValue("?hashAlgorithm").getLabel()) {
                case "SHA256":
                    hashAlgorithm = HashAlgorithm.SHA_256;
                    break;
                case "SHA384":
                    hashAlgorithm = HashAlgorithm.SHA_384;
                    break;
                default:
                    throw new TestCreationException(
                            "Unsupported hash algorithm: " + mappings.getValue("?hashAlgorithm").getLabel());
            }
        }

        switch (type) {
            case RDFC10EvalTest:
                return new RDFC10EvalTest(
                        test,
                        name,
                        comment,
                        URI.create(mappings.getValue("?action").getLabel()),
                        URI.create(mappings.getValue("?result").getLabel()),
                        hashAlgorithm);
            case RDFC10MapTest:
                return new RDFC10MapTest(
                        test,
                        name,
                        comment,
                        URI.create(mappings.getValue("?action").getLabel()),
                        URI.create(mappings.getValue("?result").getLabel()),
                        hashAlgorithm);
            case RDFC10NegativeEvalTest:
                return new RDFC10NegativeEvalTest(
                        test,
                        name,
                        comment,
                        URI.create(mappings.getValue("?action").getLabel()));
            default:
                throw new TestCreationException("Unsupported test type: " + type);
        }
    }

    /**
     * Builds a query to retrieve the test details from the manifest file.
     * 
     * @return The query to retrieve the test details.
     */
    private static String buildTestDetailQuery(String test) {
        return "PREFIX mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n"
                + "PREFIX rdfc: <https://w3c.github.io/rdf-canon/tests/vocab#>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT ?name ?comment ?action ?result WHERE {"
                + " <" + test + "> mf:name ?name ;"
                + " mf:action ?action ."
                + " optional { <" + test + "> mf:result ?result } ."
                + " optional { <" + test + "> rdfs:comment ?comment } ."
                + " optional { <" + test + "> rdfc:hashAlgorithm ?hashAlgorithm } ."
                + "}";
    }

    /**
     * Executes the specified query using the specified query process.
     * 
     * @param queryProcess The query process.
     * @param query        The query to execute.
     * @return The mappings resulting from the query execution, or an empty optional
     *         if an error occurs.
     */
    private static Optional<Mappings> executeQuery(QueryProcess queryProcess, String query) {
        try {
            return Optional.ofNullable(queryProcess.query(query));
        } catch (EngineException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Exception thrown when an error occurs while creating a test.
     */
    public static class TestCreationException extends Exception {
        public TestCreationException(String message) {
            super(message);
        }
    }
}
