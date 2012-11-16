/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.sparqlendpoint;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class LoadN3 {

    public LoadN3() {
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
    public void hello() throws UnsupportedEncodingException, MalformedURLException, IOException {
        String url = "http://localhost:8080/kgendpoint";
//        SPARQLEndpointClient endpoint1 = new SPARQLEndpointClient("http://localhost:8080/kgendpoint");

        URL queryURL = new URL(url + "?load=" + URLEncoder.encode("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-chebi/chebi.n3", "UTF-8"));
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("GET");
        
        InputStream stream = urlConn.getInputStream();
        InputStreamReader r = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();

        String str = null;
        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }
        
        System.out.println(sb.toString());
        System.out.println("");
        System.out.println("done");
        
    }
}
