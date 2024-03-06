package fr.inria.corese.w3cJunitTestsGenerator.w3cTests;

import java.util.Set;

/**
 * Interface for W3C tests.
 */
public interface IW3cTest {

    /**
     * Returns the set of imports required for the W3C test.
     *
     * @return the set of imports
     */
    public Set<String> getImports();

    /**
     * Generates the junit test for the W3C test.
     *
     * @return the junit test in string format
     */
    public String generate();
}
