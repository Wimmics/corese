/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSOND3Format;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;

/**
 * KGRAM engine exposed as a rest web service. The engine can be remotely
 * initialized, populated with an RDF file, and queried through SPARQL requests.
 *
 * @author Eric TOGUEM, eric.toguem@uy1.uninet.cm
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 */
@Path("sparql")
public class SPARQLRestAPI {

    private Logger logger = Logger.getLogger(RemoteProducer.class);
    private static Graph graph = Graph.create(false);
    private static QueryProcess exec = QueryProcess.create(graph);
    private String headerAccept = "Access-Control-Allow-Origin";

    /**
     * This webservice is used to reset the endpoint. This could be useful if we
     * would like our endpoint to point on another dataset
     */
    @POST
    @Path("/reset")
    public Response initRDF(@DefaultValue("false") @FormParam("entailments") String entailments) {
        String output;
        exec.getGraph().remove();
        if (entailments.equals("false")) {
            graph = Graph.create(false);
            exec = QueryProcess.create(graph);
            logger.info(output = "Endpoint successfully reset *without* entailments.");
        } else {
            graph = Graph.create(true);
            exec = QueryProcess.create(graph);
            logger.info(output = "Endpoint successfully reset *with* entailments.");
        }
        return Response.status(200).header(headerAccept, "*").entity(output).build();
    }

//    @POST
//    @Path("/upload")
//    @Consumes("multipart/form-data")
//    public Response uploadFile(@FormDataParam("file") InputStream f) {
//
//        // your code here to copy file to destFile
//        System.out.println("Received file " + f);
//
//        String output;
//        logger.info(output = "File uploaded.");
//        return Response.status(200).header(headerAccept, "*").entity(output).build();
//    }

    /**
     * This webservice is used to load a dataset to the endpoint. Therefore, if
     * we have many files for our datastore, we could load them by recursivelly
     * calling this webservice
     */
    @POST
    @Path("/load")
    public Response loadRDF(@FormParam("remote_path") String remotePath, @FormParam("source") String source) {

        String output = "File Uploaded";
        if (source != null) {
            if (source.isEmpty()) {
                source = null;
            } else if (!source.startsWith("http://")) {
                source = "http://" + source;
            }
        }

        if (remotePath == null) {
            String error = "Null remote path";
            logger.error(error);
            return Response.status(404).header(headerAccept, "*").entity(error).build();
        }

        logger.debug(remotePath);

        if (remotePath.startsWith("http")) {
            if (remotePath.endsWith(".rdf") || remotePath.endsWith(".ttl") || remotePath.endsWith(".rdfs") || remotePath.endsWith(".owl")) {
                Load ld = Load.create(graph);
                ld.load(remotePath, source);
            } else {
                //TODO loading of .n3 or .nt
                logger.error("TODO loading of .n3 or .nt");
                return Response.status(404).header(headerAccept, "*").entity(output).build();
            }

        } else {
            logger.info("Loading " + remotePath);
            File f = new File(remotePath);
            if (!f.exists()) {
                logger.error(output = "File " + remotePath + " not found on the server!");
                return Response.status(404).header(headerAccept, "*").entity(output).build();
            }
            if (f.isDirectory()) {
                Load ld = Load.create(graph);
                ld.load(remotePath, source);
            } else if (remotePath.endsWith(".rdf") || remotePath.endsWith(".rdfs") || remotePath.endsWith(".ttl") || remotePath.endsWith(".owl")) {
                Load ld = Load.create(graph);
                ld.load(remotePath, source);
            } else if (remotePath.endsWith(".n3") || remotePath.endsWith(".nt")) {
                FileInputStream fis = null;
                logger.warn("NOT Loaded " + f.getAbsolutePath());
            }
        }
        logger.info(output = "Successfully loaded " + remotePath);
        return Response.status(200).header(headerAccept, "*").entity(output).build();
    }

