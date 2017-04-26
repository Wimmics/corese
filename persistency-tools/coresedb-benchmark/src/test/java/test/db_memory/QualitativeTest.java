/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import static fr.inria.corese.coresetimer.utils.VariousUtils.ensureEndWith;
import static fr.inria.corese.coresetimer.utils.VariousUtils.getEnvWithDefault;
import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.wimmics.coresetimer.CoreseTimer;
import fr.inria.wimmics.coresetimer.Main.TestDescription;
import fr.inria.wimmics.coresetimer.Main.TestSuite;
import static fr.inria.wimmics.coresetimer.Main.TestSuite.DatabaseCreation.IF_NOT_EXIST;
import static fr.inria.wimmics.coresetimer.Main.compareResults;
import static fr.inria.wimmics.coresetimer.Main.writeResult;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author edemairy
 */
public class QualitativeTest {

	String inputRoot;
	String outputRoot;

	private static final Logger logger = Logger.getLogger(QualitativeTest.class);

	public QualitativeTest() {
	}

	@BeforeClass
	public void setup() {
		inputRoot = getEnvWithDefault("INPUT_ROOT", "./data/");
		inputRoot = ensureEndWith(inputRoot, "/");
		outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./outputs/");
		outputRoot = ensureEndWith(inputRoot, "/");
	}

