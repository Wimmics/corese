/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.ProviderWSImpl;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.strategies.ServiceQueryVisitorPar;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;

/**
 *
 * @author gaignard
 */
public class G5kFedBenchLS_Query {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        
        String manualFedQuery = "select ?Drug ?IntDrug ?IntEffect where {	"
                + "service <http://suno-6:8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort> {"
                + "{?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2> ?IntDrug . "
                + "?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/text> ?IntEffect . "
                + "?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1> ?y . }"
                + "}"
                + ""
                + "?Drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Drug> . "
                + "?y <http://www.w3.org/2002/07/owl#sameAs> ?Drug ."
                + "}";
        
//        ExecutorService executor = Executors.newCachedThreadPool();
//
//        ArrayList<RemoteProducer> kgs = new ArrayList<RemoteProducer>();
//        int i = 1;
//        for (String arg : args) {
//            final RemoteProducer rp = RemoteProducerServiceClient.getPort("http://" + arg + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//            kgs.add(rp);
//            rp.initEngine();
//
//
//
//            File rep = new File("/home/agaignard/data/FedBench-dataset/producer-" + i);
//            i++;
//
//            for (File f : rep.listFiles()) {
//                final String path = f.getAbsolutePath();
//                executor.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        rp.loadRDF(path);
//                    }
//                });
//
//            }
//
//        }
//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }



        ///////////////////////
        Graph graphStdOpt = Graph.create();
        QueryProcessDQP execStd = QueryProcessDQP.create(graphStdOpt);
        
         ///////////////////////
        Graph graphManualOpt = Graph.create();
        QueryProcessDQP execManualOpt = QueryProcessDQP.create(graphManualOpt);
        ProviderWSImpl pManualOpt = ProviderWSImpl.create();
        execManualOpt.set(pManualOpt);

        ///////////////////////
        Graph graphGroupOpt = Graph.create();
        QueryProcessDQP execGroupOpt = QueryProcessDQP.create(graphGroupOpt);
        execGroupOpt.set(new ServiceQueryVisitorPar(execGroupOpt));
        execGroupOpt.setOptimize(true);
        ProviderWSImpl pOpt = ProviderWSImpl.create();
        execGroupOpt.set(pOpt);

        ////////////////////////
        Graph graphGroup = Graph.create();
        QueryProcessDQP execGroup = QueryProcessDQP.create(graphGroup);
        execGroup.set(new ServiceQueryVisitorPar(execGroup));
        execGroup.setOptimize(false);
        ProviderWSImpl p = ProviderWSImpl.create();
        execGroup.set(p);

        for (int i = 0; i < 5; i++) {
            execStd.addRemote(new URL("http://" + args[i] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
            execGroup.addRemote(new URL("http://" + args[i] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
            execGroupOpt.addRemote(new URL("http://" + args[i] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
            execManualOpt.addRemote(new URL("http://" + args[i] + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        }

        int ls = Integer.parseInt(args[5]);

//        for (int i = 1; i < 8; i++) {
//            if ((i == 1) || (i == 2) || (i == 4)) {
//            if ((i == 5)) {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
//                new FileReader("fedBenchQueries/LS" + ls + ".sparql"));
                  new FileReader("fedBenchQueries/tunedLS.sparql"));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        String sparqlQuery = fileData.toString();

        
        StopWatch sw1 = new StopWatch();
        sw1.start();
        Mappings res1 = execManualOpt.query(sparqlQuery);
        System.out.println("----Manual fed query----");
        System.out.println("Results LS " + ls + " in " + sw1.getTime() + " ms");
        System.out.println("Results LS " + ls + " size " + res1.size());
        System.out.println(XMLFormat.create(res1).toString());
        
//        StopWatch sw1 = new StopWatch();
//        sw1.start();
//        Mappings res1 = execStd.query(sparqlQuery);
//        System.out.println("----Std----");
//        System.out.println("Results LS " + ls + " in " + sw1.getTime() + " ms");
//        System.out.println("Results LS " + ls + " size " + res1.size());


//                StopWatch sw2 = new StopWatch();
//                sw2.start();
//                Mappings res2 = execGroupOpt.query(sparqlQuery);
//                System.out.println("----Group Opt----");
//                System.out.println("Results LS " + ls + " in " + sw2.getTime() + " ms");
//                System.out.println("Results LS " + ls + " size " + res2.size());
//                
//        StopWatch sw3 = new StopWatch();
//        sw3.start();
//        Mappings res3 = execGroup.query(sparqlQuery);
//        System.out.println("----Group----");
//        System.out.println("Results LS " + ls + " in " + sw3.getTime() + " ms");
//        System.out.println("Results LS " + ls + " size " + res3.size());

//            String[] variables = res.getVariables();
//
//            for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
//                IResult r = en.nextElement();
//                HashMap<String, String> result = new HashMap<String, String>();
//                for (String var : variables) {
//                    if (r.isBound(var)) {
//                        IResultValue[] values = r.getResultValues(var);
//                        for (int j = 0; j < values.length; j++) {
//                            System.out.println(var + " = " + values[j].getStringValue());
////                            result.put(var, values[j].getStringValue());
//                        }
//                    } else {
//                        //System.out.println(var + " = Not bound");
//                    }
//                }
//            }
//            System.out.println(sw.getTime() + " ms");
//            System.out.println("");
//            System.out.println("");
    }
//        }
//    }
}
