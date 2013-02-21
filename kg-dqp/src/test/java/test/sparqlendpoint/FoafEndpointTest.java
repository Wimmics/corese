/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.sparqlendpoint;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLEndpointClient;
import fr.inria.edelweiss.kgdqp.strategies.ServiceQueryVisitor;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class FoafEndpointTest {

    SPARQLEndpointClient endpoint1 = new SPARQLEndpointClient("http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint");
    SPARQLEndpointClient endpoint2 = new SPARQLEndpointClient("http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint");
    String init1 = "load <http://localhost:8888/rdf/persons1.rdf>";
    String init2 = "load <http://localhost:8888/rdf/persons2.rdf>";
    String init3 = "load <file:/Users/gaignard/Documents/These/ExperimentsG5K/large-scale-dbpedia-frag/1.7M/16-stores/persondata.1.rdf>";
    String init4 = "load <file:/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source.rdf>";
    String star = "select * where {?x ?p ?y}";
    String sparqlSampleQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + "SELECT distinct ?x ?z WHERE"
            + "{"
            + "?x foaf:knows ?y ."
            + "?y foaf:knows ?z ."
            //                + "FILTER(( ?u ~ 'a') && (?x ~ 'a'))"
            + "}";
    String sparqlSeqQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
            + "SELECT distinct ?y WHERE"
            + "{"
            //                + " <http://i3s/Alban> (foaf:knows/foaf:knows) ?y ."
            //                + " <http://i3s/Simon> (foaf:knows+) ?y ."
            //                + " <http://i3s/Alban> (foaf:knows+)/foaf:name ?y ."
            //                                + " <http://i3s/Mireille> ^foaf:knows ?y "
            //                + " <http://i3s/Alban> (foaf:knows+) ?y ."
            + " <http://i3s/Alban> (foaf:knows*) ?y ."
            //                + " <http://i3s/Alban> (foaf:knows?) ?y ."
            //                + " <http://i3s/Alban> foaf:knows{2,3} ?y ."
            //                + " <http://i3s/Alban> foaf:knows{2,3}/foaf:name ?y ."
            //                + " <http://i3s/Alban> (foaf:knows{3}) ?y ."
            //                + " <http://i3s/Alban> (foaf:knows{3,}) ?y ."
            //                + " <http://i3s/Alban> (foaf:knows{,2}) ?y ."
            //                                + " <http://i3s/Alban> (foaf:knows | foaf:knows/foaf:name){2,3} ?y ."
            //                                + " <http://i3s/Tram> (^foaf:knows)+ ?y ."
            //                                + " <http://i3s/Alban> ! (foaf:knows) ?y ." //OK
            //                + " ?x foaf:givenname 'Alban'^^xsd:string ." //OK
            //                + " <http://i3s/Alban> ! (foaf:knows | foaf:name | foaf:mbox) ?y ."
            //                                + " <http://i3s/Alban> ! (foaf:knows | foaf:name / foaf:mbox ) ?y ." 
            //                                + "FILTER( ?y ~ 'a')"
            + "}";
    String sparqlDBPediaQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //            + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //                + "     OPTIONAL {?x foaf:mbox ?m}"
            //            + " FILTER ((?name ~ 'Bob') )"
            + " FILTER ((?name ~ 'Bob') )"
            + "}";
    //linguistic-expression:has-for-name
    String sparqlHeterogeneousQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            + "     ?x dbpedia:birthDate ?date ."
            + "     ?y linguistic-expression:has-for-name ?z"
            + " FILTER ((?name ~ 'Bob'))"
            + " FILTER ((?z ~ 'T2'))"
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

    public FoafEndpointTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
//        endpoint1.doGet(init1);
//        endpoint2.doGet(init2);
//        endpoint1.doGet(init3);
//        endpoint2.doGet(init4);
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void hello() throws EngineException, MalformedURLException, IOException {

//        endpoint1.doGet(init1);
//        System.out.println("done "+init1);
//        endpoint2.doGet(init2);
//        System.out.println("done "+init2);
        endpoint1.doGet(init3);
        System.out.println("done " + init3);
        endpoint2.doGet(init4);
        System.out.println("done " + init4);

        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
//        execDQP.set(new ServiceQueryVisitor(execDQP));
//        ProviderImpl p = ProviderImpl.create();
//        execDQP.set(p);
        execDQP.addRemote(new URL("http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint"));
        execDQP.addRemote(new URL("http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint"));

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps = execDQP.query(sparqlHeterogeneousQuery);
        System.out.println("[no service grouping] Results size " + maps.size() + " in " + sw.getTime() + " ms");
//        System.out.println(XMLFormat.create(maps).toString());
        sw.stop();
        sw.reset();


        graph = Graph.create();
        execDQP = QueryProcessDQP.create(graph);
        execDQP.addVisitor(new ServiceQueryVisitor(execDQP));
        ProviderImpl p = ProviderImpl.create();
        execDQP.set(p);
        execDQP.addRemote(new URL("http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint"));
        execDQP.addRemote(new URL("http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint"));

        sw = new StopWatch();
        sw.start();
        maps = execDQP.query(sparqlHeterogeneousQuery);
        System.out.println("[service grouping] Results size " + maps.size() + " in " + sw.getTime() + " ms");
//        System.out.println(XMLFormat.create(maps).toString());
    }

    @Test
    @Ignore
    public void helloFed() throws EngineException, MalformedURLException, IOException {

//        endpoint1.doGet(init1);
//        System.out.println("done "+init1);
//        endpoint2.doGet(init2);
//        System.out.println("done "+init2);
        endpoint1.doGet(init3);
        System.out.println("done " + init3);
        endpoint2.doGet(init4);
        System.out.println("done " + init4);

        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
//        execDQP.set(new ServiceQueryVisitor(execDQP));
        ProviderImpl p = ProviderImpl.create();
        execDQP.set(p);
        execDQP.addRemote(new URL("http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint"));
        execDQP.addRemote(new URL("http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint"));

        StopWatch sw = new StopWatch();
        sw.start();

        Mappings maps = execDQP.query(sparqlHeterogeneousFedQuery);

        System.out.println("Results size " + maps.size() + " in " + sw.getTime() + " ms");
        System.out.println(XMLFormat.create(maps).toString());
    }

    @Test
    @Ignore
    public void ask() throws MalformedURLException, EngineException {
        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX cos: <http://www.inria.fr/acacia/corese#>"
                + "ask  { ?x foaf:givenname ?u } ";

        Graph graph = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
//        execDQP.set(new ServiceQueryVisitor(execDQP));
//        execDQP.addRemote(new URL("http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint"));
        execDQP.addRemote(new URL("http://localhost:8092/kgendpoint-1.0.7/KGSparqlEndpoint"));

        StopWatch sw = new StopWatch();
        sw.start();

        Mappings maps = execDQP.query(query);
        if (maps.size() == 0) {
            System.out.println("FALSE");
        } else {
            System.out.println("TRUE");
        }

//        XMLFormat res = XMLFormat.create(maps);
//        RDFFormat resZ = RDFFormat.create(maps);
//        System.out.println("");
//        System.out.println(res);
//        System.out.println("");
//        System.out.println(resZ);

    }
}
