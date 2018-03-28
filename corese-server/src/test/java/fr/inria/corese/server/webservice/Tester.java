package fr.inria.corese.server.webservice;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.Service;

import static fr.inria.corese.core.load.Service.QUERY;

import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import static org.junit.Assert.assertEquals;

/**
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 */
public class Tester {
    private boolean isDebug;


    @Test
    public void test() throws LoadException {
        Service serv = new Service("http://localhost:8080/sparql");
        String q = "select * where {?x ?p ?y} limit 10";
        Mappings map = serv.select(q);
        assertEquals(10, map.size());
    }


    @Test
    public void test2() {
        String service = "http://localhost:8080/template";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(service);
        String res = target.queryParam("profile", "st:dbedit").request().get(String.class);
        assertEquals(true, res.length() > 22000);
        assertEquals(true, res.contains("Front yougoslave de la Seconde Guerre mondiale"));
        System.out.println(res.length());
    }


    @Test
    public void test3() {
        String service = "http://localhost:8080/template";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(service);
        String res = target.queryParam("profile", "st:dbpedia")
                .queryParam("uri", "http://fr.dbpedia.org/resource/Jimmy_Page")
                .request()
                .get(String.class);
        assertEquals(true, res.contains("Led Zeppelin"));
    }

    @Test
    public void test4() {
        String service = "http://localhost:8080/tutorial/cdn";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(service);
        String res = target.request().get(String.class);
        assertEquals(true, res.contains("Siècle"));
    }


    @Test
    public void test5() {
        String service = "http://localhost:8080/process/owlrl";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(service);
        String res = target.queryParam("uri", "/data/primer.owl").request().get(String.class);
        assertEquals(true, res.contains("Statement not supported in an Equivalent Class Expression"));
    }


    public String process(String service, String query, String mime) {
        if (isDebug) {
            System.out.println(query);
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(service);
        String res = target.queryParam(QUERY, query)
                .request(mime)
                .post(Entity.text(null), String.class);
        if (isDebug) {
            System.out.println(res);
        }
        return res;
    }


}
