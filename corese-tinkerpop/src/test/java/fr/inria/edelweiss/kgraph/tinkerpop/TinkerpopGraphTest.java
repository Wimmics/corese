/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

/**
 *
 * @author edemairy
 */
public class TinkerpopGraphTest {

//	public final static String DRIVER = "org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph";
//	public final static String DB_PATH = "/Users/edemairy/testDb3";

	public final static String DRIVER = "org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph";
	public final static String DB_PATH = "/Users/edemairy/btc_bd";

	public final static boolean DISPLAY_EDGES = true;
	public final static int MAX_DISPLAY_EDGES = 10000;

	public static void main(String[] args) throws Throwable {
		TinkerpopGraph graph = TinkerpopGraph.create("org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph", DB_PATH);
		int cpt = 0;
		if (DISPLAY_EDGES) {
			for (Entity e : graph.getEdges()) {
				System.out.println(e);
				cpt++;
				if (cpt > MAX_DISPLAY_EDGES) {
					break;
				}
			}
		}
//		System.out.println(IteratorUtils.count(graph.getEdges()));
		graph.finalize();
	}
}
