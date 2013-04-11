/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgraph.query.SparqlResultParser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.apache.commons.lang.time.StopWatch;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class G5KdbpediaSingle {

    static String sparqlQuery = Queries.QueryBobbyA;

    public static void main(String args[]) throws MalformedURLException, EngineException {
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://" + args[0] + ":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        kg1.initEngine();

        File rep1 = new File("/home/agaignard/data/1.7M/1-stores/persondata.1.rdf");

        System.out.println(rep1.getAbsolutePath());

//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));


        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg1.uploadRDF(data1);

            }
        });

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://" + args[0] + ":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

        StopWatch sw = new StopWatch();
        for (int i = 0; i < 20; i++) {
            sw.start();
            String resS = kg1.getEdges(sparqlQuery);
            System.out.println("Results in " + sw.getTime() + " ms");
            SparqlResultParser parser = new SparqlResultParser();
            List<HashMap<String, String>> results = parser.parse(resS);
            for (HashMap<String, String> r : results) {
                System.out.println(r.toString());
            }
            System.out.println("Results parsed in " + sw.getTime() + " ms");
            sw.reset();
        }
    }
}
