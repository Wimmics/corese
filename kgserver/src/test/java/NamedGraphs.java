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
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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

import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class NamedGraphs {

    private static Logger logger = LogManager.getLogger(NamedGraphs.class);
    private static int port2 = 9082;
    private static Server server2;

    String q1 = "select distinct *  where { graph ?g {?x ?p ?y } }";
    String q2 = "select distinct * from named <http://graph/alice> where { graph ?g {?x ?p ?y } }";
    String q3 = "select distinct * from named <http://graph/bob> where { graph ?g {?x ?p ?y } }";
    String q4 = "select distinct * from named <http://graph/alice> from named <http://graph/bob> where { graph ?g {?x ?p ?y }}";
    String q5 = "select distinct *  where { graph <http://graph/alice> {?x ?p ?y } }";
    String q6 = "select distinct * from <http://graph/alice> where { ?x ?p ?y }";
    String q7 = "select distinct * from named <http://graph/alice> where { ?x ?p ?y }"; //does not make sense since no graph patterns are specified
    String q8 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> select distinct * from <http://graph/g1> where { <http://i3s/Alban> (foaf:knows/foaf:knows) ?y }";
    String q9 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> select distinct * from named <http://graph/g1> where { graph ?g {<http://i3s/Alban> (foaf:knows/foaf:knows) ?y} }";
    String q10 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> select distinct * from named <http://graph/g2> where { graph ?g {<http://i3s/Alban> (foaf:knows/foaf:knows) ?y} }";
    String q11 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> select distinct * where { <http://i3s/Alban> (foaf:knows/foaf:knows) ?y }";
    String q12 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> select distinct * from named <http://graph/g1> from named <http://graph/g2> where { graph ?g {<http://i3s/Alban> (foaf:knows/foaf:knows) ?y} }";

    public NamedGraphs() {
    }

    @BeforeClass
    public static void setUpClass() throws FileSystemException, URISyntaxException, IOException, InterruptedException {
        URI webappUri = EmbeddedJettyServer.extractResourceDir("webapp", true);
        server2 = new Server(port2);

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
        Context servletCtx = new Context(server2, "/kgram", Context.SESSIONS);
        servletCtx.addServlet(jerseyServletHolder, "/*");
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port2 + "/kgram");
        logger.info("----------------------------------------------");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase(webappUri.getRawPath());
        ContextHandler staticContextHandler = new ContextHandler();
        staticContextHandler.setContextPath("/");
        staticContextHandler.setHandler(resource_handler);
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port2);
        logger.info("----------------------------------------------");

        HandlerList handlers_s1 = new HandlerList();
        handlers_s1.setHandlers(new Handler[]{staticContextHandler, servletCtx});
        server2.setHandler(handlers_s1);

        try {
            server2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ///// Data extraction
        File f = File.createTempFile("ng-persons-1", ".ttl");
        FileWriter fw = new FileWriter(f);
        InputStream is = NamedGraphs.class.getClassLoader().getResourceAsStream("ng-persons-1.ttl");
        int c;
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        ///// Data extraction
        File f2 = File.createTempFile("kgram-persons", ".ttl");
        FileWriter fw2 = new FileWriter(f2);
        InputStream is2 = NamedGraphs.class.getClassLoader().getResourceAsStream("kgram-persons.ttl");
        int c2;
        while ((c2 = is2.read()) != -1) {
            fw2.write(c2);
        }
        is2.close();
        fw2.close();

        ///// Data upload
        ClientConfig config = new DefaultClientConfig(); 
        Client client2 = Client.create(config);
        WebResource service2 = client2.resource(new URI("http://localhost:" + port2 + "/kgram"));

        //entailments
        MultivaluedMap formData2 = new MultivaluedMapImpl();
        formData2.add("entailments", "false");
        service2.path("sparql").path("reset").post(formData2);

        formData2 = new MultivaluedMapImpl();
        formData2.add("remote_path", f.getAbsolutePath());
        service2.path("sparql").path("load").post(formData2);

        formData2 = new MultivaluedMapImpl();
        formData2.add("remote_path", f2.getAbsolutePath());
        service2.path("sparql").path("load").post(formData2);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server2 != null) {
            server2.stop();
            server2.destroy();
            server2 = null;
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void ngTest() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess qp = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.load(NamedGraphs.class.getClassLoader().getResourceAsStream("ng-persons-1.ttl"), "ng-persons-1.ttl");
        ld.load(NamedGraphs.class.getClassLoader().getResourceAsStream("kgram-persons.ttl"), "kgram-persons.ttl");

        Mappings maps = qp.query(q1);
        System.out.println(maps);
        assertEquals(22, maps.size());

        maps = qp.query(q2);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q3);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q4);
        System.out.println(maps);
        assertEquals(4, maps.size());

        maps = qp.query(q5);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q6);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q7);
        System.out.println(maps);
        assertEquals(22, maps.size());

        //Property path queries
        maps = qp.query(q8);
        System.out.println(maps);
        assertEquals(1, maps.size());

        maps = qp.query(q9);
        System.out.println(maps);
        assertEquals(1, maps.size());

        maps = qp.query(q10);
        System.out.println(maps);
        assertEquals(0, maps.size());

        maps = qp.query(q11);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q12);
        System.out.println(maps);
        assertEquals(1, maps.size());

    }

    @Test
    public void ngDqpTest() throws LoadException, EngineException, MalformedURLException {
        Graph g = Graph.create();
        QueryProcessDQP qp = QueryProcessDQP.create(g);
        qp.addRemote(new URL("http://localhost:" + port2 + "/kgram/sparql"), WSImplem.REST);

        Mappings maps = qp.query(q1);
        System.out.println(maps);
        assertEquals(22, maps.size());

        maps = qp.query(q2);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q3);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q4);
        System.out.println(maps);
        assertEquals(4, maps.size());

        maps = qp.query(q5);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q6);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q7);
        System.out.println(maps);
        assertEquals(22, maps.size());

        //Property path
        maps = qp.query(q8);
        System.out.println(maps);
        assertEquals(1, maps.size());

        maps = qp.query(q9);
        System.out.println(maps);
        assertEquals(1, maps.size());

        maps = qp.query(q10);
        System.out.println(maps);
        assertEquals(0, maps.size());

        maps = qp.query(q11);
        System.out.println(maps);
        assertEquals(2, maps.size());

        maps = qp.query(q12);
        System.out.println(maps);
        assertEquals(1, maps.size());
    }
}