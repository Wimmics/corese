/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.openrdf.rio.RDFFormat;
import static org.neo4j.helpers.collection.IteratorUtil.count;

/**
 *
 * @author edemairy
 */
public class RdfToNeo4jTest {

	private static final String ROOT_RESOURCES = "src/test/resources/";
	private static final String[] INPUTS = {
		ROOT_RESOURCES + "testConvert/input1.nq"
	};
	/**
	 * Test of convertStreamToDb method, of class RdfToNeo4jBatch.
	 *
	 * @throws java.io.FileNotFoundException
	 */
	@Test
	public void testConvertGraphNeo4j() throws FileNotFoundException, IOException {
		String dbPath = ROOT_RESOURCES + "testConvertResult.neo4jdb";
		String expectedDb = ROOT_RESOURCES + "testConvertExpected.neo4jdb";

		RdfToGraph converter = new RdfToGraph();
		converter.setDriver(RdfToGraph.DbDriver.NEO4J).setWipeOnOpen(true);
		converter.convertFileToDb(INPUTS[0], RDFFormat.NQUADS, dbPath);

//		checkDbEqual(expectedDb, dbPath);
	}

	/**
	 * Test of convertStreamToDb method, of class RdfToNeo4jBatch.
	 *
	 * @throws java.io.FileNotFoundException
	 */
	@Test
	public void testConvertGraphOrientdb() throws FileNotFoundException {
		String dbPath = "plocal:" + ROOT_RESOURCES + "testConvertResult.orientdb";
		String expectedDb = ROOT_RESOURCES + "testConvertExpected.neo4jdb";

		RdfToGraph converter = new RdfToGraph().setDriver(RdfToGraph.DbDriver.ORIENTDB).setWipeOnOpen(true);

		converter.convertFileToDb(INPUTS[0], RDFFormat.NQUADS, dbPath);

//		checkDbEqual(expectedDb, dbPath);
	}

	@Test
	public void testConvertGraphTitandb() {
		String dbPath = ROOT_RESOURCES + "testConvertResult.titandb";
		String expectedDb = ROOT_RESOURCES + "testConvertExpected.titandb";

		RdfToGraph converted = new RdfToGraph().setDriver(RdfToGraph.DbDriver.TITANDB).setWipeOnOpen(true);
		checkDbEqual(expectedDb, dbPath);
	}
	
	private void checkDbEqual(String expectedDbPath, String resultDbPath) {
		GraphDatabaseService actual = new GraphDatabaseFactory().newEmbeddedDatabase(new File(resultDbPath));
		GraphDatabaseService expected = new GraphDatabaseFactory().newEmbeddedDatabase(new File(expectedDbPath));
		assert (areEqual(expected, actual));
		actual.shutdown();
		expected.shutdown();
	}

	private boolean areEqual(GraphDatabaseService actual, GraphDatabaseService expected) {
		actual.beginTx();
		expected.beginTx();
		int nbResultNodes = count(GlobalGraphOperations.at(actual).getAllNodes());
		int nbExpectedNodes = count(GlobalGraphOperations.at(expected).getAllNodes());
		assert (nbResultNodes == nbExpectedNodes);
		assert (count(GlobalGraphOperations.at(actual).getAllRelationships()) == count(GlobalGraphOperations.at(expected).getAllRelationships()));
		for (Node n : GlobalGraphOperations.at(expected).getAllNodes()) {
			Iterable<Label> labels = n.getLabels();
			for (Label l : labels) {
				assert (actual.findNodes(l) != null);
			}
		}
		return true;
	}
}
