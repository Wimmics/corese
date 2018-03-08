package fr.inria.corese.server.webservice;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.QueryProcessDQP;
import fr.inria.corese.kgdqp.core.WSImplem;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class InteropVirtuosoTest {

    public InteropVirtuosoTest() {
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
    public void simpleQuery() throws URISyntaxException, MalformedURLException, IOException {

        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?p ?y WHERE"
                + "{"
                + "     ?x ?p ?y ."
                + "}"
                + "     LIMIT 10";

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8890"));

        System.out.println(service.path("sparql").queryParam("query", query).accept("application/sparql-results+xml").get(String.class));
//        System.out.println(service.path("sparql").queryParam("query", query).accept("application/json").get(String.class));
    }

    @Test
    @Ignore
    public void fedQuery() throws URISyntaxException, MalformedURLException, IOException, EngineException {

        String query = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>\n"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n"
                + "SELECT ?nom ?popTotale WHERE { \n"
                + "    ?region igeo:codeRegion \"24\" .\n"
                + "    ?region igeo:subdivisionDirecte ?departement .\n"
                + "    ?departement igeo:nom ?nom .\n"
                + "    ?departement idemo:population ?popLeg .\n"
                + "    ?popLeg idemo:populationTotale ?popTotale .\n"
                + "} ORDER BY ?popTotale";

        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.addRemote(new URL("http://localhost:" + 8890 + "/sparql"), WSImplem.REST);
        exec.addRemote(new URL("http://localhost:" + 8891 + "/kgram/sparql"), WSImplem.REST);
//        exec.addRemote(new URL("http://localhost:" + 8892 + "/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(query);
        int dqpSize = map.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println("Results in " + time + "ms");
        System.out.println("Results size " + dqpSize);
        System.out.println("");
        Assert.assertEquals(6, map.size());
    }
}
