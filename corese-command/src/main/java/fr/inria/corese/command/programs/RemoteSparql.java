package fr.inria.corese.command.programs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.github.jsonldjava.shaded.com.google.common.io.Files;

import fr.inria.corese.command.utils.ConfigManager;
import fr.inria.corese.command.utils.http.EnumRequestMethod;
import fr.inria.corese.command.utils.http.SparqlHttpClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "remote-sparql", version = "1.0", description = "Execute a SPARQL query on a remote endpoint.")
public class RemoteSparql implements Callable<Integer> {

    private final int ERROR_EXIT_CODE_SUCCESS = 0;
    private final int ERROR_EXIT_CODE_ERROR = 1;

    private final String DEFAULT_OUTPUT_FILE_NAME = "output";
    private boolean outputPathIsDefined;

    @Spec
    private CommandSpec spec;

    @Option(names = { "-q", "--query" }, description = "SPARQL query to execute", required = true)
    private String query;

    @Option(names = { "-e", "--endpoint" }, description = "SPARQL endpoint URL", required = true)
    private String endpoint_url;

    @Option(names = { "-a", "--accept" }, description = "Accept header value", defaultValue = "text/csv")
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

    @Option(names = { "-g", "--default-graph" }, description = "Default graph URI", arity = "0..")
    private List<String> default_graph;

    @Option(names = { "-n", "--named-graph" }, description = "Named graph URI", arity = "0..")
    private List<String> named_graph;

    @Option(names = { "-o",
            "--output-data" }, description = "Output file path. If not provided, the result will be written to standard output.", arity = "0..1", fallbackValue = DEFAULT_OUTPUT_FILE_NAME)
    private Path output;

    @Option(names = { "-t", "--timeout" }, description = "Timeout in seconds", defaultValue = "0")
    private int timeout;

    @Option(names = { "-c",
            "--config",
            "--init" }, description = "Path to a configuration file. If not provided, the default configuration file will be used.", required = false)
    private Path configFilePath;

    @Option(names = { "-i",
            "--ignore-query-validation" }, description = "Ignore query validation.", required = false, defaultValue = "false")
    private boolean ignoreQueryValidation;

    @Override
    public Integer call() throws Exception {
        try {

            // Check if output is defined
            this.outputPathIsDefined = this.output != null;

            // Load configuration file
            Optional<Path> configFilePath = Optional.ofNullable(this.configFilePath);
            if (configFilePath.isPresent()) {
                ConfigManager.loadFromFile(configFilePath.get(), this.spec, this.verbose);
            } else {
                ConfigManager.loadDefaultConfig(this.spec, this.verbose);
            }

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
     * Send the SPARQL query to the endpoint.
     * 
     * @return The response from the endpoint.
     * @throws Exception If an error occurs.
     */
    public String sendRequest() throws Exception {

        SparqlHttpClient client = new SparqlHttpClient(this.endpoint_url);
        client.setAcceptHeader(this.accept);
        client.setQueryMethod(this.requestMethod);
        client.setVerbose(this.verbose);
        client.setTimeout(this.timeout);
        client.setMaxRedirection(this.maxRedirection);

        return client.sendRequest(this.query, this.default_graph, this.named_graph, this.ignoreQueryValidation);
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
