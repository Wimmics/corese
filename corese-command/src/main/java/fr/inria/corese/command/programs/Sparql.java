package fr.inria.corese.command.programs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.GraphUtils;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.command.utils.format.EnumResultFormat;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "sparql", version = App.version, description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Sparql implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Sparql.class.getName());

    private static final String ERROR_OUTPUT_FORMAT_CONSTRUCT_REQUEST = "Error: %s is not a valid output format for insert, delete, describe or construct requests. Use one of the following RDF formats: \"rdfxml\", \"turtle\", \"jsonld\", \"trig\", \"jsonld\".";
    private static final String ERROR_OUTPUT_FORMAT_SELECT_REQUEST = "Error: %s is not a valid output format for select or ask requests. Use one of the following result formats: \"xml\", \"json\", \"csv\", \"tsv\", \"md\".";
    private static final int EXIT_CODE_ERROR = 1;
    private final String DEFAULT_OUTPUT_FILE_NAME = "output";

    @Spec
    private CommandSpec spec;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "Input serialization format. Possible values: ${COMPLETION-CANDIDATES}.")
    private EnumInputFormat inputFormat = null;

    @Option(names = { "-i",
            "--input-data" }, description = "Path or URL of the input file. If not provided, the standard input will be used.", arity = "1...")
    private String[] inputs;

    @Option(names = { "-r", "-of",
            "--result-format" }, description = "Result fileformat. Possible values: ${COMPLETION-CANDIDATES}. ")
    private EnumResultFormat resultFormat = null;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-q",
            "--query" }, description = "SPARQL query string or path/URL to a .rq file.", required = true)
    private String queryUrlOrFile;

    @Option(names = { "-R",
            "--recursive" }, description = "Load all files in the input directory recursively.", required = false, defaultValue = "false")
    private boolean recursive;

    @Option(names = { "-v",
            "--verbose" }, description = "Prints more information about the execution of the command..", required = false, defaultValue = "false")
    private boolean verbose;

    @Option(names = { "-c",
            "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private String configFilePath;

    private String query;

    private Graph graph;

    private boolean resultFormatIsDefined = false;
    private boolean outputPathIsDefined = false;
    private boolean isDefaultOutputName = false;

    private EnumResultFormat defaultRdfBidings = EnumResultFormat.TURTLE;
    private EnumResultFormat defaultResult = EnumResultFormat.BIDING_MD;

    public Sparql() {
    }

    @Override
    public void run() {

        try {
            // Load configuration file
            if (configFilePath != null) {
                Property.load(configFilePath);
                if (this.verbose) {
                    spec.commandLine().getOut().println("Loaded configuration file: " + configFilePath);
                }
            } else if (this.verbose) {
                spec.commandLine().getOut().println("No configuration file provided. Using default configuration.");
            }

            resultFormatIsDefined = resultFormat != null;
            outputPathIsDefined = output != null;
            isDefaultOutputName = output == null || DEFAULT_OUTPUT_FILE_NAME.equals(output.toString());
            loadInputFile();
            loadQuery();
            execute();
        } catch (Exception e) {
            System.err.println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            System.exit(EXIT_CODE_ERROR);
        }
    }

    /**
     * Load the input file(s) into a graph.
     *
     * @throws IOException If the file cannot be read.
     */
    private void loadInputFile() throws IOException {
        if (inputs == null) {
            // If inputPath is not provided, load from stdin
            this.graph = GraphUtils.load(System.in, inputFormat);
            if (this.verbose) {
                spec.commandLine().getOut().println("Loaded file: stdin");
            }
        } else {
            this.graph = Graph.create();
            for (String input : inputs) {
                File file = new File(input);
                if (file.isDirectory() && recursive) {
                    loadFilesInDirectoryRecursive(file.toPath(), graph);
                } else if (file.isDirectory()) {
                    // If inputPath is a directory, load all files in the directory non-recursively
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File childFile : files) {
                            if (childFile.isFile()) {
                                GraphUtils.load(childFile.getPath(), inputFormat, graph);
                                if (this.verbose) {
                                    spec.commandLine().getOut().println("Loaded file: " + childFile.getPath());
                                }
                            }
                        }
                    }
                } else {
                    GraphUtils.load(input, inputFormat, graph);
                    if (this.verbose) {
                        spec.commandLine().getOut().println("Loaded file: " + input);
                    }
                }
            }
        }
    }

    /**
     * Load all files in the given directory recursively.
     *
     * @param directoryPath The path of the directory to load files from.
     * @param graph         The graph to load the files into.
     */
    private void loadFilesInDirectoryRecursive(Path directoryPath, Graph graph) {
        try {
            Files.walk(directoryPath)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            GraphUtils.load(filePath.toString(), inputFormat, graph);
                            if (this.verbose) {
                                spec.commandLine().getOut().println("Loaded file: " + filePath);
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Error loading file: " + filePath, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading directory: " + directoryPath, e);
        }
    }

    /**
     * Convert an input stream to a string.
     * 
     * @param inputStream The input stream to convert.
     * @return The string representation of the input stream.
     */
    private static String convertToString(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
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
            if (verbose) {
                spec.commandLine().getOut().println("Loaded query from file: " + queryUrlOrFile);
            }
        } else {
            query = queryUrlOrFile;
        }

        if (verbose) {
            spec.commandLine().getOut().println("Executing query: " + query);
        }
    }

    /**
     * Execute the query and print or write the results.
     * 
     * @throws Exception If the query cannot be executed.
     */
    private void execute() throws Exception {
        QueryProcess exec = QueryProcess.create(graph);

        // Execute query
        try {
            ASTQuery ast = exec.ast(query);
            Mappings map = exec.query(ast);

            // Print or write results
            exportResult(ast, map);
        } catch (Exception e) {
            throw new Exception("Error when executing SPARQL query : " + e.getMessage(), e);
        }
    }

    /**
     * Check if the result format is a RDF format.
     * 
     * @return True if the result format is a RDF format, false otherwise.
     */
    private boolean isRDFFormat() {
        switch (this.resultFormat) {
            case BIDING_XML:
            case BIDING_JSON:
            case BIDING_CSV:
            case BIDING_TSV:
            case BIDING_MD:
            case BIDING_MARKDOWN:
                return false;
            default:
                return true;
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
        boolean isAsk = ast.isAsk();
        boolean isSelect = ast.isSelect();

        // Set default output and result formats if not set
        if (!this.resultFormatIsDefined) {
            if (isUpdate || isConstruct) {
                this.resultFormat = this.defaultRdfBidings;
            } else {
                this.resultFormat = this.defaultResult;
            }
        }

        // Check if the output format is valid for the query type
        if ((isUpdate || isConstruct) && !isRDFFormat()) {
            throw new IllegalArgumentException(String.format(ERROR_OUTPUT_FORMAT_CONSTRUCT_REQUEST, resultFormat));
        }

        if ((isAsk || isSelect) && isRDFFormat()) {
            throw new IllegalArgumentException(String.format(ERROR_OUTPUT_FORMAT_SELECT_REQUEST, resultFormat));
        }

        // Set output file name
        if (this.outputPathIsDefined && !this.isDefaultOutputName) {
            outputFileName = this.output;
        } else {
            outputFileName = Path.of(DEFAULT_OUTPUT_FILE_NAME + "." + this.resultFormat.getExtention());
        }

        // Export results
        if (isUpdate) {
            EnumOutputFormat outputFormat = this.resultFormat.convertToOutputFormat();
            if (this.outputPathIsDefined) {
                GraphUtils.exportToFile(graph, outputFormat, outputFileName);
                if (this.verbose) {
                    spec.commandLine().getOut().println("Results exported to file: " + outputFileName);
                }
            } else {
                // if no output format is defined
                // if print results to stdout
                // then print true if the update was successful or false otherwise
                if (!resultFormatIsDefined) {
                    spec.commandLine().getOut().println(!map.isEmpty());
                    spec.commandLine().getErr()
                            .println(
                                    "Precise result format with --resultFormat option to get the result in standard output.");
                } else {
                    GraphUtils.exportToStdout(graph, outputFormat, spec);
                    if (this.verbose) {
                        spec.commandLine().getOut().println("Results exported to standard output.");
                    }
                }
            }
        } else {
            ResultFormat resultFormater = ResultFormat.create(map);
            resultFormater.setSelectFormat(this.resultFormat.getValue());
            resultFormater.setConstructFormat(this.resultFormat.getValue());

            if (this.outputPathIsDefined) {
                resultFormater.write(outputFileName.toString());
                if (this.verbose) {
                    spec.commandLine().getOut().println("Results exported to file: " + outputFileName);
                }
            } else {
                if (this.verbose) {
                    spec.commandLine().getOut().println("Results exported to standard output.");
                }
                String result = resultFormater.toString();
                spec.commandLine().getOut().println(result);
            }
        }
    }

}
