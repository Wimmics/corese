/*
 * Copyright Inria 2016. 
 */
package fr.inria.corese.rdftograph;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
		NEO4J, ORIENTDB, TITANDB, NULLDRIVER
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
		DRIVER_TO_CLASS.put(DbDriver.NULLDRIVER, "fr.inria.wimmics.rdf.to.graph.nulldriver.NullDriver");
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

			Map<String, Object> properties = new HashMap();
			properties.put(EDGE_G, contextString);
			driver.createRelationship(source, object, predicat.stringValue(), properties);
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
		}
	}

	private RdfToGraph() {
	}

	public static RdfToGraph build() {
		return new RdfToGraph();
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

	public static class AddressesFilterInputStream extends FilterInputStream {

		int start, end;
		int currentLineNumber;
		public static int INFINITE = -1;

		public AddressesFilterInputStream(InputStream input, int start, int end) {
			super(input);
			this.start = start;
			this.end = end;
			this.in = input;
		}

		/**
		 * Read one byte *
		 */
		@Override
		public int read() throws IOException {
			if (currentLineNumber < start) {
				int c;
				while ((currentLineNumber < start) && ((c = in.read()) != -1)) {
					if ((char) c == '\n') {
						currentLineNumber++;
					} // else the character is ignored
				}
			}
			if (currentLineNumber >= start && (currentLineNumber < end || end == INFINITE)) {
				int c = in.read();
				if (c == '\n') {
					currentLineNumber++;
				}
				return c;
			}
			return -1;
		}

		@Override
		public int read(byte[] b) throws IOException {
			int cpt = 0;
			int curChar;
			while (cpt < b.length && ((curChar = read()) != -1)) {
				b[cpt++] = (byte) curChar;
			}
			if (cpt == 0) {
				return -1;
			} else {
				return cpt;
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			byte[] temp = new byte[len];
			int res = read(temp);
			if (res == -1) {
				return res;
			} else {
				for (int i = 0; i < len; i++) {
					b[off + i] = temp[i];
				}
				return res;
			}
		}
	}

	public static Optional<RDFFormat> getRdfFormat(String completeFilename) {
		String[] parts = completeFilename.split(":");
		return Rio.getParserFormatForFileName(parts[0]);
	}

	/**
	 * @TODO: refactor to have correct dependencies static final int
	 * RDFXML_FORMAT = 0; static final int RDFA_FORMAT = 1; static final int
	 * TURTLE_FORMAT = 2; static final int NT_FORMAT = 3; static final int
	 * JSONLD_FORMAT = 4; static final int RULE_FORMAT = 5; static final int
	 * QUERY_FORMAT = 6; static final int UNDEF_FORMAT = 7; static final int
	 * TRIG_FORMAT = 8; static final int NQUADS_FORMAT = 9; static final int
	 * WORKFLOW_FORMAT = 10;
	 * @param completeFilename
	 * @return
	 */
	public static int getCoreseRdfFormat(String completeFilename) {
		Optional<RDFFormat> rdfFormat = getRdfFormat(completeFilename);
		if (rdfFormat.isPresent()) {
			RDFFormat format = rdfFormat.get();
			if (format.equals(RDFFormat.RDFA)) {
				return 1;
			} else if (format.equals(RDFFormat.TURTLE)) {
				return 2;
			} else if (format.equals(RDFFormat.TURTLE)) {
				return 3;
			} else if (format.equals(RDFFormat.RDFJSON)) {
				return 4;
			} else if (format.equals(RDFFormat.TRIG)) {
				return 8;
			} else if (format.equals(RDFFormat.NQUADS)) {
				return 9;
			} else {
				return 7;
			}
		} else {
			return 7;
		}
	}

	/**
	 *
	 * @param completeFilename possible syntax: * file.nq data in any format
	 * (nq given only as an example) * file.nq.gz data compressed; *
	 * file.nq:num data from line 0 to num-1; * file.nq:start, end data from
	 * line start to end-1.
	 * @return
	 * @throws IOException
	 */
	public static InputStream makeStream(String completeFilename) throws IOException {
		InputStream result = null;

		String filename;
		int start;
		int end;
		if (completeFilename.contains(":")) {
			String[] parts = completeFilename.split(":");
			filename = parts[0];
			if (parts[1].contains(",")) {
				String[] addresses = parts[1].split(",");
				start = Integer.parseInt(addresses[0]);
				end = Integer.parseInt(addresses[1]);
			} else {
				start = 0;
				end = Integer.parseInt(parts[1]);
			}
		} else {
			start = 0;
			end = -1;
			filename = completeFilename;
		}
		File root;

		if (filename.startsWith("/")) { // absolute path
			String dirPath = filename.substring(0, filename.lastIndexOf("/") + 1);
			filename = filename.substring(filename.lastIndexOf("/")+1, filename.length());
			root = new File(dirPath);
		} else if (filename.contains("/")) { // relative path
			root = new File(filename.substring(0, filename.lastIndexOf("/")));
			filename = filename.substring(filename.lastIndexOf("/")+1, filename.length());
		} else {
			root = new File("."); // filename without path
		}
		final String finalFilename = filename;
		File[] files = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(finalFilename);
			}
		});
		Arrays.sort(files);
		boolean first = true;
		for (File file : files) {
			InputStream newStream;
			if (file.getName().endsWith(".gz")) {
				newStream = new AddressesFilterInputStream(new GZIPInputStream(new FileInputStream(file)), start, end);
			} else {
				newStream = new AddressesFilterInputStream(new BufferedInputStream(new FileInputStream(file)), start, end);
			}
			if (first) {
				result = newStream;
				first = false;
			} else {
				result = new SequenceInputStream(result, newStream);
			}
		}
		return result;
	}

	/**
	 * Read a RDF stream and serialize it inside a Neo4j graph.
	 *
	 * @param rdfStream Input stream containing rdf data
	 * @param format Format used for the rdf representation in the input
	 * stream
	 * @param dbPath Where to store the rdf data.
	 */
	public void convertFileToDb(String fileName, RDFFormat format, String dbPath) throws Exception {
		try {
			LOGGER.info("** begin of convert **");
			LOGGER.log(Level.INFO, "opening the db at {0}", dbPath);
			driver.createDatabase(dbPath);
			LOGGER.info("Loading file");
			createEdges(makeStream(fileName), format);
			driver.commit();
			LOGGER.info("Writing graph in db");
			LOGGER.info("closing DB");
			driver.closeDb();
			LOGGER.info("** end of convert **");
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Exception during conversion: {0}", ex.toString());
		}
	}

	public void createEdges(InputStream in, RDFFormat format) throws IOException {
		EdgesBuilder edgesBuilder = new EdgesBuilder();
		RDFParser rdfParser = Rio.createParser(format);
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		config.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		rdfParser.setParserConfig(config);
		rdfParser.setRDFHandler(edgesBuilder);
		try {
			rdfParser.parse(in, "");
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		in.close();
		driver.commit();
	}

	final static private int CHUNK_SIZE = 10_000; //Integer.MAX_VALUE;

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
