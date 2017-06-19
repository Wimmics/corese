/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.BNODE;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.IRI;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.KIND;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.LANG;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.LARGE_LITERAL;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.LITERAL;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.RDF_VERTEX_LABEL;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.TYPE;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.VERTEX_LARGE_VALUE;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.VERTEX_VALUE;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * Interface for a Graph Database driver.
 *
 * @author edemairy
 */
public abstract class GdbDriver {

	private static Logger LOGGER = Logger.getLogger(GdbDriver.class.getName());
	protected Graph g;
	// Fields related with cache management.
	private Cache<Value, Vertex> cache;
	private long maximumByteSize = 1_000_000_000;
	private int concurrencyLevel = 1;
	private int cacheTimeMS = 0;

	/**
	 * Returns an object of GdbDriver type.
	 *
	 * @param driverName Class name of the driver.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static GdbDriver createDriver(String driverName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class driverClass = Class.forName(driverName);
		GdbDriver result = (GdbDriver) driverClass.newInstance();
		return result;
	}

	/**
	 * Default constructor. Set up a cache for Value -> Vertex.
	 */
	public GdbDriver() {
		CacheBuilder<Value, Vertex> cachebuilder = CacheBuilder.newBuilder()
			.maximumWeight(maximumByteSize)
			.concurrencyLevel(concurrencyLevel)
			.initialCapacity(1000)
			.expireAfterWrite(cacheTimeMS, TimeUnit.MILLISECONDS)
			.weigher(new Weigher<Value, Vertex>() {
				@Override
				public int weigh(Value value, Vertex vertex) {
					return 1;
				}
			});;

		cache = cachebuilder.build();
	}

	/**
	 * Open an existing database.
	 *
	 * @param dbPath
	 * @return
	 * @throws IOException
	 */
	public abstract Graph openDatabase(String dbPath);

	public Graph createDatabase(String dbPath) throws IOException {
		if (Files.exists(Paths.get(dbPath))) {
			wipeDirectory(dbPath);
		}
		return null;
	}

	public abstract void closeDb() throws Exception;

	public abstract void commit();

	public Graph getTinkerpopGraph() {
		return g;
	}

	/**
	 * Remove a directory (rm -fr path).
	 *
	 * @param path
	 * @throws IOException
	 */
	public static void wipeDirectory(String path) throws IOException {
		Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.peek(p -> LOGGER.log(Level.INFO, "removing: {0}", p))
			.forEach(File::delete);
	}

	/**
	 * Finds a tinkerpop vertex in the graph corresponding to the Jena
	 * value.
	 *
	 * @param v Jena value searched in the DB.
	 * @return the first tinkerpop node if any, null otherwise.
	 * @todo Does not handle duplicate nodes.
	 */
	public Vertex getNode(Value v) {
		GraphTraversal<Vertex, Vertex> it = null;
		Vertex result = cache.getIfPresent(v);
		if (result != null) {
			return result;
		}
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(KIND, RdfToGraph.getKind(v)).has(TYPE, l.getDatatype().toString()).has(VERTEX_LARGE_VALUE, l.getLabel());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, l.getLabel()).has(KIND, RdfToGraph.getKind(v)).has(TYPE, l.getDatatype().toString());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
		}

		if (it.hasNext()) {
			result = it.next();
			cache.put(v, result);
		} else {
			result = null;
		}

		return result;
	}

	public Vertex createOrGetNode(Value v) {
		Vertex newVertex = null;
		Vertex result = getNode(v);
		if (result != null) {
			return result;
		}
		try {
			switch (RdfToGraph.getKind(v)) {
				case IRI:
				case BNODE: {
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, v.stringValue());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					break;
				}
				case LITERAL: {
					Literal l = (Literal) v;
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, l.getLabel().toString());
					newVertex.property(TYPE, l.getDatatype().toString());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						newVertex.property(LANG, l.getLanguage().get());
					}
					break;
				}
				case LARGE_LITERAL: {
					Literal l = (Literal) v;
					newVertex = g.addVertex(RDF_VERTEX_LABEL);
					newVertex.property(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode()));
					newVertex.property(VERTEX_LARGE_VALUE, l.getLabel());
					newVertex.property(TYPE, l.getDatatype().toString());
					newVertex.property(KIND, RdfToGraph.getKind(v));
					if (l.getLanguage().isPresent()) {
						newVertex.property(LANG, l.getLanguage().get());
					}
					break;
				}
			}
			return newVertex;
		} catch (Exception ex) {
			LOGGER.log(Level.INFO, "ignoring a new occurence of vertex {0} for reason:", v);
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Add an edge in the DB from the Jena artefacts.
	 *
	 * @param sourceId Source (ie start) of the edge.
	 * @param objectId Object (ie end) of the edge.
	 * @param predicate Predicate (ie name) of the edge.
	 * @param properties Properties to add to the edge.
	 * @return
	 */
	public abstract Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties);

	/**
	 *
	 * @param key
	 * @param s Sub
	 * @param p
	 * @param o
	 * @param g
	 * @return
	 */
	public abstract Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> 
        getFilter(String key, String s, String p, String o, String g);

        public  Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> 
        getFilter(Exp exp, String key, String s, String p, String o, String g){
            return getFilter(key, s, p, o, g);
        }
        
        public  Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> 
        getFilter(Exp exp, DatatypeValue s, DatatypeValue p, DatatypeValue o, DatatypeValue g){
            return null;
        }


}
