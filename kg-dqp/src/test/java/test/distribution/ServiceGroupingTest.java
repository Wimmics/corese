/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.ProviderWSImpl;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.strategies.ServiceQueryVisitorPar;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class ServiceGroupingTest {

    RemoteProducer endpoint1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
    RemoteProducer endpoint2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
    String path1 = "/Users/gaignard/Documents/These/ExperimentsG5K/large-scale-dbpedia-frag/1.7M/16-stores/persondata.1.rdf";
    String path2 = "/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source.rdf";
    String sparqlHeterogeneousQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + "     ?x dbpedia:birthDate ?date ."
            + "     ?y linguistic-expression:has-for-name ?z"
            + " FILTER ((?name ~ 'Bob'))"
//            + " FILTER ((?z ~ 'T2'))"
            + "}";
    String sparqlUnionQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
//            + "SELECT debug distinct ?x ?name ?date ?place WHERE \n"
            + "SELECT distinct ?x ?name ?date ?place WHERE \n"
            + "{"
            + "     {"
            + "         ?x rdf:type ?t"
            + "         ?x foaf:name ?name ."
            + "         ?x dbpedia:birthDate ?date ."
            + "     } UNION {"
            + "         ?y linguistic-expression:has-for-name ?z"
            + "     }"
//            + " OPTIONAL {?x dbpedia:birthPlace ?place}"
            + " FILTER ((?name ~ 'Bob'))"
//            + " FILTER ((?z ~ 'T2'))"
            + "}";
    String sparqlHeterogeneousFedQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     service <http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint> {"
            + "         ?x foaf:name ?name ."
            + "         ?x dbpedia:birthDate ?date ."
            + "     }"
            + "     service <http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint> {"
            + "         ?y linguistic-expression:has-for-name ?z"
            + "     }"
            + " FILTER ((?name ~ 'Bob') )"
            + "}";

    public ServiceGroupingTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
    }

    @After
    public void tearDown() {
    }

    @Test
//    @Ignore
    public void hello() throws EngineException, MalformedURLException, IOException {

        endpoint1.initEngine();
        endpoint2.initEngine();
        endpoint1.loadRDF(path1);
        System.out.println("done " + path1);
        endpoint2.loadRDF(path2);
        System.out.println("done " + path2);

        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
//        execDQP.set(new ServiceQueryVisitor(execDQP));
//        ProviderImpl p = ProviderImpl.create();
//        execDQP.set(p);
        execDQP.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        execDQP.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
//        Mappings maps = execDQP.query(sparqlHeterogeneousQuery);
//        Mappings maps = execDQP.query(sparqlUnionQuery);
//        System.out.println("[no service grouping] Results size " + maps.size() + " in " + sw.getTime() + " ms");
//        System.out.println(XMLFormat.create(maps).toString());
        sw.stop();
        sw.reset();

        graph = Graph.create();
        execDQP = QueryProcessDQP.create(graph);
        execDQP.addVisitor(new ServiceQueryVisitorPar(execDQP));
        ProviderWSImpl p = ProviderWSImpl.create();
        execDQP.set(p);
        execDQP.setOptimize(false);
        execDQP.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        execDQP.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        sw = new StopWatch();
        sw.start();
//        maps = execDQP.query(sparqlHeterogeneousQuery);
        Mappings maps = execDQP.query(sparqlUnionQuery);
//        Mappings maps = execDQP.query(sparqlHeterogeneousQuery);
        System.out.println("[service grouping] Results size " + maps.size() + " in " + sw.getTime() + " ms");
        
//        maps = execDQP.query(sparqlUnionQuery);
//        System.out.println("[service grouping] Results size " + maps.size() + " in " + sw.getTime() + " ms");
//        System.out.println(XMLFormat.create(maps).toString());
//        System.out.println(XMLFormat.create(maps));
        
    }
}
