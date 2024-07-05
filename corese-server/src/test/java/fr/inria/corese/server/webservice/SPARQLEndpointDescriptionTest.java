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
public class SPARQLEndpointDescriptionTest {

    private static final Logger logger = LogManager.getLogger(SPARQLEndpointDescriptionTest.class);

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
     * Is there an RDF document with a description of the SPARQL endpoint available
     * at /.well-known/void?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlWellKnownVoidXMLRDF() throws Exception {
        String sparqlEndpoint = SPARQL_ENDPOINT_URL + "/.well-known/void";

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", RDF_XML);

        HttpURLConnection con = getConnection(sparqlEndpoint, headers);

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

        Graph voidGraph = new Graph();
        Load load = Load.create(voidGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(voidGraph.size() > 0);
    }

    /**
     * Is there an RDF document with a description of the SPARQL endpoint available
     * at /.well-known/void?
     * 
     * @throws Exception
     */
    @Test
    public void sparqlWellKnownVoidTurtleRDF() throws Exception {
        String sparqlEndpoint = SPARQL_ENDPOINT_URL + "/.well-known/void";

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", TURTLE_TEXT);

        HttpURLConnection con = getConnection(sparqlEndpoint, headers);

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

        Graph voidGraph = new Graph();
        Load load = Load.create(voidGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(voidGraph.size() > 0);
    }

    /**
     * Is there an RDF document with a description of the SPARQL endpoint available
     * at /.well-known/void?
     * 
     * @throws Exception
     */
    @Test
    public void wellKnownVoidXMLRDF() throws Exception {
        String sparqlEndpoint = SERVER_URL + ".well-known/void";

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", RDF_XML);

        HttpURLConnection con = getConnection(sparqlEndpoint, headers);

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

        Graph voidGraph = new Graph();
        Load load = Load.create(voidGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(voidGraph.size() > 0);
    }

    /**
     * Is there an RDF document with a description of the SPARQL endpoint available
     * at /.well-known/void?
     * 
     * @throws Exception
     */
    @Test
    public void wellKnownVoidTurtleRDF() throws Exception {
        String sparqlEndpoint = SERVER_URL + ".well-known/void";

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", TURTLE_TEXT);

        HttpURLConnection con = getConnection(sparqlEndpoint, headers);

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

        Graph voidGraph = new Graph();
        Load load = Load.create(voidGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, TURTLE_FORMAT);

        assertEquals(status, 200);
        assertEquals(con.getContentType(), RDF_XML);
        assertTrue(voidGraph.size() > 0);
    }
}
