package fr.inria.corese.rdftograph.driver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.SchemaAction;
import com.thinkaurelius.titan.core.schema.SchemaStatus;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.database.management.GraphIndexStatusReport;
import com.thinkaurelius.titan.graphdb.database.management.ManagementSystem;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thinkaurelius.titan.core.attribute.Text.textRegex;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;

/**
 * @author edemairy
 */
public class TitanDriver extends GdbDriver {

    private static final Logger logger = Logger.getLogger(TitanDriver.class.getName());

    private String dbPath;
    //	String nodeId(Value v) {
//		StringBuilder result = new StringBuilder();
//		String kind = RdfToGraph.getKind(v);
//		switch (kind) {
//			case IRI:
//			case BNODE:
//				result.append("label=").append(v.stringValue()).append(";");
//				result.append("value=").append(v.stringValue()).append(";");
//				result.append("kind=").append(kind);
//				break;
//			case LARGE_LITERAL:
//				Literal l = (Literal) v;
//				result.append("label=").append(l.getLabel()).append(";");
//				result.append("value=").append(Integer.toString(l.getLabel().hashCode()));
//				result.append("large_value=").append(l.getLabel()).append(";");
//				result.append("type=").append(l.getDatatype().toString()).append(";");
//				result.append("kind=").append(kind);
//				if (l.getLanguage().isPresent()) {
//					result.append("lang=").append(l.getLanguage().get()).append(";");
//				}
//				break;
//			case LITERAL:
//				l = (Literal) v;
//				result.append("label=").append(l.getLabel()).append(";");
//				result.append("value=").append(l.getLabel()).append(";");
//				result.append("type=").append(l.getDatatype().toString()).append(";");
//				result.append("kind=").append(kind);
//				if (l.getLanguage().isPresent()) {
//					result.append("lang=").append(l.getLanguage().get()).append(";");
//				}
//				break;
//		}
//		return result.toString();
//	}
    int removedNodes = 0;

    public TitanGraph getTitanGraph() {
        return (TitanGraph) g;
    }

    @Override
    public Graph openDatabase(String dbPathTemp) {
        g = TitanFactory.open(dbPathTemp + "/conf.properties");
        return g;
    }

