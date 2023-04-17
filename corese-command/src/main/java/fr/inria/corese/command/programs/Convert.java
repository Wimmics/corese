package fr.inria.corese.command.programs;

import java.nio.file.Path;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.InputFormat;
import fr.inria.corese.command.utils.format.OutputFormat;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "convert", version = App.version, description = "Convert an RDF file from one serialization format to another.", mixinStandardHelpOptions = true)
public class Convert implements Runnable {

    @Option(names = { "-i", "--input-filepath" }, description = "Path or URL of the file that needs to be converted.")
    private String inputPath;

    @Option(names = { "-f",
            "--input-format" }, description = "Input serialization format. Possible values: ${COMPLETION-CANDIDATES}. Default: TURTLE.")
    private InputFormat inputFormat = InputFormat.TURTLE;

    @Option(names = { "-o", "--output-filepath" }, description = "Path where the resulting file should be saved.")
    private Path outputPath;

    @Parameters(paramLabel = "output-format", description = "Serialization format to which the input file should be converted. Possible values: ${COMPLETION-CANDIDATES}.")
    private OutputFormat outputFormat;

    private Graph graph;

    public Convert() {
    }

    @Override
    public void run() {
        this.check();

        // Load the input file.
        if (this.inputPath == null) {
            // if inputPath is null, load from stdin
            this.graph = GraphUtils.load(System.in, this.inputFormat);
        } else {
            this.graph = GraphUtils.load(this.inputPath, this.inputFormat);
        }

        // Export the graph.
        if (this.outputPath == null) {
            // if outputPath is null, print to stdout
            GraphUtils.print(this.graph, this.outputFormat);
        } else {
            GraphUtils.export(this.graph, this.outputPath, this.outputFormat);
        }
    }

    /**
     * Checks if the input values are correct.
     * 
     * @throws IllegalArgumentException
     */
    private void check() throws IllegalArgumentException {

        // Check that the input path is not equal to the output path.
        if (this.inputPath != null && this.outputPath != null && this.inputPath.equals(this.outputPath.toString())) {
            throw new IllegalArgumentException("The input path cannot be the same as the output path.");
        }

    }

}
