package test.sparql.benchmark.bsbm;

import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This test aims at testing if the Corese engine can return correct results
 * agaist BSBM queries by comparing to Jena and Sesame
 *
 * Before running the test, we need to 1. start up the three endpoints server
 * manually 2. upload the data set needed for testing
 *
 * TestCoreseResultsBSBM.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 28 nov. 2014
 */
public class TestCoreseResultsBSBM {

    private static final String ENDPOINT_CORESE = "http://localhost:8080/kgram/sparql";
    private static final String ENDPOINT_JENA = "http://localhost:3030/ds/query";
    private static final String ENDPOINT_SESAME = "http://localhost:9090/openrdf-sesame/repositories/bsbm";

    public static void main(String[] args) {
        TestCoreseResultsBSBM tt = new TestCoreseResultsBSBM();
       //tt.run(ENDPOINT_JENA, BSBMQueries.Bi_Q03);
        //tt.run(ENDPOINT_SESAME, BSBMQueries.Bi_Q03);
        //tt.run(ENDPOINT_CORESE, BSBMQueries.Bi_Q03);

        tt.testSizeOfDataset();
    }

    @DataProvider(name = "data")
    public Object[][] data(Method mt) {
        String test = mt.getName();
        if ("testExploreUseCase".equals(test)) {
            return BSBMQueries.EXPLORE_USE_CASE;
        } else if ("testBiUseCase".equals(test)) {
            return BSBMQueries.BI_USE_CASE;
        }else if ("testBiUseCaseDebug".equals(test)) {
            return BSBMQueries.BI_USE_CASE2;
        }
        return null;
    }

    //@Test
    public void testSizeOfDataset() {
        runOneTest("BSBMQueries.DF_Q01", BSBMQueries.DF_Q01);
    }

    //use case "Explore"
    @Test(dataProvider = "data")
    public void testExploreUseCase(String title, String query) {
        runOneTest(title, query);
    }

    //use case "BI: business intellegence"
    @Test(dataProvider = "data")
    public void testBiUseCase(String title, String query) {
        runOneTest(title, query);
    }

    //use case "BI: business intellegence" debug
    //@Test(dataProvider = "data")
    public void testBiUseCaseDebug(String title, String query) {
        runOneTest(title, query);
    }

    
    private void runOneTest(String title, String query) {

        int rJena = run(ENDPOINT_JENA, query);
        int rSesame = run(ENDPOINT_SESAME, query);
        int rCorese = run(ENDPOINT_CORESE, query);

        System.out.println("\n##### " + title + " ##### \n" + query);
        System.out.println(ENDPOINT_CORESE + ":\t" + rCorese);
        System.out.println(ENDPOINT_JENA + ":\t" + rJena);
        System.out.println(ENDPOINT_SESAME + ":\t" + rSesame);

        Assert.assertEquals(title + " Corse == Jena?", rCorese, rJena);
        Assert.assertEquals(title + " Corse == Sesame?", rCorese, rSesame);
        Assert.assertEquals(title + " Jena == Sesame?", rJena, rSesame);

    }

    private int run(String sparqlEndpoint, String sQuery) {
        QueryEngineHTTP httpQuery = new QueryEngineHTTP(sparqlEndpoint, sQuery);
        return ResultSetFactory.makeRewindable(httpQuery.execSelect()).size();
    }
}
