/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResult;
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
 * Experiment deployment : - I3S neurolog.unice.fr:8443 - Irisa
 * neurolog.irisa.fr:8443 - Asclepios neurolog.inria.fr:8443 - IFR49
 * neurolog.imed.jussieu.fr:8443 - Gin terpsi.ujf-grenoble:8443
 *
 * @author gaignard
 */
public class ExpeWI {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        String qGado = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + "SELECT distinct ?patient ?study ?dataset ?dsName WHERE { "
//                + "     ?dataset linguistic-expression:has-for-name ?dsName ."
                + "     ?patient iec:is-referred-to-by/linguistic-exp:has-for-name ?dsName ."
                + "     ?patient examination-subject:has-for-subject-identifier ?clinID .                "
//                + "     ?patient iec:is-referred-to-by ?dataset ."
                + "     ?study study:involves-as-patient ?patient ."
                + "     FILTER ((?clinID ~ 'MS') && (?dsName ~ 'GADO')) "
                + "}";
        
        String qT2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "SELECT distinct ?subject ?d ?name WHERE {"
                + "     ?d linguistic-expression:has-for-name ?name ."
                + "     ?subject iec:is-referred-to-by ?d ."
                //                + "     FILTER ((?name ~ 'T2') && (?subject ~ 'IFR'))"
                + "     FILTER (?name ~ 'T1')"
                + "}";

        String nlex = "PREFIX property: <http://neurolex.org/wiki/Special:URIResolver/Property-3A>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + ""
                + "SELECT ?patient ?dataset WHERE {"
//                                + "SELECT DISTINCT ?s ?label WHERE {"
                                + "     ?t property:Label \"MRI protocol\"^^xsd:string ."
//                + "     ?t property:Label \"T2 weighted protocol\"^^xsd:string ."
//                + "     ?t property:Label \"Diffusion magnetic resonance imaging protocol\"^^xsd:string ."
                + "     ?s rdfs:subClassOf* ?t ."
//                + "     ?s rdf:type <http://neurolex.org/wiki/Special:URIResolver/Category-3AMRI_protocol>"
                + "     ?s property:Label ?label ."
                //                + "     ?x property:Label ?label ."
                //                + "     OPTIONAL {?x property:Synonym ?syn}"
//                + "     ?dataset property:Label \"Diffusion magnetic resonance imaging protocol\"^^xsd:string ."
                + "     ?dataset property:Label ?label ."
                + "     ?patient iec:is-referred-to-by ?dataset ."
//                + "     ?dataset linguistic-expression:has-for-name ?datasetName ."
//                + "     ?patient examination-subject:has-for-subject-identifier ?clinID ."

                + "}";

        final RemoteProducer kg_Nice = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg_Sophia = RemoteProducerServiceClient.getPort("http://neurolog.inria.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        final RemoteProducer kg_Paris = RemoteProducerServiceClient.getPort("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

//        final RemoteProducer kg_Rennes = RemoteProducerServiceClient.getPort("http://neurolog.irisa.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg_Grenoble = RemoteProducerServiceClient.getPort("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

//        kg_Nice.initEngine();
//        kg_Sophia.initEngine();
//        kg_Paris.initEngine();

        //        kg_Rennes.initEngine();
//        kg_Grenoble.initEngine();

        ///Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData
//        File rep_Nice = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-i3s.rdf");
//        File rep_Sophia = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-asclepios.rdf");
//        File rep_Paris = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-ifr49.rdf");
//        File rep_Rennes = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-irisa.rdf");
//        File rep_Grenoble = new File("/home/neurolog-test/sem-fed-client/data/linkedData-source-gin.rdf");
        File rep_Nice = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-i3s.rdf");
        File rep_Sophia = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-asclepios.rdf");
        File rep_Paris = new File("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-ifr49.rdf");

        File neurolex_neurolog_bridge = new File("/Users/gaignard/Desktop/Open-LS-LinkedData/bridgeNeuroLEX.rdf");
        File neurolex = new File("/Users/gaignard/Desktop/Open-LS-LinkedData/nlx_stage_all.owl");


//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg_Nice).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg_Sophia).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt3 = ((BindingProvider) kg_Paris).getRequestContext();
//        reqCtxt3.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt3.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt4 = ((BindingProvider) kg_Rennes).getRequestContext();
//        reqCtxt4.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt4.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        Map<String, Object> reqCtxt5 = ((BindingProvider) kg_Grenoble).getRequestContext();
//        reqCtxt5.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt5.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        final DataHandler data_Nice = new DataHandler(new FileDataSource(rep_Nice));
        final DataHandler data_Nlex = new DataHandler(new FileDataSource(neurolex));
        final DataHandler data_Nbridge = new DataHandler(new FileDataSource(neurolex_neurolog_bridge));
        final DataHandler data_Sophia = new DataHandler(new FileDataSource(rep_Sophia));
        final DataHandler data_Paris = new DataHandler(new FileDataSource(rep_Paris));
//        final DataHandler data_Rennes = new DataHandler(new FileDataSource(rep_Rennes));
//        final DataHandler data_Grenoble = new DataHandler(new FileDataSource(rep_Grenoble));

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    kg_Nice.uploadRDF(data_Nice);
                    System.out.println("Up nlog Nice");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    kg_Nice.uploadRDF(data_Nlex);
                    System.out.println("Up nlex");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    kg_Nice.uploadRDF(data_Nbridge);
                    System.out.println("Up nBridge");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    kg_Sophia.uploadRDF(data_Sophia);
                    System.out.println("Up nlog Sophia");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    kg_Paris.uploadRDF(data_Paris);
                    System.out.println("Up nlog Paris");
                } catch (Exception e) {
                    e.printStackTrace();
                }
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


//        StringBuffer fileData = new StringBuffer(1000);
//        BufferedReader reader = new BufferedReader(
//                new FileReader(args[0]));
//        char[] buf = new char[1024];
//        int numRead = 0;
//        while ((numRead = reader.read(buf)) != -1) {
//            String readData = String.valueOf(buf, 0, numRead);
//            fileData.append(readData);
//            buf = new char[1024];
//        }
//        reader.close();
//        String sparqlQuery = fileData.toString();
        try {
            kg_Nice.loadRDF("/home/neurolog-test/sem-fed-nlog/nlx_stage_all.owl");
        } catch (Exception e) {
            e.printStackTrace();
        }

        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine) ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://neurolog.unice.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://neurolog.inria.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemote(new URL("http://neurolog.imed.jussieu.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
        exec.addRemoteSQL("jdbc:mysql://localhost:3055/shanoirstablev3", "com.mysql.jdbc.Driver", "inrianeurotk", "inrianeurotk");
//        exec.addRemote(new URL("http://neurolog.irisa.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://terpsi.ujf-grenoble.fr:8443/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
//        IResults res = exec.SPARQLQuery(sparqlQuery);
        System.out.println("--------");
//        IResults res = exec.SPARQLQuery(qT2);
//        IResults res = exec.SPARQLQuery(nlex);
        IResults res = exec.SPARQLQuery(qGado);
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
