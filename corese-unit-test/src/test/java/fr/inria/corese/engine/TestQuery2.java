package fr.inria.corese.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author corby
 */
public class TestQuery2 {
        static String data = Thread.currentThread().getContextClassLoader().getResource("data/").getPath();

        @Test
        public void test5() throws EngineException, SparqlException {
                String i = "insert data {"
                                + "us:John foaf:knows us:Jim, us:James ;"
                                + "foaf:age 20 ."
                                + "us:Jim foaf:knows us:Jack ;"
                                + "foaf:age 30 }";
                String q = "select * where {?s foaf:knows ?o ; foaf:age ?a}";

                Graph g = Graph.create();
                QueryProcess exec = QueryProcess.create(g);
                exec.query(i);

                Mappings map = exec.query(q);

                String q1 = "select * where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "values (?s ?n) {(us:John 'John')}";

                Query qq1 = exec.compile(q1);
                Mappings map2 = exec.modifier(qq1, map);
                assertEquals(2, map2.size());

        }

        @Test
        public void test4() throws EngineException, SparqlException {
                String i = "insert data {"
                                + "us:John foaf:knows us:Jim, us:James ;"
                                + "foaf:age 20 ."
                                + "us:Jim foaf:knows us:Jack ;"
                                + "foaf:age 30 }";
                String q = "select * where {?s foaf:knows ?o ; foaf:age ?a}";

                Graph g = Graph.create();
                QueryProcess exec = QueryProcess.create(g);
                exec.query(i);

                Mappings map = exec.query(q);

                String q1 = "select distinct ?s where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}";

                Query qq1 = exec.compile(q1);
                exec.modifier(qq1, map);
                assertEquals(2, map.size());

        }

        @Test
        public void test3() throws EngineException, SparqlException {
                String i = "insert data {"
                                + "us:John foaf:knows us:Jim, us:James ;"
                                + "foaf:age 20 ."
                                + "us:Jim foaf:knows us:Jack ;"
                                + "foaf:age 30 }";
                String q = "select * where {?s foaf:knows ?o ; foaf:age ?a}";

                Graph g = Graph.create();
                QueryProcess exec = QueryProcess.create(g);
                exec.query(i);

                Mappings map = exec.query(q);

                String q1 = "select * (count(?o) as ?c) where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "group by ?s "
                                + "order by ?s "
                                + "having(?c > 1)";

                Query qq1 = exec.compile(q1);
                exec.modifier(qq1, map);
                assertEquals(2, map.getValue("?c").intValue());

        }

        @Test
        public void test2() throws EngineException, SparqlException {
                String i = "insert data {"
                                + "us:John foaf:knows us:Jim, us:James ;"
                                + "foaf:age 20 ."
                                + "us:Jim foaf:knows us:Jack ;"
                                + "foaf:age 30 }";
                String q = "select * where {?s foaf:knows ?o ; foaf:age ?a}";

                Graph g = Graph.create();
                QueryProcess exec = QueryProcess.create(g);
                exec.query(i);

                Mappings map = exec.query(q);

                String q1 = "select * (count(?o) as ?c) where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "group by ?s "
                                + "order by ?s ";

                Query qq1 = exec.compile(q1);
                exec.modifier(qq1, map);
                assertEquals(1, map.getValue("?c").intValue());
                assertEquals(2, map.get(1).getValue("?c").intValue());
        }

        @Test
        public void test1() throws EngineException, SparqlException {
                String i = "insert data {"
                                + "us:John foaf:knows us:Jim, us:James ;"
                                + "foaf:age 20 ."
                                + "us:Jim foaf:knows us:Jack ;"
                                + "foaf:age 30 }";
                String q = "select * where {?s foaf:knows ?o ; foaf:age ?a}";

                Graph g = Graph.create();
                QueryProcess exec = QueryProcess.create(g);
                exec.query(i);

                Mappings map = exec.query(q);

                String q1 = "select * where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "order by ?o";

                Query qq1 = exec.compile(q1);
                exec.modifier(qq1, map);
                assertEquals(uri("Jim"), map.getValue("?s").getLabel());

                String q2 = "select * where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "order by (?a)";

                Query qq2 = exec.compile(q2);
                exec.modifier(qq2, map);
                assertEquals(20, map.getValue("?a").intValue());

                String q3 = "select * where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "order by desc(?a)";

                Query qq3 = exec.compile(q3);
                exec.modifier(qq3, map);
                assertEquals(30, map.getValue("?a").intValue());

                String q4 = "select * (2*?a as ?ad) where {"
                                + "?s foaf:knows ?o ; foaf:age ?a}"
                                + "order by (?a)";

                Query qq4 = exec.compile(q4);
                exec.modifier(qq4, map);
                assertEquals(40, map.getValue("?ad").intValue());
        }

        String uri(String str) {
                return NSManager.USER + str;
        }

}
