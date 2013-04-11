/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
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
 * Experiment deployment : 
 *     - I3S neurolog.unice.fr:8443
 *     - Irisa neurolog.irisa.fr:8443
 *     - Asclepios neurolog.inria.fr:8443
 *     - IFR49 neurolog.imed.jussieu.fr:8443
 *     - Gin terpsi.ujf-grenoble:8443
 * 
 * @author gaignard
 */
public class ExpeNlog4DFirst {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {
        
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://neurolog.inria.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://neurolog.irisa.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg5 = RemoteProducerServiceClient.getPort("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();
        kg3.initEngine();
        kg4.initEngine();
        kg5.initEngine();

        File rep1 = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-i3s.rdf");
        File rep2 = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-asclepios.rdf");
        File rep3 = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-ifr49.rdf");
        File rep4 = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-irisa.rdf");
        File rep5 = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-gin.rdf");

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
//        Map<String, Object> reqCtxt5 = ((BindingProvider) kg5).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        final DataHandler data3 = new DataHandler(new FileDataSource(rep3));
        final DataHandler data4 = new DataHandler(new FileDataSource(rep4));
        final DataHandler data5 = new DataHandler(new FileDataSource(rep5));

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
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(args[0]));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        String sparqlQuery = fileData.toString();

//        String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
//                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
//                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
//                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
//                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
//                + "SELECT distinct ?p ?d WHERE"
//                + "{"
////                + "     ?x rdf:type dataset:MR-dataset ."
////                + "     ?x study:involves-as-patient ?p ."
//                + "     ?p human:has-for-birth-date ?d ."
//                + "FILTER (?p ~ '-IRISA-SS')"
////                + "FILTER (?x ~ '-IFR')"
//                + "}";
//        
//        String sparqlQuery2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
//                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
//                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
//                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
//                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
//                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
//                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>" 
//                + "SELECT distinct ?study ?subject ?d ?name WHERE"
//                + "{"
////                + "     ?x rdf:type dataset:MR-dataset ."
//                + "     ?d linguistic-expression:has-for-name ?name ."
//                + "     ?subject iec:is-referred-to-by ?d ."
//                + "     ?study study:involves-as-patient ?subject ."
////                + "     ?d DBIOL:subject-ref_sex ?sex ."
////                + "     ?d DBIOL:dataset-ref_mr_dataset_nature ?n ."
////                + "FILTER (?d ~ 'SS')"
//                + "FILTER (?name ~ 'FLAIR')"
////                + "FILTER ((?d ~ 'IFR') && (?name ~ 'FLAIR'))"
//                + "}";

        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine) ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://neurolog.unice.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://neurolog.inria.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://neurolog.irisa.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);

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
