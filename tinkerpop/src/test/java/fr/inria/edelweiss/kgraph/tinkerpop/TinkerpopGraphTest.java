/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgraph.tinkerpop;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.tinkerpop.TinkerpopGraph;
import java.io.File;

/**
 *
 * @author edemairy
 */
public class TinkerpopGraphTest {

	public static void main(String[] args) throws Throwable {
		TinkerpopGraph graph = TinkerpopGraph.create("src/test/resources/tinkerpop/neo4j_233/btc_1000.graphdb");
		for (Entity e : graph.getEdges()) {
			System.out.println(e);
		}
		graph.finalize();
	}
}
