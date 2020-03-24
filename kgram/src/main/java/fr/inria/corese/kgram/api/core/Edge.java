package fr.inria.corese.kgram.api.core;

/**
 * Interface for Producer iterator that encapsulate Edge or Node with its Graph Node
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Edge extends  Pointerable {
	
    /**
	 * Number of nodes.
	 * 
	 * @return
	 */
	int nbNode();
        
        /**
         * nodes that are vertex of the graph
         * use case: metadata node is not a graph vertex
         */
        int nbGraphNode();

	/**
	 * Node at index n.
	 * 
	 * @param n
	 * @return
	 */
	Node getNode(int i);
        
        default void setNode(int i, Node n)  {}
	
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
        
        // manage access right
        default byte getLevel() { return -1; }
        default Edge setLevel(byte b) { return this; }
	
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
        
        // edge variable or edge node
        Node getPredicate();
        
        Node getNode();
        
        Node getGraph();
        
        default void setGraph(Node n) {}
    
	Edge getEdge();	
        
        Object getProvenance();
        
        void setProvenance(Object obj);
        
        default boolean isMatchArity() { return false; }
}
