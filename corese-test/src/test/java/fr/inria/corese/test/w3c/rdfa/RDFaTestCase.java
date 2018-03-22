package fr.inria.corese.test.w3c.rdfa;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.test.w3c.model.TestCase;

/**
 * RDFa test case
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class RDFaTestCase extends TestCase {

    private String sparql;
    private String data;
    private String queryForm;
    private boolean result;//mf:result

    public RDFaTestCase() {
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getQueryForm() {
        return queryForm;
    }

    public void setQueryForm(String queryForm) {
        this.queryForm = queryForm;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public void toTestCase(Mapping m, Object[][] types) {
        // Object[][] types is useless for RDFa test cases

        setUri(m.getNode("?test").getLabel());
        setName(m.getNode("?name").getLabel());
        setType(m.getNode("?type").getLabel());

        setComment(m.getNode("?comment").getLabel());

        setData(m.getNode("?data").getLabel());
        setSparql(m.getNode("?sparql").getLabel());

        Node r = m.getNode("?result");
        boolean b = Boolean.valueOf(r.getLabel());
        setResult(b);
        setExpectedResult(b);

    }
}
