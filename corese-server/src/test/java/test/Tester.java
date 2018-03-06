package test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.Service;
import static fr.inria.corese.kgtool.load.Service.QUERY;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Tester {
    private boolean isDebug;
    
    
    
    @Test
    public void test() throws LoadException{
        Service serv = new Service("http://localhost:8080/sparql");        
        String q = "select * where {?x ?p ?y} limit 10";        
        Mappings map = serv.select(q);                        
        assertEquals(10, map.size());
    }
    
    
    @Test
    public void test2() {
        String service = "http://localhost:8080/template";
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.queryParam("profile", "st:dbedit").get(String.class);
        assertEquals(true, res.length() > 22000);
        assertEquals(true, res.contains("Front yougoslave de la Seconde Guerre mondiale"));
        System.out.println(res.length());
    }
    
    
     @Test
    public void test3() {
        String service = "http://localhost:8080/template";
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.queryParam("profile", "st:dbpedia")
                .queryParam("uri", "http://fr.dbpedia.org/resource/Jimmy_Page")
                .get(String.class);
        assertEquals(true, res.contains("Led Zeppelin"));
    }
    
       @Test
    public void test4() {
        String service = "http://localhost:8080/tutorial/cdn";
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.get(String.class);
        assertEquals(true, res.contains("Siècle"));
    }
       
       
         @Test
    public void test5() {
        String service = "http://localhost:8080/process/owlrl";
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.queryParam("uri", "/data/primer.owl").get(String.class);
        assertEquals(true, res.contains("Statement not supported in an Equivalent Class Expression"));
    }  
         
         
         
    
     public String process(String service, String query, String mime) {
        if (isDebug){
            System.out.println(query);
        }
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.queryParam(QUERY, query)
                .accept(mime)
                .post(String.class);
        if (isDebug){
            System.out.println(res);
        }
        return res;
    }
    
    

}
