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
public class G5kDBPedia6 {

    public static void main(String args[]) throws MalformedURLException, EngineException {
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://"+args[0]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://"+args[1]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://"+args[2]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://"+args[3]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg5 = RemoteProducerServiceClient.getPort("http://"+args[4]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg6 = RemoteProducerServiceClient.getPort("http://"+args[5]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();
        kg3.initEngine();
        kg4.initEngine();
        kg5.initEngine();
        kg6.initEngine();

        File rep1 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.1.rdf");
        File rep2 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.2.rdf");
        File rep3 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.3.rdf");
        File rep4 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.4.rdf");
        File rep5 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.5.rdf");
        File rep6 = new File("/home/agaignard/data/DBPedia-fragmentation/6-stores/persondata.6.rdf");


//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt3 = ((BindingProvider) kg3).getRequestContext();
//        reqCtxt3.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt3.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt4 = ((BindingProvider) kg4).getRequestContext();
//        reqCtxt4.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt4.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt5 = ((BindingProvider) kg5).getRequestContext();
//        reqCtxt5.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt5.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt6 = ((BindingProvider) kg6).getRequestContext();
//        reqCtxt6.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt6.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        final DataHandler data3 = new DataHandler(new FileDataSource(rep3));
        final DataHandler data4 = new DataHandler(new FileDataSource(rep4));
        final DataHandler data5 = new DataHandler(new FileDataSource(rep5));
        final DataHandler data6 = new DataHandler(new FileDataSource(rep6));

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
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg3.uploadRDF(data3);

            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg4.uploadRDF(data4);

            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg5.uploadRDF(data5);

            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                kg6.uploadRDF(data6);

            }
        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://"+args[0]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[1]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[2]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[3]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[4]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[5]+":8090/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

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
