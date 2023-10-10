package fr.inria.corese.command.programs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import fr.inria.corese.command.App;
import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.TestType;
import fr.inria.corese.command.utils.format.EnumInputFormat;
import fr.inria.corese.command.utils.format.EnumOutputFormat;
import fr.inria.corese.command.utils.format.EnumResultFormat;
import fr.inria.corese.command.utils.rdf.RdfDataExporter;
import fr.inria.corese.command.utils.rdf.RdfDataLoader;
import fr.inria.corese.command.utils.sparql.SparqlQueryLoader;
import fr.inria.corese.command.utils.sparql.SparqlResultExporter;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "sparql", version = App.version, description = "Run a SPARQL query.", mixinStandardHelpOptions = true)
public class Sparql implements Callable<Integer> {

    private final String ERROR_OUTPUT_FORMAT_CONSTRUCT_REQUEST = "Error: %s is not a valid output format for insert, delete, describe or construct requests. Use one of the following RDF formats: \"rdfxml\", \"turtle\", \"jsonld\", \"trig\", \"jsonld\".";
    private final String ERROR_OUTPUT_FORMAT_SELECT_REQUEST = "Error: %s is not a valid output format for select or ask requests. Use one of the following result formats: \"xml\", \"json\", \"csv\", \"tsv\", \"md\".";
    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;
    private final String DEFAULT_OUTPUT_FILE_NAME = "output";

    @Spec
    private CommandSpec spec;

    @Option(names = { "-f", "-if",
            "--input-format" }, description = "RDF serialization format of the input file. Possible values: ${COMPLETION-CANDIDATES}.")
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
    private Path configFilePath;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    private String query;

    private Graph graph = Graph.create();

    private boolean resultFormatIsDefined = false;
    private boolean outputPathIsDefined = false;
    private boolean isDefaultOutputName = false;

    private EnumResultFormat defaultRdfBidings = EnumResultFormat.TURTLE;
    private EnumResultFormat defaultResult = EnumResultFormat.BIDING_MD;

    public Sparql() {
    }

    @Override
    public Integer call() {

        try {
            // Load configuration file
            Optional<Path> configFilePath = Optional.ofNullable(this.configFilePath);
            if (configFilePath.isPresent()) {
                ConfigManager.loadFromFile(configFilePath.get(), this.spec, this.verbose);
            } else {
                ConfigManager.loadDefaultConfig(this.spec, this.verbose);
            }

            // Set owl import
            Property.set(Value.DISABLE_OWL_AUTO_IMPORT, this.noOwlImport);

            this.resultFormatIsDefined = this.resultFormat != null;
            this.outputPathIsDefined = this.output != null;
            this.isDefaultOutputName = this.output == null
                    || this.DEFAULT_OUTPUT_FILE_NAME.equals(this.output.toString());

            this.loadInputFile();
            this.loadQuery();
            this.executeAndPrint();

            return this.ERROR_EXIT_CODE_SUCCESS;
        } catch (Exception e) {
            this.spec.commandLine().getErr().println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            return this.ERROR_EXIT_CODE_ERROR;
        }
    }

    /**
     * Load the input file(s) into a graph.
     *
     * @throws IOException If the file cannot be read.
     */
    private void loadInputFile() throws IOException {

        if (this.inputs == null) {
            // If inputs is not provided, load from stdin
            RdfDataLoader.LoadFromStdin(
                    this.inputFormat,
                    this.graph,
                    this.spec,
                    this.verbose);
        } else {
            for (String input : this.inputs) {
                Optional<Path> path = ConvertString.toPath(input);
                Optional<URL> url = ConvertString.toUrl(input);

                if (url.isPresent()) {
                    // if input is a URL
                    RdfDataLoader.loadFromURL(
                            url.get(),
                            this.inputFormat,
                            this.graph,
                            this.spec,
                            this.verbose);
                } else if (path.isPresent()) {
                    // if input is a path
                    if (path.get().toFile().isDirectory()) {
                        // if input is a directory
                        RdfDataLoader.loadFromDirectory(
                                path.get(),
                                this.inputFormat,
                                this.graph,
                                this.recursive,
                                this.spec,
                                this.verbose);
                    } else {
                        // if input is a file
                        RdfDataLoader.loadFromFile(
                                path.get(),
                                this.inputFormat,
                                this.graph,
                                this.spec,
                                this.verbose);
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Input path is not a valid URL, file path or directory: " + input);
                }
            }
        }
    }

    /**
     * Load the query from the query string or from the query file.
     *
     * @throws IOException If the query file cannot be read.
     */
    private void loadQuery() throws IOException {
        Optional<Path> path = ConvertString.toPath(this.queryUrlOrFile);
        Optional<URL> url = ConvertString.toUrl(this.queryUrlOrFile);
        Boolean isSparqlQuery = TestType.isSparqlQuery(this.queryUrlOrFile);

        if (isSparqlQuery) {
            // if query is a SPARQL query
            this.query = this.queryUrlOrFile;
        } else if (url.isPresent()) {
            // if query is a URL
            this.query = SparqlQueryLoader.loadFromUrl(url.get(), this.spec, this.verbose);
        } else if (path.isPresent()) {
            // if query is a path
            this.query = SparqlQueryLoader.loadFromFile(path.get(), this.spec, this.verbose);
        }
    }

    /**
     * Execute the query and print or write the results.
     *
     * @throws Exception If the query cannot be executed.
     */
    private void executeAndPrint() throws Exception {
        QueryProcess exec = QueryProcess.create(graph);

        // Execute query
        try {

            if (this.verbose) {
                this.spec.commandLine().getErr().println("Query: " + this.query);
                this.spec.commandLine().getErr().println("Executing query...");
            }

            ASTQuery ast = exec.ast(this.query);
            Mappings map = exec.query(ast);

            // Print or write results
            exportResult(ast, map);
        } catch (Exception e) {
            throw new Exception("Error when executing SPARQL query : " + e.getMessage(), e);
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

        boolean isUpdate = ast.isSPARQLUpdate();
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
        if ((isUpdate || isConstruct) && !this.resultFormat.isRDFFormat()) {
            throw new IllegalArgumentException(String.format(ERROR_OUTPUT_FORMAT_CONSTRUCT_REQUEST, resultFormat));
        }

        if ((isAsk || isSelect) && this.resultFormat.isRDFFormat()) {
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
                RdfDataExporter.exportToFile(
                        outputFileName,
                        outputFormat,
                        this.graph,
                        this.spec,
                        this.verbose);
            } else {
                // if no output format is defined if print results to stdout
                // then print true if the update was successful or false
                // otherwise
                if (!resultFormatIsDefined) {
                    this.spec.commandLine().getOut().println(!map.isEmpty());
                    this.spec.commandLine().getErr()
                            .println(
                                    "Precise result format with --resultFormat option to get the result in standard output.");
                } else {
                    RdfDataExporter.exportToStdout(outputFormat, this.graph, this.spec, this.verbose);
                }
            }
        } else {
            if (this.outputPathIsDefined) {
                SparqlResultExporter.exportToFile(
                        outputFileName,
                        this.resultFormat,
                        map,
                        this.spec,
                        this.verbose);
            } else {
                SparqlResultExporter.exportToStdout(
                        this.resultFormat,
                        map,
                        this.spec,
                        this.verbose);
            }
        }
    }

}
