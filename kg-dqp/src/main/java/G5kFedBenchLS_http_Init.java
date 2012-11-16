/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLEndpointClient;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author gaignard
 */
public class G5kFedBenchLS_http_Init {

    public static void main(String args[]) throws MalformedURLException, EngineException {

        ExecutorService executor = Executors.newCachedThreadPool();


        int i = 1;
        for (String arg : args) {
            final String url = "http://" + arg + ":8090/kgendpoint-1.0.7/KGSparqlEndpoint";

            File rep = new File("/home/agaignard/data/FedBench-dataset/producer-" + i);
            i++;

            for (File f : rep.listFiles()) {
                final String path = f.getAbsolutePath();
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {


                            URL queryURL = new URL(url + "?load=" + URLEncoder.encode(path, "UTF-8"));
                            HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
                            urlConn.setRequestMethod("GET");

                            InputStream stream = urlConn.getInputStream();
                            InputStreamReader r = new InputStreamReader(stream);
                            BufferedReader br = new BufferedReader(r);
                            StringBuffer sb = new StringBuffer();

                            String str = null;
                            while ((str = br.readLine()) != null) {
                                sb.append(str);
                                sb.append("\n");
                            }

                            System.out.println(sb.toString());

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }

        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }
}
