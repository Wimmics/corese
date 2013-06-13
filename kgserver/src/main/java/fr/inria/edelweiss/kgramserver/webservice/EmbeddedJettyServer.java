/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


/**
 * Embedded HTTP server for Corese. Using Jetty implementation.
 * @author alban.gaignard@cnrs.fr
 */
public class EmbeddedJettyServer {
    private static Logger logger = Logger.getLogger(EmbeddedJettyServer.class);

    public static void main(String args[]) throws Exception {
        int port = 8080;

        Options options = new Options();
        Option portOpt = new Option("p", "port", true, "specify the server port");
        Option helpOpt = new Option("h", "help", false, "print this message");
        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        options.addOption(portOpt);
        options.addOption(helpOpt);
        options.addOption(versionOpt);

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar kgserver-1.0.7-jar-with-dependencies.jar", options);
                System.exit(0);
            } else if (cmd.hasOption("p")) {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            } else if (cmd.hasOption("v")) { 
                logger.info("version 1.0.7");
                System.exit(0);
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");

        Server server = new Server(port);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addServlet(jerseyServletHolder, "/kgram/*");
        logger.info("Starting Corese/KGRAM on http://localhost:"+port+"/kgram");
        
        server.start();
        server.join();

        ClientConfig config = new DefaultClientConfig();
        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(config);
        WebResource service = client.resource(new URI("http://localhost:8081/kgram"));
//        // Fluent interfaces
        System.out.println(service.path("sparql").path("reset").post(String.class).toString());
    }
}