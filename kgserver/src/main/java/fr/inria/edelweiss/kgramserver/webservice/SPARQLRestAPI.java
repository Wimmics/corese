
package fr.inria.edelweiss.kgramserver.webservice;

import java.io.File;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.CSVFormat;
import fr.inria.edelweiss.kgtool.print.JSOND3Format;
import fr.inria.edelweiss.kgtool.print.JSONFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TSVFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;

/**
 * KGRAM SPARQL endpoint exposed as a rest web service.
 * 
 * The engine can be remotely initialized, populated with an RDF file, and queried through SPARQL requests.
 *
 * @author Eric TOGUEM, eric.toguem@uy1.uninet.cm
 * @author Alban Gaignard, alban.gaignard@cnrs.fr
 * @author Olivier Corby
 */
@Path("sparql")
public class SPARQLRestAPI {
	private static final String headerAccept = "Access-Control-Allow-Origin";
	public static final String PROFILE_DEFAULT = "profile.ttl";
	public static final String DEFAULT = NSManager.STL + "default";
	static final int ERROR = 500;

	private static boolean isDebug = false;
	private static boolean isDetail = false;
	// set true to prevent update/load
	static boolean isProtected = !true;
	// true when Ajax
	static boolean isAjax = true;

	static String localProfile;

	static TripleStore store = new TripleStore(false, false);

	private static Profile mprofile;

	private final Logger logger = LogManager.getLogger(this.getClass());

	public SPARQLRestAPI() {
	}

	QueryProcess getQueryProcess() {
		return getTripleStore().getQueryProcess();
	}

	static TripleStore getTripleStore() {
		return store;
	}

	/**
	 * This webservice is used to reset the endpoint. This could be useful if we would like our endpoint to point on another dataset
	 */
	@POST
	@Path("/reset")
	public Response initRDF(@DefaultValue("false") @FormParam("owlrl") String owlrl, @DefaultValue("false") @FormParam("entailments") String entailments,
			@DefaultValue("false") @FormParam("load") String load, @FormParam("profile") String profile) {

		boolean ent = entailments.equals("true");
		boolean owl = owlrl.equals("true");
		boolean ld = load.equals("true");
		localProfile = profile;
		store = new TripleStore(ent, owl);
		init();
		if (ld) {
			// loadProfileData();
			Manager.getManager().init(store);
		}
		store.init(isProtected);
		mprofile.setProtect(isProtected);
		return Response.status(200).header(headerAccept, "*").entity("Endpoint reset").build();
	}

	void init() {
		mprofile = new Profile();
		// mprofile.init(Profile.WEBAPP_DATA, PROFILE_DEFAULT);
		// mprofile.initServer(PROFILE_DEFAULT);
		if (localProfile != null) {
			// mprofile.init("", localProfile);
			if (!localProfile.startsWith("http://") && !localProfile.startsWith("file://")) {
				localProfile = "file://" + localProfile;
			}
			System.out.println("Load: " + localProfile);
			// mprofile.init(localProfile);
		}
		mprofile.initServer(PROFILE_DEFAULT, localProfile);
	}

	static Profile getProfile() {
		return mprofile;
	}

	void loadProfileData() {
		for (Service s : mprofile.getServices()) {
			String[] load = s.getLoad();
			if (load != null) {
				getTripleStore().load(load);
			}
		}
	}

