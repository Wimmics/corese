package test.w3c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
 
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;

import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

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

public class TestW3C11KGraph {
    // root of test case RDF data

    static final String data0 = "/home/corby/workspace/coreseV2/src/test/resources/data/";
    
//    static final String data = "/home/corby/NetBeansProjects/kgram/trunk/kgtool/src/test/resources/data/";
    static final String data = TestW3C11KGraph.class.getClassLoader().getResource("data").getPath()+"/";

    // old local copy:
    //static final String froot = data + "w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/";
    // new local copy:
    static final String froot = data + "w3c-sparql11/sparql11-test-suite/";
    // W3C test case:
    static final String wroot = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/";
  //static final String RULE  = "/net/servers/ftp-sop/wimmics/soft/rule/rdfs.rul";
    static final String RULE  = data + "w3c-sparql11/data/rdfs.rul";
    static final String root0 = data0 + "test-suite-archive/data-r2/";
    static final String DC = "http://purl.org/dc/elements/1.1/";
    /**
     * **********
     * TO SET: 
	 ***********
     */
    // ** directory where data are:
    static final String root = froot;
    // ** directory where to save earl report:
    static final String more = data + "w3c-sparql11/data/";
    // query
    static final String man =
            "prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> "
            + "prefix qt:  <http://www.w3.org/2001/sw/DataAccess/tests/test-query#> "
            + "prefix sd:  <http://www.w3.org/ns/sparql-service-description#> "
            + "prefix ent: <http://www.w3.org/ns/entailment/> "
            + "prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> "
            + "select  * where {"
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
            + ""
            + "optional { ?a qt:serviceData [qt:endpoint ?ep ; qt:data ?ed] }"
            + "{?man rdf:first ?x} "
            + "} "
            + "group by ?x order by ?q ";
    // update
    static final String man2 =
            "prefix mf:  <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#> "
            + "prefix ut:     <http://www.w3.org/2009/sparql/tests/test-update#> "
            + "prefix sd:  <http://www.w3.org/ns/sparql-service-description#> "
            + "prefix ent: <http://www.w3.org/ns/entailment/> "
            + "prefix dawgt: <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#> "
            + "select  * where {"
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
//	Earl earl;

    class Testing extends Hashtable<String, Integer> {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
    }

    @After
    public void tearDown() {
    }

    public TestW3C11KGraph() {
        DatatypeMap.setSPARQLCompliant(true);
        tko = new Testing();
        tok = new Testing();
//		earl = new Earl();
    }

