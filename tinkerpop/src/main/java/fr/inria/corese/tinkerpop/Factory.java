/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.tinkerpop;

import fr.inria.corese.rdftograph.driver.GdbDriver;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author edemairy
 */
public class Factory {

	public static final String DB_PATH_PROPERTY = "fr.inria.corese.tinkerpop.dbinput";
	public static final String DB_DRIVER_PROPERTY = "fr.inria.corese.tinkerpop.driver";
//	public static String DEFAULT_DRIVER = "fr.inria.corese.rdftograph.driver.Neo4jDriver";
	public static String DEFAULT_DRIVER = "fr.inria.wimmics.rdf.to.graph.cypher.CypherDriver";
//	public static String DRIVER = "org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph";
//	public static String DRIVER = "com.thinkaurelius.titan.core.TitanFactory";
	private static final Logger LOGGER = LogManager.getLogger(Factory.class.getName());

	public static Producer create(Graph g) {
		try {
			String databasePath = System.getProperty(DB_PATH_PROPERTY);
			String driverClass = System.getProperty(DB_DRIVER_PROPERTY);
			if (driverClass == null) {
				driverClass = DEFAULT_DRIVER;
			}
			GdbDriver driver = GdbDriver.createDriver(driverClass);
			TinkerpopGraph graph = TinkerpopGraph.create(driver, databasePath).get();
			TinkerpopProducer p = new TinkerpopProducer(graph, driver);
			return p;
		} catch (Exception ex) {
			LOGGER.error("Impossible to create the Producer.");
			ex.printStackTrace();
			return null;
		}
	}
}
