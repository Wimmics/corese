package fr.inria.corese.test.w3c.model;

import fr.inria.corese.kgram.core.Mapping;

/**
 * Encapsulate a basic single test case
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public abstract class TestCase {

    private String name;//mf:name
    private String comment;//rdfs:comment
    private String approval;//rdft:approval
    private boolean expectedResult;//expected excution(action) result, positve|negative
    private boolean realResult;//execution(action) result 
    private boolean tested = false;
    private boolean passed = false;
    private String type;//type/group/category
    private String uri;

    public TestCase() {
    }

    public TestCase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public boolean getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(boolean expectedResult) {
        this.expectedResult = expectedResult;
    }

    public boolean getRealResult() {
        return realResult;
    }

    public void setRealResult(boolean realResult) {
        this.realResult = realResult;
    }

    public boolean isTested() {
        return tested;
    }

    public void setTested(boolean tested) {
        this.tested = tested;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String res = this.isPassed() ? "PASS" : "FAIL";
        String status = tested ? res : "NA";
        sb.append(this.name + " [" + status + "] [" + this.type + "]\n");
        return sb.toString();
    }

    /**
     * Generate string in HTML format, by default it is the same as toString
     * needs to be customized in sub-classes
     * 
     * @return Html string
     */
    public String toHtmlString() {
        return this.toString();
    }

    /**
     * abstract method that used to convert a query mapping to a object of test case
     * 
     * @param m Query mapping
     * @param types Test types
     */
    public abstract void toTestCase(Mapping m, Object[][] types);
}
