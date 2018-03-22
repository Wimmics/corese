package fr.inria.corese.test.w3c.turtle;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.test.w3c.model.TestCase;
import static fr.inria.corese.test.w3c.model.TestHelper.isPositiveTest;

/**
 * Encapsulate a single turtle test case
 *
 * @author Fuqi Song wimmics inria i3s
 * @date Feb. 2014
 */
public class TurtleTestCase extends TestCase {

    private String action;//mf:action
    private String result;//mf:result

    public TurtleTestCase() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public void toTestCase(Mapping m, Object[][] testTypes) {
        setUri(m.getNode("?test").getLabel());
        setName(m.getNode("?name").getLabel());
        Node a = m.getNode("?approval");
        if (a != null) {
            setApproval(a.getLabel());
        }
        setComment(m.getNode("?comment").getLabel());
        setAction(m.getNode("?action").getLabel());
        setType(m.getNode("?type").getLabel());

        Node r = m.getNode("?result");
        if (r != null) {
            setResult(r.getLabel());
        }

        setExpectedResult(isPositiveTest(testTypes, getType()));
    }

    @Override
    public String toHtmlString() {
        StringBuilder sb = new StringBuilder();
        QueryLoad ql = QueryLoad.create();

        //1. print the name of test and type
        String res = this.isPassed() ? "PASS" : "FAIL";
        String status = this.isTested() ? res : "NA";
        sb.append(" <a href=\"" + this.getUri() + "\"> " + this.getName() + "[" + status + "]</a> ");
        sb.append("[Type: " + this.getType().replace("http://www.w3.org/ns/rdftest#", "") + "]<br>");
        sb.append("# " + repBraces(this.getComment()) + "<br>");

        //2. print input source info.
        String action = this.getAction();
        sb.append("<pre> <a href=\"" + action + "\">[input:" + getFileName(action) + "]</a> <br>");

        String intput = ql.read(action);
        sb.append(repBraces(intput));
        sb.append("<font color=\"red\"> ** corese graph**<br>" + repBraces(this.printGraph(action)) + "</font><br>" + "</pre>");

        //3. print output source info. if any
        String result = this.getResult();
        if (result != null) {
            sb.append("<pre> <a href=\"" + result + "\">[output:" + getFileName(result) + "]</a> <br>");
            String output = ql.read(result);
            sb.append(repBraces(output));
            sb.append("<font color=\"red\"> ** corese graph**<br>" + repBraces(this.printGraph(result)) + "</font>" + "</pre>");
        }
        return sb.toString();
    }

    //Filter the path of file
    private String getFileName(String fullName) {
        return fullName.substring(fullName.lastIndexOf("/", fullName.length()));
    }

    //Repalce < > with "&lt;" "&gt;"
    private String repBraces(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }

    //Print graph using TripleFormat
    private String printGraph(String source) {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadWE(source);

        } catch (LoadException ex) {
            return "[Graph load exception:]" + ex.toString();
        } catch (Error e) {
            return "[Graph load error:]" + e.toString();
        }
        TripleFormat tf = TripleFormat.create(g, true);
        return tf.toString();
    }
}
