/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class G5KdbpediaBlind {

    static String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //                + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //                + "     OPTIONAL {?x foaf:mbox ?m}"
            + " FILTER ((?name ~ 'Bobby A') )"
//            + " FILTER ((?name ~ 'Bobb') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";

    public static void main(String args[]) throws MalformedURLException, EngineException {
        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://"+args[0]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://"+args[1]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://"+args[2]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://"+args[3]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();
        kg3.initEngine();
        kg4.initEngine();

        File rep1 = new File("/home/agaignard/data/1.7M-4-blind/persondata.1.1.rdf");
        File rep2 = new File("/home/agaignard/data/1.7M-4-blind/persondata.1.2.rdf");
        File rep3 = new File("/home/agaignard/data/1.7M-4-blind/persondata.2.1.rdf");
        File rep4 = new File("/home/agaignard/data/1.7M-4-blind/persondata.2.2.rdf");


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

        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        final DataHandler data3 = new DataHandler(new FileDataSource(rep3));
        final DataHandler data4 = new DataHandler(new FileDataSource(rep4));

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
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://"+args[0]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[1]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[2]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://"+args[3]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlQuery);
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
