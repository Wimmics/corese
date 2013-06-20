/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileDepthSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Embedded HTTP server for Corese. Using Jetty implementation.
 *
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

        //extract HTML source for the web UI
        URI webappUri = EmbeddedJettyServer.extractResourceDir("webapp", true);
        System.out.println("");
        System.out.println(webappUri.getRawPath());
        System.out.println("");

        Server server = new Server(port);

        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
        Context servletCtx = new Context(server, "/kgram", Context.SESSIONS);
        servletCtx.addServlet(jerseyServletHolder, "/*");
        logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/kgram");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("/Users/gaignard/devKgram/kgserver/src/main/resources/webapp");
//        resource_handler.setResourceBase(webappUri.getRawPath());
        ContextHandler staticContextHandler = new ContextHandler();
        staticContextHandler.setContextPath("/");
        staticContextHandler.setHandler(resource_handler);
        logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{staticContextHandler, servletCtx});
        server.setHandler(handlers);

        server.start();
        server.join();
    }

    private static URI extractResourceDir(String dirname, boolean overwrite) throws FileSystemException, URISyntaxException {
        URL dir_url = EmbeddedJettyServer.class.getClassLoader().getResource(dirname);
        FileObject dir_jar = VFS.getManager().resolveFile(dir_url.toString());
        String tempDir = FileUtils.getTempDirectory() + File.separator + System.getProperty("user.name");
        FileObject tmpF = VFS.getManager().resolveFile(tempDir);
        FileObject localDir = tmpF.resolveFile(dirname);
        if (!localDir.exists()) {
            logger.info("Extracting directory " + dirname + " to " + tmpF.getName());
            localDir.createFolder();
            localDir.copyFrom(dir_jar, new AllFileSelector());
        } else {
            if (overwrite) {
                logger.info("Overwritting directory " + dirname + " with " + dirname);
                localDir.delete(new FileDepthSelector(0, 5));
                localDir.createFolder();
                localDir.copyFrom(dir_jar, new AllFileSelector());
            }
        }
        return localDir.getURL().toURI();
    }
}