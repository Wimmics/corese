package fr.inria.corese.command.programs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import picocli.CommandLine;

public class RemoteSparqlTest {

        // Picocli objects
        private RemoteSparql convert = new RemoteSparql();
        private CommandLine cmd = new CommandLine(convert);

        private StringWriter out = new StringWriter();
        private StringWriter err = new StringWriter();

        // WireMock objects
        private static WireMockServer wireMockServer;

        // Server informations
        private final String serverUrl = "http://localhost:8080/sparql";
        private final String graphUri = "http://example.orgraphUrig/graph";
        // Query
        private static final String querySelect = "SELECT * WHERE { ?s ?p ?o }";

        ////////////////
        // Before All //
        ////////////////

        @BeforeEach
        private void initializePicoCli() {
                PrintWriter out = new PrintWriter(this.out);
                PrintWriter err = new PrintWriter(this.err);
                cmd.setOut(out);
                cmd.setErr(err);
        }

        @BeforeAll
        private static void initializeWireMockServer() {
                wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080));

                wireMockServer.start();

                // Get
                wireMockServer.stubFor(get(urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withBody("this is a fake response")));

                // Post-UrlEncoded
                wireMockServer.stubFor(post(urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withBody("this is a fake response")));

                // Post-Direct
                wireMockServer.stubFor(post(urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .willReturn(aResponse()
                                                .withStatus(200)
                                                .withBody("this is a fake response")));

        }

        ///////////////
        // After All //
        ///////////////

        @AfterEach
        private void resetStreams() {
                wireMockServer.resetRequests();
                out.getBuffer().setLength(0);
                err.getBuffer().setLength(0);
        }

        @AfterAll
        private static void tearDown() {
                wireMockServer.stop();
                wireMockServer.shutdown();
        }

        ///////////
        // Utils //
        ///////////

        private static String encode(String value) {
                try {
                        // Encode the value using URLEncoder
                        String encodedValue = URLEncoder.encode(value, "UTF-8");
                        // Replace '+' with '%20'
                        return encodedValue.replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("UTF-8 encoding is not supported", e);
                }
        }

        ////////////////
        // Test Cases //
        ////////////////

        // Query via GET

        @Test
        public void getQueryTest() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "GET" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), getRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void getQueryTestDefaultGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "GET", "-d", graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), getRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .withQueryParam("default-graph-uri", equalTo(graphUri))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void getQueryTestNamedGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "GET", "-n", graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), getRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .withQueryParam("named-graph-uri", equalTo(graphUri))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void getQueryTestAcceptHeader() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "GET", "-a", "application/json" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), getRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .withHeader("Accept", equalTo("application/json")));
        }

        @Test
        public void getQueryTestMultipleHeaders() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "GET", "-H", "Accept: application/json",
                                "-H", "Authorization: Bearer 1234" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), getRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withQueryParam("query", equalTo(querySelect))
                                .withHeader("Accept", equalTo("application/json"))
                                .withHeader("Authorization", equalTo("Bearer 1234")));
        }

        // Query via POST URL-Encoded

        @Test
        public void postQueryUrlEncodedQueryTest() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Encoded" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .withRequestBody(equalTo("query=" + encode(querySelect)))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryUrlEncodedQueryTestDefaultGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Encoded", "-d",
                                graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .withRequestBody(equalTo("query=" + encode(querySelect)
                                                + "&default-graph-uri=" + encode(graphUri)))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryUrlEncodedQueryTestNamedGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Encoded", "-n",
                                graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .withRequestBody(equalTo("query=" + encode(querySelect)
                                                + "&named-graph-uri=" + encode(graphUri)))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryUrlEncodedQueryTestAcceptHeader() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Encoded", "-a",
                                "application/json" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .withRequestBody(equalTo("query=" + encode(querySelect)))
                                .withHeader("Accept", equalTo("application/json")));
        }

        @Test
        public void postQueryUrlEncodedQueryTestMultipleHeaders() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Encoded", "-H",
                                "Accept: application/json", "-H", "Authorization: Bearer 1234" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                                .withRequestBody(equalTo("query=" + encode(querySelect)))
                                .withHeader("Accept", equalTo("application/json"))
                                .withHeader("Authorization", equalTo("Bearer 1234")));
        }

        // Query via POST Directly

        @Test
        public void postQueryDirectQueryTest() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Direct" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .withRequestBody(equalTo(querySelect))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryDirectQueryTestDefaultGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Direct", "-d",
                                graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .withRequestBody(equalTo(querySelect))
                                .withQueryParam("default-graph-uri", equalTo(graphUri))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryDirectQueryTestNamedGraphUri() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Direct", "-n",
                                graphUri };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .withRequestBody(equalTo(querySelect))
                                .withQueryParam("named-graph-uri", equalTo(graphUri))
                                .withHeader("Accept", equalTo("text/csv")));
        }

        @Test
        public void postQueryDirectQueryTestAcceptHeader() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Direct", "-a",
                                "application/json" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .withRequestBody(equalTo(querySelect))
                                .withHeader("Accept", equalTo("application/json")));
        }

        @Test
        public void postQueryDirectQueryTestMultipleHeaders() {
                String[] args = { "-e", serverUrl, "-q", querySelect, "-m", "POST-Direct", "-H",
                                "Accept: application/json", "-H", "Authorization: Bearer 1234" };
                int exitCode = cmd.execute(args);

                // Asserts
                assertEquals(0, exitCode);
                verify(exactly(1), postRequestedFor(
                                urlPathEqualTo("/sparql"))
                                .withHeader("Content-Type", equalTo("application/sparql-query"))
                                .withRequestBody(equalTo(querySelect))
                                .withHeader("Accept", equalTo("application/json"))
                                .withHeader("Authorization", equalTo("Bearer 1234")));
        }
}
