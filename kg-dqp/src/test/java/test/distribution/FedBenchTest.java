/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.acacia.corese.exceptions.EngineException;
//import fr.inria.edelweiss.kgimport.JenaGraphFactory;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import java.io.*;
import java.net.MalformedURLException;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class FedBenchTest {

    public FedBenchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    @Ignore
    public void hello() throws IOException, EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        File dir = new File("/Users/gaignard/Desktop/FedBench-dataset");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".n3")) {
//                System.out.println("Loading " + f.getAbsolutePath());
//                FileInputStream fis = new FileInputStream(f);
//                Model model = ModelFactory.createDefaultModel();
//                model.read(fis, null, "N-TRIPLE");
//                System.out.println("Loaded " + f.getAbsolutePath());
//                Graph kgGraph = JenaGraphFactory.createGraph(model);
//                System.out.println("KG import successfull");
            } else if (f.getName().endsWith(".nt")) {
//                System.out.println("Loading " + f.getAbsolutePath());
//                FileInputStream fis = new FileInputStream(f);
//                Model model = ModelFactory.createDefaultModel();
//                model.read(fis, null, "N-TRIPLE");
//                System.out.println("Loaded " + f.getAbsolutePath());
//                Graph kgGraph = JenaGraphFactory.createGraph(model);
//                System.out.println("KG import successfull");
            }
        }


//        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
//                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
//                + "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"
//                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
//                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//                + "PREFIX cos: <http://www.inria.fr/acacia/corese#>"
//                + "ask  { ?x foaf:givenname ?u } ";
//
//        Mappings map = exec.query(query);
//        XMLFormat res = XMLFormat.create(map);
////        RDFFormat resZ = RDFFormat.create(map);
//        System.out.println("");
//        System.out.println(res);
////        System.out.println("");
////        System.out.println(resZ);
    }
    
//    @Test
//    @Ignore
//    public void loadingTest() throws MalformedURLException {
//        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//
//        kg1.initEngine();
//        kg2.initEngine();
//        
//        String path1 = "/Users/gaignard/Desktop/FedBench-dataset/chebi.n3";
//        String path2 = "/Users/gaignard/Desktop/FedBench-dataset/kegg.dr.n3";
//        
//        kg2.loadRDF(path2);
//        kg1.loadRDF(path1);
//        System.out.println("Load done");
//    }
    
}
