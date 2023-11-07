
package fr.inria.corese.command.utils.sparql;

import java.nio.file.Path;

import fr.inria.corese.command.utils.format.EnumResultFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.kgram.core.Mappings;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to export SPARQL results.
 */
public class SparqlResultExporter {

    /**
     * Export SPARQL results to a file.
     *
     * @param path         Path of the file to export to.
     * @param resultFormat Output file serialization format.
     * @param map          SPARQL results to export.
     * @param spec         Command specification.
     * @param verbose      If true, print information about the exported file.
     */
    public static void exportToFile(
            Path path,
            EnumResultFormat resultFormat,
            Mappings map,
            CommandSpec spec,
            boolean verbose) {

        ResultFormat resultFormater = ResultFormat.create(map);
        resultFormater.setSelectFormat(resultFormat.getValue());
        resultFormater.setConstructFormat(resultFormat.getValue());

        try {
            resultFormater.write(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open export file: " + path.toString(), e);
        }

        if (verbose) {
            spec.commandLine().getErr().println("Exported SPARQL result with format: " + resultFormat);
            spec.commandLine().getErr().println("Exported SPARQL result to file: " + path.toString());
        }
    }

    /**
     * Export SPARQL results to standard output.
     *
     * @param resultFormat Output file serialization format.
     * @param map          SPARQL results to export.
     * @param spec         Command specification.
     * @param verbose      If true, print information about the exported file.
     */
    public static void exportToStdout(
            EnumResultFormat resultFormat,
            Mappings map,
            CommandSpec spec,
            boolean verbose) {

        ResultFormat resultFormater = ResultFormat.create(map);
        resultFormater.setSelectFormat(resultFormat.getValue());
        resultFormater.setConstructFormat(resultFormat.getValue());

        try {
            String str = resultFormater.toString();
            spec.commandLine().getOut().println(str);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to standard output", e);
        }

        if (verbose) {
            spec.commandLine().getErr().println("Exported SPARQL result with format: " + resultFormat);
            spec.commandLine().getErr().println("Exported SPARQL result to: standard output");
        }
    }
}