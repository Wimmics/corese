package fr.inria.corese.command.programs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Scanner;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.command.utils.format.EnumResultFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "sparql", version = App.version, description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Sparql implements Runnable {

    @Spec
    private CommandSpec spec;

    @Option(names = { "-f",
            "--input-format" }, description = "Input serialization format. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-i",
            "--input-data" }, description = "Path or URL of the input file. If not provided, the standard input will be used.")
    private String input;

    @Option(names = { "-r",
            "--output-format" }, description = "Result fileformat. Possible values: ${COMPLETION-CANDIDATES}. ")
    private EnumResultFormat resultFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.")
    private Path output;

    @Parameters(paramLabel = "query_or_file", description = "SPARQL query string or path/URL to a .rq file.")
    private String queryUrlOrFile;
    private String query;

    private Graph graph;

    public Sparql() {
    }

    @Override
    public void run() {
        try {
            loadInputFile();
            loadQuery();
            execute();
        } catch (Exception e) {
            spec.commandLine().getErr().println(e.getMessage());
            throw new ExitCodeException(1, e.getMessage());
        }
    }

    /**
     * Load the input file into a graph.
     * 
     * @throws IOException If the file cannot be read.
     */
    private void loadInputFile() throws IOException {
        if (input == null) {
            // If inputPath is not provided, load from stdin
            this.graph = GraphUtils.load(System.in, inputFormat);
        } else {
            this.graph = GraphUtils.load(input, inputFormat);
        }
    }

    private static String convertToString(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            String result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            return result;
        }
    }

    /**
     * Load the query from the query string or from the query file.
     * 
     * @throws IOException If the query file cannot be read.
     */
    private void loadQuery() throws IOException {
        if (queryUrlOrFile.endsWith(".rq")) {
            InputStream inputStream = GraphUtils.pathOrUrlToInputStream(queryUrlOrFile);
            query = convertToString(inputStream);
        } else {
            query = queryUrlOrFile;
        }
    }

    /**
     * Execute the query and print or write the results.
     * 
     * @throws Exception If the query cannot be executed.
     */
    private void execute() throws Exception {
        QueryProcess exec = QueryProcess.create(graph);
        ASTQuery ast = null;
        Mappings map = null;

        // Execute query
        try {
            ast = exec.ast(query);
            map = exec.query(ast);
        } catch (Exception e) {
            throw new Exception("Error when executing SPARQL query : " + e.getMessage(), e);
        }

        // Print or write results
        if (output == null) {
            printResults(ast, map);
        } else {
            writeResults(ast, map);
        }
    }

    /**
     * Print the results to standard output.
     * 
     * @param ast The query AST.
     * @param map The query results.
     * @throws IOException If the results cannot be printed.
     */
    private void printResults(ASTQuery ast, Mappings map) throws IOException {
        boolean resultFormatIsSet = this.resultFormat != null;

        if (!resultFormatIsSet) {
            this.resultFormat = EnumResultFormat.TURTLE;
        }

        EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();

        if (outputFormat == null && (ast.isUpdate() || ast.isConstruct())) {
            throw new IllegalArgumentException(
                    "Error: " + this.resultFormat
                            + " is not a valid output format for insert, delete or construct requests.");
        }

        if (ast.isUpdate()) {

            GraphUtils.exportToString(graph, outputFormat, spec);
        } else {
            if (!resultFormatIsSet) {
                this.resultFormat = EnumResultFormat.RESULT_TSV;
            }

            ResultFormat resultFormater = ResultFormat.create(map);
            resultFormater.setSelectFormat(this.resultFormat.getValue());
            resultFormater.setConstructFormat(this.resultFormat.getValue());
            String result = resultFormater.toString();
            spec.commandLine().getOut().println(result);
        }
    }

    /**
     * Write the results to a file.
     * 
     * @param ast The query AST.
     * @param map The query results.
     * @throws IOException If the results cannot be written to the output file.
     */
    private void writeResults(ASTQuery ast, Mappings map) throws IOException {
        boolean resultFormatIsSet = this.resultFormat != null;

        if (!resultFormatIsSet) {
            this.resultFormat = EnumResultFormat.TURTLE;
        }

        EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();

        if (outputFormat == null && (ast.isUpdate() || ast.isConstruct())) {
            throw new IllegalArgumentException(
                    "Error: " + this.resultFormat
                            + " is not a valid output format for insert, delete or construct requests.");
        }

        try {
            if (ast.isUpdate()) {

                GraphUtils.export(graph, this.output, outputFormat);
            } else {
                if (!resultFormatIsSet) {
                    this.resultFormat = EnumResultFormat.RESULT_TSV;
                }
                ResultFormat resultFormater = ResultFormat.create(map);
                resultFormater.setSelectFormat(this.resultFormat.getValue());
                resultFormater.setConstructFormat(this.resultFormat.getValue());
                resultFormater.write(this.output.toString());
            }
        } catch (IOException e) {
            throw new IOException("Error when writing the results to the output file : " + e.getMessage(), e);
        }
    }

}
