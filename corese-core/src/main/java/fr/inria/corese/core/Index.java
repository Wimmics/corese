package fr.inria.corese.core;



import fr.inria.corese.core.index.NodeManager;
import fr.inria.corese.core.index.PredicateList;
import fr.inria.corese.kgram.api.core.Node;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;

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
        
        void declareUpdate(boolean b);

	Iterable<Node> getProperties();
	
	Iterable<Node> getSortedProperties();
	PredicateList  getSortedPredicates();
        
        int nbProperties();
	        
        boolean same(Node n1, Node n2);

	Edge add(Edge edge);
	
	Edge add(Edge edge, boolean duplicate);
        
        void add(Node p, List<Edge> list);

        Edge delete(Edge edge);
        
        Edge delete(Node pred, Edge edge);

	void delete(Node pred);
        
        Edge find(Edge edge);

	boolean exist(Edge edge);
        
        boolean exist(Node p, Node n1, Node n2);

	void declare(Edge edge);
        
        void declare(Edge edge, boolean duplicate);        
	
	int size(Node pred);
        
        Iterable<Edge> getSortedEdges(Node node);

	Iterable<Edge> getEdges();
        
        Iterable<Edge> get(Node pred);

	Iterable<Edge> getEdges(Node pred, Node node);
        
        Iterable<Edge> getEdges(Node pred, Node node, int position);

	Iterable<Edge> getEdges(Node pred, Node node, Node node2);
        
        NodeManager getNodeManager();
        
        void finishUpdate();
                	
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