    public static void main(String[] args) {
        new TestW3C11KGraph().process();
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

    /**
     * SPARQL 1.1
     */
    void test1() {
        sparql1 = true;

        test(root + "service");

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

        //test(root + "entailment");

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
        //test(root + "functions", false);
        test(root + "functions");		 
        

    }

    void testelem() {
        sparql1 = false;
        test(root0 + "dataset");
    }

    @Test
    public void process() {
        gok = 0;
        gko = 0;

        // 28 errors  416 success
        // 29 errors 04/05/12
        // 31 errors 11/05/12 because of optional DOT 
        

        if (true) {
            //Graph.setValueTable(true);
            test1();
            testUpdate();
            //test0(); //29 errors
        } else {
            test();
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
        //println("<p><a href='http://www.w3.org/2001/sw/DataAccess/tests/r2'>SPARQL test cases</a></p>");
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

        //earl.toFile(more + "earl.ttl");

        // There are 5 known erros
        assertEquals("Results", 5, errors.size());


        //Processor.finish();

    }
    
    void println(String str){
        System.out.println(str);
    }
    
     void print(String str){
        System.out.print(str);
    }

    /**
     * Test one manifest
     */
    void skip(String path) {
        test(path, false, false);
    }

    void test(String path) {
        test(path, false, true);
    }

    void test(String path, boolean update) {
        test(path, update, true);
    }

    /**
     * Process one manifest Load manifest.ttl in a Graph SPARQL Query the graph
     * to get the query list, input and output list.
     */
    void test(String path, boolean update, boolean process) {
        String manifest = path + "/manifest.ttl";

        try {
            System.out.println("** Load: " + pp(manifest));

            Graph g = Graph.create();
            Load load = Load.create(g);
            try {
                load.loadWE(manifest);
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
            //System.out.println(res2);
            nbtest += res.size();

            int ok = 0, ko = 0;

            for (Mapping map : res) {
                // each map is a test case

                if (!process) {
                    String test = getValue(map, "?x");
//					earl.skip(test);
                } else if (query(path, map, update)) {
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
     * @param fnamed RDF files for named graphs
     * @param ent entailment
     *
     * @return
     */
    boolean query(String path, Mapping map, boolean isUpdate) {
        //System.out.println(map);

        String defbase = uri(path + File.separator);

        Dataset input = new Dataset().init(map.getMappings(), "?name", "?g");
        Dataset output = new Dataset().init(map.getMappings(), "?nres", "?gr");

        List<String> fdefault = getValueList(map, "?d");

        String test = getValue(map, "?x");
        String fquery = getValue(map, "?q");
        String fresult = getValue(map, "?r");
        String ent = getValue(map, "?ent");
        String type = getValue(map, "?t");
        String fq = getValue(map, "?fq");


        String[] ep = getValues(map, "?ep");
        String[] ed = getValues(map, "?ed");


        boolean isEmpty = getValue(map, "?ee") != null;
        boolean isBlankResult = false;
        boolean isJSON = false, isCSV = false, isTSV = false;
        //boolean rdfs = ent != null &&  ent.equals(ENTAILMENT+"RDFS");
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

       //if (! fquery.contains("uuid") ) return true;

        if (trace) {
            System.out.println(pp(fquery));
        }

        if (fresult != null) {
            fresult = clean(fresult); // remove file://
        }
        fquery = clean(fquery);
        QueryLoad ql = QueryLoad.create();
        String query = ql.read(fquery);

        if (query == null || query == "") {
            System.out.println("** ERROR 1: " + fquery + " " + query);
            System.out.println(map);
           return false;
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
        graph.setOptimize(true);
        load.reset();
        QueryProcess exec = QueryProcess.create(graph, true);
        //exec.setSPARQLCompliant(true);
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
        if (fresult == null && output.getURIs().size() == 0) { //frnamed.size()==0){
            if (isEmpty) {
                gres = Graph.create();
            }
        } else if (fresult != null && fresult.endsWith(".srx")) {
            // XML Result
            try {
                Producer p = ProducerImpl.create(Graph.create());
                InputStream stream = getStream(fresult);
                if (stream != null) {
                    w3XMLResult = fr.inria.edelweiss.kgenv.result.XMLResult.create(p).parse(stream);
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
                // Graph Result 
                gres = Graph.create();
                Load rl = Load.create(gres);
                rl.reset();

                if (fresult != null && !isBlankResult) {
                    rl.load(ttl2rdf(fresult));
                }

                int i = 0;
//				for (String g : frnamed){
//					rl.reset();
//					rl.load(ttl2rdf(g), frnames.get(i++));
//				}
                for (String g : output.getURIs()) {
                    rl.reset();
                    rl.load(g, output.getNameOrURI(i++));
                }
                gres.index();
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
        exec.setDefaultBase(defbase);
        Query qq = null;
        try {
            qq = exec.compile(query);

            if (type == null) {
            } else if (type.contains(POSITIVE)) {

//				if (! query.contains("LOAD")){
//					// Extra test: exec the query
//					exec.query(query);
//				}

                // positive syntax test
                if (!qq.isCorrect()) {
                    System.out.println("** Should be positive: " + fquery);
                    names.add(fquery);
                    errors.add(query);
                }

//				earl.define(test, qq.isCorrect());
                return qq.isCorrect();
            }
            // NEGATIVE is tested below, at runtime
        } catch (EngineException e1) {
            if (type != null && type.contains(NEGATIVE)) {
//				earl.define(test, true);
                return true;
            }
            System.out.println("** Parser Error: " + e1.getMessage());
            names.add(fquery);
            errors.add(query);
//			earl.define(test, false);
            return false;
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
        fr.inria.acacia.corese.triple.parser.Dataset ds = fr.inria.acacia.corese.triple.parser.Dataset.create();
        ds.setUpdate(isUpdate);

        // default graph
        if (fdefault.size() == 0) {
            for (Node node : qq.getFrom()) {
                String name = node.getLabel();
                fdefault.add(name);
            }
        }

        // named graphs
        if (input.getURIs().size() == 0) {
            for (Node node : qq.getNamed()) {
                String name = node.getLabel();
                //fnamed.add(name);
                input.addURI(name);
            }
        }


        // default graph
        if (fdefault.size() > 0) {
            // Load RDF files for default graph
            ds.defFrom();
            for (String file : fdefault) {
                ds.addFrom(file);
                load.load(file, file);
            }
        }


        // named graphs
        if (input.getURIs().size() > 0) {
            // Load RDF files for named graphs
            //namedGraph = new ArrayList<String>();
            ds.defNamed();
            int i = 0;
            for (String file : input.getURIs()) {
                String name = input.getNameOrURI(i++);
                load.load(file, name);
                ds.addNamed(name);
            }
        }



        if (rdfs || rdf) {
            // load RDF/S definitions & RDFS inference rules

            if (rdfs) {
                entail = QueryProcess.RDFS_ENTAILMENT;
                ds.addFrom(RDFS.RDFS);
                load.load(RDFS.RDFS);
                try {
                    load.loadWE(RULE);
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
            load.load(RDF.RDF, RDF.RDF);
            ds.addFrom(RDF.RDF);

            //re.setDebug(true);
            //graph.getWorkflow().setDebug(true);
            RuleEngine re = load.getRuleEngine();
            if (re != null) {
                //re.process();
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

            //exec.setOptimize(true);
            //exec.setDebug(true);
            Mappings res = exec.sparql(query, ds, entail);


            String q =
                    "prefix ex: <http://example.org/ns#> "
                    + "select * where {"
                    + "?x ?p ?y"
                    + "}";


//			Mappings mm = exec.query(q);
//			System.out.println(ResultFormat.create(mm));
//			System.out.println(mm.size());
//			
//			
//			System.out.println(String.format("%.5g", new Double(12345678.9876543).doubleValue()));
//			System.out.println(String.format("%.5f", new Double(12345678.9876543).doubleValue()));
//			System.out.println(String.format("%.5e", new Double(12345678.9876543).doubleValue()));
//			System.out.println(String.format("%g", new Double(1.75).doubleValue()));
//			System.out.println(new Double(1.75).doubleValue());
            //1 .0e6
            // CHECK RESULT
            boolean result = true;


            if (type != null && type.contains(NEGATIVE)) {
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
                gres.setDebug(true);
                if (kg != null && !gres.compare(kg)) {
                    System.out.println("kgram:");
                    System.out.println(kg.display());
                    if (!sparql1 && path.contains("construct")) {
                        // ok verified by hand 2011-03-15 because of blanks in graph
                    } else {
                        result = false;

//						System.out.println("w3c: " + gres.size());
//						System.out.println( TripleFormat.create(gres, true));
//						System.out.println("kgram: " + kg.size());
//						TripleFormat tf = TripleFormat.create(kg, true);
//						System.out.println(tf);
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
                    System.out.println("w3c: " + nbres + "; kgram: " + res.size());
                }
                System.out.println(query);
                result = false;
            } else if (w3RDFResult != null) {
                // old rdf result format
                result = validate(res, w3RDFResult);

            } else {
                // XML Result Format
//				System.out.println("** kgram: \n" + res);
//				System.out.println("** w3c: \n" + w3XMLResult);
                result = validate(res, w3XMLResult);
            }

            if (result == false) {
                names.add(fquery);
                errors.add(query);
            }

//			earl.define(test, result);
            return result;

        } catch (EngineException e) {
            if (type != null && type.contains(NEGATIVE)) {
//				earl.define(test, true);
                return true;
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
//			earl.define(test, false);
            return false;
        }
    }

    InputStream getStream(String path) {
        try {
            URL uri = new URL(path);
            return uri.openStream();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
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
            //String ff = ttl2rdf(name);
            Graph g = Graph.create();
            Load load = Load.create(g);
            load.load(name);
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
        return (IDatatype) n.getValue();
    }

    /**
     * KGRAM vs W3C result
     */
    boolean validate(Mappings kgram, Mappings w3c) {
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

                ok = compare(kres, w3cres);

                if (ok) {
                    //if (kgram.getSelect().size() != w3cres.size()) ok = false;

                    for (Node qNode : kgram.getSelect()) {
                        // check that kgram has no additional binding 
                        if (kres.getNode(qNode) != null) {
                            if (w3cres.getNode(qNode) == null) {
                                ok = false;
                            }
                        }
                    }
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
    boolean compare(Mapping kres, Mapping w3cres) {
        TBN tbn = new TBN();
        boolean ok = true;

        for (Node var : w3cres.getQueryNodes()) {
            if (!ok) {
                break;
            }

            // for each w3c variable/value
            IDatatype w3cval = datatype(w3cres.getNode(var));
            // find same value in kgram
            if (w3cval != null) {
                String cvar = var.getLabel();
                Node kNode = kres.getNode(var);
                if (kNode == null) {
                    ok = false;
                } else {
                    IDatatype kdt = datatype(kNode);
                    IDatatype wdt = w3cval;
                    ok = compare(kdt, wdt, tbn);
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
                // if repeated they should  both be the same
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
                    ok =
                            Math.abs((kdt.doubleValue() - wdt.doubleValue())) < 10e-10;
                    if (ok) {
                        System.out.println("** Consider as equal: " + kdt.toSparql() + " = " + wdt.toSparql());
                    }
                }
            }

        } else {
            ok = kdt.sameTerm(wdt);
        }

        if (ok && strict && wdt.isLiteral()) {
            // check same datatypes
            if (kdt.getDatatype() != null && wdt.getDatatype() != null) {
                ok = kdt.getDatatype().sameTerm(wdt.getDatatype());
            } else {
                ok = kdt.getIDatatype().sameTerm(wdt.getIDatatype());
            }
            if (!ok) {
                System.out.println("** Datatype differ: " + kdt.toSparql() + " " + wdt.toSparql());
            }
        }

        return ok;

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
//		if (name.endsWith(".ttl")){
//			name = name.substring(0, name.length()-4);
//			name = name + ".rdf";
//		}
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
        //name = clean(name);
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
        String query =
                "prefix rs: <http://www.w3.org/2001/sw/DataAccess/tests/result-set#>"
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
        load.load(fresult);

        QueryProcess exec = QueryProcess.create(g);
        exec.setListGroup(true);
        Mappings map = null;
        try {
            map = exec.query(query);
            //System.out.println(map);
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
}
