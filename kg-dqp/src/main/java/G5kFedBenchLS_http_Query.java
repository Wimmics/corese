/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;

/**
 *
 * @author gaignard
 */
public class G5kFedBenchLS_http_Query {

    public static void main(String args[]) throws MalformedURLException, EngineException, FileNotFoundException, IOException {

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
        for (String arg : args) {
            exec.addRemote(new URL("http://" + arg + ":8090/kgendpoint-1.0.7/KGSparqlEndpoint"));
        }


        for (int i = 1; i < 8; i++) {

            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(
                    new FileReader("fedBenchQueries/LS"+i+".sparql"));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            String sparqlQuery = fileData.toString();

            StopWatch sw = new StopWatch();
            sw.start();
            IResults res = exec.SPARQLQuery(sparqlQuery);
            System.out.println("--------");
            System.out.println("Results LS "+i+" in " + sw.getTime() + " ms");
            GraphEngine gEng = (GraphEngine) engine;
//            System.out.println("Graph size " + gEng.getGraph().size());
            System.out.println("Results LS "+i+" size " + res.size());
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
    }
}
