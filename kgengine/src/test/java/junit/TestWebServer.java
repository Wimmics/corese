package junit;

import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.QueryLoad;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;


/**
 * Test update/load/query in parallel threads
 *
 */
public class TestWebServer extends Thread {

    //static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
    static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
    Graph graph;
    int index;
    private String port = "8080";

    TestWebServer(int n) {
        index = n;
    }

    TestWebServer() {
    }

    public static void main(String[] args) {
        new TestWebServer().init();
    }

    void init() {
        for (int i = 1; i <= 50; i++) {
            TestWebServer pp = new TestWebServer(i);
            pp.start();
        }
    }

    public void run() {
        try {
            process();
        } catch (URISyntaxException ex) {
            LogManager.getLogger(TestWebServer.class.getName()).log(Level.ERROR, "", ex);
        }
    }

    void process() throws URISyntaxException {
        Date d1 = new Date();
        System.out.println("Run: " + index);
        //String server = "http://localhost:8080/tutorial/rdf?query=";
        String server = "http://corese.inria.fr/tutorial/rdf?query=";
        //server = "http://localhost:8080/tutorial/rdf?query=";
        String query = "select (count(distinct *) as ?c)  where {?x ?p ?y} limit 20";
        query = protect(query);
        QueryLoad ql = QueryLoad.create();
        String str = ql.read(server + query);
        Date d2 = new Date();

        System.out.println("time: "
                + ((index < 10) ? "0" : "") + index
                + " " + (d2.getTime() - d1.getTime()) / (1000.0));

        //System.out.println(str);

    }

    String protect(String query) {
        query = query.replace("?", "%3F");
        query = query.replace(" ", "%20");
        return query;
    }
}
