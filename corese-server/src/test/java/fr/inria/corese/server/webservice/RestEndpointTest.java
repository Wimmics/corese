package fr.inria.corese.server.webservice;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.apache.commons.vfs.FileSystemException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class RestEndpointTest {

    private static Logger logger = LogManager.getLogger(EmbeddedJettyServer.class);
    private static int port = 9080;
    private static Server server;

    public RestEndpointTest() {
    }

    @BeforeClass
    public static void setUpClass() throws FileSystemException, URISyntaxException, Exception {

        URI webappUri = EmbeddedJettyServer.extractResourceDir("webapp", true);
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages","rest");
        context.setContextPath("/");
        context.setServer(server);

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.corese.server.webservice");



        Context servletCtx = new Context(server, "/kgram", Context.SESSIONS);
        servletCtx.addServlet(jerseyServletHolder, "/*");
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/kgram");
        logger.info("----------------------------------------------");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase(webappUri.getRawPath());
        ContextHandler staticContextHandler = new ContextHandler();
        staticContextHandler.setContextPath("/");
        staticContextHandler.setHandler(resource_handler);
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port);
        logger.info("----------------------------------------------");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{staticContextHandler, servletCtx});
        server.setHandler(handlers);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void query() throws URISyntaxException, MalformedURLException, IOException {

        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?p ?y WHERE"
                + "{"
                + "     ?x ?p ?y ."
                + "}"
                + "     LIMIT 10";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(target.path("sparql").path("reset").request().post(Entity.text(null), String.class).toString());

        target
                .queryParam("remote_path", "http://nyx.unice.fr/~gaignard/data/neurolog.rdf")
                .path("sparql").path("load").request().post(Entity.text(null));
//        formData.add("remote_path", "/Users/gaignard/Desktop/bsbmtools-0.2/dataset.ttl");
        System.out.println(target.path("sparql").queryParam("query", query).request().accept("application/sparql-results+xml").get(String.class));
//        System.out.println(service.path("sparql").queryParam("query", query).accept("application/json").get(String.class));
    }

    @Test
    public void update() throws URISyntaxException, MalformedURLException, IOException {

        String insertData1 = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA\n"
                + "{ <http://example/book1> dc:title    \"First book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "}";
        String insertData2 = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA\n"
                + "{ <http://example/book3> dc:title    \"A new book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "}";
        String count = "SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(target.path("sparql").path("reset").request().post(Entity.text(null), String.class).toString());

        //First POST of the SPARQL protocol
        target
                .queryParam("update", insertData1)
                .path("sparql").path("update").request().post(Entity.text(null));
        System.out.println(target.path("sparql").queryParam("query", count).request("application/sparql-results+xml").get(String.class));

        //Second POST of the SPARQL protocol
        target.path("sparql").path("update").request("application/sparql-update").post(Entity.text(insertData2));
        System.out.println(target
                .path("sparql")
                .queryParam("query", count)
                .request()
                .accept("application/sparql-results+xml")
                .get(String.class));
    }

    @Test
    public void updateNG() throws URISyntaxException, MalformedURLException, IOException {

        String insertDataNG = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA  \n"
                + "{    GRAPH <http://firstStore> {"
                + "                         <http://example/book1> dc:title    \"First book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "     }"
                + "     GRAPH <http://secondStore> {"
                + "                         <http://example/book2> dc:title    \"Second book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "     }"
                + "}";

        String allWithGraph = "SELECT * WHERE {GRAPH ?g {?x ?p ?y}}";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(target.path("sparql").path("reset").request().post(Entity.text(null), String.class).toString());
        target.queryParam("entailments", "true");
        System.out.println(target.path("sparql").path("reset").request().post(Entity.text(null), String.class));


        //First POST of the SPARQL protocol
        target.path("sparql").path("update").request("application/x-www-form-urlencoded").post(Entity.text(null));
        System.out.println(target.path("sparql").queryParam("query", allWithGraph).request("application/sparql-results+xml").get(String.class));


        String selectBook = "SELECT * WHERE {GRAPH ?g {?x ?p ?y}}";
        System.out.println(target.path("sparql").queryParam("query", selectBook).queryParam("named-graph-uri", "http://secondStore").request("application/sparql-results+xml").get(String.class));
    }
}
