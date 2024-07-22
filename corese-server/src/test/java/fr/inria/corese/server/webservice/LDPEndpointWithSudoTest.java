package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.JSON_LD;
import static fr.inria.corese.core.print.ResultFormat.TURTLE;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 * Test of the behavior of the corese server against LDP requests.
 * 
 * @see <a href="https://www.w3.org/TR/ldp/">https://www.w3.org/TR/ldp/</a>
 * 
 * @author Pierre Maillot, P16 Wimmics INRIA I3S, 2024
 */
public class LDPEndpointWithSudoTest {
    

    private static final Logger logger = LogManager.getLogger(LDPEndpointWithSudoTest.class);

    private static Process server;

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String LDP_ENDPOINT = SERVER_URL + "ldp/";
    // private static final String LDP_ENDPOINT = "http://localhost:8890/";

    /**
     * Start the server before running the tests.
     * Loads a part of the DBpedia dataset in the server.
     */
    @BeforeClass
    public static void init() throws InterruptedException, IOException {

        logger.info("starting in " + System.getProperty("user.dir"));
        server = new ProcessBuilder().inheritIO().command(
                "java",
                "-jar", "./target/corese-server-4.5.1.jar",
                "-lh",
                "-su").start();
        Thread.sleep(6000);
    }

    @AfterClass
    public static void shutdown() {
        server.destroy();
    }


    @Test
    public void putJsonldContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putJSONLDContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#Container>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }"; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#Container> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#Container"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }


    @Test
    public void putJsonldBasicContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putJSONLDBasicContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }"; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#BasicContainer"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }

    @Test
    public void putJsonldDirectContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putJSONLDDirectContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }"; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#DirectContainer"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }

    @Test
    public void putJsonldIndirectContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putJSONLDIndirectContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }"; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#IndirectContainer"));
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
    }


    @Test
    public void putTurtleContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putTurtleDContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#Container>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestResource> ."; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#Container> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#Container"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }


    @Test
    public void putTurtleBasicContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putTurtleBasicContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestResource> ."; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#BasicContainer"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }

    @Test
    public void putTurtleDirectContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putTurtleDDirectContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestResource> ."; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#DirectContainer"));
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
    }

    @Test
    public void putTurtleIndirectContainer() throws Exception {
        String containerURI = LDP_ENDPOINT + "putTurtleIndirectContainer";

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestResource> ."; 
            
        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();


        String askQueryCheckContainerExists = "ASK { <" + containerURI + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201,status);
        assertNotNull(con.getHeaderField("Link"));
        assertTrue(con.getHeaderField("Link").contains("http://www.w3.org/ns/ldp#IndirectContainer"));
        assertNotNull(con.getHeaderField("Allow"));
        assertTrue(con.getHeaderField("Allow").contains("GET"));
        assertTrue(con.getHeaderField("Allow").contains("HEAD"));
        assertTrue(con.getHeaderField("Allow").contains("OPTIONS"));
        assertTrue(con.getHeaderField("Allow").contains("PUT"));
        assertTrue(con.getHeaderField("Allow").contains("POST"));
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheContainerContentInTheSparqlEndpoint);
    }
}
