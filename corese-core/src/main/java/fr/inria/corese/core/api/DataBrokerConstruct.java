package fr.inria.corese.core.api;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.update.Basic;

/**
 * Broker between GraphManager and graph DataManager for sparql query construct
 * and sparql update Refined by core.producer.DataBrokerConstructLocal for
 * corese graph Refined by core.producer.DataBrokerConstructExtern for external
 * DataManager
 */
public interface DataBrokerConstruct extends DataBroker {

    default void startRuleEngine() {
        System.out.println("DataBrokerConstruct startRuleEngine");
    }

    default void endRuleEngine() {
    }

    default void startRule() {
    }

    default void endRule() {
    }

    default Node getNode(Node gNode, IDatatype dt) {
        return dt;
    }

    default void add(Node node) {
    }

    default void add(Node node, int n) {
        add(node);
    }

    default void addPropertyNode(Node node) {
    }

    default void addGraphNode(Node node) {
    }

    default boolean exist(Node property, Node subject, Node object) {
        return getDataManager().exist(subject, property, object);
    }

    /**
     * Edge may be an rdf star triple, asserted or nested
     * RDF star triple design
     * <<s p o>> q v   -> <<edge(s p o t)>> t q v
     * s p o {| q v |} -> edge(s p o t)     t q v
     * g1 s p o t g2 s p o t
     * t is additional Node, similar to subject/object
     * every occurrence of triple s p o (whatever graph g) has same reference node t
     * edge.hasReferenceNode() == true
     * edge.getReferenceNode() = t
     * t.getEdge() = s p o t
     * t.isTriple() == true
     * edge.isNested() == true |false
     * edge.isAsserted() == false|true
     * IDatatype dt = t.getDatatypeValue()
     * dt.isTriple() == true|false
     * dt.getEdge() = s p o t
     * 
     * operation find, insert, delete may have as argument a rdf star edge
     * where subject/object may be a reference node and/or edge may have reference
     * node
     * DataManager must process these subject/object/reference using the api above
     * for example: find/insert/delete t q v where t = <<s p o>>
     * Note that it can be recursive: t = <<<<s p o>> r u>>
     * .
     */

    default Edge find(Edge edge) {
        return edge;
    }

    default String blankNode() {
        return DatatypeMap.blankID();
    }

    /**
     * Return null if edge already exists
     */
    default Edge insert(Edge edge) {
        return getDataManager().insert(edge);
    }

    // corese optimization, not for extern
    default void insert(Node predicate, List<Edge> list) {
        for (Edge edge : list) {
            insert(edge);
        }
    }

    /**********************
     * Update
     */

    default List<Edge> delete(Edge edge) {
        List<Edge> result = new ArrayList<>();
        Iterable<Edge> it = getDataManager().delete(edge);
        if (it != null) {
            it.forEach(result::add);
        }
        return result;
    }

    /**
     * Delete occurrences of edge in named graphs of from list keep other
     * occurrences edge has no named graph Return the list of deleted edges
     */
    default List<Edge> delete(Edge edge, List<Constant> from) {
        return new ArrayList<>(0);
    }

    default boolean load(Query q, Basic ope, Access.Level level, AccessRight access) throws EngineException {
        return true;
    }

    default void clear(String name, boolean silent) {
        getDataManager().clear(List.of(DatatypeMap.createResource(name)), silent);
    }

    default void deleteGraph(String name) {
        getDataManager().unDeclareContext(DatatypeMap.createResource(name));
    }

    default void clearNamed() {
        getDataManager().clear();
    }

    default void dropGraphNames() {
        getDataManager().unDeclareAllContexts();
    }

    default void clearDefault() {
        getDataManager().clear();
    }

    default boolean add(String source, String target, boolean silent) {
        return getDataManager().addGraph(
                DatatypeMap.createResource(source),
                DatatypeMap.createResource(target),
                silent);
    }

    default boolean move(String source, String target, boolean silent) {
        return getDataManager().moveGraph(
                DatatypeMap.createResource(source),
                DatatypeMap.createResource(target),
                silent);
    }

    default boolean copy(String source, String target, boolean silent) {
        return getDataManager().copyGraph(
                DatatypeMap.createResource(source),
                DatatypeMap.createResource(target),
                silent);
    }

    default void addGraph(String uri) {
        getDataManager().declareContext(DatatypeMap.createResource(uri));
    }
}
