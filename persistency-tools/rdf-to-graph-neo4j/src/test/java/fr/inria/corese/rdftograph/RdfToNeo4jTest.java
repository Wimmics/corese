/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph;

import fr.inria.corese.rdftograph.driver.Neo4jDriver;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
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
    public void testConvertGraphNeo4j() throws FileNotFoundException, IOException, Exception {

        String dbPath = ROOT_RESOURCES + "testConvertResult.neo4jdb";
        String expectedDb = ROOT_RESOURCES + "testConvertExpected.neo4jdb";

        RdfToGraph.build().
                setDriver(RdfToGraph.DbDriver.NEO4J).
                convertFileToDb(INPUTS[0], RDFFormat.NQUADS, dbPath);
        checkDbEqual(expectedDb, dbPath);
    }

    private void checkDbEqual(String expectedDbPath, String resultDbPath) throws Exception {
        try (Graph actual = (new Neo4jDriver()).openDatabase(resultDbPath); Graph expected = (new Neo4jDriver()).openDatabase(expectedDbPath)) {
            assertTrue("Actual and expected graph equals", areEqual(expected, actual));
        }
    }

    private boolean areEqual(Graph expected, Graph actual) {
        assertEquals("#E", expected.traversal().E().count().next(), actual.traversal().E().count().next());
        assertEquals("#V", expected.traversal().V().count().next(), actual.traversal().V().count().next());
        actual.traversal().E().sideEffect(e -> {
            assertTrue(e.toString(), expected.traversal().E(e).hasNext());
        });
        return true;
    }
}
