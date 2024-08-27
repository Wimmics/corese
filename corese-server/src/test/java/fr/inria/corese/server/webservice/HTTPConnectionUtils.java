package fr.inria.corese.server.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HTTPConnectionUtils {

    /**
     * Get a connection to a server.
     * 
     * @param url     server URL
     * @param headers HTTP headers
     * @return
     * @throws MalformedURLException
     * @throws IOException
     * @throws ProtocolException
     */
    
    public static HttpURLConnection methodConnection(String method, String url, List<List<String>> headers)
    throws IOException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod(method);
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(true);
        for (List<String> header : headers) {
            con.setRequestProperty(header.get(0), header.get(1));
        }
        return con;
    }

    public static HttpURLConnection getConnection(String url, List<List<String>> headers)
            throws IOException {
        return methodConnection("GET", url, headers);
    }

    public static HttpURLConnection postConnection(String url, List<List<String>> headers, String body)
            throws IOException {
        HttpURLConnection con = methodConnection("POST", url, headers);
        con.setDoOutput(true);
        con.getOutputStream().write(body.getBytes());
        return con;
    }

    public static HttpURLConnection postUrlencodedConnection(String url, List<List<String>> headers, String body)
            throws IOException {
        List<List<String>> newHeaders = new ArrayList<>(headers);
        List<String> contentTypeHeader = new ArrayList<>();
        contentTypeHeader.add("Content-Type");
        contentTypeHeader.add("application/x-www-form-urlencoded");
        newHeaders.add(contentTypeHeader);
        return postConnection(url, newHeaders, body);
    }

    public static HttpURLConnection putConnection(String url, List<List<String>> headers, String body)
            throws IOException {
        HttpURLConnection con = methodConnection("PUT", url, headers);
        con.setDoOutput(true);
        con.getOutputStream().write(body.getBytes());
        return con;
    }

    public static HttpURLConnection deleteConnection(String url, List<List<String>> headers)
            throws IOException {
        return methodConnection("DELETE", url, headers);
    }

    public static HttpURLConnection deleteConnection(String url)
            throws IOException {
        return deleteConnection( url, new ArrayList<>());
    }

    public static HttpURLConnection headConnection(String url, List<List<String>> headers)
            throws IOException {
        return methodConnection("HEAD", url, headers);
    }

    public static HttpURLConnection headConnection(String url)
            throws IOException {
        return headConnection(url, new ArrayList<>());
    }
}
