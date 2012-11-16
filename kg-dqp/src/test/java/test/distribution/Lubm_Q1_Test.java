/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.api.*;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class Lubm_Q1_Test {
    
    public static String LUBM_Q1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ub: <http://www.lehigh.edu/?zhp2/2004/0401/univ-bench.owl#> \n"
            + "SELECT distinct ?T WHERE { \n"
            + "?X rdf:type ?T"
//            + "?X rdf:type <http://www.lehigh.edu/?zhp2/2004/0401/univ-bench.owl#GraduateStudent> \n"
//            + "?X ub:takesCourse <http://www.Department0.University0.edu/GraduateCourse0> \n"
            + "}";
    
    public Lubm_Q1_Test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void hello() throws EngineException {
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
        
        IResults res = exec.SPARQLQuery(LUBM_Q1);
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
