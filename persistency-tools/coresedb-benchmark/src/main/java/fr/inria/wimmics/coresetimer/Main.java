/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.RdfToGraph.DbDriver;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.wimmics.coresetimer.CoreseTimer.Profile;
import org.openrdf.rio.RDFFormat;
import fr.inria.corese.w3c.validator.W3CMappingsValidator;
import static fr.inria.corese.coresetimer.utils.VariousUtils.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.rio.Rio;

/**
 *
 * @author edemairy
 */
public class Main {

	private static Logger logger = LogManager.getLogger(Main.class.getName());

	public static class TestSuite {

		public enum DatabaseCreation {
			IF_NOT_EXIST,
			ALWAYS
		}

		private final String OUTPUT_FILE_FORMAT = "%s/result_%s.xml";

		private String inputMem;
		private DbDriver driver = RdfToGraph.DbDriver.TITANDB;
		private int defaultWarmupCycles = 5;
		private int defaultMeasuredCycles = 20;
		private String inputRoot;
		private String outputRoot;
		private String testId;
		private int nbTests = 0;
		private String inputDb;
		private RDFFormat format;
		private String outputFilename;

		private TestSuite(String id) {
			this.testId = id;
		}

		static public TestSuite build(String id) {
			return new TestSuite(id);
		}

		public String getId() {
			return testId;
		}

		public TestSuite setWarmupCycles(int warmup) {
			this.defaultWarmupCycles = warmup;
			return this;
		}

		public TestSuite setMeasuredCycles(int warmup) {
			this.defaultMeasuredCycles = warmup;
			return this;
		}

		public TestSuite setInputFilesPattern(String input) {
			return this.setInputFilesPattern(input, Rio.getParserFormatForFileName(input).orElse(RDFFormat.NQUADS));
		}

		public TestSuite setInputFilesPattern(String input, RDFFormat format) {
			this.inputMem = input;
			this.format = format;
			inputDb = input.replaceFirst("\\..+", "_db");
			return this;
		}

		public String getInputFilesPattern() {
			return inputRoot + inputMem;
		}

		public TestSuite setDriver(DbDriver driver) {
			this.driver = driver;
			return this;
		}

		public TestSuite setInputRoot(String path) {
			inputRoot = ensureEndWith(path, "/");
			return this;
		}

		public String getInputRoot() {
			return inputRoot;
		}

		@Deprecated
		public TestSuite setOutputRoot(String path) {
			outputRoot = ensureEndWith(path, "/");
			return this;
		}

		public TestSuite setOutputFile(String filename) {
			this.outputFilename = filename;
			return this;
		}

		public String getOutputFile() {
			return this.outputFilename;
		}

		public String getOutputRoot() {
			return outputRoot;
		}

		public TestSuite createDb(DatabaseCreation mode) throws Exception {
			String databasePath = getInputDb();
			if (Files.exists(Paths.get(databasePath)) && (mode == DatabaseCreation.IF_NOT_EXIST)) {
				logger.info("Not creating database since it already exists at {}", databasePath);
			} else {
				logger.info("Creating database at {}", databasePath);
				RdfToGraph.build().setDriver(driver).convertFileToDb(getInputFilesPattern(), format, getInputDb());
			}
			return this;
		}

		public String getInputDb() {
			return inputDb;
		}

		/**
		 * Set the path to the db. 
		 * @see 
		 * @param path
		 * @return 
		 */
		public TestSuite setInputDb(String path) {
			this.inputDb = path;
			return this;
		}

		public TestSuite setFormat(RDFFormat newFormat) {
			format = newFormat;
			return this;
		}

		public RDFFormat getFormat() {
			return format;
		}

		public TestDescription buildTest(String request) {
			String testId = this.testId + "_" + nbTests;
			TestDescription newTest = TestDescription.build(testId, this)
				.setMeasuredCycles(defaultMeasuredCycles)
				.setWarmupCycles(defaultWarmupCycles)
				.setInputFilesPattern(getInputFilesPattern())
				.setOutputPath(String.format(OUTPUT_FILE_FORMAT, getOutputRoot(), testId+"_%s"))
				.setRequest(request)
				.setInputDb(getInputDb());

			nbTests++;
			return newTest;
		}

	}

	public final static String[] queries = { // @TODO afficher pour chaque requête le nombre de résultats.
	// @TODO jointure
	// @ select distinct ?p  where {?e ?p ?y}  [order by ?p]
	// @TODO tester les littéraux
	//		"select (count(*) as ?count) where { graph ?g {?x ?p ?y}}",
	//		"select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}",// limit 10000",
	//		"select * where { ?x  a ?y }", // biaisé car beaucoup de données sont typées
	//		"select (count(*) as ?c) where {?x a ?y}", // permet de supprimer le coût de construction du résultat.
	//				"select * where { <http://prefix.cc/popular/all.file.vann>  ?p ?y . ?y ?q <http://prefix.cc/popular/all.file.vann> .} limit 10000"
	//				"select * where { ?x ?p ?y . ?y ?q ?x }" // Intractable: if there are 10^6 edges, requests for 10^12 edges. @TODO Traiter la jointure.
	//		"select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"
	// Campagne de tests
	// 
	// 
	// Famille de tests 1	
	// BGP: on connait une valeur, la bd sait rechercher cette valeur instantanément ? Efficacité 
	/*		s ?p ?o 
	               ?s ?p o 2 cas principaux :URI (1cas), Literal (String, int, double, 
		humans : avec des requêtes 	
		  X ?p ?y . ?z ?q ?y
		  X ?p ?y . ?y ?q ?z
		 URI ?p ?y . 
		 ?x ?p URI. ?p ?z ?t
		 ?x p ?x

		 tester d'abord les propriétés fixées 
		 tester ensuite les propriétés libres

	Famille de tests 2
		?x ?p ?y . filter(contains(?y, " ")) . ?y ?q ?z  (même chose que précédemment, mais avec des filtres)
	Famille de tests 3 
		Cycles de longueur 2, 3, etc.	

	Famille de tests 4
		tester les 16 cas.

	1. Sémantique
	2. Benchmark
	 */};

	public static boolean compareResults(Mappings map_db, Mappings map_memory) {
		W3CMappingsValidator tester = new W3CMappingsValidator();
		boolean result = tester.validate(map_db, map_memory) && tester.validate(map_memory, map_db) && map_memory.size() == map_db.size();
		return result;
	}


}
