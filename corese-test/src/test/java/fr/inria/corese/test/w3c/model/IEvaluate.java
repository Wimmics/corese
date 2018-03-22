package fr.inria.corese.test.w3c.model;

/**
 * Interface that needs to be realized for implementation of a specific evaluator 
 * for w3c test cases
 * 
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public interface IEvaluate {

    /**
     * Execute one single test case
     * 
     * @param tc Instance of test case
     */
    public void run(TestCase tc);

    
    /**
     * Generate a test case set for a particular w3c test
     * 
     * @param manifest
     * @param root
     * @return An instance of test case set that includes all the test cases
     */
    public TestCaseSet generateTestCases(String manifest, String root);
}
