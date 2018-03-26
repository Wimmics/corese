package fr.inria.corese.core.load.sesame;

import fr.inria.corese.core.load.rdfa.RDFaLoader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * New alternative turtle parser using sesame, also for n-triples, n-quads and
 * TriG, at the moment this parser is not deployed in corese
 *
 * TurtleLoaderSesame.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date Mar 10, 2014
 */
public class ParserLoaderSesame {

	private static Logger logger = LoggerFactory.getLogger(RDFaLoader.class);

	Reader reader;
	InputStream is;
	String base;

	public ParserLoaderSesame(InputStream r, String base) {
		this.is = r;
		this.base = base;
	}

	public ParserLoaderSesame(Reader r, String base) {
		this.reader = r;
		this.base = base;
	}

	public static ParserLoaderSesame create(InputStream read, String base) {
		ParserLoaderSesame p = new ParserLoaderSesame(read, base);
		return p;
	}

	public static ParserLoaderSesame create(Reader read, String base) {
		ParserLoaderSesame p = new ParserLoaderSesame(read, base);
		return p;
	}

	public static ParserLoaderSesame create(String file) {
		FileReader read;
		try {
			read = new FileReader(file);
			ParserLoaderSesame p = new ParserLoaderSesame(read, file);
			return p;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load triples from TURTLE file to graph
	 *
	 * @param handler
	 * @param format
	 * @throws java.io.IOException
	 * @throws org.openrdf.rio.RDFParseException
	 * @throws org.openrdf.rio.RDFHandlerException
	 */
	public void loadWithSesame(RDFHandlerBase handler, RDFFormat format) throws IOException, RDFParseException, RDFHandlerException {
		RDFParser parser = Rio.createParser(format);
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		config.addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		parser.setParserConfig(config);
		parser.setRDFHandler(handler);
		//System.out.println(this.base+format);
		parser.parse(this.reader, this.base);
	}
}
