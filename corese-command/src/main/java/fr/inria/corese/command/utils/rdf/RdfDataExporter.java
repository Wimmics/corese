package fr.inria.corese.command.utils.rdf;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.JSONLDFormat;
import fr.inria.corese.core.print.NQuadsFormat;
import fr.inria.corese.core.print.NTriplesFormat;
import fr.inria.corese.core.print.RDFFormat;
import fr.inria.corese.core.print.TripleFormat;
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
            switch (outputFormat) {
                case RDFXML:
                case RDF:
                case APPLICATION_RDF_XML:
                    RDFFormat.create(graph).write(outputStream);
                    break;
                case TURTLE:
                case TTL:
                case TEXT_TURTLE:
                    TripleFormat.create(graph).write(outputStream);
                    break;
                case TRIG:
                case APPLICATION_TRIG:
                    TripleFormat.create(graph, true).write(outputStream);
                    break;
                case JSONLD:
                case APPLICATION_LD_JSON:
                    JSONLDFormat.create(graph).write(outputStream);
                    break;
                case NTRIPLES:
                case NT:
                case APPLICATION_NTRIPLES:
                    NTriplesFormat.create(graph).write(outputStream);
                    break;
                case NQUADS:
                case NQ:
                case APPLICATION_NQUADS:
                    NQuadsFormat.create(graph).write(outputStream);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported output format: " + outputFormat);
            }

            outputStream.flush();

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to RDF data to output stream", e);
        }
    }

}
