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
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;

/**
 *
 * @author gaignard
 */
public class LUBM_Standalone {


    public static void main(String args[]) throws MalformedURLException, EngineException {
        File rep1 = new File("/Users/gaignard/Desktop/LUBM-10-1.3M/");
//        File rep1 = new File("/Users/gaignard/Desktop/LUBM-100-13.8M/");
        
        System.out.println(rep1.getAbsolutePath());

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();
        StopWatch sw = new StopWatch();
        sw.start();
        engine.load(rep1.getAbsolutePath());
        System.out.println("loaded "+rep1.getAbsolutePath()+" in "+sw.getTime()+" ms");
        sw.reset();
        
        QueryExec exec = QueryExec.create(engine);
        
        IResults res = exec.SPARQLQuery(Queries.LUBM_Q1);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
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
