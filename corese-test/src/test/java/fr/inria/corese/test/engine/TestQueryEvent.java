package fr.inria.corese.test.engine;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.SOLVER_OVERLOAD;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Context;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author corby
 */
public class TestQueryEvent {
     static String data  = Thread.currentThread().getContextClassLoader().getResource("data/").getPath() ;
   
      @BeforeClass
    static public void init() {
        Property.set(SOLVER_OVERLOAD, true);
    }
     
      @Test
    public void testmethod() throws EngineException, LoadException {
        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();

        String init =
                "insert data {"
                        + "us:square a us:Square ;  us:size 5 ."
                        + "us:rect a us:Rectangle ; us:width 3 ; us:length 4 ."
                        + "us:circ a us:Circle ;    us:radius 3 ."
                        + "us:comp1 a us:Composite ; us:member (us:square us:rect us:circ)"
                        + "us:comp2 a us:Composite ; us:member (us:comp1)"

                        + "us:Rectangle     rdfs:subClassOf us:Figure "
                        + "us:SquareCircle  rdfs:subClassOf us:Figure "
                        + "us:Square        rdfs:subClassOf us:SquareCircle "
                        + "us:Circle        rdfs:subClassOf us:SquareCircle "
                        + "}";

        String q = ql.readWE(data + "test/method.rq");

        exec.query(init);
        Mappings map = exec.query(q);
        // ?a = "65.2744"^^xsd:decimal; ?p = "52.849599999999995"^^xsd:decimal;
        IDatatype a = (IDatatype) map.getValue("?a");
        IDatatype p = (IDatatype) map.getValue("?p");
        assertEquals(28.2744, a.doubleValue(), 1e-5);
        assertEquals(18.8496, p.doubleValue(), 1e-5);
    }
    
      @Test
    public void testch() throws EngineException {
        String init = "insert data {"
                + "us:Man rdfs:subClassOf us:Person "
                + "us:Person rdfs:subClassOf us:Edge "
                + "us:Edge rdfs:subClassOf rdfs:Resource "
                + "us:man a us:Man "
                + "us:person a us:Person "
                + "us:Edge  a us:Edge "
                + "us:android  a us:Android ; rdfs:label 'James' "
                + "[] a us:Person "
                + "}";

        String q = "@method "
                + "select ?x (method(us:test, ?x) as ?t) (method(us:test, ?ll) as ?l) "
                + "where { ?x a ?type optional { ?x rdfs:label ?ll }}"


                + "@type us:Man "
                + "function us:test(?x) {"
                + "'man'"
                + "}"

//                + "@type us:Person "
//                + "function us:test(?x) {"
//                + "'person'"
//                + "}"

                + "@type rdfs:Resource "
                + "function us:test(?x) {"
                + "'resource'"
                + "}"

                + "@type dt:literal "
                + "function us:test(?x) {"
                + "'literal'"
                + "}"

                + "function us:test(?x) {"
                + "'default'"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        //System.out.println(map);
        for (Mapping m : map) {
            IDatatype x = (IDatatype) m.getValue("?x");
            IDatatype v = (IDatatype) m.getValue("?t");
            String obj = x.stringValue();
            String val = v.stringValue();
            if (obj.contains("man")) assertEquals(val, "man");
            if (obj.contains("person") || obj.contains("Edge")) assertEquals(val, "resource");
            if (obj.contains("android")) assertEquals(val, "default");
            if (x.isBlank()) assertEquals(val, "resource");
        }
    }

    
    @Test
    public void testdh() throws EngineException {
        String init = "insert data { us:John rdfs:label 'John' }";

        String q =
                "@method "
                        + "select "
                        + "(method(us:display, 1.5) as ?dec)"
                        + "(method(us:display, us:John) as ?uri)"
                        + "(method(us:display, 12) as ?int)"
                        + "(method(us:display, bnode()) as ?bn)"
                        + "(method(us:display, xt:graph()) as ?g)"
                        + "(let((?tt) = xt:graph()) {?tt} as ?triple)"
                        + "(method(us:display, ?triple) as ?t)"
                        + "where { }"

                        + "@type dt:uri "
                        + "function us:display(?x) {"
                        + "concat('uri: ', ?x)"
                        + "}"

                        + "function us:display(?x) {"
                        + "concat('default: ', ?x)"
                        + "}"

                        + "@type dt:bnode "
                        + "function us:display(?x) {"
                        + "concat('bnode: ', ?x)"
                        + "}"

                        + "@type dt:standard "
                        + "function us:display(?x) {"
                        + "concat('standard: ', ?x)"
                        + "}"

                        + "@type dt:extended "
                        + "function us:display(?x) {"
                        + "concat('extended: ', ?x)"
                        + "}"

                        + "@type dt:triple "
                        + "function us:display(?x) {"
                        + "concat('triple: ', ?x)"
                        + "}"

                        + "@type dt:Edge "
                        + "function us:display(?x) {"
                        + "concat('Edge: ', ?x)"
                        + "}"

                        + "@type dt:literal "
                        + "function us:display(?x) {"
                        + "concat('literal: ', ?x)"
                        + "}"

                        + "@type xsd:integer "
                        + "function us:display(?x) {"
                        + "concat('int: ', ?x)"
                        + "}";


        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        //System.out.println(map);
        assertEquals(true, strValue(map, "?dec").contains("standard"));
        assertEquals(true, strValue(map, "?uri").contains("uri"));
        assertEquals(true, strValue(map, "?int").contains("int"));
        assertEquals(true, strValue(map, "?bn").contains("bnode"));
        assertEquals(true, strValue(map, "?g").contains("extended"));
        assertEquals(true, strValue(map, "?t").contains("triple"));
    }
    
    

