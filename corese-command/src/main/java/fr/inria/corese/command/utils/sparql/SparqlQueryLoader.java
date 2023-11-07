package fr.inria.corese.command.utils.sparql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to load SPARQL queries.
 */
public class SparqlQueryLoader {

    /**
     * Load a SPARQL query from a path.
     *
     * @param path    Path of the file to load.
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     * @return The loaded query.
     */
    public static String loadFromFile(Path path, CommandSpec spec, boolean verbose) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(path.toString());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Failed to open SPARQL query file: " + path.toString(), e);
        }

        String query = SparqlQueryLoader.loadFromInputStreamPrivate(inputStream);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded SPAQRL query file: " + path.toString());
        }

        return query;
    }

    /**
     * Load a SPARQL query from a URL.
     *
     * @param url     URL of the file to load.
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     * @return The loaded query.
     */
    public static String loadFromUrl(URL url, CommandSpec spec, boolean verbose) {
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open SPARQL query file: " + url.toString(), e);
        }

        String query = SparqlQueryLoader.loadFromInputStreamPrivate(inputStream);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded SPARQL query file: " + url.toString());
        }

        return query;
    }

    /**
     * Load a SPARQL query from stream input.
     * 
     * @param inputStream Input stream to load.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the loaded files.
     * @return The loaded query.
     */
    public static String loadFromInputStream(InputStream inputStream, CommandSpec spec, boolean verbose) {
        String query = SparqlQueryLoader.loadFromInputStreamPrivate(inputStream);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded SPARQL query from input stream");
        }

        return query;
    }

    /**
     * Load a SPARQL query from standard input.
     *
     * @param inputStream Input stream of the file to load.
     * @return The loaded query.
     */
    private static String loadFromInputStreamPrivate(InputStream inputStream) {
        try {
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read SPARQL query from input stream", e);
        }
    }
}
