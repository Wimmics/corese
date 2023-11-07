package fr.inria.corese.command.utils.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadFormat;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to load RDF data into a Corese Graph.
 */
public class RdfDataLoader {

    /**
     * Load RDF data from standard input into a Corese Graph.
     *
     * @param inputFormat Input file serialization format.
     * @param graph       Corese Graph to load RDF data into.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the loaded files.
     */
    public static void LoadFromStdin(
            EnumInputFormat inputFormat,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        RdfDataLoader.loadFromInputStream(System.in, inputFormat, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded file: standard input");
        }
    }

    /**
     * Load RDF data from a path or URL into a Corese Graph.
     *
     * @param url         URL of the file to load.
     * @param inputFormat Input file serialization format.
     * @param graph       Corese Graph to load RDF data into.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the loaded files.
     */
    public static void loadFromURL(
            URL url,
            EnumInputFormat inputFormat,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        // If the input format is not provided, try to determine it from the
        // file
        if (inputFormat == null) {
            Optional<EnumInputFormat> inputFormatOptional = RdfDataLoader.guessInputFormat(url.toString(), spec,
                    verbose);
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
        RdfDataLoader.loadFromInputStream(inputStream, inputFormat, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded file: " + url.toString());
        }
    }

    /**
     * Load RDF data from a path to a file into a Corese Graph.
     *
     * @param path        Path of the file to load.
     * @param inputFormat Input file serialization format.
     * @param graph       Corese Graph to load RDF data into.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the loaded files.
     */
    public static void loadFromFile(
            Path path,
            EnumInputFormat inputFormat,
            Graph graph,
            CommandSpec spec,
            Boolean verbose) {

        // If the input format is not provided, try to determine it from the
        // file
        if (inputFormat == null) {
            Optional<EnumInputFormat> inputFormatOptional = RdfDataLoader.guessInputFormat(path.toString(), spec,
                    verbose);
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
        RdfDataLoader.loadFromInputStream(inputStream, inputFormat, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Loaded file: " + path);
        }
    }

    /**
     * Load RDF data from a directory into a Corese Graph.
     *
     * @param path        Path of the directory to load.
     * @param inputFormat Input file serialization format.
     * @param graph       Corese Graph to load RDF data into.
     * @param recursive   If true, load RDF data from subdirectories.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the loaded files.
     */
    public static void loadFromDirectory(
            Path path,
            EnumInputFormat inputFormat,
            Graph graph,
            boolean recursive,
            CommandSpec spec,
            boolean verbose) {

        File[] files = path.toFile().listFiles();

        if (files != null) {
            for (File childFile : files) {

                if (childFile.isDirectory() && recursive) {
                    RdfDataLoader.loadFromDirectory(childFile.toPath(), inputFormat, graph, recursive, spec, verbose);
                } else if (childFile.isFile()) {
                    RdfDataLoader.loadFromFile(childFile.toPath(), inputFormat, graph, spec, verbose);
                }
            }
        }
    }

    /**
     * Load RDF data from an input stream into a Corese Graph.
     *
     * @param inputStream Input stream of the file to load.
     * @param inputFormat Input file serialization format.
     * @param graph       Corese Graph to load RDF data into.
     */
    private static void loadFromInputStream(InputStream inputStream, EnumInputFormat inputFormat, Graph graph) {

        Load load = Load.create(graph);

        if (inputFormat == null) {
            throw new IllegalArgumentException(
                    "The input format cannot be automatically determined if you use standard input or na URL. "
                            + "Please specify the input format with the option -f.");
        } else {
            try {
                load.parse(inputStream, EnumInputFormat.toLoaderValue(inputFormat));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse RDF file. Check if file is well-formed and that "
                        + "the input format is correct. " + e.getMessage(), e);
            }
        }
    }

    /**
     * Guess the input format from the file extension.
     *
     * @param input   Input file path or URL.
     * @param spec    Command specification.
     * @param verbose If true, print information about the loaded files.
     * @return The guessed input format.
     */
    private static Optional<EnumInputFormat> guessInputFormat(
            String input,
            CommandSpec spec,
            boolean verbose) {

        EnumInputFormat inputFormat = EnumInputFormat.fromLoaderValue(LoadFormat.getFormat(input));

        if (inputFormat == null) {
            if (verbose) {
                spec.commandLine().getErr().println("Failed to detect input format, defaulting to Turtle");
            }
            inputFormat = EnumInputFormat.TURTLE;
        }

        if (verbose) {
            spec.commandLine().getErr().println("Format not specified, detected input format: " + inputFormat);
        }
        return Optional.of(inputFormat);
    }
}
