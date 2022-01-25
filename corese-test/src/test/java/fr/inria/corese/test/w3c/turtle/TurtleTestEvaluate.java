package fr.inria.corese.test.w3c.turtle;

import org.testng.Assert;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.test.w3c.model.IEvaluate;
import fr.inria.corese.test.w3c.model.TestCase;
import fr.inria.corese.test.w3c.model.TestHelper;
import fr.inria.corese.test.w3c.model.TestCaseSet;
import static fr.inria.corese.test.w3c.model.TestType.NQNegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.NQPositiveSyntax;
import static fr.inria.corese.test.w3c.model.TestType.NTNegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.NTPositiveSyntax;
import static fr.inria.corese.test.w3c.model.TestType.NegativeEval;
import static fr.inria.corese.test.w3c.model.TestType.NegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.PositiveEval;
import static fr.inria.corese.test.w3c.model.TestType.PositiveSyntax;
import static fr.inria.corese.test.w3c.model.TestType.TEST_TYPE;
import static fr.inria.corese.test.w3c.model.TestType.TriGNegativeEval;
import static fr.inria.corese.test.w3c.model.TestType.TriGNegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.TriGPositiveEval;
import static fr.inria.corese.test.w3c.model.TestType.TriGPositiveSyntax;
import static fr.inria.corese.test.w3c.model.TestType.XMLNegativeSyntax;
import static fr.inria.corese.test.w3c.model.TestType.XMLPositiveEval;

/**
 * Implementation of turtle test case evaluation (This evaluation is also
 * applicable for TriG, N-Triples, XML/RDf, N-Quads)
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class TurtleTestEvaluate implements IEvaluate {

    private static String query
            = "prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "prefix mf: <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> .\n"
            + "prefix qt:     <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> .\n"
            + "prefix rdft:   <http://www.w3.org/ns/rdftest#> .\n"
            + "select * where\n"
            + "{\n"
            + "?test mf:action ?action; \n"
            + "rdf:type ?type;\n"
            + "rdfs:comment ?comment;\n"
            + "mf:name ?name.\n"
            + "optional {?test rdft:approval ?approval}\n"
            + "optional {?test mf:result ?result}\n"
            + "} \n"
            + "group by ?test order by ?type";

    public static TurtleTestEvaluate create() {
        return new TurtleTestEvaluate();
    }

    @Override
    public TestCaseSet generateTestCases(String manifest, String root) {
        TestCaseSet suite = TestHelper.generateTestSuite(root + manifest, query, TestHelper.CASE_TURTLE, TEST_TYPE);

        return suite;
    }

    @Override
    public void run(TestCase tc) {

        TurtleTestCase ttc = (TurtleTestCase) tc;
        String type = ttc.getType();
        //ttc.setTested(true);
        boolean result = false;

        //***** 1. turtle
        if (type.equals(PositiveEval)) {
            try {
                result = TestHelper.graphCompare(ttc.getAction(), ttc.getResult());
            } catch (EngineException e) {
                e.printStackTrace();
            }
        } else if (type.equals(NegativeEval) || type.equals(PositiveSyntax) || type.equals(NegativeSyntax)) {
            result = TestHelper.loadValidate(ttc.getAction());

            //***** 2. N Triples
        } else if (type.equals(NTPositiveSyntax) || type.equals(NTNegativeSyntax)) {//for N-Triples
            result = TestHelper.loadValidate(ttc.getAction());

            //***** 3. XML
        } else if (type.equals(XMLPositiveEval)) {//for RDF/XML
            try {
                result = TestHelper.graphCompare(ttc.getAction(), ttc.getResult());
            } catch (EngineException e) {
                e.printStackTrace();
            }
        } else if (type.equals(XMLNegativeSyntax)) {
            result = TestHelper.loadValidate(ttc.getAction());

            //***** 4. TriG
        } else if (type.equals(TriGPositiveEval)) {
            try {
                result = TestHelper.graphCompare(ttc.getAction(), ttc.getResult());
            } catch (EngineException e) {
                e.printStackTrace();
            }
        } else if (type.equals(TriGNegativeEval) || type.equals(TriGPositiveSyntax) || type.equals(TriGNegativeSyntax)) {
            result = TestHelper.loadValidate(ttc.getAction());

            //***** 5. N-Quads
        } else if (type.equals(NQPositiveSyntax) || type.equals(NQNegativeSyntax)) {//for N-Triples
            result = TestHelper.loadValidate(ttc.getAction());

            // **** other
        } else {
            result = false;
            System.out.println("undefined test type:" + type);
        }
        ttc.setRealResult(result);
        ttc.setTested(true);
        ttc.setPassed(tc.getExpectedResult() == tc.getRealResult());

        Assert.assertTrue(ttc.isPassed(), ttc.getName());
    }
}