    @Test
    public void testEventUpdate() throws EngineException, LoadException, IOException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String i = "@import <http://ns.inria.fr/sparql-template/function/datashape/main.rq> "
                + "@event insert data { "
                + "us:John foaf:name 'John' ; foaf:age 10 ."
                + "};"
                + "insert data { "
                + "us:Jack foaf:name 'Jack' ; foaf:age 20 "
                + "}"
                + "@beforeUpdate "
                + "function us:beforeupdate(q) {"
                + "xt:print('before update');"
                + "set(var = 10)"
                + "}"
                + "function us:const() {"
                + "var"
                + "}"
                + "@update "
                + "function us:update(q, del, ins) {"
                + "xt:print('update:', ins);"
                + "for ((s p o) in ins) {"
                + "query(delete {?s foaf:age ?o} insert {?s foaf:age ?a} "
                + "where { ?s foaf:age ?o bind (us:const() + ?o as ?a) } )"
                + "}"
                + "}"
                + "@afterUpdate "
                + "function us:afterupdate(q) {"
                + "xt:print('after update: ', coalesce(var, 'undef'));"
                + "let (shape = construct {"
                + "us:test a sh:NodeShape ; sh:targetSubjectsOf foaf:name ;"
                + "sh:property [ sh:path foaf:age ; sh:datatype xsd:integer] ."
                + "} where { }) {"
                + "set (report = coalesce(sh:shacl(shape), false));"
                + "xt:print('shape:', xt:turtle(shape));"
                + "xt:print('shacl:', xt:turtle(report))"
                + "}"
                + "}";

        String q = "select (sum(?a) as ?sum) where {?x foaf:age ?a}";

        exec.query(i);
        IDatatype res = exec.getEnvironmentBinding().getVariable("?report");
        Graph report = (Graph) res.getPointerObject();
        boolean b = new Shacl(g).conform(report);
        assertEquals(true, b);

