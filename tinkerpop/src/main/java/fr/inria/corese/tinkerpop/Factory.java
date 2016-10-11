/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.tinkerpop;

import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.Optional;
//import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;

/**
 *
 * @author edemairy
 */
public class Factory {

	public static final String DB_PATH_PROPERTY = "fr.inria.corese.tinkerpop.dbinput";
	public static String DRIVER = "com.thinkaurelius.titan.core.TitanFactory";
	public static String DB_PATH;
	public static String[] CONFIG;
	private static Optional<TinkerpopGraph> graph = null;

	public static Object create(Graph g) {
		DB_PATH = System.getProperty(DB_PATH_PROPERTY);
		TinkerpopProducer p = new TinkerpopProducer(g);
		CONFIG = new String[1];
		CONFIG[0] = DB_PATH + "/conf.properties";
		graph = TinkerpopGraph.create(DRIVER, CONFIG);
		p.setTinkerpopGraph(graph.get());
		return p;
	}
}
