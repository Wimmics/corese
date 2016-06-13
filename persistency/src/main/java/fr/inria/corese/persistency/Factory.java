/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.persistency;

import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.Optional;

/**
 *
 * @author edemairy
 */
public class Factory {

	public final static String DRIVER = "com.orientechnologies.orient.jdbc.OrientJdbcDriver";
	public final static String DB_PATH = "jdbc:orient:plocal:/Users/edemairy/btc_orientdb_1m";
	public static String[] CONFIG = {DbGraph.PATH, DB_PATH, DbGraph.USER, "admin", DbGraph.PASSWORD, "admin"};

	private static Optional<DbGraph> graph = null;

	public static Object create(Graph g) {
		DbProducer p = new DbProducer(g);
		graph = DbGraph.create(DRIVER, CONFIG);
		p.setDbGraph(graph.get());
		return p;
	}
}
