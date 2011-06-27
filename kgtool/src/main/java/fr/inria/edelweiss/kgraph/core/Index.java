package fr.inria.edelweiss.kgraph.core;



import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Interface for Index for Graph 
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public interface Index {

	int getIndex();
	
	int size();

	int duplicate();

	void index();

	void indexNode();

	Iterable<Node> getProperties();

	Entity add(Entity edge);
	
	Entity delete(Entity edge);

	boolean exist(Entity edge);

	void declare(Entity edge);
	
	int size(Node pred);

	Iterable<Entity> getEdges();

	Iterable<Entity> getEdges(Node pred, Node node);

	Iterable<Entity> getEdges(Node pred, Node node, Node node2);
	
	// ************** Update
	
	void clear(Node gNode);
	
	void copy(Node g1, Node g2);

	void add(Node g1, Node g2);
	
	void move(Node g1, Node g2);



}
