package fr.inria.corese.command.utils.loader.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadFormat;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to load RDF data into a Corese Graph.
 */
public class RdfDataLoader {

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
    public RdfDataLoader(CommandSpec spec, boolean verbose) {
        this.spec = spec;
        this.verbose = verbose;
    }

    ////////////////////
    // Public methods //
    ////////////////////

    /**
     * Load RDF data into a Corese Graph.
     * 
     * @param inputs      Paths or URLs of the files to load.
     * @param inputFormat Input file serialization format.
     * @param recursive   If true, load RDF data from subdirectories.
     * @return The Corese Graph containing the RDF data.
     */
    public Graph load(String[] inputs, EnumRdfInputFormat inputFormat, boolean recursive)
            throws IllegalArgumentException {

        if (inputs == null || inputs.length == 0) {
            return this.LoadFromStdin(inputFormat);
        } else {
            Graph graph = Graph.create();
            for (String input : inputs) {
                Optional<URL> url = ConvertString.toUrl(input);
                Optional<Path> path = ConvertString.toPath(input);

                if (url.isPresent()) {
                    // Load RDF data from URL
                    Graph resultGraph = this.loadFromURL(url.get(), inputFormat);
                    graph.merge(resultGraph);
                } else if (path.isPresent()) {
                    // Load RDF data from file or directory
                    File file = path.get().toFile();
                    if (file.isDirectory()) {
                        // Load RDF data from directory
                        Graph resultGraph = this.loadFromDirectory(path.get(), inputFormat, recursive);
                        graph.merge(resultGraph);
                    } else {
                        // Load RDF data from file
                        Graph resultGraph = this.loadFromFile(path.get(), inputFormat);
                        graph.merge(resultGraph);
                    }
                } else {
                    throw new IllegalArgumentException("Invalid input: " + input);
                }
            }
            return graph;
        }
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Load RDF data from standard input into a Corese Graph.
     *
     * @param inputFormat Input file serialization format.
     * @return The Corese Graph containing the RDF data.
     */
    private Graph LoadFromStdin(EnumRdfInputFormat inputFormat) {

        Graph graph = this.loadFromInputStream(System.in, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: standard input");
        }

        return graph;
    }

    /**
     * Load RDF data from a path or URL into a Corese Graph.
     *
     * @param url         URL of the file to load.
     * @param inputFormat Input file serialization format.
     * @return The Corese Graph containing the RDF data.
     */
    private Graph loadFromURL(URL url, EnumRdfInputFormat inputFormat) {

        // If the input format is not provided, try to determine it from the file
        if (inputFormat == null) {
            Optional<EnumRdfInputFormat> inputFormatOptional = this.guessInputFormat(url.toString());
            if (inputFormatOptional.isPresent()) {
                inputFormat = inputFormatOptional.get();
            }
        }

        // Load RDF data from URL
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to open URL: " + url.toString(), e);
        }
        Graph graph = this.loadFromInputStream(inputStream, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: " + url.toString());
        }
        return graph;
    }

    /**
     * Load RDF data from a path to a file into a Corese Graph.
     *
     * @param path        Path of the file to load.
     * @param inputFormat Input file serialization format.
     * @return The Corese Graph containing the RDF data.
     */
    private Graph loadFromFile(Path path, EnumRdfInputFormat inputFormat) {

        // If the input format is not provided, try to determine it from the file
        if (inputFormat == null) {
            Optional<EnumRdfInputFormat> inputFormatOptional = this.guessInputFormat(path.toString());
            if (inputFormatOptional.isPresent()) {
                inputFormat = inputFormatOptional.get();
            }
        }

        // Load RDF data from file
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Failed to open RDF data file: " + path.toString(), e);
        }
        Graph graph = this.loadFromInputStream(inputStream, inputFormat);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded file: " + path);
        }
        return graph;
    }

    /**
     * Load RDF data from a directory into a Corese Graph.
     *
     * @param path        Path of the directory to load.
     * @param inputFormat Input file serialization format.
     * @param recursive   If true, load RDF data from subdirectories.
     * @return The Corese Graph containing the RDF data.
     */
    private void loadFromDirectoryRecursive(Path path, EnumRdfInputFormat inputFormat, boolean recursive, Graph graph) {

        File[] files = path.toFile().listFiles();

        if (files != null) {
            for (File childFile : files) {

                if (childFile.isDirectory() && recursive) {
                    this.loadFromDirectoryRecursive(childFile.toPath(), inputFormat, recursive, graph);
                } else if (childFile.isFile()) {
                    Graph resultGraph = this.loadFromFile(childFile.toPath(), inputFormat);
                    graph.merge(resultGraph);
                }
            }
        }
    }

    /**
     * Load RDF data from a directory into a Corese Graph.
     * 
     * @param path        Path of the directory to load.
     * @param inputFormat Input file serialization format.
     * @param recursive   If true, load RDF data from subdirectories.
     * @return The Corese Graph containing the RDF data.
     */
    private Graph loadFromDirectory(Path path, EnumRdfInputFormat inputFormat, boolean recursive) {
        Graph graph = Graph.create();
        this.loadFromDirectoryRecursive(path, inputFormat, recursive, graph);

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Loaded directory: " + path);
        }
        return graph;
    }

    /**
     * Load RDF data from an input stream into a Corese Graph.
     *
     * @param inputStream Input stream of the file to load.
     * @param inputFormat Input file serialization format.
     * @return The Corese Graph containing the RDF data.
     */
    private Graph loadFromInputStream(InputStream inputStream, EnumRdfInputFormat inputFormat) {

        Graph graph = Graph.create();
        Load load = Load.create(graph);

        if (inputFormat == null) {
            throw new IllegalArgumentException(
                    "The input format cannot be automatically determined if you use standard input or na URL. "
                            + "Please specify the input format with the option -f.");
        } else {
            try {
                load.parse(inputStream, inputFormat.getCoreseCode());
                return graph;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse RDF file. Check if file is well-formed and that "
                        + "the input format is correct. " + e.getMessage(), e);
            }
        }
    }

    /**
     * Guess the input format from the file extension.
     *
     * @param input Input file path or URL.
     * @return The guessed input format.
     */
    private Optional<EnumRdfInputFormat> guessInputFormat(String input) {

        EnumRdfInputFormat inputFormat = EnumRdfInputFormat.create(LoadFormat.getFormat(input));

        if (inputFormat == null) {
            if (this.verbose) {
                this.spec.commandLine().getErr().println("Failed to detect input format, defaulting to Turtle");
            }
            inputFormat = EnumRdfInputFormat.TURTLE;
        }

        if (this.verbose) {
            this.spec.commandLine().getErr().println("Format not specified, detected input format: " + inputFormat);
        }
        return Optional.of(inputFormat);
    }
}
