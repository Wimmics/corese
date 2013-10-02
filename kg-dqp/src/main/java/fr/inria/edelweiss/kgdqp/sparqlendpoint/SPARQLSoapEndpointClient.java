/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.sparqlendpoint;

import java.net.URL;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class SPARQLSoapEndpointClient implements SparqlEndpointInterface {

    private RemoteProducer rp;

    public SPARQLSoapEndpointClient(URL url) {
        rp = RemoteProducerServiceClient.getPort(url);
    }

    @Override
    public String getEdges(String query) {
        return rp.getEdges(query);
    }
    
    @Override
    public String query(String query) {
        return rp.query(query);
    }

    @Override
    public String getEndpoint() {
        return rp.getEndpoint();
    }

    @Override
    public String getNodes(String query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
