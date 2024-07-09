package fr.inria.corese.server.webservice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SPARQLTestUtils {
    

    private static final Logger logger = LogManager.getLogger(SPARQLTestUtils.class);

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
    public static HttpURLConnection getConnection(String url, List<List<String>> headers)
            throws MalformedURLException, IOException, ProtocolException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(true);
        for (List<String> header : headers) {
            con.setRequestProperty(header.get(0), header.get(1));
        }
        return con;
    }

    public static HttpURLConnection postConnection(String url, List<List<String>> headers, String body)
            throws MalformedURLException, IOException, ProtocolException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setInstanceFollowRedirects(true);
        for (List<String> header : headers) {
            con.setRequestProperty(header.get(0), header.get(1));
        }
        con.setDoOutput(true);
        con.getOutputStream().write(body.getBytes());
        return con;
    }

    public static String generateSPARQLQueryParameters(String query, List<List<String>> optionalParameters) {
        return generateSPARQLParameters("query", query, optionalParameters);
    }

    public static String generateSPARQLUpdateParameters(String query, List<List<String>> optionalParameters) {
        return generateSPARQLParameters("update", query, optionalParameters);
    }

    public static String generateSPARQLUpdateParameters(String query) {
        return generateSPARQLUpdateParameters(query, new ArrayList<>());
    }

    public static String generateSPARQLQueryParameters(String query) {
        return generateSPARQLQueryParameters(query, new ArrayList<>());
    }

    private static String generateSPARQLParameters(String firstKeyword, String query, List<List<String>> optionalParameters) {
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
}
