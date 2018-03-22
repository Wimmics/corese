package fr.inria.corese.test.w3c;

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
import static fr.inria.corese.test.w3c.model.TestType.NTNegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.NTPositiveSyntax;
import fr.inria.corese.test.w3c.turtle.TurtleTestEvaluate;

/**
 * W3C RDF1.1 RDF/XML test ( http://www.w3.org/2013/N-TriplesTests/ )
 *
 * @author Fuqi Song Wimmics inria i3s
 * @date Feb. 2014
 */
public class W3CRDF11NTriplesTest {

    private static final String web = "http://www.w3.org/2013/N-TriplesTests/";
    private static final String manifest = "manifest.ttl";
    static final String local = W3CRDF11NTriplesTest.class.getClassLoader().getResource("data").getPath() + "/w3c-n-triples/";
    private static final String head = "W3C RDF1.1 N-Triples test";
    private static TestCaseSet suite = null;
    private static final IEvaluate eval = TurtleTestEvaluate.create();

    @BeforeClass
    public void init() {
        suite = eval.generateTestCases(manifest, web);
        suite.setName(head);
        suite.setUri(web);
        suite.setManifest(manifest);
    }

    @DataProvider
    public static Object[][] data(Method mt) {
        String test = mt.getName();
        List list;
        if ("testNTPositiveSyntax".equals(test)) {
            list = suite.getTestCasesByType(NTPositiveSyntax);
        } else if ("testNTNegativeSyntax".equals(test)) {
            list = suite.getTestCasesByType(NTNegativeSyntax);
        } else {//all test cases
            list = suite.getTests();
        }
        return TestHelper.toObjectArray(list);
    }

    //@Test(dataProvider = "data")
    public void testAll(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testNTPositiveSyntax(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testNTNegativeSyntax(TestCase tc) {
        eval.run(tc);
    }

    @AfterClass
    public void report() {
        System.out.println(TestReport.statsString(suite, true, false, true, false));
        //TestReport.toHtml(suite, "/Users/fsong/Downloads/result-nt.html", head);
    }
}
