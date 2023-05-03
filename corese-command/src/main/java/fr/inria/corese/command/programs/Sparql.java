package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "sparql", version = App.version, description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Sparql implements Runnable {

    @Option(names = { "-f",
            "--input-format" }, description = "Input file format. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat;

    @Option(names = { "-i",
            "--input-filepath" }, description = "Input file path. If not provided, the standard input will be used.")
    private String inputPath;

    @Option(names = { "-r",
            "--result-format" }, description = "Result fileformat. Possible values: ${COMPLETION-CANDIDATES}. ")
    private EnumResultFormat resultFormat;

    @Option(names = { "-o",
            "--output-filepath" }, description = "Output file path. If not provided, the result will be written to standard output.")
    private Path outputPath;

    @Parameters(paramLabel = "QUERY_OR_FILE", description = "SPARQL query string or path/URL to a .rq file.")
    private String queryOrFile;

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
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Load the input file into a graph.
     * 
     * @throws IOException If the file cannot be read.
     */
    private void loadInputFile() throws IOException {
        try {
            if (inputPath == null) {
                // If inputPath is not provided, load from stdin
                this.graph = GraphUtils.load(System.in, inputFormat);
            } else {
                this.graph = GraphUtils.load(inputPath, inputFormat);
            }
        } catch (IOException e) {
            throw new IOException("Error reading input file : " + e.getMessage(), e);
        }
    }

    /**
     * Load the query from the query string or from the query file.
     * 
     * @throws IOException If the query file cannot be read.
     */
    private void loadQuery() throws IOException {
        try {
            if (queryOrFile.endsWith(".rq")) {
                // If it's a .rq file, read the query from the file
                queryOrFile = new String(Files.readAllBytes(Path.of(queryOrFile)));
            }
        } catch (IOException e) {
            throw new IOException("Error when reading the query file : " + e.getMessage(), e);
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
            ast = exec.ast(queryOrFile);
            map = exec.query(ast);
        } catch (Exception e) {
            throw new Exception("Error when executing SPARQL query : " + e.getMessage(), e);
        }

        // Print or write results
        if (outputPath == null) {
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
     * @throws IOException
     */
    private void printResults(ASTQuery ast, Mappings map) throws IOException {
        if (ast.isInsert() || ast.isInsertData() || ast.isUpdateInsert() || ast.isUpdateInsertData()) {
            System.out.println("insert");
            EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();

            if (outputFormat == null) {
                throw new IllegalArgumentException(
                        "Error: " + this.resultFormat + "is not a valid output format for insert requests.");
            }

            GraphUtils.print(graph, outputFormat);
        } else {
            ResultFormat resultFormater = ResultFormat.create(map);
            resultFormater.setSelectFormat(this.resultFormat.getValue());
            resultFormater.setConstructFormat(this.resultFormat.getValue());
            String result = resultFormater.toString();
            System.out.println(result);

        }
    }

    /**
     * Write the results to a file.
     * 
     * @param ast The query AST.
     * @param map The query results.
     * @throws Exception If the results cannot be written to the output file.
     */
    private void writeResults(ASTQuery ast, Mappings map) throws Exception {

        if (ast.isInsert() || ast.isInsertData() || ast.isUpdateInsert() || ast.isUpdateInsertData()) {
            System.out.println("insert");
            EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();

            if (outputFormat == null) {
                throw new IllegalArgumentException(
                        "Error: " + this.resultFormat + "is not a valid output format for insert requests.");
            }

            GraphUtils.export(graph, this.outputPath, outputFormat);
        } else {
            ResultFormat resultFormater = ResultFormat.create(map);
            resultFormater.setSelectFormat(this.resultFormat.getValue());
            resultFormater.setConstructFormat(this.resultFormat.getValue());
            try {
                resultFormater.write(this.outputPath.toString());
            } catch (Exception e) {
                throw new Exception("Error when writing the results to the output file : " + e.getMessage(), e);
            }

        }
    }
}
