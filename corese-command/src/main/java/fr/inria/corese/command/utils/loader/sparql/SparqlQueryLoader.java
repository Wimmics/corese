package fr.inria.corese.command.utils.loader.sparql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.TestType;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to load SPARQL queries.
 */
public class SparqlQueryLoader {

    // Command specification
    private CommandSpec spec;
    private boolean verbose;

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructor.
     *
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     */
    public SparqlQueryLoader(CommandSpec spec, boolean verbose) {
        this.spec = spec;
        this.verbose = verbose;
    }

    ////////////////////
    // Public methods //
    ////////////////////

    /**
     * Load a SPARQL query from a path, URL, or standard input.
     * 
     * @param input Path, URL, or SPARQL query to load.
     * @return The loaded query.
     */
    public String load(String input) {
        Optional<Path> path = ConvertString.toPath(input);
        Optional<URL> url = ConvertString.toUrl(input);
        Boolean isSparqlQuery = TestType.isSparqlQuery(input);

        if (isSparqlQuery) {
            return input;
        } else if (url.isPresent()) {
            return this.loadFromUrl(url.get());
        } else if (path.isPresent()) {
            return this.loadFromFile(path.get());
        } else {
            throw new IllegalArgumentException("Invalid input: " + input);
        }
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Load a SPARQL query from a path.
     *
     * @param path Path of the file to load.
     * @return The loaded query.
     */
    private String loadFromFile(Path path) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(path.toString());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Failed to open SPARQL query file: " + path.toString(), e);
        }

        String query = this.loadFromInputStream(inputStream);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded SPAQRL query file: " + path.toString());
        }

        return query;
    }

    /**
     * Load a SPARQL query from a URL.
     *
     * @param url URL of the file to load.
     * @return The loaded query.
     */
    private String loadFromUrl(URL url) {
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open SPARQL query file: " + url.toString(), e);
        }

        String query = this.loadFromInputStream(inputStream);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded SPARQL query file: " + url.toString());
        }

        return query;
    }

    /**
     * Load a SPARQL query from standard input.
     *
     * @param inputStream Input stream of the file to load.
     * @return The loaded query.
     */
    private String loadFromInputStream(InputStream inputStream) {
        try {
            return new String(inputStream.readAllBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read SPARQL query from input stream", e);
        }
    }
}
