
package fr.inria.corese.command.utils.exporter.rdf;

import java.nio.file.Path;

import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.core.Graph;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to export RDF graphs.
 */
public class RdfDataExporter extends AbstractExporter {

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
    public RdfDataExporter(CommandSpec spec, boolean verbose, Path output) {
        super(spec, verbose, output);
    }

    ////////////////////
    // Public methods //
    ////////////////////

    /**
     * Export an RDF graph to a file or standard output.
     * 
     * @param graph  RDF graph to export.
     * @param format Serialization format.
     */
    public void export(Graph graph, EnumRdfOutputFormat format) {

        if (this.outputIsDefined) {
            Path path = this.needToAppendExtension ? Path.of(this.output + format.getExtention()) : this.output;
            exportToFile(path, format.getCoreseCodeFormat(), format.toString(), graph);
        } else {
            exportToStdout(format.getCoreseCodeFormat(), format.toString(), graph);
        }
    }

}