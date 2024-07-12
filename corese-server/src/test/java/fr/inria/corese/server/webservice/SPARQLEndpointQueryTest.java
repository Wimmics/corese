package fr.inria.corese.server.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.result.SPARQLJSONResult;
import fr.inria.corese.core.load.result.SPARQLResult;
import fr.inria.corese.kgram.core.Mappings;

import static fr.inria.corese.core.print.ResultFormat.JSON_LD;
import static fr.inria.corese.core.print.ResultFormat.NT_TEXT;
import static fr.inria.corese.core.print.ResultFormat.N_QUADS;
import static fr.inria.corese.core.print.ResultFormat.N_TRIPLES;
import static fr.inria.corese.core.print.ResultFormat.RDF_XML;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_CSV;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_JSON;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_MD;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_TSV;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_XML;
import static fr.inria.corese.core.print.ResultFormat.TRIG;
import static fr.inria.corese.core.print.ResultFormat.TURTLE;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static fr.inria.corese.core.api.Loader.JSONLD_FORMAT;
import static fr.inria.corese.core.api.Loader.NQUADS_FORMAT;
import static fr.inria.corese.core.api.Loader.NT_FORMAT;
import static fr.inria.corese.core.api.Loader.RDFXML_FORMAT;
import static fr.inria.corese.core.api.Loader.TRIG_FORMAT;
import static fr.inria.corese.core.api.Loader.TURTLE_FORMAT;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Test of the behavior of the corese server against SPARQL queries.
 * 
 * Tests:
 * - Is there an RDF void description available at /.well-known/void?
 * - Is there a SPARQL endpoint available at /sparql?
 * - Does the sparql endpoint answers to a simple SPARQL query? For every output
 * format:
 * - application/sparql-results+xml
 * - application/sparql-results+json
 * - text/csv
 * - text/tab-separated-values
 * - text/markdown
 * - text/turtle
 * - application/rdf+xml
 * - application/trig
 * - application/ld+json
 * - application/n-triples
 * - application/n-quads
 * - rdf-canon#sha-256
 * - rdf-canon#sha-384
 * SPARQL:
 * - Are every SPARQL query types supported?
 * - Are every features of the SPARQL query language supported?
 * - Are the limits of the SPARQL query language respected?
 * - Is the timeout of the query respected ?
 * 
 * @see <a href=
 *      "https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/">SPARQL 1.1
 *      Protocol</a>
 * 
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 */
public class SPARQLEndpointQueryTest {

    private static final Logger logger = LogManager.getLogger(SPARQLEndpointQueryTest.class);

    private static Process server;

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String SPARQL_ENDPOINT_URL = SERVER_URL + "sparql";

    /**
     * Start the server before running the tests.
     * Loads a part of the DBpedia dataset in the server.
     */
    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        File turtleFile = new File("src/test/resources/data.ttl");
        String turtleFileAbsolutePath = turtleFile.getAbsolutePath();

        File trigFile = new File("src/test/resources/data.trig");
        String trigFileAbsolutePath = trigFile.getAbsolutePath();

        System.out.println("starting in " + System.getProperty("user.dir"));
        server = new ProcessBuilder().inheritIO().command(
                "java",
                "-jar", "./target/corese-server-4.5.1.jar",
                "-lh",
                "-l", turtleFileAbsolutePath,
                "-l", trigFileAbsolutePath).start();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void shutdown() {
        server.destroy();
    }

