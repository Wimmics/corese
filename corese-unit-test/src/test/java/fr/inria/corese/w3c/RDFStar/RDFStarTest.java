package fr.inria.corese.w3c.RDFStar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.result.SPARQLJSONResult;
import fr.inria.corese.core.load.result.SPARQLResult;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * error in w3c test:
 * sparql/eval trs:sparql-star-op-3
 */
public class RDFStarTest {

    // nb success:216
    // nb failure:2
    // nb variant:1
    @Test
    public void test() throws EngineException, LoadException, IOException {
        System.out.println("Hello World!");
        new RDFStarTest().process();

    }

    static final String data = RDFStarTest.class.getResource("/data/rdf-star-main/tests/").getPath();
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String mf = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";
    static final String qt = "http://www.w3.org/2001/sw/DataAccess/tests/test-query#";
    static final String ut = "http://www.w3.org/2009/sparql/tests/test-update#";
    boolean trace = false;
    RDFStarReport report = new RDFStarReport("", "rdf star");
    int nbsuc = 0;
    int nbfail = 0;
    int nbvariant = 0;

    // RDFStarTest() {
    // report = new RDFStarReport("", "rdf star");
    // }

    public static void main(String[] args) throws LoadException, EngineException, IOException {
        new RDFStarTest().process();
    }

    void report(String test, boolean suc) {
        report.result(test, suc);
        if (suc) {
            nbsuc++;
        } else {
            nbfail++;
        }
    }

    /**
     * @todo:
     *        "test"@en-us not sameTerm "test"@en-US
     */

    void process() throws LoadException, EngineException, IOException {
        Property.set(Property.Value.RDF_STAR, true);
        // nested subject literal is an error
        // nested 042 does not unify with nested 42
        // nested undefined literal does not unify with bnode
        Property.set(Property.Value.RDF_STAR_VALIDATION, true);
        Property.set(Property.Value.LOAD_IN_DEFAULT_GRAPH, true);
        // Property.set(Property.Value.SPARQL_COMPLIANT, true);

        for (IDatatype dt : RDFStarTest.this.manifest()) {
            String name = dt.getLabel();
            manifest(name);
        }

        report.write(data + "/report.ttl");
        System.out.println("nb success: " + nbsuc);
        System.out.println("nb failure: " + nbfail);
        System.out.println("nb variant: " + nbvariant);
    }

