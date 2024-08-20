package fr.inria.corese.w3cJunitTestsGenerator;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Main class serves as the entry point for the application.
 * It is responsible for initializing and executing the W3cTestsGenerator based
 * on predefined paths.
 */
public class Main {

        // Define base directory using system's current directory
        private static final Path BASE_PATH = Paths.get(System.getProperty("user.dir"));

        // Specify paths for tests, resources, and the manifest within the project
        // structure
        private static final Path TESTS_PATH_DIR = BASE_PATH
                        .resolve("corese-unit-test/src/test/java/fr/inria/corese/w3c");

        /**
         * Main method to execute the application.
         * It creates and runs a W3cTestsGenerator with specified directories and
         * manifest file.
         *
         * @param args Command line arguments (not used)
         */
        public static void main(String[] args) {
                generateW3cTests("canonicalRdf", "https://w3c.github.io/rdf-canon/tests/manifest.ttl");
        }

        /**
         * Initializes and runs the W3cTestsGenerator for generating W3C tests.
         *
         * @param testName     The name of the test suite to generate tests for.
         * @param manifestPath The path to the manifest file.
         */
        private static void generateW3cTests(String testName, String manifestUri) {
                W3cTestsGenerator generator = new W3cTestsGenerator(testName, URI.create(manifestUri), TESTS_PATH_DIR);
                generator.generate();
        }
}
