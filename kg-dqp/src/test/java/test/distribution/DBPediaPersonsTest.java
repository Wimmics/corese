package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import java.io.IOException;
import java.net.MalformedURLException;
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
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class DBPediaPersonsTest {

    String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + " OPTIONAL     {?x dbpedia:birthDate ?date }."
            + " FILTER ((?x ~ 'Bob') )"
            + "}";

    public DBPediaPersonsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();

        final String rep1 = "http://neurolog.unice.fr/~neurolog-dev/data/persondata.1.rdf";
        final String rep2 = "http://neurolog.unice.fr/~neurolog-dev/data/persondata.2.rdf";

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg1.loadRDF(rep1);

            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg2.loadRDF(rep2);

            }
        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    public void remoteDBPediaQueryRes() throws EngineException, MalformedURLException, IOException {

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://neurolog.unice.fr:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.unice.fr:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

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
    public void remoteDBPediaQueryPerf() throws EngineException, MalformedURLException, IOException {

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://neurolog.unice.fr:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.unice.fr:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlQuery);
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + map.size());
        
        assertTrue(time < 80000);
    }
}
