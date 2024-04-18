package fr.inria.corese.command.programs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.format.EnumCanonicAlgo;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.rdf.RdfDataCanonicalizer;
import fr.inria.corese.command.utils.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "canonicalize", version = App.version, description = "Canonicalize an RDF file to a specific format.", mixinStandardHelpOptions = true)
public class canonicalize implements Callable<Integer> {

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;

    @Spec
    CommandSpec spec;

    @Option(names = { "-i",
            "--input-data" }, description = "Path or URL of the file that needs to be canonicalized.", arity = "1...")
    private String[] rdfData;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "RDF serialization format of the input file. Possible values:\u001b[34m ${COMPLETION-CANDIDATES}\u001b[0m.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-r", "-a", "-ca", "-of",
            "--canonical-algo" }, required = true, description = "Canonicalization algorithm to use. Possible values:\u001b[34m ${COMPLETION-CANDIDATES}\u001b[0m.")
    private EnumCanonicAlgo canonicalAlgo;

    @Option(names = { "-v",
            "--verbose" }, description = "Prints more information about the execution of the command.")
    private boolean verbose = false;

    @Option(names = { "-c", "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    @Option(names = { "-R",
            "--recursive" }, description = "If an input is a directory, load all the files in the directory recursively.")
    private boolean recursive = false;

    private Graph graph = Graph.create();

    private boolean canonicalAlgoIsDefined = false;
    private boolean isDefaultOutputName = false;

    public canonicalize() {
    }

    @Override
    public Integer call() {

        try {

            // Load configuration file
            Optional<Path> configFilePath = Optional.ofNullable(this.configFilePath);
            if (configFilePath.isPresent()) {
                ConfigManager.loadFromFile(configFilePath.get(), this.spec, this.verbose);
            } else {
                ConfigManager.loadDefaultConfig(this.spec, this.verbose);
            }

            // Set owl import
            Property.set(Value.DISABLE_OWL_AUTO_IMPORT, this.noOwlImport);

            // Check if canonical algorithm is defined
            this.canonicalAlgoIsDefined = this.output != null;

            // Check if output file name is default
            this.isDefaultOutputName = this.output != null
                    && DEFAULT_OUTPUT_FILE_NAME.equals(this.output.toString());

            // Execute command
            this.checkInputValues();
            this.loadInputFile();
            this.exportGraph();

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (IllegalArgumentException | IOException e) {
            this.spec.commandLine().getErr().println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Check if the input values are correct.
     *
     * @throws IllegalArgumentException if input path is same as output path.
     */
    private void checkInputValues() throws IllegalArgumentException {
        if (this.rdfData != null && this.output != null) {
            for (String input : this.rdfData) {
                if (Path.of(input).compareTo(this.output) == 0) {
                    throw new IllegalArgumentException("Input path is same as output path: " + input);
                }
            }
        }
    }

    /**
     * Load the input file.
     *
     * @throws IllegalArgumentException if the input format is not supported.
     * @throws IOException              if an I/O error occurs while loading the
     *                                  input file.
     */
    private void loadInputFile() throws IllegalArgumentException, IOException {

        if (rdfData == null) {
            // if input is not provided, load from standard input
            RdfDataLoader.LoadFromStdin(this.inputFormat, this.graph, this.spec, this.verbose);
        } else {
            for (String input : this.rdfData) {
                Optional<URL> url = ConvertString.toUrl(input);
                Optional<Path> path = ConvertString.toPath(input);
                if (url.isPresent()) {
                    // if input is a URL, load from the given URL
                    RdfDataLoader.loadFromURL(url.get(), this.inputFormat, this.graph, this.spec, this.verbose);
                } else if (path.isPresent()) {
                    if (path.get().toFile().isDirectory()) {
                        // if input is a directory, load all the files in the directory
                        RdfDataLoader.loadFromDirectory(path.get(), this.inputFormat, this.graph, this.recursive,
                                this.spec, this.verbose);
                    } else {
                        // if input is provided, load from the given file
                        RdfDataLoader.loadFromFile(path.get(), this.inputFormat, this.graph, this.spec, this.verbose);
                    }
                }
            }
        }
    }

    /**
     * Canonicalize the graph.
     *
     * @throws IOException if an I/O error occurs while exporting the graph.
     */
    private void exportGraph() throws IOException {

        if (this.verbose) {
            this.spec.commandLine().getOut()
                    .println("Canonicalizing file with " + this.canonicalAlgo + " algorithm...");
        }

        Path outputFileName;

        // Set output file name
        if (this.canonicalAlgoIsDefined && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(this.DEFAULT_OUTPUT_FILE_NAME + "." + this.canonicalAlgo.getExtention());
        }

        // Export the graph
        if (this.output == null) {
            RdfDataCanonicalizer.canonicalizeToStdout(this.canonicalAlgo, this.graph, this.spec, this.verbose);
        } else {
            RdfDataCanonicalizer.canonicalizeToFile(
                    outputFileName,
                    this.canonicalAlgo,
                    this.graph,
                    this.spec,
                    this.verbose);
        }
    }

}
