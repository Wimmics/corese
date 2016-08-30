package fr.inria.edelweiss.kgdqp.sparqlendpoint;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.*;
import java.net.URL;
import org.apache.logging.log4j.Level;

import javax.ws.rs.core.UriBuilder;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Eric TOGUEM, eric.toguem@uy1.uninet.cm
 */
public class SPARQLRestEndpointClient implements SparqlEndpointInterface {
    /**That is the base url of the service, it is given during construction of the class*/
    private String url;
    /**It is used to query the client*/
    private Client client;

    public SPARQLRestEndpointClient(String url) {
	this.url = url;
	ClientConfig config = new DefaultClientConfig();
	client = Client.create(config);
    }
    
    public SPARQLRestEndpointClient(URL url) {
	this.url = url.toString();
	ClientConfig config = new DefaultClientConfig();
	client = Client.create(config);
    }

    /**
     * This method is an helper method used to send a sparql query to the remote server by using the POST operation of the SPARQL 1.1 WebStore protocol,
     * here the query is embedded in body of the message
     * @param query is the sparql query qe want to send to the remote server
     * @return
     * @throws IOException  
     */
    public String doPost(String query) throws IOException {
	WebResource service;
	service = client.resource(UriBuilder.fromUri(url).build());
        ClientResponse clientResponse = service.path("/").accept("application/rdf+xml").post(ClientResponse.class, query);
	return clientResponse.getEntity(String.class);
    }

    /**
     * This method is an helper method used to send a sparql query to the remote server by using the GET operation of the SPARQL 1.1 WebStore protocol
     * @param query is the sparql query qe want to send to the remote server
     * @return
     * @throws IOException  
     */
    public String doGet(String query) throws IOException {
	WebResource service;
	service = client.resource(UriBuilder.fromUri(url).build());
        ClientResponse clientResponse = service.path("/").queryParam("query", query).accept("text/turtle").get(ClientResponse.class);
	return clientResponse.getEntity(String.class);
    }
    
    /**
     * This method is an helper method used to send a sparql query to the remote server by using the GET operation of the SPARQL 1.1 WebStore protocol
     * @param query is the sparql query qe want to send to the remote server
     * @param format is the http accept format, namely "application/sparql-results+xml", "application/rdf+xml", or "text/turtle"
     * @return
     * @throws IOException  
     */
    public String doGet(String query, String format) throws IOException {
	WebResource service;
	service = client.resource(UriBuilder.fromUri(url).build());
        ClientResponse clientResponse = service.path("/").queryParam("query", query).accept(format).get(ClientResponse.class);
	return clientResponse.getEntity(String.class);
    }
    
    /**
     * 
     * @param query
     * @return  
     */
    @Override
    public String getEdges(String query){
	try {
            return doGet(query, "application/rdf+xml");
//	    return doGet(query, "text/turtle");
	} catch (IOException ex) {
	    LogManager.getLogger(SPARQLRestEndpointClient.class.getName()).log(Level.ERROR, "", ex);
	}
	return null;
    }
    
    /**
     * 
     * @param query
     * @return  
     */
    @Override
    public String getNodes(String query){
	try {
	    return doGet(query, "application/sparql-results+xml");
	} catch (IOException ex) {
	    LogManager.getLogger(SPARQLRestEndpointClient.class.getName()).log(Level.ERROR, "", ex);
	}
	return null;
    }
    
    /**
     * 
     * @param query
     * @return  
     */
    @Override
    public String query(String query){
	try {
	    return doGet(query, "application/sparql-results+xml");
	} catch (IOException ex) {
	    LogManager.getLogger(SPARQLRestEndpointClient.class.getName()).log(Level.ERROR, "", ex);
	}
	return null;
    }

    /**
     * This method return the endpoint where our client is bounded to
     */
    @Override
    public String getEndpoint() {
	return url;
    }
}