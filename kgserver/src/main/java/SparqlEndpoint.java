/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


/**
 *
 * @author macina
 */
public class SparqlEndpoint {
    private final Logger logger = Logger.getLogger(SparqlEndpoint.class);
    private int port ;
    public  URI resourceURI;
    public Server server ;

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
            
            ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
            jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            jerseyServletHolder.setInitParameter("com.sun.jersey.config.property.packages", "fr.inria.edelweiss.kgramserver.webservice");
            jerseyServletHolder.setInitParameter("requestBufferSize", "8192");
            jerseyServletHolder.setInitParameter("headerBufferSize", "8192");
            Context servletCtx = new Context(server, "/", Context.SESSIONS);
            servletCtx.addServlet(jerseyServletHolder, "/*");
            server.start();
            logger.info("----------------------------------------------");
            logger.info("Corese/KGRAM endpoint started on http://localhost:" + port + "/");
            logger.info("----------------------------------------------");
            

            //server initialization
            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            WebResource service = client.resource(new URI("http://localhost:" + port + "/"));

            MultivaluedMap formData;
            formData = new MultivaluedMapImpl();
            formData.add("remote_path", dataPath);
            service.path("sparql").path("load").post(formData);

            server.join();

    }

      public static void main(String args[]) throws Exception {

          SparqlEndpoint geo1 = new SparqlEndpoint(8085,"/home/macina/CodeKGRAM/kgram/Dev/trunk/kgtool/src/main/resources/demographie/dep1.ttl");
          SparqlEndpoint geo2 = new SparqlEndpoint(8086,"/home/macina/CodeKGRAM/kgram/Dev/trunk/kgtool/src/main/resources/demographie/dep2.ttl");
          SparqlEndpoint demo = new SparqlEndpoint(8087,"/home/macina/CodeKGRAM/kgram/Dev/trunk/kgtool/src/main/resources/demographie/popleg-2010.ttl");
          SparqlEndpoint geoAll = new SparqlEndpoint(8088,"/home/macina/CodeKGRAM/kgram/Dev/trunk/kgtool/src/main/resources/demographie/cog-2012.ttl");
         
//          geo1.createServer();
//          geo2.createServer();
          demo.createServer();
//          geoAll.createServer();
          
      }

}
