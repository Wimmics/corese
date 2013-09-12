/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class NamedGraphs {

//    String q1 = "select * where { ?x ?p ?y }";
    String q1 = "select ?x ?p ?y from <http://graph/alice> where { ?x ?p ?y }";
    String q2 = "select ?x ?p ?y from named <http://graph/alice> where { ?x ?p ?y }";
    String q2bis = "select ?g ?x ?p ?y from named <http://graph/alice> where { graph ?g {?x ?p ?y } }";
    String q3 = "select ?g ?x ?p ?y from named <http://graph/alice> from named <http://graph/bob> where { graph ?g {?x ?p ?y }}";

    public NamedGraphs() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void ngTest() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcess qp = QueryProcess.create(g);
        Load ld = Load.create(g);
        ld.load(NamedGraphs.class.getClassLoader().getResourceAsStream("ng-persons.ttl"), "toto.ttl");
        System.out.println(g.size());
        System.out.println("");
//        System.out.println(TripleFormat.create(g, true).toString());

        ArrayList<String> queries = new ArrayList<String>();
        queries.add(q1);
        queries.add(q2);
        queries.add(q2bis);
        queries.add(q3);
        
        System.out.println("Queries :\n");
        for (String q : queries) {
            Mappings maps = qp.query(q) ; 
            System.out.println(maps);
//            System.out.println(RDFFormat.create(maps).toString());
            System.out.println("");    
        }
    }
    
    @Test
    public void ngDqpTest() throws LoadException, EngineException {
        Graph g = Graph.create();
        QueryProcessDQP qp = QueryProcessDQP.create(g);
        Load ld = Load.create(g);
        ld.load(NamedGraphs.class.getClassLoader().getResourceAsStream("ng-persons.ttl"), "toto.ttl");
        System.out.println(g.size());
        System.out.println("");
//        System.out.println(TripleFormat.create(g, true).toString());

        ArrayList<String> queries = new ArrayList<String>();
        queries.add(q1);
        queries.add(q2);
        queries.add(q2bis);
        queries.add(q3);
        
        System.out.println("Queries :\n");
        for (String q : queries) {
            Mappings maps = qp.query(q) ; 
            System.out.println(maps);
//            System.out.println(RDFFormat.create(maps).toString());
            System.out.println("");    
        }
    }
}