    // main manifest
    void manifest(String name) throws LoadException, EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(name);
        g.init();
        for (Edge ee : g.getEdges(mf + "entries")) {
            manifest(g, name, g.getDatatypeList(ee.getObjectNode()));
        }
    }

    // manifest name
    void manifest(Graph g, String name, List<IDatatype> testList) throws EngineException {
        System.out.println(name);
        if (trace)
            System.out.println("process: " + testList);

        for (IDatatype dt : testList) {
            test(g, dt);
        }
    }

    String getValue(Graph g, String predicate, String subject) {
        Edge edge = g.getEdge(predicate, subject, 0);
        if (edge == null) {
            return null;
        }
        return edge.getObjectNode().getLabel();
    }

    IDatatype getDatatypeValue(Graph g, String predicate, String subject) {
        Edge edge = g.getEdge(predicate, subject, 0);
        if (edge == null) {
            return null;
        }
        return edge.getObjectValue();
    }

    /**
     * test: subject uri of a test
     */
    void test(Graph gg, IDatatype test) throws EngineException {
        System.out.println("test: " + test);
        String subject = test.getLabel();
        Edge eaction = gg.getEdge(mf + "action", subject, 0);
        Edge eresult = gg.getEdge(mf + "result", subject, 0);
        Edge etype = gg.getEdge(NSManager.RDF + "type", subject, 0);
        String entailment = getValue(gg, mf + "entailmentRegime", subject);
        String type = etype == null ? "undefined" : etype.getObjectNode().getLabel();
        String result = null;
        Boolean bresult = null;
        String comment = getValue(gg, rdfs + "comment", subject);
        if (comment != null) {
            System.out.println("comment: " + comment);
        }

        if (eresult != null) {
            Node nresult = eresult.getObjectNode();
            IDatatype dt = eresult.getObjectValue();
            if (nresult.isBlank()) {
                eresult = gg.getEdge(ut + "data", nresult, 0);
                if (eresult != null) {
                    result = eresult.getObjectNode().getLabel();
                }
            } else if (dt.isBoolean()) {
                bresult = dt.booleanValue();
            } else {
                result = nresult.getLabel();
            }
        }

        boolean suc = true;

        if (eaction != null) {
            Node node = eaction.getObjectNode();
            if (trace)
                System.out.println("action: " + node.getLabel());
            if (trace)
                if (result != null)
                    System.out.println("result: " + result);

            if (node.isBlank()) {
                // [qt:query sparql ; qt:data rdf]
                Edge equery = gg.getEdge(qt + "query", node, 0);
                Edge erequest = gg.getEdge(qt + "request", node, 0);
                if (erequest == null) {
                    erequest = gg.getEdge(ut + "request", node, 0);
                }
                Edge edata = gg.getEdge(qt + "data", node, 0);
                if (edata == null) {
                    edata = gg.getEdge(ut + "data", node, 0);
                }

                boolean isQuery = equery != null;
                boolean isRequest = erequest != null;

                if ((isQuery || isRequest) && edata != null) {
                    if (trace)
                        if (isQuery)
                            System.out.println("query: " + equery.getObjectNode().getLabel());
                    if (trace)
                        if (isRequest)
                            System.out.println("request: " + erequest.getObjectNode().getLabel());
                    if (trace)
                        System.out.println("data: " + edata.getObjectNode().getLabel());

                    suc = query(test.getLabel(), edata.getObjectNode().getLabel(),
                            isQuery ? equery.getObjectNode().getLabel() : erequest.getObjectNode().getLabel(),
                            result, type, entailment, isQuery);
                } else {
                    if (trace)
                        System.out.println("query: " + equery);
                    if (trace)
                        System.out.println("data: " + edata);
                }
            } else {
                suc = action(test.getLabel(), node.getLabel(), result, type, entailment, bresult);
            }
        } else {
            if (trace)
                System.out.println("action: " + eaction);
        }

        report(subject, suc);
    }

    void load(Load ld, String name) throws LoadException, EngineException {
        if (name.endsWith(".rq") || name.endsWith(".ru")) {
            String q = QueryLoad.create().readWE(name);
            QueryProcess exec = QueryProcess.create(Graph.create());
            exec.compile(q);
        } else {
            ld.parse(name);
        }
    }

    void entailment(Graph g, String entailment) {
        if (entailment != null && entailment.equals("RDFS-Plus")) {
            RuleEngine re = RuleEngine.create(g);
            re.setProfile(RuleEngine.OWL_RL);
            try {
                re.process();
            } catch (EngineException ex) {
                Logger.getLogger(RDFStarTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // test with sparql query
    boolean query(String test, String rdf, String query, String result, String type, String entailment,
            boolean isQuery) {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            load(ld, rdf);
        } catch (EngineException | LoadException ex) {
            System.out.println("syntax error: " + rdf);
            System.out.println(type);
            System.out.println(ex.getMessage());
            return false;
        }

        entailment(g, entailment);

        QueryLoad ql = QueryLoad.create();
        try {
            String q = ql.readWE(query);
            if (trace)
                System.out.println("query:\n" + q);
            // if (type.contains("Negative")) System.out.println(type);
            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(q);
            Mappings map2 = result(result);
            // String strResult = ql.readWE(result);
            return genericompare(test, q, g, map, map2, !type.contains("Negative"));

        } catch (LoadException | EngineException | IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(RDFStarTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // test with load
    boolean action(String test, String name, String result, String type, String entailment, Boolean bresult)
            throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            load(ld, name);
            if (type.contains("Negative")) {
                System.out.println(type);
                if (result == null && bresult == null) {
                    System.out.println(g.display());
                }
            }
        } catch (LoadException | EngineException ex) {
            System.out.println("syntax error detected: " + name);
            System.out.println(type);
            if (type.contains("Negative")) {
                // we detect failure: test is a success
                return true;
            } else {
                System.out.println(ex.getMessage());
                return false;
            }
        }

        entailment(g, entailment);
        boolean suc = true;

        if (result != null) {
            Graph gres = Graph.create();
            Load ldr = Load.create(gres);
            QueryLoad ql = QueryLoad.create();
            try {
                // String myresult = check(name, result);
                load(ldr, result);
                // String rdfstr = ql.readWE(result);
                suc = compare(test, g, gres, !type.contains("Negative"));
            } catch (LoadException ex) {
                Logger.getLogger(RDFStarTest.class.getName()).log(Level.SEVERE, null, ex);
                suc = false;
            }
        } else if (bresult != null) {
            suc = g.isCorrect() == bresult;
            if (!suc) {
                System.out.println("corese graph: " + g.isCorrect() + " w3c result: " + bresult);
            }
        }
        return suc;
    }

    boolean genericompare(String test, String q, Graph g, Mappings m1, Mappings m2, boolean positive) {
        if (m2.getGraph() != null) {
            if (m1.getGraph() == null) {
                return compare(test, g, (Graph) m2.getGraph(), positive);
            } else {
                return compare(test, (Graph) m1.getGraph(), (Graph) m2.getGraph(), positive);
            }
        } else {
            return compare(q, m1, m2);
        }
    }

    boolean compare(String q, Mappings m1, Mappings m2) {
        return myCompare(q, m1, m2);
    }

    boolean myCompare(String q, Mappings m1, Mappings m2) {
        Compare cp = new Compare(m1, m2);
        return cp.check(m1, m2);
    }

    void basicCompare(String q, Mappings m1, Mappings m2) {
        if (m1.size() != m2.size()) {
            System.out.println("query:\n" + q);
            display(m1, m2);
        }
    }

    void compare(Graph g, Graph r) {

    }

    boolean compare(String test, Graph g, Graph r, boolean positive) {
        g.init();
        r.init();
        try {
            boolean b = query(test, g, r, positive);
            if (b != positive) {
                System.out.println("corese: " + b + " w3c: " + positive);
                display(g, r);
            }
            return b == positive;
        } catch (EngineException ex) {
            Logger.getLogger(RDFStarTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //
    /**
     * translate result graph into sparql ast query
     * return as result the projection of result graph ast query on input graph
     * semantics: input graph => result graph
     *
     */
    boolean query(String test, Graph ginput, Graph gresult, boolean positive)
            throws EngineException {

        Mappings map = query(test, ginput, gresult);

        if (map.size() > 0 != positive) {
            System.out.println("result: " + map.size() + " positive: " + positive);
            System.out.println(map.getAST());
            System.out.println("w3c trig graph:");
            System.out.println(TripleFormat.create(gresult, true));
            System.out.println(TripleFormat.create(gresult));
        }
        return map.size() > 0;
    }

    Mappings query(String test, Graph ginput, Graph gresult) throws EngineException {
        if (test.contains("semantics#malformed-literal-bnode-neg")) {
            return sparqlQuery(test, ginput, gresult);
        } else {
            return graphQuery(test, ginput, gresult);
        }
    }

    Mappings sparqlQuery(String test, Graph ginput, Graph gresult) throws EngineException {
        String q = "select * where {?s ?p ?o} limit 1";
        if (test.contains("semantics#malformed-literal-bnode-neg")) {
            // negative test: query success => test failure
            q = "# check that malformed literal in nested triple does not entail bnode\n"
                    + "prefix : <http://example.com/ns#>\n"
                    + "select * where {"
                    + "<< :a :b ?x>> :p1 :o1 "
                    + "filter isBlank(?x)"
                    + "}";
        }
        QueryProcess exec = QueryProcess.create(ginput);
        Mappings map = exec.query(q);
        System.out.println("sparql:\n" + q);
        System.out.println("result:\n" + map.size() + " " + map);
        System.out.println("input graph:\n" + ginput.display());
        System.out.println("result graph:\n" + gresult.display());
        nbvariant++;
        return map;
    }

    Mappings graphQuery(String test, Graph ginput, Graph gresult) throws EngineException {
        QueryProcess exec = QueryProcess.create(ginput);
        Mappings map = exec.queryTrig(gresult);
        return map;
    }

    void display(Graph g, Graph r) {
        System.out.println("corese:");
        display(g);
        System.out.println("rdf star:");
        display(r);
    }

    void display(Mappings m1, Mappings m2) {
        System.out.println("corese:");
        display(m1);

        System.out.println("rdf star:");
        display(m2);
    }

    void display(Mappings map) {
        if (map.getGraph() == null) {
            System.out.println(map);
        } else {
            display((Graph) map.getGraph());
        }
    }

    void display(Graph g) {
        System.out.println("size: " + g.size());
        System.out.println(g.display());
    }

    Mappings result(String name)
            throws IOException, ParserConfigurationException, SAXException, LoadException, EngineException {
        Mappings map = null;
        if (name.endsWith(".srj")) {
            SPARQLJSONResult json = SPARQLJSONResult.create();
            map = json.parse(name);

        } else if (name.endsWith(".srx")) {
            SPARQLResult xml = SPARQLResult.create();
            map = xml.parse(name);
        } else if (name.endsWith(".ttl") || name.endsWith(".trig") || name.endsWith(".nq")) {
            Graph g = Graph.create();
            Load ld = Load.create(g);
            load(ld, name);
            map = new Mappings();
            map.setGraph(g);
        }
        return map;
    }

    void compare(boolean corese, boolean w3c) {
        if (corese != w3c) {
            System.out.println("corese graph: " + corese + " w3c result: " + w3c);
        }
    }

    List<IDatatype> manifest() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "manifest.ttl");
        for (Edge ee : g.getEdges(mf + "include")) {
            return g.getDatatypeList(ee.getObjectNode().getDatatypeValue());
        }
        return new ArrayList<>();
    }

}
