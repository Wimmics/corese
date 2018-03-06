/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.RDFFormat;
import fr.inria.corese.kgtool.print.XMLFormat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class VipRuleOPM_v2 {

    String queryDescFantom = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX opmo: <http://openprovenance.org/model/opmo#>"
            + "PREFIX opmv: <http://purl.org/net/opmv/ns#>"
            + "PREFIX vip: <http://www.i3s.unice.fr/modalis/vip#>"
            + "SELECT * WHERE { "
            + "     ?out vip:has-for-fantom ?in"
            + "     ?out (opmo:avalue/opmo:content) ?cOut ."
            + "     ?in (opmo:avalue/opmo:content) ?cIn ."
            + "}";
    String queryRuleFantom = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
            + "PREFIX opmo: <http://openprovenance.org/model/opmo#>"
            + "PREFIX opmv: <http://purl.org/net/opmv/ns#>"
            + "PREFIX vip: <http://www.i3s.unice.fr/modalis/vip#>"
            + "PREFIX ws: <http://www.irisa.fr/visages/team/farooq/ontologies/web-service-owl-lite.owl#>"
            + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
            + "CONSTRUCT { "
            + "     ?out vip:has-for-fantom ?in"
            + "} \n"
//            + "SELECT distinct ?x ?out ?in ?cIn ?role ?tech "
            + "WHERE { "
            + "     ?agent (iec:refers-to/rdf:type) <http://vip.cosinus.anr.vip.fr/vip-simulation.owl#image-reconstruction-simulator-component>"
            + "     ?wcb opmo:cause ?agent ."
            + "     ?wcb opmo:effect ?x  ."
            + "     ?x rdf:type opmv:Process ."
            + "     ?wgb opmo:cause ?x ."
            + "     ?wgb opmo:effect ?out ."
//                        + "     ?out (opmo:avalue/opmo:content) ?cOut ."
            //            + ""
            + "     ?agent2 (iec:refers-to/rdf:type) <http://vip.cosinus.anr.vip.fr/vip-simulation.owl#parameters-generation-simulator-component> ."
            + "     ?wcb2 opmo:cause ?agent2 ."
            + "     ?wcb2 opmo:effect ?y  ."
            + "     ?y rdf:type opmv:Process ."
            + "     ?used opmo:cause ?in ."
            + "     ?used opmo:effect ?y ."
//            + "     ?in (opmo:avalue/opmo:content) ?cIn ."
            + "     ?used opmo:role/rdfs:label ?techRole ."
            
            + "     ?agent2 ws:has-input ?inPort"
            + "     ?inPort (iec:refers-to/rdf:type) <http://vip.cosinus.anr.fr/vip-model.owl#geometrical-phantom-object-model>"
            + "     ?inPort rdfs:comment ?techRole"
            
            + "}";

    public VipRuleOPM_v2() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws MalformedURLException {
//        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//
//        kg1.initEngine();
//        kg2.initEngine();
//
//        File rep1 = new File("/Users/gaignard/Desktop/Expe-VIP-Workflows/dist-sorteo-provenance/sorteo-provenance-source1.rdf");
//        File rep2 = new File("/Users/gaignard/Desktop/Expe-VIP-Workflows/dist-sorteo-provenance/sorteo-provenance-source2.rdf");
//
//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//
//        DataHandler data1 = new DataHandler(new FileDataSource(rep1));
//        kg1.uploadRDF(data1);
//        DataHandler data2 = new DataHandler(new FileDataSource(rep2));
//        kg2.uploadRDF(data2);
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void hello() throws MalformedURLException, EngineException, LoadException {
        StopWatch sw = new StopWatch();

        Graph graphCentralized = Graph.create();
        QueryProcess exec = QueryProcess.create(graphCentralized);
        Load.create(graphCentralized).load("/Users/gaignard/Desktop/Expe-VIP-Workflows/VIP-dist-prov-dataset/sorteo-prov-1.rdf");
        Load.create(graphCentralized).load("/Users/gaignard/Desktop/Expe-VIP-Workflows/VIP-dist-prov-dataset/sorteo-prov-2.rdf");
        Load.create(graphCentralized).load("/Users/gaignard/Desktop/Expe-VIP-Workflows/VIP-dist-prov-dataset/compileProtocole-test.rdf");
        Load.create(graphCentralized).load("/Users/gaignard/Desktop/Expe-VIP-Workflows/VIP-dist-prov-dataset/LMF2RAWSINO-test.rdf");
        sw.start();
        Mappings maps = exec.query(queryRuleFantom);
        sw.stop();
        System.out.println("Results Centralized in " + sw.getTime() + " ms");
//        System.out.println(XMLFormat.create(maps).toString());
        System.out.println(RDFFormat.create(maps).toString());
        sw.reset();

//        Graph graph = Graph.create();
//        QueryProcessDQP execDQP = QueryProcessDQP.create(graph);
//        execDQP.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        execDQP.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//
//        sw.start();
//        maps = execDQP.query(queryRuleFantom);
//        sw.stop();
//        System.out.println("Results DQP in "+sw.getTime()+" ms");
        String resRDF = RDFFormat.create(maps).toString();
//        System.out.println(resRDF);
//        sw.reset();
//        
        InputStream is = new ByteArrayInputStream(resRDF.getBytes());
        Load.create(graphCentralized).load(is);
        sw.start();
        maps = exec.query(queryDescFantom);
        sw.stop();
        System.out.println("Results in "+sw.getTime()+" ms");
        System.out.println(XMLFormat.create(maps).toString());
        sw.reset();

    }
}
