/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.sparqlendpoint;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author gaignard
 */
public class SPARQLEndpointClient {

    private String url;

    public SPARQLEndpointClient(String url) {
        this.url = url;
    }

    public StringBuffer doPost(String query) throws IOException {
        URLConnection cc = post(query);
        return getBuffer(cc.getInputStream());
    }

    private URLConnection post(String query) throws IOException {
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

        URL queryURL = new URL(url);
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

    public StringBuffer doGet(String query) throws IOException {
        URLConnection cc = get(query);
        return getBuffer(cc.getInputStream());
    }

    private StringBuffer getBuffer(InputStream stream) throws IOException {
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

    private HttpURLConnection get(String query) throws IOException {
        URL queryURL = new URL(url + "?query=" + URLEncoder.encode(query, "UTF-8"));
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("GET");

        return urlConn;
    }

    public String getEndpoint() {
        return url;
    }
}