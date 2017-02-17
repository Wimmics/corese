/*
 * Copyright Inria 2016. 
 */
package fr.inria.corese.rdftograph;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.AbstractRDFHandler;
import org.openrdf.rio.helpers.BasicParserSettings;

import org.openrdf.rio.helpers.NTriplesParserSettings;

/**
 * This application: (i) read a RDF file using sesame library; (ii) write the
 * content into a neo4j DB using the neo4j library.
 *
 * @author edemairy
 */
public class RdfToGraph {

	public enum DbDriver {
		NEO4J, ORIENTDB, TITANDB
	}

	private static Logger LOGGER = Logger.getLogger(RdfToGraph.class.getName());
	protected Model model;
	protected GdbDriver driver;
	private static final Map<DbDriver, String> DRIVER_TO_CLASS;

	static {
		DRIVER_TO_CLASS = new HashMap<>();
		DRIVER_TO_CLASS.put(DbDriver.NEO4J, "fr.inria.corese.rdftograph.driver.Neo4jDriver");
		DRIVER_TO_CLASS.put(DbDriver.ORIENTDB, "fr.inria.corese.rdftograph.driver.OrientDbDriver");
		DRIVER_TO_CLASS.put(DbDriver.TITANDB, "fr.inria.corese.rdftograph.driver.TitanDriver");
	}

	private class VerticesBuilder extends AbstractRDFHandler {

		private int triples = 0;

