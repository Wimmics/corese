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
    public void sparqlSelectResultsXMLGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectResultsXMLUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlSelectResultsXMLUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlSelectCSVGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectCSVUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlSelectCSVUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlSelectTSVGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectTSVUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlSelectTSVUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlSelectMarkdownGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectMarkdownUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlSelectMarkdownUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlSelectJSONGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectJSONUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlSelectJSONUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlAskRDFXMLGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlAskRDFXMLUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlAskRDFXMLUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlAskCSVGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlAskCSVUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlAskCSVUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlAskTSVGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlAskTSVUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_TSV);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlAskTSVUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlAskMarkdownGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlAskMarkdownUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_MD);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlAskMarkdownUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlAskJSONGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlAskJSONUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_JSON);
        headers.add(acceptHeader);

        String query = "ASK {?x ?p ?y}";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlAskJSONUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructTurtleGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructTurtleUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructTurtleUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructRDFXMLGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructRDFXMLUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructRDFXMLUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructTrigGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TRIG);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructTrigUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TRIG);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructTrigUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructJSONLDGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(JSON_LD);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructJSONLDUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(JSON_LD);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructJSONLDUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructNTriplesGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_TRIPLES);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructNTriplesUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_TRIPLES);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructNTriplesUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlConstructNQuadsGet() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_QUADS);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlConstructNQuadsUrlEncodedPost() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(N_QUADS);
        headers.add(acceptHeader);

        String query = "CONSTRUCT { ?x ?p ?y } WHERE { ?x ?p ?y } LIMIT 1";
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers,
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
    public void sparqlConstructNQuadsUnencodedPost() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);

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
    public void sparqlHtml() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(TEXT_HTML);
        headers.add(contentTypeHeader);

        HttpURLConnection con = HTTPTestUtils.getConnection(SPARQL_ENDPOINT_URL, headers);

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

    /**
     * Default graph in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectRDFXMLGetOneDefaultGraph() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlSelectRDFXMLPostUrlencodedOneDefaultGraph() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers, SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters));

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
    public void sparqlSelectRDFXMLPostUnencodedOneDefaultGraph() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return 1 result <http://example.com/nothing>
        String query = "SELECT DISTINCT ?x where { ?x ?p ?y } limit 10";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        String parametersString = HTTPTestUtils.urlParametersToString(parameters);
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL + "?" + parametersString, headers, query);

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
    public void sparqlSelectRDFXMLPostUrlencodedMultipleDefaultGraphs() throws Exception {
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
        parameters.add(new ArrayList<String>());
        parameters.get(1).add("default-graph-uri");
        parameters.get(1).add("http://example.com/A");
        parameters.add(new ArrayList<String>());
        parameters.get(2).add("default-graph-uri");
        parameters.get(2).add("http://example.com/B");
        HttpURLConnection con = HTTPTestUtils.postUrlencodedConnection(SPARQL_ENDPOINT_URL, headers, SPARQLTestUtils.generateSPARQLQueryParameters(query, parameters));

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
     * Default graph in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlSelectRDFXMLPostUnencodedMultipleDefaultGraph() throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        // Should only return 1 result <http://example.com/nothing>
        String query = "SELECT DISTINCT ?x where { ?x ?p ?y } limit 10";
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
        String parametersString = HTTPTestUtils.urlParametersToString(parameters);
        HttpURLConnection con = HTTPTestUtils.postConnection(SPARQL_ENDPOINT_URL + "?" + parametersString, headers, query);

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
    public void sparqlOneNamedGraph() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
    public void sparqlNamedGraphsAmbiguous() throws Exception {
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
        HttpURLConnection con = HTTPTestUtils.getConnection(urlQuery, headers);

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