        Mappings map = exec.query(q);
        IDatatype dt = getValue(map, "?sum");
        assertEquals(50, dt.intValue());
    }

    @Test
    public void testGlobalVar() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { us:John rdf:value 1, 2, 3 }";

        String q = "@event select "
                + "(aggregate(exists { "
                + "select (aggregate(exists {?x rdf:value ?v }) as ?vv)  where {?x rdf:value ?f  }"
                + "}) as ?l) "
                + "where {"
                + "?x rdf:value ?w"
                + "}"
                + "@statement "
                + "function us:stmt(?g, ?e) { funcall(?fun, 'test') }"
                + "@before "
                + "function us:before(?q) { "
                + "set(?fun = lambda(?x) { set(?count = 1 + ?count) } ) "
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Binding b = Binding.create().setVariable("?count", DatatypeMap.ZERO);
        Mappings map = exec.query(q, b);
        assertEquals(44, b.getVariable("?count").intValue());
    }

    @Test
    public void testEval() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "@event "
                + "select * "
                + "where {"
                + "values ?x { 1 } "
                + "filter (?x > 0)"
                + ""
                + "}"
                + "@filter "
                + "function us:filter(g, exp, bb) {"
                + "xt:setPublicDatatypeValue(eval(exp))"
                + "}";
        Mappings map = exec.query(q);
        assertEquals(true, DatatypeMap.getPublicDatatypeValue().booleanValue());

    }

    @Test
    public void testExtDT() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);

        String q2
                = "prefix spqr: <http://ns.inria.fr/sparql-extension/spqr/>"
                + "@event "
                + "select   "
                + "(spqr:digit(?res) as ?ope) "
                + "(spqr:digit(?val) as ?dig) "
                + ""
                + "where {"
                + ""
                + "bind ('II'^^us:romain * 'X'^^us:romain + 'V'^^us:romain as ?res) "
                + "bind (maplist(us:romain,  xt:iota(7)) as ?list)"
                + "bind (reduce (lambda(?x, ?y) { ?x + ?y }, ?list) as ?val)"
                + " "
                + "}"
                + "@type us:romain {"
                + "function us:eq(?x, ?y)  { (spqr:digit(?x) = spqr:digit(?y))} "
                + "function us:ne(?x, ?y)  { (spqr:digit(?x) != spqr:digit(?y))}"
                + "function us:lt(?x, ?y)  { (spqr:digit(?x) < spqr:digit(?y))}"
                + "function us:le(?x, ?y)  { (spqr:digit(?x) <= spqr:digit(?y))}"
                + "function us:gt(?x, ?y)  { (spqr:digit(?x) > spqr:digit(?y))}"
                + "function us:ge(?x, ?y)  { (spqr:digit(?x) >= spqr:digit(?y))} "
                + "function us:plus(?x, ?y)  { us:romain(spqr:digit(?x) + spqr:digit(?y))}"
                + "function us:minus(?x, ?y) { us:romain(spqr:digit(?x) - spqr:digit(?y))}"
                + "function us:mult(?x, ?y)  { us:romain(spqr:digit(?x) * spqr:digit(?y))}"
                + "function us:divis(?x, ?y) { us:romain(spqr:digit(?x) / spqr:digit(?y))} "
                + "}"
                + "function us:romain(?x) { strdt(spqr:romain(?x), us:romain)}";

        Mappings map = exec.query(q2);
        assertEquals(28, map.getValue("?dig").intValue());
        assertEquals(25, map.getValue("?ope").intValue());
    }

    @Test
    public void testOverload5() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '1 km'^^us:length, '2 km'^^us:length  . "
                + "us:t2 us:length '1000 m'^^us:length  ."
                + "}";

        String q = "@event  select * where {"
                + "select * where {"
                + "graph ?g { ?x ?p ?v . ?y ?p ?w  filter (?v = ?w) }"
                + "}"
                + "}"
                + "@type dt:error "
                + "function us:eq(?e, ?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (contains(?a, 'km'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:convertw(?a) {"
                + "if (contains(?a, 'kg'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:value(?a) {"
                + "xsd:integer(strbefore(?a, ' '))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(5, map.size());
    }

    @Test
    public void testOverload4() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '1 km'^^us:length, '2 km'^^us:length  . "
                + "us:t2 us:length '1000 m'^^us:length  ."
                + "}";

        String q = "@event  select * where {"
                + "select * where {"
                + "graph ?g { ?x ?p ?v . ?y ?p ?w  filter (?v = ?w) }"
                + "}"
                + "}"
                + "@type dt:error "
                + "function us:eq(?e, ?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (contains(?a, 'km'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:convertw(?a) {"
                + "if (contains(?a, 'kg'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:value(?a) {"
                + "xsd:integer(strbefore(?a, ' '))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(5, map.size());
    }

    @Test
    public void testOverload2() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '1 km'^^us:length, '2 km'^^us:length  . "
                + "us:t2 us:length '1000 m'^^us:length  ."
                + "}";

        String q = "@event "
                + "select * where {"
                + "select * where {"
                + "graph ?g { ?x ?p ?v . ?y ?p ?w  filter (?v = ?w) }"
                + "}"
                + "}"
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (contains(?a, 'km'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:convertw(?a) {"
                + "if (contains(?a, 'kg'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:value(?a) {"
                + "xsd:integer(strbefore(?a, ' '))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(5, map.size());
    }

    @Test
    public void testOverload() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '1 km'^^us:length, '2 km'^^us:length ; us:weight '1 kg'^^us:weight, '2 kg'^^us:weight . "
                + "us:t2 us:length '1000 m'^^us:length ; us:weight '1000 g'^^us:weight ."
                + "}";

        String q = "@event  select * where {"
                + "select * where {"
                + "graph ?g { ?x ?p ?v . ?y ?p ?w  filter (?v = ?w) }"
                + "}"
                + "}"
                + "@type us:length "
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "@type us:weight "
                + "function us:eq(?a, ?b) {"
                + "us:convertw(?a) = us:convertw(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (contains(?a, 'km'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:convertw(?a) {"
                + "if (contains(?a, 'kg'), 1000 * us:value(?a), us:value(?a))"
                + "}"
                + "function us:value(?a) {"
                + "xsd:integer(strbefore(?a, ' '))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(10, map.size());
    }

    @Test
    public void testGlobal2() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        // Other Test Sources src/test/resources/test.query
        Load ld = Load.create(g);
        String qq = "@event select (us:fun() as ?t) where {} "
                + "@before function us:before(?q) { set(?zz = 4) }"
                + "function us:fun() {let (select (us:test() as ?t) where {}) { ?t }}"
                + "function us:test() { set(?yy = 2 * ?zz + ?var + ?test) }";

        //exec.query(i);
        Binding b = Binding.create()
                .setVariable("?var", DatatypeMap.ONE)
                .setVariable("?test", DatatypeMap.TWO);
        Mappings map = exec.query(qq, b);
        assertEquals(11, b.getVariable("?yy").intValue());
        assertEquals(11, map.getValue("?t").intValue());
    }

    @Test
    public void testGlobal() throws EngineException, LoadException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        QueryLoad ql = QueryLoad.create();
        // Other Test Sources src/test/resources/test.query
        String q = "@event \n"
                + "select (us:test() as ?t) where {\n"
                + "\n"
                + "}\n"
                + "function us:test() {\n"
                + "    let (?g = \n"
                + "        construct {us:John rdfs:label 'John'} \n"
                + "        where { \n"
                // + "        bind (set(?var = 0) as ?tt)\n"
                + "        bind (us:global() as ?gg) \n"
                + "        bind (us:fun() as ?hh)\n"
                + "        }\n"
                + "    )  {\n"
                + "        ?g\n"
                + "    }\n"
                + "}\n"
                + "function us:fun() {\n"
                + "    let (select  (aggregate(us:global()) as ?agg) \n"
                + "         where {\n"
                + "         values ?x { unnest(xt:iota(3)) } }\n"
                + "         ) {\n"
                + "        ?agg\n"
                + "    }\n"
                + "}\n"
                + "function us:global() {\n"
                + "    set(?var = if (bound(?var), ?var + 1, 0)) ;"
                + "    ?var\n"
                + "}\n"
                + "@before\n"
                + "function us:before(?q) {\n"
                + "    set(?var = 0)\n"
                + "}\n";

        //exec.query(i);
        Mappings map = exec.query(q);
        DatatypeValue dt = map.getValue("?t");
        Binding b = exec.getCreateBinding();
        assertEquals(4, b.getVariable("?var").intValue());
    }

    @Test
    public void testOverload7() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '2'^^us:km "
                + "us:t2 us:length '1000'^^us:m "
                + "us:t3 us:length '2000'^^us:m, '1'^^us:km "
                + "}";

        String q = "@event select *  where {"
                + "graph ?g { ?x ?p ?v }"
                + "}"
                + "order by ?v "
                + "@init "
                + "function us:init(?q){"
                + "map(lambda(?list) {"
                + "let ((?fst | ?rst) = ?list) { map(xt:datatype, ?rst, ?fst)  }"
                + "}, us:datatypes())"
                + "}"
                + "function us:datatypes() {"
                + "let (?list = @((us:length us:m us:km))) "
                + "{ ?list }"
                + "}"
                + "function us:compare(?a, ?b) {"
                + "if (?a < ?b, -1, if (?a = ?b, us:compare(datatype(?a), datatype(?b)), 1))"
                + "}"
                + "function us:lt(?a, ?b) {"
                + "us:convert(?a) < us:convert(?b)"
                + "}"
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "@type us:km "
                + "function us:lt(?a, ?b) {"
                + "us:convert(?a) < us:convert(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (datatype(?a) = us:km, 1000 * us:value(?a), "
                + "if (datatype(?a) = us:m, us:value(?a),"
                + "if (datatype(?a) = us:length, us:valueunit(?a), us:value(?a))))"
                + "}"
                + "function us:value(?a) {"
                + "if (contains(?a, ' '), xsd:integer(strbefore(?a, ' ')), xsd:integer(?a))"
                + "}"
                + "function us:valueunit(?a) {"
                + "if (strafter(?a, ' ') = 'km', 1000 * us:value(?a), us:value(?a))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals("1", map.get(0).getValue("?v").stringValue());
        assertEquals("1000", map.get(1).getValue("?v").stringValue());
        assertEquals("2", map.get(2).getValue("?v").stringValue());
        assertEquals("2000", map.get(3).getValue("?v").stringValue());
    }

    @Test
    public void testOverload6() throws EngineException, LoadException {
        Graph g = Graph.create();

        String i = "insert data { "
                + "us:t1 us:length '1'^^us:km "
                + "us:t2 us:length '1000'^^us:m "
                + "us:t3 us:length '1000 m'^^us:length, '1 km'^^us:length "
                + "}";

        String q = "prefix spqr: <http://ns.inria.fr/sparql-extension/spqr/> "
                + " @event select *  where {"
                + "select *  where {"
                + "graph ?g { ?x ?p ?v . ?y ?p ?w  filter (?v = ?w) }"
                + "}"
                + "}"
                + "@init "
                + "function us:init(?q){"
                + "map(lambda((?x, ?y)) { us:datatype(?x, ?y) }, us:datatypes())"
                + "}"
                + "function us:datatypes() {"
                + "let (?list = @((us:km us:length)(us:m us:length))) {"
                + "?list"
                + "}"
                + "}"
                + "function us:datatype(?dt, ?sup) {"
                + "ds:datatype(xt:visitor(), ?dt, ?sup)"
                + "}"
                + "@type us:km "
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "@type us:m "
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "@type us:length "
                + "function us:eq(?a, ?b) {"
                + "us:convert(?a) = us:convert(?b)"
                + "}"
                + "function us:convert(?a) {"
                + "if (datatype(?a) = us:km, 1000 * us:value(?a), "
                + "if (datatype(?a) = us:m, us:value(?a),"
                + "if (datatype(?a) = us:length, us:valueunit(?a), us:value(?a))))"
                + "}"
                + "function us:value(?a) {"
                + "if (contains(?a, ' '), xsd:integer(strbefore(?a, ' ')), xsd:integer(?a))"
                + "}"
                + "function us:valueunit(?a) {"
                + "if (strafter(?a, ' ') = 'km', 1000 * us:value(?a), us:value(?a))"
                + "}";

        QueryProcess exec = QueryProcess.create(g);
        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(16, map.size());
    }
    
    
        @Test 
    public void testDistinct() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();
        QueryLoad ql = QueryLoad.create();
        // Other Test Sources src/test/resources/test.query
        String i = "insert data {"
                + "us:Jack owl:sameAs us:John ."
                + "us:John owl:sameAs us:Jack ."
                + "us:Jack foaf:knows us:Jim, us:Jesse ."
                + "us:John foaf:knows us:James, us:Jesse ."
                + "}";
        
          String q = "@event "
                  + "select * "
                  + "where {"
                  + "  ?a owl:sameAs ?b"
                  + "  {?a foaf:knows ?x} union {?b foaf:knows ?x}"
                  + "}"

                  + "@distinct "
                  + "function us:distinct(?q, ?m) {"
                  + "    let ((?a ?b ?x) = ?m) {"
                  + "        us:key(xt:add(xt:sort(xt:list(?a, ?b)), ?x))"
                  + "    }"
                  + "}"
                                
                  + "function us:key(?l) {"
                  + "    reduce(rq:concat, maplist(lambda(?e) { concat(xt:index(?e), '.') }, ?l))"
                  + "}"
                  + "";

        exec.query(i);
        Mappings map = exec.query(q);
        assertEquals(3, map.size());
    }
     
    
       @Test
    public void testOrderby() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();
        
         String i = "insert data {"
                 + "graph us:g1 { "
                 + "us:Jack foaf:name 'Jack'    ; "
                 + "foaf:knows us:Jim, us:Jesse "
                 + "}"
                 + "graph us:g2 { "
                 + "us:Jim foaf:knows us:Jack, us:James . "
                 + "us:James foaf:name 'James' ; foaf:knows us:Jesse "
                 + "}"
                 + "}";
        
        String q = "@event "
                + "select *"
                + "where {"
                + "  ?a foaf:knows ?y optional {?y foaf:name ?n}"
                + "}"
               
                + "@orderby  "
                + "function us:comparemap2(?m1, ?m2) {"
                + "    us:revcompare(xt:size(?m1) , xt:size(?m2))"
                + "}"
                              
                + "function us:compare(?x, ?y) {"
                + "    if (?x < ?y, -1, if (?x = ?y, 0, 1))"
                + "}"

                + "function us:revcompare(?x, ?y) {"
                + "    if (?x < ?y, 1, if (?x = ?y, 0, -1))"
                + "}";
        
        exec.query(i);
        Mappings map = exec.query(q);
        int j = 1;
        for (Mapping m : map) {
            assertEquals( (j++ > 2) ? 2 : 3, m.size());
        }
    }
     
    @Test
    public void testOrderby2() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();
        
         String i = "insert data {"
                 + "graph us:g1 { "
                 + "us:Jack foaf:name 'Jack'    ; "
                 + "foaf:knows us:Jim, us:Jesse "
                 + "}"
                 + "graph us:g2 { "
                 + "us:Jim foaf:knows us:Jack, us:James . "
                 + "us:James foaf:name 'James' ; foaf:knows us:Jesse "
                 + "}"
                 + "}";
        
        String q = "@event "
                + "select *"
                + "where {"
                + "  ?a foaf:knows ?y optional {?y foaf:name ?n}"
                + "}"
               
                + "@after "
                + "function us:after(?map){"
                + "xt:sort(?map, us:comparemap2)"
                + "}  "
                
                + "function us:comparemap2(?m1, ?m2) {"
                + "    us:revcompare(xt:size(?m1) , xt:size(?m2))"
                + "}"
                              
                + "function us:compare(?x, ?y) {"
                + "    if (?x < ?y, -1, if (?x = ?y, 0, 1))"
                + "}"

                + "function us:revcompare(?x, ?y) {"
                + "    if (?x < ?y, 1, if (?x = ?y, 0, -1))"
                + "}";
        
        exec.query(i);
        Mappings map = exec.query(q);
        int j = 1;
        for (Mapping m : map) {
            assertEquals( (j++ > 2) ? 2 : 3, m.size());
        }
    }
    
    
    
    public void testMap() throws EngineException, LoadException {
        QueryProcess exec = QueryProcess.create();

        String i = "insert data {"
                + "graph us:g1 { us:Jack foaf:name 'Jack'    ; foaf:knows us:Jim }"
                + "graph us:g2 { us:Jim foaf:knows us:James . us:James foaf:name 'James' }"
                + "}";

        String q = "@event "
                + "select *  "
                + "where {"
                + "graph ?g { ?x ?p ?y }"
                + "}"
                
                + "@before "
                + "function us:before(?q) {"
                + "st:set(st:map, xt:map())"
                + "}"
                
                + "@candidate "
                + "function us:candidate(?g, ?q, ?t) {"
                + "us:record(us:map(), ?t) "
                + "}"
                
                + "@after "
                + "function us:after(?m) {"
                + "java:setResult(?m, cast:node(reduce(rq:plus, maplist(lambda((?key, ?value)) { ?value }, us:map()))))"
                + "}"
                
                + "function us:record(?m, ?t) {"
                + "let ((?s ?p ?o) = ?t) {"
                + "xt:set(?m, ?p, coalesce(xt:get(?m, ?p) + 1, 1))"
                + "}"
                + "}"
                
                + "function us:map() {"
                + "st:get(st:map)"
                + "}"
                ;
        
        
        exec.query(i);
        
        Mappings map = exec.query(q);
        //assertEquals(4, map.getResult().getDatatypeValue().intValue());
    }   
    
    
      @Test
      public void testEvent() throws EngineException {
          String i = "insert data {"
                  + "us:John foaf:knows us:Jim ."
                  + "us:Jim foaf:knows us:James ."
                  + "}";
          
          String q = "@event "
                  + "select * (us:getcount() as ?c) "
                  + "where { ?x foaf:knows+ ?y }"
                  
                  + "@before "
                  + "function us:before(?q) {"
                  + "set(?count = 0)"
                  + "}"
                  
                  + "function us:getcount(){ ?count }"
                  
                  + "@step function us:step(?g, ?q, ?p, ?s, ?o) {"
                  + "set(?count = ?count + 1)"
                  + "}"
                  ;
          
          QueryProcess exec = QueryProcess.create(Graph.create());
          exec.setListPath(true);
          exec.query(i);
          Mappings map  = exec.query(q);
          DatatypeValue dt = map.get(2).getValue("?c");
          assertEquals(3, dt.intValue());
      }
    
     @Test
    public void testExtFun18() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = "prefix tr: <http://ns.inria.fr/sparql-datatype/triple#>"
                + "prefix gr: <http://ns.inria.fr/sparql-datatype/graph#>"
                + "@event @recursion "
                + "select (us:foo() as ?t) "
                + "where {}"

                + "function us:foo() {"
                + "let (?g1 = construct {?x ?p ?y} where {?x ?p ?y}, ?g2 = construct where {?x ?p ?y}) {"
                + "?g1 = ?g2"
                + "}"
                + "}"
                
                + "@type dt:graph dt:triple "
                + "function us:eq(g1, g2) {"
                + "letdyn (i = -1, list = maplist(rq:self, g2)) {"
                + "  mapevery(lambda(t1) { t1 = xt:get(list, set (i = i+1)) }, g1)"
                + "}"
                + "}"

                + "@type dt:graph dt:triple "
                + "function us:eqqqq(?g1, ?g2) {"
                + "  mapevery(lambda(?t1, ?t2) { ?t1 = ?t2 }, ?g1, ?g2)"
                + "}"
                
                //+ "@limit function us:limit(?m) { xt:print('limit') ; return (false) }"
                
