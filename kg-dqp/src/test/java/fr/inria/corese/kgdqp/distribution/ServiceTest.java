/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.corese.kgdqp.core.QueryProcessDQP;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.commons.lang.time.StopWatch;
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
public class ServiceTest {

    public ServiceTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    @Ignore
    public void serviceDebugTest() throws EngineException, MalformedURLException, IOException {

        String query = "PREFIX idemo:<http://rdf.insee.fr/def/demo#> \n"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n"
                + "SELECT ?region  WHERE { \n"
                + "    SERVICE <http://localhost:9091/kgram/sparql> {\n"
                + "         ?region igeo:codeRegion \"24\" .\n"
                + "         ?region igeo:subdivisionDirecte ?departement .\n"
                + "    }\n"
                + "} ";

        //---------------Service grouping-----------------------
        Graph g1 = Graph.create();
//        execDQP1.addVisitor(new ServiceQueryVisitorPar(execDQP1));
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(g1, sProv, false);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps1 = execDQP.query(query);
        System.out.println(maps1);
        System.out.println("[service] Results size " + maps1.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
    }
}
