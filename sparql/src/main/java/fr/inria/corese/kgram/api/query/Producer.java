package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import java.util.List;

import fr.inria.corese.kgram.api.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;

/**
 * Interface for the Connector that produces candidate edges for KGRAM
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Producer {

    public static final int DEFAULT = 0;
    public static final int SKIP_DUPLICATE_TEST = 1;
    // Producer delivers from Path of from Provenance Graph
    public static final int EXTENSION = 2;

    /**
     * KGRAM calls this method before executing a query. It enables to
     * initialize the Producer
     *
     */
    default void init(Query q) {
    }

    ;
	default void start(Query q) {
    }

    ;
	default void finish(Query q) {
    }

    ;
	
	/**
	 * A hook to tune Producer
	 */
    void setMode(int n);

    public int getMode();

    /**
     * Return all graph nodes that are known by the Producer. Should return
     * graph nodes that are member of from (if any).
     *
     * @param gNode The graph query node
     * @param from The from named node list (may be empty)
     * @param env The binding environment
     * @return graph nodes
     */
    Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env);

    /**
     * Check whether a node is a graph node and if yes, check it is member of
     * from (if any)
     *
     * @param gNode The graph query node
     * @param from The from named node list (may be empty)
     * @param env The binding environment
     * @return true if the node is a graph node
     */
    boolean isGraphNode(Node gNode, List<Node> from, Environment env);

    /**
     * Return candidate edges that match a query edge. Matcher checks conformity
     * afterwards. If there is a gNode If it is bound in environment, should
     * return edges from the same graph as gNode value. Otherwise if there is a
     * from, should return edges whose graph is in from. If there is no gNode,
     * if there is a from, should return edges whose graph is in from. Should
     * return edges whose nodes conform to current bindings in environment,
     * nevertheless bindings are checked afterwards in KGRAM.
     *
     * @param gNode The query graph node if any
     * @param from The from named if gNode is not null, otherwise the from (from
     * may be empty)
     * @param qEdge The query edge
     * @param env The current mapping : query node -> target node
     * @return Candidate edge iterator
     */
    Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env);

    default Iterable<Edge> getEdges(Node s, Node p, Node o, List<Node> from) {
        return new ArrayList<>(0);
    }
    
    default Edge insert(Node g, Node s, Node p, Node o) {
        return null;
    }
    
    default Iterable<Edge> delete(Node g, Node s, Node p, Node o) {
        return null;
    }
    
    default boolean hasDataManager() { return false; }

    //return IDatatype list of IDatatype edge
    // ldscript iterator
    default IDatatype getEdges(Iterable<Edge> it) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : it) {
            if (edge != null) {
                list.add(DatatypeMap.createObject(edge));
            }
        }
        return DatatypeMap.newList(list);
    }

    Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) throws SparqlException;

    /**
     * ************** PATH *************
     */
    /**
     * Start the processing of a path instruction with qEdge as pseudo query
     * edge. index is the index of the node in qEdge where to start path. It is
     * the index of the node that we want to enumerate in getNodes()
     *
     * @param qEdge
     * @param index
     */
    void initPath(Edge qEdge, int index);

    /**
     * Return nodes for Zero length path
     *
     * @param gNode The graph node where to get nodes (may be null)
     * @param from from or from named, may be empty
     * @param edge Pseudo query edge for path
     * @param env The binding environment
     * @param exp either property name or a ! (pname | pname) or null
     * @param index of the node to return
     * @return Iterable of start nodes for exp
     *
     * SPARQL 1.1 requires: ZeroLengthPath match all nodes of (current) graph,
     * including Literals If the argument is a constant, it matches even if it
     * is not a node of the graph
     *
     */
    Iterable<Node> getNodes(Node gNode, List<Node> from, Edge edge, Environment env,
            List<Regex> exp, int index);

    /**
     * Return candidate edges for a path step for an elementary regex exp. Edge
     * start at start Node, start is the node at index. Hence start can be
     * edge.getNode(0) as well as edge.getNode(1)
     *
     * isInverse = true means consider also target edge nodes in reverse order
     * query: start = Bob ; ?a ^foaf:knows ?b target: Jack foaf:knows Bob return
     * edge above because we consider also Bob foaf:knows Jack This is
     * orthogonal to index = 1, in which case we also consider nodes in inverse
     * order
     *
     * @param gNode The start node of edges
     * @param from from or from named, may be empty
     * @param qEdge Pseudo query edge for path
     * @param env The binding environment
     * @param exp either property name or a ! (pname | pname)
     * @param src current source if any
     * @param start The start node for current edge
     * @param index of the start node in edge exp.isInverse() authorize to
     * consider nodes in reverse order (as if the symmetric relation would
     * exist)
     * @return Iterable of start nodes for exp
     */
    Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env,
            Regex exp, Node src, Node start, int index);

    /**
     * ************** value to node *************
     */
    /**
     * Given a value from the filter language, return a Node that contains this
     * value use case: select fun(?x) as ?y
     *
     * @param value a target value from the filter language
     */
    Node getNode(Object value);

    // cast java value into IDatatype value
    IDatatype getValue(Object value);

    // DatatypeValue from IDatatype or from Java value
    IDatatype getDatatypeValue(Object value);

    /**
     * use case: filter (?x = ?y) filter(?x = 'cst') is it possible to bind ?x
     * to the argument to optimize query processing ? node is the argument
     */
    boolean isBindable(Node node);

    /**
     * Given a value from the filter language, return a list of Node that
     * represent this value use case: select (xpath('/book/title') ?as list)
     *
     * @param value
     * @return List<Node>
     */
    List<Node> toNodeList(IDatatype value);

    DatatypeValueFactory getDatatypeValueFactory();

    /**
     * Given an object resulting from the evaluation of an extension function,
     * return Mappings that represents the values use case: select (sql('select
     * fom where') as (?x, ?y)) in case of sql, the object is a
     * java.sql.ResultSet
     *
     * @param qNodes the query nodes to bind with values of object
     * @param object
     * @return Mappings
     */
    Mappings map(List<Node> qNodes, IDatatype value);

    Mappings map(List<Node> qNodes, IDatatype value, int n);

    /**
     * graph node { } Node node represents (contains) a graph
     */
    boolean isProducer(Node node);

    Producer getProducer(Node node, Environment env);

    // use case: Producer created for a specific query
    Query getQuery();

    // May return an object that implement the RDF graph
    Graph getGraph();

    void setGraphNode(Node n);

    Node getGraphNode();

    Edge copy(Edge ent);

    void close();
    
    // generate fresh new blank node ID
    String blankNode();
}
