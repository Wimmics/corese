package fr.inria.edelweiss.kgraph.core;



import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.List;

/**
 * Interface for Index for Graph 
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public interface Index {
    
        String toRDF();

	int getIndex();
	
	int size();
        
        int cardinality();

	int duplicate();

	void index();
        
        void compact();
                
        void index(Node pred);

	void indexNode();

        void indexNodeManager();       
        
        void setByIndex(boolean b);

	Iterable<Node> getProperties();
	
	Iterable<Node> getSortedProperties();
        
        int nbProperties();
	        
        boolean same(Node n1, Node n2);

	Entity add(Entity edge);
	
	Entity add(Entity edge, boolean duplicate);
        
        void add(Node p, List<Entity> list);

        Entity delete(Entity edge);
        
        Entity delete(Node pred, Entity edge);

	void delete(Node pred);

	boolean exist(Entity edge);
        
        boolean exist(Node p, Node n1, Node n2);

	void declare(Entity edge);
        
         void declare(Entity edge, boolean duplicate);        
	
	int size(Node pred);

	Iterable<Entity> getEdges();
        
        Iterable<Entity> get(Node pred);

	Iterable<Entity> getEdges(Node pred, Node node);

	Iterable<Entity> getEdges(Node pred, Node node, Node node2);
        
        NodeManager getNodeManager();
	
	// ************** Update
	
	
	void clear();
        
        void clean();
        
        void clearCache();

	void clearIndex(Node pred);

        void clear(Node gNode);
	
	void copy(Node g1, Node g2);

	void add(Node g1, Node g2);
	
	void move(Node g1, Node g2);

	void setDuplicateEntailment(boolean value);


}
