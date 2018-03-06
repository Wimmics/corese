/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//import com.sun.xml.internal.ws.developer.JAXWSProperties;
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
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.time.StopWatch;
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

import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author gaignard
 */
public class RDFS_entailmentsTest {

    private static Logger logger = LogManager.getLogger(RDFS_entailmentsTest.class);
    private static int port = 9090;
    private static Server server;
    private static File humanData, humanOnt;
    String sparqlEntailQueryMan = "PREFIX humans: <http://www.inria.fr/2007/09/11/humans.rdfs#>  \n"
            + "SELECT DISTINCT ?x WHERE { "
            + " ?x rdf:type humans:Man "
            + "}";
    String sparqlEntailQueryPerson = "PREFIX humans: <http://www.inria.fr/2007/09/11/humans.rdfs#>  \n"
            + "SELECT DISTINCT ?x WHERE { "
            + " ?x rdf:type humans:Person "
            + "}";

    public RDFS_entailmentsTest() throws MalformedURLException {
    }

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUpClass() throws Exception {

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

        HandlerList handlers_s1 = new HandlerList();
        handlers_s1.setHandlers(new Handler[]{staticContextHandler, servletCtx});
        server.setHandler(handlers_s1);

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ///// Data extraction
        humanData = File.createTempFile("human", ".rdf");
        FileWriter fw = new FileWriter(humanData);
        InputStream is = RDFS_entailmentsTest.class.getClassLoader().getResourceAsStream("human_2007_09_11.rdf");
        int c;
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        humanOnt = File.createTempFile("humanOnt", ".rdfs");
        fw = new FileWriter(humanOnt);
        is = RDFS_entailmentsTest.class.getClassLoader().getResourceAsStream("human_2007_09_11.rdfs");
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        ///// Data upload
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:" + port + "/kgram"));

        //entailments
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("entailments", "true");
        service.path("sparql").path("reset").post(formData);

        formData = new MultivaluedMapImpl();
        formData.add("remote_path", humanOnt.getAbsolutePath());
        service.path("sparql").path("load").post(formData);

        formData = new MultivaluedMapImpl();
        formData.add("remote_path", humanData.getAbsolutePath());
        service.path("sparql").path("load").post(formData);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        humanData.delete();
        humanOnt.delete();
        
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void localEntailments() throws EngineException {
        Graph localGraph = Graph.create(true);
        Load ld = Load.create(localGraph);
        ld.load(humanData.getAbsolutePath());
        ld.load(humanOnt.getAbsolutePath());

        QueryProcess exec = QueryProcess.create(localGraph);
        StopWatch sw = new StopWatch();
        sw.start();
        Mappings res = exec.query(sparqlEntailQueryPerson);

        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        System.out.println(res);

        assertEquals(17, res.size());
    }

    @Test
    public void rdfsEntailQuery() throws EngineException, MalformedURLException, IOException {

        Graph gRes = Graph.create(false);
        QueryProcessDQP exec = QueryProcessDQP.create(gRes);
        exec.addRemote(new URL("http://localhost:" + port + "/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings res = exec.query(sparqlEntailQueryPerson);

        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        System.out.println(res);

        assertEquals(17, res.size());
    }
}
