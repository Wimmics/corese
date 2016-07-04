/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import static org.neo4j.helpers.collection.IteratorUtil.count;
import org.neo4j.tooling.GlobalGraphOperations;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author edemairy
 */
public class RdfToNeo4jTest {

	private static final String ROOT_RESOURCES = "src/test/resources/";

	/**
	 * Test of convert method, of class RdfToNeo4jBatch.
	 */
	@Test
	public void testConvertGraphNeo4j() throws FileNotFoundException {
		String inputFile = ROOT_RESOURCES + "testConvert/input1.nq";
		String outputDb = ROOT_RESOURCES + "testConvertNeo4jResult.neo4jdb";
		String expectedDb = ROOT_RESOURCES + "testConvertExpected.neo4jdb";

		RdfToGraph converter = new RdfToGraph();
		converter.setDriver("neo4j");
		converter.setWipeOnOpen(true);

		FileInputStream inputStream = new FileInputStream(new File(inputFile));
		converter.convert(inputStream, RDFFormat.NQUADS, outputDb);

		GraphDatabaseService result = new GraphDatabaseFactory().newEmbeddedDatabase(new File(outputDb));
		GraphDatabaseService expected = new GraphDatabaseFactory().newEmbeddedDatabase(new File(expectedDb));
		assert (areEquals(result, expected));
		result.shutdown();
		expected.shutdown();
	}

	/**
	 * Test of convert method, of class RdfToNeo4jBatch.
	 */
	@Test
	public void testConvertGraphOrientdb() throws FileNotFoundException {
		String inputFile = ROOT_RESOURCES + "testConvert/input1.nq";
		String outputDb = "plocal:" + ROOT_RESOURCES + "testConvertOrientdbResult.orientdb";
		String expectedDb = ROOT_RESOURCES + "testConvertExpected.neo4jdb";

		RdfToGraph converter = new RdfToGraph();
		converter.setDriver("orientdb");
		converter.setWipeOnOpen(true);

		FileInputStream inputStream = new FileInputStream(new File(inputFile));
		converter.convert(inputStream, RDFFormat.NQUADS, outputDb);

//		GraphDatabaseService result = new GraphDatabaseFactory().newEmbeddedDatabase(new File(outputDb));
//		GraphDatabaseService expected = new GraphDatabaseFactory().newEmbeddedDatabase(new File(expectedDb));
//		assert (areEquals(result, expected));
//		result.shutdown();
//		expected.shutdown();
		// @TODO generic reader to add
	}

	private boolean areEquals(GraphDatabaseService result, GraphDatabaseService expected) {
		result.beginTx();
		expected.beginTx();
		int nbResultNodes = count(GlobalGraphOperations.at(result).getAllNodes());
		int nbExpectedNodes = count(GlobalGraphOperations.at(expected).getAllNodes());
		assert (nbResultNodes == nbExpectedNodes);
		assert (count(GlobalGraphOperations.at(result).getAllRelationships()) == count(GlobalGraphOperations.at(expected).getAllRelationships()));
		for (Node n : GlobalGraphOperations.at(expected).getAllNodes()) {
			Iterable<Label> labels = n.getLabels();
			for (Label l : labels) {
				assert (result.findNodes(l) != null);
			}
		}
		return true;
	}

}
