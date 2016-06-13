/*
 * Copyright Inria 2016
 */
package fr.inria.corese.persistency;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge to make a Neo4j database accessible from Corese.
 *
 * @author edemairy
 */
public class DbGraph extends fr.inria.edelweiss.kgraph.core.Graph {

	public static final String PATH = "path";
	public static final String USER = "user";
	public static final String PASSWORD = "password";

	private Connection connection;
	private SqlToCorese unmapper;

	private final static Logger LOGGER = Logger.getLogger(DbGraph.class.getSimpleName());

	public DbGraph(Connection connection) {
		super();
		this.connection = connection;
		this.unmapper = new SqlToCorese(this, connection);
	}

	@Override
	public void finalize() throws Throwable {
		connection.close();
		super.finalize();
	}

	/**
	 *
	 * @param dbPath
	 * @return
	 */
	public static Optional<DbGraph> create(String driverName, Properties config) {
		try {
			Connection connection = DriverManager.getConnection((String) config.get(PATH), config);
			DbGraph result = new DbGraph(connection);
			return Optional.of(result);
		} catch (SQLException ex) {
			Logger.getLogger(DbGraph.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Helper that use a String array to generate the configuration used to
	 * initialize the tinkerpop driver.
	 *
	 * @param driverName Name of the class of the driver to instanciate.
	 * @param configArray Array following the pattern { key_1, value_1,
	 * key_2, value_2, etc. }
	 * @return
	 */
	public static Optional<DbGraph> create(String driverName, String[] configArray) {
		Properties config = new Properties();
		for (int i = 0; i < configArray.length; i += 2) {
			String key = configArray[i];
			String value = configArray[i + 1];
			config.setProperty(key, value);
		}
		return create(driverName, config);
	}

	public Iterable<Entity> getEdges(ArrayList< Function<String, Boolean>> filters) {
		return new Iterable<Entity>() {
			@Override
			public Iterator<Entity> iterator() {
				return new Iterator<Entity>() {
					private boolean initialized = false;
					private boolean nextValueRead = false;
					private int chunkNumber = 0;
					private int chunkSize = 1000;
					private long pos = 0;

					private Statement statement;
					private ResultSet rs;
					private boolean statusNext;

					private void init() {
						try {
							statement = connection.createStatement();
							rs = statement.executeQuery("SELECT context, value, in, out FROM E_rdf_edge OFFSET "+chunkNumber * chunkSize + " LIMIT "+chunkSize);
						} catch (SQLException ex) {
							Logger.getLogger(DbGraph.class.getName()).log(Level.SEVERE, null, ex);
						}
					}

					private void close() {
						try {
							rs.close();
							statement.close();
						} catch (SQLException ex) {
							Logger.getLogger(DbGraph.class.getName()).log(Level.SEVERE, null, ex);
						}
					}

					private void readNextValue() {
						if (!initialized) {
							init();
							initialized = true;
						}
						if (!nextValueRead) {
							try {
								statusNext = rs.next();
								if (!statusNext) {
									close();
								}
								nextValueRead = true;
							} catch (SQLException ex) {
								Logger.getLogger(DbGraph.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}

					@Override
					public boolean hasNext() {
						readNextValue();
						return statusNext;
					}

					@Override
					public Entity next() {
						readNextValue();
						nextValueRead = false;
						pos++;
						if (pos >= chunkNumber*chunkSize) {
							try {
								chunkNumber++;
								rs = statement.executeQuery("SELECT context, value, in, out FROM E_rdf_edge OFFSET "+chunkNumber * chunkSize + " LIMIT "+chunkSize);
							} catch (SQLException ex) {
								Logger.getLogger(DbGraph.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
						return unmapper.buildEntity(rs);
					}
				};
			};
		};
	}

	@Override
	public Iterable<Entity> getEdges() {
		return getEdges(new ArrayList<>());
	}

//	/**
//	 * @param edgeName
//	 * @return
//	 */
//	@Override
//	public Iterable<Entity> getEdges(String edgeName) {
//		GraphTraversalSource traversal = tGraph.traversal();
//		Iterable<Entity> result = traversal.E().has("value", edgeName).map(e -> unmapper.buildEntity(e.get())).toList();
//		return result;
//	}
	@Override
	public void clean() {
		super.clean();
	}
}
