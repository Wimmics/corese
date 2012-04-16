/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.endpoint;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Embedded HTTP server for Corese. 
 * Using Jetty implementation. 
 */
public class KgEmbeddedServer {
    public static void main(String args[]) throws Exception {
        Server server = new Server(8080);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(new ServletHolder(new SPARQLEndpointServlet()), "/*");
        server.start();
    }
}