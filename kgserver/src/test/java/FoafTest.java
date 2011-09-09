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
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import java.util.Enumeration;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class FoafTest {
    
    private static EngineFactory ef;
    private static IEngine engine;
    
    public FoafTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
         ef = new EngineFactory();
//            ef.setProperty(EngineFactory.PROPERTY_FILE, "corese.properties");
//            ef.setProperty(EngineFactory.ENGINE_LOG4J, "log4j.properties");
            engine = ef.newInstance();
            
//            engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-foaf-t.owl"), null);
            engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
            engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-persons.rdf"), null);
            engine.runRuleEngine();
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
    
    
     @Test
     public void foafQuery() throws EngineException {
            String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                    + "SELECT distinct ?x ?y WHERE"
                    + "{"
                    + "     ?x foaf:knows ?y"
                    + "}";
         
            
            QueryExec exec = QueryExec.create();
            exec.add(engine);
            
            IResults res = exec.SPARQLQuery(sparqlQuery);
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
