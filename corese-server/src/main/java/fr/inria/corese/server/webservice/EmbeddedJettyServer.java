/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.server.webservice;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileDepthSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;

/**
 * Embedded HTTP server for Corese, Using Jetty implementation
 * SPARQL endpoint: SPARQLRestAPI
 * Semantic web services at Transformer, Tutorial, etc
 * Home page demo_new.html includes JS that feed it with html/content.html, etc
 * Web page services are executed using AJAX with trans(url) JS function (see js/server.js)
 * Semantic web services are defined in  data/profile.ttl
 * SSL connection
 *
 * @author alban.gaignard@cnrs.fr
 * @author Fuqi Song Inria 2015
 * @author Olivier Corby Inria 2015-2016
 */
@ApplicationPath("resources")
public class EmbeddedJettyServer extends ResourceConfig {

    private static final Logger logger = LogManager.getLogger(EmbeddedJettyServer.class);
    static int port = 8080;
    private static boolean entailments = false;
    private static boolean owlrl = false;
    private static String[] dataPaths = null;
    private static String localProfile = null;

    // profile.ttl may contain data to be loaded
    // use -lp option to effectively load profile data
    // default is do not load
    private static boolean loadProfileData = false;

    // options for SSL connection
    private static boolean enableSsl = false;
    private static int portSsl = 8443;
    private static String keystore, password;

    public static URI resourceURI;
    public static String HOME_PAGE;
    private static boolean isLocalHost;
    private static boolean debug = false;

    public static String BASE_URI;

    public EmbeddedJettyServer() {
        packages("fr.inria.corese.server.webservice");
    }

