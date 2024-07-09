package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static fr.inria.corese.core.api.Loader.TURTLE_FORMAT;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
    
/**
 * Test of the behavior of the corese server against graph store protocol requests.
 * 
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 * @see <a href="https://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/">https://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/</a>
 
 */
public class GraphStoreProtocolTest {
    

    private static final Logger logger = LogManager.getLogger(GraphStoreProtocolTest.class);

    private static Process server;

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String GRAPH_STORE_ENDPOINT = SERVER_URL + "rdf-graph-store";

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

        logger.info("starting in " + System.getProperty("user.dir"));
        server = new ProcessBuilder().inheritIO().command(
                "java",
                "-jar", "./target/corese-server-4.5.1.jar",
                "-lh",
                "-su",
                "-l", trigFileAbsolutePath,
                "-l", turtleFileAbsolutePath).start();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void shutdown() {
        server.destroy();
    }

    @Test
    public void getGraphStoreProtocolWithGraph() throws Exception {

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String urlQuery = GRAPH_STORE_ENDPOINT + "?" + SPARQLTestUtils.generateGraphStoreParameters("http://example.com/A");
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
        assertEquals(1, describeGraph.size());
    }

    @Test
    public void getGraphStoreProtocolWithDefault() throws Exception{

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(TURTLE_TEXT);
        headers.add(acceptHeader);

        String urlQuery = GRAPH_STORE_ENDPOINT + "?" + SPARQLTestUtils.generateGraphStoreParameters("default");;
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
        assertEquals(171, describeGraph.size());
    }
}
