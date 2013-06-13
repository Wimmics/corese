/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class RestEndpointTest {

    public RestEndpointTest() {
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
    public void hello() throws URISyntaxException, MalformedURLException, IOException {

        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?p ?y WHERE"
                + "{"
                + "     ?x ?p ?y ."
                + "}"
                + "     LIMIT 100 ";

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8080/kgram"));
//        // Fluent interfaces
        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
        System.out.println(service.path("sparql").path("load").queryParam("remote_path", "http://nyx.unice.fr/~gaignard/data/neurolog.rdf").post(String.class));
        System.out.println(service.path("sparql").queryParam("query", query).accept("application/sparql-results+xml").get(String.class));

        // Get plain text
//        System.out.println(service.path("rest").path("hello").accept(MediaType.TEXT_PLAIN).get(String.class));
//        // Get XML
//        System.out.println(service.path("rest").path("hello").accept(MediaType.TEXT_XML).get(String.class));
//        // The HTML
//        System.out.println(service.path("rest").path("hello").accept(MediaType.TEXT_HTML).get(String.class));
    }
}
