/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.tinkerpop;

import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.util.Optional;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;

/**
 *
 * @author edemairy
 */
public class Factory {

//	public static String DRIVER = "org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph";
//	public static String DB_PATH = "/Users/edemairy/btc_neo4j_1m";
//	public static String[] CONFIG = {Neo4jGraph.CONFIG_DIRECTORY, DB_PATH};
		public static String DRIVER = "org.apache.tinkerpop.gremlin.orientdb.OrientGraph";
		public static String DB_PATH = "plocal:/Users/edemairy/btc_orientdb_1m";
		public static String[] CONFIG = {OrientGraph.CONFIG_URL, DB_PATH};

	private static Optional<TinkerpopGraph> graph = null;

	public static Object create(Graph g) {
		TinkerpopProducer p = new TinkerpopProducer(g);
		graph = TinkerpopGraph.create(DRIVER, CONFIG);
		p.setTinkerpopGraph(graph.get());
		return p;
	}
}
