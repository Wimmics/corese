package fr.inria.corese.server.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import static fr.inria.corese.core.print.ResultFormat.RDF_XML;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_JSON;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_XML;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static fr.inria.corese.core.api.Loader.RDFXML_FORMAT;
import static fr.inria.corese.core.api.Loader.TURTLE_FORMAT;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Test of the behavior of the corese server against HTTP requests.
 * 
 * Tests:
 * - Is there an RDF void description available at /.well-known/void?
 * - Is there a SPARQL endpoint available at /sparql?
 * - Does the sparql endpoint answers to a simple SPARQL query?
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
public class SPARQLEndpointTest {

    private static final Logger logger = LogManager.getLogger(SPARQLEndpointTest.class);

    private static Process server;

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String SPARQL_ENDPOINT_URL = SERVER_URL + "sparql";

    /**
     * Get a connection to a server.
     * 
     * @param url     server URL
     * @param headers HTTP headers
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ProtocolException
     */
    private HttpURLConnection getConnection(String url, Map<String, String> headers)
            throws MalformedURLException, IOException, ProtocolException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(true);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return con;
    }

    private HttpURLConnection postConnection(String url, Map<String, String> headers, String body)
            throws MalformedURLException, IOException, ProtocolException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(true);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        con.setDoOutput(true);
        con.getOutputStream().write(body.getBytes());
        return con;
    }

    private String generateSPARQLQueryParameters(String query, List<List<String>> optionalParameters) {
        try {
            String result = "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            if (optionalParameters.size() > 0) {
                for (Iterator<List<String>> itParam = optionalParameters.iterator(); itParam.hasNext();) {
                    List<String> p = itParam.next();
                    if (p.size() == 2) {
                        result += "&" + p.get(0) + "=" + URLEncoder.encode(p.get(1), StandardCharsets.UTF_8.toString());
                    } else if (p.size() == 1) {
                        result += "&" + p.get(0);
                    }
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            return null;
        }
    }

    private String generateSPARQLQueryParameters(String query) {
        return generateSPARQLQueryParameters(query, new ArrayList<>());
    }

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
     * Does the endpoint answer to a query via GET?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointGet() throws Exception {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
    }

    /**
     * Does the endpoint answer to a query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointUrlEncodedPost() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = postConnection(SPARQL_ENDPOINT_URL, headers,
                "query=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString()));

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

        assertEquals(status, 200);
    }

    /**
     * Does the endpoint answer to a query via URL-encoded POST?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointUnencodedPost() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);
        headers.put("Content-Type", "application/sparql-query");

        String query = "select * where {?x ?p ?y} limit 1";
        HttpURLConnection con = postConnection(SPARQL_ENDPOINT_URL, headers, query);

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

        assertEquals(status, 200);
    }

    /**
     * Is there an HTML page available at /sparql?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointHtml() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", TEXT_HTML);

        HttpURLConnection con = getConnection(SPARQL_ENDPOINT_URL, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), TEXT_HTML);
    }

    @Test
    public void sparqlEndpointSelectRDFXML() throws Exception {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), SPARQL_RESULTS_XML);
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointAskRDFXML() throws Exception {

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), SPARQL_RESULTS_XML);
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointSelectJSON() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_JSON);

        String query = "select * where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), SPARQL_RESULTS_JSON);
        assertTrue(queryResults.size() > 0);
    }

    @Test
    public void sparqlEndpointAskJSON() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_JSON);

        String query = "ASK {?x ?p ?y}";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        logger.debug(content.toString());
        assertEquals(status, 200);
        assertEquals(con.getContentType(), SPARQL_RESULTS_JSON);
    }

    @Test
    public void sparqlEndpointConstructRDFXML() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", RDF_XML);

        String query = "construct {?x ?p ?y} where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(constructGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointDescribeRDFXML() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", RDF_XML);

        String query = "describe ?x where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(describeGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointConstructTurtle() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", TURTLE_TEXT);

        String query = "construct {?x ?p ?y} where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), TURTLE_TEXT);
        assertTrue(constructGraph.size() > 0);
    }

    @Test
    public void sparqlEndpointDescribeTurtle() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", TURTLE_TEXT);

        String query = "describe ?x where {?x ?p ?y} limit 1";
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query);
        HttpURLConnection con = getConnection(urlQuery, headers);

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

        assertEquals(status, 200);
        assertEquals(con.getContentType(), TURTLE_TEXT);
        assertTrue(describeGraph.size() > 0);
    }

    /**
     * Default graph in the HTTP protocol taken into account?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointOneDefaultGraph() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

        // Should only return 1 result <http://example.com/nothing>
        String query = "select DISTINCT ?x where { ?x ?p ?y } limit 10";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = getConnection(urlQuery, headers);

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
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

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
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = getConnection(urlQuery, headers);

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
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

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
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = getConnection(urlQuery, headers);

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
     * @see <a href="https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#dataset">SPARQL Protocol</a>
     * 
     * @throws Exception
     */
    @Test
    public void sparqlEndpointNamedGraphsAmbiguous() throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", SPARQL_RESULTS_XML);

        // Should only return ex:A that is the only named graph in the protocol. Should not returns ex:B specified in the query.
        String query = "select DISTINCT ?x FROM NAMED <http://example.com/B> where { GRAPH ?g { ?x a ?type } } limit 20";
        List<List<String>> parameters = new ArrayList<>();
        parameters.add(new ArrayList<String>());
        parameters.get(0).add("default-graph-uri");
        parameters.get(0).add("http://example.com/nothing");
        parameters.add(new ArrayList<String>());
        parameters.get(1).add("default-graph-uri");
        parameters.get(1).add("http://example.com/A");
        String urlQuery = SPARQL_ENDPOINT_URL + generateSPARQLQueryParameters(query, parameters);
        HttpURLConnection con = getConnection(urlQuery, headers);

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