    public static void main(String args[]) throws Exception {

        // Checking if Log4j is overriden by system property
        String overrideLog4j = System.getProperty("log4j.configurationFile");
        URL log4jfile = null;
        if (overrideLog4j != null && overrideLog4j != "")
            log4jfile = new URL(overrideLog4j);
        else
            log4jfile = EmbeddedJettyServer.class.getClassLoader().getResource("log4j.properties");
        logger.info("Loading log4j configuration: " + log4jfile);
        logger.info("To override log4j configuration add JVM option: -Dlog4j.configurationFile=file:/home/.../your_log4j2.xml");


        HOME_PAGE = SPARQLRestAPI.isAjax ? "demo_new.html" : "demo.html";

        // logger.debug("Started.");
        Options options = new Options();
        Option portOpt = new Option("p", "port", true, "specify the server port");
        Option helpOpt = new Option("h", "help", false, "print this message");
        Option entailOpt = new Option("e", "entailments", false, "enable RDFS entailments");
        Option owlrlOpt = new Option("o", "owlrl", false, "enable OWL RL entailments");
        Option dataOpt = new Option("l", "load", true, "data file or directory to be loaded");
        Option profileOpt = new Option("lp", "profile", false, "load profile data");
        Option locProfileOpt = new Option("pp", "profile", true, "local profile");
        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        Option localhost = new Option("lh", "localhost", false, "set server name as localhost");
        Option optDebug = new Option("debug", "debug", false, "set server mode as debug");
        Option protect = new Option("protect", "protect", false, "set server mode as protect");

        Option sslOpt = new Option("ssl", "ssl", false, "enable ssl connection ?");
        Option portSslOpt = new Option("pssl", "pssl", true, "port of ssl connection");
        Option keystoreOpt = new Option("jks", "keystore", true, "java key store name (../keystore/xxx)");
        Option keypasswordOpt = new Option("pwd", "password", true, "java key store password (key, store, trust store)");

        options.addOption(portOpt);
        options.addOption(entailOpt);
        options.addOption(owlrlOpt);
        options.addOption(dataOpt);
        options.addOption(profileOpt);
        options.addOption(locProfileOpt);
        options.addOption(helpOpt);
        options.addOption(versionOpt);
        options.addOption(localhost);
        options.addOption(optDebug);
        options.addOption(protect);

        options.addOption(sslOpt);
        options.addOption(portSslOpt);
        options.addOption(keystoreOpt);
        options.addOption(keypasswordOpt);

        String header = "Once launched, the server can be managed through a web user interface, available at http://localhost:<PortNumber>\n\n";
        String footer = "\nPlease report any issue to alban.gaignard@cnrs.fr";

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("kgserver", header, options, footer, true);
                System.exit(0);
            }
            if (cmd.hasOption("p")) {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            }
            if (cmd.hasOption("v")) {
                logger.info("version 1.0.7");
                System.exit(0);
            }
            if (cmd.hasOption("e")) {
                entailments = true;
            }
            if (cmd.hasOption("o")) {
                owlrl = true;
            }
            if (cmd.hasOption("l")) {
                // deprecated load
                dataPaths = cmd.getOptionValues("l");
                logger.info("Server: " + String.join(" ", dataPaths));
            }
            if (cmd.hasOption("lp")) {
                // load st:default server content into SPARQL endpoint
                loadProfileData = true;
            }
            // Option for SSL connection
            if (cmd.hasOption("ssl")) {
                enableSsl = true;
                if (cmd.hasOption("pssl")) {
                    portSsl = Integer.parseInt(cmd.getOptionValue("pssl"));
                }

                if (cmd.hasOption("jks")) {
                    keystore = cmd.getOptionValue("jks");
                } else {
                    throw new ParseException("Please specify the path of keystore for SSL.");
                }

                if (cmd.hasOption("pwd")) {
                    password = cmd.getOptionValue("pwd");
                } else {
                    throw new ParseException("Please specify the password of keystore for SSL.");
                }
            }
            if (cmd.hasOption("pp")) {
                // user defined profile.ttl to define additional servers
                localProfile = cmd.getOptionValue("pp");
                logger.info("Profile: " + localProfile);
            }
            if (cmd.hasOption("lh")) {
                // user defined profile.ttl to define additional servers
                isLocalHost = true;
                logger.info("localhost");
            }
            if (cmd.hasOption("debug")) {
                logger.info("debug");
                setDebug(true);
            }
            if (cmd.hasOption("protect")) {
                logger.info("protect");
                SPARQLRestAPI.isProtected = true;
            }

//            final ResourceConfig resourceConfig = new ResourceConfig(Transformer.class);
//            resourceConfig.register(SPARQLRestAPI.class);
//            resourceConfig.register(MultiPartFeature.class);
//
//            ServletContainer servletContainer = new ServletContainer(resourceConfig);
//            ServletHolder jerseyServletHolder = new ServletHolder(servletContainer);
////            jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
////            jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.corese.server.webservice");
//            jerseyServletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "fr.inria.corese.server.webservice");
//            jerseyServletHolder.setInitParameter("requestBufferSize", "8192");
//            jerseyServletHolder.setInitParameter("headerBufferSize", "8192");
//            Context servletCtx = new Context(server, "/", Context.SESSIONS);
//            servletCtx.addServlet(jerseyServletHolder, "/*");

            URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
            BASE_URI = baseUri.toString();
            logger.info("BASE_URI = {}", BASE_URI);

            logger.info("----------------------------------------------");
            logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/sparql");

            Server server = JettyHttpContainerFactory.createServer(baseUri, false);
            ContextHandlerCollection root = new ContextHandlerCollection();
            server.setHandler(root);

            // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setWelcomeFiles(new String[]{HOME_PAGE, "index.html"});
            URI webappUri = EmbeddedJettyServer.extractResourceDir("webapp", true);
            logger.info("Webapp dir: " + webappUri);
            resource_handler.setResourceBase(webappUri.getRawPath());

            ContextHandler staticContextHandler = new ContextHandler(root, "/");
            staticContextHandler.setHandler(resource_handler);
            logger.info("Corese/KGRAM webapp UI started on http://localhost:" + port);
            logger.info("----------------------------------------------");

//            Server server = JettyHttpContainerFactory.createServer(baseUri, config, false);

