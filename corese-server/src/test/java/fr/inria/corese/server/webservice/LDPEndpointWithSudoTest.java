package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.JSON_LD;
import static fr.inria.corese.core.print.ResultFormat.TURTLE_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.NTriplesFormat;

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
        server = new ProcessBuilder().inheritIO().command(
                "java",
                "-jar", "./target/corese-server-4.5.1.jar",
                "-lh",
                "-su").start();
        Thread.sleep(7000);
    }

    @AfterClass
    public static void shutdown() {
        server.destroy();
    }

    @Test
    public void putJsonldBasicContainer() throws Exception {
        String testResourcePrefix = "putJsonldBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }";

        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postJsonldBasicContainer() throws Exception {
        String testResourcePrefix = "postJsonldBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }";

        HttpURLConnection con = HTTPTestUtils.postConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
        String testResourcePrefix = "putJsonldDirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "[ { \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestContainer\" ], \"http://www.w3.org/ns/ldp#hasMemberRelation\": [ { \"@id\": \"http://corese.inria.fr/#memberProperty\" } ], \"http://www.w3.org/ns/ldp#membershipResource\": [ { \"@id\": \""
                + containerURI + "\" } ] } ]";
        logger.info(jsonldContainerDescription);

        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postJsonldDirectContainer() throws Exception {
        String testResourcePrefix = "postJsonldDirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "[ { \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestContainer\" ], \"http://www.w3.org/ns/ldp#hasMemberRelation\": [ { \"@id\": \"http://corese.inria.fr/#memberProperty\" } ], \"http://www.w3.org/ns/ldp#membershipResource\": [ { \"@id\": \""
                + containerURI + "\" } ] } ]";

        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
        String testResourcePrefix = "putJsonldIndirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }";

        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postJsonldIndirectContainer() throws Exception {
        String testResourcePrefix = "postJsonldIndirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(JSON_LD);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String jsonldContainerDescription = "{ \"@id\": \"" + containerURI
                + "\", \"@type\": [ \"http://corese.inria.fr/#TestResource\" ] }";

        HttpURLConnection con = HTTPTestUtils.postConnection(containerURI, headers, jsonldContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void putTurtleBasicContainer() throws Exception {
        String testResourcePrefix = "putTurtleBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

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

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postTurtleBasicContainer() throws Exception {
        String testResourcePrefix = "postTurtleBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

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

        HttpURLConnection con = HTTPTestUtils.postConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#BasicContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
        String testResourcePrefix = "putTurtleDirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <> .";

        HttpURLConnection con = HTTPTestUtils.putConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postTurtleDirectContainer() throws Exception {
        String testResourcePrefix = "postTurtleDirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        headers.add(acceptHeader);
        headers.add(linkHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <> .";

        HttpURLConnection con = HTTPTestUtils.postConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#DirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
        String testResourcePrefix = "putTurtleIndirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

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

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void postTurtleIndirectContainer() throws Exception {
        String testResourcePrefix = "postTurtleIndirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;

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

        HttpURLConnection con = HTTPTestUtils.postConnection(containerURI, headers, turtleContainerDescription);
        con.connect();

        int status = con.getResponseCode();

        con.disconnect();

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://www.w3.org/ns/ldp#IndirectContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryCheckContainerContentExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheContainerContentInTheSparqlEndpoint = SPARQLTestUtils
                .sendSPARQLAsk(askQueryCheckContainerContentExists);
        assertEquals(201, status);
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
    public void putTurtleResourceBasicContainer() throws Exception {
        String testResourcePrefix = "putTurtleResourceBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainer = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> }";
        boolean isTheResourceInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        assertEquals(201, containerCreationStatus);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainer);
    }

    @Test
    public void postTurtleResourceBasicContainer() throws Exception {
        String testResourcePrefix = "postTurtleResourceBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.postConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.postConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainer = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> }";
        boolean isTheResourceInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        assertEquals(201, containerCreationStatus);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainer);
    }

    @Test
    public void putTurtleResourceDirectContainerWithNoMembershipResource() throws Exception {
        String testResourcePrefix = "putTurtleResourceDirectContainerWithNoMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> . }";
        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <" + containerURI
                + "> <http://corese.inria.fr/#memberProperty> <" + resourceURI + "> }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void postTurtleResourceDirectContainerWithNoMembershipResource() throws Exception {
        String testResourcePrefix = "postTurtleResourceDirectContainerWithNoMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.postConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.postConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> . }";
        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <" + containerURI
                + "> <http://corese.inria.fr/#memberProperty> <" + resourceURI + "> }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void putTurtleResourceDirectContainerWithMembershipResource() throws Exception {
        String testResourcePrefix = "putTurtleResourceDirectContainerWithMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <http://corese.inria.fr/#membershipResource> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainerUsingMember = "ASK { <http://corese.inria.fr/#membershipResource> <http://www.w3.org/ns/ldp#member> <"
                + resourceURI + "> . }";
        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <http://corese.inria.fr/#membershipResource> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "> }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void postTurtleResourceDirectContainerWithMembershipResource() throws Exception {
        String testResourcePrefix = "postTurtleResourceDirectContainerWithMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <http://corese.inria.fr/#membershipResource> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.postConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.postConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainerUsingMember = "ASK { <http://corese.inria.fr/#membershipResource> <http://www.w3.org/ns/ldp#member> <"
                + resourceURI + "> . }";
        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <http://corese.inria.fr/#membershipResource> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "> }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, resourceCreationStatus);
        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void putTurtleResourceIndirectContainerWithMembershipResource() throws Exception {
        String testResourcePrefix = "putTurtleResourceIndirectContainerWithMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String containerMembershipResourceURI = LDP_ENDPOINT + testResourcePrefix + "#membershipResource";
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <" + containerMembershipResourceURI
                + "> ;"
                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI
                + "1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI + "2> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> ; <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> . }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);

        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerMembershipResourceURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI
                + "1> ; <http://www.w3.org/ns/ldp#member> <" + resourceURI + "2> . }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);

        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <"
                + containerMembershipResourceURI + "> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "1> ; <http://corese.inria.fr/#memberProperty> <" + resourceURI
                + "2> . }";
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void postTurtleResourceIndirectContainerWithMembershipResource() throws Exception {
        String testResourcePrefix = "postTurtleResourceIndirectContainerWithMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String containerMembershipResourceURI = LDP_ENDPOINT + testResourcePrefix + "#membershipResource";
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <" + containerMembershipResourceURI
                + "> ;"
                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI
                + "1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI + "2> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.postConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.postConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> ; <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> . }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);

        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerMembershipResourceURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI
                + "1> ; <http://www.w3.org/ns/ldp#member> <" + resourceURI + "2> . }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);

        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <"
                + containerMembershipResourceURI + "> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "1> ; <http://corese.inria.fr/#memberProperty> <" + resourceURI
                + "2> . }";
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void putTurtleResourceIndirectContainerWithNoMembershipResource() throws Exception {
        String testResourcePrefix = "putTurtleResourceIndirectContainerWithNoMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI
                + "1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI + "2> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> ; <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> . }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);

        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI
                + "1> ; <http://www.w3.org/ns/ldp#member> <" + resourceURI + "2> . }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);

        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <"
                + containerURI + "> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "1> ; <http://corese.inria.fr/#memberProperty> <" + resourceURI
                + "2> . }";
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void postTurtleResourceIndirectContainerWithNoMembershipResource() throws Exception {
        String testResourcePrefix = "postTurtleResourceIndirectContainerWithNoMembershipResource";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI
                + "1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI + "2> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.postConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.postConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);

        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> ; <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> . }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);

        String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI
                + "1> ; <http://www.w3.org/ns/ldp#member> <" + resourceURI + "2> . }";
        boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);

        String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <"
                + containerURI + "> <http://corese.inria.fr/#memberProperty> <"
                + resourceURI + "1> ; <http://corese.inria.fr/#memberProperty> <" + resourceURI
                + "2> . }";
        boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainerUsingMember);
        assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
    }

    @Test
    public void deleteTurtleResourceInBasicContainer() throws Exception {
        String testResourcePrefix = "deleteTurtleResourceBasicContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainer = "ASK { <" + containerURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> }";
        boolean isTheResourceInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        HttpURLConnection resourceDeletionCon = HTTPTestUtils.deleteConnection(resourceURI);
        resourceDeletionCon.connect();
        int resourceDeletionStatus = resourceDeletionCon.getResponseCode();
        resourceDeletionCon.disconnect();

        boolean isTheResourceStillInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        Graph resourceDescription = SPARQLTestUtils.sendSPARQLConstructDescribe("DESCRIBE <" + resourceURI + ">");
        NTriplesFormat.create(resourceDescription).write(System.err);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainer);
        assertEquals(204, resourceDeletionStatus);
        assertFalse(isTheResourceStillInContainer);
    }

    @Test
    public void deleteTurtleResourceInDirectContainer() throws Exception {
        String testResourcePrefix = "deleteTurtleResourceInDirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#DirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <http://corese.inria.fr/#membershipResource> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        String askQueryCheckResourceExists = "ASK { <" + resourceURI
                + "> a <http://corese.inria.fr/#TestResource> }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainer = "ASK { <http://corese.inria.fr/#membershipResource> <http://www.w3.org/ns/ldp#member> <" + resourceURI + "> ; <http://corese.inria.fr/#memberProperty> <" + resourceURI + "> . }";
        boolean isTheResourceInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        HttpURLConnection resourceDeletionCon = HTTPTestUtils.deleteConnection(resourceURI);
        resourceDeletionCon.connect();
        int resourceDeletionStatus = resourceDeletionCon.getResponseCode();
        resourceDeletionCon.disconnect();
        
        boolean isTheResourceStillInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainer);
        assertEquals(204, resourceDeletionStatus);
        assertFalse(isTheResourceStillInContainer);
    }

    @Test
    public void deleteTurtleResourceInIndirectContainer() throws Exception {
        String testResourcePrefix = "deleteTurtleResourceInIndirectContainer";
        String containerURI = LDP_ENDPOINT + testResourcePrefix;
        String resourceURI = LDP_ENDPOINT + testResourcePrefix + "/resource";
        String containerMembershipResourceURI = LDP_ENDPOINT + testResourcePrefix + "#membershipResource";
        List<List<String>> containerCreationHeaders = new LinkedList<>();
        List<List<String>> resourceCreationHeaders = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Content-Type");
        acceptHeader.add(TURTLE_TEXT);
        List<String> linkHeader = new LinkedList<>();
        linkHeader.add("Link");
        linkHeader.add("<http://www.w3.org/ns/ldp#IndirectContainer>; rel=\"type\"");
        List<String> slugHeader = new LinkedList<>();
        slugHeader.add("Slug");
        slugHeader.add("resource");
        containerCreationHeaders.add(acceptHeader);
        containerCreationHeaders.add(linkHeader);
        resourceCreationHeaders.add(acceptHeader);
        resourceCreationHeaders.add(slugHeader);

        String turtleContainerDescription = "<> a <http://corese.inria.fr/#TestContainer> ;"
                + "    <http://www.w3.org/ns/ldp#hasMemberRelation> <http://corese.inria.fr/#memberProperty> ;"
                + "    <http://www.w3.org/ns/ldp#membershipResource> <" + containerMembershipResourceURI
                + "> ;"
                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
        String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI
                + "1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <"
                + resourceURI + "2> .";

        HttpURLConnection containerCreationCon = HTTPTestUtils.putConnection(containerURI,
                containerCreationHeaders, turtleContainerDescription);
        containerCreationCon.connect();
        int containerCreationStatus = containerCreationCon.getResponseCode();
        containerCreationCon.disconnect();

        HttpURLConnection resourceCreationCon = HTTPTestUtils.putConnection(containerURI,
                resourceCreationHeaders, turtleResourceDescription);
        resourceCreationCon.connect();
        int resourceCreationStatus = resourceCreationCon.getResponseCode();
        resourceCreationCon.disconnect();

        Graph resourceDescriptionBefore = SPARQLTestUtils.sendSPARQLConstructDescribe("DESCRIBE <" + resourceURI + "1>");
        NTriplesFormat.create(resourceDescriptionBefore).write(System.err);

        String askQueryCheckResourceExists = "ASK { { <" + resourceURI
                + "2> ?p ?o . } UNION { ?s ?p <" + resourceURI + "2> } { <" + resourceURI
                + "1> ?p ?o . } UNION { ?s ?p <" + resourceURI + "1> } }";
        boolean isTheResourceInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckResourceExists);
        String askQueryCheckContainerExists = "ASK { <" + containerURI
                + "> a <http://corese.inria.fr/#TestContainer> }";
        boolean isTheContainerInTheSparqlEndpoint = SPARQLTestUtils.sendSPARQLAsk(askQueryCheckContainerExists);
        String askQueryResourceIsInContainer = "ASK { <" + containerMembershipResourceURI
                + "> <http://www.w3.org/ns/ldp#member> <" + resourceURI
                + "1> ; <http://www.w3.org/ns/ldp#member> <" + resourceURI + "2> . }";
        boolean isTheResourceInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        HttpURLConnection resourceDeletionCon = HTTPTestUtils.deleteConnection(resourceURI);
        resourceDeletionCon.connect();
        int resourceDeletionStatus = resourceDeletionCon.getResponseCode();
        resourceDeletionCon.disconnect();
        
        boolean isTheResourceStillInContainer = SPARQLTestUtils.sendSPARQLAsk(askQueryResourceIsInContainer);

        Graph resourceDescriptionAfter = SPARQLTestUtils.sendSPARQLConstructDescribe("DESCRIBE <" + resourceURI + "1>");
        NTriplesFormat.create(resourceDescriptionAfter).write(System.err);

        assertEquals(201, containerCreationStatus);
        assertTrue(isTheContainerInTheSparqlEndpoint);
        assertEquals(201, resourceCreationStatus);
        assertTrue(isTheResourceInTheSparqlEndpoint);
        assertTrue(isTheResourceInContainer);
        assertEquals(204, resourceDeletionStatus);
        assertFalse(isTheResourceStillInContainer);
    }

}
