/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class NeuroLEX_NeuroLOG_Test {

    public NeuroLEX_NeuroLOG_Test() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {

//        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        
//        kg1.initEngine();
//        kg2.initEngine();

        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source-neurolex.rdf");
        File rep2 = new File("/Users/gaignard/Desktop/Open-LS-LinkedData/nlx_stage_all.owl");

//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        DataHandler data1 = new DataHandler(new FileDataSource(rep1));
//        kg1.uploadRDF(data1);
        DataHandler data2 = new DataHandler(new FileDataSource(rep2));
//        kg2.uploadRDF(data2);
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void remoteNeuroQuery() throws EngineException, MalformedURLException, IOException {

        String sparqlQuery = "PREFIX property: <http://neurolex.org/wiki/Special:URIResolver/Property-3A>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + ""
                + "SELECT DISTINCT ?patient ?dataset ?datasetName ?label ?syn WHERE {"
                + "     ?t property:Label \"MRI protocol\"^^xsd:string ."
//                + "     ?t property:Label \"T2 weighted protocol\"^^xsd:string ."

                + "     ?x rdfs:subClassOf* ?t ."
                + "     ?x property:Label ?label ."
//                + "     OPTIONAL {?x property:Synonym ?syn}"
                + ""
                + "     ?dataset linguistic-expression:has-for-name ?datasetName ."
                + "     ?dataset property:Label ?label ."
                + "     ?patient iec:is-referred-to-by ?dataset ."
//                + "     ?patient examination-subject:has-for-subject-identifier ?clinID ."
                
                + "}";

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

//        QueryExec exec2 = QueryExec.create(engine);
        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlQuery);

        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        GraphEngine gEng = (GraphEngine) engine;
        System.out.println("Results size " + res.size());
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
