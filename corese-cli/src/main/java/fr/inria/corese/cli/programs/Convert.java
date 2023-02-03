package fr.inria.corese.cli.programs;

import java.nio.file.Path;

import fr.inria.corese.cli.utils.GraphUtils;
import fr.inria.corese.cli.utils.format.InputFormat;
import fr.inria.corese.cli.utils.format.OutputFormat;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "convert", version = "4.4.0", description = "Convert an RDF file between different serialization formats.", mixinStandardHelpOptions = true)
public class Convert implements Runnable {

    @Parameters(paramLabel = "INPUT_FORMAT", description = "Input file format."
            + " Candidates: ${COMPLETION-CANDIDATES}")
    private InputFormat inputFormat;

    @Parameters(paramLabel = "INPUT", description = "File to convert.")
    private Path intputPath;

    @Parameters(paramLabel = "OUTPUT_FORMAT", description = "Desired serialization format."
            + "%nCandidates: ${COMPLETION-CANDIDATES}")
    private OutputFormat outputFormat;

    @Parameters(paramLabel = "OUTPUT", description = "File resulting from the conversion.")
    private Path outputPath;

    private Graph graph;

    public Convert() {
    }

    @Override
    public void run() {
        this.check();
        this.graph = GraphUtils.load(this.intputPath, this.inputFormat);
        GraphUtils.export(this.graph, this.outputPath, this.outputFormat);
    }

    /**
     * Checks if the input values are correct.
     * 
     * @throws IllegalArgumentException
     */
    private void check() throws IllegalArgumentException {

        // Check that the input path is not equal to the output path.
        if (this.intputPath.equals(this.outputPath)) {
            throw new IllegalArgumentException("The input path cannot be the same as the output path.");
        }

    }

}
