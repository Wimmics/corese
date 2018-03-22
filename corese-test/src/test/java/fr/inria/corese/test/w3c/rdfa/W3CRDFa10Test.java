package fr.inria.corese.test.w3c.rdfa;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.test.w3c.model.IEvaluate;
import fr.inria.corese.test.w3c.model.TestCase;
import fr.inria.corese.test.w3c.model.TestCaseSet;
import fr.inria.corese.test.w3c.model.TestHelper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * W3C RDFa 1.0 test (http://rdfa.info/test-suite/)
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class W3CRDFa10Test {

    private static final String web = "http://rdfa.info/test-suite/rdfa1.1/";
    private static final String html4 = web + "html4/";
    private static final String html5 = web + "html5/";
    private static final String xhtml1 = web + "xhtml1/";
    private static final String xhtml5 = web + "xhtml5/";
    private static final String svg = web + "svg/";
    private static final String xml = web + "xml/";

    private static final String manifest = "manifest.ttl";
    static final String local = W3CRDFa10Test.class.getClassLoader().getResource("data").getPath() + "/w3c-rdfa/";
    private static final IEvaluate eval = RDFaTestEvaluate.create();
    private static final String head = "W3C RDFa1.0 test ";
    private static List<TestCaseSet> allTestSuites = new ArrayList();
    private static TestCaseSet globalTestSet = new TestCaseSet(head);

    @BeforeClass
    public void before() {
        globalTestSet.setUri(web);
    }

    @DataProvider
    public static Object[][] data(Method mt) {
        String mn = mt.getName();
        List list = new ArrayList();
        if (mn.equals("testHtml4")) {
            list = init(html4).getTests();
        } else if (mn.equals("testHtml5")) {
            list = init(html5).getTests();
        } else if (mn.equals("testXhtml1")) {
            list = init(xhtml1).getTests();
        } else if (mn.equals("testXhtml5")) {
            list = init(xhtml5).getTests();
        } else if (mn.equals("testXml")) {
            list = init(xml).getTests();
        } else if (mn.equals("testSvg")) {
            list = init(svg).getTests();
        }
        return TestHelper.toObjectArray(list);
    }

    @Test(dataProvider = "data")
    public void testHtml4(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testHtml5(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testXhtml1(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testXhtml5(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testXml(TestCase tc) {
        eval.run(tc);
    }

    @Test(dataProvider = "data")
    public void testSvg(TestCase tc) {
        eval.run(tc);
    }

    @AfterClass
    public void report() {
        //TestReport.toHtml(globalTestSet, "/Users/fsong/Downloads/result-rdfa.html", head);
    }

    //Initalize one test case set
    private static TestCaseSet init(String root) {
        TestCaseSet caseSet = eval.generateTestCases(manifest, root);
        caseSet.setName(head + " [" + root + "]");
        caseSet.setUri(root);
        caseSet.setManifest(manifest);
        allTestSuites.add(caseSet);
        add(caseSet, root);
        return caseSet;
    }

    //Add test case from test set to a global test case set that contains all cases
    private static void add(TestCaseSet tcs, String root) {
        for (TestCase tc : tcs.getTests()) {
            tc.setType(root);
            globalTestSet.add(tc);
        }
    }
}
