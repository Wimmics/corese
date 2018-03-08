package fr.inria.corese.kgengine.junit;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.TripleFormat;
import static fr.inria.corese.kgengine.junit.TestQuery1.data;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestQuery2 {
    
    
    @Test
    public void test40() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person "
                + "foaf:knows rdfs:range foaf:Person "
                + "ex:a foaf:knows ex:b "
                + "ex:b rdfs:seeAlso ex:c "
                + "ex:c foaf:knows ex:d "
                + "ex:d rdfs:seeAlso ex:e "
                + "ex:a ex:rel ex:b "
                + "ex:b ex:rel ex:c "
                + "ex:c ex:rel ex:d "
                + "ex:c ex:rel ex:e "
                + "}";

        String query =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * where {"
                + "?x (foaf:knows/rdfs:seeAlso @[a foaf:Person] || ex:rel+)+ ?y"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 1, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
      @Test
    public void test3() {
        String query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  * (kg:similarity() as ?sim) where {"
                + "?x rdf:type c:Engineer "
                + "?x c:hasCreated ?doc "
                + "?doc rdf:type c:WebPage"
                + "}"
                + "order by desc(?sim)"
                + "pragma {kg:match kg:mode 'general'}";
        Graph graph = TestQuery1.graph(); 
        QueryProcess exec = QueryProcess.create(graph);
        try {
            Mappings map = exec.query(query);
            IDatatype dt = (IDatatype) map.getValue("?x");

            assertEquals("Result", dt.getLabel(),
                    "http://www-sop.inria.fr/acacia/personnel/Fabien.Gandon/");

            assertEquals("Result", 39, map.size());


        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }
    }
    
     @Test
    public void test44() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "insert data {"
                + "foaf:knows rdfs:domain foaf:Person "
                + "foaf:knows rdfs:range  foaf:Person "
                + "ex:a foaf:knows ex:b "
                + "ex:b foaf:knows ex:c "
                + "ex:a rdfs:seeAlso ex:b "
                + "ex:b rdfs:seeAlso ex:c "
                + "}";

        String query =
                "prefix ex: <http://test/> "
                + "prefix foaf: <http://foaf/> "
                + "select * (kg:pathWeight($path) as ?w) where {"
                + "ex:a ( foaf:knows@2 | rdfs:seeAlso@1 )@[a rdfs:Resource] +  :: $path ?x "
                + "filter(kg:pathWeight($path) = 3)"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 1, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test29() {

        String query ="prefix c: <http://www.inria.fr/acacia/comma#>" +
                "select  "
                + "where {"
                + "?class rdfs:subClassOf+ :: $path c:Person "
                + "graph $path {?a ?p c:Person} "
                + "?x rdf:type/rdfs:subClassOf+ :: $path2 ?class "
                + "graph $path2 {?x rdf:type ?c } "
                + "?x c:FirstName ?n "
                + "}";

        try {
        Graph graph = TestQuery1.graph(); 

            QueryProcess exec = QueryProcess.create(graph);
            Mappings map = exec.query(query);
            //IDatatype dt = getValue(map, "?max");
            assertEquals("Result", 43, map.size());

        } catch (EngineException e) {
            assertEquals("Result", true, e);
        }

    }


     @Test
    public void test45() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix c: <http://test/> "
                + "insert data {"
                + "tuple(c:name <John>  'John' 1)"
                + "tuple(c:name <Jim>   'Jim' 1)"
                + "tuple(c:name <James> 'James' )"
                + "}";

        String query =
                "prefix c: <http://test/> "
                + "select * where {"
                + "graph ?g { tuple(c:name ?x ?n 1) }"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test451() {

        Graph graph = Graph.create(true);
        QueryProcess exec = QueryProcess.create(graph);

        String init =
                "prefix c: <http://test/> "
                + "insert data {"
                + "tuple(c:name <John>  'John' 1)"
                + "tuple(c:name <John>  'John' 2)"
                + "}";

        String query =
                "prefix c: <http://test/> "
                + "select * where {"
                + "graph ?g { tuple(c:name ?x ?n ) }"
                + "}";

        try {
            exec.query(init);
            Mappings map = exec.query(query);
            assertEquals("Result", 2, map.size());


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void test46() throws LoadException {

        Graph graph = Graph.create(true);
        Load load = Load.create(graph);
        load.parse(data + "test/test1.ttl");
        load.parse(data + "test/test1.rul");

        RuleEngine re = load.getRuleEngine();
        QueryProcess exec = QueryProcess.create(graph);

        String query =
                "prefix c: <http://www.inria.fr/test/> "
                + "select * where {"
                + "graph ?g { tuple(c:name ?x ?n 1) }"
                + "}";

        String query2 =
                "prefix c: <http://www.inria.fr/test/> "
                + "select * where {"
                + "graph ?g { "
                + "tuple(c:name ?x ?n 1) "
                + "tuple(c:name ?y ?m 2) "
                + "}"
                + "}";

        String query3 =
                "prefix c: <http://www.inria.fr/test/> "
                + "select * where {"
                + "graph ?g { "
                + "tuple(c:name ?x 'JohnJohn' 1) "
                + "tuple(c:name ?x  'John' ?v)"
                + "}"
                + "}";


        String query4 =
                "prefix c: <http://www.inria.fr/test/> "
                + "construct {"
                + "tuple(c:name ?x 'JohnJohn' ?y) "
                + "} where {"
                + "graph ?g { "
                + "tuple(c:name ?x 'JohnJohn' ?y) "
                + "tuple(c:name ?x  'John' ?v)"
                + "}"
                + "}";


        try {
            Mappings map = exec.query(query);
            assertEquals("Result", 1, map.size());

            map = exec.query(query2);
            assertEquals("Result", 1, map.size());

            re.process();

            map = exec.query(query3);
            assertEquals("Result", 1, map.size());

            map = exec.query(query4);
            TripleFormat tf = TripleFormat.create(exec.getGraph(map));
            //System.out.println(tf);


        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
     @Test
    public void testProv() throws EngineException {

        String init =  "insert data { <John> foaf:knows <Jim>, <James> }";

        Graph g = Graph.create();
        g.setTuple(true);
        QueryProcess exec = QueryProcess.create(g);
        exec.query(init);

        for (Entity ent : g.getEdges()) {
            ent.setProvenance(g);
        }

        String query =  "select * where {"
                + "tuple(foaf:knows <John>  ?v ?prov) "
                + "graph ?prov { <John>  ?p ?v }"
                + "}";

        Mappings map = exec.query(query);

        assertEquals("result", 2, map.size());

        for (Mapping m : map) {
            for (Entity ent : m.getEdges()) {
                assertEquals("result", true, ent.getProvenance() != null && ent.getProvenance() instanceof Graph);
            }
        }

    }
    
}