    @GET
    @Produces("application/sparql-results+xml")
    public Response getTriplesXMLForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(
                    ResultFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+json")
    public Response getTriplesJSONForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+json")
    @Path("/d3")
    public Response getTriplesJSONForGetWithGraph(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {

            Mappings m = exec.query(query, createDataset(defaultGraphUris, namedGraphUris));

            String mapsD3 = "{ \"mappings\" : "
                    + JSONFormat.create(m).toString()
                    + " , "
                    + "\"d3\" : "
                    + JSOND3Format.create((Graph) m.getGraph()).toString()
                    + " }";

//            System.out.println(mapsD3);
            return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsD3).build();

        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+csv")
    public Response getTriplesCSVForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/sparql-results+tsv")
    public Response getTriplesTSVForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("application/rdf+xml")
    public Response getRDFGraphXMLForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @GET
    @Produces("text/turtle")
    public Response getRDFGraphNTripleForGet(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            Mappings maps = exec.query(query, createDataset(defaultGraphUris, namedGraphUris));
            String ttl = TripleFormat.create(maps, true).toString();
            logger.debug(query);
            logger.debug(ttl);
            return Response.status(200).header(headerAccept, "*").entity(ttl).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+xml")
    public Response getTriplesXMLForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+json")
    public Response getTriplesJSONForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+csv")
    public Response getTriplesCSVForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/sparql-results+tsv")
    public Response getTriplesTSVForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("application/rdf+xml")
    public Response getRDFGraphXMLForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            if (query.equals("")) {
                query = message;
            }
            return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    @POST
    @Produces("text/nt")
    public Response getRDFGraphNTripleForPost(@DefaultValue("")
            @FormParam("query") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            String message) {
        try {
            return Response.status(200).header(headerAccept, "*").entity(TripleFormat.create(exec.query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /// SPARQL 1.1 Update ///
    //update via URL-encoded POST
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/update")
    public Response updateTriplesEncoded(@FormParam("update") String query,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            logger.info(query);
            if (query != null) {
                exec.query(query, createDataset(defaultGraphUris, namedGraphUris));
            } else {
                logger.warn("Null update query !");
            }

            return Response.status(200).header(headerAccept, "*").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
        }
    }

    /// SPARQL 1.1 Update ///
    //Direct update 
    @POST
    @Consumes("application/sparql-update")
    @Path("/update")
    public Response updateTriplesDirect(String message,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            logger.info(message);
            if (message != null) {
                exec.query(message, createDataset(defaultGraphUris, namedGraphUris));
            } else {
                logger.warn("Null update query !");
            }

            return Response.status(200).header(headerAccept, "*").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return Response.status(500).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
        }
    }

    @HEAD
    public Response getTriplesForHead(@QueryParam("query") String query,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris) {
        try {
            Mappings mp = exec.query(query, createDataset(defaultGraphUris, namedGraphUris));
            return Response.status(mp.size() > 0 ? 200 : 400).header(headerAccept, "*").entity("Query has no response").build();
        } catch (Exception ex) {
            logger.error("Error while querying the remote KGRAM engine");
            return Response.status(500).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
        }
    }

    /**
     * Creates a Corese/KGRAM Dataset based on a set of default or named graph
     * URIs. For *strong* SPARQL compliance, use dataset.complete() before
     * returning the dataset.
     *
     * @param defaultGraphUris
     * @param namedGraphUris
     * @return a dataset if the parameters are not null or empty.
     */
    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
        if (((defaultGraphUris != null) && (!defaultGraphUris.isEmpty())) || ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
            Dataset ds = Dataset.newInstance(defaultGraphUris, namedGraphUris);
            return ds;
        } else {
            return null;
        }
    }

    /**
     * This function is used to copy the InputStream into a local file.
     */
    private void writeToFile(InputStream uploadedInputStream,
            File uploadedFile) throws IOException {
        OutputStream out = new FileOutputStream(uploadedFile);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
}