            // === SSL Connector begin ====
            if (enableSsl) {
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStorePath( webappUri.getRawPath() + "/keystore/" + keystore );
                sslContextFactory.setKeyStorePassword( password );
                sslContextFactory.setKeyManagerPassword( password );
                sslContextFactory.setTrustStorePath( webappUri.getRawPath() + "/keystore/" + keystore );
                sslContextFactory.setTrustStorePassword( password );

              /*X  SslSocketConnector sslConnector = new SslSocketConnector();
                X  sslConnector.setPort(portSsl);
                sslConnector.setServer(server);
                X sslConnector.setKeystore(webappUri.getRawPath() + "/keystore/" + keystore);
                X sslConnector.setKeyPassword(password);
                X sslConnector.setPassword(password);
                X sslConnector.setTruststore(webappUri.getRawPath() + "/keystore/" + keystore);
                X sslConnector.setTrustPassword(password);

                server.addConnector(sslConnector);*/

                HttpConfiguration httpsConfiguration = new HttpConfiguration();
                ServerConnector https = new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfiguration));
                https.setPort(portSsl);
                server.addConnector( https );

                logger.info("Corese SSL connection https://localhost:" + portSsl);
            }
            // === SSL Connector end ====

            ResourceConfig config = new ResourceConfig(
                    SPARQLRestAPI.class
                    , SrvWrapper.class
                    , LdpRequestAPI.class
                    , SPIN.class
                    , MultiPartFeature.class
                    , SDK.class
                    , Tutorial.class
                    , Transformer.class
                    , Processor.class
            );
            ServletContainer servletContainer = new ServletContainer(config);
            ServletHolder servletHolder = new ServletHolder(servletContainer);
            ServletContextHandler servletContextHandler = new ServletContextHandler(root, "/*");
            servletContextHandler.addServlet(servletHolder, "/*");


            server.start();
            // server initialization
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(new URI("http://localhost:" + port + "/"));
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
            formData.add("entailments", Boolean.toString(entailments));
            formData.add("owlrl", Boolean.toString(owlrl));
            formData.add("load", Boolean.toString(loadProfileData));
            if (localProfile != null) {
                formData.add("profile", localProfile);
            }
            if (isLocalHost) {
                formData.add("localhost", "true");
            }
            target.path("sparql").path("reset")
                    .request(APPLICATION_FORM_URLENCODED_TYPE)
                    .post(Entity.form(formData));

            if (dataPaths != null) {
                for (String dataPath : dataPaths) {
                    String[] lp = dataPath.split(";");
                    for (String p : lp) {
                        formData.clear();
                        formData.add("remote_path", p);
                        target.path("sparql").path("load")
                                .request(APPLICATION_FORM_URLENCODED_TYPE)
                                .post(Entity.form(formData));
                    }
                }
            }

            server.join();

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    public static URI extractResourceDir(String dirname, boolean overwrite) throws FileSystemException, URISyntaxException {
        URL dir_url = EmbeddedJettyServer.class.getClassLoader().getResource(dirname);
        FileObject dir_jar = VFS.getManager().resolveFile(dir_url.toString());
        String tempDir = FileUtils.getTempDirectory() + File.separator + System.getProperty("user.name").replace(" ", "");
        FileObject tmpF = VFS.getManager().resolveFile(tempDir);
        FileObject localDir = tmpF.resolveFile(dirname);
        if (!localDir.exists()) {
            logger.info("Extracting directory " + dirname + " to " + tmpF.getName());
            localDir.createFolder();
            localDir.copyFrom(dir_jar, new AllFileSelector());
        } else {
            if (overwrite) {
                logger.info("Overwritting directory " + dirname + " in " + tmpF.getName());
                localDir.delete(new FileDepthSelector(0, 5));
                localDir.createFolder();
                localDir.copyFrom(dir_jar, new AllFileSelector());
            }
        }
        resourceURI = localDir.getURL().toURI();
        return resourceURI;
    }

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * @param aDebug the debug to set
     */
    public static void setDebug(boolean aDebug) {
        debug = aDebug;
    }
}
