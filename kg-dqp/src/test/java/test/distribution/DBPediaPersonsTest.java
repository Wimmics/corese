package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
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
            //                + "     ?x ?y ?name2 ."
//            + "     ?x dbpedia:birthPlace ?place ."
                            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //                + "     OPTIONAL {?x foaf:mbox ?m}"
            + " FILTER ((?name ~ 'Bobby A') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";

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
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();

//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep1.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep2.rdf");
//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/dbpediaNames.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/dbpediaBirthDate.rdf");
        
                
//        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/72K/2-stores/persondata.1.rdf");
//        File rep2 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/72K/2-stores/persondata.2.rdf");

        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/338K/2-stores/persondata.1.rdf");
        File rep2 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/338K/2-stores/persondata.2.rdf");
//        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/147K/2-stores/persondata.1.rdf");
//        File rep2 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/147K/2-stores/persondata.2.rdf");
//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep_small.1.1.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep_small.1.2.rdf");

//        File rep1 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep_ascii.1.rdf");
//        File rep2 = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep_ascii.2.rdf");

        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg1.uploadRDF(data1);

            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg2.uploadRDF(data2);

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
    @Ignore
    public void remoteDBPediaQuery() throws EngineException, MalformedURLException, IOException {

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://cavaco.unice.fr:8091/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://cavaco.unice.fr:8092/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlQuery);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        GraphEngine gEng = (GraphEngine) engine;
        System.out.println("Graph size " + gEng.getGraph().size());
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
                    //System.out.println(var + " = Not bound");
                }
            }
        }
        System.out.println(sw.getTime() + " ms");
    }
}
