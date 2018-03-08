package test.rdfa;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for validating RDFa parsing and sparql query against the parsed
 * data
 * 
 * @deprecated This test is merged to W3C test cases
 * please refer to kgtool:test.w3c.rdfa.W3CRDFa10Test.java
 * 
 * @author Fuqi Song Wimmics inria i3s
 * @date Jan 2014 new
 * @date 28 Feb. 2014 deprecated
 */
@RunWith(DataProviderRunner.class)
public class RdfaParserTest {

    protected static final RdfaTestHelper helper = new RdfaTestHelper();

    //--------------generate test suites-------------
    @DataProvider
    public static Object[][] getTestSuiteForHtml4() {
        return helper.generateTestSuite(RdfaTestHelper.HTML4);
    }

    @DataProvider
    public static Object[][] getTestSuiteForHtml5() {
        return helper.generateTestSuite(RdfaTestHelper.HTML5);
    }

    @DataProvider
    public static Object[][] getTestSuiteForXhtml1() {
        return helper.generateTestSuite(RdfaTestHelper.XHTML1);
    }

    @DataProvider
    public static Object[][] getTestSuiteForXhtml5() {
        return helper.generateTestSuite(RdfaTestHelper.XHTML5);
    }

    @DataProvider
    public static Object[][] getTestSuiteForXml() {
        return helper.generateTestSuite(RdfaTestHelper.XML);
    }

    @DataProvider
    public static Object[][] getTestSuiteForSvg() {
        return helper.generateTestSuite(RdfaTestHelper.SVG);
    }

    //-------------run test----------
    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForHtml4")
    public void runTestsHtml4(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }

    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForHtml5")
    public void runTestsHtml5(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }

    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForXhtml1")
    public void runTestsXhtml1(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }

    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForXhtml5")
    public void runTestsXhtml5(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }

    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForXml")
    public void runTestsXml(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }

    @Ignore(value = "unknown reason")
    @Test
    @UseDataProvider("getTestSuiteForSvg")
    public void runTestsSvg(String sourceFile, String sparql) throws IOException {
        helper.process(sourceFile, sparql);
    }
}
