/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and openDatabase the template in the editor.
 */
package fr.inria.corese.rdftograph.driver;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.core.Exp;
import static fr.inria.wimmics.rdf_to_bd_map.RdfToBdMap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outV;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
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
    private static final String VAR_CST = "?_var_";
    
    private static final String VERTEX = RDF_VERTEX_LABEL;
    private static final String VALUE  = VERTEX_VALUE;
    
    SPARQL2Tinkerpop sp2t;
    
    public Neo4jDriver(){
        sp2t = new SPARQL2Tinkerpop();
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
            graph.cypher(String.format("CREATE INDEX ON :%s(%s)", VERTEX, VALUE));
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
                it = graph.traversal().V().hasLabel(VERTEX).has(VALUE, v.stringValue()).has(KIND, RdfToGraph.getKind(v));
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(VERTEX);
                    result.property(VALUE, v.stringValue());
                    result.property(KIND, RdfToGraph.getKind(v));
                }
                break;
            }
            case LITERAL: {
                Literal l = (Literal) v;
                it = graph.traversal().V().hasLabel(VERTEX).has(VALUE, l.getLabel()).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v));
                if (l.getLanguage().isPresent()) {
                    it = it.has(LANG, l.getLanguage().get());
                }
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(VERTEX);
                    result.property(VALUE, l.getLabel());
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
                it = graph.traversal().V().hasLabel(VERTEX).has(VALUE, Integer.toString(l.getLabel().hashCode())).has(TYPE, l.getDatatype().toString()).has(KIND, RdfToGraph.getKind(v)).has(VERTEX_LARGE_VALUE, l.getLabel());
                if (l.getLanguage().isPresent()) {
                    it = it.has(LANG, l.getLanguage().get());
                }
                if (it.hasNext()) {
                    result = it.next();
                } else {
                    result = graph.addVertex(VERTEX);
                    result.property(VALUE, Integer.toString(l.getLabel().hashCode()));
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
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, Edge>>
            getFilter(String key, String s, String p, String o, String g) {
        return getFilter(null, key, s, p, o, g);
    }
            

     @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, Edge>>
            getFilter(Exp exp, String key, String s, String p, String o, String g) {
        Function<GraphTraversalSource, GraphTraversal<? extends Element, Edge>> filter;
        switch (key) {
            
            case "?g?sPO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, o).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                };
                break;

            case "?g?sP?o":
                filter = t -> {
                                return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                            };
                break;

            case "?g?s?pO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, o).inE();
                };
                break;
            case "?gSPO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p)
                            .where(inV().hasLabel(VERTEX).has(VALUE, o));
                };
                break;
            case "?gSP?o":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p);
                };
                break;
            case "?gS?pO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE().where(inV().hasLabel(VERTEX).has(VALUE, o));
                };
                break;
            case "?gS?p?o":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE();
                };
                break;
            case "G?sP?o":
                filter = t -> {
                    return t.E().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, p).where(inV().hasLabel(VERTEX));
                };
                break;
            case "?g?s?p?o":
            default:
                filter = t -> {
                                return t.E().hasLabel(RDF_EDGE_LABEL);
                };
                break;
                
        }
        return filter;
    }
    
    int getKey(DatatypeValue s, DatatypeValue p, DatatypeValue o) {
        int key = 0;
        key += (o == null) ? 0 : 1;
        key += (p == null) ? 0 : 10;
        key += (s == null) ? 0 : 100;
        return key;
    }
    
    String getKeyString(DatatypeValue s, DatatypeValue p, DatatypeValue o) {
        StringBuilder sb = new StringBuilder();
        sb.append((s == null) ? "?s" : "S");
        sb.append((p == null) ? "?p" : "P");
        sb.append((o == null) ? "?o" : "O");
        return sb.toString();
    }
       
    P getPredicate(DatatypeValue dt){
        if (dt == null){
            return P.test(SPARQL2Tinkerpop.atrue, "");
        }
        return P.eq(dt.stringValue());
    }
       
    GraphTraversal<? extends Element, Edge> getVertexPredicate(GraphTraversal<? extends Element, Edge> p){
        return getVertexPredicate(p, null);
    }
    
     GraphTraversal<? extends Element, Edge> getVertexPredicate(GraphTraversal<? extends Element, Edge> p, DatatypeValue dt){
        if (p == null){
            if (dt == null){
                return __.has(VALUE, P.test(SPARQL2Tinkerpop.atrue, ""));
            }
            else {
                return getVertexPredicate(dt);
            }
        }
        return p;
    }
       
   GraphTraversal<? extends Element, Edge> getVertexPredicate(DatatypeValue dt){
       return sp2t.getVertexPredicate(dt);
   }
    
    GraphTraversal<? extends Element, Edge> getEdgePredicate(GraphTraversal<? extends Element, Edge> p){
       return getEdgePredicate(p, null);
    }
    
    GraphTraversal<? extends Element, Edge> getEdgePredicate(GraphTraversal<? extends Element, Edge> p, DatatypeValue dt){
        if (p != null){
            return p;
        }
        return __.has(EDGE_P, getPredicate(dt));
    }
    
     
    GraphTraversal<? extends Element, Edge> getPredicate(Exp exp, int index){
        GraphTraversal<? extends Element, Edge> p = sp2t.getPredicate(exp, index);
        fr.inria.edelweiss.kgram.api.core.Node node = exp.getEdge().getNode(index);
        DatatypeValue dt = (node.isConstant()) ? node.getDatatypeValue() : null ;
        if (p == null && dt != null){
            p = getVertexPredicate(p, dt);
        }
        return p;
    }
    
    /**
     * Implements getMappings by returning Iterator<Map<String, Vertex>>
     * Generate a Tinkerpop BGP query
     * @param exp is a BGP
     * @return 
     * TODO
     * getFilter
     * constant in first edge
     * complete getEdge
     * factorize constant and filter in getEdge
     */
    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, Map<String, Vertex>>> 
        getFilter(Exp exp) {
            
        GraphTraversal<? extends Element, Edge> ps = getPredicate(exp.get(0), Exp.SUBJECT);
        GraphTraversal<? extends Element, Edge> po = getPredicate(exp.get(0), Exp.OBJECT);       
        GraphTraversal<? extends Element, Edge> pt = (po == null) ? ps : po;

        ArrayList<GraphTraversal> edgeList = new ArrayList<>();
        VariableTable varList = new VariableTable();
        int i = 0;
        // swap = true: 
        // first edge pattern starts with object because there is a filter on object
        boolean swap = po != null;
        if (exp.isDebug()){
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
    
        class VariableTable {
            ArrayList<String> list;
            HashMap<String, String> table;
            
            VariableTable(){
                list = new ArrayList<>();
                table = new HashMap<>();
            }
            
            List<String> getList(){
                return list;
            }
            
            HashMap<String, String> getTable(){
                return table;
            }
            
            String get(int i){
                return list.get(i);
            }
            
            boolean contains(String s){
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
    
    GraphTraversal getEdge(Exp exp, VariableTable varList, int n, boolean swap) {
        fr.inria.edelweiss.kgram.api.core.Edge edge = exp.getEdge();
        fr.inria.edelweiss.kgram.api.core.Node ns = edge.getNode(0);
        fr.inria.edelweiss.kgram.api.core.Node no = edge.getNode(1);
        fr.inria.edelweiss.kgram.api.core.Node np = edge.getPredicate();
      
        String s = varName(varList, ns, n, 0);
        String o = varName(varList, no, n, 1);
               
        if (! varList.getList().isEmpty() && ! varList.contains(s) && varList.contains(o)){
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

        GraphTraversal<? extends Element, Edge> ps = getVertexPredicate(sp2t.getPredicate(exp, Exp.SUBJECT), dts);
        GraphTraversal<? extends Element, Edge> po = getVertexPredicate(sp2t.getPredicate(exp, Exp.OBJECT), dto);
        GraphTraversal<? extends Element, Edge> pp = getEdgePredicate(sp2t.getPredicate(exp, Exp.PREDICATE), dtp);

        if (swap) {
            return __.as(o).hasLabel(VERTEX).where(po).inE().where(pp).outV().hasLabel(VERTEX).as(s).where(ps);
        } else {
            return __.as(s).hasLabel(VERTEX).where(ps).outE().where(pp).inV().hasLabel(VERTEX).as(o).where(po);
        }
    }
    
    
    String varName(VariableTable table, fr.inria.edelweiss.kgram.api.core.Node node, int n, int rank){
        if (node.isVariable()){
            return node.getLabel();
        }
        
        return table.getVariable(node, n, rank);
    }
       
    /**
     * Exploir relevant filters for edge
     * exp = Exp(EDGE)
     */
    @Override
    public Function<GraphTraversalSource, GraphTraversal<? extends Element, Edge>>
            getFilter(Exp exp, DatatypeValue dts, DatatypeValue dtp, DatatypeValue dto, DatatypeValue dtg) {
        Function<GraphTraversalSource, GraphTraversal<? extends Element, Edge>> filter;

        String s = (dts==null)?"?s":dts.stringValue();
        String p = (dtp==null)?"?p":dtp.stringValue();
        String o = (dto==null)?"?o":dto.stringValue();
        String g = (dtg==null)?"?g":dtg.stringValue();
        
        //System.out.println(getKeyString(dts, dtp, dto));
        
        switch (getKeyString(dts, dtp, dto)) {
           
            case "?sPO":
            case "?s?pO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, o).inE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp));
                };
                break;
               
                
            case "?sP?o":
            case "?s?p?o":
            default:    
                GraphTraversal<? extends Element, Edge> ps = sp2t.getPredicate(exp, Exp.SUBJECT);
                GraphTraversal<? extends Element, Edge> po = sp2t.getPredicate(exp, Exp.OBJECT);
                GraphTraversal<? extends Element, Edge> pp = sp2t.getPredicate(exp, Exp.PREDICATE);
                    
                if (po != null) {
                    filter = t -> {
                        return t.V().hasLabel(VERTEX).where(po)
                                .inE().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
                                .where(outV().hasLabel(VERTEX).where(getVertexPredicate(ps)));
                    };
                } else if (ps != null) {
                    filter = t -> {
                        return t.V().hasLabel(VERTEX).where(ps)
                                .outE().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
                    };
                } 
                else if (exp.getEdge().getNode(0).equals(exp.getEdge().getNode(1))) {
                    // ?x ?p ?x
                   filter = t -> {
                        return t.V().hasLabel(VERTEX).as("s")
                                .inE().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp))
                                .where(outV().hasLabel(VERTEX).as("o")
                                .where(P.eq("s")));
                    };
                
                }
               else {                    
                    filter = t -> {
                            return t.E().hasLabel(RDF_EDGE_LABEL).where(getEdgePredicate(pp, dtp));
                    }; 
                }
                
                break;
                                         
                
            case "SPO":
            case "S?pO":
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp))
                            .where(inV().hasLabel(VERTEX).hasLabel(VERTEX).has(VALUE, o));
                };
                break;
                
                
            case "SP?o":
            case "S?p?o":       
                filter = t -> {
                    return t.V().hasLabel(VERTEX).has(VALUE, s).outE().hasLabel(RDF_EDGE_LABEL).has(EDGE_P, getPredicate(dtp));
                };
                break;
      }
        return filter;
    }
    
}
