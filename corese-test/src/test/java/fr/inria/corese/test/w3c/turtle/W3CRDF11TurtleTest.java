package fr.inria.corese.test.w3c.turtle;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import fr.inria.corese.test.w3c.model.IEvaluate;
import fr.inria.corese.test.w3c.model.TestCase;
import fr.inria.corese.test.w3c.model.TestHelper;
import fr.inria.corese.test.w3c.model.TestCaseSet;

/**
 * W3C RDF1.1 Turtle test [http://www.w3.org/2013/TurtleTests/]
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class W3CRDF11TurtleTest {

    private static final String web = "http://www.w3.org/2013/TurtleTests/";
    private static final String manifest = "manifest.ttl";
    static final String local = W3CRDF11TurtleTest.class.getClassLoader().getResource("data").getPath() + "/w3c-turtle/";
    private static TestCaseSet suite_turtle = new TestCaseSet();
    private static final IEvaluate eval = TurtleTestEvaluate.create();
    private static final String head_turtle = "W3C RDF1.1 Turtle test";

    @BeforeClass
    public void init() {
        suite_turtle = eval.generateTestCases(manifest, web);
        suite_turtle.setName(head_turtle);
        suite_turtle.setUri(web);
        suite_turtle.setManifest(manifest);
    }

    @DataProvider
    public static Object[][] data(Method mt) {
        String mn = mt.getName();
        String test = mn.substring(4, mn.length());
        List list;

        //1. return all test cases
        if ("all".equalsIgnoreCase(test)) {
            list = suite_turtle.getTests();
        } else {//2. return test case with certain type
            String testType = TestHelper.getValueByFieldName("test.TestType", test);
            if (testType == null) {
                return null;
            }
            list = suite_turtle.getTestCasesByType(testType);
        }
        return TestHelper.toObjectArray(list);
    }

    // @Test(dataProvider = "data")
    public void testAll(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    //The name of the method should be ["test"+ "filed name of test type"],
    //the field name(*public static*) is defined in another class
    public void testPositiveEval(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testNegativeEval(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testPositiveSyntax(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testNegativeSyntax(TestCase tc) {
        eval.run(tc);
    }

    @AfterClass
    public void report() {
        //System.out.println(TestReport.statsString(suite_turtle, true,false,true, false));
        //System.out.println(TestReport.toEarl(suite, web + manifest));
        //TestReport.toHtml(suite_turtle, "/Users/fsong/Downloads/result-turtle.html", head_turtle);
        //String earl = TestReport.toEarl(suite_turtle, web+manifest);
        //TestReport.toFile("/Users/fsong/Downloads/result-turtle.earl", earl);
    }
}
