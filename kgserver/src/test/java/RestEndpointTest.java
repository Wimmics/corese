/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MultivaluedMap;
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
    @Ignore
    public void query() throws URISyntaxException, MalformedURLException, IOException {

        String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?p ?y WHERE"
                + "{"
                + "     ?x ?p ?y ."
                + "}"
                + "     LIMIT 10";

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8080/kgram"));
        
        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
        
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("remote_path", "http://nyx.unice.fr/~gaignard/data/neurolog.rdf");
//        formData.add("remote_path", "/Users/gaignard/Desktop/bsbmtools-0.2/dataset.ttl");
        service.path("sparql").path("load").post(formData);
        System.out.println(service.path("sparql").queryParam("query", query).accept("application/sparql-results+xml").get(String.class));
        System.out.println(service.path("sparql").queryParam("query", query).accept("application/json").get(String.class));
    }

    @Test
    @Ignore
    public void update() throws URISyntaxException, MalformedURLException, IOException {

        String insertData1 = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA\n"
                + "{ <http://example/book1> dc:title    \"First book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "}";
        String insertData2 = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA\n"
                + "{ <http://example/book3> dc:title    \"A new book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "}";
        String count = "SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }";

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8080/kgram"));

        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
        
        //First POST of the SPARQL protocol
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("update", insertData1);
        service.path("sparql").path("update").type("application/x-www-form-urlencoded").post(formData);
        System.out.println(service.path("sparql").queryParam("query", count).accept("application/sparql-results+xml").get(String.class));

        //Second POST of the SPARQL protocol
        service.path("sparql").path("update").type("application/sparql-update").entity(insertData2).post();
        System.out.println(service.path("sparql").queryParam("query", count).accept("application/sparql-results+xml").get(String.class));
    }
    
    @Test
    @Ignore
    public void updateNG() throws URISyntaxException, MalformedURLException, IOException {

        String insertDataNG = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
                + "INSERT DATA  \n"
                + "{    GRAPH <http://firstStore> {"
                + "                         <http://example/book1> dc:title    \"First book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "     }"
                + "     GRAPH <http://secondStore> {"
                + "                         <http://example/book2> dc:title    \"Second book\" ;\n"
                + "                         dc:creator  \"A.N.Other\" .\n"
                + "     }"
                + "}";
        
        String allWithGraph = "SELECT * WHERE {GRAPH ?g {?x ?p ?y}}";

        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8080/kgram"));

        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
        MultivaluedMap resetParams = new MultivaluedMapImpl();
        resetParams.add("entailments", "true");
        System.out.println(service.path("sparql").path("reset").post(String.class,resetParams));
        
        
        //First POST of the SPARQL protocol
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("update", insertDataNG);
        service.path("sparql").path("update").type("application/x-www-form-urlencoded").post(formData);
        System.out.println(service.path("sparql").queryParam("query", allWithGraph).accept("application/sparql-results+xml").get(String.class));

        
        String selectBook = "SELECT * WHERE {GRAPH ?g {?x ?p ?y}}";
        System.out.println(service.path("sparql").queryParam("query", selectBook).queryParam("named-graph-uri", "http://secondStore").accept("application/sparql-results+xml").get(String.class));
    }
}
