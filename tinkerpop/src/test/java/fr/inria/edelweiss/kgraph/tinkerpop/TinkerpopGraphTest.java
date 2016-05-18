/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

/**
 *
 * @author edemairy
 */
public class TinkerpopGraphTest {

	public final static String DRIVER = "org.apache.tinkerpop.gremlin.orientdb.OrientGraph";
	public final static String DB_PATH = "plocal:/Users/edemairy/Developpement/Neo4jTinkerpop/RdfToGraph/src/test/resources/testConvertOrientdbResult.orientdb";
	public final static String[] CONFIG = {OrientGraph.CONFIG_URL, DB_PATH};


//	public final static String DRIVER = "org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph";
//	public final static String DB_PATH = "/Users/edemairy/Developpement/Neo4jTinkerpop/RdfToGraph/src/test/resources/testConvertNeo4jResult.neo4jdb";
//	public final static String[] CONFIG = {Neo4jGraph.CONFIG_DIRECTORY, DB_PATH};
	
	public final static boolean DISPLAY_EDGES = true;
	public final static int MAX_DISPLAY_EDGES = 10000;

	public static void main(String[] args) throws Throwable {
		Configuration config = new BaseConfiguration();
		for (int i = 0; i < CONFIG.length; i += 2) {
			String key = CONFIG[i];
			String value = CONFIG[i + 1];
			config.setProperty(key, value);
		}

		TinkerpopGraph graph = TinkerpopGraph.create(DRIVER, config);
		int cpt = 0;
		System.out.println("Number of edges:" + IteratorUtils.count(graph.getEdges()));
		if (DISPLAY_EDGES) {
			for (Entity e : graph.getEdges()) {
				System.out.println(e);
				cpt++;
				if (cpt > MAX_DISPLAY_EDGES) {
					break;
				}
			}
		}
		graph.finalize();
	}
}
