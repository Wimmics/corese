package fr.inria.corese.server.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of the behavior of the corese server against SPARQL Updates.
 * 
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 * @see <a href="https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#update-operation">https://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#update-operation</a>
 
 */
public class SPARQLEndpointUpdateTest {
    

    private static final Logger logger = LogManager.getLogger(SPARQLEndpointUpdateTest.class);

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

        logger.info("starting in " + System.getProperty("user.dir"));
        server = new ProcessBuilder().inheritIO().command(
                "java",
                "-jar", "./target/corese-server-4.5.1.jar",
                "-lh",
                "-l", turtleFileAbsolutePath,
                "-l", trigFileAbsolutePath,
                "-su").start();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void shutdown() {
        server.destroy();
    }

    /**
     * Test the insertion of a triple in the server using a POST request with a URL-encoded body.
     * @throws Exception
     */
    @Test
    public void postUrlencodedUpdateTest() throws Exception {
        String query = "INSERT DATA { <http://example.org/s> <http://example.org/p> <http://example.org/o> }";
        String body = SPARQLTestUtils.generateSPARQLUpdateParameters(query);
        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/x-www-form-urlencoded");
        headers.add(contentTypeHeader);
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, body);
        int responseCode = con.getResponseCode();
        con.disconnect();

        assertEquals(200, responseCode);
    }

    /**
     * Test the insertion of a triple in the server using a POST request with a SPARQL Update body.
     * @throws Exception
     */
    @Test
    public void postUpdateTest() throws Exception {
        String query = "INSERT DATA { <http://example.org/s> <http://example.org/p> <http://example.org/o> }";
        List<List<String>> headers = new LinkedList<>();
        List<String> contentTypeHeader = new LinkedList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/sparql-update");
        headers.add(contentTypeHeader);
        HttpURLConnection con = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, headers, query);
        int responseCode = con.getResponseCode();
        con.disconnect();

        // Send a query to check if the instance was inserted
        String askQuery = "ASK { <http://example.org/s> <http://example.org/p> <http://example.org/o> }";
        boolean askResult = SPARQLTestUtils.sendSPARQLAsk(askQuery);

        assertTrue(responseCode >= 200 && responseCode < 400);
        assertTrue(askResult);
    }

    /**
     * Test the insertion of a triple in the server using a POST request with a URL-encoded body.
     * @throws Exception
     */
    @Test
    public void usingNamedGraphUpdateTest() throws Exception {
        // Insert a new instance in ex:A
        String updateQuery = "PREFIX owl: <http://www.w3.org/2002/07/owl#> INSERT { <http://example.com/Another> a owl:Thing } WHERE { <http://example.com/A> a owl:Thing }";
        List<List<String>> updateParameters = new LinkedList<>();
        List<String> graphParameter = new LinkedList<>();
        graphParameter.add("using-graph-uri");
        graphParameter.add("http://example.com/A");
        updateParameters.add(graphParameter);
        List<List<String>> updateHeaders = new LinkedList<>();
        List<String> contentTypeFormUrlEncodedHeader = new LinkedList<>();
        contentTypeFormUrlEncodedHeader.add("Content-Type");
        contentTypeFormUrlEncodedHeader.add("application/sparql-update");
        updateHeaders.add(contentTypeFormUrlEncodedHeader);
        HttpURLConnection updateCon = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, updateHeaders, updateQuery);
        int updateResponseCode = updateCon.getResponseCode();
        updateCon.disconnect();

        // Should be present in the dataset as it is loaded
        String askQueryABaseline = "PREFIX owl: <http://www.w3.org/2002/07/owl#> ASK { GRAPH <http://example.com/A> { <http://example.com/A> a owl:Thing } }";
        boolean askResultABaseline = SPARQLTestUtils.sendSPARQLAsk(askQueryABaseline);
        // Should have been created by the update
        String askQueryA = "PREFIX owl: <http://www.w3.org/2002/07/owl#> ASK { GRAPH <http://example.com/A> { <http://example.com/Another> a owl:Thing } }";
        boolean askResultA = SPARQLTestUtils.sendSPARQLAsk(askQueryA);
        // Should not be present in the dataset
        String askQueryB = "PREFIX owl: <http://www.w3.org/2002/07/owl#> ASK { GRAPH <http://example.com/B> { <http://example.com/Another> a owl:Thing } }";
        boolean askResultB = SPARQLTestUtils.sendSPARQLAsk(askQueryB);

        assertEquals(200, updateResponseCode);
        assertTrue(updateResponseCode >= 200 && updateResponseCode < 400);
        assertTrue(askResultABaseline);
        assertTrue(askResultA);
        assertFalse(askResultB);
    }
}
