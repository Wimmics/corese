/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.exceptions.EngineException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class G5kFedBenchLS_Init {

    public static void main(String args[]) throws MalformedURLException, EngineException {

        ExecutorService executor = Executors.newCachedThreadPool();

        ArrayList<RemoteProducer> kgs = new ArrayList<RemoteProducer>();
        int i = 1;
        for (String arg : args) {
            final RemoteProducer rp = RemoteProducerServiceClient.getPort("http://" + arg + ":8090/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
            kgs.add(rp);
            rp.initEngine();

            File rep = new File("/home/agaignard/data/FedBench-dataset/producer-" + i);
            i++;

            for (File f : rep.listFiles()) {
                final String path = f.getAbsolutePath();
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        rp.loadRDF(path);
                    }
                });

            }

        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }
}
