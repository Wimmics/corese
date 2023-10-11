package fr.inria.corese.command.programs;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.github.jsonldjava.shaded.com.google.common.io.Files;

import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.ConvertString;
import fr.inria.corese.command.utils.TestType;
import fr.inria.corese.command.utils.http.EnumRequestMethod;
import fr.inria.corese.command.utils.http.SparqlHttpClient;
import fr.inria.corese.command.utils.sparql.SparqlQueryLoader;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.Property.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "remote-sparql", version = "1.0", description = "Execute a SPARQL query on a remote endpoint.", mixinStandardHelpOptions = true)
public class RemoteSparql implements Callable<Integer> {

    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private boolean outputPathIsDefined;

    @Spec
    private CommandSpec spec;

    @Option(names = { "-q", "--query" }, description = "SPARQL query to execute", required = false)
    private String queryUrlOrFile;

    @Option(names = { "-e", "--endpoint" }, description = "SPARQL endpoint URL", required = true)
    private String endpoint_url;

    @Option(names = { "-H", "--header" }, description = "HTTP header to add to the request", arity = "0..")
    private List<String> headers;

    @Option(names = { "-a", "-of", "--accept" }, description = "Accept header value")
    private String accept;

    @Option(names = { "-m",
            "--request-method" }, description = "HTTP request method to use (GET, POST-urlencoded, POST-direct).", defaultValue = "GET")
    private EnumRequestMethod requestMethod;

    @Option(names = { "-v",
            "--verbose" }, description = "Prints more information about the execution of the command..", required = false, defaultValue = "false")
    private boolean verbose;

    @Option(names = { "-r",
            "--max-redirection" }, description = "Maximum number of redirections to follow", defaultValue = "5")
    private int maxRedirection;

    @Option(names = { "-d", "--default-graph" }, description = "Default graph URI", arity = "0..")
    private List<String> default_graph;

    @Option(names = { "-n", "--named-graph" }, description = "Named graph URI", arity = "0..")
    private List<String> named_graph;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-c",
            "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-i",
            "--ignore-query-validation" }, description = "Ignore query validation.", required = false, defaultValue = "false")
    private boolean ignoreQueryValidation;

    @Option(names = { "-w",
            "--no-owl-import" }, description = "Disables the automatic importation of ontologies specified in 'owl:imports' statements. When this flag is set, the application will not fetch and include referenced ontologies.", required = false, defaultValue = "false")
    private boolean noOwlImport;

    private String query;

    private final String DEFAULT_ACCEPT_HEADER = "text/csv";

    @Override
    public Integer call() throws Exception {
        try {

            // Check if output is defined
            this.outputPathIsDefined = this.output != null;

            // if accept is not defined, set it to text/csv
            if (this.accept == null && !this.containAcceptHeader(this.headers)) {
                this.accept = DEFAULT_ACCEPT_HEADER;
            }

            // Load configuration file
            Optional<Path> configFilePath = Optional.ofNullable(this.configFilePath);
            if (configFilePath.isPresent()) {
                ConfigManager.loadFromFile(configFilePath.get(), this.spec, this.verbose);
            } else {
                ConfigManager.loadDefaultConfig(this.spec, this.verbose);
            }

            // Set owl import
            Property.set(Value.DISABLE_OWL_AUTO_IMPORT, this.noOwlImport);

            // Load query
            this.loadQuery();

            // Execute query
            String res = this.sendRequest();

            // Export result
            this.exportResult(res);

        } catch (Exception e) {
            this.spec.commandLine().getErr().println("\u001B[31mError: " + e.getMessage() + "\u001B[0m");
            return this.ERROR_EXIT_CODE_ERROR;
        }

        return this.ERROR_EXIT_CODE_SUCCESS;
    }

    /**
     * Check if the headers contain an accept header.
     * 
     * @param headers The headers to check.
     * @return True if the headers contain an accept header, false otherwise.
     */
    private Boolean containAcceptHeader(List<String> headers) {
        if (headers == null) {
            return false;
        }
        for (String header : headers) {
            String[] headerParts = header.split(":", 2);
            if (headerParts.length == 2) {
                if (headerParts[0].toLowerCase().equals("accept")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Load the query from the query string or from the query file.
     *
     * @throws IOException If the query file cannot be read.
     */
    private void loadQuery() throws IOException {

        // If query is not defined, read from standard input
        if (this.queryUrlOrFile == null) {
            this.query = SparqlQueryLoader.loadFromInputStream(System.in, this.spec, this.verbose);

            if (this.query == null || this.query.isEmpty()) {
                throw new RuntimeException("The query is not a valid SPARQL query, a URL or a file path.");
            }
        } else {
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
            } else {
                throw new RuntimeException("The query is not a valid SPARQL query, a URL or a file path.");
            }
        }
    }

    /**
     * Send the SPARQL query to the endpoint.
     * 
     * @return The response from the endpoint.
     * @throws Exception If an error occurs.
     */
    public String sendRequest() throws Exception {

        SparqlHttpClient client = new SparqlHttpClient(this.endpoint_url);
        this.parseHeader(client);
        client.setQueryMethod(this.requestMethod);
        client.setVerbose(this.verbose);
        client.setMaxRedirection(this.maxRedirection);

        return client.sendRequest(this.query, this.default_graph, this.named_graph, this.ignoreQueryValidation);
    }

    /**
     * Parse the header and add them to the client.
     * 
     * @param client The client to add the header to.
     */
    private void parseHeader(SparqlHttpClient client) {

        // Add accept header
        if (this.accept != null) {
            client.addHeader("Accept", this.accept);
        }

        // Add other headers
        if (this.headers != null) {
            for (String header : this.headers) {
                String[] headerParts = header.split(":", 2);
                if (headerParts.length == 2) {
                    client.addHeader(headerParts[0], headerParts[1]);
                } else {
                    throw new RuntimeException("Invalid header: " + header);
                }
            }
        }
    }

    /**
     * Export the result to a file or to standard output.
     * 
     * @param response The response to export.
     */
    private void exportResult(String response) {

        if (this.outputPathIsDefined) {
            // Write result to file
            try {
                Files.write(response.getBytes(StandardCharsets.UTF_8), this.output.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Error while writing result to file: " + e.getMessage());
            }
        } else {
            // Write result to standard output
            this.spec.commandLine().getOut().println(response);
        }

    }
}
