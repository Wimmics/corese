package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "convert", version = App.version, description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Convert.class.getName());
    private static final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private static final int EXIT_CODE_ERROR = 1;

    @Spec
    CommandSpec spec;

    @Option(names = { "-i", "--input-data" }, description = "Path or URL of the file that needs to be converted.")
    private String input;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Input serialization format. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-r", "-of",
            "--output-format" }, required = true, description = "Serialization format to which the input file should be converted. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumOutputFormat outputFormat;

    private Graph graph;

    private boolean outputFormatIsDefined = false;
    private boolean isDefaultOutputName = false;

    public Convert() {
    }

    @Override
    public void run() {
        try {
            this.outputFormatIsDefined = this.output != null;
            this.isDefaultOutputName = this.output != null && DEFAULT_OUTPUT_FILE_NAME.equals(this.output.toString());
            checkInputValues();
            loadInputFile();
            exportGraph();
        } catch (IllegalArgumentException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during conversion", e);
            spec.commandLine().getErr().println(e.getMessage());
            throw new ExitCodeException(EXIT_CODE_ERROR, e.getMessage());
        }
    }

    /**
     * Check if the input values are correct.
     * 
     * @throws IllegalArgumentException if input path is same as output path.
     */
    private void checkInputValues() throws IllegalArgumentException {
        if (input != null && output != null && input.equals(output.toString())) {
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
        if (input == null) {
            // if input is null, load from stdin
            this.graph = GraphUtils.load(System.in, inputFormat);
        } else {
            this.graph = GraphUtils.load(input, inputFormat);
        }
    }

    /**
     * Export the graph.
     * 
     * @throws IOException if an I/O error occurs while exporting the graph.
     */
    private void exportGraph() throws IOException {

        Path outputFileName;

        // Set output file name
        if (this.outputFormatIsDefined && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(DEFAULT_OUTPUT_FILE_NAME + "." + this.outputFormat.getExtention());
        }

        // Export the graph
        if (output == null) {
            // if output is null, print to stdout
            GraphUtils.exportToStdout(graph, outputFormat, spec);
        } else {
            GraphUtils.exportToFile(graph, outputFormat, outputFileName);
        }
    }

}
