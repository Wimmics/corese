/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgengine.kgramenv.util.QueryExec;
import java.util.Enumeration;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gaignard
 */
public class ResultsManagementTest {

    public ResultsManagementTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
            
    @Test
    @Ignore
    public void hello() {
        try {
            String insert1 = "insert data {"
                    + "<http://i3s/Alban> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Javier>"
                    + "}";

            String insert2 = "insert data {"
                    + "<http://i3s/Filip> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Javier>"
                    + "<http://i3s/Filip> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Tram>"
                    + "<http://i3s/Javier> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Filip>"
                    + "<http://i3s/Javier> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Tram>"
                    + "<http://i3s/Tram> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Filip>"
                    + "<http://i3s/Tram> <http://xmlns.com/foaf/0.1/knows> <http://i3s/Javier>"
                    + "}";

            String sparqlQuery = "select * where {?x ?t ?y}";

            EngineFactory ef = new EngineFactory();
            IEngine engine = ef.newInstance();

            engine.SPARQLQuery(insert1);
            engine.SPARQLQuery(insert2);

            QueryExec qExec = QueryExec.create();
            qExec.add(engine);

            IResults res = qExec.SPARQLQuery(sparqlQuery);
            String[] variables = res.getVariables();
            

            for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
                IResult r = en.nextElement();
                HashMap<String, String> result = new HashMap<String, String>();
                for (String var : variables) {
                    if (r.isBound(var)) {
                        IResultValue[] values = r.getResultValues(var);
                        for (int j = 0; j < values.length; j++) {
//                            System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                        }
                    } else {
//                        System.out.println(var + " = Not bound");
                    }
                }

            }
            assertEquals(9,res.size());

        } catch (EngineException ex) {
            ex.printStackTrace();
        }

    }
}
