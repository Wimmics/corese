package fr.inria.corese.server.webservice;

import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static fr.inria.corese.core.load.Service.QUERY;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import static org.junit.Assert.assertEquals;

/**
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 */
public class TesterSSL {
    private boolean isDebug = true;
    private static Process server;
    private static final String SERVER_URL = "https://localhost:8443/";
    private static final String SPARQL_ENDPOINT_URL = SERVER_URL + "sparql";
    private static final String TEMPLATE_URL = SERVER_URL + "template";
    private static final String CDN_URL = SERVER_URL + "tutorial/cdn";
    private static final String ORLRL_URL = SERVER_URL + "process/owlrl";
    private static ClientBuilder clientBuilder;
    @BeforeClass
    public static void init() throws InterruptedException, IOException, Exception
    {
        SSLContext sslcontext = SSLContext.getInstance("TLS");

        sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0];}
        }}, new java.security.SecureRandom());

        clientBuilder = ClientBuilder.newBuilder()
                .sslContext(sslcontext)
                .hostnameVerifier((s1, s2) -> true);
        System.out.println( "starting in " + System.getProperty( "user.dir" ) );
        server = new ProcessBuilder().inheritIO().command(
                "/usr/bin/java",
                "-jar", "./target/corese-server-4.0.1-SNAPSHOT-jar-with-dependencies.jar",
                "-lh",
                "-l", "./target/classes/webapp/data/dbpedia/dbpedia.ttl",
                "-ssl",
                "-jks", "corese.inria.fr.jks",
                "-pwd", "coreseatwimmics"
        ).start();
        Thread.sleep( 5000 );
    }

    @AfterClass
    public static void shutdown()
    {
        server.destroy();
    }

    @Test
    public void test() throws Exception
    {
        Service serv = new Service( SPARQL_ENDPOINT_URL , clientBuilder );

        String q = "select * where {?x ?p ?y} limit 10";
        Mappings map = serv.select( q );
        for ( Mapping m : map )
        {
            System.out.println( map );
        }
        assertEquals( 10, map.size() );
    }

    @Test
    public void test2()
    {
        String service = TEMPLATE_URL;
        Client client = clientBuilder.build();
        WebTarget target = client.target( service );
        String res = target.queryParam( "profile", "st:dbedit" ).request().get( String.class );
        assertEquals( true, res.length() > 17000 );
        assertEquals( true, res.contains( "Front yougoslave de la Seconde Guerre mondiale" ) );
        System.out.println( res.length() );
    }


    @Test
    public void test3()
    {
        String service = TEMPLATE_URL;
        Client client = clientBuilder.build();
        WebTarget target = client.target( service );
        String res = target.queryParam( "profile", "st:dbpedia" )
                .queryParam( "uri", "http://fr.dbpedia.org/resource/Jimmy_Page" )
                .request()
                .get( String.class );
        assertEquals( true, res.contains( "Led Zeppelin" ) );
    }

    @Test
    public void test4()
    {
        String service = CDN_URL;
        Client client = clientBuilder.build();
        WebTarget target = client.target( service );
        String res = target.request().get( String.class );
        assertEquals( true, res.contains( "Siècle" ) );
    }


    @Test
    public void test5()
    {
        String service = ORLRL_URL;
        Client client = clientBuilder.build();
        WebTarget target = client.target( service );
        String res = target.queryParam( "uri", "/data/primer.owl" ).request().get( String.class );
        assertEquals( true, res.contains( "Statement not supported in an Equivalent Class Expression" ) );
    }
}
