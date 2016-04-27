/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.tinkerpop.TinkerpopGraph;
import java.io.File;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

/**
 *
 * @author edemairy
 */
public class TinkerpopGraphTest {

	public static void main(String[] args) throws Throwable {
		TinkerpopGraph graph = TinkerpopGraph.create("/Users/edemairy/Developpement/Neo4jTinkerpop/CreateRepoSail/src/test/resources/testConvertResult.neo4jdb");
		int cpt = 0;
		System.out.println(IteratorUtils.count(graph.getEdges()));
		for (Entity e : graph.getEdges()) {
			System.out.println(e);
			cpt++;
			if (cpt > 10000) { 
				break;
			}
		}
		graph.finalize();
	}
}
