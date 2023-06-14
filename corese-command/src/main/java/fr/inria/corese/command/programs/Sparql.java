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

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";

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
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Parameters(paramLabel = "query", description = "SPARQL query string or path/URL to a .rq file.")
    private String queryUrlOrFile;
    private String query;

    private Graph graph;

    private boolean resultFromatIsDefine = false;
    private boolean outputFromatIsDefine = false;
    private boolean isDefaultOutputName = false;
    private EnumResultFormat defaultRdfBidings = EnumResultFormat.TURTLE;
    private EnumResultFormat defaultResult = EnumResultFormat.BIDING_TSV;

    public Sparql() {
    }

    @Override
    public void run() {
        try {
            this.resultFromatIsDefine = resultFormat != null;
            this.outputFromatIsDefine = this.output != null;
            this.isDefaultOutputName = this.output == null || this.output.toString().equals(DEFAULT_OUTPUT_FILE_NAME);
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
        this.exportResult(ast, map);
    }

    /**
     * Check if the output format is not compatible with the SELECT query type.
     * 
     * @return True if the output format is not compatible with the SELECT query
     *         type.
     */
    private boolean isNotCompatibleWithSelect() {
        switch (this.resultFormat) {
            case BIDING_XML:
            case BIDING_JSON:
            case BIDING_CSV:
            case BIDING_TSV:
                return true;
            default:
                return false;
        }
    }

    /**
     * Export the results to the output file or to the standard output.
     * 
     * @param ast – AST of the query
     * @param map – Mappings of the query
     * @throws IOException If the output file cannot be written.
     */
    private void exportResult(ASTQuery ast, Mappings map) throws IOException {
        Path outputFileName;

        boolean isUpdate = ast.isInsert() || ast.isDelete() || ast.isUpdate();
        boolean isConstruct = ast.isConstruct();

        // Set default output and result formats if not set
        if (!this.resultFromatIsDefine) {
            if (isUpdate || isConstruct) {
                this.resultFormat = this.defaultRdfBidings;
            } else {
                this.resultFormat = this.defaultResult;
            }
        }

        // Check if the output format is valid for the query type
        if (this.isNotCompatibleWithSelect() && (isUpdate || isConstruct)) {
            throw new IllegalArgumentException(
                    "Error: " + this.resultFormat
                            + " is not a valid output format for insert, delete, describe or construct requests.");
        }

        // Set output file name
        if (this.outputFromatIsDefine && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(DEFAULT_OUTPUT_FILE_NAME + "." + this.resultFormat.getExtention());
        }

        // Export results
        if (isUpdate) {
            EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();
            if (this.outputFromatIsDefine) {
                GraphUtils.exportToFile(graph, outputFormat, outputFileName);
            } else {
                GraphUtils.exportToStdout(graph, outputFormat, spec);
            }
        } else {
            ResultFormat resultFormater = ResultFormat.create(map);
            resultFormater.setSelectFormat(this.resultFormat.getValue());
            resultFormater.setConstructFormat(this.resultFormat.getValue());

            if (this.outputFromatIsDefine) {
                resultFormater.write(outputFileName.toString());
            } else {
                String result = resultFormater.toString();
                spec.commandLine().getOut().println(result);
            }
        }
    }

}
