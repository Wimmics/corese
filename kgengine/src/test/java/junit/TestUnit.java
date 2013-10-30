package junit;

import fr.inria.edelweiss.kgtool.util.QueryGraphVisitorImpl;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.cg.datatype.CoreseDate;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.javacc1.JavaccParseException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.ExpandPath;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.QueryGraphVisitor;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.ValueResolverImpl;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.query.QueryGraph;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.BuildImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.PPrinter;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import fr.inria.edelweiss.kgtool.print.TemplateFormat;
import fr.inria.edelweiss.kgtool.print.TemplatePrinter;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.GListener.Operation;
import static junit.TestQuery1.graph;
import static junit.TestQuery1.root;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import fr.inria.acacia.corese.triple.printer.SPIN;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgtool.util.QueryManager;
import fr.inria.edelweiss.kgenv.parser.ExpandList;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.RuleLoad;
import fr.inria.edelweiss.kgtool.util.GraphStoreInit;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import static junit.TestQuery1.init;
import static junit.TestQuery1.root;
import static junit.TestQuery1.text;
import static org.junit.Assert.assertEquals;

public class TestUnit {

    static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
    static String root = data;
    static String text = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/text/";
    static String ndata = "/home/corby/workspace/kgtool/src/test/resources/data/";
    static String cos2 = "/home/corby/workspace/coreseV2/src/test/resources/data/ign/";
    static String cos = "/home/corby/workspace/corese/data/";
    static Graph graph;
    private String SPIN_PREF = "prefix sp: <" + NSManager.SPIN + ">\n";
    private String FOAF_PREF = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n";
    private String SQL_PREF = "prefix sql: <http://ns.inria.fr/ast/sql#>\n";

    public IDatatype getSqrt(Object dist) {
        IDatatype dt = (IDatatype) dist;
        Double distSqrt = Math.sqrt(dt.doubleValue());
        return DatatypeMap.newInstance(distSqrt);
    }

    class MyBuild extends BuildImpl {

        MyBuild(Graph g) {
            super(g);
        }

        public String getID(String b) {
            return b;
        }
    }

//    @BeforeClass
    static public void init() {
        QueryProcess.definePrefix("Mappingsc", "http://www.inria.fr/acacia/comma#");
        QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        graph = Graph.create(true);
        graph.setOptimize(true);

        Load ld = Load.create(graph);
        init(graph, ld);
    }

    static void init(Graph g, Load ld) {
        ld.load(data + "comma/comma.rdfs");
        ld.load(data + "comma/model.rdf");
        ld.load(data + "comma/data");
        ld.load(data + "comma/data2");

    }

    public void translate() {
        TemplatePrinter p = TemplatePrinter.create(root + "pprint/asttemplate", root + "pprint/turtle.rul");
        try {
            p.process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoadException e) {
            e.printStackTrace();//    public static void main(String[] args){
//        new TestUnit().testSPIN3();
//    }
        }
    }

    //@Test
    public void testQVV() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        Load ld = Load.create(g);

        String q = ql.read(data + "work/listmatch.rq");

        System.out.println(q);

        SPINProcess sp = SPINProcess.create();

        String spin = sp.toSpin(q);

        System.out.println(spin);

        ql.write(data + "work/listmatch.ttl", spin);

        ld.loadWE(data + "work/listmatch.ttl");


        String qq = ql.read(data + "work/querylistmatch.rq");
        System.out.println(qq);

        exec.setVisitor(ExpandList.create());
        Mappings map = exec.query(qq);


        System.out.println(map);
        System.out.println(map.size());

        System.out.println(map.getQuery().getAST());

    }

    //@Test
    public void testQV() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person ;"
                + " foaf:knows ((<Jack> <John>) <Jim>)"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * (kg:pprint(?x) as ?res)  where {"
                + "?x foaf:knows @ (<Jim> (?x) ) "
                + "}"
                + "pragma {"
                //                + "kg:list kg:expand true "
                //                + "kg:path kg:expand true"
                + "}"
                + "";
        try {
            exec.query(init);
            //exec.setVisitor(ExpandList.create());
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());




        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testPPrintghjfgh() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        ld.load(root + "pprint/data/");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();
        System.out.println(str);

        assertEquals("Results", 3055, str.length());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

//        str = nsm.toString() + "\n" + str;
//
//        InputStream io = new ByteArrayInputStream(str.getBytes());
//        Graph gg = Graph.create();
//        Load ll = Load.create(gg);
//        try {
//            ll.load(io, "test.ttl");
//            System.out.println(g.size() + " " + (gg.size() + 1));
//
//            assertEquals("Results", g.size(), gg.size());
//        } catch (LoadException e) {
//            e.printStackTrace();
//        }

    }

    public void testbug() throws EngineException {
        QueryLoad ql = QueryLoad.create();

        String str = ql.read(data + "work/bug.rq");
        SPINProcess sp = SPINProcess.create();

        String spin = sp.toSpinSparql(str);

        System.out.println(spin);
    }

    public void testOumy() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.loadWE(data + "work/owlrl.rul");
        QueryLoad ql = QueryLoad.create();

        RuleEngine re = ld.getRuleEngine();

        int i = 0;

        for (Rule r : re.getRules()) {
            System.out.println(i++);
            ASTQuery ast = (ASTQuery) r.getQuery().getAST();
            
            SPINProcess sp = SPINProcess.create();
            String spin = sp.toSpin(ast);
            String sparql = sp.toSparql(spin);
//            System.out.println(ast);
//            System.out.println(spin);
//            System.out.println(sparql);
            exec.compile(sparql);

            //ql.write(data + "work/owlrl/f" + i + ".ttl", spin);
        }

    }

    
    
    
    @Test
    
     public void testLoc() throws EngineException, LoadException {
        
        String init = FOAF_PREF                
                + "insert data { "
                + "[ foaf:knows <Jim> ] . "
                + "<Jim> foaf:knows <James> "
                + "<Jim> rdfs:label 'Jim' "
                + " "
                + "}";
        
        
        GraphStore gs = GraphStore.create();
//        Graph gg = g.createNamedGraph(Graph.SYSTEM);
//        gg.addListener(new GraphListenerImpl(g));
        GraphStoreInit.create(gs).init();
        Graph gg = gs.getNamedGraph(Graph.SYSTEM);
        PPrinter pp =  PPrinter.create(gg, PPrinter.TURTLE);
        System.out.println(pp);
        Load ql = Load.create(gs);
        QueryProcess exec = QueryProcess.create(gs);
        
        String query = 
                "insert { graph kg:system { ?x ?p ?y } }"
                + " where {"
                + "?x ?p ?y "
                + "graph ?g {  }"
                + "}";
        
        String q = FOAF_PREF
                + "select debug *"
                + "where {"
                //+ "graph ?g { ?x ?p ?y  }"
                + "graph kg:system { "
                + "?a kg:version+ ?b "
                + "filter ("
                + "?a != ?a || "
                + "if (! (exists { ?a kg:date+ ?d } = false),  true, false)"
                + ")"
                + "filter not exists { ?x foaf:knows ?y }"                
                + "}"
                + "}";
        
        String qq = FOAF_PREF
                + "select * where {"
                + "?x foaf:knows+ :: $path ?y "
                + "graph $path { filter exists { ?a foaf:knows ?b }"
               // + "?a foaf:knows+ ?b "
                + "}"
                + "}";
        
//        Mappings map = exec.query(query);
//        System.out.println(map);
        System.out.println("init");
        exec.query(init);
       // System.out.println(gg);
        Mappings map = exec.query(q);
        System.out.println(map);
        System.out.println(map.size());
    }
    
    public void testProv() throws EngineException, LoadException {
        
        String init = FOAF_PREF 
                + "insert data { <John> foaf:knows <Jim>, <James> }";
        Graph g = Graph.create();
        Load ql = Load.create(g);
        ql.loadWE(data + "work/owlrl");
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        
        for (Entity ent : g.getEdges()){
            EdgeImpl edge = (EdgeImpl) ent;
            edge.add(g.getNode(DatatypeMap.newDate(), true, false));
            ent.setProvenance(g);

        }
        
        String query =FOAF_PREF 
                +"select *  where {"
                + "tuple(?p ?x ?y ?t ?prov)"
                //+ "tuple(foaf:knows <John>  ?v ?prov) "
                //+ "graph ?prov { <John>  ?p ?v }"
                + "}"
               // + "group by ?prov"
                + "";
        
        Mappings map = exec.query(query);
        System.out.println(map);
       // assertEquals("result", 2, map.size());
        
        
        for (Mapping m : map){
            for (Entity ent : m.getEdges()){
               // assertEquals("result", true, ent.getProvenance() != null);
            }
        }
        
    }

    
    public void testlist() throws EngineException {
        StringBuilder sb = new StringBuilder(FOAF_PREF);
        sb.append("insert data {\n");
        sb.append("<John> foaf:list (\n");
        for (int i = 0; i < 10; i++) {
            sb.append(i + " ");
        }
        sb.append(")}");

        System.out.println(sb);

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.setListPath(true);
        exec.query(sb.toString());
        QueryLoad ql = QueryLoad.create();

        //String query = ql.read(data + "work/list.rq");
         String query = ql.read(data + "work/prov.rq");
       Mappings map = null;
        Date d1 = new Date();
        int n = 1;
        for (int j = 0; j < n; j++) {
            System.out.println(j);
            map = exec.query(query);
        }
        Date d2 = new Date();

        System.out.println(map);
        System.out.println(map.size());
        System.out.println("time: " + (d2.getTime() - d1.getTime()) / (n * 1000.0));
        
        for (Mapping m : map) {
            for (Entity ent : m.getEdges()) {
                Node node = (Node) ent.getProvenance();
                System.out.println(node.getObject());
            }
        }
    }

    //@Test
    public void tOPT() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("Load");
        //ld.load(data + "template/sql/data/"); 
        //ld.load(data + "template/owl/data/primer.owl"); 
        //ld.load(data + "template/owl/data/galen.ttl"); 
        ld.load(data + "template/owl/data/hao.owl");

        QueryProcess exec = QueryProcess.create(g);


        String ppowl = data + "template/owl/template/uri.rq";
        String temp =
                SQL_PREF
                + "prefix f: <http://example.com/owl/families/> "
                + "template  { ?c ; separator = '\\n\\n'}"
                + "where {"
                + "?c a owl:Class "
                + "filter(isURI(?c)) "
                // + "filter(?c = f:Person)"                
                + "}"
                + "order by ?c "
                + "";

        temp =
                SQL_PREF
                + "prefix f: <http://example.com/owl/families/> "
                + "prefix e: <http://example.org/factkb#>"
                + "prefix obo: <http://purl.obolibrary.org/obo/> "
                + "prefix oo: <http://www.geneontology.org/formats/oboInOwl#>"
                + "prefix a: <http://api.hymao.org/api/ref/>"
                + "template  { kg:pprintWith(pp:owl)}"
                + "where {}"
                + "";


        /**
         * new: size in char: 4691 nb answer: 1
         *
         *
         * size in char: 3138 nbt: 2314 nb answer: 32
         */
        //PPrinter.setOptimizeDefault(false);
//         PPrinter.define(PPrinter.OWL, true);
        //PPrinter.setExplainDefault(true);
        //PPrinter.define(ppowl, true);
        Mappings map = exec.query(temp);
        //System.out.println(map);


//        if (map.getTemplateResult() != null){
//           // System.out.println(map.getTemplateResult().getLabel());
//           QueryLoad ql = QueryLoad.create();
//           ql.write( data + "work/tmp.txt", map.getTemplateResult().getLabel());
//            System.out.println("size in char: " + map.getTemplateResult().getLabel().length());
//        }
        // 8765
        if (!true) {
            Date d1 = new Date();
            int n = 5;
            for (int i = 0; i < n; i++) {
                System.out.println(i);
                map = exec.query(temp);
            }
            Date d2 = new Date();
            //System.out.println(map.getTemplateResult().getLabel());
            System.out.println(map.size());
            System.out.println("time: " + (d2.getTime() - d1.getTime()) / (n * 1000.0));
        }

        //System.out.println(map.getTemplateResult().getLabel());

        Query q = map.getQuery();
        PPrinter p = (PPrinter) q.getPP();
        System.out.println("nbt: " + p.nbTemplates());
        System.out.println("nb answer: " + map.size());
        // 22807 tps
        // 208228 tps
        // 126712