	/**
	 * Purpose of these tests: know what parts of SPARQL are handled with
	 * corese-db
	 */
	@DataProvider(name = "qualitative", parallel = false)
	public Object[][] getResults() throws Exception {
		TestSuite suite = TestSuite.build("base_tests").
			setDriver(RdfToGraph.DbDriver.NEO4J).
			setWarmupCycles(2).
			setMeasuredCycles(5).
			setInput("human_2007_04_17.rdf", RDFFormat.RDFXML).
			setInputDb("/tmp/human_db").
			setInputRoot(inputRoot).
			setOutputRoot(outputRoot);
		suite.createDb(IF_NOT_EXIST);
		TestDescription[][] tests = {
			{suite.buildTest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdf:type ?t }")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdf:type rdfs:Class }")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdfs:subClassOf ?y }")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#> \nSELECT * WHERE { ?x humans:hasSpouse ?y}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?x rdf:type humans:Male}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?y rdf:type humans:Male}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT (count(*) as ?count) WHERE { ?y humans:hasFriend ?z }")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT ?x WHERE { { ?y humans:hasChild ?x } UNION { ?x humans:hasParent ?y }}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?person ?age\n" + "WHERE\n" + "{\n" + " ?person rdf:type humans:Person\n" + " OPTIONAL { ?person humans:age ?age }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "ASK\n" + "WHERE\n" + "{\n" + " <http://www.inria.fr/2007/04/17/humans.rdfs-instances#Mark> humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + "  ?x rdf:type humans:Lecturer .\n" + "  ?x rdf:type ?t \n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Male .\n" + " ?x rdf:type humans:Person .\n" + " ?x rdf:type ?t\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Person\n" + " OPTIONAL\n" + " {\n" + "   ?x rdf:type ?t\n" + "   FILTER ( ?t = humans:Researcher )\n" + " }\n" + " FILTER ( ! bound( ?t ) )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?y \n" + "WHERE\n" + "{\n" + " ?x humans:hasAncestor ?y\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"size\"@en .\n" + "  ?t rdfs:label ?le .\n" + "  ?t rdfs:comment ?ce .\n" + "  FILTER ( lang(?le) = 'en' && lang(?ce) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"person\"@en .\n" + "  ?t rdfs:label ?syn .\n" + "  FILTER ( ?syn != \"person\"@en && lang(?syn) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?lf \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"shoe size\"@en .\n" + "  ?t rdfs:label ?lf .\n" + "  FILTER ( lang(?lf) = 'fr' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "  ?type rdfs:label ?l .\n" + "  {\n" + "   {\n" + "    ?laura rdf:type ?type\n" + "   }\n" + "   UNION\n" + "   {\n" + "    {\n" + "     ?laura ?type ?with\n" + "    }\n" + "    UNION\n" + "    {\n" + "     ?from ?type ?laura\n" + "    }\n" + "   }\n" + "  }\n" + "  FILTER ( lang(?l) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "DESCRIBE ?laura\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "CONSTRUCT \n" + "{\n" + " ?x rdf:type humans:Man\n" + "}\n" + "WHERE\n" + "{\n" + " {\n" + "  ?x rdf:type humans:Man\n" + " }\n" + "  UNION\n" + " {\n" + "  ?x rdf:type humans:Male .\n" + "  ?x rdf:type humans:Person\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * WHERE\n" + "{\n" + " ?x rdf:type humans:Person .\n" + " ?x humans:name ?name .\n" + " FILTER ( regex(?name, '.*ar.*') )\n" + "}")}, //		TestDescription.build("1m_select_s_1").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>  ?p ?y .}")
		};
		return tests;
	}

	/**
	 * The purpose of these tests is to push the limits of the amount of
	 * data that can be handled by coresedb.
	 */
	@DataProvider(name = "quantitative")
	public Iterator<Object[]> buildTests() throws Exception {
		String[] inputFiles = {
			//			"btc-2010-chunk-000.nq:1",
			//			"btc-2010-chunk-000.nq:3",
			//			"btc-2010-chunk-000.nq:10",
			//			"btc-2010-chunk-000.nq:31",
			//			"btc-2010-chunk-000.nq:100",
			//			"btc-2010-chunk-000.nq:316",
			//			"btc-2010-chunk-000.nq:1000",
			//			"btc-2010-chunk-000.nq:3162",
			//			"btc-2010-chunk-000.nq:10000",
			"btc-2010-chunk-000.nq:31622",
			"btc-2010-chunk-000.nq:100000",
			"btc-2010-chunk-000.nq:316227",
			"btc-2010-chunk-000.nq:1000000",
			"btc-2010-chunk-000.nq:3162277",
			"btc-2010-chunk-000.nq", //                      "btc-2010-chunk-00(0|1|2|3).nq.gz"      ,
		//                      "btc-2010-chunk-00(0|1|2|3|4|5|6|7|8|9).nq.gz"  ,
		//                      "btc-2010-chunk-0(0|1|2|3)(0|1|2|3|4|5|6|7|8|9).nq.gz"  ,
		//                      "btc-2010-chunk-0(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9).nq.gz"   
		};
		String[] requests = {
			"select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c", 
			"select ?x ?y where { ?x rdf:type ?y}",
			"select ?x ?p ?y ?q where { ?x ?p ?y . ?y ?q ?x}", 
			"select ?x ?p ?y where { ?x ?p ?y} order by ?x ?p ?y"
		};

		return new Iterator<Object[]>() {
			boolean started = false;
			int cptInputFiles = 0;
			int cptRequests = 0;
			TestSuite currentSuite;

			@Override
			public boolean hasNext() {
				if (inputFiles.length == 0 || requests.length == 0) {
					return false;
				}
				if (started) {
					return !(cptInputFiles == inputFiles.length - 1 && cptRequests == requests.length - 1);
				} else {
					return true;
				}
			}

			@Override
			public Object[] next() {
				if (started) {
					cptRequests++;
					if (cptRequests >= requests.length) {
						cptRequests = 0;
						cptInputFiles++;
					}
					if (cptInputFiles >= inputFiles.length) {
						throw new IllegalArgumentException("no more elements");
					}
				} else {
					started = true;
				}

				String inputFile = inputFiles[cptInputFiles];
				String request = requests[cptRequests];

				if (cptRequests == 0) {
					currentSuite = TestSuite.build("test_" + inputFile).
						setDriver(RdfToGraph.DbDriver.NEO4J).
						setWarmupCycles(2).
						setMeasuredCycles(5).
						setInput(inputFile).
						setInputDb("/tmp/" + inputFile.replace(":", "_").replace(",", "_") + "_db").
						setInputRoot(inputRoot).
						setOutputRoot(outputRoot);
					try {
						currentSuite.createDb(TestSuite.DatabaseCreation.ALWAYS);
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new RuntimeException(ex);
					}
				}
				TestDescription[] result = {currentSuite.buildTest(request)};
				return result;
			}
		};
	}

	@Test(dataProvider = "quantitative", groups = "")
	public static void testBasic(TestDescription test) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		java.util.logging.Logger.getGlobal().setLevel(Level.FINEST);
		java.util.logging.Logger.getGlobal().setUseParentHandlers(false);
		Handler newHandler = new ConsoleHandler();
		newHandler.setLevel(Level.FINEST);
		java.util.logging.Logger.getGlobal().addHandler(newHandler);

		CoreseTimer timerMemory = null;
		CoreseTimer timerDb = null;
		try {
			setCacheForDb(test);
			System.gc();
			timerDb = new CoreseTimer().setMode(CoreseTimer.Profile.DB).init().run(test);
			System.gc();
			timerMemory = new CoreseTimer().setMode(CoreseTimer.Profile.MEMORY).init().run(test);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("running test: " + test.getId());
		boolean result;
		try {
			result = compareResults(timerDb.getMapping(), timerMemory.getMapping());
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		}
		test.setResultsEqual(result);
		writeResult(test, timerDb, timerMemory);
		assertTrue(result, test.getId());
	}

	// Useful only for titandb.
	private static void setCacheForDb(TestDescription test) {
		String confFileName = String.format("%s/conf.properties", test.getInputDb());
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(new File(confFileName));
			config.setProperty("storage.batch-loading", "false");
			config.setProperty("cache.db-cache", "true");
			config.setProperty("cache.db-cache-size", "250000000");
			config.setProperty("cache.db-cache-time", "0");
			config.setProperty("query.batch", "true");
			config.setProperty("query.fast-property", "true");
			config.setProperty("storage.transactions", "false");
			config.setProperty("storage.read-only", "false");
			config.save();
		} catch (ConfigurationException ex) {
			java.util.logging.Logger.getLogger(QualitativeTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
