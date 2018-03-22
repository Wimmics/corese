package fr.inria.corese.test.w3c.model;

/**
 * Constant class of test types
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public final class TestType {

    //turtle test types
    public static final String RDFT = "http://www.w3.org/ns/rdftest#";
    public static final String PositiveEval = RDFT + "TestTurtleEval";
    public static final String NegativeEval = RDFT + "TestTurtleNegativeEval";
    public static final String PositiveSyntax = RDFT + "TestTurtlePositiveSyntax";
    public static final String NegativeSyntax = RDFT + "TestTurtleNegativeSyntax";

    //N-Triples test
    public static final String NTPositiveSyntax = RDFT + "TestNTriplesPositiveSyntax";
    public static final String NTNegativeSyntax = RDFT + "TestNTriplesNegativeSyntax";

    //N-Quads test
    public static final String NQPositiveSyntax = RDFT + "TestNQuadsPositiveSyntax";
    public static final String NQNegativeSyntax = RDFT + "TestNQuadsNegativeSyntax";

    //RDF/XML test
    public static final String XMLPositiveEval = RDFT + "TestXMLEval";
    public static final String XMLNegativeSyntax = RDFT + "TestXMLNegativeSyntax";

    //TriG
    public static final String TriGPositiveEval = RDFT + "TestTrigEval";
    public static final String TriGNegativeEval = RDFT + "TestTrigNegativeEval";
    public static final String TriGPositiveSyntax = RDFT + "TestTrigPositiveSyntax";
    public static final String TriGNegativeSyntax = RDFT + "TestTrigNegativeSyntax";

    public static final Object[][] TEST_TYPE = {
        {PositiveEval, true}, {NegativeEval, false},
        {PositiveSyntax, true}, {NegativeSyntax, false},
        {NTPositiveSyntax, true}, {NTNegativeSyntax, false},
        {NQPositiveSyntax, true}, {NQNegativeSyntax, false},
        {XMLPositiveEval, true}, {XMLNegativeSyntax, false},
        {TriGPositiveEval, true}, {TriGNegativeEval, false},
        {TriGPositiveSyntax, true}, {TriGNegativeSyntax, false}};

    //RDFa
    public static final String MF = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";
    public static final String QueryEvaluation = MF + "mf:QueryEvaluationTest";
}
