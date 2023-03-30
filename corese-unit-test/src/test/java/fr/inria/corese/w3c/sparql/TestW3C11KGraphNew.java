package fr.inria.corese.w3c.sparql;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.logic.RDF;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.print.CSVFormat;
import fr.inria.corese.core.print.JSONFormat;
import fr.inria.corese.core.print.TSVFormat;
import fr.inria.corese.core.print.TemplateFormat;
import fr.inria.corese.core.query.ProducerImpl;
import fr.inria.corese.core.query.ProviderImpl;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.exceptions.UndefinedExpressionException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.printer.SPIN;

/**
 * KGRAM benchmark on W3C SPARQL 1.1 Query & Update Test cases
 *
 * entailment:
 *
 * error in w3c ? rdfs08.rq inherit rdfs:range ? rdfs11.rq subclassof is
 * reflexive
 *
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class TestW3C11KGraphNew {
    // root of test case RDF data

    // static final String data0 =
    // "/home/corby/workspace/coreseV2/src/test/resources/data/";
    // static final String data =
    // "/home/corby/NetBeansProjects/kgram/trunk/kgtool/src/test/resources/data/";
    static final String data = TestW3C11KGraphNew.class.getResource("/data/").getPath();
    // old local copy:
    // static final String froot = data +
    // "w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/";
    // new local copy:
    static final String froot = data + "w3c-sparql11/sparql11-test-suite/";
    static final String root0 = data + "w3c-sparql10/data-r2/";
    static final String frdf = data + "w3c-rdf/rdf-mt/tests/";
    static final String wrdf = "https://dvcs.w3.org/hg/rdf/raw-file/default/rdf-mt/tests/";

    // W3C test case:
    static final String wroot = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/";
    // static final String RULE = "/net/servers/ftp-sop/wimmics/soft/rule/rdfs.rul";
    static final String RULE = data + "w3c-sparql11/data/rdfs.rul";
    // static final String root0 = data0 + "test-suite-archive/data-r2/";
    static final String DC = "http://purl.org/dc/elements/1.1/";

    static final String TTL = "/home/corby/AData/work/w3c/";
    static int cc = 0;
    /**
     * **********
     * TO SET: **********
     */
    // ** directory where data are:
    static final String root = froot;
    // ** directory where to save earl report:
    static final String more = data + "earl/";
    // query
    static final String man = "prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> "
            + "prefix qt:  <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> "
            + "prefix sd:  <http://www.w3.org/ns/sparql-service-description#> "
            + "prefix ent: <http://www.w3.org/ns/entailment/> "
            + "prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> "
            + "select * where {"
            + "?x mf:action ?a "
            + "minus {?x dawgt:approval dawgt:Withdrawn}"
            + "optional {?a qt:query ?q} "
            + "optional {?a qt:data ?d}"
            + "optional {?a qt:graphData ?g} "
            + "optional {?a sd:entailmentRegime ?ent}"
            + "optional {?x sd:entailmentRegime ?ent}"
            + "optional {?x mf:result ?r}"
            + "optional {?x rdf:type ?t}"
            + "optional {?x mf:feature ?fq}"
            + "optional { ?x mf:recognizedDatatypes/rdf:rest*/rdf:first ?rdt }"
            + "optional { ?x mf:unrecognizedDatatypes/rdf:rest*/rdf:first ?udt }"
            + "optional { ?a qt:serviceData [ qt:endpoint ?ep ; qt:data ?ed ] }"
            + "{?man rdf:first ?x} "
            + "} "
            + "group by ?x order by ?q ";
    // update
    static final String man2 = "prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> "
            + "prefix ut:     <http://www.w3.org/2009/sparql/tests/test-update#> "
            + "prefix sd:  <http://www.w3.org/ns/sparql-service-description#> "
            + "prefix ent: <http://www.w3.org/ns/entailment/> "
            + "prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> "
            + "select * where {"
            + "?x mf:action ?a "
            + "minus {?x dawgt:approval dawgt:Withdrawn}"
            + "optional {?a ut:request ?q} "
            + "optional {?a ut:data ?d}"
            + "optional {?a ut:graphData [?p ?g; rdfs:label ?name] "
            + "filter(?p = ut:data || ?p = ut:graph) } "
            + "optional {?x sd:entailmentRegime ?ent}"
            + "optional {?x mf:result ?ee filter(! exists {?ee ?qq ?yy}) }"
            + "optional {?x mf:result [ut:data ?r] }"
            + "optional {?x mf:result [ut:graphData [rdfs:label ?nres; ?pp ?gr ]] "
            + "filter(?pp = ut:data || ?pp = ut:graph) }"
            + "optional {?x rdf:type ?t}"
            + "{?man rdf:first ?x} "
            + "} "
            + "group by ?x order by ?q ";
    static String ENTAILMENT = "http://www.w3.org/ns/entailment/";
    static String NEGATIVE = "Negative";
    static String POSITIVE = "Positive";
    Testing tok, tko;
    int gok = 0, gko = 0,
            total = 0, nbtest = 0;
    boolean verbose = true;
    boolean sparql1 = true;
    boolean strict = true;
    boolean trace = true;
    List<String> errors = new ArrayList<String>();
    List<String> names = new ArrayList<String>();
    Earl earl;
    private Graph spinGraph = Graph.create();

    class Testing extends Hashtable<String, Integer> {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // before2();
        // Graph.setEdgeMetadataDefault(true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // after2();
    }

    static void before() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        QuerySolver.setVisitorable(true);
        DatatypeMap.TRUE.setPublicDatatypeValue(DatatypeMap.newList());

        String q = "@public @error "
                + "function us:error(?e, ?x , ?y) { "
                + "xt:print('****************** error') ; "
                + "xt:print(java:getAST(xt:query())) ;"
                + "xt:display( ?e, ?x, ?y) ; "
                + "xt:add(ds:getPublicDatatypeValue(true), xt:list(?e, ?x, ?y)) ;"
                + "error() "
                + "}";

        try {
            Query qq = exec.compile(q);
        } catch (EngineException ex) {
            System.out.println(ex);
            Logger.getLogger(TestW3C11KGraphNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    static void after() {
        System.out.println("After");
        int i = 0;
        for (IDatatype dt : DatatypeMap.TRUE.getPublicDatatypeValue().getValues()) {
            System.out.println(i++ + " " + dt);
        }
    }

    static void before2() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        QuerySolver.setVisitorable(true);

        IDatatype map = DatatypeMap.map();
        map.set(DatatypeMap.newResource(NSManager.USER, "error"), DatatypeMap.newList());
        DatatypeMap.setPublicDatatypeValue(map);

        String q = "@public {"
                + "function us:start() {"
                + "let (?map = xt:map()) {"
                + "xt:set(?map, us:error, xt:list()) ;"
                + "ds:setPublicDatatypeValue(true, ?map)"
                + "}"
                + "}"
                + "@error "
                + "function us:error(?e, ?x , ?y) { "
                + "us:recerror(?e, ?x, ?y) ; "
                + "error() "
                + "}"
                + "@filter "
                + "function us:filter(?g, ?e, ?b) { "
                // + "xt:print(?e);"
                + "us:record(?e) ;"
                + "?b "
                + "}"
                + "@select "
                + "function us:select(?e, ?b) { "
                // + "xt:print(?e);"
                + "us:record(?e) ;"
                + "?b "
                + "}"
                + "function us:record(?e) {"
                + "if (java:isTerm(?e)) {"
                + "xt:set(ds:getPublicDatatypeValue(true), java:getLabel(?e), java:getLabel(?e)) ;"
                + "let (( | ?l) = ?e) {"
                + "for (?ee in ?e) {"
                + "us:record(?ee)"
                + "}"
                + "}"
                + "}"
                + "}"
                + "function us:recerror(?e, ?x, ?y) {"
                + "xt:add(xt:get(ds:getPublicDatatypeValue(true), us:error), xt:list(?e, ?x, ?y))"
                + "}"
                + "}";

        try {
            Query qq = exec.compile(q);
            // exec.funcall(NSManager.USER + "start", new IDatatype[0]);
        } catch (EngineException ex) {
            System.out.println(ex);
            Logger.getLogger(TestW3C11KGraphNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    static void after2() {
        System.out.println("After");
        int i = 0;
        for (IDatatype dt : DatatypeMap.getPublicDatatypeValue()) {
            System.out.println(i++ + " " + dt.getValueList().get(1));
        }
        i = 0;
        for (IDatatype dt : DatatypeMap.getPublicDatatypeValue()
                .get(DatatypeMap.newResource(NSManager.USER, "error"))) {
            System.out.println(i++ + " " + dt);
        }
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
        // doSetUp();
    }

    void doSetUp() {
        String q = "@public @update "
                + "function us:myupdate(del, ins) {"
                + "if (xt:size(del) > 0, xt:print('delete:', del),  true);"
                + "if (xt:size(ins) > 0, xt:print('insert:', ins),  true)"
                + "}";
        QueryProcess exec = QueryProcess.create(Graph.create());
        try {
            exec.compile(q);
        } catch (EngineException ex) {
            Logger.getLogger(TestW3C11KGraphNew.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
    }

    public TestW3C11KGraphNew() {
        DatatypeMap.setSPARQLCompliant(true);
        // fr.inria.corese.core.NodeImpl.byIDatatype = true;
        // Load.setDefaultGraphValue(true);
        tko = new Testing();
        tok = new Testing();
        earl = new Earl();
        // QueryProcess.testAlgebra(true);
        // Graph.METADATA_DEFAULT = true;
    }

    // Number of known failures: 2
    public static void main(String[] args) {
        int nb_errors = new TestW3C11KGraphNew().process();
        Assert.assertEquals(2, nb_errors);
    }

    // Number of known failures: 2
    @Test
    public void mytest() {
        int nb_errors = new TestW3C11KGraphNew().process();
        Assert.assertEquals(2, nb_errors);
    }

    /**
     * SPARQL 1.0
     */
    void test0() {
        sparql1 = false;
        test(root0 + "distinct");
        test(root0 + "algebra");
        test(root0 + "ask");
        test(root0 + "basic");
        test(root0 + "bnode-coreference");
        test(root0 + "optional");
        test(root0 + "boolean-effective-value");
        test(root0 + "bound");
        test(root0 + "optional-filter");
        test(root0 + "cast");
        test(root0 + "expr-equals");
        test(root0 + "expr-ops");
        test(root0 + "graph");
        test(root0 + "i18n");
        test(root0 + "regex");
        test(root0 + "solution-seq");
        test(root0 + "triple-match");
        test(root0 + "type-promotion");
        test(root0 + "syntax-sparql1");
        test(root0 + "syntax-sparql2");
        test(root0 + "syntax-sparql3");
        test(root0 + "syntax-sparql4");
        test(root0 + "syntax-sparql5");
        test(root0 + "open-world");
        test(root0 + "sort");
        test(root0 + "dataset");
        test(root0 + "reduced");
        test(root0 + "expr-builtin");
        test(root0 + "construct");
    }

    void test00() {
        sparql1 = false;
        test(root0 + "open-world");

    }

    void testRDF() {
        sparql1 = true;

        test(wrdf, "manifest.ttl", false, true, true);
        // test(wrdf, "manifest-az.ttl", false, true, true);
    }

    /**
     * SPARQL 1.1
     */
    void test1() {
        sparql1 = true;

        // test(root + "service");
        test(root + "syntax-fed");
        test(root + "syntax-query");
        test(root + "negation");
        test(root + "project-expression");
        test(root + "subquery");
        test(root + "construct");
        test(root + "grouping");
        test(root + "functions");
        test(root + "json-res");
        test(root + "csv-tsv-res");
        test(root + "aggregates");
        test(root + "property-path");
        test(root + "bind");
        test(root + "bindings");
        test(root + "exists");

        test(root + "subquery");

    }

    void testUpdate() {
        sparql1 = true;

        test(root + "syntax-update-1", true);
        test(root + "syntax-update-2", true);
        test(root + "basic-update", true);
        test(root + "delete-data", true);
        test(root + "delete-where", true);
        test(root + "delete", true);
        test(root + "delete-insert", true);
        test(root + "update-silent", true);

        test(root + "drop", true);
        test(root + "clear", true);
        test(root + "add", true);
        test(root + "move", true);
        test(root + "copy", true);
    }

    void test() {
        sparql1 = true;
        // test(root + "functions", false);
        test(root + "bind");

    }

    void testelem() {
        sparql1 = false;
        test(root0 + "bind");
    }

    public int process() {
        return process(1);
    }

    // @Test
    public int process(int version) {
        gok = 0;
        gko = 0;

        // 25 (june 2016)
        // 28 errors 416 success
        // 29 errors 04/05/12
        // 31 errors 11/05/12 because of optional DOT
        // QueryProcess.setJoin(true);
        // Graph.setValueTable(true);
        // Graph.setCompareIndex(true);
        if (version == 1) {
            test1();
            testUpdate();
        } else {
            test0(); // 25 errorstest
        }

        ArrayList<String> vec = new ArrayList<String>();
        for (String key : tok.keySet()) {
            vec.add(key);
        }
        Collections.sort(vec);

        total = gok + gko;

        println("<html><head>");
        println("<title>Corese 3.0/KGRAM  SPARQL 1.1 Query &amp; Update W3C Test cases</title>");

        println("<style type = 'text/css'>");
        println(".success   {background:lightgreen}");
        println("body {font-family: Verdana, Arial, Helvetica, Geneva, sans-serif}");
        println("</style>");
        println("<link rel='stylesheet' href='kgram.css' type='text/css'  />");
        println("</head><body>");
        println("<h2>Corese 3.0 KGRAM  SPARQL 1.1 Query &amp; Update W3C Test cases</h2>");
        println("<p> Olivier Corby - Wimmics - INRIA I3S</p>");
        println("<p>" + new Date() + " - Corese 3.0 <a href='http://wimmics.inria.fr/corese'>homepage</a></p>");
        // println("<p><a href='http://www.w3.org/2001/sw/DataAccess/tests/r2'>SPARQL
        // test cases</a></p>");
        println("<table border='1'>");
        println("<tr>");
        println("<th/> <th>test</th><th>success</th><th>failure</th><th>ratio</th>");
        println("</tr>");

        println("<th/> <th>total</th><th>" + gok + "</th><th>" + gko + "</th><th>"
                + (100 * gok) / (total) + "%</th>");
        int i = 1;

        for (String key : vec) {
            int ind = key.lastIndexOf("/");
            String title = key.substring(0, ind);
            ind = title.lastIndexOf("/");
            title = title.substring(ind + 1);

            int suc = tok.get(key);
            int fail = tko.get(key);
            String att = "";
            if (fail == 0 && suc != 0) {
                att = " class='success'";
            }
            System.out.print("<tr" + att + ">");
            System.out.print("<th>" + i++ + "</th>");
            System.out.print("<th>" + title + "</th>");
            System.out.print("<td>" + suc + "</td>");
            System.out.print("<td>" + fail + "</td>");

            int ratio = 0;
            try {
                ratio = 100 * suc / (suc + fail);
            } catch (Exception e) {
            }
            print("<td>" + ratio + "%</td>");
            println("</tr>");
        }

        println("</table>");
        int j = 0, k = 1;
        if (errors.size() > 0) {
            println("<h2>Failure</h2>");
        }
        for (String name : names) {
            if (name.indexOf("data-sparql11") != -1) {
                println(k++ + ": " + name.substring(name.indexOf("data-sparql11")));
            } else {
                println(k++ + ": " + name);
            }

            println("<pre>\n" + errors.get(j++) + "\n</pre>");
            println("");
        }
        println("</body><html>");

        if (total != nbtest) {
            println("*** Missing result: " + total + " " + nbtest);
        }

        process(spinGraph);

        earl.toFile(more + "earl.ttl");

        Graph ge = Graph.create();
        Load le = Load.create(ge);
        try {
            le.parse(more + "earl.ttl");
            // System.out.println(ge);
        } catch (LoadException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }

        // There are 5 known erros
        return errors.size();

        // Processor.finish();
    }

    void println(String str) {
        System.out.println(str);
    }

    void print(String str) {
        System.out.print(str);
    }

    /**
     * Test one manifest
     */
    void skip(String path) {
        test(path, false, false, false);
    }

    void test(String path) {
        test(path, false, false, true);
    }

    void test(String path, boolean update) {
        test(path, update, false, true);
    }

    /**
     * Process one manifest Load manifest.ttl in a Graph SPARQL Query the graph
     * to get the query list, input and output list.
     */
    void test(String path, boolean update, boolean isRDF, boolean process) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        test(path, "manifest.ttl", update, isRDF, process);
    }

    void test(String path, String fman, boolean update, boolean isRDF, boolean process) {
        String manifest = path + fman;

        try {
            System.out.println("** Load: " + pp(manifest));

            Graph g = Graph.create();
            Load load = Load.create(g);
            try {
                load.parse(manifest);
            } catch (LoadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            QueryProcess exec = QueryProcess.create(g);
            exec.setListGroup(true);

            String qman = man;
            if (update) {
                qman = man2;
            }

            Mappings res = exec.query(qman);
            System.out.println("** NB test: " + res.size());
            // System.out.println(res);
            nbtest += res.size();

            int ok = 0, ko = 0;

            for (Mapping map : res) {
                // each map is a test case
                if (!process) {
                    String test = getValue(map, "?x");
                    earl.skip(test);
                } else if (query(path, map, update, isRDF)) {
                    ok++;
                } else {
                    ko++;
                }
            }

            gok += ok;
            gko += ko;

            tok.put(pp(manifest), ok);
            tko.put(pp(manifest), ko);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            System.out.println(manifest);
            e.printStackTrace();
        }
    }

    String getValue(Mapping map, String var) {
        if (map.getNode(var) != null) {
            return map.getNode(var).getLabel();
        }
        return null;
    }

    String[] getValues(Mapping map, String var) {
        List<Node> list = map.getNodes(var, true);
        String[] fnamed = new String[list.size()];
        int j = 0;
        for (Node n : list) {
            fnamed[j++] = n.getLabel();
        }
        return fnamed;
    }

    List<String> getValueList(Mapping map, String var) {
        List<Node> list = map.getNodes(var, true);
        ArrayList<String> fnamed = new ArrayList<String>();
        for (Node n : list) {
            fnamed.add(n.getLabel());
        }
        return fnamed;
    }

    /**
     * One test
     *
     * @param fquery
     * @param fdefault RDF file for default graph
     * @param fresult
     * @param fnamed   RDF files for named graphs
     * @param ent      entailment
     *
     * @return
     */
    boolean query(String path, Mapping map, boolean isUpdate, boolean isRDF) {
        // System.out.println(map);

        String defbase = uri(path + File.separator);
        // Dataset with named graphs only
        Dataset input = new Dataset().init(map.getMappings(), "?name", "?g");
        Dataset output = new Dataset().init(map.getMappings(), "?nres", "?gr");
        // dEFAULT GRAPH
        List<String> fdefault = getValueList(map, "?d");

        String test = getValue(map, "?x");
        String fquery = getValue(map, "?q");
        String fresult = getValue(map, "?r");
        String ent = getValue(map, "?ent");
        String type = getValue(map, "?t");
        String fq = getValue(map, "?fq");

        String[] ep = getValues(map, "?ep");
        String[] ed = getValues(map, "?ed");

        List<Node> rdt = map.getNodes("?rdt", true);
        List<Node> udt = map.getNodes("?udt", true);

        boolean isEmpty = getValue(map, "?ee") != null;
        boolean isBlankResult = false;
        boolean isJSON = false, isCSV = false, isTSV = false;
        // boolean rdfs = ent != null && ent.equals(ENTAILMENT+"RDFS");
        boolean rdfs = ent != null && !ent.equals(ENTAILMENT + "RDF");
        boolean rdf = ent != null && ent.equals(ENTAILMENT + "RDF");
        int entail = QueryProcess.STD_ENTAILMENT;

        if (fresult != null) {
            Node nr = map.getNode("?r");
            isBlankResult = nr.isBlank();
        }

        if (fquery == null) {
            fquery = getValue(map, "?a");
        }

        // here
        // if (! fquery.contains("exists03") ) return true;
        if (trace && fquery != null) {
            System.out.println(pp(fquery));
        }

        if (fresult != null) {
            fresult = clean(fresult); // remove file://
        }
        fquery = clean(fquery);

        String query = "";
        Graph qg = null;

        if (isRDF) {
            // action is a RDF graph
            qg = Graph.create(true);
            qg.set(Entailment.DATATYPE_INFERENCE, true);
            Load ld = Load.create(qg);
            try {
                ld.parse(fquery);
            } catch (LoadException ex) {
                LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                earl.define(test, false);
                return false;
            }

        } else {
            QueryLoad ql = QueryLoad.create();
            try {
                query = ql.readWE(fquery);
            } catch (LoadException ex) {
                LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
            }

            if (query == null || query == "") {
                System.out.println("** ERROR 1: " + fquery + " " + query);
                System.out.println(map);
                return false;
            }
        }

        Graph graph = Graph.create();

        if (rdf || rdfs) {

            graph.setEntailment();
            if (rdf) {
                graph.set(RDF.RDF, true);
            } else {
                graph.set(RDFS.RDFS, true);
            }
        }

        Load load = Load.create(graph);
        // graph.setOptimize(true);
        load.reset();
        QueryProcess.setPlanDefault(Query.QP_HEURISTICS_BASED);
        QueryProcess exec = QueryProcess.create(graph, true);
        // exec.setSPARQLCompliant(true);
        // for update:
        exec.setLoader(load);

        /**
         * *********************************************************
         *
         * Load Result
         *
         **********************************************************
         */
        Mappings w3XMLResult = null;
        Mappings w3RDFResult = null;
        Graph gres = null;
        int nbres = -1;

        // Load the result
        if (isRDF) {
            gres = Graph.create();
            if (fresult.equals("true") || fresult.equals("false")) {
            } else {
                Load ld = Load.create(gres);
                try {
                    ld.parse(fresult);
                } catch (LoadException ex) {
                    LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                }
            }
        } else if (fresult == null && output.getURIs().isEmpty()) { // frnamed.size()==0){
            if (isEmpty) {
                gres = Graph.create();
            }
        } else if (fresult != null && fresult.endsWith(".srx")) {
            // XML Result
            try {
                Producer p = ProducerImpl.create(Graph.create());
                InputStream stream = getStream(fresult);
                if (stream != null) {
                    w3XMLResult = fr.inria.corese.compiler.result.XMLResult.create(p).parse(stream);
                } else {
                    System.out.println("** Stream Error: " + fresult);
                    w3XMLResult = new Mappings();
                }
                nbres = w3XMLResult.size();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (fresult != null && fresult.endsWith(".srj")) {
            isJSON = true;
        } else if (fresult != null && fresult.endsWith(".csv")) {
            isCSV = true;
        } else if (fresult != null && fresult.endsWith(".tsv")) {
            isTSV = true;
        } else if (output.getURIs().size() > 0
                || (fresult != null && (fresult.endsWith(".ttl") || fresult.endsWith(".rdf")))) {

            if (sparql1 || path.contains("construct")) {
                // Result Dataset
                gres = Graph.create();
                Load rl = Load.create(gres);
                rl.reset();

                if (fresult != null && !isBlankResult) {
                    try {
                        rl.parse(ttl2rdf(fresult));
                    } catch (LoadException ex) {
                        LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                    }
                }

                int i = 0;

                for (String g : output.getURIs()) {
                    rl.reset();
                    try {
                        rl.parse(g, output.getNameOrURI(i++));
                    } catch (LoadException ex) {
                        LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                    }
                }
                gres.prepare();
            } else {
                // SPARQL 1.0 RDF Result Format
                w3RDFResult = parseRDFResult(ttl2rdf(fresult));
                nbres = w3RDFResult.size();
            }
        }

        /**
         * *********************************************************
         *
         * Check Positive Negative Syntax
         *
         **********************************************************
         */
        // default base to interpret from <data.ttl>
        Query qq = null;
        if (!isRDF) {
            exec.setDefaultBase(defbase);
            try {
                qq = exec.compile(query);

                if (type == null) {
                } else if (type.contains(POSITIVE)) {

                    // positive syntax test
                    if (!qq.isCorrect()) {
                        System.out.println("** Should be positive: " + fquery);
                        names.add(fquery);
                        errors.add(query);
                    }

                    earl.define(test, qq.isCorrect());
                    return qq.isCorrect();
                }
                // NEGATIVE is tested below, at runtime
            } catch (SafetyException e1) {
            } catch (UndefinedExpressionException e1) {
                if (type.contains(POSITIVE)) {

                    // // positive syntax test
                    // if (!qq.isCorrect()) {
                    // System.out.println("** Should be positive: " + fquery);
                    // names.add(fquery);
                    // errors.add(query);
                    // }

                    earl.define(test, true);
                    return true;
                }
            } catch (EngineException e1) {
                if (type != null && type.contains(NEGATIVE)) {
                    earl.define(test, true);
                    return true;
                }

                names.add(fquery);
                errors.add(query);
                earl.define(test, false);
                return false;
            }
        }

        if (ep.length > 0) {
            Provider p = endpoint(ep, ed);
            exec.set(p);
        } else if (fq != null) {
            Provider p = ProviderImpl.create();
            exec.set(p);
        }

        /**
         * *********************************************************
         *
         * Load RDF Dataset Consider from [named] to build Dataset
         *
         **********************************************************
         */
        fr.inria.corese.sparql.triple.parser.Dataset ds = fr.inria.corese.sparql.triple.parser.Dataset.create();
        ds.setUpdate(isUpdate);

        if (!isRDF) {

            // Dataset may be specified by query from/named:
            if (fdefault.isEmpty()) {
                // get query from
                for (Node node : qq.getFrom()) {
                    String name = node.getLabel();
                    fdefault.add(name);
                }
            }
            if (input.getURIs().isEmpty()) {
                // get query from named
                for (Node node : qq.getNamed()) {
                    String name = node.getLabel();
                    input.addURI(name);
                }
            }

            if (fdefault.size() > 0) {
                // Load RDF files for default graph
                ds.defFrom();
                for (String file : fdefault) {
                    ds.addFrom(file);
                    try {
                        load.parse(file, file);
                    } catch (LoadException ex) {
                        LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                    }
                }
            }

            if (input.getURIs().size() > 0) {
                // Load RDF files for named graphs
                ds.defNamed();
                int i = 0;
                for (String file : input.getURIs()) {
                    String name = input.getNameOrURI(i++);
                    try {
                        load.parse(file, name);
                    } catch (LoadException ex) {
                        LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
                    }
                    ds.addNamed(name);
                }
            }
        }

        if (rdfs || rdf) {
            // load RDF/S definitions & RDFS inference rules

            if (rdfs) {
                entail = QueryProcess.RDFS_ENTAILMENT;
                ds.addFrom(RDFS.RDFS);
                try {
                    load.parse(RDFS.RDFS);
                    load.parse(RULE);
                } catch (LoadException e) {
                    e.printStackTrace();
                }
            } else {
                entail = QueryProcess.RDF_ENTAILMENT;
                // exclude rdfs properties when load rdf
                load.exclude(RDFS.RDFS);
            }

            // exclude dublin core:
            load.exclude(DC);
            try {
                load.parse(RDF.RDF, RDF.RDF);
            } catch (LoadException ex) {
                LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
            }
            ds.addFrom(RDF.RDF);

            // re.setDebug(true);
            // graph.getWorkflow().setDebug(true);
            RuleEngine re = load.getRuleEngine();
            if (re != null) {
                // re.process();
                graph.addEngine(re);
            }
        }

        /**
         * ****************************************************
         *
         * Query Processing
         *
         ****************************************************
         */
        try {
            // may be used to test SPIN, etc.
            // query = process(query, defbase);
            Mappings res;

            if (isRDF) {
                // project the entailed graph on the action graph
                exec = QueryProcess.create(qg);
                res = exec.query(gres);
            } else {
                // System.out.println(query);
                // exec.setVisitorable(true);
                // exec.setDetail(true);
                res = exec.sparql(query, ds, entail);
            }
            // System.out.println("Test: " + res);

            // Mappings res = exec.sparql(query, ds, entail);
            // System.out.println(res.getQuery().getAST());
            // System.out.println(res);
            // CHECK RESULT
            boolean result = true, b = true;

            if (isRDF) {
                if (gres.size() > 0) {
                    // compare action and result graph
                    result = res.size() > 0;
                    // boolean tc = qg.typeCheck();
                    // System.out.println("type check (opt): " + tc);
                } else if (fresult.equals("false")) {
                    // check action graph is flawed
                    boolean tc = qg.typeCheck();
                    System.out.println("type check: " + tc);
                    result = !tc;
                }

                if (!rdt.isEmpty() || !udt.isEmpty()) {
                    b = check(qg, rdt, udt);
                }

                if (type.contains(POSITIVE)) {
                    result = result && b;
                } else {
                    result = !(result && b);
                }

                if (!result) {
                    System.out.println("error: " + test);
                }

            } else if (type != null && type.contains(NEGATIVE)) {
                // KGRAM should detect an error here
                if (res.getQuery().isCorrect()) {
                    System.out.println("** Should be false: " + res.getQuery().isCorrect());
                    result = false;
                }
            } else if (!res.getQuery().isCorrect()) {
                System.out.println("** Should be true: " + res.getQuery().isCorrect());
                result = false;
            } else if (isJSON) {
                // checked by hand
                JSONFormat json = JSONFormat.create(res);
                System.out.println(json);
            } else if (isCSV) {
                // checked by hand
                CSVFormat json = CSVFormat.create(res);
                System.out.println(json);
            } else if (isTSV) {
                // checked by hand
                TSVFormat json = TSVFormat.create(res);
                System.out.println(json);
            } else if (gres != null) {
                // construct where
                Graph kg = exec.getGraph(res);
                if (kg == null) {
                    kg = graph;
                }
                gres.setDebug(true);
                // compare SPARQL result kg and Test case result gres
                QueryProcess verif = QueryProcess.create(kg);
                Mappings mm = verif.queryTurtle(gres);
                if (mm.size() == 0) { // (kg != null && !gres.compare(kg)) {
                    System.out.println("kgram:");
                    System.out.println(kg.display());
                    System.out.println("w3c");
                    System.out.println(gres.display());
                    if (!sparql1 && path.contains("construct")) {
                        // ok verified by hand 2011-03-15 because of blanks in graph
                        System.out.println("*** SKIP");
                    } else {
                        result = false;

                        // System.out.println("w3c: " + gres.size());
                        // System.out.println( TripleFormat.create(gres, true));
                        // System.out.println("kgram: " + kg.size());
                        // TripleFormat tf = TripleFormat.create(kg, true);
                        // System.out.println(tf);
                    }
                }
            } else if (nbres != res.size()) {
                if (verbose) {
                    System.out.println("** Failure");
                    if (fdefault.size() > 0) {
                        System.out.println(pp(fdefault.get(0)) + " ");
                    }
                    if (fquery != null) {
                        System.out.println(pp(fquery) + " ");
                    }
                    if (fresult != null) {
                        System.out.println(pp(fresult));
                    }
                    System.out.println("kgram result: ");
                    System.out.println(res);
                    System.out.println("w3c result:");
                    System.out.println(w3XMLResult);
                    System.out.println("w3c: " + nbres + "; kgram: " + res.size());
                }
                System.out.println(query);
                result = false;
            } else if (w3RDFResult != null) {
                // old rdf result format
                result = validate(res, w3RDFResult);
            } else {
                // XML Result Format
                // System.out.println("** kgram: \n" + res);
                // System.out.println("** w3c: \n" + w3XMLResult);
                result = validate(res, w3XMLResult);
            }

            if (result == false) {
                names.add(fquery);
                errors.add(query);
            }

            earl.define(test, result);
            return result;

        } catch (EngineException e) {
            if (type != null && type.contains(NEGATIVE)) {
                earl.define(test, true);
                return true;
            } else {
                System.out.println(e);
            }

            System.out.println("** ERROR 2: " + e.getMessage() + " " + nbres + " " + 0);
            if (fdefault.size() > 0) {
                System.out.print(pp(fdefault.get(0)) + " ");
            }
            if (fquery != null) {
                System.out.print(pp(fquery) + " ");
            }
            if (fresult != null) {
                System.out.println(pp(fresult));
            }
            System.out.println(query);
            errors.add(query);
            names.add(fquery);
            earl.define(test, false);
            return false;
        }
    }

    InputStream getStream(String path) {
        try {
            URL uri = new URL(path);
            return uri.openStream();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }

        FileInputStream stream;
        try {
            stream = new FileInputStream(path);
            return stream;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    Provider endpoint(String[] ep, String[] ed) {
        ProviderImpl p = ProviderImpl.create();
        int j = 0;
        for (String nep : ep) {
            String name = ed[j++];

            // rdf version
            // String ff = ttl2rdf(name);
            Graph g = Graph.create();
            Load load = Load.create(g);
            try {
                load.parse(name);
            } catch (LoadException ex) {
                LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
            }
            p.add(nep, g);
        }

        return p;
    }

    /**
     * Blanks may have different ID in test case and in kgram but same ID should
     * remain the same Hence store ID in hashtable to compare
     *
     */
    class TBN extends Hashtable<IDatatype, IDatatype> {

        boolean same(IDatatype dt1, IDatatype dt2) {
            if (containsKey(dt1)) {
                return get(dt1).sameTerm(dt2);
            } else {
                put(dt1, dt2);
                return true;
            }
        }
    }

    // target value of a Node
    IDatatype datatype(Node n) {
        if (n == null) {
            return null;
        }
        return (IDatatype) n.getValue();
    }

    /**
     * KGRAM vs W3C result
     */
    boolean validate(Mappings kgram, Mappings w3c) {
        if (kgram.size() != w3c.size()) {
            return false;
        }
        return check(kgram, w3c); // && check(w3c, kgram);
    }

    boolean validate2(Mappings kgram, Mappings w3c) {
        return check(kgram, w3c);
    }

    boolean check(Mappings kgram, Mappings w3c) {
        boolean result = true, printed = false;
        Hashtable<Mapping, Mapping> table = new Hashtable<Mapping, Mapping>();

        for (Mapping w3cres : w3c) {
            // for each w3c result
            boolean ok = false;

            for (Mapping kres : kgram) {
                // find a new kgram result that is equal to w3c
                if (table.contains(kres)) {
                    continue;
                }

                ok = compare(kgram, w3c, kres, w3cres);

                if (ok) {
                    // if (kgram.getSelect().size() != w3cres.size()) ok = false;

                    // for (Node qNode : kgram.getSelect()) {
                    // // check that kgram has no additional binding
                    // if (kres.getNode(qNode) != null) {
                    // if (w3cres.getNode(qNode) == null) {
                    // ok = false;
                    // }
                    // }
                    // }
                }

                if (ok) {
                    table.put(kres, w3cres);
                    break;
                }
            }

            if (!ok) {
                result = false;

                System.out.println("** Failure");
                if (printed == false) {
                    System.out.println(kgram);
                    printed = true;
                }
                for (Node var : w3cres.getQueryNodes()) {
                    // for each w3c variable/value
                    Node val = w3cres.getNode(var);
                    System.out.println(var + " [" + val + "]");
                }
                System.out.println("--");
            }

        }
        return result;
    }

    // compare two results
    boolean compare(Mappings kgmap, Mappings w3map, Mapping kgres, Mapping w3res) {
        TBN tbn = new TBN();
        boolean ok = true;

        for (Node var : w3res.getQueryNodes()) { // map.getSelect()){ //w3cres.getQueryNodes()) {
            if (!ok) {
                break;
            }

            // for each w3c variable/value
            IDatatype w3val = datatype(w3res.getNode(var));
            // find same value in kgram
            if (w3val != null) {
                String cvar = var.getLabel();
                Node kNode = kgres.getNode(var);
                if (kNode == null) {
                    ok = false;
                } else {
                    IDatatype kdt = datatype(kNode);
                    IDatatype wdt = w3val;
                    ok = compare(kdt, wdt, tbn);
                }
            }
        }

        if (ok && kgmap.getSelect() != null) {
            // kgram result has additional data
            for (Node node : kgmap.getSelect()) {
                if (kgres.getNodeValue(node) != null && w3res.getNode(node) == null) {
                    ok = false;
                    // if (w3res.getQueryNodes().length > 0) {
                    // System.out.println("kg: "+ node + " = " + kgres.getNodeValue(node));
                    // System.out.println();
                    // System.out.println("w3c: " + w3res);
                    // System.out.println("kgr: " + kgres);
                    // }
                    break;
                }
            }
        }

        return ok;
    }

    // compare kgram vs w3c values
    boolean compare(IDatatype kdt, IDatatype wdt, TBN tbn) {
        boolean ok = true;
        if (kdt.isBlank()) {
            if (wdt.isBlank()) {
                // blanks may not have same ID but
                // if repeated they should both be the same
                ok = tbn.same(kdt, wdt);
            } else {
                ok = false;
            }
        } else if (wdt.isBlank()) {
            ok = false;
        } else if (kdt.isNumber() && wdt.isNumber()) {
            ok = kdt.sameTerm(wdt);

            if (DatatypeMap.isLong(kdt) && DatatypeMap.isLong(wdt)) {
                // ok
            } else {
                if (!ok) {
                    // compare them at 10^-10
                    ok = Math.abs((kdt.doubleValue() - wdt.doubleValue())) < 10e-10;
                    if (ok) {
                        // System.out.println("** Consider as equal: " + kdt.toSparql() + " = " +
                        // wdt.toSparql());
                    }
                }
            }

        } else {
            ok = kdt.sameTerm(wdt);
            if (!ok) {
                if (matchDatatype(kdt, wdt)) {
                    ok = kdt.equals(wdt);
                }
            }
        }

        if (ok && strict && wdt.isLiteral()) {
            // check same datatypes
            if (kdt.getDatatype() != null && wdt.getDatatype() != null) {
                ok = kdt.getDatatype().sameTerm(wdt.getDatatype());
            } else if (kdt.getDatatype() != wdt.getDatatype()) {
                ok = false;
            }
            if (!ok) {
            }
        }

        return ok;

    }

    boolean matchDatatype(IDatatype dt1, IDatatype dt2) {
        return (dt1.getCode() == IDatatype.LITERAL) && (dt2.getCode() == IDatatype.STRING)
                || (dt1.getCode() == IDatatype.STRING) && (dt2.getCode() == IDatatype.LITERAL);
    }

    boolean compare(List<Node> lVar, List<Node> lVal, Mapping kres) {
        boolean ok = true;
        TBN tbn = new TBN();
        int i = 0;
        for (Node var : lVar) {
            if (!ok) {
                break;
            }

            // for each w3c variable/value
            // find same value in kgram
            Node w3cval = lVal.get(i++);

            if (w3cval != null) {
                String cvar = "?" + var.getLabel();
                Node kNode = kres.getNode(cvar);
                if (kNode == null) {
                    ok = false;
                } else {
                    IDatatype kdt = datatype(kNode);
                    IDatatype wdt = datatype(w3cval);
                    ok = compare(kdt, wdt, tbn);
                }
            }
        }

        return ok;
    }

    private boolean check(Graph g, List<Node> rdt, List<Node> udt) {
        boolean b1 = rdt(g, rdt, true);
        boolean b2 = rdt(g, udt, false);
        return b1 && b2;
    }

    // (un)recognized datatype
    boolean rdt(Graph g, List<Node> list, boolean res) {
        if (list.isEmpty()) {
            return true;
        }
        for (Node n : list) {
            IDatatype dd = (IDatatype) n.getValue();

            for (Edge ent : g.getEdges()) {
                IDatatype dt = (IDatatype) ent.getNode(1).getValue();

                if (dt.getDatatypeURI() != null) {
                    if (dt.getDatatypeURI().equals(dd.getLabel())) {
                        return res;
                    }
                } else if (dt.getLabel().equals(dd.getLabel())) {
                    // rdfs:range xsd:integer
                    return res;
                }
            }
        }
        return !res;
    }

    String pp(String name) {
        String pat = "sparql11/";
        int index = name.indexOf(pat);
        if (index == -1) {
            return name;
        }
        return name.substring(index + pat.length());
    }

    String name(String path) {
        int index = path.lastIndexOf(File.separator);
        return path.substring(index + 1);
    }

    String uri(String file) {
        try {
            URL url = new URL(file);
            return file;
        } catch (MalformedURLException e) {
        }
        return "file://" + file;
    }

    String ttl2rdf(String name) {
        // if (name.endsWith(".ttl")){
        // name = name.substring(0, name.length()-4);
        // name = name + ".rdf";
        // }
        return clean(name);
    }

    String clean(String name) {
        String HEAD = "file://";
        if (name.startsWith(HEAD)) {
            name = name.substring(HEAD.length());
        }
        return name;
    }

    // read and return query
    public String read(String name) {
        // name = clean(name);
        String query = "", str = "";
        try {
            BufferedReader fq = new BufferedReader(new FileReader(name));
            while (true) {
                str = fq.readLine();
                if (str == null) {
                    fq.close();
                    break;
                }
                query += str + "\n";
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return query;
    }

    Mappings parseRDFResult(String fresult) {
        String query = "prefix rs: <http://www.w3.org/2001/sw/DataAccess/tests/result-set#>"
                + "select ?var ?val where { "
                + "{ ?r rs:solution ?s "
                + "optional {?s rs:index ?i }"
                + "optional { "
                + "?s rs:binding [  rs:variable ?var ; rs:value ?val ] } "
                + "} "
                + "union {?r rs:boolean 'true'^^xsd:boolean}"
                + "}"
                + "order by ?i  "
                + "group by ?s  ";

        Graph g = Graph.create();
        Load load = Load.create(g);
        try {
            load.parse(fresult);
        } catch (LoadException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }

        QueryProcess exec = QueryProcess.create(g);
        exec.setListGroup(true);
        Mappings map = null;
        try {
            map = exec.query(query);
            // System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mappings res = translate(map);
        return res;

    }

    /**
     * W3C map to KGRAM map
     */
    Mappings translate(Mappings ms) {
        Mappings res = new Mappings();
        for (Mapping map : ms) {
            Mapping m = translate(map);
            res.add(m);
        }
        return res;
    }

    Mapping translate(Mapping m) {
        if (m.getMappings() == null) {
            return m;
        }

        Mapping res = Mapping.create();

        for (Mapping map : m.getMappings()) {
            Node var = map.getNode("?var");
            Node val = map.getNode("?val");
            if (var != null && val != null) {
                NodeImpl q = NodeImpl.createVariable("?" + var.getLabel());
                res.addNode(q, val);
            }
        }

        return res;
    }

    /**
     * Generate SPIN query Pretty Print SPIN quert as SPARQL Parse SPARQL query
     */
    void spin(Query qq) {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);

        ASTQuery ast = exec.getAST(qq);
        // System.out.println(ast);

        SPIN sp = SPIN.create();
        sp.visit(ast);

        // System.out.println("spin:\n" + sp);
        String str = sp.toString();
        try {
            ld.load(new ByteArrayInputStream(str.getBytes("UTF-8")), "spin.ttl");
        } catch (LoadException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "query: [" + str + "]");

        } catch (UnsupportedEncodingException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }

        TemplateFormat tf = TemplateFormat.create(g, "/home/corby/AData/spin/template");
        tf.setNSM(ast.getNSM());
        // System.out.println(tf);

        String res = tf.toString();
        try {
            Query q = exec.compile(res);

            // if (res.length() == 0 ){
            // System.out.println(ast);
            // System.out.println("PP:");
            // System.out.println(res);
            // System.out.println("SPIN:");
            // System.out.println(str);
            // }
        } catch (EngineException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "PP:\n" + res);
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "AST:\n" + ast.toString());
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "SPIN:\n" + str);

        }
    }

    /**
     * **************************************************
     */
    private String process(String query, String defbase) throws EngineException {
        // spin(query);
        // System.out.println(query);
        return toSPIN(query, defbase);
    }

    private void spin(String query) {
        SPINProcess sp = SPINProcess.create();
        try {
            String spin = sp.toSpin(query);
            QueryLoad ql = QueryLoad.create();
            String name = TTL + "f" + cc++ + ".ttl";
            ql.write(name, spin);
        } catch (EngineException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }

    }

    private String toSPIN(String query, String defbase) throws EngineException {
        System.out.println("query: \n" + query);
        SPINProcess sp = SPINProcess.create();
        sp.setSPARQLCompliant(true);
        sp.setDefaultBase(defbase);
        String str = sp.toSpinSparql(query);
        System.out.println("result: " + str);
        return str;
    }

    // add SPIN query into a global graph
    void store(String query) {
        try {
            SPINProcess spin = SPINProcess.create();
            String sp = spin.toSpin(query);
            spin.toGraph(sp, spinGraph);
        } catch (EngineException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }

    }

    // SPIN Graph
    void process(Graph g) {
        // query(g);
    }

    void pprint(Graph g) {
        Transformer pp = Transformer.create(g, Transformer.SPIN);
        NSManager nsm = NSManager.create();
        nsm.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
        nsm.definePrefix("ex", "http://www.example.org/");

        pp.setNSM(nsm);
        System.out.println(pp);
    }

    void query(Graph g) {
        String q = "prefix sp: <http://spinrdf.org/sp#>"
                + "select distinct ?c (count(?x) as ?cc) where {"
                // + "{?x a ?c} union "
                + "{?x ?c ?v filter(strstarts(?c, sp:))}"
                + "} "
                + "group by ?c";

        String q1 = "prefix sp: <http://spinrdf.org/sp#>"
                + "select  (count(*) as ?cc) where {"
                // + "{?x a ?c} union "
                + "{?x sp:subject ?s ; "
                + "sp:object ?o"
                + "{?x sp:predicate ?p } union { ?x sp:path ?p } "
                + "}"
                + "} "
                + "";

        QueryProcess exec = QueryProcess.create(g);
        try {
            Mappings map = exec.query(q);
            System.out.println(map);
        } catch (EngineException ex) {
            LogManager.getLogger(TestW3C11KGraphNew.class.getName()).log(Level.ERROR, "", ex);
        }
    }
}
