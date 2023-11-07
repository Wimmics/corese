package fr.inria.corese.command.programs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.TestType;
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

@Command(name = "shacl", version = App.version, description = "Run SHACL validation on a RDF dataset.", mixinStandardHelpOptions = true)
public class Shacl implements Callable<Integer> {

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";

    @Spec
    private CommandSpec spec;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "RDF serialization format of the input file. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-i",
            "--input-data" }, description = "Path or URL of the file that needs to be converted.", arity = "1...")
    private String[] rdfData;

    @Option(names = { "-a", "-sf",
            "--shapes-format" }, description = "Serialization format of the SHACL shapes. Possible values: ${COMPLETION-CANDIDATES}.", defaultValue = "TURTLE")
    private EnumInputFormat reportFormat = null;

    @Option(names = { "-s",
            "--shapes" }, description = "Path or URL of the file containing the SHACL shapes.", arity = "1...", required = true)
    private String[] shaclShapes;

    @Option(names = { "-r", "-of",
            "--output-format" }, description = "Serialization format of the validation report. Possible values: ${COMPLETION-CANDIDATES}.", defaultValue = "TURTLE")
    private EnumOutputFormat outputFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-R",
            "--recursive" }, description = "If an input is a directory, load all the files in the directory recursively.")
    private boolean recursive = false;

    @Option(names = { "-v", "--verbose" }, description = "Prints more information about the execution of the command.")
    private boolean verbose = false;

    @Option(names = { "-c", "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    private Graph dataGraph = Graph.create();
    private Graph shapesGraph = Graph.create();
    private Graph reportGraph = Graph.create();

    private boolean outputFormatIsDefined = false;
    private boolean isDefaultOutputName = false;

    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;

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

            // Load input file(s)
            this.loadInputFile(false);

            // Load shapes file(s)
            this.loadInputFile(true);

            // Check if shapes graph contains SHACL shapes
            if (!TestType.isShacl(this.shapesGraph)) {
                throw new IllegalArgumentException("No SHACL shapes found in the input file(s).");
            }

            // Evaluation of SHACL shapes
            this.execute();

            // Export the report graph
            this.exportGraph();

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            this.spec.commandLine().getErr().println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Load the input file(s) into a graph.
     *
     * @throws IOException If the file cannot be read.
     */
    private void loadInputFile(boolean isShapes) throws IOException {

        if (rdfData == null && !isShapes) {
            // if input is not provided, load from standard input
            RdfDataLoader.LoadFromStdin(
                    this.inputFormat,
                    isShapes ? this.shapesGraph : this.dataGraph,
                    this.spec,
                    this.verbose);
        } else {
            for (String input : isShapes ? shaclShapes : rdfData) {
                Optional<URL> url = ConvertString.toUrl(input);
                Optional<Path> path = ConvertString.toPath(input);

                if (url.isPresent()) {
                    RdfDataLoader.loadFromURL(
                            url.get(),
                            isShapes ? this.reportFormat : this.inputFormat,
                            isShapes ? this.shapesGraph : this.dataGraph,
                            this.spec,
                            this.verbose);
                } else if (path.isPresent()) {
                    // if input is a path
                    if (path.get().toFile().isDirectory()) {
                        // if input is a directory
                        RdfDataLoader.loadFromDirectory(
                                path.get(),
                                this.inputFormat,
                                isShapes ? this.shapesGraph : this.dataGraph,
                                this.recursive,
                                this.spec,
                                this.verbose);
                    } else {
                        // if input is a file
                        RdfDataLoader.loadFromFile(
                                path.get(),
                                isShapes ? this.reportFormat : this.inputFormat,
                                isShapes ? this.shapesGraph : this.dataGraph,
                                this.spec,
                                this.verbose);
                    }

                } else {
                    throw new IllegalArgumentException(
                            "Input path is not a valid URL, file path or directory: " + input);
                }
            }
        }
    }

    /**
     * Execute the SHACL validation.
     * 
     * @throws Exception If an error occurs while evaluating the SHACL shapes.
     */
    private void execute() throws Exception {
        fr.inria.corese.core.shacl.Shacl shacl = new fr.inria.corese.core.shacl.Shacl(dataGraph, shapesGraph);
        try {

            if (this.verbose) {
                this.spec.commandLine().getErr().println("Evaluating SHACL shapes...");
            }

            this.reportGraph = shacl.eval();
        } catch (Exception e) {
            throw new Exception("Error while evaluating SHACL shapes: " + e.getMessage(), e);
        }
    }

    /**
     * Export the report graph.
     * 
     * @throws IOException if an I/O error occurs while exporting the report graph.
     */
    private void exportGraph() throws IOException {

        if (this.verbose) {
            this.spec.commandLine().getOut().println("Exporting report to " + this.outputFormat + " format...");
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
            RdfDataExporter.exportToStdout(this.outputFormat, this.reportGraph, this.spec, this.verbose);
        } else {
            RdfDataExporter.exportToFile(
                    outputFileName,
                    this.outputFormat,
                    this.reportGraph,
                    this.spec,
                    this.verbose);
        }
    }
}
