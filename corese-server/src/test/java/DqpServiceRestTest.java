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
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramserver.webservice.EmbeddedJettyServer;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.ProviderImpl;
import fr.inria.corese.kgtool.load.LoadException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class DqpServiceRestTest {

    private String sparqlServiceQueryOK = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct * WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + "     FILTER regex(?name,'Bobby')"
            + "     SERVICE <http://localhost:" + port + "/kgram/sparql> {"
            + "         ?x rdf:type foaf:Person ."
            + "     }"
            + "}"
            + "PRAGMA {kg:service kg:slice 100}";
    
    private String sparqlServiceQueryKO = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct * WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + "     FILTER regex(?name,'Bobby')"
//            + "     SERVICE <http://localhost:9082/kgram/sparql> {"
            + "     SERVICE ?s {"
            + "         ?x rdf:type foaf:Person ."
            + "     }"
            + "}"
            + "VALUES ?s { 'http://localhost:" + port + "/kgram/sparql' } "
            + "PRAGMA {kg:service kg:slice 100}";
    
    private String sparqlDBPediaQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct * WHERE \n"
            + "{"
            + "         ?x foaf:name ?name ."
            + "         ?x rdf:type foaf:Person ."
            + "         FILTER regex(?name,'Bobby')"
            + "} ";
    
    private String sparqlServiceDBPediaQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct * WHERE \n"
            + "{"
            + "     SERVICE ?s {"
//            + "     SERVICE <http://fr.dbpedia.org/sparql> {"
            + "         ?x foaf:name ?name ."
            + "         ?x rdf:type foaf:Person ."
            + "     }"
            + "         FILTER regex(?name,'Bobby')"
            + "}"
            + "VALUES ?s { 'http://localhost:" + port + "/kgram/sparql' 'http://fr.dbpedia.org/sparql' } "
            + "PRAGMA {kg:service kg:slice 50}";
    
    private static Logger logger = LogManager.getLogger(DqpRestTest.class);
    
    private static int port = 9082;
    private static Server server;

    public DqpServiceRestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, IOException, InterruptedException {

        ///////////// Secondserver in that JVM
        URI webappUri1 = EmbeddedJettyServer.extractResourceDir("webapp", true);
        server = new Server(port);

        ServletHolder jerseyServletHolder_s1 = new ServletHolder(ServletContainer.class);
        jerseyServletHolder_s1.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder_s1.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
        Context servletCtx_s1 = new Context(server, "/kgram", Context.SESSIONS);
        servletCtx_s1.addServlet(jerseyServletHolder_s1, "/*");
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/kgram");
        logger.info("----------------------------------------------");

        ResourceHandler resource_handler_s1 = new ResourceHandler();
        resource_handler_s1.setWelcomeFiles(new String[]{"index.html"});
//        resource_handler_s1.setResourceBase("/Users/gaignard/Documents/Dev/svn-kgram/Dev/trunk/kgserver/src/main/resources/webapp");
        resource_handler_s1.setResourceBase(webappUri1.getRawPath());
        ContextHandler staticContextHandler_s1 = new ContextHandler();
        staticContextHandler_s1.setContextPath("/");
        staticContextHandler_s1.setHandler(resource_handler_s1);
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port);
        logger.info("----------------------------------------------");

        HandlerList handlers_s1 = new HandlerList();
        handlers_s1.setHandlers(new Handler[]{staticContextHandler_s1, servletCtx_s1});
        server.setHandler(handlers_s1);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ///// Data upload
        ClientConfig config = new DefaultClientConfig();
        Client client2 = Client.create(config);
        WebResource service2 = client2.resource(new URI("http://localhost:" + port + "/kgram"));
        System.out.println(service2.path("sparql").path("reset").post(String.class).toString());
        MultivaluedMap formData2 = new MultivaluedMapImpl();
        formData2.add("remote_path", "http://nyx.unice.fr/~gaignard/data/persondata.2.rdf");
        service2.path("sparql").path("load").post(formData2);

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
    public void JavaApiTest() throws MalformedURLException, EngineException {
        Graph graph = Graph.create();
//        ProviderImplV2 prov = ProviderImplV2.create();
        ProviderImpl prov = ProviderImpl.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph, prov);
        exec.addRemote(new URL("http://localhost:" + port + "/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlServiceQueryOK);
        int dqpSize = map.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        assertEquals(41, dqpSize);
    }
    
    @Test
    public void RestApiTest() throws MalformedURLException, EngineException, URISyntaxException, LoadException {
        ClientConfig config = new DefaultClientConfig();
        Client client1 = Client.create(config);
        WebResource serviceDQP = client1.resource(new URI("http://localhost:" + port + "/kgram/dqp"));

        MultivaluedMap formData2 = new MultivaluedMapImpl();
        formData2.add("endpointUrl", "http://localhost:" + port + "/kgram/sparql");
        serviceDQP.path("configureDatasources").post(formData2);

        String sparqlRes = serviceDQP.path("sparql").queryParam("query", sparqlServiceQueryOK).accept("application/sparql-results+xml").get(String.class);
        int nbResults = StringUtils.countMatches(sparqlRes,"<result>");
        System.out.println(nbResults+" results.");
        assertEquals(41, nbResults);
    }
    
    @Test
    @Ignore
    public void DBPediaAccessTest() throws MalformedURLException, EngineException {
        Graph graph = Graph.create();
//        ProviderImplV2 prov = ProviderImplV2.create();
        ProviderImpl prov = ProviderImpl.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph, prov);
        exec.addRemote(new URL("http://fr.dbpedia.org/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlDBPediaQuery);
        int dqpSize = map.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        assertEquals(96, dqpSize);
        
        sw.stop();
        sw.reset();
        sw.start();
        map = exec.query(sparqlServiceDBPediaQuery);
        dqpSize = map.size();
        System.out.println("--------");
        time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        assertEquals(99, dqpSize);
    }
}