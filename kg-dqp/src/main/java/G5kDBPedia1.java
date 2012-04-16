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
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class G5kDBPedia1 {

    public static void main(String args[]) throws MalformedURLException, EngineException {
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://"+args[0]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
//        kg2.initEngine();

        File rep1 = new File("/home/agaignard/data/DBPedia-fragmentation/1-stores/persondata.1.rdf");
//        File rep1 = new File("/Users/gaignard/Desktop/DBPedia-fragmentation/1-store/persondata.rdf");
//        File rep2 = new File("persondata_en_rep_small.1.2.rdf");


        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
//        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg1.uploadRDF(data1);

            }
        });
//        executor.submit(new Runnable() {
//
//            @Override
//            public void run() {
//                kg2.uploadRDF(data2);
//
//            }
//        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://"+args[0]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://localhost:8093/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://localhost:8094/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(Queries.QueryBobbyA);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + " ms");
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
