/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler.Context;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;

/**
 *
 * @author Abdoul Macina macina@i3s.unice.fr
 */
public class SparqlEndpoint {

    private Logger logger = LogManager.getLogger(SparqlEndpoint.class);
    private int port;
    public URI resourceURI;
    public Server server;

    public Server getServer() {
        return server;
    }
    private String dataPath;

    public SparqlEndpoint(int port, String dataPath) {
        this.port = port;
        this.dataPath = dataPath;
        server = new Server(port);
    }

    public void createServer() throws Exception {
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
//        ResourceConfig config = new ResourceConfig(MyResource.class);
        Server server = JettyHttpContainerFactory.createServer(baseUri, false);

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.corese.server.webservice");
        jerseyServletHolder.setInitParameter("requestBufferSize", "8192");
        jerseyServletHolder.setInitParameter("headerBufferSize", "8192");
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/*");
        servletContextHandler.addServlet(jerseyServletHolder, "/*");
        server.start();
        logger.info("----------------------------------------------");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/");
        logger.info("----------------------------------------------");

        //server initialization
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(new URI("http://localhost:" + port + "/"));

        MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
        formData.add("remote_path", dataPath);
        target.path("sparql").path("load")
                .request(APPLICATION_FORM_URLENCODED_TYPE)
                .post(Entity.form(formData));
        server.join();

    }

    public static void main(String args[]) throws Exception {

////      All predicates in all sources
//        SparqlEndpoint dep1 = new SparqlEndpoint(8081, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/dep1.ttl");
//        SparqlEndpoint dep2 = new SparqlEndpoint(8082, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/dep2.ttl");
//        SparqlEndpoint depComplet = new SparqlEndpoint(8083, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/cog-2012.ttl");
//
////          dep1.createServer();
////          dep2.createServer();
////          depComplet.createServer();
//        
////      Predicates without join variable in same source
//        SparqlEndpoint geo1 = new SparqlEndpoint(8084, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/region_dep.ttl");
//        SparqlEndpoint geo2 = new SparqlEndpoint(8085, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/dep_noms.ttl");
//        SparqlEndpoint geo3 = new SparqlEndpoint(8086, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/region_noms.ttl");
//
////          geo1.createServer();
////          geo2.createServer();
////          geo3.createServer();
        
////      Predicates with join variables in all source
//        SparqlEndpoint s1 = new SparqlEndpoint(8092, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/s1.ttl");
//        SparqlEndpoint s2 = new SparqlEndpoint(8088, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/s2.ttl");
//        SparqlEndpoint s3 = new SparqlEndpoint(8089, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/s3.ttl");
//        SparqlEndpoint s4 = new SparqlEndpoint(8090, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/s4.ttl");

          
//          s1.createServer();
//          s2.createServer();
//          s3.createServer();
//          s4.createServer();
          
          
////      demographic data
//        SparqlEndpoint demographic = new SparqlEndpoint(8091, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/popleg-2010.ttl");
//        demographic.createServer();
        
        
        //new tests
        
        
        //Test redondance
        SparqlEndpoint geographicAll = new SparqlEndpoint(8081, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/cog-2014.ttl");
//        geographicAll.createServer();
        SparqlEndpoint geographicAllBis = new SparqlEndpoint(8082, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/cog-2014.ttl");
//        geographicAllBis.createServer();
        
        
        //Test globalBGP
        SparqlEndpoint geographicGBGP2 = new SparqlEndpoint(8083, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/globalBGPS2.ttl");
//        geographicGBGP2.createServer();
        SparqlEndpoint geographicGBGP1 = new SparqlEndpoint(8084, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/globalBGPS1.ttl");
//        geographicGBGP1.createServer();
        
        
//      Test Partial BGP
        SparqlEndpoint region_departement = new SparqlEndpoint(8085, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/regions_departements.ttl");
        SparqlEndpoint departement_name = new SparqlEndpoint(8086, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/departements_names.ttl");
        SparqlEndpoint region_name = new SparqlEndpoint(8087, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/regions_names.ttl");

//          region_departement.createServer();
//          region_name.createServer();
//          departement_name.createServer();
        
        
        SparqlEndpoint demographic = new SparqlEndpoint(8088, SparqlEndpoint.class.getClassLoader().getResource("demographie").getPath()+"/popleg-2013-sc.ttl");
        demographic.createServer();
    }

}
