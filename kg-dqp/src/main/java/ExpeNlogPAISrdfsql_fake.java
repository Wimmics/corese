/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import java.io.*;
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
 * Experiment deployment : - I3S neurolog.unice.fr:8443 - Irisa
 * neurolog.irisa.fr:8443 - Asclepios neurolog.inria.fr:8443 - IFR49
 * neurolog.imed.jussieu.fr:8443 - Gin terpsi.ujf-grenoble:8443
 *
 * @author gaignard
 */
public class ExpeNlogPAISrdfsql_fake {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        final RemoteProducer kg_Nice = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg_Sophia = RemoteProducerServiceClient.getPort("http://neurolog.inria.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg_Paris = RemoteProducerServiceClient.getPort("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg_Rennes = RemoteProducerServiceClient.getPort("http://neurolog.irisa.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg_Grenoble = RemoteProducerServiceClient.getPort("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg_Nice.initEngine();
        kg_Sophia.initEngine();
        kg_Paris.initEngine();
        kg_Rennes.initEngineFromSQL("jdbc:datafederator://neurolog.irisa.fr:3055/localIRISA_v21", "LeSelect.ThinDriver.ThinDriver", "localIRISA_v21", "nlogserv");
//        kg_Grenoble.initEngineFromSQL("jdbc:datafederator://terpsi.ujf-grenoble.fr:3055/localGIN_v21", "LeSelect.ThinDriver.ThinDriver", "localGIN_v21", "nlogserv");

        File rep_Nice = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-i3s.rdf");
        File rep_Sophia = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-asclepios.rdf");
        File rep_Paris = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-ifr49.rdf");
//        File rep_Rennes = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-irisa.rdf");
//        File rep_Grenoble = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-gin.rdf");

        Map<String, Object> reqCtxt1 = ((BindingProvider) kg_Nice).getRequestContext();
        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        Map<String, Object> reqCtxt2 = ((BindingProvider) kg_Sophia).getRequestContext();
        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        Map<String, Object> reqCtxt3 = ((BindingProvider) kg_Paris).getRequestContext();
        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt4 = ((BindingProvider) kg_Rennes).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt5 = ((BindingProvider) kg_Grenoble).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data_Nice = new DataHandler(new FileDataSource(rep_Nice));
        final DataHandler data_Sophia = new DataHandler(new FileDataSource(rep_Sophia));
        final DataHandler data_Paris = new DataHandler(new FileDataSource(rep_Paris));
//        final DataHandler data_Rennes = new DataHandler(new FileDataSource(rep_Rennes));
//        final DataHandler data_Grenoble = new DataHandler(new FileDataSource(rep_Grenoble));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg_Nice.uploadRDF(data_Nice);
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg_Sophia.uploadRDF(data_Sophia);
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                kg_Paris.uploadRDF(data_Paris);
            }
        });
//        executor.submit(new Runnable() {
//
//            @Override
//            public void run() {
//                kg_Rennes.uploadRDF(data_Rennes);
//            }
//        });
//        executor.submit(new Runnable() {
//
//            @Override
//            public void run() {
//                kg_Grenoble.uploadRDF(data_Grenoble);
//            }
//        });
        executor.shutdown();
        while (!executor.isTerminated()) {
        }


        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(args[0]));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        String sparqlQuery = fileData.toString();

        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine) ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://neurolog.unice.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.inria.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.irisa.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemoteSQL("jdbc:datafederator://terpsi.ujf-grenoble.fr:3055/localGIN_v21", "LeSelect.ThinDriver.ThinDriver", "localGIN_v21", "nlogserv");

        StopWatch sw = new StopWatch();
        sw.start();
//        IResults res = exec.SPARQLQuery(sparqlQuery);
        System.out.println("--------");
        IResults res = exec.SPARQLQuery(sparqlQuery);
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
