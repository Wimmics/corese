/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class GroupingTest {

    public GroupingTest() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void hello() throws EngineException{
         
        String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?z WHERE"
                + "{"
                + "     ?x foaf:knows ?y ."
                + "     ?y foaf:knows ?z ."
                + "     OPTIONAL {?x foaf:mbox ?m}"
                //                + " FILTER ((?x ~ 'Alban') || (?y ~ 'Tram'))"
                + " FILTER (?x ~ 'Alban')"
                + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";

        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine) ef.newInstance();
        
        ASTQuery query =  ASTQuery.create(sparqlQuery);
        
        for (int i = 0 ; i < query.getBody().size() ; i++) {
            Exp exp = query.getBody().get(i);
            System.out.println(exp.toString());
        }
        
    }
    
}
