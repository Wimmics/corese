package fr.inria.corese.test.w3c.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test cases set that includes 0 to many single test cases
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class TestCaseSet {

    private String name;
    private String uri;
    private String manifest;

    private List<TestCase> tests = new ArrayList<TestCase>();

    public void add(TestCase ts) {
        tests.add(ts);
    }

    public TestCaseSet() {
    }

    public TestCaseSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public List<TestCase> getTests() {
        return tests;
    }

    public void setTests(List<TestCase> tests) {
        this.tests = tests;
    }

    /**
     * Return a list of test cases that belong to the same test type
     * 
     * @param type test type
     * @return List of test cases
     */
    public List<TestCase> getTestCasesByType(String type) {
        List<TestCase> list = new ArrayList<TestCase>();
        for (TestCase tc : tests) {
            if (type.equalsIgnoreCase(tc.getType())) {
                list.add(tc);
            }
        }
        return list;
    }

    /**
     * Return the size of test cases contained in the set
     * 
     * @return size
     */
    public int size() {
        return tests.size();
    }

    /**
     * Classify the test cases in the set by their test type
     * 
     * @return Hash [test type, list of test cases of this test type]
     */
    public HashMap<String, List<TestCase>> classify() {
        HashMap<String, List<TestCase>> group = new HashMap<String, List<TestCase>>();
        for (TestCase tc : tests) {
            String type = tc.getType();
            if (group.containsKey(type)) {
                group.get(type).add(tc);
            } else {
                List ls = new ArrayList();
                ls.add(tc);
                group.put(type, ls);
            }
        }

        return group;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int passed = 0, failed = 0, na = 0;
        for (TestCase tc : tests) {
            sb.append(tc);

            if (tc.isTested()) {
                if (tc.isPassed()) {
                    passed++;
                } else {
                    failed++;
                }
            } else {
                na++;
            }
        }

        String info = "Test set:" + this.name + "[ ]\n";
        info += "total test cases:" + size();
        info += "[not tested:" + na + "]\n";
        info += "[ passed:" + passed + "],[failed:" + failed + "], [" + passed / (passed + failed) + "]\n";
        sb.insert(0, info);
        return sb.toString();
    }
}
