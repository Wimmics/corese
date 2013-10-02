/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import java.net.MalformedURLException;
import java.net.URL;
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
    private static QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv);

    @POST
    @Path("/reset")
    public Response resetDQP(@FormParam("endpointUrl") String endpointURLs) {
        //reset the query process DQP
        try {
            DqpRestAPI.graph = Graph.create(false);
            DqpRestAPI.sProv = ProviderImpl.create();
            DqpRestAPI.execDQP = QueryProcessDQP.create(graph, sProv);
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
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response addDataSource(@FormParam("endpointUrl") List<String> endpointURLs) {
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
            System.out.println("");
            System.out.println("***********************************************************");
            System.out.println("***********************************************************");
            System.out.println("Remote queries");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
            System.out.println("Transferred results per query");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
            System.out.println("Remote queries per data source");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));


            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(JSONFormat.create(map).toString()).build();
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
            System.out.println("");
            System.out.println("***********************************************************");
            System.out.println("***********************************************************");
            System.out.println("Remote queries");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
            System.out.println("Transferred results per query");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
            System.out.println("Remote queries per data source");
            System.out.println(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));

            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(ResultFormat.create(map).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM-DQP engine");
            ex.printStackTrace();
            return Response.status(500).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }
}
