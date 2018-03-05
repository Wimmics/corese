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
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.Messages;
import fr.inria.edelweiss.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.JSOND3Format;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * REST API to expose main features of the KGRAM-DQP federation engine.
 *
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 */
@Path("dqp")
public class DqpRestAPI {

    private Logger logger = LogManager.getLogger(DqpRestAPI.class);
    private static Graph graph = Graph.create(false);
    private static ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
    private static QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, false);

    /**
     * This web service reset the local KGRAM graph. By default, RDFS
     * entailments and provenance are desactivated.
     *
     * @return a confirmation message to be possibly displayed from client side.
     */
    @POST
    @Path("/reset")
    public Response resetDQP() {
        try {
            DqpRestAPI.graph = Graph.create(false);
            DqpRestAPI.sProv = ProviderImplCostMonitoring.create();
            DqpRestAPI.execDQP = QueryProcessDQP.create(graph, sProv, false);

            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("Reinitialized KGRAM-DQP federation engine").build();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Exception while reseting KGRAM-DQP").build();
        }
    }

    /**
     * Web service adding a new sparql endpoint url to the federation engine.
     *
     * @param endpointURL the URL of the SPARQL endpoint to be federated.
     * @return a confirmation message to be possibly displayed from client side.
     */
    @POST
    @Path("/configureDatasources")
    public Response addDataSource(@FormParam("endpointUrl") String endpointURL) {

        if ((endpointURL == null) || (endpointURL.isEmpty())) {
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity("Empty list of data sources !").build();
        }

        String output = "";
        try {
            execDQP.addRemote(new URL(endpointURL), WSImplem.REST);
            output += endpointURL;
            output += " added to the federation engine";
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(output).build();
        } catch (MalformedURLException ex) {
            logger.error(endpointURL + " is a malformed URL");
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("URL exception while configuring KGRAM-DQP").build();
        }
    }

    /**
     * Web service testing if a SPARQL endpoint is available.
     *
     * @param endpointURL the URL of the SPARQL endpoint to be tested.
     * @return JSON data with true or false depending on the availability of the
     * endpoint.
     */
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

    /**
     * Web service for federated query processing.
     *
     * @param query the initial SPARQL query.
     * @param tpgrouping a boolean for enabling/disabling tripple patterns
     * grouping into SERVICE clauses.
     * @param slicing an integer value describing the slicing parameter, used to
     * transmit intermediate bindings by blocks. The slice number corresponds to
     * the size of each block of bindings.
     * @return a JSON serialization of the SPARQL results.
     */
    @GET
    @Produces("application/sparql-results+json")
    @Path("/sparql")
    public Response getTriplesJSONForGet(@QueryParam("query") String query, @DefaultValue("false") @QueryParam("tpgrouping") String tpgrouping, @QueryParam("slicing") String slicing) {

        QueryProcessDQP.queryCounter.clear();
        QueryProcessDQP.queryVolumeCounter.clear();
        QueryProcessDQP.sourceCounter.clear();
        QueryProcessDQP.sourceVolumeCounter.clear();

        logger.info("Federated querying: " + query);

        // for TP groupping in SERVICE clauses
        if (!tpgrouping.equals("false")) {
            DqpRestAPI.execDQP.setGroupingEnabled(true);
            logger.info("Service grouping enabled");

            // for slicing in SERVICE clauses
            try {
                int sliceNb = Integer.valueOf(slicing);
                if (sliceNb > 0) {
                    DqpRestAPI.execDQP.addPragma(Pragma.SERVICE, Pragma.SLICE, sliceNb);
                    logger.info("Slicing set to " + sliceNb);
                }
            } catch (NumberFormatException ex) {
                logger.warn(slicing + " is not formatted as number for the slicing parameter");
                logger.warn("Slicing disabled");
            }
        }

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings map = execDQP.query(query);
            sw.stop();
            logger.info(Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter));
            logger.info(map.size() + " results in " + sw.getTime() + "ms");
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(JSONFormat.create(map).toString()).build();
        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Web service for federated query processing with provenance documented
     * results.
     *
     * @param query the SPARQL query to be sent over the federation.
     * @return JSON data describing SPARQL results and the associated PROV
     * graph.
     */
    @GET
    @Produces("application/sparql-results+json")
    @Path("/sparqlprov")
    public Response getProvTriplesJSONForGet(@QueryParam("query") String query, @DefaultValue("false") @QueryParam("tpgrouping") String tpgrouping, @QueryParam("slicing") String slicing) {

        DqpRestAPI.sProv = ProviderImplCostMonitoring.create();
        DqpRestAPI.execDQP.set(sProv);
        
        DqpRestAPI.execDQP.setProvEnabled(true);
        DqpRestAPI.sProv.setProvEnabled(true);
        logger.info("Federated querying: " + query);

        QueryProcessDQP.queryCounter.clear();
        QueryProcessDQP.queryVolumeCounter.clear();
        QueryProcessDQP.sourceCounter.clear();
        QueryProcessDQP.sourceVolumeCounter.clear();

        logger.info("Federated querying: " + query);

        // for TP groupping in SERVICE clauses
        if (!tpgrouping.equals("false")) {
            DqpRestAPI.execDQP.setGroupingEnabled(true);
            logger.info("Service grouping enabled");

            // for slicing in SERVICE clauses
            try {
                int sliceNb = Integer.valueOf(slicing);
                if (sliceNb > 0) {
                    DqpRestAPI.execDQP.addPragma(Pragma.SERVICE, Pragma.SLICE, sliceNb);
                    logger.info("Slicing set to " + sliceNb);
                }
            } catch (NumberFormatException ex) {
                logger.warn(slicing + " is not formatted as number for the slicing parameter");
                logger.warn("Slicing disabled");
            }
        }

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings maps = execDQP.query(query);
            logger.info(maps.size() + " results in " + sw.getTime() + "ms");
            logger.info(Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter));

            Graph resProv = Graph.create();

            for (Mapping map : maps) {
                for (Entity ent : map.getEdges()) {
                    Graph prov = (Graph) ent.getProvenance();
                    if (prov != null) {
                        resProv.copy(prov);
                    }
                }
            }

            //For service provenance, to be cleaned
            if (sProv.getProvenance() != null) {
                resProv.copy(sProv.getProvenance());
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

            execDQP.setProvEnabled(false);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsProvJson).build();
        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            logger.error(ex.getMessage());
            execDQP.setProvEnabled(false);
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Web service for federated query processing with XML SPARQL results.
     *
     * @param query the initial SPARQL query.
     * @return the XML sparql results.
     */
    @GET
    @Produces("application/sparql-results+xml")
    @Path("/sparql")
    public Response getTriplesXMLForGet(@QueryParam("query") String query) {

        QueryProcessDQP.queryCounter.clear();
        QueryProcessDQP.queryVolumeCounter.clear();
        QueryProcessDQP.sourceCounter.clear();
        QueryProcessDQP.sourceVolumeCounter.clear();

        logger.info("Federated querying: " + query);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            Mappings map = execDQP.query(query);
            logger.info(map.size() + " results in " + sw.getTime() + "ms");
            logger.info(Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter));
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(ResultFormat.create(map).toString()).build();
        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            logger.error(ex.getMessage());
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Get the current monitored distributed query processing cost.
     *
     * @return the cost serialized into JSON.
     */
    @GET
    @Path("/getCost")
    public Response getCost() {
        String response = Util.jsonDqpCost(QueryProcessDQP.queryCounter, QueryProcessDQP.queryVolumeCounter, QueryProcessDQP.sourceCounter, QueryProcessDQP.sourceVolumeCounter);
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(response).build();
    }

    private void logCost() {
        logger.info(Messages.countQueries);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        logger.info(Messages.countTransferredResults);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        logger.info(Messages.countDS);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
    }
}
