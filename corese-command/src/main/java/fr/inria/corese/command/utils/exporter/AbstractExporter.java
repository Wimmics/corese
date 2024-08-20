
package fr.inria.corese.command.utils.exporter;

import java.nio.file.Path;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.function.extension.ResultFormater;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to export SPARQL query results and RDF graphs.
 */
public abstract class AbstractExporter {

    public static final String DEFAULT_OUTPUT = "./output";
    private final Path DEFAULT_OUTPUT_PATH = Path.of(DEFAULT_OUTPUT);

    // Command specification
    private CommandSpec spec;
    private boolean verbose;

    // Output
    protected Path output;
    protected boolean outputIsDefined;
    protected boolean needToAppendExtension;
    private boolean outputToFileIsDefault;

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructor.
     * 
     * @param spec    Command specification.
     * @param verbose If true, print information about the exported file.
     * @param output  Output file path. If not provided, the result will be written
     *                to standard output.
     */
    public AbstractExporter(CommandSpec spec, boolean verbose, Path output) throws IllegalArgumentException {
        // Command specification
        this.spec = spec;
        this.verbose = verbose;

        // Output
        this.outputIsDefined = output != null;
        this.outputToFileIsDefault = outputIsDefined && DEFAULT_OUTPUT_PATH.equals(this.output);
        this.output = outputToFileIsDefault ? DEFAULT_OUTPUT_PATH : output;
        this.needToAppendExtension = outputIsDefined && !hasExtension(this.output);
    }

    ///////////////////////
    // Protected methods //
    ///////////////////////

    /**
     * Export the result to a file.
     * 
     * @param path       Path of the file to export to.
     * @param FormatCode Corese code of the format.
     * @param formatName Name of the format.
     * @param graph      Graph to export.
     */
    protected void exportToFile(Path path, int FormatCode, String formatName, Graph graph) {
        ResultFormat resultFormater = ResultFormat.create(graph);
        exportToFile(path, FormatCode, formatName, resultFormater);
    }

    /**
     * Export the result to standard output.
     * 
     * @param FormatCode Corese code of the format.
     * @param formatName Name of the format.
     * @param graph      Graph to export.
     */
    protected void exportToStdout(int FormatCode, String formatName, Graph graph) {
        ResultFormat resultFormater = ResultFormat.create(graph);
        exportToStdout(FormatCode, formatName, resultFormater);
    }

    /**
     * Export the result to a file.
     * 
     * @param path       Path of the file to export to.
     * @param FormatCode Corese code of the format.
     * @param formatName Name of the format.
     * @param mappings   Mappings to export.
     */
    protected void exportToFile(Path path, int FormatCode, String formatName, Mappings mappings) {
        ResultFormat resultFormater = ResultFormat.create(mappings);
        exportToFile(path, FormatCode, formatName, resultFormater);
    }

    /**
     * Export the result to standard output.
     * 
     * @param FormatCode Corese code of the format.
     * @param formatName Name of the format.
     * @param mappings   Mappings to export.
     */
    protected void exportToStdout(int FormatCode, String formatName, Mappings mappings) {
        ResultFormat resultFormater = ResultFormat.create(mappings);
        exportToStdout(FormatCode, formatName, resultFormater);
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Export the result to a file.
     * 
     * @param path         Path of the file to export to.
     * @param FormatCode   Corese code of the format.
     * @param formatName   Name of the format.
     * @param ResultFormat Result formater.
     */
    private void exportToFile(Path path, int FormatCode, String formatName, ResultFormat resultFormater) {

        resultFormater.setSelectFormat(FormatCode);
        resultFormater.setConstructFormat(FormatCode);

        try {
            resultFormater.write(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open export file: " + path.toString(), e);
        }

        if (this.verbose) {
            this.spec.commandLine().getErr()
                    .println("Exported result in file: " + path.toString() + " with format: " + formatName);

        }
    }

    /**
     * Export the result to standard output.
     * 
     * @param FormatCode   Corese code of the format.
     * @param formatName   Name of the format.
     * @param ResultFormat Result formater.
     */
    private void exportToStdout(int FormatCode, String formatName, ResultFormat resultFormater) {

        // Configure the result formater
        resultFormater.setSelectFormat(FormatCode);
        resultFormater.setConstructFormat(FormatCode);

        // Write the result to standard output
        try {
            String str = resultFormater.toString();
            spec.commandLine().getOut().println(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to standard output", e);
        }

        // Print information about the exported file
        if (verbose) {
            spec.commandLine().getErr().println("Exported result to standard output with format: " + formatName);
        }
    }

    /**
     * Determine if the given path has an extension.
     * 
     * @param path Path to check.
     * @return True if the path has an extension, false otherwise.
     */
    private boolean hasExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1;
    }
}