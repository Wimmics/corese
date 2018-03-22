package fr.inria.corese.test.w3c;

import java.lang.reflect.Method;
import java.util.List;

import fr.inria.corese.test.w3c.model.IEvaluate;
import fr.inria.corese.test.w3c.model.TestCase;
import fr.inria.corese.test.w3c.model.TestCaseSet;
import fr.inria.corese.test.w3c.model.TestHelper;
import fr.inria.corese.test.w3c.turtle.TurtleTestEvaluate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * W3C RDF/XML test (http://www.w3.org/2013/RDFXMLTests/)
 *
 * @author Fuqi Song Wimmics inria i3s
 * @date Feb. 2014
 */
public class W3CRDF11XMLTest {

    private static final String web = "http://www.w3.org/2013/RDFXMLTests/";
    private static final String manifest = "manifest.ttl";
    static final String local = W3CRDF11XMLTest.class.getClassLoader().getResource("data").getPath() + "/w3c-rdf-xml/";
    private static TestCaseSet suite = new TestCaseSet();
    private static final IEvaluate eval = TurtleTestEvaluate.create();

    @BeforeClass
    public void init() {
        suite = eval.generateTestCases(manifest, web);
        suite.setName("W3C RDF1.1/XML test");
        suite.setUri(web);
        suite.setManifest(manifest);
    }

    @DataProvider
    public static Object[][] data(Method mt) {
        String mn = mt.getName();
        String test = mn.substring(4, mn.length());
        List list;

        //1. return all test cases
        if ("all".equalsIgnoreCase(test)) {
            list = suite.getTests();
        } else {//2. return test case with certain type
            String testType = TestHelper.getValueByFieldName("test.TestType", test);
            if (testType == null) {
                return null;
            }
            list = suite.getTestCasesByType(testType);
        }
        return TestHelper.toObjectArray(list);
    }

     //@Test(dataProvider = "data")
    public void testAll(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    //The name of the method should be ["test"+ "filed name of test type"],
    //the field name(*public static*) is defined in another class
    public void testXMLPositiveEval(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testXMLNegativeSyntax(TestCase tc) {
        eval.run(tc);
    }

    @AfterClass
    public void report() {
        System.out.println(TestReport.statsString(suite, true, false, false, false));
        //TestReport.toHtml(suite, "/Users/fsong/Downloads/result-xml.html", "W3C RDF1.1/XML test");
    }
}
