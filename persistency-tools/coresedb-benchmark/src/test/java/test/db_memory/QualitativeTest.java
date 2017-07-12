/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.wimmics.coresetimer.CoreseTimer;
import fr.inria.wimmics.coresetimer.Main.TestSuite;
import fr.inria.wimmics.coresetimer.TestDescription;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;
import org.testng.ITest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.inria.corese.coresetimer.utils.VariousUtils.ensureEndWith;
import static fr.inria.corese.coresetimer.utils.VariousUtils.getEnvWithDefault;
import static fr.inria.wimmics.coresetimer.Main.compareResults;
import static org.testng.Assert.assertTrue;

/**
 * @author edemairy
 */
public class QualitativeTest implements ITest {

    private static final Logger logger = Logger.getLogger(QualitativeTest.class);
    private String inputRoot;
    private String outputRoot;
    private String mTestCaseName;

    public QualitativeTest() {
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
            timerDb = CoreseTimer.build(test).setMode(CoreseTimer.Profile.DB).init().run();
            System.gc();
            timerMemory = CoreseTimer.build(test).setMode(CoreseTimer.Profile.MEMORY).init().run();
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
        timerDb.writeResults();
        timerDb.writeStatistics();
        timerMemory.writeResults();
        timerMemory.writeStatistics();
//		writeResult(test, timerDb, timerMemory);
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

    @BeforeClass
    public void setup() {
        inputRoot = getEnvWithDefault("INPUT_ROOT", getEnvWithDefault("HOME", "/tmp/") + "/data/");
        inputRoot = ensureEndWith(inputRoot, "/");
        outputRoot = getEnvWithDefault("OUTPUT_ROOT", getEnvWithDefault("HOME", "/tmp/") + "/outputs/");
        outputRoot = ensureEndWith(inputRoot, "/");
    }

    @BeforeMethod(alwaysRun = true)
    public void testData(Method method, Object[] testData) {
        String testCase = "";
        if (testData != null && testData.length > 0) {
            TestDescription testParams = null;
            //Check if test method has actually received required parameters
            for (Object testParameter : testData) {
                if (testParameter instanceof TestDescription) {
                    testParams = (TestDescription) testParameter;
                    break;
                } else {
                    logger.error("Test data are not of TestDescription data as expected.");
                }
            }
            if (testParams != null) {
                testCase = testParams.getId();
            }
        }
        this.mTestCaseName = String.format("%s(%s)", method.getName(), testCase);
    }

    @Override
    public String getTestName() {
        return this.mTestCaseName;
    }

    /**
     * Purpose of these tests: know what parts of SPARQL are handled with
     * corese-db
     */
    @DataProvider(name = "qualitative", parallel = false)
    public Object[][] getResults() throws Exception {
        TestSuite suite = TestSuite.build("base_tests").
                setDriver(RdfToGraph.DbDriver.NEO4J).
                setWarmupCycles(0).
                setMeasuredCycles(1).setInputFilesPattern("human_2007_04_17.rdf", RDFFormat.RDFXML).
                setInputDb("/tmp/human_db").
                setInputRoot("./data").
                setOutputRoot(outputRoot);
        suite.createDb(TestSuite.DatabaseCreation.ALWAYS);
        TestDescription[][] tests = {
                {suite.buildTest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c")},
                {suite.buildTest("SELECT ?s ?t WHERE { ?s rdf:type ?t }")},
                {suite.buildTest("SELECT ?s ?t WHERE { ?s rdf:type rdfs:Class }")},
                {suite.buildTest("SELECT ?s ?t WHERE { ?s rdfs:subClassOf ?t }")},
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
                {suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * WHERE\n" + "{\n" + " ?x rdf:type humans:Person .\n" + " ?x humans:name ?name .\n" + " FILTER ( regex(?name, '.*ar.*') )\n" + "}")}, //		TestDescription.build("1m_select_s_1").setWarmupCycles(2).setMeasuredCycles(5).setInputFilesPattern("btc-2010-chunk-000.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>  ?p ?y .}")
        };
        return tests;
    }

    /**
     * The purpose of these tests is to push the limits of the amount of
     * data that can be handled by coresedb.
     */
    @DataProvider(name = "quantitative", parallel = false)
    public Iterator<Object[]> buildTests() throws Exception {
        String[] inputFiles = {
//			"btc-2010-chunk-000.nq.gz:1",
//			"btc-2010-chunk-000.nq.gz:3",
//			"btc-2010-chunk-000.nq.gz:10",
//			"btc-2010-chunk-000.nq.gz:31",
//			"btc-2010-chunk-000.nq.gz:100",
//			"btc-2010-chunk-000.nq.gz:316",
//			"btc-2010-chunk-000.nq.gz:1000",
//			"btc-2010-chunk-000.nq.gz:3162",
//			"btc-2010-chunk-000.nq.gz:10000",
//			"btc-2010-chunk-000.nq.gz:31622",
//			"btc-2010-chunk-000.nq.gz:100000",
//			"btc-2010-chunk-000.nq.gz:316227",
//			"btc-2010-chunk-000.nq.gz:1000000",
//			"btc-2010-chunk-000.nq.gz:3162277",
//			"btc-2010-chunk-000.nq.gz:10000000",
                "btc-2010-chunk-00(0|1|2|3).nq.gz",
                "btc-2010-chunk-00\\d.nq.gz",
                "btc-2010-chunk-0\\d.nq.gz",
                "btc-2010-chunk-0(0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9).nq.gz"
        };
        ArrayList<String> requests = new ArrayList<String>();
        requests.add("select ?s ?t where { ?s rdf:type ?t} limit 1");
//		requests.add("select ?s ?p ?o ?q where { ?s ?p ?o . ?o ?q ?s}");
//		requests.addAll(makeAllRequests("select ?s ?p ?o where { ?s ?p ?o }", "<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1> <http://webns.net/mvcb/generatorAgent> <http://rdfs.org/sioc/wp-sioc.php?version=1.24>"));
//		// À décommenter seulement une fois implémentés les appels G*** (sinon on tombe dans le cas par défaut : énumération exhaustive du graphe)
////		requests.addAll(makeAllRequests("select ?s ?p ?o ?g where { graph ?g { ?s ?p ?o } }", "<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1> <http://webns.net/mvcb/generatorAgent> <http://rdfs.org/sioc/wp-sioc.php?version=1.24> <http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>"));
//		requests.add("select ?s ?p ?o where { ?s ?p ?o FILTER regex(?s, \"kaufkauf\") }");
//		requests.add("select ?s ?p ?o where { ?s ?p ?o FILTER regex(?o, \"weather\", \"i\") }");

        return new Iterator<Object[]>() {
            boolean started = false;
            int cptInputFiles = 0;
            int cptRequests = 0;
            TestSuite currentSuite;

            @Override
            public boolean hasNext() {
                if (inputFiles.length == 0 || requests.isEmpty()) {
                    return false;
                }
                if (started) {
                    return !(cptInputFiles == inputFiles.length - 1 && cptRequests == requests.size() - 1);
                } else {
                    return true;
                }
            }

            @Override
            public Object[] next() {
                if (started) {
                    cptRequests++;
                    if (cptRequests >= requests.size()) {
                        cptRequests = 0;
                        cptInputFiles++;
                    }
                    if (cptInputFiles >= inputFiles.length) {
                        throw new IllegalArgumentException("no more elements");
                    }
                } else {
                    started = true;
                }

                String inputFilePattern = inputFiles[cptInputFiles];
                String request = requests.get(cptRequests);

                if (cptRequests == 0) {
                    // build the template of the test, ie
                    // an object describing how to proceed
                    // the test. Only the request is updated
                    // between each test.
                    currentSuite = TestSuite.build("test_" + inputFilePattern).
                            setDriver(RdfToGraph.DbDriver.NEO4J).
                            setWarmupCycles(2).
                            setMeasuredCycles(5).
                            setInputFilesPattern(inputFilePattern).
                            setInputDb(inputFilePattern.replace(":", "_").replace(",", "_") + "_db").
                            setInputRoot(inputRoot).
                            setOutputRoot(outputRoot);
                    try {
                        currentSuite.createDb(TestSuite.DatabaseCreation.IF_NOT_EXIST);
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

    /**
     * Build all the possible requests by setting the values. By example:
     * sparqlRequest = "select ?x ?p ?y { where ?x ?p ?y}" and data = "A B
     * C" will return a list containing the requests "select ?p ?y where {A
     * ?p ?y}", "select ?x ?y where {?x B ?y}", etc. The variables ?x ?p ?y
     * are given the values of A, B, C respectively, for all the
     * combinations except the trivial ones (ie ?x ?p ?y not set or all set
     * to A, B and C).
     *
     * @param sparqlRequest
     * @param data
     * @return
     */
    private ArrayList<String> makeAllRequests(String sparqlRequest, String data) {
        String[] dataValues = data.split(" ");
        int nbValues = dataValues.length;
        Pattern pattern = Pattern.compile("\\?\\w+");
        ArrayList<String> sparqlVariables = new ArrayList<>();
        HashMap<String, Integer> sparqlVariablesPosition = new HashMap<>();
        Matcher matcher = pattern.matcher(sparqlRequest);
        int pos = 0;
        while (pos < sparqlRequest.length() && matcher.find(pos)) {
            String newMatch = matcher.group();
            if (!sparqlVariables.contains(newMatch)) {
                sparqlVariablesPosition.put(newMatch, sparqlVariables.size());
                sparqlVariables.add(newMatch);
            }
            pos = matcher.end() + 1;
        }
        assertTrue(dataValues.length == sparqlVariables.size(), "The number of values and variables in the SPARQL request should be equal.");
        ArrayList<String> result = new ArrayList<>();
        for (int i = 1; i < (1 << nbValues) - 1; i++) {
            int current = i;
            int posInCurrent = 0;
            String currentRequest = sparqlRequest;
            while (current != 0) {
                if (current % 2 == 1) { // replace all occurences of "posInCurrent"-th variable in sparqlRequest by "posInCurrent"-th value in data, if the posInCurrent bit is set in "current" value.
                    String currentVariable = sparqlVariables.get(posInCurrent);
                    currentRequest = currentRequest.replaceFirst("\\" + currentVariable, "").replace(currentVariable, dataValues[posInCurrent]);
                }
                posInCurrent++;
                current /= 2;
            }
            result.add(currentRequest);
            logger.info("Adding: " + currentRequest);
        }
        return result;
    }

}
