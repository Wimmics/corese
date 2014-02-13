/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.endpoint;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Embedded HTTP server for Corese. Using Jetty implementation.
 */
@Deprecated
public class KgEmbeddedServer {

    public static void main(String args[]) throws Exception {
        Server server = new Server(8081);
        Context root = new Context(server, "/", Context.SESSIONS);
//        root.addServlet(new ServletHolder(new SPARQLEndpointServlet()), "/*");

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
        root.addServlet(jerseyServletHolder, "/kgram/*");
        
        server.start();
        
        ClientConfig config = new DefaultClientConfig();
        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8081/kgram"));
//        // Fluent interfaces
        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
    }
}