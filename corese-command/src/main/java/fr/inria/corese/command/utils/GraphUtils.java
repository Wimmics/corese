package fr.inria.corese.command.utils;

import java.io.InputStream;
import java.nio.file.Path;

import fr.inria.corese.command.utils.format.InputFormat;
import fr.inria.corese.command.utils.format.OutputFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.transform.Transformer;

public class GraphUtils {

    private GraphUtils() {
    }

    /**
     * Parse a file and load RDF data into a Corese Graph.
     *
     * @param inputFile   Path or URL of the input RDF file.
     * @param inputFormat Input file serialization format.
     * @return Corese Graph with RDF data loaded.
     */
    public static Graph load(String inputFile, InputFormat inputFormat) {
        Graph outputGraph = new Graph();

        Load ld = Load.create(outputGraph);
        try {
            ld.parse(inputFile, FromatManager.getCoreseinputFormat(inputFormat));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputGraph;
    }

    /**
     * Parse a file and load RDF data into a Corese Graph.
     *
     * @param input       Input stream of the input RDF file.
     * @param inputFormat Input file serialization format.
     * @return Corese Graph with RDF data loaded.
     */
    public static Graph load(InputStream input, InputFormat inputFormat) {
        Graph outputGraph = new Graph();

        Load ld = Load.create(outputGraph);
        try {
            ld.parse(input, FromatManager.getCoreseinputFormat(inputFormat));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputGraph;
    }

    public static void print(Graph graph, OutputFormat outputFormat) {
        Transformer transformer = Transformer.create(graph, FromatManager.getCoreseOutputFormat(outputFormat));
        try {
            transformer.write(System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Export RDF data from a Corese Graph into a serialized file format.
     * 
     * @param inputGraph   Graph with data to export.
     * @param outputFile   Path of the output RDF file.
     * @param outputFormat output file serialization format.
     */
    public static void export(Graph inputGraph, Path outputFile, OutputFormat outputFormat) {

        Transformer transformer = Transformer.create(inputGraph, FromatManager.getCoreseOutputFormat(outputFormat));
        try {
            transformer.write(outputFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