//        QueryLoad ql = QueryLoad.create();
//        String str = ql.read(data + "work/oumy.rq");
//        
//        ld.loadWE(data + "work/owlrl.brul");
        /**
         *
         * time: 0.348 nbt: 9050
         *         
* time: 0.0864 nbt: 787
         *
         *
         *
         *
         * time: 0.2672 nbt: 8765 size: 38
         *         
* time: 0.2448 nbt: 6562 size: 38
         */
    }

    public void tttt() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("Load");
        ld.load(data + "template/owl/data/primer.owl");
        QueryProcess exec = QueryProcess.create(g);

        String temp2 =
                "prefix f: <http://example.com/owl/families/>"
                // + "template { kg:pprintWith('/home/corby/AData/pprint/owl-save/template') }"
                + "template { kg:pprintWith(pp:owl) }"
                + "where {}";

        String temp =
                "prefix f: <http://example.com/owl/families/>"
                + "template debug { ?c ; separator = '\\n\\n'}"
                + "where {"
                + "?c a ?class "
                + "filter(isURI(?c))"
                //    + "filter(?c = f:Person)"
                + "}"
                + "values ?class { owl:Class }";

        Mappings map = exec.query(temp);




        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map.size());
        // 5296
    }

    public void ttt() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("Load");
        ld.load(data + "spin/data");
        ld.load(data + "sql/data");

        QueryLoad ql = QueryLoad.create();
        String temp = ql.read(data + "sql/query/mixte.txt");

        QueryProcess exec = QueryProcess.create(g);
        System.out.println("First");
        Mappings map = exec.query(temp);



        System.out.println("Start");
        Date d1 = new Date();
        int n = 10;
        for (int i = 0; i < n; i++) {
            System.out.println(i);
            map = exec.query(temp);
        }
        Date d2 = new Date();
        System.out.println(map.getTemplateResult().getLabel());
        System.out.println(map.size());
        System.out.println((d2.getTime() - d1.getTime()) / (n * 1000.0));
        // 0.51
    }

    public void tt() throws EngineException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "work/test.rq");
        SPINProcess sp = SPINProcess.create();
        Graph gg = sp.toSpinGraph(q);

        String temp = SPIN_PREF + FOAF_PREF
                + "template { ?q }"
                + "where { ?q a sp:Select }";
        //PPrinter.define(NSManager.SPIN, PPrinter.TURTLE);
        QueryProcess exec = QueryProcess.create(gg);
        Mappings map = exec.query(temp);

        System.out.println(map.getTemplateResult().getLabel());
    }

    public void testSpinQueryGraph2() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "work/test.rq");
        String t = ql.read(data + "work/target.rq");
        String t2 = ql.read(data + "work/target.rq");

        SPINProcess sp = SPINProcess.create();
        SPINProcess sp2 = SPINProcess.create();

        try {
            //System.out.println(sp.toSpin(q));
            Graph qg = sp.toSpinGraph(q);

            Graph tg = Graph.create();
            sp2.toSpinGraph(t, tg);
            sp2.toSpinGraph(q, tg);

            QueryProcess exec = QueryProcess.create(tg);

            QueryGraph qq = QueryGraph.create(qg);
            QueryGraphVisitorImpl vis = QueryGraphVisitorImpl.create();
            // replace string name by variable
            vis.addPredicate(NSManager.SPIN + "varName");
            qq.setVisitor(vis);

            //exec.setDebug(true);
            Mappings map = exec.query(qq);

            System.out.println(map);

            System.out.println(map.size());

            // root of query AST
            Node node = qg.getRoot();
            // System.out.println(node);

            PPrinter pp = PPrinter.create(tg, PPrinter.SPIN);
            pp.setNSM(sp.getNSM());

            int i = 1;
            for (Mapping m : map) {
                Node res = m.getNode(node);
                if (res != null) {
                    IDatatype dt = pp.pprint(res);
                    if (dt != null) {
                        System.out.println(i++);
                        System.out.println(dt.getLabel());
                    }
                }
            }

            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testAtest() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x ?p ?y"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testGG() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        System.out.println("load");
        ld.load("/home/corby/Work/dbpedia.ttl");
        QueryProcess exec = QueryProcess.create(g);
        System.out.println("query");

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix dbpedia-owl: <http://dbpedia.org/ontology/> \n"
                + "prefix geo:	<http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
                + "select distinct ?label  ?codepostal (datatype(?codepostal) as ?dt)  where {\n"
                + "?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
                + "?s rdfs:label ?label .\n"
                + "?s dbpedia-owl:postalCode ?codepostal .\n"
                + "?s dbpedia-owl:populationTotal ?pop .\n"
                + "?s geo:lat ?lat .\n"
                + "?s geo:long ?lon .\n"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getQuery().getAST());
            //System.out.println(map);
            ResultFormat f = ResultFormat.create(map);
            System.out.println("size: " + map.size());
            System.out.println(f);

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //@Test
    public void testToSpin() throws EngineException, IOException {
        String sparql =
                "prefix sp: <http://spinrdf.org/sp#>"
                + "select * where {"
                + "?x sp:test [ sp:test1 ?v1 ; sp:test2 ?v2] bind(concat(?v1, ?v2) as ?o)"
                + "}";

        QueryLoad ql = QueryLoad.create();
        InputStream stream = Graph.class.getResourceAsStream("/update/path.rq");
        String str = ql.read(stream);

        SPINProcess sp = SPINProcess.create();

        String spin = sp.toSpin(str);

        System.out.println(spin);

    }

    //@Test
    public void testJoin2() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James> ; "
                + "foaf:service <http://fr.dbpedia.org/sparql> "
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "?x foaf:knows ?y "
                + //  "; foaf:service ?s " +
                "service ?s {"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                + "} limit 2"
                + "}"
                + "service <http://fr.dbpedia.org/sparql>{"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "}"
                + "}"
                + "}"
                + "values ?s { <http://fr.dbpedia.org/sparql> <http://dbpedia.org/sparql>} "
                + //"pragma {kg:kgram kg:detail true}" +
                "";

        QueryProcess exec = QueryProcess.create(g);
        exec.setSlice(30);
        exec.setDebug(true);

        try {
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

            assertEquals("Result", 7, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testQM() {
        Graph g = Graph.create();
        QueryManager man = QueryManager.create(g);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:name 'John' ; foaf:age 18 "
                + "<Jim> foaf:name 'Jim' ; foaf:knows <John>"
                + "}";

        String query = "prefix sp: <http://spinrdf.org/sp#>"
                + "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:name ?n "
                + "?x foaf:knows ?p "
                + "minus { ?x foaf:age ?a } "
                + "<James> foaf:fake ?f "
                + "?f a foaf:Person "
                + "?f sp:elements ?e "
                + "?f sp:test ?t "
                + "filter(?b >= 20)"
                + "}";

        query = "select * where {"
                + "?x rdf:test* ?y"
                + "}";

        try {
            exec.query(init);

            Mappings map = man.query(query);
            System.out.println(map.getQuery().getAST());
            System.out.println(map);
            System.out.println("size: " + map.size());
            //assertEquals("result", 1, map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
            assertEquals("result", true, ex);
        }

    }

    public void test55() {


        String o1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Human rdfs:subClassOf foaf:Person "
                + "}";

        String o2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "}";

        String init1 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<John> a foaf:Human"
                + "}";

        String init2 = "prefix foaf: <http://foaf.org/>"
                + "insert data {"
                + "<Jack> a foaf:Man"
                + "}";




        String query = "prefix foaf: <http://foaf.org/>"
                + "select * where {"
                + "?x a foaf:Person"
                + "}";

        Graph o = Graph.create(true);
        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        QueryProcess exec1 = QueryProcess.create(g1);
        QueryProcess exec2 = QueryProcess.create(g2);

        QueryProcess exec = QueryProcess.create(g1, true);
        exec.add(g2);


        try {
            exec1.query(o1);
            exec1.query(init1);

            exec2.query(o2);
            exec2.query(init2);

//			exec.query(o1);
//			exec.query(o2);

            Mappings map = exec.query(query);
            System.out.println(map);

            String str = "123";
            System.out.println(str.toLowerCase().contains(str.toLowerCase()));

            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void test63() {
//        Graph.setValueTable(true);
//        Graph.setCompareKey(true);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<A> foaf:date 20 "
                + "} ;"
                + "insert data {"
                + "<B> foaf:date 20 "
                + "} ";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:date ?date"
                + "}"
                + "order by desc(?date)";


        String test = "prefix : <http://example.org/> "
                + "select ?z  where "
                + "{?s ?p ?o . "
                + "bind ((?o + 10) as ?z) "
                + "}";

        try {
            SPINProcess sp = SPINProcess.create();
            sp.setDebug(true);
            String s = sp.toSpinSparql(init);
            //System.out.println(s);
            Mappings map = exec.query(init);
//            System.out.println(map.getQuery().getAST());
//            System.out.println(graph.display());
//            for (Entity e : graph.getEdges()) {
//                System.out.println(e.getNode(1));
//            }
            sp.setDebug(true);
            String str = sp.toSpinSparql(test);
            Dataset ds = Dataset.create();
            ds.addFrom(Constant.create(Entailment.DEFAULT));
            //ds.addNamed(Constant.create("empty"));
            exec.setDebug(true);
            map = exec.sparql(str, ds);
            System.out.println(map);
            System.out.println(map.getQuery().getAST());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ;
           

     
    public void test14() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  where {"
                + "?x rdf:type c:Person; c:hasCreated ?doc "
                + "?doc rdf:type/rdfs:subClassOf* c:Document "
                + "c:Document rdfs:label ?l ;"
                + "rdfs:comment ?c"
                + "}";

        try {

            Graph g = Graph.create(true);
            Load ld = Load.create(g);
            //ld.setBuild(new MyBuild(g));

            init(g, ld);

            QueryProcess exec = QueryProcess.create(g);

            QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

            Mappings map = exec.query(query);
            assertEquals("Result", 68, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }





    }

//        public static void main(String[] args){
//        new TestUnit().testDistType();
//    }
    public void iiii() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "@prefix ast: <http://www.inria.fr/2012/ast#> "
                + "insert data {"
                + "[a ast:SelectQuery ; "
                + "ast:select ( '*' "
                + "[ast:var [a ast:Var ; ast:name '?xx'] ; "
                + " ast:exp [ast:fun 'self' ; ast:body ( [a ast:Var ; ast:name '?x'])]"
                + "]"
                + ") ;"
                + "ast:where ("
                + "[ast:subject  [a ast:Var ; ast:name '?x'] ;"
                + " ast:property [a ast:Var ; ast:name '?p'] ;"
                + " ast:object   [a ast:Var ; ast:name '?y']]"
                + ")"
                + "]"
                + "}";


        try {
            exec.query(init);

            PPrinter pp = PPrinter.create(g);
            IDatatype dt = pp.pprint();
            System.out.println(dt.getLabel());

            Mappings map = exec.query(g);
            System.out.println(map);


            assertEquals("Result", 1, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            assertEquals("Result", true, false);
            e.printStackTrace();
        }

    }

    public void testSPIN4() {
        Graph g = Graph.create(true);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person ; foaf:knows <James> "
                + "<Jim> a foaf:Person "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select ?x (count(?y) as ?c) where {"
                + " { ?x a foaf:Test } union { ?x a foaf:Person ; foaf:pp* :: $path ?z }"
                + "optional { ?x foaf:knows ?y } "
                + "minus { ?x a foaf:Test } "
                + "filter(bound(?x) && ?x != 12)"
                + "}"
                + "group by ?x "
                + "having (?c >= 0)";
        try {

            SPINProcess sp = SPINProcess.create();
            QueryProcess exec = QueryProcess.create(g);
            sp.setDebug(true);
            Mappings m = exec.query(init);
            String str = sp.toSpinSparql(query);
            Mappings map = exec.query(sp.toSparql(sp.toSpin(query)));
            System.out.println(map);
            System.out.println(map.getQuery().getAST());

            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSPIN() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select  "
                + "?x  (?x + 2 * isURI(?y) as ?z)"
                + "(count(distinct ?x) as ?c) "
                + "(group_concat(?x ; separator=';') as ?g) "
                + "from <http://example.org/gg1> "
                + "from named <http://example.org/gg2>"
                + "where {"
                + "service <http://example.org> {"
                + "values (?x ?y) { (12 13) (14 15) }  "
                + "{?x foaf:name 'John' } union { ?x foaf:name 'Jack'}  "
                + "graph ?g { ?x (foaf:knows/foaf:knows+)* ?z } "
                + "optional { ?x foaf:age (12)  minus { ?x foaf:age 222 }} "
                + "minus { ?x foaf:age 333 }"
                + "}"
                + "filter exists {?x ?p ?y}"
                + "}"
                + "group by ?y "
                + "order by desc(?x) ?x "
                + "having(?x != 0 && count() > 1)"
                + "values ?x { 23 undef 24}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                //                "load foaf:graph into graph foaf:gg ;"
                + "insert data {<John> foaf:knows <James>} ;"
                + "delete {?x ?p ?y}"
                + "insert {?z ?q ?t}"
                + "where {"
                + "?a foaf:knows ?b "
                + "filter(?a not in (22, 23))"
                + "}";

        String ask = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "ask { <John> foaf:name ?n  }";

        String desc = "describe ?x <John>";
        try {
            Query qq = exec.compile(query);
            System.out.println(qq.getAST());

            ASTQuery ast = exec.getAST(qq);

            SPIN sp = SPIN.create();
            sp.visit(ast);

            System.out.println("spin:\n" + sp);

            String str = sp.toString();

            ld.load(new ByteArrayInputStream(str.getBytes("UTF-8")), "spin.ttl");

            TemplateFormat tf = TemplateFormat.create(g, "/home/corby/AData/spin/template");
            tf.setNSM(ast.getNSM());
            System.out.println(tf);


        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoadException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testRes() {
        String fileName = "/junit/spin.rul";
        InputStream resource = this.getClass().getResourceAsStream(fileName);
        System.out.println("stream: " + resource);
        BufferedReader read = new BufferedReader(new InputStreamReader(resource));
        StringBuffer buf = new StringBuffer();
        try {
            while (true) {
                String str = read.readLine();
                if (str == null) {
                    read.close();
                    break;
                }
                buf.append(str);
                buf.append("\n");

            }
        } catch (IOException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("** res: \n" + buf);

    }

    public void testXML() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ foaf:member <John> , <Jack>] ."
                + ""
                + "<John> foaf:author 'b1', 'b2' "
                + "<Jack> foaf:author 'b4', 'b3' "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "template {kg:pprintWith(<" + data + "result>)}"
                + "where {}";

        String temp = QueryLoad.create().read(data + "test/template.rq");
        String tt = QueryLoad.create().read(data + "test/tmp.rq");


        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map.getTemplateResult().getLabel());
            System.out.println("size: " + map.size());

            XMLResult r = XMLResult.create(exec.getProducer());

            Mappings m = r.parseString(map.getTemplateResult().getLabel());

            System.out.println(m.getValue("?p"));

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testPPP() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ foaf:member <John> , <Jack>] ."
                + ""
                + "<John> foaf:author 'b1', 'b2' "
                + "<Jack> foaf:author 'b4', 'b3' "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + ""
                + "}";

        String temp = QueryLoad.create().read(data + "test/template.rq");
        String tt = QueryLoad.create().read(data + "test/tmp.rq");


        try {
            exec.query(init);
            Mappings map = exec.query(temp);
            System.out.println(map.getTemplateResult().getLabel());
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testList() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[ rdf:value (1 1 1 4 1 5) ]"
                + "}";

        String update = QueryLoad.create().read(data + "test/list.rq");

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x ?p ?e "
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

            exec.query(update);

            map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testMytest() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "[] foaf:arg1 1 ; foaf:arg11 3 ; foaf:arg2 2 "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "(xsd:integer(substr(?p, 1 + strlen(foaf:arg))) as ?o)"
                + "where {"
                + "?x ?p ?v"
                + "}"
                //+ "order by ?p "
                + "order by xsd:integer(substr(?p, 1 + strlen(foaf:arg)))"
                + "";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("size: " + map.size());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void testDataset() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        Dataset ds = Dataset.create();
        ds.setUpdate(true);
        ds.addFrom("http://inria.fr/g2");
        ds.addNamed("http://inria.fr/g1");

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "graph <http://inria.fr/g1> {"
                + "<John> foaf:name 'John' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/g2> {"
                + "<Jim> foaf:name 'Jim' ; a foaf:Person"
                + "}"
                + "graph <http://inria.fr/o> {"
                + "foaf:Person rdfs:subClassOf foaf:Human"
                + "}"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from named <http://inria.fr/g2>"
                + "where {"
                + "    {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String query2 = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * "
                + "from <http://inria.fr/g2>"
                + "where {"
                + " graph ?g   {?x rdf:type foaf:Person ; ?p ?y}"
                + "}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "delete  where {?x ?p ?y}";

        try {
            QueryProcess sparql = QueryProcess.create(g);
            sparql.query(init);

            Mappings map = sparql.sparql(query, ds);
            assertEquals("result", 0, map.size());

            QueryProcess exec = QueryProcess.create(g);
            Mappings map2 = exec.query(query, ds);
            assertEquals("result", 2, map2.size());

            map = sparql.sparql(query2, ds);
            assertEquals("result", 0, map.size());

            map2 = exec.query(query2, ds);
            assertEquals("result", 2, map2.size());

        } catch (EngineException ex) {
            System.out.println(ex);

        }



    }

    public void testsrjfhgk() {
        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = Graph.create();
        Graph.setValueTable(true);
        Graph.setCompareKey(true);
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            for (Mapping m : map) {
                IDatatype dt = (IDatatype) m.getValue("?a");
                System.out.println(dt);
                System.out.println(dt.getClass().getName());
            }
            System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testNSM() {
        NSManager nsm = NSManager.create();

        nsm.setBase("http://example.org/x/");

        nsm.definePrefix("", "#");

        System.out.println(nsm.toNamespaceB(":x"));

        System.out.println(nsm.toNamespaceB("x"));

        //DatatypeMap.setLiteralAsString(false);

        IDatatype dt = DatatypeMap.newStringBuilder("test");

        System.out.println(dt.toString());




        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(data + "sql/data/q1.ttl");

        PPrinter pp = PPrinter.create(g, data + "sql/template");

        System.out.println(pp.pprint());

    }

    public void testAtest1() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:knows <Jim> "
                + "<Jack> foaf:name 'Jack' "
                + ""
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:knows ?y"
                + "}";

        String update = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "delete  {?x foaf:knows ?y}"
                + "insert {?x rdfs:seeAlso ?y}"
                + "where {?x foaf:knows ?y}";

        String delete = "prefix foaf:    <http://xmlns.com/foaf/0.1/>"
                + "delete "
                + "where {?x foaf:knows ?y}";

        GListener gl = GListener.create();
        g.addListener(gl);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
//            System.out.println(map);
//            System.out.println("size: " + map.size());

            map = exec.query(update);

            // map = exec.query(delete);


            Graph gg = Graph.create();
            gg.index();
            for (Operation o : gl.getOperations()) {
                for (Entity ent : o.getDelete()) {
                    gg.delete(ent);
                }
                for (Entity ent : o.getInsert()) {
                    gg.insert(ent);
                }
            }

            System.out.println(gg.display());


            String q = "select (max(count(*)) as ?c) where {"
                    + "?x ?p ?y "
                    + "} group by ?x";

            Mappings m = exec.query(q);
            System.out.println(m);

//            System.out.println(map);
//            XMLFormat f = XMLFormat.create(map);
//            System.out.println(f);
//            System.out.println("size: " + map.size());
//            System.out.println(g.display());

        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testAG() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.setSkolem(true);

        // construct graph setSkolem(true)
        exec.setSkolem(true);

        String q = "prefix foaf: <http://foaf.org/>"
                + "select  * "
                + "('a-01-16'^^foaf:date as ?d)"
                + "('a-01-16'^^foaf:date as ?dd)"
                + "(datatype(?d) as ?dt) (?d = ?dd as ?bool)"
                + "('-10-01-16'^^xsd:date as ?d2)"
                // + "(kg:skolem(bnode()) as ?bb)"
                + "where {"
                + ""
                + "?a ?p ?b "
                //+ "filter(kg:isSkolem(?b))"
                // check that blank is not already in another graph than gNode

                + "}";


        String q2 = "prefix foaf: <http://foaf.org/>"
                + "construct { ?x ?p ?b ; rdfs:seeAlso ?b }"
                + "where {"
                + "{select * ((bnode()) as ?x) where {"
                + ""
                + "?a ?p ?b "
                //+ "filter(kg:isSkolem(?a))"
                + "}}"
                + "}";


        String q3 = "prefix foaf: <http://foaf.org/>"
                + "construct { [ ?p ?b ; rdfs:seeAlso ?b ] }"
                + "where {"
                + ""
                + "?a ?p ?b "
                // + "filter(kg:isSkolem(?a))"

                + "}";



        String q5 = "prefix foaf: <http://foaf.org/>"
                + "construct { ?a ?p ?b ; rdfs:seeAlso ?b }"
                + "where {"
                + "?a ?p ?b "
                + "}";

        String q6 = "prefix foaf: <http://foaf.org/>"
                + "select * "
                + "where {"
                + "{?a ?p ?b} union {?b ?p ?a} "
                + "}";


        String init = "prefix foaf: <http://foaf.org/>"
                + "insert data { "
                + "[foaf:name 'John' ; foaf:knows [ foaf:name 'James' ] ] "
                + ""
                + "}";


        try {
            exec.query(init);
            Mappings map = exec.query(q2);
            //System.out.println(map);
            System.out.println(exec.getGraph(map).display());

            map = exec.query(q2);
            //System.out.println(map);
            System.out.println(exec.getGraph(map).display());




//            exec.skolem(map);
//            
////            Node qn = map.getQueryNode("?b");
////            Node n = map.getNode("?b");
////            System.out.println(n);
////            
////            Mapping m = Mapping.create(qn, n);
////            
////            map = exec.query(q6, m);
//            System.out.println(map);
//            System.out.println(map.size());


//            map = exec.query(q6);
//            System.out.println(map);
//            System.out.println(exec.skolem(map));
//            
//
//            System.out.println(map.size());
//            ResultFormat f = ResultFormat.create(map);
//            System.out.println(f);
//            
//            map = exec.query(q5);
//            System.out.println(map);
//            System.out.println(map.size());
//             f = ResultFormat.create(map);
//            System.out.println(f);


//            CSVFormat csv = CSVFormat.create(map);
//            System.out.println(csv);
//            TSVFormat tsv = TSVFormat.create(map);
//            System.out.println(tsv);
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

    /**
     *
     *
     * hash: 766488133 hash: 211543962 hash: 1839367010 hash: 169456094
     */
    public void testValidate() {

        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "comma/model.rdf");
            ld.loadWE(root + "comma/comma.rdfs");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q =
                "template  { kg:pprintWith(<" + data + "validate/template> ) }"
                + "where {  }";

        QueryLoad qr = QueryLoad.create();
        String qq = qr.read(data + "validate/instance.rq");

        QueryProcess exec = QueryProcess.create(g);

        try {
            ASTQuery.setTemplateAggregate("agg_and");
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            System.out.println("** Test: \n" + node.getLabel());

            map = exec.query(qq);
            System.out.println(map);
            System.out.println(map.size());

            assertEquals("result", "false", node.getLabel());

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testVR() {
        ValueResolverImpl vr = new ValueResolverImpl();
        HashMap h = new HashMap();
        Date d1 = new Date();
        String value = "http://www.inria.fr/schema/name/";
        String key = vr.getKey(value);
        System.out.println(key);
        System.out.println(key.length());
        System.out.println(value.length());

        h.put(key, value);
        h.put(value, value);
        for (int i = 0; i < 1000000; i++) {
            key = vr.getKey(value);
            // vr.hashByte(value);
            //h.put(key, value);
            h.get(key);
            // System.out.println(str);
        }
        Date d2 = new Date();
        System.out.println((d2.getTime() - d1.getTime()) / 1000.0);

    }

    public void testValues() {
        String init =
                "insert data {"
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 21.0 "
                + "}";

        String query =
                "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter(?a = ?b && ?x != ?y) "
                + "}";

        String query2 =
                "select  * where {"
                + "{select (21.0 as ?a) where {}}"
                + "?x foaf:age ?a "
                + "}";

        String query3 =
                "select  * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());

            map = exec.query(query2);
            System.out.println(map);
            assertEquals("Result", 1, map.size());

            map = exec.query(query3);
            System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testValues2() {
        String init =
                "insert data {"
                + "<Jim>  foaf:age 21.0 "
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 22 "
                + "}";

        String query =
                "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter(?a = ?b && ?x != ?y) "
                + "}";

        String query2 =
                "select debug * where {"
                // + "{select (23.0 as ?a) where {}}"
                + "?x foaf:age ?a "
                + "}"
                + "group by ?a "
                + "having (min(?x) = <Jim>)"
                + "limit 1";

        String query3 =
                "select  * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
//            Mappings map = exec.query(query);
//            System.out.println(map);
//            assertEquals("Result", 2, map.size());

            Mappings map = exec.query(query2);
            System.out.println(map);
            System.out.println(map.getQuery().getAST());

            assertEquals("Result", 1, map.size());

//            map = exec.query(query3);
//            System.out.println(map);
//            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testtest() {
        // System.out.println(graph.getValueResolver().getCount());
        System.out.println(graph.getValueResolver().size());
    }

    public void TestOnto() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "c:Human rdfs:subClassOf c:Animal "
                + "c:Man   rdfs:subClassOf c:Human "
                + "c:Woman rdfs:subClassOf c:Human "
                + ""
                + "<John> a c:Man "
                + "<Tigrou> a c:Cat "
                + "<Mary> a c:Woman "
                + "<James> a c:Human "
                + "}";

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {"
                + "?x a c:Human, ?t"
                + "}";
        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            System.out.println("Size: " + graph.getValueResolver().size());
            //System.out.println("Size: " + graph.getValueResolver().getCount());

        } catch (EngineException ex) {
            Logger.getLogger(TestQuery1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testValues3() {
        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 3, map.size());


        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    public void testIDX() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> foaf:age '1'^^xsd:integer "
                + "<Jack> foaf:age '1'^^xsd:long "
                + "}";

        String q =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?a"
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            System.out.println(g.display());
            Mappings map = exec.query(q);
            System.out.println(map);

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void test18() {
        String query = "select ?a ?b where {"
                + "c:Person rdfs:subClassOf+ :: $path ?c "
                + "graph $path {?a ?p ?b}"
                + "}"
                + "order by ?a ?b ";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 28, map.size());
        } catch (EngineException e) {
            assertEquals("Result", 28, e);
        }

    }

    public void testMath() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "math/data");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q =
                "prefix m: <http://ns.inria.fr/2013/math#>"
                + "template  { kg:pprintWith(?p) }"
                + "where { ?p a m:PrettyPrinter }";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            System.out.println(node.getLabel());

            assertEquals("result", true, node.getLabel().length() > 10);

        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testDB() {
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "dbpedia/query/text.txt");
        System.out.println(q);
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            TripleFormat f = TripleFormat.create(map);
            System.out.println(f);
            System.out.println(map.size());
        } catch (EngineException ex) {
            Logger.getLogger(TestUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test2graph() {
        Graph go = Graph.create(true);
        Load load = Load.create(go);
        load.load("http://www-sop.inria.fr/edelweiss/software/corese/v2_4_0/data/human_2007_09_11.rdfs");

        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.load("http://www-sop.inria.fr/edelweiss/software/corese/v2_4_0/data/human_2007_09_11.rdf");

        QueryProcess exec = QueryProcess.create(go);
        exec.add(g);

        String q =
                "PREFIX h: <http://www.inria.fr/2007/09/11/humans.rdfs#>"
                + "SELECT *   WHERE{"
                + " graph ?g {?x rdf:type h:Male}"
                + "}";

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void testBug() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        Load load = Load.create(g);
        load.load(data + "kgraph/rdf.rdf", RDF.RDF);
        load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
        load.load(data + "comma/comma.rdfs");
        load.load(data + "comma/commatest.rdfs");
        load.load(data + "comma/model.rdf");
        load.load(data + "comma/testrdf.rdf");
        load.load(data + "comma/data");
        load.load(data + "comma/data2");

        String q = "select * where {?x ?p ?y optional{?y rdf:type ?class} filter (! bound(?class) && ! isLiteral(?y))}";

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testPerf() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.setLimit(2000000);
        Date d1 = new Date();

//		ld.load(root + "alban/drugbank_dump.ttl");
//		ld.load(root + "alban/article_categories_en.ttl");

        ld.load(root + "alban2/");


        Date d2 = new Date();
        System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);


        System.out.println(g.size());
        System.out.println(g);
        System.out.println(g.getIndex());

        String sparqlQuery = "SELECT ?predicate ?object WHERE {"
                + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }"
                + " UNION    "
                + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff ."
                + "     ?caff ?predicate ?object . } "
                + "}";

        String edgeSelect =
                "SELECT * WHERE { "
                + //"?x rdfs:label ?object . } limit 1";
                "<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }";
        // "<http://dbpedia.org/resource/Category:%22Weird_Al%22_Yankovic_albums> ?predicate ?object . }";

        String distinct =
                "SELECT distinct ?predicate  WHERE { "
                + "<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }";


        String edgeConstruct =
                "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object } "
                + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object .}";
        String edgeConstruct2 =
                "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object } "
                + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object .}";

        try {
            d1 = new Date();
            Mappings map1 = exec.query(distinct);

            d2 = new Date();

            System.out.println(map1);
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

            d1 = new Date();
            StopWatch sw = new StopWatch();
            Mappings map2 = null;
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                sw.reset();
                sw.start();
                map2 = exec.query(edgeSelect);
                System.out.println(sw.getTime());
            }
            d2 = new Date();

            System.out.println(map2);
            System.out.println("Time : " + (d2.getTime() - d1.getTime()) / 1000.0);



            System.out.println("nb prop: " + map1.size());







        } catch (EngineException e) {
            e.printStackTrace();
        }

        TemplateFormat tf = TemplateFormat.create(g, PPrinter.PPRINTER);
        //System.out.println(tf);

    }

    public void testPPAgg() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix ex: <http://www.example.org/>"
                + "insert data {"
                + "ex:Jack a ex:Man ; ex:name 'Jack' "
                + "ex:Jim a ex:Man  "
                + "ex:John a ex:Man ; ex:name 'John' "
                + "}";

        String t1 = "prefix ex: <http://www.example.org/>"
                + "template {group_concat(if (bound(?n), ?n, '') ; separator = ';')} "
                + "where {?in a ex:Man optional {?in ex:name ?n}}";

        String q = "prefix ex: <http://www.example.org/>"
                + "select (group_concat(?n ; separator = ';') as ?out) "
                + "where {?in a ex:Man optional {?in ex:name ?n}}";


        try {
            exec.query(init);
            PPrinter f = PPrinter.create(g);
            f.defTemplate(t1);
            f.trace();

            System.out.println(f);


            Mappings map = exec.query(q);
            System.out.println(map);
        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testPPrint2() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        //g.init();

        ld.load(root + "pprint/data");
        //ld.load(cos + "ontology/carto.owl");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();

        int length = str.length();

        str = nsm.toString() + str;

        System.out.println(str);
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
        System.out.println(str.length());



    }

    public void testPPrint() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        ld.load(root + "pprint/data/");

        NSManager nsm = NSManager.create();
        nsm.definePrefix("ex", "http://www.example.org/");
        nsm.definePrefix("ast", "http://www.inria.fr/2012/ast#");

        Date d1 = new Date();

        TemplateFormat tf = TemplateFormat.create(g);
        tf.setPPrinter(root + "pprint/asttemplate");
        tf.setNSM(nsm);
        String str = tf.toString();

        Date d2 = new Date();
        System.out.println(str);

        assertEquals("Results", 3058, str.length());
        System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
    }

    public void testBib() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.load(root + "bib/data/glc-all-utf.rdf");

        QueryLoad ql = QueryLoad.create();

        String q = ql.read(root + "bib/query/title.rq");





        try {
            Mappings map = exec.query(q);
            IDatatype dt = (IDatatype) map.getValue("?tt");

            System.out.println(dt.getLabel());

//			try {
//				FileWriter f = new FileWriter(root + "bib/corpus.txt");
//				f.write(dt.getLabel());
//				f.flush();
//				f.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

            //System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testPPCount() {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String query = "select * where {"
                + "service <test> {select (count(*) as ?c) where {}}"
                + "}";

        try {
            Query q = exec.compile(query);
            System.out.println(q.getAST());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testAlban2() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);


        Load ld = Load.create(graph);

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "test/inference-atest.rq");


        ld.load(root + "test/FIELD2.rdf");

        try {
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testExam() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "graph <g1> { <a> <p> <b>, <c> "
                + "<b> <p> <a> "
                + "<c> <p> <a>}"
                + "}";

        String query = "ask {"
                + "filter (not exists {"
                + " {?x ?p ?y "
                + " filter not exists { ?y ?p ?x}"
                + "}"
                + "})"
                + "}";

        query = "select * where {?x ?p ?y}";

        Load ld = Load.create(graph);
        ld.setRenameBlankNode(false);

        ld.load(root + "test/luc.ttl");


        try {
            //exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map.size());
            System.out.println(map);

//			Node n = graph.getResource("a");
//			Node p = graph.getResource("p");
//			
//			graph.getEdges(n);
//			
//			for (Entity ent : graph.getEdges(p, n, 0)){
//				System.out.println(ent);
//			}
//			
//			System.out.println(graph.getEdge(p, n, 0));


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testReverseList() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "[rdf:value (1 2 3)]"
                + "}";

        String query =
                "delete {"
                + "?y rdf:rest ?x ?x rdf:first ?e	"
                + "?xx rdf:first ?ee "
                + "}"
                + "insert {graph <g1> {"
                + "?x rdf:rest ?y ; rdf:first ?e ."
                + "?xx rdf:first ?ee ; rdf:rest rdf:nil "
                + "}}"
                + "where {"
                + "{?y rdf:rest ?x ?x rdf:first ?e}"
                + "union"
                + "{?xx rdf:first ?ee minus {?yy rdf:rest ?xx}}"
                + "}";

        query = "select ?e where {"
                + "rdf:nil (^rdf:rest)*/rdf:first ?e"
                + "}";



        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());



            TripleFormat f = TripleFormat.create(graph);
            f.with(Entailment.ENTAIL);
            System.out.println(f);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testGC() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://example.org/> "
                + "insert data {"
                + "[ex:name 'John' , 'Jim']"
                + "[ex:name 'John' , 'Jim']"
                + "}"
                + "";

        String query = "prefix ex: <http://example.org/> "
                + "select (group_concat(distinct self(?n1), ?n2 ;  separator='; ') as ?t) where {"
                + "?x ex:name ?n1 "
                + "?y ex:name ?n2 "
                + "filter(?x != ?y)"
                + ""
                + "}";

        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);

            IDatatype dt = (IDatatype) map.getValue("?t");
            System.out.println(dt.getLabel().length());
            assertEquals("Results", 42, dt.getLabel().length());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testQueryGraph() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String cons =
                "prefix : <http://example.org/> "
                + ""
                + "construct {?x :p []}"
                + "where {?x :p ?y}";

        String init2 =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p [] ."
                + "}";


        try {
            // create a graph
            exec.query(init);

            // create a copy where triple objects (values) are Blank Nodes (aka Variables)
            // consider the copy as a Query Graph and execute it
            Mappings map = exec.queryGraph(cons);

            assertEquals("Results", 4, map.size());

            Graph g2 = Graph.create();
            QueryProcess exec2 = QueryProcess.create(g2);
            exec2.query(init2);

            QueryGraph qg = QueryGraph.create(g2);
            QGV vis = new QGV();
            qg.setVisitor(vis);
            //qg.setConstruct(true);
            map = exec.query(qg);

            //Graph res = exec.getGraph(map);
            System.out.println(map.toString(true));
            System.out.println(map.size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    class QGV implements QueryGraphVisitor {

        public Graph visit(Graph g) {
            return g;
        }

        public ASTQuery visit(ASTQuery ast) {
            return ast;
        }

        public Entity visit(Entity ent) {
            return ent;
        }

        public Query visit(Query q) {
            //q.setLimit(1);
            return q;
        }
    }

    public void test47() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<person><name>John</name><lname>K</lname></person>"
                + "<person><name>James</name><lname>C</lname></person>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = ""
                + "prefix i: <http://www.inria.fr/test/> "
                + "select (concat(?n, '.', ?ll) as ?name) where {"
                + "?x i:contain ?xml "
                + "{select  (xpath(?xml, '/doc/person') as ?p) where {}}"
                + "{select  (xpath(?p, 'name/text()')  as ?n)  where {}}"
                + "{select  (xpath(?p, 'lname/text()') as ?l)  (concat(?l, ' 123') as ?ll ) where {}}"
                + "}";


        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testCompile() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String query =
                "select * where {"
                + "graph ?g {?x ?p ?y "
                + "{select * where {"
                + "?a (rdf:type@[a rdfs:Resource]) ?b  "
                + "{values ?a {<John>}}"
                + "}"
                + "order by ?a "
                + "group by ?b "
                + "having (?a > ?b) "
                + "}"
                + "?a (rdf:type@[a rdfs:Resource]) ?b"
                + ""
                + "}"
                + "}";

        try {
            Mappings map = exec.query(query);
            Query q = map.getQuery();

            assertEquals("Result", 16, q.nbNodes());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testIndex() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //exec.addEventListener(EvalListener.create());

        String init =
                "prefix ex: <http://www.example.org/>"
                + "insert data {"
                + "ex:a ex:p1 '2'^^xsd:integer "
                + "ex:a ex:p1 '2'^^xsd:long "
                + //				"ex:a ex:p2 '2'^^xsd:integer "  +
                //				"ex:a ex:p2 '2'^^xsd:long "  +
                //				
                //				"ex:a ex:p3 '2'^^xsd:float " +
                //				"ex:a ex:p3 '2'^^xsd:double " +
                //				
                //				
                //				"ex:a ex:p 'toto' " +
                //				"ex:a ex:p 'toto'^^xsd:string " +
                "}"
                + ""
                + "";

        String q =
                "prefix ex: <http://www.example.org/>"
                + " select * where {"
                + "?x ex:p1* ?y "
                + //"filter(?y = 2)" +
                "}";

//		q = 
//				"prefix ex: <http://www.example.org/>" +				
//				" select * where {" +
//				"?x ex:p ?y " +
//				"}";

        try {
            exec.query(init);

            Mappings map = exec.query(q);

            System.out.println(g.display());



            System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testPP() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "@prefix ast: <http://www.inria.fr/2012/ast#> "
                + "insert data {"
                + "[a ast:SelectQuery ; "
                + "ast:select ( '*' "
                + "[ast:var [a ast:Var ; ast:name '?xx'] ; "
                + " ast:exp [ast:fun 'self' ; ast:body ( [a ast:Var ; ast:name '?x'])]"
                + "]"
                + ") ;"
                + "ast:where ("
                + "[ast:subject  [a ast:Var ; ast:name '?x'] ;"
                + " ast:property [a ast:Var ; ast:name '?p'] ;"
                + " ast:object   [a ast:Var ; ast:name '?y']]"
                + ")"
                + "]"
                + "}";

        String q = "prefix ast: <http://www.inria.fr/2012/ast#> "
                + "select * where {?in ast:select ?s}";

        try {
            exec.query(init);

            PPrinter pp = PPrinter.create(g);
            //pp.setDebug(true);
            IDatatype dt = pp.pprint();
            System.out.println(dt.getLabel());

            Mappings map = exec.query(dt.getLabel());
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testFF() {

        System.out.println(String.format("%g", new Double(1.23456789e6).doubleValue()));
        System.out.println(String.format("%f", new Double(1.23456789e6).doubleValue()));
        System.out.println(String.format("%e", new Double(1.23456789e6).doubleValue()));

        System.out.println(String.format("%g", new Double(1.54e6).doubleValue()));
        System.out.println(String.format("%g", new Double(1e6).doubleValue()));
        System.out.println(String.format("%.1g", new Double(1e6).doubleValue()));
        System.out.println(String.format("%f", new Double(1e6).doubleValue()));
        System.out.println(new Double(1e6).doubleValue());

    }

    public void testAST() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        ld.load(root + "pprint/pprint.ttl");

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "pprint/pprint.rq");
//		String q1 = ql.read(root + "test/pprint1.rq");
//		String q2 = ql.read(root + "test/pprint2.rq");

        //System.out.println(q);

        //System.out.println(g.display());

        QueryProcess exec = QueryProcess.create(g);

        try {

            Mappings map1 = exec.query(q + "values ?pp {ast:construct}");
            Mappings map2 = exec.query(q + "values ?pp {ast:where}");

            IDatatype cst = (IDatatype) map1.getValue("?res");
            IDatatype whr = (IDatatype) map2.getValue("?res");

            System.out.println("construct {" + cst.getLabel() + "}");
            System.out.println("where {" + whr.getLabel() + "}");



        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testJoin2xcgf() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<John> foaf:name 'John' "
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>"
                + "<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>"
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "filter(regex(?n, '^Augustin'))"
                + "}  "
                + "limit 20"
                + ""
                + "}"
                // + "?x foaf:knows ?y "
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?y rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                + "}  "
                + "limit 20"
                + "}"
                + "}"
                + "pragma {kg:kgram kg:detail true}";

        String qq = "select * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {"
                + "?y rdfs:label ?n "
                + "filter(regex(?n, '^August'))"
                //                + "filter ((((((((((((((((?n = 'Augustin'@fr) || "
                //                + "(?n = 'Augustins'@fr)) || "
                //                + "(?n = \"Augustins de l'Assomption\"@fr)) || "
                //                + "(?n = 'Augustin français'@fr)) || "
                //                + "(?n = 'Augustin Bonrepaux'@fr)) || "
                //                + "(?n = 'Augustin Berque'@fr)) || "
                //                + "(?n = 'Augustin Matata Ponyo'@fr)) || "
                //                + "(?n = 'Augustin Challamel'@fr)) || "
                //                + "(?n = 'Augustin Bernard'@fr)) || "
                //                + "(?n = 'Augustin d’Hippone'@fr)) || "
                //                + "(?n = 'Augustinus'@fr)) || "
                //               // + "(?n = 'Augustin d\'Hippone'@fr)) || "
                //                + "(?n = 'Augustin Fabre'@fr)) || (?n = 'Augustin, roi du kung-fu'@fr)) || (?n = 'Augustin Louis Cauchy'@fr)) || (?n = 'Augustin Fresnel'@fr)) || (?n = 'Augustin Chauvet'@fr)) " 
                + "}  "
                //+ "limit 20"
                + "}}";

        QueryProcess exec = QueryProcess.create(g);
        // exec.setSlice(30);
        //exec.setDebug(true);

        try {
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

            assertEquals("Result", 26, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testUpdateSyntax() {
        Graph g = Graph.create();
        String init =
                "prefix ex: <http://www.example.org/test/> "
                + "insert data {"
                + "ex:test1 ex:name 'John'"
                + "} ;"
                + "prefix ex: <http://www.example.org/> "
                + "insert data {"
                + "ex:test2 ex:name 'Jack'"
                + "} ;"
                + "prefix ex: <http://www.example.org/test/> "
                + "create graph  ex: ; "
                + "delete  {ex:test2 ex:name 'Jack'} "
                + "insert {?x a ex:Person} where {?x ex:name ?n}"
                + "";

        String q = "select * where {?x ?p ?y}";

        QueryProcess exec = QueryProcess.create(g);
        exec.setDebug(true);
        try {
            Mappings map = exec.query(init);
            ASTQuery ast = exec.getAST(map);

            System.out.println(ast);
            System.out.println(map);
            System.out.println(map.size());

            map = exec.query(q);
            System.out.println(map);



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void test15() {

        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(data + "comma/comma.rdfs");

        String q1 =
                "select  *  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";

        String q2 =
                "select (kg:similarity() as ?sim) (max(kg:depth(?x)) as ?max)  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";
        try {

            QueryProcess exec = QueryProcess.create(g);
//			System.out.println("q1");
            exec.query(q1);
//			System.out.println("q2");
//			System.out.println(g.getClassDistance());
//			Mappings map = exec.query(q2);
//			Node n = map.getNode("?max");
//			System.out.println(n);
//			System.out.println(map);
//			System.out.println(map.size());

//			IDatatype dt = (IDatatype) n.getValue();
//			assertEquals("Result", 13, dt.intValue()); 
            Node node = g.getResource("http://www.inria.fr/acacia/comma#Person");
            Node type = g.getPropertyNode(RDF.TYPE);
            for (Entity e : g.getEdges(type, node, 0)) {
                System.out.println(e.getNode(1));
            }

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    /**
     * Rule engine with QueryExec on two graphs
     */
    public void test6() {
        QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        Load load1 = Load.create(g1);
        Load load2 = Load.create(g2);

        load1.load(data + "engine/ontology/test.rdfs");
        load2.load(data + "engine/data/test.rdf");

        QueryProcess exec = QueryProcess.create(g1);
        exec.add(g2);
        RuleEngine re = RuleEngine.create(g2, exec);
        //re.setOptimize(true);

        load2.setEngine(re);

        try {
            load2.loadWE(data + "engine/rule/test2.brul");
            load2.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }






        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select     * where {"
                + "?x c:hasGrandParent c:Pierre "
                + "}";





        re.process();

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 4, map.size());
            System.out.println(map);
        } catch (EngineException e) {
            assertEquals("Result", 4, e);
        }

    }

    public static void main22(String[] args) {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);


        Load ld = Load.create(g);

        //ld.load(data + "comma/comma.rdfs");
//		ld.load(data + "comma/model.rdf");
//		ld.load(data + "comma/data");


        String rule =
                "rule construct {?x rdfs:subClassOf ?y }"
                + "where {?x rdfs:subClassOf ?y }";

        String q =
                "select * where {"
                + " graph ?g {?x ?p ?y}"
                + "}";

        String q2 = ""
                + "insert data  {"
                + "graph <g1> { _:b rdfs:label 'John'}"
                + "graph <g2> { _:b rdfs:label 'Jack'}} ;"
                + "";

        String q3 =
                "INSERT  { GRAPH :g1  { _:b :p :o } } WHERE {};"
                + "INSERT  { GRAPH :g2  { _:b :p :o } } WHERE {}";

        try {
            exec.query(rule);
            exec.query(q3);
            Mappings map = exec.query(q);

            System.out.println(map);
            System.out.println(ResultFormat.create(map));

            System.out.println(map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testWF2() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";


        String query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}"
                + "pragma{kg:kgram rdfs:entailment true}";


        String query2 = "drop graph kg:entailment "
                + "pragma{kg:kgram rdfs:entailment false}";

        String query3 =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}";

        try {

            g.getWorkflow().setDebug(true);

            exec.query(init);
            Mappings map = exec.query(query);


            System.out.println(map);
            System.out.println(map.size());

            System.out.println("query2");

            exec.query(query2);

            System.out.println("query3");

            map = exec.query(query3);

            System.out.println(map);
            System.out.println(map.size());

            //g.getEntailment().setActivate(true);
            //g.process();

            map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());


        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testWFQE() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        QueryEngine qe = QueryEngine.create(g);
        g.addEngine(qe);

        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "[ c:hasParent [] ]"
                + "}";


        String update =
                "prefix c: <http://example.org/>"
                + "insert {?y c:hasChild ?x}"
                + "where { ?x c:hasParent ?y}";

        qe.addQuery(update);
//		qe.setDebug(true);
//		g.getWorkflow().setDebug(true);

        String query = "select * where {?x ?p ?y}";

        try {
            //System.out.println("init");
            exec.query(init);
            //System.out.println("query");

            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            assertEquals("Result", true, false);
        }



    }

    public void testWF() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.setClearEntailment(true);
        String init =
                "prefix c: <http://example.org/>"
                + "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop =
                "prefix c: <http://example.org/>"
                + "delete data {"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human}"
                + "pragma {kg:kgram kg:detail true}";

        String query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        query =
                "prefix c: <http://example.org/>"
                + "select  *  where {"
                + "graph ?g {?x rdf:type ?c}"
                + "}";

        String rule =
                "prefix c: <http://example.org/>"
                + "construct {?x a c:Human}"
                + "where {?x c:name ?n}";

        String upd =
                "prefix c: <http://example.org/>"
                + "insert data {<Jack> a c:Human}";

        RuleEngine re = RuleEngine.create(g);
        g.addEngine(re);

        QueryEngine qe = QueryEngine.create(g);
        try {
            qe.defQuery(upd);
        } catch (EngineException e2) {
            e2.printStackTrace();
        }
        g.addEngine(qe);


        g.getWorkflow().setDebug(true);
        try {
            re.defRule(rule);
        } catch (EngineException e1) {
            e1.printStackTrace();
        }

        try {
            exec.query(init);
            Mappings m = exec.query(drop);
            System.out.println(XMLFormat.create(m));
            g.remove();
            g.getWorkflow().setActivate(false);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            g.getWorkflow().setActivate(true);
            g.process();
            map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    /**
     * 26.263 25.246
     *
     *
     */
    public void test() {
        Date d1 = new Date();
        for (int i = 0; i < 10; i++) {
            testRule();
        }
        Date d2 = new Date();
        System.out.println((d2.getTime() - d1.getTime()) / 1000.0);

    }

    public void testRule() {

        Graph g = Graph.create(true);
        //g.addListener(GraphListenerImpl.create(10000));
        QueryProcess exec = QueryProcess.create(g);



        Load ld = Load.create(g);

        ld.load(data + "comma/comma.rdfs");
        ld.load(data + "comma/model.rdf");
        ld.load(data + "comma/data");
        //ld.load(data + "comma/data2");

        //ld.load(data + "comma/test.rul");
        RuleEngine re = ld.getRuleEngine();
        ld.getQueryEngine();

        RuleEngine re2 = RuleEngine.create(g);
        ld.setEngine(re2);

        Entailment ent = Entailment.create(g);

        String loadrule = "load  <" + data + "comma/test.rul>  ";

        String rule = "select * where {graph kg:rule {?x ?p ?y}} ";

        String entail = "select * where {graph kg:entailment {?x ?p ?y}} ";

        String drop = "drop graph kg:rule ;"
                + "drop graph kg:entailment";

        /**
         *
         * ** Rule: 26 * Entail: 1604
         *
         */
        try {
//			Mappings map = exec.query(entail);
//			System.out.println(map.size());

            g.index();
            g.setDebug(true);


            //g.addEngine(ent);
//			g.addEngine(re);
//			g.addEngine(re2);

            g.process();
            exec.query(loadrule);

            Mappings map = exec.query(rule);
            System.out.println("** Rule: " + map.size());

            map = exec.query(entail);
            System.out.println("** Entail: " + map.size());

            System.out.println("** Graph: " + g.size());


            map = exec.query(drop);

            map = exec.query(rule);
            System.out.println("** Rule: " + map.size());

            exec.query(loadrule);

            map = exec.query(rule);
            System.out.println("** Rule: " + map.size());


        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testLoop() {

        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        //g.init();

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "load <file://" + data + "comma/model.rdf> ";

        String update =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "delete {?x c:FirstName ?n}"
                + "where  {?x c:FirstName ?n}";

        update =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "rule construct {?x c:hasFather []} "
                + "where  {?x c:FirstName ?n} ";

        String query = "select * where {?x ?p ?y}";

        try {
            exec.query(init);

            //exec.setDebug(true);
            System.out.println("** U: 1");

            boolean go = true;
            Mappings map = exec.query(update);

            System.out.println(map);

            System.out.println("** Insert: " + map.nbInsert());
            System.out.println("** Delete: " + map.nbDelete());

            System.out.println("** Insert: " + map.getInsert());
            System.out.println("** Delete: " + map.getDelete());

            System.out.println("** Total: " + map.nbUpdate());

            if (map.nbUpdate() == 0) {
                go = false;
            }

            map = exec.query(query);

            //System.out.println(g.display());

//			System.out.println(g.getIndex());
//
//			for (Entity ent : map.getInsert()){
//				System.out.println(ent);
//				System.out.println(g.delete(ent));
//			}
//			
//			System.out.println(g.getIndex());


            //while (exec.query(update).nbUpdate()>0){}



        } catch (EngineException e) {
            e.printStackTrace();
        }


    }

    public void testGL() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        //g.init();

        String init =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "load <file://" + data + "comma/comma.rdfs> "
                + "pragma {"
                + //"[" +
                //	"kg:when   [a kg:Insert ; kg:graph kg:entailment ; kg:triple(?x rdf:type c:Person)] ;"+
                //	"kg:action [a kg:Log    ; kg:file '/tmp' ] ;" +
                //	"kg:action [a kg:Broadcast ; kg:target(<server1> <server2>) ]" +
                //"]" +
                ""
                + "["
                + "kg:when [a kg:Query  ; kg:load [a kg:RuleBase]] ;"
                + "kg:then [a kg:Action ; kg:run  [a kg:RuleBase]]"
                + "]"
                + //"[kg:when  [a kg:Insert ; "+
                //  "a kg:greaterThan ; kg:args(kg:size 10000)] ;"+
                //  "kg:action [a kg:Reject]]"+
                "}";


        String ins =
                "insert {?y ?p ?x} where {?x ?p ?y}"
                + "pragma {"
                + "graph kg:listen {"
                + "[ kg:size 3000;"
                + "kg:insert true]"
                + "}"
                + "}";

        String query = "prefix ext: <function://junit.TestUnit>"
                + "select (ext:size(kg:graph()) as ?size) where {"
                + "optional {"
                + "[ex:relation( ex:subject ex:object ex:arg)]"
                + "}"
                + "}";

        try {
//			exec.addPragma(Pragma.LISTEN, Pragma.SIZE, 1000);
//			exec.addPragma(Pragma.LISTEN, Pragma.INSERT, true);

            exec.query(init);
            Mappings map = exec.query(query);

            //g.addListener(GraphListenerImpl.create(5000));

            //exec.query(ins);

            System.out.println(g.size());
            System.out.println(map);
            //System.out.println(g.getListeners().size());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public IDatatype size(Object o) {
        Graph g = (Graph) o;
        return DatatypeMap.newInstance(g.size());
    }

    public void testMatch() {

        Graph g = Graph.create();
        //g.addListener(GraphListenerImpl.create(10000));
        QueryProcess exec = QueryProcess.create(g);



        Load ld = Load.create(g);

        ld.load(data + "comma/comma.rdfs");
        ld.load(data + "comma/data");
        ld.load(data + "comma/data2");

        String cons =
                "construct {?x ?p ?z}"
                + "where {"
                + "?p a rdf:Property "
                + "{select * (bnode(?y) as ?z) where {"
                + "?x ?p ?y "
                + "} limit 1}"
                + "}";

        try {
            Mappings map = exec.query(cons);
            Graph res = exec.getGraph(map);
            System.out.println("** Query: " + res.size());
            System.out.println("** Target: " + g.size());
            System.out.println(res.display());

            Date d1 = new Date();
            QueryGraph qg = QueryGraph.create(res);

            map = exec.query(qg);
            Date d2 = new Date();
            //System.out.println(map.toString(true));

            System.out.println("** Result: " + map.size());
            System.out.println("** Time: " + (d2.getTime() - d1.getTime()) / 1000.0);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testList2() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "@prefix : <http://test.fr/> "
                + "insert data {"
                + ":xxx rdf:value (3 2 3 3)"
                + "}";

        String update =
                "prefix : <http://test.fr/> "
                + "delete {"
                + "?e rdf:rest ?n ?n rdf:first ?val ?n rdf:rest ?rst"
                + "?x rdf:value ?nn ?nn rdf:first ?val . ?nn rdf:rest ?rr"
                + "}"
                + "insert {"
                + "?e rdf:rest ?rst"
                + "?x rdf:value ?rr "
                + "}"
                + "where {"
                + "{?x rdf:value ?l . ?l rdf:rest* ?e . ?e rdf:rest ?n"
                + " ?n rdf:first ?val . ?n rdf:rest ?rst }"
                + "union"
                + "{?x rdf:value ?nn ?nn rdf:first ?val . ?nn rdf:rest ?rr }"
                + "values ?val {3}"
                + "}";

        String query =
                "prefix : <http://test.fr/> "
                + "select * where {?x ?p ?y}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            exec.query(update);
            map = exec.query(query);
            System.out.println(map);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testNS() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "@prefix : <http://test.fr/> "
                + "insert data {:a :p :b}";

        String query =
                "prefix : <http://example/> "
                + "construct {?x ?p ?y} where {?x ?p ?y}";

        Load ld = Load.create(g);
        ld.load(root + "test/crdt.ttl");

        try {
            //exec.query(init);
            Mappings map = exec.query(query);
            Graph gg = exec.getGraph(map);
            RDFFormat f = RDFFormat.create(map);
            f.write(root + "test/tmp.rdf");
            System.out.println(f);

            Graph g2 = Graph.create();
            Load l2 = Load.create(g2);
            l2.load(root + "test/tmp.rdf");
            System.out.println(g2.display());
            System.out.println(g2.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    public void test61() {
        DatatypeMap.setLiteralAsString(false);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "prefix p: <http://fr.dbpedia.org/property/>"
                + "select  * where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y ."
                + "?y rdfs:label ?l}"
                + "}"
                + "pragma {kg:path kg:expand 12}";




        try {
            Mappings map = exec.query(query);
            ResultFormat f = ResultFormat.create(map);
            System.out.println(map);
            System.out.println(f);
            assertEquals("Result", 12, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testExpand() {

        String query =
                "prefix ex: <http://example.org/>"
                + "select  * where {"
                + //"?x rdf:type/rdfs:subClassOf* ?c" +
                //"?x ex:p0 / (!(ex:p1 | ex:p2))*  ?y" +
                "?x rdf:rest*/rdf:first ?y"
                + //"?x ^rdf:first/(^(! rdf:gogo))+ ?y" +
                "}";
        //"pragma {kg:path kg:expand 3}";

        String init =
                "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:a ex:p0 ex:b "
                + "ex:b ex:p0 ex:c "
                + "ex:c ex:p1 ex:d "
                + ""
                + "ex:list rdf:value (1 2 3) "
                + ""
                + "ex:Human rdfs:subClassOf ex:Animal "
                + "ex:Animal rdfs:subClassOf ex:Living "
                + "ex:John a ex:Human"
                + "}";



        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.addVisitor(ExpandPath.create(3));

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            exec.getGraph(map);

//			ASTQuery ast = exec.getAST(map);			
//			ExpandPath rew = ExpandPath.create(3);
//			System.out.println(rew.rewrite(ast.getBody()));

            System.out.println(exec.getAST(map));
            System.out.println(map.getQuery());
            System.out.println(map);
            System.out.println(ResultFormat.create(map));
            System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testSyntax() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<a> a rdfs:Resource}";

        String cons = "construct {?x rdf:value ?y} where {?x a rdfs:Resource} values ?y {10}";

        String query = "select * where {?x ?p ?y}";

        Load ld = Load.create(g);

        ld.load(root + "test/deco.rl");

        try {
            exec.query(init);

            RuleEngine re = ld.getRuleEngine();
            re.process();

            Mappings map = exec.query(query);
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Total: 0.6 Without XML parse: 0.4
     *
     * 0.553
     */
    public void testService() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = QueryLoad.create().read(root + "alu/nico.rq");

        try {
            int size = 1;
            Date d1 = new Date();
            for (int i = 0; i < size; i++) {
                //exec.setDebug(true);
                Mappings map = exec.query(q);
                if (i == 0) {
                    System.out.println(map);
                }
            }
            Date d2 = new Date();
            System.out.println(((d2.getTime() - d1.getTime()) / 1000.0) / size);
            //System.out.println(map.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testSAP() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.load(root + "sap/q1-light.ttl");
        ld.load(root + "sap/q2-light.ttl");
        ld.load(root + "sap/sqlOnto.ttl");

        String q = QueryLoad.create().read(root + "sap/q1.rq");

        try {
            Date d1 = new Date();
            Mappings map = exec.query(q);
            Date d2 = new Date();
            //System.out.println(map);
            System.out.println(map.size());
            System.out.println((d2.getTime() - d1.getTime()) / 1000.0);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testCountPath() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = ""
                + "prefix ex: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "ex:a ex:p ex:b ex:b ex:p ex:c "
                + "ex:a ex:p ex:d ex:d ex:p ex:c "
                + ""
                + "} ";

        String query = ""
                + "prefix ex: <http://www.inria.fr/acacia/comma#> "
                + "select * where {"
                + "ex:a ex:p+ ?x"
                + "}"
                + "pragma {kg:path kg:count false}";

        try {
            //exec.setCountPath(true);
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());




        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void testBUG() {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Load ld = Load.create(g);
        ld.load(root + "test/q1.ttl");

        QueryLoad ql = QueryLoad.create();
        String q = ql.read(root + "test/q1.rq");
        System.out.println(q);

        try {
            Mappings map = exec.query(q);
            System.out.println(exec.getGraph().display());

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testDelete() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph, true);

        String init =
                "prefix ex: <http://example.org/> "
                + ""
                + "insert data {"
                + "ex:John a ex:Man "
                + "ex:Jill a ex:Woman "
                + "ex:John a ex:Human "
                + "} ";


        String update =
                "prefix ex: <http://example.org/> "
                + "delete where {"
                + "ex:John ?p ?y"
                + "}"
                + ""
                + "";

        String query =
                "prefix ex: <http://example.org/> "
                + "select *  where  {"
                + "{?x rdf:type* ?c}"
                + "}";


        try {

            exec.query(init);
            exec.query(update);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            System.out.println(graph);
            System.out.println(graph.getResource("http://example.org/John"));

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testType() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph, true);

        String init =
                "prefix ex: <http://example.org/> "
                + ""
                + "insert data {"
                + "ex:Human rdfs:subClassOf ex:Animal "
                + "ex:Man   rdfs:subClassOf ex:Human "
                + "ex:Woman rdfs:subClassOf ex:Human "
                + "graph ex:g1 { "
                + "ex:John a ex:Man "
                + "ex:Jill a ex:Woman "
                + "}"
                + "ex:John a ex:Human "
                + "ex:John a ex:Man "
                + "} ";

        String query =
                "prefix ex: <http://example.org/> "
                + "select *  where  {"
                + //"graph ?g " +
                "{?x a ex:Human}"
                + "}";


        try {

            exec.query(init);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testOption() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :p :d, :a "
                + ":c :p :d "
                + ""
                + ":e :p :b, :c ."
                + ""
                + "} ";

        String query =
                "prefix : <http://example.org/> "
                + "select *  where  {"
                + "?x ((:p/:p) ?)  :d "
                + "}";


        try {

            exec.query(init);
            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            System.out.println(map.getQuery().getAST());

            //assertEquals("Results", 9, map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Create a Query graph from an RDF Graph Execute the query Use case: find
     * similar Graphs (cf Corentin)
     */
    public void test52() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix : <http://example.org/> "
                + "insert data {"
                + "graph <q1> {"
                + "[a :Query ; :query <q1> ; "
                + ":where [:subject [] ; :property [] ; :object 10 ]]"
                + "}"
                + "graph <q2> {"
                + "[a :Query ; :query <q2> ; "
                + ":where [:subject [] ; :property [] ; :object 20 ]]"
                + "}"
                + ""
                + "} ";

        String onto =
                "prefix : <http://example.org/> "
                + "insert data {"
                + ":Query rdfs:subClassOf :Action"
                + "}";

        // extract a subgraph 
        // replace literal with bnode
        String cons =
                "prefix : <http://example.org/> "
                + "construct { graph ?g { ?x ?p ?o }}"
                + "where {"
                + "select * "
                + "(if (?p = :query || isLiteral(?y), bnode(), ?y) as ?o) "
                + "where {"
                + "graph ?g { [:object ?v] . filter(?v >= 10) ?x ?p ?y }"
                + "}"
                + "}";

        // rewrite subClass as superClass
        String rew =
                "prefix : <http://example.org/> "
                + "delete {graph ?g {?x a ?c}}"
                + "insert {graph ?g {?x a ?c2}}"
                + "where  {"
                + "graph ?g {?x a ?c} "
                + "?c rdfs:subClassOf ?c2"
                + "}";

        Graph go = Graph.create();


        try {
            // Load ontology
            QueryProcess.create(go).query(onto);

            // create a graph
            exec.query(init);
            exec.query(onto);

            // create a copy where triple objects (values) are Blank Nodes (aka Variables)
            Mappings map = exec.query(cons);
//			System.out.println(map);
//			System.out.println(map.size());

            Graph g2 = exec.getGraph(map);
            //System.out.println(TripleFormat.create(g2, true));

            List<Graph> list = g2.split();

            for (Graph g : list) {
                System.out.println(TripleFormat.create(g, true));

                QueryProcess rewrite = QueryProcess.create(g);
                rewrite.add(go);
                rewrite.query(rew);


                System.out.println(TripleFormat.create(g, true));

                map = exec.query(g);
                System.out.println(map.toString(true));
                System.out.println(map.size());
            }

//			QueryProcess rewrite = QueryProcess.create(g2);
//			rewrite.query(rew);
//			System.out.println(TripleFormat.create(g2));


//			QueryGraph qg = QueryGraph.create(g2);
//			QGVisitor vis = new QGVisitor();
            //qg.setVisitor(vis);
            //qg.setConstruct(true);
            //map = exec.query(g2);									

            //Graph res = exec.getGraph(map);
            //assertEquals("Results", 2, res.size());

            //System.out.println(TripleFormat.create(res));
//			
//			System.out.println(map.toString(true));
//			System.out.println(map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void testBNode() {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = ""
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jim>, <James>; foaf:prout <jjj>  "
                + "<Jack> foaf:knows <Jim> ; foaf:prout <hhh>"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select ?x "
                + "(count(?y) as ?c1) (count(distinct ?z) as ?c2)"
                + "(bnode(?c1) as ?b1) (bnode(?c2) as ?b2)"
                + "where {"
                + "?x foaf:knows ?y ; foaf:prout ?z "
                + "} group by ?x";

        try {
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void test60() {
        DatatypeMap.setLiteralAsString(false);
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init, query;

        init = ""
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<John> foaf:knows <Jack> ; foaf:knows 'John' "
                + "<Jack> foaf:knows <Jim> ; foaf:knows 'Jack'"
                + "<John> foaf:knows <James>"
                + "<James> foaf:knows <Jim> ; foaf:knows 'James' "
                + "<Jim> foaf:knows <Jules>  "
                + "<Jim> foaf:knows 'Jim' "
                + "<James> foaf:knows <John>  "
                + "}";

        query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:knows+ ?y "
                + "filter(isURI(?x))"
                + "}"
                + "order by ?x ?y";


//		init = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"insert data {" +
//				"<John> foaf:knows (<Jack> <Jack> <Jim> <Jack>) " +
//				"" +
//				"}";				
//		
//		query = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"select * where {" +
//			"?x foaf:knows/rdf:rest+/rdf:first ?y " +
//			"filter(isURI(?y))" +
//			"}";
//		

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());



        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * select where { ?x foaf:FamilyName 'Corby' ?x foaf:isMemberOf ?org ?x
     * foaf:FirstName ?name filter (?name = 'toto' || ?org ~ 'inria' )}
     */
    Graph init2() {
        String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

        Graph graph = Graph.create(true);
        graph.set(Entailment.DATATYPE_INFERENCE, true);

        Load load = Load.create(graph);
        graph.setOptimize(true);
        System.out.println("load");

        load.load(data + "kgraph/rdf.rdf", RDF.RDF);
        load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
        load.load(data + "comma/comma.rdfs");
        //load.load(data + "comma/commatest.rdfs");
        load.load(data + "comma/model.rdf");
        load.load(data + "comma/testrdf.rdf");
        load.load(data + "comma/data");
        load.load(data + "comma/data2");

        try {
            load.loadWE(root + "rule/rdfs.rul");
            load.loadWE(root + "rule/owl.rul");

            //load.loadWE(root + "rule/tmp.rul");

        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("init");
        long t1 = new Date().getTime();
        graph.init();
        long t2 = new Date().getTime();
        System.out.println((t2 - t1) / 1000.0 + "s");

        System.out.println("rule");

        t1 = new Date().getTime();
//		RuleEngine re = load.getRuleEngine();
//		//int nb = re.process();
//		 t2 = new Date().getTime();
//		System.out.println("** Time: " + (t2-t1) / 1000.0 + "s");
//		System.out.println("** Size: " +graph.size());
        //System.out.println("** Rule Entailment: " + nb);
//		
        return graph;
    }

    public void test7() throws ParserConfigurationException, SAXException, IOException {
        Graph g1 = Graph.create(true);
//		Load ld = Load.create(g1);
//		Date d1 = new Date();
//		ld.load(data + "commattl/copy.ttl");
//		Date d2 = new Date();
//		System.out.println("Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
//		g1.init();
//		System.out.println(g1.size());

        String query =
                "prefix foaf: <http://www.inria.fr/acacia/comma#>"
                + "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "prefix p: <http://fr.dbpedia.org/property/>"
                + "insert {<http://fr.dbpedia.org/resource/Auguste> p:succ ?y}  where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y "
                + "}" + ""
                + "}"
                + "pragma {"
                + "kg:service kg:timeout 1000"
                + "kg:path kg:expand 5 "
                + "}";


        String del = "clear all";

        QueryProcess exec = QueryProcess.create(g1);
        //exec.addPragma(Pragma.SERVICE, Pragma.TIMEOUT, 10);
        //exec.addPragma(Pragma.PATH, Pragma.EXPAND, 5);

        try {

            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            TripleFormat tf = TripleFormat.create(g1, true);
            tf.with(Entailment.DEFAULT);
            System.out.println(tf);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void test8() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<a> foaf:knows <e> "
                + "<a> foaf:knows <c> "
                + "<a> foaf:knows <b> <b> foaf:knows <c>}";

        String query =
                //"construct {?x ?p ?y}" +
                "select * (min(?l) as ?min)"
                + "where {"
                + "{select * (pathLength($path) as ?l)  where {"
                + "?a short(foaf:knows+) :: $path ?b } }"
                + "values ?a {<a>}"
                + "graph $path {?x ?p ?y}"
                + "} "
                + //"having (?l = min(?l))" +
                "bindings ?a ?b {"
                + "(<a> <c>)"
                + "}"
                + "pragma {kg:query kg:check true}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            ASTQuery ast = exec.getAST(map);
            System.out.println(ast);

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public void ttest2() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "graph <g1> {<John> foaf:knows <Jim>} "
                + "graph <g2> {<John> foaf:knows <James> }"
                + "}";

        String del = "delete data {"
                + "<John> foaf:knows <James> "
                + "}";


        String query =
                "select * "
                + "from kg:entailment "
                + "where {"
                + "?x ?p ?y "
                + //"?p rdf:type rdf:Property"+
                "}";


        query =
                "select * "
                + "where {"
                + "?x foaf:knows* ?y"
                + "}";


        try {
            exec.query(init);
            exec.query(del);
            Mappings map = exec.query(query);

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

//			RDFFormat ff = RDFFormat.create(g);
//			System.out.println(ff);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * Random graph creation
     */
    public void testUpdate() {
        int nbnode = 100000;
        int nbedge = nbnode;


        Graph g = Graph.create(true);

        Node pred = g.addProperty("foaf:knows");

        String init = ""
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person "
                + "foaf:knows rdfs:range  foaf:Person"
                + "}";

        Date d1 = new Date();

        for (int j = 0; j < 10; j++) {
            System.out.println(j);
            for (int i = 0; i < nbedge; i++) {
                long sd = Math.round(Math.random() * nbnode);
                long od = Math.round(Math.random() * nbnode);

                Node sub = g.addResource(Long.toString(sd));
                Node obj = g.addResource(Long.toString(od));

                g.addEdge(sub, pred, obj);
            }
        }

        System.out.println("Size: " + g.size());

        Date d2 = new Date();
        System.out.println("Create Time : " + (d2.getTime() - d1.getTime()) / 1000.0);

        g.init();

        Date d3 = new Date();


        System.out.println("Index Time : " + (d3.getTime() - d2.getTime()) / 1000.0);


        String query = "select * where {"
                + "?x foaf:knows ?y "
                + "?z foaf:knows ?x"
                + "?y foaf:knows ?z "
                + "}"
                + "limit 5";

        String update =
                "delete {"
                + "?x foaf:knows ?y "
                + "?y foaf:knows ?z "
                + "?z foaf:knows ?x"
                + "}"
                + "where {"
                + "{select * where {"
                + "?x foaf:knows ?y "
                + "?z foaf:knows ?x"
                + "?y foaf:knows ?z "
                + "}"
                + "limit 5}"
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            d2 = new Date();
            g.init();
            d3 = new Date();
            System.out.println("Infer Time : " + (d3.getTime() - d2.getTime()) / 1000.0);


            exec.query(query);
            Date d4 = new Date();
            System.out.println("Query Time : " + (d4.getTime() - d3.getTime()) / 1000.0);


            Mappings map = exec.query(update);
            Date d5 = new Date();
            System.out.println("Update Time : " + (d5.getTime() - d4.getTime()) / 1000.0);

            g.init();

            Date d6 = new Date();
            System.out.println("Infer Time : " + (d6.getTime() - d5.getTime()) / 1000.0);

            System.out.println(g.size());
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Size: 1000000 Create Time : 3.102 Index Time : 4.844
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:knows
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:Person Infer Time : 7.345
     * fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372)
     * Undefined prefix: foaf:knows
     *
     * Query Time : 1.468 Query Time : 4.767
     *
     * Infer Time : 5.18 1099947
     */
    public void test451() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix foaf: <http://test/> "
                + "insert data {"
                + "tuple(foaf:knows <John>  <James> 2)"
                + "tuple(foaf:knows <John>  <James> 1)"
                + "tuple(foaf:knows <John>  <James> 2 3)"
                + "tuple(foaf:knows <John>  <Jim>   1)"
                + "tuple(foaf:knows <John>  <James> )"
                + "tuple(foaf:knows <Jack>  <James> )"
                + "tuple(foaf:knows <Jim>  <James> )"
                + "} ;";

        String query =
                "prefix foaf: <http://test/> "
                + "prefix ext: <function://junit.TestUnit>"
                + "select * where {"
                + //"graph ?g " +
                "{ "
                + "tuple(foaf:knows ?x ?n ?v) "
                + "?x foaf:knows::?p ?y  "
                + //					  "filter(?x != ?y)" +
                "}"
                + "}";



        try {
            exec.query(init);
            graph.init();
            System.out.println("** Size: " + graph.size());

            ASTQuery ast = ASTQuery.create(query);
            ParserSparql1.create(ast).parse();

            System.out.println(ast);

            Mappings map = exec.query(query);

            System.out.println(map);

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String dateToGMTString(Date dateToBeFormatted) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
        return dateFormat.format(dateToBeFormatted);
    }

    public static Date GMTStringToDate(String gmtDateString) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
            return dateFormat.parse(gmtDateString);
        } catch (ParseException e) {
            // manage exception
            e.printStackTrace();
        }
        return null;
    }

    public void test50() {

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String d1 = dateToGMTString(new Date());
        for (int i = 0; i < 10000000; i++) {
        }
        String d2 = dateToGMTString(new Date());

        System.out.println(d1);
        System.out.println(d2);

        try {
            IDatatype dt1 = new CoreseDate(d1);
            IDatatype dt2 = new CoreseDate(d2);
        } catch (CoreseDatatypeException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://test/> "
                + "insert data {"
                + "<John> foaf:date '" + d1 + "'^^xsd:dateTime "
                + "<Jim> foaf:date  '" + d2 + "'^^xsd:dateTime "
                + "} ;";

        String query =
                "prefix foaf: <http://test/> "
                + "select * where {"
                + "?x foaf:date ?d"
                + "}"
                + "order by desc(?d)";


        try {
            exec.query(init);
            graph.init();
            System.out.println("** Size: " + graph.size());



            Mappings map = exec.query(query);

            System.out.println(map);

            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Object fun(Object obj) {
        return DatatypeMap.TRUE;
    }

    public void testDate() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.load(root + "isicil/date.rdf");

        QueryLoad ql = QueryLoad.create();
        String query = ql.read(root + "isicil/date.rq");

        IEngine engine = new EngineFactory().newInstance();
        try {
            engine.load(root + "isicil/date.rdf");
        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            IResults res = engine.query(query);
            System.out.println(res);

        } catch (EngineException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }




        String query2 = "prefix dc: <http://purl.org/dc/elements/1.1/>"
                + "select * where {	"
                + "?msg dc:created ?date "
                + "}"
                + "order by (?date)";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);

            ResultFormat f = ResultFormat.create(map);

            //
            //System.out.println(f);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    public void test65() {
        Graph g = Graph.create();

        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "test/iso.ttl");
            ld.loadWE(root + "test/iso.rdf");

            ld.loadWE(root + "test/utf.ttl");
            ld.loadWE(root + "test/utf.rdf");

            ld.loadWE(root + "test/iso.rul");

        } catch (LoadException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String query = "select * where {"
                + "?x ?p ?y . ?z ?q ?y filter(?x != ?z)"
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

            RDFFormat ff = RDFFormat.create(g);
            System.out.println(ff);

        } catch (EngineException e) {
            e.printStackTrace();
        }

        RuleEngine re = ld.getRuleEngine();
        Rule r = re.getRules().get(0);
        System.out.println(r.getQuery().getAST());

        System.out.println(System.getProperty("file.encoding"));

    }

    public void test70() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            System.out.println("** Load: alu/dbpedia_3.7.rdfs");
            ld.loadWE(root + "alu/dbpedia_3.7.rdfs");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String query = "select ?sim (kg:similarity(owl:Thing,owl:Thing) as ?sim) where {}";



        String query2 = "select ?depth (kg:depth(<http://schema.org/CreativeWork>) as ?depth) where {}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            System.out.println("** Query 1");
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println("** Query 2");
            map = exec.query(query2);

            System.out.println(map);
            System.out.println(map.size());
            assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testJoin() {
        Graph g = Graph.create(); //init(); //Graph.create();
        Load ld = Load.create(g);

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data   {"
                + "graph <g1> {"
                + "<John> foaf:name 'John' "
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>"
                + "<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <Jack>"
                + "<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>"
                + "<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>"
                + "}"
                + "graph <g1> {"
                + "<Jim> foaf:knows <James>"
                + "<Jim> foaf:name 'Jim' "
                + "}"
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug * where {"
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "<http://fr.dbpedia.org/resource/Auguste> <http://www.w3.org/2000/01/rdf-schema#label> ?n"
                + "?x rdfs:label ?n "
                + "} limit 20"
                + "}}"
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "?x rdfs:label ?n "
                + "}"
                + "}}"
                + "}"
                + "";


        query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select debug *  where {"
                + "?x ?p ?y "
                + "service <http://fr.dbpedia.org/sparql> {{"
                + "select * where {"
                + "?x ?p ?y "
                + "}"
                + "}}"
                + "}"
                + "";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.setOptimize(true);
            exec.query(init);

            Mappings map = exec.query(query);
            System.out.println(map);

            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }



    }

    public void testRelax() {
        Graph g = Graph.create(); //init();

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "<John> foaf:type c:Researcher "
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:type c:Engineer "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:type c:Fireman "
                + "<e> foaf:type c:Event "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select   more * (kg:similarity() as ?s) "
                + "(uuid() as ?u)"
                + "(struuid() as ?su)"
                + "where {"
                + "?x foaf:type c:Engineer "
                + "?x foaf:knows ?y "
                + "?y foaf:type c:Engineer"
                + "}"
                + "order by desc(?s) "
                + "pragma {kg:kgram kg:relax  rdf:type, foaf:type}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }




    }

    public void testValues4() {
        Graph g = Graph.create();
        Load ld = Load.create(g);



        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + //"?x foaf:knows ?y " +
                "values ?x {<b> <c>}"
                + "}"
                + "values ?y {<b> <e>}"
                + "";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.getQuery());
            System.out.println(exec.getAST(map));

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testNode() {
        Graph g = Graph.create();


        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<?a> foaf:knows <?b> "
                + "<?b> foaf:knows <?c> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "<?a> foaf:knows ?a ?a foaf:knows ?b "
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testBlank() {
        Graph g = Graph.create();
        Load ld = Load.create(g);


        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "graph <g1> {_:b foaf:knows <b>} "
                + "graph <g2> {_:b foaf:knows <b>} "
                + "graph <g2> {_:b1 foaf:knows <b>} "
                + "}";

        String query0 =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert {graph <g2> {?x foaf:knows ?y} }"
                + " where {"
                + "graph <g1> {?x foaf:knows ?y} "
                + //"values ?x {<a>}" +
                "}";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "graph ?g {?x foaf:knows ?y} "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings m = exec.query(query0);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testTTL() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "commattl/comma.ttl");
            ld.loadWE(data + "commattl/model.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        System.out.println(g);

        EngineFactory fac = new EngineFactory();
        IEngine engine = fac.newInstance();
        GraphEngine eng = (GraphEngine) engine;
        Graph gg = eng.getGraph();
        gg.setEntailment(false);


    }

    public void testAlban() {
        Graph g = Graph.create();
        QueryLoad ld = QueryLoad.create();

        String q = ld.read(ndata + "alban/query/q1.rq");

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            System.out.println(q);
            Mappings map = exec.query(q);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testBind() {
        Graph g = Graph.create();
        Load ld = Load.create(g);

        String init = " insert data {"
                + "<John> foaf:age 2"
                + "<John> foaf:age 3"
                + "<John> foaf:age 4"
                + "}";


        String query2 =
                "select more * where {"
                + //"filter(?x = 2)" +
                "bind(3 as ?x)"
                + ""
                + "}"
                + "values ?x {1 2 3}";

        String query =
                "select * where {"
                + "select more  *  (5 as ?x) (3 as ?x)  where {"
                + "?a foaf:age ?x"
                + //"{select (3 as ?x) {}}" +
                ""
                + "}"
                + "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);

            Mappings map = exec.query(query);

            System.out.println(map);
            System.out.println(map.size());
            //System.out.println(map.get(0).getQueryNodes().length);
            System.out.println(exec.getAST(map).getSelectAllVar());
            System.out.println(map.getQuery().getSelectFun());
            System.out.println(exec.getAST(map));

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void test30() {

        NSManager nsm = NSManager.create();
        nsm.definePrefix("foaf", "http://foaf.org/");


        ASTQuery ast = ASTQuery.create();
        ast.setNSM(nsm);

        Triple t1 = Triple.create(Variable.create("?x"), ast.createQName("foaf:knows"), Variable.create("?y"));

        ast.setBody(BasicGraphPattern.create(t1));

        ast.setDescribe(Variable.create("?x"));

        String init =
                "prefix foaf: <http://foaf.org/>"
                + "insert data {<John> foaf:knows <Jim>"
                + "<John> owl:sameAs <Johnny>}";

        String query =
                "prefix foaf: <http://foaf.org/>"
                + "construct {?y foaf:knows ?x}"
                + "where {?x foaf:knows ?y}";


        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(ast);
            RDFFormat f = RDFFormat.create(map);

            System.out.println(ast);
            System.out.println(map);
            System.out.println(f);
            assertEquals("Result", map.size(), 2);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            assertEquals("Result", true, e);
        }
    }

    public void testAgg() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();
        String qq = ql.read(ndata + "test/agg.rq");



        String init =
                "PREFIX ex: <http://example.org/meals#>  "
                + "insert data {"
                + "[ ex:mealPrice 1 ; "
                + " ex:mealTip 2 ;"
                + "ex:mealPrice 3 ; "
                + "ex:mealTip 4 ;"
                + " ] "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(qq);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testJulien() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        String qq = ql.read(ndata + "test/dbpedia.rq");

        try {
            ld.loadWE(root + "test/iso.ttl");
            //ld.loadWE(data + "commattl/comma.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        //http://fr.dbpedia.org/property/texte	

        String query =
                "prefix dbpedia-owl: <http://dbpedia.org/ontology/> "
                + "prefix dbfr: <http://fr.dbpedia.org/> "
                + "prefix dbpedia-prop: <http://dbpedia.org/property/> "
                + "prefix dbp: <http://dbpedia.org/property/> "
                + "select debug  distinct  ?p1 "
                + "where { "
                + "service <http://dbpedia.org/sparql>    {<http://dbpedia.org/resource/Paris> ?r2 ?p1 "
                + "filter(?r2 != dbp:texte)"
                + "filter(isLiteral(?p1))"
                + // "filter(?p1 = 20)" +
                "}  . "
                + "filter( isNumeric(?p1))"
                + //               "filter( lang(?p1) = 'fr')" +
                //               
                " service <http://fr.dbpedia.org/sparql> {<http://fr.dbpedia.org/resource/Paris> ?r1 ?p1 . }  "
                + "}"
                + "order by ?p1 "
                + "pragma {"
                + "kg:service kg:slice 3 "
                + "}";;


        QueryProcess exec = QueryProcess.create(g);
        //exec.setSlice(30);

        try {
            Mappings map = exec.query(query);
            System.out.println("** Result: " + map.size());
            System.out.println(map.size());
            System.out.println(map);

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            //System.out.println(f);			

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void test1() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "comma/comma.rdfs");
            ld.loadWE(data + "comma/data");
            ld.loadWE(data + "comma/data2");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TripleFormat t = TripleFormat.create(g, true);
        try {
            t.write(data + "commattl/global.ttl");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void test2() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "commattl/global.ttl");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String query = "select * where {?x <p> 'ab\\ncd'}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(exec.getAST(map));

        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("** Size: " + g.size());
    }

    public void test3() {

        Graph g = Graph.create();
        Load ld = Load.create(g);

        try {
            ld.loadWE(data + "comma/data2/f125.rdf");
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String query =
                "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {?x c:Title ?t}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testNicolas() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();



        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        //  9.095

        // 11.81


        String prop = "prefix db: <http://fr.dbpedia.org/property/>"
                + "select  ?p where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select distinct ?p where {"
                + "?p rdf:type rdf:Property "
                + "filter(?p != db:isbn)"
                + "filter (! regex(str(?p), 'owl'))"
                + "filter (! regex(str(?p), 'wiki'))"
                + "}  limit 100"
                + "}"
                + "}  order by ?p ";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + //"select * " +
                "insert {?x <%s> ?p1 }"
                + "where {"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "select * where {?x <%s> ?p1 } limit 10"
                + "}}";

        /**
         * 219247 22.83 29 17.271
         *
         * 353422 49 45.816 49 25.154
         *
         * 49 37.099
         *
         *
         * 498615 99 60.255 99 44.21
         *
         *
         */
        QueryProcess exec = QueryProcess.create(g);

        try {
            //exec.query(init);
            Date d1 = new Date();

            int slice = 1;

            Mappings res = exec.query(prop);
            System.out.println(res);


            for (int i = 0; i < res.size(); i++) {
                Node np = res.get(i).getNode("?p");
                String name = np.getLabel();
                System.out.println(name);
                Formatter f = new Formatter();
                String qq = f.format(query, name, name).toString();
                //String qq = f.format(query, name).toString();
                //System.out.println(qq);
                Mappings map = exec.query(qq);
                //System.out.println(map);
                System.out.println(g.size());
                Date d2 = new Date();
                System.out.println(name);
                System.out.println(i + " " + (d2.getTime() - d1.getTime()) / 1000.0);
            }

            //System.out.println(res);


            /**
             * 9 12.824 9 12.073 9 12.288
             *
             */
// 28.5 pour 200 000 (vs 15.7 select *)
// 		300 000 insert : 51.007    select * : 23
        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testIGN() {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        try {
            ld.loadWE(root + "ign/ontology/ign.owl");
            //ld.loadWE(ndata + "test/onto.ttl");


        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        // query generate a construct where rule for property Chain
        String init =
                "select debug  "
                + "(concat('construct {?x ',  kg:qname(?q), ' ?y} where {?x ', ?r , ' ?y}') as ?req)"
                + "(group_concat(kg:qname(?p) ; separator='/') as ?r)"
                + "where {"
                + "?q owl:propertyChainAxiom/rdf:rest*/rdf:first ?p "
                + "}"
                + "group by ?q";


        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix ign: <http://www.semanticweb.org/ontologies/2012/5/Ontology1339508605479.owl#>"
                + "select * where {"
                + "?x ign:aLaTeinteDe ?t "
                + "}";

        String query2 =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix t: <http://ns.inria.fr/test/>"
                + "select * where {"
                + "graph ?g {?x a t:Male ;  a ?t } "
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {


//			OWLRule owl = OWLRule.create(g);
//			owl.process();

            Mappings map = exec.query(init);
            System.out.println(map);
            System.out.println(map.getQuery());
            System.out.println(map.size());
            System.out.println(exec.getAST(map));


        } catch (EngineException e) {
            e.printStackTrace();
        }
//		catch (LoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

    }
    // http://www.subshell.com/en/subshell/blog/article-Changing-from-m2eclipse-to-m2e-Eclipse-Indigo100.html
    // org.eclipse.m2e.launchconfig.classpathProvider"/>
    // org.maven.ide.eclipse.launchconfig.classpathProvider

    public void dbpedia() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        String query = ql.read(root + "test/dbpedia.rq");

        System.out.println(query);



        QueryProcess exec = QueryProcess.create(g);
        //exec.setDebug(true);

        try {
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        QueryLoad ql = QueryLoad.create();

        try {
            ld.loadWE(root + "test/iso.ttl");

        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String init =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<a> foaf:knows <b> "
                + "<c> foaf:knows <d> "
                + "}";

        String query =
                "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows ?y "
                + //"values ?x {<a>}" +
                "}";


        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            System.out.println(map);
            System.out.println(map.size());

            //assertEquals("Result", 4, map.size());

            ResultFormat f = ResultFormat.create(map);
            System.out.println(f);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }
}
