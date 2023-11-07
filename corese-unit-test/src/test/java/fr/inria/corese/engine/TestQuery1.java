package fr.inria.corese.engine;

import static fr.inria.corese.core.util.Property.Value.LOAD_IN_DEFAULT_GRAPH;
import static fr.inria.corese.core.util.Property.Value.SPARQL_COMPLIANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.result.XMLResult;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.print.JSONLDFormat;
import fr.inria.corese.core.print.NTriplesFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.print.XMLFormat;
import fr.inria.corese.core.producer.DataFilter;
import fr.inria.corese.core.producer.DataFilterFactory;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryGraph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.transform.Loader;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.StatListener;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.datatype.extension.CoresePointer;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.LDScriptException;
import fr.inria.corese.sparql.exceptions.UndefinedExpressionException;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;

public class TestQuery1 {
    // private static org.slf4j.Logger logger =
    // LoggerFactory.getLogger(TestQuery1.class);

    static String data = TestQuery1.class.getResource("/data/").getPath();
    // static String QUERY = TestQuery1.class.getResource("query/").getPath();
    // static String test = TestQuery1.class.getResource("test/").getPath();
    // static String text = TestQuery1.class.getResource("text/").getPath();

    private static final String FOAF = "http://xmlns.com/foaf/0.1/";
    // private static final String SPIN_PREF = "prefix sp: <" + NSManager.SPIN +
    // ">\n";
    private static final String FOAF_PREF = "prefix foaf: <http://xmlns.com/foaf/0.1/>\n";
    // private static final String SQL_PREF = "prefix sql:
    // <http://ns.inria.fr/ast/sql#>\n";
    static Graph graph;

    @BeforeClass
    static public void init() {
        // Query.STD_PLAN = Query.PLAN_RULE_BASED;
        // Property.set(GRAPH_NODE_AS_DATATYPE, true);
        Property.set(LOAD_IN_DEFAULT_GRAPH, true);
        // Property.set(INTERPRETER_TEST, true);
        QueryProcess.DISPLAY_QUERY = true;
        System.out.println("Property: " + Property.display());
        // Graph.DEFAULT_GRAPH_MODE = Graph.DEFAULT_GRAPH;

        QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
        // QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        graph = Graph.create(true);
        // graph.setOptimize(true);

        Load ld = Load.create(graph);
        try {
            init(graph, ld);
        } catch (LoadException ex) {
        }
        // Option.isOption = false;
        // QueryProcess.setJoin(true);
        // fr.inria.corese.compiler.parser.Transformer.ISBGP = !true;
        // QueryProcess.setPlanDefault(Query.QP_HEURISTICS_BASED);

        QueryProcess.testAlgebra(!true);

        // QueryProcess.setSolverVisitorName("fr.inria.corese.test.engine.MyVisitor");
        // QueryProcess.setVisitorable(true);
    }

    @AfterClass
    static public void finish() {
        // after2();
    }

    static void before() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "@public {"
                + "@error function us:error(?e, ?x , ?y) { "
                + "xt:print('****************** error') ; "
                + "xt:print(java:getAST(xt:query())) ;"
                + "xt:display( ?e, ?x, ?y) ; "
                + "error() "
                + "}"
                + "}";

        try {
            exec.compile(q);
        } catch (EngineException ex) {
        }

