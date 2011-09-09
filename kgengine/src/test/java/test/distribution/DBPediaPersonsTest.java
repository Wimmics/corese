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
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
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

    public DBPediaPersonsTest() {
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

    @Test
    @Ignore
    public void remoteDBPediaQuery() throws EngineException, MalformedURLException, IOException {

        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8089/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8090/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");


        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-persons/persondata_en_rep1.rdf");
        File rep2 = new File("/Users/gaignard/Desktop/DBPedia-persons/persondata_en_rep2.rdf");

        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data = new DataHandler(new FileDataSource(rep1));
        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg1.uploadRDF(data);

            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg2.uploadRDF(data);

            }
        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
                + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
                + "SELECT distinct ?name ?date ?place WHERE \n"
                + "{"
                + "     ?x foaf:name ?name ."
                + "     ?x dbpedia:birthPlace ?place ."
                + "     ?x dbpedia:birthDate ?date ."
                //                + "     OPTIONAL {?x foaf:mbox ?m}"
                + " FILTER (?x ~ 'Alban')"
                + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";


        StopWatch sw = new StopWatch();
        sw.start();
        
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExec exec = QueryExec.create(engine);
        exec.addRemote(new URL("http://cavaco.unice.fr:8089/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://cavaco.unice.fr:8090/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

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
        System.out.println(sw.getTime()+" ms");
    }
}
