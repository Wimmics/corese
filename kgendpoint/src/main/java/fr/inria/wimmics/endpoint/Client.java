package fr.inria.wimmics.endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;

import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;
import fr.inria.corese.kgtool.print.RDFFormat;

public class Client {

    static String PATH = "webapps/examples/WEB-INF/resources/data/";
    static final String server = "http://localhost:8091/kgendpoint-1.0.7/KGSparqlEndpoint";
//	static final String server  = "http://localhost:8080/corese/sparql";
    //static final String server  = "http://localhost:8080/examples/sparql";
    static final String squery = server + "?query=";
    static final String update = server + "update=";
    static final String from = "default-graph-uri", named = "named-graph-uri";

    public static void main(String[] args) {

        new Client().run();

    }

    void run() {
        try {
            StringBuffer res;
            String init =
                    "prefix path: </webapps/corese/WEB-INF/resources/data/>"
                    + //				"load path:tmp.rdf ;" +
                    //				"load rdf: ;" +
                    //"drop all; " +
                    //"load <" + PATH + "sdk.rdf> " +
                    "load <http://wimmics.inria.fr/shared/data/sdk.rdf> ; "
                    + "load <http://wimmics.inria.fr/shared/data/server.rul> into graph kg:rule"
                    + "";

            init = "load rdfs:";

            String sel = "select * where { graph ?g {?x a ?y}}  ";
            String cons = "construct {?x ?p ?y} where {?x ?p ?y} ";
            String ask = "ask {?x ?p ?y} ";
            String desc = "describe rdf: ";

            QueryLoad ql = QueryLoad.create();
            String sdk = ql.read("/home/corby/workspace/kgendpoint/src/test/resources/data/sdk.rq");


            // initialize server graph
            //init = "clear default";
            String ins = "insert {?x a <Test>} where {?x a ?c}";

            URLConnection in;
            in = get(init);
            res = getBuffer(in.getInputStream());
            System.out.println(res);

//			in = post(ins);
//			res = getBuffer(in.getInputStream());
//
//			
            System.out.println("** Start");
            Date d1 = new Date();

            in = get(sel);


            if (isRDF(in)) {
                Graph g = Graph.create();
                Load load = Load.create(g);
                try {
                    load.load(in.getInputStream());
                    RDFFormat f = RDFFormat.create(g);
                    System.out.println(f);
                } catch (LoadException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                res = getBuffer(in.getInputStream());
                Date d2 = new Date();
                System.out.println(res);
                System.out.println("** Time : " + (d2.getTime() - d1.getTime()) / 1000.0);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            //StringBuffer mes = getBuffer(in.getInputStream());

        }
    }

    boolean isRDF(URLConnection urlConn) {
        return urlConn.getContentType().contains("rdf+xml");
    }

    StringBuffer doPost(String query) throws IOException {
        URLConnection cc = post(query);
        return getBuffer(cc.getInputStream());
    }

    URLConnection post(String query) throws IOException {
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

        URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml, text/xml");
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();

        return urlConn;

    }

    StringBuffer doGet(String query) throws IOException {
        URLConnection cc = get(query);
        return getBuffer(cc.getInputStream());
    }

    StringBuffer getBuffer(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();

        String str = null;
        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }

        return sb;
    }

    HttpURLConnection get(String query) throws IOException {
        URL queryURL = new URL(squery + URLEncoder.encode(query, "UTF-8"));
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("GET");

        return urlConn;
    }
}
