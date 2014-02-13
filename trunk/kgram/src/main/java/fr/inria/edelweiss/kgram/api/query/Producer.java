package fr.inria.edelweiss.kgram.api.query;

import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.core.Mappings;

/**
 * Interface for the Connector that produces candidate edges for KGRAM 
 * 
* @author Olivier Corby, Edelweiss, INRIA 2010
*
*/
public interface Producer {
	
	/**
	 * KGRAM calls this method before executing a query.
	 * It enables to initialize the Producer.
	 * 
	 * @param nbNodes Number of query nodes 
	 * @param nbEdges Number of query edges
	 * 
	 */
	void init(int nbNodes, int nbEdges);
	
	/**
	 * A hook to tune Producer
	 */
	void setMode(int n);
	
	
	/**
	 * Return all graph nodes that are known by the Producer.
	 * Should return graph nodes that are member of from (if any).
	 * 
	 * @param gNode The graph query node 
	 * @param from  The from named node list (may be empty)
	 * @param env   The binding environment
	 * @return graph nodes
	 */
	Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env);
	
	/**
	 * Check whether a node is a graph node and if yes, check it is member of from (if any)
	 * @param gNode The graph query node 
	 * @param from  The from named node list (may be empty)
	 * @param env   The binding environment
	 * @return true if the node is a graph node
	 */
	boolean isGraphNode(Node gNode, List<Node> from, Environment env);
	
	/**
	 * Return candidate edges that match a query edge. Matcher checks conformity afterwards.
	 * If there is a gNode
	 *     If it is bound in environment, should return edges from the same graph as gNode value.
	 *     Otherwise if there is a from, should return edges whose graph is in from.
	 * If there is no gNode, if there is a from, should return edges whose graph is in from. 
	 * Should return edges whose nodes conform to current bindings in environment, nevertheless bindings are checked afterwards in KGRAM.
	 * 
	 * @param gNode The query graph node if any
	 * @param from  The from named if gNode is not null, otherwise the from (from may be empty)
	 * @param qEdge The query edge
	 * @param env   The current mapping : query node -> target node
	 * @return Candidate edge iterator
	 */
	Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge,  Environment env);
	
	
	/**
	 * Same as candidate() for edges
	 * @param gNode
	 * @param from
	 * @param qNode
	 * @param env
	 * @return
	 */
	Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode,  Environment env);

	
	
	/**************** PATH **************/
	
	
	/**
	 * Start the processing of a path instruction with qEdge as pseudo query edge.
	 * index is the index of the node in qEdge where to start path.
	 * It is the index of the node that we want to enumerate in getNodes()
	 * 
	 * @param qEdge
	 * @param index
	 */
	void initPath(Edge qEdge, int index);
	
	
	/**
	 * Return  nodes for Zero length path  
	 * 
	 * @param gNode The graph node where to get nodes (may be null)
	 * @param from from or from named, may be empty
	 * @param qEdge Pseudo query edge for path
	 * @param env The binding environment
	 * @param exp either property name or a ! (pname | pname) or null
	 * @param index of the node to return
	 * @return Iterable of start nodes for exp
	 * 
	 * SPARQL 1.1 requires:
	 * ZeroLengthPath match all nodes of (current) graph, including Literals
	 * If the argument is a constant, it matches even if it is not a node of the graph
	 * 
	 */
	
	Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, 
			List<Regex> exp, int index);
	
	
	/**
	 * Return candidate edges for a path step for an elementary regex exp.
	 * Edge start at start Node, start is the node at index.
	 * Hence start can be edge.getNode(0) as well as edge.getNode(1)
	 *  
	 * isInverse = true means consider also target edge nodes in reverse order
	 * query: start = Bob ; ?a ^foaf:knows ?b
	 * target: Jack foaf:knows Bob
	 * return edge above because we consider also Bob foaf:knows Jack
	 * This is orthogonal to index = 1, in which case we also consider nodes in inverse order
	 * 
	 * @param gNode The start node of edges
	 * @param from from or from named, may be empty
	 * @param qEdge Pseudo query edge for path
	 * @param env The binding environment
	 * @param exp either property name or a ! (pname | pname)
	 * @param src current source if any
	 * @param start The start node for current edge
	 * @param index of the start node in edge
	 * exp.isInverse() authorize to consider nodes in reverse order (as if the symmetric relation would exist)
	 * @return Iterable of start nodes for exp
	 */
	Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, 
			Regex exp, Node src, Node start, int index);
	
	
	
	/**************** value to node **************/
	
	
	/**
	 * Given a value from the filter language, return a Node that contains this value
	 * use case: select fun(?x) as ?y
	 * 
	 * @param value a target value from the filter language
	 */
	Node getNode(Object value);
	
	/**
	 * use case: filter (?x = ?y)  filter(?x = 'cst')
	 * is it possible to bind ?x to the argument to optimize query processing ?
	 * node is the argument 
	 */
	boolean isBindable(Node node);
	
	
	/**
	 * Given a value from the filter language, return a list of Node that represent this value
	 * use case:  select (xpath('/book/title') ?as list) 
	 * 
	 * @param value
	 * @return List<Node>
	 */
	List<Node> toNodeList(Object value);
	
	
	/**
	 * Given an object resulting from the evaluation of an extension function, return  Mappings
	 * that represents the values
	 * use case: select (sql('select fom where') as (?x, ?y))
	 * in case of sql, the object is a java.sql.ResultSet
	 * 
	 * @param qNodes the query nodes to bind with values of object
	 * @param object
	 * @return Mappings
	 */
	Mappings map(List<Node> qNodes, Object object);

        /**
         * graph node { }
         * Node node represents (contains) a graph 
         */
         boolean isProducer(Node node);

         Producer getProducer(Node node);
	
         // May return an object that implement the RDF graph
         Object getGraph();
}
