package fr.inria.corese.command.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.core.transform.Transformer;
import picocli.CommandLine.Model.CommandSpec;

public class GraphUtils {

    private GraphUtils() {
    }

    /**
     * Transform a path or URL to an input stream.
     * 
     * @param pathOrUrl Path or URL of the input file.
     * @return Input stream of the input file.
     */
    public static InputStream pathOrUrlToInputStream(String pathOrUrl) {
        InputStream inputStream = null;
        try {
            if (pathOrUrl.startsWith("http")) {
                URL url = new URL(pathOrUrl);
                URI uri = url.toURI();
                if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                    throw new IllegalArgumentException("Invalid URL scheme: " + uri.getScheme());
                }
                inputStream = url.openStream();
            } else {
                inputStream = new FileInputStream(pathOrUrl);
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Error while loading : " + e.getMessage(), e);
        }
        return inputStream;
    }

    /**
     * Parse a file and load RDF data into a Corese Graph.
     *
     * @param pathOrUrl   Path or URL of the input RDF file.
     * @param inputFormat Input file serialization format.
     * @return Corese Graph with RDF data loaded.
     * @throws IOException if an error occurs while loading the input file.
     */
    public static Graph load(String pathOrUrl, EnumInputFormat inputFormat) throws IOException {
        InputStream inputStream = GraphUtils.pathOrUrlToInputStream(pathOrUrl);

        if (inputFormat == null) {
            inputFormat = EnumInputFormat.fromLoaderValue(LoadFormat.getFormat(pathOrUrl));
        }

        Graph graph = load(inputStream, inputFormat);

        inputStream.close();
        return graph;
    }

    /**
     * Parse a file and load RDF data into a Corese Graph.
     *
     * @param inputStream Input stream of the input RDF file.
     * @param inputFormat Input file serialization format.
     * @return Corese Graph with RDF data loaded.
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the input format cannot be determined
     *                                  automatically when using standard input.
     */
    public static Graph load(InputStream inputStream, EnumInputFormat inputFormat)
            throws IOException, IllegalArgumentException {
        final Graph graph = new Graph();

        Load load = Load.create(graph);
        if (inputFormat == null) {
            throw new IllegalArgumentException(
                    "The input format cannot be automatically determined if you use standard input or na URL. "
                            + "Please specify the input format with the option -f.");
        } else {
            try {
                load.parse(inputStream, EnumInputFormat.toLoaderValue(inputFormat));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse RDF file.", e);

            }
        }

        return graph;
    }

    /**
     * Export RDF data from a Corese Graph into a serialized file format.
     * 
     * @param inputGraph   Graph with data to export.
     * @param outputFormat output file serialization format.
     * @throws IOException if an I/O error occurs.
     */
    public static void exportToStdout(Graph inputGraph, EnumOutputFormat outputFormat, CommandSpec spec)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        export(inputGraph, outputStream, outputFormat);
        String outputContent = outputStream.toString(StandardCharsets.UTF_8);
        spec.commandLine().getOut().println(outputContent);
    }

    /**
     * Export RDF data from a Corese Graph into a serialized file format.
     * 
     * @param inputGraph   Graph with data to export.
     * @param outputFile   Path of the output RDF file.
     * @param outputFormat output file serialization format.
     * @throws IOException           if an I/O error occurs.
     * @throws FileNotFoundException if the file exists but is a directory rather
     */
    public static void exportToFile(Graph inputGraph, EnumOutputFormat outputFormat, Path outputFile)
            throws FileNotFoundException, IOException {
        export(inputGraph, new FileOutputStream(outputFile.toFile()), outputFormat);
    }

    /**
     * Export the RDF data in the given graph to the specified output stream using
     * the given output format.
     *
     * @param graph        The graph to export.
     * @param outputStream The output stream to write the RDF data to.
     * @param outputFormat The output format to use.
     * @throws IOException if an I/O error occurs.
     */
    private static void export(Graph graph, OutputStream outputStream, EnumOutputFormat outputFormat)
            throws IOException {
        try (outputStream) {
            Transformer transformer = Transformer.create(graph, EnumOutputFormat.convertToTransformer(outputFormat));
            transformer.write(outputStream);
        } catch (IOException e) {
            throw new IOException("Failed to write RDF data to output stream.", e);
        }
    }
}
