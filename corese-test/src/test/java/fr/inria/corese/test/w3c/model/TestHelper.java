package fr.inria.corese.test.w3c.model;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.List;

import org.junit.Ignore;

import fr.inria.corese.test.w3c.turtle.TurtleTestCase;
import fr.inria.corese.test.w3c.rdfa.RDFaTestCase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for testing
 * 
 * @author Fuqi Song, Wimmics - Inria I3S
 * @date March 2014
 */
public class TestHelper {

    public static final int CASE_TURTLE = 10;
    public static final int CASE_RDFA = 20;

    /**
     * Generate a test suite
     * 
     * @param manifest
     * @param query Sparql statement for querying manifest
     * @param testCaseType Test case type [rdfa, turtle,...]
     * @param info Array of test types and their expected results
     * 
     * @return An instance of test case set
     */
    public static TestCaseSet generateTestSuite(String manifest, String query, int testCaseType, Object[][] info) {
        //**1. create graph, load manifest
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(manifest);
        QueryProcess qp = QueryProcess.create(g);

        //**2. 
        TestCaseSet ts = new TestCaseSet();

        Mappings maps;
        try {
            maps = qp.query(query);
            for (Mapping m : maps) {
                TestCase tc = null;
                if (testCaseType == CASE_TURTLE) {
                    tc = new TurtleTestCase();
                } else if (testCaseType == CASE_RDFA) {
                    tc = new RDFaTestCase();
                } else {
                    continue;
                }

                tc.toTestCase(m, info);
                ts.add(tc);
            }
        } catch (EngineException ex) {
            System.out.println("Generate test cases error/Query Manifest.ttl error\n" + query);
        }
        return ts;
    }

    /**
     * Check a test is positive or negative test
     * 
     * @param info Array of test types and their expected results
     * @param testType Test type
     * 
     * @return true (positive test) | false (negative test)
     */
    public static boolean isPositiveTest(Object[][] info, String testType) {
        for (Object[] pair : info) {
            if (testType.equals((String) pair[0])) {
                return (Boolean) pair[1];
            }
        }
        return false;
    }

    /**
     * Validate parser by loading a file
     *
     * @param input file to load
     * @return false if exception/error throw, otherwise return true
     */
    public static boolean loadValidate(String input) {
        Graph g = Graph.create();
        return load(input, g);
    }

    /**
     * Compare two graphs to see if they are semantic equivalent 
     * 
     * @param input Source of graph 1
     * @param output Source of graph 2
     * 
     * @return true (semantic equivalent), otherwise false
     * @throws EngineException
     */
    public static boolean graphCompare(String input, String output) throws EngineException {
        //create a graph for input
        Graph g1 = Graph.create();
        boolean b1 = load(input, g1);
        if(!b1) return false;
        
        //create a graph for output
        Graph g2 = Graph.create();
        boolean b2 = load(output, g2);
        if(!b2) return false;

        QueryProcess qp = QueryProcess.create(g1);
        Mappings map;
        try {
            map = qp.query(g2);
            return map.size() > 0;
        } catch (EngineException ex) {
            Logger.getLogger(TestHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Convert a list of test cases to a 2d array of objects
     * this functions is reserved for method dataProvider(testng), 
     * which requires the input has to be 2d Object[][] array
     * 
     * @param ls List of test cases
     * @return 2D array with type "object"
     */
    public static Object[][] toObjectArray(List<TestCase> ls) {
        Object[][] data = new Object[ls.size()][1];
        int i = 0;
        for (TestCase tc : ls) {
            data[i++][0] = tc;
        }
        return data;
    }

    /**
     * Using java reflection get the value of variable 
     * according to its class name and field name
     * 
     * @param clazz class name
     * @param name field name
     * @return value of variable
     */
    public static String getValueByFieldName(String clazz, String name) {
        try {
            Class c = Class.forName(clazz);
            Object obj = c.getField(name).get(c);
            return (String) obj;
        } catch (Exception ex) {
            System.out.println("The field name or/and class name is wrong!");
        } 

        return null;
    }

    // Load a source file to Graph g, if no exceptions/error thrown return true
    //otherwise return falseF
    private static boolean load(String input, Graph g) {
        Load ld = Load.create(g);
        try {
            ld.loadWE(input);
        } catch (LoadException ex) {
            return false;
        } catch (Error e) {
            return false;
        }

        return true;
    }
}
