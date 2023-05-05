package fr.inria.corese.command.programs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "convert", version = App.version, description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert implements Runnable {

    @Spec
    CommandSpec spec;

    @Option(names = { "-i", "--input-filepath" }, description = "Path or URL of the file that needs to be converted.")
    private String inputPath;

    @Option(names = { "-f",
            "--input-format" }, description = "Input serialization format. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-o", "--output-filepath" }, description = "Path where the resulting file should be saved.")
    private Path outputPath;

    @Parameters(paramLabel = "output-format", description = "Serialization format to which the input file should be converted. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumOutputFormat outputFormat;

    private Graph graph;

    public Convert() {
    }

    @Override
    public void run() {
        try {
            checkInputValues();
            loadInputFile();
            exportGraph();
        } catch (IllegalArgumentException | IOException e) {
            spec.commandLine().getErr().println(e.getMessage());
            throw new ExitCodeException(1, e.getMessage());
        }
    }

    /**
     * Check if the input values are correct.
     * 
     * @throws IllegalArgumentException if input path is same as output path.
     */
    private void checkInputValues() throws IllegalArgumentException {
        if (inputPath != null && outputPath != null && inputPath.equals(outputPath.toString())) {
            throw new IllegalArgumentException("Input path cannot be the same as output path.");
        }
    }

    /**
     * Load the input file.
     * 
     * @throws IllegalArgumentException if the input file path is null.
     * @throws IOException              if an I/O error occurs while loading the
     *                                  input file.
     */
    private void loadInputFile() throws IllegalArgumentException, IOException {
        if (inputPath == null) {
            // if inputPath is null, load from stdin
            InputStream inputStream = System.in;
            this.graph = GraphUtils.load(inputStream, inputFormat);
        } else {
            this.graph = GraphUtils.load(inputPath, inputFormat);
        }
    }

    /**
     * Export the graph.
     * 
     * @throws IOException if an I/O error occurs while exporting the graph.
     */
    private void exportGraph() throws IOException {
        if (outputPath == null) {
            // if outputPath is null, print to stdout
            GraphUtils.exportToString(graph, outputFormat, spec);
        } else {
            GraphUtils.export(graph, outputPath, outputFormat);
        }
    }

}