	/**
	 * This webservice is used to load a dataset to the endpoint. Therefore, if we have many files for our datastore, we could load them by recursivelly calling this webservice
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

		try {
			// path with extension : use extension
			// path with no extension : load as turtle
			// use case: rdf: is in Turtle
			if (getTripleStore().getMode() != QueryProcess.SERVER_MODE) {
				getTripleStore().load(remotePath, source);
			}
		} catch (LoadException ex) {
			logger.error(ex);
			return Response.status(404).header(headerAccept, "*").entity(output).build();
		}

		logger.info(output = "Successfully loaded " + remotePath);
		return Response.status(200).header(headerAccept, "*").entity(output).build();
	}

	@GET
	@Path("/debug")
	public Response setDebug(@QueryParam("value") String debug, @QueryParam("detail") String detail) {
		if (debug != null) {
			isDebug = debug.equals("true");
		}
		if (detail != null) {
			isDetail = detail.equals("true");
		}
		return Response.status(200).header(headerAccept, "*").entity("debug: " + isDebug + " ; " + "detail: " + isDetail).build();
	}

	// DQP query for triple store index
	@GET
	@Produces({ "application/sparql-results+xml", "application/xml", "text/plain" })
	public Response getTriplesXMLForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris, @QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get SPARQL Result XML/plain: " + query);
			if (query == null)
				throw new Exception("No query");

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	// Query Corese Server here
	@GET
	@Produces("application/sparql-results+json")
	public Response getTriplesJSONForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris,
			@QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get SPARQL Result JOSN: " + query);
			if (query == null)
				throw new Exception("No query");

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces("application/sparql-results+json")
	@Path("/draw")
	public Response getJSON(@QueryParam("query") String query) {
		if (logger.isDebugEnabled())
			logger.debug("Querying: " + query);
		try {
			if (query == null)
				throw new Exception("No query");
			Mappings maps = getTripleStore().query(query);
			logger.info(maps.size());

			Graph g = (Graph) maps.getGraph();
			String mapsProvJson = "{ \"mappings\" : " + JSONFormat.create(maps).toString() + " , " + "\"d3\" : " + JSOND3Format.create(g).toString() + " }";
			return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsProvJson).build();

		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM-DQP engine", ex);
			return Response.status(ERROR).header("Access-Control-Allow-Origin", "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces("application/sparql-results+json")
	@Path("/d3")
	public Response getTriplesJSONForGetWithGraph(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris,
			@QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Querying: " + query);
			if (query == null)
				throw new Exception("No query");
			Mappings m = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));

			String mapsD3 = "{ \"mappings\" : " + JSONFormat.create(m).toString() + " , " + "\"d3\" : " + JSOND3Format.create((Graph) m.getGraph()).toString() + " }";
			return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(mapsD3).build();

		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces("application/sparql-results+csv")
	public Response getTriplesCSVForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris, @QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get SPARQL Results CSV: " + query);
			if (query == null)
				throw new Exception("No query");

			return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces("application/sparql-results+tsv")
	public Response getTriplesTSVForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris, @QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get SPARQL Results TSV: " + query);
			if (query == null)
				throw new Exception("No query");

			return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces("application/rdf+xml")
	public Response getRDFGraphXMLForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris,
			@QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get RDF XML: " + query);
			if (query == null)
				throw new Exception("No query");

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@GET
	@Produces({"text/turtle", "application/turtle", "text/nt"})
	public Response getRDFGraphNTripleForGet(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris,
			@QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Get RDF Turtle: " + query);
			if (query == null)
				throw new Exception("No query");

			Mappings maps = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			String ttl = TripleFormat.create(maps, true).toString();
			logger.debug(ttl);
			return Response.status(200).header(headerAccept, "*").entity(ttl).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces({ "application/sparql-results+xml", "application/xml", "text/plain" })
	@Consumes("application/x-www-form-urlencoded")
	public Response getTriplesXMLForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result XML/plain: " + query);
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result XML/plain: " + query);

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces("application/sparql-results+xml")
	public Response getXMLForPost(@DefaultValue("") @QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris,
			@QueryParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result XML: " + query);
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result XML/plain: " + query);

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces("application/sparql-results+json")
	// @Path("json")
	public Response getTriplesJSONForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result JSON: " + query);

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(JSONFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces("application/sparql-results+csv")
	public Response getTriplesCSVForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result CSV: " + query);

			return Response.status(200).header(headerAccept, "*").entity(CSVFormat.create(getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces("application/sparql-results+tsv")
	public Response getTriplesTSVForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post SPARQL Result TSV: " + query);

			return Response.status(200).header(headerAccept, "*").entity(TSVFormat.create(getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces("application/rdf+xml")
	public Response getRDFGraphXMLForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (query.equals(""))
				query = message;
			if (logger.isDebugEnabled())
				logger.debug("Rest Post RDF XML: " + query);

			Mappings map = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(200).header(headerAccept, "*").entity(ResultFormat.create(map).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	@POST
	@Produces({"text/turtle", "application/turtle", "text/nt"})
	public Response getRDFGraphNTripleForPost(@DefaultValue("") @FormParam("query") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris,
			@FormParam("named-graph-uri") List<String> namedGraphUris, String message) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Rest Post RDF NT: " + query);

			return Response.status(200).header(headerAccept, "*").entity(TripleFormat.create(getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris))).toString()).build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	/// SPARQL 1.1 Update ///
	// update via URL-encoded POST
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/update")
	public Response updateTriplesEncoded(@FormParam("update") String query, @FormParam("default-graph-uri") List<String> defaultGraphUris, @FormParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			logger.info(query);
			if (query != null) {
				getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			} else {
				logger.warn("Null update query !");
			}

			return Response.status(200).header(headerAccept, "*").build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
		}
	}

	/// SPARQL 1.1 Update ///
	// Direct update
	@POST
	@Consumes("application/sparql-update")
	@Path("/update")
	public Response updateTriplesDirect(String message, @QueryParam("default-graph-uri") List<String> defaultGraphUris, @QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			logger.info(message);
			if (message != null) {
				getTripleStore().query(message, createDataset(defaultGraphUris, namedGraphUris));
			} else {
				logger.warn("Null update query !");
			}

			return Response.status(200).header(headerAccept, "*").build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while updating the Corese/KGRAM endpoint").build();
		}
	}

	@HEAD
	public Response getTriplesForHead(@QueryParam("query") String query, @QueryParam("default-graph-uri") List<String> defaultGraphUris, @QueryParam("named-graph-uri") List<String> namedGraphUris) {
		try {
			Mappings mp = getTripleStore().query(query, createDataset(defaultGraphUris, namedGraphUris));
			return Response.status(mp.size() > 0 ? 200 : 400).header(headerAccept, "*").entity("Query has no response").build();
		} catch (Exception ex) {
			logger.error("Error while querying the remote KGRAM engine", ex);
			return Response.status(ERROR).header(headerAccept, "*").entity("Error while querying the remote KGRAM engine").build();
		}
	}

	/**
	 * Creates a Corese/KGRAM Dataset based on a set of default or named graph URIs. For *strong* SPARQL compliance, use dataset.complete() before returning the dataset.
	 *
	 * @param defaultGraphUris
	 * @param namedGraphUris
	 * @return a dataset if the parameters are not null or empty.
	 */
	private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
		return createDataset(defaultGraphUris, namedGraphUris, null);
	}

	private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris, Context c) {
		if (isProtected) {
			if (c == null) {
				c = new Context();
			}
			c.setUserQuery(true);
		}
		if (c != null || ((defaultGraphUris != null) && (!defaultGraphUris.isEmpty())) || ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
			Dataset ds = Dataset.instance(defaultGraphUris, namedGraphUris);
			ds.setContext(c);
			return ds;
		} else {
			return null;
		}
	}

	/**
	 * This function is used to copy the InputStream into a local file.
	 */
	private void writeToFile(InputStream uploadedInputStream, File uploadedFile) throws IOException {
		OutputStream out = new FileOutputStream(uploadedFile);
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = uploadedInputStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		out.flush();
		out.close();
	}

	String getTemplate(String name) {
		String sep = "";
		if (name.contains("/")) {
			sep = "'";
		}
		String query = "template { st:atw(" + sep + name + sep + ")} where {}";
		return query;
	}
}
