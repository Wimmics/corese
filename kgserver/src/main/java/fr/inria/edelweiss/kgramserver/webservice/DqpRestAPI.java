/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import fr.inria.edelweiss.kgdqp.core.Messages;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.JSOND3Format;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * REST API to expose main features of the KGRAM-DQP federation engine.
 *
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 */
@Path("dqp")
public class DqpRestAPI {

    private Logger logger = Logger.getLogger(DqpRestAPI.class);
    private static Graph graph = Graph.create(false);
    private static Provider sProv = ProviderImpl.create();
    private static QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);

    /**
     * This web service reinitalize the local KGRAM graph. Be careful, the graph
     * is a static variable.
     */
    @POST
    @Path("/reset")
    public Response resetDQP(@FormParam("endpointUrl") String endpointURLs) {
        try {
            DqpRestAPI.graph = Graph.create(false);
            DqpRestAPI.sProv = ProviderImpl.create();
            DqpRestAPI.execDQP = QueryProcessDQP.create(graph, sProv, true);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("Reinitialized KGRAM-DQP federation engine").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Exception while reseting KGRAM-DQP").build();
        }
    }

    /**
     * this web service add a new sparql endpoint url to the federation engine.
     */
    @POST
    @Path("/configureDatasources")
    public Response addDataSource(@FormParam("endpointUrl") String endpointURLs) {

        if ((endpointURLs == null) || (endpointURLs.isEmpty())) {
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("Empty list of data sources !").build();
        }

        String output = "";
        try {
            execDQP.addRemote(new URL(endpointURLs), WSImplem.REST);
            output += endpointURLs;
            output += " added to the federation engine";
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(output).build();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("URL exception while configuring KGRAM-DQP").build();
        }
    }

    @POST
    @Path("/testDatasources")
    @Produces("application/sparql-results+json")
    public Response testDataSource(@FormParam("endpointUrl") String endpointURL) {

        if ((endpointURL == null) || (endpointURL.isEmpty())) {
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("{\"test\" : false}").build();
        }

        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                    + "SELECT distinct ?x ?p ?y WHERE"
                    + "{"
                    + "     ?x ?p ?y ."
                    + "}"
                    + "     LIMIT 1";

            ClientConfig config = new DefaultClientConfig();
            Client client = Client.create(config);
            WebResource service = client.resource(new URI(endpointURL));
            String response = service.queryParam("query", query).accept("application/sparql-results+xml").get(String.class);

            if (response.contains("sparql-results")) {
                return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("{\"test\" : true}").build();
            } else {
                return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("{\"test\" : false}").build();
            }

        } catch (URISyntaxException ex) {
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("{\"test\" : false}").build();
        } catch (ClientHandlerException ex) {
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("{\"test\" : false}").build();
        }

    }

    @GET
    @Produces("application/sparql-results+json")
    @Path("/sparql")
    public Response getTriplesJSONForGet(@QueryParam("query") String query) {

        QueryProcessDQP.queryCounter.clear();
        QueryProcessDQP.queryVolumeCounter.clear();
        QueryProcessDQP.sourceCounter.clear();

        logger.info("Federated querying: " + query);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings map = execDQP.query(query);
            logger.info(map.size() + " results in " + sw.getTime() + "ms");
            logCost();

            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(JSONFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Federated query processing with provenance documented results.
     *
     * @param query the SPARQL query to be sent over the federation.
     * @return JSON data describing SPARQL results and the associated PROV
     * graph.
     */
    @GET
    @Produces("application/sparql-results+json")
    @Path("/sparqlprov")
    public Response getProvTriplesJSONForGet(@QueryParam("query") String query) {

        logger.info("Federated querying: " + query);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings maps = execDQP.query(query);
            logger.info(maps.size() + " results in " + sw.getTime() + "ms");

            Graph resProv = Graph.create();

            for (Mapping map : maps) {
                for (Entity ent : map.getEdges()) {
                    Graph prov = (Graph) ent.getProvenance();
                    if ((prov != null) && (prov instanceof Graph)) {
                        resProv.copy(prov);
                    }
                }
            }

            //Filtering PROV annotations
            String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
                    + "CONSTRUCT {"
                    + " ?x ?p ?y ."
                    + "} WHERE {"
                    + " ?x ?p ?y ."
                    + " FILTER (?p NOT IN (rdf:type, prov:wasGeneratedBy, prov:qualifiedAssociation, prov:hadPlan, prov:agent, rdfs:comment)) "
                    + "} ";

//            String provQuery = "PREFIX prov:<" + Util.provPrefix + ">"
//                + "CONSTRUCT {"
//                + " ?x ?p ?y ."
//                + "} WHERE {"
//                + " ?x ?p ?y ."
//                + " FILTER (?p NOT IN (rdf:type)) "
//                + "} ";
//            String provQuery2 = "PREFIX prov:<" + Util.provPrefix + ">"
//                + "CONSTRUCT {"
//                + " ?s prov:wasAttributedTo ?ds ."
//                + " ?o prov:wasAttributedTo ?ds ."
//                + " ?s ?p ?o ."
//                + "} WHERE {"
//                + " ?x prov:wasAttributedTo ?ds ."
//                + " ?x rdf:subject ?s ."
//                + " ?x rdf:predicate ?p ."
//                + " ?x rdf:object ?o ."
//                + "}";
            QueryProcess qpProv = QueryProcess.create(resProv);
            Mappings mProv = qpProv.query(provQuery);

            String mapsProvJson = "{ \"mappings\" : "
                    + JSONFormat.create(maps).toString()
                    + " , "
                    + "\"d3\" : "
                    + JSOND3Format.create((Graph) mProv.getGraph()).toString()
                    + " }";

//            System.out.println(mapsProvJson);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsProvJson).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+xml")
    @Path("/sparql")
    public Response getTriplesXMLForGet(@QueryParam("query") String query) {
        logger.info("Federated querying: " + query);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings map = execDQP.query(query);
            logger.info(map.size() + " results in " + sw.getTime() + "ms");
            logCost();
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    private void logCost() {
        logger.debug(Messages.countQueries);
        logger.debug(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        logger.debug(Messages.countTransferredResults);
        logger.debug(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        logger.debug(Messages.countDS);
        logger.debug(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
    }
}
