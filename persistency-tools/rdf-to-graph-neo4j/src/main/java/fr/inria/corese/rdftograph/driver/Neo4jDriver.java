/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.match;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 *
 * @author edemairy
 */
public class Neo4jDriver extends GdbDriver {

    Neo4jGraph graph;
    private static final Logger LOGGER = Logger.getLogger(Neo4jDriver.class.getName());

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

    private static enum RelTypes implements RelationshipType {
        CONTEXT
    }
    Map<String, Object> alreadySeen = new HashMap<>();

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

    /**
     * Returns a new node if v does not exist yet.
     *
     * @param v
     * @param context
     * @return
     */
    @Override
    public Vertex createOrGetNode(Value v) {
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
        GraphTraversal<Vertex, Edge> alreadyExist = graph.traversal().V(vSource.id()).outE().has(EDGE_P, predicate).as("e").inV().hasId(vObject.id()).select("e");
        try {
            result = alreadyExist.next();
        } catch (NoSuchElementException ex) {
            result = null;
        }
        if (result == null) {
            Edge e = vSource.addEdge(RDF_EDGE_LABEL, vObject, p.toArray());
            result = e.id();
        }
        return result;
    }

    @Override
    public void commit() {
        graph.tx().commit();
    }

    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>>
            getFilter(String key, String s, String p, String o, String g) {
        return getFilter(null, key, s, p, o, g);
    }

    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>>
            getFilter(Exp exp, String key, String s, String p, String o, String g) {
        Function<GraphTraversalSource, GraphTraversal<? extends org.apache.tinkerpop.gremlin.structure.Element, org.apache.tinkerpop.gremlin.structure.Edge>> filter;
        switch (key) {
            case "test":
                filter = t -> {
                    //return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, P.within(Arrays.asList(o, "Nice", "Cannes"))).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                    // return t.V().hasLabel(RDF_VERTEX_LABEL).where(__.has(VERTEX_VALUE, "Antibes").or().has(VERTEX_VALUE, "Nice")).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                    // return t.V().hasLabel(RDF_VERTEX_LABEL).union(__.has(VERTEX_VALUE, "Antibes"), __.has(VERTEX_VALUE, "Nice")).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, P.between("A", "AB")).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                };
                break;

            case "?g?sPO":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                };
                break;

            case "?g?sP?o":
                P pred = getPredicate(exp, Exp.OBJECT);
                if (pred != null) {
                    filter = t -> {
                        return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, pred).inE().has(EDGE_P, p).hasLabel(RDF_EDGE_LABEL);
                    };
                } else {
                    String kind = getKind(exp, Exp.OBJECT);
                    if (kind != null){
                        filter = t -> {
                            return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(inV().has(KIND, kind));
                        };
                    }
                    else {
                        filter = t -> {
                            return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                        };
                    }
                }
                             
                break;

            case "?g?s?pO":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o).inE();
                };
                break;
            case "?gSPO":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
                };
                break;
            case "?gSP?o":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                };
                break;
            case "?gS?pO":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).outE().where(inV().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, o));
                };
                break;
            case "?gS?p?o":
                filter = t -> {
                    return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, s).outE();
                };
                break;
            case "G?sP?o":
                filter = t -> {
                    return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(inV().hasLabel(RDF_VERTEX_LABEL));
                };
                break;
            case "?g?s?p?o":
            default:
                pred = getPredicate(exp, Exp.OBJECT);
                String kind = getKind(exp, Exp.OBJECT);
                
                if (pred != null) {
                    filter = t -> {
                        return t.V().hasLabel(RDF_VERTEX_LABEL).has(VERTEX_VALUE, pred).inE().hasLabel(RDF_EDGE_LABEL);
                    };
                } else if (kind != null){
                    filter = t -> {
                        return t.V().hasLabel(RDF_VERTEX_LABEL).has(KIND, kind).inE().hasLabel(RDF_EDGE_LABEL);
                    };
                }
                else {
                    filter = t -> {
                        return t.E().hasLabel(RDF_EDGE_LABEL);
                    };
                }            
                break;
                
        }
        return filter;
    }
    
    String getKind(Exp exp, int node){
        if (exp == null){
            return null;
        }
        List<Filter> list = exp.getFilters(node, ExprType.KIND);
        if (list == null || list.isEmpty()){
            return null;
        }
        Expr e = list.get(0).getExp();
        switch (e.oper()){
            case ExprType.ISURI:    return RdfToBdMap.IRI;
            case ExprType.ISBLANK:  return RdfToBdMap.BNODE;
            case ExprType.ISLITERAL: return RdfToBdMap.LITERAL;
        }
        return null;
    }
    
    P getPredicate(Exp exp, int node){
        if (exp == null){
            return null;
        }
        List<Object> ls = getList(exp, node);
        if (ls != null){
            return P.within(ls);
        }
        else {
           ls = getBetween(exp, node); 
           if (ls != null){
               return P.between(ls.get(0), ls.get(1));
           }
           else {
               P p = getMoreLess(exp, node);
               if (p != null){
                   return p;
               }
           }
        }
       
        return null;
    }
    
    P getMoreLess(Exp exp, int node){
        List<Filter> list = exp.getFilters(node, ExprType.BETWEEN);
        if (list == null || list.isEmpty()){
            return null;
        }
        
        Expr e = list.get(0).getExp();
        switch (e.oper()){
            case ExprType.LT: return P.lt(e.getExp(1).getDatatypeValue().objectValue());
            case ExprType.LE: return P.lte(e.getExp(1).getDatatypeValue().objectValue());
            case ExprType.GE: return P.gte(e.getExp(1).getDatatypeValue().objectValue());
            case ExprType.GT: return P.gt(e.getExp(1).getDatatypeValue().objectValue());
        }
        
        return null;        
    }

            /**
             * node is node index (subject, predicate, object)
             * Search filter like: 
             * ?o = v1 || ?o = v2
             * ?o in (v1, v2)
             * return list of string value
             */
    List<Object> getList(Exp exp, int node) {        
        return toList(exp.getList(node));
    }
    
     List<Object> getBetween(Exp exp, int node) {        
        return toList(exp.getBetween(node));
    }
    
    ArrayList<Object> toList(List<DatatypeValue> list){
        if (list == null || list.isEmpty()){
            return null;
        }     
        ArrayList<Object> ls = new ArrayList<>();
        for (DatatypeValue dt : list){
            ls.add(dt.objectValue());
        }
        return ls;
    }
          
}
