/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.ProviderWSImpl;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLEndpointClient;
import fr.inria.edelweiss.kgdqp.strategies.ServiceQueryVisitorPar;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import static org.junit.Assert.*;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
@Ignore
public class ServiceGroupingTest {

    final int nloop = 5;
    final int expectedResults = 53;
    static final String host = "localhost";
//    static final String host = "nyx.unice.fr";
    static String path1 = "/Users/gaignard/Documents/These/ExperimentsG5K/large-scale-dbpedia-frag/1.7M/16-stores/persondata.1.rdf";
    static String path2 = "/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source.rdf";
//    static String init1 = "load <file:" + path1 + ">";
//    static String init2 = "load <file:" + path2 + ">";
//    static String path1 = "http://nyx.unice.fr/~gaignard/data/persondata.1.rdf";
//    static String path2 = "http://nyx.unice.fr/~gaignard/data/neurolog.rdf";
    static String init1 = "load <" + path1 + ">";
    static String init2 = "load <" + path2 + ">";
    static String sparqlHeterogeneousQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{\n"
            + "     ?x foaf:name ?name .\n"
            + "     ?x dbpedia:birthDate ?date .\n"
            + "     ?y linguistic-expression:has-for-name ?z\n"
            + " FILTER ((?name ~ 'Bob'))\n"
            + "}"
            + " ORDER BY ?x" ;
            //            + " FILTER ((?z ~ 'T2'))"
//    static String sparqlUnionQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
//            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
//            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
//            //            + "SELECT debug distinct ?x ?name ?date ?place WHERE \n"
//            + "SELECT distinct ?x ?name ?date ?place WHERE \n"
//            + "{"
//            + "     {"
//            + "         ?x rdf:type ?t"
//            + "         ?x foaf:name ?name ."
//            + "         ?x dbpedia:birthDate ?date ."
//            + "     } UNION {"
//            + "         ?y linguistic-expression:has-for-name ?z"
//            + "     }"
//            //            + " OPTIONAL {?x dbpedia:birthPlace ?place}"
//            + " FILTER ((?name ~ 'Bob'))"
//            //            + " FILTER ((?z ~ 'T2'))"
//            + "}";
    static String sparqlHeterogeneousFedQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{\n"
            + "     service <http://" + host + ":8091/kgendpoint-1.0.7/KGSparqlEndpoint> {\n"
            + "         ?x foaf:name ?name .\n"
            + "         ?x dbpedia:birthDate ?date .\n"
            //            + " FILTER ((?name ~ 'Bob') )"
            + "     }\n"
            + "     service <http://" + host + ":8092/kgendpoint-1.0.7/KGSparqlEndpoint> {\n"
            + "         ?y linguistic-expression:has-for-name ?z\n"
            + "     }\n"
            + " FILTER ((?name ~ 'Bob') )\n"
            + "}";
    static String sparqlHeterogeneousFedWSQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{\n"
            + "     service <http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort> {\n"
            + "         ?x foaf:name ?name .\n"
            + "         ?x dbpedia:birthDate ?date .\n"
            //            + " FILTER ((?name ~ 'Bob') )"
            + "     }\n"
            + "     service <http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort> {\n"
            + "         ?y linguistic-expression:has-for-name ?z\n"
            + "     }\n"
            + " FILTER ((?name ~ 'Bob') )\n"
            + "}";
    static String sparqlHeterogeneousFedRESTQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{\n"
            + "     service <http://" + host + ":8091/kgserver-1.0.7-kgram-webservice> {\n"
            + "         ?x foaf:name ?name .\n"
            + "         ?x dbpedia:birthDate ?date .\n"
            //            + " FILTER ((?name ~ 'Bob') )"
            + "     }\n"
            + "     service <http://" + host + ":8092/kgserver-1.0.7-kgram-webservice> {\n"
            + "         ?y linguistic-expression:has-for-name ?z\n"
            + "     }\n"
            + " FILTER ((?name ~ 'Bob') )\n"
            + "}";
    static SPARQLEndpointClient sparqlEndpoint1;
    static SPARQLEndpointClient sparqlEndpoint2;
    static RemoteProducer endpoint1;
    static RemoteProducer endpoint2;

    public ServiceGroupingTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        sparqlEndpoint1 = new SPARQLEndpointClient("http://" + host + ":8091/kgendpoint-1.0.7/KGSparqlEndpoint");
        sparqlEndpoint2 = new SPARQLEndpointClient("http://" + host + ":8092/kgendpoint-1.0.7/KGSparqlEndpoint");

        // SOAP Sparql endpoint
        endpoint1 = RemoteProducerServiceClient.getPort("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        endpoint2 = RemoteProducerServiceClient.getPort("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        // REST Sparql endpoint
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource restEndpoint1 = client.resource(new URI("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice"));
        WebResource restEndpoint2 = client.resource(new URI("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice"));

        endpoint1.initEngine();
        endpoint2.initEngine();
        restEndpoint1.path("sparql").path("reset").post(String.class).toString();
        restEndpoint2.path("sparql").path("reset").post(String.class).toString();

        sparqlEndpoint1.doGet(init1);
        System.out.println("done HTTP WS loading " + init1);
        sparqlEndpoint2.doGet(init2);
        System.out.println("done HTTP WS loading " + init2);
        endpoint1.loadRDF(path1);
        System.out.println("done SOAP WS loading " + path1);
        endpoint2.loadRDF(path2);
        System.out.println("done SOAP WS loading " + path2);
        restEndpoint1.path("sparql").path("load").queryParam("remote_path", path1).post(String.class);
        System.out.println("done REST WS loading " + path1);
        restEndpoint2.path("sparql").path("load").queryParam("remote_path", path2).post(String.class);
        System.out.println("done REST WS loading " + path2);
        System.out.println("");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void serviceFedQueryingHTTP() throws MalformedURLException, EngineException, IOException {
        System.out.println("===========================");
        System.out.println(sparqlHeterogeneousFedQuery);
        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
        ProviderImpl p = ProviderImpl.create();
        execDQP.set(p);
        
        ArrayList<Long> values = new ArrayList<Long>();
        for (int i = 0; i < nloop; i++) {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings maps = execDQP.query(sparqlHeterogeneousFedQuery);
            Long t = sw.getTime();
            values.add(t);
            System.out.println("[HTTP service querying] Results size " + maps.size() + " in " + t + " ms");
            sw.stop();
            sw.reset();

            assertEquals(expectedResults, maps.size());
        }
        System.out.println("[HTTP service querying] Mean time : "+average(values)+" ms");
        System.out.println("");
    }

    @Test
    @Ignore
    public void serviceFedQueryingSOAP() throws MalformedURLException, EngineException, IOException {
        System.out.println("===========================");
        System.out.println(sparqlHeterogeneousFedWSQuery);
        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
        ProviderWSImpl p = ProviderWSImpl.create(WSImplem.SOAP);
        execDQP.set(p);
//        execDQP.setOptimize(false);

        ArrayList<Long> values = new ArrayList<Long>();
        for (int i = 0; i < nloop; i++) {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings maps = execDQP.query(sparqlHeterogeneousFedWSQuery);
            Long t = sw.getTime();
            values.add(t);
            System.out.println("[SOAP service querying] Results size " + maps.size() + " in " + t + " ms");
            sw.stop();
            sw.reset();

            assertEquals(expectedResults, maps.size());
        }
        System.out.println("[SOAP service querying] Mean time : "+average(values)+" ms");
        System.out.println("");
    }

    @Test
    @Ignore
    public void serviceFedQueryingREST() throws MalformedURLException, EngineException, IOException {
        System.out.println("===========================");
        System.out.println(sparqlHeterogeneousFedRESTQuery);
        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
        ProviderWSImpl p = ProviderWSImpl.create(WSImplem.REST);
        execDQP.set(p);
//        execDQP.setOptimize(false);

        ArrayList<Long> values = new ArrayList<Long>();
        for (int i = 0; i < nloop; i++) {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings maps = execDQP.query(sparqlHeterogeneousFedRESTQuery);
            Long t = sw.getTime();
            values.add(t);
            System.out.println("[REST service querying] Results size " + maps.size() + " in " + t + " ms");
            sw.stop();
            sw.reset();
            assertEquals(expectedResults, maps.size());
        }
        System.out.println("[REST service querying] Mean time : "+average(values)+" ms");
        System.out.println("");
    }

    @Test
    @Ignore
    public void serviceGroupingOptimSOAP() throws EngineException, MalformedURLException, IOException {
        System.out.println("============== SOAP service grouping =============");
        System.out.println(sparqlHeterogeneousQuery);
        
        //---------------Service grouping-----------------------
        Graph g1 = Graph.create();
        QueryProcessDQP execDQP1 = QueryProcessDQP.create(g1);
        execDQP1.addVisitor(new ServiceQueryVisitorPar(execDQP1));
        ProviderWSImpl p = ProviderWSImpl.create(WSImplem.SOAP);
        execDQP1.set(p);
//        execDQP.setOptimize(false);
        execDQP1.addRemote(new URL("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        execDQP1.addRemote(new URL("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps1 = execDQP1.query(sparqlHeterogeneousQuery);
        System.out.println("[SOAP service grouping] Results size " + maps1.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
        assertEquals(expectedResults, maps1.size());

        //---------------No service grouping-----------------------
        Graph g2 = Graph.create();
        QueryProcessDQP execDQP2 = QueryProcessDQP.create(g2);
        execDQP2.addRemote(new URL("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        execDQP2.addRemote(new URL("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        sw = new StopWatch();
        sw.start();
        Mappings maps2 = execDQP2.query(sparqlHeterogeneousQuery);
        System.out.println("[SOAP no service grouping] Results size " + maps2.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
        assertEquals(expectedResults, maps2.size());
        System.out.println("");
    }

    @Test
    @Ignore
    public void serviceGroupingOptimREST() throws EngineException, MalformedURLException, IOException {
        System.out.println("============= REST service grouping ==============");
        System.out.println(sparqlHeterogeneousQuery);

        //---------------Service grouping-----------------------
        Graph g1 = Graph.create();
        QueryProcessDQP execDQP1 = QueryProcessDQP.create(g1);
        execDQP1.addVisitor(new ServiceQueryVisitorPar(execDQP1));
        ProviderWSImpl p = ProviderWSImpl.create(WSImplem.REST);
        execDQP1.set(p);
//        execDQP.setOptimize(false);
        execDQP1.addRemote(new URL("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/sparql"), WSImplem.REST);
        execDQP1.addRemote(new URL("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps1 = execDQP1.query(sparqlHeterogeneousQuery);
//        System.out.println(maps1);
        System.out.println("[REST service grouping] Results size " + maps1.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
        assertEquals(expectedResults, maps1.size());

        //---------------No service grouping-----------------------
        Graph g2 = Graph.create();
        QueryProcessDQP execDQP2 = QueryProcessDQP.create(g2);
        execDQP2.addRemote(new URL("http://" + host + ":8091/kgserver-1.0.7-kgram-webservice/sparql"), WSImplem.REST);
        execDQP2.addRemote(new URL("http://" + host + ":8092/kgserver-1.0.7-kgram-webservice/sparql"), WSImplem.REST);

        sw = new StopWatch();
        sw.start();
        Mappings maps2 = execDQP2.query(sparqlHeterogeneousQuery);
//        System.out.println(maps2);
        System.out.println("[REST no service grouping] Results size " + maps2.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
        assertEquals(61, maps2.size());
        System.out.println("");
    }
    
    public Float average(ArrayList<Long> values) {
        Float av = new Float(0);
        
        for(Long v : values) {
            av+=v;
        }
        av = av/values.size();
        return av;
    }
}
