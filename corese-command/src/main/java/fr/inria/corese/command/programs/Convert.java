package fr.inria.corese.command.programs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.command.utils.rdf.RdfDataExporter;
import fr.inria.corese.command.utils.rdf.RdfDataLoader;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "convert", version = App.version, description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert implements Callable<Integer> {

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;

    @Spec
    CommandSpec spec;

    @Option(names = { "-i", "--input-data" }, description = "Path or URL of the file that needs to be converted.")
    private String input;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "RDF serialization format of the input file. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-r", "-of",
            "--output-format" }, required = true, description = "Serialization format to which the input file should be converted. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumOutputFormat outputFormat;

    @Option(names = { "-v",
            "--verbose" }, description = "Prints more information about the execution of the command.")
    private boolean verbose = false;

    @Option(names = { "-c", "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    private Graph graph = Graph.create();

    private boolean outputFormatIsDefined = false;
    private boolean isDefaultOutputName = false;

    public Convert() {
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

            // Check if output format is defined
            this.outputFormatIsDefined = this.output != null;

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
        if (this.input != null
                && this.output != null
                && this.input.equals(this.output.toString())) {
            throw new IllegalArgumentException("Input path cannot be the same as output path.");
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
        Optional<URL> url = ConvertString.toUrl(this.input);
        Optional<Path> path = ConvertString.toPath(this.input);

        if (input == null) {
            // if input is not provided, load from standard input
            RdfDataLoader.LoadFromStdin(this.inputFormat, this.graph, this.spec, this.verbose);
        } else if (url.isPresent()) {
            // if input is a URL, load from the given URL
            RdfDataLoader.loadFromURL(url.get(), this.inputFormat, this.graph, this.spec, this.verbose);
        } else if (path.isPresent()) {
            // if input is provided, load from the given file
            RdfDataLoader.loadFromFile(path.get(), this.inputFormat, this.graph, this.spec, this.verbose);
        } else {
            throw new IllegalArgumentException("Input path is not a valid URL or file path: " + this.input);
        }
    }

    /**
     * Export the graph.
     *
     * @throws IOException if an I/O error occurs while exporting the graph.
     */
    private void exportGraph() throws IOException {

        if (this.verbose) {
            this.spec.commandLine().getOut().println("Converting file to " + this.outputFormat + " format...");
        }

        Path outputFileName;

        // Set output file name
        if (this.outputFormatIsDefined && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(this.DEFAULT_OUTPUT_FILE_NAME + "." + this.outputFormat.getExtention());
        }

        // Export the graph
        if (this.output == null) {
            RdfDataExporter.exportToStdout(this.outputFormat, this.graph, this.spec, this.verbose);
        } else {
            RdfDataExporter.exportToFile(
                    outputFileName,
                    this.outputFormat,
                    this.graph,
                    this.spec,
                    this.verbose);
        }
    }

}
