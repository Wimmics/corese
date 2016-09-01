/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.vfs.FileSystemException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

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

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
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

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(service.path("sparql").path("reset").post(String.class).toString());

        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("remote_path", "http://nyx.unice.fr/~gaignard/data/neurolog.rdf");
//        formData.add("remote_path", "/Users/gaignard/Desktop/bsbmtools-0.2/dataset.ttl");
        service.path("sparql").path("load").post(formData);
        System.out.println(service.path("sparql").queryParam("query", query).accept("application/sparql-results+xml").get(String.class));
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

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(service.path("sparql").path("reset").post(String.class).toString());

        //First POST of the SPARQL protocol
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("update", insertData1);
        service.path("sparql").path("update").type("application/x-www-form-urlencoded").post(formData);
        System.out.println(service.path("sparql").queryParam("query", count).accept("application/sparql-results+xml").get(String.class));

        //Second POST of the SPARQL protocol
        service.path("sparql").path("update").type("application/sparql-update").entity(insertData2).post();
        System.out.println(service.path("sparql").queryParam("query", count).accept("application/sparql-results+xml").get(String.class));
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

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:" + RestEndpointTest.port + "/kgram"));

        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
        MultivaluedMap resetParams = new MultivaluedMapImpl();
        resetParams.add("entailments", "true");
        System.out.println(service.path("sparql").path("reset").post(String.class, resetParams));


        //First POST of the SPARQL protocol
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("update", insertDataNG);
        service.path("sparql").path("update").type("application/x-www-form-urlencoded").post(formData);
        System.out.println(service.path("sparql").queryParam("query", allWithGraph).accept("application/sparql-results+xml").get(String.class));


        String selectBook = "SELECT * WHERE {GRAPH ?g {?x ?p ?y}}";
        System.out.println(service.path("sparql").queryParam("query", selectBook).queryParam("named-graph-uri", "http://secondStore").accept("application/sparql-results+xml").get(String.class));
    }
}
