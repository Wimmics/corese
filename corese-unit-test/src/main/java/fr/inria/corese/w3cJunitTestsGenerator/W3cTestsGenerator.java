package fr.inria.corese.w3cJunitTestsGenerator;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.IW3cTest;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.factory.W3cTestFactory;
import fr.inria.corese.w3cJunitTestsGenerator.w3cTests.factory.W3cTestFactory.TestCreationException;

/**
 * Generates JUnit test cases from W3C test manifest files.
 */
public class W3cTestsGenerator {

    private static final Logger logger = LogManager.getLogger(W3cTestsGenerator.class);

    private final URI manifestUri;
    private final Path testsPath;
    private final String testName;

    /**
     * Constructs a new W3cTestsGenerator with the specified test name, manifest
     * file path and test directory path.
     * 
     * @param testName    The name of the test.
     * @param manifestUri The URI of the manifest file.
     * @param testsPath   The path to tests directory.
     */
    public W3cTestsGenerator(String testName, URI manifestUri, Path testsPath) {
        this.testName = testName;
        this.manifestUri = manifestUri;
        this.testsPath = testsPath;
    }

    /**
     * Generates JUnit test cases from the W3C test manifest file.
     */
    public void generate() {
        // Load manifest file
        Graph graph = loadManifest();

        // Generate list of test cases
        List<IW3cTest> testCases = getListOfTestCases(graph);

        // Generate JUnit test file
        JUnitTestFileGenerator generator = new JUnitTestFileGenerator(testName, manifestUri, testsPath, testCases);
        generator.generate();
    }

    ////////////////////////
    // Load manifest file //
    ////////////////////////

    /**
     * Loads the W3C test manifest file into a graph.
     * 
     * @return The graph containing the manifest file.
     */
    private Graph loadManifest() {
        logger.info("Loading manifest file: " + manifestUri);
        Graph graph = Graph.create();
        graph.init();
        Load loader = Load.create(graph);

        try {
            loader.parse(manifestUri.toString());
        } catch (Exception e) {
            logger.error("Error loading manifest file: " + manifestUri, e);
            System.exit(1);
        }

        return graph;
    }

    ////////////////////////////
    // Get list of test cases //
    ////////////////////////////

    /**
     * Gets the list of test cases from the specified graph.
     * 
     * @param graph The graph containing the test cases.
     * @return The list of test cases.
     */
    private List<IW3cTest> getListOfTestCases(Graph graph) {
        QueryProcess exec = QueryProcess.create(graph);
        String query = buildTestCasesQuery();
        Mappings mappings;

        try {
            mappings = exec.query(query);
        } catch (Exception e) {
            logger.error("Error executing query.", e);
            return new ArrayList<>();
        }

        if (mappings == null) {
            logger.warn("Query returned null mappings.");
            return new ArrayList<>();
        }

        List<IW3cTest> testCases = new ArrayList<>();
        for (Mapping mapping : mappings) {
            String test = mapping.getValue("?test").getLabel();
            String type = mapping.getValue("?type").getLabel();
            try {
                testCases.add(W3cTestFactory.createW3cTest(test, type, exec));
            } catch (TestCreationException e) {
                logger.error("Error creating test: " + test, e);
                System.exit(1);
            }
        }

        logger.info("Loaded " + testCases.size() + " test cases.");
        return testCases;
    }

    /**
     * Builds a query to retrieve the test cases from the manifest file.
     * 
     * @return The query to retrieve the test cases.
     */
    private String buildTestCasesQuery() {
        return "PREFIX mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "SELECT ?type ?test WHERE {\n" +
                "  ?manifest a mf:Manifest .\n" +
                "  ?manifest mf:entries/rdf:rest*/rdf:first ?test .\n" +
                "  ?test rdf:type ?type .\n" +
                "} ORDER BY ?test";
    }
}
