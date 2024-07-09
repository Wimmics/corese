package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_XML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import fr.inria.corese.core.load.result.SPARQLResult;

import fr.inria.corese.kgram.core.Mappings;

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

        assertTrue(responseCode >= 200 && responseCode < 400);
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

        assertTrue(responseCode >= 200 && responseCode < 400);
    }

    /**
     * Test the insertion of a triple in the server using a POST request with a URL-encoded body.
     * @throws Exception
     */
    @Test
    public void usingNamedGraphUpdateTest() throws Exception {
        // Insert a new instance in ex:A
        String updateQuery = "PREFIX owl: <http://www.w3.org/2002/07/owl#> INSERT DATA { GRAPH ?g { <http://example.com/Another> a owl:Thing } }";
        List<List<String>> updateParameters = new LinkedList<>();
        List<String> graphParameter = new LinkedList<>();
        graphParameter.add("using-named-graph-uri");
        graphParameter.add("http://example.com/A");
        updateParameters.add(graphParameter);
        String body = SPARQLTestUtils.generateSPARQLUpdateParameters(updateQuery, updateParameters);
        List<List<String>> updateHeaders = new LinkedList<>();
        List<String> contentTypeFormUrlEncodedHeader = new LinkedList<>();
        contentTypeFormUrlEncodedHeader.add("Content-Type");
        contentTypeFormUrlEncodedHeader.add("application/x-www-form-urlencoded");
        updateHeaders.add(contentTypeFormUrlEncodedHeader);
        HttpURLConnection updateCon = SPARQLTestUtils.postConnection(SPARQL_ENDPOINT_URL, updateHeaders, body);
        int updateResponseCode = updateCon.getResponseCode();
        updateCon.disconnect();

        // Send a query to check if the instance was inserted
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String askQuery = "ASK {GRAPH ?g { <http://example.com/Another> a owl:Thing } }";
        String askUrlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(askQuery);
        HttpURLConnection askCon = SPARQLTestUtils.getConnection(askUrlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(askCon.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        int askStatus = askCon.getResponseCode();

        askCon.disconnect();

        Mappings queryResults = SPARQLResult.create().parseString(content.toString());

        assertTrue(updateResponseCode >= 200 && updateResponseCode < 400);
        assertEquals(askStatus, 200);
        assertEquals(askCon.getContentType(), SPARQL_RESULTS_XML);
        assertTrue(queryResults.size() > 0);
    }
}
