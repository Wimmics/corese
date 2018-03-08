package fr.inria.corese.server.webservice;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.QueryExecDQP;
import fr.inria.corese.kgengine.GraphEngine;
import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author gaignard
 */
public class DBPediaPersonsStandaloneTest {

    String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
            + "SELECT distinct ?x ?t ?name ?date WHERE \n"
            + "{"
            + "     ?x foaf:name ?name ."
            //                + "     ?x ?y ?name2 ."
            //            + "     ?x dbpedia:birthPlace ?place ."
            + "     ?x dbpedia:birthDate ?date ."
            //                + "     ?y foaf:name ?name2 ."
            //                + "     ?z foaf:name ?name3 ."
            //                + "     OPTIONAL {?x foaf:mbox ?m}"
            + "     ?x rdf:type <http://xmlns.com/foaf/0.1/Person> ."
            + " FILTER ((?name ~ 'Bobby A') )"
            + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";
    
//    String sparqlQuery318 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
//            + "PREFIX dbpedia: <http://dbpedia.org/ontology/> \n"
//            + "SELECT distinct ?x ?name ?date WHERE \n"
//            + "{"
//            + "     ?x foaf:name ?name ."
//            + " OPTIONAL     {?x dbpedia:birthDate ?date }."
//            + " FILTER ((?x ~ 'Bob') )"
//            + "}";

    public DBPediaPersonsStandaloneTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    public void remoteDBPediaQuery() throws EngineException, MalformedURLException, IOException {

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        String rep1 = "http://nyx.unice.fr/~gaignard/data/persondata.1.rdf";
        String rep2 = "http://nyx.unice.fr/~gaignard/data/persondata.2.rdf";

        engine.load(rep1);
        engine.load(rep2);

        QueryExecDQP exec = QueryExecDQP.create(engine);

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlQuery);
        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        GraphEngine gEng = (GraphEngine) engine;
        System.out.println("Graph size " + gEng.getGraph().size());
        System.out.println("Results size " + res.size());
        String[] variables = res.getVariables();
        
        assertEquals(3,res.size());

        for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
            IResult r = en.nextElement();
            HashMap<String, String> result = new HashMap<String, String>();
            for (String var : variables) {
                if (r.isBound(var)) {
                    IResultValue[] values = r.getResultValues(var);
                    for (int j = 0; j < values.length; j++) {
                        System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                    }
                } else {
                    //System.out.println(var + " = Not bound");
                }
            }
        }
        System.out.println(sw.getTime() + " ms");
    }
}