        QuerySolver.setVisitorable(true);
    }

    static void before2() {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        QuerySolver.setVisitorable(true);
        IDatatype map = DatatypeMap.map();
        map.set(DatatypeMap.newResource(NSManager.USER, "error"), DatatypeMap.newList());
        DatatypeMap.setPublicDatatypeValue(map);

        String q = "@public {"

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

                + "function us:map() { ds:getPublicDatatypeValue(true) }"

                + "function us:record(?e) {"
                + "if (java:isTerm(?e)) {"
                + "xt:set(us:map(), java:getLabel(?e), coalesce(1 + xt:get(us:map(), java:getLabel(?e)), 1)) ;"
                + "let (( | ?l) = ?e) {"
                + "for (?ee in ?e) {"
                + "us:record(?ee)"
                + "}"
                + "}"
                + "}"
                + "}"

                + "function us:recerror(?e, ?x, ?y) {"
                + "xt:add(xt:get(us:map(), us:error), xt:list(?e, ?x, ?y))"
                + "}"

                + "}"

        ;
        try {
            exec.compile(q);
        } catch (EngineException ex) {
            System.out.println(ex);
        }

    }

    static void after2() {
        System.out.println("After");
        int i = 0;
        for (IDatatype dt : DatatypeMap.getPublicDatatypeValue()) {
            System.out.println(i++ + " " + dt.getValueList().get(0) + " " + dt.getValueList().get(1));
        }
        i = 0;
        for (IDatatype dt : DatatypeMap.getPublicDatatypeValue()
                .get(DatatypeMap.newResource(NSManager.USER, "error"))) {
            System.out.println(i++ + " " + dt);
        }
    }

    static void init(Graph g, Load ld) throws LoadException {

        ld.parse(TestQuery1.class.getResourceAsStream("/data/comma/comma.rdfs"));
        // ld.parse(data + "comma/comma.rdfs");
        ld.parse(TestQuery1.class.getResourceAsStream("/data/comma/model.rdf"));
        // ld.parse(data + "comma/model.rdf");
        ld.parseDir(data + "comma/data");
    }

    Graph getGraph() {
        return graph;
    }

    public static Graph graph() {
        Graph graph = Graph.create(true);
        graph.setOptimize(true);

        Load ld = Load.create(graph);
        try {
            ld.parse(TestQuery1.class.getResource("data").getPath() + "/"
                    + "comma/comma.rdfs");
            ld.parse(TestQuery1.class.getResource("data").getPath() + "/"
                    + "comma/model.rdf");
            ld.parseDir(
                    TestQuery1.class.getResource("data").getPath() + "/" + "comma/data");
        } catch (LoadException ex) {
        }

        return graph;
    }

    public void generic() throws EngineException {
        String i = "insert data { [] rdf:value ( [] ) }";

        String q = "select (us:test() as ?t)  where {}"
                + "function us:test() {}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        exec.query(q);
    }

    @Test
    public void typecheckowlrl() throws EngineException, MalformedURLException, LoadException {
        Graph g = Graph.create();
        Load load = Load.create(g);
        load.parse(TestQuery1.class.getResource("/primer.owl").getPath());

        QueryProcess.create(g);
        Transformer t = Transformer.create(g, Transformer.OWLRL);
        t.process();
        Transformer t2 = Transformer.create(g, Transformer.TURTLE_HTML);
        IDatatype dt = t2.process(t.getBinding());
        // System.out.println(dt.getLabel());
        assertEquals(true, dt.getLabel().contains("<span class='fail'>"));
    }

    @Test
    public void union5() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "minus { "
                + "{?x foaf:name ?n filter us:incr(?x, ?n) }  union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // Mappings parameter focus edge iteration in graph/minus to relevant resources
        assertEquals(1, map.size());
        assertEquals(1, b.getVariable("?count").intValue());
    }

    @Test
    public void union4() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "optional {"
                + "graph ?g { "
                + "{?x foaf:name ?n filter us:incr(?x, ?n) }  union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // Mappings parameter focus edge iteration in optional/graph/union to relevant
        // resources
        assertEquals(2, map.size());
        assertEquals(1, b.getVariable("?count").intValue());
    }

    @Test
    public void union333() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "{select ?n where { "
                + "{?y foaf:name ?n filter us:incr(?y, ?n) } union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // System.out.println(map);
        // Mappings parameter do not focus edge iteration in query/union because select
        // ?n
        assertEquals(8, map.size());
        assertEquals(4, b.getVariable("?count").intValue());
    }

    @Test
    public void union33() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "{select * where { "
                + "{?y foaf:name ?n filter us:incr(?y, ?n) } union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // System.out.println(map);
        // Mappings parameter focus edge iteration in query/union because ?y is bound in
        // both branches
        assertEquals(2, map.size());
        assertEquals(2, b.getVariable("?count").intValue());
    }

    @Test
    public void union3() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "{select * where { "
                + "{?x foaf:name ?n filter us:incr(?x, ?n) } union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // Mappings parameter do not focus edge iteration in query/union because ?x and
        // ?y are not bound in both branches
        assertEquals(1, map.size());
        assertEquals(4, b.getVariable("?count").intValue());
    }

    @Test
    public void union2() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "graph ?g { "
                + "{?x foaf:name ?n filter us:incr(?x, ?n) }  union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        // Mappings parameter focus edge iteration in graph/union to relevant resources
        assertEquals(1, map.size());
        assertEquals(1, b.getVariable("?count").intValue());
    }

    @Test
    public void union1() throws EngineException, MalformedURLException, LoadException {
        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jack foaf:knows us:Jim ."
                + "us:Jim   foaf:name 'Jim' ."
                + "us:James foaf:name 'James' ."
                + "}";
        String q = "select * where {"
                + "?x foaf:knows ?y "
                + "optional { "
                + "{?x foaf:name ?n filter us:incr(?x, ?n) }  union { ?y foaf:name ?n filter us:incr(?y, ?n) } "
                + "}"
                + "}"
                + "function us:incr(?x, ?y) {"
                + "set(?count = ?count+1)"
                + "}";
        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Binding b = Binding.create();
        b.setGlobalVariable("?count", DatatypeMap.ZERO);
        exec.query(i);
        Mappings map = exec.query(q, b);
        assertEquals(2, map.size());
        // Mappings parameter focus edge iteration in optional/union to relevant
        // resources
        assertEquals(1, b.getVariable("?count").intValue());
    }

    // @Test
    public void format() throws EngineException {
        String i = "insert data { [] rdf:value () }";
        String q = "select (xt:xml(?m) as ?xml) (xt:rdf(?m) as ?rdf) (xt:json(?m) as ?json) "
                + "(xt:graph(?m) as ?g) "
                + "where { bind (us:test() as ?m) }"

                + "function us:test() { "
                + "query(select * where {?s ?p ?o})"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);

        DatatypeValue xml = map.getValue("?xml");
        DatatypeValue rdf = map.getValue("?rdf");
        DatatypeValue json = map.getValue("?json");
        map.getValue("?g");

        System.out.println(xml.stringValue());
        System.out.println(rdf.stringValue());
        System.out.println(json.stringValue());
    }

    @Test
    public void xslt() throws EngineException {
        String data = TestQuery1.class.getResource("/test/data/data.xml").getPath();
        String xslt = TestQuery1.class.getResource("/test/data/xslt.xsl").getPath();

        String q = String.format(
                "select (us:test(<%s>, <%s>) as ?t) (us:test(xt:xml(?t)) as ?name) where {}"

                        + "function us:test(xml, xslt) {"
                        + "let (doc = xt:xml(xml)) {"
                        + "xt:xslt(doc, xslt)"
                        + "}"
                        + "}",
                data, xslt)

                + "function us:test(xml) {"
                + "for (doc in xml) {"
                + "for (node in doc) {"
                + "return(dom:getNodeName(node))"
                + "}"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        DatatypeValue dt = map.getValue("?name");
        assertEquals("title", dt.stringValue());
    }

    @Test
    public void json() throws EngineException, MalformedURLException, LoadException, URISyntaxException {
        String q = "select (us:test() as ?t) "
                + "where {}"
                + "function us:test() {"
                + "let (json = xt:json('{\"knows\": [ {\"name\": \"John\"} , {\"name\": \"Jack\"}] }')) {"
                + "return (xt:path(json, '/knows/1/name'))"
                + "} "
                + "}";

        Graph g = Graph.create();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        DatatypeValue dt = map.getValue("?t");
        assertEquals("Jack", dt.stringValue());
    }

    @Test
    public void testmap2() throws EngineException {
        String q = "select (us:test(dt:map) as ?t)  "
                + "(us:test(dt:json) as ?js)"
                + "where {}"

                + "function us:test(atype) {"
                + "let (amap = xt:create(atype), "
                + "list = @(('a' 1)('b' 2)('c' 3))) {"
                + "for ((key val) in list) {"
                + "xt:set(amap, key, val)"
                + "} ;"
                + "let ((aa | bb . cc) = amap,"
                + "(ee) = bb) {"
                + "reduce(rq:plus, maplist (function((key, val)) { val }, xt:list(aa, ee, cc)))"
                + "}"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(6, map.getValue("?t").intValue());
        assertEquals(6, map.getValue("?js").intValue());
    }

    @Test
    public void testmap() throws EngineException {
        String q = "select (us:test(dt:map) as ?t)  "
                + "(us:test(dt:json) as ?js)"
                + "where {}"

                + "function us:test(atype) {"
                + "let (amap = xt:create(atype), "
                + "list = @(('a' 1)('b' 2)('c' 3))) {"
                + "for ((key val) in list) {"
                + "xt:set(amap, key, val)"
                + "} ;"
                + "reduce(rq:plus, maplist (function((key, val)) { val }, amap))"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(6, map.getValue("?t").intValue());
        assertEquals(6, map.getValue("?js").intValue());
    }

    @Test
    public void testgraph5() throws EngineException {
        String q = "select (us:test() as ?t)  "
                + "where {}"

                + "function us:test() {"
                + "let (agraph = construct {[] rdf:value 1, 2, 3} where {}) {"
                + "let ((t1 | t2 . t3) = maplist(xt:self, agraph), (tt) = t2) {"
                + "reduce(rq:plus, maplist (function((s, p, o)) { o }, xt:list(t1, tt, t3)))"
                + "}"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(6, map.getValue("?t").intValue());
    }

    @Test
    public void testgraph3() throws EngineException {
        String q = "select (us:test() as ?t)  "
                + "where {}"

                + "function us:test() {"
                + "let (agraph = construct {[] rdf:value 1, 2, 3} where {}) {"
                + "let ((t1 | t2 . t3) = agraph, (tt) = t2) {"
                + "reduce(rq:plus, maplist (function((s, p, o)) { o }, xt:list(t1, tt, t3)))"
                + "}"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(6, map.getValue("?t").intValue());
    }

    @Test
    public void testgraph2() throws EngineException {
        String q = "select (us:test() as ?t)  "
                + "where {}"

                + "function us:test() {"
                + "let (agraph = construct {[] rdf:value 1, 2, 3} where {}) {"
                + "reduce(rq:plus, maplist (function((s, p, o)) { o }, agraph))"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(6, map.getValue("?t").intValue());
    }

    @Test
    public void exception4() {
        String q = "select ?x (us:test() as ?t)  where { values ?x {1} }"
                + "function us:test() { try {us:foo() } catch (var) { static(myvar=var); throw(2) }  }"
                + "function us:foo()  {throw(throw(1))}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            exec.query(q);
            assertEquals(false, true);
        } catch (EngineException ex) {
            assertEquals(2, ex.getDatatypeValue().intValue());
            IDatatype dt = Binding.getStaticVariable("?myvar");
            assertEquals(1, dt.intValue());
        }
    }

    @Test
    public void exception3() {
        String q = "select ?x (us:test() as ?t)  where { values ?x {1} }"
                + "function us:test() { try {us:foo() } catch (var) { var }  }"
                + "function us:foo()  {throw(throw(1))}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            Mappings map = exec.query(q);
            assertEquals(1, map.size());
        } catch (EngineException ex) {
            assertEquals(false, true);
        }
    }

    @Test
    public void exception2() {
        String q = "select ?x (us:test() as ?t)  where { values ?x {1} }"
                + "function us:test() { try {us:foo() } catch (var) { var }  }"
                + "function us:foo()  {throw(1)}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            Mappings map = exec.query(q);
            assertEquals(1, map.size());
        } catch (EngineException ex) {
            assertEquals(false, true);
        }
    }

    @Test
    public void exception() throws EngineException {
        String q = "select ?x (us:test() as ?t)  where { values ?x {1} }"
                + "function us:test() { throw(1) }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        try {
            exec.query(q);
            assertEquals(false, true);
        } catch (LDScriptException ex) {
            assertEquals(1, ex.getDatatypeValue().intValue());
        }

    }

    @Test
    public void testStatic2() throws EngineException {
        String q = "select (us:test() as ?t)  where {}"
                + "function us:test() {static (stat = stat+1) }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Binding.setStaticVariable("?stat", DatatypeMap.ONE);
        Mappings map = exec.query(q);
        DatatypeValue dt = map.getValue("?t");
        assertEquals(true, (dt != null) && (dt.intValue() == 2));
        assertEquals(2, Binding.getStaticVariable("?stat").intValue());
    }

    @Test
    public void testStatic() throws EngineException {
        String q = "select (us:test() as ?t)  where {}"
                + "function us:test() {let (stat = stat+1) {return (stat) }}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Binding.setStaticVariable("?stat", DatatypeMap.ONE);
        Mappings map = exec.query(q);
        DatatypeValue dt = map.getValue("?t");
        assertEquals(true, (dt != null) && (dt.intValue() == 2));
        assertEquals(1, Binding.getStaticVariable("?stat").intValue());
    }

    @Test
    public void testREM() throws EngineException, LoadException {
        String q = "prefix ex: <function://fr.inria.corese.engine.TestQuery1> "
                + "select * where {bind (ex:mytest(xt:graph()) as ?t) } limit 10";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryProcess.setReentrant(true);
        Mappings map = exec.query(q);
        map = exec.query("select * from us:g1 where { ?s ?p ?o }");
        assertEquals(1, map.size());
    }

    public IDatatype mytest(IDatatype dt) {
        Graph g = (Graph) dt.getPointerObject();
        try {
            QueryProcess exec = QueryProcess.create(g);
            exec.query("insert { graph us:g1 { [] rdf:value ?v } } where { bind (rand() as ?v) }");
        } catch (EngineException ex) {
        }
        return DatatypeMap.TRUE;
    }

    @Test
    public void testForLoop() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {[] rdf:value (1)}";
        String q = "select (us:fun() as ?list) where {"
                + "}"
                + "function us:fun() {"
                + "let (list = xt:list()) {"
                + "for ((s, p, o) in select * where {?s ?p ?o}) {"
                + "xt:add(list, xt:list(s, p, o))"
                + "};"
                + "return (list)"
                + "} "
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?list");
        assertEquals(3, dt.size());
    }

    @Test
    public void testRDFList() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {[] rdf:value (1)}";
        String q = "construct where { [] rdf:value (1) }";
        exec.query(i);
        Mappings map = exec.query(q);
        Graph res = (Graph) map.getGraph();
        // System.out.println(map.getGraph());
        assertEquals(3, res.size());
    }

    @Test
    public void testXML4() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + " ?x ?y ?z "
                + "where {"
                + "bind ('"
                + "<doc xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance#\" xmlns:ns=\"http://example.org/\"  xml:base=\"http://example.org/\">"
                + "<name xml:lang=\"en\">label</name>"
                + "<age xsi:type=\"http://www.w3.org/2001/XMLSchema#integer\">10</age>"
                + "<test rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\">true</test>"
                + "</doc>' as ?doc)"
                + "bind (xt:xml(?doc) as ?xml)"
                + "bind (xt:list(us:test(?xml)) as ?test)"
                + "values (?x ?y ?z) {unnest(?test)}"
                + "}"

                + "function us:test(xml) {"
                + "let (list = xt:xpath(xml,'/doc/*/text()')"
                + ") {"
                + "maplist(dom:getNodeDatatypeValue, list)"
                + "}"
                + "}";

        Mappings map = exec.query(q);

        assertEquals("label", map.getValue("?x").stringValue());
        assertEquals("en", map.getValue("?x").getLang());
        assertEquals(10, map.getValue("?y").intValue());
        assertEquals(true, map.getValue("?z").booleanValue());
    }

    @Test
    public void testXML3() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + " ?x ?y ?z "
                + "where {"
                + "bind ('"
                + "<doc xmlns:ns=\"http://example.org/\"  xml:base=\"http://example.org/\">"
                + "<ns:test ns:att=\"att\">text</ns:test>"
                + "<other>"
                + "<![CDATA[cdata text]]>"
                + "<!--comment text-->"
                + "<?proc proc text?>"
                + "</other>"
                + "</doc>' as ?doc)"
                + "bind (xt:xml(?doc) as ?xml)"
                + "bind (xt:list(us:test(?xml)) as ?test)"
                + "values (?x ?y ?z) {unnest(?test)}"
                + "}"

                + "function us:test(xml) {"
                + "let ((other) = dom:getElementsByTagName(xml,  \"other\"), "
                // + "sublist = dom:getChildNodes(other)"
                + "sublist = xt:xpath(xml,'//other/node()')"
                + ") {"
                + "maplist(dom:getNodeValue, sublist)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals("cdata text", map.getValue("?x").stringValue());
        assertEquals("comment text", map.getValue("?y").stringValue());
        assertEquals("proc text", map.getValue("?z").stringValue());
    }

    @Test
    public void testXML2() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(us:fun(?xml) as ?t) "
                + "(dom:getNamespaceURI(?elem) as ?ns)"
                + "(dom:getBaseURI(?elem) as ?base)"
                + "(dom:getNodeName(?elem) as ?name)"
                + "(dom:getLocalName(?elem) as ?local)"
                + "(dom:hasAttributeNS(?elem, ?ns, 'att') as ?b)"
                + "(dom:getAttributeNS(?elem, ?ns, 'att') as ?att)"
                + "(dom:getFirstChild(?elem) as ?child)"
                + "(dom:getTextContent(?child) as ?text)"
                + "where {"
                + "bind (xt:xml('<doc xmlns:ns=\"http://example.org/\"  xml:base=\"http://example.org/\">"
                + "<ns:test ns:att=\"att\">text</ns:test></doc>') as ?xml)"
                + "bind (us:fun(?xml) as ?elem)"
                + "}"

                + "function us:fun(xml) {"
                + "let (dom = dom:getFirstChild(xml), ns = \"http://example.org/\","
                + "list = dom:getElementsByTagNameNS(dom, ns, \"test\"),"
                + "(first | rest) = list"
                + ") {"
                + "first"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals("http://example.org/", map.getValue("?ns").stringValue());
        assertEquals("http://example.org/", map.getValue("?base").stringValue());
        assertEquals("test", map.getValue("?local").stringValue());
        assertEquals("ns:test", map.getValue("?name").stringValue());
        assertEquals("text", map.getValue("?text").stringValue());
        // System.out.println(map);
    }

    @Test
    public void testXML1() throws EngineException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);
        String init = "insert data {"
                + "<doc> us:contain "
                + "'<doc xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance#\">"
                + "<phrase color=\"red\"><subject>Cat</subject><verb>on</verb><object>mat</object>"
                + "<size xsi:type=\"http://www.w3.org/2001/XMLSchema#integer\">10</size></phrase>"
                + "<phrase><subject xml:lang=\"en\">Cat</subject><verb>eat</verb><object>mouse</object></phrase>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = "select (xt:text(?dom) as ?t) "
                + "(xt:get(xt:attributes(?dom), 'color') as ?c)"
                + "(xt:nodename(?dom) as ?n)"
                + "where { "
                + "?x us:contain ?xml "
                + "bind (xt:xml(?xml) as ?doc)"
                + "bind (us:phrase(?doc) as ?dom)"
                + "}"

                + "function us:phrase(root) {"
                + "for (doc in root) {"
                + "for (phrase in xt:elements(doc, 'phrase')) {"
                + "return (phrase)"
                + "}"
                + "}"
                + "}";

        exec.query(init);
        Mappings map = exec.query(query);
        // System.out.println(map);
        assertEquals("Catonmat10", map.getValue("?t").stringValue());
        assertEquals("red", map.getValue("?c").stringValue());
        assertEquals("phrase", map.getValue("?n").stringValue());
    }

    @Test
    public void testReduce() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (reduce(us:test, xt:list()) as ?x) "
                + "(reduce(us:test, xt:list(1)) as ?y)"
                + "(reduce(us:test, xt:list(1, 2)) as ?z)"
                + "where {}"

                + "function us:test(x, y) {"
                + "x + y"
                + "}"

                + "function us:test() {"
                + "0"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(0, map.getValue("?x").intValue());
        assertEquals(1, map.getValue("?y").intValue());
        assertEquals(3, map.getValue("?z").intValue());

    }

    @Test
    public void testDynLet() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test(10) as ?l) (xt:get(?l, 0) as ?n) where {}"
                + ""
                + "function us:test(x) { "
                + "letdyn (y = x) { maplist(lambda(g) { y } , xt:list(y)) } "
                + "}";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(true, dt != null);
        if (dt != null) {
            assertEquals(10, dt.intValue());
        }
    }

    @Test
    public void testDynLet2() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test(10) as ?l) (xt:get(?l, 0) as ?n) where {}"
                + ""
                + "function us:test(x) { "
                + "letdyn (y = x) { maplist(lambda(g) { let (y = 2 * y) { y }} , xt:list(y)) } "
                + "}";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(true, dt != null);
        if (dt != null) {
            assertEquals(20, dt.intValue());
        }
    }

    @Test
    public void testDynLet3() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?n) where {}"
                + ""
                + "function us:test() { "
                + "letdyn (x = 1) { us:fun() } "
                + "}"

                + "function us:fun() { x }";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(true, dt != null);
        if (dt != null) {
            assertEquals(1, dt.intValue());
        }
    }

    @Test
    public void testDynLet4() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (reduce(rq:plus, us:bar(10)) as ?a) where {}"

                + "function us:bar(n) {"
                + "letdyn(select (1 as ?x) where {}) {"
                + "maplist(lambda(y) { set(x = 2*x) }, xt:iota(n)) "
                + "}"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(2046, map.getValue("?a").intValue());
    }

    @Test
    public void testDynLet44() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (reduce(rq:plus, us:bar(10)) as ?a) where {}"

                + "function us:bar(n) {"
                + "letdyn(select (1 as ?x) where {}) {"
                + "maplist(us:test, xt:iota(n)) "
                + "}"
                + "}"

                + "function us:test(y) {"
                + "set(x = 2*x)"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(2046, map.getValue("?a").intValue());
    }

    @Test
    public void testDynLet5() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?n) where {}"
                + ""
                + "function us:test() { "
                + "let (z = 1) {"
                + "letdyn (x = 1) { us:fun() } "
                + "}"
                + "}"

                + "function us:fun() { z }"

                + "";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(true, dt == null);
    }

    @Test
    public void testDynLet6() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?n) where {}"
                + ""
                + "function us:test() { "
                + "letdyn (x = 1) { us:fun() } "
                + "}"

                + "function us:fun() { let (x = 2) { x } }"

                + "";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(2, dt.intValue());
    }

    @Test
    public void testexist() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {  "
                + "graph us:g1 { "
                + "rdf:type rdf:type   rdf:Property "
                + "rdf:type owl:sameAs rdf:type "
                + "rdf:type us:test  us:test "
                + "us:test us:test us:test "
                + "}"
                + "}";

        String q = "select * where {"
                + "bind (bnode() as ?bn)"

                + "bind (xt:exists(rdf:type,  rdf:type, rdf:Property)  as ?c1)"
                + "bind (xt:exists(?bn,  rdf:type, rdf:Property)    as ?c2)"
                + "bind (xt:exists(?bn,  rdf:type, ?bn)             as ?c3)"
                + "bind (xt:exists(?bn,  bnode(), ?bn)              as ?c4)"
                + "bind (xt:exists(?bn, ?bn)                        as ?c5)"

                + "bind (xt:exists(?bn,  ?bn, rdf:Property) as ?b1)"
                + "bind (xt:exists(bnode(),  ?bn,  ?bn)     as ?b2)"
                + "bind (xt:exists(?bn,  ?bn,  ?bn)         as ?b3)"

                + "graph kg:default {"
                + "bind (bnode() as ?bb)"
                + "bind (xt:exists(?bb,  ?bb, rdf:Property) as ?a1)"
                + "bind (xt:exists(?bb,  bnode(), ?bb)      as ?a2)"
                + "bind (xt:exists(bnode(),  ?bb,  ?bb)     as ?a3)"
                + "bind (xt:exists(bnode(),  bnode(),  bnode())  as ?a4)"
                + "bind (xt:exists(?bb,  ?bb,  ?bb)         as ?a5)"
                + "}"

                + "}"

        ;

        exec.query(i);
        Mappings map = exec.query(q);
        // ?c1 = true; ?c2 = true; ?c3 = false; ?c4 = true; ?c5 = true;
        // ?b1 = true; ?b3 = true; ?b4 = true;
        // ?a1 = false; ?a2 = false; ?a3 = false; ?a4 = false; ?a5 = false;
        assertEquals(true, map.getValue("?c1").booleanValue());
        assertEquals(true, map.getValue("?c2").booleanValue());
        assertEquals(false, map.getValue("?c3").booleanValue());
        assertEquals(true, map.getValue("?c4").booleanValue());
        assertEquals(true, map.getValue("?c5").booleanValue());

        assertEquals(true, map.getValue("?b1").booleanValue());
        assertEquals(true, map.getValue("?b2").booleanValue());
        assertEquals(true, map.getValue("?b3").booleanValue());

        assertEquals(false, map.getValue("?a1").booleanValue());
        assertEquals(false, map.getValue("?a2").booleanValue());
        assertEquals(false, map.getValue("?a3").booleanValue());
        assertEquals(false, map.getValue("?a4").booleanValue());
        assertEquals(false, map.getValue("?a5").booleanValue());

    }

    @Test
    public void testTTL1() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {  "
                + "_:b rdf:value (_:b)  "
                + "}";
        String q = "select "
                + "(st:apply-templates-with(st:turtle) as ?t) "
                + "where { }";

        exec.query(i);
        Mappings map = exec.query(q);
        System.out.println("res: " + map.getValue("?t").stringValue());
        String str = map.getValue("?t").stringValue();
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.loadString(str, Load.TURTLE_FORMAT);

        QueryProcess ex = QueryProcess.create(gg);
        String qq = "select * where {"
                + "?s ?p (?s) "
                + "}";

        Mappings m1 = exec.query(qq);
        Mappings m2 = ex.query(qq);
        assertEquals(m1.size(), m2.size());
    }

    @Test
    public void testTTL2() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {  "
                + "_:b rdf:value _:b  "
                + "}";
        String q = "select (st:apply-templates-with(st:turtle) as ?t) where { }";

        exec.query(i);
        Mappings map = exec.query(q);
        // System.out.println(map.getValue("?t").stringValue());
        String str = map.getValue("?t").stringValue();
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.loadString(str, Load.TURTLE_FORMAT);

        QueryProcess ex = QueryProcess.create(gg);
        String qq = "select * where {"
                + "?s ?p ?s  "
                + "}";

        Mappings m1 = exec.query(qq);
        Mappings m2 = ex.query(qq);
        assertEquals(m1.size(), m2.size());
    }

    @Test
    public void testTTL3() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {  "
                + "_:b rdf:value [rdf:value _:b]  "
                + "}";
        String q = "select (st:apply-templates-with(st:turtle) as ?t) where { }";

        exec.query(i);
        Mappings map = exec.query(q);
        // System.out.println(map.getValue("?t").stringValue());
        String str = map.getValue("?t").stringValue();
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.loadString(str, Load.TURTLE_FORMAT);

        QueryProcess ex = QueryProcess.create(gg);
        String qq = "select * where {"
                + "?s ?p [ rdf:value ?s]  "
                + "}";

        Mappings m1 = exec.query(qq);
        Mappings m2 = ex.query(qq);
        assertEquals(m1.size(), m2.size());
    }

    @Test
    public void testTTL4() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {  "
                + "_:a rdf:value _:e "
                + "_:b rdf:value _:e "
                + "_:e rdf:value _:b "
                + "}";

        String q = "select (st:apply-templates-with(st:turtle) as ?t) where { }";

        exec.query(i);
        Mappings map = exec.query(q);
        // System.out.println(map.getValue("?t").stringValue());
        String str = map.getValue("?t").stringValue();
        Graph gg = Graph.create();
        Load ld = Load.create(gg);
        ld.loadString(str, Load.TURTLE_FORMAT);

        QueryProcess ex = QueryProcess.create(gg);
        String qq = "select * where {"
                + "?a rdf:value ?e "
                + "?b rdf:value ?e "
                + "?e rdf:value ?b   "
                + "}";

        Mappings m1 = exec.query(qq);
        Mappings m2 = ex.query(qq);
        assertEquals(m1.size(), m2.size());
    }

    @Test
    public void testOPP2() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data {  "
                + "us:John foaf:knows us:Jim , us:John . "
                + "us:Jim  foaf:knows us:John, us:Jim "
                + "}";
        String q = "select * where {  ?x foaf:knows+ ?y }";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(4, map.size());

    }

    @Test
    public void testOPP() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {  "
                + "us:John foaf:knows  us:Jim , us:John . "
                + "us:Jim foaf:knows us:John, us:Jim."
                + "}";

        String q = "select * where { ?x foaf:knows+ ?y }";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(4, map.size());
    }

    @Test
    public void testNGG1() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "optional { ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?g");
            assertEquals(true, dt != null);
        }
    }

    @Test
    public void testNGG2() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim, us:Jesse }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "minus { ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?g");
            assertEquals(true, dt != null);
        }
    }

    @Test
    public void testNGG3() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim, us:Jesse }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "{?s foaf:knows  ?o }"
                + "union { ?s foaf:knows  ?o ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(5, map.size());
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?g");
            assertEquals(true, dt != null);
        }
    }

    @Test
    public void testNGG4() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:Jack foaf:knows us:Jim, us:Jesse }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jules foaf:knows us:James }"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o filter exists {?o foaf:knows ?y} "
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
    }

    @Test
    public void testNGG5() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "from named us:g2 "
                + "where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "optional { ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?g");
            assertEquals(true, dt != null);
        }
    }

    @Test
    public void testNGG6() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim }"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "where {"
                + "graph us:g2 {"
                + "?s foaf:knows  ?o "
                + "optional { ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());

    }

    @Test
    public void testNGG7() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim . us:Jack foaf:knows us:James}"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "from named us:g1 "
                + "from named us:g2 "
                + "where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "filter exists { ?o foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testNGG8() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim . us:Jack foaf:knows us:Jim}"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "from named us:g1 "
                + "from named us:g2 "
                + "where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "{select * where  { ?o foaf:knows ?y }}"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testNGG9() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim . us:Jack foaf:knows us:Jim}"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "graph us:g3 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "from named us:g1 "
                + "from named us:g2 "
                + "where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "bind ( exists { ?o foaf:knows ?y } as ?b )"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        int count = 0;
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?b");
            if (dt.booleanValue())
                count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testNGG10() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "graph us:g1 { us:John foaf:knows us:Jim . us:Jack foaf:knows us:Jim}"
                + "graph us:g2 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "graph us:g3 { us:Jim foaf:knows us:Jack . us:Jack foaf:knows us:James }"
                + "}";

        String q = "select * "
                + "from named us:g1 "
                + "from named us:g2 "
                + "where {"
                + "graph ?g {"
                + "?s foaf:knows  ?o "
                + "values ?b { unnest( exists { ?o foaf:knows ?y } )  }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        int count = 0;
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?b");
            if (dt.booleanValue())
                count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testNGG11() throws EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String q = "select * where {"
                + "bind (query(construct { us:John foaf:knows us:Jack, us:Jim . us:Jack foaf:knows us:James } where {}) as ?g)"
                + "graph ?g { "
                + "{ ?s foaf:knows ?o filter exists {?s foaf:knows ?t} graph ?g {filter us:test(?g)} "
                + "  optional { ?o foaf:knows ?y filter exists {?y foaf:knows ?t}}}"
                + "union"
                + "{ ?a foaf:knows ?b minus { ?b foaf:knows ?c }}"
                + "}"
                + "}"

                + "function us:test(?g) {"
                // + "xt:print(?g)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(5, map.size());
    }

    @Test
    public void testRee() throws EngineException {

        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);
        QueryProcess.setOverwrite(true);

        String i1 = "insert data { graph <http://example.org/g>  { us:John rdfs:label 'John'  } }";
        String i11 = "with <http://example.org/g1> "
                + "insert { ?x rdfs:label ?y }"
                + "where { graph <http://example.org/g>  { ?x rdfs:label ?y  } }";

        String i2 = "with <http://example.org/g> "
                + "insert  { ?x foaf:name ?n } where { ?x rdfs:label ?n }";

        String q = "select * "
                + "from  <http://example.org/g> "
                + "where { ?x ?p ?y  }";

        String q2 = "select * "
                + "from  <http://example.org/g> "
                + "where { ?x foaf:name ?y  }";

        String q4 = "select * "
                + "from named <http://example.org/g> "
                + "where { "
                + "graph ?g { ?x foaf:name ?y  } "
                + "}";

        exec.query(i1);
        Graph g = graph.getNamedGraph("http://example.org/g");

        exec.query(i11);
        graph.getNamedGraph("http://example.org/g1");
        exec.query(q4);
        // System.out.println(m4);

        exec.query(q);
        exec.query(i2);
        Mappings m2 = exec.query(q);
        assertEquals(2, m2.size());
        assertEquals(true, g != null);
        if (g != null) {
            assertEquals(2, g.size());
        }
        Mappings m3 = exec.query(q2);
        assertEquals(1, m3.size());

        QueryProcess.setOverwrite(false);

    }

    @Test
    public void testLock() throws EngineException {

        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        QueryProcess.setOverwrite(true);

        String i = "select * where { "
                + "bind (us:test()  as ?r) "
                + "graph us:g1 { ?s ?p ?o }"
                + "}"
                + "function us:test() {"
                + "query(insert data { graph us:g1 { us:Jack foaf:name 'Jack' } } ) "
                + "}";

        String q2 = "drop graph us:g1";

        String q3 = "select * where {"
                + "graph us:g1 { ?s ?p ?o }"
                + "}";

        Mappings map = exec.query(i);
        assertEquals(1, map.size());
        exec.query(q2);
        Mappings m = exec.query(q3);
        assertEquals(0, m.size());

        QueryProcess.setOverwrite(false);

    }

    @Test
    public void testBB() throws EngineException {

        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String i = "insert data {"
                + "us:John foaf:knows us:Jack ."
                + "us:Jim  foaf:knows us:James ."
                + "}";

        String q = "select * where {"
                + "?a foaf:knows  ?b "
                + "{select * where  { ?a foaf:knows ?c }}"
                + "{select ?d where { ?a foaf:knows ?d }}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(4, map.size());
    }

    @Test
    public void testUpdate() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:foo(us:John) as ?g) where {}"

                + "function us:foo(?x) {"
                + "let (?g = construct {} where {} ) {"
                + "xt:focus(?g, query("
                + "insert data { "
                + "us:John rdfs:label 'John', 'Jack'"
                + "us:Jim  rdfs:label 'Jim'"
                + "} ;"
                + "delete {?x rdfs:label ?name} insert {?x foaf:name ?name} "
                + "where  {?x rdfs:label ?name}"
                + ") ) ;"
                + "?g"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?g");
        Graph gg = (Graph) dt.getPointerObject();

        String qq = "select ?p (count(*) as ?c) {"
                + "?x ?p ?y"
                + "}"
                + "group by ?p order by ?c ";
        QueryProcess exec2 = QueryProcess.create(gg);
        Mappings m = exec2.query(qq);
        assertEquals(1, m.get(0).getValue("?c").intValue());
        assertEquals(2, m.get(1).getValue("?c").intValue());
    }

    @Test
    public void testApply() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "function xt:main() {"
                + "apply(us:test, xt:list()) + apply(us:test, xt:list(1)) + apply (us:test, xt:list(2, 3))"
                + "}"

                + "function us:test() {"
                + "10"
                + "}"

                + "function us:test(?x) {"
                + "?x"
                + "}"

                + "function us:test(?x, ?y) {"
                + "?x + ?y "
                + "}"

        ;

        Mappings dt = exec.query(q);
        assertEquals(16, dt.get(0).getValue("?_main_").intValue());
    }

    @Test
    public void testMap3() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();
        String q = "select  "
                + "(xt:size(us:test(5))   as ?a)"
                + "(xt:size(us:test(10))  as ?b)"
                + "(xt:size(us:test(15))  as ?c)"
                + "(xt:size(us:test2(5))  as ?d)"
                + "(xt:size(us:test2(10)) as ?e)"
                + "(xt:size(us:test2(15)) as ?f)"
                + "where {}"

                + "function us:test(?n) {"
                + "maplist(lambda(?x, ?y) { xt:list(?x, ?y) }, xt:iota(?n), us:map())"
                + "}"

                + "function us:test2(?n) {"
                + "maplist(lambda(?x, ?y) { xt:list(?x, ?y) }, us:map(),xt:iota(?n))"
                + "}"

                + "function us:map() {"
                + "let (?m = xt:map()) {"
                + "xt:set(?m, 1, 1) ;"
                + "xt:set(?m, 01, 01) ;"
                + "xt:set(?m, 1.0, 1.0) ;"
                + "xt:set(?m, 1.0e0, 1.0e0) ;"
                + "xt:set(?m, '1'^^xsd:int, '1'^^xsd:int) ;"
                + "xt:set(?m, 1, 1) ;"
                + "xt:set(?m, 01, 01) ;"
                + "xt:set(?m, true, true) ;"
                + "xt:set(?m, st:test, st:test) ;"
                + "xt:set(?m, 'test', 'test') ;"
                + "xt:set(?m, 'test'@fr, 'test'@fr) ;"
                + "xt:set(?m, 'test', 'test') ;"
                + "xt:set(?m, bnode(), bnode()) ;"
                + "return (?m)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(5, map.getValue("?a").intValue());
        assertEquals(10, map.getValue("?b").intValue());
        assertEquals(15, map.getValue("?c").intValue());
        assertEquals(10, map.getValue("?d").intValue());
        assertEquals(10, map.getValue("?e").intValue());
        assertEquals(10, map.getValue("?f").intValue());
    }

    @Test
    public void testMap2() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();

        String q = "function xt:main() {"
                + "let (?m = xt:map()) {"
                + "xt:set(?m, 1, 1) ;"
                + "xt:set(?m, 01, 01) ;"
                + "xt:set(?m, 1.0, 1.0) ;"
                + "xt:set(?m, 1.0e0, 1.0e0) ;"
                + "xt:set(?m, '1'^^xsd:int, '1'^^xsd:int) ;"
                + "xt:set(?m, 1, 1) ;"
                + "xt:set(?m, 01, 01) ;"
                + "xt:set(?m, true, true) ;"
                + "xt:set(?m, st:test, st:test) ;"
                + "xt:set(?m, 'test', 'test') ;"
                + "xt:set(?m, 'test'@fr, 'test'@fr) ;"
                + "xt:set(?m, 'test', 'test') ;"
                + "xt:set(?m, bnode(), bnode()) ;"
                + "return (?m)"
                + "}"
                + "}";

        Mappings dt = exec.query(q);
        assertEquals(10, dt.get(0).getValue("?_main_").size());
    }

    @Test
    public void testNGExist() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();

        String i = "insert data {"
                + "graph us:g1 { us:Jack foaf:name 'Jack' ; foaf:knows us:Jim }"
                + "graph us:g2 { us:Jack foaf:knows us:James . us:James foaf:name 'James' }"
                + "}";

        String q = "select * "
                + "where {"
                + "graph ?g {  "
                + "?x foaf:name ?n optional { ?x foaf:name ?nn filter exists { ?z foaf:name 'Jack' } }"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        for (Mapping m : map) {
            DatatypeValue dt = m.getValue("?g");
            DatatypeValue val = m.getValue("?nn");
            assertEquals(dt.stringValue().contains("g2"), val == null);
        }
    }

    @Test
    public void testNG() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();

        String i = "insert data {"
                + "graph us:g1 { us:Jack foaf:name 'Jack' ; foaf:knows us:Jim }"
                + "graph us:g2 { us:Jack foaf:knows us:James . us:James foaf:name 'James' }"
                + "}";

        String q = "select * "
                + "where {"
                + "graph ?g {  ?x foaf:name ?n  "
                + "{ ?x foaf:knows ?y } union { ?y foaf:knows ?x } "
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testPPmatch2() throws EngineException {
        String q = "function xt:main() { us:test(xt:iota(2)) }"

                + "function us:test(?list) {"
                + "let ((?a ?b | ?l . ?c ?d) = ?list) {"
                + "return(?a + ?b + ?c + ?d + xt:size(?l))"
                + "}"
                + "}";

        QueryProcess exec = QueryProcess.create(Graph.create());
        Mappings dt = exec.query(q);
        assertEquals(6, dt.get(0).getValue("?_main_").intValue());
    }

    @Test
    public void testPPmatch() throws EngineException {
        String q = "function xt:main() { us:test(xt:iota(5)) }"

                + "function us:test(?list) {"
                + "let ((?a ?b | ?l . ?c ?d) = ?list) {"
                + "return(?a + ?b + ?c + ?d + xt:size(?l))"
                + "}"
                + "}";

        QueryProcess exec = QueryProcess.create(Graph.create());
        Mappings dt = exec.query(q);
        assertEquals(13, dt.get(0).getValue("?_main_").intValue());
    }

    @Test
    public void testpplist() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { "
                + "graph us:g1 { us:John us:age 10 } "
                + "graph us:g2 { us:John us:age 10, 20, 30 }"
                + "}";

        String q = "prefix ns: <http://example.org>"
                + "select * "
                + "(let ((?x ?y | ?l) = xt:graph()) { ?l } as ?l) "
                + "(let ((?t | ?ll) = ?l) { ?t } as ?t) "
                + "(let ((?s ?p | ?ll) = ?t) { ?ll } as ?ll) "
                + "(let ((?o | ?rst) = ?ll) { ?o } as ?o) "
                + "where {"
                + "}";
        exec.query(i);
        Mappings map = exec.query(q);
        // System.out.println(map);
        IDatatype dt = map.getValue("?o");
        assertEquals(20, dt.intValue());
    }

    @Test
    public void testplist() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String q = "prefix ns: <http://example.org>"
                + "select * "
                + "(let ((?x ?y | ?l) = xt:list(1, 2, 3, 4)) { ?l } as ?l) "
                + "(let ((?x ?y | ?l) = xt:list(1, 2, 3, 4)) { ?y } as ?y) "
                + "where {"
                + "}";
        Mappings map = exec.query(q);
        IDatatype list = map.getValue("?l");
        IDatatype dt = map.getValue("?y");
        assertEquals(true, list.equals(DatatypeMap.newList(3, 4)));
        assertEquals(2, dt.intValue());

    }

    @Test
    public void testextequal() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select "
                + "(kg:equals('ete', 'été') as ?eq)"
                + "(kg:contains('un été', 'ete') as ?ct)"
                + "where {}";
        Mappings map = exec.query(q);
        IDatatype dt1 = map.getValue("?eq");
        IDatatype dt2 = map.getValue("?ct");
        assertEquals(true, dt1.booleanValue() && dt2.booleanValue());
    }

    // @Test
    public void testgraphit() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { "
                + "graph us:g1 { us:John us:age 10, 20 } "
                + "graph us:g2 { us:John us:knows us:Jack }"
                + "}";

        String q = "select * (xt:toList(xt:graph()) as ?list) "
                + "(let ((?e1) = ?list) {?e1} as ?t1)"
                + "(let ((?e1, ?e2) = ?list) {?e2} as ?t2)"
                + "(let ((?s, ?p, ?o) = ?t1) {?o} as ?v1)"
                + "(let ((?s, ?p, ?o) = ?t2) {?o} as ?v2)"
                + "where {"
                + "}";
        exec.query(i);
        Mappings map = exec.query(q);
        IDatatype dt1 = map.getValue("?v1");
        IDatatype dt2 = map.getValue("?v2");
        assertEquals(true, dt1.intValue() == 10 && dt2.intValue() == 20);
    }

    @Test
    public void testedge() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { "
                + "graph us:g1 { us:John us:age 10, 20 } "
                + "graph us:g2 { us:John us:knows us:Jack }"
                + "}";

        String q = "select * "
                + "where {"
                + "values ?t { unnest(xt:edges(us:age)) }"
                + "}"

        ;

        exec.query(i);

        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        IDatatype v1 = map.get(0).getValue("?t");
        IDatatype v2 = map.get(1).getValue("?t");
        assertEquals(10, v1.getPointerObject().getEdge().getNode(1).getDatatypeValue().intValue());
        assertEquals(20, v2.getPointerObject().getEdge().getNode(1).getDatatypeValue().intValue());

    }

    @Test
    public void testvalue() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { "
                + "graph us:g1 { us:John us:age 10 } "
                + "graph us:g2 { us:John us:knows us:Jack }"
                + "}";

        String q = "select * (xt:index(?x) as ?i) (xt:index(?a) as ?j) (xt:index(us:age) as ?k) "
                + "where {"
                + "?x us:age ?a "
                + "filter (?a = xt:value(?x, us:age))"
                + "filter (?a = xt:value(?x, us:age, 1))"
                + "filter (?x = xt:value(?x, us:age, 0))"

                + "}"
                + "";

        exec.query(i);

        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testLList() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String q = "select "
                + "(xt:reverse(xt:iota(5)) as ?list)"
                + "(xt:member(xt:iota(5), 5) as ?member)"
                + "(xt:sort(xt:reverse(xt:iota(5))) as ?sort)"
                + "(xt:remove(xt:iota(5), 5) as ?remove) "
                + "(xt:removeindex(xt:iota(5), 0) as ?remindex) "
                + "(xt:swap(xt:iota(5), 0, 1) as ?swap)"
                + "where {"
                + "}"
                + "";

        Mappings map = exec.query(q);
        IDatatype rev = DatatypeMap.newList(5, 4, 3, 2, 1);
        IDatatype sort = DatatypeMap.newList(1, 2, 3, 4, 5);
        IDatatype rem = DatatypeMap.newList(1, 2, 3, 4);
        IDatatype remi = DatatypeMap.newList(2, 3, 4, 5);
        IDatatype sw = DatatypeMap.newList(2, 1, 3, 4, 5);

        IDatatype list = map.getValue("?list");
        assertEquals(true, list.eq(rev).booleanValue());

        IDatatype sorted = map.getValue("?sort");
        assertEquals(true, sort.eq(sorted).booleanValue());

        IDatatype remove = map.getValue("?remove");
        assertEquals(true, rem.eq(remove).booleanValue());

        IDatatype remindex = map.getValue("?remindex");
        assertEquals(true, remi.eq(remindex).booleanValue());

        IDatatype swap = map.getValue("?swap");
        assertEquals(true, sw.eq(swap).booleanValue());

    }

    @Test
    public void testList() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select "
                + "(xt:list() as ?nil)"
                + "(xt:list(1, 2) as ?list)"
                + "(xt:cons(0, ?list) as ?res)"
                + "(xt:first(?res) as ?fst)"
                + "(xt:rest(?res) as ?rst)"
                + "(us:copy(xt:list(1, 2)) as ?cp)"
                + "(us:append(xt:list(1, 2), xt:list(3, 4)) as ?app)"
                + "(xt:list(1, 3) as ?ll) (xt:set(?ll, 1, 2) as ?set)"
                + "where {}"
                + "function us:copy(?list) { maplist(xt:self, ?list) }"
                + ""
                + "function us:append(?l1, ?l2) {"
                + "if (xt:size(?l1) = 0, us:copy(?l2),"
                + "xt:cons(xt:first(?l1), us:append(xt:rest(?l1), ?l2)))}"
                + "";

        Mappings map = exec.query(q);
        IDatatype res = map.getValue("?res");
        assertEquals(true, res.eq(DatatypeMap.newList(0, 1, 2)).booleanValue());

        IDatatype fst = map.getValue("?fst");
        assertEquals(true, fst.eq(DatatypeMap.ZERO).booleanValue());

        IDatatype rst = map.getValue("?rst");
        assertEquals(true, rst.eq(DatatypeMap.newList(1, 2)).booleanValue());

        IDatatype app = map.getValue("?app");
        assertEquals(true, app.eq(DatatypeMap.newList(1, 2, 3, 4)).booleanValue());

        IDatatype ll = map.getValue("?ll");
        assertEquals(true, ll.eq(DatatypeMap.newList(1, 2)).booleanValue());
    }

    @Test
    public void testIterate() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String q = "select (us:test() as ?s) "
                + "where {"
                + "}"
                + ""
                + "function us:test() {"
                + "let (?list = xt:list(), ?sum = 0){"
                + "for (?i in xt:iterate(1, 10)) {"
                + "set(?sum = ?sum + ?i)"
                + "};"
                + "?sum"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?s");
        assertEquals(55, dt.intValue());
    }

    @Test
    public void testGName() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String q = "select (st:cget(st:test1, st:value) as ?b1)  (st:cget(st:test2, st:value) as ?b2) "
                + "where {"
                + "bind (st:cset(st:test1, st:value, 10) as ?v1)"
                + "bind (st:cset(st:test2, st:value, 20) as ?v2)"
                + "}";

        Mappings map = exec.query(q);
        IDatatype v1 = map.getValue("?b1");
        IDatatype v2 = map.getValue("?b2");
        assertEquals(true, v1.intValue() == 10 && v2.intValue() == 20);
    }

    @Test
    public void testGName2() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { "
                + "graph us:g1 { us:John us:age 10 } "
                + "graph us:g2 { us:John us:knows us:Jack }"
                + "}";

        String q = "select * where {"
                + "graph ?g { "
                + "?s ?p ?o bind (xt:name() as ?g1) optional { ?s ?q ?r  bind(xt:name() as ?g2)  } "
                + "}"
                + "}";

        exec.query(i);

        Mappings map = exec.query(q);
        for (Mapping m : map) {
            IDatatype g1 = m.getValue("?g1");
            IDatatype g2 = m.getValue("?g2");
            assertEquals(true, g1.equals(g2));
        }
    }

    @Test
    public void testAccess() throws LoadException, EngineException {
        Graph gg = Graph.create();
        QueryProcess exec = QueryProcess.create(gg);

        String i = "insert data { us:John us:age 10 }";

        String q = "select "
                + "(xt:subject(?t) as ?s) (xt:property(?t) as ?p) "
                + "(xt:object(?t) as ?o)  (xt:graph(?t) as ?g) "
                + "where {"
                + "values ?t { unnest( xt:graph() )}"
                + "}";

        exec.query(i);

        Mappings map = exec.query(q);
        IDatatype s = map.getValue("?s");
        IDatatype p = map.getValue("?p");
        IDatatype o = map.getValue("?o");
        IDatatype g = map.getValue("?g");

        assertEquals(NSManager.USER + "John", s.getLabel());
        assertEquals(NSManager.USER + "age", p.getLabel());
        assertEquals(10, o.intValue());
        assertEquals(NSManager.KGRAM + "default", g.getLabel());
    }

    @Test
    public void testPPath() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert data { "
                + "graph us:g1 { us:John us:name 'John'   ; us:knows us:James }"
                + "graph us:g2 { us:James us:name 'James' ; us:knows us:Jack}"
                + "}";

        // String q1 = "select * where { ?x us:knows+ ?y }";
        // String q2 = "select * where { ?x us:knows* ?y }";
        // String q3 = "select * from us:g1 where { ?x us:knows* ?y }";
        // String q4 = "select * where { graph ?g { ?x us:knows* ?y }}";
        // String q5 = "select * from named us:g1 where { graph ?g { ?x us:knows* ?y
        // }}";
        // String q6 = "select * where { us:John us:knows* ?y }";
        // String q7 = "select * from us:g1 where { us:John us:knows* ?y }";
        // String q8 = "select * from us:g1 from us:g2 where { us:John us:knows* ?y }";
        // String q9 = "select * from named us:g1 where { graph ?g { us:John us:knows*
        // ?y }}";
        // String q10 = "select * where { graph us:g1 { ?x us:knows* ?y }}";
        String q11 = "select *  where { bind (us:Jim as ?x) bind (us:g1 as ?g) graph  ?g { ?x us:knows* ?y  }}";
        String q12 = "select *  where { bind (us:g1 as ?g) graph  ?g { us:Jim us:knows* ?y  }}";
        String q13 = "select * from us:g1 where { us:Jack us:knows* ?y  }";

        exec.query(i);

        // Mappings m1 = exec.query(q1);
        // assertEquals(3, m1.size());
        //
        // Mappings m2 = exec.query(q2);
        // assertEquals(8, m2.size());
        //
        // Mappings m3 = exec.query(q3);
        // assertEquals(4, m3.size());
        //
        // Mappings m4 = exec.query(q4);
        // assertEquals(8, m4.size());
        //
        // Mappings m5 = exec.query(q5);
        // assertEquals(4, m5.size());
        //
        // Mappings m6 = exec.query(q6);
        // assertEquals(3, m6.size());
        //
        // Mappings m7 = exec.query(q7);
        // assertEquals(2, m7.size());
        //
        // Mappings m8 = exec.query(q8);
        // assertEquals(3, m8.size());
        //
        // Mappings m9 = exec.query(q9);
        // assertEquals(2, m9.size());
        //
        // Mappings m10 = exec.query(q10);
        // assertEquals(4, m10.size());

        Mappings m11 = exec.query(q11);
        // System.out.println(m11);
        // 1 is a bug, it should be 0 !!!
        assertEquals(1, m11.size());

        Mappings m12 = exec.query(q12);
        assertEquals(1, m12.size());

        Mappings m13 = exec.query(q13);
        assertEquals(1, m13.size());
    }

    @Test
    public void testSparql() throws LoadException, EngineException {
        Graph g = Graph.create();
        String q = "select (xt:sparql('select * (sum(?i) as ?sum) where { values ?i { unnest(xt:iota(5))} }') as ?res) "
                + "where {}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        Mappings m = dt.getPointerObject().getMappings();
        IDatatype sum = m.getValue("?sum");
        assertEquals(15, sum.intValue());
    }

    @Test
    public void testSparql2() throws LoadException, EngineException {
        Graph g = Graph.create();
        String q = "select (xt:sparql('select * (sum(?i) as ?sum) "
                + "where { values ?n {UNDEF} values ?i { unnest(xt:iota(?n))} }', '?n', 5) as ?res) "
                + "where {}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        Mappings m = dt.getPointerObject().getMappings();
        IDatatype sum = m.getValue("?sum");
        assertEquals(15, sum.intValue());
    }

    @Test
    public void testNumber() throws LoadException, EngineException {
        Graph g = Graph.create();
        String q = "select (kg:number() as ?n) (sum(?n) as ?sum) where {"
                + "values ?e { unnest (xt:iota(5)) }"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(10, dt.intValue());
    }

    @Test
    public void testJSON() throws EngineException, LoadException {
        String t = "template  {  st:apply-templates-with(st:json)}"
                + "where {}";

        Graph g = Graph.create(); // createGraph();
        Load ld = Load.create(g);
        ld.parse(data + "jsonld/test.jsonld");

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t);

        String json = map.getTemplateStringResult();
        // System.out.println(json);

        assertEquals(true, (json.length() <= 1350 && json.length() >= 1000));

        Graph gg = Graph.create();
        Load ll = Load.create(gg);
        ll.loadString(json, Load.JSONLD_FORMAT);
        // System.out.println(g.size() + " " + gg.size());
        assertEquals(g.size(), gg.size());

    }

    @Test
    public void testNG2() throws EngineException {

        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String q = "select * "
                + "where {"
                + "bind (us:gg() as ?gg) "
                + "graph ?gg {"
                + "?x foaf:knows ?y "
                + "values ?g { us:g1 }"
                + " "
                + "{select ?y ?l  where {  graph ?g {  ?y rdfs:label ?l }}}"
                + "}"
                + "}"

                + "function us:gg() {"
                + "let (?g = construct {"
                + "graph us:g1 {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "} "
                + "graph us:g2 {"
                + "<Jim> rdfs:label 'Jim' "
                + "} } where {}) {"
                + "?g"
                + "}"
                + "}";

        // exec.query(init);

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testNG1() throws EngineException {

        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "graph us:g1 {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "} "
                + "graph us:g2 {"
                + "<Jim> rdfs:label 'Jim' "
                + "}"
                + "}";

        String q = "select * "
                + "where {"
                + "?x foaf:knows ?y "
                + "values ?g { us:g1 }"
                + " "
                + "{select ?y ?l  where {  graph ?g {  ?y rdfs:label ?l }}}"
                + "}";

        exec.query(init);

        Mappings map = exec.query(q);

        assertEquals(1, map.size());
    }

    @Test
    public void testfocus() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:foo() as ?f) where {"
                + ""
                + "}"

                + "function us:foo() {"
                + "let (?g = construct {us:John rdfs:label 'John'} where {}) {"
                + "xt:focus(?g, us:bar())"
                + "}"
                + "}"

                + "function us:bar() {"
                + "let (select (count(*) as ?c) where {?x ?p ?y}) { ?c }"
                + "}";
        Mappings map = exec.query(q);
        IDatatype dt = getValue(map, "?f");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testbnode() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select * where {"
                + "values ?n { 1 2 }"
                + "bind(bnode('a') as ?b1)"
                + "bind(bnode('a') as ?b2)"
                + "bind(bnode() as ?b)"
                + "}";

        Mappings map = exec.query(q);
        for (Mapping m : map) {
            assertEquals(true, m.getValue("?b1") == m.getValue("?b2"));
            assertEquals(false, m.getValue("?b1") == m.getValue("?b3"));

        }
        // Assert.assertEquals(0, map.size());
    }

    @Test
    public void testBindIndex7() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(us:test(3, 3) as ?t) where {}"

                + "function us:test(?n, ?n)  {xt:display('here') ;"
                + "let (?n = ?n) {"
                + "for (?x in (xt:iota(?n))) {"
                + "if (?x > 1) {"
                + "return (?x)}"
                + "} "
                + "}"
                + "}";

        Mappings map = exec.query(q);
        //// System.out.println(map);
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testBindIndex6() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(us:test(3) as ?t) "
                + "where {}"

                + "function us:test(?n)  {"
                + "let (?n = ?z) {"
                + "for (?x in (xt:iota(?n))) {"
                + "if (?x > 1) {"
                + "return (?x)}"
                + "} "
                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        Assert.assertEquals(1, map.size());
    }

    @Test
    public void testBindIndex5() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test(3) as ?t) where {}"

                + "function us:test(?n)  {"
                + "let (?n = ?n) {"
                + "for (?x in (xt:iota(?n))) {"
                + "if (?x > 1) {"
                + "return (?x)}"
                + "}"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        Assert.assertEquals(2, getValue(map, "?t").intValue());
    }

    @Test
    public void testBindIndex4() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(let(?x = false, ?y = true) { ?y } as ?t1)"
                + "(if (?t1, let(?x = 2, ?y = 3) { ?y }, let(?z = 4) { ?z }) as ?t2)"
                + " where {}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        Assert.assertEquals(true, getValue(map, "?t1").booleanValue());
        Assert.assertEquals(3, getValue(map, "?t2").intValue());
    }

    @Test
    public void testBindIndex3() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test(2) as ?t) where {}"

                + "function us:test(?x) {"
                + "let ((?y, ?x) = @(1)) {"
                + "bound(?x);"

                + "let ((?x, ?y) = @(1)) {"
                + "bound(?x)"
                + "}"

                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        Assert.assertEquals(true, getValue(map, "?t").booleanValue());
    }

    @Test
    public void testBindIndex2() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test(2) as ?t) where {}"

                + "function us:test(?x) {"
                + "let ((?y, ?x) = @(1)) {"
                + "bound(?x)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        Assert.assertEquals(false, getValue(map, "?t").booleanValue());
    }

    @Test
    public void testBindIndex() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test(2) as ?t) where {}"

                + "function us:test(?x) {"
                + "let (?x = ?x * ?x) {"
                + "?x;"
                + "let (?x = ?x * ?x) {"
                + "?x}"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println(map);
        assertEquals(16, getValue(map, "?t").intValue());
    }

    @Test
    public void testrecfun() throws EngineException {
        String q = "select (us:fac(5) as ?t) where {}"
                + "function us:fac(?n) {"
                + "if (?n = 0) {"
                + " return(1) "
                + "}"
                + "else {"
                + " let (?m = ?n, ?res = ?n * us:fac(?n - 1)) { "
                + "if (?m = ?n, ?res, 0)"
                + "}"
                + "}"
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        // System.out.println(map.getQuery().getAST());
        IDatatype dt = map.getValue("?t");
        assertEquals(120, dt.intValue());
    }

    String stringValue(Mappings m, String var) {
        return (m.getValue(var)).stringValue();
    }

    @Test

    public void testExtFun21() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 . "
                + "[] rdfs:label 2 ."
                + "us:Jim rdf:value (1 (2 3) 4 ())"
                + "us:James rdf:value ()"
                + "}";

        String q = "select  (us:foo(us:bar()) as ?t1) (us:foo(xt:graph()) as ?t2) (us:gee() as ?t3)"
                + "where {}"

                + "function us:foo(?g) {"
                + "query(construct  where { ?x ?p ?y }, ?g)"
                + "}"

                + "function us:bar() {"
                + "query(construct where {?x rdf:value ?y})"
                + "}"

                + "function us:gee() {"
                + "query(select * where { ?x ?p ?y}, query (construct where {?x rdf:value ?y}))"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt1 = map.getValue("?t1");
        IDatatype dt2 = map.getValue("?t2");
        IDatatype dt3 = map.getValue("?t3");
        Graph g1 = (Graph) dt1.getPointerObject().getTripleStore();
        Graph g2 = (Graph) dt2.getPointerObject().getTripleStore();
        Mappings m = dt3.getPointerObject().getMappings();
        assertEquals(3, g1.size());
        assertEquals(16, g2.size());
        assertEquals(3, m.size());
    }

    @Test
    public void testExtFun20() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = "select (datatype(query(construct where {?x ?p ?y})) as ?g) "
                + "(datatype( let((?a) = construct where {?x ?p ?y}) {?a} ) as ?t) "
                + "(datatype(query(select * where {?x ?p ?y})) as ?s)"
                + "(datatype( let((?b) = select * where {?x ?p ?y}) {?b}) as ?m)"
                + "(datatype(xt:iota(5)) as ?l)"
                + "where {}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dg = map.getValue("?g");
        IDatatype dt = map.getValue("?t");
        IDatatype ds = map.getValue("?s");
        IDatatype dm = map.getValue("?m");
        IDatatype dl = map.getValue("?l");
        assertEquals(CoresePointer.getDatatype(PointerType.GRAPH), dg);
        assertEquals(CoresePointer.getDatatype(PointerType.TRIPLE), dt);
        assertEquals(CoresePointer.getDatatype(PointerType.MAPPINGS), ds);
        assertEquals(CoresePointer.getDatatype(PointerType.MAPPING), dm);
        assertEquals(IDatatype.LIST_DATATYPE, dl.stringValue());
    }

    @Test
    public void testExtFun16() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = " select (us:foo() as ?t) "
                + "(us:bar() as ?tt) "
                + "where {}"

                + "function us:foo() {"
                + "maplist("
                + "lambda((?x, ?p, ?y)) { xt:list(?x, ?p, ?y) }, "
                + "query(select * where {?x ?p ?y}))"
                + "}"

                + "function us:bar() {"
                + "maplist("
                + "lambda((?s, ?p, ?o)) { xt:list(?s, ?p, ?o) }, "
                + "query(construct where {?x ?p ?y}))"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        IDatatype dq = map.getValue("?tt");
        assertEquals(2, dt.size());
        assertEquals(2, dq.size());
        for (IDatatype list : dt.getValueList()) {
            assertEquals(3, list.size());
        }
        for (IDatatype list : dq.getValueList()) {
            assertEquals(3, list.size());
        }
    }

    @Test
    public void testExtFun15() throws EngineException {
        String q = "select (us:foo() as ?t) where {}"
                + "function us:foo() {"
                + "maplist(rq:funcall, xt:list(rq:year, rq:month, rq:day, rq:hours, rq:minutes, rq:seconds), now())"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(q);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(true, dt.isList());
        assertEquals(6, dt.size());
    }

    @Test
    public void testExtFun14() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = "base <http://example.org/>"
                + "select (us:bar2() as ?t) (datatype(?t) as ?d) where {}"

                + "@public function us:bar2() {"
                + "let (?f1 = rq:uri, ?f2 = rq:replace, ?f3 = rq:sha256, ?f4 = xsd:integer) {"
                + "xt:list("
                + "xt:list(funcall(?f1, 'abc'), uri('abc')),"
                + "xt:list(funcall(?f2, 'abc', 'c', 'd'), replace('abc', 'c', 'd')), "
                // + "xt:list(funcall(?f3, 'abc'), sha256('abc')),"
                + "xt:list(sha256('abc'), sha256('abc')),"
                + "xt:list(funcall(?f4, '12'), xsd:integer('12'))"
                + ") "
                + "}"
                + "}";

        String q2 = "base <http://example2.org/>"
                + "select (us:bar2() as ?t)  where {}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        exec.query(q);
        Mappings map = exec.query(q2);
        IDatatype dt = map.getValue("?t");
        // System.out.println(dt);
        for (IDatatype pair : dt.getValueList()) {
            assertEquals(true, pair.getValueList().get(0).equals(pair.getValueList().get(1)));
        }
    }

    @Test
    public void testExtFun13() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = " select (us:bar() as ?t) (datatype(?t) as ?d) where {}"

                + "@public function us:bar() {"
                + "let (?fun = rq:regex) {"
                + "(exists {?x ?p 1} && funcall(?fun, 'abc', 'a'))"
                + "}"
                + "}";

        String q2 = "select (us:bar() as ?t) (datatype(?t) as ?d) where {}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        exec.query(q);
        Mappings map = exec.query(q2);
        IDatatype dt = map.getValue("?t");
        assertEquals(true, dt.booleanValue());
    }

    @Test
    public void testExtFun11() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 ; rdfs:label 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "(sum(let (?a = ?y + 1, ?b = exists { ?z ?p ?a }){ if (?b, 1, 0) }) as ?sum)"
                + "where {"
                + "?x ?p ?y "

                + "}"

                + "function us:foo(?x) {"
                + "exists { ?x ?p ?y }"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        // System.out.println(map);
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testapply6() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());

        String q = " "
                + "select (us:foo (5) as ?t) (us:foo (10) as ?r)"
                + "where {"
                + "}"

                + "function us:foo(?n){"
                + "let (?fun = if (?n <= 5, lambda(?x) { ?x * ?x }, lambda(?x) { ?x + ?x } )) {"
                + "us:bar(?fun, ?n)"
                + "}"
                + "}"

                + "function us:bar(?fun, ?n) {"
                + "funcall(?fun, ?n)"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(25, dt.intValue());
        IDatatype dt2 = map.getValue("?r");
        assertEquals(20, dt2.intValue());
    }

    @Test
    public void testapply5() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());

        String q = "select "
                + "(funcall (let (?fun = lambda(?x) { ?x * ?x }) { ?fun }, 5) as ?t) "
                + "where {"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(25, dt.intValue());
    }

    @Test
    public void testapply44() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());

        String init = "insert data { us:John rdf:value 2 }";

        String q = "select * where {"
                + "select (us:foo() as ?t) where {"
                + "}"
                + "}"

                + "function us:foo(){"
                + "let (select * where {select "
                + "(maplist(lambda(?x) {  "
                + "exists { select * where { select * where { values ?x { UNDEF } filter exists { select * where {select * where { ?y ?p ?x }}}}}}"
                + "}, xt:iota(5)) as ?t) "
                + "where {}}) {"
                + "?t }"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        int i = 0;
        for (IDatatype val : dt.getValueList()) {
            assertEquals((i++ == 1) ? true : false, val.booleanValue());
        }
    }

    @Test
    public void testapply4() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());

        String init = "insert data { us:John rdf:value 2 }";

        String q = "select * where {"
                + "select (us:foo() as ?t) where {"
                + "}"
                + "}"

                + "function us:foo(){"
                + "let (select * where {select "
                // + "(maplist(lambda(?x) { exists { ?y ?p ?x }}, xt:iota(5)) as ?t) "
                + "(maplist(lambda(?x) {  exists { select * where { ?y ?p ?x }}}, xt:iota(5)) as ?t) "
                + "where {}}) {"
                + "?t }"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        int i = 0;
        for (IDatatype val : dt.getValueList()) {
            assertEquals((i++ == 1) ? true : false, val.booleanValue());
        }
    }

    @Test
    public void testapply3() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());

        String q = "select (us:test() as ?t) where {"
                + "}"

                + "function us:test(){"
                + "let (?funcall = rq:funcall, ?map = rq:maplist, ?apply = rq:reduce, ?plus  = rq:plus, "
                + "?funlist = @(rq:plus rq:mult)){"
                // + "funcall(?funcall, ?apply, ?plus, funcall(?map, ?apply, ?funlist,
                // xt:list(xt:iota(5))))"
                + "funcall(?funcall, ?apply, ?plus, funcall(?map, ?apply, ?funlist, xt:iota(5)))"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(135, dt.intValue());
    }

    @Test
    public void testapply22() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select * where {"
                + "values ?t { unnest(us:test()) }"
                + "}"

                + "function us:test(){"
                + "let( "
                + "select * where {"
                + "select  * (reduce(rq:plus,   xt:iota(5)) as ?sum1) "
                + "(reduce(rq:concat, xt:iota('a', 'c')) as ?con)"
                + "(reduce(rq:mult,   xt:iota(5)) as ?mul)"
                + "where {"
                + "bind  (reduce(rq:plus,   xt:iota(5)) as ?sum2) "
                + "filter (?sum2 > 1) "
                + "filter exists { "
                + "select * where { select * where { bind  (reduce(rq:plus,   xt:iota(5)) as ?test ) filter (?test = 15) }}"
                + "}"
                + "}"
                + "}"
                + ") {"
                + "xt:list(?sum1, ?mul, ?con, ?sum2)"
                + ""
                + "}"
                + "}";

        Mappings map = exec.query(q);
        //// System.out.println(map);
        assertEquals(4, map.size());
    }

    @Test

    public void test2ndOrder3() throws EngineException {
        String q = "select \n"
                + "  (us:test(lambda(?x, ?y) { ?x + ?y }, lambda(?x) { 2 * ?x }, 10) as ?t) \n"
                + "where {}\n"
                + "               \n"
                + "function us:test(?agg, ?fun, ?n){\n"
                + " let (?map = lambda(?f, ?x) { funcall(?f, ?x) } ) {\n"
                + "   reduce(?agg, maplist(?map , ?fun,   xt:iota(?n))) \n"
                + " }"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(110, dt.intValue());
    }

    @Test

    public void test2ndOrder2() throws EngineException {
        String q = "select \n"
                + "  (us:test(lambda(?list) { reduce(rq:plus, ?list) }, lambda(?x) { 2 * ?x }, 10) as ?t) \n"
                + "where {}\n"

                + "function us:test(?agg, ?fun, ?n){\n"
                + "   funcall(?agg, maplist(lambda(?f, ?x) { funcall(?f, ?x) } , ?fun,   xt:iota(?n))) \n"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(110, dt.intValue());
    }

    @Test
    public void test2ndOrder() throws EngineException {
        String q = "select (us:test() as ?t) where {}"

                + "function us:test(){\n"
                + "    funcall (lambda(?x) { 2 * ?x }, \n"
                + "        reduce(lambda(?x, ?y) { ?x + ?y }, \n"
                + "            maplist(lambda(?x) {?x * ?x}, xt:iota(5)))) \n"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(110, dt.intValue());
    }

    @Test
    public void testLetQuery2() throws EngineException {
        String init = "insert data {"
                + "us:John us:child us:Jim, us:Jane, us:Janis ."
                + "us:Jane a us:Woman ;"
                + "us:child us:Jack, us:Mary ."
                + "us:Mary a us:Woman ."
                + "us:Janis a us:Woman ;"
                + "us:child us:James, us:Sylvia ."
                + "us:Sylvia a us:Woman ."
                + "}";

        String q = "select * where {"
                + "values ?e { unnest(us:pattern()) }"
                + "}"

                + "function us:pattern(){"
                + "let (?sol = select * where { ?x us:child ?y optional { ?y a ?t } } ){"
                + "  maplist(us:collect, ?sol)     "
                + "}"
                + "}"

                + "function us:collect(?map){"
                + "let ((?y, ?t) = ?map){"
                + "xt:list(?y, coalesce(?t, us:Person))"
                + "}"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(7, map.size());
    }

    // return descendants on the woman side
    @Test
    public void testLetQuery() throws EngineException {
        String init = "insert data {"
                + "us:John us:child us:Jim, us:Jane, us:Janis ."
                + "us:Jane a us:Woman ;"
                + "us:child us:Jack, us:Mary ."
                + "us:Mary a us:Woman . "
                + "us:Janis a us:Woman ;"
                + "us:child us:James, us:Sylvia ."
                + "us:Sylvia a us:Woman ."
                + "}";

        String q = "select *  where {"
                + "values ?t { unnest(us:pattern(us:John)) } "
                + "}"

                + "function us:pattern(?x){"
                + "    let (select ?x (xt:cons(aggregate(?y), aggregate(us:pattern(?y))) as ?l) "
                + "         where { ?x us:child ?y . ?y a us:Woman }"
                + "         group by ?x){"
                + "        reduce(xt:merge, ?l)"
                + "    }"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(4, map.size());
    }

    @Test
    public void testDatatypeValue12() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"

                + "graph us:g2 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"
                + "} ";

        // String q = "delete data { <Jack> rdf:value 01 }";

        // String q2 = "delete where { <Jack> rdf:value 01 }";

        String q3 = "delete  where { ?x rdf:value '1'^^xsd:long }";

        // String q4 = "select where { <Jack> rdf:value 01 }";
        Property.set(SPARQL_COMPLIANT, !true);
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q3);
        System.out.println(map);
        // assertEquals(4, g.size());
    }

    @Test
    public void testDatatypeValue11() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"

                + "graph us:g2 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"
                + "} ";

        String q = "delete data  { <Jack> rdf:value 01 }";

        // String q2 = "delete where { <Jack> rdf:value 01 }";

        // String q3 = "delete where { ?x rdf:value 01 }";

        // String q4 = "select where { <Jack> rdf:value 01 }";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);

        exec.query(q);
        assertEquals(12, g.size());

    }

    @Test
    public void testDatatypeValue111() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"

                + "graph us:g2 { <Jack> rdf:value  1,  1.0, 01, '1'^^xsd:long, '01'^^xsd:byte, 1e0, 2 }"
                + "} ";

        // String q = "delete data { <Jack> rdf:value 01 }";

        String q2 = "delete  where { <Jack> rdf:value 01 }";

        // String q3 = "delete where { ?x rdf:value 01 }";

        // String q4 = "select where { <Jack> rdf:value 01 }";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);

        exec.query(q2);
        assertEquals(12, g.size());
    }

    @Test
    public void testDatatypeValue10() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> rdf:value  1,  1.0 }"

                + "graph us:g2 { <Jack> rdf:value  1,  1.0 }"
                + "} ";

        String q = "select * (kg:index(?v) as ?i) where {"
                + "?x ?p ?v "
                + "?y ?p ?v "
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        // System.out.println(map);
        assertEquals(4, map.size());

    }

    @Test
    public void testDatatypeValue9() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean, 'test', 'value', _:b, <uri>, _:b, <uri> . }"

                + "graph us:g2 { <Jack> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte, _:b, <uri>}"
                + "} ";

        String q = "select * (kg:index(?v1) as ?i) where {"
                + "graph ?g1 {?x ?p ?v1} "
                + "graph ?g2 {?y ?p ?v2} "
                + "filter sameTerm(?v1, ?v2)"
                + "filter (?g1 < ?g2)"
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(5, map.size());
    }

    @Test
    public void testDatatypeValue8() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean, 'test', 'value', _:b, <uri>, _:b, <uri> . }"

                + "graph us:g2 { <Jack> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte, _:b, <uri>}"
                + "} ";

        String q = "select * (kg:index(?v) as ?i) where {"
                + "graph ?g1 {?x ?p ?v} "
                + "graph ?g2 {?y ?p ?v} "
                + "filter (?g1 < ?g2)"
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(70, map.size());
    }

    @Test
    public void testDatatypeValue7() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean . }"

                + "graph us:g2 { <Jack> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte}"
                + "} ";

        String q = "select *  where {"
                + "?x ?p ?v"
                + "}"
                + "order by ?x ?v ";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(17, map.size());
    }

    @Test
    public void testDatatypeValue6() throws EngineException {
        String init = "insert data {"
                + "graph us:g1 { <Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean . }"

                + "graph us:g2 { <Jack> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte}"
                + "} ";

        String q = "select *  where {"
                + "graph ?g {"
                + "?x ?p ?v"
                + "} "
                + "}"
                + "order by ?x ?v ";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(20, map.size());
    }

    @Test
    public void testDatatypeValue5() throws EngineException {
        String init = "insert data {"
                + "<Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean ."

                + " <Jim> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte"
                + "} ";

        String q = "select *  where {"
                + "?x ?p ?v filter (?v = 1)"
                + "}"
                + "order by ?x ?v ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(15, map.size());
    }

    @Test
    public void testDatatypeValue4() throws EngineException {
        String init = "insert data {"
                + "<Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean ."

                + " <Jim> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte"
                + "} ";

        String q = "select *  where {"
                + "?x ?p ?v "
                + "}"
                + "order by ?x ?v "
                + "values ?v { 1 }";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(10, map.size());
    }

    @Test
    public void testDatatypeValue3() throws EngineException {
        String init = "insert data {"
                + "<Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean ."

                + " <Jim> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte"
                + "} ";

        String q = "select *  where {"
                + "?x ?p 1"
                + "}"
                + "order by ?x";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(10, map.size());
    }

    @Test
    public void testDatatypeValue1() throws EngineException {
        String init = "insert data {"
                + "<Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, '1.0'^^xsd:float,"
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean, false, '01'^^xsd:double, "
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte"
                + "} ";

        String q = "select *  where {"
                + " ?x ?p ?v "
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(17, map.size());
    }

    @Test
    public void testDatatypeValue2() throws EngineException {
        String init = "insert data {"
                + "<Jack> "
                + "rdf:value  "
                + " 1,  1.0,  '1'^^xsd:long, 1e0, '1'^^xsd:double, '1'^^xsd:float, "
                + "true, '1'^^xsd:boolean, false,   01, "
                + "'1'^^xsd:boolean, '0'^^xsd:boolean ."

                + " <Jim> rdf:value false, '01'^^xsd:double, '1.0'^^xsd:float,"
                + "'01'^^xsd:integer, '1'^^xsd:integer"
                + ", '1'^^xsd:short, '1'^^xsd:byte , '1'^^xsd:int, '01'^^xsd:byte, '1'^^xsd:byte"
                + "} ";

        String q = "select *  where {"
                + "?x ?p ?v "
                + "?y ?p ?v "
                + "filter (?x != ?y)"
                + "}";
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(58, map.size());
    }

    @Test
    public void testDatatype1() throws EngineException {
        String init = "insert data {"
                + "<John> foaf:age '21'^^xsd:double, 21 "
                + "<Jack> foaf:age 21e0, 21.0 "
                + "} ";

        String query = "select distinct ?a where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?a "
                + "filter (?x != ?y)"

                + "}  ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        exec.query(init);
        Mappings map = exec.query(query);
        assertEquals(4, map.size());

    }

    @Test
    public void testDatatype2() throws EngineException {
        String init = "insert data {"
                + "<John> rdf:value true "
                + "<Jack> rdf:value '1'^^xsd:boolean "
                + "} ";

        String query = "select distinct ?a where {"
                + "?x ?p ?a "
                + "?y ?p ?a "
                + "filter (?x != ?y)"

                + "}  ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        exec.query(init);
        Mappings map = exec.query(query);
        assertEquals(2, map.size());

    }

    @Test
    public void testGenAgg() throws EngineException {
        String q = "select (st:aggregate(?x) as ?list) (us:merge(maplist(us:fun, ?list)) as ?y)"
                + "where { values ?x {unnest(xt:iota(10))}}"

                + "function st:aggregate(?x){ aggregate(?x) }"

                + "function us:fun(?x){ 1 / (?x * ?x)}"

                + "function us:merge(?list){ reduce(rq:plus, ?list) }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?y");
        assertEquals("test", dt.doubleValue(), 1.5497, 10e-5);
    }

    public void testGenAggOld() throws EngineException {
        String q = "select (st:aggregate(?x) as ?y) "
                + "where { values ?x {unnest(xt:iota(10))}}"

                + "function st:aggregate(?x){aggregate(us:fun(?x), us:merge)}"

                + "function us:fun(?x){ 1 / (?x * ?x)}"

                + "function us:merge(?list){ reduce(rq:plus, ?list) }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?y");
        assertEquals("test", dt.doubleValue(), 1.5497, 10e-5);
    }

    @Test
    public void testinsertwhere() throws EngineException {
        String i = "insert { graph us:Jim {?s ?p ?o}} "
                + "where { "
                + "bind (us:Jim as ?s)"
                + "bind (us:Jim as ?p)"
                + "bind (us:Jim as ?o)"
                + "}";
        String d = "delete data { graph us:Jim { us:Jim us:Jim us:Jim }}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);

        for (Edge e : g.getEdges()) {
            int n = e.getGraph().getIndex();
            // System.out.println("s "+ e.getNode(0) + " " + e.getNode(0).getIndex());
            // System.out.println("o " +e.getNode(1) + " " + e.getNode(1).getIndex());
            // System.out.println("p " +e.getEdgeNode() + " " + e.getEdgeNode().getIndex());
            // System.out.println("g "+ e.getGraph() + " " + e.getGraph().getIndex());

            assertEquals(n, e.getNode(0).getIndex());
            assertEquals(n, e.getNode(1).getIndex());
            assertEquals(n, e.getEdge().getEdgeNode().getIndex());
        }

        exec.query(d);
        assertEquals(0, g.size());
    }

    @Test
    public void testinsertdata() throws EngineException {
        String i = "insert data { "
                + "graph us:Jim { us:Jim us:Jim us:Jim }"
                + "graph us:Jim { us:Jim us:Jim us:Jim }"
                + "}";
        String d = "delete data { graph us:Jim { us:Jim us:Jim us:Jim }}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);

        for (Edge e : g.getEdges()) {
            int n = e.getGraph().getIndex();
            assertEquals(n, e.getNode(0).getIndex());
            assertEquals(n, e.getNode(1).getIndex());
            assertEquals(n, e.getEdge().getEdgeNode().getIndex());
        }

        exec.query(d);
        assertEquals(0, g.size());
    }

    @Test
    public void testFormatBase() throws EngineException, LoadException {
        Graph g = Graph.create();
        // Access.define("junit/sttl/format1/", true);
        Access.define("junit/sttl/format2/", true);
        Access.authorize(Feature.READ_FILE);
        Transformer t = Transformer.createWE(g, data + "junit/sttl/format1/");
        String res = t.transform();
        // System.out.println("result: " + res);
        assertEquals(true, res != null && res.equals("test"));
        Access.define("junit/sttl/format2/", false);
    }

    @Test
    public void testReturn() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:fun(5) as ?n) where {}"
                + "function us:fun(?n) {"
                + "for (?x in xt:iota(?n)){"
                + "if (?x = ?n){"
                + "return (?x)}"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?n");
        assertEquals(dt.intValue(), 5);
    }

    @Test
    public void testOptionalFilter() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {"
                + "us:John foaf:knows us:James "
                + "us:James foaf:knows us:Jack, us:Jim "
                + "us:Jim foaf:knows us:John "
                + ""
                + "}";

        String q = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select  *  "
                + ""
                + "where { "
                + " ?x foaf:knows ?y "
                + "optional { "
                + "?y foaf:knows ?z filter (?x != us:John) optional { ?z ?p ?x }"
                + "}  "
                + "} ";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(map.size(), 4);

    }

    @Test
    public void testDataset2() throws EngineException, LoadException {
        Graph g = Graph.create();
        g.getDataStore().addDefaultGraph();

        Load ld = Load.create(g);
        ld.setDefaultGraph(true);

        ld.parse(data + "test/primerdata.ttl");
        int size = g.size();
        ld.parse(data + "test/primer.owl", NSManager.KGRAM + "ontology");

        String q = "select  * where {"
                + "?x ?p ?y "
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(size, map.size());
    }

    @Test
    public void testDataStore() throws EngineException {
        Graph g = Graph.create(false);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "graph us:g1 {"
                + "us:John foaf:knows us:Jack, us:Jules "
                + "foaf:knows rdfs:domain foaf:Person"
                + "} "
                + "graph us:g2 {"
                + "us:John foaf:knows us:Jack "
                + "foaf:Person rdfs:subClassOf foaf:Human "
                + "foaf:Human rdfs:subClassOf foaf:Humanoid "
                + "}"
                + "}";

        exec.query(init);
        g.init();
        ArrayList<Node> list = new ArrayList<Node>();
        ArrayList<Node> list2 = new ArrayList<Node>();

        Node g1 = g.getGraphNode(NSManager.USER + "g1");
        Node g2 = g.getGraphNode(NSManager.USER + "g2");
        list.add(g1);
        list2.add(g1);
        list2.add(g2);

        Node p = g.getPropertyNode(NSManager.FOAF + "knows");
        Node n = g.getNode(NSManager.USER + "John");
        Node n1 = g.getNode(NSManager.USER + "Jack");

        assertEquals(2, count(g.getDefault().iterate(p)));
        assertEquals(3, count(g.getNamed().iterate(p)));
        assertEquals(2, count(g.getDefault().iterate(n, 0)));
        assertEquals(3, count(g.getNamed().iterate(n, 0)));
        assertEquals(5, count(g.getDefault().iterate()));
        assertEquals(3, count(g.getDefault().from(list).iterate()));
        assertEquals(5, count(g.getDefault().from(list2).iterate()));
        assertEquals(6, count(g.getNamed().iterate()));
        assertEquals(3, count(g.getNamed().from(list).iterate()));
        assertEquals(6, count(g.getNamed().from(list2).iterate()));
        assertEquals(2, count(g.getDefault().from(list2).iterate(p, n, 0)));
        assertEquals(3, count(g.getNamed().from(list2).iterate(p, n, 0)));
        assertEquals(1, count(g.getDefault().from(list2).iterate(p, n1, 1)));
        assertEquals(2, count(g.getNamed().from(list2).iterate(p, n1, 1)));
        assertEquals(3, count(g.getNamed().minus(g1).iterate()));
        assertEquals(3, count(g.getDefault().minus(g1).iterate()));
        assertEquals(0, count(g.getDefault().minus(list2).iterate()));
        DataFilterFactory df = new DataFilterFactory();
        assertEquals(5, count(g.getDefault().iterate().filter(df.init().object(ExprType.ISURI))));
        assertEquals(0, count(g.getDefault().iterate().filter(df.init().object(ExprType.ISBLANK))));
        assertEquals(5, count(g.getDefault().iterate().filter(df.init().not().subject(ExprType.ISBLANK))));

        assertEquals(4, count(g.getNamed().iterate().filter(df.init().or()
                .and().graph(ExprType.EQ, g1).subject(ExprType.EQ, n)
                .and().graph(ExprType.EQ, g2).not().subject(ExprType.EQ, n))));

        assertEquals(5, count(g.getDefault().iterate().filter(df.init().object(ExprType.ISURI))));
        assertEquals(0, count(g.getDefault().iterate().filter(df.init().object(ExprType.ISBLANK))));
        assertEquals(5, count(g.getDefault().iterate().filter(df.init().not().subject(ExprType.ISBLANK))));

        assertEquals(0, count(g.getDefault().iterate()
                .filter(df.init().edge(ExprType.EQ, DataFilter.SUBJECT, DataFilter.OBJECT))));
    }

    int count(Iterable<Edge> it) {
        int c = 0;
        for (Edge ent : it) {
            if (ent != null) {
                //// System.out.println(ent);
                c++;
            }
        }
        //// System.out.println("");
        return c;
    }

    @Test
    public void testrepl() throws EngineException {
        String q = "select ('o' as ?pat) ('oo' as ?rep) (replace('aobooc', ?pat, ?rep) as ?res) where {}";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        assertEquals("aoobooooc", dt.getLabel());
    }

    @Test
    public void testrdfxml() throws LoadException, IOException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.parse(data + "/test/primer.owl");
        // ld.parse("/home/corby/AAServer/data/primer.owl");
        g.init();
        // String RDFXMLNEW = "/user/corby/home/AAData/template/rdfxmlnew.rul";

        Transformer t = Transformer.create(g, Transformer.RDFXML);
        // Transformer t = Transformer.create(g, RDFXMLNEW);
        t.write("/tmp/tmp.rdf");

        Graph g1 = Graph.create();
        Load ld1 = Load.create(g1);
        ld1.parse("/tmp/tmp.rdf");
        g1.init();

        Transformer t2 = Transformer.create(g1, Transformer.TURTLE);
        t2.write("/tmp/tmp.ttl");

        Graph g2 = Graph.create();
        Load ld2 = Load.create(g2);
        ld2.parse("/tmp/tmp.ttl");
        g2.init();

        // System.out.println(g.compare(g2));

        assertEquals(354, g.size());
        assertEquals(g.size(), g1.size());
        // missing: _:b a rdf:List
        assertEquals(308, g2.size());

        //// System.out.println(g.compare(g1, false, true, true));

    }

    @Test
    public void testTTLabc() throws EngineException, LoadException {

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {  "
                + "_:b rdf:value _:b, (_:b (_:b) ((_:b)))  "
                + "}";

        exec.query(i);

        Transformer t = Transformer.create(g, Transformer.TURTLE);
        System.out.println(t.transform());
        // assertEquals(197, t.transform().length());
    }

    @Test
    public void testIndex() throws EngineException {
        String q = "select * where {"
                + "bind (1 as ?x) "
                + "values (?x ?y) {"
                + "(2 2) (1 1)"
                + "}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testAGG22() throws EngineException {
        String q = "select * (max(?c) as ?mc) where {"
                + "select (count(*) as ?c) (max(?y) as ?m) where {?x ?p ?y}"
                + "}";

        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?c");
        IDatatype dt2 = map.getValue("?mc");
        assertEquals(0, dt.intValue());
        assertEquals(0, dt2.intValue());
    }

    @Test
    public void testContext2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "template { st:get(st:test) }"
                + " where {"
                + "select * where {"
                + "bind (us:test() as ?t)"
                + "}"
                + "}"
                + "function us:test(){"
                + "st:set(st:test, 10)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals("10", map.getTemplateStringResult());
        Context c = (Context) map.getContext();
        IDatatype val = c.getName("test");
        assertEquals(10, val.intValue());
        assertEquals(true, map.getQuery().getTransformer() == null);
    }

    @Test
    public void testContext() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select * (st:get(st:test) as ?tt) where {"
                + "select * where {"
                + "bind (us:test() as ?t)"
                + "}"
                + "}"
                + "function us:test(){"
                + "st:set(st:test, 10)"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?tt");
        assertEquals(10, dt.intValue());
        Context c = (Context) map.getContext();
        IDatatype val = c.getName("test");
        assertEquals(10, val.intValue());
    }

    // loop return concat of results of body of loop

    public void testGLoop() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "select (us:test() as ?t)"
                + "where {}"
                + ""
                + "function us:test(){"
                + "loop ((?s, ?p, ?o) in construct {us:John rdfs:label 'Jon', 'Jim' } where {}){"
                + "?o"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals("JonJim", dt.stringValue());
    }

    @Test
    public void testIO() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess.create(g);

        Load ld = Load.create(g);
        try {
            ld.parse(data + "junit/data/test.ttl");
            ld.parse(data + "junit/data/test.ttl", "http://example.org/");

            ld.parse(data + "junit/data/test.rdf");
            ld.parse(data + "junit/data/test.rdf", "http://example.org/");

            ld.parse(data + "junit/data/test.xml", Load.RDFXML_FORMAT);

        } catch (LoadException ex) {
        }
        assertEquals(5, g.size());
    }

    @Test
    public void testIO2() throws EngineException, IOException {
        Graph g = GraphStore.create();
        QueryProcess.create(g);

        Load ld = Load.create(g);
        try {
            ld.parseDir(data + "junit/data");
            ld.parseDir(data + "junit/data", "http://example.org/");
        } catch (LoadException ex) {
        }
        System.out.println(g.display());
        assertEquals(4, g.size());
    }

    @Test
    public void testUnion5() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "select (us:main() as ?m) where {}"

                + "function us:main(){"
                + "let (?list = xt:list(us:test1, us:test2)){"
                + "reduce(xt:union, maplist(rq:funcall, ?list))"
                + "}"
                + "}"

                + "function us:test1(){"
                + "let (?m = select *  where { ?x foaf:knows ?y}){"
                + "?m}"
                + "}"

                + "function us:test2(){"
                + "let (?m = select *  where { ?z foaf:knows ?t}){"
                + "?m}"
                + "}";

        exec1.query(i1);

        Mappings map = exec1.query(q);
        IDatatype dt = map.getValue("?m");
        Mappings m = dt.getPointerObject().getMappings();
        assertEquals(4, m.size());
    }

    @Test
    public void testUnnestSelectCons() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?x ?y ?path) {unnest(us:test())}"
                + "graph ?path { ?a ?p ?b }"
                + "}"

                + "function us:test(){"
                + "let (?m = select * where {"
                + "bind (us:graph() as ?g)"
                + "graph ?g {?x foaf:knows+ :: ?path  ?y}"
                + "})"
                + "{?m}"
                + "}"

                + "function us:graph(){"
                + "let (?g = construct  {"
                + "us:John foaf:knows us:Jack "
                + "us:Jack foaf:knows us:Jim} where {})"
                + "{?g}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(4, map.size());

    }

    @Test
    public void testUnnestGraph() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        // QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select ?s ?p ?o  where {"
                + "bind (us:test() as ?g)"
                + "values (?s ?p ?o) {unnest(?g)}"
                + "graph ?g {?s ?p ?o}"
                + "}"
                + "function us:test(){"
                + "let (?g = construct { us:John rdfs:label 'John', 'Johnny' } where {})"
                + "{?g}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(2, map.size());

    }

    @Test
    public void testUnnestSelect() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        // QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?x ?y) {unnest(us:test())}"
                + "}"
                + "function us:test(){"
                + "let (?m = select * where { "
                + "values ?x {unnest(xt:iota(2))}  "
                + "values ?z {unnest(xt:iota(1))}  "
                + "values ?y {unnest(xt:iota(2))}})"
                + "{?m}"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(4, map.size());
        IDatatype dt = map.getValue("?x");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testUnnestSelect2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        // QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?y ?x) {unnest(us:test())}"
                + "}"
                + "function us:test(){"
                + "let (?m = select * where { "
                + "values ?x {unnest(xt:iota(2))}  "
                + "values ?z {unnest(xt:iota(1))}  "
                + "values ?y {unnest(xt:iota(2))}})"
                + "{?m}"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(4, map.size());

    }

    @Test
    public void testFunUpdate() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String i = "insert { us:John foaf:name ?n } "
                + "where { bind (us:name(us:John) as ?n) }"
                + "function us:name(?n){ 'John' }";

        exec.query(i);
        Edge e = g.getEdges().iterator().next();
        IDatatype dt = e.getNode(1).getValue();
        assertEquals("John", dt.stringValue());
    }

    @Test
    public void testNSMUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "prefix ex: <htp://example.org/>"
                + "insert data { "
                + "us:John rdfs:label 'John'"
                + "ex:John rdfs:label 'John'"
                + "}";

        String q = "prefix ex: <htp://example.org/>"
                + "select * "
                + "where {"
                + "?x ?p ?y "
                + "filter mapany(us:test, ?x, st:prefix())"
                + "}"
                + "function us:test(?uri, ?def){"
                + "let ((?p, ?n) = ?def){"
                + "strstarts(?uri, ?n)"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testQueryUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess.create(g1);

        String i = "insert data { us:John rdfs:label 'John'}";

        String q = "prefix ex: <htp://example.org/>"
                + "select * "
                + "where {"
                + "?x ?p ?y optional {?x ?p ?p }"
                + "values ?t { unnest(xt:query()) }"
                + "values ?e { unnest(?t) }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(6, map.size());
    }

    @Test
    public void testGraphUnnest() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess.create(g1);

        String i = "insert data { us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2' }";

        String q = "select * where {"
                + "values (?s ?p ?o) { unnest(us:define()) }"
                + "values (?s ?p ?o ?g) { unnest(us:define()) }"
                + "?s ?p ?o "
                + "}"
                + "function us:define(){"
                + "let (?g = construct {us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2'} where {})"
                + "{ "
                + "?g }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testUnnestNSM() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <htp://example.org/>"
                + "select *  where {"
                + "values (?p ?n) {unnest(st:prefix())}"
                + "}"
                + "function us:test(){"
                + "for ((?p, ?n) in st:prefix()){"
                + "xt:display(?p, ?n)"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        // System.out.println("*****************************************");
        // System.out.println(map);
        assertEquals(2, map.size()); // there is also a global prefix c:
        // IDatatype p = map.getValue("?p");
        // IDatatype n = map.getValue("?n");
        // assertEquals("ex", p.stringValue());
        // assertEquals("htp://example.org/", n.stringValue());
    }

    @Test
    public void testExtNode() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        Graph g1 = Graph.create();
        QueryProcess.create(g1);

        String i = "insert data { us:prop rdfs:label 'prop' }";

        String q = "select * where {"
                + "bind (us:define() as ?g)"
                + "?p rdfs:label ?l "
                + "graph ?g {"
                + "?p rdfs:label ?ll"
                + "}"
                + "}"
                + "function us:define(){"
                + "let (?g = construct {us:prop1 rdfs:label 'prop' us:prop rdfs:label 'prop2'} where {})"
                + "{ ?g }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testUnion4() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let ("
                + "?m1 = select *  where { ?x foaf:knows ?y},"
                + "?m2 = select *  where { ?x foaf:knows ?y}){"
                + "xt:union(?m1, ?m2)"
                + "}"
                + "}";

        exec1.query(i1);

        Mappings dt = exec1.query(q);
        assertEquals(4, dt.get(0).getValue("?_main_").size());
    }

    @Test
    public void testUnion3() throws EngineException {
        Graph g1 = Graph.create();
        QueryProcess exec1 = QueryProcess.create(g1);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "select * where {"
                + "?x foaf:knows ?y"
                + "}";

        exec1.query(i1);

        Mappings m1 = exec1.query(q);
        Mappings m2 = exec1.query(q);
        Mappings m = m1.union(m2);

        assertEquals(4, m.size());

    }

    @Test
    public void testUnion2() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let (?g = us:union(),"
                + "?m = select * where { graph ?g {?x foaf:knows+ ?y}})"
                + "{ ?m }"
                + "}"
                + "function us:union(){"
                + "let ("
                + "?g1 = construct {us:John foaf:knows us:Jim}  where {},"
                + "?g2 = construct {us:Jim  foaf:knows us:Jack} where {}){"
                + "xt:union(?g1, ?g2)"
                + "}"
                + "}";

        Mappings dt = exec.query(q);
        assertEquals(3, dt.get(0).getValue("?_main_").size());
    }

    @Test
    public void testUnion() throws EngineException {
        Graph g1 = Graph.create();
        Graph g2 = Graph.create();

        QueryProcess exec1 = QueryProcess.create(g1);
        QueryProcess exec2 = QueryProcess.create(g2);

        String i1 = "insert data {"
                + "us:John foaf:knows us:Jim "
                + "}";

        String i2 = "insert data {"
                + "us:Jim foaf:knows us:Jack "
                + "}";

        String q = "select * where {"
                + "?x foaf:knows+ ?y"
                + "}";

        exec1.query(i1);
        exec2.query(i2);

        Graph g = g1.union(g2);

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);

        assertEquals(3, map.size());

    }

    @Test
    public void testRelax6() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John'@en ; foaf:age 11 "
                + "}";
        exec.query(i);

        String q = "@relax "
                + "select * (xt:sim() as ?s) where {"
                + "us:John foaf:name 'Jon' ; foaf:age 11 "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testRelax5() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:uri "
                + "select * where {"
                + "us:Jim foaf:name 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testRelax4() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax * "
                + "select * where {"
                + "?x rdfs:label 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testRelax3() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John foaf:name 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:property "
                + "select * where {"
                + "?x rdfs:label 'John' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testRelax2() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John rdfs:label 'John' "
                + "}";
        exec.query(i);

        String q = "@relax kg:literal "
                + "select * where {"
                + "?x rdfs:label 'Jon' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testRelax1() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { "
                + "us:John rdfs:label 'John' "
                + "}";
        exec.query(i);

        String q = "@relax "
                + "select * where {"
                + "?x rdfs:label 'Jon' "
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testApprox() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data {"
                + "us:John us:name 'John' "
                + "us:Jack us:name 'Jack' "
                + "us:Jim  us:name 'Jim' "
                + "}";

        String q = "@relax * "
                + "select * (xt:sim() as ?s) where {"
                + "?x xt:name 'Jon'"
                + "}"
                + "order by desc(?s)";

        exec.query(i);

        Mappings map = exec.query(q);
        assertEquals(3, map.size());
        IDatatype dt = map.getValue("?x");
        assertEquals(NSManager.USER + "John", dt.stringValue());
    }

    @Test
    public void testSubqueryFun() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String i = "insert data { us:John rdfs:label 'John' }"
                + "@public  {"
                + "function us:test(){"
                + "let ((?m) = select * where {?x ?p ?y}, (?y)=?m"
                + "){?y}"
                + "}"
                + "}";

        String q = "select * (us:test() as ?r) (us:foo() as ?s) "
                + "where {"
                + "select * where {select * (us:test() as ?t) (us:foo() as ?f) "
                + "where {}}"
                + "}"
                + "function us:foo(){"
                + "let ((?m) = select * where {?a ?q ?b}, (?b)=?m){ us:test() }"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);

        assertEquals("John", strValue(map, "?r"));
        assertEquals("John", strValue(map, "?s"));
        assertEquals("John", strValue(map, "?t"));
        assertEquals("John", strValue(map, "?f"));

    }

    String strValue(Mappings m, String v) {
        return (m.get(0).getValue(v)).stringValue();
    }

    @Test
    public void testImport() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <http://ns.inria.fr/sparql-extension/aggregate#>"
                + "select * "
                + "(funcall(ex:median, xt:iota(5)) as ?m)"
                + "(ex:sigma(xt:iota(5)) as ?s)"
                + ""
                + "where {}";
        // exec.setLinkedFunction(true);
        Access.setLinkedFunction(true);
        Mappings map = exec.query(q);
        IDatatype dm = map.getValue("?m");
        IDatatype ds = map.getValue("?s");
        assertEquals(3, dm.intValue());
        assertEquals(1.41421, ds.doubleValue(), 0.01);
        // 1.41421
    }

    @Test
    public void testAnnot() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String i = "@public {"
                + "function us:foo(){"
                + "us:bar()"
                + "}"
                + " function us:bar(){"
                + "10"
                + "}"
                + "}";

        String q = "function xt:main(){"
                + "us:foo()"
                + "}";

        exec.query(i);

        Mappings dt = exec.query(q);
        assertEquals(10, dt.get(0).getValue("?_main_").intValue());
    }

    @Test
    public void testSPARQLfun() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(rq:isURI(us:test) as ?uri)"
                + "(rq:isBlank(us:test) as ?bn)"
                + "(mapany(rq:strstarts, us:test, xt:list(xt:, st:, us:)) as ?st)"
                + "where {}";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?uri");
        IDatatype bn = map.getValue("?bn");
        IDatatype st = map.getValue("?st");
        assertEquals(true, dt.booleanValue());
        assertEquals(false, bn.booleanValue());
        assertEquals(true, st.booleanValue());
    }

    @Test
    public void testUnbound() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test() as ?res)"
                + "where {}"
                + "function us:test(){"
                + "let ((?m) = select * where { optional { ?x rdf:value ?y }}, (?x)=?m){"
                + "if (bound(?x), true, false)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        assertEquals(false, dt.booleanValue());
        //// System.out.println(map);
    }

    // @Test
    public void testLetService() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        String q = "prefix r: <http://fr.dbpedia.org/resource/>"
                + "select (us:dbpedia(?x, rdfs:label) as ?t)"
                + "where {"
                + "}"
                + "values ?x {r:Auguste}"
                + "function us:dbpedia(?x, ?p) {"
                + "  let((?l) ="
                + "    service <http://fr.dbpedia.org/sparql> {"
                + "	?x ?p ?l"
                + "      })"
                + "  {?l}"
                + "}";

        Mappings map = exec.query(q);
        for (Mapping m : map) {
            IDatatype dt = m.getValue("?t");
            assertEquals("Auguste", dt.stringValue());
        }

    }

    @Test
    public void testFuncall() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String q = "function xt:main(){"
                + "let (?fun = us:test){"
                + "funcall(?fun, 'Hello')}"
                + "}"
                + "function us:test(?m){"
                + "?m"
                + "}";

        Mappings dt = exec.query(q);
        assertEquals("Hello", dt.get(0).getValue("?_main_").stringValue());

    }

    @Test
    public void testMethod() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "foaf:Man rdfs:subClassOf foaf:Person "
                + "us:John a foaf:Man "
                + "us:Boat a foaf:Thing "
                + "us:bar a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Person) ;"
                + "xt:output xsd:string ."
                + ""
                + "us:foo a xt:Method ;"
                + "xt:name us:title ;"
                + "xt:input (foaf:Thing) ;"
                + "xt:output xsd:string ."
                + "}";

        String q = "select (funcall(us:method(us:title, ?x), ?x) as ?t) where {"
                + "?x a foaf:Man"
                + "}"

                + "function us:method(?m, ?x){"
                + "let ((?res) = select * where {"
                + "?x rdf:type/rdfs:subClassOf* ?t . "
                + "?fun a xt:Method ; xt:name ?m ; xt:input(?t)},  (?fun)=?res )"
                + "{ ?fun }"
                + "}"

                + "function us:bar(?x){"
                + "'bar'"
                + "}"

                + "function us:foo(?x){"
                + "'foo'"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        //// System.out.println(map);
        IDatatype dt = map.getValue(("?t"));
        assertEquals("bar", dt.stringValue());
    }

    @Test
    public void testMain() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String i = "insert data {"
                + "[] a us:Test ; us:width 2 ; us:length 3 "
                + "}";

        String q = "function xt:main(){"
                + "us:test()"
                + "}"
                + "function us:test(){"
                + "let (select ?x (us:surface(?x) as ?s) where {?x a ?t} ) {"
                // + "kg:display(?x); kg:display(?s);"
                + "?s"
                + "}}"
                + "function us:surface(?x){"
                + "let (select * where {?x us:length ?l ; us:width ?w }){"
                + "?l * ?w}"
                + "}";

        exec.query(i);
        gs.init();
        // Mappings map = exec.query(q);
        // ////System.out.println(map);
        // Mappings m = (Mappings) map.getNodeObject(ASTQuery.MAIN_VAR);
        // IDatatype dt = m.getValue("?s");

        Mappings dt = exec.query(q);
        // System.out.println(dt);
        assertEquals(6, dt.get(0).getValue("?_main_").intValue());
    }

    @Test
    public void testexistsexport() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "[] rdf:value 1"
                + "}";

        String qe = "select where {}"
                + "@public {"
                + "function us:test(){"
                + "if (exists {select * where {?x ?p ?y}}){"
                + "let ((?m) = select * where {?x ?p ?y}, (?x, ?y)=?m){"
                + "?y}"
                + "}"
                + "else {us:fun(false)}"
                + "}"
                + "function us:fun(?x){"
                + "?x}"
                + "}";

        String q = "select (us:test() as ?t)"
                + "where {}";

        exec.compile(qe);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?t");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testexists() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String i = "insert data {"
                + "[] us:width 2 ; us:length 3"
                + "[] us:width 3 ; us:length 4"
                + "}";

        String q = " select * (us:surface(?x) as ?s) where {"
                + "?x us:width ?w "
                + "}"
                + "function us:surface(?x){"
                + "let ((?m) = select * where { ?x us:length ?l ; us:width ?w }, (?l, ?w)=?m){"
                + "?l * ?w"
                + "}"
                + "}";

        String q2 = "select (us:test() as ?s) where {}"
                + "function us:test(){"
                + "let (?sum = 0){"
                + "for (?m in select * where { ?x us:width ?w }){"
                + "let ((?w, ?x) = ?m){  "
                + "set (?sum = ?sum + ?w)"
                + "}}; ?sum"
                + "}"
                + "}";

        exec.query(i);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?s");
        assertEquals(6, dt.intValue());

        map = exec.query(q2);
        dt = map.getValue("?s");
        assertEquals(5, dt.intValue());
    }

    public void testBnode()
            throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "[] ex:speed [ rdf:value 100 ; ex:unit 'km/h' ]"
                + "[] ex:speed [ rdf:value 90 ; ex:unit 'km/h' ]"
                + ""
                + ""
                + "}";

        String q = "prefix bn: <http://ns.inria.fr/sparql-extension/bnode#>"
                + "prefix ex: <http://example.org/>"
                + "select  ?x ?z where {"
                + "?x ex:speed ?y "
                + "?z ex:speed ?t "
                + "filter (?x != ?z) "
                + "filter (?y <= ?t) "
                + "}"
                + "function bn:lessEqual(?x, ?y){"
                + "let (?v1 = xt:value(?x, rdf:value), ?v2 = xt:value(?y, rdf:value)){"
                + "?v1 <= ?v2}"
                + "} ";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());
    }

    @Test
    public void testSet() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select (us:test() as ?t) where {}"
                + "function us:test(){"
                + "let (?sum = 0){ "
                + "for (?x in xt:iota(100)){"
                + "if (xt:prime(?x)){"
                + "set(?sum = ?sum + 1)"
                + "}};"
                + "?sum"
                + "}"
                + "}";

        Mappings map = exec.query(q);
        //// System.out.println(map);
        IDatatype dt = map.getValue("?t");
        assertEquals(25, dt.intValue());

    }

    @Test
    public void testCandidate() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select * where {"
                + "bind (unnest(xt:iota(3)) as ?n)"
                + "}"
                + "function xt:solution(?q, ?ms){"
                + "map (kg:display, ?ms)"
                + "}";

        Mappings map = exec.query(q);

        assertEquals(3, map.size());
    }

    // @Test
    public void testSolution() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select * where {"
                + "bind (unnest(xt:iota(100)) as ?n)"
                + "}"
                + "function xt:solution(?q, ?ms){"
                + "for (?m in ?ms){"
                + "if (! us:check(?m)){"
                + "xt:reject(?m)}"
                + "}"
                + "}"
                + "function us:check(?m){"
                + "rand() <= 0.5"
                + "}";

        Mappings map = exec.query(q);
        //// System.out.println(map.size());
        assertEquals(true, map.size() < 75);
    }

    @Test
    public void testCustomAgg() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "[] rdf:value 4, 5, 6, 7"
                + "[] rdf:value 1, 2, 3"
                + "}";

        String q = "select (aggregate(?v) as ?list) ( us:mediane(?list) as ?res)"
                + "where {"
                + "  ?x rdf:value ?v "
                + "}"
                + ""
                + "function us:mediane(?list){"
                + "  let (?l = xt:sort(?list)){"
                + "    xt:get(?l, xsd:integer((xt:size(?l) - 1) / 2))"
                + "  }"
                + "}"
                + "";

        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        assertEquals(4, dt.intValue());

    }

    // @Test
    public void testVD13() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                + "select ?day  where {"
                + "bind (unnest(mapmerge(xt:span, mapfindlist(xt:prime, xt:iota(1901, 2000)))) as ?day)"
                + "}"
                + "function xt:span(?y) { "
                + "mapselect (xt:check, \"Friday\", maplist(cal:date, ?y, xt:iota(1, 12), 13)) "
                + "}"
                + "function xt:check(?d, ?x) { (xt:day(?x) = ?d) }";

        Mappings map = exec.query(q);
        assertEquals(23, map.size());

    }

    @Test
    public void testMapList() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        // String q = "select * where {"
        // + "bind (maplist (rq:plus, xt:iota(1, 10), xt:iota(1, 10)) as ?res)"
        // + "}"
        // + "";

        String q1 = "select * where {"
                + "bind (maplist (rq:plus, xt:iota(1, 10), 10) as ?res)"
                + "}"
                + "";

        String q2 = "select * where {"
                + "bind (maplist (rq:plus, 10, xt:iota(1, 10)) as ?res)"
                + "}"
                + "";

        // String q3 = "select * where {"
        // + "bind (maplist (rq:plus, xt:iota(1, 20), xt:iota(1, 10)) as ?res)"
        // + "}"
        // + "";

        Mappings map;
        IDatatype dt;
        // map = exec.query(q);
        // dt = map.getValue("?res");
        // assertEquals(10, dt.size());
        // assertEquals(20, dt.get(dt.size() - 1).intValue());

        map = exec.query(q1);
        dt = map.getValue("?res");
        assertEquals(10, dt.size());
        assertEquals(20, dt.get(dt.size() - 1).intValue());

        map = exec.query(q2);
        dt = map.getValue("?res");
        assertEquals(10, dt.size());
        assertEquals(20, dt.get(dt.size() - 1).intValue());

        // map = exec.query(q3);
        // dt = map.getValue("?res");
        // assertEquals(20, dt.size());
        // assertEquals(30, dt.get(dt.size() - 1).intValue());
    }

    @Test
    public void testExtList() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select ?n  "
                + "where {"
                + "bind (unnest(xt:iota(1, 100)) as ?n)"
                + "filter  xt:prime(?n)"
                + "}";

        String q2 = "select * where {"
                + "bind (mapfindlist (xt:prime, xt:iota(100)) as ?test)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(25, map.size());

        map = exec.query(q2);
        IDatatype dt = map.getValue("?test");
        assertEquals(25, dt.size());

    }

    // @Test
    public void testAgenda() throws EngineException {
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "/query2/agenda.rq");
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        assertEquals(1637, map.getTemplateStringResult().length());

    }

    @Test
    public void testExtFun10() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2, 3 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select  * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter xt:test(?n)"
                + "filter xt:fun()"
                + "}"
                + "function xt:test(?n) { "
                + "if (?n = 1, "
                + "xt:test(?n + 1),"
                + "let (?m = ?n + 1){ exists { ?x rdf:value ?m }}"
                + ") "
                + "}"
                + "function xt:fun() { exists {?n rdf:value ?x} }";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testExtFun9() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select *"
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y rdf:value ?n "
                + "filter (let (?z = 3){ exists { ?t ?p ?z}}) } "
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(0, map.size());
    }

    @Test
    public void testExtFun8() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { select { ?y ?p ?n  filter ( xt:fun(?n)) } } "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?x ?q ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //// System.out.println(map);
    }

    @Test
    public void testExtFun7() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { "
                + "if (exists { select ?x where { ?x ?p ?n filter (?n < 10)} }, 1, 0) "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(2, dt.intValue());
    }

    @Test
    public void testExtFun6() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { "
                + "if (exists { select ?n where { ?x ?p ?n filter (?n < 10)} }, 1, 0) "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testExtFun3() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y ?p ?n filter (! xt:fun(?n)) } "
                + "}"
                + " function xt:fun(?n) { "
                + "exists { ?n ?q ?x } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //// System.out.println(map);
    }

    @Test
    public void testExtFun2() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter exists { ?y ?p ?n filter xt:fun(?n) } "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?z ?q ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
        //// System.out.println(map);
    }

    @Test
    public void testExtFun1() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select * "
                + "where {"
                + "?x rdf:value ?n "
                + "filter xt:fun(?n) "
                + "}"
                + "function xt:fun(?n) { "
                + "exists { ?y ?p ?n } "
                + "} ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }

    @Test
    public void testExtFun5() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n + 1)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { if (exists { ?x ?p ?n }, 1, 0) } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(1, dt.intValue());
    }

    @Test
    public void testExtFun4() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?n) { if (exists { ?x ?p ?n }, 1, 0) } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(2, dt.intValue());
    }

    @Test
    public void testExtAgg() throws EngineException {
        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1, 2 "
                + "}";

        String q = "prefix ex: <http://example.org/> "
                + "select (sum(xt:fun(?n)) as ?sum)"
                + "where {"
                + "?x rdf:value ?n "
                + "}"
                + "function xt:fun(?x)  { ?x + ?x } ";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?sum");
        assertEquals(6, dt.intValue());
    }

    @Test
    /**
     * Test the occurrence of a recursive graph pattern that appears at least ?t
     * times
     */
    public void testExistFunRec() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:a a ex:Case "
                + "ex:a ex:p ex:b "
                + "ex:b ex:q ex:c "
                + "ex:c ex:r ex:a "
                + ""
                + "ex:c a ex:Case "
                + "ex:c ex:p ex:d "
                + "ex:d ex:q ex:e "
                + "ex:e ex:r ex:c "
                + "ex:e a ex:Case "
                + "ex:e ex:p ex:f "
                + "ex:f ex:q ex:g "
                + "ex:g ex:r ex:e "
                + "}";

        String q = "prefix ex: <http://example.org/>"
                + "select *"
                + "where {"
                + "?x a ex:Case "
                + "filter xt:test(?x, 2, 0)"
                + "}"
                + "function xt:test(?x, ?n, ?m) {"
                + "if (?m >= ?n, true,"
                + "exists { ?x ex:p ?y . ?y ex:q ?z . ?z ex:r ?x "
                + "filter xt:test(?z, ?n, ?m + 1) }"
                + ") }";

        exec.query(init);
        exec.query(q);
        // assertEquals(2, map.size());

    }

    public void testGeneralize()
            throws EngineException, LoadException, ParserConfigurationException, SAXException, IOException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix ex: <http://example.org/>"
                + "insert data { "
                + "ex:John a ex:OldMan ;"
                + "ex:author [ a ex:Document ]"
                + "ex:Jack a ex:Person ;"
                + "ex:author [ a ex:Document ]"
                + "ex:Man       rdfs:subClassOf ex:Human "
                + "ex:YoungMan  rdfs:subClassOf ex:Man "
                + "ex:OldMan    rdfs:subClassOf ex:Man "
                + "ex:Report    rdfs:subClassOf ex:Document  "
                + "}";

        // target type more general than query
        // target type brother of query
        String qq = "prefix ex: <http://example.org/>"
                + "select * (kg:similarity() as ?sim) "
                + "where {"
                + "?x a ex:YoungMan, ?tt ;"
                + "ex:author [ a ex:Report ] "
                + ""
                + "}"
                + "function xt:candidate(?q, ?t, ?b) { "
                + "let ((?qs, ?qp, ?qo) = ?q, "
                + "     (?ts, ?tp, ?to) = ?t) {"
                + "if (?qp = rdf:type && isURI(?qo), "
                + "?b || exists { ?qo rdfs:subClassOf/(rdfs:subClassOf*|^rdfs:subClassOf) ?to } ,"
                + "?b)"
                + "}"
                + "}";

        exec.query(init);
        Mappings map = exec.query(qq);
        //// System.out.println(map);
        //// System.out.println(map.size());
        assertEquals(1, map.size());
    }

    @Test
    public void testNoGlobal() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { "
                + "<John> rdf:value 1, 2  ."
                + ""
                + "}";

        String qq = "select *"
                + "where {"
                + "bind (xt:test(?x) as ?z)"
                + "?x ?p ?y "
                + "}"
                + "function xt:test(?x) { ?y } ";

        exec.query(init);
        Mappings map = exec.query(qq);
        Node n = map.getNode("?z");
        assertEquals(null, n);
    }

    @Test
    public void testFunExist() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3 .}";
        String q = "select "
                + "(xt:test(?n) as ?r) "
                + "(xt:test(4) as ?b)"
                + "where {"
                + "?x rdf:value ?n "
                + "filter (xt:test(?n))}"
                + "function xt:test(?y) {  exists {?z rdf:value ?y} }";

        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());

        for (Mapping m : map) {
            IDatatype dt = m.getValue("?r");
            assertEquals(true, dt.booleanValue());
            IDatatype dtf = m.getValue("?b");
            assertEquals(false, dtf.booleanValue());
        }
    }

    @Test
    public void testSPQR() throws EngineException {
        Graph g = createGraph();
        // QueryLoad ql = QueryLoad.create();
        // String q = ql.read(data + "query/spqr.rq");
        QueryProcess exec = QueryProcess.create(g);
        // exec.query(q);

        String query = "prefix cal: <http://ns.inria.fr/sparql-extension/spqr/>\n"
                + "select \n"
                + "(99 as ?n)\n"
                + "(cal:romain(?n) as ?r)\n"
                + "(cal:digit(?r)  as ?d)"
                + "where {}";

        Mappings map = exec.query(query);

        IDatatype dtr = map.getValue("?r");
        IDatatype dtn = map.getValue("?d");

        assertEquals("XCIX", dtr.stringValue());
        assertEquals(99, dtn.intValue());

    }

    @Test
    public void testFunfdghf() throws EngineException {
        String q = "select \n"
                + "(xt:f(1) as ?x)\n"
                + "(xt:f(1, 2) as ?y)\n"
                + "\n"
                + "where {}"
                + "function xt:f(?x) { ?x }"
                + "function xt:f(?x, ?y) { ?x + ?y }"
                + "function xt:f(?x, ?y, ?z) { ?x + ?y + ?z }";
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt1 = map.getValue("?x");
        IDatatype dt2 = map.getValue("?y");
        assertEquals(1, dt1.intValue());
        assertEquals(3, dt2.intValue());
    }

    @Test
    public void testFuture() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 .}";
        String q = "template {"
                + "st:number() ' : ' ?y}"
                + "where {"
                + "?x rdf:value ?y "
                + "} order by desc(?y)";

        exec.query(init);
        Mappings map = exec.query(q);
        String str = map.getTemplateStringResult();
        assertEquals(true, str.contains("8 : 1"));

    }

    public void testCal2() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryLoad ql = QueryLoad.create();
        String q = ql.read(data + "query/cal.rq");
        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?fr");
        assertEquals("Vendredi", dt.stringValue());
        //// System.out.println(Interpreter.getExtension());
        String qq = "prefix cal: <http://ns.inria.fr/sparql-extension/calendar/>"
                + "select *"
                + "where {"
                + "?x ?p ?y "
                + "filter (cal:jour(?y) = 'Mardi' )"
                + "}";

        String init = "insert data { "
                + "<Day1> rdf:value '2015-06-16'^^xsd:date ."
                + "<Day2> rdf:value '2015-06-17'^^xsd:date ."
                + "<Day3> rdf:value '2015-06-23'^^xsd:date ."
                + "}";
        exec.query(init);

        Mappings m = exec.query(qq);
        assertEquals(2, m.size());

    }

    @Test
    public void testBBB() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { "
                + "<John> rdf:value 5 ."
                + "<Jim>  rdf:value 30}";
        String q = "select * "
                + "where {"
                + "?x rdf:value ?y "
                + "filter (xt:foo(?x, 10) || xt:foo(?x, 5))"
                + "} "
                + "function xt:foo(?x, ?n) { exists {?x rdf:value ?n} }";
        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals(1, map.size());

    }

    @Test
    public void testreduce() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        String q = "select "
                + "(reduce(rq:concat, xt:iota('a', 'c')) as ?con)"
                + "(reduce(rq:plus,   xt:iota(5)) as ?sum)"
                + "(reduce(rq:mult,   xt:iota(5)) as ?mul)"
                + "where {}";
        Mappings map = exec.query(q);
        IDatatype dt1 = map.getValue("?con");
        IDatatype dt2 = map.getValue("?sum");
        IDatatype dt3 = map.getValue("?mul");
        assertEquals("abc", dt1.stringValue());
        assertEquals(15, dt2.intValue());
        assertEquals(120, dt3.intValue());
    }

    @Test
    public void testEEE2() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "select "
                + "(reduce  (rq:plus, maplist (xt:fun, xt:iota(0, 12))) as ?res)"
                + "where {}"
                + "function xt:fac(?n) { if (?n = 0, 1, ?n *  xt:fac(?n - 1)) }"
                + "function xt:fun(?x) { 1.0 / xt:fac(?x) }";

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        assertEquals(2.71828, dt.doubleValue(), 0.0001);

    }

    @Test
    public void testEEE() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "insert data { <John> rdf:value 1, 2, 3, 4, 5, 6, 7, 8 .}";
        String q = "select * "
                + "(1 + sum(xt:foo(xsd:long(?n))) as ?res)"
                + "where {"
                + "?x rdf:value ?n"
                + "}"
                + "function xt:fac(?n) { if (?n = 0, 1, ?n * xt:fac(?n - 1)) }"
                + "function xt:foo(?n) { 1 / xt:fac(?n) }";
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        assertEquals(2.71828, dt.doubleValue(), 0.0001);

    }

    @Test
    public void testExtFun() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String query = "select "
                + "( st:test(st:fac(?x)) as ?r)"
                + "where {"
                + "bind (5 as ?x)"
                + "}"
                + "function st:fac(?x)  { if (?x = 1, 1, ?x * st:fac(?x - 1)) }"
                + "function st:test(?x) { let(?y = ?x * ?x){ ?y} }";

        String query2 = "select "
                + "(st:test(st:fac(?x)) as ?r)"
                + "where {"
                + "bind (5 as ?x)}"
                + "function st:fac(?x)   { if (?x = 1, 1, ?x * st:fac(?x - 1)) } "
                + "function st:test(?x) { let(?y = ?x * ?x){ ?y} } ";

        Mappings map = exec.query(query);

        IDatatype dt = map.getValue("?r");

        assertEquals(14400, dt.intValue());

        map = exec.query(query2);

        dt = map.getValue("?r");

        assertEquals(14400, dt.intValue());

    }

    @Test
    public void myastpp() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);

        String q = "select  * "
                + "where {"
                + "?x foaf:knows ?y "
                + "graph st:sys {"
                + "?y foaf:knows ?z, ?t "
                + "filter  exists { ?u rdfs:label 'toto' }"
                + "filter not exists { ?u rdfs:label 'tata' }"
                + "}"
                + "graph st:test { "
                + "?x rdfs:label ?n "
                + "filter exists { ?a rdfs:label 'John' }"
                + "filter not exists { ?u rdfs:label 'tata' }"
                + "}"
                + "}"

        ;

        Mappings map = exec.query(q);

        assertEquals(5, map.size());

    }

    @Test
    public void myastpp2() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);

        String q = "template {"
                + "str(?res)"
                + "}"
                + "where {"
                + "graph st:sys {"
                + "bind (st:atw(st:turtle) as ?res)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        //// System.out.println(map.getTemplateStringResult());
        assertEquals(258, map.getTemplateStringResult().length());

    }

    public void myastpp3() throws LoadException, EngineException {
        GraphStore graph = GraphStore.create();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jim>, <Jack> "
                + "graph st:test "
                + "{"
                + "<John> rdfs:label 'John' "
                + ""
                + "}"
                + "}";

        exec.query(init);

        Graph g = GraphStore.create();
        QueryProcess exec2 = QueryProcess.create(g);

        String init2 = "insert data {"
                + "<Jim>  foaf:knows <Jack>, <James> "
                + "<Jack> foaf:knows <Jesse>"
                + "<John> rdfs:label 'toto'"
                + "}";

        exec2.query(init2);

        graph.setNamedGraph(NSManager.STL + "sys", g);

        String q = "template {"
                + "str(?res)"
                + "}"
                + "where {"
                + "graph st:sys {"
                + "bind (st:atw('" + data + "template/test') as ?res)"
                + "}"
                + "}";
        Mappings map = exec.query(q);
        ////// System.out.println(map);
        assertEquals(map.getTemplateStringResult().length(), 0);

    }

    Graph createGraph() {
        Graph g = Graph.create();
        Parameters p = Parameters.create();
        p.add(Parameters.type.MAX_LIT_LEN, 2);
        g.setStorage(IStorage.STORAGE_FILE, p);
        return g;
    }

    @Test
    public void testOWLRL() throws EngineException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.parse(data + "template/owl/data/primer.owl");
        Transformer t = Transformer.create(gs, Transformer.OWLRL);
        IDatatype dt = t.process();

        assertEquals(DatatypeMap.FALSE, dt);
    }

    // @Test
    // public void testTCgg () throws EngineException{
    // GraphStore gs = GraphStore.create();
    // QueryProcess exec = QueryProcess.create(gs);
    // Load ld = Load.create(gs);
    // ld.load(data + "template/owl/data/success.ttl");
    // Transformer t = Transformer.create(gs, Transformer.OWLRL);
    // IDatatype dt = t.process();
    //
    // assertEquals(DatatypeMap.TRUE, dt);
    // }
    @Test
    public void testgraph() throws EngineException {
        Graph gs = createGraph();

        Node g = gs.addGraph(FOAF + "gg");
        Node s = gs.addResource(FOAF + "John");
        Node p = gs.addProperty(FOAF + "name");
        Node o = gs.addLiteral("John");

        Node b = gs.addBlank();
        Node q = gs.addProperty(FOAF + "knows");
        Node l = gs.addLiteral("Jack");

        Node gg = gs.createNode(DatatypeMap.newResource(FOAF, "gg"));
        Node ss = gs.createNode(DatatypeMap.newResource(FOAF, "Jim"));
        Node pp = gs.createNode(DatatypeMap.newResource(FOAF, "age"));
        Node oo = gs.createNode(DatatypeMap.newInstance(10));

        IDatatype g2 = DatatypeMap.newResource(FOAF, "gg");
        IDatatype s2 = DatatypeMap.newResource(FOAF, "James");
        IDatatype p2 = DatatypeMap.newResource(FOAF, "age");
        IDatatype o2 = DatatypeMap.newInstance(10);

        gs.addEdge(g, s, p, o);
        gs.addEdge(g, s, q, b);
        gs.addEdge(g, b, p, l);
        gs.add(gg, ss, pp, oo);
        gs.insert(g2, s2, p2, o2);

        QueryProcess exec = QueryProcess.create(gs);

        String str = "select * where  { ?x ?p ?y . ?y ?q ?z }";

        Mappings m1 = exec.query(str);
        assertEquals(1, m1.size());

        String q2 = FOAF_PREF
                + "select * where {"
                + "?x foaf:age ?y"
                + "}";

        Mappings m2 = exec.query(q2);
        assertEquals(2, m2.size());

        String q3 = FOAF_PREF
                + "select * where {"
                + "?x foaf:pp* ?y"
                + "}";

        Mappings m3 = exec.query(q3);
        //// System.out.println(m3);
        assertEquals(7, m3.size());

    }

    // @Test
    public void testeng() throws EngineException, LoadException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        Load ld = Load.create(gs);
        ld.parse(data + "template/owl/data/primer.owl");

        String q = "select * where {"
                + "graph eng:describe {"
                + "[] kg:index 0 ; kg:item [ rdf:predicate ?p ; rdf:value ?v ] "
                + "}"
                + "filter exists { ?x ?p ?y }"
                + "}";

        Mappings map = exec.query(q);

        assertEquals(56, map.size());

        // query the SPIN graph of previous query
        q = "select * where {"
                + "graph eng:query {"
                + "[] sp:predicate ?p "
                + "values ?p { rdf:predicate rdf:value} "
                + "}"
                + "}";

        map = exec.query(q);

        assertEquals(2, map.size());
    }

    public void testTr() throws EngineException {
        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "prefix ex: <http://example.org/>"
                + "insert data {"
                + "ex:C4 owl:unionOf (ex:C5 ex:C6) "
                + "ex:C0 owl:unionOf (ex:C2 ex:C3) "
                + "ex:C1 owl:unionOf (ex:C2 ex:C3) "
                + "}";

        exec.query(init);

        String q = "select *"
                + "  where { "
                + "    ?x owl:unionOf (?c1 ?c2)  ;"
                + "       owl:unionOf ?l"
                + "  }"
                + "group by (st:apply-templates-with(st:hash2, ?l) as ?exp)";

        Mappings map = exec.query(q);

        assertEquals(2, map.size());

    }

    @Test
    public void testExists() throws EngineException {

        Graph g1 = createGraph();
        QueryProcess exec = QueryProcess.create(g1);
        Graph g2 = createGraph();
        QueryProcess exec2 = QueryProcess.create(g2);
        String init1 = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String init2 = "insert data { "
                + "<Jim> rdfs:label 'Jim' "
                + "}";

        String q = "select "
                + "(group_concat (exists {"

                + "select (group_concat(exists {"
                + "select (group_concat(exists {?x rdfs:label ?ll}) as ?temp) "
                + "where { ?x rdfs:label ?l } } ) as ?temp) "
                + "where {?x rdfs:label ?l}"

                + "} ) as ?res) "

                + "where {"
                + "?x rdfs:label ?l "
                + "}";

        exec.query(init1);
        exec2.query(init2);

        exec.add(g2);

        Mappings map = exec.query(q);
        IDatatype dt = map.getValue("?res");
        //// System.out.println(map);
        assertEquals("true true true", dt.stringValue());
    }

    @Test
    public void testQQS() throws EngineException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data { "
                + "<John> rdfs:label 'John' "
                + "<James> rdfs:label 'James'"
                + "}";

        String q = "select * where {"
                + "graph ?g {"
                + "{"
                + "?x rdfs:label 'John' "
                + "filter exists { select * where {filter(?l = 'John') ?y rdfs:label ?l}} "
                + "}"
                + "union {filter(?l = 'John') ?x rdfs:label ?l}"
                + "}"
                + ""
                + ""
                + "}";

        exec.query(init);

        Mappings map = exec.query(q);
        assertEquals(2, map.size());

    }

    @Test
    public void testGTT() throws LoadException, EngineException {

        Graph g = createGraph();
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        Transformer t = Transformer.createWE(g, Transformer.TURTLE, RDF.RDF);
        String str = t.transform();
        // System.out.println("result:\n" + str);
        assertEquals(6202, str.length());

        t = Transformer.createWE(g, Transformer.TURTLE, RDFS.RDFS);
        str = t.transform();
        // System.out.println(str);
        assertEquals(3872, str.length());

        t = Transformer.create(g, Transformer.TURTLE);
        str = t.transform();
        //// System.out.println(str);
        assertEquals(9859, str.length());
    }

    @Test
    public void testGT() throws LoadException, EngineException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        String t1 = "template { st:apply-templates-with-graph(st:turtle, rdf:)} where {}";
        String t2 = "template { st:apply-templates-with-graph(st:turtle, rdfs:)} where {}";
        String t3 = "template { st:apply-templates-with(st:turtle)} where {}";

        QueryProcess exec = QueryProcess.create(g);
        Mappings map = exec.query(t1);
        String str = map.getTemplateStringResult();
        //// System.out.println(str);
        assertEquals(6202, str.length());

        map = exec.query(t2);
        str = map.getTemplateStringResult();
        // System.out.println(str);
        assertEquals(3872, str.length());

        map = exec.query(t3);
        str = map.getTemplateStringResult();
        //// System.out.println(str);
        assertEquals(9859, str.length());
    }

    @Test
    public void testGCC() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "<John> rdf:value 'test'@fr, 'titi'@fr . "
                + "<Jack> rdf:value 'test'@fr,'titi'@en . "
                + "<Jim>  rdf:value 'test'@fr, 'titi' . "
                + "}";

        String q = "select ?x (group_concat(?v) as ?g) (datatype(?g) as ?dt)where {"
                + "?x rdf:value ?v"
                + "}"
                + "group by ?x "
                + "order by ?x";

        exec.query(init);

        Mappings map = exec.query(q);
        //// System.out.println(map);

        IDatatype dt0 = map.get(0).getValue("?g");
        assertEquals(true, dt0.getDatatypeURI().equals(NSManager.XSD + "string"));

        IDatatype dt1 = map.get(1).getValue("?g");
        assertEquals(true, dt1.getDatatypeURI().equals(NSManager.XSD + "string"));

        IDatatype dt2 = map.get(2).getValue("?g");
        assertEquals(true, dt2.getLang() != null && dt2.getLang().equals("fr"));

    }

    @Test
    public void testTrig() throws LoadException, EngineException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.parse(RDF.RDF, Load.TURTLE_FORMAT);
        ld.parse(RDFS.RDFS, Load.TURTLE_FORMAT);

        Transformer pp = Transformer.create(g, Transformer.TRIG);
        String str = pp.transform();
        assertEquals(14923, str.length());

    }

    @Test
    public void testPPOWL() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        //// System.out.println("Load");
        ld.parse(data + "template/owl/data/primer.owl");
        QueryProcess exec = QueryProcess.create(g);

        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:owl)}"
                + "where {}";

        String t2 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:turtle)}"
                + "where {}";

        Mappings map = exec.query(t1);
        assertEquals(7764, map.getTemplateResult().getLabel().length());

        map = exec.query(t2);

        assertEquals(9438, map.getTemplateResult().getLabel().length());

    }

    @Test
    public void testPPSPIN() throws EngineException, LoadException {
        Graph g = createGraph();
        Load ld = Load.create(g);
        //// System.out.println("Load");
        ld.parseDir(data + "template/spinhtml/data/");
        QueryProcess exec = QueryProcess.create(g);

        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";

        Mappings map = exec.query(t1);
        int size = map.getTemplateResult().getLabel().length();
        assertTrue("Result not big enough: size = " + size, 3000 <= size);

    }

    @Test
    public void testMove1() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {"
                + "graph <g1> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "graph rdf: {"
                + "<John> rdfs:label 'John', 'Jim' ."
                + "<James> rdfs:seeAlso <Jack>, <Jim> . "
                + "}"
                + "graph <g3> {"
                + "<John> rdfs:label 'John' ."
                + "<James> rdfs:seeAlso <Jack> . "
                + "}"
                + "}";

        exec.query(init);

        String u1 = "move rdf: to default";

        exec.query(u1);

        String q1 = "select * from kg:default where  {?x ?p ?y}";
        String q2 = "select * from rdf: where  {?x ?p ?y}";

        Mappings m1 = exec.query(q1);
        Mappings m2 = exec.query(q2);
        assertEquals(4, m1.size());
        assertEquals(0, m2.size());

        String u2 = "move <g3> to <g0>";

        exec.query(u2);

        String q3 = "select * from <g0> where  {?x ?p ?y}";
        String q4 = "select * from <g3> where  {?x ?p ?y}";

        Mappings m3 = exec.query(q3);
        Mappings m4 = exec.query(q4);

        assertEquals(2, m3.size());
        assertEquals(0, m4.size());
    }

    @Test
    public void testJoinDistinct() {
        String init = "insert data {"
                + "<John> rdfs:label 'John', 'Jack' "
                + "}";

        String query = "select  distinct ?x where {"
                + "?x rdfs:label ?n "
                + "{?x rdfs:label ?a} "
                + "{?x rdfs:label ?b} "
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void testBase() {
        NSManager nsm = NSManager.create();

        nsm.setBase("http://example.org/test.html");
        nsm.setBase("foo/");
        nsm.definePrefix(":", "bar#");

        String str = nsm.toNamespaceB(":Joe");

        assertEquals("http://example.org/foo/bar#Joe", str);

    }

    @Test
    public void testLoadJSONLD() throws LoadException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parse(data + "jsonld/test.jsonld");

        String init = "select  "
                + "(count(*) as ?c)  "
                + " where {"
                + "?x ?p ?y"
                + "}";

        try {
            Mappings map = exec.query(init);
            IDatatype dt = map.getValue("?c");
            assertEquals("Result", 18, dt.intValue());
            //// System.out.println(g.display());

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJSONLD() throws LoadException, EngineException {
        Graph g = Graph.create(true);

        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person ; rdfs:range foaf:Person ."
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <James> "
                + "<Jack> foaf:knows <Jim> "
                + "<James> a foaf:Person"
                + "}";

        exec.query(init);

        JSONLDFormat jf = JSONLDFormat.create(g);
        String str = jf.toString();

        assertEquals(true, str.length() > 0);
    }

    @Test
    public void testNT() throws LoadException, EngineException {
        Graph g = Graph.create(true);

        QueryProcess exec = QueryProcess.create(g);

        String init = FOAF_PREF
                + "insert data {"
                // Cas standard :
                + "<http://example.com/John> foaf:knows <http://example.com/Jim> ."
                + "foaf:knows rdfs:domain foaf:Person ; rdfs:range foaf:Person ."
                + "<http://example.com/John> foaf:knows <http://example.com/Jim> "
                + "<http://example.com/Jim> foaf:knows <http://example.com/James> "
                + "<http://example.com/Jack> foaf:knows <http://example.com/Jim> "
                + "<http://example.com/James> a foaf:Person"
                + "<http://example.com/John> foaf:name 'John' "
                + "<http://example.com/Jim> foaf:name 'Jim' "
                + "<http://example.com/James> foaf:name 'James' "
                + "<http://example.com/Jack> foaf:name 'Jack' "
                + "<http://example.com/Jim> foaf:age 20 "
                + "<http://example.com/James> foaf:age 25 "
                + "<http://example.com/John> foaf:age 30 "
                // Test avec des caractères non-ASCII dans l'URI (devrait être valide, mais est
                // une source courante d'erreurs) :
                + "<http://example.com/Jöhn> foaf:knows <http://example.com/Jím> ."
                // Test avec un littéral contenant des caractères spéciaux et des échappements :
                + "<http://example.com/John> foaf:quote \"John's favorite quote is \\\"Hello World!\\\"\" ."
                // Test avec un littéral multiligne (devrait être valide, mais peut causer des
                // problèmes) :
                + "<http://example.com/John> foaf:note \"\"\"This is\na multi-line\nnote.\"\"\" ."
                // Test avec un littéral avec une langue :
                + "<http://example.com/John> foaf:greeting \"Hello\"@en ."
                // Test avec un littéral avec un type de données :
                + "<http://example.com/John> foaf:age \"30\"^^<http://www.w3.org/2001/XMLSchema#integer> ."
                + "}";

        exec.query(init);

        NTriplesFormat nTriplesFromat = NTriplesFormat.create(g);
        String str = nTriplesFromat.toString();

        System.out.println("–––––");
        System.out.println(str);
        System.out.println("–––––");

        assertEquals(true, str.length() > 0);
    }

    @Test
    public void testDescr() {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String q = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "describe ?z  where {"
                + "?x ?p ?y filter exists { ?x ?p ?z}"
                + "}";

        try {
            Mappings map = exec.query(q);
            ASTQuery ast = exec.getAST(map);
            assertEquals(0, ast.getConstruct().size());
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testRDFa() throws LoadException {

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.parseDir(data + "rdfa");

        String init = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select  "
                + "(count(*) as ?c)  "
                + " where {"
                + "?x ?p ?y"
                + "}";

        try {
            Mappings map = exec.query(init);
            IDatatype dt = map.getValue("?c");
            assertEquals("Result", 11, dt.intValue());

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClear() throws LoadException, EngineException {
        Graph g = Graph.create(true);
        Load ld = Load.create(g);

        QueryProcess exec = QueryProcess.create(g);

        String q1 = "select * where {graph ?g {?x a ?t}}";
        String qrem = "clear all";

        ld.parse(data + "math/data/fun.ttl");

        Mappings map = exec.query(q1);

        int res = map.size();

        exec.query(qrem);

        ld.parse(data + "math/data/fun.ttl");

        map = exec.query(q1);

        assertEquals(res, map.size());

    }

    @Test
    public void testDT() throws EngineException {
        GraphStore gs = GraphStore.create();
        // GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data { "
                + "[ rdf:value '2013-11-11'^^xsd:gYear "
                + ", '2013-11-11'^^xsd:gMonth "
                + ", '2013-11-11'^^xsd:gDay "
                + ", 'ar'^^xsd:int "
                + ", 'toto'^^xsd:double "
                + ""
                + "]}";

        String q = "select (datatype(?y) as ?res)  where {?x ?p ?y}";

        exec.query(init);

        Mappings map = exec.query(q);
        //// System.out.println(map);

        assertEquals(5, map.size());
        assertEquals(false, gs.getProxy().typeCheck());

    }

    // @Test
    public void testSystem() throws EngineException {
        GraphStore gs = GraphStore.create();
        // GraphStoreInit.create(gs).init();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data { graph kg:system { "
                + "kg:kgram kg:listen true "
                + "kg:store sp:query true "
                + "}}";

        String q = "select * where  {?x ?p ?y}";

        String query = "select ?res where {"
                + "graph kg:query {"
                + "select (st:apply-templates-with(st:spin, ?q) as ?res) where {"
                + "?q a sp:Select"
                + "}"
                + "}"
                + "}";

        exec.query(init);
        exec.query(q);

        Mappings map = exec.query(query);

        IDatatype dt = map.getValue("?res");
        assertEquals(true, dt.getLabel().contains("?x ?p ?y"));

    }

    // @Test
    public void testLoc2() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { "
                + "[ foaf:knows <Jim> ] . "
                + "<Jim> foaf:knows <James> "
                + "<Jim> rdfs:label 'Jim' "
                + " "
                + "}";

        GraphStore gs = GraphStore.create();
        // GraphStoreInit.create(gs).init();
        gs.getNamedGraph(Graph.SYSTEM);
        QueryProcess exec = QueryProcess.create(gs);

        String q = FOAF_PREF
                + "select  *"
                + "where {"
                + "graph kg:system { "
                + "?a kg:version+ ?b "
                + "filter ("
                + "?a != ?a || "
                + "if (! (exists { ?a kg:date+ ?d } = false),  true, false)"
                + ")"
                + "filter not exists { ?x foaf:knows ?y }"
                + "}"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q);
        assertEquals("result", 1, map.size());
    }

    @Test
    public void testLoc() throws EngineException, LoadException {

        String init = FOAF_PREF
                + "insert data { "
                + "graph kg:system { "
                + "  kg:kgram kg:version '3.0.22' ;"
                + "    kg:date '2013-11-27'^^xsd:date ;"
                + "    kg:skolem true "
                + "}} ;"
                + "insert data { "
                + "[ foaf:knows <Jim> ]"
                + " "
                + "}";

        GraphStore g = GraphStore.create();
        g.createNamedGraph(Graph.SYSTEM);
        QueryProcess exec = QueryProcess.create(g);

        String q1 = "select *"
                + "where {"
                + "graph ?g { ?x ?p ?y  }"
                + "}";

        exec.query(init);
        Mappings map = exec.query(q1);

        assertEquals("result", 1, map.size());

        String q2 = "select *"
                + "where {"
                + "graph kg:system { ?x ?p ?y  }"
                + "}";

        exec.query(init);
        map = exec.query(q2);

        assertEquals("result", 3, map.size());

    }

    @Test
    public void testTurtle() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data { "
                + "<John> foaf:name 'John' ; "
                + "foaf:knows [ foaf:name 'Jim' ]"
                + "}";

        String temp = "template {st:apply-templates-with(st:turtle)} where {}";
        exec.query(init);
        Mappings map = exec.query(temp);
        Node node = map.getTemplateResult();
        assertEquals("result", node == null, false);
        if (node != null) {
            assertEquals("result", node.getLabel().contains("John"), true);
            assertEquals("result", node.getLabel().contains("Property"), false);
        }
    }

    @Test
    public void testTurtle2() throws EngineException {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);
        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data { "
                + "<John> foaf:name 'John' ; "
                + "foaf:knows [ foaf:name 'Jim' ]"
                + "}";

        String temp = "template {st:call-template-with(st:turtle, st:all)} where {}";
        exec.query(init);
        Mappings map = exec.query(temp);
        Node node = map.getTemplateResult();
        assertEquals("result", node == null, false);
        if (node != null) {
            assertEquals("result", node.getLabel().contains("John"), true);
            assertEquals("result", node.getLabel().contains("Property"), true);
        }
    }

    // @Test
    public void testQV() {
        Graph g = createGraph();
        Load.create(g);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:knows (<John> <Jim>)"
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + "?x foaf:knows (<Jim> ?x) "
                + "}"
                + "pragma {"
                + "kg:list kg:expand true"
                + "}"
                + "";
        try {
            exec.query(init);
            // exec.setVisitor(ExpandList.create());
            Mappings map = exec.query(query);
            assertEquals(1, map.size());

        } catch (EngineException ex) {
            assertEquals(ex, true);
        }

    }

    // @Test
    // public void testQM() {
    // Graph g = createGraph();
    // QueryManager man = QueryManager.create(g);
    // QueryProcess exec = QueryProcess.create(g);
    // String init = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
    // + "insert data {"
    // + "<John> foaf:name 'John' ; foaf:age 18 ."
    // + "<Jim> foaf:name 'Jim' ; foaf:knows <John> ."
    // + "}";
    //
    // String query = "prefix sp: <http://spinrdf.org/sp#>"
    // + "prefix foaf: <http://xmlns.com/foaf/0.1/> "
    // + "select * where {"
    // + "?x foaf:name ?n "
    // + "?x foaf:knows ?p "
    // + "minus { ?x foaf:age ?a } "
    // + "<James> foaf:fake ?f "
    // + "?f a foaf:Person "
    // + "?f sp:elements ?e "
    // + "?f sp:test ?t "
    // + "filter(?b >= 20)"
    // + "}";
    // try {
    // exec.query(init);
    // Mappings map = man.query(query);
    // assertEquals("result", 1, map.size());
    // ////System.out.println(map.getQuery().getAST());
    // ////System.out.println(map);
    // ////System.out.println("size: " + map.size());
    //
    //
    // } catch (EngineException ex) {
    // assertEquals("result", true, ex);
    // }
    //
    // }

    @Test
    public void testPPSPINwdfgdwfgd() throws EngineException, LoadException {
        File f = new File(data + "template/spinhtml/data/");

        for (File ff : f.listFiles()) {
            System.out.println(ff);
            testSPPP(ff.getAbsolutePath());
        }
    }

    public void testSPPP(String path) throws EngineException, LoadException {
        String t1 = "prefix f: <http://example.com/owl/families/> "
                + "template  {  st:apply-templates-with(st:spin)}"
                + "where {}";
        Graph g = createGraph();
        Load ld = Load.create(g);
        //// System.out.println("Load");
        ld.parseDir(path);

        QueryProcess exec = QueryProcess.create(g);

        Mappings map = exec.query(t1);
        //// System.out.println(map.getTemplateStringResult());
        try {
            exec.compile(map.getTemplateStringResult());
            assertEquals(true, true);
        } catch (UndefinedExpressionException e) {
            System.out.println("trap1: " + e.getMessage());
        } catch (EngineException e) {
            System.out.println("trap2: " + e.getMessage());
            assertEquals(true, false);
        }
    }

    @Test
    public void testSPIN() {
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
            exec.query(init);
            String str = sp.toSpinSparql(query);
            Mappings map = exec.query(str);
            //// System.out.println(map);
            //// System.out.println(map.getQuery().getAST());
            assertEquals("result", 2, map.size());

        } catch (EngineException ex) {
        }

    }

    @Test
    public void testDistType() {
        Graph g1 = Graph.create(true);
        Graph g2 = createGraph();

        QueryProcess e1 = QueryProcess.create(g1);
        e1.add(g2);
        QueryProcess e2 = QueryProcess.create(g2);

        String init = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John> a foaf:Person "
                + "}";

        String query = "prefix foaf:    <http://xmlns.com/foaf/0.1/> "
                + "select * where {"
                + " ?x a foaf:Person "
                + "}";
        try {
            e2.query(init);
            Mappings map = e1.query(query);
            //// System.out.println(map);
            assertEquals("result", 1, map.size());
        } catch (EngineException ex) {
        }

    }

    // @Test
    public void testPPLib() {
        assertEquals("result", true, test("owl.rul") != null);
        assertEquals("result", true, test("spin.rul") != null);
        assertEquals("result", true, test("sql.rul") != null);
        assertEquals("result", true, test("turtle.rul") != null);
    }

    InputStream test(String pp) {
        String lib = Loader.PPLIB;

        InputStream stream = getClass().getResourceAsStream(lib + pp);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
            }
        }
        return stream;
    }

    @Test
    public void testDataset() {
        Graph g = Graph.create(true);
        Load.create(g);
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

        // String update = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
        // + "delete where {?x ?p ?y}";

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
            //// System.out.println(ex);
        }

    }

    @Test
    public void TestOnto() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "prefix c: <http://www.inria.fr/acacia/comma#>"
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

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select * where {"
                + "?x a c:Human, ?t"
                + "}";
        try {
            exec.query(init);
            exec.query(query);
            //// System.out.println(map);
            //// System.out.println(map.size());
        } catch (EngineException ex) {
        }
    }

    /**
     * Test With valueOut
     */
    @Test
    public void testValues() {
        String init = "insert data {"
                + "<John> foaf:age '21'^^xsd:double "
                + "<Jack> foaf:age 21.0 "
                + "}";

        String query = "select  * where {"
                + "?x foaf:age ?a "
                + "?y foaf:age ?b "
                + "filter (?a = ?b && ?x != ?y) "
                + "}";

        String query2 = "select  * where {"
                + "{select (21.0 as ?a) where { }}"
                + "?x foaf:age ?a "
                + "}";

        String query3 = "select distinct * where {"
                + "?x foaf:age ?a "
                + "}"
                + "values ?a { 21 21.0 }";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        // exec.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 2, map.size());

            map = exec.query(query2);
            //// System.out.println(map);
            assertEquals("Result", 1, map.size());

            map = exec.query(query3);
            // System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    @Test
    public void testValues2() {
        String init = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "insert data {"
                + "<John>  foaf:name 'http://www.inria.fr' "
                + "<Jack>  foaf:name 'http://www.inria.fr' "
                + "<James> foaf:name <http://www.inria.fr> "
                + "<Jim>   foaf:name <http://www.inria.fr> "
                + "<John>  foaf:name 'http://www.inria.fr'@en "
                + "<Jack>  foaf:name 'http://www.inria.fr'@en "
                + "}";

        String query = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "select  * where {"
                + "?x foaf:name ?a "
                + "?y foaf:name ?a "
                + "filter(?x < ?y) "
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    // @Test
    public void testMath() {
        Graph g = createGraph();
        Load ld = Load.create(g);

        try {
            ld.parseDir(data + "math/data");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String q = "prefix m: <http://ns.inria.fr/2013/math#>"
                + "template  { st:apply-templates-with(?p) }"
                + "where { ?p a m:PrettyPrinter }";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(q);
            Node node = map.getTemplateResult();

            //// System.out.println(node.getLabel());

            assertEquals("result", true, node.getLabel().length() > 10);

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGC() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "[ex:name 'John' , 'Jim']"
                + "[ex:name 'John' , 'Jim']"
                + "}"
                + "";

        String query2 = "prefix ex: <http://example.org/> "
                + "select (group_concat( concat(self(?n1), ?n2) ;  separator='; ') as ?t) where {"
                + "?x ex:name ?n1 "
                + "?y ex:name ?n2 "
                + "filter(?x != ?y)"
                + ""
                + "}";

        try {
            exec.query(init);

            Mappings map = exec.query(query2);
            IDatatype dt = map.getValue("?t");
            assertEquals("Results", 70, dt.getLabel().length());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    // @Test
    public void test1() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select check where {"
                + "?x rdf:type c:Person ;"
                + "c:FirstName 'John' ;"
                + "c:name ?n"
                + "}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            exec.query(query);
            assertEquals("Result", true, true);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select more * (kg:similarity() as ?sim) where {"
                + "?x rdf:type c:Engineer "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type c:WebPage"
                + "}"
                + "order by desc(?sim)";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");
            assertEquals("Result", true, dt != null);
            if (dt != null) {
                double sim = dt.doubleValue();
                assertEquals("Result", .84, sim, 1e-2);
            }
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2b() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select more * (kg:similarity() as ?sim) where {"
                + "?x rdf:type ?c1 filter(kg:similarity(?c1, c:Engineer) > .5) "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type ?c2 filter(kg:similarity(?c2, c:WebPage) > .4)"
                + "}"
                + "order by desc(?sim)";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);

            assertEquals("Result", 9, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test2c() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  (kg:similarity(c:Person, c:Document) as ?sim) where {}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");
            double sim = dt.doubleValue();

            assertEquals("Result", sim, .16, 1e-2);
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test4() {
        Load ld = Load.create(Graph.create());
        try {
            ld.parse("gogo.rdf");
            assertEquals("Result", false, true);
        } catch (LoadException e) {
            //// System.out.println(e);
            assertEquals("Result", e, e);
        }
        try {
            ld.parse(data + "comma/fail.rdf");
            assertEquals("Result", false, true);
        } catch (LoadException e) {
            //// System.out.println(e);
            assertEquals("Result", e, e);
        }
    }

    @Test
    public void test5() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            Mappings map = exec.query(query);

            assertEquals("Result", 1, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test6() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop = "drop graph kg:entailment";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            exec.query(drop);
            Mappings map = exec.query(query);

            assertEquals("Result", 0, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test7() {
        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "insert data {"
                + "<John> c:name 'John' ; rdf:value (1 2 3)"
                + "c:name rdfs:domain c:Person "
                + "c:Person rdfs:subClassOf c:Human "
                + "}";

        String drop = "drop graph kg:entailment";
        String create = "create graph kg:entailment";

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type c:Human ; c:name ?n ;"
                + "rdf:value @(1 2)"
                + "}";

        try {
            exec.query(update);
            exec.query(drop);
            exec.query(create);
            Mappings map = exec.query(query);

            assertEquals("Result", 1, map.size());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    public void test8() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x c:hasCreated ?doc"
                + "} "
                + "group by any "
                + "order by desc(count(?doc))"
                + "pragma {"
                + "kg:kgram kg:list true "
                + "kg:kgram kg:detail true}";

        try {
            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            assertEquals("Result", 3, map.size());
            Mapping m = map.get(0);
            assertEquals("Result", 2, m.getMappings().size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test9() {

        Graph g1 = Graph.create(true);
        Graph g2 = Graph.create(true);

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  *  where {"
                + "?x rdf:type ?t; c:name ?n"
                + "} ";

        try {
            QueryProcess e1 = QueryProcess.create(g1);
            QueryProcess e2 = QueryProcess.create(g2);
            QueryProcess exec = QueryProcess.create(g1);
            exec.add(g2);

            e1.query("prefix c: <http://www.inria.fr/acacia/comma#>" + "insert data {<John> rdf:type c:Person}");
            e2.query("prefix c: <http://www.inria.fr/acacia/comma#>" + "insert data {<John> c:name 'John'}");

            Mappings map = exec.query(query);
            assertEquals("Result", 1, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    // @Test
    // public void test10() {
    //
    // String query = "select * where {"
    // + "bind (kg:unnest(kg:sparql('select * where {?x rdf:type c:Person;
    // c:hasCreated ?doc}')) "
    // + "as (?a, ?b))"
    // + "} ";
    //
    // try {
    //
    // QueryProcess exec = QueryProcess.create(graph);
    //
    // Mappings map = exec.query(query);
    // assertEquals("Result", 3, map.size());
    // Node node = map.getNode("?a");
    // assertEquals("Result", true, node != null);
    //
    // } catch (EngineException e) {
    // assertEquals("Result", true, e);
    // }
    // }
    // @Test
    // public void test10cons() {
    //
    // String query =
    // "prefix c: <http://www.inria.fr/acacia/comma#>"
    // + "select * where {"
    // + "bind ((kg:sparql('"
    // + "prefix c: <http://www.inria.fr/acacia/comma#>"
    // + "construct where {?x rdf:type c:Person; c:hasCreated ?doc}')) "
    // + "as ?g)"
    // + "graph ?g { ?a ?p ?b }"
    // + "} ";
    //
    // try {
    //
    // QueryProcess exec = QueryProcess.create(graph);
    //
    // Mappings map = exec.query(query);
    // assertEquals("Result", 5, map.size());
    //
    //
    // } catch (EngineException e) {
    // assertEquals("Result", true, e);
    // }
    // }
    // @Test
    // public void test11() {
    //
    // String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
    // "select * (count(?doc) as ?c)"
    // + "(kg:setObject(?x, ?c) as ?t)"
    // + "where {"
    // + "?x c:hasCreated ?doc"
    // + ""
    // + "}"
    // + "group by ?x";
    //
    // String query2 ="prefix c: <http://www.inria.fr/acacia/comma#>" +
    // "select distinct ?x"
    // + "(kg:getObject(?x) as ?v)"
    // + "where {"
    // + "?x c:hasCreated ?doc filter(kg:getObject(?x) > 0)"
    // + "}"
    // + "order by desc(kg:getObject(?x))";
    //
    //
    // try {
    //
    // QueryProcess exec = QueryProcess.create(graph);
    //
    // exec.query(query);
    // Mappings map = exec.query(query2);
    //
    // assertEquals("Result", 3, map.size());
    //
    // IDatatype dt = getValue(map, "?v");
    //
    // assertEquals("Result", 2, dt.getIntegerValue());
    //
    // } catch (EngineException e) {
    // assertEquals("Result", true, e);
    // }
    // }

    public void test111() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * (count(?doc) as ?c)"
                + "(kg:setProperty(?x, 0, ?c) as ?t)"
                + "where {"
                + "?x c:hasCreated ?doc"
                + ""
                + "}"
                + "group by ?x";

        String query2 = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select distinct ?x"
                + "(kg:getProperty(?x, 0) as ?v)"
                + "where {"
                + "?x c:hasCreated ?doc filter(kg:getProperty(?x, 0) > 0)"
                + "}"
                + "order by desc(kg:getProperty(?x, 0))";

        try {

            QueryProcess exec = QueryProcess.create(graph);

            exec.query(query);
            Mappings map = exec.query(query2);

            assertEquals("Result", 3, map.size());

            IDatatype dt = getValue(map, "?v");

            assertEquals("Result", 2, dt.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test12() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" + "select  *  where {"
                + "?x rdf:type ?class; c:hasCreated ?doc}";

        try {

            QueryProcess.setSort(true);
            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            QueryProcess.setSort(false);

            assertEquals("Result", 22, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test13() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" + "select  *  where {"
                + "?x rdf:type ?class; c:hasCreated ?doc}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            StatListener el = StatListener.create();
            exec.addEventListener(el);
            Mappings map = exec.query(query);
            ////// System.out.println(el);
            assertEquals("Result", 22, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test14() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" + "select  *  where {"
                + "?x rdf:type c:Person; c:hasCreated ?doc "
                + "?doc rdf:type/rdfs:subClassOf* c:Document "
                + "c:Document rdfs:label ?l ;"
                + "rdfs:comment ?c"
                + "}";

        try {

            Graph g = Graph.create(true);
            Load ld = Load.create(g);
            // ld.setBuild(new MyBuild(g));
            try {
                init(g, ld);
            } catch (LoadException ex) {
            }

            QueryProcess exec = QueryProcess.create(g);
            Mappings map = exec.query(query);
            assertEquals("Result", 68, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test15() {

        String query = "select (kg:similarity() as ?sim) (max(kg:depth(?x)) as ?max)  where {"
                + "?x rdfs:subClassOf ?sup"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 14, dt.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test16() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * (st:number() as ?num)  where {"
                + "?x c:hasCreated ?doc "
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            Mapping m = map.get(map.size() - 1);
            IDatatype dt = datatype(m.getNode("?num"));
            //// System.out.println(map);
            assertEquals("Result", map.size(), dt.intValue());
        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test17() throws LoadException {

        Graph g = Graph.create(true);
        Load ld = Load.create(g);
        ld.parse(data + "comma/comma.rdfs");

        QueryProcess exec = QueryProcess.create(g);
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select (kg:similarity(c:Person, c:Document) as ?sim) {}";
        try {
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?sim");

            assertEquals("Result", true, dt.doubleValue() < 0.5);

            String update = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                    "insert data {c:Human rdfs:subClassOf c:Person}";
            exec.query(update);

            // assertEquals("Result", null, g.getClassDistance());

            map = exec.query(query);
            IDatatype sim = getValue(map, "?sim");

            assertEquals("Result", dt, sim);

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test18() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * where {"
                + "c:Person rdfs:subClassOf+ :: $path ?c "
                + "graph $path {?a ?p ?b}"
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 31, map.size());
        } catch (EngineException e) {
            assertEquals("Result", 31, e);
        }

    }

    @Test
    public void test19() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select * "
                + "(xt:size($path) as ?l) (count(?a) as ?c) where {"
                + "?x c:isMemberOf+ :: $path ?org "
                + "graph $path {?a ?p ?b}"
                + "}"
                + "group by $path";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 99, map.size());

            for (Mapping mm : map) {
                IDatatype ldt = getValue(mm, "?l");
                IDatatype lc = getValue(mm, "?c");

                assertEquals("Result", ldt, lc);
            }

        } catch (EngineException e) {
            assertEquals("Result", 99, e);
        }

    }

    public IDatatype fun(IDatatype dt1, IDatatype dt2) {
        String str = concat(dt1, dt2);
        return DatatypeMap.newLiteral(str);
    }

    String concat(IDatatype dt1, IDatatype dt2) {
        return dt1.stringValue() + "." + dt2.getLabel();
    }

    @Test
    public void test20() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "prefix ext: <function://fr.inria.corese.engine.TestQuery1> "
                + "select (ext:fun(?fn, ?ln) as ?res) where {"
                + "?x c:FirstName ?fn ; c:FamilyName ?ln"
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 23, map.size());

            for (Mapping mm : map) {
                IDatatype dt1 = getValue(mm, "?fn");
                IDatatype dt2 = getValue(mm, "?ln");
                IDatatype dt3 = getValue(mm, "?res");

                assertEquals("Result", dt3.getLabel(), concat(dt1, dt2));
            }

        } catch (EngineException e) {
            assertEquals("Result", 23, e);
        }

    }

    @Test
    public void test21() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * where {"
                + "?x c:FirstName 'Olivier' "
                + "filter(kg:contains('é', 'e')) "
                + "filter(kg:contains('e', 'é')) "
                + "filter(kg:equals('e', 'é')) "
                + "}";

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            assertEquals("Result", 2, e);
        }

    }

    @Test
    public void test22() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {?a ?p ?b}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            getValue(map, "?max");
            assertEquals("Result", 64, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test23() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {{c:Toto ?p ?b} union {c:Engineer ?p ?b}}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test24() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {?a ?p ?b filter(?a = c:Engineer)}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test25() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {optional{c:Engineer ?p ?b} filter(! bound(?b))}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            getValue(map, "?max");
            assertEquals("Result", 0, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test26() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {optional{c:Toto ?p ?b} filter(! bound(?b))}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            getValue(map, "?max");
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test27() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {{c:Engineer ?p ?b} minus {?a ?p c:Engineer}}"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            assertEquals("Result", 17, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test28() {

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "c:Engineer rdfs:subClassOf+ :: $path ?y "
                + "graph $path {c:Engineer ?p ?b} "
                + "?x rdf:type c:Engineer "
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            assertEquals("Result", 119, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    public void test30() {

        String query = "select  "
                + "(xt:size($path) as ?l) "
                + "(max(?l, groupBy(?x, ?y)) as ?m) "
                + "(max(?m) as ?max) "
                + "where {"
                + "?x rdfs:subClassOf+ :: $path ?y"
                + "}";

        try {

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 13, dt.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test31() {
        String query = "select (count(?l) as ?c1) "
                + "(count(distinct ?l) as ?c2) "
                + "(count(distinct self(?l)) as ?c3) "
                + "where {"
                + "?x rdfs:label ?l"
                + "}";
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt1 = getValue(map, "?c1");
            IDatatype dt2 = getValue(map, "?c2");
            IDatatype dt3 = getValue(map, "?c3");

            assertEquals("Result", 1406, dt1.intValue());
            assertEquals("Result", 1367, dt2.intValue());
            assertEquals("Result", 1367, dt3.intValue());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test33() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x foaf:knows+ :: $path <Jim> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test34() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x ^ (foaf:knows+) :: $path <John> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test35() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:knows <Jim> "
                + "}";

        String query = "select * where {"
                + "?x  (^foaf:knows)+ :: $path <John> "
                + "graph $path { ?a foaf:knows ?b }"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test36() {
        // select (group_concat(distinct ?x, ?y) as ?str)
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String update = "insert data {"
                + "<John> foaf:knows (<a> <b> <c>) "
                + "}";

        String query = "select * where {"
                + "graph ?g {optional{?x rdf:rest*/rdf:first ?y} "
                + "filter(!bound(?y))  "
                + "}"
                + "}";

        try {
            exec.query(update);

            Mappings map = exec.query(query);
            ////// System.out.println(map);
            assertEquals("Result", 0, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }

    @Test
    public void test37() {
        Graph g = Graph.create(true);
        QueryProcess exec = QueryProcess.create(g);

        String init = "insert data {<John> <name> 'John'}";
        try {
            exec.query(init);

            g.init();

            // RDFFormat f = RDFFormat.create(g);
            // ////System.out.println(f);

            assertEquals("Result", 3, g.size());

            String query = "select * where {?p rdf:type rdf:Property}";

            Mappings res = exec.query(query);
            // ////System.out.println("** Res: " );
            // ////System.out.println(res);
            assertEquals("Result", 2, res.size());

            String update = "delete {?x ?p ?y} where {?x ?p ?y}";
            exec.query(update);

            String qq = "select * where {?x ?p ?y}";
            res = exec.query(qq);
            assertEquals("Result", 0, res.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test38() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John>  <age> 20 "
                + "<John>  <age> 10 "
                + "<James> <age> 30 "
                + "}";

        String query = "select distinct (sum(?age) as ?s) where {"
                + "?x <age> ?age"
                + "}"
                + "group by ?x";

        try {
            exec.query(init);
            Mappings res = exec.query(query);
            assertEquals("Result", 1, res.size());
            assertEquals("Result", 30, getValue(res, "?s").intValue());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    // @Test
    public void test39() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<a> foaf:knows <b> "
                + "<b> foaf:knows <a> "
                + "<b> foaf:knows <c> "
                + "<a> foaf:knows <c> "
                + "}";

        String query = "select * where {"
                + "<a> foaf:knows+ ?t "
                + "}";

        // String query2 = "select * where {"
        // + "<a> foaf:knows{1,10} ?t "
        // + "}"
        // + "pragma {kg:path kg:loop false}";

        try {
            exec.query(init);
            Mappings res = exec.query(query);
            assertEquals("Result", 2, res.size());

            exec.setPathLoop(false);
            res = exec.query(query);
            assertEquals("Result", 2, res.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test41() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a (  foaf:knows+ || (^rdfs:seeAlso) +) ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test42() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a (  foaf:knows || ^rdfs:seeAlso )+ ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test43() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:e "
                + "ex:e foaf:knows ex:c "
                + "ex:b rdfs:seeAlso ex:a "
                + "ex:c rdfs:seeAlso ex:b "
                + "}";

        String query = "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "ex:a ( foaf:knows+ || (^rdfs:seeAlso)+ ) ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test47() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<person><name>John</name><lname>K</lname><year>2000</year></person>"
                + "<person><name>James</name><lname>C</lname><year>1950</year></person>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String query = ""
                + "prefix i: <http://www.inria.fr/test/> "
                + "construct {"
                + "[i:name ?name]"
                + "} where {"
                + "select (concat(xt:objectvalue(?n), '.', xt:objectvalue(?l)) as ?name) "
                + "(xsd:integer(xt:objectvalue(?y)) as ?yy) where {"
                + "?x i:contain ?xml "
                // + "bind (xpath(?xml, '/doc/person') as ?p) "
                // + "bind (xpath(?p, 'name/text()') as ?n) "
                // + "bind (xpath(?p, 'lname/text()') as ?l) "

                + "values ?p { unnest(xt:xpath(?xml, '/doc/person')) } "
                + "values ?n { unnest(xt:xpath(?p, 'name/text()')) }  "
                + "values ?l { unnest(xt:xpath(?p, 'lname/text()')) }  "
                + "values ?y { unnest(xt:xpath(?p, 'year/text()')) }  "

                + "}}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            // System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test49() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix i: <http://www.inria.fr/test/> "
                + ""
                + "insert data {"
                + "<doc> i:contain "
                + "'<doc>"
                + "<phrase><subject>Cat</subject><verb>on</verb><object>mat</object></phrase>"
                + "<phrase><subject>Cat</subject><verb>eat</verb><object>mouse</object></phrase>"
                + "</doc>'^^rdf:XMLLiteral   "
                + "}";

        String phrase = TestQuery1.class.getResource("/text/phrase.xml").getPath();

        String query = ""
                + "base      <http://www.example.org/schema/>"
                + "prefix s: <http://www.example.org/schema/>"
                + "prefix i: <http://www.inria.fr/test/> "
                + "construct {?su ?pr ?ob} "
                + "where {"
                + "select * where {"
                + "values ?xml { <file://" + phrase + "> } "
                + "values ?st { unnest(xt:xpath(?xml, '/doc/phrase')) }  "
                + "values ?s  { unnest(xt:xpath(?st, 'subject/text()')) }  "
                + "values ?p  { unnest(xt:xpath(?st, 'verb/text()')) }   "
                + "values ?o  { unnest(xt:xpath(?st, 'object/text()')) }  "

                + "bind  (uri(xt:objectvalue(?s)) as ?su) "
                + "bind  (uri(xt:objectvalue(?p)) as ?pr)   "
                + "bind  (uri(xt:objectvalue(?o)) as ?ob)   "

                + "}"
                + "}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            // System.out.println(map);
            ResultFormat.create(map);
            // System.out.println(f);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
            assertEquals("Result", 2, null);
        }

    }

    @Test
    public void test50() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":A0 :P :A1, :A2 . "
                + ":A1 :P :A0, :A2 . "
                + ":A2 :P :A0, :A1"
                + "}";

        String query = "prefix : <http://example.org/>"
                + "select * where { :A0 ((:P)*)* ?X }";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test51() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":A0 :P :A1, :A2 . "
                + ":A1 :P :A0, :A2 . "
                + ":A2 :P :A0, :A1"
                + "}";

        String query = "prefix : <http://example.org/>"
                + "select * where { ?X ((:P)*)* :A1 }";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test52() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String query = "prefix : <http://example.org/>"
                + "select * where { :a (:p/:q)+ ?y }";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test53() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * (kg:similarity() as ?sim) where {"
                + "?x rdf:type c:Engineer "
                + "}"
                + "order by desc(?sim)"
                + "pragma {kg:match kg:mode 'strict'}";

        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            //// System.out.println(map);

            assertEquals("Result", 7, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }

    @Test
    public void test54() {
        String query = "select * where {"
                + "graph ?g {?s <name> ?o} "
                + "?s <age> ?a"
                + "}";
        Graph graph = createGraph();

        Node g = graph.addGraph("test");
        Node s = graph.addResource("URIJohn");
        Node p = graph.addProperty("age");
        Node o = graph.addLiteral(24);
        Node p2 = graph.addProperty("name");
        Node o2 = graph.addLiteral("John");

        graph.addEdge(g, s, p, o);
        graph.addEdge(s, p2, o2);

        QueryProcess exec = QueryProcess.create(graph);

        try {
            Mappings map = exec.query(query);
            //// System.out.println(map);

            assertEquals("Result", 1, map.size());

        } catch (EngineException ee) {
            assertEquals("Result", true, ee);
        }
    }

    /**
     * Two graphs with partial ontology each Each graph answer with its
     * viewpoint on the ontology
     */
    @Test
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

        Graph.create(true);
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

            // exec.query(o1);
            // exec.query(o2);

            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());
            //// System.out.println(map);

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test56() {
        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);
        // exec.setOptimize(true);

        String init = "insert data {"
                + "graph <g1> {<John> foaf:knows <Jim> }"
                + "graph <g2> {<Jim> foaf:knows <Jack>}"
                + "}";

        String query = "select  * where {"
                + "?x foaf:knows+ ?y "
                + "filter(?y = <Jack> || <John> = ?x)"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test58() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:knows <Jack> "
                + "}";

        String query = "select * (xt:size($path) as ?l) where {"
                + "?x (foaf:knows|rdfs:seeAlso)+ :: $path ?y"
                + "}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            ResultFormat.create(map);
            assertEquals("Result", 3, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test59() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "insert data {"
                + "<John> foaf:knows <Jack> "
                + "<John> foaf:name 'Jack' ;"
                + "foaf:age 12 ;"
                + "foaf:date '2012-04-01'^^xsd:date ;"
                + "foaf:knows [] "
                + "}";

        String query = "select * where {?x ?p ?y}";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            XMLFormat f = XMLFormat.create(map);

            XMLResult xml = XMLResult.create(exec.getProducer());
            xml.parseString(f.toString());
            //// System.out.println(m);

            assertEquals("Result", 5, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // @Test
    // public void test62() {
    //
    // Graph graph = createGraph();
    // QueryProcess exec = QueryProcess.create(graph);
    //
    // String init =
    // "prefix foaf: <http://xmlns.com/foaf/0.1/>"
    // + "insert data {"
    // + "<John> foaf:age 12 "
    // + "<James> foaf:age 20"
    // + "}";
    //
    // String query =
    // "prefix foaf: <http://xmlns.com/foaf/0.1/>"
    // + "select * where {"
    // + "?x foaf:age ?age"
    // + "}";
    //
    // try {
    // Mappings map = exec.query(init);
    // map = exec.query(query);
    // assertEquals("Result", 2, map.size());
    //
    // exec.filter(map, "?age > 15");
    // assertEquals("Result", 1, map.size());
    // } catch (EngineException e) {

    // e.printStackTrace();
    // }
    // }

    @Test
    public void test63() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "insert data {"
                + "<A> foaf:date '2012-05-10'^^xsd:dateTime "
                + "<B> foaf:date '2012-05-11'^^xsd:dateTime "
                + "<C> foaf:date '2012-05-10T10:20:30'^^xsd:dateTime "
                + "<D> foaf:date '2012-05-10T10:30:30.50'^^xsd:dateTime "
                + "<E> foaf:date '2012-05-10T10:30:30'^^xsd:dateTime "
                + "}";

        String query = "prefix foaf: <http://xmlns.com/foaf/0.1/>"
                + "select * where {"
                + "?x foaf:date ?date"
                + "}"
                + "order by desc(?date)";

        try {
            Mappings map = exec.query(init);
            map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 5, map.size());

            IDatatype dt0 = map.get(0).getNode("?x").getValue();
            IDatatype dt1 = map.get(1).getNode("?x").getValue();
            IDatatype dt2 = map.get(2).getNode("?x").getValue();
            IDatatype dt3 = map.get(3).getNode("?x").getValue();
            IDatatype dt4 = map.get(4).getNode("?x").getValue();

            assertEquals("Result", "B", dt0.getLabel());
            assertEquals("Result", "D", dt1.getLabel());
            assertEquals("Result", "E", dt2.getLabel());
            assertEquals("Result", "C", dt3.getLabel());
            assertEquals("Result", "A", dt4.getLabel());

            // B D E C A

        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void test64() {

        QueryProcess exec = QueryProcess.create(graph);

        String query = "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select (kg:ancestor(c:Event, c:Document) as ?a) where {"
                + ""
                + "}";

        try {
            Mappings map = exec.query(query);

            Node aa = graph.getResource("http://www.inria.fr/acacia/comma#Something");
            Node rr = map.getNode("?a");

            assertEquals("Result", aa.getLabel(), rr.getLabel());

            Node n1 = graph.getResource("http://www.inria.fr/acacia/comma#Person");
            Node n2 = graph.getResource("http://www.inria.fr/acacia/comma#Event");

            //// System.out.println("ANC: " + n1);
            //// System.out.println("ANC: " + n2);
            graph.setClassDistance();
            Node vv = graph.getClassDistance().ancestor(n1, n2);
            assertEquals("Result", aa.getLabel(), vv.getLabel());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test65() {
        Graph g = createGraph();

        Load ld = Load.create(g);

        try {
            ld.parse(data + "test/iso.ttl");
            ld.parse(data + "test/iso.rdf");

            ld.parse(data + "test/utf.ttl");
            ld.parse(data + "test/utf.rdf");
        } catch (LoadException e1) {
            e1.printStackTrace();
        }

        String query = "select * where {"
                + "?x ?p ?y . ?z ?q ?y filter(?x != ?z)"
                + "}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 4, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    public void testRelax() {
        Graph g = graph();

        String init = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "insert data {"
                + "<John> foaf:type c:Researcher "
                + "<John> foaf:knows <Jack> "
                + "<Jack> foaf:type c:Engineer "
                + "<John> foaf:knows <Jim> "
                + "<Jim> foaf:type c:Fireman "
                + "<e> foaf:type c:Event "
                + "}";

        String query = "prefix foaf: <http://xmlns.com/foaf/0.1/> "
                + "prefix c: <http://www.inria.fr/acacia/comma#>"
                + "select   more * (kg:similarity() as ?s) where {"
                + "?x foaf:type c:Engineer "
                + "?x foaf:knows ?y "
                + "?y foaf:type c:Engineer"
                + "}"
                + "order by desc(?s) "
                + "pragma {kg:kgram kg:relax (foaf:type)}";

        QueryProcess exec = QueryProcess.create(g);

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            //// System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create a Query graph from an RDF Graph Execute the query Use case: find
     * similar Graphs (cf Corentin)
     */

    public void testQueryGraph() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :q :d "
                + ":c :q :d "
                + ":d :p :e "
                + ":e :q :f "
                + ""
                + "} ";

        String cons = "prefix : <http://example.org/> "
                + ""
                + "construct {?x :p []}"
                + "where {?x :p ?y}";

        String init2 = "prefix : <http://example.org/> "
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

            Graph g2 = createGraph();
            QueryProcess exec2 = QueryProcess.create(g2);
            exec2.query(init2);

            QueryGraph qg = QueryGraph.create(g2);
            new QGVisitor();
            // qg.setVisitor(vis);
            qg.setConstruct(true);
            map = exec.query(qg);

            Graph res = exec.getGraph(map);
            assertEquals("Results", 2, res.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testOption() {

        Graph graph = createGraph();
        QueryProcess exec = QueryProcess.create(graph);

        String init = "prefix : <http://example.org/> "
                + ""
                + "insert data {"
                + ":a :p :b, :c ."
                + ":b :p :d, :a "
                + ":c :p :d "
                + ""
                + ":e :p :b, :c ."
                + ""
                + "} ";

        String query = "prefix : <http://example.org/> "
                + "select *  where  {"
                + "?x ((:p/:p) ?)  ?y "
                + "}";

        try {

            exec.query(init);
            Mappings map = exec.query(query);
            // System.out.println(map);

            assertEquals("Results", 9, map.size());

        } catch (EngineException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testWF() {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        QueryEngine qe = QueryEngine.create(g);
        g.addEngine(qe);

        String init = "prefix c: <http://example.org/>"
                + "insert data {"
                + "[ c:hasParent [] ]"
                + "}";

        String update = "prefix c: <http://example.org/>"
                + "insert {?y c:hasChild ?x}"
                + "where { ?x c:hasParent ?y}";

        qe.addQuery(update);
        // qe.setDebug(true);
        // g.getWorkflow().setDebug(true);

        String query = "select * where {?x ?p ?y}";

        try {
            ////// System.out.println("init");
            exec.query(init);
            ////// System.out.println("query");

            Mappings map = exec.query(query);
            // ////System.out.println(map);
            //
            // System.out.println("*****************");
            // System.out.println(map);
            assertEquals("Result", 2, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, false);
        }

    }

    // @Test
    public void testCompile() throws EngineException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);

        String query = "select * where {"
                + "graph ?g {?x ?p ?y "
                + "{select * where {"
                + "?a (rdf:type@[a rdfs:Resource]) :: $path ?b  "
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
        Mappings map = exec.query(query);
        Query q = map.getQuery();
        System.out.println(q);
        // System.out.println("NB Procesor: " + Processor.count);
        assertEquals("Result", 17, q.nbNodes());
    }

    IDatatype getValue(Mapping map, String name) {
        return datatype(map.getValue(name));
    }

    IDatatype getValue(Mappings map, String name) {
        Object value = map.getValue(name);
        if (value == null) {
            return null;
        }
        return datatype(value);
    }

    IDatatype datatype(Object n) {
        return (IDatatype) n;
    }

    IDatatype datatype(Node n) {
        return n.getValue();
    }
}
