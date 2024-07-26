package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.print.ResultFormat.JSON_LD;
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
                Thread.sleep(6000);
        }

        @AfterClass
        public static void shutdown() {
                server.destroy();
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
                String containerURI = LDP_ENDPOINT + "postJSONLDBasicContainer";

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
                String containerURI = LDP_ENDPOINT + "postJSONLDDirectContainer";

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
                String containerURI = LDP_ENDPOINT + "postJSONLDIndirectContainer";

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
                String containerURI = LDP_ENDPOINT + "postTurtleBasicContainer";

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
                String containerURI = LDP_ENDPOINT + "putTurtleDDirectContainer";

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
                String containerURI = LDP_ENDPOINT + "postTurtleDDirectContainer";

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
                String containerURI = LDP_ENDPOINT + "postTurtleIndirectContainer";

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
                String containerURI = LDP_ENDPOINT + "putTurtleResourceBasicContainer";
                String resourceURI = LDP_ENDPOINT + "putTurtleResourceBasicContainer/resource";
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
                String containerURI = LDP_ENDPOINT + "postTurtleResourceBasicContainer";
                String resourceURI = LDP_ENDPOINT + "postTurtleResourceBasicContainer/resource";
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
                String containerURI = LDP_ENDPOINT + "putTurtleResourceDirectContainerWithNoMembershipResource";
                String resourceURI = LDP_ENDPOINT + "putTurtleResourceDirectContainerWithNoMembershipResource/resource";
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
                String containerURI = LDP_ENDPOINT + "postTurtleResourceDirectContainerWithNoMembershipResource";
                String resourceURI = LDP_ENDPOINT
                                + "postTurtleResourceDirectContainerWithNoMembershipResource/resource";
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
                String containerURI = LDP_ENDPOINT + "putTurtleResourceDirectContainerWithMembershipResource";
                String resourceURI = LDP_ENDPOINT + "putTurtleResourceDirectContainerWithMembershipResource/resource";
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
                String containerURI = LDP_ENDPOINT + "postTurtleResourceDirectContainerWithMembershipResource";
                String resourceURI = LDP_ENDPOINT + "postTurtleResourceDirectContainerWithMembershipResource/resource";
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
                assertTrue(isTheContainerInTheSparqlEndpoint);
                assertTrue(isTheResourceInTheSparqlEndpoint);
                assertTrue(isTheResourceInContainerUsingMember);
                assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
        }

        @Test
        public void putTurtleResourceIndirectContainerWithMembershipResource() throws Exception {
                String containerURI = LDP_ENDPOINT + "putTurtleResourceIndirectContainerWithNoMembershipResource";
                String containerMembershipResourceURI = LDP_ENDPOINT + "putTurtleResourceIndirectContainerWithNoMembershipResource#membershipResource";
                String resourceURI = LDP_ENDPOINT
                                + "putTurtleResourceIndirectContainerWithNoMembershipResource/resource";
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
                                + "    <http://www.w3.org/ns/ldp#membershipResource> <" + containerMembershipResourceURI + "> ;"
                                + "    <http://www.w3.org/ns/ldp#insertedContentRelation> <http://corese.inria.fr/#inverseMemberProperty> .";
                String turtleResourceDescription = "<> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <http://te.st/1> . <> a <http://corese.inria.fr/#TestResource> . <> <http://corese.inria.fr/#inverseMemberProperty> <http://te.st/2> .";

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
                String askQueryResourceIsInContainerUsingMember = "ASK { <" + containerMembershipResourceURI + "> <http://corese.inria.fr/#memberProperty> <"
                                + resourceURI + "> ; <http://corese.inria.fr/#inverseMemberProperty> <" + resourceURI
                                + "1> ; <http://corese.inria.fr/#inverseMemberProperty> <" + resourceURI + "2> . }";
                String askQueryResourceIsInContainerUsingCustomMemberProperty = "ASK { <" + containerMembershipResourceURI + "> <http://corese.inria.fr/#memberProperty> <"
                                + resourceURI + "> ; <http://corese.inria.fr/#inverseMemberProperty> <" + resourceURI
                                + "1> ; <http://corese.inria.fr/#inverseMemberProperty> <" + resourceURI + "2> . }";
                boolean isTheResourceInContainerUsingMember = SPARQLTestUtils
                                .sendSPARQLAsk(askQueryResourceIsInContainerUsingMember);
                boolean isTheResourceInContainerUsingCustomMemberProperty = SPARQLTestUtils
                                .sendSPARQLAsk(askQueryResourceIsInContainerUsingCustomMemberProperty);

                Graph containerResourceDescription = SPARQLTestUtils
                                .sendSPARQLConstructDescribe("DESCRIBE <" + containerURI + ">");
                NTriplesFormat.create(containerResourceDescription).write(System.err);

                Graph membershipResourceDescription = SPARQLTestUtils
                                .sendSPARQLConstructDescribe("DESCRIBE <" + containerMembershipResourceURI + ">");
                NTriplesFormat.create(membershipResourceDescription).write(System.err);

                assertEquals(201, containerCreationStatus);
                assertEquals(201, resourceCreationStatus);
                assertTrue(isTheContainerInTheSparqlEndpoint);
                assertTrue(isTheResourceInTheSparqlEndpoint);
                assertTrue(isTheResourceInContainerUsingMember);
                assertTrue(isTheResourceInContainerUsingCustomMemberProperty);
        }

}
