
package fr.inria.corese.command.utils.exporter.sparql;

import java.nio.file.Path;

import fr.inria.corese.command.utils.exporter.AbstractExporter;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to export SPARQL query results and SPARQL graphs results.
 */
public class SparqlResultExporter extends AbstractExporter {

    // Default output
    private static final EnumResultFormat DEFAULT_GRAPH_OUTPUT = EnumResultFormat.TURTLE;
    private static final EnumResultFormat DEFAULT_MAPPING_OUTPUT = EnumResultFormat.MARKDOWN;

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
    public SparqlResultExporter(CommandSpec spec, boolean verbose, Path output) {
        super(spec, verbose, output);
    }

    ////////////////////
    // Public methods //
    ////////////////////

    /**
     * Export a SPARQL query result to a file or standard output.
     * 
     * @param format Serialization format.
     * @param graph  SPARQL graph to export.
     * @param map    SPARQL query result to export.
     */
    public void export(Mappings map, Graph graph, EnumResultFormat format) {

        ASTQuery ast = map.getAST();

        boolean isUpdate = ast.isUpdate();
        boolean isConstruct = ast.isConstruct();
        boolean isAsk = ast.isAsk();
        boolean isSelect = ast.isSelect();
        boolean isDescribe = ast.isDescribe();

        // Define the output format if not provided
        if (format == null) {
            if (isUpdate || isConstruct) {
                format = DEFAULT_GRAPH_OUTPUT;
            } else {
                format = DEFAULT_MAPPING_OUTPUT;
            }
        }

        // Check if the output format is compatible with the query result
        if ((isUpdate || isConstruct) && format.isMappingFormat()) {
            throw new IllegalArgumentException(String.format(
                    "Error: %s is not a valid output format for insert, delete, describe or construct requests. Use one of the following RDF formats: %s",
                    format, EnumResultFormat.getRdfFormats()));
        } else if ((isSelect || isAsk || isDescribe) && format.isRdfGraphFormat()) {
            throw new IllegalArgumentException(String.format(
                    "Error: %s is not a valid output format for select or ask requests. Use one of the following mapping formats: %s",
                    format, EnumResultFormat.getMappingFormats()));
        }

        // Define the output file name if not provided
        if (!this.outputIsDefined) {
            this.output = Path.of(DEFAULT_OUTPUT + format.getExtention());
        } else if (this.needToAppendExtension) {
            this.output = Path.of(this.output + format.getExtention());
        }

        // Export the query result
        if (isUpdate) {
            if (this.outputIsDefined) {
                exportToFile(this.output, format.getCoreseCodeFormat(), format.toString(), graph);
            } else {
                exportToStdout(format.getCoreseCodeFormat(), format.toString(), graph);
            }
        } else {
            if (this.outputIsDefined) {
                exportToFile(this.output, format.getCoreseCodeFormat(), format.toString(), map);
            } else {
                exportToStdout(format.getCoreseCodeFormat(), format.toString(), map);
            }
        }
    }

}