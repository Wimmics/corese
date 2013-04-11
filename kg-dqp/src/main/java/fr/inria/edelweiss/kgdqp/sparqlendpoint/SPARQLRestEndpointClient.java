package fr.inria.edelweiss.kgdqp.sparqlendpoint;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;

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
     */
    public String doPost(String query) throws IOException {
	WebResource service;
	service = client.resource(UriBuilder.fromUri(url).build());
	ClientResponse clientResponse = service.path("sparql").accept("application/sparql-results+xml").post(ClientResponse.class, query);
	return clientResponse.getEntity(String.class);
    }

    /**
     * This method is an helper method used to send a sparql query to the remote server by using the GET operation of the SPARQL 1.1 WebStore protocol
     * @param query is the sparql query qe want to send to the remote server
     */
    public String doGet(String query) throws IOException {
	WebResource service;
	service = client.resource(UriBuilder.fromUri(url).build());
	ClientResponse clientResponse = service.path("sparql").queryParam("query", query).accept("application/sparql-results+xml").get(ClientResponse.class);
	return clientResponse.getEntity(String.class);
    }
    
    /**
     * that is the very method of this class. It is used to send a query to the remote server
     */
    public String getEdges(String query){
	try {
	    return doGet(query);
	} catch (IOException ex) {
	    Logger.getLogger(SPARQLRestEndpointClient.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
    
    /**
     * that is the very method of this class. It is used to send a query to the remote server
     */
    public String query(String query){
	try {
	    return doGet(query);
	} catch (IOException ex) {
	    Logger.getLogger(SPARQLRestEndpointClient.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }

    /**
     * This method return the endpoint where our client is bounded to
     */
    public String getEndpoint() {
	return url;
    }
}