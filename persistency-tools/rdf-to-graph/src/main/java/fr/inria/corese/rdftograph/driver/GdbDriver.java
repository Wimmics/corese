/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDb the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
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
	private boolean wipeOnOpen;
	protected Graph g;

	public abstract void openDb(String dbPath);

	public void setWipeOnOpen(boolean newValue) {
		wipeOnOpen = newValue;
	}

	public boolean getWipeOnOpen() {
		return wipeOnOpen;
	}

	public static void delete(String path) throws IOException {
		Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS)
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.peek(p -> LOGGER.log(Level.INFO, "removing: {0}", p))
			.forEach(File::delete);
	}

	public abstract void closeDb();

	HashMap<Value, Vertex> cache = new HashMap<>();

	public Vertex getNode(Value v) {
		GraphTraversal<Vertex, Vertex> it = null;
		if (cache.containsKey(v)) {
			return cache.get(v);
		}
		Vertex result = null;
		switch (RdfToGraph.getKind(v)) {
			case IRI:
			case BNODE: {
				it = g.traversal().V().has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
				break;
			}
			case LARGE_LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
			case LITERAL: {
				Literal l = (Literal) v;
				it = g.traversal().V().has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
				if (l.getLanguage().isPresent()) {
					it = it.has(LANG, l.getLanguage().get());
				}
				break;
			}
		}

		if (cache.size() > 10_000) {
			LOGGER.info("Cleaning cache");
			cache.clear();
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
	
	public abstract Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties);

	public abstract void commit();
}
