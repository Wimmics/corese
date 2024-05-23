package fr.inria.corese.command.utils.rdf;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import fr.inria.corese.command.utils.format.EnumCanonicAlgo;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.CanonicalRdf10Format;
import fr.inria.corese.core.print.rdfc10.CanonicalRdf10.CanonicalizationException;
import fr.inria.corese.core.print.rdfc10.HashingUtility.HashAlgorithm;
import picocli.CommandLine.Model.CommandSpec;

/**
 * Utility class to canonicalize RDF data from a Corese Graph.
 */
public class RdfDataCanonicalizer {

    /**
     * Canonicalize RDF data from a Corese Graph and write it to a file.
     *
     * @param path        Path of the file to canonicalize to.
     * @param canonicAlgo Canonicalization algorithm to use.
     * @param graph       Corese Graph to canonicalize RDF data from.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the exported file.
     */
    public static void canonicalizeToFile(
            Path path,
            EnumCanonicAlgo canonicAlgo,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        OutputStream outputStream;

        try {
            outputStream = new FileOutputStream(path.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to open export file: " + path.toString(), e);
        }

        canonicalizeToOutputStream(outputStream, canonicAlgo, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Canonicalized RDF data with format: " + canonicAlgo);
            spec.commandLine().getErr().println("Canonicalized RDF data to file: " + path.toString());
        }
    }

    /**
     * Canonicalize RDF data from a Corese Graph and write it to standard output.
     *
     * @param canonicAlgo Canonicalization algorithm to use.
     * @param graph       Corese Graph to canonicalize RDF data from.
     * @param spec        Command specification.
     * @param verbose     If true, print information about the exported file.
     */
    public static void canonicalizeToStdout(
            EnumCanonicAlgo canonicAlgo,
            Graph graph,
            CommandSpec spec,
            boolean verbose) {

        canonicalizeToOutputStream(System.out, canonicAlgo, graph);

        if (verbose) {
            spec.commandLine().getErr().println("Canonicalized RDF data with format: " + canonicAlgo);
            spec.commandLine().getErr().println("Canonicalized RDF data to: standard output");
        }
    }

    /**
     * Canonicalize RDF data from a Corese Graph and write it to an output stream.
     *
     * @param outputStream Output stream to write the canonicalized RDF data to.
     * @param canonicAlgo  Canonicalization algorithm to use.
     * @param graph        Corese Graph to canonicalize RDF data from.
     */
    private static void canonicalizeToOutputStream(
            OutputStream outputStream,
            EnumCanonicAlgo canonicAlgo,
            Graph graph) {

        try {
            switch (canonicAlgo) {
                case RDFC10:
                case RDFC10SHA256:
                    CanonicalRdf10Format.create(graph, HashAlgorithm.SHA_256).write(outputStream);
                    break;
                case RDFC10SHA384:
                    CanonicalRdf10Format.create(graph, HashAlgorithm.SHA_384).write(outputStream);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported output format: " + canonicAlgo);
            }

            outputStream.flush();

        } catch (CanonicalizationException e) {
            throw new IllegalArgumentException("Unable to canonicalize the RDF data. " + e.getMessage(),
                    e);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write to RDF data to output stream", e);
        }
    }

}
