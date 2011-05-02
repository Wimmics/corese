package fr.inria.edelweiss.kgram.api.core;


/**
 * Interface of Edge provided by graph implementation
 * and also by KGRAM query edges
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Edge  {
	
	/**
	 * Number of nodes.
	 * 
	 * @return
	 */
	int nbNode();

	/**
	 * Node at index n.
	 * 
	 * @param n
	 * @return
	 */
	Node getNode(int n);
	
	/**
	 * Additional Node that represents the label of the edge as a Node.
	 * This node is matched with query edge node if any
	 * use case: ?x ?p ?y
	 * The target edge node is bound to ?p 
	 * This node is not supposed to be returned by getNode() neither is it supposed to be
	 * counted in nbNode().
	 * 
	 * @return
	 */
	Node getEdgeNode();
	
	/**
	 * Is node returned by getNode()
	 * 
	 * @param n
	 * @return
	 */
	boolean contains(Node node);

	/**
	 * The label of the edge.
	 * 
	 * @return
	 */
	String getLabel();
	
	/**
	 * Query edge must have an index.
	 * Target edge are not committed to.
	 * @return
	 */
	int getIndex();
	
	/**
	 * Query edge must have an index (computed by KGRAM).
	 * Target edge are not committed to.
	 * @return
	 */
	void setIndex(int n);
	
	/**
	 * Query edge may have variable Node
	 * Target edge are not committed to.
	 * @return
	 */
	Node getEdgeVariable();

		
	
}