    @Override
    public Graph createDatabase(String dbPathTemp) throws IOException {
        File f = new File(dbPathTemp);
        dbPath = f.getAbsolutePath();
        super.createDatabase(dbPath);
        PropertiesConfiguration configuration = null;
        File confFile = new File(dbPath + "/conf.properties");
        try {
            configuration = new PropertiesConfiguration(confFile);

        } catch (ConfigurationException ex) {
            Logger.getLogger(TitanDriver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        configuration.setProperty("schema.default", "none");
        configuration.setProperty("storage.batch-loading", true);
//		configuration.setProperty("storage.batch-loading", false);
        configuration.setProperty("storage.backend", "berkeleyje");
        configuration.setProperty("storage.directory", dbPath + "/db");
        configuration.setProperty("storage.buffer-size", 50_000);
//		configuration.setProperty("storage.berkeleyje.cache-percentage", 50);
//		configuration.setProperty("storage.read-only", true);
        configuration.setProperty("index.search.backend", "elasticsearch");
        configuration.setProperty("index.search.directory", dbPath + "/es");
        configuration.setProperty("index.search.elasticsearch.client-only", false);
        configuration.setProperty("index.search.elasticsearch.local-mode", true);
        configuration.setProperty("index.search.refresh_interval", 600);
        configuration.setProperty("ids.block-size", 50_000);

        configuration.setProperty("cache.db-cache", true);
        configuration.setProperty("cache.db-cache-size", 250_000_000);
        configuration.setProperty("cache.db-cache-time", 0);
//		configuration.setProperty("cache.tx-dirty-size", 100_000);
        // to make queries faster
        configuration.setProperty("query.batch", true);
        configuration.setProperty("query.fast-property", true);
        configuration.setProperty("query.force-index", false);
        configuration.setProperty("query.ignore-unknown-index-key", true);
        try {
            configuration.save();

        } catch (ConfigurationException ex) {
            Logger.getLogger(TitanDriver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        g = TitanFactory.open(configuration);

        TitanManagement mgmt = getTitanGraph().openManagement();
        if (!mgmt.containsVertexLabel(RDF_VERTEX_LABEL)) {
            mgmt.makeVertexLabel(RDF_VERTEX_LABEL).make();
        }
        if (!mgmt.containsEdgeLabel(RDF_EDGE_LABEL)) {
            mgmt.makeEdgeLabel(RDF_EDGE_LABEL).multiplicity(Multiplicity.MULTI).make();
        }
        mgmt.commit();

        makeIfNotExistProperty(EDGE_P);
        makeIfNotExistProperty(VERTEX_VALUE);
        makeIfNotExistProperty(VERTEX_LARGE_VALUE);
        makeIfNotExistProperty(EDGE_G);
        makeIfNotExistProperty(EDGE_S);
        makeIfNotExistProperty(EDGE_O);
        makeIfNotExistProperty(KIND);
        makeIfNotExistProperty(TYPE);
        makeIfNotExistProperty(LANG);

        createIndexes();
        return g;
    }

    @Override
    public void closeDatabase() {
        getTitanGraph().close();
    }

    /**
     * Define a new entry of type String in the data model of the db.
     *
     * @param propertyName Name of the entry to add.
     */
    private void makeIfNotExistProperty(String propertyName) {
        makeIfNotExistProperty(propertyName, String.class);
    }

    /**
     * Create in the db model a new property and the class it uses.
     *
     * @param propertyName Entry name in the model.
     * @param c            Class for the entry.
     */
    private void makeIfNotExistProperty(String propertyName, Class<?> c) {
        ManagementSystem manager = (ManagementSystem) getTitanGraph().openManagement();
        if (!manager.containsPropertyKey(propertyName)) {
            manager.makePropertyKey(propertyName).dataType(c).cardinality(Cardinality.SINGLE).make();
            System.out.println("adding key " + propertyName);
            manager.commit();
        } else {
            manager.rollback();
        }
    }

    private void createIndexes() {
        ManagementSystem manager = (ManagementSystem) getTitanGraph().openManagement();
        if (!manager.containsGraphIndex("vertices") && !manager.containsGraphIndex("allIndex")) {
            PropertyKey vertexValue = manager.getPropertyKey(VERTEX_VALUE);
            PropertyKey kindValue = manager.getPropertyKey(KIND);
            PropertyKey typeValue = manager.getPropertyKey(TYPE);
            PropertyKey langValue = manager.getPropertyKey(LANG);

            PropertyKey graphKey = manager.getPropertyKey(EDGE_G);
            PropertyKey subjectKey = manager.getPropertyKey(EDGE_S);
            PropertyKey predicateKey = manager.getPropertyKey(EDGE_P);
            PropertyKey objectKey = manager.getPropertyKey(EDGE_O);
            TitanGraphIndex vIndex = manager.
                    buildIndex("vertices", Vertex.class).
                    addKey(vertexValue).
                    addKey(kindValue).
                    buildCompositeIndex();
            manager.
                    buildIndex("vertices2", Vertex.class).
                    addKey(vertexValue).
                    addKey(kindValue).
                    addKey(typeValue).
                    buildCompositeIndex();
//			manager.
//				buildIndex("vertexIndex", Vertex.class).
//				addKey(vertexValue, Mapping.STRING.asParameter()).
//				addKey(kindValue, Mapping.STRING.asParameter()).
//				addKey(typeValue, Mapping.STRING.asParameter()).
//				addKey(langValue, Mapping.STRING.asParameter()).
//				buildMixedIndex("search");
//			manager.
//				buildIndex("allIndex", Edge.class).
//				addKey(predicateKey, Mapping.STRING.asParameter()).
//				addKey(subjectKey, Mapping.STRING.asParameter()).
//				addKey(objectKey, Mapping.STRING.asParameter()).
//				addKey(graphKey, Mapping.STRING.asParameter()).
//				buildMixedIndex("search");
//g.traversal().E().has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value());
//			manager.
//				buildIndex("spoIndex", Edge.class).
//				addKey(subjectKey).
//				addKey(predicateKey).
//				addKey(objectKey).
//				buildCompositeIndex();

            manager.commit();
            String[] indexNames = {
                    "vertices",
                    "vertices2", //				"allIndex",
                    //				"spoIndex",
                    //				"vertexIndex"
            };
            for (String indexName : indexNames) {
                try {
                    GraphIndexStatusReport indexStatus = ManagementSystem.awaitGraphIndexStatus(getTitanGraph(), indexName).status(SchemaStatus.REGISTERED).call();
                    logger.log(Level.INFO, "status for {0} {1}", new Object[]{indexName, indexStatus});
                    manager = (ManagementSystem) getTitanGraph().openManagement();
                    manager.updateIndex(manager.getGraphIndex(indexName), SchemaAction.REINDEX).get();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);

                } finally {
                    manager.commit();
                }
            }
        }

    }

    @Override
    public Object createRelationship(Value source, Value object, String predicate, Map<String, Object> properties) {
        Object result = null;
        Vertex vSource = createOrGetNode(source);
        Vertex vObject = createOrGetNode(object);

        ArrayList<String> p = new ArrayList<>();
        properties.keySet().stream().forEach((key) -> {
            p.add(key);
            p.add(properties.get(key).toString());
        });
        p.add(EDGE_S);
        String s_value = vSource.property(VERTEX_VALUE).value().toString();
        p.add(s_value);
        p.add(EDGE_P);
        p.add(predicate);
        p.add(EDGE_O);
        String o_value = vObject.property(VERTEX_VALUE).value().toString();
        p.add(o_value);

        GraphTraversal<Vertex, Edge> alreadyExist = g.traversal().V(vSource.id()).outE().has(EDGE_P, predicate).as("e").inV().hasId(vObject.id()).select("e");
        try {
            result = alreadyExist.next();
        } catch (NoSuchElementException ex) {
            result = null;
        }
        if (result == null) {
//		Iterator<Edge> it = vSource.edges(Direction.OUT, RDF_EDGE_LABEL);
//		GraphTraversal<Vertex, Vertex> found = g.traversal().V(vSource.id()).outE(RDF_EDGE_LABEL).has(EDGE_S, vSource.property(VERTEX_VALUE).value()).has(EDGE_P, predicate).has(EDGE_O, vObject.property(VERTEX_VALUE).value()).as("edge").inV().hasId(vObject.id()).select("edge");
//		if (found.hasNext()) {
//			result = found.next();
//		} else {
//			Transaction transaction = g.tx();
            Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
            result = e.id();
//			transaction.commit();
        }
        return result;
    }

    @Override
    public void commit() {
        logger.fine("#transactions = " + ((StandardTitanGraph) g).getOpenTransactions().size());
        g.tx().commit();
    }

    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> getFilter(String key, String s, String p, String o, String g) {
        Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, ? extends org.apache.tinkerpop.gremlin.structure.Element>> filter;
        switch (key.toString()) {
            case "?g?sPO":
                filter = t -> {
                    return t.E().has(EDGE_P, p).has(EDGE_O, o);
                };
                break;
            case "?g?sP?o":
                filter = t -> {
                    return t.E().has(EDGE_P, p);
                };
                break;
            case "?g?s?pO":
                filter = t -> {
                    return t.E().has(EDGE_O, o);
                };
                break;
            case "?gSPO":
                filter = t -> {
                    return t.E().has(EDGE_P, p).has(EDGE_S, s).has(EDGE_O, o);
                };
                break;
            case "?gSP?o":
                filter = t -> {
                    return t.E().has(EDGE_P, p).has(EDGE_S, s);
                };
                break;
            case "?gS?pO":
                filter = t -> {
                    return t.E().has(EDGE_S, s).has(EDGE_O, o);
                };
                break;
            case "?gS?p?o":
                filter = t -> {
                    return t.E().has(EDGE_S, s);
                };
                break;
            case "G?sP?o":
                filter = t -> {
                    return t.E().has(EDGE_P, p).has(EDGE_G, g);
                };
                break;
            case "?g?s?p?o":
            default:
                filter = t -> {
                    return t.E().has(EDGE_P, textRegex(".*"));
                };
        }
        return filter;
    }

    @Override
    public Entity buildEdge(Element e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Node buildNode(Element e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isGraphNode(String label) {
        return g.traversal().E().has(EDGE_G, label).hasNext();
    }
}
