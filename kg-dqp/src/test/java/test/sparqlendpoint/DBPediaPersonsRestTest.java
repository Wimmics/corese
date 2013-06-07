package test.sparqlendpoint;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author gaignard
 */
public class DBPediaPersonsRestTest {

    String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + " OPTIONAL     {?x dbpedia:birthDate ?date }."
            + " FILTER ((?x ~ 'Bob') )"
            + "}";

    public DBPediaPersonsRestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        final WebResource kg1 = client.resource(new URI("http://nyx.unice.fr:8091/kgserver-1.0.7-kgram-webservice"));
        final WebResource kg2 = client.resource(new URI("http://nyx.unice.fr:8092/kgserver-1.0.7-kgram-webservice"));

        final String rep1 = "http://nyx.unice.fr/~gaignard/data/persondata.1.rdf";
        final String rep2 = "http://nyx.unice.fr/~gaignard/data/persondata.2.rdf";

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
               System.out.println(kg1.path("sparql").path("load").queryParam("remote_path", rep1).post(String.class));
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println(kg2.path("sparql").path("load").queryParam("remote_path", rep2).post(String.class));

            }
        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException, URISyntaxException {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    @Ignore
    public void remoteDBPediaQueryRes() throws EngineException, MalformedURLException, IOException {

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://nyx.unice.fr:8091/kgserver-1.0.7-kgram-webservice"), WSImplem.REST);
        exec.addRemote(new URL("http://nyx.unice.fr:8092/kgserver-1.0.7-kgram-webservice"), WSImplem.REST);
        
//        exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
//        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlQuery);
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + map.size());
//        System.out.println(RDFFormat.create(map));
        
        assertEquals(318, map.size());
    }
    
    @Test
    @Ignore
    public void remoteDBPediaQueryPerf() throws EngineException, MalformedURLException, IOException {

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://nyx.unice.fr:8091/kgserver-1.0.7-kgram-webservice"), WSImplem.REST);
        exec.addRemote(new URL("http://nyx.unice.fr:8092/kgserver-1.0.7-kgram-webservice"), WSImplem.REST);
        
//        exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
//        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlQuery);
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + map.size());
        
        assertTrue(time < 8000);
    }
}
