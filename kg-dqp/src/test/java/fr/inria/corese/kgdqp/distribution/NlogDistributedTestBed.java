/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgengine.GraphEngine;
import fr.inria.corese.kgdqp.core.QueryExecDQP;
import fr.inria.corese.kgdqp.core.WSImplem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
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
public class NlogDistributedTestBed {

    public NlogDistributedTestBed() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
//        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8091/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8092/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8093/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8094/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

//        kg1.initEngine();
//        kg2.initEngine();
//        kg3.initEngine();
//        kg4.initEngine();
//
//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-irisa.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-ifr49.rdf");
//        File rep3 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-asclepios.rdf");
//        File rep4 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-i3s.rdf");

//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt3 = ((BindingProvider) kg3).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt4 = ((BindingProvider) kg4).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

//        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
//        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));
//        final DataHandler data3 = new DataHandler(new FileDataSource(rep3));
//        final DataHandler data4 = new DataHandler(new FileDataSource(rep4));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
//                kg1.uploadRDF(data1);
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
//                kg2.uploadRDF(data2);
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
//                kg3.uploadRDF(data3);
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
//                kg4.uploadRDF(data4);
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
    //

    @Test
    @Ignore
    public void T1DS() throws MalformedURLException, EngineException {

        String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "SELECT distinct ?p ?d WHERE"
                + "{"
//                + "     ?x rdf:type dataset:MR-dataset ."
//                + "     ?x study:involves-as-patient ?p ."
                + "     ?p human:has-for-birth-date ?d ."
                + "FILTER (?p ~ '-IRISA-SS')"
//                + "FILTER (?x ~ '-IFR')"
                + "}";
        
        String sparqlQuery2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>" 
                + "SELECT distinct ?study ?subject ?d ?name WHERE"
                + "{"
//                + "     ?x rdf:type dataset:MR-dataset ."
                + "     ?d linguistic-expression:has-for-name ?name ."
                + "     ?study study:involves-as-patient ?subject ."
                + "     ?subject iec:is-referred-to-by ?d ."
//                + "     ?d DBIOL:subject-ref_sex ?sex ."
//                + "     ?d DBIOL:dataset-ref_mr_dataset_nature ?n ."
//                + "FILTER (?d ~ 'SS')"
//                + "FILTER (?name ~ 'FLAIR')"
                + "FILTER ((?d ~ 'IFR') && (?name ~ 'FLAIR'))"
                + "}";

        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine) ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://cavaco.unice.fr:8091/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://cavaco.unice.fr:8092/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://cavaco.unice.fr:8093/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://cavaco.unice.fr:8094/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        sw.start();
//        IResults res = exec.SPARQLQuery(sparqlQuery);
        IResults res = exec.SPARQLQuery(sparqlQuery2);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        System.out.println("Graph size " + engine.getGraph().size());
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
