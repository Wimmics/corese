package fr.inria.corese.command.utils.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fr.inria.corese.command.utils.TestType;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Composite;
import fr.inria.corese.sparql.triple.update.Update;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This class provides functionalities to send HTTP requests to a SPARQL
 * endpoint.
 */
public class SparqlHttpClient {

    private final String endpointUrl;
    private EnumRequestMethod queryMethod = EnumRequestMethod.GET;
    private List<Pair<String, String>> headers = new ArrayList<>();

    private boolean verbose = false;

    private int redirectCount = 0;
    private int maxRedirects = 5;
    private final String USERAGENT = "Corese-Command/4.5.0";

    /////////////////
    // Constructor //
    /////////////////

    /**
     * Constructor.
     * 
     * @param endpointUrl URL of the SPARQL endpoint to send the request to.
     */
    public SparqlHttpClient(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isEmpty()) {
            throw new IllegalArgumentException("Endpoint URL must be specified");
        }
        this.endpointUrl = endpointUrl;
    }

    ///////////////////////
    // Getters & Setters //
    ///////////////////////

    /**
     * Sets the query method.
     * 
     * @param requestMethod the query method
     */
    public void setQueryMethod(EnumRequestMethod requestMethod) {
        this.queryMethod = requestMethod;
    }

    /**
     * Sets the verbose mode.
     * 
     * @param verbose true to enable verbose mode, false otherwise
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Sets the maximum number of redirections to follow.
     * 
     * @param maxRedirects the maximum number of redirections to follow
     */
    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Sets the maximum number of redirections to follow.
     * 
     * @param maxRedirection the maximum number of redirections to follow
     */
    public void setMaxRedirection(int maxRedirection) {
        this.maxRedirects = maxRedirection;
    }

    /**
     * Gets the endpoint URL.
     * 
     * @return the endpoint URL
     */
    public String getEndpointUrl() {
        return this.endpointUrl;
    }

    /**
     * Sets the personalized header.
     * 
     * @param key   the key of the header
     * @param value the value of the header
     */
    public void addHeader(String key, String value) {
        this.headers.add(Pair.of(key, value));
    }

    /////////////////////////
    // HTTP request method //
    /////////////////////////

    /**
     * Sends a SPARQL query to the SPARQL endpoint.
     * 
     * @param query SPARQL query to send
     * @return the response from the endpoint
     * @throws Exception if an error occurs while sending the request
     */
    public String sendRequest(String query) throws Exception {
        return sendRequest(query, new ArrayList<>(), new ArrayList<>(), false);
    }

    /**
     * Sends a SPARQL query to the SPARQL endpoint.
     * 
     * @param query                 SPARQL query to send
     * @param defaultGraphUris      default graph URIs to use
     * @param namedGraphUris        named graph URIs to use
     * @param ignoreQueryValidation true to ignore query validation, false otherwise
     * @return the response from the endpoint
     * @throws Exception if an error occurs while sending the request
     */
    public String sendRequest(String query, List<String> defaultGraphUris, List<String> namedGraphUris,
            boolean ignoreQueryValidation)
            throws Exception {

        // Fix parameters
        if (defaultGraphUris == null) {
            defaultGraphUris = new ArrayList<>();
        }

        if (namedGraphUris == null) {
            namedGraphUris = new ArrayList<>();
        }

        // Validate the query
        if (!ignoreQueryValidation) {
            this.validateQuery(query, defaultGraphUris, namedGraphUris);
        }

        // Create the web target based on type of query method
        WebTarget webTarget = this.buildWebTarget(this.endpointUrl, query, defaultGraphUris, namedGraphUris);

        // Create the request body based on type of query method
        String bodyContent = this.buildRequestBody(query, defaultGraphUris, namedGraphUris);

        // Print query and body content if verbose mode is enabled
        if (this.verbose) {
            this.printRequest(webTarget, bodyContent);
        }

        Response response;
        this.redirectCount = 0;
        while (true) {
            // Execute the request
            response = this.executeRequest(webTarget, bodyContent);

            // Manage redirections
            if (this.isRedirection(response) && this.redirectCount < this.maxRedirects) {
                this.redirectCount++;

                String newLocation = this.getRedirectLocation(response);
                if (this.verbose) {
                    System.err.println("Redirecting to: " + newLocation);
                }

                webTarget = this.buildWebTarget(newLocation, query, defaultGraphUris, namedGraphUris);
            } else {
                break;
            }
        }

        // Print the response if verbose mode is enabled
        if (this.verbose) {
            this.printResponse(response);
        }

        // Validate the response
        validateResponse(response);

        // Return the response
        return response.readEntity(String.class);
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Prints the response details.
     * 
     * @param response the response to print
     */
    private void printResponse(Response response) {
        System.err.println("Response Details:");

        if (response != null) {
            System.err.println("  HTTP code: " + response.getStatus());
        }
    }

    /**
     * Prints the request details.
     * 
     * @param webTarget   the web target of the request
     * @param bodyContent the body content of the request
     */
    private void printRequest(WebTarget webTarget, String bodyContent) {
        System.err.println("Request Details:");

        // Print URL
        if (webTarget != null && webTarget.getUri() != null) {
            System.err.println("  URL: " + webTarget.getUri());
        }

        // Print query method
        if (this.queryMethod != null && this.queryMethod.getName() != null && !this.queryMethod.getName().isEmpty()) {
            System.err.println("  method: " + this.queryMethod.getName());
        }

        // Print query string parameter
        if (webTarget != null && webTarget.getUri() != null && webTarget.getUri().getQuery() != null
                && !webTarget.getUri().getQuery().isEmpty()) {
            System.err.println("  Query string parameter: " + webTarget.getUri().getQuery());
        }

        // Print headers
        if (this.headers != null && !this.headers.isEmpty()) {
            System.err.println("  Headers:");
            for (Pair<String, String> header : this.headers) {

                System.err.println("    " + header.getKey() + ": " + header.getValue());
            }
        }

        if (bodyContent != null && !bodyContent.isEmpty()) {
            System.err.println("  Request body: " + bodyContent);
        }
    }

    /**
     * Validates the query. The query must be defined and must be a valid SPARQL
     * query and respect the SPARQL specification.
     * 
     * @param queryString      the query to validate
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     */
    private void validateQuery(String queryString, List<String> defaultGraphUris, List<String> namedGraphUris) {

        // Check if the query is defined
        if (queryString == null || queryString.isEmpty()) {
            throw new IllegalArgumentException("SPARQL query must be specified");
        }

        // Check if the query is a valid SPARQL query
        if (!TestType.isSparqlQuery(queryString)) {
            throw new IllegalArgumentException("Invalid SPARQL query");
        }

        Query query = buildQuery(queryString);

        // Check if the query is an update query and the method is GET
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#update-operation)
        if (this.queryMethod == EnumRequestMethod.GET && query.getAST().isSPARQLUpdate()) {
            throw new IllegalArgumentException(
                    "SPARQL query is an update query, but GET method is used. Please use a POST method instead.");
        }

        // Check if the query contains FROM clause and default/named graph URIs
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#query-operation)
        if (containsFromClause(query) && !(defaultGraphUris.isEmpty() && namedGraphUris.isEmpty())) {
            throw new IllegalArgumentException(
                    "SPARQL query contains FROM clause, but default and named graph URIs are specified. It is not allowed to specify both FROM clause and default/named graph URIs. Please remove FROM clause from the query or remove default/named graph URIs.");
        }

        // Check if the update query contains USING, USING NAMED, or WITH clauses
        // and the using-graph-uri/using-named-graph-uri parameters are also specified
        // which is not allowed by the SPARQL specification
        // (see https://www.w3.org/TR/sparql11-protocol/#update-operation)
        List<String> sparqlConstants = new ArrayList<>();
        ASTUpdate astUpdate = query.getAST().getUpdate();
        if (astUpdate != null) {
            for (Update update : astUpdate.getUpdates()) {
                Composite composite = update.getComposite();
                if (composite != null) {
                    Constant with = composite.getWith();
                    if (with != null) {
                        sparqlConstants.add(with.getLabel());
                    }
                }
            }
        }

        if (!sparqlConstants.isEmpty() && (!defaultGraphUris.isEmpty() || !namedGraphUris.isEmpty())) {
            throw new IllegalArgumentException(
                    "SPARQL update query contains USING, USING NAMED, or WITH clause and the using-graph-uri/using-named-graph-uri parameters are also specified. It is not allowed to specify both USING, USING NAMED, or WITH clause and the using-graph-uri/using-named-graph-uri parameters. Please remove USING, USING NAMED, or WITH clause from the query or remove the using-graph-uri/using-named-graph-uri parameters.");
        }

    }

    /**
     * Builds a query object from the given query string.
     * 
     * @param query the query string
     * @return the query object
     */
    private Query buildQuery(String query) {
        QueryProcess exec = QueryProcess.create(Graph.create());
        Query q;
        try {
            q = exec.compile(query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SPARQL query", e);
        }
        return q;
    }

    /**
     * Checks if the query contains FROM clause.
     * 
     * @param query the query to check
     * @return true if the query contains FROM clause, false otherwise
     */
    private boolean containsFromClause(Query query) {
        return query.getFrom() != null && !query.getFrom().isEmpty();
    }

    /**
     * Builds a web target.
     * 
     * @param endpoint         the endpoint URL
     * @param query            the query
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     * @return the web target object
     */
    private WebTarget buildWebTarget(
            String endpoint,
            String query,
            List<String> defaultGraphUris,
            List<String> namedGraphUris) {

        // Create the web target
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        Client client = clientBuilder.build();
        WebTarget webTarget = client.target(endpoint);

        // Add the query parameter
        if (this.queryMethod == EnumRequestMethod.GET) {
            webTarget = webTarget.queryParam("query", this.encode(query));
        }

        // Add graph URIs
        if (this.queryMethod == EnumRequestMethod.GET || this.queryMethod == EnumRequestMethod.POST_DIRECT) {
            for (String defaultGraphUri : defaultGraphUris) {
                webTarget = webTarget.queryParam("default-graph-uri", this.encode(defaultGraphUri));
            }
            for (String namedGraphUri : namedGraphUris) {
                webTarget = webTarget.queryParam("named-graph-uri", this.encode(namedGraphUri));
            }
        }

        return webTarget;
    }

    /**
     * Builds the request body.
     * 
     * @param query            the query
     * @param defaultGraphUris default graph URIs to use
     * @param namedGraphUris   named graph URIs to use
     * @return the request body
     */
    private String buildRequestBody(
            String query,
            List<String> defaultGraphUris,
            List<String> namedGraphUris) {

        StringBuilder bodyContent = new StringBuilder();

        if (this.queryMethod == EnumRequestMethod.POST_URLENCODED) {
            // Add the query parameter
            bodyContent.append("query=").append(this.encode(query));

            // Add graph URIs
            for (String defaultGraphUri : defaultGraphUris) {
                bodyContent.append("&default-graph-uri=").append(this.encode(defaultGraphUri));
            }
            for (String namedGraphUri : namedGraphUris) {
                bodyContent.append("&named-graph-uri=").append(this.encode(namedGraphUri));
            }
        } else if (this.queryMethod == EnumRequestMethod.POST_DIRECT) {
            // Add the query parameter
            bodyContent.append(query);
        }

        return bodyContent.toString();
    }

    /**
     * Executes the request.
     * 
     * @param webTarget   the web target of the request
     * @param bodyContent the body content of the request
     * @return the response from the endpoint
     */
    private Response executeRequest(WebTarget webTarget, String bodyContent) {
        Response response = null;

        // Add headers
        Builder builder = webTarget.request()
                .header("User-Agent", this.USERAGENT);

        for (Pair<String, String> header : this.headers) {
            builder = builder.header(header.getKey(), header.getValue());
        }

        // Send the request
        if (this.queryMethod == EnumRequestMethod.GET) {
            response = builder.get();
        } else if (this.queryMethod == EnumRequestMethod.POST_URLENCODED) {
            response = builder.post(Entity.entity(bodyContent, MediaType.APPLICATION_FORM_URLENCODED));
        } else if (this.queryMethod == EnumRequestMethod.POST_DIRECT) {
            response = builder.post(Entity.entity(bodyContent, "application/sparql-query"));
        }

        return response;
    }

    /**
     * Encodes the given value using the UTF-8 encoding scheme.
     *
     * @param value the value to be encoded
     * @return the encoded value
     * @throws IllegalStateException if the UTF-8 encoding is not supported, which
     *                               should never happen as it is guaranteed to be
     *                               supported by the JVM
     *                               (see <a href=
     *                               "https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html">Java
     *                               Charset documentation</a>).
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to encode value: " + value, e);
        }
    }

    /**
     * Validates the response. If the response is not valid, an exception is thrown.
     * 
     * @param response the response to validate
     * @throws Exception if the response is not valid
     */
    private void validateResponse(Response response) throws Exception {
        int status = response.getStatus();

        if (status < 200 || status >= 300) {
            String errorMessage = response.readEntity(String.class);

            String detailedMessage;
            switch (status) {
                case 301:
                    detailedMessage = "Moved Permanently. The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs. Try to increase the number of max redirections.";
                    break;
                case 302:
                    detailedMessage = "Found. The requested resource has been assigned a new permanent URI and any future references to this resource SHOULD use one of the returned URIs. Try to increase the number of max redirections.";
                    break;
                case 303:
                    detailedMessage = "See Other. The response to the request can be found under another URI using a GET method. Try to increase the number of max redirections.";
                    break;
                case 400:
                    detailedMessage = "Bad Request. The request could not be understood or was missing required parameters.";
                    break;
                case 401:
                    detailedMessage = "Unauthorized. Authentication failed or user does not have permissions for the requested operation.";
                    break;
                case 403:
                    detailedMessage = "Forbidden. Authentication succeeded but authenticated user does not have access to the resource.";
                    break;
                case 404:
                    detailedMessage = "Not Found. The requested resource could not be found.";
                    break;
                case 406:
                    detailedMessage = "Not Acceptable. The server cannot produce a response matching the list of acceptable values defined in the request's headers.";
                    break;
                case 408:
                    detailedMessage = "Request Timeout. The server would like to shut down this unused connection.";
                    break;
                case 429:
                    detailedMessage = "Too Many Requests. The user has sent too many requests in a given amount of time.";
                    break;
                case 500:
                    detailedMessage = "Internal Server Error. An error occurred on the server.";
                    break;
                case 502:
                    detailedMessage = "Bad Gateway. The server received an invalid response from the upstream server.";
                    break;
                case 503:
                    detailedMessage = "Service Unavailable. The server is currently unavailable.";
                    break;
                case 504:
                    detailedMessage = "Gateway Timeout. The gateway did not receive a timely response from the upstream server or some other auxiliary server.";
                    break;
                default:
                    detailedMessage = "Unexpected error.";
            }

            throw new Exception(
                    "HTTP error code: " + status + ".\n"
                            + "Error message: " + errorMessage + ".\n"
                            + "Detailed message: " + detailedMessage);
        }
    }

    /**
     * Checks if the response is a redirection.
     * 
     * @param response the response to check
     * @return true if the response is a redirection, false otherwise
     */
    private boolean isRedirection(Response response) {
        int status = response.getStatus();
        return status == Response.Status.MOVED_PERMANENTLY.getStatusCode()
                || status == Response.Status.FOUND.getStatusCode()
                || status == Response.Status.SEE_OTHER.getStatusCode();
    }

    /**
     * Gets the location of the redirection.
     * 
     * @param response the response to check
     * @return the location of the redirection
     */
    private String getRedirectLocation(Response response) {
        return response.getHeaderString("Location");
    }

}