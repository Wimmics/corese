package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class BsbmTest {

    private static EngineFactory ef;
    private static IEngine engine;

    public BsbmTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ef = new EngineFactory();
//            ef.setProperty(EngineFactory.PROPERTY_FILE, "corese.properties");
//            ef.setProperty(EngineFactory.ENGINE_LOG4J, "log4j.properties");
        engine = ef.newInstance();

//            engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-foaf-t.owl"), null);
        engine.load(BsbmTest.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
//        engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-persons.rdf"), null);
        engine.runRuleEngine();
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

    @Test
    @Ignore
    public void remoteBsbmQuery() throws EngineException, MalformedURLException, IOException {

        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8089/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");


        File rep1 = new File("/Users/gaignard/Desktop/BSBM/DatasetBSBM-1M-split1.rdf");
        File rep2 = new File("/Users/gaignard/Desktop/BSBM/DatasetBSBM-1M-split2.rdf");
        
//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        DataHandler data = new DataHandler(new FileDataSource(rep1));
        kg1.uploadRDF(data);
        DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        kg2.uploadRDF(data2);

        
        String sparqlQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>"
                + "  SELECT distinct ?product ?label WHERE {"
                + "    ?product rdf:type bsbm:ProductFeature ."
                + "    ?product rdfs:label ?label ."
//                + "    ?product rdf:type bsbm:Product ."
//                + "  FILTER regex(?label, \"electro\")"
                + "  FILTER (?label ~ 'shoo')"
                + "}";


        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();
            
        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://cavaco.unice.fr:8089/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://cavaco.unice.fr:8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        
        IResults res = exec.SPARQLQuery(sparqlQuery);
        String[] variables = res.getVariables();

        for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
            IResult r = en.nextElement();
            HashMap<String, String> result = new HashMap<String, String>();
            for (String var : variables) {
                if (r.isBound(var)) {
                    IResultValue[] values = r.getResultValues(var);
                    for (int j = 0; j < values.length; j++) {
                        System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                    }
                } else {
                    System.out.println(var + " = Not bound");
                }
            }
        }
    }
}
