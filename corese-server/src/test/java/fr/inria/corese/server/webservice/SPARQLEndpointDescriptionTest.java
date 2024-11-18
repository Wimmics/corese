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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import static fr.inria.corese.core.print.ResultFormat.RDF_XML;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static fr.inria.corese.core.api.Loader.RDFXML_FORMAT;
import static fr.inria.corese.core.api.Loader.TURTLE_FORMAT;

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

        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(RDF_XML);
        headers.add(contentTypeHeader);

        HttpURLConnection con = HTTPConnectionUtils.getConnection(sparqlEndpoint, headers);

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

        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(TURTLE_TEXT);
        headers.add(contentTypeHeader);

        HttpURLConnection con = HTTPConnectionUtils.getConnection(sparqlEndpoint, headers);

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

        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(RDF_XML);
        headers.add(contentTypeHeader);

        HttpURLConnection con = HTTPConnectionUtils.getConnection(sparqlEndpoint, headers);

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

        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add(TURTLE_TEXT);
        headers.add(contentTypeHeader);

        HttpURLConnection con = HTTPConnectionUtils.getConnection(sparqlEndpoint, headers);

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