		@Override
		public void handleStatement(Statement statement) {
			Resource source = statement.getSubject();
			Value object = statement.getObject();

			triples++;
			if (triples % CHUNK_SIZE == 0) {
				LOGGER.log(Level.INFO, "{0}", triples);
				try {
					driver.commit();
				} catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "Trying to pursue after: {0}", ex.getMessage());
					ex.printStackTrace();
				}
			}
			try {
				driver.createNode(source);
				driver.createNode(object);
				driver.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class EdgesBuilder extends AbstractRDFHandler {

		private int triples = 0;

		@Override
		public void handleStatement(Statement statement) {
			Resource context = statement.getContext();
			Resource source = statement.getSubject();
			IRI predicat = statement.getPredicate();
			Value object = statement.getObject();

			String contextString = (context == null) ? "" : context.stringValue();

			Object sourceNode = driver.getNode(source);
			Object objectNode = driver.getNode(object);

			Map<String, Object> properties = new HashMap();
			properties.put(EDGE_G, contextString);
			driver.createRelationship(sourceNode, objectNode, predicat.stringValue(), properties);
			triples++;
			if (triples % 100_000 == 0) {
				LOGGER.info("triples = " + triples);
			}
			if (triples % CHUNK_SIZE == 0) {
				LOGGER.log(Level.INFO, "{0}", triples);
				try {
					driver.commit();
				} catch (Exception ex) {
					LOGGER.log(Level.SEVERE, "Trying to pursue after: {0}", ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}

	public RdfToGraph() {
	}

	/**
	 *
	 * @param driver Short name given in DRIVER_TO_CLASS for the driver to
	 * use.
	 */
	public RdfToGraph setDriver(DbDriver driver) {
		try {
			String driverClassName = DRIVER_TO_CLASS.get(driver);
			this.driver = buildDriver(driverClassName);
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, "Impossible to set driver to {0}, caused by ", driver);
			ex.printStackTrace();
		}
		return this;
	}

	private static GdbDriver buildDriver(String driverName) throws Exception {
		GdbDriver result;
		Constructor driverConstructor = Class.forName(driverName).getConstructor();
		result = (GdbDriver) driverConstructor.newInstance();
		return result;
	}

	public RdfToGraph convertFileToDb(String rdfFileName, RDFFormat format, String dbPath) throws FileNotFoundException, IOException {
		InputStream inputStream;
		if (rdfFileName.endsWith(".gz")) {
			inputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(rdfFileName)));
		} else {
			inputStream = new FileInputStream(new File(rdfFileName));
		}
		convertStreamToDb(inputStream, format, dbPath);
		return this;
	}

	/**
	 * Read a RDF stream and serialize it inside a Neo4j graph.
	 *
	 * @param rdfStream Input stream containing rdf data
	 * @param format Format used for the rdf representation in the input
	 * stream
	 * @param dbPath Where to store the rdf data.
	 */
	public void convertStreamToDb(InputStream rdfStream, RDFFormat format, String dbPath) {
		try {
			LOGGER.info("** begin of convert **");
			LOGGER.log(Level.INFO, "opening the db at {0}", dbPath);
			driver.openDb(dbPath);
			LOGGER.info("Loading file");
			BufferedInputStream bufferedInput = new BufferedInputStream(rdfStream);
			bufferedInput.mark(Integer.MAX_VALUE);
			createVertices(bufferedInput, format);
			bufferedInput.reset();
			createEdges(bufferedInput, format);
			LOGGER.info("Writing graph in db");
			LOGGER.info("closing DB");
			driver.closeDb();
			LOGGER.info("** end of convert **");
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Exception during conversion: {0}", ex.toString());
		}
	}

	/**
	 * Fill model with the content of an input stream.
	 *
	 * @param in Stream on an RDF file.
	 * @param format Format used to represent the RDF in the file.
	 * @throws IOException
	 */
	public void createVertices(InputStream in, RDFFormat format) throws IOException {
		VerticesBuilder myCounter = new VerticesBuilder();
		RDFParser rdfParser = Rio.createParser(format);
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		config.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		rdfParser.setParserConfig(config);
		rdfParser.setRDFHandler(myCounter);
		try {
			rdfParser.parse(in, "");
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		driver.commit();
	}

	public void createEdges(InputStream in, RDFFormat format) throws IOException {
		EdgesBuilder edgesBuilder = new EdgesBuilder();
		RDFParser rdfParser = Rio.createParser(format);
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		config.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		rdfParser.setParserConfig(config);
		rdfParser.setRDFHandler(edgesBuilder);
		in.reset();
		try {
			rdfParser.parse(in, "");
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		driver.commit();
	}

	public RdfToGraph setWipeOnOpen(boolean b) {
		driver.setWipeOnOpen(b);
		return this;
	}

	final static private int CHUNK_SIZE = 10_000; //Integer.MAX_VALUE;

	public void writeModelToNeo4j() {
		int triples = 0;
		for (Statement statement : model) {
			Resource context = statement.getContext();
			Resource source = statement.getSubject();
			IRI predicat = statement.getPredicate();
			Value object = statement.getObject();

			String contextString = (context == null) ? "" : context.stringValue();

			driver.createNode(source);
			driver.createNode(object);

			Map<String, Object> properties = new HashMap();
			properties.put(EDGE_G, contextString);
			driver.createRelationship(source, object, predicat.stringValue(), properties);
			triples++;
			if (triples % CHUNK_SIZE == 0) {
				LOGGER.info("" + triples);
				driver.commit();
			}
		}
		System.out.println(triples + " processed");
	}

	public static String getKind(Value resource) {
		if (isLargeLiteral(resource)) {
			return LARGE_LITERAL;
		} else if (isLiteral(resource)) {
			return LITERAL;
		} else if (isIRI(resource)) {
			return IRI;
		} else if (isBNode(resource)) {
			return BNODE;
		}
		throw new IllegalArgumentException("Impossible to find the type of:" + resource.stringValue());
	}

	private static boolean isType(Class c, Object o) {
		try {
			c.cast(o);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean isLargeLiteral(Value resource) {
		if (isType(Literal.class, resource)) {
			Literal l = (Literal) resource;	
			return l.getLabel().length() > MAX_INDEXABLE_LENGTH;
		} else {
			return false;
		}
	}
	
	private static boolean isLiteral(Value resource) {
		return isType(Literal.class, resource);
	}

	private static boolean isIRI(Value resource) {
		return isType(IRI.class, resource);
	}

	private static boolean isBNode(Value resource) {
		return isType(BNode.class, resource);
	}

}
