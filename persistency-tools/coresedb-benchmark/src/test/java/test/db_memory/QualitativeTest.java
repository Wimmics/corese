/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import static fr.inria.corese.coresetimer.utils.VariousUtils.ensureEndWith;
import static fr.inria.corese.coresetimer.utils.VariousUtils.getEnvWithDefault;
import fr.inria.wimmics.coresetimer.CoreseTimer;
import static fr.inria.wimmics.coresetimer.Main.TestDescription.DB_INITIALIZATION.DB_UNINITIALIZED;
import fr.inria.wimmics.coresetimer.Main.TestDescription;
import static fr.inria.wimmics.coresetimer.Main.TestDescription.DB_INITIALIZATION.DB_INITIALIZED;
import static fr.inria.wimmics.coresetimer.Main.TestDescription.DB_INITIALIZATION.DB_RESET;
import static fr.inria.wimmics.coresetimer.Main.compareResults;
import static fr.inria.wimmics.coresetimer.Main.writeResult;
import java.io.IOException;
import org.openrdf.rio.RDFFormat;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author edemairy
 */
public class QualitativeTest {

	public QualitativeTest() {
	}

	@DataProvider(name = "getResults")
	public Object[][] getResults() {
		TestDescription[][] tests = {
			//		TestDescription.build("minimal_1").setInput("minimal_1.nq").setInputDb("/minimal_1_db", DB_INITIALIZED).setRequest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"),
			//		TestDescription.build("minimal_2").setInput("minimal_2.nq").setInputDb("/minimal_2_db", DB_INITIALIZED).setRequest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"),
			//		TestDescription.build("test1_count").setWarmupCycles(0).setMeasuredCycles(1).setInput("test1.nq").setInputDb("/test1_db", DB_INITIALIZED).setRequest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"),
			//		TestDescription.build("test1_search_s").setInput("test1.nq").setInputDb("/test1_db", DB_INITIALIZED).setRequest("select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}"),
			//		TestDescription.build("test1_search_jointure") .setInput("test1.nq").setInputDb("/test1_db", DB_INITIALIZED).setRequest("select * where {?x ?p ?y . ?y ?q ?x}"),
			{TestDescription.build("humans_question1").setInput("human_2007_04_17.rdf").setFormat(RDFFormat.RDFXML).setInputDb("/human_db", DB_RESET).setRequest("SELECT ?x ?t WHERE { ?x rdf:type ?t }")},
//					TestDescription.build("humans_question1_fake").setInput("human_2007_04_17.rdfs").setFormat(RDFFormat.RDFXML).setInputDb("/human_db", DB_UNINITIALIZED).setRequest("SELECT ?x ?t WHERE { ?x rdf:type ?t }"),
			{TestDescription.build("humans_question2").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("SELECT ?x ?t WHERE { ?x rdf:type rdfs:Class }")},
			{TestDescription.build("humans_question3").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("SELECT ?x ?t WHERE { ?x rdfs:subClassOf ?y }")},
			{TestDescription.build("humans_question4").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#> \nSELECT * WHERE { ?x humans:hasSpouse ?y}")},
			{TestDescription.build("humans_question5_1").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?x rdf:type humans:Male}")},
			{TestDescription.build("humans_question5_2").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?y rdf:type humans:Male}")},
			{TestDescription.build("humans_question6").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT (count(*) as ?count) WHERE { ?y humans:hasFriend ?z }")},
			{TestDescription.build("humans_question7").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT ?x WHERE { { ?y humans:hasChild ?x } UNION { ?x humans:hasParent ?y }}")},
			{TestDescription.build("humans_question8").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?person ?age\n" + "WHERE\n" + "{\n" + " ?person rdf:type humans:Person\n" + " OPTIONAL { ?person humans:age ?age }\n" + "}")},
			{TestDescription.build("humans_question9").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{TestDescription.build("humans_question10").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "ASK\n" + "WHERE\n" + "{\n" + " <http://www.inria.fr/2007/04/17/humans.rdfs-instances#Mark> humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{TestDescription.build("humans_question11").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + "  ?x rdf:type humans:Lecturer .\n" + "  ?x rdf:type ?t \n" + "}")},
			{TestDescription.build("humans_question12").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Male .\n" + " ?x rdf:type humans:Person .\n" + " ?x rdf:type ?t\n" + "}")},
			{TestDescription.build("humans_question13").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{TestDescription.build("humans_question14_researchers").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{TestDescription.build("humans_question14_non_researchers").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Person\n" + " OPTIONAL\n" + " {\n" + "   ?x rdf:type ?t\n" + "   FILTER ( ?t = humans:Researcher )\n" + " }\n" + " FILTER ( ! bound( ?t ) )\n" + "}")},
			{TestDescription.build("humans_question15").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?y \n" + "WHERE\n" + "{\n" + " ?x humans:hasAncestor ?y\n" + "}")},
			{TestDescription.build("humans_question16").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"size\"@en .\n" + "  ?t rdfs:label ?le .\n" + "  ?t rdfs:comment ?ce .\n" + "  FILTER ( lang(?le) = 'en' && lang(?ce) = 'en' )\n" + "}")},
			{TestDescription.build("humans_question17").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"person\"@en .\n" + "  ?t rdfs:label ?syn .\n" + "  FILTER ( ?syn != \"person\"@en && lang(?syn) = 'en' )\n" + "}")},
			{TestDescription.build("humans_question18").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?lf \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"shoe size\"@en .\n" + "  ?t rdfs:label ?lf .\n" + "  FILTER ( lang(?lf) = 'fr' )\n" + "}")},
			{TestDescription.build("humans_question19").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "  ?type rdfs:label ?l .\n" + "  {\n" + "   {\n" + "    ?laura rdf:type ?type\n" + "   }\n" + "   UNION\n" + "   {\n" + "    {\n" + "     ?laura ?type ?with\n" + "    }\n" + "    UNION\n" + "    {\n" + "     ?from ?type ?laura\n" + "    }\n" + "   }\n" + "  }\n" + "  FILTER ( lang(?l) = 'en' )\n" + "}")},
			{TestDescription.build("humans_question20").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "DESCRIBE ?laura\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "}")},
			{TestDescription.build("humans_question21").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "CONSTRUCT \n" + "{\n" + " ?x rdf:type humans:Man\n" + "}\n" + "WHERE\n" + "{\n" + " {\n" + "  ?x rdf:type humans:Man\n" + " }\n" + "  UNION\n" + " {\n" + "  ?x rdf:type humans:Male .\n" + "  ?x rdf:type humans:Person\n" + " }\n" + "}")},
			{TestDescription.build("humans_question22").setInput("human_2007_04_17.rdf").setInputDb("/human_db", DB_INITIALIZED).setRequest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * WHERE\n" + "{\n" + " ?x rdf:type humans:Person .\n" + " ?x humans:name ?name .\n" + " FILTER ( regex(?name, '.*ar.*') )\n" + "}")}, //		TestDescription.build("1m_count").setWarmupCycles(0).setMeasuredCycles(1).setInput("btc-2010-chunk-000.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}"),
		//		TestDescription.build("1m_select_s_1").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>  ?p ?y .}")
		};
		return tests;
	}

	@Test(dataProvider = "getResults")
	public static void testBasic(TestDescription test) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

		String inputRoot = getEnvWithDefault("INPUT_ROOT", "./data/");
		inputRoot = ensureEndWith(inputRoot, "/");
		TestDescription.setInputRoot(inputRoot);
		String outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./outputs/");
		outputRoot = ensureEndWith(inputRoot, "/");
		TestDescription.setOutputRoot(outputRoot);

		test.init();
		CoreseTimer timerDb = new CoreseTimer().setMode(CoreseTimer.Profile.DB).init().run(test);
		CoreseTimer timerMemory = new CoreseTimer().setMode(CoreseTimer.Profile.MEMORY).init().run(test);
		boolean result = compareResults(timerDb.getMapping(), timerMemory.getMapping());
		test.setResultsEqual(result);
		writeResult(test, timerDb, timerMemory);
		assertTrue(result, test.getId());
	}

}
