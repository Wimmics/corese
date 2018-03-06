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
import fr.inria.corese.kgtool.load.LoadException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.core.MultivaluedMap;
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

/**
 *
 * @author gaignard
 */
public class FoafPathTest {

    private static Logger logger = LogManager.getLogger(FoafPathTest.class);
    private static int port = 9090;
    private static Server server;
    private static File p1, p2;

    public FoafPathTest() throws MalformedURLException {
    }

    @BeforeClass
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
        p1 = File.createTempFile("persons1", ".rdf");
        FileWriter fw = new FileWriter(p1);
        InputStream is = FoafPathTest.class.getClassLoader().getResourceAsStream("kgram1-persons.rdf");
        int c;
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        p2 = File.createTempFile("persons2", ".rdf");
        fw = new FileWriter(p2);
        is = FoafPathTest.class.getClassLoader().getResourceAsStream("kgram2-persons.rdf");
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
        formData.add("entailments", "false");
        service.path("sparql").path("reset").post(formData);

        formData = new MultivaluedMapImpl();
        formData.add("remote_path", p2.getAbsolutePath());
        service.path("sparql").path("load").post(formData);

        formData = new MultivaluedMapImpl();
        formData.add("remote_path", p1.getAbsolutePath());
        service.path("sparql").path("load").post(formData);

        p1.delete();
        p2.delete();
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
    public void setUp() throws EngineException, MalformedURLException, IOException {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void remoteFoafQuery() throws EngineException, MalformedURLException, IOException, LoadException {

        String sparqlSeqQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?y WHERE"
                + "{"
                //                                + " <http://i3s/Alban> (foaf:knows/foaf:knows) ?y ."
                + " <http://i3s/Simon> (foaf:knows+) ?y ."
                //                                + " <http://i3s/Alban> (foaf:knows+)/foaf:name ?y ."
                //                                                + " <http://i3s/Mireille> ^foaf:knows ?y "
                //                                + " <http://i3s/Alban> (foaf:knows+) ?y ."
                //                                + " <http://i3s/Alban> (foaf:knows*) ?y ."
                //                + " <http://i3s/Alban> (foaf:knows?) ?y ."
                //                + " <http://i3s/Alban> foaf:knows{2,3} ?y ."
                //                + " <http://i3s/Alban> foaf:knows{2,3}/foaf:name ?y ."
                //                + " <http://i3s/Alban> (foaf:knows{3}) ?y ."
                //                + " <http://i3s/Alban> (foaf:knows{3,}) ?y ."
                //                                + " <http://i3s/Alban> (foaf:knows{,2}) ?y ."
                //                                + " <http://i3s/Alban> (foaf:knows | foaf:knows/foaf:name){2,3} ?y ."
                //                                + " <http://i3s/Tram> (^foaf:knows)+ ?y ."
                //                                + " <http://i3s/Alban> ! (foaf:knows) ?y ." //OK
                //                + " ?x foaf:givenname 'Alban'^^xsd:string ." //OK
                //                + " <http://i3s/Alban> ! (foaf:knows | foaf:name | foaf:mbox) ?y ."
                //                                + " <http://i3s/Alban> ! (foaf:knows | foaf:name / foaf:mbox ) ?y ." 
                //                                + "FILTER( ?y ~ 'a')"
                + "}";

        String pathQuery1 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct * WHERE {"
                + "   <http://i3s/Alban> (foaf:knows/foaf:knows) ?y ."
                + "}";
        
        String pathQuery2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct * WHERE {"
                + "   <http://i3s/Simon> (foaf:knows+) ?y ."
                + "}";
        
        String pathQuery3 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct * WHERE {"
                + "   <http://i3s/Alban> (foaf:knows | foaf:knows/foaf:name){2,3} ?y ."
                + "}";

        

        Graph gDqp = Graph.create();
        QueryProcessDQP execDqp = QueryProcessDQP.create(gDqp);
        execDqp.addRemote(new URL("http://localhost:" + port + "/kgram/sparql"), WSImplem.REST);
        
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(FoafPathTest.class.getClassLoader().getResourceAsStream("kgram1-persons.rdf"));
        ld.load(FoafPathTest.class.getClassLoader().getResourceAsStream("kgram2-persons.rdf"));
        QueryProcess exec = QueryProcess.create(g);
        
        
        
        
        Mappings res = exec.query(pathQuery1);
        System.out.println(res);
        assertEquals(1,res.size());
        Mappings resDqp = execDqp.query(pathQuery1);
        System.out.println(resDqp);
        assertEquals(res.size(),resDqp.size());
        
        res = exec.query(pathQuery2);
        System.out.println(res);
        assertEquals(2,res.size());
        resDqp = execDqp.query(pathQuery2);
        System.out.println(resDqp);
        assertEquals(res.size(),resDqp.size());
        
        res = exec.query(pathQuery3);
        System.out.println(res);
        assertEquals(4,res.size());
        resDqp = execDqp.query(pathQuery3);
        System.out.println(resDqp);
        assertEquals(res.size(),resDqp.size());

    }
}