    /**
     * Does the endpoint answer to SELECT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectResultsXMLEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectResultsXMLEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectResultsXMLEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to SELECT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectCSVEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectCSVEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectCSVEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to SELECT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectTSVEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectTSVEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectTSVEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to SELECT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectMarkdownEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectMarkdownEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectMarkdownEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to SELECT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectJSONEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectJSONEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    /**
     * Does the endpoint answer to a SELECT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectJSONEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    /**
     * Does the endpoint answer to ASK a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskRDFXMLEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskRDFXMLEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskRDFXMLEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
    }

    /**
     * Does the endpoint answer to ASK a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskCSVEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskCSVEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskCSVEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to ASK a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskTSVEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskTSVEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskTSVEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
    }

    /**
     * Does the endpoint answer to ASK a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskMarkdownEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskMarkdownEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskMarkdownEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_MD, con.getContentType());
    }

    /**
     * Does the endpoint answer to ASK a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskJSONEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskJSONEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    /**
     * Does the endpoint answer to a ASK query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlAskJSONEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }


    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTurtleEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(TURTLE_TEXT, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTurtleEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(TURTLE_TEXT, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTurtleEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(TURTLE_TEXT, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructRDFXMLEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(200, status);
        assertEquals(RDF_XML, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructRDFXMLEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(RDF_XML, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructRDFXMLEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(200, status);
        assertEquals(RDF_XML, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTrigEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TRIG);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TRIG_FORMAT);

        assertEquals(200, status);
        assertEquals(TRIG, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTrigEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TRIG);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TRIG_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(TRIG, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructTrigEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TRIG);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TRIG_FORMAT);

        assertEquals(200, status);
        assertEquals(TRIG, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructJSONLDEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(JSON_LD);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, JSONLD_FORMAT);

        assertEquals(200, status);
        assertEquals(JSON_LD, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructJSONLDEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(JSON_LD);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, JSONLD_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(JSON_LD, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructJSONLDEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(JSON_LD);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, JSONLD_FORMAT);

        assertEquals(200, status);
        assertEquals(JSON_LD, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNTriplesEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_TRIPLES);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(N_TRIPLES, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNTriplesEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_TRIPLES);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(N_TRIPLES, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNTriplesEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_TRIPLES);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(N_TRIPLES, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to CONSTRUCT a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNQuadsEndpointGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_QUADS);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, NQUADS_FORMAT);

        assertEquals(200, status);
        assertEquals(N_QUADS, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNQuadsEndpointUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_QUADS);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
                SPARQLTestUtils.generateSPARQLQueryParameters(query));

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, NQUADS_FORMAT);

        con.disconnect();

        assertEquals(200, status);
        assertEquals(N_QUADS, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Does the endpoint answer to a CONSTRUCT query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlConstructNQuadsEndpointUnencodedPost() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_QUADS);
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-query");
        headers.add(acceptHeader);
        headers.add(contentTypeHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, NQUADS_FORMAT);

        assertEquals(200, status);
        assertEquals(N_QUADS, con.getContentType());
        assertEquals(1, constructGraph.size());
    }

    /**
     * Is there an HTML page available at /sparql?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointHtml() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(TEXT_HTML);
        headers.add(contentTypeHeader);

        HttpURLConnection con = SPARQLTestUtils.getConnection(SPARQL_ENDPOINT_URL, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(TEXT_HTML, con.getContentType());
    }

    @Test
    public void sparqlEndpointSelectRDFXML() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointSelectJSON() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Mappings queryResults = SPARQLJSONResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointSelectCSV() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resultString = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            resultString.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
        assertTrue(resultString.toString().contains("x,p,y"));
    }

    @Test
    public void sparqlEndpointSelectTSV() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resultString = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            resultString.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
        assertTrue(resultString.toString().contains("?x\t?p\t?y"));
    }

    @Test
    public void sparqlEndpointAskRDFXML() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointAskJSON() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        SPARQLJSONResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_JSON, con.getContentType());
    }

    @Test
    public void sparqlEndpointAskCSV() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resultString = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            resultString.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_CSV, con.getContentType());
        assertEquals("true", resultString.toString());
    }

    @Test
    public void sparqlEndpointAskTSV() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resultString = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            resultString.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_TSV, con.getContentType());
        assertEquals("true", resultString.toString());
    }

    @Test
    public void sparqlEndpointConstructRDFXML() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "construct {?x ?p ?y} where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(200, status);
        assertEquals(RDF_XML, con.getContentType());
        assertTrue(constructGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointDescribeRDFXML() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "describe ?x where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph describeGraph = new Graph();
        Load load = Load.create(describeGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(200, status);
        assertEquals(RDF_XML, con.getContentType());
        assertTrue(describeGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointConstructTurtle() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "construct {?x ?p ?y} where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(TURTLE_TEXT, con.getContentType());
        assertTrue(constructGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointDescribeTurtle() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "describe ?x where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();

        con.disconnect();

        Graph describeGraph = new Graph();
        Load load = Load.create(describeGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(200, status);
        assertEquals(TURTLE_TEXT, con.getContentType());
        assertTrue(describeGraph.size() > 0);
    }

    /**
     * Default graph in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointOneDefaultGraph() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return 1 result <http://example.com/nothing>
        String query = "select DISTINCT ?x where { ?x ?p ?y } limit 10";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();
        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertEquals(1, queryResults.size());
        assertEquals("http://example.com/nothing", queryResults.get(0).getNode("?x").getLabel());
    }

    /**
     * Default graph in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointMultipleDefaultGraphs() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return 1 result <http://example.com/nothing>
        String query = "select DISTINCT ?x where { ?x ?p ?y } ORDER BY ?x limit 10";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        parameters.add(new ArrayList<String>());
        parameters.get(1).add("default-graph-uri");
        parameters.get(1).add("http://example.com/A");
        parameters.add(new ArrayList<String>());
        parameters.get(2).add("default-graph-uri");
        parameters.get(2).add("http://example.com/B");
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();
        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertEquals(3, queryResults.size());
        assertEquals("http://example.com/A", queryResults.get(0).getNode("?x").getLabel());
        assertEquals("http://example.com/B", queryResults.get(1).getNode("?x").getLabel());
        assertEquals("http://example.com/nothing", queryResults.get(2).getNode("?x").getLabel());
    }

    /**
     * Named graphs in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointOneNamedGraph() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return 2 results <http://example.com/nothing> and
        // <http://example.com/cities>
        String query = "select DISTINCT ?x where { GRAPH ?g { ?x a ?type } } limit 20";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        parameters.add(new ArrayList<String>());
        parameters.get(1).add("named-graph-uri");
        parameters.get(1).add("http://example.com/A");
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();
        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertEquals(1, queryResults.size());
        assertEquals("http://example.com/A", queryResults.get(0).getNode("?x").getLabel());
    }

    /**
     * In a conflict between protocol named graphs and query named graphs, the
     * protocol named graphs should be taken into account.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#dataset">SPARQL
     *      Protocol</a>
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointNamedGraphsAmbiguous() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return ex:A that is the only named graph in the protocol. Should
        // not returns ex:B specified in the query.
        String query = "select DISTINCT ?x FROM NAMED <http://example.com/B> where { GRAPH ?g { ?x a ?type } } limit 20";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        parameters.add(new ArrayList<String>());
        parameters.get(1).add("named-graph-uri");
        parameters.get(1).add("http://example.com/A");
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int status = con.getResponseCode();
        con.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertEquals(200, status);
        assertEquals(SPARQL_RESULTS_XML, con.getContentType());
        assertEquals(1, queryResults.size());
        assertEquals("http://example.com/A", queryResults.get(0).getNode("?x").getLabel());
    }
}
