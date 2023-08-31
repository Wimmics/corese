package fr.inria.corese.command.utils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.transform.Transformer;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to export RDF data from a Corese Graph.
 */
public class RdfDataExporter {

    /**
     * Export RDF data from a Corese Graph to a file.
     *
     * @param path         Path of the file to export to.
     * @param outputFormat Output file serialization format.
     * @param graph        Corese Graph to export RDF data from.
     * @param spec         Command specification.
     * @param verbose      If true, print information about the exported file.
     */
    public static void exportToFile(
            Path path,
            EnumOutputFormat outputFormat,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open export file: " + path.toString(), e);
        }

        exportToOutputStream(outputStream, outputFormat, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Exported RDF data with format: " + outputFormat);
            spec.commandLine().getErr().println("Exported RDF data to file: " + path.toString());
        }
    }

    /**
     * Export RDF data from a Corese Graph to standard output.
     *
     * @param outputFormat Output file serialization format.
     * @param graph        Corese Graph to export RDF data from.
     * @param spec         Command specification.
     * @param verbose      If true, print information about the exported file.
     */
    public static void exportToStdout(
            EnumOutputFormat outputFormat,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        exportToOutputStream(System.out, outputFormat, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Exported RDF data with format: " + outputFormat);
            spec.commandLine().getErr().println("Exported RDF data to: standard output");
        }
    }

    /**
     * Export RDF data from a Corese Graph to a output stream.
     *
     * @param outputStream Output stream to export to.
     * @param outputFormat Output file serialization format.
     * @param graph        Corese Graph to export RDF data from.
     */
    private static void exportToOutputStream(
            OutputStream outputStream,
            EnumOutputFormat outputFormat,
            Graph graph) {

        try {
            Transformer transformer = Transformer.create(graph, EnumOutputFormat.convertToTransformer(outputFormat));
            transformer.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to RDF data to output stream", e);
        }
    }

}
