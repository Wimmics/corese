package fr.inria.corese.server.webservice;

import static fr.inria.corese.core.api.Loader.RDFXML_FORMAT;
import static fr.inria.corese.core.print.ResultFormat.RDF_XML;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_CSV;
import static fr.inria.corese.core.print.ResultFormat.SPARQL_RESULTS_XML;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.result.SPARQLResult;
import fr.inria.corese.kgram.core.Mappings;

public class SPARQLTestUtils {

    private static final Logger logger = LogManager.getLogger(SPARQLTestUtils.class);

    private static final String SERVER_URL = "http://localhost:8080/";
    private static final String SPARQL_ENDPOINT_URL = SERVER_URL + "sparql";

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

    public static String generateSPARQLQueryParameters(String query, List<List<String>> optionalParameters) {
        return generateSPARQLParameters("query", query, optionalParameters);
    }

    public static String generateSPARQLQueryParameters(String query) {
        return generateSPARQLQueryParameters(query, new ArrayList<>());
    }

    public static String generateSPARQLUpdateParameters(String query, List<List<String>> optionalParameters) {
        return generateSPARQLParameters("update", query, optionalParameters);
    }

    public static String generateSPARQLUpdateParameters(String query) {
        return generateSPARQLUpdateParameters(query, new ArrayList<>());
    }

    public static String generateGraphStoreParameters(String query) {
        return generateGraphStoreParameters(query, new ArrayList<>());
    }

    public static String generateGraphStoreParameters(String query, List<List<String>> optionalParameters) {
        return generateSPARQLParameters("graph", query, optionalParameters);
    }

    private static String generateSPARQLParameters(String firstKeyword, String query,
            List<List<String>> optionalParameters) {
        try {
            String result = firstKeyword + "=" + URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            if (optionalParameters.size() > 0) {
                for (Iterator<List<String>> itParam = optionalParameters.iterator(); itParam.hasNext();) {
                    List<String> p = itParam.next();
                    if (p.size() == 2) {
                        result += "&" + p.get(0) + "=" + URLEncoder.encode(p.get(1), StandardCharsets.UTF_8.toString());
                    } else if (p.size() == 1) {
                        result += "&" + p.get(0);
                    }
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            return null;
        }
    }

    public static Mappings sendSPARQLSelect(String query) throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_XML);
        headers.add(acceptHeader);

        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();

        return SPARQLResult.create().parseString(content.toString());
    }

    public static boolean sendSPARQLAsk(String query) throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(SPARQL_RESULTS_CSV);
        headers.add(acceptHeader);

        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resultString = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            resultString.append(inputLine);
        }
        in.close();

        con.disconnect();

        return Boolean.parseBoolean(resultString.toString());
    }

    public static Graph sendSPARQLConstructDescribe(String query) throws Exception {
        List<List<String>> headers = new LinkedList<>();
        List<String> acceptHeader = new LinkedList<>();
        acceptHeader.add("Accept");
        acceptHeader.add(RDF_XML);
        headers.add(acceptHeader);

        String urlQuery = SPARQL_ENDPOINT_URL + "?" + SPARQLTestUtils.generateSPARQLQueryParameters(query);
        HttpURLConnection con = SPARQLTestUtils.getConnection(urlQuery, headers);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        Graph constructGraph = new Graph();
        Load load = Load.create(constructGraph);
        InputStream inputStream = new ByteArrayInputStream(content.toString().getBytes());
        load.parse(inputStream, RDFXML_FORMAT);

        return constructGraph;
    }
    
}
