/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgraph.core.edge.EdgeQuad;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

/**
 * @author edemairy
 */
public class Neo4jDriver extends GdbDriver {

    private static final Logger LOGGER = Logger.getLogger(Neo4jDriver.class.getName());
    private static final String VAR_CST = "?_var_";
    private static final String VERTEX = RDF_VERTEX_LABEL;
    private static final String VALUE = VERTEX_VALUE;
    Neo4jGraph graph;
    SPARQL2Tinkerpop sp2t;
    Map<String, Object> alreadySeen = new HashMap<>();
    private LoadingCache<Value, Vertex> cache;

    public Neo4jDriver() {
        super();
        sp2t = new SPARQL2Tinkerpop();
        this.cache = CacheBuilder.newBuilder().
                maximumSize(1000000).
                expireAfterAccess(100, TimeUnit.DAYS).
                build(new CacheLoader<Value, Vertex>() {
                    @Override
                    public Vertex load(Value v) throws Exception {
                        Vertex result = createOrGetNodeIntern(v);
                        return result;
                    }

                });
    }

    @Override
    public Graph openDatabase(String databasePath) {
        LOGGER.entering(getClass().getName(), "openDatabase");
        graph = Neo4jGraph.open(databasePath);
        return graph;
    }

    @Override
    public Graph createDatabase(String databasePath) throws IOException {
        LOGGER.entering(getClass().getName(), "createDatabase");
        super.createDatabase(databasePath);
        try {
            graph = Neo4jGraph.open(databasePath);
            graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_EDGE_LABEL, EDGE_P));
            graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_EDGE_LABEL, EDGE_G));
            graph.cypher(String.format("CREATE INDEX ON :%s(%s)", RDF_VERTEX_LABEL, VERTEX_VALUE));
            graph.tx().commit();
            return graph;
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void closeDb() throws Exception {
        LOGGER.entering(getClass().getName(), "closeDb");
        try {
            while (graph.tx().isOpen()) {
                graph.tx().commit();
            }
        } finally {
            graph.close();
        }
    }

    protected boolean nodeEquals(Node endNode, Value object) {
        boolean result = true;
        result &= endNode.getProperty(KIND).equals(RdfToGraph.getKind(object));
        if (result) {
            switch (RdfToGraph.getKind(object)) {
                case BNODE:
                case IRI:
                    result &= endNode.getProperty(EDGE_P).equals(object.stringValue());
                    break;
                case LITERAL:
                    Literal l = (Literal) object;
                    result &= endNode.getProperty(EDGE_P).equals(l.getLabel());
                    result &= endNode.getProperty(TYPE).equals(l.getDatatype().stringValue());
                    if (l.getLanguage().isPresent()) {
                        result &= endNode.hasProperty(LANG) && endNode.getProperty(LANG).equals(l.getLanguage().get());
                    } else {
                        result &= !endNode.hasProperty(LANG);
                    }
            }
        }
        return result;
    }

    /**
     * Returns a unique id to store as the key for alreadySeen, to prevent
     * creation of duplicates.
     *
     * @param v
     * @return
     */
    String nodeId(Value v) {
        StringBuilder result = new StringBuilder();
        String kind = RdfToGraph.getKind(v);
        switch (kind) {
            case IRI:
            case BNODE:
                result.append("label=" + v.stringValue() + ";");
                result.append("value=" + v.stringValue() + ";");
                result.append("kind=" + kind);
                break;
            case LITERAL:
                Literal l = (Literal) v;
                result.append("label=" + l.getLabel() + ";");
                result.append("value=" + l.getLabel() + ";");
                result.append("type=" + l.getDatatype().toString() + ";");
                result.append("kind=" + kind);
                if (l.getLanguage().isPresent()) {
                    result.append("lang=" + l.getLanguage().get() + ";");
                }
                break;
        }
        return result.toString();
    }

    @Override
    public boolean isGraphNode(String label) {
        return graph.traversal().V().hasLabel(RDF_EDGE_LABEL).has(EDGE_G, label).hasNext();
    }

    public Object createRelationship(Value sourceId, Value objectId, String predicate, Map<String, Object> properties) {
        Object result;
        Vertex vSource = createOrGetNode(sourceId);
        Vertex vObject = createOrGetNode(objectId);
        ArrayList<Object> p = new ArrayList<>();
        properties.keySet().stream().forEach((key) -> {
            p.add(key);
            p.add(properties.get(key));
        });
        p.add(EDGE_P);
        p.add(predicate);
        p.add(T.label);
        p.add(RDF_EDGE_LABEL);
        Vertex e = graph.addVertex(p.toArray());
        e.addEdge(SUBJECT_EDGE, vSource);
        e.addEdge(OBJECT_EDGE, vObject);
        result = e.id();
        return result;
    }

    @Override
    public void commit() {
        graph.tx().commit();
    }

    @Override
    public Vertex createOrGetNode(Value v) {
        return cache.getUnchecked(v);
    }

    /**
     * Returns a new node if v does not exist yet.
     *
     * @param v
     * @return
     */
    public Vertex createOrGetNodeIntern(Value v) {
        GraphTraversal<Vertex, Vertex> it;
        Vertex result = null;
        switch (RdfToGraph.getKind(v)) {
            case IRI:
            case BNODE: {
                it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(RDF_VERTEX_LABEL);
                    result.property(VERTEX_VALUE, v.stringValue());
                    result.property(KIND, RdfToGraph.getKind(v));
                }
                break;
            }
            case LITERAL: {
                Literal l = (Literal) v;
                it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
                if (l.getLanguage().isPresent()) {
                    it = it.has(LANG, l.getLanguage().get());
                }
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(RDF_VERTEX_LABEL);
                    result.property(VERTEX_VALUE, l.getLabel());
                    result.property(TYPE, l.getDatatype().toString());
                    result.property(KIND, RdfToGraph.getKind(v));
                    if (l.getLanguage().isPresent()) {
                        result.property(LANG, l.getLanguage().get());
                    }
                }

                break;
            }
            case LARGE_LITERAL: {
                Literal l = (Literal) v;
                it = graph.traversal().V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
                if (l.getLanguage().isPresent()) {
                    it = it.has(LANG, l.getLanguage().get());
                }
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(RDF_VERTEX_LABEL);
                    result.property(VERTEX_VALUE, Integer.toString(l.getLabel().hashCode()));
                    result.property(VERTEX_LARGE_VALUE, l.getLabel());
                    result.property(TYPE, l.getDatatype().toString());
                    result.property(KIND, RdfToGraph.getKind(v));
                    if (l.getLanguage().isPresent()) {
                        result.property(LANG, l.getLanguage().get());
                    }
                }
                break;
            }
        }
        return result;
    }

    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(String key, String s, String p, String o, String g) {
        return getFilter(null, key, s, p, o, g);
    }

    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(Exp exp, String key, String s, String p, String o, String g) {
        Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> filter;
        switch (key) {
            case "?g?sPO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                break;
            case "?g?sP?o":
                filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                break;
            case "?g?s?pO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL);
                break;
            case "?gSPO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
                break;
            case "?gSP?o":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                break;
            case "GSP?o":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
                break;
            case "?gS?pO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
                break;
            case "?gS?p?o":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV();
                break;
            case "G?sP?o":
                filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).has(EDGE_G, g);
                break;
            case "?g?s?p?o":
            default:
                filter = t -> t.V().hasLabel(RDF_EDGE_LABEL);
        }
        return filter;
    }

    String getKeyString(DatatypeValue s, DatatypeValue p, DatatypeValue o) {
        StringBuilder sb = new StringBuilder();
        sb.append((s == null) ? "?s" : "S");
        sb.append((p == null) ? "?p" : "P");
        sb.append((o == null) ? "?o" : "O");
        return sb.toString();
    }

    P getPredicate(DatatypeValue dt) {
        if (dt == null) {
            return P.test(SPARQL2Tinkerpop.atrue, "");
        }
        return P.eq(dt.stringValue());
    }

    GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(GraphTraversal<? extends Element, ? extends Element> p) {
        return getVertexPredicate(p, null);
    }

    GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(GraphTraversal<? extends Element, ? extends Element> p, DatatypeValue dt) {
        if (p == null) {
            if (dt == null) {
                return __.has(VALUE, P.test(SPARQL2Tinkerpop.atrue, ""));
            } else {
                return getVertexPredicate(dt);
            }
        }
        return p;
    }

    GraphTraversal<? extends Element, ? extends Element> getVertexPredicate(DatatypeValue dt) {
        return sp2t.getVertexPredicate(dt);
    }

    GraphTraversal<? extends Element, ? extends Element> getEdgePredicate(GraphTraversal<? extends Element, ? extends Element> p) {
        return getEdgePredicate(p, null);
    }

    GraphTraversal<? extends Element, ? extends Element> getEdgePredicate(GraphTraversal<? extends Element, ? extends Element> p, DatatypeValue dt) {
        if (p != null) {
            return p;
        }
        return __.has(EDGE_P, getPredicate(dt));
    }

    GraphTraversal<? extends Element, ? extends Element> getPredicate(Exp exp, int index) {
        GraphTraversal<? extends Element, ? extends Element> p = sp2t.getPredicate(exp, index);
        fr.inria.edelweiss.kgram.api.core.Node node = exp.getEdge().getNode(index);
        DatatypeValue dt = (node.isConstant()) ? node.getDatatypeValue() : null;
        if (p == null && dt != null) {
            p = getVertexPredicate(p, dt);
        }
        return p;
    }

    /**
     * Implements getMappings by returning Iterator<Map<String, Vertex>>
     * Generate a Tinkerpop BGP query
     *
     * @param exp is a BGP
     * @return TODO getFilter constant in first edge complete getEdge
     * factorize constant and filter in getEdge
     */
    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, Map<String, Vertex>>>
    getFilter(Exp exp) {

        GraphTraversal<? extends Element, ? extends Element> ps = getPredicate(exp.get(0), Exp.SUBJECT);
        GraphTraversal<? extends Element, ? extends Element> po = getPredicate(exp.get(0), Exp.OBJECT);
        GraphTraversal<? extends Element, ? extends Element> pt = (po == null) ? ps : po;

        ArrayList<GraphTraversal> edgeList = new ArrayList<>();
        VariableTable varList = new VariableTable();
        int i = 0;
        // swap = true:
        // first edge pattern starts with object because there is a filter on object
        boolean swap = po != null;
        if (exp.isDebug()) {
            System.out.println("Neo fst predicate: " + pt + " swap: " + swap);
        }
        for (Exp e : exp.getExpList()) {
            if (e.isEdge()) {
                edgeList.add(getEdge(e, varList, i++, swap));
                swap = false;
            }
        }

        GraphTraversal[] query = new GraphTraversal[edgeList.size()];
        edgeList.toArray(query);
        String[] select = new String[varList.getList().size()];
        varList.getList().toArray(select);

        switch (varList.getList().size()) {
            case 1:
                return t -> {
                    return t.V().match(query).select(varList.get(0));
                };

            default:
                if (pt == null) {
                    return t -> {
                        return t.V().hasLabel(VERTEX).match(query).select(varList.get(0), varList.get(1), select);
                    };
                } else {
                    return t -> {
                        return t.V().hasLabel(VERTEX).where(pt).match(query).select(varList.get(0), varList.get(1), select);
                    };
                }
        }
    }

    GraphTraversal getEdge(Exp exp, VariableTable varList, int n, boolean swap) {
        fr.inria.edelweiss.kgram.api.core.Edge edge = exp.getEdge();
        fr.inria.edelweiss.kgram.api.core.Node ns = edge.getNode(0);
        fr.inria.edelweiss.kgram.api.core.Node no = edge.getNode(1);
        fr.inria.edelweiss.kgram.api.core.Node np = edge.getPredicate();

        String s = varName(varList, ns, n, 0);
        String o = varName(varList, no, n, 1);

        if (!varList.getList().isEmpty() && !varList.contains(s) && varList.contains(o)) {
            // select start node: subject or object according to which is already bound
            // in previous triples
            swap = true;
        }

        if (!varList.contains(s)) {
            varList.getList().add(s);
        }
        if (!varList.contains(o)) {
            varList.getList().add(o);
        }

        DatatypeValue dts = (ns.isVariable()) ? null : ns.getDatatypeValue();
        DatatypeValue dto = (no.isVariable()) ? null : no.getDatatypeValue();
        DatatypeValue dtp = (np.isVariable()) ? null : np.getDatatypeValue();

        GraphTraversal<? extends Element, ? extends Element> ps = getVertexPredicate(sp2t.getPredicate(exp, Exp.SUBJECT), dts);
        GraphTraversal<? extends Element, ? extends Element> po = getVertexPredicate(sp2t.getPredicate(exp, Exp.OBJECT), dto);
        GraphTraversal<? extends Element, ? extends Element> pp = getEdgePredicate(sp2t.getPredicate(exp, Exp.PREDICATE), dtp);

        if (swap) {
            return __.as(o).hasLabel(VERTEX).where(po).inE().where(pp).outV().hasLabel(VERTEX).as(s).where(ps);
        } else {
            return __.as(s).hasLabel(VERTEX).where(ps).outE().where(pp).inV().hasLabel(VERTEX).as(o).where(po);
        }
    }

    String varName(VariableTable table, fr.inria.edelweiss.kgram.api.core.Node node, int n, int rank) {
        if (node.isVariable()) {
            return node.getLabel();
        }

        return table.getVariable(node, n, rank);
    }

    /**
     * Exploir relevant filters for edge exp = Exp(EDGE)
     */
    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> getFilter(Exp exp, DatatypeValue dts, DatatypeValue dtp, DatatypeValue dto, DatatypeValue dtg) {
        Function<GraphTraversalSource, GraphTraversal<? extends Element, ? extends Element>> filter;

        String s = (dts == null) ? "?s" : dts.stringValue();
        String p = (dtp == null) ? "?p" : dtp.stringValue();
        String o = (dto == null) ? "?o" : dto.stringValue();
        String g = (dtg == null) ? "?g" : dtg.stringValue();

        //System.out.println(getKeyString(dts, dtp, dto));
        switch (getKeyString(dts, dtp, dto)) {

            case "?sPO":
            case "?s?pO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL);
                break;

            case "?sP?o":
            case "?s?p?o":
            default:
                GraphTraversal<? extends Element, ? extends Element> ps = sp2t.getPredicate(exp, Exp.SUBJECT);
                GraphTraversal<? extends Element, ? extends Element> po = sp2t.getPredicate(exp, Exp.OBJECT);
                GraphTraversal<? extends Element, ? extends Element> pp = sp2t.getPredicate(exp, Exp.PREDICATE);

                if (po != null) {
                    filter = t -> {
                        return t.V().hasLabel(RDF_VERTEX_LABEL).where(po)
                                .inE(OBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
                                .where(outE(SUBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).where(getVertexPredicate(ps)));
                    };
                } else if (ps != null) {
                    filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).where(ps)
                            .inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
                } else if (exp.getEdge().getNode(0).equals(exp.getEdge().getNode(1))) {
                    // ?x ?p ?x
                    filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).as("s")
                            .inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
                            .where(outE(OBJECT_EDGE).inV().hasLabel(VERTEX_VALUE).as("o")
                                    .where(P.eq("s")));
                } else {
                    filter = t -> t.V().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
                }

                break;

            case "SPO":
            case "S?pO":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp))
                        .where(outE(OBJECT_EDGE).inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
                break;

            case "SP?o":
            case "S?p?o":
                filter = t -> t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).inE(SUBJECT_EDGE).outV().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp));
                break;
        }
        return filter;
    }

    @Override
    public Entity buildEdge(Element e) {
        Vertex nodeEdge = (Vertex) e;
        Entity result = EdgeQuad.create(
                DatatypeMap.createResource(nodeEdge.value(EDGE_G)),
                buildNode(nodeEdge.edges(Direction.OUT, SUBJECT_EDGE).next().inVertex()),
                DatatypeMap.createResource(nodeEdge.value(EDGE_P)),
                buildNode(nodeEdge.edges(Direction.OUT, OBJECT_EDGE).next().inVertex())
        );
        return result;
    }

    @Override
    public fr.inria.edelweiss.kgram.api.core.Node buildNode(Element e) {
        Vertex node = (Vertex) e;
        String id = (String) node.value(VERTEX_VALUE);
        switch ((String) node.value(KIND)) {
            case IRI:
                return DatatypeMap.createResource(id);
            case BNODE:
                return DatatypeMap.createBlank(id);
            case LITERAL:
                String label = (String) node.value(VERTEX_VALUE);
                String type = (String) node.value(TYPE);
                VertexProperty<String> lang = node.property(LANG);
                if (lang.isPresent()) {
                    return DatatypeMap.createLiteral(label, type, lang.value());
                } else {
                    return DatatypeMap.createLiteral(label, type);
                }
            case LARGE_LITERAL:
                label = (String) node.value(VERTEX_LARGE_VALUE);
                type = (String) node.value(TYPE);
                lang = node.property(LANG);
                if (lang.isPresent()) {
                    return DatatypeMap.createLiteral(label, type, lang.value());
                } else {
                    return DatatypeMap.createLiteral(label, type);
                }
            default:
                throw new IllegalArgumentException("node " + node.toString() + " type is unknown.");
        }
    }

    private static enum RelTypes implements RelationshipType {
        CONTEXT
    }

    class VariableTable {

        ArrayList<String> list;
        HashMap<String, String> table;

        VariableTable() {
            list = new ArrayList<>();
            table = new HashMap<>();
        }

        List<String> getList() {
            return list;
        }

        HashMap<String, String> getTable() {
            return table;
        }

        String get(int i) {
            return list.get(i);
        }

        boolean contains(String s) {
            return list.contains(s);
        }

        // same variable for same literal
        String getVariable(fr.inria.edelweiss.kgram.api.core.Node node, int n, int rank) {
            String value = node.getDatatypeValue().toString();
            String var = table.get(value);
            if (var == null) {
                var = VAR_CST.concat(Integer.toString(2 * n + rank));
                table.put(value, var);
            }
            return var;
        }
    }

}