//                + "@type  dt:triple "
//                + "function us:eq(?g1, ?g2) {"
//                + "  mapevery(lambda(?t1, ?t2) { ?t1 = ?t2 }, ?g1, ?g2)"
//                + "}"

               ;

        Graph g = createGraph();
        //QuerySolverVisitor.REENTRANT_DEFAULT = true;
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        //System.out.println(map);
        assertEquals(true, dt.booleanValue());
    }

    //@Test
    public void testExtFun17() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = "prefix tr: <http://ns.inria.fr/sparql-datatype/triple#>"
                + "@event "
                + "select (us:foo() as ?t) "
                + "where {}"

                + "function us:foo() {"
                + "mapevery(lambda(?t) { ?t = ?t }, query(construct where {?x ?p ?y}))"
                + "}"

                + "@type dt:triple function us:eq(?t1, ?t2) {"
                + "  mapevery(lambda(?x, ?y) { xt:print(?x, ?y) ; ?x = ?y }, ?t1, ?t2)"
                + "}";

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(true, dt.booleanValue());
    }
    
      Graph createGraph() {
        Graph g = Graph.create();
        
        return g;
    }
    
       @Test
    public void testUnnestContext() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "@event select * where {"
                + "bind (st:set(st:test, 'test') as ?test) "
                + "values (?key ?val) { unnest(xt:context()) } "
                + "}"
                + "@before function xt:start(?q){ st:set(st:count, 0)  } ";

        Mappings map = exec.query(q);
        assertEquals(2, map.size());
    }
    
    
      @Test
    public void testEventCall() throws EngineException {
        Graph g = GraphStore.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "@event select * where {"
                + "?x ?p ?y"
                + "}"
                + "@produce function xt:produce(?g, ?t){xt:list(?t)} "
                + "@before function xt:start(?q){ st:set(st:count, 0) ; us:count() } "
                + "@result function xt:result(?map, ?m){us:count()}"
                + "@after function xt:finish(?m){us:count()}"
                + "function us:count(){st:set(st:count, 1 + st:get(st:count))}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        Transformer t = (Transformer) map.getQuery().getTransformer();
        Context c = (Context) map.getContext();
        IDatatype dt = c.getName("count");
        assertEquals(3, dt.intValue());
    }
    
  @Test
    public void testCallback() throws EngineException {
        Graph g = Graph.create();
        QueryProcess exec = QueryProcess.create(g);
        String q = "@event select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
                + "filter  exists {?x foaf:knows ?y} "
                + "}"
                + "@produce function xt:produce(?g, ?q){"
                + "xt:list(?q)"
                + "}";

        String q2 = "@event select * "
                + "where {"
                + "?x foaf:name 'John' ; rdf:value 2 "
                + "?y foaf:name 'John' "
                + "filter not exists {?x foaf:knows ?y} "
                + "}"
                + "@produce function xt:produce(?g, ?q){"
                + "xt:list(?q)"
                + "}";

        Mappings map = exec.query(q);
        assertEquals(1, map.size());
        map = exec.query(q2);
        assertEquals(0, map.size());
    }

    
    
    
    
    
     //@Test
    public void testConstruct() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);

        String init = "insert data {"
                + "[] rdf:value 1, 2, 3 ."
                + "[] rdfs:label 'a', 'b', 'c' ."
                + "}";

        String qe = "@event select * where {"
                + "?x ?p ?y"
                + "}"
                + "@produce function xt:produce(?g, ?q){"
                + "let (?g = construct where {?x rdfs:label ?y}){"
                + "?g"
                + "}}";

        exec.query(init);
        Mappings map = exec.query(qe);
        assertEquals(3, map.size());
    }

    
    
     // @Test
    public void testExtFun19() throws EngineException {

        String init = "prefix ex: <http://example.org/> "
                + "insert data {"
                + "ex:John rdf:value 1 ; rdfs:label 2"
                + "}";

        String q = "prefix sol: <http://ns.inria.fr/sparql-datatype/mappings#>"
                + "prefix map: <http://ns.inria.fr/sparql-datatype/mapping#>"
                + "prefix ls: <http://ns.inria.fr/sparql-datatype/list#>"
                + "@event @recursion "
                + "select (us:foo() as ?t) "
                + "where {}"

                + "function us:foo() {"
                + "query(select ?x ?y where {?x ?p ?y}) = query(select ?x ?y where {?x ?q ?y})"
                + "}"

                + "@type dt:mappings "
                + "function us:eq(?s1, ?s2) {"
                + " xt:size(?s1) = xt:size(?s2) &&  mapevery(lambda(?t1, ?t2) { ?t1 = ?t2 }, ?s1, ?s2)"
                + "}"

                + "@type dt:mapping "
                + "function us:eq(?m1, ?m2) {"
                + "sol:equal(?m1, ?m2)"
                + "}"

//                + "function ls:equal(?l1, ?l2) {"
//                + "sol:equal(?l1, ?l2)"
//                + "}"
                ;

        Graph g = createGraph();
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);
        Mappings map = exec.query(q);
        IDatatype dt = (IDatatype) map.getValue("?t");
        assertEquals(true, dt.booleanValue());
    }


    
    String strValue(Mappings m, String v) {
        return ((IDatatype) m.get(0).getValue(v)).stringValue();
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
        return (IDatatype) n.getValue();
    }

}
