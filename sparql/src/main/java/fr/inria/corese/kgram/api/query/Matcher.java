package fr.inria.corese.kgram.api.query;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;

/**
 * Interface for the Matcher that checks conformity of candidate edges and nodes 
 * with respect to their query edge/node for KGRAM 
 * 
* @author Olivier Corby, Edelweiss, INRIA 2010
*
*/

public interface Matcher {
	
	/**
	 * Match mode
	 */
	// equality of types
	public static final int UNDEF = -1;
	
	public static final int STRICT = 0;
	// exploit type inference (subsumption)
	public static final int SUBSUME = 1;
	// type inference and accept generalization of types 
	public static final int GENERAL = 2;
	// subsume + generalize
	public static final int MIX = 3;
	// accept any types
	public static final int RELAX = 4;
	// exploit rules 
	public static final int INFERENCE = 5;


	/**
	 * Checks whether a candidate edge matches a query edge.
	 * May verify that edge labels match (Producer may have done the job properly).
	 * May verify edges such as rdf:type and check subsumption of type.
	 * Must verify that each candidate edge node match its query edge node, 
	 * e.g. constant query nodes.
	 * (KGRAM does not perform the latter test and supposes that matcher does it)
	 * Matcher is not concerned by bindings that are checked by KGRAM.
	 * 
	 * @param q The query edge
	 * @param e The candidate edge
	 * @param env The binding environment
	 * @return true if it matches
	 */
	boolean match(Edge q, Edge e, Environment env);

	/**
	 * Checks whether a candidate node matches a query node.
	 * 
	 * @param q The query node
	 * @param n	The candidate node
	 * @param env The binding environment
	 * @return true if it matches
	 */
	boolean match(Node q, Node n, Environment env);
	
	/**
	 * Two occurrences of a query node are bound to two candidate nodes.
	 * Check that the candidate nodes are the same or are equivalent.
	 * May use owl:sameAs
	 * 
	 * @param qNode
	 * @param n1
	 * @param n2
	 * @param env The binding environment
	 * @return
	 */
	boolean same(Node qNode, Node n1, Node n2, Environment env);
	
	void setMode(int mode);
	
	int getMode();
	
}
